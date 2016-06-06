package abstractgame.world.entity.playermodules;

import javax.vecmath.*;

import org.lwjgl.input.Keyboard;

import com.bulletphysics.linearmath.QuaternionUtil;

import abstractgame.Common;
import abstractgame.io.user.PerfIO;
import abstractgame.io.user.keybinds.BindGroup;
import abstractgame.render.Camera;
import abstractgame.ui.elements.ModuleElement.State;
import abstractgame.util.*;
import abstractgame.world.entity.Player;

/** This module provides basic ground movement and rudimentry air-strafing */
public class BasicMovement extends UpgradeModule {
	static final float FORCE = 5;
	
	public Vector3f surfaceNormal = new Vector3f();
	public boolean isOnGround;
	
	public BasicMovement(Player owner) {
		super(owner);
		
		if(Common.isClientSide()) {
			BindGroup group = owner.getKeybinds();
			group.add(this::forward, Keyboard.KEY_W, PerfIO.BUTTON_DOWN, "forward");
			group.add(this::backward, Keyboard.KEY_S, PerfIO.BUTTON_DOWN, "backward");
			group.add(this::left, Keyboard.KEY_A, PerfIO.BUTTON_DOWN, "left");
			group.add(this::right, Keyboard.KEY_D, PerfIO.BUTTON_DOWN, "right");
			group.add(this::crouch, Keyboard.KEY_LSHIFT, PerfIO.BUTTON_DOWN, "crouch");
			group.add(this::jump, Keyboard.KEY_SPACE, PerfIO.BUTTON_DOWN, "jump");
			owner.onTick(this::moveOrientation);
		}
		
		owner.getRigidBody().setAngularFactor(0);
	}

	private void moveOrientation() {
		if(!PerfIO.holdMouse)
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

	@Override
	public State getState() {
		return State.HIDDEN;
	}
	
	//NB all of this is just for testing, I will be making a more conventional feeling
	//movement system soon.
	
	void forward() {
		Vector3f forward = new Vector3f(0, 0, 1);
		QuaternionUtil.quatRotate(owner.getOrientation(), forward, forward);
		forward.y = 0;
		forward.normalize();
		forward.scale(isOnGround ? FORCE : FORCE * .05f);
		owner.getRigidBody().applyCentralForce(forward);
	}
	
	void backward() {
		Vector3f backward = new Vector3f(0, 0, -1);
		QuaternionUtil.quatRotate(owner.getOrientation(), backward, backward);
		backward.y = 0;
		backward.normalize();
		backward.scale(isOnGround ? FORCE : FORCE * .05f);
		owner.getRigidBody().applyCentralForce(backward);
	}
	
	void left() {
		Vector3f left = new Vector3f(-1, 0, 0);
		QuaternionUtil.quatRotate(owner.getOrientation(), left, left);
		left.y = 0;
		left.normalize();
		left.scale(isOnGround ? FORCE : FORCE * .05f);
		owner.getRigidBody().applyCentralForce(left);
	}
	
	void right() {
		Vector3f right = new Vector3f(1, 0, 0);
		QuaternionUtil.quatRotate(owner.getOrientation(), right, right);
		right.y = 0;
		right.normalize();
		right.scale(isOnGround ? FORCE : FORCE * .05f);
		owner.getRigidBody().applyCentralForce(right);
	}
	
	void jump() {
		if(!isOnGround)
			return;
		
		Vector3f jump = new Vector3f(0, 1, 0);
		jump.scale(FORCE);
		owner.getRigidBody().applyCentralForce(jump);
	}
	
	void crouch() {
		if(!isOnGround)
			return;
		
		Vector3f crouch = new Vector3f(0, -1, 0);
		crouch.scale(FORCE);
		owner.getRigidBody().applyCentralForce(crouch);
	}
}
