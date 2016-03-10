package abstractgame.world;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import abstractgame.io.config.ConfigFile;
import abstractgame.world.entity.DynamicEntity;

import com.bulletphysics.dynamics.DiscreteDynamicsWorld;

/** Represents a game world, when the game is running in embeded server mode there is only one copy
 * held by both the server and client sides */
public class World extends TickableImpl {
	DiscreteDynamicsWorld physicsWorld;

	public static final String MAP_FOLDER = "maps/";
	
	static final Map<String, MapObjectReader> CREATORS = new HashMap<>();
	
	/** No check is made for duplicate entries */
	public static void regesterObjectType(String name, MapObjectReader creator) {
		CREATORS.put(name, creator);
	}
	
	public static boolean isRegestered(String name) {
		return CREATORS.containsKey(name);
	}
	
	static enum Source {
		SERVER,
		WORKSHOP,
		LOCAL
	}

	String name;
	Source source;
	ConfigFile mapFile;

	float sizeX;
	float sizeY;

	List<MapObject> mapObjects = new ArrayList<>();

	public World(String identifier) {
		String[] split = identifier.split(":");
		
		name = split[1];
		source = Source.valueOf(split[0]);
		
		switch(source) {
			case SERVER:
			case WORKSHOP:
			case LOCAL:
				//TODO
				mapFile = ConfigFile.getFile(MAP_FOLDER + name);
		}
		
		List<Double> size = mapFile.getProperty("size", Arrays.asList(100d, 100d), List.class);
		sizeX = size.get(0).floatValue();
		sizeY = size.get(1).floatValue();
		
		List<Map<String, Object>> objects = mapFile.getProperty("objects", Collections.EMPTY_LIST, List.class);
		
		mapObjects = objects.stream().map(o -> CREATORS.get(o.get("type")).apply(o)).collect(Collectors.toList());
		mapObjects.forEach(o -> o.addToWorld(this));
	}
	
	public String getMapIdentifier() {
		return source.toString() + ":" + name;
	}
	
	/** Adds the entity to the physics grid, this will not add it to the tick counter */
	public void addDynamicEntity(DynamicEntity entity) {
		physicsWorld.addRigidBody(entity);
	}
}
