package abstractgame.util;

import abstractgame.Client;
import abstractgame.io.config.ConfigFile;
import abstractgame.io.config.ConfigFile.Policy;

public class Language {
	public final static String LANG_FOLDER = "lang/";
	public static Language LANG = new Language(Client.GLOBAL_CONFIG.getProperty("language", "ENG_UK"));
	
	final ConfigFile languageFile;
	
	protected Language(String language) {
		languageFile = ConfigFile.getFile(LANG_FOLDER + language);
	}
	
	protected String getLocal(String name) {
		return languageFile.getProperty(name, name);
	}
	
	public static String get(String name) {
		return LANG.getLocal(name);
	}
}
