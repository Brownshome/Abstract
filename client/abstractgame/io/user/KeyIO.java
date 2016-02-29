package abstractgame.io.user;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Consumer;

import javax.vecmath.Vector2f;

import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;

import abstractgame.util.ApplicationException;

public class KeyIO {
	private static class KeyListener {
		int code;
		int flags;
		Runnable action;

		public KeyListener(Runnable action, int code, int flags) {
			this.action = action;
			this.code = code;
			this.flags = flags;
		}
	}

	private static class MouseListener {
		int code;
		int flags;
		Consumer<Vector2f> action;

		public MouseListener(Consumer<Vector2f> action, int code, int flags) {
			this.action = action;
			this.code = code;
			this.flags = flags;
		}
	}

	public static float dx;
	public static float dy;
	public static float dz;

	public static boolean holdMouse = true;

	static boolean isPolling = false;
	static List<Runnable> afterPoll = new ArrayList<>();

	/** Only works for KEY_PRESSED and KEY_RELEASED */
	public static final int ALL_KEYS = Integer.MIN_VALUE;

	public static final int BUTTON_DOWN = 2;
	public static final int BUTTON_PRESSED = 4;
	public static final int BUTTON_RELEASED = 8;
	public static final int BUTTON_UP = 1;

	private static Map<Integer, MouseListener> mouseClickListeners = new HashMap<>();
	private static Map<Integer, KeyListener> keyListeners = new HashMap<>();

	/** Used in typing requests */
	private static Map<Integer, int[]> down = new HashMap<>();

	static TypingRequest request; //will be null when there is no active request

	static boolean ctrl = false;
	static boolean shift = false;

	public static void holdMouse(boolean hold) {
		if(holdMouse != hold) {
			holdMouse = hold;
			Mouse.setGrabbed(hold);
		}
	}

	public static TypingRequest getText(Consumer<TypingRequest> l) {
		return getText(l, Keyboard.KEY_RETURN, true);
	}

	public static TypingRequest getText() {
		return getText(null, Keyboard.KEY_RETURN, true);
	}

	public static TypingRequest getText(boolean block) {
		return getText(null, Keyboard.KEY_RETURN, block);
	}

	public static TypingRequest getText(int terminator) {
		return getText(null, terminator, true);
	}

	public static TypingRequest getText(int terminator, boolean block) {
		return getText(null, terminator, block);
	}

	public static TypingRequest getText(Consumer<TypingRequest> l, int terminator, boolean block) {
		return request = new TypingRequest(terminator, l, block);
	}

	static int id = Integer.MIN_VALUE;

	public static void removeMouseListener(int id) {
		if(isPolling) {
			afterPoll.add(() -> removeMouseListener(id));
			return;
		}

		mouseClickListeners.remove(id);
	}

	public static void removeKeyListener(int id) {
		if(isPolling) {
			afterPoll.add(() -> removeKeyListener(id));
			return;
		}

		keyListeners.remove(id);
	}

	public static int addMouseListener(Runnable action, int button, int flags) {
		return addMouseListener(e -> action.run(), button, flags);
	}

	/** returns an index to be used to remove it, The Vector2f is the position of the mouse in the window */
	public static int addMouseListener(Consumer<Vector2f> action, int button, int flags) {
		if(isPolling) {
			afterPoll.add(() -> addMouseListener(action, button, flags));
			return id;
		}

		if ((flags & (BUTTON_UP | BUTTON_DOWN | BUTTON_RELEASED | BUTTON_PRESSED)) == 0)
			throw new ApplicationException("Invalid Flag", "KEYBINDS");

		mouseClickListeners.put(id, new MouseListener(action, button, flags & (BUTTON_UP | BUTTON_DOWN | BUTTON_RELEASED | BUTTON_PRESSED)));

		return id++;
	}

	public static int addKeyListener(Runnable action, int code, int flags) {
		if(isPolling) {
			afterPoll.add(() -> addKeyListener(action, code, flags));
			return id;
		}

		if ((flags & (BUTTON_UP | BUTTON_DOWN | BUTTON_RELEASED | BUTTON_PRESSED)) == 0)
			throw new ApplicationException("Invalid Flag", "KEYBINDS");

		keyListeners.put(id, new KeyListener(action, code, flags & (BUTTON_UP | BUTTON_DOWN | BUTTON_RELEASED | BUTTON_PRESSED)));

		return id++;
	}


	static Vector2f tmp = new Vector2f();
	/** NB if keeping the output of this function please clone it */
	public static Vector2f getPos(int x, int y) {
		tmp.set(x * 2 / (float) Display.getWidth() - 1, y * 2 / (float) Display.getHeight() - 1);
		return tmp;
	}
	
	public static Vector2f getPos() {
		return getPos(Mouse.getX(), Mouse.getY());
	}

	public static void tick() {
		isPolling = true;

		dx = Mouse.getDX();
		dy = Mouse.getDY();
		dz = Mouse.getDWheel();

		for (MouseListener l : mouseClickListeners.values()) {
			if ((l.flags & BUTTON_DOWN) != 0
					&& Mouse.isButtonDown(l.code))
				l.action.accept(getPos(Mouse.getX(), Mouse.getY()));

			if ((l.flags & BUTTON_UP) != 0 && !Mouse.isButtonDown(l.code))
				l.action.accept(getPos(Mouse.getX(), Mouse.getY()));
		}

		for (KeyListener l : keyListeners.values()) {
			if ((l.flags & BUTTON_DOWN) != 0 && Keyboard.isKeyDown(l.code))
				l.action.run();

			if ((l.flags & BUTTON_UP) != 0 && !Keyboard.isKeyDown(l.code))
				l.action.run();
		}

		shift = Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT);
		ctrl = Keyboard.isKeyDown(Keyboard.KEY_RCONTROL) || Keyboard.isKeyDown(Keyboard.KEY_LCONTROL);

		// start poll for press / release
		while (Keyboard.next()) {
			int key = Keyboard.getEventKey();
			char c = Keyboard.getEventCharacter();

			if(Keyboard.getEventKeyState()) {
				down.put(key, new int[] {0, c});
			} else
				down.remove(key);

			if(request != null && request.blockOtherKeyEvents) 
				continue;

			for (KeyListener l : keyListeners.values()) {
				if (key != l.code && l.code != ALL_KEYS) 
					continue;

				if ((l.flags & BUTTON_PRESSED) != 0 && Keyboard.getEventKeyState()) {
					l.action.run();
					continue;
				}

				if ((l.flags & BUTTON_RELEASED) != 0 && !Keyboard.getEventKeyState()) {
					l.action.run();
					continue;
				}
			}
		}

		if(request != null)
			for(Entry<Integer, int[]> e : down.entrySet()) {

				int f = e.getValue()[0]; //not micro-optimization, just making the line not so ruddy long

				if(f == 0 || f > TypingRequest.FRAMES_OF_GRACE && f % TypingRequest.POST_GRACE_REFRESH_RATE == 0)
					request.press(e.getKey(), (char) e.getValue()[1]);

				e.getValue()[0]++;
			}

		while (Mouse.next()) {
			int button = Mouse.getEventButton();

			if (button == -1) 
				continue;

			for (MouseListener l : mouseClickListeners.values()) {
				if (button != l.code) 
					continue;

				if ((l.flags & BUTTON_PRESSED) != 0
						&& Mouse.getEventButtonState()) {
					l.action.accept(getPos(Mouse.getEventX(), Mouse.getEventY()));
					continue;
				}

				if ((l.flags & BUTTON_RELEASED) != 0
						&& !Mouse.getEventButtonState()) {
					l.action.accept(getPos(Mouse.getEventX(), Mouse.getEventY()));
					continue;
				}
			}
		}
		isPolling = false;

		afterPoll.forEach(Runnable::run);
		afterPoll.clear();
	}

	public static boolean isCtrlDown() {
		return ctrl;
	}

	public static boolean isShiftDown() {
		return shift;
	}
}
