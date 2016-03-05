package abstractgame.net;

import abstractgame.net.packet.Packet;

public interface Connection {	
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
		
		public synchronized void trigger() {
			done = true;
			this.notify();
		}
	}
	
	/** Sends a packet along this conenction, this method should not block if possible.
	 * This method will attempt to cause the packet's handler to be called on the other
	 * end of the connection on the correct thread. */
	void send(Packet packet);
	
	/** Sends a packet along this conenction, this method should not block if possible.
	 * This method will attempt to cause the packet's handler to be called on the other
	 * end of the connection on the correct thread. The Ack returned is used to wait for
	 * the packet, there is no gurantee this will ever happen. */
	Ack sendWithAck(Packet packet);
}
