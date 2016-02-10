package abstractgame.world.entity;

import javax.vecmath.Quat4f;
import javax.vecmath.Vector3f;

public class BasicEntity implements Entity {
	Vector3f position;
	Quat4f orientation;
	
	@Override
	public Vector3f getPosition() {
		return position;
	}

	@Override
	public Quat4f getOrientation() {
		return orientation;
	}

}
