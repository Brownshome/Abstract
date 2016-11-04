package abstractgame.net;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.util.BitSet;
import java.util.concurrent.atomic.*;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Function;

import abstractgame.*;
import abstractgame.io.user.Console;
import abstractgame.net.packet.*;
import abstractgame.util.ApplicationException;

/* int uuid -> not populated if the packet if from the server
 * int packetId
 * */

//TODO close the socket
public class UDPConnection implements Connection {
	static final int MTU = 1200;
	
	static DatagramSocket socket;
	
	int port;
	InetAddress address;
	TransmissionPolicy policy;
	
	final FragmentHandler fragmentHandler = new FragmentHandler();
	
	public FragmentHandler getFragmentHandler() {
		return fragmentHandler;
	}
	
	/** This is a subclass built to handle packet fragmentation. There are 8 fragment groups, the assembler discards any
	 * fragment older than the currently receiving packet */
	public class FragmentHandler {
		static final int FRAGMENT_GROUP_MASK = -1 >>> Integer.SIZE - FragmentPacket.GROUP_BITS;
		
		/** This is a {@value FragmentPacket#GROUP_BITS} bit value */
		AtomicInteger fragmentGroup = new AtomicInteger();
		
		int latestRecieveGroup = -1;
		
		BitSet recievedFragments = new BitSet();
		byte[] fragmentGroupData = new byte[FragmentPacket.MAX_SIZE];
		int lastFragment = -1; //-1 is a sentinel that indicates that the last fragment has not yet arrived
		int lastFragmentSize = 0;
		
		void sendPacket(ByteBuffer buffer) {
			assert buffer.remaining() > MTU;
			
			if(buffer.remaining() > FragmentPacket.MAX_SIZE)
				throw new ApplicationException("Excessive packet size", "NET");
			
			FragmentPacket packet;
			int localFragmentGroup = fragmentGroup.getAndUpdate(this::nextFragmentGroup);
			
			int packetNo = 0;
			do {
				packet = new FragmentPacket(buffer, fragmentGroup.get(), packetNo++);
				send(packet);
			} while(!packet.isLastFragment);
		}
		
		public void reassembleFragmentedPacket(FragmentPacket packet) {
			if(packet.fragmentGroup != latestRecieveGroup) {
				//this acts as a circular adding system.
				if(latestRecieveGroup == -1 || (packet.fragmentGroup - latestRecieveGroup & (1 << FragmentPacket.GROUP_BITS - 1)) == 0) {
					//reset the fragment system as a later packet has arrived
					latestRecieveGroup = packet.fragmentGroup;
					recievedFragments.clear();
					lastFragment = -1;
				} else {
					return; //old fragment, discard
				}
			}
			
			//not using packet.isLastFragment to avoid the possibility of malicious packets crashing the fragmenter
			int size = packet.data.remaining();
			packet.data.get(fragmentGroupData, packet.fragmentNumber * FragmentPacket.FRAGMENT_SIZE, Math.min(size, FragmentPacket.FRAGMENT_SIZE));
			
			if(packet.isLastFragment) {
				if(lastFragment != -1 && lastFragment != packet.fragmentNumber) {
					//something has gone seriously wrong here, drop the entire packet.
					lastFragment = -1;
					recievedFragments.clear();
					
					Console.warn("Duplicate last fragment packets of different indexs", "NET");
					return;
				}
				
				lastFragment = packet.fragmentNumber;
				lastFragmentSize = size;
			}
			
			recievedFragments.set(packet.fragmentNumber);
			
			if(lastFragment != -1 && recievedFragments.nextClearBit(0) > lastFragment) {
				//all fragments recieved
				ByteBuffer buffer = ByteBuffer.wrap(fragmentGroupData, 0, lastFragment * FragmentPacket.FRAGMENT_SIZE + lastFragmentSize);
				
				UDPHeader header = readHeader(buffer, Common.isServerSide());
				Connection.handle(header.reader.apply(buffer), header.id);
				
				latestRecieveGroup = -1;
			}
		}
		
		int nextFragmentGroup(int old) {
			return (old + 1) & FRAGMENT_GROUP_MASK;
		}
	}
	
	final AckHandler ackHandler = new AckHandler();
	
	public AckHandler getAckHandler() {
		return ackHandler;
	}
	
	/** This is a subclass built to handle the sending and receiving of acks */
	public class AckHandler {
		static final int SEQUENCE_NUMBERS = 1 << AckPacket.BITS;
		static final int COMP_BIT = 1 << AckPacket.BITS - 1;
		static final int NUMBER_OF_BLOCKS = SEQUENCE_NUMBERS / Long.SIZE;
		
		BitSet ackedPackets = new BitSet(SEQUENCE_NUMBERS);
		int lastBlockClearedAck = SEQUENCE_NUMBERS / Long.SIZE - 1; //block 1023 was the last one moved
		
		BitSet receivedPackets = new BitSet(SEQUENCE_NUMBERS);
		int lastBlockClearedRec = SEQUENCE_NUMBERS / Long.SIZE - 1; //block 1023 was the last one moved
		
		int blockNo(int seqNo) {
			return seqNo / Long.SIZE;
		}
		
		public void receivedPacket(short sequenceNumber) {
			int seqNo = Short.toUnsignedInt(sequenceNumber);
			int blockNo = blockNo(seqNo) ^ NUMBER_OF_BLOCKS;
			if((blockNo - lastBlockClearedRec & COMP_BIT) == 0) {
				//clear new block
				receivedPackets.clear(blockNo * Long.SIZE, (blockNo + 1) * Long.SIZE);
				lastBlockClearedRec = blockNo;
			}
			
			receivedPackets.set(seqNo);
			
			//can be optimized, but who cares
			short bitfield = 0;
			for(int i = seqNo - AckPacket.BITS - 1; i <= seqNo; i++)
				if(receivedPackets.get(i)) 
					bitfield |= 1 << i;
			
			send(new AckPacket((short) (seqNo - AckPacket.BITS - 1), (short) bitfield));
		}

		public synchronized void processAck(short startSequence, short bitfield) {
			int startNo = Short.toUnsignedInt(startSequence);
			int blockNo = blockNo(startNo) ^ NUMBER_OF_BLOCKS / 2;
			if((blockNo - lastBlockClearedRec & COMP_BIT) == 0) {
				//clear new block
				ackedPackets.clear(blockNo * Long.SIZE, (blockNo + 1) * Long.SIZE);
				lastBlockClearedAck = blockNo;
			}
			
			//again could be optimized
			for(int i = 0; i < AckPacket.BITS; i++)
				if((bitfield & 1 << i) != 0)  {
					ackedPackets.set(startNo + i);
					
					packetArray[startNo + i] = null;
					ackArray[startNo + i].trigger();
					
					if(startNo + i == oldestPacket)
						oldestPacket++;
				}
		}

		int sequenceNumber;
		int oldestPacket;
		
		Ack[] ackArray = new Ack[SEQUENCE_NUMBERS];
		ReliablePacket[] packetArray = new ReliablePacket[SEQUENCE_NUMBERS];
		long[] lastSend = new long[SEQUENCE_NUMBERS];
		
		int waitTime = 100;
		
		//TODO call this somehow
		/** Ticks the connection, resending packets if need be */
		public synchronized void tick() {
			for(int i = oldestPacket; i < sequenceNumber; i++) {
				if(Common.getClock().getTime() - lastSend[i] - waitTime < 0)
					break;
				
				if(packetArray[i] != null)
					send(packetArray[i]);
			}
		}
		
		public Ack sendPacket(Class<? extends Packet> type, ByteBuffer data) {
			return sendPacket(Packet.IDS.get(type), data);
		}
		
		public synchronized Ack sendPacket(int type, ByteBuffer data) {
			sequenceNumber++;
			
			ReliablePacket wrappedPacket = new ReliablePacket(sequenceNumber, type, data);
			
			lastSend[sequenceNumber] = Common.getClock().getTime();
			ackArray[sequenceNumber] = new Ack();
			packetArray[sequenceNumber] = wrappedPacket;
			
			send(wrappedPacket);
			
			return ackArray[sequenceNumber];
		}
		
		public synchronized Ack sendPacket(Packet packet) {
			sequenceNumber++;
			
			ReliablePacket wrappedPacket = new ReliablePacket(sequenceNumber, packet);
			
			lastSend[sequenceNumber] = Common.getClock().getTime();
			ackArray[sequenceNumber] = new Ack();
			packetArray[sequenceNumber] = wrappedPacket;
			
			send(wrappedPacket);
			
			return ackArray[sequenceNumber];
		}
	}
	
	public UDPConnection(InetAddress address, int port) {
		this.port = port;
		this.address = address;
	}

	@Override
	public void setTransmissionPolicy(TransmissionPolicy policy) {
		this.policy = policy;
	}
	
	@Override
	public Ack sendReliably(Packet packet) {
		return getAckHandler().sendPacket(packet);
	}

	/** This method may be called by multiple threads, the netthread and the mainthread */
	@Override
	public void send(Class<? extends Packet> type, ByteBuffer data) {
		Console.fine("Sending " + type.getSimpleName(), "NET");

		ByteBuffer buffer = ByteBuffer.allocate(data.remaining() + getHeaderSize()); 
		Identity identity = Common.getIdentity();
		
		if(identity != null)
			buffer.putInt(identity.uuid);
		
		buffer.putInt(Packet.IDS.get(type));
		buffer.put(data);
		buffer.flip();
		
		try {
			if(buffer.remaining() > MTU) {
				//fragment packet
				fragmentHandler.sendPacket(buffer);
			} else {
				socket.send(new DatagramPacket(buffer.array(), buffer.remaining(), address, port));
			}
		} catch (IOException e) {
			throw new ApplicationException("Unable to send packet", e, "NET");
		}
	}
	
	/** @return the size of the header in bytes of packets sent from this machine */
	public static int getHeaderSize() {
		return Integer.BYTES * (Common.isClientSide() ? 2 : 1);
	}

	public static int getHeaderSizeWithoutID() {
		return Integer.BYTES;
	}
	
	/** Writes the packet data to the specified buffer.
	 * <p>
	 * The data is written as follows:
	 * <ul>
	 * <li> [ 4B ] identity
	 * <li> [ 4B ] packet type
	 * <li> [ nB ] packet data
	 * </ul><p>
	 * NB: These methods do not flip the buffer after writing
	 * @see #writePacket(Packet)
	 * @see #writePacketNoIdentity(Packet, ByteBuffer)
	 * 
	 * @param packet the packet to write to the buffer
	 * @param buffer the buffer to write the data to
	 *  */
	public static void writePacket(Packet packet, ByteBuffer buffer) {
		writeHeader(Common.getIdentity(), Packet.IDS.get(packet.getClass()), buffer);
		packet.fill(buffer);
	}

	/**
	 * Creates and fills a new buffer of packet data 
	 * @see #writePacket(Packet, ByteBuffer) 
	 * */
	public static ByteBuffer writePacket(Packet packet) {
		ByteBuffer buffer = ByteBuffer.allocate(packet.getPayloadSize() + getHeaderSize()); 
		writePacket(packet, buffer);
		
		return buffer;
	}
	
	/** Writes the UDP Header.
	 * 
	 * @param buffer the buffer to write the header to
	 * @param type the packet id to write to the buffer
	 * @param id the uuid to write to the buffer, if this is null to uuid is written
	 */
	public static void writeHeader(Identity id, int type, ByteBuffer buffer) {
		if(id != null)
			buffer.putInt(id.uuid);
		buffer.putInt(type);
	}
	
	public static UDPHeader readHeaderClient(ByteBuffer buffer) {
		return new UDPHeader(buffer, false);
	}
	
	public static UDPHeader readHeaderServer(ByteBuffer buffer) {
		return new UDPHeader(buffer, true);
	}
	
	public static UDPHeader readHeader(ByteBuffer buffer, boolean hasIdentity) {
		return new UDPHeader(buffer, hasIdentity);
	}
	
	public static class UDPHeader {
		public final Identity id;
		public final Function<ByteBuffer, Packet> reader;
		
		public UDPHeader(Identity id, int reader) {
			this.id = id;
			this.reader = Packet.PACKET_READERS.get(reader);
		}
		
		public UDPHeader(ByteBuffer buffer, boolean hasIdentity) {
			this(hasIdentity ? PlayerDataHandler.getIdentity(buffer.getInt()) : null, buffer.getInt());
		}
	}
	
	@Override
	public void send(Packet packet) {
		Console.fine("Sending " + packet.getClass().getSimpleName(), "NET");
		
		ByteBuffer buffer = writePacket(packet);
		buffer.flip();
		
		try {
			if(buffer.remaining() > MTU) {
				//fragment packet
				fragmentHandler.sendPacket(buffer);
			} else {
				//synchronized by the OS?
				socket.send(new DatagramPacket(buffer.array(), buffer.remaining(), address, port));
			}
		} catch (IOException e) {
			throw new ApplicationException("Unable to send packet", e, "NET");
		}
	}

	@Override
	public Ack sendReliably(Class<? extends Packet> type, ByteBuffer data) {
		return getAckHandler().sendPacket(type, data);
	}

	/** This starts a UDP listen thread on a particular port */
	public static void startServerListener(int port) {
		assert socket == null;
		
		try {
			socket = new DatagramSocket(port);
		} catch (SocketException e) {
			throw new ApplicationException("Unable to bind to port \'" + port + "\', maybe the port is already in use.", "NET");
		}
		
		Thread serverListenThread = new Thread(() -> {
			DatagramPacket packet = new DatagramPacket(new byte[2048], 2048);
			
			while(true) {
				try {
					socket.receive(packet);
					
					ByteBuffer buffer = ByteBuffer.wrap(packet.getData(), packet.getOffset(), packet.getLength());
					
					UDPHeader header = readHeaderServer(buffer);
					
					if(!Server.getConnectedIds().contains(header.id))
						Server.createConnection(header.id, new UDPConnection(packet.getAddress(), packet.getPort()));
					
					Connection.handle(header.reader.apply(buffer), header.id);
				} catch (IOException e) {
					Console.warn("Error in net thread wait, re-queuing." + e.getMessage(), "NET");
				}
			}
		}, "SERVER-UDP-THREAD");
		
		serverListenThread.setDaemon(true);
		Server.addServerThread(serverListenThread);
		serverListenThread.start();
	}
	
	public static void startClientListener() {
		assert socket == null;
		
		try {
			socket = new DatagramSocket();
		} catch (SocketException e) {
			throw new ApplicationException("Unable to open connection.", "NET");
		}
		
		Thread clientListenThread = new Thread(() -> {
			DatagramPacket packet = new DatagramPacket(new byte[2048], 2048);
			
			while(true) {
				try {
					socket.receive(packet);
					ByteBuffer buffer = ByteBuffer.wrap(packet.getData(), packet.getOffset(), packet.getLength());

					UDPHeader header = readHeaderClient(buffer);

					Connection.handle(header.reader.apply(buffer), null);
				} catch (IOException e) {
					Console.warn("Error in net thread wait, re-queuing." + e.getMessage(), "NET");
				}
			}
		}, "CLIENT-UDP-THREAD");
		
		clientListenThread.setDaemon(true);
		clientListenThread.start();
	}

	public void tick() {
		getAckHandler().tick();
	}
}