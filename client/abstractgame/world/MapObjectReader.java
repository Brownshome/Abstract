package abstractgame.world;

import java.util.Map;
import java.util.function.Function;

/** Just a typedef for Function&lt;Map&lt;String, Object&gt;, MapObject&gt; */
public interface MapObjectReader extends Function<Map<String, Object>, MapObject> {}
