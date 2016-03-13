package abstractgame.render;

import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;
import org.lwjgl.opengl.GL42;

import abstractgame.Client;
import abstractgame.io.image.ImageIO;
import abstractgame.io.image.Texture;
import abstractgame.ui.elements.Icon;
import abstractgame.util.ApplicationException;
import de.matthiasmann.twl.utils.PNGDecoder.Format;

/** Renderers alpha tested images */
public class IconRenderer implements Renderer {
	static final int ICON_RES = 128;
	
	static int VBO;
	static int VAO;
	static int TEXTURE;
	static int ICON_PROGRAM;
	
	static List<String> iconNames = new ArrayList<>();
	static Map<String, Integer> idLookup = new HashMap<>(); 
	
	static List<Icon> icons = new ArrayList<>();
	static int length = 0;

	static {
		regesterIcon("settings");
		regesterIcon("server");
		regesterIcon("new");
	}
	
	public static void addIcon(Icon icon) {
		icons.add(icon);
		length += icon.getLength();
	}
	
	/** This must be called before IconRenderer.initialize() */
	public static void regesterIcon(String name) {
		int i = iconNames.size();
		iconNames.add(name);
		idLookup.put(name, i);
	}
	
	@Override
	public void initialize() {
		Future<Texture>[] textureFutures = (Future<Texture>[]) new Future<?>[iconNames.size()];

		for(int i = 0; i < textureFutures.length; i++) {
			textureFutures[i] = ImageIO.loadPNG(iconNames.get(i), Format.BGRA);
		}
			
		VBO = GL15.glGenBuffers();
		VAO = GL30.glGenVertexArrays();
		
		GL30.glBindVertexArray(VAO);
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, VBO);
		
		GL20.glEnableVertexAttribArray(0); //position
		GL20.glEnableVertexAttribArray(1); //colour
		GL20.glEnableVertexAttribArray(2); //texture coord (3D)
		GL20.glEnableVertexAttribArray(3); //ID
		
		GL20.glVertexAttribPointer(0, 2, GL11.GL_FLOAT, false, 40, 0);
		GL20.glVertexAttribPointer(1, 4, GL11.GL_FLOAT, false, 40, 8);
		GL20.glVertexAttribPointer(2, 3, GL11.GL_FLOAT, false, 40, 24);
		GL30.glVertexAttribIPointer(3, 1, GL11.GL_UNSIGNED_INT, 40, 36);
		
		int vertexShader = GLHandler.createShader("icon-vertex", GL20.GL_VERTEX_SHADER);
		ICON_PROGRAM = GLHandler.createProgram(vertexShader, GLHandler.alphaTestShader);
		
		TEXTURE = GL11.glGenTextures();
		
		GL11.glBindTexture(GL30.GL_TEXTURE_2D_ARRAY, TEXTURE);
		GL42.glTexStorage3D(GL30.GL_TEXTURE_2D_ARRAY, GLHandler.getNumberOfMipmaps(ICON_RES), GL30.GL_R8, ICON_RES, ICON_RES, iconNames.size());
		
		int layer = 0;
		for(Future<Texture> t : textureFutures) {
			Texture texture;
			try {
				texture = t.get();
			} catch (InterruptedException | ExecutionException e) {
				throw new ApplicationException(e, "IMAGEIO");
			}
			
			GL12.glTexSubImage3D(GL30.GL_TEXTURE_2D_ARRAY, 0, 0, 0, layer++, ICON_RES, ICON_RES, 1, GL12.GL_BGRA, GL11.GL_UNSIGNED_BYTE, texture.data);
		}
		
		GL11.glTexParameteri(GL30.GL_TEXTURE_2D_ARRAY, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR_MIPMAP_LINEAR);
		GL11.glTexParameteri(GL30.GL_TEXTURE_2D_ARRAY, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
		GL11.glTexParameteri(GL30.GL_TEXTURE_2D_ARRAY, GL11.GL_TEXTURE_WRAP_S, GL12.GL_CLAMP_TO_EDGE);
		GL11.glTexParameteri(GL30.GL_TEXTURE_2D_ARRAY, GL11.GL_TEXTURE_WRAP_T, GL12.GL_CLAMP_TO_EDGE);
		GL30.glGenerateMipmap(GL30.GL_TEXTURE_2D_ARRAY);
	}

	@Override
	public void render() {
		FloatBuffer buffer = BufferUtils.createFloatBuffer(length);
		
		for(Icon icon : icons)
			icon.fillBuffer(buffer);
		
		buffer.flip();
		GL30.glBindVertexArray(VAO);
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, VBO);
		GL15.glBufferData(GL15.GL_ARRAY_BUFFER, buffer, GL15.GL_STATIC_DRAW);
		GL20.glUseProgram(ICON_PROGRAM);
		
		GL13.glActiveTexture(GL13.GL_TEXTURE0);
		GL11.glBindTexture(GL30.GL_TEXTURE_2D_ARRAY, TEXTURE);
		
		GL11.glDrawArrays(GL11.GL_TRIANGLES, 0, length / Icon.FLOAT_PER_VERTEX);
	
		icons.clear();
		length = 0;
	}

	@Override
	public float getPass() {
		return 0;
	}

	public static boolean isIconLoaded(String icon) {
		return getIcon(icon) != null;
	}
	
	/** Will return null if the icon does not exist */
	public static Integer getIcon(String icon) {
		return idLookup.get(icon);
	}
	
	public static String getIcon(int icon) {
		return iconNames.get(icon);
	}
}
