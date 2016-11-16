package abstractgame.world.entity;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import javax.vecmath.Quat4f;
import javax.vecmath.Vector3f;

import com.bulletphysics.collision.broadphase.*;
import com.bulletphysics.collision.dispatch.*;
import com.bulletphysics.collision.narrowphase.*;
import com.bulletphysics.collision.shapes.*;
import com.bulletphysics.linearmath.*;
import com.bulletphysics.util.ObjectArrayList;

import abstractgame.Client;
import abstractgame.Common;
import abstractgame.io.model.ModelLoader;
import abstractgame.io.user.Console;
import abstractgame.io.user.keybinds.BindGroup;
import abstractgame.net.Identity;
import abstractgame.net.PlayerDataHandler;
import abstractgame.net.packet.Packet;
import abstractgame.render.*;
import abstractgame.ui.GameScreen;
import abstractgame.util.FloatSupplier;
import abstractgame.util.Util;
import abstractgame.world.Destroyable;
import abstractgame.world.Destroyer;
import abstractgame.world.Tickable;
import abstractgame.world.World;
import abstractgame.world.entity.playermodules.BasicMovement;
import abstractgame.world.entity.playermodules.Heatsink;
import abstractgame.world.entity.playermodules.UpgradeModule;

/** This class represents a player object */
public class Player extends NetworkPhysicsEntity implements CameraHost, Tickable, Destroyable, Destroyer, NetworkEntity {
	static CollisionShape playerShape = new CylinderShape(new Vector3f(.25f, .25f, 1f));
	static CollisionShape sensorShape = new CylinderShape(new Vector3f(.3f, .3f, 1.25f));
	
	protected final List<Runnable> onTick = new ArrayList<>();
	protected final List<Runnable> onHeat = new ArrayList<>();
	
	BindGroup keybinds;
	float heat = .5f;
	Identity id;
	List<UpgradeModule> modules = new ArrayList<>();
	RenderEntity renderEntity;
	PairCachingGhostObject sensorObject;
	BasicMovement movementHandler;
	Vector3f lookDirection;
	
	Player(Identity id, boolean isSlave) {
		super(playerShape, new Vector3f(), new Quat4f(0, 0, 0, 1), getModelOffset(), isSlave);
		
		DefaultMotionState oldMotionState = (DefaultMotionState) getRigidBody().getMotionState();
		
		DefaultMotionState newState = new DefaultMotionState(oldMotionState.startWorldTrans, oldMotionState.centerOfMassOffset);
		getRigidBody().setMotionState(newState);
		transform = newState.graphicsWorldTrans;
		
		this.id = id;
		
		useCustomMask(CollisionFilterGroups.CHARACTER_FILTER, (short) (CollisionFilterGroups.ALL_FILTER & ~CollisionFilterGroups.SENSOR_TRIGGER));
		
		getRigidBody().setActivationState(CollisionObject.DISABLE_DEACTIVATION);
	}

	/** The constructor to be called on the server side */
	public Player(Identity id) {
		this(id, false);
	}
	
	/** The constructor to be called on the client side */
	public Player(ByteBuffer buffer) {
		this(PlayerDataHandler.getIdentity(buffer.getInt()), true);
		
		updateState(buffer);
	}
	
	public BasicMovement getMovementHandler() {
		return movementHandler;
	}
	
	@Override
	public void initializeClient() {
		renderEntity = new RenderEntity(ModelLoader.loadModel("Monkey"), this, new Vector3f(), new Quat4f(0, 0, 0, 1));
			
		if(Client.getIdentity().equals(id)) {
			GameScreen.setPlayerEntity(this);
			keybinds = new BindGroup("player");
			keybinds.deactivate();
			modules.add(movementHandler);
		}
			
		super.initializeClient();
	}
	
	@Override
	public void fillStateUpdate(ByteBuffer buffer) {
		//position
		Vector3f tmp = body.getCenterOfMassPosition(new Vector3f());
		buffer.putFloat(tmp.x).putFloat(tmp.y).putFloat(tmp.z);

		//velocity
		body.getLinearVelocity(tmp);
		buffer.putFloat(tmp.x).putFloat(tmp.y).putFloat(tmp.z);

		//orientation
		buffer.putFloat(0).putFloat(0).putFloat(0).putFloat(1);

		//angularVelocity
		body.getAngularVelocity(tmp);
		buffer.putFloat(tmp.x).putFloat(tmp.y).putFloat(tmp.z);
	}
	
	/** This method regesters the ghost object for this player */
	@Override
	public void initializeCommon() {
		movementHandler = new BasicMovement(this);
		
		sensorObject = new PairCachingGhostObject();
		sensorObject.setCollisionShape(sensorShape);
		sensorObject.setCollisionFlags(CollisionFlags.NO_CONTACT_RESPONSE);
		
		Common.getWorld().physicsWorld.addCollisionObject(sensorObject, CollisionFilterGroups.SENSOR_TRIGGER, (short) (CollisionFilterGroups.ALL_FILTER & ~CollisionFilterGroups.CHARACTER_FILTER));
	}
	
	@Override
	public void flushChanges() {
		body.setWorldTransform(getRigidBody().getMotionState().getWorldTransform(new Transform()));
	}
	
	private static Transform getModelOffset() {
		Transform t = new Transform();
		t.basis.setIdentity();
		t.origin.set(0, 0, 0);
		return t;
	}
	
	/** Gets the list of currently installed modules on
	 * this player */
	public List<UpgradeModule> getLoadout() {
		return modules;
	}
	
	@Override
	public void run() {
		ObjectArrayList<BroadphasePair> pairs = sensorObject.getOverlappingPairCache().getOverlappingPairArray();
		ObjectArrayList<PersistentManifold> manifoldArray = new ObjectArrayList<>();
		
		movementHandler.isOnGround = false;
		
		loop:
		for(BroadphasePair pair : pairs) {
			BroadphasePair worldPair = Common.getWorld().physicsWorld.getPairCache().findPair(pair.pProxy0, pair.pProxy1);
			if(worldPair == null)
				continue;
			
			worldPair.algorithm.getAllContactManifolds(manifoldArray);
			
			for(PersistentManifold pm : manifoldArray) {
				for(int i = 0; i < pm.getNumContacts(); i++) {
					float sign = pm.getBody0() ==  sensorObject ? 1.0f : -1.0f;
					
					ManifoldPoint mp = pm.getContactPoint(i);
					if(mp.getDistance() < 0) {
						if(mp.normalWorldOnB.y * sign > 0.707) {
							movementHandler.isOnGround = true;
							movementHandler.surfaceNormal.set(mp.normalWorldOnB);
							movementHandler.surfaceNormal.scale(sign);
							movementHandler.surfaceNormal.normalize();
							break loop;
						}
					}
				}
			}
			
			manifoldArray.clear();
		}
		
		//TODO move this to motionstate
		Transform trans = new Transform();
		getRigidBody().getWorldTransform(trans);
		sensorObject.setWorldTransform(trans);
		
		onTick.forEach(Runnable::run);
		
		if(Common.isClientSide() && Camera.host == this)
			Camera.recalculate();
		
		if(heat < 0)
			heat = 0;
		
		if(heat > 1) {
			//TODO die
		}
	}

	@Override
	public void onTick(Runnable r) {
		onTick.add(r);
	}
	
	@Override
	public void removeOnTick(Runnable r) {
		onTick.remove(r);
	}
	
	public void onHeat(Runnable r) {
		onHeat.add(r);
	}
	
	public void onHeat(Consumer<Player> action) {
		onHeat.add(() -> { action.accept(this); });
	}
	
	public void addHeat(float heat) {
		this.heat += heat;
		
		if(heat > 0)
			onHeat.forEach(Runnable::run);
	}

	public float getHeat() {
		return heat;
	}

	@Override
	public void onCameraUnset() {
		ModelRenderer.addDynamicModel(renderEntity);
		keybinds.deactivate();
	}
	
	@Override
	public void onCameraSet() {
		ModelRenderer.removeDynamicModel(renderEntity);
		keybinds.activate();
	}

	private static final Vector3f OFFSET = new Vector3f(0, 0, 0);
	@Override
	public Vector3f getOffset() {
		return OFFSET;
	}
	
	@Override
	public void onAddedToWorld(World world) {
		super.onAddedToWorld(world);
		world.onTick(this);
		
		if(Common.isClientSide()) {
			if(id.equals(Client.getIdentity())) {
				//we are spawning in
				//Removes the 'Spawning...' message
				GameScreen.INSTANCE.respawnTimer = null;
				Camera.setCameraHost(this);
				
				keybinds.activate();
			} else {
				// someone else spawning in
				ModelRenderer.addDynamicModel(renderEntity);
			}
		}
	}
	
	protected final List<BiConsumer<Destroyable, Destroyer>> onDestroy = new ArrayList<>(); 
	@Override
	public void onDestroy(BiConsumer<Destroyable, Destroyer> action) {
		onDestroy.add(action);
	}

	@Override
	public void destroy(Destroyer destroyer) {
		onDestroy.forEach(a -> a.accept(this, destroyer));
		
		if(Common.isClientSide()) {
			keybinds.deactivate();
			
			if(id.equals(Client.getIdentity())) {
				FreeCamera.enable();
			}
		}
		
		Common.getWorld().removeOnTick(this);
	}

	@Override
	public int getCreateLength() {
		return super.getStateUpdateLength() + Integer.BYTES;
	}

	@Override
	public void fillCreateData(ByteBuffer buffer) {
		buffer.putInt(id.uuid);
		super.fillStateUpdate(buffer);
	}

	public BindGroup getKeybinds() {
		assert Common.isClientSide();
		
		return keybinds;
	}

	/** This class is static so as to not break the packet reflection systems */
	public static class UserInputPacket extends Packet {
		Player player;
		
		UserInputPacket(Player player) {
			assert player == GameScreen.getPlayerEntity() && Common.isClientSide();
			
			this.player = player;
		}
		
		@Override
		public void fill(ByteBuffer output) {
			//desired velocity
			//isJumpPressed
			//lookDirection
		}

		@Override
		public int getPayloadSize() {
			assert false : "not implemented";
			return 0;
		}
		
	}
	
	public void sendUserInputs() {
		//TODO assert false : "not implemented";
		
	}
}
