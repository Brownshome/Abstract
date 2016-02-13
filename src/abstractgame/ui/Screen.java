package abstractgame.ui;

import java.util.ArrayList;
import java.util.List;

import abstractgame.world.Tickable;
import abstractgame.world.TickableImpl;

public abstract class Screen extends TickableImpl {
	static Screen baseScreen;
	static List<Screen> overlays = new ArrayList<>();
	
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
	}
}
