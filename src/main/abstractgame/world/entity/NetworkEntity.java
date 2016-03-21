package abstractgame.world.entity;

import java.nio.ByteBuffer;

import abstractgame.net.Identity;
import abstractgame.net.Side;

/** Indicates an entity that should have it's physical state synced across the network */
public interface NetworkEntity extends Entity {
	/** States which client has control of this entity, returning null indicates
	 * that the server has control of the entity */
	Identity getController();
	
	/** Returns the length of the state in Bytes, this should be constant */
	int getStateUpdateLength();
	/** Returns the length of the data used to create the entity */
	int getCreateLength();
	/** Populates the ByteBuffer with data, this data can be in any form */
	void fillStateUpdate(ByteBuffer buffer);
	/** Updates the state from the buffer given */
	void updateState(ByteBuffer buffer);
	/** Fills the data needed to create this object */
	void fillCreateData(ByteBuffer buffer);
}
