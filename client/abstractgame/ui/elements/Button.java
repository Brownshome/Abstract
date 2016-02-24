package abstractgame.ui.elements;

import java.nio.FloatBuffer;

import javax.vecmath.Color4f;
import javax.vecmath.Vector2f;

import abstractgame.io.user.KeyIO;
import abstractgame.render.Renderer;
import abstractgame.render.TextRenderer;
import abstractgame.render.UIRenderer;

public abstract class Button extends UIElement {
	public static class Strong extends Button {
		HexFill fill;
		HexLine line;

		public Strong(Vector2f from, Vector2f to, String text, float layer, float size, int ID) {
			super(new Vector2f(from), new Vector2f(to), text, layer, size, ID);
			fill = new HexFill(from, to, layer + .1f, UIRenderer.BASE_STRONG, ID);

			float taperDist = HexFill.TAPER_MULT * (to.y - from.y) * Renderer.xCorrectionScalar;

			super.from.x += taperDist;
			super.to.x -= taperDist;

			final float DIST = .015f;
			final float DIST_X = (float) (DIST * 2 / Math.sqrt(3)) * Renderer.xCorrectionScalar;
			
			line = new HexLine(new Vector2f(from.x + DIST_X, from.y + DIST), new Vector2f(to.x - DIST_X, to.y - DIST), layer, UIRenderer.BACKGROUND, ID);
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
			return ID == Renderer.hoveredID ? line.getLinesLength() : 0;
		}

		@Override
		public void fillLines(FloatBuffer buffer) {
			if(ID == Renderer.hoveredID) {
				line.fillLines(buffer);
			}
		}
	}

	public static class Weak extends Button {
		HexLine outer;
		HexLine inner;
		HexFill fill;

		public Weak(Vector2f from, Vector2f to, String text, float layer, float size, int ID) {
			super(new Vector2f(from), new Vector2f(to), text, layer, size, ID);

			float taperDist = HexFill.TAPER_MULT * (to.y - from.y) * Renderer.xCorrectionScalar;

			super.from.x += taperDist;
			super.to.x -= taperDist;

			//inners
			final float DIST = .015f;
			final float DIST_X = (float) (DIST * 2 / Math.sqrt(3)) * Renderer.xCorrectionScalar;

			outer = new HexLine(from, to, layer, UIRenderer.BASE_STRONG, ID);
			inner = new HexLine(new Vector2f(from.x + DIST_X, from.y + DIST), new Vector2f(to.x - DIST_X, to.y - DIST), layer, UIRenderer.BASE_STRONG, ID);
			fill = new HexFill(from, to, layer + .2f, UIRenderer.BACKGROUND, ID);
		}

		@Override
		Color4f getTextColour() {
			return UIRenderer.BASE_STRONG;
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
		public int getLinesLength() {
			return outer.getLinesLength() + ((ID == Renderer.hoveredID) ? inner.getLinesLength() : 0);
		}

		@Override
		public void fillLines(FloatBuffer buffer) {
			outer.fillLines(buffer);

			if(ID == Renderer.hoveredID) {
				inner.fillLines(buffer);
			}
		}

		@Override
		public void fillTriangles(FloatBuffer buffer) {
			fill.fillTriangles(buffer);
		}
	}

	final static float BORDER = 0.15f;

	Vector2f from;
	Vector2f to;
	String text;
	float layer;
	float size;
	int ID;

	public Button(Vector2f from, Vector2f to, String text, float layer, float size, int ID) {
		this.from  = from;
		this.to = to;
		this.text = text;
		this.layer = layer;
		this.ID = ID;
		this.size = size;
	}

	@Override
	public void tick() {
		float textWidth = TextRenderer.getWidth(text);
		float textHeight = TextRenderer.getHeight(text);

		Vector2f dim = new Vector2f();
		dim.sub(to, from);

		float maxSizeX = dim.x / textWidth * (1 - BORDER);
		float maxSizeY = dim.y / textHeight * (1 - BORDER);

		float finalSize = Math.min(Math.min(maxSizeX, maxSizeY), size);

		Vector2f position = dim;
		position.add(from, to);
		position.x -= textWidth * finalSize;
		position.y -= textHeight * finalSize * 1.25f;
		position.scale(.5f);

		TextRenderer.addString(text, position, finalSize, getTextColour(), 0, ID);
	}

	abstract Color4f getTextColour();

	int clickListenerID = 0;
	public void addOnClick(Runnable task) {
		clickListenerID = KeyIO.addAction(() -> {
			if(Renderer.hoveredID == ID)
				task.run();
		}, 0, KeyIO.MOUSE_BUTTON_PRESSED);
	}

	public void removeOnClick() {
		KeyIO.removeAction(clickListenerID);
	}
}
