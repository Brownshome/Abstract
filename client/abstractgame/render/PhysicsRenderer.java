package abstractgame.render;

import javax.vecmath.Vector3f;

import com.bulletphysics.linearmath.IDebugDraw;

/** This is the debug renderer for the world physics */
public class PhysicsRenderer extends IDebugDraw implements Renderer {
	public static PhysicsRenderer INSTANCE = new PhysicsRenderer();
	
	@Override
	public void initialize() {
		// TODO Auto-generated method stub

	}

	@Override
	public void render() {
		// TODO Auto-generated method stub

	}

	@Override
	public float getPass() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void drawLine(Vector3f from, Vector3f to, Vector3f color) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void drawContactPoint(Vector3f PointOnB, Vector3f normalOnB,
			float distance, int lifeTime, Vector3f color) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void reportErrorWarning(String warningString) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void draw3dText(Vector3f location, String textString) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setDebugMode(int debugMode) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public int getDebugMode() {
		// TODO Auto-generated method stub
		return 0;
	}

}
