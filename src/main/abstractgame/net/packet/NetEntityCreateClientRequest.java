package abstractgame.net.packet;

import java.nio.ByteBuffer;

import abstractgame.net.Identity;
import abstractgame.net.ServerNetHandler;
import abstractgame.net.ServerProxy;
import abstractgame.world.World;
import abstractgame.world.entity.NetworkEntity;

/** Sent from the client to the server when an entity is created from the client side */
public class NetEntityCreateClientRequest extends Packet {
	NetworkEntity entity;
	int typeID;
	int requestID;
	
	public NetEntityCreateClientRequest(ByteBuffer data) {
		typeID = data.getInt();
		requestID = data.getInt();
		entity = World.createNetworkEntity(typeID, data);
	}
	
	public NetEntityCreateClientRequest(NetworkEntity netEntity) {
		entity = netEntity;
		requestID = netEntity.getID();
		typeID = World.getNetworkEntityTypeID(netEntity);
	}

	@Override
	public void fill(ByteBuffer output) {
		output.putInt(typeID);
		output.putInt(requestID);
		entity.fillCreateData(output);
	}

	@Override
	public void handleServer(Identity id) {
		ServerNetHandler.createNetworkEntity(entity, id);
		ServerProxy.getCurrentServerProxy().getConnection().send(new NetEntityCreateAck(requestID, entity.getID()));
	}
	
	@Override
	public int getPayloadSize() {
		return entity.getCreateLength() + Integer.BYTES * 2;
	}

}
