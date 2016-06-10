package abstractgame.util;

import java.util.*;

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
	
	/** @return null if the id is not bound to anything
	 * 
	 *  @param id The id to query */
	public T get(int id) {
		assert id >= 0;
		
		if(id >= list.size())
			return null;
		
		return list.get(id);
	}
	
	public int get(T t) {
		return t.getID();
	}

	@Override
	public Iterator<T> iterator() {
		return list.stream().filter(Objects::nonNull).iterator();
	}
}
