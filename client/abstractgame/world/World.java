package abstractgame.world;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.vecmath.Vector3f;

import abstractgame.Client;
import abstractgame.io.config.ConfigFile;
import abstractgame.io.config.Decoder;
import abstractgame.render.PhysicsRenderer;
import abstractgame.util.ApplicationException;
import abstractgame.world.entity.DynamicEntity;

import com.bulletphysics.collision.broadphase.DbvtBroadphase;
import com.bulletphysics.collision.dispatch.CollisionDispatcher;
import com.bulletphysics.collision.dispatch.DefaultCollisionConfiguration;
import com.bulletphysics.dynamics.DiscreteDynamicsWorld;

/** Represents a game world, when the game is running in embeded server mode there is only one copy
 * held by both the server and client sides */
public class World extends TickableImpl {
	public DiscreteDynamicsWorld physicsWorld;

	public static final String MAP_FOLDER = "../maps/";
	
	public static final Map<String, Decoder<MapObject>> DECODERS = new HashMap<>();
	
	static enum Source {
		SERVER,
		WORKSHOP,
		LOCAL
	}

	String name;
	Source source;
	ConfigFile mapFile;
	Map<String, MapObject> namedObjects = new HashMap<>();

	float sizeX;
	float sizeY;
	
	public World(String identifier) {
		
		//TODO tune these paramaters
		physicsWorld = new DiscreteDynamicsWorld(new CollisionDispatcher(new DefaultCollisionConfiguration()), new DbvtBroadphase(), null, new DefaultCollisionConfiguration());
		physicsWorld.setDebugDrawer(PhysicsRenderer.INSTANCE);
		physicsWorld.setGravity(new Vector3f(0, -0.1f, 0));
		
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
		
		objects.stream().map(o -> {
			MapObject object = DECODERS.get(o.get("type")).apply(o);
			object.setID((String) o.get("ID"));
			return object;
		}).forEach(o -> {
			if(o.getID() != null)
				if(namedObjects.put(o.getID(), o) != null)
					throw new ApplicationException("Duplicate map object names: " + o.getID(), "MAP LOADER");
			
			o.addToWorld(this);
		});
	}
	
	@Override
	public void tick() {
		super.tick();
		
		physicsWorld.debugDrawWorld();
		physicsWorld.stepSimulation(Client.GAME_CLOCK.getDelta(), 7);
	}
	
	public String getMapIdentifier() {
		return source.toString() + ":" + name;
	}
	
	/** Adds the entity to the physics grid, this will not add it to the tick counter */
	public void addDynamicEntity(DynamicEntity entity) {
		physicsWorld.addRigidBody(entity);
	}
}
