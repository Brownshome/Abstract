package abstractgame;

import java.security.Policy;

import abstractgame.io.model.PhysicsMeshLoader;
import abstractgame.net.packet.JoinPacket;
import abstractgame.net.packet.Packet;
import abstractgame.net.packet.QueryPacket;
import abstractgame.net.packet.QueryResponsePacket;
import abstractgame.net.packet.SpawnTimerPacket;
import abstractgame.security.GamePolicy;
import abstractgame.world.World;
import abstractgame.world.map.MapLogicProxy;
import abstractgame.world.map.PlayerSpawn;
import abstractgame.world.map.StaticMapObjectClient;

/** Holds methods called on both the server and client */
public class Common {
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
	}

	public static void setupSecurity() {
		Policy.setPolicy(new GamePolicy());
		System.setSecurityManager(new SecurityManager());
	}
}
