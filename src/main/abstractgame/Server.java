package abstractgame;

import java.io.IOException;
import java.net.*;
import java.nio.ByteBuffer;
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
import abstractgame.net.packet.Packet;
import abstractgame.time.Clock;
import abstractgame.ui.GameScreen;
import abstractgame.util.ApplicationException;
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
	
	static boolean isRunning = false;
	static World world;
	/** Whether the server is running in the same jvm as the client */
	static boolean isInternal = true;
	static int tickInterval;
	
	static Thread mainServerThread;
	static List<Thread> threads = Collections.synchronizedList(new ArrayList<>());
	
	static List<Runnable> runnableList = Collections.synchronizedList(new ArrayList<>());
	
	public static void main(String[] args) {
		isRunning = true;
		isInternal = false;
		Server.startServer(args[0], true);
	}
	
	public static void startServer(ConfigFile file, boolean openConnection) {
		startServer(file, file.getProperty("port", 35565), openConnection);
	}
	
	/** Starts the server threads and reads the config file
	 * 
	 *  @param file The server {@link ConfigFile}*/
	public static void startServer(ConfigFile file, int port, boolean openConnection) {
		if(Common.isClientSide()) {
			mainServerThread = new Thread(() -> {
				startServer(file, port, openConnection);
				
				while(true) {
					runServerTick();
				}
			}, "SERVER-MAIN-THREAD");
			
			mainServerThread.setDaemon(true);
			
			threads.add(mainServerThread);
			isRunning = true;
			
			mainServerThread.start();
			
			return;
		}
		
		Console.inform("Started server main thread.", "THREAD ENGINE");
		
		SERVER_CLOCK.startTimer();
		name = file.getProperty("name", "Server");
		version = file.getProperty("version", "0.1.0");
		Server.port = port;
		tickInterval = file.getProperty("tickInterval", 50); //default of 20TPS
		mapIdentifier = file.getProperty("map", "LOCAL:testmap");
		world = new World(mapIdentifier);
		
		Thread serverNetThread = new Thread(() -> {
			if(openConnection)
				UDPConnection.startServerListener(port);
			
			while(true) {
				try {
					INBOUND_QUEUE.take().run();
				} catch (InterruptedException e) {}
			}
		}, 
		"SERVER-MEMORY-THREAD");
		
		threads.add(serverNetThread);
		
		serverNetThread.setDaemon(true);
		serverNetThread.start();
		
		Console.inform("Started server net thread.", "THREAD ENGINE");
	}
	
	public static void startServer(String file, boolean openConnection) {
		startServer(ConfigFile.getFile(SERVER_FOLDER + file), openConnection);
	}
	
	public static void startServer(String file, int port) {
		startServer(ConfigFile.getFile(SERVER_FOLDER + file), port, true);
	}
	
	public static Collection<Connection> getConnections() {
		return CONNECTIONS.values();
	}
	
	/** Regesteres a new connection
	 * 
	 *  @param id The client to connect to */
	public static void createConnection(Identity id, Connection c) {
		CONNECTIONS.put(id, c);
	}
	
	/** @return true if the server is running on the same jvm
	 * as the client */
	public static boolean isInternal() {
		return isInternal;
	}

	public static void closeServer() {
		isRunning = false;
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
			world.run();
		
		for(Connection c : getConnections())
			if(c instanceof UDPConnection)
				((UDPConnection) c).tick();
		
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
		
		if(c == null)
			throw new ApplicationException("That client \'" + id + "\' has no active connection", "NET");
		
		return c;
	}

	public static Collection<Identity> getConnectedIds() {
		return CONNECTIONS.keySet();
	}

	public static Player getPlayer(Identity id) {
		assert Common.isServerSide();
		
		return PLAYERS.computeIfAbsent(id, i -> new Player(i));
	}
	
	public static void addTask(Runnable r) {
		runnableList.add(r);
	}

	public static int getTargetTPS() {
		assert Common.isServerSide();
		
		return 1000 / tickInterval;
	}

	public static List<Thread> getThreads() {
		return threads;
	}

	public static void addServerThread(Thread thread) {
		threads.add(thread);
	}

	public static boolean isRunning() {
		return isRunning;
	}
}
