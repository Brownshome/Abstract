package abstractgame.render;

import javax.vecmath.Matrix3f;
import javax.vecmath.Matrix4f;
import javax.vecmath.Quat4f;
import javax.vecmath.Vector3f;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;

import abstractgame.Client;
import abstractgame.io.model.Model;
import abstractgame.util.Util;
import abstractgame.world.entity.BasicEntity;
import abstractgame.world.entity.Entity;

import com.bulletphysics.linearmath.QuaternionUtil;

/** The renderEntity holds a cashe each frame to speed propergation */
public class RenderEntity extends BasicEntity {
	/** If this is null it means that the RenderEntity is in world coordinates */
	Entity parent;
	Model model;
	
	long lastFrame = -1;
	Quat4f orientationCashe = new Quat4f();
	Vector3f positionCashe = new Vector3f();
	
	public RenderEntity(Model model, Entity parent, Vector3f offset, Quat4f orientation) {
		super(offset, orientation);
		
		this.model = model;
		this.parent = parent;
		model.buildOpenGLBuffers();
	}
	
	public RenderEntity(Model model, Vector3f offset, Quat4f orientation) {
		super(offset, orientation);
		
		this.model = model;
		model.buildOpenGLBuffers();
	}
	
	void render() {
		GL30.glBindVertexArray(model.getVAO());
		
		Matrix3f modelRot = new Matrix3f();
		modelRot.set(getOrientation());
		GL20.glUniformMatrix3(1, true, Util.toFloatBuffer(modelRot));
		
		Matrix4f mvp = new Matrix4f();
		mvp.set(modelRot, getPosition(), 1);
		mvp.mul(Camera.viewMatrix, mvp);
		mvp.mul(Camera.projectionMatrix, mvp);
		GL20.glUniformMatrix4(0, true, Util.toFloatBuffer(mvp));
		
		GL11.glDrawElements(GL11.GL_TRIANGLES, model.getLength(), GL11.GL_UNSIGNED_INT, 0);
	}
	
	void updateCashe() {
		orientationCashe.mul(parent.getOrientation(), super.getOrientation());
		QuaternionUtil.quatRotate(parent.getOrientation(), super.getPosition(), positionCashe);
		positionCashe.add(parent.getPosition());
		
		lastFrame = Client.GAME_CLOCK.getFrame();
	}
	
	@Override
	public Quat4f getOrientation() {
		if(parent == null)
			return super.getOrientation();
		
		if(Client.GAME_CLOCK.getFrame() != lastFrame)
			updateCashe();
			
		return orientationCashe;
	}
	
	@Override
	public Vector3f getPosition() {
		if(parent == null)
			return super.getPosition();
		
		if(Client.GAME_CLOCK.getFrame() != lastFrame)
			updateCashe();
			
		return positionCashe;
	}
}
