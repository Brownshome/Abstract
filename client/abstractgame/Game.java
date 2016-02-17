package abstractgame;

import javax.vecmath.Vector3f;

import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.Display;

import abstractgame.io.FileIO;
import abstractgame.io.config.ConfigFile;
import abstractgame.io.user.Console;
import abstractgame.io.user.KeyBinds;
import abstractgame.io.user.KeyIO;
import abstractgame.render.Camera;
import abstractgame.render.FreeCamera;
import abstractgame.render.Renderer;
import abstractgame.time.Clock;
import abstractgame.ui.Screen;
import abstractgame.ui.TitleScreen;
import abstractgame.world.World;

public class Game {
	/*  The final game will most probably have an IO thread, a phyiscs thread and a render thread
	 *  atm the physics thread and render thread are the same
	 */
	
	public static boolean close = false;
	public static ConfigFile GLOBAL_CONFIG;
	public static final Clock GAME_CLOCK = new Clock();
	
	public static void main(String[] args) {
		try {
			initialize();
		
			while(!closeCheck()) {
				loop();
			}
		} catch(Throwable exception) {
			Console.error(exception);
		}
		
		cleanUp();
	}

	static void cleanUp() {
		FileIO.close(false);
		Display.destroy();
		Console.inform("Close complete.", "MAIN");
	}
	
	public static void close() {
		close = true;
	}
	
	/** Checks if a close is necessary, if it is performs necessary actions. When this method returns the game should be able
	 * to close gracefully. */
	static boolean closeCheck() {
		return close || Display.isCloseRequested();
	}

	static void loop() {
		Screen.tickScreen();
		Renderer.tick();
		KeyIO.tick();
		Renderer.checkGL();
		
		if(World.currentWorld != null)
			World.currentWorld.tick();
		
		GAME_CLOCK.tick();
	}

	static void initialize() {
		GLOBAL_CONFIG = ConfigFile.getFile("globalConfig");
		setupErrorHandlingAndLogging();
		Renderer.createDisplay();
		Renderer.initializeRenderer();
		KeyBinds.add(Game::close, Keyboard.KEY_ESCAPE, KeyIO.KEY_PRESSED, "game.exit");
		
		FreeCamera c = new FreeCamera(new Vector3f(0, 0, -5), new Vector3f(0, 1, 0), new Vector3f(0, 0, 1));
		
		World.currentWorld = new World();
		World.currentWorld.onTick(c);
		
		KeyBinds.add(c::up, Keyboard.KEY_SPACE, KeyIO.KEY_DOWN, "free camera.up");
		KeyBinds.add(c::down, Keyboard.KEY_LSHIFT, KeyIO.KEY_DOWN, "free camera.down");
		KeyBinds.add(c::forward, Keyboard.KEY_W, KeyIO.KEY_DOWN, "free camera.forward");
		KeyBinds.add(c::backward, Keyboard.KEY_S, KeyIO.KEY_DOWN, "free camera.backward");
		KeyBinds.add(c::left, Keyboard.KEY_A, KeyIO.KEY_DOWN, "free camera.left");
		KeyBinds.add(c::right, Keyboard.KEY_D, KeyIO.KEY_DOWN, "free camera.right");
		KeyBinds.add(() -> c.slow = true, Keyboard.KEY_LMENU, KeyIO.KEY_DOWN, "free camera.slow");
		KeyBinds.add(() -> c.slow = false, Keyboard.KEY_LMENU, KeyIO.KEY_UP, "free camera.slow");
		KeyBinds.add(c::stop, Keyboard.KEY_X, KeyIO.KEY_PRESSED, "free camera.stop");
		KeyBinds.add(() -> KeyIO.holdMouse(!KeyIO.holdMouse), Keyboard.KEY_F, KeyIO.KEY_PRESSED, "game.free mouse");

		Camera.setCameraHost(c);
		
		Screen.setScreen(TitleScreen.INSTANCE);
	}
	
	static void setupErrorHandlingAndLogging() {
		Thread.setDefaultUncaughtExceptionHandler(Console::error);
		Console.setLevel(GLOBAL_CONFIG.getProperty("logging.level", 0));
		Console.setFormat(GLOBAL_CONFIG.getProperty("logging.format", "HH:mm:ss"));
	}
}
