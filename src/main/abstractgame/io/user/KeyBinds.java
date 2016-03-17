package abstractgame.io.user;

import java.util.HashMap;
import java.util.Map;

import org.lwjgl.input.Keyboard;

import abstractgame.io.config.ConfigFile;

/** Keeps track of keybinds and other such things */
public class KeyBinds {
	static class Action {
		Runnable action;
		int keyFlags = PerfIO.BUTTON_PRESSED;
		int current;
		int id;
		
		Action(Runnable a, int key) {
			action = a;
			current = key;
			id = PerfIO.addKeyListener(action, current, keyFlags);
		}
		
		Action(Runnable a, int keyFlag, int key) {
			action = a;
			current = key;
			keyFlags = keyFlag;
			id = PerfIO.addKeyListener(action, current, keyFlags);
		}
		
		public void rebind() {
			PerfIO.removeKeyListener(id);
			PerfIO.addKeyListener(action, current, keyFlags);
		}
	}
	
	static Map<String, Action> stringAssoc = new HashMap<>();
	static final ConfigFile KEYBINDS_FILE = ConfigFile.getFile("keybinds");
	
	public static void rebind(String name, int key) {
		Action a = stringAssoc.get(name);
		a.current = key;
		a.rebind();
	}
	
	public static void rebind(String name, int key, int flags) {
		Action a = stringAssoc.get(name);
		a.keyFlags = flags;
		a.current = key;
		a.rebind();
	}
	
	public static void add(Runnable r, int code, int flags, String name) {
		stringAssoc.put(name, new Action(r, flags, Keyboard.getKeyIndex(KEYBINDS_FILE.getProperty(name, Keyboard.getKeyName(code)))));
	}
}
