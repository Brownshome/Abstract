package abstractgame.io.user;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;

import org.lwjgl.input.Keyboard;

import abstractgame.util.ApplicationException;

public class TypingRequest {
	/** A request to be used when avoiding null pointer exceptions */
	public static final TypingRequest DUMMY_REQUEST = new TypingRequest(0, null, false);

	static final int FRAMES_OF_GRACE = 20;
	static final int POST_GRACE_REFRESH_RATE = 1;

	int ignoreCount = 0;
	Map<Integer, Supplier<String>> specialActions = new HashMap<>();

	static {
		DUMMY_REQUEST.done = true;
	}

	String text = "";
	final boolean blockOtherKeyEvents;
	boolean done = false;
	final int terminator; //the key that closes the text box

	int position = 0;
	int selection = -1; //-1 means the nothing is selected

	Consumer<TypingRequest> l;

	/** l can be null, if that is the case no event will be fired upon termination */
	TypingRequest(int terminator, Consumer<TypingRequest> l, boolean blockInput) {
		this.terminator = terminator;
		this.l = l;
		this.blockOtherKeyEvents = blockInput;
	}

	public void ignore(int n) {
		if(n < 0) throw new ApplicationException("Cannot ignore negative numbers: " + n, "TEXT IO");
		ignoreCount += n;
	}

	public int getPosition() {
		return position;
	}

	/** Selection is -1 when there is no selection and goes to text.length() */
	public int getSelectionIndex() {
		return selection;
	}

	public String getText() {
		return text;
	}

	void press(int key, char character) {
		if(ignoreCount != 0) {
			ignoreCount--;
			return;
		}

		if(key == terminator) {
			if(l != null) l.accept(this);
			KeyIO.request = null;
			done = true;
			return;
		}

		switch(key) {
		case Keyboard.KEY_BACK:
			if(text.length() == 0)
				return;

			if(selection == -1) {
				if(position == 0)
					return;
				
				text = text.substring(0, position - 1) + text.substring(position);
				position--;
			} else {
				int start, end;
				if(position > selection) {
					start = selection;
					end = position;
				} else {
					start = position;
					end = selection;
				}

				text = text.substring(0, start) + text.substring(end);
				position = start;
				selection = -1;
			}

				return;
			case Keyboard.KEY_DELETE:
				if(text.length() == 0)
					return;
				
				if(selection == -1) {
					if(position == text.length()) {
						text = text.substring(0, position - 1);
						position--;
					} else 
						text = text.substring(0, position) + text.substring(position + 1);
				} else {
					int start, end;
					if(position > selection) {
						start = selection;
						end = position;
					} else {
						start = position;
						end = selection;
					}

					text = text.substring(0, start) + text.substring(end);
					position = start;
					selection = -1;
				}

				return;
			case Keyboard.KEY_V:
				if(!KeyIO.isCtrlDown()) break;

				String clipText = ClipboardWrapper.getClipboardContents();
				if(selection != -1) {
					int start, end;
					if(position > selection) {
						start = selection;
						end = position;
					} else {
						start = position;
						end = selection;
					}
					
					text = text.substring(0, start) + clipText + text.substring(end);
					position = start + clipText.length();
					selection = -1;
				} else {
					text = text.substring(0, position) + clipText + text.substring(position);
					position += clipText.length();
				}
				return;
			case Keyboard.KEY_C:
				if(!KeyIO.isCtrlDown() || selection == -1) break;

				int start, end;
				if(position > selection) {
					start = selection;
					end = position;
				} else {
					start = position;
					end = selection;
				}
				
				ClipboardWrapper.setClipboardContents(text.substring(start, end));
				return;
			case Keyboard.KEY_X:
				if(!KeyIO.isCtrlDown() || selection == -1) break;

				if(position > selection) {
					start = selection;
					end = position;
				} else {
					start = position;
					end = selection;
				}
				
				ClipboardWrapper.setClipboardContents(text.substring(start, end));
				text = text.substring(0, start) + text.substring(end);
				
				position = selection;
				selection = -1;
				return;
			case Keyboard.KEY_A:
				if(!KeyIO.isCtrlDown()) break;

				selection = 0;
				position = text.length();
				return;
			case Keyboard.KEY_LEFT:
				if(KeyIO.isShiftDown()) {
					if(selection == -1 && position != 0) 
						selection = position;
					
					if(position > 0) 
						position--;
				} else {
					if(selection != -1) {
						if(selection < position) {
							position = selection;
						}
						
						selection = -1;
					} else if(position != 0) 
						position--;
				}

				return;

			case Keyboard.KEY_RIGHT:
				if(KeyIO.isShiftDown()) {
					if(selection == -1 && position != text.length()) 
						selection = position;
					
					if(position != text.length()) 
						position++;
				} else {
					if(selection != -1) {
						if(selection > position) {
							position = selection;
						}
						
						selection = -1;
					} else if(position != text.length()) 
						position++;
				}

				return;
		}

		if(character >= 32)
			if(selection == -1) {
				text = text.substring(0, position) + character + text.substring(position);
				position++;
			} else {
				text = text.substring(0, selection) + character + text.substring(position);
				position = selection + 1;
				selection = -1;
			}
	}

	public boolean isDone() {
		return done;
	}

	public void setText(String string) {
		text = string;
		if(position > text.length()) position = text.length();
		if(selection > text.length()) selection = text.length();
	}

	public void addSpecialKey(int key, Supplier<String> task) {
		specialActions.put(key, task);
	}

	public void setPosition(int index) {
		position = index;
		selection = -1;
	}

	public void setSelectionIndex(int index) {
		selection = index;
	}
}
