package abstractgame.world;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class TickableImpl implements Tickable {
	protected final List<Runnable> onTick = new CopyOnWriteArrayList<>(); //TODO might be too costly?
	
	@Override
	public void tick() {
		onTick.forEach(Runnable::run);
	}

	@Override
	public void onTick(Runnable r) {
		onTick.add(r);
	}
}
