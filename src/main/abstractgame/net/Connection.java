package abstractgame.net;

import java.nio.ByteBuffer;

import abstractgame.io.user.Console;
import abstractgame.net.packet.Packet;

public interface Connection {	
	static void sendToAll(Packet packet, Iterable<Connection> connections) {
		byte[] data = new byte[packet.getPayloadSize()];
		ByteBuffer buffer = ByteBuffer.wrap(data);
		packet.fill(buffer);
		for(Connection c : connections) {
			c.send(packet.getClass(), data);
		}
	}
	
	static void handle(int type, ByteBuffer buffer, Identity identity) {
		Packet reconstructed = Packet.PACKET_READERS.get(type).apply(buffer);
		
		Console.fine("Recieved " + reconstructed.getClass().getSimpleName() + (identity == null ? "" : " from " + identity), "NET");
		
		reconstructed.handle(identity);
	}
	
	/** This object will be notified when the packet reaches the other end */
	public class Ack {
		private boolean done;
		
		public boolean isDone() {
			return done;
		}
		
		public synchronized void waitFor() throws InterruptedException {
			while(!done)
				this.wait();
		}
		
		/** This method simply exits on interrupt */
		public void waitForSafe() {
			try {
				waitFor();
			} catch (InterruptedException e) {}
		}
		
		public synchronized void trigger() {
			done = true;
			this.notify();
		}
	}
	
	enum TransmissionPolicy {
		DATA_RATE,
		LATENCY
	}
	
	/** Sets the aspect of the transmission to optimize for. This may or may not have any effect depending on the transmission. */
	void setTransmissionPolicy(TransmissionPolicy policy);
	
	/** Sends a packet along this connection, this method should not block if possible.
	 * This method will attempt to cause the packet's handler to be called on the other
	 * end of the connection on the correct thread. */
	void send(Packet packet);
	
	/** Similar to {@link #send(Packet)} but is used in bulk sending methods
	 * when the packet needs to be pre-encoded */
	void send(Class<? extends Packet> type, byte[] data);
	
	/** Sends a packet along this connection, this method should not block if possible.
	 * This method will attempt to cause the packet's handler to be called on the other
	 * end of the connection on the correct thread. The {@link Ack} returned is used to wait for
	 * the packet, this is guaranteed to happen. 
	 * 
	 * @param packet The {@link Packet} to send
	 * 
	 * @return The object used to wait on*/
	Ack sendReliably(Packet packet);
	
	/** Similar to {@link #sendReliably(Packet)} but is used in bulk sending methods
	 * when the packet needs to be pre-encoded.
	 * 	
	 * @param type The class object representing the type of packet
	 * @param data An array containing the already compiled packet data
	 * @return The object used to wait on*/
	Ack sendReliably(Class<? extends Packet> type, byte[] data);
}
