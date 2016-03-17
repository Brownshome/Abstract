package abstractgame.io.image;

import java.nio.ByteBuffer;

public class Texture {
	public String name;
	public int width;
	public int height;
	public int glFormat;
	public int glType;
	public int dataSize;
	
	public ByteBuffer data;

	Texture(String name, int glFormat, int glType, int width, int height, ByteBuffer data) {
		this.name = name;
		this.width = width;
		this.height = height;
		this.data = data;
		this.glFormat = glFormat;
		this.glType = glType;
		this.dataSize = data.remaining();
	}
}
