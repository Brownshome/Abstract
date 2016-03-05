package abstractgame.world;

import abstractgame.Client;
import abstractgame.world.entity.DynamicEntity;

import com.bulletphysics.dynamics.DiscreteDynamicsWorld;

/** Represents a game world */
public class World extends TickableImpl {
	public static World currentWorld = null;
	
	DiscreteDynamicsWorld physicsWorld;
	WorldMap map;
	
	/** Adds the entity to the physics grid, this will not add it to the tick counter */
	public void addDynamicEntity(DynamicEntity entity) {
		physicsWorld.addRigidBody(entity);
	}
	
	@Override
	public void tick() {
		super.tick();
		
		//physicsWorld.stepSimulation(Game.GAME_CLOCK.getDelta(), 6);
	}
}
