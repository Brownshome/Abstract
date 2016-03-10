package abstractgame.world;

/** Map objects are non-player elements that make up a map */
public interface MapObject {
	public static void loadCreators() {
		World.regesterObjectType("static", StaticMapObject::creator);
	}
	
	void addToWorld(World world);
}
