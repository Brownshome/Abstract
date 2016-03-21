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
	
	public QueryResponsePacket(ByteBuffer buffer) {
		version = Util.readTerminatedString(buffer);
		worldIdentifier = Util.readTerminatedString(buffer);
		int length = Byte.toUnsignedInt(buffer.get());
		ids = new int[length];
		buffer.asIntBuffer().get(ids);
	}
	
	public QueryResponsePacket() {
		version = Server.version;
		worldIdentifier = Server.mapIdentifier;
		ids = Server.getConnectedIds().stream().mapToInt(i -> i.uuid).toArray();
	}

	@Override
	public void handleClient() {
		//this is temporary, TODO implement server browser
		
		Util.queueOnMainThread(() -> {
			ServerProxy.getCurrentServerProxy().setServerInfo(version, worldIdentifier, ids);
			ServerProxy.getCurrentServerProxy().getConnection().send(new JoinPacket());
		});
	}

	@Override
	public void fill(ByteBuffer output) {
		Util.writeTerminatedString(output, version);
		Util.writeTerminatedString(output, worldIdentifier);
		for(int id : ids)
			output.putInt(id);
	}

	@Override
	public int getPayloadSize() {
		return Util.getMaxLength(version) + Util.getMaxLength(worldIdentifier) + ids.length * Integer.BYTES;
	}
}
