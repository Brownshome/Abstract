package abstractgame.net;

import java.util.Map.Entry;

import abstractgame.Common;
import abstractgame.Server;
import abstractgame.net.packet.NetEntityCreatePacket;
import abstractgame.net.packet.NetEntityUpdatePacket;
import abstractgame.util.Index;
import abstractgame.world.entity.NetworkEntity;

/** Handles the syncing of NetworkEntities on the server */
public class ServerNetHandler {
	static Index<NetworkEntity> index = new Index<>();

	/** Adds networkEntity to the internal pool of entities and sends the creation message to all clients */
	public static void createNetworkEntity(NetworkEntity networkEntity) {
		assert Common.isServerSide();

		index.add(networkEntity);
		Connection.sendToAll(new NetEntityCreatePacket(networkEntity), Server.getConnections());
	}
	
	/** Adds networkEntity to the internal pool of entities and sends the creation message to all clients
	 * baring the one that created it */
	public static void createNetworkEntity(NetworkEntity entity, Identity id) {
		assert Common.isServerSide();
		
		index.add(entity);
		Connection.sendToAll(new NetEntityCreatePacket(entity), Server.CONNECTIONS.entrySet()
				.stream()
				.filter(e -> e.getKey() != id)
				.map(Entry::getValue)::iterator);
	}
	
	public static void syncEntities() {
		for(NetworkEntity ne : index) {
			if(ne.getController() != null || !ne.needsSync())
				continue;

			NetEntityUpdatePacket packet = new NetEntityUpdatePacket(ne);

			Connection.sendToAll(packet, Server.CONNECTIONS.entrySet()
					.stream()
					.filter(entry -> ne.needsSyncTo(entry.getKey()))
					.map(Entry::getValue)::iterator);
		}
	}

	public static NetworkEntity getNetworkEntity(int id) {
		return index.get(id);
	}
}
