package abstractgame.world.map;

import abstractgame.world.World;

/** Map objects are non-player elements that make up a map */
public interface MapObject {
	void addToWorld(World world);
	
	/** If this object does not have an ID this method will
	 * return null */
	String getID();

	void setID(String ID);
}
