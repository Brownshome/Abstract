package abstractgame.ui.elements;

import java.nio.FloatBuffer;

import javax.vecmath.Color4f;
import javax.vecmath.Vector2f;

import abstractgame.render.Renderer;
import abstractgame.render.UIRenderer;

public class Line extends UIElement {
	Vector2f from;
	Vector2f to;
	Color4f colour;
	int ID;
	float layer;
	
	public Line(Vector2f from, Vector2f to, float layer, Color4f colour, int ID) {
		this.from = from;
		this.to = to;
		this.layer = layer;
		this.colour = colour;
		this.ID = ID;
	}
	
	public Line(Vector2f from, Vector2f to, Color4f colour) {
		this(from, to, 0, colour, 0);
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
		buffer.put(Renderer.encodeIDAsFloat(ID));

		buffer.put(to.x);
		buffer.put(to.y);
		buffer.put(layer);
		buffer.put(colour.x);
		buffer.put(colour.y);
		buffer.put(colour.z);
		buffer.put(colour.w);
		buffer.put(Renderer.encodeIDAsFloat(ID));
	}

	@Override
	public void setID(int ID) {
		this.ID = ID;
	}
}