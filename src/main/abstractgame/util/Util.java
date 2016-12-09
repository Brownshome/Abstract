package abstractgame.util;

import java.nio.*;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.MalformedInputException;
import java.nio.charset.StandardCharsets;
import java.util.List;

import javax.vecmath.Matrix3d;
import javax.vecmath.Matrix3f;
import javax.vecmath.Matrix4f;
import javax.vecmath.Quat4d;
import javax.vecmath.Quat4f;
import javax.vecmath.Vector3f;

import org.lwjgl.BufferUtils;
import org.yaml.snakeyaml.*;
import org.yaml.snakeyaml.DumperOptions.FlowStyle;

import com.bulletphysics.linearmath.Transform;

import abstractgame.Client;
import abstractgame.Common;
import abstractgame.Server;

public class Util {
	private static final FloatBuffer MAT_BUFFER = BufferUtils.createFloatBuffer(16);
	
	public static final Vector3f ZERO_VEC3 = new Vector3f(0, 0, 0);
	
	/** A shared parser to be used for all yaml reading tasks */
	public static final Yaml YAML_PARSER;
	
	static {
		DumperOptions dumper = new DumperOptions();
		dumper.setDefaultFlowStyle(FlowStyle.BLOCK);
		YAML_PARSER = new Yaml(dumper);
	}
	
	/** This reads UTF-8 strings terminated by 0xFF, this allows NUL to occur in
	 * the string without weirdness happening. This will throw {@link ApplicationException}
	 * if the string is malformed
	 * 
	 *  @return The retrieved {@link String}
	 *  @param buffer The input data */
	public static String readTerminatedString(ByteBuffer buffer) {
		buffer.mark();
		
		try {
			while(buffer.get() != -1);
		} catch(BufferUnderflowException bue) {
			throw new ApplicationException("String not 0xFF terminated", "NET");
		}
		
		int oldLimit = buffer.limit();
		buffer.limit(buffer.position() - 1);
		buffer.reset();
		String s = StandardCharsets.UTF_8.decode(buffer).toString();
		buffer.limit(oldLimit);
		buffer.get(); //discard the NUL byte
		return s;
	}
	
	/** This writes UTF-8 strings terminated by 0xFF, this allows NUL to occur in
	 * the string without weirdness happening
	 * 
	 *  @param buffer The buffer to put the data into
	 *  @param string The string to encode
	 *
	 **/
	public static void writeTerminatedString(ByteBuffer buffer, String string) {
		buffer.put(string.getBytes(StandardCharsets.UTF_8));
		buffer.put((byte) -1); //0xff
	}
	
	static final float MBPC = StandardCharsets.UTF_8.newEncoder().maxBytesPerChar();
	public static int getMaxLength(String string) {
		return (int) (string.length() * MBPC + 1);
	}
	
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

	/** Note that this method re-uses the buffer returned, so use the buffer
	 * before the next call to this method 
	 * 
	 * @return A buffer containing the matrix
	 * @param m The matrix to encode
	 * 
	 **/
	public static FloatBuffer toFloatBuffer(Matrix4f m) {
		MAT_BUFFER.clear();
		
		MAT_BUFFER.put(m.m00).put(m.m01).put(m.m02).put(m.m03);
		MAT_BUFFER.put(m.m10).put(m.m11).put(m.m12).put(m.m13);
		MAT_BUFFER.put(m.m20).put(m.m21).put(m.m22).put(m.m23);
		MAT_BUFFER.put(m.m30).put(m.m31).put(m.m32).put(m.m33);
		
		MAT_BUFFER.flip();
		
		return MAT_BUFFER;
	}

	/** Note that this method re-uses the buffer returned, so use the buffer
	 * before the next call to this method 
	 * 
	 * @return A buffer containing the matrix
	 * @param m The matrix to encode
	 * 
	 **/
	public static FloatBuffer toFloatBuffer(Matrix3f m) {
		MAT_BUFFER.clear();
		
		MAT_BUFFER.put(m.m00).put(m.m01).put(m.m02);
		MAT_BUFFER.put(m.m10).put(m.m11).put(m.m12);
		MAT_BUFFER.put(m.m20).put(m.m21).put(m.m22);
		
		MAT_BUFFER.flip();
		
		return MAT_BUFFER;
	}

	/** Makes a quat the transforms (0, 1, 0) to up and (0, 0, 1) to forward.
	 * <br>
	 * NB: up and forward are normalized and orthogalized prior to use 
	 * 
	 * @return The new quaternion
	 * @param up The up basis vector
	 * @param forward The forward basis vector
	 * 
	 **/
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

	/** Runs the task on the main thread 
	 * 
	 * @param r The task to queue on the main thread
	 * 
	 **/
	public static void queueOnMainThread(Runnable r) {
		if(Common.isServerSide())
			Server.addTask(r);
		else
			Client.addTask(r);
	}

	/** fills vec with 3 floats from data, reading only the first 3 values
	 * 
	 * @return vec
	 * @param vec The ouput vector
	 * @param data The input list */
	public static Vector3f toVector3f(List<? extends Number> data, Vector3f vec) {
		vec.set(data.get(0).floatValue(), data.get(1).floatValue(), data.get(2).floatValue());
		return vec;
	}
	
	/** fills vec with 3 floats from data, reading only the first 3 values 
	 * 
	 * @return The new vector
	 * @param data The input list 
	 *
	 **/
	public static Vector3f toVector3f(List<? extends Number> data) {
		return toVector3f(data, new Vector3f());
	}
	
	/** fills quat with 4 floats from data, reading only the first 4 values
	 * 
	 * @return quat
	 * @param quat The ouput quaternion
	 * @param data The input list */
	public static Quat4f toQuat4f(List<? extends Number> data, Quat4f quat) {
		quat.set(data.get(0).floatValue(), data.get(1).floatValue(), data.get(2).floatValue(), data.get(3).floatValue());
		return quat;
	}
	
	/** fills quat with 4 floats from data, reading only the first 4 values 
	 * 
	 * @param data The input list
	 * @return A new quaternion
	 *
	 **/
	public static Quat4f toQuat4f(List<? extends Number> data) {
		return toQuat4f(data);
	}

	public static String toHexString(int val) {
		String result = "";
		String raw = Integer.toUnsignedString(val, 16);
		
		for(int i = 0; i < 8; i++) {
			int index = i - 8 + raw.length();
			
			if(index < 0)
				result += '0';
			else
				result += raw.charAt(index);
			
			if(i != 7 && i % 2 == 1)
				result += '-';
		}
		
		return result;
	}

	public static Transform getIdentityTransform() {
		Transform t = new Transform();
		t.setIdentity();
		return t;
	}

	public static IntBuffer toIntBuffer(int... data) {
		IntBuffer buffer = BufferUtils.createIntBuffer(data.length);
		buffer.put(data);
		buffer.flip();
		return buffer;
	}
}
