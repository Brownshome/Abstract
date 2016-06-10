package abstractgame.net;

import abstractgame.Server;
import abstractgame.net.packet.QueryPacket;

public class InternalServerProxy extends ServerProxy {
	final MemoryConnection connection;
	
	/** This method creates the connection to the server and starts the integrated server */
	public InternalServerProxy(String config, boolean openConnection) {
		Server.startServer(config, openConnection);
		connection = new MemoryConnection(Server.getInboundPacketQueue());
	}
	
	/** This method creates the connection to the server and starts the integrated server, the port argument is used to
	 * override the configured port value */
	public InternalServerProxy(String config, int port) {
		Server.startServer(config, port);
		connection = new MemoryConnection(Server.getInboundPacketQueue());
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
