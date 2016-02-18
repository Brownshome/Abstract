package abstractgame.ui;

import javax.vecmath.Color4f;
import javax.vecmath.Vector2f;

import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;

import abstractgame.io.user.KeyIO;
import abstractgame.render.IconRenderer;
import abstractgame.render.TextRenderer;
import abstractgame.ui.elements.QuadIcon;

public class TopMenu extends Screen {
	public static final TopMenu INSTANCE = new TopMenu();
	
	QuadIcon icon;
	
	public void initialize() {
		icon = new QuadIcon(new Color4f(0, 0, 0, 1), new Vector2f(-.6f, -.6f), new Vector2f(.6f, .6f), 0);
		KeyIO.holdMouse(false);
	}
	
	public void tick() {
		float x = Mouse.getX() * 1f / Display.getWidth();
		float y = Mouse.getY() * 1f / Display.getHeight();
		
		if(Math.abs(x - .5f) < .2f && Math.abs(y - .5f) < .2f) {
			icon.colour.set(.8f, .6f, .6f, 1);
		} else {
			icon.colour.set(0, 0, 0, 1);
		}
		
		IconRenderer.addIcon(icon);
		//TextRenderer.addString(x + " : " + y, new Vector2f(-.3f, -.3f), .1f, new Color4f(0, 0, 0, 1), 0);
	}
}
