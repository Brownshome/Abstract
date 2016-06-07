package abstractgame.net.packet;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import abstractgame.net.Identity;
import abstractgame.net.Side;
import abstractgame.util.ApplicationException;

/** Represents and data sent between the client and server, implementers of this class
 * MUST define {@code <innit>(ByteBuffer)} and call {@link Packet#regesterPacket(Class)} */
public abstract class Packet {
	public static final List<Function<ByteBuffer, Packet>> PACKET_READERS = new ArrayList<>();
	public static final Map<Class<? extends Packet>, Integer> IDS = new HashMap<>();
	
	/** This method must be called in the same order in ALL situations */
	public static <T extends Packet> void regesterPacket(Class<T> packet) {
		try {
			IDS.put(packet, PACKET_READERS.size());
			Constructor<T> constructor = packet.getConstructor(ByteBuffer.class);
			PACKET_READERS.add((ByteBuffer b) -> {
				try {
					return constructor.newInstance(b);
				} catch(InstantiationException | IllegalAccessException | IllegalArgumentException e) {
					throw new ApplicationException(packet.getSimpleName() + " was regestered improperly", e, "NET");
				} catch(InvocationTargetException e) {
					throw new ApplicationException("Error handling packet " + packet.getSimpleName(), e, "NET");
				}
			});
		} catch (NoSuchMethodException | SecurityException | IllegalArgumentException e) {
			throw new ApplicationException(packet.getName() + " was regestered improperly", "NET");
		}
	}
	/** Runs the handler for this packet, id will only be populated on the serverSide,
	 * on the client it will be null, this can be used as a test for whether the packet
	 * is on the server or the client. 
	 * 
	 * @param id The id representing the sender of the packet */
	public void handle(Identity id) {
		if(id == null)
			handleClient();
		else
			handleServer(id);
	}
	
	public void handleClient() { 
		throw new ApplicationException("Unhandled packet on client " + getClass(), "NET"); 
	}
	
	public void handleServer(Identity id) { 
		throw new ApplicationException("Unhandled packet on server " + getClass(), "NET");
	}

	public abstract void fill(ByteBuffer output);
	
	/** @return The maximum size of the packet payload in bytes */
	public abstract int getPayloadSize();
}
