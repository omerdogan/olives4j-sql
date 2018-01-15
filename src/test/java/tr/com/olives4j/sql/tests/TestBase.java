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
package tr.com.olives4j.sql.tests;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.junit.Assert;

import tr.com.olives4j.sql.SQL;
import tr.com.olives4j.sql.SQLBind;
import tr.com.olives4j.stree.StreeGroup;
import tr.com.olives4j.stree.StreeIterator;
import tr.com.olives4j.stree.StreeNode;

public class TestBase {
	Logger logger = Logger.getLogger(TestBase.class);

	public TestBase() {
		super();
	}

	public static class QueryCheck {
		public int numOfParams;
		public int numOfBindings;
	}

	public static class BindCheck {
		public int numOfValues;
		public boolean optional;
		public Object defaultValue;
		public boolean excluded;
		public Integer excludedNode;
	}

	public void debugQuery(SQL sql, int num) {
		StringBuilder buffer = new StringBuilder();
		buffer.append("\n\n" + num + ".1 query :\n" + sql.toString());
		buffer.append("\n\n" + num + ".2 formatted :\n" + sql.format());
		buffer.append("\n\n" + num + ".3 Bindings	:\n" + lines(sql.bindings()));
		logger.debug(buffer);
	}

	public void checkBind(SQL sql, int bindIndex, int numOfValues, boolean optional, boolean excluded,
			Integer excludedClauseIndex, Object defaultValue, Object value) {
		checkBind(sql, sql.bindings().get(bindIndex), numOfValues, optional, excluded, excludedClauseIndex,
				defaultValue, value);
	}

	public void checkBind(SQL sql, String bindName, int numOfValues, boolean optional, boolean excluded,
			Integer excludedClauseIndex, Object defaultValue, Object value) {
		checkBind(sql, sql.bindings().get(bindName), numOfValues, optional, excluded, excludedClauseIndex, defaultValue,
				value);
	}

	public void checkBind(SQL sql, SQLBind bind, int numOfValues, boolean optional, boolean excluded,
			Integer excludedClauseIndex, Object defaultValue, Object value) {
		sql.process();
		Assert.assertEquals(bind.extract().size(), numOfValues);
		Assert.assertEquals(bind.isOptional(), optional);
		Assert.assertEquals(bind.getDefaultValue(), defaultValue);
		Assert.assertEquals(bind.isExcluded(), excluded);

		if (excludedClauseIndex != null) {
			Assert.assertTrue(sql.getNodes().get(excludedClauseIndex).isExclude());
		}
	}

	/**
	 * 
	 * @param list
	 * @return
	 */
	public static String lines(Iterable<? extends Object> list) {
		return lines(list.iterator());
	}

	/**
	 * 
	 * @param list
	 * @return
	 */
	public static String lines(Iterator<? extends Object> list) {
		StringBuilder buffer = new StringBuilder();
		if (list == null) {
			return null;
		}
		int i = 1;
		while (list.hasNext()) {
			Object object = list.next();
			if (object != null && object.getClass().isArray()) {
				buffer.append(i).append(". ").append(Arrays.asList((Object[]) object)).append("\n");
			} else {
				buffer.append(i).append(". ").append(object).append("\n");
			}
			i++;
		}
		return buffer.toString();
	}

	public static class Param {
		String tableName;

		public Param(String value) {
			super();
			this.tableName = value;
		}
	}

	public static int countChar(String s, char ch) {
		int counter = 0;
		for (int i = 0; i < s.length(); i++) {
			if (s.charAt(i) == ch) {
				counter++;
			}
		}
		return counter;
	}

	@SuppressWarnings("rawtypes")
	public static int size(Iterator it) {
		int counter = 0;
		while (it.hasNext()) {
			it.next();
			counter++;
		}
		return counter;
	}

	public static <T> List<T> toList(Iterator<T> it) {
		ArrayList<T> list = new ArrayList<T>();
		while (it.hasNext()) {
			list.add(it.next());
		}
		return list;
	}

	private static StringBuilder tabs = new StringBuilder();
	{
		for (int i = 0; i < 100; i++) {
			tabs.append("\t\t\t\t\t\t\t\t\t\t");
		}
	}

	public String toTree(StreeGroup sql, int deep) {
		StringBuilder buffer = new StringBuilder();
		buffer.append(tabs.substring(0, deep) + "SQL#" + sql.hashCode());
		StreeIterator<StreeNode> iterator = sql.iterator();
		buffer.append("\n");
		while (iterator.hasNext()) {
			StreeNode next = iterator.next();
			if (next instanceof StreeGroup) {
				buffer.append(toTree((StreeGroup) next, deep + 1));
			} else {
				buffer.append(tabs.substring(0, deep + 1))
						.append(next.getClass().getSimpleName() + "#" + next.hashCode());
			}
			buffer.append("\n");
		}
		return buffer.toString();
	}

	@SuppressWarnings("unchecked")
	private static final List<Class<? extends Object>> LEAVES = Arrays.asList(Boolean.class, Character.class,
			Byte.class, Short.class, Integer.class, Long.class, Float.class, Double.class, Void.class, String.class);

	private static Set<Class<?>> wrapperTypes = new HashSet<Class<?>>() {
		{
			add(Boolean.class);
			add(Character.class);
			add(Byte.class);
			add(Short.class);
			add(Integer.class);
			add(Long.class);
			add(Float.class);
			add(Double.class);
			add(Void.class);
		}
	};
	private static Set<Object> stack = new HashSet<Object>();

	public static String toStringRecursive(Object o, int deep) throws Exception {
		if (o == null)
			return "null";

		if (LEAVES.contains(o.getClass()))
			return o.toString();

		String indent = tabs.substring(0, deep + 1);
		StringBuilder sb = new StringBuilder();
		sb.append(indent).append(o.getClass().getSimpleName()).append("#").append(o.hashCode());
		indent = indent + "\t";

		if (o instanceof String) {
			return sb.toString();
		} else if (o.getClass().isPrimitive()) {
			return sb.toString();
		} else if (Collection.class.isAssignableFrom(o.getClass())) {
			Iterator iterator = ((Collection) o).iterator();
			while (iterator.hasNext()) {
				Object next = iterator.next();
				toStringRecursive(next, deep);
			}
			return sb.toString();
		}

		List<Field> declaredFields = Arrays.asList(o.getClass().getDeclaredFields());
		if (o instanceof StreeNode) {
			declaredFields = getAllFields(new ArrayList<Field>(), o.getClass());
		}

		for (Field f : declaredFields) {
			if (Modifier.isStatic(f.getModifiers()))
				continue;
			f.setAccessible(true);
			Object o2 = f.get(o);
			String o2Name = f.getName();
			if (o2 == null) {
				sb.append("\n").append(indent + o2Name).append(":null");
				continue;
			}
			if (o2 instanceof String) {
				continue;
			} else if (o2.getClass().isPrimitive() || wrapperTypes.contains(o2.getClass())) {
				sb.append("\n").append(indent + o2Name).append(":").append(o2);
				continue;
			}

			if (stack.contains(o2)) {
				sb.append("\n").append(indent).append(o2.getClass().getSimpleName() + "#" + o2.hashCode() + "(rec)");
			} else if (o instanceof StreeNode) {
				stack.add(o2);
				String o2Detail = toStringRecursive(o2, deep + 1);
				sb.append("\n").append(indent).append(o2Name).append(" - ").append(o2Detail).append(" ");
			} else {
				sb.append("\n").append(indent).append(o2Name).append("#").append(o2 == null ? "null" : o2.hashCode());
			}
		}
		return sb.toString();
	}

	public static List<Field> getAllFields(List<Field> fields, Class<?> type) {
		fields.addAll(Arrays.asList(type.getDeclaredFields()));

		if (type.getSuperclass() != null) {
			fields = getAllFields(fields, type.getSuperclass());
		}

		return fields;
	}

	public Connection getConnection() throws SQLException {
		String url = "jdbc:h2:tcp://localhost/sakila";
		String username = "sa";
		String password = "";
		Connection connection = DriverManager.getConnection(url, username, password);
		return connection;
	}

	protected void execute(SQL sql) throws SQLException {
		Connection conn = getConnection();
		PreparedStatement pstmt = conn.prepareStatement(sql.toString());
		sql.bindings().apply(pstmt);
		ResultSet rs = pstmt.executeQuery();
		while (rs.next()) {
			System.out.println(rs.getString(1));
		}
	}
}