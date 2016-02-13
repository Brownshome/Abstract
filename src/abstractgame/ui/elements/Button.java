package abstractgame.ui.elements;

import java.nio.FloatBuffer;

import javax.vecmath.Color4f;
import javax.vecmath.Vector2f;

import abstractgame.render.Renderer;
import abstractgame.render.TextRenderer;

public class Button extends UIElement {
	public enum Emphasis {
		STRONG,
		WEAK,
		NORMAL
	}
	
	final static float BORDER = 0.1f;
	
	Vector2f from;
	Vector2f to;
	String text;
	float size;
	Emphasis emphasis;
	
	Box box;
	Quad shadowQuad;
	Quad filledQuad;
	
	public Button(Vector2f from, Vector2f to, String text, float size, Emphasis emphasis) {
		this.from  = from;
		this.to = to;
		this.text = text;
		this.emphasis = emphasis;
		this.size = size;
		box = new Box(from, to, 0f, new Color4f(.5f, .5f, .5f, 1));
		shadowQuad = new Quad(new Vector2f(from.x + 0.01f * Renderer.corr, from.y - 0.01f), new Vector2f(to.x + 0.01f * Renderer.corr, to.y - 0.01f), 0.3f, new Color4f(0f, 0f, 0f, 1));
		filledQuad = new Quad(from, to, 0.1f, new Color4f(1, 1, 1, 1));
	}
	
	public int getLinesLength() {  
		return box.getLinesLength();
	}
	
	public void fillLines(FloatBuffer buffer) {
		box.fillLines(buffer);
	}
	
	public int getTrianglesLength() {
		return filledQuad.getTrianglesLength() * 2;
	}
	
	public void fillTriangles(FloatBuffer buffer) {
		shadowQuad.fillTriangles(buffer);
		filledQuad.fillTriangles(buffer);
	}
	
	public void tick() {
		float textWidth = TextRenderer.getWidth(text);
		float textHeight = TextRenderer.getHeight(text);
		
		Vector2f dim = new Vector2f();
		dim.sub(to, from);
		
		float maxSizeX = dim.x / textWidth * (1 - BORDER);
		float maxSizeY = dim.y / textHeight * (1 - BORDER);
		
		float finalSize = Math.min(Math.min(maxSizeX, maxSizeY), size);
		
		Vector2f position = dim;
		position.add(from, to);
		position.x -= textWidth * finalSize;
		position.y -= textHeight * finalSize * 1.15f;
		position.scale(0.5f);
		
		TextRenderer.addString(text, position, finalSize, getColour(), 0);
	}

	private Color4f getColour() {
		return new Color4f(0, 0, 0, 1);
	}
}
