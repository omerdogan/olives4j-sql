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

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import tr.com.olives4j.sql.SQLBind.Set;

/**
 * 
 * @author omer.dogan
 *
 */
public class SQLBindings implements Collection<SQLBindNode> {
	/**
	 * Hold list of sql binding nodes
	 */
	List<SQLBindNode> nodes;

	/**
	 * 
	 */
	SQL sql;

	/**
	 * 
	 * @param sql
	 */
	public SQLBindings(SQL sql) {
		super();
		this.sql = sql;
		nodes = new ArrayList<SQLBindNode>();
	}

	/**
	 * Populate the named binding variables with given context
	 * 
	 * @param expression
	 * @param value
	 */
	public SQLBindings map(Object bean, SQLBind.Mapper mapper) {
		for (SQLBindNode sqlBind : nodes) {
			mapper.map(sqlBind, bean);
		}
		return this;
	}

	/**
	 * 
	 * @param expression
	 * @param value
	 */
	public SQLBind bind(int index, Object value) {
		if (index >= nodes.size()) {
			throw new RuntimeException("SQL has not any parameter with index : " + index);
		}
		SQLBindNode bind = nodes.get(index);
		bind.value(value);
		this.sql.setModified(true);
		return bind;
	}

	/**
	 * 
	 * @param name
	 * @param value
	 */
	public SQLBind bind(String name, Object value) {
		SQLBindNode target = null;
		Set set = null;

		for (int i = 0; i < nodes.size(); i++) {
			SQLBindNode next = nodes.get(i);
			if (next.name != null && next.name.equals(name)) {
				if (set != null) {
					set.add(next);
				} else if (target != null) {
					next.value(value);
					set = new Set();
					set.add(target);
					set.add(next);
				} else {
					next.value(value);
					target = next;
					this.sql.setModified(true);
				}
			}
		}

		if (target == null) {
			throw new RuntimeException("SQL has not any named parameter with the name : " + name);
		}
		if (set != null) {
			return set;
		}
		return target;
	}

	/**
	 * 
	 * @return
	 */
	public SQLBind get(int index) {
		SQLBindNode bind = nodes.get(index);
		if (bind.name == null) {
			return bind;
		}

		SQLBind set = get(bind.name);
		return set;
	}

	/**
	 * 
	 * @return
	 */
	public SQLBind get(String name) {
		ArrayList<SQLBindNode> findings = new ArrayList<SQLBindNode>();
		Set set = new Set();
		for (SQLBindNode sqlBind : nodes) {
			if (name.equals(sqlBind.name)) {
				set.add(sqlBind);
			}
		}
		return set;
	}

	/**
	 * 
	 * @param pstmt
	 * @throws SQLException
	 */
	public void apply(PreparedStatement pstmt) {
		try {
			Iterator<SQLBindNode> iterator = iterator();
			int i = 1;
			while (iterator.hasNext()) {
				SQLBindNode next = iterator.next();
				if (next.isExclude()) {
					continue;
				}
				Collection<?> values = next.extract();
				for (Object value : values) {
					pstmt.setObject(i, value);
					i++;
				}
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	// **************************************************************/
	// **************************************************************/

	/**
	 * 
	 * @return
	 */
	public Iterator<SQLBindNode> iterator() {
		Iterator<SQLBindNode> iterator = this.nodes.iterator();
		return iterator;
	}

	@Override
	public int size() {
		return nodes.size();
	}

	@Override
	public boolean isEmpty() {
		return nodes.isEmpty();
	}

	@Override
	public boolean contains(Object o) {
		return nodes.contains(o);
	}

	@Override
	public Object[] toArray() {
		return nodes.toArray();
	}

	@Override
	public <T> T[] toArray(T[] a) {
		return nodes.toArray(a);
	}

	@Override
	public boolean add(SQLBindNode e) {
		return nodes.add(e);
	}

	@Override
	public boolean remove(Object o) {
		return nodes.remove(o);
	}

	@Override
	public boolean containsAll(Collection<?> c) {
		return nodes.containsAll(c);
	}

	@Override
	public boolean addAll(Collection<? extends SQLBindNode> c) {
		return nodes.addAll(c);
	}

	@Override
	public boolean removeAll(Collection<?> c) {
		return nodes.removeAll(c);
	}

	@Override
	public boolean retainAll(Collection<?> c) {
		return nodes.retainAll(c);
	}

	@Override
	public void clear() {
		nodes.clear();
	}
}
