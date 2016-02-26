package abstractgame.ui;

import javax.vecmath.Vector2f;

import abstractgame.render.UIRenderer;
import abstractgame.ui.elements.SingleLineTextEntry;

public class ServerScreen extends Screen {
	public static ServerScreen INSTANCE = new ServerScreen();
	
	SingleLineTextEntry portTextEntry = new SingleLineTextEntry(new Vector2f(-.9f, .5f), new Vector2f(-.4f, .6f), 0, true);
	
	@Override
	public void initialize() {
		UIRenderer.addElement(portTextEntry);
	}
	
	@Override
	public void destroy() {
		UIRenderer.removeElement(portTextEntry);
	}
}
