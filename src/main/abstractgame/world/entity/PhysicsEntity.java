package abstractgame.world.entity;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import javax.vecmath.Quat4f;
import javax.vecmath.Vector2f;
import javax.vecmath.Vector3f;

import abstractgame.world.Destroyable;
import abstractgame.world.Destroyer;
import abstractgame.world.Tickable;
import abstractgame.world.TickableImpl;
import abstractgame.world.World;

import com.bulletphysics.collision.shapes.CollisionShape;
import com.bulletphysics.dynamics.RigidBody;
import com.bulletphysics.linearmath.DefaultMotionState;
import com.bulletphysics.linearmath.MotionState;
import com.bulletphysics.linearmath.Transform;

/** Represents an entity that exists in the physics engine */
public abstract class PhysicsEntity implements MovableEntity, Collidable {
	//primary quantities
	protected RigidBody body;
	protected boolean movePhysBody = false;
	protected final Transform physicsOffset;
	
	//secondary
	private final Transform transform;
	private final Quat4f orientation;
	
	public PhysicsEntity(CollisionShape shape, Vector3f position, Quat4f orientation, Transform physicsOffset) {
		Transform tmp = new Transform();
		tmp.setRotation(orientation);
		tmp.origin.set(position);
		
		this.physicsOffset = physicsOffset;
		
		DefaultMotionState motionState = new DefaultMotionState(tmp, physicsOffset) {
			@Override
			public void setWorldTransform(Transform t) {
				super.setWorldTransform(t);
				t.getRotation(PhysicsEntity.this.orientation);
			}
		};
		
		transform = motionState.graphicsWorldTrans;
		this.orientation = new Quat4f(orientation);
		
		//TODO get this number from somewhere
		float mass = 1;
		Vector3f inertia = new Vector3f();
		shape.calculateLocalInertia(mass, inertia);
		body = new RigidBody(mass, motionState, shape, inertia);
		
	}

	public RigidBody getRigidBody() {
		return body;
	}
	
	/**The returned transformation should not be set, use {@link MovableEntity.getOrientWritable()} and
	 * {@link MovableEntity.getPosWritable()} to change this. 
	 * 
	 * @return the transform of the model in worldspace. */
	public Transform getTransform() {
		return transform;
	}
	
	@Override
	public Vector3f getPosition() {
		return transform.origin;
	}
	
	@Override
	public Quat4f getOrientation() {
		return orientation;
	}

	@Override
	public void flushChanges() {
		transform.setRotation(orientation);
		body.setWorldTransform(getRigidBody().getMotionState().getWorldTransform(new Transform()));
	}
	
	@Override
	public void onAddedToWorld(World world) {
		world.physicsWorld.addRigidBody(getRigidBody());
	}
}
