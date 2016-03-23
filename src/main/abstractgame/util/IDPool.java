package abstractgame.util;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Stack;

public class IDPool {
	public class OutOfIDsException extends ApplicationException {
		public OutOfIDsException() {
			super("Ran out of IDs, max: " + max, "ID POOL");
		}
	}

	Deque<Integer> stack = new ArrayDeque<>();
	int max;
	int counter;
	
	public IDPool(int min, int max) {
		assert max >= min;
		this.max = max;
		counter = min;
	}
	
	/** Creates a pool that uses the whole integer range */
	public IDPool() {
		this(Integer.MIN_VALUE, Integer.MAX_VALUE);
	}

	/** gets a new ID from the pool */
	public int get() {
		int next = stack.isEmpty() ? counter++ : stack.pop();
		if(next > max) throw new OutOfIDsException();
		return next;
	}
	
	/** Returns this ID to the pool */
	public void release(int id) {
		assert counter >= id && !stack.contains(id);
		stack.add(id);
	}
}
