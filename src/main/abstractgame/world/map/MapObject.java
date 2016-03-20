package abstractgame.world.map;

import java.util.Map;

import abstractgame.world.World;

/** Map objects are non-player elements that make up a map */
public interface MapObject {
	public static <T> T validate(String type, Class<T> castTo, String name, Map<String, Object> data) {
		Object value = data.get(name);
		
		if(value == null) throw new MapFormatException(type, name);
		
		try {
			return castTo.cast(value);
		} catch(ClassCastException cce) {
			throw new MapFormatException(type, name);
		}
	}
	
	void addToWorld(World world);
	
	/** If this object does not have an ID this method will
	 * return null */
	String getID();

	void setID(String ID);
}
