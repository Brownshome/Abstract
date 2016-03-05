package abstractgame.net;

import java.net.InetAddress;

/*	The current security system is as follows, the password is salted with the uuid on the client.
 * 	This is used as the 'password' in a conventional hash salt system which is sent to the server
 * 	which then verifies it with the master user database.
 * 
 *  TODO Implement this
 */

/** Represents a player in the global state */
public class Identity {
	/** This field is only populated on the server */
	public InetAddress ip;
	/** This field is only populated on the server */
	public int port;
	/** This is used for displayname and other such things */
	public String username;
	/** This is used for login verification and stat tracking */
	public long uuid;
	
	public Identity(String username, long uuid) {
		this.username = username;
		this.uuid = uuid;
	}

	public void updateConnection(InetAddress ip, int port) {
		this.ip	= ip;
		this.port = port;
	}
	
	@Override
	public boolean equals(Object other) {
		return other instanceof Identity && ((Identity) other).uuid == uuid;
	}
	
	@Override
	public int hashCode() {
		return (int) uuid;
	}
}
