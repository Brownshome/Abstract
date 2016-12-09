package abstractgame;

import java.security.Policy;
import java.util.*;

import abstractgame.io.config.ConfigFile;
import abstractgame.io.model.*;
import abstractgame.io.model.ModelLoader.LoadType;
import abstractgame.io.user.Console;
import abstractgame.mod.ModManager;
import abstractgame.net.Identity;
import abstractgame.net.packet.*;
import abstractgame.security.GamePolicy;
import abstractgame.time.Clock;
import abstractgame.ui.GameScreen;
import abstractgame.world.World;
import abstractgame.world.entity.Player;
import abstractgame.world.map.MapLogicProxy;
import abstractgame.world.map.PlayerSpawn;
import abstractgame.world.map.StaticMapObjectClient;

/** Holds methods called on both the server and client */
public class Common {
	public static final ConfigFile GLOBAL_CONFIG = ConfigFile.getFile("globalConfig");
	
	public static void loadHooks() {
		World.DECODERS.put("static", StaticMapObjectClient::creator);
		World.DECODERS.put("spawn", PlayerSpawn::creator);
		
		PhysicsMeshLoader.DECODERS.put("static mesh", PhysicsMeshLoader::decodeStaticMesh);
		PhysicsMeshLoader.DECODERS.put("box", PhysicsMeshLoader::decodeBox);
		PhysicsMeshLoader.DECODERS.put("sphere", PhysicsMeshLoader::decodeSphere);
		PhysicsMeshLoader.DECODERS.put("capsule", PhysicsMeshLoader::decodeCapsule);
		PhysicsMeshLoader.DECODERS.put("cylinder", PhysicsMeshLoader::decodeCylinder);
		PhysicsMeshLoader.DECODERS.put("cone", PhysicsMeshLoader::decodeCone);
		PhysicsMeshLoader.DECODERS.put("convex hull", PhysicsMeshLoader::decodeConvexHull);
		PhysicsMeshLoader.DECODERS.put("triangle", PhysicsMeshLoader::decodeTriangle);
		PhysicsMeshLoader.DECODERS.put("plane", PhysicsMeshLoader::decodePlane);
		PhysicsMeshLoader.DECODERS.put("compound", PhysicsMeshLoader::decodeCompound);
		PhysicsMeshLoader.DECODERS.put("external", PhysicsMeshLoader::decodeExternal);
		
		MapLogicProxy.DECODERS.put("none", m -> MapLogicProxy.NO_LOGIC);
		MapLogicProxy.DECODERS.put("default", m -> MapLogicProxy.DEFAULT_LOGIC);
		MapLogicProxy.DECODERS.put("java", MapLogicProxy::javaScript);
		
		Packet.regesterPacket(QueryPacket.class);
		Packet.regesterPacket(QueryResponsePacket.class);
		Packet.regesterPacket(JoinPacket.class);
		Packet.regesterPacket(SpawnTimerPacket.class);
		Packet.regesterPacket(NetEntityCreatePacket.class);
		Packet.regesterPacket(AckPacket.class);
		Packet.regesterPacket(NetEntityUpdatePacket.class);
		Packet.regesterPacket(FragmentPacket.class);
		
		World.regesterNetworkEntity(Player.class);
		
		ModManager.loadHooks();
	}

	public static void setupSecurity() {
		Policy.setPolicy(new GamePolicy());
		System.setSecurityManager(new SecurityManager());
	}

	/** @return the {@link World} object, taking into accout whether the */
	public static World getWorld() {
		return Common.isServerSide() ? Server.getWorld() : GameScreen.getWorld();
	}

	public static Clock getClock() {
		return Common.isServerSide() ? Server.SERVER_CLOCK : Client.GAME_CLOCK;
	}

	/** Returns true if the current thread is a server thread */
	public static boolean isServerSide() {
		if(!Server.isRunning())
			return false;
		
		if(!Server.isInternal)
			return true;
		
		Thread current = Thread.currentThread();
		return Server.getThreads().contains(current);
	}
	
	/** Returns true if the current thread is not a server thread */
	public static boolean isClientSide() {
		return !isServerSide();
	}

	/** Returns either the client ID or null on the server side */
	public static Identity getIdentity() {
		return isServerSide() ? null : Client.getIdentity();
	}

	public static void preLoadPhysicsModels() {
		for(String name : GLOBAL_CONFIG.getProperty("pre-load.pys", (List<String>) Collections.EMPTY_LIST))
			ModelLoader.preLoadModel(name, LoadType.LOAD_OBJ, LoadType.CREATE_PHYS);
	}

	static void setupErrorHandlingAndLogging() {
		Thread.setDefaultUncaughtExceptionHandler(Console::error);
		Console.setLevel(GLOBAL_CONFIG.getProperty("logging.level", 0));
		Console.setFormat(GLOBAL_CONFIG.getProperty("logging.format", "HH:mm:ss"));
	}
}
