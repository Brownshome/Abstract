package abstractgame.render;

import java.nio.FloatBuffer;
import java.util.Collection;
import java.util.HashSet;

import javax.vecmath.Color4f;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;

import abstractgame.ui.elements.UIElement;

/** The UI renderer renders buttons, taskbars and window frames */
public class UIRenderer implements Renderer {
	//modders can change any of these
	
	/** The base color for basic UI elements */
	public static final Color4f BASE_STRONG = new Color4f(0, 0, 0, 1);
	/** A more sudtle base color */
	public static final Color4f BASE = new Color4f(.6f, .6f, .6f, 1);
	/** An attention grabbing strong color */
	public static final Color4f HIGHLIGHT_STRONG = new Color4f(1, .2f, 0, 1);
	/** An attention grabbing weaker color */
	public static final Color4f HIGHLIGHT = new Color4f(1, .64f, .41f, 1);
	/** The background color */
	public static final Color4f BACKGROUND = new Color4f(1, 1, 1, 1);
	/** See-through */
	public static final Color4f TRANSPARENT = new Color4f(0, 0, 0, 0);
	
	static Collection<UIElement> uiElements = new HashSet<>();
	
	public static final int FLOATS_PER_VERTEX = 8;
	
	static int flatProgram;
	static int flatVBO;
	static int flatVAO;
	
	public static void addElement(UIElement element) {
		uiElements.add(element);
		element.onAdd();
	}
	
	public static void removeElement(UIElement element) {
		uiElements.remove(element);
		element.onRemove();
	}
	
	@Override
	public void initialize() {
		int vertex = GLHandler.createShader("ui-vertex", GL20.GL_VERTEX_SHADER);
		int fragment = GLHandler.createShader("ui-fragment", GL20.GL_FRAGMENT_SHADER);
		flatProgram = GLHandler.createProgram(vertex, fragment);
		
		flatVAO = GL30.glGenVertexArrays();
		flatVBO = GL30.glGenVertexArrays();
		
		GL30.glBindVertexArray(flatVAO);
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, flatVBO);
		GL11.glLineWidth(2);
		
		GL20.glEnableVertexAttribArray(0); //position
		GL20.glEnableVertexAttribArray(1); //colour
		GL20.glEnableVertexAttribArray(2); //ID
		
		GL20.glVertexAttribPointer(0, 3, GL11.GL_FLOAT, false, 32, 0);
		GL20.glVertexAttribPointer(1, 4, GL11.GL_FLOAT, false, 32, 12);
		GL30.glVertexAttribIPointer(2, 1, GL11.GL_UNSIGNED_INT, 32, 28);
		
		UIElement.populateValues();
	}

	@Override
	public void render() {
		int linesLength = 0;
		int trianglesLength = 0;
		
		for(UIElement ui : uiElements) {
			ui.tick();
			linesLength += ui.getLinesLength();
			trianglesLength += ui.getTrianglesLength();
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
		
		GL11.glEnable(GL11.GL_DEPTH_TEST);
		//lines
		GL11.glDrawArrays(GL11.GL_LINES, 0, linesLength);
		//triangles
		GL11.glDrawArrays(GL11.GL_TRIANGLES, linesLength, trianglesLength);
	}

	@Override
	public float getPass() {
		return .7f;
	}
}
