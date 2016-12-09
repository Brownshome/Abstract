package abstractgame.io.model;

import java.nio.file.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.*;

import javax.vecmath.Vector2f;
import javax.vecmath.Vector3f;

import abstractgame.Client;
import abstractgame.io.FileIO;
import abstractgame.io.patch.PatchTree;
import abstractgame.io.user.Console;
import abstractgame.util.*;

public class ModelLoader {
	/**
	 * Represents tasks to be done by the IO thread with regards to model loading. These actions are designed to be
	 * chained to form the final task.
	 **/
	@FunctionalInterface
	public static interface ModelLoadingTask {
		RawModel call(String name, RawModel model) throws Exception;
		
		/** Returns true is this task accepts a null input and creates a new model, default false */
		default boolean isHead() {
			return false;
		}

		/** 
		 * Concatenates a list of IO tasks into a single {@link Callable<RawModel>}. The tasks are
		 * performed in sequential order. The first task is given {@link null} as an input.
		 * 
		 * NB the first task must be a 'head' task see {@link ModelLoadingTask#isHead()}
		 **/
		static Callable<RawModel> chainOperations(String name, ModelLoadingTask... actions) {
			return () -> {
				RawModel start = null;
				for(int i = 0; i < actions.length; i++) {
					if(i == 0 != actions[i].isHead())
						throw new ApplicationException("The loading task ' " + actions[i] + " '( " + i + " ) returned the wrong value for 'isHead()'", "MODEL_IO");
						
					start = actions[i].call(name, start);
				}
				
				return start;
			};
		}
	}
	
	/** 
	 * A list of pre-built {@link ModelLoadingTask} objects for common model configurations.
	 **/
	public static enum LoadType implements ModelLoadingTask {
		/** Loads a model from disk or gets it from memory if it is already loaded */
		LOAD_OBJ(true) {
			@Override
			public RawModel call(String name, RawModel model) throws Exception {
				List<String> lines = Files.readAllLines(Paths.get(MODEL_DIR + name + OBJ_EXT));
				return decodeOBJ(lines);
			}
		},
		
		CREATE_PHYS(false) {
			@Override
			public RawModel call(String name, RawModel model) throws Exception {
				model.genPhysicsModel();
				return model;
			}
		},
		
		CREATE_GPU_MODEL(false) {
			@Override
			public RawModel call(String name, RawModel model) throws Exception {
				model.genGPUModel();
				return model;
			}
		},
		
		GEN_PATCH_DATA(false) {
			@Override
			public RawModel call(String name, RawModel model) throws Exception {
				model.setPatchTree(PatchTree.generateFromUV(model));
				return model;
			}
		},
		
		LOAD_PATCH_DATA(false) {
			@Override
			public RawModel call(String name, RawModel model) throws Exception {
				model.setPatchTree(PatchTree.readFromFile(name, model));
				return model;
			}
		};
		
		boolean isHead;
		
		LoadType(boolean head) {
			isHead = head;
		}
		
		@Override
		public boolean isHead() {
			return isHead;
		}
	}
	
	static final String MODEL_DIR = "resources/model/";
	static final String OBJ_EXT = ".obj";
	static final String PATCH_EXT = ".patches.yaml";
	
	static final Map<String, Future<RawModel>> MODEL_FUTURES = Collections.synchronizedMap(new HashMap<>());
	
	/** If this model was already loading this method attaches the tasks onto the start of the list
	 * 
	 * @param name The identifier of the model to load
	 * @param actions The actions to perform on loading */
	public static void preLoadModel(String name, ModelLoadingTask... actions) {
		if(MODEL_FUTURES.containsKey(name)) {
			//The IO executor must be ordered for this to work!
			actions[0] = new ModelLoadingTask() {
				Future<RawModel> future = MODEL_FUTURES.get(name);
				
				@Override
				public RawModel call(String name, RawModel model) throws Exception {
					//May cause deadlock if out of order execution occurs, FileIO#IO_THREAD
					return future.get();
				}
				
				@Override
				public boolean isHead() {
					return true;
				}
			};
		}
		
		MODEL_FUTURES.put(name, FileIO.IO_THREAD.submit(ModelLoadingTask.chainOperations(name, actions)));
	}

	/**
	 * Loads the model and performs no further processing on it
	 **/
	public static void preLoadModel(String name) {
		preLoadModel(name, LoadType.LOAD_OBJ);
	}
	
	/** Gets a model, this method will block until the model is loaded
	 * if the model was not preloaded by a prior call to preLoadModel 
	 * 
	 * @param name The identifier of the model to load
	 * @return The loaded model
	 **/
	public static RawModel getModel(String name) {
		Future<RawModel> task = MODEL_FUTURES.get(name);
		
		if(task != null) {
			try {
				return task.get();
			} catch (InterruptedException e) {
				//DO NOTHING
			} catch (ExecutionException e) {
				throw new ApplicationException("Error loading model", e, "MODEL IO");
			}
		}
		
		Console.warn("Model not pre-loaded '" + name + "'.", "MODEL IO");
		preLoadModel(name);
		return getModel(name);
	}

	static RawModel decodeOBJ(List<String> lines) {
		List<Vector3f> vs = new ArrayList<>();
		List<Vector3f> ns = new ArrayList<>();
		List<IndexedFace> fs = new ArrayList<>();
		List<Vector2f> ts = new ArrayList<>();
		
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
				case "f":
					fs.add(new IndexedFace(elements, vs.size(), ns.size()));
					break;
				case "vt":
					ts.add(decodeVec2(elements));
					break;
				case "#":
				case "":
					break;
				default:
					Console.warn("Unimplemented .obj tag found " + elements[0], "MODEL IO");
			}
		}
		
		return new RawModel(vs.toArray(new Vector3f[vs.size()]), ns.toArray(new Vector3f[ns.size()]), ts.toArray(new Vector2f[ts.size()]), fs.toArray(new IndexedFace[fs.size()]));
	}

	private static Vector2f decodeVec2(String[] elements) {
		return new Vector2f(Float.parseFloat(elements[1]), Float.parseFloat(elements[2]));
	}

	private static Vector3f decodeVec3(String[] elements) {
		return new Vector3f(Float.parseFloat(elements[1]), Float.parseFloat(elements[2]), Float.parseFloat(elements[3]));
	}

	public static Path getPatchPatch(String modelID) {
		return Paths.get(MODEL_DIR + modelID + PATCH_EXT);
	}
}
