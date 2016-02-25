package abstractgame.ui.elements;

import java.nio.FloatBuffer;

import javax.vecmath.Color4f;
import javax.vecmath.Vector2f;

import abstractgame.io.user.KeyIO;
import abstractgame.render.Renderer;
import abstractgame.render.UIRenderer;

public class CheckBox extends UIElement {
	boolean checked = false;
	boolean enabled = false;
	
	HexFill fill;
	HexFill baseFill;
	HexLine line;
	int ID;
	
	/** Size is the height of the checkbox in screen space. pos is the center of the checkbox */
	public CheckBox(Vector2f pos, float size, float layer, int ID) {
		this.ID = ID;
		
		size *= .5f; //r = d / 2;
		
		line = new HexLine(new Vector2f(pos.x - size * HEX_ASPECT, pos.y - size), new Vector2f(pos.x + size * HEX_ASPECT, pos.y + size), layer + .1f, new Color4f(UIRenderer.BASE_STRONG), ID);
		baseFill = new HexFill(new Vector2f(pos.x - size * HEX_ASPECT, pos.y - size), new Vector2f(pos.x + size * HEX_ASPECT, pos.y + size), layer + .1f, UIRenderer.BACKGROUND, ID);
		fill = new HexFill(new Vector2f(pos.x - size * HEX_ASPECT + INSET_DIST_X, pos.y - size + INSET_DIST_Y), new Vector2f(pos.x + size * HEX_ASPECT - INSET_DIST_X, pos.y + size - INSET_DIST_Y), layer, UIRenderer.BASE_STRONG, ID);
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
