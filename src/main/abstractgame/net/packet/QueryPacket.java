package abstractgame.net.packet;

import java.nio.ByteBuffer;

import abstractgame.Server;
import abstractgame.net.Identity;
import abstractgame.net.Side;

/** Starts retrieves information about the server */
public class QueryPacket extends Packet {
	public QueryPacket(ByteBuffer data) {}
	
	public QueryPacket() {}

	@Override
	public void handle(Identity id) {
		Server.getConnection(id).send(new QueryResponsePacket());
	}

	@Override
	public void fill(ByteBuffer output) {
		//do nothing
	}

	@Override
	public int getPayloadSize() {
		return 0;
	}
}
