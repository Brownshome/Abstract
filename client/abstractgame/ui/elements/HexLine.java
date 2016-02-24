package abstractgame.ui.elements;

import java.nio.FloatBuffer;

import javax.vecmath.Color4f;
import javax.vecmath.Vector2f;

import abstractgame.render.Renderer;

public class HexLine extends UIElement {
	Line top;
	Line bottom;

	Line leftTop;
	Line leftBottom;

	Line rightTop;
	Line rightBottom;

	Color4f colour;
	
	public HexLine(Vector2f from, Vector2f to, float layer, Color4f colour, int ID) {
		this.colour = colour;
		final float taperDist = HexFill.TAPER_MULT * (to.y - from.y) * Renderer.xCorrectionScalar;
		
		top = new Line(new Vector2f(from.x + taperDist, to.y), new Vector2f(to.x - taperDist, to.y), layer, colour, ID);
		bottom = new Line(new Vector2f(from.x + taperDist, from.y), new Vector2f(to.x - taperDist, from.y), layer, colour, ID);

		leftTop = new Line(new Vector2f(from.x, (to.y + from.y) * .5f), new Vector2f(from.x + taperDist, to.y), layer, colour, ID);
		leftBottom = new Line(new Vector2f(from.x, (to.y + from.y) * .5f), new Vector2f(from.x + taperDist, from.y), layer, colour, ID);

		rightTop = new Line(new Vector2f(to.x - taperDist, to.y), new Vector2f(to.x, (to.y + from.y) * .5f), layer, colour, ID);
		rightBottom = new Line(new Vector2f(to.x - taperDist, from.y), new Vector2f(to.x, (to.y + from.y) * .5f), layer, colour, ID);
	}
	
	@Override
	public void setID(int ID) {
		top.setID(ID);
		bottom.setID(ID);
		
		leftTop.setID(ID);
		leftBottom.setID(ID);
		
		rightTop.setID(ID);
		rightBottom.setID(ID);
	}
	
	@Override
	public int getLinesLength() {
		return top.getLinesLength() * 6;
	}
	
	@Override
	public void fillLines(FloatBuffer buffer) {
		top.fillLines(buffer);
		bottom.fillLines(buffer);
		
		leftTop.fillLines(buffer);
		leftBottom.fillLines(buffer);
		
		rightTop.fillLines(buffer);
		rightBottom.fillLines(buffer);
	}
}
