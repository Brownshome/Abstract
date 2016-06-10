package abstractgame.world.entity.playermodules;

import javax.vecmath.*;

import org.lwjgl.input.Keyboard;

import com.bulletphysics.BulletGlobals;
import com.bulletphysics.linearmath.QuaternionUtil;

import abstractgame.*;
import abstractgame.io.user.PerfIO;
import abstractgame.io.user.keybinds.BindGroup;
import abstractgame.render.Camera;
import abstractgame.ui.elements.ModuleElement.State;
import abstractgame.util.*;
import abstractgame.world.entity.Player;

/** This module provides basic ground movement and rudimentry air-strafing */
public class BasicMovement extends UpgradeModule {
	static final float MAX_FORCE = 50;
	static final float DELTA_MAX = 4f;
	
	static final float MAX_SPEED = 3f;
	static final float JUMP_SPEED = 3f;
	
	public Vector3f surfaceNormal = new Vector3f(0, 1, 0);
	public boolean isOnGround;
	
	protected boolean forward = false;
	protected boolean backward = false;
	protected boolean left = false;
	protected boolean right = false;
	
	public BasicMovement(Player owner) {
		super(owner);
		
		if(Common.isClientSide() && Client.getIdentity() == owner.getController()) {
			BindGroup group = owner.getKeybinds();
			group.add(() -> forward = true, Keyboard.KEY_W, PerfIO.BUTTON_DOWN, "forward");
			group.add(() -> backward = true, Keyboard.KEY_S, PerfIO.BUTTON_DOWN, "backward");
			group.add(() -> left = true, Keyboard.KEY_A, PerfIO.BUTTON_DOWN, "left");
			group.add(() -> right = true, Keyboard.KEY_D, PerfIO.BUTTON_DOWN, "right");
			group.add(this::crouch, Keyboard.KEY_LSHIFT, PerfIO.BUTTON_DOWN, "crouch");
			group.add(this::jump, Keyboard.KEY_SPACE, PerfIO.BUTTON_DOWN, "jump");
			
			owner.onTick(this::tick);
		}
		
		owner.getRigidBody().setAngularFactor(0);
	}

	protected void tick() {
		moveOrientation();
		doGroundMovement();
	}
	
	protected void doGroundMovement() {
		if(!isOnGround)
			return;
		
		//ground movement is as follows, the arrow key dictate the target velocity, and accelleration is
		//added to achieve that. If opposing keys are held the target velocity is set to zero.
		
		// This is the velocity in the normal plane in the player space
		Vector3f targetVelocity = new Vector3f();
		if(forward) targetVelocity.z += 1;
		if(backward) targetVelocity.z -= 1;
		if(left) targetVelocity.x -= 1;
		if(right) targetVelocity.x += 1;
		
		if(forward != backward || left != right) {
			targetVelocity.normalize();
			targetVelocity.scale(MAX_SPEED);
		}
		
		forward = false;
		backward = false;
		left = false;
		right = false;
		
		Vector3f forward = new Vector3f(0, 0, 1);
		QuaternionUtil.quatRotate(owner.getOrientation(), forward, forward);
		
		//right = normal x forward
		//forward = right x normal
		Vector3f right = new Vector3f();
		right.cross(surfaceNormal, forward);
		forward.cross(right, surfaceNormal);
		right.normalize();
		forward.normalize();
		
		Matrix3f transform = new Matrix3f();
		transform.setColumn(0, right);
		transform.setColumn(1, surfaceNormal);
		transform.setColumn(2, forward);
		
		transform.transform(targetVelocity);
		
		Vector3f delta = new Vector3f();
		owner.getRigidBody().getLinearVelocity(delta);
		delta.sub(targetVelocity, delta);
		
		//feet can't pull and don't need to push for now
		//Ds = D - N(N.D)
		Vector3f parComp = new Vector3f(surfaceNormal);
		parComp.scale(surfaceNormal.dot(delta));
		delta.sub(delta, parComp);
		
		owner.getRigidBody().applyCentralForce(scaleForce(delta));
	}
	
	protected Vector3f scaleForce(Vector3f delta) {
		if(delta.lengthSquared() > DELTA_MAX) {
			delta.normalize();
			delta.scale(MAX_FORCE);
		} else {
			delta.scale(MAX_FORCE / DELTA_MAX);
		}
		
		return delta;
	}
	
	protected void moveOrientation() {
		if(Camera.host != owner || !PerfIO.holdMouse)
			return;
		
		AxisAngle4f tmp = new AxisAngle4f(Camera.right, -PerfIO.dy * 0.001f);
		
		Quat4f rot = new Quat4f();
		rot.set(tmp);
		owner.getOrientWritable().mul(rot, owner.getOrientation());
		owner.getOrientWritable().normalize();
		
		tmp.set(0, Camera.up.y > 0 ? 1 : -1, 0, PerfIO.dx * 0.001f);
		rot.set(tmp);
		owner.getOrientWritable().mul(rot, owner.getOrientWritable());
		
		owner.flushChanges();
	}
	
	@Override
	public String getDescription() {
		return Language.get("module.basic movement.desc");
	}

	public float getMaxSpeed() {
		return MAX_SPEED;
	}
	
	public float getMaxForce() {
		return MAX_FORCE;
	}
	
	@Override
	public State getState() {
		return State.HIDDEN;
	}
	
	void jump() {
		if(!isOnGround)
			return;
		
		Vector3f newVelocity = new Vector3f();
		owner.getRigidBody().getLinearVelocity(newVelocity);
		newVelocity.y = JUMP_SPEED;
		owner.getRigidBody().setLinearVelocity(newVelocity);
	}
	
	void crouch() {
		if(!isOnGround)
			return;
		
		//TODO
	}
}
