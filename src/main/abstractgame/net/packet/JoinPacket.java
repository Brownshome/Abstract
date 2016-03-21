package abstractgame.net.packet;

import java.nio.ByteBuffer;

import abstractgame.Server;
import abstractgame.net.Identity;
import abstractgame.net.Side;
import abstractgame.util.Util;
import abstractgame.world.World;

/** Set to the server whenever a client joins a game */
public class JoinPacket extends Packet {
	public JoinPacket(ByteBuffer data) {}
	public JoinPacket() {}

	@Override
	public void fill(ByteBuffer output) {}

	@Override
	public void handleServer(Identity id) {
		Util.queueOnMainThread(() -> Server.getWorld().join(id));
	}
	
	@Override
	public int getPayloadSize() {
		return 0;
	}
}
