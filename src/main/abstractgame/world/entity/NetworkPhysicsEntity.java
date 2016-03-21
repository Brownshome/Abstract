package abstractgame.world.entity;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;

import javax.vecmath.Quat4f;
import javax.vecmath.Vector3f;

import com.bulletphysics.collision.shapes.CollisionShape;
import com.bulletphysics.linearmath.Transform;

import abstractgame.Server;
import abstractgame.net.Connection;
import abstractgame.net.Identity;
import abstractgame.net.packet.EntitySpawnPacket;
import abstractgame.world.World;

/** This syncs position, velocity, orientation and angular velocity, the position used
 * is the center of mass as it's motion will be more predictable by the interpolation
 * engine.
 * */

//TODO add interpolation

public abstract class NetworkPhysicsEntity extends PhysicsEntity implements NetworkEntity {
public NetworkPhysicsEntity(CollisionShape shape, Vector3f position, Quat4f orientation, Transform physicsOffset) {
		super(shape, position, orientation, physicsOffset);
	}

	@Override
	public int getStateUpdateLength() {
		return 13 * Float.BYTES;
	}

	//I have to use new here :( why make it mulable if you don't expose the references
	@Override
	public void fillStateUpdate(ByteBuffer buffer) {
		//position
		Vector3f tmp = body.getCenterOfMassPosition(new Vector3f());
		buffer.putFloat(tmp.x).putFloat(tmp.y).putFloat(tmp.z);
		
		//velocity
		body.getLinearVelocity(tmp);
		buffer.putFloat(tmp.x).putFloat(tmp.y).putFloat(tmp.z);
		
		//orientation
		Quat4f orientation = getOrientation();
		buffer.putFloat(orientation.x).putFloat(orientation.y).putFloat(orientation.z).putFloat(orientation.w);
		
		//angularVelocity
		body.getAngularVelocity(tmp);
		buffer.putFloat(tmp.x).putFloat(tmp.y).putFloat(tmp.z);
	}

	//same here :(
	@Override
	public void updateState(ByteBuffer buffer) {
		Transform transform = new Transform();
		Vector3f tmp = new Vector3f();
		
		//position
		transform.origin.x = buffer.getFloat();
		transform.origin.y = buffer.getFloat();
		transform.origin.z = buffer.getFloat();
		
		//velocity
		tmp.x = buffer.getFloat();
		tmp.y = buffer.getFloat();
		tmp.z = buffer.getFloat();
		body.setLinearVelocity(tmp);
		
		//orientation
		//TODO investigate setting the orientation on the rigidBody
		Quat4f quat = new Quat4f();
		quat.x = buffer.getFloat();
		quat.y = buffer.getFloat();
		quat.z = buffer.getFloat();
		quat.w = buffer.getFloat();
		transform.setRotation(quat);
		
		body.setCenterOfMassTransform(transform);
		body.getMotionState().setWorldTransform(transform);
		
		//angularVelocity
		tmp.x = buffer.getFloat();
		tmp.y = buffer.getFloat();
		tmp.z = buffer.getFloat();
		body.setAngularVelocity(tmp);
	}
	
	@Override
	public void onAddedToWorld(World world) {
		super.onAddedToWorld(world);
		
		EntitySpawnPacket packet = new EntitySpawnPacket(this);
		if(Server.isSeverSide()) {
			for(Connection c : Server.getConnections()) {
				c.send(packet);
			}
		}
	}
}
