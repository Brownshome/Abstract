package abstractgame.net.packet;

import abstractgame.Server;
import abstractgame.net.Identity;
import abstractgame.net.Side;

/** Starts retrieves information about the server */
public class QueryPacket extends Packet {
	public QueryPacket(byte[] data) {}
	
	public QueryPacket() {}

	@Override
	public void handle(Identity id) {
		Server.getConnection(id).send(new QueryResponsePacket());
	}

	@Override
	public void fill(byte[] data, int offset) {
		//do nothing
	}

	@Override
	public Side getHandleSide() {
		return Side.SERVER;
	}
}
