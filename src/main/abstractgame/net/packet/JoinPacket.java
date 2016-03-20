package abstractgame.net.packet;

import abstractgame.Server;
import abstractgame.net.Identity;
import abstractgame.net.Side;
import abstractgame.world.World;

/** Set to the server whenever a client joins a game */
public class JoinPacket extends Packet {
	public JoinPacket(byte[] data) {}
	public JoinPacket() {}

	@Override
	public void fill(byte[] data, int offset) {}

	@Override
	public void handleServer(Identity id) {
		Server.getWorld().join(id);
	}
	
	@Override
	public Side getHandleSide() {
		return Side.SERVER;
	}

}
