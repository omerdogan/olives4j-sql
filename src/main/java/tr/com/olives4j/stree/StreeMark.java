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
import java.util.List;

/**
 * 
 * @author omer.dogan
 *
 */
public final class StreeMark extends StreeNode {
	/**
	 * 
	 */
	private final String name;
	/**
	 * 
	 */
	private List<StreeNode> markedNodes = new ArrayList<StreeNode>();

	/**
	 * 
	 * @param name
	 */
	public StreeMark(String name) {
		super();
		this.name = name;
	}

	/**
	 * 
	 * @param name
	 * @return
	 */
	public static StreeMark create(String name) {
		return new StreeMark(name);
	}

	/**
	 * 
	 */
	@Override
	public StreeMark process() {
		if (!this.exclude) {
			return this;
		}

		Iterator<StreeNode> nodes = null;
		if (this.parent() != null) {
			nodes = ((StreeGroup) this.parent()).nodes.iterator();
		} else {
			return this;
		}

		StreeNode prev = null;

		while (nodes.hasNext()) {
			StreeNode next = nodes.next();
			if (!(prev instanceof StreeMark)) {
				prev = next;
				continue;
			}
			if (prev != null && prev == this) {
				this.markedNodes.add(next);
				processMarkedNode(next);
			}
			prev = next;
		}
		return this;
	}

	/**
	 * 
	 * @param node
	 */
	protected void processMarkedNode(StreeNode node) {
		if (this.exclude) {
			node.exclude(exclude);
		}
	}

	/**
	 * 
	 * @return
	 */
	public List<StreeNode> getMarkedNodes() {
		return markedNodes;
	}

	/**
	 * 
	 * @return
	 */
	public String getName() {
		return name;
	}

	@Override
	public String toString() {
		return "SQLMark [name=" + name + ", marked=" + markedNodes + "]";
	}
}