package abstractgame.net.packet;

import java.nio.ByteBuffer;

import abstractgame.Server;
import abstractgame.net.*;
import abstractgame.net.bits.*;

/* [ 3b ] Fragment Set Index
 * [ 1b ] Is Last Fragment
 * [ 12b ] Fragment Number
 * [ 1 - 1024B ] Data
 */

/** This packet splits up packets into fragments, it is only used for MTU limited connections such as UDP */
public class FragmentPacket extends Packet {
	public static final int FRAGMENT_SIZE = 1024;
	public static final int NO_BITS = 8;
	public static final int GROUP_BITS = 3;
	public static final int FRAGMENT_HEADER_BYTES = (NO_BITS + GROUP_BITS) / Byte.SIZE + 1;
	public static final int MAX_SIZE = FRAGMENT_SIZE * (1 << NO_BITS);
	
	public final boolean isLastFragment;
	public final int fragmentGroup;
	public final int fragmentNumber;
	public final ByteBuffer data;
	
	public FragmentPacket(ByteBuffer buffer) {
		BitUnpacker unpacker = new BitUnpacker(buffer);
		fragmentGroup = unpacker.read(GROUP_BITS);
		isLastFragment = unpacker.readBoolean();
		fragmentNumber = unpacker.read(NO_BITS);
		unpacker.byteAllign(); //NOP
		data = buffer; //data in this buffer should be valid until at least after handle is called
	}
	
	/** The underlying array in buffer should remain valid but the buffer itself need not */
	public FragmentPacket(ByteBuffer buffer, int fragmentGroup, int fragmentNumber) {
		data = buffer.duplicate();
		isLastFragment = buffer.remaining() <= FRAGMENT_SIZE;
		if(!isLastFragment) {
			data.limit(data.position() + FRAGMENT_SIZE);
			buffer.position(buffer.position() + FRAGMENT_SIZE);
		} else {
			buffer.position(buffer.limit());
		}
		
		this.fragmentGroup = fragmentGroup;
		this.fragmentNumber = fragmentNumber;
	}
	
	@Override
	public void fill(ByteBuffer output) {
		BitPacker packer = new BitPacker(output);
		packer.put(fragmentGroup, GROUP_BITS);
		packer.putBoolean(isLastFragment);
		packer.put(fragmentNumber, NO_BITS);
		packer.flush(); //NB not actually required
		output.put(data);
	}

	@Override
	public void handleClient() {
		assert ServerProxy.getCurrentServerProxy().getConnection() instanceof UDPConnection;
		
		((UDPConnection) ServerProxy.getCurrentServerProxy().getConnection()).fragmentHandler.reassembleFragmentedPacket(this);
	}
	
	@Override
	public void handleServer(Identity id) {
		assert Server.getConnection(id) instanceof UDPConnection;
		
		((UDPConnection) Server.getConnection(id)).fragmentHandler.reassembleFragmentedPacket(this);
	}
	
	@Override
	public int getPayloadSize() {
		return FRAGMENT_HEADER_BYTES + FRAGMENT_SIZE;
	}
}
