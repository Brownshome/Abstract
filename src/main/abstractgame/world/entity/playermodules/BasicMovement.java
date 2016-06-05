package abstractgame.world.entity.playermodules;

import javax.vecmath.Vector3f;

import org.lwjgl.input.Keyboard;

import com.bulletphysics.linearmath.QuaternionUtil;

import abstractgame.io.user.PerfIO;
import abstractgame.io.user.keybinds.BindGroup;
import abstractgame.ui.elements.ModuleElement.State;
import abstractgame.util.*;
import abstractgame.world.entity.Player;

public class BasicMovement extends UpgradeModule {
	static final float FORCE = 100;
	
	public BasicMovement(Player owner) {
		super(owner);
		
		BindGroup group = owner.getKeybinds();
		group.add(this::forward, Keyboard.KEY_W, PerfIO.BUTTON_DOWN, "forward");
		group.add(this::backward, Keyboard.KEY_S, PerfIO.BUTTON_DOWN, "backward");
		group.add(this::left, Keyboard.KEY_A, PerfIO.BUTTON_DOWN, "left");
		group.add(this::right, Keyboard.KEY_D, PerfIO.BUTTON_DOWN, "right");
		group.add(this::crouch, Keyboard.KEY_RSHIFT, PerfIO.BUTTON_DOWN, "crouch");
		group.add(this::jump, Keyboard.KEY_SPACE, PerfIO.BUTTON_DOWN, "jump");
		
		owner.onTick(() -> {
			owner.getRigidBody().setAngularVelocity(Util.ZERO_VEC3);
		});
	}

	@Override
	public String getDescription() {
		return Language.get("module.basic movement.desc");
	}

	@Override
	public State getState() {
		return State.HIDDEN;
	}
	
	void forward() {
		Vector3f forward = new Vector3f(0, 0, 1);
		QuaternionUtil.quatRotate(owner.getOrientation(), forward, forward);
		forward.scale(FORCE);
		owner.getRigidBody().applyCentralForce(forward);
	}
	
	void backward() {
		Vector3f backward = new Vector3f(0, 0, -1);
		QuaternionUtil.quatRotate(owner.getOrientation(), backward, backward);
		backward.scale(FORCE);
		owner.getRigidBody().applyCentralForce(backward);
	}
	
	void left() {
		Vector3f left = new Vector3f(1, 0, 0);
		QuaternionUtil.quatRotate(owner.getOrientation(), left, left);
		left.scale(FORCE);
		owner.getRigidBody().applyCentralForce(left);
	}
	
	void right() {
		Vector3f right = new Vector3f(-1, 0, 0);
		QuaternionUtil.quatRotate(owner.getOrientation(), right, right);
		right.scale(FORCE);
		owner.getRigidBody().applyCentralForce(right);
	}
	
	void jump() {
		Vector3f jump = new Vector3f(0, 1, 0);
		QuaternionUtil.quatRotate(owner.getOrientation(), jump, jump);
		jump.scale(FORCE);
		owner.getRigidBody().applyCentralForce(jump);
	}
	
	void crouch() {
		Vector3f crouch = new Vector3f(0, -1, 0);
		QuaternionUtil.quatRotate(owner.getOrientation(), crouch, crouch);
		crouch.scale(FORCE);
		owner.getRigidBody().applyCentralForce(crouch);
	}
}
