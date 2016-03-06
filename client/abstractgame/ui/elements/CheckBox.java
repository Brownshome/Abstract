package abstractgame.ui.elements;

import java.nio.FloatBuffer;

import javax.vecmath.Color4f;
import javax.vecmath.Vector2f;

import abstractgame.io.user.PerfIO;
import abstractgame.render.Renderer;
import abstractgame.render.UIRenderer;

public class CheckBox extends UIElement {
	boolean checked = false;
	
	HexFill fill;
	HexFill baseFill;
	HexLine line;
	int ID;

	public boolean disabled;
	
	/** Size is the height of the checkbox in screen space. pos is the center of the checkbox */
	public CheckBox(Vector2f pos, float size, float layer, int ID) {
		this.ID = ID;
		
		size *= .5f; //r = d / 2;
		
		line = new HexLine(new Vector2f(pos.x - size * HEX_ASPECT, pos.y - size), new Vector2f(pos.x + size * HEX_ASPECT, pos.y + size), layer + .1f, new Color4f(UIRenderer.BASE_STRONG), ID);
		baseFill = new HexFill(new Vector2f(pos.x - size * HEX_ASPECT, pos.y - size), new Vector2f(pos.x + size * HEX_ASPECT, pos.y + size), layer + .1f, UIRenderer.BACKGROUND, ID);
		fill = new HexFill(new Vector2f(pos.x - size * HEX_ASPECT + INSET_DIST_X, pos.y - size + INSET_DIST_Y), new Vector2f(pos.x + size * HEX_ASPECT - INSET_DIST_X, pos.y + size - INSET_DIST_Y), layer, UIRenderer.BASE_STRONG, ID);
	}
	
	int clickID = 0;
	@Override
	public void onAdd() {
		clickID = PerfIO.addMouseListener(() -> {
			if(ID == Renderer.hoveredID && !disabled) 
				setState(!checked);
		}, 0, PerfIO.BUTTON_PRESSED);
	}
	
	@Override
	public void onRemove() {
		PerfIO.removeMouseListener(clickID);
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
		fill.colour = disabled ? UIRenderer.BASE : UIRenderer.BASE_STRONG;
		line.colour.set(disabled ? UIRenderer.BASE : ID == Renderer.hoveredID ? UIRenderer.HIGHLIGHT_STRONG : UIRenderer.BASE_STRONG);
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
	
	public boolean getState() {
		return checked;
	}
}
