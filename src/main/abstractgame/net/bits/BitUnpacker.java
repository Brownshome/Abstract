package abstractgame.net.bits;

import java.nio.ByteBuffer;

public class BitUnpacker {
	public final ByteBuffer input;
	long scratch;
	int bitsWritten;
	
	public BitUnpacker(ByteBuffer input) {
		this.input = input;
		bitsWritten = 0;
		scratch = 0;
	}
	
	//puts a byte into the scratch
	void fillScratch() {
		long value = Byte.toUnsignedLong(input.get());
		value <<= bitsWritten;
		scratch |= value;
		bitsWritten += Byte.SIZE;
	}
	
	public void byteAllign() {
		bitsWritten = 0;
		scratch = 0;
	}
	
	public int read(int bits) {
		assert bits <= 32 && bits > 0;
		
		while(bitsWritten < bits) fillScratch();
		
		int value = (int) scratch << 32 - bits >>> 32 - bits;
		scratch >>= bits;
		bitsWritten -= bits;
		return value;
	}

	public boolean readBoolean() {
		return read(1) == 1;
	}
}
