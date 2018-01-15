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
package tr.com.olives4j.sql;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import tr.com.olives4j.sql.util.SQLFormatter;
import tr.com.olives4j.stree.Stree;
import tr.com.olives4j.stree.StreeClause;
import tr.com.olives4j.stree.StreeGroup;
import tr.com.olives4j.stree.StreeIterator;
import tr.com.olives4j.stree.StreeNode;
import tr.com.olives4j.stree.StreeNodeMatcher;

/**
 * SQL is a tree like data structure to build sql statements. The main element
 * of this structure is {@code StreeNode} which are added by appropriate
 * {@code #append} methods. Sql statement is constructed by these nodes . Any
 * modification on data structure invalidate the internal buffer and force
 * reconstraction of sql.
 * 
 * *
 * <p>
 * The principal operations on a {@code SQL} is the {@code append} methods,
 * <br/>
 * which are overloaded so as to accept data in any type. Typically
 * 
 * @see Stree
 * @author omer.dogan
 * 
 */
public class SQL extends Stree {
	/** Iterator instance for traversing sql bind nodes **/
	private static final StreeNodeMatcher<SQLBindNode> BINDS = new StreeNodeMatcher<SQLBindNode>(SQLBindNode.class,
			false);
	/** Hold the SQL instance count **/
	private static final AtomicInteger instanceCounter = new AtomicInteger();
	/** Hold default options */
	public static final Options DEFAULT_OPTIONS = new Options();

	/**
	 * 
	 */
	SQLBindings bindings;

	/**
	 * 
	 */
	Options options = null;

	/**
	 * Construct empty sql
	 */
	public SQL() {
		this("SQL-" + instanceCounter.incrementAndGet(), DEFAULT_OPTIONS.clone());
	}

	/**
	 * Construct empty sql with the given options
	 */
	public SQL(Options options) {
		this("SQL-" + instanceCounter.incrementAndGet(), DEFAULT_OPTIONS.clone());
	}

	/**
	 * Construct empty sql with the given name
	 * 
	 * @param name
	 *            name of this instance
	 */
	public SQL(String name) {
		super(name);
		this.bindings = new SQLBindings(this);
		this.options = (Options) DEFAULT_OPTIONS.clone();
	}

	/**
	 * Construct empty sql with the given name and options
	 * 
	 * @param options
	 *            sql options
	 * @param name
	 *            name of this instance
	 */
	public SQL(String name, Options options) {
		super(name);
		this.options = options;
		this.bindings = new SQLBindings(this);
	}

	/**
	 * 
	 * @return new SQL instance with empty content
	 */
	public static SQL of() {
		return new SQL();
	}

	/**
	 * 
	 * @param exprs
	 *            variable number of sql nodes
	 * @return new SQL instance initiazed with the given nodes
	 */
	public static SQL of(Object... exprs) {
		SQL tree = new SQL();
		tree.append(exprs);
		return tree;
	}

	/**
	 * @return a copy of this sql instance
	 */
	public SQL clone() {
		SQL sql = (SQL) new SQL(this.options).name(name);
		for (int i = 0; i < this.nodes.size(); i++) {
			sql.append(this.nodes.get(i).clone());
		}

		return sql;
	}

	/**
	 * 
	 * @param node
	 *            target node
	 */
	@Override
	public StreeGroup append(StreeNode node) {
		if (node instanceof StreeGroup) {
			StreeIterator<SQLBindNode> it = ((StreeGroup) node).iterator(BINDS);
			while (it.hasNext()) {
				this.bindings.add(it.next());
			}
		} else if (node instanceof SQLBindNode) {
			this.bindings.add((SQLBindNode) node);
		}
		return super.append(node);
	}

	/**
	 * 
	 * @param query
	 *            A CharSequence holding SQL content
	 * @return SQL type instance representing the given SQL content
	 */
	@Override
	protected StreeNode parse(CharSequence query) {
		int length = query.length();
		boolean inSingleQuote = false;
		boolean inDoubleQuote = false;
		int lastIndex = 0;

		StringBuilder buffer = new StringBuilder();
		ArrayList<StreeNode> nodes = new ArrayList<StreeNode>(4);

		for (int i = 0; i < length; i++) {
			char c = query.charAt(i);
			if (inSingleQuote) {
				if (c == '\'') {
					inSingleQuote = false;
				}
			} else if (inDoubleQuote) {
				if (c == '"') {
					inDoubleQuote = false;
				}
			} else {
				if (c == '\'') {
					inSingleQuote = true;
				} else if (c == '"') {
					inDoubleQuote = true;
				} else if (c == ':' && i + 1 < length && Character.isJavaIdentifierStart(query.charAt(i + 1))) {
					int j = i + 2;
					while (j < length && Character.isJavaIdentifierPart(query.charAt(j))) {
						j++;
					}

					CharSequence before = query.subSequence(lastIndex, i);
					CharSequence name = query.subSequence(i + 1, j);
					c = '?';
					i += name.length();
					StreeClause cBefore = new StreeClause(before);
					SQLBindNode bind = new SQLBindNode().name(name.toString());

					nodes.add(cBefore);
					nodes.add(bind);
					lastIndex = j;
					buffer.setLength(0);
					continue;
				}
			}

			buffer.append(c);
		}

		if (buffer.length() > 0) {
			nodes.add(new StreeClause(buffer));
		}

		if (nodes.size() == 0) {
			return new StreeClause();
		} else if (nodes.size() == 1) {
			return nodes.get(0);
		} else {
			StreeGroup streeGroup = new StreeGroup(nodes);
			return streeGroup;
		}
	}

	/**
	 * 
	 * @return SQL string formatted and binding values placed
	 */
	public StringBuilder format() {
		String content = this.toString();
		List<Object> params = new ArrayList<Object>();
		Iterator<SQLBindNode> iterator = iterator(BINDS);
		while (iterator.hasNext()) {
			SQLBindNode next = (SQLBindNode) iterator.next();
			next.extract(params);
		}
		StringBuilder buffer = new StringBuilder();
		options.formatter.format(content, 0, params, buffer, options.keepFormat);
		return buffer;
	}

	// Getter/Setter //////////////////////////////

	/**
	 * 
	 * @return the parameter bindings
	 */
	public SQLBindings bindings() {
		return bindings;
	}

	/**
	 * 
	 * @return options
	 */
	public Options getOptions() {
		return options;
	}

	/**
	 * 
	 * @param options
	 *            to set
	 */
	public void setOptions(Options options) {
		this.options = options;
	}

	// Builder methods /////////////////////////////////////////

	/**
	 * shortcut to create SQLBindNode
	 * 
	 * @param var
	 *            first parameter
	 * @param vars
	 *            rest of the parameters
	 * @return SQLBind instance
	 */
	@SafeVarargs
	public static <T> SQLBind $(final T var, final T... vars) {
		return new SQLBindNode(var, vars);
	}

	// Inner classes /////////////////////////////////////////

	/**
	 * Hold SQL options
	 */
	static class Options implements Cloneable {
		boolean keepFormat = false;
		SQLFormatter formatter = SQLFormatter.INSTANCE;

		public Options() {
			super();
		}

		@Override
		protected Options clone() {
			try {
				return (Options) super.clone();
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
	}
}