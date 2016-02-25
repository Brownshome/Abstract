package abstractgame.ui.elements;

import java.nio.FloatBuffer;

import javax.vecmath.Color4f;
import javax.vecmath.Vector2f;

import abstractgame.render.Renderer;
import abstractgame.render.UIRenderer;

public class HexFill extends UIElement {
	Vector2f to;
	Vector2f from;
	float layer;
	final float taperDist;
	Color4f colour;
	int ID;
	
	public HexFill(Vector2f from, Vector2f to, float layer, Color4f colour, int ID) {
		this.to = to;
		this.from = from;
		this.colour = colour;
		this.layer = layer;
		taperDist = (to.y - from.y) * TAPER_MULT;
		this.ID = ID;
	}
	
	@Override
	public void setID(int id) {
		ID = id;
	}
	
	@Override
	public int getTrianglesLength() {
		return UIRenderer.FLOATS_PER_VERTEX * 12;
	}
	
	void putVertex(float x, float y, FloatBuffer buffer) {
		buffer.put(x).put(y).put(layer);
		buffer.put(colour.x).put(colour.y).put(colour.z).put(colour.w);
		buffer.put(Renderer.encodeIDAsFloat(ID));
	}
	
	@Override
	public void fillTriangles(FloatBuffer buffer) {
		putVertex(from.x + taperDist, from.y, buffer);
		putVertex(from.x, (to.y + from.y) * .5f, buffer);
		putVertex(from.x + taperDist, to.y, buffer);
		
		putVertex(from.x + taperDist, from.y, buffer);
		putVertex(from.x + taperDist, to.y, buffer);
		putVertex(to.x - taperDist, from.y, buffer);
		
		putVertex(to.x - taperDist, from.y, buffer);
		putVertex(from.x + taperDist, to.y, buffer);
		putVertex(to.x - taperDist, to.y, buffer);
		
		putVertex(to.x - taperDist, from.y, buffer);
		putVertex(to.x - taperDist, to.y, buffer);
		putVertex(to.x, (to.y + from.y) * .5f, buffer);
	}
}
