package abstractgame.world.entity;

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

import abstractgame.Server;
import abstractgame.net.Identity;
import abstractgame.render.Camera;
import abstractgame.render.CameraHost;
import abstractgame.render.RenderEntity;
import abstractgame.util.FloatSupplier;
import abstractgame.world.Destroyable;
import abstractgame.world.Destroyer;
import abstractgame.world.Tickable;
import abstractgame.world.World;
import abstractgame.world.entity.playermodules.UpgradeModule;

/** This class represents a player object */
public class Player extends PhysicsEntity implements CameraHost, Tickable, Destroyable, Destroyer {
	static CollisionShape playerShape = new BoxShape(new Vector3f(.5f, .5f, .5f));
	protected final List<Runnable> onTick = new ArrayList<>(); 
	
	double heat;
	
	protected List<Runnable> onHeat = new ArrayList<>();
	
	Identity id;
	List<UpgradeModule> modules = new ArrayList<>();
	RenderEntity renderEntity;

	public Player(Identity id) {
		super(playerShape, new Vector3f(), new Quat4f(0, 0, 0, 1), new Transform());
		
		this.id = id;
	}
	
	/** Gets the list of currently installed modules on
	 * this player */
	public List<UpgradeModule> getLoadout() {
		return modules;
	}
	
	@Override
	public void tick() {
		onTick.forEach(Runnable::run);
	}

	@Override
	public void onTick(Runnable r) {
		onTick.add(r);
	}
	
	public void onHeat(Runnable r) {
		onHeat.add(r);
	}
	
	public void onHeat(Consumer<Player> action) {
		onHeat.add(() -> { action.accept(this); });
	}
	
	public void addHeat(double heat) {
		this.heat += heat;
		
		if(heat > 0)
			onHeat.forEach(Runnable::run);
	}

	public double getHeat() {
		return heat;
	}

	@Override
	public void onCameraUnset() {
		// TODO Unregester player keybinds?
	}

	@Override
	public void onAddedToWorld(World world) {
		if(!Server.isSeverSide())
			Camera.setCameraHost(this);
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
}
