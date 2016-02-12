package abstractgame.render;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import javax.vecmath.Vector2f;
import javax.vecmath.Vector4f;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.EXTTextureFilterAnisotropic;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;
import org.lwjgl.opengl.GL32;

import abstractgame.Game;
import abstractgame.io.image.ImageIO;
import abstractgame.io.image.Texture;
import abstractgame.util.ApplicationException;
import de.matthiasmann.twl.utils.PNGDecoder.Format;

public class TextRenderer extends Renderer {
	static final int GRID_ROWS = 8;
	static final int GRID_COLUMNS = 16;
	static final String FONT_PATH = "path/";
	
	static final int BYTES_PER_LETTER = 1 + 1 + 8 + 16 + 4;
	static final int OFFSET = 32;
	static final float MIN_SIZE = 0.04f;

	static float correctionScalar;
	
	static int VAO;
	static int VBO;
	static int PROGRAM;
	static int TEXTURE;

	static ByteBuffer mapping;
	
	static ArrayList<ByteBuffer> buffer = new ArrayList<>();
	static int length;
	
	public static void updateCorrectionFactor() {
		correctionScalar = (float) Display.getHeight() / Display.getWidth();
		GL20.glUseProgram(PROGRAM);
		GL20.glUniform1f(2, correctionScalar);
	}
	
	@Override
	public void initialize() {
		List<String> textureNames = Game.GLOBAL_CONFIG.getProperty("font.list", Arrays.asList("Courier-New"), List.class);
		int textureSize = Game.GLOBAL_CONFIG.getProperty("font.size", 512);
		//int mipmaps = 32 - Integer.numberOfLeadingZeros(textureSize);
		
		Future<Texture>[] textureFutures = (Future<Texture>[]) new Future<?>[textureNames.size()];
		
		for(int i = 0; i < textureFutures.length; i++)
			textureFutures[i] = ImageIO.loadPNG(textureNames.get(i), Format.BGRA);
		
		GL11.glEnable(GL11.GL_BLEND);
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

		//create the buffers
		VAO = Renderer.getVertexArrayID();
		GL30.glBindVertexArray(VAO);

		VBO = Renderer.getBufferID();
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, VBO);

		GL20.glEnableVertexAttribArray(0); //image number
		GL20.glEnableVertexAttribArray(1); //character
		GL20.glEnableVertexAttribArray(2); //position
		GL20.glEnableVertexAttribArray(3); //colour
		GL20.glEnableVertexAttribArray(4); //size
		
		final byte b = 1 + 1 + 4 * 7;
		GL30.glVertexAttribIPointer(0, 1, GL11.GL_UNSIGNED_BYTE, b, 0);
		GL30.glVertexAttribIPointer(1, 1, GL11.GL_UNSIGNED_BYTE, b, 1);
		GL20.glVertexAttribPointer(2, 2, GL11.GL_FLOAT, false, b, 2);
		GL20.glVertexAttribPointer(3, 4, GL11.GL_FLOAT, false, b, 10);
		GL20.glVertexAttribPointer(4, 1, GL11.GL_FLOAT, false, b, 26);
		
		//create the program
		int vertex = Renderer.createShader("text-vertex", GL20.GL_VERTEX_SHADER);
		int fragment = Renderer.createShader("text-fragment", GL20.GL_FRAGMENT_SHADER);
		int geometry = Renderer.createShader("text-geometry", GL32.GL_GEOMETRY_SHADER);
		
		PROGRAM = Renderer.createProgram(vertex, geometry, fragment);

		//load the texture file
		TEXTURE = Renderer.getTextureID();
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, TEXTURE);
		
		if(textureFutures.length == 0)
			throw new ApplicationException("No fonts were specified", "RENDERER");
		
		Texture texture;
		try {
			texture = ImageIO.loadPNG("Courier-New", Format.BGRA).get();
		} catch (InterruptedException | ExecutionException e) {
			throw new ApplicationException(e, "IMAGEIO");
		}
		
		GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RED, textureSize, textureSize, 0, GL12.GL_BGRA, GL11.GL_UNSIGNED_BYTE, texture.data);
		GL30.glGenerateMipmap(GL11.GL_TEXTURE_2D);
		
		float aniso = GL11.glGetFloat(EXTTextureFilterAnisotropic.GL_MAX_TEXTURE_MAX_ANISOTROPY_EXT);
		GL11.glTexParameterf(GL11.GL_TEXTURE_2D, EXTTextureFilterAnisotropic.GL_TEXTURE_MAX_ANISOTROPY_EXT, aniso);
		
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL12.GL_CLAMP_TO_EDGE);
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL12.GL_CLAMP_TO_EDGE);

		correctionScalar = (float) Display.getHeight() / Display.getWidth();
		
		GL20.glUseProgram(PROGRAM);
		GL20.glUniform1i(0, GRID_ROWS);
		GL20.glUniform1i(1, GRID_COLUMNS);
		GL20.glUniform1f(2, correctionScalar);
		
		Renderer.checkGL();
	}
	
	public static void addString(String text, Vector2f position, float size, Vector4f colour, int font) {
		addText(encode(text, position, size, colour, font));
	}
	
	public static ByteBuffer encode(String text, Vector2f position, float size, Vector4f colour, int font) {
		ByteBuffer data = ByteBuffer.wrap(new byte[text.length() * BYTES_PER_LETTER]).order(ByteOrder.nativeOrder());

		float x = (float) position.x;
		float y = (float) position.y;
		float start = x;

		for(int i = 0; i < text.length(); i++) {
			char c = text.charAt(i);
			
			switch(c) {
				case '\n':
					x = start;
					y -= size;
					break;
				case '\t':
					x += size * 2f;
					break;
				default:
					data.put((byte) font);
					data.put((byte) (c - OFFSET));
					data.putFloat(x);
					data.putFloat(y);
					data.putFloat(colour.x);
					data.putFloat(colour.y);
					data.putFloat(colour.z);
					data.putFloat(colour.w);
					data.putFloat(size);
				case ' ':
					x += size * 0.75f * correctionScalar;
			}
		}
		
		data.flip();
		return data;
	}

	public static void addText(ByteBuffer data) {
		buffer.add(data);
		length += data.remaining();
	}

	@Override
	public void render() {
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, VBO);
		
		if(buffer.size() == 0)
			return;

		ByteBuffer b = BufferUtils.createByteBuffer(length);
		
		for(ByteBuffer data : buffer)
			b.put(data);
		
		b.flip();
		
		GL15.glBufferData(GL15.GL_ARRAY_BUFFER, b, GL15.GL_STATIC_DRAW);

		GL20.glUseProgram(PROGRAM);
		GL30.glBindVertexArray(VAO);
		
		GL13.glActiveTexture(GL13.GL_TEXTURE0);
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, TEXTURE);
		
		GL11.glDrawArrays(GL11.GL_POINTS, 0, length / BYTES_PER_LETTER);

		length = 0;
		buffer.clear();
	}

	@Override
	public float getPass() {
		return 1;
	}
}
