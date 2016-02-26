package abstractgame.ui.elements;

import java.nio.FloatBuffer;

import javax.vecmath.Color4f;
import javax.vecmath.Vector2f;

import abstractgame.Game;
import abstractgame.io.user.KeyIO;
import abstractgame.io.user.TypingRequest;
import abstractgame.render.Renderer;
import abstractgame.render.UIRenderer;

public class SingleLineTextEntry extends UIElement {
	Line base;
	Line leftBottom;
	Line leftTop;
	
	HexFill fill;
	
	Vector2f from;
	Vector2f to;
	final Color4f colour = new Color4f(UIRenderer.BASE);
	
	TypingRequest request;
	
	int ID = getNewID();
	
	public SingleLineTextEntry(Vector2f from, Vector2f to, float layer, boolean limitCharacters) {
		float taperDist = (to.y - from.y) * TAPER_MULT;
		
		base = new Line(new Vector2f(from.x + taperDist, from.y), new Vector2f(to.x, from.y), layer, colour, ID);
		leftBottom = new Line(new Vector2f(from.x, (from.y + to.y) * .5f), new Vector2f(from.x + taperDist, from.y), layer, colour, ID);
		leftTop = new Line(new Vector2f(from.x, (from.y + to.y) * .5f), new Vector2f(from.x + taperDist, to.y), layer, colour, ID);
		
		fill = new HexFill(from, to, taperDist, UIRenderer.BACKGROUND, ID);
	}
	
	int clickHandler;
	public void enableTextEntry() {
		clickHandler = KeyIO.addAction(this::onClick, 0, KeyIO.MOUSE_BUTTON_PRESSED);
	}
	
	public void removeTextEntry() {
		KeyIO.removeAction(clickHandler);
	}
	
	void onClick() {
		Game.close();
	}
	
	@Override
	public int getLinesLength() {
		return base.getLinesLength() * 3;
	}
	
	@Override
	public void tick() {
		if(request == null)
			colour.set(Renderer.hoveredID == ID ? UIRenderer.HIGHLIGHT_STRONG : UIRenderer.BASE);
	}
	
	@Override
	public void fillLines(FloatBuffer buffer) {
		base.fillLines(buffer);
		leftBottom.fillLines(buffer);
		leftTop.fillLines(buffer);
	}
	
	@Override
	public int getTrianglesLength() {
		return fill.getTrianglesLength();
	}
	
	@Override
	public void fillTriangles(FloatBuffer buffer) {
		fill.fillTriangles(buffer);
	}
	
	@Override
	public void setID(int ID) {
		base.setID(ID);
		leftBottom.setID(ID);
		leftTop.setID(ID);
	}
}
