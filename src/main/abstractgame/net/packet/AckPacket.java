package abstractgame.net.packet;

import java.nio.ByteBuffer;

import abstractgame.Server;
import abstractgame.net.*;

/* [ 2B ] the packet sequence number at the first bit index
 * [ 2B ] bitfield for acked packets
 * 
 * This gives a 1 - packetLoss ^ 16 chance of the ack getting through
 */

public class AckPacket extends Packet {
	public static final int BITS = 16;
	short startSequence;
	short bitfield;
	
	public AckPacket(ByteBuffer buffer) {
		startSequence = buffer.getShort();
		bitfield = buffer.getShort();
	}
	
	public AckPacket(short startSequence, short bitfield) {
		this.startSequence = startSequence;
		this.bitfield = bitfield;
	}
	
	@Override
	public void handleClient() {
		UDPConnection connection = (UDPConnection) ServerProxy.getCurrentServerProxy().getConnection();
		connection.getAckHandler().processAck(startSequence, bitfield);
	}
	
	@Override
	public void handleServer(Identity id) {
		UDPConnection connection = (UDPConnection) Server.getConnection(id);
		connection.getAckHandler().processAck(startSequence, bitfield);
	}

	@Override
	public void fill(ByteBuffer output) {
		output.putShort(startSequence);
		output.putShort(bitfield);
	}

	@Override
	public int getPayloadSize() {
		return Short.BYTES * 2;
	}
}
