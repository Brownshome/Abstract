package abstractgame.ui;

import javax.vecmath.Color4f;
import javax.vecmath.Vector2f;

import abstractgame.Game;
import abstractgame.render.TextRenderer;

public class DebugScreen extends Screen {
	static final DebugScreen INSTANCE = new DebugScreen();
	static boolean isActive = false;
	
	@Override
	public void tick() {
		TextRenderer.addString(String.valueOf(Game.GAME_CLOCK.getTPS()), new Vector2f(-1, .95f), .05f, new Color4f(0, 0, 0, 1), 0);
	}

	public void initialize() {
		isActive = true;
	}
	
	public void destroy() {
		isActive = false;
	}
	
	public static void toggle() {
		if(!isActive) {
			Screen.addOverlay(INSTANCE);
		} else {
			Screen.removeOverlay(INSTANCE);
		}
	}
	
	public boolean isActive() {
		return isActive;
	}
}
