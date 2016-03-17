package abstractgame.security;

import java.security.AllPermission;
import java.security.CodeSource;
import java.security.PermissionCollection;
import java.security.Permissions;
import java.security.Policy;
import java.security.ProtectionDomain;

import abstractgame.util.ApplicationException;
import abstractgame.world.map.MapLogicProxy;

public class GamePolicy extends Policy {
	static final Permissions ALL_PERMS;
	static final ClassLoader BASE_LOADER = ClassLoader.getSystemClassLoader();
	
	static {
		ALL_PERMS = new Permissions();
		ALL_PERMS.add(new AllPermission());
	}

	@Override
	public PermissionCollection getPermissions(ProtectionDomain pd) {
		if(pd.getClassLoader() == BASE_LOADER)
			return ALL_PERMS;
		else
			return super.getPermissions(pd);
	}
}
