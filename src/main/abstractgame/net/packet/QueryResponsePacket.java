package abstractgame.net.packet;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

import abstractgame.Server;
import abstractgame.net.Identity;
import abstractgame.net.ServerProxy;
import abstractgame.net.Side;
import abstractgame.util.Util;
import abstractgame.world.World;

/** Sends information about the server to the client */
public class QueryResponsePacket extends Packet {
	String version;
	String worldIdentifier;
	int[] ids;
	
	public QueryResponsePacket(byte[] data) {
		ByteBuffer buffer = ByteBuffer.wrap(data);
		
		version = Util.readTerminatedString(buffer);
		worldIdentifier = Util.readTerminatedString(buffer);
		int length = Byte.toUnsignedInt(buffer.get());
		ids = new int[length];
		buffer.asIntBuffer().get(ids);
	}
	
	public QueryResponsePacket() {
		worldIdentifier = Server.mapIdentifier;
		ids = Server.getConnectedIds().stream().mapToInt(i -> i.uuid).toArray();
	}

	@Override
	public void handleClient() {
		//this is temporary, TODO implement server browser
		
		Util.queueOnMainThread(() -> {
			ServerProxy.getCurrentServerProxy().setServerInfo(worldIdentifier, ids);
			ServerProxy.getCurrentServerProxy().getConnection().send(new JoinPacket());
		});
	}

	@Override
	public void fill(byte[] data, int offset) {
		ByteBuffer buffer = ByteBuffer.wrap(data, offset, data.length - offset);
		
		Util.writeTerminatedString(buffer, version);
		Util.writeTerminatedString(buffer, worldIdentifier);
		buffer.asIntBuffer().put(ids);
	}

	@Override
	public Side getHandleSide() {
		return Side.CLIENT;
	}
}
