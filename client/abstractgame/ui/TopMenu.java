package abstractgame.ui;

import javax.vecmath.Vector2f;

import abstractgame.Game;
import abstractgame.io.user.PerfIO;
import abstractgame.render.IconRenderer;
import abstractgame.render.Renderer;
import abstractgame.render.TextRenderer;
import abstractgame.render.UIRenderer;
import abstractgame.ui.elements.Button;
import abstractgame.ui.elements.CheckBox;
import abstractgame.ui.elements.NamedCheckBox;
import abstractgame.ui.elements.Quad;
import abstractgame.ui.elements.QuadIcon;
import abstractgame.ui.elements.UIElement;

public class TopMenu extends Screen {
	public static TopMenu INSTANCE = new TopMenu();

	static int ID_SETTINGS = UIElement.getNewID();
	static int ID_SERVER = UIElement.getNewID();
	static int ID_CREATE = UIElement.getNewID();
	static int ID_EXIT = UIElement.getNewID();
	static int ID_CREDITS = UIElement.getNewID();

	static Button exitButton = new Button.Weak(new Vector2f(-.6f, -.8f), new Vector2f(-.1f, -.7f), "Exit", Game::close, 0, ID_EXIT);
	static Button creditsButton = new Button.Weak(new Vector2f(.1f, -.8f), new Vector2f(.6f, -.7f), "Show Credits", () -> Screen.setScreen(CreditScreen.INSTANCE), 0, ID_CREDITS);

	static QuadIcon settings = new QuadIcon(UIRenderer.BASE, new Vector2f(-1.5f * Renderer.xCorrectionScalar, -.5f), new Vector2f(-.5f * Renderer.xCorrectionScalar, .5f), "settings", ID_SETTINGS);
	static QuadIcon server = new QuadIcon(UIRenderer.BASE, new Vector2f(-.5f * Renderer.xCorrectionScalar, -.5f), new Vector2f(.5f * Renderer.xCorrectionScalar, .5f), "server", ID_SERVER);
	static QuadIcon create = new QuadIcon(UIRenderer.BASE, new Vector2f(.5f * Renderer.xCorrectionScalar, -.5f), new Vector2f(1.5f * Renderer.xCorrectionScalar, .5f), "new", ID_CREATE);

	static Quad settingsClick = new Quad(new Vector2f(-1.5f * Renderer.xCorrectionScalar, -.5f), new Vector2f(-.5f * Renderer.xCorrectionScalar, .5f), .1f, UIRenderer.BACKGROUND, ID_SETTINGS);
	static Quad serverClick = new Quad(new Vector2f(-.5f * Renderer.xCorrectionScalar, -.5f), new Vector2f(.5f * Renderer.xCorrectionScalar, .5f), .1f, UIRenderer.BACKGROUND, ID_SERVER);
	static Quad createClick = new Quad(new Vector2f(.5f * Renderer.xCorrectionScalar, -.5f), new Vector2f(1.5f * Renderer.xCorrectionScalar, .5f), .1f, UIRenderer.BACKGROUND, ID_CREATE);

	static int clickHandler;
	
	@Override
	public void initialize() {
		UIRenderer.addElement(exitButton);
		UIRenderer.addElement(creditsButton);
		UIRenderer.addElement(settingsClick);
		UIRenderer.addElement(serverClick);
		UIRenderer.addElement(createClick);

		clickHandler = PerfIO.addMouseListener(TopMenu::handleClick, 0, PerfIO.BUTTON_PRESSED);
		PerfIO.holdMouse(false);
	}

	static void handleClick() {
		if(Renderer.hoveredID == ID_SETTINGS) {
			Screen.setScreen(SettingsScreen.INSTANCE);
		} else if(Renderer.hoveredID == ID_SERVER) {
			Screen.setScreen(ServerScreen.INSTANCE);
		} else if(Renderer.hoveredID == ID_CREATE) {
			Screen.setScreen(CreateScreen.INSTANCE);
		}
	}

	@Override
	public void tick() {
		IconRenderer.addIcon(settings);
		IconRenderer.addIcon(server);
		IconRenderer.addIcon(create);

		if(Renderer.hoveredID == ID_SETTINGS) {
			settings.colour = UIRenderer.BASE_STRONG;
			server.colour = UIRenderer.BASE;
			create.colour = UIRenderer.BASE;
		} else if(Renderer.hoveredID == ID_SERVER) {
			settings.colour = UIRenderer.BASE;
			server.colour = UIRenderer.BASE_STRONG;
			create.colour = UIRenderer.BASE;
		} else if(Renderer.hoveredID == ID_CREATE) {
			settings.colour = UIRenderer.BASE;
			server.colour = UIRenderer.BASE;
			create.colour = UIRenderer.BASE_STRONG;
		} else {
			settings.colour = UIRenderer.BASE;
			server.colour = UIRenderer.BASE;
			create.colour = UIRenderer.BASE;
		}

		TextRenderer.addString(Game.NAME, new Vector2f(-TextRenderer.getWidth(Game.NAME) * .25f, .45f), .5f, UIRenderer.BASE_STRONG, 0);
	}

	@Override
	public void destroy() {
		UIRenderer.removeElement(exitButton);
		UIRenderer.removeElement(creditsButton);
		UIRenderer.removeElement(settingsClick);
		UIRenderer.removeElement(serverClick);
		UIRenderer.removeElement(createClick);
		
		PerfIO.removeMouseListener(clickHandler);
	}
}
