package abstractgame.net;

import abstractgame.net.packet.Packet;

public interface Connection {
	/** This object will be notified when the packet reaches the other end */
	@FunctionalInterface
	public interface Ack {
		boolean isDone();
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
