package abstractgame.net.packet;

import java.nio.ByteBuffer;

/* [ 3b ] Fragment Set Index
 * [ 1b ] Is Last Fragment
 * [ 1B ] Fragment Number
 * [ 1 - 1024B ] Data
 */

public class FragmentPacket extends Packet {

	@Override
	public void fill(ByteBuffer output) {
		assert false : "not implemented";

	}

	@Override
	public int getPayloadSize() {
		assert false : "not implemented";
		return 0;
	}

}
