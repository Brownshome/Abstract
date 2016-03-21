package abstractgame.net.packet;

import java.nio.ByteBuffer;

import abstractgame.net.Side;
import abstractgame.ui.GameScreen;
import abstractgame.world.World;
import abstractgame.world.entity.NetworkEntity;

public class EntitySpawnPacket extends Packet {
	NetworkEntity entity;
	int ID;
	
	public EntitySpawnPacket(ByteBuffer data) {
		int ID = data.getInt();
		entity = World.createNetworkEntity(ID, data);
	}
	
	public EntitySpawnPacket(NetworkEntity entity) {
		this.entity = entity;
		ID = World.getNetworkEntityID(entity);
	}
	
	@Override
	public void fill(ByteBuffer output) {
		output.putInt(ID);
		entity.fillCreateData(output);
	}

	@Override
	public void handleClient() {
		if(GameScreen.getWorld() != null)
			GameScreen.getWorld().addEntity(entity);
	}
	
	@Override
	public int getPayloadSize() {
		return entity.getCreateLength() + Integer.BYTES;
	}

}
