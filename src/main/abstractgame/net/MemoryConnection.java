package abstractgame.net;

import java.nio.ByteBuffer;
import java.util.concurrent.BlockingQueue;

import abstractgame.Client;
import abstractgame.Server;
import abstractgame.io.user.Console;
import abstractgame.net.packet.Packet;

@Sided(Side.CLIENT)
public class MemoryConnection implements Connection {
	static int counter = 0;
	
	BlockingQueue<Runnable> threadQueue;
	/** Whether the sending side of this connection is serverside */
	boolean isServerSide;
	
	public MemoryConnection(BlockingQueue<Runnable> threadQueue, boolean isServerSide) {
		this.threadQueue = threadQueue;
		this.isServerSide = isServerSide;
	}
	
	@Override
	public void send(Packet packet) {
		Console.fine("Sending " + packet.getClass().getSimpleName(), "NET");
		
		ByteBuffer buffer = ByteBuffer.allocate(packet.getPayloadSize()); 
		packet.fill(buffer);
		
		buffer.flip();
		
		try {
			threadQueue.put(() -> {
				int id = Packet.IDS.get(packet.getClass());
				Packet reconstructed = Packet.PACKET_READERS.get(id).apply(buffer);
				reconstructed.handle(isServerSide ? Client.getIdentity() : null);
			});
		} catch (InterruptedException e) {}
	}

	@Override
	public Ack sendWithAck(Packet packet) {
		Console.fine("Sending with ack " + packet.getClass().getSimpleName(), "NET");
		
		Ack ack = new Ack();
		
		ByteBuffer buffer = ByteBuffer.allocate(packet.getPayloadSize()); 
		packet.fill(buffer);
		
		buffer.flip();
		
		try {
			threadQueue.put(() -> {
				int id = Packet.IDS.get(packet.getClass());
				Packet reconstructed = Packet.PACKET_READERS.get(id).apply(buffer);
				reconstructed.handle(isServerSide ? Client.getIdentity() : null);
				
				ack.trigger();
			});
		} catch (InterruptedException e) {}
		
		return ack;
	}
}
