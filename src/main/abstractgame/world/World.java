package abstractgame.world;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import javax.vecmath.Vector3f;

import abstractgame.Client;
import abstractgame.Common;
import abstractgame.Server;
import abstractgame.io.config.ConfigFile;
import abstractgame.io.config.ConfigFile.Policy;
import abstractgame.io.config.Decoder;
import abstractgame.io.user.Console;
import abstractgame.net.ClientNetHandler;
import abstractgame.net.Identity;
import abstractgame.net.ServerNetHandler;
import abstractgame.net.ServerProxy;
import abstractgame.net.Side;
import abstractgame.net.Sided;
import abstractgame.render.PhysicsRenderer;
import abstractgame.render.ServerPhysicsRenderer;
import abstractgame.util.ApplicationException;
import abstractgame.world.entity.Entity;
import abstractgame.world.entity.NetworkEntity;
import abstractgame.world.entity.PhysicsEntity;
import abstractgame.world.entity.Player;
import abstractgame.world.map.MapLogicProxy;
import abstractgame.world.map.MapObject;

import com.bulletphysics.collision.broadphase.DbvtBroadphase;
import com.bulletphysics.collision.dispatch.*;
import com.bulletphysics.dynamics.DiscreteDynamicsWorld;

public class World extends TickableImpl {
	public DiscreteDynamicsWorld physicsWorld;

	public static final String MAP_FOLDER = "../maps/";
	
	public static final Map<String, Decoder<MapObject>> DECODERS = new HashMap<>();
	
	static final Map<Class<? extends NetworkEntity>, Integer> NETIDS = new HashMap<>();
	static final List<Class<? extends NetworkEntity>> NETCLASSES = new ArrayList<>();
	static final List<Function<ByteBuffer, ? extends NetworkEntity>> CONSTRUCTORS = new ArrayList<>();
	
	public static int getNetworkEntityTypeID(NetworkEntity entity) {
		try {
			return NETIDS.get(entity.getClass());
		} catch(NullPointerException npe) {
			throw new ApplicationException("Unregestered NetworkEntity \'" + entity.getClass().getSimpleName() + "\'", "NET");
		}
	}
	
	/** This method returns null if there is no entity regestered to that ID */
	public static NetworkEntity getNetworkEntity(int id) {
		if(Common.isServerSide()) {
			return ServerNetHandler.getNetworkEntity(id);
		} else {
			return ClientNetHandler.getNetworkEntity(id);
		}
	}
	
	public static <T extends NetworkEntity> void regesterNetworkEntity(Class<T> type) {
		Constructor<T> c;
		try {
			c = type.getConstructor(ByteBuffer.class);
		} catch (NoSuchMethodException e) {
			throw new ApplicationException(type.getSimpleName() + " was not properly defined", e, "NET");
		} catch (SecurityException e) {
			throw new ApplicationException(e, "NET");
		}
		
		regesterNetworkEntity(type, (ByteBuffer b) -> { 
			try {
				return c.newInstance(b);
			} catch (IllegalAccessException | InstantiationException e) {
				throw new ApplicationException("NetworkEntity " + type.getSimpleName() + " is improperly setup", e, "NET");
			} catch(InvocationTargetException eiie) {
				throw new ApplicationException("Error creating NetworkEntity " + type.getSimpleName(), eiie.getCause(), "NET");
			}
		});
	}
	
	public static <T extends NetworkEntity> void regesterNetworkEntity(Class<T> type, Function<ByteBuffer, ? extends NetworkEntity> constructor) {
		if(NETIDS.put(type, NETCLASSES.size()) != null)
			throw new ApplicationException("NetworkEntity " + type.getSimpleName() + " is already regestered", "NET");
		
		CONSTRUCTORS.add(constructor);
		NETCLASSES.add(type);
	}
	
	public static NetworkEntity createNetworkEntity(int id, ByteBuffer data) {
		return CONSTRUCTORS.get(id).apply(data);
	}
	
	static enum Source {
		SERVER,
		WORKSHOP,
		LOCAL
	}

	String name;
	Source source;
	ConfigFile mapFile;
	MapLogicProxy logic;
	Map<String, MapObject> namedObjects = new HashMap<>();
	Consumer<Identity> connectionHandler = id -> {};
	
	float sizeX;
	float sizeY;
	
	public World(String identifier) {
		//TODO tune these paramaters
		physicsWorld = new DiscreteDynamicsWorld(new CollisionDispatcher(new DefaultCollisionConfiguration()), new DbvtBroadphase(), null, new DefaultCollisionConfiguration());
		physicsWorld.getPairCache().setInternalGhostPairCallback(new GhostPairCallback());
		
		if(Common.isClientSide())
			physicsWorld.setDebugDrawer(PhysicsRenderer.INSTANCE);
		else if(Server.isInternal())
			physicsWorld.setDebugDrawer(ServerPhysicsRenderer.INSTANCE);
		
		String[] split = identifier.split(":");
		
		name = split[1];
		source = Source.valueOf(split[0]);
		
		switch(source) {
			case SERVER:
			case WORKSHOP:
			case LOCAL:
				//TODO
				mapFile = ConfigFile.getFile(MAP_FOLDER + name, Policy.NO_WRITE);
		}
		
		List<Number> size = mapFile.getProperty("size", Arrays.asList(100d, 100d), List.class);
		sizeX = size.get(0).floatValue();
		sizeY = size.get(1).floatValue();
		
		String scriptType = mapFile.getProperty("script.type", "none");
		Decoder<MapLogicProxy> d = MapLogicProxy.DECODERS.get(scriptType);
		if(d == null) throw new ApplicationException("Script type \"" + scriptType + "\" does not exist", "MAP LOADER");
		logic = d.apply(mapFile.getPropertyNoDefault("script", Map.class));
		
		List<Map<String, Object>> objects = mapFile.getProperty("objects", Collections.EMPTY_LIST, List.class);
		
		objects.stream().map(o -> {
			String type = (String) o.get("type");
			if(type == null) throw new ApplicationException("Map objects must have a type field", "MAP LOADER");
			
			Decoder<MapObject> decoder = DECODERS.get(type);
			if(decoder == null) throw new ApplicationException("There is no decoder regestered for type \"" + type + "\"", "MAP LOADER");
			
			MapObject object = null;
			object = decoder.apply(o);	
			object.setID((String) o.get("ID"));
			
			return object;
		}).forEach(o -> {
			if(o.getID() != null)
				if(namedObjects.put(o.getID(), o) != null)
					throw new ApplicationException("Duplicate map object IDs " + o.getID(), "MAP LOADER");
			
			o.addToWorld(this);
		});
		
		logic.initialize(this);
	}
	
	@Override
	public void run() {
		physicsWorld.stepSimulation(Common.getClock().getDelta(), Common.isServerSide() ? 15 : 7);
		
		if(Common.isServerSide()) {
			ServerPhysicsRenderer.INSTANCE.reset();
		}
		
		super.run();
		
		physicsWorld.debugDrawWorld();
		
		sync();
	}
	
	void sync() {
		if(Common.isServerSide()) {
			ServerNetHandler.syncEntities();
		} else {
			ClientNetHandler.sendUserInput();
		}
	}
	
	public ConfigFile getMapFile() {
		return mapFile;
	}
	
	public MapObject getNamedObject(String ID) {
		MapObject m;
		if((m = namedObjects.get(ID)) == null)
			throw new ApplicationException("There is no object named \'" + ID + "\'", "WORLD");
		
		return m;
	}
	
	public String getMapIdentifier() {
		return source.toString() + ":" + name;
	}

	/** Calls the hooks for the logic proxy to unload */
	public void cleanUp() {
		logic.destroy(this);
	}

	/** Called whenever someone joins the server */
	public void join(Identity id) {
		Console.inform("Player " + id.username + " ( " + id + " ) joined the game", "WORLD");
		
		if(Common.isServerSide())
			ServerNetHandler.sendEntities(id);
		
		connectionHandler.accept(id);
	}
	
	public Consumer<Identity> getConnectionHandler() {
		return connectionHandler;
	}
	
	/** The handler is called on the server side only */
	public void setConnectHandler(Consumer<Identity> handler) {
		connectionHandler = handler;
	}

	public void addEntity(Entity entity) {
		assert !(entity instanceof PhysicsEntity) || !((PhysicsEntity) entity).getRigidBody().isInWorld() : "Entity already added";
		
		entity.onAddedToWorld(this);
	}
}
