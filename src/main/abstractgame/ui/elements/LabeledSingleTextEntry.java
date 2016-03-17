package abstractgame.ui.elements;

import javax.vecmath.Vector2f;

import abstractgame.render.TextRenderer;
import abstractgame.render.UIRenderer;

public class LabeledSingleTextEntry extends SingleLineTextEntry {
	String label;
	Vector2f oldStart;
	
	public LabeledSingleTextEntry(Vector2f from, Vector2f to, float layer, String label) {
		super(new Vector2f(from.x + TextRenderer.getWidth(label) * (to.y - from.y) * .8f, from.y), to, layer);
		
		float taperDist = (to.y - from.y) * TAPER_MULT;
		oldStart = new Vector2f(from.x + taperDist, from.y);
		
		this.label = label;
	}
	
	@Override
	public void tick() {
		super.tick();
		
		TextRenderer.addString(label, oldStart, super.textSize, disabled ? UIRenderer.BASE : UIRenderer.BASE_STRONG, 0);
	}
}
