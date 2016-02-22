package abstractgame.ui.elements;

import java.nio.FloatBuffer;

public abstract class UIElement {
	/** Sets the ID for click / hover detection, 0 removes the item from click detection but will still
	 * overright items */
	public abstract void setID(int ID);
	
	public void fillLines(FloatBuffer buffer) {}
	public int getLinesLength() { return 0; }
	
	public void fillTriangles(FloatBuffer buffer) {}
	public int getTrianglesLength() { return 0; }
	public void tick() {}
}