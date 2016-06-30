package abstractgame.net;

import java.nio.ByteBuffer;

import abstractgame.io.user.Console;
import abstractgame.net.packet.Packet;

public interface Connection {	
	static void sendToAll(Packet packet, Iterable<Connection> connections) {
		ByteBuffer buffer = ByteBuffer.allocate(packet.getPayloadSize());
		packet.fill(buffer);
		buffer.flip();
		
		for(Connection c : connections) {
			c.send(packet.getClass(), buffer);
			buffer.rewind();
		}
	}
	
	static void handle(Packet packet, Identity identity) {
		Console.fine("Recieved " + packet.getClass().getSimpleName() + (identity == null ? "" : " from " + identity), "NET");
		
		packet.handle(identity);
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
			this.notifyAll();
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
	void send(Class<? extends Packet> type, ByteBuffer buffer);
	
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
	Ack sendReliably(Class<? extends Packet> type, ByteBuffer data);
}
