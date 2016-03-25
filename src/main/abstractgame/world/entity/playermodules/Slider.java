package abstractgame.world.entity.playermodules;

public class Slider extends Customization {
	float value;
	
	final float min;
	final float max;
	final int graduation;
	
	/** A graduation of 0 means that the slider is completely smooth */
	public Slider(String name, String description, float min, float max, int graduation) {
		super(name, description);
		this.min = min;
		this.max = max;
		this.graduation = graduation;
	}

	public float getValue() {
		return value;
	}
}
