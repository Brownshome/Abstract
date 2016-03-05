package abstractgame;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.SynchronousQueue;

import abstractgame.io.config.ConfigFile;
import abstractgame.io.user.Console;
import abstractgame.net.Connection;
import abstractgame.net.Identity;
import abstractgame.net.MemoryConnection;
import abstractgame.net.UDPConnection;

/* The current server communication is as follows
 * 
 * Server		Client
 * 				QueryPacket
 * InfoPacket
 * 				ClientInfoPacket
 * 
 * 		.....
 */

/** This is the main class for the server */
public class Server {
	public static final String SERVER_FOLDER = "server/";
	
	public static final BlockingQueue<Runnable> INBOUND_QUEUE = new SynchronousQueue<>();
	public static final Map<Identity, Connection> CONNECTIONS = new HashMap<>();
	
	public static String name;
	public static int port;
	public static String version;
	static boolean isInternal = true;
	
	public static void main(String[] args) {
		isInternal = false;
		Server.startServer(args[0]);
	}
	
	public static void startServer(ConfigFile file) {
		name = file.getProperty("name", "Server");
		version = file.getProperty("version", "0.1.0");
		port = file.getProperty("port", 35565);
		
		Thread netThread = new Thread(() -> {
			while(true) {
				try {
					INBOUND_QUEUE.take().run();
				} catch (Exception e) {}
			}
		}, "SERVER-NET-THREAD");
		netThread.setDaemon(true);
		netThread.start();
		
		Console.inform("Started server net thread.", "THREAD ENGINE");
	}
	
	public static void startServer(String file) {
		startServer(ConfigFile.getFile(SERVER_FOLDER + file));
	}
	
	/** creates a new connection */
	public static Connection createConnection(Identity id) {
		return Server.isInternal()
				? new MemoryConnection(Client.getInboundQueue(), false)
				: new UDPConnection(id);
	}
	
	private static boolean isInternal() {
		return isInternal;
	}

	public static void closeServer() {
		
	}
	
	public static void runServerTick() {
		
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
}
