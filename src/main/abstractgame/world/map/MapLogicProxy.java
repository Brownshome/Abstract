package abstractgame.world.map;

import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.CodeSource;
import java.security.PermissionCollection;
import java.security.Policy;
import java.util.HashMap;
import java.util.Map;

import abstractgame.Server;
import abstractgame.io.config.ConfigFile;
import abstractgame.io.config.Decoder;
import abstractgame.net.ServerProxy;
import abstractgame.net.packet.SpawnTimerPacket;
import abstractgame.security.GamePolicy;
import abstractgame.util.ApplicationException;
import abstractgame.world.World;
import abstractgame.world.entity.Player;

/** This defines an interface that allows maps to have
 * complex logic added to them */
public interface MapLogicProxy {
	/** Holds the creators for the different types of map logic */
	Map<String, Decoder<MapLogicProxy>> DECODERS = new HashMap<>();
	
	URLClassLoader MAP_CLASSLOADER = createClassLoader();
	
	static URLClassLoader createClassLoader() {
		try {
			return new URLClassLoader(new URL[] { Paths.get(ConfigFile.CONFIG_PATH + World.MAP_FOLDER).toUri().toURL() }) {
				@Override
				protected PermissionCollection getPermissions(CodeSource cs) {
					return super.getPermissions(cs);
				}
			};
		} catch (MalformedURLException e) {
			throw new ApplicationException(e, "MAP LOADER");
		}
	}
	
	public static MapLogicProxy javaScript(Map<String, Object> data) {
		String clazz = (String) data.get("class");
		if(clazz == null) throw new ApplicationException("No class stated for java script type", "MAP LOADER");
		
		Class c = null;
		try {
			c = MAP_CLASSLOADER.loadClass(clazz);
		} catch (ClassNotFoundException e) {
			throw new ApplicationException("Script file " + clazz + ".class was not found" , e, "MAP LOADER");
		}
		
		try {
			return (MapLogicProxy) c.newInstance();
		} catch (InstantiationException | IllegalAccessException e) {
			throw new ApplicationException("Failed to intialize script: " + clazz, e, "MAP LOADER");
		} catch (ClassCastException cce) {
			throw new ApplicationException("Script \"" + clazz + "\" does not implement " + MapLogicProxy.class.getName(), cce, "MAP LOADER");
		}
	}
	
	/** A MapLogicProxy that has no logic, used for the "none" script type */
	MapLogicProxy NO_LOGIC = new MapLogicProxy() {
		@Override
		public void initialize(World world) {}

		@Override
		public void destroy(World world) {}
	};
	
	/** A MapLogicProxy that has the default logic, used for the "default" script type */
	MapLogicProxy DEFAULT_LOGIC = new MapLogicProxy() {};
	
	/** Allows the map to load add custom decoders, this is checked first so can
	 * be used to override exising decoders, returning null will use the existing
	 * decoder */
	default Decoder<MapObject> getDecoder(String type) {
		return null;
	}
	
	/** Called after the creation of the world so that complex logic
	 * can be added. Note that hooks such as decoders cannot be added
	 * here. The default implementation spawns the player with the default
	 * loadout at the spawn with ID: playerSpawn */
	default void initialize(World world) {
		PlayerSpawn spawn;
		
		try {
			spawn = (PlayerSpawn) world.getNamedObject("playerSpawn");
		} catch(ApplicationException | ClassCastException cce) {
			throw new ApplicationException("The loaded map does not override the initilaize method and has no \'playerSpawn\' object", cce, "WORLD");
		}
		
		world.setConnectHandler(id -> {
			Player player = Server.getPlayer(id);
			int timeLeft = spawn.spawn(player);
			
			Server.getConnection(id).send(new SpawnTimerPacket(timeLeft));
		});
	}
	
	/** Called when the map is unloaded so the map can clean up after itself */
	default void destroy(World world) {}
}
