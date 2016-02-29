package abstractgame.net.packet;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

import abstractgame.io.config.ConfigFile;

public abstract class Packet {
	public static List<Function<byte[], Packet>> packetReaders = Arrays.asList(
			VersionPacket::new
			);
	
	/** Runs the handler for this packet */
	public abstract void handle();
	public abstract byte[] send();
}
