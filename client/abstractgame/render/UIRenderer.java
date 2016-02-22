package abstractgame.render;

import java.nio.FloatBuffer;
import java.util.Collection;
import java.util.HashSet;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;

import abstractgame.ui.elements.UIElement;

/** The UI renderer renders buttons, taskbars and window frames */
public class UIRenderer extends Renderer {
	static Collection<UIElement> uiElements = new HashSet<>();
	
	public static final int FLOATS_PER_VERTEX = 8;
	
	static int flatProgram;
	static int flatVBO;
	static int flatVAO;
	
	public static void addElement(UIElement element) {
		uiElements.add(element);
	}
	
	public static void removeElement(UIElement element) {
		uiElements.remove(element);
	}
	
	@Override
	public void initialize() {
		int vertex = Renderer.createShader("ui-vertex", GL20.GL_VERTEX_SHADER);
		int fragment = Renderer.createShader("ui-fragment", GL20.GL_FRAGMENT_SHADER);
		flatProgram = Renderer.createProgram(vertex, fragment);
		
		flatVAO = GL30.glGenVertexArrays();
		flatVBO = GL30.glGenVertexArrays();
		
		GL30.glBindVertexArray(flatVAO);
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, flatVBO);
		
		GL20.glEnableVertexAttribArray(0); //position
		GL20.glEnableVertexAttribArray(1); //colour
		GL20.glEnableVertexAttribArray(2); //ID
		
		GL20.glVertexAttribPointer(0, 3, GL11.GL_FLOAT, false, 32, 0);
		GL20.glVertexAttribPointer(1, 4, GL11.GL_FLOAT, false, 32, 12);
		GL30.glVertexAttribIPointer(2, 1, GL11.GL_UNSIGNED_INT, 32, 28);
	}

	@Override
	public void render() {
		int linesLength = 0;
		int trianglesLength = 0;
		
		for(UIElement ui : uiElements) {
			linesLength += ui.getLinesLength();
			trianglesLength += ui.getTrianglesLength();
			ui.tick();
		}
		
		FloatBuffer buffer = BufferUtils.createFloatBuffer(linesLength + trianglesLength);
		
		for(UIElement ui : uiElements)
			ui.fillLines(buffer);
		
		for(UIElement ui : uiElements)
			ui.fillTriangles(buffer);
		
		linesLength /= FLOATS_PER_VERTEX;
		trianglesLength /= FLOATS_PER_VERTEX;
		
		buffer.flip();
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, flatVBO);
		GL15.glBufferData(GL15.GL_ARRAY_BUFFER, buffer, GL15.GL_STATIC_DRAW);
		
		GL20.glUseProgram(flatProgram);
		GL30.glBindVertexArray(flatVAO);
		
		//lines
		GL11.glDrawArrays(GL11.GL_LINES, 0, linesLength);
		//triangles
		GL11.glDrawArrays(GL11.GL_TRIANGLES, linesLength, trianglesLength);
	}

	@Override
	public float getPass() {
		return -1;
	}
}
