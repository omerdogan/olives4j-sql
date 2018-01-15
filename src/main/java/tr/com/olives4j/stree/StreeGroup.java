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

import tr.com.olives4j.sql.util.Functionals.Predicate;

/**
 * Group which containing linked sub nodes
 */
public class StreeGroup extends StreeNode {
	/**
	 * Holds list of child nodes
	 */
	protected List<StreeNode> nodes;

	/**
	 * 
	 */
	public StreeGroup() {
		this.nodes = new ArrayList<StreeNode>();
	}

	/**
	 * 
	 * @param nodes
	 */
	public <T extends StreeNode> StreeGroup(List<T> nodes) {
		this();
		append(nodes.toArray());
	}

	/**
	 * 
	 * @return a new Stree instance with empty content
	 */
	public static Stree of() {
		return new Stree();
	}

	/**
	 * 
	 * @return a new Stree instance with the given content
	 */
	public static Stree of(Object... exprs) {
		Stree tree = new Stree();
		tree.append(exprs);
		return tree;
	}

	/**
	 * append given
	 * 
	 */
	public StreeGroup append(Object... nodes) {
		for (int i = 0; i < nodes.length; i++) {
			StreeNode expr = parse(nodes[i]);
			append(expr);
		}

		return this;
	}

	/**
	 * append given
	 * 
	 */
	public StreeGroup append(Iterable<?> nodes) {
		Iterator<?> iterator = nodes.iterator();
		while (iterator.hasNext()) {
			StreeNode expr = parse(iterator.next());
			append(expr);
		}

		return this;
	}

	/**
	 * 
	 * @param node
	 */
	public StreeGroup append(StreeNode node) {
		this.nodes.add(node);
		node.parent(this);
		return this;
	}

	/**
	 * 
	 * @param part
	 * @return
	 */
	protected StreeNode parse(Object part) {
		StreeNode expr = null;
		Object next = part;
		if (next instanceof StreeNode) {
			expr = ((StreeNode) next);
		} else if (next instanceof StreeGroup) {
			expr = new StreeGroup().append((StreeGroup) next);
		} else if (next == null) {
			expr = new StreeClause("null");
		} else {
			expr = parse(next.toString());
		}
		return expr;
	}

	/**
	 * 
	 * @param part
	 * @return
	 */
	protected StreeNode parse(CharSequence part) {
		return new StreeClause(part);
	}

	/*
	 * (non-Javadoc)
	 * 
	 */
	@Override
	public StreeNode merge(StringBuilder buffer) {
		if (this.isExclude()) {
			return this;
		}
		super.merge(buffer);
		for (StreeNode clause : nodes) {
			clause.merge(buffer);
		}

		return this;
	}

	@Override
	public StreeNode process() {
		for (StreeNode node : this.nodes) {
			node.process();
		}
		return this;
	}

	/**
	 * 
	 * @return
	 */
	public StreeIterator<StreeNode> iterator() {
		StreeIterator<StreeNode> nodeWalker = new StreeIterator<StreeNode>(this, null);
		return nodeWalker;
	}

	/**
	 * 
	 * @param selector
	 * @return
	 */
	public <T extends StreeNode> StreeIterator<T> iterator(Predicate<StreeNode> selector) {
		return new StreeIterator<T>(this, selector);
	}

	// SETTTER/GETTER ///////////////////////////////////////////////

	/**
	 * @param sql
	 *            the sql to set
	 */
	public void setContent(String sql) {
		this.nodes = new ArrayList<StreeNode>();
		append(sql);
	}

	/**
	 * 
	 * @return
	 */
	public List<StreeNode> getNodes() {
		return nodes;
	}

	/**
	 * 
	 * @return
	 */
	public int size() {
		return nodes == null ? 0 : nodes.size();
	}

	/**
	 * 
	 */
	public StreeNode clone() {
		StreeGroup clause = new StreeGroup();
		for (int i = 0; i < this.nodes.size(); i++) {
			clause.append(nodes.get(i).clone());
		}

		return clause;
	}

	/**
	 * 
	 */
	public String toString() {
		StringBuilder buffer = new StringBuilder();
		buffer.append("StreeGroup[");
		for (int i = 0; i < nodes.size(); i++) {
			buffer.append(nodes.get(i).toString()).append(",");
		}
		buffer.append("]");
		return buffer.toString();
	}
}