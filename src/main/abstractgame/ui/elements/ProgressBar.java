package abstractgame.ui.elements;

import java.nio.FloatBuffer;
import java.util.function.Supplier;

import javax.vecmath.Color4f;
import javax.vecmath.Vector2f;

import abstractgame.Client;
import abstractgame.render.UIRenderer;
import abstractgame.time.Clock;
import abstractgame.util.FloatSupplier;

public class ProgressBar extends UIElement {
	static final float BAR_WIDTH = 0.5f;
	
	public final Color4f colour;
	
	Line line;
	Quad quad;
	
	float xStart;
	float length;
	FloatSupplier value;
	int ID;
	
	/** if initialValue is negative the bar will show an indeterminate level. The bar will cycle every -value seconds. */
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
		float suppliedValue = value.supply();
		
		if(suppliedValue < 0) {
			double sec = Client.GAME_CLOCK.getTime() / 1000.0f;
			float cycle = (float) (sec / -suppliedValue - Math.floor(sec / -suppliedValue));
			quad.to.x = xStart + length * Math.min(cycle * (1 + BAR_WIDTH), 1);
			quad.from.x = xStart + length * Math.max((cycle * (1 + BAR_WIDTH)) - BAR_WIDTH, 0);
		} else {
			quad.to.x = xStart + length * suppliedValue;
		}
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
