package abstractgame.ui.elements;

import java.nio.FloatBuffer;

import javax.vecmath.Vector2f;

import abstractgame.render.UIRenderer;

public class NamedCheckBox extends CheckBox {
	Line top;
	Line bottom;

	Line leftTop;
	Line leftBottom;

	Line rightTop;
	Line rightBottom;
	
	Vector2f from;
	Vector2f to;
	
	String text;
	
	public NamedCheckBox(Vector2f from, Vector2f to, String text, float layer, int ID) {
		super(new Vector2f(to.x - (to.y - from.y) * HEX_ASPECT * .5f, (from.y + to.y) * .5f), to.y - from.y, layer, ID);
		
		this.text = text;
		
		super.baseFill.from = from;
		super.baseFill.to = to;
		
		float taperDist = (to.y - from.y) * TAPER_MULT;
		
		leftTop = new Line(new Vector2f(from.x, (to.y + from.y) * .5f), new Vector2f(from.x + taperDist, to.y), layer, super.line.colour, ID);
		leftBottom = new Line(new Vector2f(from.x, (to.y + from.y) * .5f), new Vector2f(from.x + taperDist, from.y), layer, super.line.colour, ID);
		
		float start = to.x - (to.y - from.y) * HEX_ASPECT - INSET_DIST_X;
		
		top = new Line(new Vector2f(from.x + taperDist, to.y), new Vector2f(start + taperDist, to.y), layer, super.line.colour, ID);
		bottom = new Line(new Vector2f(from.x + taperDist, from.y), new Vector2f(start + taperDist, from.y), layer, super.line.colour, ID);
		
		rightTop = new Line(new Vector2f(start + taperDist, to.y), new Vector2f(start, (to.y + from.y) * .5f), layer, super.line.colour, ID);
		rightBottom = new Line(new Vector2f(start + taperDist, from.y), new Vector2f(start, (to.y + from.y) * .5f), layer, super.line.colour, ID);
	
		this.from = new Vector2f(from.x - taperDist, from.y);
		this.to = new Vector2f(start, to.y);
	}
	
	public NamedCheckBox(Vector2f from, Vector2f to, String text, float layer) {
		this(from, to, text, layer, UIElement.getNewID());
	}

	@Override
	public void tick() {
		super.tick();
		
		UIElement.renderTextWithinBounds(from, to, text, disabled ? UIRenderer.BASE : UIRenderer.BASE_STRONG, ID, true);
	}
	
	@Override
	public int getLinesLength() {
		return super.getLinesLength() + top.getLinesLength() * 6;
	}
	
	@Override
	public void fillLines(FloatBuffer buffer) {
		super.fillLines(buffer);
		
		top.fillLines(buffer);
		bottom.fillLines(buffer);
		leftTop.fillLines(buffer);
		leftBottom.fillLines(buffer);
		rightTop.fillLines(buffer);
		rightBottom.fillLines(buffer);
	}
}
