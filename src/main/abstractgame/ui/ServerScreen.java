package abstractgame.ui;

import java.net.InetAddress;
import java.net.UnknownHostException;

import javax.vecmath.Vector2f;

import abstractgame.net.InternalServerProxy;
import abstractgame.net.ServerProxy;
import abstractgame.net.packet.QueryPacket;
import abstractgame.render.TextRenderer;
import abstractgame.render.UIRenderer;
import abstractgame.ui.elements.Button;
import abstractgame.ui.elements.LabeledSingleTextEntry;
import abstractgame.ui.elements.NamedCheckBox;
import abstractgame.ui.elements.SingleLineTextEntry;
import abstractgame.util.ApplicationException;
import abstractgame.util.Language;

public class ServerScreen extends Screen {
	public static final String DEFAULT_CONFIG_FILE = "server";

	public static ServerScreen INSTANCE = new ServerScreen();
	
	static SingleLineTextEntry portJoin = new LabeledSingleTextEntry(new Vector2f(-.9f, .35f), new Vector2f(-.3f, .45f), 0, Language.get("server.port"));
	static SingleLineTextEntry addressJoin = new LabeledSingleTextEntry(new Vector2f(-.9f, .15f), new Vector2f(-.3f, .25f), 0, Language.get("server.address"));
	
	static SingleLineTextEntry portHost = new LabeledSingleTextEntry(new Vector2f(.1f, .35f), new Vector2f(.7f, .45f), 0, Language.get("server.port"));
	static SingleLineTextEntry configFileHost = new LabeledSingleTextEntry(new Vector2f(.1f, .15f), new Vector2f(.7f, .25f), 0, Language.get("server.config"));
	static NamedCheckBox privateButton = new NamedCheckBox(new Vector2f(.1f, -.05f), new Vector2f(.7f, .05f), Language.get("server.open connection"), 0);
	
	static Button joinButton = new Button.Weak(new Vector2f(-.8f, -.8f), new Vector2f(-.2f, -.7f), Language.get("server.join"), ServerScreen::join);
	static Button hostButton = new Button.Weak(new Vector2f(.2f, -.8f), new Vector2f(.8f, -.7f), Language.get("server.host"), ServerScreen::host);
	
	static void join() {
		try {
			if(ServerProxy.getCurrentServerProxy() instanceof InternalServerProxy) {
				ServerProxy.getCurrentServerProxy().getConnection().send(new QueryPacket());
			} else {
				ServerProxy.connectToServer(InetAddress.getByName(addressJoin.getText()), Integer.parseInt(portJoin.getText()));
			}
			
			Screen.setScreen(GameScreen.INSTANCE);
		} catch (UnknownHostException e) {
			throw new ApplicationException("Host not found", "SERVER SCREEN");
		}
	}
	
	static void host() {
		boolean connection = privateButton.getState();
		
		String config = configFileHost.getText();
		if(config.isEmpty()) config = DEFAULT_CONFIG_FILE;
		
		if(connection) {
			ServerProxy.startIntegratedServer(Integer.parseInt(portHost.getText()), config);
			portJoin.setText(portHost.getText());
		} else {
			portJoin.setText("Memory");
			ServerProxy.startPrivateServer(config);
		}
			
		hostButton.disabled = true;
		portHost.disabled = true;
		configFileHost.disabled = true;
		portJoin.disabled = true;
		privateButton.disabled = true;
		addressJoin.disabled = true;
		addressJoin.setText("localhost");
	}
	
	@Override
	public void initialize() {
		UIRenderer.addElement(portJoin);
		UIRenderer.addElement(portHost);
		UIRenderer.addElement(addressJoin);
		UIRenderer.addElement(configFileHost);
		UIRenderer.addElement(joinButton);
		UIRenderer.addElement(hostButton);
		UIRenderer.addElement(privateButton);
	}
	
	@Override
	public void run() {
		TextRenderer.addString(Language.get("server.join title"), new Vector2f(-.9f, .6f), .15f, UIRenderer.BASE_STRONG, -1);
		TextRenderer.addString(Language.get("server.host title"), new Vector2f(.1f, .6f), .15f, UIRenderer.BASE_STRONG, -1);
	}
	
	@Override
	public void destroy() {
		UIRenderer.removeElement(portJoin);
		UIRenderer.removeElement(portHost);
		UIRenderer.removeElement(addressJoin);
		UIRenderer.removeElement(configFileHost);
		UIRenderer.removeElement(joinButton);
		UIRenderer.removeElement(hostButton);
		UIRenderer.removeElement(privateButton);
	}
}
