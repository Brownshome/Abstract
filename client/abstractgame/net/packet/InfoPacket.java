package abstractgame.net.packet;

import abstractgame.net.Identity;
import abstractgame.net.Side;

/** Sends information about the server to the client */
public class InfoPacket extends Packet {
	public InfoPacket(byte[] data) {
		
	}
	
	public InfoPacket() {
	
	}

	@Override
	public void handle(Identity id) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public int getSize() {
		return 0;
	}

	@Override
	public void fill(byte[] data, int offset) {
		//do nothing
	}

	@Override
	public Side getHandleSide() {
		return Side.CLIENT;
	}
}
