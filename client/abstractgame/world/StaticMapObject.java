package abstractgame.world;

import java.util.List;
import java.util.Map;

import javax.vecmath.Quat4f;
import javax.vecmath.Vector3f;

import com.bulletphysics.collision.shapes.CollisionShape;
import com.bulletphysics.dynamics.RigidBody;
import com.bulletphysics.linearmath.MotionState;
import com.bulletphysics.linearmath.QuaternionUtil;
import com.bulletphysics.linearmath.Transform;

import abstractgame.io.model.ModelLoader;
import abstractgame.io.model.PhysicsMeshLoader;
import abstractgame.render.ModelRenderer;
import abstractgame.render.RenderEntity;

public class StaticMapObject extends RenderEntity implements MapObject {
	public static MapObject creator(Map<String, Object> data) {
		String model = (String) data.get("model");
		
		Map<String, Object> physics = (Map<String, Object>) data.get("physics");
		
		CollisionShape shape = null;
		Vector3f o = null;
		
		if(physics != null) {
			String physicsFile = (String) physics.get("file");
		
			List<? extends Number> offset = (List<Double>) physics.get("offset");
			if(offset != null)
				o = new Vector3f(offset.get(0).floatValue(), offset.get(1).floatValue(), offset.get(2).floatValue());
			
			shape = PhysicsMeshLoader.getShape(physicsFile);
		}
		
		List<? extends Number> position = (List<Double>) data.get("position");
		List<? extends Number> orientation = (List<Double>) data.get("orientation");
		
		Vector3f p = new Vector3f(position.get(0).floatValue(), position.get(1).floatValue(), position.get(2).floatValue());
		Quat4f q = new Quat4f(orientation.get(0).floatValue(), orientation.get(1).floatValue(), orientation.get(2).floatValue(), orientation.get(3).floatValue());
		
		if(model == null) {
			/** A physics only object */
			return new StaticPhysicsObject(shape, p, q);
		}
	
		return new StaticMapObject(model, shape, p, q, o);
	}
	
	RigidBody body;
	String ID;
	
	public StaticMapObject(String modelName, CollisionShape shape, Vector3f position, Quat4f orientation, Vector3f offset) {
		super(ModelLoader.loadModel(modelName), position, orientation);
		
		if(offset != null)
			QuaternionUtil.quatRotate(orientation, offset, offset);
		
		body = new RigidBody(0, new MotionState() {
			@Override
			public Transform getWorldTransform(Transform out) {
				out.basis.set(StaticMapObject.super.getOrientation());
				
				if(offset == null)
					out.origin.set(StaticMapObject.super.getPosition());
				else
					out.origin.add(StaticMapObject.super.getPosition(), offset);
				
				return out;
			}

			@Override
			public void setWorldTransform(Transform worldTrans) {
				//do nothing, this is a static object
			}
			
		}, shape);
	}
	
	@Override
	public void addToWorld(World world) {
		ModelRenderer.addDynamicModel(this);
		world.physicsWorld.addRigidBody(body);
	}

	@Override
	public String getID() {
		return ID;
	}

	@Override
	public void setID(String ID) {
		this.ID = ID;
	}
}
