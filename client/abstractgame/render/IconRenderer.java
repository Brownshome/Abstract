package abstractgame.render;

import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
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

import abstractgame.Game;
import abstractgame.io.image.ImageIO;
import abstractgame.io.image.Texture;
import abstractgame.ui.elements.Icon;
import abstractgame.util.ApplicationException;
import de.matthiasmann.twl.utils.PNGDecoder.Format;

/** Renderers alpha tested images */
public class IconRenderer extends Renderer {
	static int VBO;
	static int VAO;
	static int TEXTURE;
	static int ICON_PROGRAM;
	
	static List<Icon> icons = new ArrayList<>();
	static int length = 0;
	
	public static void addIcon(Icon icon) {
		icons.add(icon);
		length += icon.getLength();
	}
	
	@Override
	public void initialize() {
		int iconRes = Game.GLOBAL_CONFIG.getProperty("icon.size", 64);
		List<String> icons = Game.GLOBAL_CONFIG.getProperty("icon.list", Arrays.asList("Settings"), List.class);
		
		Future<Texture>[] textureFutures = (Future<Texture>[]) new Future<?>[icons.size()];

		for(int i = 0; i < textureFutures.length; i++)
			textureFutures[i] = ImageIO.loadPNG(icons.get(i), Format.BGRA);

		VBO = Renderer.getBufferID();
		VAO = Renderer.getVertexArrayID();
		
		GL30.glBindVertexArray(VAO);
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, VBO);
		
		GL20.glEnableVertexAttribArray(0); //position
		GL20.glEnableVertexAttribArray(1); //colour
		GL20.glEnableVertexAttribArray(2); //texture coord (3D)
		
		GL20.glVertexAttribPointer(0, 2, GL11.GL_FLOAT, false, 36, 0);
		GL20.glVertexAttribPointer(1, 4, GL11.GL_FLOAT, false, 36, 8);
		GL20.glVertexAttribPointer(2, 3, GL11.GL_FLOAT, false, 36, 24);
		
		int vertexShader = Renderer.createShader("icon-vertex", GL20.GL_VERTEX_SHADER);
		ICON_PROGRAM = Renderer.createProgram(vertexShader, Renderer.alphaTestShader);
		
		TEXTURE = Renderer.getTextureID();
		
		GL11.glBindTexture(GL30.GL_TEXTURE_2D_ARRAY, TEXTURE);
		GL42.glTexStorage3D(GL30.GL_TEXTURE_2D_ARRAY, Renderer.getNumberOfMipmaps(iconRes), GL30.GL_R8, iconRes, iconRes, icons.size());
		
		int layer = 0;
		for(Future<Texture> t : textureFutures) {
			Texture texture;
			try {
				texture = t.get();
			} catch (InterruptedException | ExecutionException e) {
				throw new ApplicationException(e, "IMAGEIO");
			}
			
			GL12.glTexSubImage3D(GL30.GL_TEXTURE_2D_ARRAY, 0, 0, 0, layer++, iconRes, iconRes, 1, GL12.GL_BGRA, GL11.GL_UNSIGNED_BYTE, texture.data);
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
}
