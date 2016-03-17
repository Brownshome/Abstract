
package abstractgame.render;

import static org.lwjgl.opengl.GL11.GL_NO_ERROR;
import static org.lwjgl.opengl.GL11.GL_VERSION;
import static org.lwjgl.opengl.GL11.glClearColor;
import static org.lwjgl.opengl.GL11.glGetError;
import static org.lwjgl.opengl.GL11.glGetString;
import static org.lwjgl.util.glu.GLU.gluErrorString;

import java.io.IOException;
import java.nio.ByteBuffer;
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
import org.lwjgl.opengl.ARBTextureMultisample;
import org.lwjgl.opengl.ContextAttribs;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL14;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL21;
import org.lwjgl.opengl.GL30;
import org.lwjgl.opengl.GL32;
import org.lwjgl.opengl.GL41;
import org.lwjgl.opengl.GL42;
import org.lwjgl.opengl.GL43;
import org.lwjgl.opengl.GL45;
import org.lwjgl.opengl.PixelFormat;

import abstractgame.Client;
import abstractgame.io.FileIO;
import abstractgame.io.user.Console;
import abstractgame.io.user.KeyBinds;
import abstractgame.io.user.PerfIO;
import abstractgame.util.ApplicationException;

public class GLHandler {
	public static final boolean CHECK_GL = Client.GLOBAL_CONFIG.getProperty("opengl.debug", false);
	public static final String SHADER_PATH = "resources/shaders/";
	public static final String SHADER_EXT = ".glsl";
	public static boolean vSync = false;
	
	private static final List<Renderer> RENDERERS = new ArrayList<>();
	
	/** Don't edit this unless you know what you are doing */
	public static int MAIN_FRAMEBUFFER;
	public static int ID_DOWNSAMPLE_FRAMEBUFFER;
	public static int hoveredID = -1;
	
	public static float xCorrectionScalar;
	public static int alphaTestShader = -1;
	
	public static void updateCorrectionFactor() {
		xCorrectionScalar = (float) Display.getHeight() / Display.getWidth();
		RENDERERS.forEach(Renderer::onAspectChange);
	}
	public static void addRenderer(Renderer renderer) {
		RENDERERS.add(renderer);
	}
	
	public static void initializeRenderer() {
		RENDERERS.sort((a, b) -> Float.compare(a.getPass(), b.getPass()));
		
		RENDERERS.forEach(Renderer::initialize);
		
		Camera.createProjectionMatrix();
		
		GL11.glClearColor(1, 1, 1, 1f);

		//enable alpha blending
		GL11.glEnable(GL11.GL_BLEND);
		
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		
		createFramebuffer();
		
		KeyBinds.add(GLHandler::toggleVsync, Keyboard.KEY_V, PerfIO.BUTTON_PRESSED, "game.vsync");
		
		checkGL();
	}
	
	private static void createFramebuffer() {
		ID_DOWNSAMPLE_FRAMEBUFFER = GL30.glGenFramebuffers();
		GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, ID_DOWNSAMPLE_FRAMEBUFFER);
		
		int downsampler = GL30.glGenRenderbuffers();
		GL30.glBindRenderbuffer(GL30.GL_RENDERBUFFER, downsampler);
		GL30.glRenderbufferStorage(GL30.GL_RENDERBUFFER, GL30.GL_R8, 1, 1);
		GL30.glFramebufferRenderbuffer(GL30.GL_FRAMEBUFFER, GL30.GL_COLOR_ATTACHMENT0, GL30.GL_RENDERBUFFER, downsampler);
		
		int status;
		if(CHECK_GL && (status = GL30.glCheckFramebufferStatus(GL30.GL_FRAMEBUFFER)) != GL30.GL_FRAMEBUFFER_COMPLETE) {
			throw new ApplicationException("Framebuffer is not complete: " + status ,"RENDERER");
		}
		
		MAIN_FRAMEBUFFER = GL30.glGenFramebuffers();
		GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, MAIN_FRAMEBUFFER);
		
		int mainTexture = GL11.glGenTextures();
		GL11.glBindTexture(ARBTextureMultisample.GL_TEXTURE_2D_MULTISAMPLE, mainTexture);
		GL43.glTexStorage2DMultisample(ARBTextureMultisample.GL_TEXTURE_2D_MULTISAMPLE, 8, GL11.GL_RGBA8, Display.getWidth(), Display.getHeight(), true);
		GL30.glFramebufferTexture2D(GL30.GL_FRAMEBUFFER, GL30.GL_COLOR_ATTACHMENT0, ARBTextureMultisample.GL_TEXTURE_2D_MULTISAMPLE, mainTexture, 0);
		checkGL();
		
		int IDRenderbuffer = GL30.glGenRenderbuffers();
		GL30.glBindRenderbuffer(GL30.GL_RENDERBUFFER, IDRenderbuffer);
		checkGL();
		GL30.glRenderbufferStorageMultisample(GL30.GL_RENDERBUFFER, 8, GL30.GL_R8 /* WHY THE HELL DOES GL_R8UI NOT WORK */, Display.getWidth(), Display.getHeight());
		checkGL();
		GL30.glFramebufferRenderbuffer(GL30.GL_FRAMEBUFFER, GL30.GL_COLOR_ATTACHMENT1, GL30.GL_RENDERBUFFER, IDRenderbuffer);
		checkGL();
		
		int depthBuffer = GL30.glGenRenderbuffers();
		GL30.glBindRenderbuffer(GL30.GL_RENDERBUFFER, depthBuffer);
		GL30.glRenderbufferStorageMultisample(GL30.GL_RENDERBUFFER, 8, GL14.GL_DEPTH_COMPONENT32, Display.getWidth(), Display.getHeight());
		GL30.glFramebufferRenderbuffer(GL30.GL_FRAMEBUFFER, GL30.GL_DEPTH_ATTACHMENT, GL30.GL_RENDERBUFFER, depthBuffer);
		checkGL();
		
		//set this up for ID reading
		GL20.glDrawBuffers((IntBuffer) BufferUtils.createIntBuffer(2).put(GL30.GL_COLOR_ATTACHMENT0).put(GL30.GL_COLOR_ATTACHMENT1).flip());
		GL11.glReadBuffer(GL30.GL_COLOR_ATTACHMENT0);
		
		GL20.glDrawBuffers((IntBuffer) BufferUtils.createIntBuffer(2).put(GL30.GL_COLOR_ATTACHMENT0).put(GL30.GL_COLOR_ATTACHMENT1).flip());
		if(CHECK_GL && (status = GL30.glCheckFramebufferStatus(GL30.GL_FRAMEBUFFER)) != GL30.GL_FRAMEBUFFER_COMPLETE) {
			throw new ApplicationException("Framebuffer is not complete: " + status ,"RENDERER");
		}
		
		checkGL();
	}
	
	public static void tick() {
		Display.update();
		GL11.glClear(GL11.GL_DEPTH_BUFFER_BIT | GL11.GL_COLOR_BUFFER_BIT);
		if(Display.wasResized())
			onResize();
		
		checkGL();
		for(Renderer r : RENDERERS) {
			r.render();
			checkGL("OpenGL error in " + r.getClass() + ": ");
		}
		
		//read -> MAIN
		
		//read the hovered over object from the framebuffer
		
		//TODO sort out multisampling of the ID buffer causing issues
		if(!PerfIO.holdMouse) {
			GL11.glReadBuffer(GL30.GL_COLOR_ATTACHMENT1);
			
			GL30.glBindFramebuffer(GL30.GL_DRAW_FRAMEBUFFER, ID_DOWNSAMPLE_FRAMEBUFFER);
			//MAIN -> DOWN
			GL30.glBlitFramebuffer(Mouse.getX(), Mouse.getY(), Mouse.getX() + 1, Mouse.getY() + 1, 0, 0, 1, 1, GL11.GL_COLOR_BUFFER_BIT, GL11.GL_NEAREST);
			
			GL11.glReadBuffer(GL30.GL_COLOR_ATTACHMENT0);
			
			GL30.glBindFramebuffer(GL30.GL_READ_FRAMEBUFFER, ID_DOWNSAMPLE_FRAMEBUFFER);
			
			ByteBuffer id = BufferUtils.createByteBuffer(1);
			GL11.glReadPixels(0, 0, 1, 1, GL11.GL_RED, GL11.GL_UNSIGNED_BYTE, id);
			hoveredID = id.get();
			
			//TextRenderer.addString(hoveredID + "", new Vector2f(-1, -1), .1f, UIRenderer.BASE_STRONG, 0);
			
			GL30.glBindFramebuffer(GL30.GL_READ_FRAMEBUFFER, MAIN_FRAMEBUFFER);
		}
		
		//post processing goes here, atm just a blit
		GL30.glBindFramebuffer(GL30.GL_DRAW_FRAMEBUFFER, 0);
		//MAIN -> DRAWBUFFER
		GL30.glBlitFramebuffer(0, 0, Display.getWidth(), Display.getHeight(), 0, 0, Display.getWidth(), Display.getHeight(), GL11.GL_COLOR_BUFFER_BIT, GL11.GL_NEAREST);
		GL30.glBindFramebuffer(GL30.GL_DRAW_FRAMEBUFFER, MAIN_FRAMEBUFFER);
	}
	
	static void onResize() {
		GL11.glViewport(0, 0, Display.getWidth(), Display.getHeight());
		GLHandler.updateCorrectionFactor();
		Camera.onResize();
	}
	
	public static void toggleVsync() {
		vSync = !vSync;
		Display.setVSyncEnabled(vSync);
	}
	
	public static int createShader(String name, int type) {
		int id = GL20.glCreateShader(type);

		try {
			String s = FileIO.readTextFileAsString(Paths.get(SHADER_PATH + name + SHADER_EXT), false);
			GL20.glShaderSource(id, s);
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

	public static void createDisplay() {
		try {
			if(Client.GLOBAL_CONFIG.getProperty("display.fullscreen", false)) {
				Display.setFullscreen(true);
			} else {
				Display.setFullscreen(false);
				Display.setResizable(true);
				Display.setDisplayMode(new DisplayMode(Client.GLOBAL_CONFIG.getProperty("display.size.width", 800), Client.GLOBAL_CONFIG.getProperty("display.size.height", 600)));
			}
			
			Display.setVSyncEnabled(true);
			Display.create(new PixelFormat().withDepthBits(24).withBitsPerPixel(24)/*, new ContextAttribs(4, 5, 0, ContextAttribs.CONTEXT_CORE_PROFILE_BIT_ARB)*/);
			//ARBDebugOutput.glDebugMessageCallbackARB(new ARBDebugOutputCallback());
			
			Mouse.setGrabbed(PerfIO.holdMouse);

			glClearColor(0, 0, 0, 0);

			Console.inform("OpenGL Version: " + glGetString(GL_VERSION), "OPENGL");

		} catch (LWJGLException e) {
			throw new ApplicationException("A LWJGL error occured when creating the context.", e, "LWJGL");
		}
		
		xCorrectionScalar = (float) Display.getHeight() / Display.getWidth();
		alphaTestShader = GLHandler.createShader("alphatest-fragment", GL20.GL_FRAGMENT_SHADER);
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

	public static int getNumberOfMipmaps(int res) {
		return 32 - Integer.numberOfLeadingZeros(res);
	}
	
	public static float encodeIDAsFloat(int ID) {
		if(ID == -1)
			return 1;
		
		return ID * 1f / 255;
	}
}
