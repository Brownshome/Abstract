package abstractgame.world.entity;

import javax.vecmath.Quat4f;
import javax.vecmath.Vector3f;

import abstractgame.world.World;

/** Represents any movable object that has a position and an orientation */
public interface Entity {
	/** The returned vector should not be edited, it will be updated by the Entity
	 * 
	 *  @return The entity's position */
	Vector3f getPosition();
	
	/** The returned quaternion should not be edited, it will be updated by the Entity 
	 * 
	 * @return The entity's orientation */
	Quat4f getOrientation();

	/** Called when this entity is first added to the world
	 * 
	 * @param world The world that the entity is being added to */
	default void onAddedToWorld(World world) {};
}
