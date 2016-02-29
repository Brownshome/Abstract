package abstractgame.net;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.SynchronousQueue;

import abstractgame.net.packet.Packet;

public class MemoryConnection extends Thread implements Connection {
	static final Ack DONE = () -> true;
	static int counter = 0;
	
	BlockingQueue<byte[]> in;
	BlockingQueue<byte[]> out;
	
	/** Creates a memory connection linking to 'other' */
	public MemoryConnection(MemoryConnection other) {
		super("PacketHandler" + counter++);
		
		in = other.out;
		out = other.in;
		
		other.start();
		super.start();
	}
	
	/** Creates an unlinked memory connection */
	public MemoryConnection() {
		super("PacketHandler" + counter++);
		
		in = new SynchronousQueue<>();
		out = new SynchronousQueue<>();
	}
	
	@Override
	public void run() {
		while(true) {
			try {	
				byte[] data = null;
				data = in.take();
				int packetID = data[0];
				Packet.packetReaders.get(packetID).apply(data).handle();
			} catch (InterruptedException e) {}
		}
	}
	
	@Override
	public void send(Packet packet) {
		try {
			out.put(packet.send());
		} catch (InterruptedException e) {}
	}

	@Override
	public Ack sendWithAck(Packet packet) {
		try {
			out.put(packet.send());
		} catch (InterruptedException e) {}
		
		return DONE;
	}
}
