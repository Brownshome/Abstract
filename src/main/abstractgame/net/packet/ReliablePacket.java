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
	//either
	Packet packet = null;
	//or
	ByteBuffer data = null;
	int type = 0;
	
	short sequenceNumber;
	
	public ReliablePacket(int sequenceNumber, Class<? extends Packet> type, ByteBuffer buffer) {
		this(sequenceNumber, Packet.IDS.get(type.getClass()), buffer);
	}
	
	public ReliablePacket(int sequenceNumber, int type, ByteBuffer buffer) {
		assert sequenceNumber < 1 << 16;
		this.data = buffer.duplicate();
		this.type = type;
		this.sequenceNumber = (short) sequenceNumber;
	}
	
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
		
		if(packet != null) {
			UDPConnection.writeHeader(null, Packet.IDS.get(packet.getClass()), output);
			packet.fill(output);
		} else {
			UDPConnection.writeHeader(null, type, output);
			output.put(data);
		}
	}

	@Override
	public int getPayloadSize() {
		return (packet == null ? data.remaining() : packet.getPayloadSize()) + Short.BYTES + UDPConnection.getHeaderSizeWithoutID();
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
