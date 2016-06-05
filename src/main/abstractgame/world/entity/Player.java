package abstractgame.world.entity;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import javax.vecmath.Quat4f;
import javax.vecmath.Vector3f;

import com.bulletphysics.collision.shapes.BoxShape;
import com.bulletphysics.collision.shapes.CollisionShape;
import com.bulletphysics.collision.shapes.SphereShape;
import com.bulletphysics.linearmath.DefaultMotionState;
import com.bulletphysics.linearmath.Transform;

import abstractgame.Client;
import abstractgame.Common;
import abstractgame.io.model.ModelLoader;
import abstractgame.io.user.keybinds.BindGroup;
import abstractgame.net.Identity;
import abstractgame.net.PlayerDataHandler;
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
	static CollisionShape playerShape = new SphereShape(1f);
	
	protected final List<Runnable> onTick = new ArrayList<>();
	
	BindGroup keybinds;
	float heat = .5f;
	
	protected List<Runnable> onHeat = new ArrayList<>();
	
	Identity id;
	List<UpgradeModule> modules = new ArrayList<>();
	RenderEntity renderEntity;

	public Player(Identity id) {
		super(playerShape, new Vector3f(), new Quat4f(0, 0, 0, 1), getModelOffset());
		
		this.id = id;
	}
	
	public Player(ByteBuffer buffer) {
		this(PlayerDataHandler.getIdentity(buffer.getInt()));
		
		updateState(buffer);
	}
	
	@Override
	public void initialize() {
		if(!Common.isSeverSide()) {
			renderEntity = new RenderEntity(ModelLoader.loadModel("monkey"), this, new Vector3f(), new Quat4f(0, 0, 0, 1));
			if(Client.getIdentity().equals(id)) {
				GameScreen.setPlayerEntity(this);
				keybinds = new BindGroup("player");
				keybinds.deactivate();
				
				modules.add(new BasicMovement(this));
			}
		}
			
		super.initialize();
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
		onTick.forEach(Runnable::run);
		
		if(!Common.isSeverSide() && Camera.host == this)
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
		
		if(!Common.isSeverSide()) {
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
		
		if(!Common.isSeverSide()) {
			keybinds.deactivate();
			
			if(id.equals(Client.getIdentity())) {
				FreeCamera.enable();
			}
		}
		
		Common.getWorld().removeOnTick(this);
	}

	@Override
	public Identity getController() {
		return id;
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
		return keybinds;
	}
}
