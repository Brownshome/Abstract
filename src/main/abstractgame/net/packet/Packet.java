package abstractgame.net.packet;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import abstractgame.net.Identity;
import abstractgame.net.Side;
import abstractgame.util.ApplicationException;

public abstract class Packet {
	public static final List<Function<byte[], Packet>> PACKET_READERS = new ArrayList<>();
	
	/** This method must be called in the same order in ALL situations */
	public static void regesterPacket(Class packet) {
		try {
			Constructor constructor = packet.getConstructor(byte[].class);
			PACKET_READERS.add((byte[] b) -> {
				try {
					return (Packet) constructor.newInstance(b);
				} catch(InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
					throw new ApplicationException(packet.getName() + " was regestered inproperly", e, "NET");
				}
			});
		} catch (NoSuchMethodException | SecurityException | IllegalArgumentException e) {
			throw new ApplicationException(packet.getName() + " was regestered inproperly", "NET");
		}
	}
	/** Runs the handler for this packet, id will only be populated on the serverSide,
	 * on the client it will be null, this can be used as a test for whether the packet
	 * is on the server or the client. */
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

	public abstract void fill(byte[] data, int offset);
	
	/** Gets the side that should handle this packet */
	public abstract Side getHandleSide();
}
