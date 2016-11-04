package abstractgame.net;

import java.util.Map.Entry;

import abstractgame.Client;
import abstractgame.Server;
import abstractgame.net.packet.*;
import abstractgame.ui.GameScreen;
import abstractgame.util.IDPool;
import abstractgame.util.Index;
import abstractgame.util.SlaveIndex;
import abstractgame.world.entity.NetworkEntity;

/** Handles synchronization from the server on the client and sending of user input */
public class ClientNetHandler {
	static Index<NetworkEntity> phantomEntities = new Index<>();
	static SlaveIndex<NetworkEntity> index = new SlaveIndex<>();
	
	/** Creates a phantom entity, this is an entity that may or may not exist on the server but is used for client side prediction */
	public static void createPhantomEntity(NetworkEntity netEntity) {
		phantomEntities.add(netEntity);
		ServerProxy.getCurrentServerProxy().getConnection().send(new NetEntityCreateClientRequest(netEntity));
	}

	/** Sends user input to the server */
	public static void sendUserInput() {
		if(GameScreen.getPlayerEntity() != null)
			GameScreen.getPlayerEntity().sendUserInputs();
	}
	
	public static void populateID(int requestID, int entityID) {
		index.put(entityID, phantomEntities.get(requestID));
		phantomEntities.remove(requestID);
	}

	public static void populateID(int entityID, NetworkEntity entity) {
		index.put(entityID, entity);
	}

	public static NetworkEntity getNetworkEntity(int id) {
		return index.get(id);
	}
}
