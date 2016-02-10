package abstractgame.render;

import javax.vecmath.AxisAngle4f;
import javax.vecmath.Quat4f;
import javax.vecmath.Vector3f;

import abstractgame.io.user.KeyIO;
import abstractgame.util.Util;
import abstractgame.world.TickableImpl;

/** Represents a physicsless player controled entity */
public class FreeCamera extends TickableImpl implements CameraHost {
	static final float PHYS_DELTA = 1f / 60f;
	static final float ACCEL = .3f;
	static final float DRAG = ACCEL / (15 + ACCEL);
	static final float DRAG_SLOW = ACCEL / (5 + ACCEL);
	
	public final Vector3f position;
	public final Quat4f orientation;
	public final Vector3f velocity = new Vector3f();
	public boolean slow = false;
	
	public FreeCamera(Vector3f position, Vector3f up, Vector3f forward) {
		this.position = position;
		this.orientation = Util.getQuat(up, forward);
	}
	
	public FreeCamera(Vector3f position, Quat4f orientation) {
		this.position = position;
		this.orientation = orientation;
	}
	
	public void up() { velocity.scaleAdd(ACCEL, Camera.up, velocity); }
	public void down() { velocity.scaleAdd(-ACCEL, Camera.up, velocity); }
	public void left() { velocity.scaleAdd(-ACCEL, Camera.right, velocity); }
	public void right() { velocity.scaleAdd(ACCEL, Camera.right, velocity); }
	public void forward() { velocity.scaleAdd(ACCEL, Camera.forward, velocity); }
	public void backward() { velocity.scaleAdd(-ACCEL, Camera.forward, velocity); }
	public void stop() { velocity.set(0, 0, 0); }
	
	@Override
	public void tick() {
		super.tick();
		
		if(Camera.host != this)
			return;
			
		float drag = slow ? DRAG_SLOW : DRAG;
		
		moveOrientation();
		
		velocity.scale(1 - drag);
		position.scaleAdd(PHYS_DELTA, velocity, position);
		
		Camera.recalculate();
	}
	
	private void moveOrientation() {
		if(!KeyIO.holdMouse)
			return;
		
		AxisAngle4f tmp = new AxisAngle4f(Camera.right, -KeyIO.dy * 0.001f);
		
		Quat4f rot = new Quat4f();
		rot.set(tmp);
		orientation.mul(rot, orientation);
		orientation.normalize();
		
		tmp.set(0, Camera.up.y > 0 ? 1 : -1, 0, KeyIO.dx * 0.001f);
		rot.set(tmp);
		orientation.mul(rot, orientation);
	}

	public void setIsActive(boolean active) {
		if(active != (Camera.host == this)) {
			if(active) {
				Camera.setCameraHost(this);
			} else {
				Camera.setCameraHost(null);
			}
		}
	}
	
	@Override
	public Vector3f getPosition() {
		return position;
	}

	@Override
	public Quat4f getOrientation() {
		return orientation;
	}

	@Override
	public void onCameraUnset() {
		// TODO Auto-generated method stub
		
	}
}
