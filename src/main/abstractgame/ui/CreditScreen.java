package abstractgame.ui;

import javax.vecmath.Vector2f;

import abstractgame.Client;
import abstractgame.render.TextRenderer;
import abstractgame.render.UIRenderer;

public class CreditScreen extends Screen {
	public static CreditScreen INSTANCE = new CreditScreen();
	
	@Override
	public void tick() {
		TextRenderer.addString("CREDITS", new Vector2f(-TextRenderer.getWidth("CREDITS") * .25f, .45f), .5f, UIRenderer.BASE_STRONG, 0);
		TextRenderer.addString("James Brown - Everything", new Vector2f(-TextRenderer.getWidth("James Brown - Everything") * .075f, 0), .15f, UIRenderer.BASE_STRONG, 0);
	}
}
