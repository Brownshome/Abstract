package abstractgame.ui.elements;

import java.nio.FloatBuffer;

import javax.vecmath.Color4f;
import javax.vecmath.Vector2f;

import abstractgame.render.GLHandler;
import abstractgame.render.Renderer;
import abstractgame.render.TextRenderer;

public abstract class UIElement {
	static float INSET_DIST_X;
	static float INSET_DIST_Y = .02f;
	static float TAPER_MULT;
	static float HEX_ASPECT;
	static float BORDER = .15f;
	
	/** Updates the constants used in building the UI */
	public static void populateValues() {
		INSET_DIST_X = INSET_DIST_Y * 2 / (float) Math.sqrt(3) * GLHandler.xCorrectionScalar;
		TAPER_MULT = .5f / (float) Math.sqrt(3) * GLHandler.xCorrectionScalar;
		HEX_ASPECT = 2 / (float) Math.sqrt(3) * GLHandler.xCorrectionScalar;
	}
	
	static int ID = 0;
	public static int getNewID() {
		return ID++;
	}
	
	public static void renderTextWithinBounds(Vector2f from, Vector2f to, String text, Color4f colour, int ID, boolean withBorder) {
		float textWidth = TextRenderer.getWidth(text);
		float textHeight = TextRenderer.getHeight(text);

		Vector2f dim = new Vector2f();
		dim.sub(to, from);

		float maxSizeX = dim.x / textWidth * (withBorder ? (1 - BORDER) : 1);
		float maxSizeY = dim.y / textHeight * (withBorder ? (1 - BORDER) : 1);

		float finalSize = Math.min(maxSizeX, maxSizeY);

		Vector2f position = dim;
		position.add(from, to);
		position.x -= textWidth * finalSize;
		position.y -= textHeight * finalSize * 1.25f;
		position.scale(.5f);

		TextRenderer.addString(text, position, finalSize, colour, 0, ID);
	}
	
	/** Sets the ID for click / hover detection, -1 removes the item from click detection but will still
	 * overright items */
	public abstract void setID(int ID);
	
	public void onAdd() {}
	public void onRemove() {}
	
	public void fillLines(FloatBuffer buffer) {}
	public int getLinesLength() { return 0; }
	
	public void fillTriangles(FloatBuffer buffer) {}
	public int getTrianglesLength() { return 0; }
	public void tick() {}
}