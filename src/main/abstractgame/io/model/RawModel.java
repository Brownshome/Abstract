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

import abstractgame.render.Renderer;

/** This represents a models that contains the data from a read in .obj file
 * this will be converted to a more appropriate internal representation closer
 * to the time */
public class RawModel {
	GPUModel gpuModel = null;
	PhysicsModel physModel = null;
	
	Vector3f[] vertexs;
	Vector3f[] normals;
	Face[] faces;

	public RawModel(Vector3f[] vertexs, Vector3f[] normals, Face[] faces) {
		this.vertexs = vertexs;
		this.normals = normals;
		this.faces = faces;
	}

	public GPUModel getGPUModel() {
		return gpuModel != null ? gpuModel : (gpuModel = new GPUModel(this));
	}
	
	public PhysicsModel getPhysicsModel() {
		return physModel != null ? physModel : (physModel = new PhysicsModel(this));
	}
}
