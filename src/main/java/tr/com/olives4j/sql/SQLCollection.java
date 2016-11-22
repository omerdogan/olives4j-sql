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
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 
 * @author omer.dogan
 * 
 */
public class SQLCollection implements Collection<SQL>, Cloneable {
	ArrayList<SQL> sqls = new ArrayList<SQL>();

	/**
	 * 
	 */
	public SQLCollection() {
	}

	/**
	 * 
	 * @param sqls
	 */
	public SQLCollection(Collection<SQL> sqls) {
		this.sqls.addAll(sqls);
	}

	/**
	 * 
	 * @param sqls
	 */
	public SQLCollection(SQLCollection sqls) {
		for (SQL sql : sqls) {
			add(sql.clone());
		}
	}

	/**
	 * 
	 */
	@Override
	public SQLCollection clone() {
		return new SQLCollection(this);
	}

	/**
	 * 
	 */
	@Override
	public Iterator<SQL> iterator() {
		return sqls.iterator();
	}

	/**
	 * Return the SQL collection as named map
	 * 
	 * @return
	 */
	public Map<String, SQL> asMap() {
		HashMap<String, SQL> map = new LinkedHashMap<String, SQL>();
		int i = 1;
		for (SQL sql : sqls) {
			String name = sql.name();
			if (name == null) {
				name = "SQL-" + i;
				i++;
			}
			SQL old = map.put(name, sql);
			if (old != null) {
				throw new RuntimeException("Could not convert sql collection to map, Dublicate keys found! Key:" + name);
			}
		}
		return map;
	}

	/**
	 * 
	 * @param sqlPart
	 * @param name
	 * @return
	 */
	public SQL get(String name) {
		for (SQL sql : this.sqls) {
			if (name.equals(sql.name())) {
				return sql;
			}
		}
		return null;
	}

	/**
	 * 
	 * @param sqlPart
	 * @param expression
	 * @return
	 */
	public SQL get(int index) {
		if (this.sqls instanceof List) {
			List<SQL> list = (List<SQL>) this.sqls;
			return list.get(index);
		}
		int i = 0;
		final Iterator<SQL> iterator = sqls.iterator();
		while (iterator.hasNext()) {
			final SQL next = iterator.next();
			if (i == index) {
				return next;
			}
			i++;
		}
		return null;
	}

	@Override
	public int size() {
		return sqls.size();
	}

	@Override
	public boolean isEmpty() {
		return sqls.isEmpty();
	}

	@Override
	public boolean contains(Object o) {
		return sqls.contains(o);
	}

	@Override
	public Object[] toArray() {
		return sqls.toArray();
	}

	@Override
	public <T> T[] toArray(T[] a) {
		return sqls.toArray(a);
	}

	@Override
	public boolean add(SQL e) {
		return sqls.add(e);
	}

	@Override
	public boolean remove(Object o) {
		return sqls.remove(o);
	}

	@Override
	public boolean containsAll(Collection<?> c) {
		return sqls.containsAll(c);
	}

	@Override
	public boolean addAll(Collection<? extends SQL> c) {
		return sqls.addAll(c);
	}

	@Override
	public boolean removeAll(Collection<?> c) {
		return sqls.removeAll(c);
	}

	@Override
	public boolean retainAll(Collection<?> c) {
		return sqls.retainAll(c);
	}

	@Override
	public void clear() {
		sqls.clear();
	}

	@Override
	public String toString() {
		return "SQLCollection(" + sqls.toString() + ")";
	}
}
