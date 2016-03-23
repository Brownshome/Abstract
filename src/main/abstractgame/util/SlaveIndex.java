package abstractgame.util;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import abstractgame.world.entity.NetworkEntity;

public class SlaveIndex<T extends Indexable> implements Iterable<T> {
	ArrayList<T> list = new ArrayList<>();
	
	public void put(int id, T e) {
		e.setID(id);
		
		if(list.size() <= id) {
			for(int i = id - list.size(); i >= 0; i--)
				list.add(null);
		}
		
		list.set(id, e);
	}
	
	/** @return null if the id is not bound to anything */
	public T get(int id) {
		assert id < list.size() && list.get(id) != null;
		
		try {
			return list.get(id);
		} catch(IndexOutOfBoundsException ioobe) {
			return null;
		}
	}
	
	public int get(T t) {
		return t.getID();
	}

	@Override
	public Iterator<T> iterator() {
		return list.iterator();
	}
}
