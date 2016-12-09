package abstractgame.io.model;

import java.nio.*;
import java.util.*;
import java.util.Map.Entry;

import javax.vecmath.*;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.*;

import abstractgame.io.patch.*;
import abstractgame.render.ModelRenderer;

/** A lightweight class representing a handle to a model already uploaded
 * to the GPU */
public class GPUModel {
	int VAO;
	int VBO;
	int IBO;
	int length;
	
	//A debug variable to hold the patches for an object on the CPU side
	PatchTree patches;
	
	//Tempary buffers for storing data for upload to the GPU
	IntBuffer indexBuffer;
	FloatBuffer vertexBuffer;

	public GPUModel(RawModel inputModel) {
		buildGeometry(inputModel);
	}
	
	private void buildGeometry(RawModel inputModel) {
		class WrappedIntArray {
			int[] array;

			WrappedIntArray(int[] array) {
				this.array = array;
			}

			@Override
			public boolean equals(Object other) {
				if(!(other instanceof WrappedIntArray))
					return false;

				int[] otherArray = ((WrappedIntArray) other).array;

				for(int i = 0; i < array.length; i++)
					if(array[i] != otherArray[i])
						return false;

				return true;
			}

			@Override
			public int hashCode() {
				int hash = 7;

				for(int i = 0; i < array.length; i++) {
					hash ^= i % 2 == 0 ? ~array[i] : array[i];
					hash *= 7;
				}

				return hash;
			}

			@Override
			public String toString() {
				return Arrays.toString(array);
			}
		}

		indexBuffer = BufferUtils.createIntBuffer(inputModel.faces.length * 3);
		int index = 0;

		//int[2] representing [vertex id, normal id]
		HashMap<WrappedIntArray, Integer> glVertexs = new HashMap<>();

		for(IndexedFace face : inputModel.faces) {
			//1
			for(int i = 0; i < 3; i++) {
				Integer vertexID;
				int[] vertex = new int[] {face.position[i], face.normal[i]};
				WrappedIntArray w = new WrappedIntArray(vertex);

				if((vertexID = glVertexs.putIfAbsent(w, index)) == null)
					vertexID = index++;

				indexBuffer.put(vertexID);
			}
		}

		vertexBuffer = BufferUtils.createFloatBuffer(glVertexs.size() * 6);

		for(Entry<WrappedIntArray, Integer> entry : glVertexs.entrySet()) {
			int[] v = entry.getKey().array;

			vertexBuffer.position(entry.getValue() * 6);
			vertexBuffer.put(inputModel.vertexs[v[0]].x).put(inputModel.vertexs[v[0]].y).put(inputModel.vertexs[v[0]].z)
			.put(inputModel.normals[v[1]].x).put(inputModel.normals[v[1]].y).put(inputModel.normals[v[1]].z);
		}

		vertexBuffer.position(0);
	}

	/** This must be called from the render thread */
	void uploadToGPU() {		
		int VBO = GL15.glGenBuffers();
		int VAO = GL30.glGenVertexArrays();
		int indexs = GL15.glGenBuffers();

		indexBuffer.flip();
		int length = indexBuffer.remaining();
		
		GL30.glBindVertexArray(VAO);
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, VBO);
		GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, indexs);

		GL15.glBufferData(GL15.GL_ARRAY_BUFFER, vertexBuffer, GL15.GL_STATIC_DRAW);
		GL15.glBufferData(GL15.GL_ELEMENT_ARRAY_BUFFER, indexBuffer, GL15.GL_STATIC_DRAW);

		GL20.glEnableVertexAttribArray(0);
		GL20.glEnableVertexAttribArray(1);
		GL20.glVertexAttribPointer(0, 3, GL11.GL_FLOAT, false, 24, 0);  //position
		GL20.glVertexAttribPointer(1, 3, GL11.GL_FLOAT, false, 24, 12); //normal
		
		this.VAO = VAO;
		this.VBO = VBO;
		this.IBO = indexs;
		this.length = length;
	}
	
	public int getLength() {
		return length;
	}

	public int getVAO() {
		assert VAO != 0 : "Model VAO not created";
		
		return VAO;
	}

	public void drawPatches() {
		patches.drawPatches();
	}
}
