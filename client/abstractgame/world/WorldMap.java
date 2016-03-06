package abstractgame.world;

import java.util.ArrayList;
import java.util.List;

public class WorldMap {
	enum Source {
		SERVER,
		WORKSHOP,
		LOCAL
	}

	String name;
	Source source;

	double sizeX;
	double sizeY;

	List<MapObject> mapObjects = new ArrayList<>();

	public String getMapIdentifier() {
		return source.toString() + ":" + name;
	}
}
