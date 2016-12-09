package abstractgame.io.model;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.vecmath.Quat4f;
import javax.vecmath.Vector3f;

import abstractgame.Common;
import abstractgame.io.config.ConfigFile;
import abstractgame.io.config.Decoder;
import abstractgame.util.Util;

import com.bulletphysics.collision.shapes.BoxShape;
import com.bulletphysics.collision.shapes.BvhTriangleMeshShape;
import com.bulletphysics.collision.shapes.CapsuleShape;
import com.bulletphysics.collision.shapes.CollisionShape;
import com.bulletphysics.collision.shapes.CompoundShape;
import com.bulletphysics.collision.shapes.ConeShape;
import com.bulletphysics.collision.shapes.ConvexHullShape;
import com.bulletphysics.collision.shapes.CylinderShape;
import com.bulletphysics.collision.shapes.SphereShape;
import com.bulletphysics.collision.shapes.StaticPlaneShape;
import com.bulletphysics.collision.shapes.TriangleShape;
import com.bulletphysics.linearmath.Transform;

public class PhysicsMeshLoader {
	public static final String PHYSICS_DIR = "../../" + ModelLoader.MODEL_DIR + "physics/";
	
	public static final Map<String, Decoder<CollisionShape>> DECODERS = new HashMap<>();
	
	static final Map<String, CollisionShape> CASHE_SERVER = new HashMap<>();
	static final Map<String, CollisionShape> CASHE_CLIENT = new HashMap<>();
	
	public static CollisionShape getShape(String shape) {
		return (Common.isServerSide() ? CASHE_SERVER : CASHE_CLIENT).computeIfAbsent(shape, s -> decodeShape(ConfigFile.getFile(PHYSICS_DIR + s + "_phys")));
	}

	public static BvhTriangleMeshShape decodeStaticMesh(Map<String, Object> data) {
		PhysicsModel model = ModelLoader.getModel((String) data.get("model")).getPhysicsModel();
		
		return new BvhTriangleMeshShape(model, true /* TODO IDK what this does yet */);
	}
	
	/** No check is made for loops in the recursion so don't be stupid pls
	 * 
	 *  @param data The config data to decode
	 *  @return The created {@link CollisionShape}
	 *
	 **/
	public static CollisionShape decodeExternal(Map<String, Object> data) {
		return getShape((String) data.get("file"));
	}
	
	public static BoxShape decodeBox(Map<String, Object> data) {
		List<? extends Number> sizeRaw = (List<? extends Number>) data.get("size");
		Vector3f size = Util.toVector3f(sizeRaw);
		
		return new BoxShape(size);
	}
	
	public static SphereShape decodeSphere(Map<String, Object> data) {
		return new SphereShape(((Number) data.get("radius")).floatValue());
	}
	
	public static CapsuleShape decodeCapsule(Map<String, Object> data) {
		float radius = ((Number) data.get("radius")).floatValue();
		float height = ((Number) data.get("height")).floatValue();
		
		return new CapsuleShape(radius, height);
	}
	
	public static CylinderShape decodeCylinder(Map<String, Object> data) {
		List<? extends Number> sizeRaw = (List<? extends Number>) data.get("size");
		Vector3f size = Util.toVector3f(sizeRaw);
		
		return new CylinderShape(size);
	}
	
	public static ConeShape decodeCone(Map<String, Object> data) {
		float radius = ((Number) data.get("radius")).floatValue();
		float height = ((Number) data.get("height")).floatValue();
		
		return new ConeShape(radius, height);
	}
	
	public static ConvexHullShape decodeConvexHull(Map<String, Object> data) {
		assert false : "not implemented";
		return null;
	}
	
	public static TriangleShape decodeTriangle(Map<String, Object> data) {
		List<List<? extends Number>> points = (List<List<? extends Number>>) data.get("points");
		
		Vector3f v1 = Util.toVector3f(points.get(0));
		Vector3f v2 = Util.toVector3f(points.get(1));
		Vector3f v3 = Util.toVector3f(points.get(2));
		
		return new TriangleShape(v1, v2, v3);
	}
	
	public static CompoundShape decodeCompound(Map<String, Object> data) {
		CompoundShape shape = new CompoundShape();
		
		List<Map<String, Object>> children = (List<Map<String, Object>>) data.get("children");
		
		Vector3f offset = new Vector3f();
		Quat4f orientation = new Quat4f();
		Transform transform = new Transform();
		for(Map<String, Object> childRaw : children) {
			List<? extends Number> offsetRaw = (List<? extends Number>) childRaw.get("offset");
			Util.toVector3f(offsetRaw, offset);
			
			List<? extends Number> orientationRaw = (List<? extends Number>) childRaw.get("orientation");
			Util.toQuat4f(orientationRaw, orientation);
			
			transform.origin.set(offset);
			transform.basis.set(orientation);
			
			CollisionShape child = DECODERS.get(childRaw.get("type")).apply(childRaw);
		
			shape.addChildShape(transform, child);
		}
		
		shape.recalculateLocalAabb();
		return shape;
	}
	
	public static StaticPlaneShape decodePlane(Map<String, Object> data) {
		List<? extends Number> normalRaw = (List<? extends Number>) data.get("normal");
		Vector3f normal = Util.toVector3f(normalRaw);
		float constant = ((Number) data.get("constant")).floatValue();
		
		return new StaticPlaneShape(normal, constant);
	}
	
	static CollisionShape decodeShape(ConfigFile file) {
		Map<String, Object> map = file.getTree();
		return DECODERS.get(map.get("type")).apply(map);
	}
}
