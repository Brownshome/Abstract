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
public class InfoPacket extends Packet {
	String version;
	String worldIdentifier;
	long[] ids;
	
	public InfoPacket(byte[] data) {
		ByteBuffer buffer = ByteBuffer.wrap(data);
		
		version = Util.readTerminatedString(buffer);
		worldIdentifier = Util.readTerminatedString(buffer);
		int length = Byte.toUnsignedInt(buffer.get());
		ids = new long[length];
		buffer.asLongBuffer().get(ids);
	}
	
	public InfoPacket() {
		worldIdentifier = Server.mapIdentifier;
		ids = Server.getConnectedIds().stream().mapToLong(i -> i.uuid).toArray();
	}

	@Override
	public void handleClient() {
		Util.queueOnMainThread(() -> {
			ServerProxy.getCurrentServerProxy().setServerInfo(worldIdentifier, ids);
		});
	}

	@Override
	public void fill(byte[] data, int offset) {
		ByteBuffer buffer = ByteBuffer.wrap(data, offset, data.length - offset);
		
		Util.writeTerminatedString(buffer, version);
		Util.writeTerminatedString(buffer, worldIdentifier);
		buffer.asLongBuffer().put(ids);
	}

	@Override
	public Side getHandleSide() {
		return Side.CLIENT;
	}
}
