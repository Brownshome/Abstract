package abstractgame;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.SynchronousQueue;

import org.lwjgl.Sys;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.Display;

import abstractgame.io.FileIO;
import abstractgame.io.config.ConfigFile;
import abstractgame.io.model.PhysicsMeshLoader;
import abstractgame.io.user.Console;
import abstractgame.io.user.KeyBinds;
import abstractgame.io.user.PerfIO;
import abstractgame.mod.ModManager;
import abstractgame.net.Identity;
import abstractgame.net.PlayerDataHandler;
import abstractgame.net.Side;
import abstractgame.net.Sided;
import abstractgame.render.IconRenderer;
import abstractgame.render.ModelRenderer;
import abstractgame.render.PhysicsRenderer;
import abstractgame.render.GLHandler;
import abstractgame.render.Renderer;
import abstractgame.render.ServerPhysicsRenderer;
import abstractgame.render.TextRenderer;
import abstractgame.render.UIRenderer;
import abstractgame.time.Clock;
import abstractgame.ui.DebugScreen;
import abstractgame.ui.Screen;
import abstractgame.ui.TitleScreen;
import abstractgame.world.World;
import abstractgame.world.map.MapLogicProxy;
import abstractgame.world.map.MapObject;
import abstractgame.world.map.PlayerSpawn;
import abstractgame.world.map.StaticMapObjectClient;

/** This is the main class for the client */
@Sided(Side.CLIENT)
public class Client {
	/*  The final game will most probably have an IO thread, a phyiscs thread and a render thread
	 *  atm the physics thread and render thread are the same
	 */
	
	static {
		THREAD = Thread.currentThread();
		GLOBAL_CONFIG = ConfigFile.getFile("globalConfig");
		setupErrorHandlingAndLogging();
		Common.setupSecurity();
	}
	
	public static boolean close = false;
	public static ConfigFile GLOBAL_CONFIG;
	public static final Clock GAME_CLOCK = new Clock();
	public static final String NAME = "ABSTRACT";
	public static Thread THREAD;
	public static List<Runnable> runnableList = Collections.synchronizedList(new ArrayList<>());
	
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
		synchronized(runnableList) {
			runnableList.forEach(Runnable::run);
			runnableList.clear();
		}
		
		Screen.tickScreen();
		GLHandler.tick();
		PerfIO.tick();
		GLHandler.checkGL();
		
		GAME_CLOCK.tick();
	}

	static void loadHooks() {
		IconRenderer.regesterIcon("settings");
		IconRenderer.regesterIcon("server");
		IconRenderer.regesterIcon("new");
		IconRenderer.regesterIcon("module/default");
		
		GLHandler.addRenderer(new TextRenderer());
		GLHandler.addRenderer(new ModelRenderer());
		GLHandler.addRenderer(new UIRenderer());
		GLHandler.addRenderer(new IconRenderer());
		GLHandler.addRenderer(PhysicsRenderer.INSTANCE);
		GLHandler.addRenderer(ServerPhysicsRenderer.INSTANCE);
		
		Common.loadHooks();
	}
	
	static void initialize() {
		GLHandler.createDisplay();
		loadHooks();
		GLHandler.initializeRenderer();
		
		KeyBinds.add(Client::close, Keyboard.KEY_ESCAPE, PerfIO.BUTTON_PRESSED, "game.exit");
		KeyBinds.add(() -> PerfIO.holdMouse(!PerfIO.holdMouse), Keyboard.KEY_F, PerfIO.BUTTON_PRESSED, "game.free mouse");
		KeyBinds.add(DebugScreen::toggle, Keyboard.KEY_F3, PerfIO.BUTTON_PRESSED, "debug.toggle debug display");
		
		Client.startClientNetThread();
		Screen.setScreen(TitleScreen.INSTANCE);
	}
	
	static void setupErrorHandlingAndLogging() {
		Thread.setDefaultUncaughtExceptionHandler(Console::error);
		Console.setLevel(GLOBAL_CONFIG.getProperty("logging.level", 0));
		Console.setFormat(GLOBAL_CONFIG.getProperty("logging.format", "HH:mm:ss"));
	}

	public static Identity getIdentity() {
		return PlayerDataHandler.getIdentity(11257);
	}

	public static BlockingQueue<Runnable> getInboundQueue() {
		return inboundPackets;
	}

	public static void addTask(Runnable r) {
		runnableList.add(r);
	}

	public static void startClientNetThread() {
		Thread ioThread = new Thread(() -> {
			while(true) {
				try {
					inboundPackets.take().run();
				} catch (InterruptedException e) {}
			}
		}, "CLIENT-NET-THREAD");
		ioThread.setDaemon(true);
		ioThread.start();
		
		Console.inform("Started client net thread.", "THREAD ENGINE");
	}
}
