package abstractgame.render;

import javax.vecmath.AxisAngle4f;
import javax.vecmath.Quat4f;
import javax.vecmath.Vector2f;
import javax.vecmath.Vector3f;

import org.lwjgl.input.Keyboard;

import com.bulletphysics.linearmath.QuaternionUtil;

import abstractgame.io.user.PerfIO;
import abstractgame.io.user.keybinds.BindGroup;
import abstractgame.ui.GameScreen;
import abstractgame.util.ApplicationException;
import abstractgame.util.Util;
import abstractgame.world.TickableImpl;
import abstractgame.world.World;

/** Represents a physicsless player controled entity */
public class FreeCamera extends TickableImpl implements CameraHost {
	static final float PHYS_DELTA = 1f / 60f;
	static final float ACCEL = .3f;
	static final float DRAG = ACCEL / (15 + ACCEL);
	static final float DRAG_SLOW = ACCEL / (5 + ACCEL);
	
	static final BindGroup CAMERA_BINDS = new BindGroup("free camera");
	static {
		CAMERA_BINDS.add(FreeCamera::cameraUp, Keyboard.KEY_SPACE, PerfIO.BUTTON_DOWN, "up");
		CAMERA_BINDS.add(FreeCamera::cameraDown, Keyboard.KEY_LSHIFT, PerfIO.BUTTON_DOWN, "down");
		CAMERA_BINDS.add(FreeCamera::cameraForward, Keyboard.KEY_W, PerfIO.BUTTON_DOWN, "forward");
		CAMERA_BINDS.add(FreeCamera::cameraBackward, Keyboard.KEY_S, PerfIO.BUTTON_DOWN, "backward");
		CAMERA_BINDS.add(FreeCamera::cameraLeft, Keyboard.KEY_A, PerfIO.BUTTON_DOWN, "left");
		CAMERA_BINDS.add(FreeCamera::cameraRight, Keyboard.KEY_D, PerfIO.BUTTON_DOWN, "right");
		CAMERA_BINDS.add(() -> { FreeCamera.slow = true; }, Keyboard.KEY_LMENU, PerfIO.BUTTON_DOWN, "slow");
		CAMERA_BINDS.add(() -> { FreeCamera.slow = false; }, Keyboard.KEY_LMENU, PerfIO.BUTTON_UP, "slow");
		CAMERA_BINDS.add(FreeCamera::cameraStop, Keyboard.KEY_X, PerfIO.BUTTON_PRESSED, "stop");
	}
	
	static final String FREECAM_STRING = "Freecam Active";
	static final Vector3f COLOUR = new Vector3f(0, 0, 0);
	
	public static boolean slow = false;
	private static CameraHost oldHost = null;
	static final FreeCamera FREE_CAM = new FreeCamera(new Vector3f(), new Quat4f()) {
		@Override
		public void run() {
			super.run();
			
			TextRenderer.addString(FREECAM_STRING, new Vector2f(-1, -1 + TextRenderer.getHeight(FREECAM_STRING) * .05f), .05f, UIRenderer.BASE_STRONG, 0);

			if(PhysicsRenderer.INSTANCE.isActive()) {
				Vector3f to = new Vector3f(oldHost.getOffset());
				QuaternionUtil.quatRotate(oldHost.getOrientation(), to, to);
				
				Vector3f from = new Vector3f(oldHost.getPosition());
				from.add(to);
				
				to.set(0, 0, 1);
				QuaternionUtil.quatRotate(oldHost.getOrientation(), to, to);
				to.add(from);
				
				PhysicsRenderer.INSTANCE.drawLine(from, to, COLOUR);
				
				from.x -= .1f;
				from.y -= .1f;
				from.z -= .1f;
				
				to.x = from.x + .2f;
				to.y = from.y + .2f;
				to.z = from.z + .2f;
				
				PhysicsRenderer.INSTANCE.drawAabb(from, to, COLOUR);
			}
		}
	};
	
	public final Vector3f position;
	public final Quat4f orientation;
	public final Vector3f velocity = new Vector3f();
	
	World world;

	public static void enable() {
		throw new UnsupportedOperationException("Not implemented");
	}
	
	/** Saves the old camera host and switches to a freecam. */
	public static void toggle() {
		if(oldHost == null) {
			oldHost = Camera.host;
			FREE_CAM.position.set(Camera.position);
			FREE_CAM.orientation.set(oldHost.getOrientation());
			FREE_CAM.velocity.set(0, 0, 0);
			Camera.setCameraHost(FREE_CAM);
		} else {
			CameraHost tmp = oldHost;
			oldHost = null;
			Camera.setCameraHost(tmp);
		}
		
		assert oldHost != FREE_CAM;
	}
	
	public static boolean isActive() {
		return oldHost != null;
	}
	
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
	
	public static void cameraUp() { ((FreeCamera) Camera.host).up(); }
	public static void cameraDown() { ((FreeCamera) Camera.host).down(); }
	public static void cameraLeft() { ((FreeCamera) Camera.host).left(); }
	public static void cameraRight() { ((FreeCamera) Camera.host).right(); }
	public static void cameraForward() { ((FreeCamera) Camera.host).forward(); }
	public static void cameraBackward() { ((FreeCamera) Camera.host).backward(); }
	public static void cameraStop() { ((FreeCamera) Camera.host).stop(); }
	
	@Override
	public void run() {
		super.run();
		
		if(Camera.host != this)
			return;
			
		float drag = slow ? DRAG_SLOW : DRAG;
		
		moveOrientation();
		
		velocity.scale(1 - drag);
		position.scaleAdd(PHYS_DELTA, velocity, position);
		
		Camera.recalculate();
	}
	
	private void moveOrientation() {
		if(!PerfIO.holdMouse)
			return;
		
		AxisAngle4f tmp = new AxisAngle4f(Camera.right, -PerfIO.dy * 0.001f);
		
		Quat4f rot = new Quat4f();
		rot.set(tmp);
		orientation.mul(rot, orientation);
		orientation.normalize();
		
		tmp.set(0, Camera.up.y > 0 ? 1 : -1, 0, PerfIO.dx * 0.001f);
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
		CAMERA_BINDS.deactivate();
		world.removeOnTick(this);
	}
	
	@Override
	public void onCameraSet() {
		world = GameScreen.getWorld();
		if(world == null)
			throw new ApplicationException("There must be a world for the Free Camera to function.", "CAMERA");
		
		CAMERA_BINDS.activate();
		world.onTick(this);
	}

	public static void setOldHost(CameraHost newHost) {
		oldHost = newHost;
	}
}
