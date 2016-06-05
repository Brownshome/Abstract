package abstractgame.world.map;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;

import javax.vecmath.Quat4f;
import javax.vecmath.Vector3f;

import com.bulletphysics.linearmath.QuaternionUtil;

import abstractgame.Client;
import abstractgame.Common;
import abstractgame.Server;
import abstractgame.io.user.Console;
import abstractgame.time.Clock;
import abstractgame.util.ApplicationException;
import abstractgame.util.FloatSupplier;
import abstractgame.util.Util;
import abstractgame.world.World;
import abstractgame.world.entity.BasicEntity;
import abstractgame.world.entity.Entity;
import abstractgame.world.entity.MovableEntity;
import abstractgame.world.entity.Player;

public class PlayerSpawn extends BasicEntity implements MapObject {
	public static enum CheckCollision {
		ALL,
		LAND,
		ENTITY
	}
	
	public static PlayerSpawn creator(Map<String, Object> data) {
		Vector3f position = Util.toVector3f(MapObject.validate("spawn", List.class, "position", data));
		return new PlayerSpawn(
				position, 
				CheckCollision.valueOf(MapObject.validate("spawn", String.class, "checkCollision", data).toUpperCase(Locale.ROOT)), 
				MapObject.validate("spawn", Number.class, "spawnInt", data).floatValue()
		);
	}
	
	String ID;
	CheckCollision checks;
	List<Player> spawnList = new ArrayList<>();
	
	int spawnInt;
	boolean hadFirstSpawn = false;
	
	public PlayerSpawn(Vector3f position, CheckCollision checks, float spawnInt) {
		super(position, new Quat4f(0, 0, 0, 1));
		this.spawnInt = (int) (spawnInt * Server.getTargetTPS());
	}
	
	@Override
	public void addToWorld(World world) {
		if(spawnInt == 0)
			return;
		
		if(Common.isSeverSide())
			world.onTick(() -> {
				if(spawnList.isEmpty())
					return;
				
				if(Server.SERVER_CLOCK.getTickNo() % spawnInt != 0)
					return;
				
				Console.fine("Spawning", "MAP");
				spawnList.forEach(this::spawnNow);
				spawnList.clear();
			});
	}

	/** Spawns the player now, this method is also used internally
	 * so this is the one to override for custom spawning logic */
	public void spawnNow(Player player) {
		if(!Common.isSeverSide())
			throw new ApplicationException("This method should not be called on the client", "CLIENT");
		
		player.getPosWritable().set(getPosition());
		player.flushChanges();
		Server.getWorld().addEntity(player);
	}
	
	/** Adds the player to the spawn queue, returning the time left until
	 * the player is spawned again in server ticks. */
	public int spawn(Player player) {
		if(!Common.isSeverSide())
			throw new ApplicationException("This method should not be called on the client", "CLIENT");
		
		if(spawnInt == 0) {
			spawnNow(player);
			return 0;
		}
		
		spawnList.add(player);
		return spawnInt - (int) (Server.SERVER_CLOCK.getTickNo() % spawnInt);
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
