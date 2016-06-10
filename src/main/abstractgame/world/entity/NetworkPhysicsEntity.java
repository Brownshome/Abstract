package abstractgame.world.entity;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;

import javax.vecmath.Quat4f;
import javax.vecmath.Vector3f;

import com.bulletphysics.collision.shapes.CollisionShape;
import com.bulletphysics.linearmath.Transform;

import abstractgame.Common;
import abstractgame.net.ClientNetHandler;
import abstractgame.net.Connection;
import abstractgame.net.Identity;
import abstractgame.net.ServerNetHandler;
import abstractgame.net.packet.NetEntityCreatePacket;
import abstractgame.world.World;

/** This syncs position, velocity, orientation and angular velocity, the position used
 * is the center of mass as it's motion will be more predictable by the interpolation
 * engine.
 * */

//TODO add interpolation

public abstract class NetworkPhysicsEntity extends PhysicsEntity implements NetworkEntity {
	int id;
	/** Whether this entity has been created due to packets */
	boolean isSlave = false;
	
	public NetworkPhysicsEntity(CollisionShape shape, Vector3f position, Quat4f orientation, Transform physicsOffset, boolean isSlave) {
		super(shape, position, orientation, physicsOffset);
		
		this.isSlave = isSlave;
		if(!isSlave) {
			//we are on the main thread and can call sensitive code
			initializeCommon();
		}
	}
	
	@Override
	public int getID() {
		return id;
	}
	
	@Override
	public void setID(int id) {
		this.id = id;
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
	public void initializeSlave() {
		isSlave = true;
		
		initializeCommon();
		
		Common.getWorld().addEntity(this);
	}

	@Override
	public void onAddedToWorld(World world) {
		super.onAddedToWorld(world);

		if(!isSlave) {
			if(Common.isServerSide()) {
				ServerNetHandler.createNetworkEntity(this);
			} else {
				ClientNetHandler.createNetworkEntity(this);
			}
		}
	}

	@Override
	public boolean needsSync() {
		return true;
	}
}
