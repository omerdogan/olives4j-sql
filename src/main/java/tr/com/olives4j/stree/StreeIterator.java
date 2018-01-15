/*******************************************************************************
 *   Copyright (c) 2016, Omer Dogan.  All rights reserved.
 *  
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *  
 *   http://www.apache.org/licenses/LICENSE-2.0
 *  
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 *    
 *******************************************************************************/
package tr.com.olives4j.stree;

import java.util.Iterator;

import tr.com.olives4j.sql.util.Functionals.Predicate;

/**
 * An Iterator walks all nodes for the given {@link StreeGroup}
 * 
 * @author omer.dogan
 *
 * @param <T>
 */
public final class StreeIterator<T extends StreeNode> implements Iterator<T> {
	/** Holds the current position on the current parent node **/
	int index = 0;
	/** Holds root node for iteration **/
	StreeNode root;
	/** Holds the current parent node in the tree **/
	StreeNode currentParent;
	/** Filter to select specific nodes **/
	Predicate<StreeNode> selector;
	/****/
	boolean isStreeGroup;

	/**
	 * 
	 */
	public StreeIterator(StreeNode parent, Predicate<StreeNode> selector) {
		super();
		this.root = parent;
		this.currentParent = parent;
		this.selector = selector;
		this.isStreeGroup = parent instanceof StreeGroup;
	}

	/**
	 * 
	 * @return
	 */
	@Override
	public boolean hasNext() {
		return isStreeGroup && alter((StreeGroup) currentParent, this.index) != null;
	}

	/**
	 * 
	 */
	@SuppressWarnings("unchecked")
	public T next() {
		Path path = alter((StreeGroup) currentParent, this.index);
		this.index = path.index;
		this.currentParent = path.parent;
		StreeNode next = ((StreeGroup) currentParent).getNodes().get(index);
		this.index++;
		return (T) next;
	}

	/**
	 * 
	 * @return
	 */
	private Path alter(StreeGroup parent, int index) {
		Path path = new Path(parent, index);
		int size = parent.size();
		for (int i = index; i < size; i++) {
			path.index = i;
			StreeNode next = parent.getNodes().get(i);

			if (next instanceof StreeGroup) {
				Path path2 = alter((StreeGroup) next, 0);
				if (path2 != null) {
					return path2;
				}
			} else if (selector == null) {
				return path;
			} else if (selector.test(next)) {
				return path;
			}
		}

		if (parent != root) {
			int currentIndex = parent.parent().getNodes().indexOf(parent);
			path = alter(parent.parent(), currentIndex + 1);
			return path;
		}

		return null;
	}

	/**
	 * Travers all nodes and apply given {@link Consumer}
	 */
	public void foreach(Consumer<T> f) {
		while (hasNext()) {
			T next = next();
			f.apply(next);
		}
	}

	/**
	 * Represents an operation that accepts a single input argument and returns no
	 * result.
	 * 
	 * @param <T>
	 */
	public static abstract class Consumer<T> {
		public abstract void apply(T t);
	}

	/**
	 * 
	 */
	class Path {
		StreeGroup parent;
		int index;

		public Path(StreeGroup parent, int index) {
			super();
			this.parent = parent;
			this.index = index;
		}
	}

	@Override
	public void remove() {
		throw new UnsupportedOperationException("remove");
	}
}