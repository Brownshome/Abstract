package abstractgame.io.model;

import java.util.Arrays;
import java.util.function.IntUnaryOperator;
import java.util.stream.*;

import javax.vecmath.*;

import com.bulletphysics.collision.shapes.*;

public class PhysicsModel extends StridingMeshInterface {
	static class ArrayVertexData extends VertexData {
		Vector3f[] vertexs;
		/** indices to the vertexs array grouped in 3s */
		int[] indexs;

		ArrayVertexData(Vector3f[] vertexs, int[] indexs) {
			this.vertexs = vertexs;
			this.indexs = indexs;
		}
		
		@Override
		public int getVertexCount() {
			return vertexs.length;
		}

		@Override
		public int getIndexCount() {
			return indexs.length;
		}

		@Override
		public <T extends Tuple3f> T getVertex(int idx, T out) {
			out.set(vertexs[idx]);
			return out;
		}

		@Override
		public void setVertex(int idx, float x, float y, float z) {
			assert false : "This should be happening";
		}

		@Override
		public int getIndex(int idx) {
			return indexs[idx];
		}
	}
	
	ArrayVertexData data;
	
	public PhysicsModel(RawModel model) {
		data = new ArrayVertexData(model.vertexs, Arrays.stream(model.faces).flatMapToInt(f -> IntStream.of(f.position)).map(i -> i - 1).toArray()); //IDK why I used streams here, slow but cool :D
	}
	
	@Override
	public VertexData getLockedVertexIndexBase(int subpart) {
		return data;
	}

	@Override
	public VertexData getLockedReadOnlyVertexIndexBase(int subpart) {
		return data;
	}

	@Override
	public void unLockVertexBase(int subpart) {}

	@Override
	public void unLockReadOnlyVertexBase(int subpart) {}

	@Override
	public int getNumSubParts() {
		return 1;
	}
	
	@Override
	public void preallocateVertices(int numverts) {}

	@Override
	public void preallocateIndices(int numindices) {}
}
