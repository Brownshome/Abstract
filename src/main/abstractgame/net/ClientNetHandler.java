package abstractgame.net;

import java.util.Map.Entry;

import abstractgame.Client;
import abstractgame.Server;
import abstractgame.net.packet.NetEntityCreateClientRequest;
import abstractgame.net.packet.NetEntityUpdatePacket;
import abstractgame.util.IDPool;
import abstractgame.util.Index;
import abstractgame.util.SlaveIndex;
import abstractgame.world.entity.NetworkEntity;

/** Handles the syncing of NetworkEntities on the client */
public class ClientNetHandler {
	static Index<NetworkEntity> requests = new Index<>();
	static SlaveIndex<NetworkEntity> index = new SlaveIndex<>();
	
	/** Sends the entity create request to the server and prepares for the response */
	public static void createNetworkEntity(NetworkEntity netEntity) {
		requests.add(netEntity);
		ServerProxy.getCurrentServerProxy().getConnection().send(new NetEntityCreateClientRequest(netEntity));
	}

	/** TODO not sync every frame */
	public static void syncEntities() {
		for(NetworkEntity ne : index) {
			if(ne.getController() != Client.getIdentity() || !ne.needsSync())
				continue;

			ServerProxy.getCurrentServerProxy().getConnection().send(new NetEntityUpdatePacket(ne));
		}
	}
	
	public static void populateID(int requestID, int entityID) {
		index.put(entityID, requests.get(requestID));
		requests.remove(requestID);
	}

	public static void populateID(int entityID, NetworkEntity entity) {
		index.put(entityID, entity);
	}

	public static NetworkEntity getNetworkEntity(int id) {
		return index.get(id);
	}
}
