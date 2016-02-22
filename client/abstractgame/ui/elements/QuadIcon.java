package abstractgame.ui.elements;

import java.nio.FloatBuffer;

import javax.vecmath.Color4f;
import javax.vecmath.Vector2f;

public class QuadIcon extends Icon {
	public int icon;
	public Vector2f from;
	public Vector2f to;
	public Color4f colour;
	public int ID;
	
	public QuadIcon(Color4f colour, Vector2f from, Vector2f to, int icon, int ID) {
		this.from = from;
		this.to = to;
		this.icon = icon;
		this.colour = colour;
		this.ID = ID;
	}
	
	@Override
	public void fillBuffer(FloatBuffer buffer) {
		buffer.put(from.x).put(from.y);
		buffer.put(colour.x).put(colour.y).put(colour.z).put(colour.w);
		buffer.put(0).put(1).put(icon);
		buffer.put(Float.intBitsToFloat(ID));
		
		buffer.put(from.x).put(to.y);
		buffer.put(colour.x).put(colour.y).put(colour.z).put(colour.w);
		buffer.put(0).put(0).put(icon);
		buffer.put(Float.intBitsToFloat(ID));
		
		buffer.put(to.x).put(to.y);
		buffer.put(colour.x).put(colour.y).put(colour.z).put(colour.w);
		buffer.put(1).put(0).put(icon);
		buffer.put(Float.intBitsToFloat(ID));
		
		buffer.put(from.x).put(from.y);
		buffer.put(colour.x).put(colour.y).put(colour.z).put(colour.w);
		buffer.put(0).put(1).put(icon);
		buffer.put(Float.intBitsToFloat(ID));
		
		buffer.put(to.x).put(to.y);
		buffer.put(colour.x).put(colour.y).put(colour.z).put(colour.w);
		buffer.put(1).put(0).put(icon);
		buffer.put(Float.intBitsToFloat(ID));
		
		buffer.put(to.x).put(from.y);
		buffer.put(colour.x).put(colour.y).put(colour.z).put(colour.w);
		buffer.put(1).put(1).put(icon);
		buffer.put(Float.intBitsToFloat(ID));
	}

	@Override
	public int getLength() {
		return 6 * Icon.FLOAT_PER_VERTEX;
	}

}
