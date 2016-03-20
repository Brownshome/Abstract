package abstractgame.ui.elements;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;

import javax.vecmath.Color4f;
import javax.vecmath.Vector2f;

import org.lwjgl.input.Keyboard;

import abstractgame.Client;
import abstractgame.io.user.PerfIO;
import abstractgame.io.user.TypingRequest;
import abstractgame.render.GLHandler;
import abstractgame.render.Renderer;
import abstractgame.render.TextRenderer;
import abstractgame.render.UIRenderer;

public class SingleLineTextEntry extends UIElement {
	static int BLINK_DELAY = 30;
	
	public boolean disabled = false;
	
	Line base;
	Line leftBottom;
	Line leftTop;
	
	Line cursor;
	
	HexFill fill;
	
	Vector2f textStart;
	float textSize;
	
	final Color4f colour = new Color4f(UIRenderer.BASE);
	
	TypingRequest request = new TypingRequest(Keyboard.KEY_RETURN, null, true);
	
	{
		request.finish();
	}
	
	int ID = getNewID();
	
	public SingleLineTextEntry(Vector2f from, Vector2f to, float layer) {
		float taperDist = (to.y - from.y) * TAPER_MULT;
		
		textSize = (to.y - from.y) * .8f;
		textStart = new Vector2f(from.x + taperDist, from.y);
		
		base = new Line(new Vector2f(from.x + taperDist, from.y), new Vector2f(to.x, from.y), layer, colour, ID);
		leftBottom = new Line(new Vector2f(from.x, (from.y + to.y) * .5f), new Vector2f(from.x + taperDist, from.y), layer, colour, ID);
		leftTop = new Line(new Vector2f(from.x, (from.y + to.y) * .5f), new Vector2f(from.x + taperDist, to.y), layer, colour, ID);
		cursor = new Line(new Vector2f(-1, from.y + UIElement.INSET_DIST_Y), new Vector2f(-1, to.y - UIElement.INSET_DIST_Y), layer, UIRenderer.BASE, ID);
		
		fill = new HexFill(from, to, taperDist, UIRenderer.BACKGROUND, ID);
	}
	
	int pressHandler;
	int realeaseHandler;
	@Override
	public void onAdd() {
		pressHandler = PerfIO.addMouseListener(this::onClick, 0, PerfIO.BUTTON_PRESSED);
		realeaseHandler = PerfIO.addMouseListener(this::onRelease, 0, PerfIO.BUTTON_RELEASED);
	}
	
	@Override
	public void onRemove() {
		PerfIO.removeMouseListener(pressHandler);
	}
	
	
	boolean isMouseDown = false;
	void onRelease(Vector2f position) {
		if(isMouseDown) {
			isMouseDown = false;
		}
	}
	
	void onClick(Vector2f position) {
		if(GLHandler.hoveredID != ID || disabled)
			return;
		
		isMouseDown = true;
		
		request.setIsDone(false);
		PerfIO.setRequest(request);
		
		//set the position based on the click location
		float dist = (position.x - textStart.x) / textSize;
		float acc = 0;
		
		int index = 0;
		for(int c = 0; c < request.getText().length(); c++) {
			acc += TextRenderer.getWidth(request.getText().charAt(c));
			
			if(acc > dist) {
				index = c;
				break;
			} else {
				if(c == request.getText().length() - 1)
					index = request.getText().length();
			}
		}
		
		request.setPosition(index);
	}
	
	@Override
	public int getLinesLength() {
		return base.getLinesLength() * (isCursorVisable() ? 4 : 3);
	}
	
	boolean isCursorVisable() {
		return !request.isDone() && Client.GAME_CLOCK.getTickNo() % BLINK_DELAY > BLINK_DELAY / 2;
	}
	
	@Override
	public void tick() {
		colour.set(!disabled && (GLHandler.hoveredID == ID || !request.isDone()) ? UIRenderer.HIGHLIGHT_STRONG : UIRenderer.BASE);
		
		String text = request.getText();
		
		float cursorX = textStart.x + TextRenderer.getWidth(request.getText().substring(0, request.getPosition())) * textSize + .01f;
		cursor.from.x = cursorX;
		cursor.to.x = cursorX;
		
		if(isMouseDown) {
			//set the position based on the click location
			float dist = (PerfIO.getPos().x - textStart.x) / textSize;
			float acc = 0;
			
			int index = 0;
			for(int c = 0; c < request.getText().length(); c++) {
				acc += TextRenderer.getWidth(request.getText().charAt(c));
				
				if(acc > dist) {
					index = c;
					break;
				} else {
					if(c == request.getText().length() - 1)
						index = request.getText().length();
				}
			}
			
			request.setSelectionIndex(index);
		}
		
		if(!request.isDone() && request.getSelectionIndex() != -1) {
			ByteBuffer buffer = TextRenderer.encode(text, textStart, textSize, request.isDone() || disabled ? UIRenderer.BASE : UIRenderer.BASE_STRONG, 0, ID);
			
			int start, end;
			if(request.getPosition() > request.getSelectionIndex()) {
				start = request.getSelectionIndex();
				end = request.getPosition();
			} else {
				start = request.getPosition();
				end = request.getSelectionIndex();
			}
			
			int bufferIndex = 0;
			for(int position = 0; position < end; position++) {
				if(Character.isWhitespace(text.charAt(position)))
					continue;
				
				if(position >= start) {
					buffer.position(bufferIndex * TextRenderer.BYTES_PER_LETTER + 10);
					buffer.putFloat(UIRenderer.HIGHLIGHT_STRONG.x).putFloat(UIRenderer.HIGHLIGHT_STRONG.y).putFloat(UIRenderer.HIGHLIGHT_STRONG.z).putFloat(UIRenderer.HIGHLIGHT_STRONG.w);
				}
				
				bufferIndex++;
			}
			
			buffer.position(0); //prepare for reading
			
			TextRenderer.addText(buffer);
		} else {
			TextRenderer.addString(text, textStart, textSize, request.isDone() ? UIRenderer.BASE : UIRenderer.BASE_STRONG, 0, ID);
		}
	}
	
	@Override
	public void fillLines(FloatBuffer buffer) {
		base.fillLines(buffer);
		leftBottom.fillLines(buffer);
		leftTop.fillLines(buffer);
		
		if(isCursorVisable())
			cursor.fillLines(buffer);
	}
	
	@Override
	public int getTrianglesLength() {
		return fill.getTrianglesLength();
	}
	
	@Override
	public void fillTriangles(FloatBuffer buffer) {
		fill.fillTriangles(buffer);
	}
	
	@Override
	public void setID(int ID) {
		base.setID(ID);
		leftBottom.setID(ID);
		leftTop.setID(ID);
		cursor.setID(ID);
	}

	public String getText() {
		return request.getText();
	}

	public void setText(String text) {
		request.setText(text);
	}
}
