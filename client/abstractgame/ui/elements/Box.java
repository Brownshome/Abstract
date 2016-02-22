package abstractgame.ui.elements;

import java.nio.FloatBuffer;

import javax.vecmath.Color4f;
import javax.vecmath.Vector2f;

public class Box extends UIElement {
	Line top;
	Line left;
	Line right;
	Line bottom;
	
	public Box(Vector2f from, Vector2f to, float layer, Color4f colour, int ID) {
		top = new Line(new Vector2f(from.x, to.y), new Vector2f(to.x, to.y), layer, colour, ID);
		left = new Line(new Vector2f(from.x, from.y), new Vector2f(from.x, to.y), layer, colour, ID);
		right = new Line(new Vector2f(to.x, from.y), new Vector2f(to.x, to.y), layer, colour, ID);
		bottom = new Line(new Vector2f(from.x, from.y), new Vector2f(to.x, from.y), layer, colour, ID);
	}
	
	public void setID(int id) {
		top.setID(id);
		left.setID(id);
		right.setID(id);
		bottom.setID(id);
	}
	
	public int getLinesLength() {
		return top.getLinesLength() * 4;
	}
	
	public void fillLines(FloatBuffer buffer) {
		top.fillLines(buffer);
		left.fillLines(buffer);
		right.fillLines(buffer);
		bottom.fillLines(buffer);
	}
}
