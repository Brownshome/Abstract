package abstractgame.world;

import javax.vecmath.Quat4f;
import javax.vecmath.Vector3f;

import com.bulletphysics.collision.shapes.CollisionShape;

import abstractgame.io.model.PhysicsMeshLoader;
import abstractgame.world.entity.BasicEntity;

public class PhysicsShapeWrapper extends BasicEntity implements MapObject {
	CollisionShape collisionShape;
	
	PhysicsShapeWrapper(CollisionShape shape, Vector3f position, Quat4f orientation) {
		super(position, orientation);
		
		collisionShape = shape;
	}
	
	@Override
	public void addToWorld(World world) {
		// TODO Auto-generated method stub

	}
}
