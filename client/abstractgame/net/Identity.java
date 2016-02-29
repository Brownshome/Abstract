package abstractgame.net;

/** Represents a player in the global state */
public class Identity {
	String username;
	long uuid;
	
	@Override
	public boolean equals(Object other) {
		return other instanceof Identity && ((Identity) other).uuid == uuid;
	}
	
	@Override
	public int hashCode() {
		return (int) uuid;
	}
}
