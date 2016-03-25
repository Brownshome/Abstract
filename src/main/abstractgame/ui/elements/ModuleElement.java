package abstractgame.ui.elements;

import java.nio.FloatBuffer;

import javax.vecmath.Color4f;
import javax.vecmath.Vector2f;

import abstractgame.render.IconRenderer;
import abstractgame.render.UIRenderer;
import abstractgame.world.entity.playermodules.UpgradeModule;

/** Displays the HUD for one particular module */
public class ModuleElement extends UIElement {
	public static enum State {
		ACTIVE(UIRenderer.BACKGROUND, UIRenderer.HIGHLIGHT_STRONG),
		LOCKED(UIRenderer.BASE),
		IDLE(UIRenderer.BASE_STRONG),
		HIDDEN(),
		PASSIVE(UIRenderer.HIGHLIGHT_STRONG);

		public final Color4f lineColour;
		public final Color4f fillColour;
		public final Color4f iconColour;
		public final boolean isVisible;

		State() {
			lineColour = null;
			fillColour = null;
			iconColour = null;
			isVisible = false;
		}

		State(Color4f colour) {
			lineColour = colour;
			fillColour = UIRenderer.BACKGROUND;
			iconColour = colour;
			isVisible = true;
		}
		
		State(Color4f iconColour, Color4f fillColour) {
			this.lineColour = UIRenderer.TRANSPARENT;
			this.fillColour = fillColour;
			this.iconColour = iconColour;
			isVisible = true;
		}
	}

	UpgradeModule module;
	int ID = -1;

	Color4f iconColour;
	Color4f lineColour;
	Color4f fillColour;

	State state;
	
	HexFill fill;
	HexLine line;
	QuadIcon icon;

	public ModuleElement(UpgradeModule upgradeModule) {
		module = upgradeModule;
		state = upgradeModule.getState();
		lineColour = new Color4f();
		iconColour = new Color4f();
		fillColour = new Color4f();
	}

	/** This must be called before the first UI tick */
	public void setDims(Vector2f from, Vector2f to, float layer) {
		fill = new HexFill(from, to, layer, fillColour, ID);
		line = new HexLine(from, to, layer - .1f, lineColour, ID);
		icon = new QuadIcon(iconColour, from, to, module.getImage(), ID);
	}

	@Override
	public void tick() {
		assert fill != null && line != null;

		state = module.getState();

		if(state.isVisible) {
			lineColour.set(state.lineColour);
			fillColour.set(state.fillColour);
			iconColour.set(state.iconColour);
			
			IconRenderer.addIcon(icon);
		}
	}

	@Override
	public int getTrianglesLength() {
		return state.isVisible ? fill.getTrianglesLength() : 0;
	}

	@Override
	public void fillTriangles(FloatBuffer buffer) {
		if(state.isVisible)
			fill.fillTriangles(buffer);
	}

	@Override
	public int getLinesLength() {
		return state.isVisible ? line.getLinesLength() : 0;
	}

	@Override
	public void fillLines(FloatBuffer buffer) {
		if(state.isVisible)
			line.fillLines(buffer);
	}

	@Override
	public void setID(int ID) {
		this.ID = ID;
		if(fill != null) {
			fill.setID(ID);
			line.setID(ID);
			icon.setID(ID);
		}
	}
}
