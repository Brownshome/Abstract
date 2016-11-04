package abstractgame.util;

import java.util.*;

/** A list iterator that excludes null values */
public class ListIteratorNonNull<T> implements Iterator<T> {
		int next = -1;
		List<T> list;
		
		public ListIteratorNonNull(List<T> list) {
			this.list = list;
			findNext();
		}
		
		void findNext() {
			int i = next + 1;
			for(next = -1; i < list.size(); i++)
				if(list.get(i) != null)
					next = i;
		}
		
		@Override
		public boolean hasNext() {
			return next != -1;
		}

		@Override
		public T next() {
			T tmp;
			if(next == -1 || (tmp = list.get(next)) == null)
				throw new NoSuchElementException();
			findNext();
			return tmp;
		}
}
