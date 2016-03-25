package abstractgame.world.map;

import javax.vecmath.Quat4f;
import javax.vecmath.Vector3f;

import abstractgame.world.World;
import abstractgame.world.entity.BasicEntity;

import com.bulletphysics.collision.shapes.CollisionShape;
import com.bulletphysics.dynamics.RigidBody;
import com.bulletphysics.linearmath.MotionState;
import com.bulletphysics.linearmath.QuaternionUtil;
import com.bulletphysics.linearmath.Transform;

public class StaticMapObject extends BasicEntity implements MapObject {
	RigidBody body;
	String ID;
	
	public StaticMapObject(CollisionShape shape, Vector3f position, Quat4f orientation, Vector3f offset) {
		super(position, orientation);
		
		body = new RigidBody(0, new MotionState() {
			@Override
			public Transform getWorldTransform(Transform out) {
				out.basis.set(getOrientation());
				
				if(offset == null)
					out.origin.set(getPosition());
				else {
					QuaternionUtil.quatRotate(getOrientation(), offset, out.origin);
					out.origin.add(getPosition());
				}
					
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
	
	@Override
	public String getID() {
		return ID;
	}

	@Override
	public void setID(String ID) {
		this.ID = ID;
	}
}
