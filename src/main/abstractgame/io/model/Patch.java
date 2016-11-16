package abstractgame.io.model;

import abstractgame.render.PhysicsRenderer;
import abstractgame.util.Util;
import java.util.*;

import javax.vecmath.*;

import static java.lang.Math.*;

public class Patch {
	Vector3f position;
	Vector3f normal;
	Vector2f uv;
	
	/** Only used in building the initial topography */
	int[] uvIndexs;
	
	Collection<Patch> children;
	int numberOfChildren;
	float area;
	int layer = 0;
	
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
	 * @return the numberOfChildren
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
	
	Patch(Vector3f normal, Vector3f position, Vector2f uv, int[] uvIndexs, float area, Collection<Integer> vertexs) {
		this.position = position;
		this.normal = normal;
		this.area = area;
		this.uv = uv;
		this.uvIndexs = uvIndexs;
	}

	void build() {
		position = new Vector3f();
		normal = new Vector3f();
		uv = new Vector2f();
		
		for(Patch child : children) {
			area += child.area;
			normal.scaleAdd(child.area, child.normal, normal);
			position.scaleAdd(child.area, child.position, position);
			uv.scaleAdd(child.area, child.uv, uv);
			numberOfChildren += 1 + child.numberOfChildren;
		}
		
		position.scale(1 / area);
		uv.scale(1 / area);
		normal.normalize();
	}
	
	public void drawDebug(Vector3f colour) {
		Vector3f UP = new Vector3f(0, 1, 0);
		Vector3f RIGHT = new Vector3f(1, 0, 0);
		
		if(Math.abs(UP.dot(normal)) < Math.abs(RIGHT.dot(normal))) {
			UP.cross(RIGHT, normal);
			RIGHT.cross(UP, normal);
		} else {
			RIGHT.cross(UP, normal);
			UP.cross(RIGHT, normal);
		}
		
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
	void addPatches(List<Patch> elements) {
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
}
