package abstractgame.net.packet;

import java.nio.ByteBuffer;

import abstractgame.Server;
import abstractgame.net.*;
import abstractgame.net.UDPConnection.UDPHeader;

/* [ 2B ] Packet sequence number
 * [ nB ] Packet data and UDP header, less Identity
 */

/** This is a wrapper packet that uses a sequence number */
public class ReliablePacket extends Packet {
	Packet packet;
	short sequenceNumber;
	
	public ReliablePacket(int sequenceNumber, Packet packet) {
		assert sequenceNumber < 1 << 16;
		this.packet = packet;
		this.sequenceNumber = (short) sequenceNumber;
	}
	
	public ReliablePacket(ByteBuffer buffer) {
		sequenceNumber = buffer.getShort();
		UDPHeader header = new UDPHeader(buffer, false);
		packet = header.reader.apply(buffer);
	}
	
	@Override
	public void fill(ByteBuffer output) {
		output.putShort(sequenceNumber);
		UDPConnection.writeHeader(null, Packet.IDS.get(packet.getClass()), output);
		packet.fill(output);
	}

	@Override
	public int getPayloadSize() {
		return packet.getPayloadSize() + Short.BYTES + UDPConnection.getHeaderSizeWithoutID();
	}
	
	@Override
	public void handle(Identity id) {
		super.handle(id);
		packet.handle(id);
	}
	
	@Override
	public void handleClient() {
		UDPConnection connection = (UDPConnection) ServerProxy.getCurrentServerProxy().getConnection();
		connection.getAckHandler().receivedPacket(sequenceNumber);
	}
	
	@Override
	public void handleServer(Identity id) {
		UDPConnection connection = (UDPConnection) Server.getConnection(id);
		connection.getAckHandler().receivedPacket(sequenceNumber);
	}
}
