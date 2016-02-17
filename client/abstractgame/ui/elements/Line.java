package abstractgame.ui.elements;

import java.nio.FloatBuffer;

import javax.vecmath.Color4f;
import javax.vecmath.Vector2f;

import abstractgame.render.UIRenderer;

public class Line extends UIElement {
	Vector2f from;
	Vector2f to;
	Color4f colour;
	float layer;
	
	public Line(Vector2f from, Vector2f to, float layer, Color4f colour) {
		this.from = from;
		this.to = to;
		this.layer = layer;
		this.colour = colour;
	}
	
	public Line(Vector2f from, Vector2f to, Color4f colour) {
		this(from, to, 0, colour);
	}

	@Override
	public int getLinesLength() {
		return UIRenderer.FLOATS_PER_VERTEX * 2;
	}

	@Override
	public void fillLines(FloatBuffer buffer) {
		buffer.put(from.x);
		buffer.put(from.y);
		buffer.put(layer);
		buffer.put(colour.x);
		buffer.put(colour.y);
		buffer.put(colour.z);
		buffer.put(colour.w);

		buffer.put(to.x);
		buffer.put(to.y);
		buffer.put(layer);
		buffer.put(colour.x);
		buffer.put(colour.y);
		buffer.put(colour.z);
		buffer.put(colour.w);
	}
}