package abstractgame.world.entity;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

import javax.vecmath.Quat4f;
import javax.vecmath.Vector3f;

import abstractgame.world.Destroyable;
import abstractgame.world.Destroyer;
import abstractgame.world.Tickable;

import com.bulletphysics.collision.shapes.CollisionShape;
import com.bulletphysics.dynamics.RigidBody;
import com.bulletphysics.linearmath.MotionState;
import com.bulletphysics.linearmath.Transform;

/** Represents an entity that exists in the physics engine */
public abstract class DynamicEntity extends RigidBody implements Entity, Tickable, Destroyable, Collidable {
	protected final List<Runnable> onTick = new ArrayList<>();
	protected final List<BiConsumer<Destroyable, Destroyer>> onDestroy = new ArrayList<>();
	
	private final Transform transform = new Transform();
	private final Quat4f orientation = new Quat4f();
	
	public DynamicEntity(float mass, MotionState motionState, CollisionShape collisionShape, Vector3f localInertia) {
		super(mass, motionState, collisionShape, localInertia);
	}
	
	@Override
	public void tick() {
		updateTransform();
		onTick.forEach(Runnable::run);
	}
	
	/** Causes getRotation and getOrientation to return the correct values */
	protected void updateTransform() {
		super.getMotionState().getWorldTransform(transform);
		transform.getRotation(orientation);
	}
	
	@Override
	public void onTick(Runnable action) {
		onTick.add(action);
	}

	@Override
	public void destroy(Destroyer destroyer) {
		onDestroy.forEach(action -> action.accept(this, destroyer));
	}
	
	@Override
	public void onDestroy(BiConsumer<Destroyable, Destroyer> action) {
		onDestroy.add(action);
	}

	@Override
	public Vector3f getPosition() {
		return transform.origin;
	}

	@Override
	public Quat4f getOrientation() {
		return orientation;
	}
}
