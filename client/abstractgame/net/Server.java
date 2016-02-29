package abstractgame.net;

import java.util.Collection;

public abstract class Server {
	public abstract String getName();
	public abstract Collection<Identity> getConnected();
	public abstract Connection getSafeConnection();
	public abstract Connection getUnsafeConnection();
}
