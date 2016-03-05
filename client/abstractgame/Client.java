package abstractgame;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.SynchronousQueue;

import javax.vecmath.Vector3f;

import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.Display;

import abstractgame.io.FileIO;
import abstractgame.io.config.ConfigFile;
import abstractgame.io.user.Console;
import abstractgame.io.user.KeyBinds;
import abstractgame.io.user.PerfIO;
import abstractgame.net.Identity;
import abstractgame.net.ServerProxy;
import abstractgame.net.Side;
import abstractgame.net.Sided;
import abstractgame.render.Camera;
import abstractgame.render.FreeCamera;
import abstractgame.render.Renderer;
import abstractgame.time.Clock;
import abstractgame.ui.DebugScreen;
import abstractgame.ui.Screen;
import abstractgame.ui.TitleScreen;
import abstractgame.world.World;

/** This is the main class for the client */
@Sided(Side.CLIENT)
public class Client {
	/*  The final game will most probably have an IO thread, a phyiscs thread and a render thread
	 *  atm the physics thread and render thread are the same
	 */
	
	public static boolean close = false;
	public static ConfigFile GLOBAL_CONFIG;
	public static final Clock GAME_CLOCK = new Clock();
	public static final String NAME = "ABSTRACT";
	
	public static BlockingQueue<Runnable> inboundPackets = new SynchronousQueue<>();
	
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
		PerfIO.tick();
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
		KeyBinds.add(Client::close, Keyboard.KEY_ESCAPE, PerfIO.BUTTON_PRESSED, "game.exit");
		
		FreeCamera c = new FreeCamera(new Vector3f(0, 0, -5), new Vector3f(0, 1, 0), new Vector3f(0, 0, 1));
		
		World.currentWorld = new World();
		World.currentWorld.onTick(c);
		
		KeyBinds.add(c::up, Keyboard.KEY_SPACE, PerfIO.BUTTON_DOWN, "free camera.up");
		KeyBinds.add(c::down, Keyboard.KEY_LSHIFT, PerfIO.BUTTON_DOWN, "free camera.down");
		KeyBinds.add(c::forward, Keyboard.KEY_W, PerfIO.BUTTON_DOWN, "free camera.forward");
		KeyBinds.add(c::backward, Keyboard.KEY_S, PerfIO.BUTTON_DOWN, "free camera.backward");
		KeyBinds.add(c::left, Keyboard.KEY_A, PerfIO.BUTTON_DOWN, "free camera.left");
		KeyBinds.add(c::right, Keyboard.KEY_D, PerfIO.BUTTON_DOWN, "free camera.right");
		KeyBinds.add(() -> c.slow = true, Keyboard.KEY_LMENU, PerfIO.BUTTON_DOWN, "free camera.slow");
		KeyBinds.add(() -> c.slow = false, Keyboard.KEY_LMENU, PerfIO.BUTTON_UP, "free camera.slow");
		KeyBinds.add(c::stop, Keyboard.KEY_X, PerfIO.BUTTON_PRESSED, "free camera.stop");
		KeyBinds.add(() -> PerfIO.holdMouse(!PerfIO.holdMouse), Keyboard.KEY_F, PerfIO.BUTTON_PRESSED, "game.free mouse");
		KeyBinds.add(DebugScreen::toggle, Keyboard.KEY_F3, PerfIO.BUTTON_PRESSED, "debug.toggle debug display");
		
		Camera.setCameraHost(c);
		
		ServerProxy.startClientNetThread();
		Screen.setScreen(TitleScreen.INSTANCE);
	}
	
	static void setupErrorHandlingAndLogging() {
		Thread.setDefaultUncaughtExceptionHandler(Console::error);
		Console.setLevel(GLOBAL_CONFIG.getProperty("logging.level", 0));
		Console.setFormat(GLOBAL_CONFIG.getProperty("logging.format", "HH:mm:ss"));
	}

	static Identity ID = new Identity("James", 11257);
	public static Identity getIdentity() {
		return ID;
	}

	public static BlockingQueue<Runnable> getInboundQueue() {
		return inboundPackets;
	}
}
