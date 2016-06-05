package abstractgame.ui;

import javax.vecmath.*;

import org.lwjgl.input.Keyboard;

import abstractgame.io.user.PerfIO;
import abstractgame.render.*;
import abstractgame.ui.elements.*;
import abstractgame.util.*;
import abstractgame.world.World;
import abstractgame.world.entity.Player;

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
	
	static ProgressBar heatBar = new ProgressBar(new Vector2f(-.6f, -.9f), new Vector2f(1.2f, .01f), UIRenderer.HIGHLIGHT_STRONG,
			(FloatSupplier) () -> GameScreen.getPlayerEntity() == null ? 0 : GameScreen.getPlayerEntity().getHeat()
	);
	
	static ModuleHUDDisplay modules;
	
	/** This gives the number of seconds until respawn, if it is null there
	 * is no respawn in progress */
	public static FloatSupplier respawnTimer;
	
	//only populated when the player first joins a server
	static Player player;
	
	public static World getWorld() {
		return world;
	}
	
	@Override
	public void initialize() {
		PerfIO.holdMouse(true);
		UIRenderer.addElement(heatBar);
	}
	
	@Override
	public void destroy() {
		UIRenderer.removeElement(heatBar);
		
		if(modules != null)
			UIRenderer.removeElement(modules);
	}
	
	@Override
	public void run() {
		super.run();
		
		//wait for a response from the server
		//load the world data
		//recieve state update
		//tick world
		
		if(world != null)
			world.run();
		
		if(respawnTimer != null) {
			final float max = .2f;
			final float min = .075f;
			
			float timeLeft = respawnTimer.supply();
				
			float lerp = Math.min(Math.max(0, timeLeft * .2f), 1);
			
			String text = timeLeft < 0 ? Language.get("spawn.spawning") : String.format(Language.get("language.spawning in"), timeLeft);
			
			float size = min * lerp + max * (1 - lerp);
			
			Color4f colour = new Color4f();
			colour.interpolate(UIRenderer.HIGHLIGHT_STRONG, UIRenderer.BASE, lerp);
			
			TextRenderer.addString(text, new Vector2f(-TextRenderer.getWidth(text) * size * .5f, -TextRenderer.getHeight(text) * size * .5f), size, colour, 0);
		}
	}

	public static void setPlayerEntity(Player player) {
		GameScreen.player = player;
		assert modules == null;
		
		modules = new ModuleHUDDisplay(.1f, player);
		UIRenderer.addElement(modules);
	}

	public static Player getPlayerEntity() {
		return player;
	}

	public static void setWorld(World newWorld) {
		if(world != null)
			world.cleanUp();
		
		world = newWorld;
		
		FreeCamera camera = new FreeCamera(new Vector3f(0, 0, -5), new Vector3f(0, 1, 0), new Vector3f(0, 0, 1));
		Camera.setCameraHost(camera);
	}
}
