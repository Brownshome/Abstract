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
	
	public SpawnTimerPacket(ByteBuffer buffer) {
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
	public void fill(ByteBuffer output) {
		output.putInt(ticks);
		output.putInt(TPS);
	}

	@Override
	public void handleClient() {
		long startFrame = Client.GAME_CLOCK.getTime();
		float seconds = (float) ticks / TPS;
		
		GameScreen.respawnTimer = () -> seconds - (Client.GAME_CLOCK.getTime() - startFrame) / 1000f;
	}

	@Override
	public int getPayloadSize() {
		return 2 * Integer.BYTES;
	}

}
