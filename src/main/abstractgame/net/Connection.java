package abstractgame.net;

import java.nio.ByteBuffer;
import java.util.stream.Stream;

import abstractgame.Server;
import abstractgame.net.packet.Packet;

public interface Connection {	
	public static void sendToAll(Packet packet, Iterable<Connection> connections) {
		byte[] data = new byte[packet.getPayloadSize()];
		ByteBuffer buffer = ByteBuffer.wrap(data);
		packet.fill(buffer);
		for(Connection c : connections) {
			c.send(packet.getClass(), data);
		}
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
		
		/** This method simply exits on interupt */
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
	
	/** Sends a packet along this conenction, this method should not block if possible.
	 * This method will attempt to cause the packet's handler to be called on the other
	 * end of the connection on the correct thread. */
	void send(Packet packet);
	
	/** Similar to {@link send(Packet)} but is used in bulk sending methods
	 * when the packet needs to be pre-encoded */
	void send(Class<? extends Packet> type, byte[] data);
	
	/** Sends a packet along this conenction, this method should not block if possible.
	 * This method will attempt to cause the packet's handler to be called on the other
	 * end of the connection on the correct thread. The Ack returned is used to wait for
	 * the packet, there is no gurantee this will ever happen. */
	Ack sendWithAck(Packet packet);
	
	/** Similar to {@link sendWithAck(Packet)} but is used in bulk sending methods
	 * when the packet needs to be pre-encoded */
	Ack sendWithAck(Class<? extends Packet> type, byte[] data);
}
