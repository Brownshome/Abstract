package abstractgame.ui;

import javax.vecmath.Color4f;
import javax.vecmath.Vector2f;

import abstractgame.io.image.ImageIO;
import abstractgame.io.model.ModelLoader;
import abstractgame.render.TextRenderer;
import abstractgame.render.UIRenderer;
import abstractgame.ui.elements.Button;
import abstractgame.ui.elements.ProgressBar;

public class TitleScreen extends Screen {
	public static final TitleScreen INSTANCE = new TitleScreen();
	
	//Button button = new Button(new Vector2f(-.2f, -.45f), new Vector2f(.2f, -.35f), "MEDIOCER BUTTON", 10, null);
	ProgressBar bar;
	
	public void initialize() {
		bar = new ProgressBar(new Vector2f(-.4f, -.2f), new Vector2f(.8f, .01f), ModelLoader.preLoadAll());
		UIRenderer.addElement(bar);
	}
	
	public void destroy() {
		UIRenderer.removeElement(bar);
	}
	
	@Override
	public void tick() {
		super.tick();
		
		TextRenderer.addString("ABSTRACT", new Vector2f(-0.85f, -0.25f), 0.5f, new Color4f(0, 0, 0, 1), 0);
		TextRenderer.addString("Loading...", new Vector2f(-.4f, -.35f), 0.12f, new Color4f(0, 0, 0, 1), 0);
	}
}
