package abstractgame.render;

import javax.vecmath.*;

import org.lwjgl.opengl.Display;

import abstractgame.world.entity.Entity;

import com.bulletphysics.linearmath.QuaternionUtil;

public class Camera implements Entity {
	public static final Camera INSTANCE = new Camera();
	private static final CameraHost NULL_HOST = new CameraHost() {
		private Quat4f orientation = new Quat4f(1, 0, 0, 0);
		private Vector3f position = new Vector3f(0, 0, 0);
		
		@Override
		public Vector3f getPosition() {
			return position;
		}

		@Override
		public Quat4f getOrientation() {
			return orientation;
		}
	};
	
	public static Matrix4f projectionMatrix = new Matrix4f();
	
	//variables to set the projection matrix
	static float fov = (float) Math.toRadians(106);
	static float nearPlane = 0.1f;
	static float farPlane = 1000f;
	
	//primary quantities
	public static CameraHost host = NULL_HOST;
	
	//secondary quantities
	public static Vector3f position = new Vector3f();
	public static Matrix4f viewMatrix = new Matrix4f();
	public static Vector3f up = new Vector3f();
	public static Vector3f forward = new Vector3f();
	public static Vector3f right = new Vector3f();
	
	/** Populates the secondary fields from offset and host */
	public static void recalculate() {
		if(host == null)
			return;
		
		Quat4f quat = new Quat4f(host.getOrientation());
		Vector3f vec = new Vector3f(host.getOffset());
		QuaternionUtil.quatRotate(quat, vec, vec);
		position.add(host.getPosition(), vec);
		
		vec.set(position);
		vec.negate();
		
		quat.inverse();
		QuaternionUtil.quatRotate(quat, vec, vec);
		Matrix4f tmp = new Matrix4f(quat, vec, 1);
		
		viewMatrix.set(tmp);
		tmp.set(host.getOrientation(), host.getPosition(), 1);
		
		up.set(tmp.m01, tmp.m11, tmp.m21);
		forward.set(tmp.m02, tmp.m12, tmp.m22);
		right.set(tmp.m00, tmp.m10, tmp.m20);
	}
	
	@Override
	public Vector3f getPosition() {
		return position;
	}
	
	public static void setCameraHost(CameraHost newHost) {
		if(newHost == null)
			host = NULL_HOST;
		
		if(newHost != FreeCamera.FREE_CAM &&FreeCamera.isActive()) {
			FreeCamera.setOldHost(newHost);
			return;
		}
			
		if(host == newHost)
			return;
		
		host.onCameraUnset();
		
		host = newHost;
		
		host.onCameraSet();
		
		recalculate();
	}
	
	public static CameraHost getCamera() {
		return host;
	}
	
	@Override
	public Quat4f getOrientation() {
		return host.getOrientation();
	}
	
	/** moves a point from world space to view space
	 * 
	 *  @param v The point to transform 
	 * @return the w value returned by the transformation, this is the scale of the object as view by the camera. */
	public static float transform(Point3f p) {
		Vector4f v = new Vector4f(p.x, p.y, p.z, 1);
		viewMatrix.transform(v);
		projectionMatrix.transform(v);
		p.x = v.x / v.w;
		p.y = v.y / v.w;
		p.z = v.z / v.w;
		
		return v.w;
	}
	
	/** To be used for changes in fov and initial creation */
	public static void createProjectionMatrix() {
		float aspectRatio = (float) Display.getWidth() / Display.getHeight();
		float xScale = 1f / (float) Math.tan(fov * 0.5);
		float yScale = xScale * aspectRatio;
		float frustrumLength = farPlane - nearPlane;
		
		projectionMatrix.m00 = xScale;
		projectionMatrix.m11 = yScale;
		projectionMatrix.m22 = (farPlane + nearPlane) / frustrumLength;
		projectionMatrix.m32 = 1;
		projectionMatrix.m23 = - 2 * nearPlane * farPlane / frustrumLength;
	}
	
	static void onResize() {
		float aspectRatio = (float) Display.getHeight() / Display.getWidth();
		projectionMatrix.m00 = projectionMatrix.m11 * aspectRatio;
	}
	
	/**Creates a {@link Matrix4f} representing the transformation from worldspace to cameraspace.
	 * <p> 
	 * The vectors are all normalized and orthoganalized, so feel free to put in whatever you want.
	 * 
	 *  @param position The position of the camera
	 *  @param forward A unit vector pointing forward
	 *  @param up A unit vector pointing up
	 *  @return The matrix
	 **/
	public static Matrix4f lookAt(Vector3f position, Vector3f forward, Vector3f up) {
		Vector3f z = new Vector3f();
		z.normalize(forward);
		
		Vector3f y = new Vector3f();
		y.normalize(up);
		
		Vector3f x = new Vector3f();
		x.cross(y, z);
		x.normalize();
		
		y.cross(z, x);
		
		Matrix4f r = new Matrix4f();
		
		r.m00 = x.x;
		r.m10 = x.y;
		r.m20 = x.z;
		r.m01 = y.x;
		r.m11 = y.y;
		r.m21 = y.z;
		r.m02 = z.x;
		r.m12 = z.y;
		r.m22 = z.z;
		r.m03 = -position.x;
		r.m13 = -position.y;
		r.m23 = -position.z;
		r.m33 = 1;
		
		return r;
	}
}
