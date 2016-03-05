package abstractgame.net;

import abstractgame.Server;
import abstractgame.net.packet.QueryPacket;

public class InternalServerProxy extends ServerProxy {
	final MemoryConnection connection;
	
	/** This method creates the connection to the server and starts the integrated server */
	public InternalServerProxy(String config) {
		Server.startServer(config);
		connection = new MemoryConnection(Server.getInboundPacketQueue(), true);
		connection.send(new QueryPacket());
	}
	
	@Override
	public String getName() {
		return "Integrated server";
	}

	@Override
	public Connection getConnection() {
		return connection;
	}
}
