package abstractgame.net;

import java.nio.ByteBuffer;
import java.util.concurrent.BlockingQueue;

import abstractgame.Client;
import abstractgame.Common;
import abstractgame.Server;
import abstractgame.io.config.ConfigFile.Policy;
import abstractgame.io.user.Console;
import abstractgame.net.packet.Packet;

@Sided(Side.CLIENT)
public class MemoryConnection implements Connection {
	static int counter = 0;
	
	BlockingQueue<Runnable> threadQueue;
	
	public MemoryConnection(BlockingQueue<Runnable> threadQueue) {
		this.threadQueue = threadQueue;
	}
	
	@Override
	public void setTransmissionPolicy(TransmissionPolicy policy) { /* Do nothing as the memory connection is very simple */ }
	
	@Override
	public void send(Class<? extends Packet> type, ByteBuffer data) {
		assert Server.isInternal();
		
		ByteBuffer safeData = data.duplicate(); //to avoid concurrent modification of position and mark for dual connection types
		Console.fine("Sending " + type.getSimpleName(), "NET");
		Identity identity = Common.getIdentity();
		BlockingQueue<Runnable> clientQueue = Common.isClientSide() && !Server.getConnectedIds().contains(identity) ? Client.getInboundQueue() : null;
		
		try {
			threadQueue.put(() -> {
				if(clientQueue != null)
					Server.createConnection(identity, new MemoryConnection(clientQueue));
				
				Connection.handle(Packet.PACKET_READERS.get(Packet.IDS.get(type)).apply(safeData), identity); 
			});
		} catch (InterruptedException e) {}
	}
	
	@Override
	public void send(Packet packet) {
		assert Server.isInternal();
		
		Console.fine("Sending " + packet.getClass().getSimpleName(), "NET");
		
		ByteBuffer buffer = ByteBuffer.allocate(packet.getPayloadSize()); 
		packet.fill(buffer);
		buffer.flip();
		
		Identity id = Common.getIdentity();
		BlockingQueue<Runnable> clientQueue = Common.isClientSide() && !Server.getConnectedIds().contains(id) ? Client.getInboundQueue() : null;
		
		try {
			threadQueue.put(() -> {
				if(clientQueue != null)
					Server.createConnection(id, new MemoryConnection(clientQueue));
				
				// It would be easier to just pass the packet, but that might not be safe in all situations
				Connection.handle(Packet.PACKET_READERS.get(Packet.IDS.get(packet.getClass())).apply(buffer), id);
			});
		} catch (InterruptedException e) {}
	}

	@Override
	public Ack sendReliably(Class<? extends Packet> type, ByteBuffer data) {
		Console.fine("Sending with ack " + type.getSimpleName(), "NET");

		Ack ack = new Ack();
		ByteBuffer safeData = data.duplicate();

		Identity id = Common.getIdentity();
		BlockingQueue<Runnable> clientQueue = Common.isClientSide() && !Server.getConnectedIds().contains(id) ? Client.getInboundQueue() : null;
		
		try {
			threadQueue.put(() -> {
				if(clientQueue != null)
					Server.createConnection(id, new MemoryConnection(clientQueue));
				
				//instantiating the packet on the othe side in case it is sensitive to which side it is on
				Connection.handle(Packet.PACKET_READERS.get(Packet.IDS.get(type)).apply(safeData), id);

				ack.trigger();
			});
		} catch (InterruptedException e) {}

		return ack;
	}
	
	@Override
	public Ack sendReliably(Packet packet) {
		Console.fine("Sending with ack " + packet.getClass().getSimpleName(), "NET");
		
		Ack ack = new Ack();
		
		ByteBuffer buffer = ByteBuffer.allocate(packet.getPayloadSize()); 
		packet.fill(buffer);
		
		buffer.flip();
		
		Identity id = Common.getIdentity();
		BlockingQueue<Runnable> clientQueue = Common.isClientSide() && !Server.getConnectedIds().contains(id) ? Client.getInboundQueue() : null;
		
		try {
			threadQueue.put(() -> {
				if(clientQueue != null)
					Server.createConnection(id, new MemoryConnection(clientQueue));
				
				Connection.handle(Packet.PACKET_READERS.get(Packet.IDS.get(packet.getClass())).apply(buffer), id);
				ack.trigger();
			});
		} catch (InterruptedException e) {}
		
		return ack;
	}
}
