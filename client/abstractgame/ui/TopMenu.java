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
	Button strongButton;
	Button weakButton;
	
	@Override
	public void initialize() {
		icon = new QuadIcon(new Color4f(0, 0, 0, 1), new Vector2f(-.6f, -.6f), new Vector2f(.6f, .6f), 0, -1);
		
		strongButton = new Button.Strong(new Vector2f(-.6f, -.8f), new Vector2f(-.1f, -.65f), "YES", 0, 10f, 2);
		strongButton.addOnClick(Game::close);
		weakButton = new Button.Weak(new Vector2f(.1f, -.8f), new Vector2f(.6f, -.65f), "NO", 0, 10f, 3);
		weakButton.addOnClick(() -> Screen.setScreen(null));
		
		UIRenderer.addElement(strongButton);
		UIRenderer.addElement(weakButton);
		
		KeyIO.holdMouse(false);
	}
	
	@Override
	public void tick() {
		IconRenderer.addIcon(icon);
	}
	
	@Override
	public void destroy() {
		UIRenderer.removeElement(strongButton);
		UIRenderer.removeElement(weakButton);
		strongButton.removeOnClick();
	}
}
