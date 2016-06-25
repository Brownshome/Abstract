package abstractgame.net.bits;

import java.nio.ByteBuffer;

public class BitPacker {
	public final ByteBuffer buffer;
	long scratch;
	int bitsWritten;
	
	public BitPacker(ByteBuffer buffer) {
		this.buffer = buffer;
		bitsWritten = 0;
	}
	
	void write() {
		buffer.put((byte) scratch);
		bitsWritten -= 8;
		scratch >>>= 8;
	}
	
	/** Writes all data to the underlying array, and alligns to the nearest byte */
	public void flush() {
		while(bitsWritten > 0) write();
		bitsWritten = 0;
	}
	
	public void put(int value, int bits) {
		assert bits > 0 && bits <= 32;
		assert 32 - Integer.numberOfLeadingZeros(value) <= bits;
		
		long tmp = value;
		tmp <<= bitsWritten;
		scratch |= tmp;
		bitsWritten += bits;
		
		while(bitsWritten >= 8) write();
	}
}