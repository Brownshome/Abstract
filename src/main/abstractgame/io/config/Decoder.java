package abstractgame.io.config;

import java.util.Map;
import java.util.function.Function;

import abstractgame.world.map.MapObject;

/** Just a typedef for Function&lt;Map&lt;String, Object&gt;, T&gt; */
@FunctionalInterface
public interface Decoder<T> extends Function<Map<String, Object>, T> {}
