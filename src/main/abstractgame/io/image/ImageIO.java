package abstractgame.io.image;

import static java.nio.file.StandardOpenOption.CREATE;
import static java.nio.file.StandardOpenOption.READ;
import static java.nio.file.StandardOpenOption.TRUNCATE_EXISTING;
import static java.nio.file.StandardOpenOption.WRITE;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileChannel.MapMode;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.Future;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;
import org.lwjgl.opengl.GL30;

import abstractgame.io.FileIO;
import abstractgame.util.ApplicationException;
import de.matthiasmann.twl.utils.PNGDecoder;
import de.matthiasmann.twl.utils.PNGDecoder.Format;

public class ImageIO {
	static final String IMAGE_DIR = "resources/image/";
	static final String PNG_EXT = ".png";
	static final String GLI_EXT = ".gli";
	static final String DDS_EXT = ".dds";
	static final int GLI_HEADER = 16;
	
	public static Future<Texture> loadPNG(String name, Format desiredFormat) {
		return FileIO.IO_THREAD.submit(() -> {
			Path path = Paths.get(IMAGE_DIR + name + GLI_EXT);
			
			if(!Files.exists(path)) {
				Path pngPath = Paths.get(IMAGE_DIR + name + PNG_EXT);
				
				ByteBuffer buffer;
				PNGDecoder decoder;
				Format format;
				int size;
				try(InputStream in = new BufferedInputStream(Files.newInputStream(pngPath))) {
					decoder = new PNGDecoder(in);
					format = decoder.decideTextureFormat(desiredFormat);
					size = decoder.getHeight() * decoder.getWidth() * format.getNumComponents();
			
					buffer = BufferUtils.createByteBuffer(size);
					decoder.decode(buffer, decoder.getWidth() * format.getNumComponents(), format);
				} catch(IOException io) {
					throw new ApplicationException("Error reading PNG file", io, "IMAGEIO");
				}
				
				Texture texture = new Texture(name, getGLFormat(format), GL11.GL_BYTE, decoder.getWidth(), decoder.getHeight(), (ByteBuffer) buffer.flip());
				
				/*FileIO.IO_THREAD.submit(() -> {
					writeTexture(texture);
				});*/
				
				return texture;
			} else {
				try {
				MappedByteBuffer data = FileIO.readFileMapped(path);
				int width = data.getInt();
				int height = data.getInt();
				int glFormat = data.getInt();
				int glType = data.getInt();
				
				return new Texture(name, glFormat, glType, width, height, data);
				} catch(BufferUnderflowException bufe) {
					throw new ApplicationException("File " + name + " was corrupt", bufe, "IMAGEIO");
				}
			}
		});
	}
	
	static void writeTexture(Texture t) {
		try (FileChannel channel = FileChannel.open(Paths.get(IMAGE_DIR + t.name + GLI_EXT), CREATE, WRITE, READ, TRUNCATE_EXISTING)) {
			MappedByteBuffer b = channel.map(MapMode.READ_WRITE, 0, t.dataSize + GLI_HEADER);
			b.putInt(t.width);
			b.putInt(t.height);
			b.putInt(t.glFormat);
			b.putInt(t.glType);
			b.put(t.data);
		} catch(IOException e) {
			throw new ApplicationException("Failed to write " + t.name + ".gli", e, "IMAGEIO");
		}
	}
	
	static int getGLFormat(Format format) {
		switch(format) {
			case ABGR:
				throw new ApplicationException("Format ABGR is not supported", "IMAGEIO");
			case ALPHA:
				return GL11.GL_ALPHA;
			case BGRA:
				return GL12.GL_BGRA;
			case LUMINANCE:
				return GL11.GL_RED;
			case LUMINANCE_ALPHA:
				return GL30.GL_RG;
			case RGB:
				return GL11.GL_RGB;
			case RGBA:
				return GL11.GL_RGBA;
			default:
				throw new AssertionError();
		}
	}
	
	public static Future<Texture> loadDDS(String location) {
		throw new ApplicationException("Not implemented", "IMAGEIO");
	}
}
