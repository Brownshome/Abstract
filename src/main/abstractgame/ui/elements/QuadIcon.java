package abstractgame.ui.elements;

import java.nio.FloatBuffer;

import javax.vecmath.Color4f;
import javax.vecmath.Vector2f;

import abstractgame.render.GLHandler;
import abstractgame.render.IconRenderer;
import abstractgame.render.Renderer;

public class QuadIcon extends Icon {
	public int icon;
	public Vector2f from;
	public Vector2f to;
	public Color4f colour;
	
	public QuadIcon(Color4f colour, Vector2f from, Vector2f to, String icon, int ID) {
		this.from = from;
		this.to = to;
		this.icon = IconRenderer.getIcon(icon);
		this.colour = colour;
		this.ID = ID;
	}
	
	@Override
	public void fillBuffer(FloatBuffer buffer) {
		float encodedID = GLHandler.encodeIDAsFloat(ID);
		
		buffer.put(from.x).put(from.y);
		buffer.put(colour.x).put(colour.y).put(colour.z).put(colour.w);
		buffer.put(0).put(1).put(icon);
		buffer.put(encodedID);
		
		buffer.put(from.x).put(to.y);
		buffer.put(colour.x).put(colour.y).put(colour.z).put(colour.w);
		buffer.put(0).put(0).put(icon);
		buffer.put(encodedID);
		
		buffer.put(to.x).put(to.y);
		buffer.put(colour.x).put(colour.y).put(colour.z).put(colour.w);
		buffer.put(1).put(0).put(icon);
		buffer.put(encodedID);
		
		buffer.put(from.x).put(from.y);
		buffer.put(colour.x).put(colour.y).put(colour.z).put(colour.w);
		buffer.put(0).put(1).put(icon);
		buffer.put(encodedID);
		
		buffer.put(to.x).put(to.y);
		buffer.put(colour.x).put(colour.y).put(colour.z).put(colour.w);
		buffer.put(1).put(0).put(icon);
		buffer.put(encodedID);
		
		buffer.put(to.x).put(from.y);
		buffer.put(colour.x).put(colour.y).put(colour.z).put(colour.w);
		buffer.put(1).put(1).put(icon);
		buffer.put(encodedID);
	}

	@Override
	public int getLength() {
		return 6 * Icon.FLOAT_PER_VERTEX;
	}
}
