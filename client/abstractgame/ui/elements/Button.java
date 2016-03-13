package abstractgame.ui.elements;

import java.nio.FloatBuffer;

import javax.vecmath.Color4f;
import javax.vecmath.Vector2f;

import abstractgame.io.user.PerfIO;
import abstractgame.render.GLHandler;
import abstractgame.render.Renderer;
import abstractgame.render.TextRenderer;
import abstractgame.render.UIRenderer;

public abstract class Button extends UIElement {
	public static class Strong extends Button {
		HexFill fill;
		HexLine line;

		public Strong(Vector2f from, Vector2f to, String text, Runnable task, float layer, int ID) {
			super(new Vector2f(from), new Vector2f(to), text, task, layer, ID);
			fill = new HexFill(from, to, layer + .1f, UIRenderer.BASE_STRONG, ID);

			float taperDist = (to.y - from.y) * TAPER_MULT;

			super.from.x += taperDist;
			super.to.x -= taperDist;

			line = new HexLine(new Vector2f(from.x, from.y), new Vector2f(to.x, to.y), layer, UIRenderer.HIGHLIGHT_STRONG, ID);
		}

		public Strong(Vector2f from, Vector2f to, String text, Runnable task) {
			this(from, to, text, task, 0, UIElement.getNewID());
		}

		@Override
		Color4f getTextColour() {
			return UIRenderer.BACKGROUND;
		}

		@Override
		public void setID(int ID) {
			fill.ID = ID;
		}

		@Override
		public void tick() {
			super.tick();
			
			fill.colour = disabled ? UIRenderer.BASE : UIRenderer.BASE_STRONG;
		}

		@Override
		public int getTrianglesLength() {
			return fill.getTrianglesLength();
		}

		@Override
		public void fillTriangles(FloatBuffer buffer) {
			fill.fillTriangles(buffer);
		}

		@Override
		public int getLinesLength() {
			return !disabled && ID == GLHandler.hoveredID ? line.getLinesLength() : 0;
		}

		@Override
		public void fillLines(FloatBuffer buffer) {
			if(ID == GLHandler.hoveredID) {
				line.fillLines(buffer);
			}
		}
	}

	public static class Weak extends Button {
		HexLine outline;
		HexFill fill;

		public Weak(Vector2f from, Vector2f to, String text, Runnable task, float layer, int ID) {
			super(new Vector2f(from), new Vector2f(to), text, task, layer, ID);

			float taperDist = HexFill.TAPER_MULT * (to.y - from.y) * GLHandler.xCorrectionScalar;

			super.from.x += taperDist;
			super.to.x -= taperDist;

			outline = new HexLine(from, to, layer, new Color4f(UIRenderer.BASE_STRONG), ID);
			fill = new HexFill(from, to, layer + .1f, UIRenderer.BACKGROUND, ID);
		}

		public Weak(Vector2f from, Vector2f to, String text, Runnable task) {
			this(from, to, text, task, 0, UIElement.getNewID());
		}

		@Override
		Color4f getTextColour() {
			return disabled ? UIRenderer.BASE : UIRenderer.BASE_STRONG;
		}

		@Override
		public void setID(int ID) {
			fill.setID(ID);
		}

		@Override
		public int getTrianglesLength() {
			return fill.getTrianglesLength();
		}

		@Override
		public void tick() {
			super.tick();
			
			outline.colour.set(disabled ? UIRenderer.BASE : ID == GLHandler.hoveredID ? UIRenderer.HIGHLIGHT_STRONG : UIRenderer.BASE_STRONG);
		}
		
		@Override
		public int getLinesLength() {
			return outline.getLinesLength();
		}

		@Override
		public void fillLines(FloatBuffer buffer) {
			outline.fillLines(buffer);
		}

		@Override
		public void fillTriangles(FloatBuffer buffer) {
			fill.fillTriangles(buffer);
		}
	}

	Vector2f from;
	Vector2f to;
	String text;
	Runnable task;
	float layer;
	int ID;
	
	public boolean disabled = false;

	public Button(Vector2f from, Vector2f to, String text, Runnable task, float layer, int ID) {
		this.task = task;
		this.from  = from;
		this.to = to;
		this.text = text;
		this.layer = layer;
		this.ID = ID;
	}
	
	@Override
	public void tick() {
		UIElement.renderTextWithinBounds(from, to, text, getTextColour(), ID, true);
	}

	abstract Color4f getTextColour();

	int clickListenerID;
	@Override
	public void onAdd() {
		clickListenerID = PerfIO.addMouseListener(() -> {
			if(GLHandler.hoveredID == ID && !disabled)
				task.run();
		}, 0, PerfIO.BUTTON_PRESSED);
	}

	@Override
	public void onRemove() {
		PerfIO.removeMouseListener(clickListenerID);
	}
}
