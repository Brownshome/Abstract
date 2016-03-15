package abstractgame.world;

import javax.vecmath.Quat4f;
import javax.vecmath.Vector3f;

import abstractgame.world.entity.BasicEntity;

public class MapSpawn extends BasicEntity implements MapObject {
	String ID;
	
	public MapSpawn(Vector3f position, Quat4f orientation) {
		super(position, orientation);
	}
	
	@Override
	public void addToWorld(World world) {}

	@Override
	public String getID() {
		return ID;
	}

	@Override
	public void setID(String ID) {
		this.ID = ID;
	}
}
