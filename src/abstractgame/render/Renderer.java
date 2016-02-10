
package abstractgame.render;

import static org.lwjgl.opengl.GL11.GL_NO_ERROR;
import static org.lwjgl.opengl.GL11.GL_VERSION;
import static org.lwjgl.opengl.GL11.glClearColor;
import static org.lwjgl.opengl.GL11.glGetError;
import static org.lwjgl.opengl.GL11.glGetString;
import static org.lwjgl.util.glu.GLU.gluErrorString;

import java.io.IOException;
import java.nio.IntBuffer;
import java.nio.file.Paths;

import org.lwjgl.BufferUtils;
import org.lwjgl.LWJGLException;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;
import org.lwjgl.opengl.PixelFormat;

import abstractgame.Game;
import abstractgame.io.FileIO;
import abstractgame.io.user.Console;
import abstractgame.io.user.KeyBinds;
import abstractgame.io.user.KeyIO;
import abstractgame.util.ApplicationException;

public class Renderer {
	public static final boolean CHECK_GL = true;
	public static final int ID_CASHE_AMOUNT = 32;
	public static final String SHADER_PATH = "res/shaders/";
	public static final String SHADER_EXT = ".glsl";
	public static boolean vSync = false;

	private static IntBuffer buffers = BufferUtils.createIntBuffer(ID_CASHE_AMOUNT);
	private static IntBuffer vertArrays = BufferUtils.createIntBuffer(ID_CASHE_AMOUNT);
	private static IntBuffer textures = BufferUtils.createIntBuffer(ID_CASHE_AMOUNT);
	
	public static void initializeRenderer() {
		buffers.flip();
		vertArrays.flip();
		textures.flip();
		TextRenderer.initialize();
		Camera.createProjectionMatrix();
		
		GL11.glClearColor(1, 1, 1, 1f);

		//enable alpha blending
		GL11.glEnable(GL11.GL_BLEND);
		
		GL11.glEnable(GL11.GL_POLYGON_SMOOTH);
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		
		KeyBinds.add(Renderer::toggleVsync, Keyboard.KEY_V, KeyIO.KEY_PRESSED, "game.vsync");
	}

	public static void tick() {
		Display.update();
		GL11.glClear(GL11.GL_DEPTH_BUFFER_BIT | GL11.GL_COLOR_BUFFER_BIT);
		if(Display.wasResized())
			onResize();
		
		GL11.glEnable(GL11.GL_DEPTH_TEST);
		
		GL11.glDisable(GL11.GL_DEPTH_TEST);
		TextRenderer.render();
	}
	
	static void onResize() {
		GL11.glViewport(0, 0, Display.getWidth(), Display.getHeight());
		TextRenderer.updateCorrectionFactor();
		Camera.onResize();
	}
	
	public static void toggleVsync() {
		vSync = !vSync;
		Display.setVSyncEnabled(vSync);
	}
	
	public static int createShader(String name, int type) {
		int id = GL20.glCreateShader(type);

		try {
			GL20.glShaderSource(id, FileIO.readTextFileAsString(Paths.get(SHADER_PATH + name + SHADER_EXT), false));
		} catch(IOException e) {
			throw new ApplicationException("Error reading shader", e, "IO");
		}
		
		GL20.glCompileShader(id);

		if(CHECK_GL && GL11.GL_FALSE == GL20.glGetShaderi(id, GL20.GL_COMPILE_STATUS)) {
			int length = GL20.glGetShaderi(id, GL20.GL_INFO_LOG_LENGTH);
			throw new ApplicationException("Shader \"" + name + "\" did not compile: \n" + GL20.glGetShaderInfoLog(id, length), "RENDER");
		}

		return id;
	}

	public static int createProgram(int... shaders) {
		int id = GL20.glCreateProgram();
		for(int shader : shaders)
			GL20.glAttachShader(id, shader);

		GL20.glLinkProgram(id);
		
		if(CHECK_GL && GL11.GL_FALSE == GL20.glGetProgrami(id, GL20.GL_LINK_STATUS)) {
			int length = GL20.glGetProgrami(id, GL20.GL_INFO_LOG_LENGTH);
			throw new ApplicationException(GL20.glGetProgramInfoLog(id, length), "RENDER");
		}

		return id;
	}

	public static int getTextureID() {
		if(!textures.hasRemaining()) {
			textures.clear();
			GL11.glGenTextures(textures);
		}

		return textures.get();
	}

	public static void createDisplay() {
		try {
			if(Game.GLOBAL_CONFIG.getProperty("display.fullscreen", false)) {
				Display.setFullscreen(true);
			} else {
				Display.setFullscreen(false);
				Display.setResizable(true);
				Display.setDisplayMode(new DisplayMode(Game.GLOBAL_CONFIG.getProperty("display.size.width", 800), Game.GLOBAL_CONFIG.getProperty("display.size.height", 600)));
			}
			
			Display.setVSyncEnabled(true);
			Display.create(new PixelFormat(8, 8, 0, 4));
			Mouse.setGrabbed(KeyIO.holdMouse);

			glClearColor(0, 0, 0, 0);

			Console.inform("OpenGL Version: " + glGetString(GL_VERSION), "OPENGL");

		} catch (LWJGLException e) {
			throw new ApplicationException("A LWJGL error occured when creating the context.", e, "LWJGL");
		}
	}

	public static void checkGL(String message) {
		if(!CHECK_GL)
			return;

		int errorCheckValue = glGetError();
		if (errorCheckValue != GL_NO_ERROR)
			throw new ApplicationException(message + gluErrorString(errorCheckValue), "OPENGL");
	}

	public static void checkGL() {
		checkGL("GL Error: ");
	}

	public static int getBufferID() {
		if(!buffers.hasRemaining()) {
			buffers.clear();
			GL15.glGenBuffers(buffers);
		}

		return buffers.get();
	}

	public static int getVertexArrayID() {
		if(!vertArrays.hasRemaining()) {
			vertArrays.clear();
			GL30.glGenVertexArrays(vertArrays);
		}

		return vertArrays.get();
	}
}
