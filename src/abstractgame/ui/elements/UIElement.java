package abstractgame.ui.elements;

import java.nio.FloatBuffer;

public abstract class UIElement {
	public void fillLines(FloatBuffer buffer) {}
	public int getLinesLength() { return 0; }
	
	public void fillTriangles(FloatBuffer buffer) {}
	public int getTrianglesLength() { return 0; }
	public void tick() {}
}