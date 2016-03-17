package abstractgame.world.map;

import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import abstractgame.io.config.ConfigFile;
import abstractgame.io.config.Decoder;
import abstractgame.util.ApplicationException;
import abstractgame.world.World;

/** This defines an interface that allows maps to have
 * complex logic added to them */
public interface MapLogicProxy {
	/** Holds the creators for the different types of map logic */
	Map<String, Decoder<MapLogicProxy>> DECODERS = new HashMap<>();
	
	/** The classloader has been edited so that calls to find abstractgame.maps.CLASSNAME search the maps folder without
	 * the abstractgame.maps. header */
	URLClassLoader MAP_CLASSLOADER = createClassLoader();
	public static final String HEADER = "abstractgame.maps.";
	
	static URLClassLoader createClassLoader() {
		try {
			return new URLClassLoader(new URL[] { Paths.get(ConfigFile.CONFIG_PATH + World.MAP_FOLDER).toUri().toURL() }) {
				@Override
				protected Class<?> findClass(final String string) throws ClassNotFoundException {
					int i = string.indexOf(HEADER);
					
					if(i != -1)
						return super.findClass(string.substring(i + HEADER.length()));
					else
						return super.findClass(string);
						
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
	
	/** A MapLogicProxy that has no logic, used for the \"none\" script type */
	MapLogicProxy NO_LOGIC = new MapLogicProxy() {
		@Override
		public void initialize(World world) {}

		@Override
		public void destroy(World world) {}
	};
	
	/** Called after the creation of the world so that complex logic
	 * can be added. Note that hooks such as decoders cannot be added
	 * here. */
	void initialize(World world);
	
	/** Called when the map is unloaded so the map can clean up after itself */
	void destroy(World world);
}