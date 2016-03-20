package abstractgame.world.entity;

import javax.vecmath.Quat4f;
import javax.vecmath.Vector3f;

import abstractgame.world.World;

/** Represents any movable object that has a position and an orientation */
public interface Entity {
	/** The returned vector should not be edited, it will be updated by the Entity */
	Vector3f getPosition();
	
	/** The returned quaternion should not be edited, it will be updated by the Entity */
	Quat4f getOrientation();

	/** Called when this entity is first added to the world */
	default void onAddedToWorld(World world) {};
}
