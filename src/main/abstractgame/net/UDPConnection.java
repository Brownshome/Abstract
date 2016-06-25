package abstractgame.net;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.nio.ByteBuffer;

import abstractgame.Common;
import abstractgame.Server;
import abstractgame.io.user.Console;
import abstractgame.net.packet.Packet;
import abstractgame.util.ApplicationException;

/* int uuid -> not populated if the packet if from the server
 * int packetId
 * */

//TODO close the socket
public class UDPConnection implements Connection {
	static DatagramSocket socket;
	
	int port;
	InetAddress address;
	TransmissionPolicy policy;
	
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
			socket.send(new DatagramPacket(buffer.array(), buffer.remaining(), address, port));
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
			socket.send(new DatagramPacket(buffer.array(), buffer.remaining(), address, port));
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
