package abstractgame.ui;

import javax.vecmath.*;

import abstractgame.Client;
import abstractgame.render.Camera;
import abstractgame.render.TextRenderer;

public class DebugScreen extends Screen {
	static final DebugScreen INSTANCE = new DebugScreen();
	static boolean isActive = false;
	
	@Override
	public void run() {
		TextRenderer.addString(String.valueOf(Client.GAME_CLOCK.getTPS()), new Vector2f(-1, .95f), .05f, new Color4f(0, 0, 0, 1), 0);
		TextRenderer.addString(String.format("C: (%.2f, %.2f, %.2f)", Camera.position.x, Camera.position.y, Camera.position.z), new Vector2f(-1, .9f), .05f, new Color4f(0, 0, 0, 1), 0);
		
		if(GameScreen.getPlayerEntity() != null) {
			TextRenderer.addString("G: " + GameScreen.getPlayerEntity().getMovementHandler().isOnGround, new Vector2f(-1, .85f), .05f, new Color4f(0, 0, 0, 1), 0);
			
			Vector3f velocity = GameScreen.getPlayerEntity().getRigidBody().getLinearVelocity(new Vector3f());
			TextRenderer.addString(String.format("|V|: %.2f", velocity.length()), new Vector2f(-1, .8f), .05f, new Color4f(0, 0, 0, 1), 0);
			TextRenderer.addString(String.format("V: (%.2f, %.2f, %.2f)", velocity.x, velocity.y, velocity.z), new Vector2f(-1, .75f), .05f, new Color4f(0, 0, 0, 1), 0);
		}
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
