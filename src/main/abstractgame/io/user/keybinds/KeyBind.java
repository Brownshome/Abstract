package abstractgame.io.user.keybinds;

import abstractgame.io.user.PerfIO;

/** Represents a single action - event link */
public class KeyBind {
	public Runnable action;
	public int keyFlags = PerfIO.BUTTON_PRESSED;
	public int keyCode;
	
	boolean isBound = false;
	
	public KeyBind(Runnable a, int key) {
		action = a;
		keyCode = key;
	}
	
	public KeyBind(Runnable a, int keyFlag, int key) {
		action = a;
		keyCode = key;
		keyFlags = keyFlag;
	}
	
	/** Refreshes the binding, this gurantees that any changes to current or keyFlags are pushed through */
	public void rebind() {
		deactivate();
		activate();
	}
	
	public boolean isBound() {
		return isBound;
	}
	
	public void deactivate() {
		if(!isBound)
			return;
		
		isBound = false;
		PerfIO.removeKeyListener(this);
	}
	
	public void activate() {
		if(isBound)
			return;
		
		isBound = true;
		PerfIO.addKeyListener(this);
	}
}