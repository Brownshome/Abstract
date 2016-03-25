package abstractgame.render;

import java.util.List;
import java.util.Map.Entry;

import javax.vecmath.Vector2f;
import javax.vecmath.Vector3f;

import com.bulletphysics.linearmath.IDebugDraw;

/** This delegates internally to the {@link PhysicsRenderer} */
public class ServerPhysicsRenderer extends PhysicsRenderer {
	public static ServerPhysicsRenderer INSTANCE = new ServerPhysicsRenderer();
	
	@Override
	public void initialize() {}
	
	public void reset() {
		batches.forEach((c, l) -> l.clear());
	}
	
	@Override
	public float getPass() {
		return 0.4f;
	}
	
	@Override
	public synchronized void drawLine(Vector3f from, Vector3f to, Vector3f colour) {
		Vector3f neg = new Vector3f(colour);
		neg.x = 1 - neg.x;
		neg.y = 1 - neg.y;
		neg.z = 1 - neg.z;
		super.drawLine(from, to, neg);
	}
	
	@Override
	public synchronized void render() {
		PhysicsRenderer.INSTANCE.addBatches(batches);
	}
	
	@Override
	public void draw3dText(Vector3f location, String textString) {
		//TODO
	}
}
