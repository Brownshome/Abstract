package abstractgame.ui.elements;

import java.nio.FloatBuffer;

import javax.vecmath.Color4f;
import javax.vecmath.Vector2f;

import abstractgame.io.user.KeyIO;
import abstractgame.render.Renderer;
import abstractgame.render.UIRenderer;

public class CheckBox extends UIElement {
	static final float BORDER = .015f;
	
	boolean checked = false;
	boolean enabled = false;
	
	HexFill fill;
	HexFill baseFill;
	HexLine line;
	int ID;
	
	public CheckBox(Vector2f pos, float size, float layer, int ID) {
		float xMult = 2 / (float) Math.sqrt(3) * Renderer.xCorrectionScalar;
		size *= .5f;
		this.ID = ID;
		
		line = new HexLine(new Vector2f(pos.x - size * xMult, pos.y - size), new Vector2f(pos.x + size * xMult, pos.y + size), layer + .1f, new Color4f(UIRenderer.BASE_STRONG), ID);
		baseFill = new HexFill(new Vector2f(pos.x - size * xMult, pos.y - size), new Vector2f(pos.x + size * xMult, pos.y + size), layer + .1f, UIRenderer.BACKGROUND, ID);
		
		final float DIST = .02f;
		final float DIST_X = (float) (DIST * 2 / Math.sqrt(3)) * Renderer.xCorrectionScalar;
		
		fill = new HexFill(new Vector2f(pos.x - size * xMult + DIST_X, pos.y - size + DIST), new Vector2f(pos.x + size * xMult - DIST_X, pos.y + size - DIST), layer, UIRenderer.BASE_STRONG, ID);
	}
	
	int clickID = 0;
	public void enable() {
		clickID = KeyIO.addAction(() -> {
			if(ID == Renderer.hoveredID) 
				setState(!checked);
		}, 0, KeyIO.MOUSE_BUTTON_PRESSED);
		
		enabled = true;
	}
	
	public void disable() {
		KeyIO.removeAction(clickID);
		enabled = true;
	}
	
	public void setState(boolean state) {
		checked = state;
	}
	
	@Override
	public void setID(int ID) {
		fill.setID(ID);
		line.setID(ID);
		baseFill.setID(ID);
	}
	
	@Override
	public int getTrianglesLength() {
		return baseFill.getTrianglesLength() + (checked ? fill.getTrianglesLength() : 0);
	}
	
	@Override
	public void tick() {
		line.colour.set(ID == Renderer.hoveredID && enabled ? UIRenderer.HIGHLIGHT_STRONG : UIRenderer.BASE_STRONG);
	}
	
	@Override
	public void fillTriangles(FloatBuffer buffer) {
		baseFill.fillTriangles(buffer);
		
		if(checked)
			fill.fillTriangles(buffer);
	}
	
	@Override
	public int getLinesLength() {
		return line.getLinesLength();
	}
	
	@Override
	public void fillLines(FloatBuffer buffer) {
		line.fillLines(buffer);
	}
}
