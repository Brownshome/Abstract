package abstractgame.ui;

import javax.vecmath.Vector2f;

import abstractgame.render.TextRenderer;
import abstractgame.render.UIRenderer;
import abstractgame.ui.elements.Button;
import abstractgame.ui.elements.LabeledSingleTextEntry;
import abstractgame.ui.elements.Line;
import abstractgame.ui.elements.SingleLineTextEntry;
import abstractgame.ui.elements.UIElement;

public class ServerScreen extends Screen {
	public static ServerScreen INSTANCE = new ServerScreen();
	
	static SingleLineTextEntry portJoin = new LabeledSingleTextEntry(new Vector2f(-.9f, .35f), new Vector2f(-.3f, .45f), 0, "Port:    ");
	static SingleLineTextEntry addressJoin = new LabeledSingleTextEntry(new Vector2f(-.9f, .15f), new Vector2f(-.3f, .25f), 0, "Address: ");
	
	static SingleLineTextEntry portHost = new LabeledSingleTextEntry(new Vector2f(.1f, .35f), new Vector2f(.7f, .45f), 0, "Port:    ");
	static SingleLineTextEntry nameHost = new LabeledSingleTextEntry(new Vector2f(.1f, .15f), new Vector2f(.7f, .25f), 0, "Name:    ");
	static SingleLineTextEntry configFileHost = new LabeledSingleTextEntry(new Vector2f(.1f, -.05f), new Vector2f(.7f, .05f), 0, "Config:  ");
	
	static Button joinButton = new Button.Weak(new Vector2f(-.8f, -.8f), new Vector2f(-.2f, -.7f), "Join", ServerScreen::join);
	static Button hostButton = new Button.Weak(new Vector2f(.2f, -.8f), new Vector2f(.8f, -.7f), "Host", ServerScreen::host);
	
	static void join() {
		assert false : "not implemented";
	}
	
	static void host() {
		assert false : "not implemented";
	}
	
	@Override
	public void initialize() {
		UIRenderer.addElement(portJoin);
		UIRenderer.addElement(portHost);
		UIRenderer.addElement(addressJoin);
		UIRenderer.addElement(configFileHost);
		UIRenderer.addElement(nameHost);
		UIRenderer.addElement(joinButton);
		UIRenderer.addElement(hostButton);
	}
	
	@Override
	public void tick() {
		TextRenderer.addString("Join Server", new Vector2f(-.9f, .6f), .15f, UIRenderer.BASE_STRONG, -1);
		TextRenderer.addString("Host Server", new Vector2f(.1f, .6f), .15f, UIRenderer.BASE_STRONG, -1);
	}
	
	@Override
	public void destroy() {
		UIRenderer.removeElement(portJoin);
		UIRenderer.removeElement(portHost);
		UIRenderer.removeElement(addressJoin);
		UIRenderer.removeElement(configFileHost);
		UIRenderer.removeElement(nameHost);
		UIRenderer.removeElement(joinButton);
		UIRenderer.removeElement(hostButton);
	}
}
