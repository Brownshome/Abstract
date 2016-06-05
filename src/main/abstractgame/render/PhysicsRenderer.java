package abstractgame.render;

import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.vecmath.Matrix4f;
import javax.vecmath.Vector2f;
import javax.vecmath.Vector3f;

import org.lwjgl.BufferUtils;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;

import com.bulletphysics.linearmath.DebugDrawModes;
import com.bulletphysics.linearmath.IDebugDraw;

import abstractgame.Client;
import abstractgame.io.user.Console;
import abstractgame.io.user.PerfIO;
import abstractgame.util.Util;

/** This is the debug renderer for the world physics */
public class PhysicsRenderer extends IDebugDraw implements Renderer {
	public static PhysicsRenderer INSTANCE = new PhysicsRenderer();
	public static int PROGRAM;
	public static int VAO;
	public static int VBO;
	static boolean isActive = false;
	
	int size = 0;
	Map<Vector3f, List<Vector3f>> batches = new HashMap<>();
	
	public static void setState(boolean active) {
		isActive = active;
	}
	
	public static boolean isActive() {
		return isActive;
	}

	public static void toggle() {
		setState(!isActive());
	}
	
	@Override
	public void initialize() {
		Client.DEBUG_BINDS.add(PhysicsRenderer::toggle, Keyboard.KEY_F4, PerfIO.BUTTON_PRESSED, "toggle physics display");
		
		int fragment = GLHandler.createShader("physics-fragment", GL20.GL_FRAGMENT_SHADER);
		int vertex = GLHandler.createShader("physics-vertex", GL20.GL_VERTEX_SHADER);
		PROGRAM = GLHandler.createProgram(fragment, vertex);
	
		VBO = GL15.glGenBuffers();
		VAO = GL30.glGenVertexArrays();
		
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, VBO);
		GL30.glBindVertexArray(VAO);
		
		//position
		GL20.glEnableVertexAttribArray(0);
		GL20.glVertexAttribPointer(0, 3, GL11.GL_FLOAT, false, 12, 0);
		
		GLHandler.checkGL();
	}

	@Override
	public void render() {
		if(!isActive)
			return;
		
		TextRenderer.addString("Physics Debug Active", new Vector2f(-1, -1), .05f, UIRenderer.BASE_STRONG, 0);
		
		GL30.glBindVertexArray(VAO);
		GL20.glUseProgram(PROGRAM);
		
		FloatBuffer data = BufferUtils.createFloatBuffer(size * 3);

		for(List<Vector3f> batch : batches.values()) {
			for(Vector3f v : batch) {
				data.put(v.x).put(v.y).put(v.z);
			}
		}
		
		//terrible performance I know, but who cares for a debug renderer
		data.flip();
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, VBO);
		GL15.glBufferData(GL15.GL_ARRAY_BUFFER, data, GL15.GL_STATIC_DRAW);
		
		Matrix4f tmp = new Matrix4f();
		tmp.mul(Camera.projectionMatrix, Camera.viewMatrix);
		GL20.glUniformMatrix4(0, true, Util.toFloatBuffer(tmp));
		
		int count = 0;
		for(Entry<Vector3f, List<Vector3f>> batch : batches.entrySet()) {
			GL20.glUniform3f(1, batch.getKey().x, batch.getKey().y, batch.getKey().z);
			GL11.glDrawArrays(GL11.GL_LINES, count, batch.getValue().size());
			count += batch.getValue().size();
			
			batch.getValue().clear();
		}
		
		size = 0;
	}

	@Override
	public float getPass() {
		return .5f;
	}

	@Override
	public void drawLine(Vector3f from, Vector3f to, Vector3f colour) {
		List<Vector3f> list = batches.computeIfAbsent(new Vector3f(colour), v -> new ArrayList<>());
		list.add(new Vector3f(from));
		list.add(new Vector3f(to));
		size += 2;
	}

	@Override
	public void drawContactPoint(Vector3f PointOnB, Vector3f normalOnB, float distance, int lifeTime, Vector3f color) {
		Vector3f other = new Vector3f(normalOnB);
		
		other.scaleAdd(distance, PointOnB);
		drawLine(PointOnB, other, color);
	}

	@Override
	public void reportErrorWarning(String warningString) {
		Console.warn(warningString, "PHYSICS ENGINE");
	}

	public void addBatches(Map<Vector3f, List<Vector3f>> batches) {
		batches.forEach(this::addBatch);
	}
	
	public void addBatch(Vector3f colour, List<Vector3f> lines) {
		batches.computeIfAbsent(colour, v -> new ArrayList<>()).addAll(lines);
		size += lines.size();
	}
	
	@Override
	public void draw3dText(Vector3f location, String textString) {
		location = new Vector3f(location); //just for safety
		
		Camera.transform(location);
		Vector2f pos = new Vector2f(location.x, location.y);
		
		TextRenderer.addString(textString, pos, 0.5f, UIRenderer.BASE_STRONG, 0);
	}

	@Override
	public void setDebugMode(int debugMode) {}

	@Override
	public int getDebugMode() { 
		return isActive ? 0x7FF : DebugDrawModes.NO_DEBUG; 
	}
}
