package abstractgame.world;

import java.util.function.BiConsumer;

public interface Destroyable {
	void onDestroy(BiConsumer<Destroyable, Destroyer> action);
	default void onDestroy(Runnable r) {
		onDestroy((t, u) -> r.run());
	}
	
	void destroy(Destroyer destroyer);
}