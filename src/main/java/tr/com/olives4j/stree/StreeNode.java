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

/**
 * Defines the requirements for an object that can be used as a tree node in a
 * {@code Stree}.
 */
public abstract class StreeNode {
	/**
	 * Holds parent node
	 */
	private StreeGroup parent;
	/**
	 * Flag indicated this node excluded
	 */
	protected boolean exclude;

	/**
	 * Constructor
	 */
	public StreeNode() {
		super();
		exclude = false;
		parent = null;
	}

	/**
	 * Constructor
	 * 
	 * @param parent
	 */
	public StreeNode(StreeGroup parent) {
		super();
		this.exclude = parent.exclude;
		this.parent = parent;
	}

	/**
	 * 
	 * @return
	 */
	public StreeNode process() {
		return this;
	}

	/**
	 * 
	 * @param buffer
	 * @param sq
	 * @return
	 */
	public StreeNode merge(StringBuilder buffer) {
		return this;
	}

	/**
	 * 
	 * @return root node
	 */
	public StreeNode root() {
		StreeNode next = this;
		while (next.parent != null) {
			next = this.parent;
		}
		return next;
	}

	/**
	 * 
	 * @return parent node
	 */
	public StreeGroup parent() {
		return parent;
	}

	/**
	 * Set parent node
	 * 
	 * @return self
	 */
	public StreeGroup parent(StreeGroup parent) {
		this.parent = parent;
		return parent;
	}

	/**
	 * 
	 * @return if this node excluded
	 */
	public boolean isExclude() {
		return exclude;
	}

	/**
	 * 
	 * @param exclude
	 *            flag indicates if this node excluded
	 * 
	 * @return
	 */
	public StreeNode exclude(boolean exclude) {
		this.exclude = exclude;
		return this;
	}

	// Object implementation //////////////////////////////
	/**
	 * Return a copy of this node
	 */
	public StreeNode clone() {
		try {
			return (StreeNode) super.clone();
		} catch (CloneNotSupportedException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * 
	 */
	@Override
	public String toString() {
		return "StreeNode [parent=" + parent + ", exclude=" + exclude + "]";
	}
}