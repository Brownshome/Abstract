package abstractgame.io.user.keybinds;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.lwjgl.input.Keyboard;

import abstractgame.io.config.ConfigFile;

public class BindGroup {
	static final ConfigFile KEYBINDS_FILE = ConfigFile.getFile("keybinds");
	
	String name;
	List<KeyBind> binds = new ArrayList<>();
	boolean isActive = true;
	
	public BindGroup(String name) {
		this.name = name;
	}
	
	public void deactivate() {
		isActive = false;
		
		for(KeyBind bind : binds)
			bind.deactivate();
	}
	
	public void activate() {
		isActive = true;
		
		for(KeyBind bind : binds)
			bind.activate();
	}
	
	/** A true return indicated that new items added will be automatically added to the keylistener */
	public boolean isActive() {
		return isActive;
	}
	
	public KeyBind add(Runnable r, int code, int flags, String name) {
		KeyBind k;
		binds.add(k = new KeyBind(r, flags, Keyboard.getKeyIndex(KEYBINDS_FILE.getProperty(this.name + "." + name, Keyboard.getKeyName(code)))));
		if(isActive)
			k.activate();
		
		return k;
	}
}
