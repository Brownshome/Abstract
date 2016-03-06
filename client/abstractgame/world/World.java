package abstractgame.world;

import abstractgame.Client;
import abstractgame.world.entity.DynamicEntity;

import com.bulletphysics.dynamics.DiscreteDynamicsWorld;

/** Represents a game world, when the game is running in embeded server mode there is only one copy
 * held by both the server and client sides */
public class World extends TickableImpl {
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

	public WorldMap getMap() {
		return map;
	}
}
