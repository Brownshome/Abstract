package abstractgame.net.packet;

import java.nio.ByteBuffer;

import abstractgame.net.ClientNetHandler;
import abstractgame.util.Util;

/** This is sent from the server to the client to confirm the ID of an entity
 * created on the client in response to NetEntityCreateClientRequest */
public class NetEntityCreateAck extends Packet {
	int requestID;
	int entityID;
	
	public NetEntityCreateAck(int requestID, int entityID) {
		this.requestID = requestID;
		this.entityID = entityID;
	}
	
	public NetEntityCreateAck(ByteBuffer data) {
		requestID = data.getInt();
		entityID = data.getInt();
	}
	
	@Override
	public void fill(ByteBuffer output) {
		output.putInt(requestID);
		output.putInt(entityID);
	}

	@Override
	public void handleClient() {
		Util.queueOnMainThread(() -> ClientNetHandler.populateID(requestID, entityID));
	}
	
	@Override
	public int getPayloadSize() {
		return Integer.BYTES * 2;
	}

}
