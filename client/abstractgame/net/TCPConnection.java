package abstractgame.net;

import abstractgame.net.packet.Packet;

public class TCPConnection implements Connection {
	public TCPConnection(Identity id) {
		// TODO Auto-generated constructor stub
	}

	@Override
	public void send(Packet packet) {
		// TODO Auto-generated method stub

	}

	@Override
	public Ack sendWithAck(Packet packet) {
		// TODO Auto-generated method stub
		return null;
	}

}
