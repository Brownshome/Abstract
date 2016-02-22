
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
import java.util.ArrayList;
import java.util.List;

import javax.vecmath.Color4f;
import javax.vecmath.Vector2f;

import org.lwjgl.BufferUtils;
import org.lwjgl.LWJGLException;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.ARBDebugOutput;
import org.lwjgl.opengl.ARBDebugOutputCallback;
import org.lwjgl.opengl.ContextAttribs;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;
import org.lwjgl.opengl.GL42;
import org.lwjgl.opengl.GL43;
import org.lwjgl.opengl.PixelFormat;

import abstractgame.Game;
import abstractgame.io.FileIO;
import abstractgame.io.user.Console;
import abstractgame.io.user.KeyBinds;
import abstractgame.io.user.KeyIO;
import abstractgame.util.ApplicationException;

public abstract class Renderer {
	public static final boolean CHECK_GL = true;
	public static final int ID_CASHE_AMOUNT = 32;
	public static final String SHADER_PATH = "res/shaders/";
	public static final String SHADER_EXT = ".glsl";
	public static boolean vSync = false;

	private static IntBuffer buffers = BufferUtils.createIntBuffer(ID_CASHE_AMOUNT);
	private static IntBuffer vertArrays = BufferUtils.createIntBuffer(ID_CASHE_AMOUNT);
	private static IntBuffer textures = BufferUtils.createIntBuffer(ID_CASHE_AMOUNT);
	private static IntBuffer framebuffers = BufferUtils.createIntBuffer(ID_CASHE_AMOUNT);
	
	private static final List<Renderer> RENDERERS = new ArrayList<>();
	
	/** Don't edit this unless you know what you are doing */
	public static int MAIN_FRAMEBUFFER;
	public static int hoveredID = -1;
	
	public static float corr;
	public static int alphaTestShader;
	
	public abstract void initialize();
	public abstract void render();
	public abstract float getPass();
	
	public static void addRenderer(Renderer renderer) {
		RENDERERS.add(renderer);
		RENDERERS.sort((a, b) -> Float.compare(a.getPass(), b.getPass()));
		renderer.initialize();
	}
	
	public static void initializeRenderer() {
		buffers.flip();
		vertArrays.flip();
		textures.flip();
		framebuffers.flip();
		
		alphaTestShader = Renderer.createShader("alphatest-fragment", GL20.GL_FRAGMENT_SHADER);
		
		addRenderer(new TextRenderer());
		addRenderer(new ModelRenderer());
		addRenderer(new UIRenderer());
		addRenderer(new IconRenderer());
		
		Camera.createProjectionMatrix();
		
		GL11.glClearColor(1, 1, 1, 1f);

		//enable alpha blending
		GL11.glEnable(GL11.GL_BLEND);
		
		GL11.glEnable(GL11.GL_POLYGON_SMOOTH);
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		
		createFramebuffer();
		
		KeyBinds.add(Renderer::toggleVsync, Keyboard.KEY_V, KeyIO.KEY_PRESSED, "game.vsync");
		checkGL();
	}
	
	private static void createFramebuffer() {
		MAIN_FRAMEBUFFER = getFramebufferID();
		GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, MAIN_FRAMEBUFFER);
		
		int mainTexture = Renderer.getTextureID();
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, mainTexture);
		GL42.glTexStorage2D(GL11.GL_TEXTURE_2D, 1, GL11.GL_RGBA16, Display.getWidth(), Display.getHeight());
		GL30.glFramebufferTexture2D(GL30.GL_FRAMEBUFFER, GL30.GL_COLOR_ATTACHMENT0, GL11.GL_TEXTURE_2D, mainTexture, 0);
		
		int IDTexture = Renderer.getTextureID();
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, IDTexture);
		GL42.glTexStorage2D(GL11.GL_TEXTURE_2D, 1, GL30.GL_R8UI, Display.getWidth(), Display.getHeight());
		GL30.glFramebufferTexture2D(GL30.GL_FRAMEBUFFER, GL30.GL_COLOR_ATTACHMENT1, GL11.GL_TEXTURE_2D, IDTexture, 0);
		
		//set this up for ID reading
		GL20.glDrawBuffers((IntBuffer) BufferUtils.createIntBuffer(2).put(GL30.GL_COLOR_ATTACHMENT0).put(GL30.GL_COLOR_ATTACHMENT1).flip());
		int status = GL30.glCheckFramebufferStatus(GL30.GL_FRAMEBUFFER);
		if(status != GL30.GL_FRAMEBUFFER_COMPLETE) {
			throw new ApplicationException("Framebuffer is not completely: " + status ,"RENDERER");
		}
		
		checkGL();
	}
	
	public static void tick() {
		Display.update();
		GL11.glClear(GL11.GL_DEPTH_BUFFER_BIT | GL11.GL_COLOR_BUFFER_BIT);
		if(Display.wasResized())
			onResize();
		
		checkGL();
		RENDERERS.forEach(r -> {
			r.render();
			checkGL("OpenGL error in " + r.getClass() + ": ");
		});
		Renderer.checkGL();
		
		//read the hovered over object from the framebuffer
		if(!KeyIO.holdMouse) {
			GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, MAIN_FRAMEBUFFER);
			GL11.glReadBuffer(GL30.GL_COLOR_ATTACHMENT1);
			checkGL();
			IntBuffer id = BufferUtils.createIntBuffer(1);
			GL11.glReadPixels(Mouse.getX(), Mouse.getY(), 1, 1, GL11.GL_RED, GL11.GL_UNSIGNED_INT, id);
			checkGL();
			hoveredID = id.get();
			
			TextRenderer.addString("" + hoveredID, new Vector2f(-0.7f, -0.7f), 0.1f, new Color4f(0, 0, 0, 1), 0);
			GL11.glReadBuffer(GL30.GL_COLOR_ATTACHMENT0);
		}
		
		Renderer.checkGL();
		
		//post processing goes here, atm just a blit
		GL30.glBindFramebuffer(GL30.GL_DRAW_FRAMEBUFFER, 0);
		GL30.glBlitFramebuffer(0, 0, Display.getWidth(), Display.getHeight(), 0, 0, Display.getWidth(), Display.getHeight(), GL11.GL_COLOR_BUFFER_BIT, GL11.GL_NEAREST);
		GL30.glBindFramebuffer(GL30.GL_DRAW_FRAMEBUFFER, MAIN_FRAMEBUFFER);
		Renderer.checkGL();
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
		
		for(int shader : shaders)
			GL20.glDetachShader(id, shader);
		
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

	public static int getFramebufferID() {
		if(!framebuffers.hasRemaining()) {
			framebuffers.clear();
			GL30.glGenFramebuffers(framebuffers);
		}

		return framebuffers.get();
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
			Display.create(new PixelFormat().withDepthBits(24).withBitsPerPixel(24), new ContextAttribs(4, 4, 0, ContextAttribs.CONTEXT_DEBUG_BIT_ARB));
			ARBDebugOutput.glDebugMessageCallbackARB(new ARBDebugOutputCallback());
			
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
	
	public static int getNumberOfMipmaps(int res) {
		return 32 - Integer.numberOfLeadingZeros(res);
	}
}
