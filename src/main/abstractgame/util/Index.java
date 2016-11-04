package abstractgame.util;

import java.util.*;

/** Represents an ID index that allocates IDs for new objects added */
public class Index<T extends Indexable> implements Iterable<T> {
	IDPool pool;
	List<T> lookup = new ArrayList<>();
	
	public Index(IDPool pool) {
		this.pool = pool;
	}
	
	public Index() {
		pool = new IDPool(0, Integer.MAX_VALUE);
	}
	
	public void add(T e) {
		assert !lookup.contains(e);
		
		int i = pool.get();
		e.setID(i);
		if(lookup.size() <= i)
			lookup.add(e);
		else
			lookup.set(i, e);
	}
	
	/** @return null if there is no entity regestered to that ID
	 * 
	 *  @param i The index to get */
	public T get(int i) {
		try {
			return lookup.get(i);
		} catch(IndexOutOfBoundsException e) {
			return null;
		}
	}
	
	/** NB this will not set e's ID to anything
	 * 
	 *  @param e The element to remove from the index */
	public void remove(T e) {
		assert lookup.get(e.getID()) == e;
		
		remove(e.getID());
	}

	@Override
	public Iterator<T> iterator() {
		return new ListIteratorNonNull<>(lookup);
	}

	public int getID(T ne) {
		return ne.getID();
	}

	public void remove(int id) {
		pool.release(id);
		lookup.set(id, null);
	}
}
