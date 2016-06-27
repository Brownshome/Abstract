package abstractgame.net;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.util.BitSet;

import abstractgame.Common;
import abstractgame.Server;
import abstractgame.io.user.Console;
import abstractgame.net.packet.FragmentPacket;
import abstractgame.net.packet.Packet;
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
	
	public final FragmentHandler fragmentHandler = new FragmentHandler();
	
	/** This is a subclass built to handle packet fragmentation. There are 8 fragment groups, the assembler discards any
	 * fragment older than the currently receiving packet */
	public class FragmentHandler {
		static final int FRAGMENT_GROUP_MASK = -1 >>> Integer.SIZE - FragmentPacket.GROUP_BITS;
		
		/** This is a {@value FragmentPacket#GROUP_BITS} bit value */
		int fragmentGroup = 0;
		
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
			
			int packetNo = 0;
			do {
				packet = new FragmentPacket(buffer, fragmentGroup, packetNo++);
				send(packet);
			} while(!packet.isLastFragment);
			
			nextFragmentGroup();
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
				
				if(Common.isServerSide()) {
					Identity id = PlayerDataHandler.getIdentity(buffer.getInt());
					
					Connection.handle(buffer.getInt(), buffer, id);
				} else {
					Connection.handle(buffer.getInt(), buffer, null);
				}
				
				latestRecieveGroup = -1;
			}
		}
		
		void nextFragmentGroup() {
			fragmentGroup = (fragmentGroup + 1) & FRAGMENT_GROUP_MASK;
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
		assert false : "not implemented";
		return null;
	}

	@Override
	public void send(Class<? extends Packet> type, byte[] data) {
		Console.fine("Sending " + type.getSimpleName(), "NET");

		ByteBuffer buffer = ByteBuffer.allocate(data.length + getHeaderSize()); 
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
	private int getHeaderSize() {
		return Integer.BYTES * (Common.isClientSide() ? 2 : 1);
	}

	@Override
	public void send(Packet packet) {
		Console.fine("Sending " + packet.getClass().getSimpleName(), "NET");
		
		ByteBuffer buffer = ByteBuffer.allocate(packet.getPayloadSize() + getHeaderSize()); 
		
		Identity identity = Common.getIdentity();
		if(identity != null)
			buffer.putInt(identity.uuid);
		
		buffer.putInt(Packet.IDS.get(packet.getClass()));
		packet.fill(buffer);
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

	@Override
	public Ack sendReliably(Class<? extends Packet> type, byte[] data) {
		assert false : "not implemented";
		return null;
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
					
					Identity identity = PlayerDataHandler.getIdentity(buffer.getInt());
					int id = buffer.getInt();
					
					if(!Server.getConnectedIds().contains(identity))
						Server.createConnection(identity, new UDPConnection(packet.getAddress(), packet.getPort()));
					
					Connection.handle(id, buffer, identity);
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
					int id = buffer.getInt();
					Connection.handle(id, buffer, null);
				} catch (IOException e) {
					Console.warn("Error in net thread wait, re-queuing." + e.getMessage(), "NET");
				}
			}
		}, "CLIENT-UDP-THREAD");
		
		clientListenThread.setDaemon(true);
		clientListenThread.start();
	}
}