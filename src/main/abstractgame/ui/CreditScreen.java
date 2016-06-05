package abstractgame.ui;

import javax.vecmath.Vector2f;

import abstractgame.Client;
import abstractgame.render.TextRenderer;
import abstractgame.render.UIRenderer;
import abstractgame.util.Language;

public class CreditScreen extends Screen {
	public static CreditScreen INSTANCE = new CreditScreen();
	
	@Override
	public void run() {
		TextRenderer.addString(Language.get("credits.title"), new Vector2f(-TextRenderer.getWidth(Language.get("credits.title")) * .25f, .45f), .5f, UIRenderer.BASE_STRONG, 0);
		TextRenderer.addString(Language.get("credits.attrib"), new Vector2f(-TextRenderer.getWidth(Language.get("credits.attrib")) * .075f, 0), .15f, UIRenderer.BASE_STRONG, 0);
	}
}
