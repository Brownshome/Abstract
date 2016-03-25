package abstractgame.ui.elements;

import java.nio.FloatBuffer;

public abstract class Icon {
	public int ID = -1;
	
	public final static int FLOAT_PER_VERTEX = 10;
	
	public abstract void fillBuffer(FloatBuffer buffer);
	
	/** The length in floats */
	public abstract int getLength();
	
	public int getID() {
		return ID;
	}
	
	public void setID(int ID) {
		this.ID = ID;
	}
}
