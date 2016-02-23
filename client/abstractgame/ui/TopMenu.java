package abstractgame.ui;

import javax.vecmath.Color4f;
import javax.vecmath.Vector2f;

import abstractgame.Game;
import abstractgame.io.user.KeyIO;
import abstractgame.render.IconRenderer;
import abstractgame.render.Renderer;
import abstractgame.render.UIRenderer;
import abstractgame.ui.elements.Button;
import abstractgame.ui.elements.QuadIcon;

public class TopMenu extends Screen {
	public static final TopMenu INSTANCE = new TopMenu();
	
	QuadIcon icon;
	Button button;
	
	public void initialize() {
		icon = new QuadIcon(new Color4f(0, 0, 0, 1), new Vector2f(-.6f, -.6f), new Vector2f(.6f, .6f), 0, -1);
		button = new Button.Strong(new Vector2f(-.6f, -.8f), new Vector2f(0f, -.65f), "Start Game", 0, 10f, 2);
		button.addOnClick(Game::close);
		UIRenderer.addElement(button);
		KeyIO.holdMouse(false);
	}
	
	public void tick() {
		IconRenderer.addIcon(icon);
	}
	
	public void destory() {
		UIRenderer.removeElement(button);
		button.removeOnClick();
	}
}
