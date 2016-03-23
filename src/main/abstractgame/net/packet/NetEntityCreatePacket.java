package abstractgame.net.packet;

import java.nio.ByteBuffer;

import abstractgame.net.ClientNetHandler;
import abstractgame.net.Side;
import abstractgame.ui.GameScreen;
import abstractgame.util.Util;
import abstractgame.world.World;
import abstractgame.world.entity.NetworkEntity;

/** Sent from the server to the client when an entity is created */
public class NetEntityCreatePacket extends Packet {
	NetworkEntity entity;
	int typeID;
	int entityID;
	
	public NetEntityCreatePacket(ByteBuffer data) {
		typeID = data.getInt();
		entityID = data.getInt();
		entity = World.createNetworkEntity(typeID, data);
	}
	
	/** entity.getID() must hold the entityID of the entity */
	public NetEntityCreatePacket(NetworkEntity entity) {
		this.entity = entity;
		entityID = entity.getID();
		typeID = World.getNetworkEntityTypeID(entity);
	}
	
	@Override
	public void fill(ByteBuffer output) {
		output.putInt(typeID);
		output.putInt(entityID);
		entity.fillCreateData(output);
	}

	@Override
	public void handleClient() {
		Util.queueOnMainThread(() -> {
			ClientNetHandler.populateID(entityID, entity);
			entity.initialize();
		});
	}
	
	@Override
	public int getPayloadSize() {
		return entity.getCreateLength() + Integer.BYTES * 2;
	}

}
