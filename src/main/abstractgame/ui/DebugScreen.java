package abstractgame.ui;

import javax.vecmath.Color4f;
import javax.vecmath.Vector2f;

import abstractgame.Client;
import abstractgame.render.Camera;
import abstractgame.render.TextRenderer;

public class DebugScreen extends Screen {
	static final DebugScreen INSTANCE = new DebugScreen();
	static boolean isActive = false;
	
	@Override
	public void tick() {
		TextRenderer.addString(String.valueOf(Client.GAME_CLOCK.getTPS()), new Vector2f(-1, .95f), .05f, new Color4f(0, 0, 0, 1), 0);
		TextRenderer.addString(String.format("C: (%.2f, %.2f, %.2f)", Camera.position.x, Camera.position.y, Camera.position.z), new Vector2f(-1, .9f), .05f, new Color4f(0, 0, 0, 1), 0);
	}

	@Override
	public void initialize() {
		isActive = true;
	}
	
	@Override
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
