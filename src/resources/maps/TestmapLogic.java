import abstractgame.io.user.Console;
import abstractgame.world.World;
import abstractgame.world.map.MapLogicProxy;

public class TestmapLogic implements MapLogicProxy {

	@Override
	public void initialize(World world) {
		Console.inform("Initializer run successfully", "MAP LOGIC");
	}

	@Override
	public void destroy(World world) {}
}
