package abstractgame.imageconverter;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.function.BiFunction;
import java.util.stream.IntStream;

import javax.imageio.ImageIO;

import de.matthiasmann.twl.utils.PNGDecoder;
import de.matthiasmann.twl.utils.PNGDecoder.Format;

public class ImageConverter {
	/** Use source, destination, spread, destResX, destResY. Note that this tool reads png files and nothing else. 
	 * spread is the number of pixel-lengths that the algorithm will search.
	 * @throws IOException */
	public static void main(String[] args) throws IOException {
		if(args.length != 5)
			throw new IllegalArgumentException("Invalid arguments, expected: source, destination, spreadf, destResX, destResY");

		String source = args[0];
		String destination = args[1];
		int spread = Integer.decode(args[2]);
		int destResX = Integer.decode(args[3]);
		int destResY = Integer.decode(args[4]);
		BufferedImage image = new BufferedImage(destResX, destResY, BufferedImage.TYPE_INT_ARGB);

		PNGDecoder decoder = new PNGDecoder(Files.newInputStream(Paths.get(source)));
		Format format = decoder.decideTextureFormat(Format.RGB);

		int height = decoder.getHeight();
		int width = decoder.getWidth();

		BiFunction<Integer, Integer, Integer> I = (x, y) -> x * format.getNumComponents() + y * width * format.getNumComponents();

		byte[] in = new byte[width * height * format.getNumComponents()];
		ByteBuffer buffer = ByteBuffer.wrap(in);
		decoder.decode(buffer, decoder.getWidth() * format.getNumComponents(), format);

		int pppx = width / destResX;
		int pppy = height / destResY;

		class Counter {
			int count = 0;
			int lastPerc = 0;
			
			synchronized void done() {
				if(lastPerc != (lastPerc = ++count * 100 / destResX))
					System.out.println(lastPerc + "%");
			}
		}
		
		Counter c = new Counter();
		
		IntStream.range(0, destResX).parallel().forEach(x -> {
			for(int y = 0; y < destResY; y++) {
				//decide whether the sample is inside or outside

				//x & 0x80 != 0  ==  x > 127 in unsigned

				int count = 0;
				for(int i = x * pppx; i < (x + 1) * pppx; i++) {
					for(int j = y * pppy; j < (y + 1) * pppy; j++) {
						if((in[I.apply(i, j)] & 0x80) == 0)
							count--;
						else
							count++;
					}
				}

				boolean inside = count > 0;

				//compute distance
				float closest = -1;
				for(int i = Math.max(x * pppx + pppx / 2 - spread, 0); i < Math.min(x * pppx + pppx / 2 + spread, width); i++) {
					for(int j = Math.max(y * pppy + pppy / 2 - spread, 0); j < Math.min(y * pppy + pppy / 2 + spread, height); j++) {
						if((in[I.apply(i, j)] & 0x80) != 0 != inside) {
							int dx = x * pppx + pppx / 2 - i;
							int dy = y * pppy + pppy / 2 - j;

							float dist = dx * dx + dy * dy;
							dist = (float) Math.sqrt(dist);

							if(closest == -1)
								closest = dist;
							else
								closest = Math.min(closest, dist);
						}
					}
				}

				int value = inside ? 0 : 255;
				if(closest != -1) {
					if(inside) {
						value = 128 - (int) Math.min((closest * 384 / spread), 128);
					} else {
						value = 127 + (int) Math.min((closest * 384 / spread), 128);
					}
				}

				//value = inside ? 0 : 255;
				value = 0xff000000 | value | value << 8 | value << 16;
				image.setRGB(x, y, value);
			}
			
			c.done();
		});

		ImageIO.write(image, "png", Paths.get(destination).toFile());
	}
}
