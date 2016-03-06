package abstractgame.net;

import java.net.InetAddress;

import abstractgame.Client;
import abstractgame.io.user.Console;
import abstractgame.util.ApplicationException;

/** This represents the server as viewed from one of the clients */
@Sided(Side.CLIENT)
public abstract class ServerProxy {
	static ServerProxy INSTANCE;
	
	String serverVersion;
	String mapIdentifier;
	Identity connected;
	
	private static void checkNoServerRunning() {
		if(INSTANCE != null)
			throw new ApplicationException("Server already running", "NET");
	}
	
	public static void startPrivateServer(String config) {
		checkNoServerRunning();
		
		INSTANCE = new InternalServerProxy(config);
	}
	
	public static void startIntegratedServer(int port, String config) {
		checkNoServerRunning();
		
		assert false : "Not yet implemented";
	}
	
	public static void connectToServer(InetAddress address, int port) {
		assert false : "Not yet implemented";
	}
	
	public static ServerProxy getCurrentServerProxy() {
		return INSTANCE;
	}
	
	public static void startClientNetThread() {
		Thread ioThread = new Thread(() -> {
			while(true) {
				try {
					Client.inboundPackets.take().run();
				} catch (Exception e) {}
			}
		}, "CLIENT-NET-THREAD");
		ioThread.setDaemon(true);
		ioThread.start();
		
		Console.inform("Started client net thread.", "THREAD ENGINE");
	}
	
	public void setVersion(String version) {
		serverVersion = version;
	}
	
	public void setServerInfo(String worldIdentifier, long[] ids) {
		// TODO Auto-generated method stub
		
	}
	
	public abstract String getName();
	public abstract Connection getConnection();
}
