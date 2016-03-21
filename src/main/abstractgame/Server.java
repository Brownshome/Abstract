package abstractgame;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.SynchronousQueue;

import abstractgame.io.config.ConfigFile;
import abstractgame.io.user.Console;
import abstractgame.net.Connection;
import abstractgame.net.Identity;
import abstractgame.net.MemoryConnection;
import abstractgame.net.UDPConnection;
import abstractgame.time.Clock;
import abstractgame.ui.GameScreen;
import abstractgame.world.World;
import abstractgame.world.entity.Player;

/* The current server communication is as follows
 * 
 * Server		Client
 * 				QueryPacket
 * InfoPacket
 * 
 * 		.....
 */

/** This is the main class for the server */
public class Server {
	public static final String SERVER_FOLDER = "server/";
	
	public static final BlockingQueue<Runnable> INBOUND_QUEUE = new SynchronousQueue<>();
	public static final Map<Identity, Connection> CONNECTIONS = new HashMap<>();
	
	static final Map<Identity, Player> PLAYERS = new HashMap<>();

	public static final Clock SERVER_CLOCK = new Clock();
	
	public static String name;
	public static int port;
	public static String version;
	public static String mapIdentifier;
	
	static World world;
	/** Whether the server is running in the same jvm as the client */
	static boolean isInternal = true;
	static int tickInterval;
	
	static Thread serverNetThread;
	static Thread mainServerThread;
	
	static List<Runnable> runnableList = Collections.synchronizedList(new ArrayList<>());
	
	public static void main(String[] args) {
		isInternal = false;
		Server.startServer(args[0]);
	}
	
	/** Starts the server threads and reads the config file */
	public static void startServer(ConfigFile file) {
		if(!Server.isSeverSide()) {
			mainServerThread = new Thread(() -> {
				startServer(file);
				
				while(true) {
					runServerTick();
				}
			}, "SERVER-MAIN-THREAD");
			
			mainServerThread.setDaemon(true);
			mainServerThread.start();
			
			return;
		}
		
		Console.inform("Started server main thread.", "THREAD ENGINE");
		
		SERVER_CLOCK.startTimer();
		name = file.getProperty("name", "Server");
		version = file.getProperty("version", "0.1.0");
		port = file.getProperty("port", 35565);
		tickInterval = file.getProperty("tickInterval", 50); //default of 20TPS
		mapIdentifier = file.getProperty("map", "LOCAL:testmap");
		world = new World(mapIdentifier);
		
		serverNetThread = new Thread(() -> {
			while(true) {
				try {
					INBOUND_QUEUE.take().run();
				} catch (InterruptedException e) {}
			}
		}, "SERVER-NET-THREAD");
		serverNetThread.setDaemon(true);
		serverNetThread.start();
		
		Console.inform("Started server net thread.", "THREAD ENGINE");
	}
	
	public static void startServer(String file) {
		startServer(ConfigFile.getFile(SERVER_FOLDER + file));
	}
	
	public static Collection<Connection> getConnections() {
		return CONNECTIONS.values();
	}
	
	/** creates a new connection */
	public static Connection createConnection(Identity id) {
		return Server.isInternal()
				? new MemoryConnection(Client.getInboundQueue(), false)
				: new UDPConnection(id);
	}
	
	/** returns true if the server is running on the same jvm
	 * as the client */
	public static boolean isInternal() {
		return isInternal;
	}

	public static void closeServer() {
		
	}
	
	private static void doTimings() {
		int delta;
		while((delta = (int) (SERVER_CLOCK.getTime() - SERVER_CLOCK.getLastTick())) < tickInterval) {
			try {
				Thread.sleep(tickInterval - delta);
			} catch (InterruptedException e) {}
		}

		if(SERVER_CLOCK.getAverageDelta() > tickInterval * 5)
			Console.warn("Server thread overloaded, average delta = " + SERVER_CLOCK.getAverageDelta(), "SERVER");

		SERVER_CLOCK.tick();
	}
	
	public static void runServerTick() {
		synchronized(runnableList) {
			runnableList.forEach(Runnable::run);
			runnableList.clear();
		}
		
		if(getWorld() != null)
			world.tick();
		
		doTimings();
	}
	
	public static World getWorld() {
		return world;
	}
	
	public static BlockingQueue<Runnable> getInboundPacketQueue() {
		return INBOUND_QUEUE;
	}

	public static Connection getConnection(Identity id) {
		Connection c = CONNECTIONS.get(id);
		if(c == null) {
			c = createConnection(id);
			CONNECTIONS.put(id, c);
		}
		
		return c;
	}

	public static Collection<Identity> getConnectedIds() {
		return CONNECTIONS.keySet();
	}

	public static Player getPlayer(Identity id) {
		return PLAYERS.computeIfAbsent(id, i -> new Player(i));
	}
	
	/** Returns true if the current thread is a server thread */
	public static boolean isSeverSide() {
		if(!isInternal)
			return true;
		
		Thread current = Thread.currentThread();
		return current == serverNetThread || current == mainServerThread;
	}

	public static void addTask(Runnable r) {
		runnableList.add(r);
	}

	public static int getTargetTPS() {
		return 1000 / tickInterval;
	}
}
