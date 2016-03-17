package abstractgame.world.entity;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import abstractgame.render.CameraHost;
import abstractgame.world.entity.modules.UpgradeModule;

public class Player extends DynamicEntity implements CameraHost {
	double heat;
	protected List<Runnable> onHeat = new ArrayList<>();
	
	public List<UpgradeModule> modules = new ArrayList<>();
	
	public Player() {
		super(0, null, null, null);
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
