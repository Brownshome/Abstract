package abstractgame.world;

import java.util.function.Consumer;

public interface Tickable extends Runnable {
	default void onTick(Consumer<Tickable> action) {
		onTick(() -> {
			action.accept(this);
		});
	}

	void removeOnTick(Runnable r);
	
	void onTick(Runnable r);
}
