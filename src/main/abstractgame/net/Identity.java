package abstractgame.net;

import java.net.InetAddress;
import java.util.UUID;

import abstractgame.util.Util;

/*	The current security system is as follows, the password is salted with the uuid on the client.
 * 	This is used as the 'password' in a conventional hash salt system which is sent to the server
 * 	which then verifies it with the master user database.
 * 
 *  TODO Implement this
 */

/** Represents a player in the global state */
public class Identity {
	/** This is used for displayname and other such things */
	public String username;
	/** This is used for login verification and stat tracking */
	public int uuid;
	
	public Identity(String username, int uuid) {
		this.username = username;
		this.uuid = uuid;
	}
	
	@Override
	public boolean equals(Object other) {
		return other instanceof Identity && ((Identity) other).uuid == uuid;
	}
	
	@Override
	public int hashCode() {
		return (int) uuid;
	}
	
	@Override
	public String toString() {
		return Util.toHexString(uuid);
	}
}
