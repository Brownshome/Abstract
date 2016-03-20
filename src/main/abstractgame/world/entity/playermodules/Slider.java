package abstractgame.world.entity.playermodules;

public class Slider extends Customization {
	double value;
	
	final double min;
	final double max;
	final int graduation;
	
	/** A graduation of 0 means that the slider is completely smooth */
	public Slider(String name, String description, double min, double max, int graduation) {
		super(name, description);
		this.min = min;
		this.max = max;
		this.graduation = graduation;
	}

	public double getValue() {
		return value;
	}
}
