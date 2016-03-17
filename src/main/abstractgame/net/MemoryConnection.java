package abstractgame.net;

import java.util.concurrent.BlockingQueue;

import abstractgame.Client;
import abstractgame.net.packet.Packet;

@Sided(Side.CLIENT)
public class MemoryConnection implements Connection {
	static int counter = 0;
	
	BlockingQueue<Runnable> threadQueue;
	boolean setID;
	
	public MemoryConnection(BlockingQueue<Runnable> threadQueue, boolean setID) {
		this.threadQueue = threadQueue;
		this.setID = setID;
	}
	
	@Override
	public void send(Packet packet) {
		try {
			threadQueue.put(() -> packet.handle(setID ? Client.getIdentity() : null));
		} catch (InterruptedException e) {}
	}

	@Override
	public Ack sendWithAck(Packet packet) {
		Ack ack = new Ack();
		
		try {
			threadQueue.put(() -> {
				packet.handle(setID ? Client.getIdentity() : null);
				ack.trigger();
			});
		} catch (InterruptedException e) {}
		
		return ack;
	}
}