package abstractgame.io.config;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.Future;

import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.DumperOptions.FlowStyle;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.scanner.ScannerException;

import abstractgame.io.FileIO;
import abstractgame.io.user.Console;
import abstractgame.util.ApplicationException;
import abstractgame.util.ProcessFuture;

public class ConfigFile {
	public static final String CONFIG_PATH = "resources/config/";
	static final String CONFIG_EXT = ".yaml";
	static final Map<String, ConfigFile> OPEN_FILES = new HashMap<>();
	static final Yaml PARSER;
	
	public static enum Policy {
		WRITE,
		NO_WRITE
	}
	
	static {
		DumperOptions dumper = new DumperOptions();
		dumper.setDefaultFlowStyle(FlowStyle.BLOCK);
		PARSER = new Yaml(dumper);
	}

	LinkedHashMap<String, Object> map = new LinkedHashMap<>();
	String name;
	Policy policy = Policy.WRITE;

	public static Future<ConfigFile> loadConfigFileAsync(String name) {
		Future<String> innerFuture = FileIO.readTextFileAsyncAsString(Paths.get(CONFIG_PATH + name + CONFIG_EXT), false);
		return new ProcessFuture<>(innerFuture, text -> {
			ConfigFile file = new ConfigFile(text, name);
			OPEN_FILES.put(name, file);
			return file;
		});
	}
	
	public static ConfigFile loadConfigFile(String name) {
		try {
			String lines = FileIO.readTextFileAsString(Paths.get(CONFIG_PATH + name + CONFIG_EXT), true);
			ConfigFile file = new ConfigFile(lines, name);
			OPEN_FILES.put(name, file);
			return file;
		} catch (IOException ioe) {
			throw new ApplicationException("Error reading config files", ioe, "IO");
		}
	}

	public static ConfigFile getFile(String path, Policy policy) {
		//TODO possibly cashe these values too
		
		ConfigFile file = getFile(path);
		if(file.policy != policy)
			return new ConfigFile(file, policy);
		else
			return file;
	}
	
	public static ConfigFile getFile(String path) {
		return OPEN_FILES.computeIfAbsent(path, ConfigFile::loadConfigFile);
	}

	ConfigFile(ConfigFile file, Policy policy) {
		this.map = file.map;
		this.name = file.name;
		this.policy = policy;
	}
	
	ConfigFile(String file, String name) {
		try {
			map = (LinkedHashMap<String, Object>) PARSER.load(file);
		} catch(ScannerException se) {
			throw new ApplicationException("Unable to read " + name, se, "IO");
		}
		
		if(map == null)
			map = new LinkedHashMap<String, Object>();
		this.name = name;
		
		if(getProperty("format on next load", false)) {
			map.put("format on next load", false);
			Console.fine("formatting file " + name, "CONFIG");
			writeToFileSystem();
		}
	}

	/** This method returns null if there is no value assigned */
	public <T> T getPropertyNoDefault(String string, Class<T> clazz) {
		return getProperty(string, null, clazz);
	}
	
	public <T> T getProperty(String location, T defaultValue) {
		return getProperty(location, defaultValue, (Class<T>) defaultValue.getClass());
	}
	
	public <T extends V, V> V getProperty(String location, T defaultValue, Class<V> clazz) {
		String[] tree = location.split("[.]");

		assert tree.length != 0;

		Map<String, Object> mapLevel = map;

		for(int i = 0; i < tree.length - 1; i++) {
			Map<String, Object> tmp = (Map<String, Object>) mapLevel.get(tree[i]);
			if(tmp == null) {
				tmp = new LinkedHashMap<String, Object>();
				mapLevel.put(tree[i], tmp);
			}
			mapLevel = tmp;
		}

		Object value = mapLevel.get(tree[tree.length - 1]);

		if(value == null) {
			if(defaultValue == null)
				return null;
			
			if(policy == Policy.WRITE) {
				mapLevel.put(tree[tree.length - 1], defaultValue);
				writeToFileSystem();
				Console.inform("Config value " + location + " in file " + name + " was missing, creating.", "CONFIG");
			}
			
			value = defaultValue;
		}

		if(!clazz.isAssignableFrom(value.getClass()))
			throw new ApplicationException("Item " + location + " already exists but is not " + defaultValue.getClass().getSimpleName(), "CONFIG");

		return (V) value;
	}

	void writeToFileSystem() {
		FileIO.writeAsync(Paths.get(CONFIG_PATH + name + CONFIG_EXT), PARSER.dump(map));
	}

	public Map<String, Object> getTree() {
		return map;
	}
}
