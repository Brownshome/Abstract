package abstractgame.io.model;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map.Entry;

import javax.vecmath.Vector2f;
import javax.vecmath.Vector3f;

import org.lwjgl.BufferUtils;

/** Represents one of the large 2.5m segments */
public class Model {
	/** The largest patch that will be present in the smallest detail level */
	public static final double GRID_SIZE = 0.1;
	
	Vector3f[] vertexs;
	Vector2f[] textureCoords;
	Vector3f[] normals;
	Face[] faces;
	
	public FloatBuffer vertexBuffer;
	public IntBuffer indexBuffer;
	public ByteBuffer patchData;
	
	public Model(Vector3f[] vertexs, Vector2f[] textureCoords, Vector3f[] normals, Face[] faces) {
		this.vertexs = vertexs;
		this.textureCoords = textureCoords;
		this.normals = normals;
		this.faces = faces;
	}
	
	public void buildOpenGLBuffers() {
		class WrappedIntArray {
			int[] array;
			
			WrappedIntArray(int[] array) {
				this.array = array;
			}
			
			public boolean equals(Object other) {
				if(!(other instanceof WrappedIntArray))
					return false;
				
				int[] otherArray = ((WrappedIntArray) other).array;
				
				for(int i = 0; i < array.length; i++)
					if(array[i] != otherArray[i])
						return false;
				
				return true;
			}
			
			public int hashCode() {
				int hash = 7;
				
				for(int i = 0; i < array.length; i++) {
					hash ^= i % 2 == 0 ? ~array[i] : array[i];
					hash *= 7;
				}
				
				return hash;
			}
			
			public String toString() {
				return Arrays.toString(array);
			}
		}
		
		indexBuffer = BufferUtils.createIntBuffer(faces.length * 3);
		int index = 0;
		
		//int[2] representing [vertex id, normal id]
		HashMap<WrappedIntArray, Integer> glVertexs = new HashMap<>();
		
		for(Face face : faces) {
			//1
			Integer vertexID;
			int[] vertex = new int[] {face.v1, face.n1};
			WrappedIntArray w = new WrappedIntArray(vertex);
			
			if((vertexID = glVertexs.putIfAbsent(w, index)) == null)
				vertexID = index++;
			
			indexBuffer.put(vertexID);
			
			//2
			vertex = new int[] {face.v2, face.n2};
			w = new WrappedIntArray(vertex);
			
			if((vertexID = glVertexs.putIfAbsent(w, index)) == null)
				vertexID = index++;
			
			indexBuffer.put(vertexID);
			
			//3
			vertex = new int[] {face.v3, face.n3};
			w = new WrappedIntArray(vertex);
			
			if((vertexID = glVertexs.putIfAbsent(w, index)) == null)
				vertexID = index++;
			
			indexBuffer.put(vertexID);
		}
		
		indexBuffer.flip();
		
		vertexBuffer = BufferUtils.createFloatBuffer(glVertexs.size() * 6);
		
		for(Entry<WrappedIntArray, Integer> entry : glVertexs.entrySet()) {
			int[] v = entry.getKey().array;

			vertexBuffer.position(entry.getValue() * 6);
			vertexBuffer.put(vertexs[v[0] - 1].x).put(vertexs[v[0] - 1].y).put(vertexs[v[0] - 1].z)
						.put(normals[v[1] - 1].x).put(normals[v[1] - 1].y).put(normals[v[1] - 1].z);
		}
		
		vertexBuffer.position(0);
	}
}
