package abstractgame.world.entity;

import java.nio.ByteBuffer;

import abstractgame.net.Identity;
import abstractgame.net.Side;
import abstractgame.util.Indexable;

/** Indicates an object that should have it's physical state synced across the network.
 * The implementer NEEDS to define &lt<init&gt>(ByteBuffer) making sure it does
 * not access sensitive state as it will be called on the net thread. The initialize method
 * will be called on the main thread and thread sensitive code should be placed here */
public interface NetworkEntity extends Indexable {
	/** States which client has control of this entity, returning null indicates
	 * that the server has control of the entity
	 * 
	 *  @return the {@link Identity} controling this object, or null if the
	 *  server is in control */
	Identity getController();
	
	/** @return the length of the state in Bytes */
	int getStateUpdateLength();
	/** @return the length of the data used to create the entity */
	int getCreateLength();
	/** Populates the ByteBuffer with data, this data can be in any form */
	void fillStateUpdate(ByteBuffer buffer);
	/** Updates the state from the buffer given */
	void updateState(ByteBuffer buffer);
	/** Fills the data needed to create this object */
	void fillCreateData(ByteBuffer buffer);
	/** Called on the main thread once the entity has populated */
	void initialize();

	boolean needsSyncTo(Identity id);

	boolean needsSync();
}
