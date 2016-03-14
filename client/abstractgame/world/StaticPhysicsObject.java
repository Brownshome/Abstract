package abstractgame.world;

import javax.vecmath.Quat4f;
import javax.vecmath.Vector3f;

import com.bulletphysics.collision.shapes.CollisionShape;
import com.bulletphysics.dynamics.RigidBody;
import com.bulletphysics.linearmath.MotionState;
import com.bulletphysics.linearmath.Transform;

import abstractgame.io.model.PhysicsMeshLoader;
import abstractgame.world.entity.BasicEntity;

public class StaticPhysicsObject extends BasicEntity implements MapObject {
	RigidBody body;
	
	StaticPhysicsObject(CollisionShape shape, Vector3f position, Quat4f orientation) {
		super(position, orientation);
		
		body = new RigidBody(0, new MotionState() {
			@Override
			public Transform getWorldTransform(Transform out) {
				out.basis.set(StaticPhysicsObject.super.getOrientation());
				out.origin.set(StaticPhysicsObject.super.getPosition());
				
				return out;
			}

			@Override
			public void setWorldTransform(Transform worldTrans) {
				//do nothing, this is a static object
			}
			
		}, shape);
	}
	
	@Override
	public void addToWorld(World world) {
		world.physicsWorld.addRigidBody(body);
	}
}
