package abstractgame.ui.elements;

import java.nio.FloatBuffer;
import java.util.function.Supplier;

import javax.vecmath.Color4f;
import javax.vecmath.Vector2f;

public class ProgressBar extends UIElement {
	Line line;
	Quad quad;
	float xStart;
	float length;
	Supplier<Float> value;
	int ID;
	
	/** if initialValue is -1 to -2 the bar will show an indeterminate level, the bar will -1 to -2 */
	public ProgressBar(Vector2f position, Vector2f dim, Supplier<Float> value, int ID) {
		this.value = value;
		xStart = position.x;
		length = dim.x;
		this.ID = ID;
		
		quad = new Quad(new Vector2f(position.x, position.y - dim.y), new Vector2f(position.x, position.y + dim.y), 0.1f, new Color4f(0, 0, 0, 1), ID);
		line = new Line(position, new Vector2f(position.x + dim.x, position.y), .1f, new Color4f(0, 0, 0, 1), ID);
	}
	
	public void setID(int id) {
		ID = id;
		quad.setID(id);
		line.setID(id);
	}
	
	public void tick() {
		quad.to.x = xStart + length * value.get();
	}
	
	public int getLinesLength() {
		return line.getLinesLength();
	}
	
	public void fillLines(FloatBuffer buffer) {
		line.fillLines(buffer);
	}
	
	public int getTrianglesLength() {
		return quad.getTrianglesLength();
	}
	
	public void fillTriangles(FloatBuffer buffer) {
		quad.fillTriangles(buffer);
	}
}
