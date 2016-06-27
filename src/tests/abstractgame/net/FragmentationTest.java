package abstractgame.net;

import java.net.*;

import org.junit.Test;

import abstractgame.net.packet.Packet;

public class FragmentationTest {
	@Test
	public void testFragmentation() {
		try {
			Connection testConnection = new UDPConnection(null, 0) {
				{
					socket = new DatagramSocket() {
						public void send() {
							
						}
					};
				}
			};
		} catch (SocketException e) {}
	}
}
