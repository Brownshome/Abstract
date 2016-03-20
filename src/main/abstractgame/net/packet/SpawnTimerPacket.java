package abstractgame.net.packet;

import java.nio.ByteBuffer;

import abstractgame.Client;
import abstractgame.Server;
import abstractgame.net.Side;
import abstractgame.ui.GameScreen;

/** Informs the client of the amount of time left until the player spawns.
 * 
 *  Format: 
 *  	int ticks */
public class SpawnTimerPacket extends Packet {
	int ticks;
	int TPS;
	
	public SpawnTimerPacket(byte[] array) {
		ByteBuffer buffer = ByteBuffer.wrap(array);
		ticks = buffer.getInt();
		TPS = buffer.getInt();
	}
	
	public SpawnTimerPacket(int ticksLeft) {
		ticks = ticksLeft;
		TPS = Server.SERVER_CLOCK.getTPS();
		if(TPS == -1)
			TPS = Server.getTargetTPS();
	}

	@Override
	public void fill(byte[] data, int offset) {
		ByteBuffer buffer = ByteBuffer.wrap(data, offset, data.length - offset);
		buffer.putInt(ticks);
	}

	@Override
	public void handleClient() {
		long startFrame = Client.GAME_CLOCK.getTime();
		float seconds = (float) ticks / TPS;
		
		GameScreen.respawnTimer = () -> seconds - (Client.GAME_CLOCK.getTime() - startFrame) / 1000f;
	}
	
	@Override
	public Side getHandleSide() {
		return Side.CLIENT;
	}

}
