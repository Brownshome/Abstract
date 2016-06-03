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
import com.bulletphysics.linearmath.DefaultMotionState;
import com.bulletphysics.linearmath.Transform;

import abstractgame.Common;
import abstractgame.io.model.ModelLoader;
import abstractgame.net.Identity;
import abstractgame.net.PlayerDataHandler;
import abstractgame.render.Camera;
import abstractgame.render.CameraHost;
import abstractgame.render.ModelRenderer;
import abstractgame.render.RenderEntity;
import abstractgame.ui.GameScreen;
import abstractgame.util.FloatSupplier;
import abstractgame.util.Util;
import abstractgame.world.Destroyable;
import abstractgame.world.Destroyer;
import abstractgame.world.Tickable;
import abstractgame.world.World;
import abstractgame.world.entity.playermodules.Heatsink;
import abstractgame.world.entity.playermodules.UpgradeModule;

/** This class represents a player object */
public class Player extends NetworkPhysicsEntity implements CameraHost, Tickable, Destroyable, Destroyer, NetworkEntity {
	static CollisionShape playerShape = new BoxShape(new Vector3f(.5f, .5f, .5f));
	protected final List<Runnable> onTick = new ArrayList<>(); 
	
	float heat = .5f;
	
	protected List<Runnable> onHeat = new ArrayList<>();
	
	Identity id;
	List<UpgradeModule> modules = new ArrayList<>();
	RenderEntity renderEntity;

	public Player(ByteBuffer buffer) {
		super(playerShape, new Vector3f(), new Quat4f(0, 0, 0, 1), getModelOffset());
		id = PlayerDataHandler.getIdentity(buffer.getInt());
		
		updateState(buffer);
		
		modules.add(new Heatsink(this));
	}
	
	@Override
	public void initialize() {
		if(!Common.isSeverSide())
			renderEntity = new RenderEntity(ModelLoader.loadModel("box"), this, new Vector3f(), new Quat4f(0, 0, 0, 1));
		
		super.initialize();
	}
	
	private static Transform getModelOffset() {
		Transform t = new Transform();
		t.basis.setIdentity();
		t.origin.set(-.5f, -.5f, -.5f);
		return t;
	}
	
	public Player(Identity id) {
		super(playerShape, new Vector3f(), new Quat4f(0, 0, 0, 1), getModelOffset());
		
		if(!Common.isSeverSide())
			renderEntity = new RenderEntity(ModelLoader.loadModel("box"), this, new Vector3f(), new Quat4f(0, 0, 0, 1));
		
		this.id = id;
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
		// TODO Unregester player keybinds?
	}

	private static final Vector3f OFFSET = new Vector3f(.5f, .5f, -5f);
	@Override
	public Vector3f getOffset() {
		return OFFSET;
	}
	
	@Override
	public void onAddedToWorld(World world) {
		if(!Common.isSeverSide())
			ModelRenderer.addDynamicModel(renderEntity);
		
		super.onAddedToWorld(world);
		
		world.onTick(this);
		
		if(!Common.isSeverSide()) {
			GameScreen.setPlayerEntity(this);
			GameScreen.INSTANCE.respawnTimer = null;
			Camera.setCameraHost(this);
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
}
