package abstractgame.world;

import java.util.function.Consumer;

public interface Tickable {
	void tick();
	
	default void onTick(Consumer<Tickable> action) {
		onTick(() -> {
			action.accept(this);
		});
	}

	default void onTick(Tickable t) {
		onTick(t::tick);
	}
	
	void onTick(Runnable r);
}
