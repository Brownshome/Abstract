package abstractgame.ui.elements;

import java.nio.FloatBuffer;

import javax.vecmath.Color4f;
import javax.vecmath.Vector2f;

import abstractgame.render.GLHandler;
import abstractgame.render.Renderer;
import abstractgame.render.UIRenderer;

public class Quad extends UIElement {
	Vector2f to;
	Vector2f from;
	float layer;
	Color4f colour;
	int ID;
	
	public Quad(Vector2f from, Vector2f to, float layer, Color4f colour, int ID) {
		this.to = to;
		this.from = from;
		this.colour = colour;
		this.layer = layer;
		this.ID = ID;
	}
	
	@Override
	public void setID(int id) {
		ID = id;
	}
	
	@Override
	public int getTrianglesLength() {
		return UIRenderer.FLOATS_PER_VERTEX * 6;
	}
	
	@Override
	public void fillTriangles(FloatBuffer buffer) {
		buffer.put(from.x).put(from.y).put(layer);
		buffer.put(colour.x).put(colour.y).put(colour.z).put(colour.w);
		buffer.put(GLHandler.encodeIDAsFloat(ID));
		
		buffer.put(from.x).put(to.y).put(layer);
		buffer.put(colour.x).put(colour.y).put(colour.z).put(colour.w);
		buffer.put(GLHandler.encodeIDAsFloat(ID));
		
		buffer.put(to.x).put(to.y).put(layer);
		buffer.put(colour.x).put(colour.y).put(colour.z).put(colour.w);
		buffer.put(GLHandler.encodeIDAsFloat(ID));
		
		buffer.put(from.x).put(from.y).put(layer);
		buffer.put(colour.x).put(colour.y).put(colour.z).put(colour.w);
		buffer.put(GLHandler.encodeIDAsFloat(ID));
		
		buffer.put(to.x).put(to.y).put(layer);
		buffer.put(colour.x).put(colour.y).put(colour.z).put(colour.w);
		buffer.put(GLHandler.encodeIDAsFloat(ID));
		
		buffer.put(to.x).put(from.y).put(layer);
		buffer.put(colour.x).put(colour.y).put(colour.z).put(colour.w);
		buffer.put(GLHandler.encodeIDAsFloat(ID));
	}
}
