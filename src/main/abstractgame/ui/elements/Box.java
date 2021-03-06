package abstractgame.ui.elements;

import java.nio.FloatBuffer;

import javax.vecmath.Color4f;
import javax.vecmath.Vector2f;

public class Box extends UIElement {
	Line top;
	Line left;
	Line right;
	Line bottom;
	
	/** Any changes to this will be updated in the children */
	final Color4f colour;
	
	public Box(Vector2f from, Vector2f to, float layer, Color4f colour, int ID) {
		this.colour = colour;
		top = new Line(new Vector2f(from.x, to.y), new Vector2f(to.x, to.y), layer, colour, ID);
		left = new Line(new Vector2f(from.x, from.y), new Vector2f(from.x, to.y), layer, colour, ID);
		right = new Line(new Vector2f(to.x, from.y), new Vector2f(to.x, to.y), layer, colour, ID);
		bottom = new Line(new Vector2f(from.x, from.y), new Vector2f(to.x, from.y), layer, colour, ID);
	}
	
	@Override
	public void setID(int id) {
		top.setID(id);
		left.setID(id);
		right.setID(id);
		bottom.setID(id);
	}
	
	@Override
	public int getLinesLength() {
		return top.getLinesLength() * 4;
	}
	
	@Override
	public void fillLines(FloatBuffer buffer) {
		top.fillLines(buffer);
		left.fillLines(buffer);
		right.fillLines(buffer);
		bottom.fillLines(buffer);
	}
}
