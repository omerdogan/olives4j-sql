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

import java.util.ArrayList;
import java.util.Iterator;

/**
 * Stree is a tree like data structure to construct structured text. Stree is
 * like {@link StringBuilder} wich is used to construct string, in constrast
 * {@code StreeNode} is used to construct textual context like sql. <br/>
 * Main element of Stree structure is {@code StreeNode}.
 * <p>
 * The principal operations on a {@code Stree} is the {@code append} methods,
 * which are overloaded so as to accept {@code StreeNode} types, For the
 * convenience these methods also accept any type which are converted to
 * {@link StreeNode}
 * 
 * @author omer.dogan
 * 
 */
public class Stree extends StreeGroup {
	private static final StreeNodeMatcher<StreeAnnotation> ANNOTATIONS = new StreeNodeMatcher<StreeAnnotation>(
			StreeAnnotation.class, false);

	/**
	 * Hold the descriptor name
	 */
	protected String name;

	/**
	 * Constructor
	 */
	public Stree() {
		this(null);
	}

	/**
	 * Constructor
	 * 
	 * @param name
	 *            descriptor name
	 */
	public Stree(String name) {
		super();
		this.name = name;
		this.nodes = new ArrayList<StreeNode>();
	}

	/**
	 * @return new Stree instance which is clone of this instance
	 */
	public Stree clone() {
		Stree stree = new Stree(this.name);
		stree.append(this.nodes);
		return stree;
	}

	/**
	 * @return the name
	 */
	public String name() {
		if (name == null) {
			return "SQL-" + hashCode();
		}
		return name;
	}

	/**
	 * @param name
	 *            the name to set
	 */
	public Stree name(String name) {
		this.name = name;
		return this;
	}

	/**
	 * 
	 * @param stree
	 * @param unionType
	 * @return
	 */
	public Stree concat(Stree stree, String unionType) {
		Stree clone = stree.clone();
		append(unionType);
		append(clone);
		return this;
	}

	// SETTTER/GETTER ///////////////////////////////////////////////

	/**
	 * @return the annotations
	 */
	public Iterator<StreeAnnotation> getAnnotations() {
		Iterator<StreeAnnotation> iterator = iterator(ANNOTATIONS);
		return iterator;
	}

	/*
	 * (non-Javadoc)
	 * 
	 */
	@Override
	public StreeNode merge(StringBuilder buffer) {
		for (StreeNode clause : nodes) {
			clause.merge(buffer);
		}

		return this;
	}

	// Object implementation //////////////////////////////
	/**
	 * 
	 */
	@Override
	public String toString() {
		StringBuilder buffer = new StringBuilder();
		merge(buffer);
		return buffer.toString();
	}

}