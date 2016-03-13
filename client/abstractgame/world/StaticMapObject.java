package abstractgame.world;

import java.util.List;
import java.util.Map;

import javax.vecmath.Quat4f;
import javax.vecmath.Vector3f;

import com.bulletphysics.collision.shapes.CollisionShape;
import com.bulletphysics.dynamics.RigidBody;
import com.bulletphysics.linearmath.MotionState;
import com.bulletphysics.linearmath.Transform;

import abstractgame.io.model.ModelLoader;
import abstractgame.io.model.PhysicsMeshLoader;
import abstractgame.render.ModelRenderer;
import abstractgame.render.RenderEntity;

public class StaticMapObject extends RenderEntity implements MapObject {
	public static MapObject creator(Map<String, Object> data) {
		String model = (String) data.get("model");
		String physics = (String) data.get("physics");
		
		CollisionShape shape = physics == null ? null : PhysicsMeshLoader.getShape(physics);
		
		List<Double> position = (List<Double>) data.get("position");
		List<Double> orientation = (List<Double>) data.get("orientation");
		
		Vector3f p = new Vector3f(position.get(0).floatValue(), position.get(1).floatValue(), position.get(2).floatValue());
		Quat4f q = new Quat4f(orientation.get(0).floatValue(), orientation.get(1).floatValue(), orientation.get(2).floatValue(), orientation.get(3).floatValue());
		
		if(model == null) {
			/** A physics only object */
			return new PhysicsShapeWrapper(shape, p, q);
		}
	
		return new StaticMapObject(model, shape, p, q);
	}
	
	CollisionShape shape;
	
	public StaticMapObject(String modelName, CollisionShape shape, Vector3f position, Quat4f orientation) {
		super(ModelLoader.loadModel(modelName), position, orientation);
		
		this.shape = shape;
	}
	
	@Override
	public void addToWorld(World world) {
		ModelRenderer.addDynamicModel(this);
		
		RigidBody body = new RigidBody(0, new MotionState() {
			@Override
			public Transform getWorldTransform(Transform out) {
				out.basis.set(StaticMapObject.super.getOrientation());
				out.origin.set(StaticMapObject.super.getPosition());
				return out;
			}

			@Override
			public void setWorldTransform(Transform worldTrans) {
				//do nothing, this is a static object
			}
			
		}, shape);
		
		world.physicsWorld.addRigidBody(body);
	}
}
