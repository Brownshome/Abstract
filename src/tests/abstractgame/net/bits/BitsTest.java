package abstractgame.net.bits;

import org.junit.Test;

import java.nio.ByteBuffer;
import java.util.Random;

import org.junit.Assert;

public class BitsTest {
	@Test
	public void testBitPacking() {
		byte[] data = new byte[2];
		ByteBuffer testBuffer = ByteBuffer.wrap(data);
		BitPacker packer = new BitPacker(testBuffer);
		packer.put(0b10101100101, 11);
		packer.put(0b01001, 5);
		packer.flush();
		
		Assert.assertArrayEquals(new byte[] { 0b01100101, 0b01001101 }, data);
	}
	
	@Test
	public void testBitUnpacking() {
		byte[] data = new byte[] { 0b01100101, 0b01001101 };
		ByteBuffer buffer = ByteBuffer.wrap(data);
		BitUnpacker unpacker = new BitUnpacker(buffer);
		int a = unpacker.read(11);
		int b = unpacker.read(5);
		
		Assert.assertTrue(a == 0b10101100101 && b == 0b01001);
	}
	
	@Test
	public void testRandomWithFlush() {
		final int n = 1000;
		
		Random r = new Random();
		
		int[] testData = new int[n];
		int[] bits = new int[n];
		int sum = 0;
		
		for(int i = 0; i < n; i++) {
			bits[i] = r.nextInt(30) + 1;
			sum += bits[i];
			testData[i] = r.nextInt(1 << bits[i]);
		}
		
		int bytes = (sum - 1) / 8 + 1 + n / 5;
		
		ByteBuffer buffer = ByteBuffer.allocate(bytes);
		BitPacker packer = new BitPacker(buffer);
		for(int i = 0; i < n; i++) {
			if(i % 5 == 0) packer.flush();
			packer.put(testData[i], bits[i]);
		}
		
		packer.flush();
		buffer.flip();
		
		BitUnpacker unpacker = new BitUnpacker(buffer);
		
		for(int i = 0; i < n; i++) {
			if(i % 5 == 0) unpacker.byteAllign();
			Assert.assertEquals(String.valueOf(i), testData[i], unpacker.read(bits[i]));
		}
	}
}
