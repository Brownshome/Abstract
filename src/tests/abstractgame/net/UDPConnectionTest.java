package abstractgame.net;

import java.net.SocketException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.*;

import org.junit.Assert;
import org.junit.Test;

import abstractgame.net.packet.*;

public class UDPConnectionTest {
	List<Packet> sentPackets = new ArrayList<>();
	UDPConnection testConnection = new UDPConnection(null, 0) {
		@Override
		public void send(Packet packet) {
			sentPackets.add(packet);
		}
	};

	@Test
	public void testFragmentation() throws SocketException {
		final int testDataSize = 20000;

		byte[] testData = new byte[testDataSize];
		Random r = new Random();
		r.nextBytes(testData);
		testConnection.getFragmentHandler().sendPacket(ByteBuffer.wrap(testData));

		List<FragmentPacket> frags = sentPackets.stream().filter(p -> p instanceof FragmentPacket).map(p -> (FragmentPacket) p).collect(Collectors.toList());
		
		boolean errored = false;
		try {
			r.ints(0, frags.size()).distinct().mapToObj(frags::get).forEach(testConnection.getFragmentHandler()::reassembleFragmentedPacket);
		} catch(IndexOutOfBoundsException aiooe) { errored = true; }

		Assert.assertTrue(errored);

		byte[] result = new byte[testConnection.getFragmentHandler().lastFragment * FragmentPacket.FRAGMENT_SIZE + testConnection.getFragmentHandler().lastFragmentSize];
		System.arraycopy(testConnection.getFragmentHandler().fragmentGroupData, 0, result, 0, result.length);

		Assert.assertArrayEquals(testData, result);
	}
}
