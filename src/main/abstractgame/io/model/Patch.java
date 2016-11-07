package abstractgame.io.model;

import javax.vecmath.Vector3f;

import abstractgame.render.PhysicsRenderer;
import abstractgame.util.Util;
import java.util.*;

import static java.lang.Math.*;

public class Patch {
	Vector3f position;
	Vector3f normal;
	Collection<Patch> children;
	int numberOfChildren;
	float area;
	
	//only used while building the patch topography, this may contain vertexs wholy inside the patch
	Collection<Integer> vertexs;
	
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

	public Patch(Collection<Patch> children) {
		position = new Vector3f();
		normal = new Vector3f();
		vertexs = new HashSet<>();
		
		this.children = new ArrayList<>(children);
		
		for(Patch child : children) {
			vertexs.addAll(child.vertexs);
			area += child.area;
			normal.scaleAdd(child.area, child.normal, normal);
			position.scaleAdd(child.area, child.position, position);
			numberOfChildren += 1 + child.numberOfChildren;
		}
		
		position.scale(1 / area);
		normal.normalize();
	}
	
	public Patch(Vector3f normal, Vector3f position, float area, Collection<Integer> vertexs) {
		this.position = position;
		this.normal = normal;
		this.area = area;
		this.vertexs = vertexs;
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
	}

	public void addPatches(List<Patch> elements) {
		elements.add(this);
		if(children != null)
			for(Patch child : children) {
				child.addPatches(elements);
			}
	}
	
	@Override
	public String toString() {
		return String.format("Patch[ Area = %.3g, Children = %d ]", area, numberOfChildren);
	}
}
