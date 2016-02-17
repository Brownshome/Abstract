package abstractgame.world.entity;

import javax.vecmath.Quat4f;
import javax.vecmath.Vector3f;

/** Represents any movable object that has a position and an orientation */
public interface Entity {
	/** The returned vector should not be edited, it will be updated by the Entity */
	public Vector3f getPosition();
	
	/** The returned quaternion should not be edited, it will be updated by the Entity */
	public Quat4f getOrientation();
}
