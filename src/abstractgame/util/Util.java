package abstractgame.util;

import java.nio.FloatBuffer;
import java.util.List;

import javax.vecmath.Matrix3d;
import javax.vecmath.Matrix3f;
import javax.vecmath.Matrix4f;
import javax.vecmath.Quat4d;
import javax.vecmath.Quat4f;
import javax.vecmath.Vector3f;

import org.lwjgl.BufferUtils;

public class Util {
	private static final FloatBuffer MAT4_BUFFER = BufferUtils.createFloatBuffer(16);
	
	public static String collect(List<String> list) {
		if(list.isEmpty())
			return "";
		
		StringBuilder builder = new StringBuilder();
		for(String s : list) {
			builder.append(s);
			builder.append('\n');
		}
		
		builder.deleteCharAt(builder.length() - 1);
		return builder.toString();
	}
	
	public static void rotate(Quat4f quat, Vector3f vec) {
		Quat4f tmp = toQuat(vec);
		tmp.mul(quat, tmp);
		tmp.mulInverse(quat);
		vec.set(tmp.x, tmp.y, tmp.z);
	}
	
	public static Quat4f toQuat(Vector3f v) {
		Quat4f q = new Quat4f();
		//not using the constructor to avoid normalization
		q.x = v.x;
		q.y = v.y;
		q.z = v.z;
		return q;
	}

	/** Note that this method re-uses the buffer returned, so use the buffer
	 * before the next call to this method */
	public static FloatBuffer toFloatBuffer(Matrix4f m) {
		MAT4_BUFFER.clear();
		
		MAT4_BUFFER.put(m.m00).put(m.m01).put(m.m02).put(m.m03);
		MAT4_BUFFER.put(m.m10).put(m.m11).put(m.m12).put(m.m13);
		MAT4_BUFFER.put(m.m20).put(m.m21).put(m.m22).put(m.m23);
		MAT4_BUFFER.put(m.m30).put(m.m31).put(m.m32).put(m.m33);
		
		MAT4_BUFFER.flip();
		
		return MAT4_BUFFER;
	}

	/** Makes a quat the transforms (0, 1, 0) to up and (0, 0, 1) to forward.
	 * <br>
	 * NB: up and forward are normalized and orthogalized prior to use */
	public static Quat4f getQuat(Vector3f up, Vector3f forward) {
		Vector3f z = new Vector3f();
		z.normalize(forward);
			
		Vector3f y = new Vector3f();
		y.normalize(up);
			
		Vector3f x = new Vector3f();
		x.cross(y, z);
		x.normalize();
			
		y.cross(z, x);
			
		Matrix3f r = new Matrix3f();
			
		r.m00 = x.x;
		r.m10 = x.y;
		r.m20 = x.z;
		r.m01 = y.x;
		r.m11 = y.y;
		r.m21 = y.z;
		r.m02 = z.x;
		r.m12 = z.y;
		r.m22 = z.z;

		Quat4f result = new Quat4f();
		result.set(r);
		
		return result;
	}

	public static void upright(Quat4d quat) {
		Matrix3d mat = new Matrix3d();
		mat.set(quat);
	}
}
