package abstractgame.io.model;

import java.util.*;
import java.util.Map.Entry;

import javax.vecmath.*;

import abstractgame.render.ModelRenderer;

class TopologyGenerator {
	List<Collection<Patch>> layers = new ArrayList<>();
	
	RawModel model;
	
	Comparator<Patch> uComparator = (a, b) -> (int) (a.uv.x - b.uv.x);
	Comparator<Patch> vComparator = (a, b) -> (int) (a.uv.y - b.uv.y);
	
	TopologyGenerator(RawModel model) {
		this.model = model;
	}
	
	void generateLayers() {
		for(int layer = 1; layer < layers.size() + 1; layer++) {
			for(Patch patch : layers.get(layer - 1)) {
				if(patch.children == null)
					continue;
					
				if(layer == layers.size())
					layers.add(new ArrayList<>());
					
				layers.get(layers.size() - 1).addAll(patch.children);
			}
		}
	}
	
	List<Patch> generate() {
		//Create large patches for each uv segment
		//For each of these patches, sibdivide along the median of the patches, storing the result in the parent
	
		//Create the base patches
		
		//flood fill from a random point
		//remove from set
		
		Collection<Patch> segments = new ArrayList<>();
		Set<Patch> toBeSorted = new HashSet<>(model.faces.length);
		
		Map<Integer, List<Patch>> vertexConnectivity = new HashMap<>();
		
		for(Face face : model.faces) {
			Patch p = createPatch(face, model);
			toBeSorted.add(p);
			
			vertexConnectivity.computeIfAbsent(face.t1, v -> new ArrayList<>()).add(p);
			vertexConnectivity.computeIfAbsent(face.t2, v -> new ArrayList<>()).add(p);
			vertexConnectivity.computeIfAbsent(face.t3, v -> new ArrayList<>()).add(p);
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
		List<Patch> patches = new ArrayList<>();
		for(Patch segment : segments)
			segment.addPatches(patches);
		
		layers.add(segments);
		
		return patches;
	}
	
	/** Splits the patches by their u values, this is the first step in splitting. */
	void subdivideByU(List<Patch> list, Patch parent) {
		if(list.size() <= 4) {
			parent.children = list;
			parent.build();
			return;
		}
		
		list.sort(uComparator);
		
		parent.children = new ArrayList<>();
		subdivideByV(list.subList(0, list.size() / 2), parent);
		subdivideByV(list.subList(list.size() / 2, list.size()), parent);
		parent.build();
	}
	
	/** Splits the patches by their v values, this is the last step and is when the patches are addded. */
	void subdivideByV(List<Patch> list, Patch parent) {
		Patch a = new Patch();
		Patch b = new Patch();
		
		list.sort(vComparator);
		
		parent.children.add(a);
		parent.children.add(b);
		
		subdivideByU(list.subList(0, list.size() / 2), a);
		subdivideByU(list.subList(list.size() / 2, list.size()), b);
	}
	
	Patch createPatch(Face face, RawModel model) {
		Vector3f a = model.vertexs[face.v1 - 1];
		Vector3f b = model.vertexs[face.v2 - 1];
		Vector3f c = model.vertexs[face.v3 - 1];
		
		Vector3f normal = new Vector3f();
		normal.add(model.normals[face.n1 - 1]);
		normal.add(model.normals[face.n2 - 1]);
		normal.add(model.normals[face.n3 - 1]);
		normal.scale(1f / 3);
		
		Vector3f position = new Vector3f();
		position.add(a);
		position.add(b);
		position.add(c);
		position.scale(1f / 3);
		position.scaleAdd(-0.001f, normal, position); //inset each face to avoid artifacts from self shadowing
		
		Vector2f uv = new Vector2f();
		uv.add(model.textureCoordinates[face.t1 - 1]);
		uv.add(model.textureCoordinates[face.t2 - 1]);
		uv.add(model.textureCoordinates[face.t3 - 1]);
		uv.scale(1f / 3);
		
		Vector3f tmp = new Vector3f();
		tmp.sub(a, b);
		float ab = tmp.length();
		tmp.sub(b, c);
		float bc = tmp.length();
		tmp.sub(c, a);
		float ac = tmp.length();
		
		float s = ab + bc + ac;
		s /= 2;
		
		float area = (float) Math.sqrt(s * (s - ab) * (s - bc) * (s - ac));
		
		return new Patch(normal, position, uv, new int[] {face.t1, face.t2, face.t3}, area, Arrays.asList(face.v1 - 1, face.v2 - 1, face.v3 - 1));
	}
}
