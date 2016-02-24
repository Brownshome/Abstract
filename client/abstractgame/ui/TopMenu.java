package abstractgame.ui;

import javax.vecmath.Color4f;
import javax.vecmath.Vector2f;

import abstractgame.Game;
import abstractgame.io.user.KeyIO;
import abstractgame.render.IconRenderer;
import abstractgame.render.UIRenderer;
import abstractgame.ui.elements.Button;
import abstractgame.ui.elements.CheckBox;
import abstractgame.ui.elements.QuadIcon;

public class TopMenu extends Screen {
	public static final TopMenu INSTANCE = new TopMenu();

	Button strongButton;
	Button weakButton;
	CheckBox checkBox;
	
	@Override
	public void initialize() {
		strongButton = new Button.Strong(new Vector2f(-.6f, -.8f), new Vector2f(-.1f, -.7f), "Y", 0, 10f, 2);
		strongButton.addOnClick(Game::close);
		weakButton = new Button.Weak(new Vector2f(.1f, -.8f), new Vector2f(.6f, -.7f), "N", 0, 10f, 3);
		weakButton.addOnClick(() -> Screen.setScreen(null));
		checkBox = new CheckBox(new Vector2f(0, 0), .1f, 0, 4);
		checkBox.enable();
		
		UIRenderer.addElement(strongButton);
		UIRenderer.addElement(weakButton);
		UIRenderer.addElement(checkBox);
		
		KeyIO.holdMouse(false);
	}
	
	@Override
	public void destroy() {
		UIRenderer.removeElement(strongButton);
		UIRenderer.removeElement(weakButton);
		UIRenderer.addElement(checkBox);
		
		strongButton.removeOnClick();
	}
}
