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
