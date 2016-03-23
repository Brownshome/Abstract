package abstractgame.net.packet;

import java.nio.ByteBuffer;

import abstractgame.Server;
import abstractgame.net.Identity;
import abstractgame.world.World;
import abstractgame.world.entity.NetworkEntity;

public class NetEntityUpdatePacket extends Packet {
	NetworkEntity entity;
	ByteBuffer buffer;
	
	public NetEntityUpdatePacket(ByteBuffer buffer) {
		int id = buffer.getInt();
		entity = World.getNetworkEntity(id);
		
		//copying for thread safety
		this.buffer = ByteBuffer.allocate(buffer.remaining());
		this.buffer.put(buffer);
		this.buffer.flip();
	}

	public NetEntityUpdatePacket(NetworkEntity ne) {
		entity = ne;
	}

	@Override
	public void fill(ByteBuffer output) {
		output.putInt(entity.getID());
		entity.fillStateUpdate(output);
	}

	@Override
	public void handle(Identity id) {
		if(entity == null)
			return;
		
		if(id == null || id == entity.getController()) {
			entity.updateState(buffer);
		}
	}
	
	@Override
	public int getPayloadSize() {
		return entity.getStateUpdateLength() + Integer.BYTES;
	}
}