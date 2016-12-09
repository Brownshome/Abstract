package abstractgame;

import java.util.*;
import java.util.concurrent.*;

import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.Display;

import abstractgame.io.FileIO;
import abstractgame.io.config.ConfigFile;
import abstractgame.io.model.ModelLoader;
import abstractgame.io.model.ModelLoader.LoadType;
import abstractgame.io.user.*;
import abstractgame.io.user.keybinds.BindGroup;
import abstractgame.net.*;
import abstractgame.render.*;
import abstractgame.time.Clock;
import abstractgame.ui.*;
import abstractgame.util.Language;

/** This is the main class for the client */
@Sided(Side.CLIENT)
public class Client {
	/*  The final game will most probably have an IO thread, a phyiscs thread and a render thread
	 *  atm the physics thread and render thread are the same
	 */
	
	static {
		THREAD = Thread.currentThread();
		Common.setupErrorHandlingAndLogging();
		Common.setupSecurity();
	}
	
	public static boolean close = false;
	
	public static final Clock GAME_CLOCK = new Clock();
	
	public static final BindGroup DEBUG_BINDS = new BindGroup("debug");
	public static final BindGroup GLOBAL_BINDS = new BindGroup("global");
	
	public static final String NAME = Language.get("game.name");
	
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
		if(ServerProxy.getCurrentServerProxy() != null)
			ServerProxy.getCurrentServerProxy().tick();
		
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
	
	static void preLoadModels() {
		for(String name : Common.GLOBAL_CONFIG.getProperty("pre-load.gpu.gen patch", (List<String>) Collections.EMPTY_LIST))
			ModelLoader.preLoadModel(name, LoadType.LOAD_OBJ, LoadType.CREATE_GPU_MODEL, LoadType.GEN_PATCH_DATA);
		
		for(String name : Common.GLOBAL_CONFIG.getProperty("pre-load.gpu.load patch", (List<String>) Collections.EMPTY_LIST))
			ModelLoader.preLoadModel(name, LoadType.LOAD_OBJ, LoadType.CREATE_GPU_MODEL, LoadType.LOAD_PATCH_DATA);
		
		Common.preLoadPhysicsModels();
	}
	
	static void uploadModels() {
		for(String name : Common.GLOBAL_CONFIG.getProperty("pre-load.gpu.gen patch", (List<String>) Collections.EMPTY_LIST))
			ModelLoader.getModel(name).uploadToGPU();
		
		for(String name : Common.GLOBAL_CONFIG.getProperty("pre-load.gpu.load patch", (List<String>) Collections.EMPTY_LIST))
			ModelLoader.getModel(name).uploadToGPU();
	}
	
	static void initialize() {
		preLoadModels();
		
		GLHandler.createDisplay();
		loadHooks();
		GLHandler.initializeRenderer();
		
		uploadModels();
		
		GLOBAL_BINDS.add(FreeCamera::toggle, Keyboard.KEY_F2, PerfIO.BUTTON_PRESSED, "toggle freecam");
		GLOBAL_BINDS.add(Client::close, Keyboard.KEY_ESCAPE, PerfIO.BUTTON_PRESSED, "exit");
		GLOBAL_BINDS.add(() -> PerfIO.holdMouse(!PerfIO.holdMouse), Keyboard.KEY_F, PerfIO.BUTTON_PRESSED, "free mouse");
		DEBUG_BINDS.add(DebugScreen::toggle, Keyboard.KEY_F3, PerfIO.BUTTON_PRESSED, "toggle debug display");
		
		Client.startClientNetThread();
		Screen.setScreen(TitleScreen.INSTANCE);
	}

	static int random = (int) (Math.random() * 20000);
	public static Identity getIdentity() {
		return PlayerDataHandler.getIdentity(random);
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
