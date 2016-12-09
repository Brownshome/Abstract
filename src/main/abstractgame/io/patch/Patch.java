package abstractgame.io.patch;

import abstractgame.io.model.*;
import abstractgame.render.PhysicsRenderer;
import abstractgame.util.Util;
import java.util.*;

import javax.vecmath.*;

import static java.lang.Math.*;

public class Patch {
	Vector3f position;
	Vector3f normal;
	
	float area;
	
	int layer = 0;
	int numberOfChildren;
	int triangleIndex;
	
	Collection<Patch> children;
	
	/** Used when building from disk */
	int parentIndex;
	boolean built = false;
	
	/** Only used in building the initial topography */
	Vector2f uv;
	int[] uvIndexs;
	
	public Vector3f getPosition() {
		return position;
	}
	
	/**
	 * @return the normal
	 */
	public Vector3f getNormal() {
		return normal;
	}

	/**
	 * @return the number of children that this patch has.
	 */
	public int getNumberOfChildren() {
		return numberOfChildren;
	}

	/**
	 * @return the area
	 */
	public float getArea() {
		return area;
	}

	/** Creates a blank patch */
	Patch() {
		
	}

	/** Constructs the patch using the data available, fields are left blank in the absence of data. */
	Patch(Map<String, Object> data, RawModel triangleSource) {
		children = new ArrayList<>();
		
		triangleIndex = (int) data.getOrDefault("triangle index", -1);
		
		if(triangleIndex != -1)
			extractDataFromTriangle(triangleSource.faces[triangleIndex]);
		
		if(data.containsKey("position"))
			position = Util.toVector3f((List<? extends Number>) data.get("position"));
		
		if(data.containsKey("normal"))
			normal = Util.toVector3f((List<? extends Number>) data.get("normal"));
		
		area = ((Number) data.getOrDefault("area", -1)).floatValue();
		parentIndex = ((Number) data.getOrDefault("parent index", -1)).intValue();
	}
	
	Patch(IndexedFace face, int triangleIndex) {
		this.triangleIndex = triangleIndex;
		
		extractDataFromTriangle(face);
	}
	
	void extractDataFromTriangle(IndexedFace face) {
		Vector3f a = face.getVertexPosition(0);
		Vector3f b = face.getVertexPosition(1);
		Vector3f c = face.getVertexPosition(2);
		
		normal = new Vector3f();
		normal.add(face.getVertexNormal(0));
		normal.add(face.getVertexNormal(1));
		normal.add(face.getVertexNormal(2));
		normal.scale(1f / 3);
		
		position = new Vector3f();
		position.add(a);
		position.add(b);
		position.add(c);
		position.scale(1f / 3);
		
		if(face.hasTextureCoords()) {
			uv = new Vector2f();
			uv.add(face.getTextureCoord(0));
			uv.add(face.getTextureCoord(1));
			uv.add(face.getTextureCoord(2));
			uv.scale(1f / 3);
		}
		
		Vector3f tmp = new Vector3f();
		tmp.sub(a, b);
		float ab = tmp.length();
		tmp.sub(b, c);
		float bc = tmp.length();
		tmp.sub(c, a);
		float ac = tmp.length();
		
		float s = ab + bc + ac;
		s /= 2;
		
		area = (float) Math.sqrt(s * (s - ab) * (s - bc) * (s - ac));
		uvIndexs = face.getUVIndexs();
		
		built = true;
	}
	
	/** Populates the position, normal and uv fields if they are not already populated.
	 * This method also sets the built flag and builds any unbuilt children. */
	void buildFromChildren() {
		if(built || children.isEmpty())
			return;
		
		for(Patch child : children) {
			if(!child.built)
				child.buildFromChildren();
		
			numberOfChildren += 1 + child.numberOfChildren;
		}
		
		if(area == -1) {
			area = 0;
			
			for(Patch child : children)
				area += child.area;
		}
		
		if(normal == null) {
			normal = new Vector3f();
			
			for(Patch child : children)
				normal.scaleAdd(child.area, child.normal, normal);
		
			normal.normalize();
		}
		
		if(position == null) {
			position = new Vector3f();
			
			for(Patch child : children)
				position.scaleAdd(child.area, child.position, position);
			
			position.scale(1 / area);
		}
		
		if(uv == null) {
			uv = new Vector2f();
			
			for(Patch child : children)
				uv.scaleAdd(child.area, child.uv, uv);
		
			uv.scale(1 / area);
		}
		
		built = true;
	}
	
	public void drawDebug(Vector3f colour) {
		Vector3f UP = new Vector3f(0, 1, 0);
		Vector3f RIGHT = new Vector3f(1, 0, 0);
		
		if(Math.abs(UP.dot(normal)) > Math.abs(RIGHT.dot(normal))) {
			UP.cross(RIGHT, normal);
			RIGHT.cross(UP, normal);
		} else {
			RIGHT.cross(UP, normal);
			UP.cross(RIGHT, normal);
		}
		
		UP.normalize();
		RIGHT.normalize();
		
		UP.scale((float) Math.sqrt(area));
		RIGHT.scale((float) Math.sqrt(area));
		
		Vector3f from = new Vector3f();
		from.scaleAdd(-.5f, UP, position);
		from.scaleAdd(-.5f, RIGHT, from);
		Vector3f to = new Vector3f(from);
		
		to.add(UP);
		PhysicsRenderer.INSTANCE.drawLine(from, to, colour);
		
		from.add(UP);
		from.add(RIGHT);
		PhysicsRenderer.INSTANCE.drawLine(from, to, colour);
		
		to.sub(UP);
		to.add(RIGHT);
		PhysicsRenderer.INSTANCE.drawLine(from, to, colour);
		
		from.sub(RIGHT);
		from.sub(UP);
		PhysicsRenderer.INSTANCE.drawLine(from, to, colour);
		
		PhysicsRenderer.INSTANCE.draw3dText(position, String.valueOf(numberOfChildren));
	}

	/** Adds all children of this patch in a heirachical method */
	void addPatchesTo(List<Patch> elements) {
		addPatches(elements, 0);
	}
	
	/** Adds all children and sets their 'layer' field accordingly */
	void addPatches(List<Patch> elements, int layer) {
		elements.add(this);
		this.layer = layer;
		
		if(children != null)
			for(Patch child : children) {
				child.addPatches(elements, layer + 1);
			}
	}
	
	@Override
	public String toString() {
		return String.format("Patch[ Area = %.3g, Children = %d ]", area, numberOfChildren);
	}

	public int getLayer() {
		return layer;
	}

	public int getTriangle() {
		return triangleIndex;
	}

	public Collection<Patch> children() {
		return children;
	}

	public void children(Collection<Patch> children) {
		this.children = children;
	}
}
