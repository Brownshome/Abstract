package abstractgame.ui;

import abstractgame.world.World;

public class GameScreen extends Screen {
	private static enum State {
		WAIT_FOR_SERVER,
		LOAD_WORLD_DATA,
		RECIEVE_STATE_UPDATE,
		RUN
	}
	
	public static GameScreen INSTANCE = new GameScreen();
	
	State state = State.WAIT_FOR_SERVER;
	World world;
	
	@Override
	public void tick() {
		
		//wait for a response from the server
		//load the world data
		//recieve state update
		//tick world
		
		
	}
}
