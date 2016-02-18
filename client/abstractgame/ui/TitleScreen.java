package abstractgame.ui;

import java.util.function.Supplier;

import javax.vecmath.Color4f;
import javax.vecmath.Vector2f;

import abstractgame.Game;
import abstractgame.render.IconRenderer;
import abstractgame.render.TextRenderer;
import abstractgame.render.UIRenderer;
import abstractgame.ui.elements.ProgressBar;
import abstractgame.ui.elements.QuadIcon;

public class TitleScreen extends Screen {
	public static final TitleScreen INSTANCE = new TitleScreen();
	static final int PAUSE_COUNT = 100;
	static boolean done = false;
	
	ProgressBar bar;
	QuadIcon icon;
	
	public void initialize() {
		bar = new ProgressBar(new Vector2f(-.4f, -.2f), new Vector2f(.8f, .01f), nothingBar());
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
		
		if(done)
			Screen.setScreen(TopMenu.INSTANCE);
	}
	
	Supplier<Float> nothingBar() {
		long start = Game.GAME_CLOCK.getFrame();
		
		return () -> {
			float v = Math.min((Game.GAME_CLOCK.getFrame() - start) / 120f, 1);
			if(v == 1)
				done = true;
			return v;
		};
	}
}
