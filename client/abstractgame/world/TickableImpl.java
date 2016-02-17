package abstractgame.world;

import java.util.ArrayList;
import java.util.List;

public class TickableImpl implements Tickable {
	protected final List<Runnable> onTick = new ArrayList<>(); 
	
	@Override
	public void tick() {
		onTick.forEach(Runnable::run);
	}

	@Override
	public void onTick(Runnable r) {
		onTick.add(r);
	}
}
