package abstractgame.world.entity;

import javax.vecmath.Quat4f;
import javax.vecmath.Vector3f;

public interface MovableEntity extends Entity {
	/** Returns a position that can be written to. Flush should be called after a write */
	default Vector3f getPosWritable() {
		return getPosition();
	}
	
	/** Returns a orientation that can be written to. Flush should be called after a write */
	default Quat4f getOrientWritable() {
		return getOrientation();
	}
	
	/** Propergates the changes to the underlying storage */
	default void flushChanges() {}
}
