package abstractgame.net;

import java.net.InetAddress;

import abstractgame.io.user.Console;
import abstractgame.ui.GameScreen;
import abstractgame.util.ApplicationException;
import abstractgame.world.World;

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
	
	public void setVersion(String version) {
		serverVersion = version;
	}
	
	public void setServerInfo(String version, String worldIdentifier, int[] ids) {
		mapIdentifier = worldIdentifier;
		serverVersion = version;
		
		if(GameScreen.getWorld() == null || GameScreen.getWorld().getMapIdentifier() != mapIdentifier) {
			Console.inform("Changing world to: " + mapIdentifier, "WORLD");
			GameScreen.setWorld(new World(mapIdentifier));
		}
	}
	
	public abstract String getName();
	public abstract Connection getConnection();
}
