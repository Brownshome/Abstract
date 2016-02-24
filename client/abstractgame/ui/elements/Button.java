package abstractgame.ui.elements;

import java.nio.FloatBuffer;

import javax.vecmath.Color4f;
import javax.vecmath.Vector2f;

import abstractgame.io.user.KeyBinds;
import abstractgame.io.user.KeyIO;
import abstractgame.render.Renderer;
import abstractgame.render.TextRenderer;
import abstractgame.render.UIRenderer;

public abstract class Button extends UIElement {
	public static class Strong extends Button {
		ButtonFill fill;
		
		public Strong(Vector2f from, Vector2f to, String text, float layer, float size, int ID) {
			super(new Vector2f(from), new Vector2f(to), text, layer, size, ID);
			fill = new ButtonFill(from, to, layer, UIRenderer.BASE_STRONG, ID);
			
			super.from.x += ButtonFill.TAPER_MULT * (to.y - from.y);
			super.to.x -= ButtonFill.TAPER_MULT * (to.y - from.y);
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
			
			fill.colour = Renderer.hoveredID == ID ? UIRenderer.HIGHLIGHT_STRONG : UIRenderer.BASE_STRONG;
		}
		
		@Override
		public int getTrianglesLength() {
			return fill.getTrianglesLength();
		}
		
		@Override
		public void fillTriangles(FloatBuffer buffer) {
			fill.fillTriangles(buffer);
		}
	}
	
	public static class Weak extends Button {
		Line top;
		Line bottom;
		
		Line leftTop;
		Line leftBottom;
		
		Line rightTop;
		Line rightBottom;
		
		Line innerTop;
		Line innerBottom;
		
		Line innerLeftTop;
		Line innerLeftBottom;
		
		Line innerRightTop;
		Line innerRightBottom;
		
		ButtonFill fill;
		
		public Weak(Vector2f from, Vector2f to, String text, float layer, float size, int ID) {
			super(new Vector2f(from), new Vector2f(to), text, layer, size, ID);

			float taperDist = ButtonFill.TAPER_MULT * (to.y - from.y) * Renderer.xCorrectionScalar;
			
			super.from.x += taperDist;
			super.to.x -= taperDist;
			
			top = new Line(new Vector2f(from.x + taperDist, to.y), new Vector2f(to.x - taperDist, to.y), layer + .1f, UIRenderer.BASE, ID);
			bottom = new Line(new Vector2f(from.x + taperDist, from.y), new Vector2f(to.x - taperDist, from.y), layer + .1f, UIRenderer.BASE, ID);
			
			leftTop = new Line(new Vector2f(from.x, (to.y + from.y) * .5f), new Vector2f(from.x + taperDist, to.y), layer + .1f, UIRenderer.BASE, ID);
			leftBottom = new Line(new Vector2f(from.x, (to.y + from.y) * .5f), new Vector2f(from.x + taperDist, from.y), layer + .1f, UIRenderer.BASE, ID);
			
			rightTop = new Line(new Vector2f(to.x - taperDist, to.y), new Vector2f(to.x, (to.y + from.y) * .5f), layer + .1f, UIRenderer.BASE, ID);
			rightBottom = new Line(new Vector2f(to.x - taperDist, from.y), new Vector2f(to.x, (to.y + from.y) * .5f), layer + .1f, UIRenderer.BASE, ID);
		
			//inners
			final float DIST = .01f;
			final float DIST_X = (float) (DIST * 2 / Math.sqrt(3)) * Renderer.xCorrectionScalar;
			final float DIST_TOP_X = (float) (DIST * 1 / Math.sqrt(3)) * Renderer.xCorrectionScalar;
			
			innerTop = new Line(new Vector2f(from.x + taperDist + DIST_TOP_X, to.y - DIST), new Vector2f(to.x - taperDist - DIST_TOP_X, to.y - DIST), layer + .1f, UIRenderer.BASE, ID);
			innerBottom = new Line(new Vector2f(from.x + taperDist + DIST_TOP_X, from.y + DIST), new Vector2f(to.x - taperDist - DIST_TOP_X, from.y + DIST), layer + .1f, UIRenderer.BASE, ID);
			
			innerLeftTop = new Line(new Vector2f(from.x + DIST_X, (to.y + from.y) * .5f), new Vector2f(from.x + taperDist + DIST_TOP_X, to.y - DIST), layer + .1f, UIRenderer.BASE, ID);
			innerLeftBottom = new Line(new Vector2f(from.x + DIST_X, (to.y + from.y) * .5f), new Vector2f(from.x + taperDist + DIST_TOP_X, from.y + DIST), layer + .1f, UIRenderer.BASE, ID);
			
			innerRightTop = new Line(new Vector2f(to.x - taperDist - DIST_TOP_X, to.y - DIST), new Vector2f(to.x - DIST_X, (to.y + from.y) * .5f), layer + .1f, UIRenderer.BASE, ID);
			innerRightBottom = new Line(new Vector2f(to.x - taperDist - DIST_TOP_X, from.y + DIST), new Vector2f(to.x - DIST_X, (to.y + from.y) * .5f), layer + .1f, UIRenderer.BASE, ID);
			
			fill = new ButtonFill(from, to, layer + .2f, UIRenderer.BACKGROUND, ID);
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
			return top.getLinesLength() * ((ID == Renderer.hoveredID) ? 12 : 6);
		}
		
		@Override
		public void fillLines(FloatBuffer buffer) {
			top.fillLines(buffer);
			bottom.fillLines(buffer);
			leftTop.fillLines(buffer);
			leftBottom.fillLines(buffer);
			rightTop.fillLines(buffer);
			rightBottom.fillLines(buffer);
			
			if(ID == Renderer.hoveredID) {
				innerTop.fillLines(buffer);
				innerBottom.fillLines(buffer);
				innerLeftTop.fillLines(buffer);
				innerLeftBottom.fillLines(buffer);
				innerRightTop.fillLines(buffer);
				innerRightBottom.fillLines(buffer);
			}
		}
		
		@Override
		public void fillTriangles(FloatBuffer buffer) {
			fill.fillTriangles(buffer);
		}
	}
	
	final static float BORDER = 0.1f;
	
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
		position.y -= textHeight * finalSize * 1.2f;
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
