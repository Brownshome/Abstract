package abstractgame.ui.elements;

import java.nio.FloatBuffer;
import java.util.function.Supplier;

import javax.vecmath.Color4f;
import javax.vecmath.Vector2f;

import abstractgame.render.UIRenderer;
import abstractgame.util.FloatSupplier;

public class ProgressBar extends UIElement {
	public final Color4f colour;
	
	Line line;
	Quad quad;
	
	float xStart;
	float length;
	FloatSupplier value;
	int ID;
	
	/** if initialValue is -1 to -2 the bar will show an indeterminate level, the bar will -1 to -2 (TODO) */
	public ProgressBar(Vector2f position, Vector2f dim, Color4f colour, FloatSupplier value, int ID) {
		this.value = value;
		xStart = position.x;
		length = dim.x;
		this.ID = ID;
		
		this.colour = colour;
		quad = new Quad(new Vector2f(position.x, position.y - dim.y), new Vector2f(position.x, position.y + dim.y), 0.1f, colour, ID);
		line = new Line(position, new Vector2f(position.x + dim.x, position.y), .1f, colour, ID);
	}
	
	public ProgressBar(Vector2f position, Vector2f dim, FloatSupplier value) {
		this(position, dim, UIRenderer.BASE_STRONG, value, -1);
	}

	public ProgressBar(Vector2f position, Vector2f dim, Color4f colour, FloatSupplier value) {
		this(position, dim, colour, value, -1);
	}

	@Override
	public void setID(int id) {
		ID = id;
		quad.setID(id);
		line.setID(id);
	}
	
	@Override
	public void tick() {
		quad.to.x = xStart + length * value.supply();
	}
	
	@Override
	public int getLinesLength() {
		return line.getLinesLength();
	}
	
	@Override
	public void fillLines(FloatBuffer buffer) {
		line.fillLines(buffer);
	}
	
	@Override
	public int getTrianglesLength() {
		return quad.getTrianglesLength();
	}
	
	@Override
	public void fillTriangles(FloatBuffer buffer) {
		quad.fillTriangles(buffer);
	}
}
