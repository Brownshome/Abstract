package abstractgame.io.model;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;
import java.util.function.Supplier;

import javax.vecmath.Vector2f;
import javax.vecmath.Vector3f;

import abstractgame.Game;
import abstractgame.io.FileIO;
import abstractgame.util.ApplicationException;

public class ModelLoader {
	static final String MODEL_DIR = "res/model/";
	static final String OBJ_EXT = ".obj";
	static final String MTL_EXT = ".mtl";
	static boolean hasPreloaded = false;
	
	public static Future<Model> loadModel(String name) {
		if(!hasPreloaded)
			return null;
		
		return FileIO.IO_THREAD.submit(() -> {
			List<String> lines = Files.readAllLines(Paths.get(MODEL_DIR + name + OBJ_EXT));
			return decodeOBJ(lines);
		});
	}

	static Model decodeOBJ(List<String> lines) {
		List<Vector3f> vs = new ArrayList<>();
		List<Vector3f> ns = new ArrayList<>();
		List<Vector2f> uvs = new ArrayList<>();
		List<Face> fs = new ArrayList<>();
		List<String> materialLibs = new ArrayList<>();
		String material = null;
		String objectName = null;
		String groupName = null;
		boolean smoothing = false;
		
		for(String s : lines) {
			String[] elements = s.split(" ");
			if(elements.length == 0)
				continue;
			
			switch(elements[0]) {
				case "v":
					vs.add(decodeVec3(elements));
					break;
				case "vn":
					ns.add(decodeVec3(elements));
					break;
				case "vt":
					uvs.add(decodeVec2(elements));
					break;
				case "f":
					fs.add(new Face(elements, material, vs.size(), ns.size(), uvs.size()));
					break;
				case "mtllib":
					materialLibs.add(elements[1]);
					break;
				case "usemtl":
					material = elements[1];
					break;
				case "o":
					objectName = elements[1];
					break;
				case "g":
					groupName = elements[1];
					break;
				case "s":
					smoothing = elements[1].equalsIgnoreCase("on");
					break;
				case "#":
				case "":
					break;
				default:
					throw new ApplicationException("Error reading line '" + s + "'", "MODEL IO");
			}
		}
		
		return new Model(vs.toArray(new Vector3f[vs.size()]), uvs.toArray(new Vector2f[uvs.size()]), ns.toArray(new Vector3f[ns.size()]), fs.toArray(new Face[fs.size()]));
	}

	private static Vector2f decodeVec2(String[] elements) {
		return new Vector2f(Float.parseFloat(elements[1]), Float.parseFloat(elements[2]));
	}

	private static Vector3f decodeVec3(String[] elements) {
		return new Vector3f(Float.parseFloat(elements[1]), Float.parseFloat(elements[2]), Float.parseFloat(elements[3]));
	}
}
