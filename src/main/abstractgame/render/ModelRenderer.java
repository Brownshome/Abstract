package abstractgame.render;

import java.nio.*;
import java.util.*;

import javax.vecmath.Matrix3f;
import javax.vecmath.Matrix4f;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.*;

import com.bulletphysics.linearmath.MatrixUtil;

import abstractgame.io.model.Patch;
import abstractgame.util.Util;

public class ModelRenderer implements Renderer {
	static boolean isDirty = false;
	
	static List<RenderEntity> staticMesh = new ArrayList<>();
	static List<RenderEntity> dynamicModels = new ArrayList<>();
	
	static int modelProgram;
	
	static int patchBuffer = -1;
	static int patchBufferSize = 0; //The size in patches
	
	static class VariableData {
		static final VariableData INVALID_DATA = new VariableData(-1, -1);
		
		int stride;
		int offset;
		
		VariableData(int stride, int offset) {
			this.stride = stride;
			this.offset = offset;
		}
	}
	
	/** Variables dictating the layout of the patches in GPU memory */
	static VariableData positionData, areaData, normalData, 
		brightnessData, illuminationDirectionData, nextElementData;
	
	static int staticMeshVAO;
	static int staticMeshVBO;
	static int staticIndexBuffer;
	static int staticMeshSize;
	
	/** This will cause a rebuild of the static mesh next frame, try to keep all calls to this
	 * within one frame. NB this model will be fixed in the position it is now, it will not respect
	 * updates. Updating the entity and then invalidating the static mesh will move the model.
	 *
	 * @param entity The render object to add
	 **/
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
		assert entity != null;
		
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
		
		createPatchBuffer();
		
		/*staticMeshVAO = GL30.glGenVertexArrays();
		staticMeshVBO = GL15.glGenBuffers();
		staticIndexBuffer = GL15.glGenBuffers();
		
		GL30.glBindVertexArray(staticMeshVAO);
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, staticMeshVBO);
		GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, staticIndexBuffer);
		
		GL20.glEnableVertexAttribArray(0);
		GL20.glEnableVertexAttribArray(1);
		GL20.glVertexAttribPointer(0, 3, GL11.GL_FLOAT, true, 24, 0);
		GL20.glVertexAttribPointer(1, 3, GL11.GL_FLOAT, true, 24, 12);*/
	}
	
	private void createPatchBuffer() {
		//These must match the variable names in 'shaders/model-fragment.glsl'
		//The stride of each of these is guranteed to be the same
		positionData = getVariableData("patches[0].position");
		areaData = getVariableData("patches[0].area");
		normalData = getVariableData("patches[0].normal");
		brightnessData = getVariableData("patches[0].brightness");
		illuminationDirectionData = getVariableData("patches[0].illuminationDirection");
		nextElementData = getVariableData("patches[0].nextElement");
	}
	
	private VariableData getVariableData(String name) {
		IntBuffer returnData = BufferUtils.createIntBuffer(2);
		
		int index = GL43.glGetProgramResourceIndex(modelProgram, GL43.GL_BUFFER_VARIABLE, name);
		
		if(index != GL31.GL_INVALID_INDEX) {
			GL43.glGetProgramResource(modelProgram, GL43.GL_BUFFER_VARIABLE, index, Util.toIntBuffer(GL43.GL_TOP_LEVEL_ARRAY_STRIDE, GL43.GL_OFFSET), null, returnData);
			return new VariableData(returnData.get(), returnData.get());
		}
		
		return VariableData.INVALID_DATA;
	}
	
	public static void addPatchData(List<Patch> patches) {		
		ByteBuffer compiledPatches = BufferUtils.createByteBuffer(patches.size() * positionData.stride);
		
		for(ListIterator<Patch> it = patches.listIterator(); it.hasNext(); ) {
			Patch patch = it.next();
			int index = it.previousIndex();
			int start = index * positionData.stride;
			
			compiledPatches.position(start + positionData.offset);
			compiledPatches.putFloat(patch.getPosition().x);
			compiledPatches.putFloat(patch.getPosition().y);
			compiledPatches.putFloat(patch.getPosition().z);
			
			compiledPatches.position(start + areaData.offset);
			compiledPatches.putFloat(patch.getArea() / (float) Math.PI);
			
			compiledPatches.position(start + normalData.offset);
			compiledPatches.putFloat(patch.getNormal().x);
			compiledPatches.putFloat(patch.getNormal().y);
			compiledPatches.putFloat(patch.getNormal().z);
			
			compiledPatches.position(start + brightnessData.offset);
			compiledPatches.putFloat(0);
			
			compiledPatches.position(start + illuminationDirectionData.offset);
			compiledPatches.putFloat(0);
			compiledPatches.putFloat(0);
			compiledPatches.putFloat(0);
			
			compiledPatches.position(start + nextElementData.offset);
			compiledPatches.putInt(patch.getNumberOfChildren() == 0 ? -1 : patch.getNumberOfChildren() + index + 1);
		}
		
		compiledPatches.rewind();
		
		if(patchBuffer != -1) {
			int oldPatchBuffer = patchBuffer;
			GL15.glBindBuffer(GL31.GL_COPY_READ_BUFFER, oldPatchBuffer);
		
			patchBuffer = GL15.glGenBuffers();
			GL30.glBindBufferBase(GL43.GL_SHADER_STORAGE_BUFFER, 0, patchBuffer);
			GL15.glBufferData(GL43.GL_SHADER_STORAGE_BUFFER, (patchBufferSize + patches.size()) * positionData.stride, GL15.GL_STATIC_DRAW);
			GL31.glCopyBufferSubData(GL31.GL_COPY_READ_BUFFER, GL43.GL_SHADER_STORAGE_BUFFER, 0, 0, patchBufferSize * positionData.stride); //All strides are the same
			GL15.glDeleteBuffers(oldPatchBuffer);
			GL15.glBufferSubData(GL43.GL_SHADER_STORAGE_BUFFER, patchBufferSize * positionData.stride, compiledPatches);
		} else {
			//create new buffer, no need to copy data from the old one
			GL30.glBindBufferBase(GL43.GL_SHADER_STORAGE_BUFFER, 0, patchBuffer);
			GL15.glBufferData(GL43.GL_SHADER_STORAGE_BUFFER, compiledPatches, GL15.GL_STATIC_DRAW);
		}
	}
	
	static void rebuildStaticMesh() {
		//TODO
	}
	
	static void renderStaticMesh() {
		/*GL30.glBindVertexArray(staticMeshVAO);
		GL11.glDrawElements(GL11.GL_TRIANGLES, staticMeshSize, GL11.GL_UNSIGNED_INT, 0);*/
	}
	
	@Override
	public void render() {
		if(isDirty)
			rebuildStaticMesh();
		
		GL20.glUseProgram(modelProgram);
		
		Matrix4f tmp = new Matrix4f();
		tmp.mul(Camera.projectionMatrix, Camera.viewMatrix);
		GL20.glUniformMatrix4(0, true, Util.toFloatBuffer(tmp));
		
		/*Matrix3f id = new Matrix3f();
		id.setIdentity();
		GL20.glUniformMatrix3(1, true, Util.toFloatBuffer(id));*/

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