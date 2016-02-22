package abstractgame.ui;

import java.util.HashSet;
import java.util.Set;

import abstractgame.world.TickableImpl;

public abstract class Screen extends TickableImpl {
	static Screen baseScreen;
	static Set<Screen> overlays = new HashSet<>();
	
	public void initialize() {}
	public void destroy() {}
	
	public static Screen setScreen(Screen screen) {
		if(baseScreen != null)
			baseScreen.destroy();
		
		Screen tmp = baseScreen;
		baseScreen = screen;
		baseScreen.initialize();
		return tmp;
	}
	
	public static Screen getScreen() {
		return baseScreen;
	}
	
	public static void tickScreen() {
		baseScreen.tick();
		
		overlays.forEach(Screen::tick);
	}
	
	public static void addOverlay(Screen overlay) {
		overlays.add(overlay);
		overlay.initialize();
	}
	
	public static void removeOverlay(Screen overlay) {
		overlays.remove(overlay);
		overlay.destroy();
	}
}
