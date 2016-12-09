package abstractgame.io.model;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map.Entry;

import javax.vecmath.Vector2f;
import javax.vecmath.Vector3f;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;

import abstractgame.io.patch.PatchTree;
import abstractgame.render.Renderer;

/** This represents a models that contains the data from a read in .obj file
 * this will be converted to a more appropriate internal representation closer
 * to the time */
public class RawModel {
	GPUModel gpuModel = null;
	PhysicsModel physModel = null;
	PatchTree patchTree = null;
	
	public Vector3f[] vertexs;
	public Vector3f[] normals;
	public Vector2f[] textureCoordinates;
	public IndexedFace[] faces;

	public RawModel(Vector3f[] vertexs, Vector3f[] normals, Vector2f[] UVs, IndexedFace[] faces) {
		this.vertexs = vertexs;
		this.normals = normals;
		this.faces = faces;
		this.textureCoordinates = UVs;
		
		for(IndexedFace face : faces)
			face.model = this;
	}

	public GPUModel getGPUModel() {
		if(gpuModel == null)
			genGPUModel();
		
		return gpuModel;
	}
	
	void genGPUModel() {
		gpuModel = new GPUModel(this);
		if(patchTree != null)
			gpuModel.patches = patchTree;
	}

	public PhysicsModel getPhysicsModel() {
		if(physModel == null)
			genPhysicsModel();
		
		return physModel;
	}

	void genPhysicsModel() {
		physModel = new PhysicsModel(this);
	}

	void setPatchTree(PatchTree tree) {
		patchTree = tree;
		if(gpuModel != null)
			gpuModel.patches = tree;
	}

	public void uploadToGPU() {
		if(gpuModel != null)
			gpuModel.uploadToGPU();
		
		if(patchTree != null)
			patchTree.uploadToGPU();
	}
}
