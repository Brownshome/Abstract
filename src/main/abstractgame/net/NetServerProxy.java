package abstractgame.net;

import java.net.InetAddress;

import abstractgame.net.packet.QueryPacket;

public class NetServerProxy extends ServerProxy {
	UDPConnection connection;
	
	public NetServerProxy(InetAddress address, int port) {
		UDPConnection.startClientListener();
		connection = new UDPConnection(address, port);
		connection.send(new QueryPacket());
	}

	@Override
	public String getName() {
		return connection.address + ":" + connection.port;
	}

	@Override
	public Connection getConnection() {
		return connection;
	}

	@Override
	public void tick() {
		connection.tick();
	}
}
