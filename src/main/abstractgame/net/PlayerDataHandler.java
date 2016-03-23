package abstractgame.net;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import abstractgame.world.entity.playermodules.UpgradeModule;

/** The class responsible for retrieving and cashing data about players */
public class PlayerDataHandler {
	static HashMap<Integer, Identity> identityCashe = new HashMap<>();
	
	static {
		identityCashe.put(11257, new Identity("James Brown", 11257));
	}
	
	private static int n = 0;
	
	public static Identity getIdentity(int uuid) {
		return identityCashe.computeIfAbsent(uuid, id -> new Identity("Player " + n++, id));
	}
}
