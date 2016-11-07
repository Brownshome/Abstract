package abstractgame.io.model;

import java.nio.*;
import java.util.*;
import java.util.Map.Entry;

import javax.vecmath.Vector3f;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.*;

import abstractgame.render.ModelRenderer;

/** A lightweight class representing a handle to a model already uploaded
 * to the GPU */
public class GPUModel {
	int VAO;
	int VBO;
	int IBO;
	int length;

	List<Collection<Patch>> layers = new ArrayList<>();
	
	public GPUModel(RawModel inputModel) {
		buildOpenGLBuffers(inputModel);
	}
	
	/** This method does nothing if the opengl buffers have already been populated */
	void buildOpenGLBuffers(RawModel inputModel) {
		buildGeometry(inputModel);
		createPatchTopograhpy(inputModel);
	}

	private void createPatchTopograhpy(RawModel model) {
		Collection<Patch> patchLayer = new ArrayList<>();
		
		for(Face face : model.faces) {
			Patch p = createPatch(face, model);
			patchLayer.add(p);
		}

		//group surrounding patches into a parent patch, ensure there are 3 sepperate patches of the desired level surrounding the vertex
		
		for(int level = 0; layers.size() == 0 || patchLayer.size() != layers.get(layers.size() - 1).size(); level++) {
			layers.add(new ArrayList<>(patchLayer));
			
			Map<Integer, Collection<Patch>> vertexConnectivity = new HashMap<>();
			
			//populate the vertex map
			for(Patch patch : patchLayer)
				for(int vertex : patch.vertexs)
					vertexConnectivity.computeIfAbsent(vertex, v -> new ArrayList<>()).add(patch);
			
			Iterator<Entry<Integer, Collection<Patch>>> iterator = vertexConnectivity.entrySet().iterator();
			while(iterator.hasNext()) {
				Entry<Integer, Collection<Patch>> entry = iterator.next();
				Collection<Patch> touchingPatches = entry.getValue();
				
				//only bother shrinking vertexs greater than 2 patches
				if(touchingPatches.size() <= 2) continue;
				
				//remove the patches that are being merged from the connectivity map
				for(Patch patch : touchingPatches)
					for(int vertex : patch.vertexs)
						if(vertex != entry.getKey())
							vertexConnectivity.get(vertex).remove(patch);
				
				patchLayer.removeAll(touchingPatches);
				patchLayer.add(new Patch(touchingPatches));
			}
		}
		
		layers.add(patchLayer);
		
		//add elements starting from the top down.
		ArrayList<Patch> elements = new ArrayList<>();
		for(Patch p : patchLayer) {
			p.addPatches(elements);
		}
		
		ModelRenderer.addPatchData(elements);
	}

	Patch createPatch(Face face, RawModel model) {
		Vector3f a = model.vertexs[face.v1 - 1];
		Vector3f b = model.vertexs[face.v2 - 1];
		Vector3f c = model.vertexs[face.v3 - 1];
		
		Vector3f normal = new Vector3f();
		normal.add(model.normals[face.n1 - 1]);
		normal.add(model.normals[face.n2 - 1]);
		normal.add(model.normals[face.n3 - 1]);
		normal.scale(1f / 3);
		
		Vector3f position = new Vector3f();
		position.add(a);
		position.add(b);
		position.add(c);
		position.scale(1f / 3);
		
		Vector3f tmp = new Vector3f();
		tmp.sub(a, b);
		float ab = tmp.length();
		tmp.sub(b, c);
		float bc = tmp.length();
		tmp.sub(c, a);
		float ac = tmp.length();
		
		float s = ab + bc + ac;
		s /= 2;
		
		float area = (float) Math.sqrt(s * (s - ab) * (s - bc) * (s - ac));
		
		return new Patch(normal, position, area, Arrays.asList(face.v1 - 1, face.v2 - 1, face.v3 - 1));
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

		IntBuffer indexBuffer;
		FloatBuffer vertexBuffer;
		
		indexBuffer = BufferUtils.createIntBuffer(inputModel.faces.length * 3);
		int index = 0;

		//int[2] representing [vertex id, normal id]
		HashMap<WrappedIntArray, Integer> glVertexs = new HashMap<>();

		for(Face face : inputModel.faces) {
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

		vertexBuffer = BufferUtils.createFloatBuffer(glVertexs.size() * 6);

		for(Entry<WrappedIntArray, Integer> entry : glVertexs.entrySet()) {
			int[] v = entry.getKey().array;

			vertexBuffer.position(entry.getValue() * 6);
			vertexBuffer.put(inputModel.vertexs[v[0] - 1].x).put(inputModel.vertexs[v[0] - 1].y).put(inputModel.vertexs[v[0] - 1].z)
			.put(inputModel.normals[v[1] - 1].x).put(inputModel.normals[v[1] - 1].y).put(inputModel.normals[v[1] - 1].z);
		}

		vertexBuffer.position(0);

		uploadBuffers(indexBuffer, vertexBuffer);
	}

	void uploadBuffers(IntBuffer indexBuffer, FloatBuffer vertexBuffer) {
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
		return VAO;
	}
}
