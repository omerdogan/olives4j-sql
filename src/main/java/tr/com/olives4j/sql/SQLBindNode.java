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
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;

import tr.com.olives4j.stree.StreeGroup;
import tr.com.olives4j.stree.StreeIterator;
import tr.com.olives4j.stree.StreeNode;
import tr.com.olives4j.stree.StreeNodeMatcher;

/**
 * An SQL node holds a binding definition
 */
public class SQLBindNode extends StreeNode implements SQLBind {
	/** Name of this binding **/
	public String name;
	/** Value of this binding **/
	private Object value;
	/** Default value **/
	private Object defaultValue;
	/** Holds if this binding is optional **/
	private boolean optional;
	/** Holds if this binding is inlined into the final sql query **/
	private boolean inline;
	/** Hold the jdbc type **/
	private Integer jdbcType;
	/** Holds the seperator characters. Default is ',' character */
	private String seperator = ",";

	/**
	 * Construct SQLBindNode with default properties
	 */
	public SQLBindNode() {
		super();
	}

	/**
	 * Construct SQLBindNode with the given parameters
	 * 
	 * @param var
	 *            first parameter
	 * @param vars
	 *            rest of the parameter if exists
	 */
	@SafeVarargs
	public <T> SQLBindNode(T var, T... vars) {
		value(var, vars);
	}

	/**
	 * @param name
	 *            name of this binding
	 * @param var
	 *            first parameter
	 * @param vars
	 *            rest of the parameter if exists
	 */
	@SafeVarargs
	public <T> SQLBindNode(String name, T var, T... vars) {
		name(name);
		value(var, vars);
	}

	/**
	 * 
	 * @param it
	 *            target nodes iterator
	 */
	public <T> SQLBindNode(Iterable<T> it) {
		value(it);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see tr.com.olives4j.sql.SQLBind#name(java.lang.String)
	 */
	@Override
	public SQLBindNode name(String name) {
		this.name = name;
		return this;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see tr.com.olives4j.sql.SQLBind#jdbcType(java.lang.Integer)
	 */
	@Override
	public SQLBind jdbcType(Integer type) {
		this.jdbcType = type;
		return this;
	}

	/**
	 * 
	 * @see tr.com.olives4j.sql.SQLBind#jdbcType()
	 */
	public Integer jdbcType() {
		return this.jdbcType;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see tr.com.olives4j.sql.SQLBind#jdbcType(java.lang.Integer)
	 */
	@Override
	public SQLBind inline(boolean inline) {
		this.inline = inline;
		return this;
	}

	/**
	 * 
	 * @see tr.com.olives4j.sql.SQLBind#
	 */
	public boolean isInline() {
		return this.inline;
	}

	/**
	 * 
	 * @param vars
	 */
	@SuppressWarnings("unchecked")
	public <T> SQLBind value(T var, T... vars) {
		if (vars.length == 0) {
			this.value = var;
			return this;
		}

		ArrayList<T> list = new ArrayList<T>(vars.length + 1);
		list.add(var);
		list.addAll(Arrays.asList(vars));
		this.value = list;
		return this;
	}

	/**
	 * 
	 * @param vars
	 */
	public SQLBind value(Iterable<?> vars) {
		ArrayList<Object> list = new ArrayList<Object>();
		Iterator<?> iterator = vars.iterator();
		while (iterator.hasNext()) {
			Object next = iterator.next();
			list.add(next);
		}

		this.value = list;
		return this;
	}

	/**
	 * 
	 * @return
	 */
	public Object value() {
		if (optional) {
			return defaultValue;
		}

		return value;
	}

	/**
	 * 
	 * @param param
	 * @return
	 */
	public SQLBind defaultValue(Object param) {
		this.defaultValue = param;
		return this;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see tr.com.olives4j.sql.SQLBind#optional()
	 */
	@Override
	public SQLBindNode optional() {
		this.optional = true;
		return this;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see tr.com.olives4j.sql.SQLBind#optional(Object defaultValue)
	 */
	@Override
	public SQLBindNode optional(Object defaultValue) {
		this.optional = true;
		return this;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see tr.com.olives4j.sql.SQLBind#required()
	 */
	@Override
	public SQLBindNode required() {
		this.optional = false;
		return this;
	}

	@Override
	public boolean isExclude() {
		return exclude || (this.optional && this.defaultValue == null && checkNull());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see tr.com.olives4j.sql.SQLBind#seperator(java.lang.String)
	 */
	@Override
	public SQLBind seperator(String seperator) {
		this.seperator = seperator;
		return this;
	}

	/**
	 * 
	 * @return
	 */
	public Collection<Object> extract() {
		return extract(new ArrayList<Object>());
	}

	/**
	 * 
	 */
	public Collection<Object> extract(Collection<Object> c) {
		Object targetValue = value();

		if (targetValue == null) {
			c.add(null);
		} else if (targetValue.getClass().isArray()) {
			c.addAll(Arrays.asList((Object[]) targetValue));
		} else if (targetValue instanceof Collection) {
			c.addAll((Collection<?>) targetValue);
		} else {
			c.add(targetValue);
		}
		return c;
	}

	/**
	 * 
	 * @return
	 */
	@Override
	public SQLBindNode exclude(boolean exclude) {
		super.exclude(exclude);
		if (this.parent() != null && (!(this.parent() instanceof SQL) && this.parent() instanceof StreeGroup)) {
			StreeIterator<SQLBindNode> walker = new StreeIterator<SQLBindNode>(parent(),
					StreeNodeMatcher.of(SQLBindNode.class, false));
			if (!walker.hasNext()) {
				this.parent().exclude(exclude);
			}
		} else {
			StreeNode prev = null;
			Iterator<StreeNode> nodes = ((StreeGroup) root()).iterator(null);
			while (nodes.hasNext()) {
				StreeNode next = nodes.next();
				if (next == this && prev != null) {
					prev.exclude(exclude);
				}
				prev = next;
			}
		}

		return this;
	}

	/**
	 * 
	 * @return
	 */
	public StreeNode process() {
		if (this.optional && this.defaultValue == null) {
			if (checkNull()) {
				this.exclude(true);
			}
		} else if (this.exclude) {
			this.exclude(true);
		}
		return this;
	}

	/**
	 * 
	 * @return
	 */
	public boolean checkNull() {
		if (value == null) {
			return true;
		} else if (value.getClass().isArray() && ((Object[]) value).length == 1 && ((Object[]) value)[0] == null) {
			return true;
		} else if (value instanceof Collection) {
			return ((Collection<?>) value).size() > 0;
		} else if (value instanceof Iterable) {
			return ((Iterable<?>) value).iterator().hasNext();
		}
		return false;
	}

	/**
	 * 
	 */
	@Override
	protected StreeNode merge(StringBuilder buffer) {
		if (this.isExclude()) {
			return this;
		}

		super.merge(buffer);

		Object targetValue = value();

		boolean isArray = targetValue != null && targetValue.getClass().isArray();
		boolean isIterable = targetValue != null && targetValue instanceof Iterable;

		if (buffer.length() == 0 || !Character.isWhitespace(buffer.charAt(buffer.length() - 1))) {
			buffer.append(" ");
		}

		Iterable<?> ita = null;
		if (isArray) {
			ita = Arrays.asList((Object[]) targetValue);
		} else if (isIterable) {
			ita = (Iterable<?>) targetValue;
		} else {
			buffer.append("(?)");
			return this;
		}

		Iterator<?> it = ita.iterator();

		buffer.append("(");
		if (!it.hasNext()) {
			buffer.append("null").append(seperator);
		} else {
			while (it.hasNext()) {
				it.next();
				buffer.append("?").append(seperator);
			}
		}
		buffer.setLength(buffer.length() - 1);
		buffer.append(")");

		return this;
	}

	/**
	 * 
	 */
	@Override
	public String getName() {
		return name;
	}

	/**
	 * 
	 */
	@Override
	public boolean isExcluded() {
		return exclude || (this.optional && this.defaultValue == null && checkNull());
	}

	/**
	 * 
	 */
	@Override
	public boolean isOptional() {
		return optional;
	}

	/**
	 * 
	 */
	@Override
	public Object getDefaultValue() {
		return defaultValue;
	}

	/**
	 * 
	 */
	@Override
	public StreeNode clone() {
		return new SQLBindNode(name, value);
	}

	/**
	 * 
	 */
	@Override
	public String toString() {
		String val = null;
		if (value == null) {
			val = "null";
		} else {
			boolean isArray = value.getClass().isArray();
			if (isArray && ((Object[]) value).length == 0) {
				val = "null";
			} else if (value.getClass().isArray() && ((Object[]) value)[0] == null) {
				val = "null";
			} else if (value.getClass().isArray()) {
				val = Arrays.asList((Object[]) value).toString();
			} else {
				val = Arrays.asList(value).toString();
			}

		}
		return "SQLBind [name=" + name + ", value=" + val + ", default:" + defaultValue + ", optional=" + optional
				+ ", exlude=" + exclude + "]";
	}

}