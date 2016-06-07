package abstractgame.world.entity.playermodules;

public class Slider extends Customization {
	float value;
	
	final float min;
	final float max;
	final int graduation;
	
	/** A graduation of 0 means that the slider is completely smooth 
	 * 
	 * @param name The name of this slider
	 * @param description A description of ths slider
	 * @param min The minimum value that this slider can return
	 * @param max The maximum value that this slider can return
	 * @param graduation The number of graducations along the slider */
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
