package abstractgame.net;

import java.net.SocketException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.IntStream;

import org.junit.Assert;
import org.junit.Test;

import abstractgame.net.packet.FragmentPacket;
import abstractgame.net.packet.Packet;

public class FragmentationTest {
	@Test
	public void testFragmentation() throws SocketException {
		final int testDataSize = 20000;
		List<FragmentPacket> sentPackets = new ArrayList<>();

		UDPConnection testConnection = new UDPConnection(null, 0) {
			@Override
			public void send(Packet packet) {
				if(packet instanceof FragmentPacket) {
					sentPackets.add((FragmentPacket) packet);
				} else {
					Assert.fail();
				}
			}
		};
		
		byte[] testData = new byte[testDataSize];
		Random r = new Random();
		r.nextBytes(testData);
		testConnection.fragmentHandler.sendPacket(ByteBuffer.wrap(testData));
		
		boolean errored = false;
		try {
			r.ints(0, sentPackets.size()).distinct().mapToObj(sentPackets::get).forEach(testConnection.fragmentHandler::reassembleFragmentedPacket);
		} catch(IndexOutOfBoundsException aiooe) { errored = true; }
		
		Assert.assertTrue(errored);
		
		byte[] result = new byte[testConnection.fragmentHandler.lastFragment * FragmentPacket.FRAGMENT_SIZE + testConnection.fragmentHandler.lastFragmentSize];
		System.arraycopy(testConnection.fragmentHandler.fragmentGroupData, 0, result, 0, result.length);
		
		Assert.assertArrayEquals(testData, result);
	}
}
