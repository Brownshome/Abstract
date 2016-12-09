package abstractgame.render;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import javax.vecmath.Color4f;
import javax.vecmath.Vector2f;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;
import org.lwjgl.opengl.GL32;
import org.lwjgl.opengl.GL42;

import abstractgame.*;
import abstractgame.io.image.ImageIO;
import abstractgame.io.image.Texture;
import abstractgame.io.user.Console;
import abstractgame.util.ApplicationException;
import de.matthiasmann.twl.utils.PNGDecoder.Format;

public class TextRenderer implements Renderer {
	static final int GRID_ROWS = 8;
	static final int GRID_COLUMNS = 16;
	static final String FONT_PATH = "path/";

	public static final int BYTES_PER_LETTER = 1 + 1 + 8 + 16 + 4 + 4;
	
	static final float TEXT_WIDTH_MOD = 0.65f;
	static final float TAB_WIDTH = 2;
	
	static final int OFFSET = 32;
	static final float MIN_SIZE = 0.04f;

	static int VAO;
	static int VBO;
	static int PROGRAM;
	static int TEXTURE;

	static ByteBuffer mapping;

	static ArrayList<ByteBuffer> buffer = new ArrayList<>();
	static int length;

	@Override
	public void initialize() {
		List<String> textureNames = Common.GLOBAL_CONFIG.getProperty("font.list", Arrays.asList("Courier-New"), List.class);
		int textureSize = Common.GLOBAL_CONFIG.getProperty("font.size", 1024);

		Future<Texture>[] textureFutures = (Future<Texture>[]) new Future<?>[textureNames.size()];

		for(int i = 0; i < textureFutures.length; i++)
			textureFutures[i] = ImageIO.loadPNG(textureNames.get(i), Format.BGRA);

		GL11.glEnable(GL11.GL_BLEND);
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

		//create the buffers
		VAO = GL30.glGenVertexArrays();
		GL30.glBindVertexArray(VAO);

		VBO = GL15.glGenBuffers();
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, VBO);

		GL20.glEnableVertexAttribArray(0); //image number
		GL20.glEnableVertexAttribArray(1); //character
		GL20.glEnableVertexAttribArray(2); //position
		GL20.glEnableVertexAttribArray(3); //colour
		GL20.glEnableVertexAttribArray(4); //size
		GL20.glEnableVertexAttribArray(5);

		final byte b = 1 + 1 + 4 * 8;
		GL30.glVertexAttribIPointer(0, 1, GL11.GL_UNSIGNED_BYTE, b, 0);
		GL30.glVertexAttribIPointer(1, 1, GL11.GL_UNSIGNED_BYTE, b, 1);
		GL20.glVertexAttribPointer(2, 2, GL11.GL_FLOAT, false, b, 2);
		GL20.glVertexAttribPointer(3, 4, GL11.GL_FLOAT, false, b, 10);
		GL20.glVertexAttribPointer(4, 1, GL11.GL_FLOAT, false, b, 26);
		GL30.glVertexAttribIPointer(5, 1, GL11.GL_UNSIGNED_INT, b, 30);

		//create the program
		int vertex = GLHandler.createShader("text-vertex", GL20.GL_VERTEX_SHADER);
		int geometry = GLHandler.createShader("text-geometry", GL32.GL_GEOMETRY_SHADER);

		PROGRAM = GLHandler.createProgram(vertex, geometry, GLHandler.alphaTestShader);

		GLHandler.checkGL();
		
		//load the texture file
		TEXTURE = GL11.glGenTextures();
		GL11.glBindTexture(GL30.GL_TEXTURE_2D_ARRAY, TEXTURE);
		
		GL42.glTexStorage3D(GL30.GL_TEXTURE_2D_ARRAY, GLHandler.getNumberOfMipmaps(textureSize), GL30.GL_R8, textureSize, textureSize, textureNames.size());
		
		int layer = 0;
		for(String textureName : textureNames) {
			Texture texture;
			try {
				texture = ImageIO.loadPNG(textureName, Format.BGRA).get();
			} catch (InterruptedException | ExecutionException e) {
				throw new ApplicationException(e, "IMAGEIO");
			}

			if(texture.height != textureSize || texture.width != textureSize)
				throw new ApplicationException("Image file " + textureName + " is the wrong resolution.", "IMAGEIO");
				
			GL12.glTexSubImage3D(GL30.GL_TEXTURE_2D_ARRAY, 0, 0, 0, layer++, textureSize, textureSize, 1, GL12.GL_BGRA, GL11.GL_UNSIGNED_BYTE, texture.data);
		}

		GLHandler.checkGL();
		
		GL11.glTexParameteri(GL30.GL_TEXTURE_2D_ARRAY, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR_MIPMAP_LINEAR);
		GL11.glTexParameteri(GL30.GL_TEXTURE_2D_ARRAY, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
		GL11.glTexParameteri(GL30.GL_TEXTURE_2D_ARRAY, GL11.GL_TEXTURE_WRAP_S, GL12.GL_CLAMP_TO_EDGE);
		GL11.glTexParameteri(GL30.GL_TEXTURE_2D_ARRAY, GL11.GL_TEXTURE_WRAP_T, GL12.GL_CLAMP_TO_EDGE);
		GL30.glGenerateMipmap(GL30.GL_TEXTURE_2D_ARRAY);

		GL20.glUseProgram(PROGRAM);
		GL20.glUniform1i(0, GRID_ROWS);
		GL20.glUniform1i(1, GRID_COLUMNS);
		GL20.glUniform1f(2, GLHandler.xCorrectionScalar);

		GLHandler.checkGL();
	}
	
	@Override
	public void onAspectChange() {
		GL20.glUseProgram(TextRenderer.PROGRAM);
		GL20.glUniform1f(2, GLHandler.xCorrectionScalar);
	}
	
	public static void addString(String text, Vector2f position, float size, Color4f colour, int font, int ID) {
		addText(encode(text, position, size, colour, font, ID));
	}

	public static void addString(String text, Vector2f position, float size, Color4f colour, int font) {
		addString(text, position, size, colour, font, -1);
	}
	
	/** Encodes text data into a bytebuffer as follows:
	 * <ul>
	 * <li> font
	 * <li> character index
	 * <li> (xy)
	 * <li> (rgba)
	 * <li> size
	 * <li> ID
	 * </ul>
	 *  @param text The text to display
	 *  @param position The position of the text in opengl screenspace
	 *  @param size The size of the text in opengl screenspace
	 *  @param colour The colour of the text
	 *  @param font The identifier for the font used
	 *  @param ID An id used for click detection
	 *  
	 *  @return The buffer with the encoded data
	 **/
	public static ByteBuffer encode(String text, Vector2f position, float size, Color4f colour, int font, int ID) {
		ByteBuffer data = ByteBuffer.wrap(new byte[text.length() * BYTES_PER_LETTER]).order(ByteOrder.nativeOrder());

		float x = (float) position.x;
		float y = (float) position.y;
		float start = x;

		for(int i = 0; i < text.length(); i++) {
			char c = text.charAt(i);
			
			if(c > 128 + OFFSET) {
				Console.warn(c + " is not within the character range displayable.", "TEXT RENDERER");
				c = ' ';
			}
				
			switch(c) {
			case '\n':
				x = start;
				y -= size;
				break;
			case '\t':
				x += size * TAB_WIDTH * GLHandler.xCorrectionScalar;
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
				data.putFloat(GLHandler.encodeIDAsFloat(ID));
			case ' ':
				x += size * TEXT_WIDTH_MOD * GLHandler.xCorrectionScalar;
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
		GL30.glBindVertexArray(VAO);
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, VBO);
		GL11.glDisable(GL11.GL_DEPTH_TEST);
		
		if(buffer.size() == 0)
			return;

		ByteBuffer b = BufferUtils.createByteBuffer(length);

		for(ByteBuffer data : buffer)
			b.put(data);

		b.flip();

		GL15.glBufferData(GL15.GL_ARRAY_BUFFER, b, GL15.GL_STATIC_DRAW);

		GL20.glUseProgram(PROGRAM);

		//TODO don't call this every frame
		GL13.glActiveTexture(GL13.GL_TEXTURE0);
		GL11.glBindTexture(GL30.GL_TEXTURE_2D_ARRAY, TEXTURE);

		GL11.glDrawArrays(GL11.GL_POINTS, 0, length / BYTES_PER_LETTER);

		length = 0;
		buffer.clear();
	}

	@Override
	public float getPass() {
		return 1;
	}

	/** Does not support multi line strings
	 * 
	 *  @return The width of the chacter
	 *  
	 *  @param c The character to measure */
	public static float getWidth(char c) {
		return (c == '\t' ? TAB_WIDTH : TEXT_WIDTH_MOD) * GLHandler.xCorrectionScalar;
	}
	
	/** @return the width of the text if it was at 1 size, Does not support multi line strings 
	 * 
	 * @param text The string to measure */
	public static float getWidth(String text) {
		float acc = 0;
		for(int i = 0; i < text.length(); i++) {
			acc += getWidth(text.charAt(i)); //everything will explode with non single codepoint characters, TODO FIX
		}
		return acc;
	}

	/** @return the height of the text as if it was at 1 size
	 * @param text The text to measure */
	public static float getHeight(String text) {
		return text.length() - text.replace("\n", "").length() + 1;
	}
}
