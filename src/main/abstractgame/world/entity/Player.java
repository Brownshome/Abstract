package abstractgame.world.entity;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import javax.vecmath.Vector3f;

import com.bulletphysics.collision.shapes.BoxShape;
import com.bulletphysics.linearmath.DefaultMotionState;

import abstractgame.net.Identity;
import abstractgame.render.CameraHost;
import abstractgame.util.FloatSupplier;
import abstractgame.world.entity.playermodules.UpgradeModule;

/** This class represents a player object */
public class Player extends DynamicEntity implements CameraHost {
	double heat;
	
	protected List<Runnable> onHeat = new ArrayList<>();
	
	Identity id;
	List<UpgradeModule> modules = new ArrayList<>();

	public Player(Identity id) {
		super(0, new DefaultMotionState(), new BoxShape(new Vector3f(.5f, .5f, .5f)), new Vector3f(0, 0, 0));
		
		this.id = id;
	}
	
	/** Gets the list of currently installed modules on
	 * this player */
	public List<UpgradeModule> getLoadout() {
		return modules;
	}
	
	@Override
	public void tick() {
		super.tick();
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
		// TODO Auto-generated method stub
		
	}
}
