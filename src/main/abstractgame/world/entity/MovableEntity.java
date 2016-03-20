package abstractgame.world.entity;

import javax.vecmath.Vector3f;

public interface MovableEntity extends Entity {
	/** Returns a position that can be written to */
	Vector3f getPosWritable();
	/** Returns a orientation that can be written to */
	Vector3f getOrientWritable();
}
