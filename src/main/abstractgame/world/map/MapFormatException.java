package abstractgame.world.map;

import abstractgame.util.ApplicationException;

public class MapFormatException extends ApplicationException {
	public MapFormatException(String type, String name) {
		super("Map object " + type + " does not define a valid " + name + " entry.", "MAP LOADER");
	}
}
