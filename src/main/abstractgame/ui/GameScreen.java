package abstractgame.ui;

import java.util.function.IntSupplier;

import javax.vecmath.Color3f;
import javax.vecmath.Color4f;
import javax.vecmath.Vector2f;
import javax.vecmath.Vector3f;

import org.lwjgl.input.Keyboard;

import abstractgame.Client;
import abstractgame.io.user.KeyBinds;
import abstractgame.io.user.PerfIO;
import abstractgame.net.ServerProxy;
import abstractgame.render.Camera;
import abstractgame.render.FreeCamera;
import abstractgame.render.GLHandler;
import abstractgame.render.TextRenderer;
import abstractgame.render.UIRenderer;
import abstractgame.util.FloatSupplier;
import abstractgame.world.World;

public class GameScreen extends Screen {
	private static enum State {
		WAIT_FOR_SERVER,
		LOAD_WORLD_DATA,
		RECIEVE_STATE_UPDATE,
		RUN
	}
	
	public static GameScreen INSTANCE = new GameScreen();
	
	static State state = State.WAIT_FOR_SERVER;
	static World world;
	
	/** This gives the number of seconds until respawn, if it is null there
	 * is no respawn in progress */
	public static FloatSupplier respawnTimer;
	
	public static World getWorld() {
		return world;
	}
	
	@Override
	public void initialize() {
		PerfIO.holdMouse(true);
	}
	
	@Override
	public void tick() {
		super.tick();
		
		//wait for a response from the server
		//load the world data
		//recieve state update
		//tick world
		
		if(world != null)
			world.tick();
		
		if(respawnTimer != null) {
			float timeLeft = respawnTimer.supply();
			
			if(timeLeft < 0)
				timeLeft = 0;
				
			String text = String.format("Spawning in %3.1fs", timeLeft);
			
			float size = .2f * (1 - Math.min(timeLeft / 5, .8f));
			
			Color4f colour = new Color4f();
			colour.interpolate(UIRenderer.HIGHLIGHT_STRONG, UIRenderer.BASE, Math.min(timeLeft / 5, .8f));
			
			TextRenderer.addString(text, new Vector2f(-TextRenderer.getWidth(text) * size * .5f, -TextRenderer.getHeight(text) * size * .5f), size, colour, 0);
		}
	}

	public static void setWorld(World newWorld) {
		if(world != null)
			world.cleanUp();
		
		world = newWorld;
		FreeCamera camera = new FreeCamera(new Vector3f(0, 0, -5), new Vector3f(0, 1, 0), new Vector3f(0, 0, 1));
		
		KeyBinds.add(camera::up, Keyboard.KEY_SPACE, PerfIO.BUTTON_DOWN, "free camera.up");
		KeyBinds.add(camera::down, Keyboard.KEY_LSHIFT, PerfIO.BUTTON_DOWN, "free camera.down");
		KeyBinds.add(camera::forward, Keyboard.KEY_W, PerfIO.BUTTON_DOWN, "free camera.forward");
		KeyBinds.add(camera::backward, Keyboard.KEY_S, PerfIO.BUTTON_DOWN, "free camera.backward");
		KeyBinds.add(camera::left, Keyboard.KEY_A, PerfIO.BUTTON_DOWN, "free camera.left");
		KeyBinds.add(camera::right, Keyboard.KEY_D, PerfIO.BUTTON_DOWN, "free camera.right");
		KeyBinds.add(() -> camera.slow = true, Keyboard.KEY_LMENU, PerfIO.BUTTON_DOWN, "free camera.slow");
		KeyBinds.add(() -> camera.slow = false, Keyboard.KEY_LMENU, PerfIO.BUTTON_UP, "free camera.slow");
		KeyBinds.add(camera::stop, Keyboard.KEY_X, PerfIO.BUTTON_PRESSED, "free camera.stop");
		
		world.onTick(camera);
		Camera.setCameraHost(camera);
	}
}
