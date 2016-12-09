package abstractgame.io.patch;

import java.io.IOException;
import java.nio.file.Files;
import java.util.*;
import java.util.stream.Collectors;

import javax.vecmath.Vector3f;

import abstractgame.io.config.ConfigFile;
import abstractgame.io.model.*;
import abstractgame.render.ModelRenderer;
import abstractgame.util.Util;

/** 
 * A class representing the software-side storage and maniplation of
 * the patch data for GPU shading.
 * 
 * This class includes methods to generate the patch topography, read it from disk
 * and export a generated topography.
 **/
public class PatchTree {
	static final Vector3f[] DEBUG_COLOURS = new Vector3f[] {
		new Vector3f(0, 0, 0),
		new Vector3f(1, 0, 0),
		new Vector3f(0, 1, 0),
		new Vector3f(0, 0, 1),
		new Vector3f(.6f, .6f, 0),
		new Vector3f(.6f, 0, .6f),
		new Vector3f(0, .6f, .6f),
		new Vector3f(1, 1, 1)
	};
	
	static final float TRIANGLE_OFFSET = 1e-5f;
	
	//A list of all patches to be uploaded to the GPU in the correct order
	List<Patch> patches = new ArrayList<>();
	//Layer zero is the base layer
	List<Collection<Patch>> layers;
	//contains the triangles used in the finest level of approximation
	List<Vector3f[]> triangles = new ArrayList<>();
	
	Comparator<Patch> uComparator = (a, b) -> (int) (a.uv.x - b.uv.x);
	Comparator<Patch> vComparator = (a, b) -> (int) (a.uv.y - b.uv.y);	
	
	RawModel model;
	
	public static PatchTree generateFromUV(RawModel model) {
		PatchTree tree = new PatchTree(model);
		tree.generateGPUDataFromUV();
		return tree;
	}
	
	/**
	 * Reads a patch tree from the disk. The file format is a yaml base format, the file is a
	 * list of patches, each patch has an area, a position, a triangle id refering to the obj file, a normal and a parent. The root patches
	 * are missing a parent. If the patch is missing an area field they are populated by the areas of the children.
	 * If the patch has no children and no area an is thrown.
	 * 
	 * All normal vectors are normalized prior to reading. Zero length vectors will result in an error being thrown.
	 * 
	 * @param triangleSource The obj file to draw triangle data from
	 *
	 * @throws PatchFileFormatException if the file is not a valid patch structure
	 **/
	public static PatchTree readFromFile(String modelID, RawModel triangleSource) throws IOException, PatchFileFormatException {
		//Read all the patches.
		//Populate the children fields.
		//fill in missing data.
		//form into linear tree structure.
		
		PatchTree tree = new PatchTree();
		List<Map<String, Object>> list = Util.YAML_PARSER.loadAs(Files.newBufferedReader(ModelLoader.getPatchPatch(modelID)), List.class);
		List<Patch> rootPatches = new ArrayList<>();
		
		Patch[] patchArray = new Patch[list.size()];
		for(int i = 0; i < list.size(); i++) {
			readPatch(list, patchArray, triangleSource, i);
			if(patchArray[i].parentIndex == -1)
				rootPatches.add(patchArray[i]);
		}
		
		for(Patch patch : patchArray)
			patch.buildFromChildren();
		
		for(Patch root : rootPatches)
			root.addPatchesTo(tree.patches);
		
		tree.layers = new ArrayList<>();
		tree.layers.add(rootPatches);
		//TODO compact triangle source down
		tree.triangles = Arrays.stream(triangleSource.faces).map(IndexedFace::getTriangle).collect(Collectors.toList());
		
		for(int faceIndex = 0; faceIndex < triangleSource.faces.length; faceIndex++) {
			Vector3f[] triangle = tree.triangles.get(faceIndex);
			IndexedFace face = triangleSource.faces[faceIndex];
			
			for(int i = 0; i < triangle.length; i++) {
				triangle[i].scaleAdd(-TRIANGLE_OFFSET, face.getVertexNormal(i), triangle[i]);
			}
		}
		
		return tree;
	}
	
	static void readPatch(List<Map<String, Object>> rawData, Patch[] patches, RawModel triangleSource, int index) {
		patches[index] = new Patch(rawData.get(index), triangleSource);
		
		if(patches[index].parentIndex != -1) {
			if(patches[patches[index].parentIndex] == null)
				readPatch(rawData, patches, triangleSource, patches[index].parentIndex);
			
			patches[patches[index].parentIndex].children.add(patches[index]);
		}
	}
	
	PatchTree() {}
	
	PatchTree(RawModel model) {
		this.model = model;
	}
	
	boolean hasGeneratedDebug = false;
	public List<Collection<Patch>> getDebugInformation() {
		if(hasGeneratedDebug)
			return layers;
		
		hasGeneratedDebug = true;
		
		if(layers == null)
			generateGPUDataFromUV();
		
		for(int layer = 1; layer < layers.size() + 1; layer++) {
			for(Patch patch : layers.get(layer - 1)) {
				if(patch.children() == null)
					continue;
					
				if(layer == layers.size())
					layers.add(new ArrayList<>());
					
				layers.get(layers.size() - 1).addAll(patch.children());
			}
		}
		
		return layers;
	}
	
	void generateGPUDataFromUV() {
		//Create large patches for each uv segment
		//For each of these patches, sibdivide along the median of the patches, storing the result in the parent
	
		//Create the base patches
		
		//flood fill from a random point
		//remove from set
		Collection<Patch> segments = new ArrayList<>();
		Set<Patch> toBeSorted = new HashSet<>(model.faces.length);
		Map<Integer, List<Patch>> vertexConnectivity = new HashMap<>();
		
		for(IndexedFace face : model.faces) {
			Patch p = new Patch(face, triangles.size());
			triangles.add(face.getTriangle());
			
			toBeSorted.add(p);
			
			for(int x : face.getUVIndexs())
				vertexConnectivity.computeIfAbsent(x, v -> new ArrayList<>()).add(p);
		}
		
		while(!toBeSorted.isEmpty()) {
			List<Patch> currentSegment = new ArrayList<>();
			Patch seed = toBeSorted.iterator().next();
			currentSegment.add(seed);
			toBeSorted.remove(seed);
			
			//members that have been traversed are after the pointer, members that have not are still to be iterated
			for(ListIterator<Patch> iterator = currentSegment.listIterator(); iterator.hasNext(); ) {
				Patch patch = iterator.next();
				List<Patch> list = new ArrayList<>();
				
				for(int uv : patch.uvIndexs)
					for(Patch neighbour : vertexConnectivity.getOrDefault(uv, Collections.emptyList()))
						if(toBeSorted.remove(neighbour)) {
							iterator.add(neighbour);
							iterator.previous();
						}
			}
			
			Patch rootPatch = new Patch();
			subdivideByU(currentSegment, rootPatch);
			segments.add(rootPatch);
		}
		
		//segments now contains the root patches
		for(Patch segment : segments)
			segment.addPatchesTo(patches);
		
		layers = new ArrayList<>();
		layers.add(segments);
	}
	
	public void uploadToGPU() {
		ModelRenderer.addPatchData(patches);
		ModelRenderer.addTriangleData(triangles);
	}
	
	/** Splits the patches by their u values, this is the first step in splitting. */
	void subdivideByU(List<Patch> list, Patch parent) {
		if(list.size() <= 4) {
			parent.children(list);
			parent.buildFromChildren();
			return;
		}
		
		list.sort(uComparator);
		
		parent.children(new ArrayList<>());
		subdivideByV(list.subList(0, list.size() / 2), parent);
		subdivideByV(list.subList(list.size() / 2, list.size()), parent);
		parent.buildFromChildren();
	}
	
	/** Splits the patches by their v values, this is the last step and is when the patches are addded. */
	void subdivideByV(List<Patch> list, Patch parent) {
		Patch a = new Patch();
		Patch b = new Patch();
		
		list.sort(vComparator);
		
		parent.children().add(a);
		parent.children().add(b);
		
		subdivideByU(list.subList(0, list.size() / 2), a);
		subdivideByU(list.subList(list.size() / 2, list.size()), b);
	}
	
	public void drawPatches() {
		List<Collection<Patch>> debugInfo = getDebugInformation();
		
		if(ModelRenderer.getLayerToRender() < layers.size() && ModelRenderer.getLayerToRender() >= 0)
			for(Patch patch : layers.get(ModelRenderer.getLayerToRender())) 
				patch.drawDebug(DEBUG_COLOURS[0]);
	}
}
