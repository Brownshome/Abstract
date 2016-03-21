package abstractgame.render;

import java.util.ArrayList;
import java.util.List;

import javax.vecmath.Matrix3f;
import javax.vecmath.Matrix4f;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;

import com.bulletphysics.linearmath.MatrixUtil;

import abstractgame.util.Util;

public class ModelRenderer implements Renderer {
	static boolean isDirty = false;
	
	static List<RenderEntity> staticMesh = new ArrayList<>();
	static List<RenderEntity> dynamicModels = new ArrayList<>();
	
	static int modelProgram;
	
	static int staticMeshVAO;
	static int staticMeshVBO;
	static int staticIndexBuffer;
	static int staticMeshSize;
	
	/** This will cause a rebuild of the static mesh next frame, try to keep all calls to this
	 * within one frame. NB this model will be fixed in the position it is now, it will not respect
	 * updates. Updating the entity and then invalidating the static mesh will move the model. */
	public static void addStaticModel(RenderEntity entity) {
		addDynamicModel(entity);
		
		/*staticMesh.add(entity);
		isDirty = true;*/
	}
	
	public static void removeStaticModel(RenderEntity entity) {
		staticMesh.remove(entity);
		isDirty = true;
	}
	
	public static void addDynamicModel(RenderEntity entity) {
		dynamicModels.add(entity);
	}
	
	public static void removeDynamicModel(RenderEntity entity) {
		dynamicModels.remove(entity);
	}
	
	@Override
	public void initialize() {
		int vertexShader = GLHandler.createShader("model-vertex", GL20.GL_VERTEX_SHADER);
		int fragmentShader = GLHandler.createShader("model-fragment", GL20.GL_FRAGMENT_SHADER);
		modelProgram = GLHandler.createProgram(vertexShader, fragmentShader);
		
		staticMeshVAO = GL30.glGenVertexArrays();
		staticMeshVBO = GL15.glGenBuffers();
		staticIndexBuffer = GL15.glGenBuffers();
		
		GL30.glBindVertexArray(staticMeshVAO);
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, staticMeshVBO);
		GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, staticIndexBuffer);
		
		GL20.glEnableVertexAttribArray(0);
		GL20.glEnableVertexAttribArray(1);
		GL20.glVertexAttribPointer(0, 3, GL11.GL_FLOAT, true, 24, 0);
		GL20.glVertexAttribPointer(1, 3, GL11.GL_FLOAT, true, 24, 12);
	}
	
	static void rebuildStaticMesh() {
		//TODO
	}
	
	static void renderStaticMesh() {
		GL30.glBindVertexArray(staticMeshVAO);
		GL11.glDrawElements(GL11.GL_TRIANGLES, staticMeshSize, GL11.GL_UNSIGNED_INT, 0);
	}
	
	@Override
	public void render() {
		if(isDirty)
			rebuildStaticMesh();
		
		GL20.glUseProgram(modelProgram);
		
		Matrix4f tmp = new Matrix4f();
		tmp.mul(Camera.projectionMatrix, Camera.viewMatrix);
		GL20.glUniformMatrix4(0, true, Util.toFloatBuffer(tmp));
		
		Matrix3f id = new Matrix3f();
		id.setIdentity();
		GL20.glUniformMatrix3(1, true, Util.toFloatBuffer(id));
		
		GL11.glEnable(GL11.GL_DEPTH_TEST);
		
		renderStaticMesh();
		dynamicModels.forEach(RenderEntity::render);
		GL11.glDisable(GL11.GL_DEPTH_TEST);
	}

	@Override
	public float getPass() {
		return 0;
	}
}