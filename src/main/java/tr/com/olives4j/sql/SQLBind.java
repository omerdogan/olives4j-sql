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

import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public interface SQLBind {
	public static final BeanMapper BEANMAPPER = new BeanMapper();

	/**
	 * 
	 * @param name
	 * @return
	 */
	SQLBind name(String name);

	/**
	 * 
	 * @param name
	 * @return
	 */
	SQLBind jdbcType(Integer type);

	/**
	 * 
	 * @return
	 */
	SQLBind optional();

	/**
	 * 
	 * @return
	 */
	SQLBind required();

	/**
	 * 
	 * @param inline
	 * @return
	 */
	SQLBind inline(boolean inline);

	/**
	 * 
	 * @param seperator
	 * @return
	 */
	SQLBind seperator(String seperator);

	/**
	 * 
	 * @param param
	 * @return
	 */
	public SQLBind defaultValue(Object param);

	/**
	 * 
	 * @param param
	 * @return
	 */
	public SQLBind exclude(boolean exclude);

	/**
	 * @return
	 */
	public String getName();

	/**
	 * @return
	 */
	public boolean isExcluded();

	/**
	 * @return
	 */
	public boolean isOptional();

	/**
	 * @return
	 */
	public Object getDefaultValue();

	/**
	 * @return
	 */
	public Object value();

	/**
	 * @return
	 */
	public Collection<Object> extract();
	
	/**
	 * @return
	 */
	public Collection<Object> extract(Collection<Object> c);

	/**
	 * 
	 * @author omer.dogan
	 *
	 */
	public interface Mapper {
		Object map(SQLBindNode binding, Object bean);
	}

	public static final class BeanMapper implements Mapper {
		@Override
		public Object map(SQLBindNode binding, Object bean) {
			try {
				BeanInfo beanInfo = Introspector.getBeanInfo(bean.getClass());
				PropertyDescriptor[] propertyDescriptors = beanInfo.getPropertyDescriptors();
				for (PropertyDescriptor property : propertyDescriptors) {
					String propertyName = property.getName();
					if (binding.name != null) {
						if (binding.name.equals(propertyName)) {
							binding.value(property.getReadMethod().invoke(bean));
						}
					}
				}
				return null;
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
	};

	/**
	 * 
	 * @author omer.dogan
	 *
	 */
	public static class Set implements SQLBind {
		List<SQLBind> set;

		public Set() {
			super();
			set = new ArrayList<SQLBind>();
		}

		/**
		 * 
		 * @param set
		 */
		public Set(List<SQLBind> set) {
			super();
			this.set = set;
		}

		/**
		 * 
		 * @param bind
		 */
		public void add(SQLBind bind) {
			set.add(bind);
		}

		/**
		 * 
		 */
		@Override
		public SQLBind name(String name) {
			for (SQLBind sqlBind : set) {
				sqlBind.name(name);
			}
			return this;
		}

		/**
		 * 
		 */
		@Override
		public SQLBind jdbcType(Integer type) {
			for (SQLBind sqlBind : set) {
				sqlBind.jdbcType(type);
			}
			return this;
		}

		/**
		 * 
		 */
		@Override
		public SQLBind optional() {
			for (SQLBind sqlBind : set) {
				sqlBind.optional();
			}
			return this;
		}

		/**
		 * 
		 */
		@Override
		public SQLBind required() {
			for (SQLBind sqlBind : set) {
				sqlBind.required();
			}
			return this;
		}

		/**
		 * 
		 */
		@Override
		public SQLBind inline(boolean inline) {
			for (SQLBind sqlBind : set) {
				sqlBind.inline(inline);
			}
			return null;
		}

		/**
		 * 
		 */
		@Override
		public SQLBind seperator(String seperator) {
			for (SQLBind sqlBind : set) {
				sqlBind.seperator(seperator);
			}
			return this;
		}

		/**
		 * 
		 */
		@Override
		public SQLBind defaultValue(Object param) {
			for (SQLBind sqlBind : set) {
				sqlBind.defaultValue(param);
			}
			return this;
		}

		/**
		 * 
		 */
		@Override
		public SQLBind exclude(boolean param) {
			for (SQLBind sqlBind : set) {
				sqlBind.exclude(param);
			}
			return this;
		}

		@Override
		public String getName() {
			return set.get(0).getName();
		}

		@Override
		public boolean isExcluded() {
			return set.get(0).isExcluded();
		}

		@Override
		public boolean isOptional() {
			return set.get(0).isOptional();
		}

		@Override
		public Object getDefaultValue() {
			return set.get(0).getDefaultValue();
		}

		@Override
		public Object value() {
			return set.get(0).value();
		}

		@Override
		public Collection<Object> extract() {
			return extract(new ArrayList<Object>());
		}
		
		@Override
		public Collection<Object> extract(Collection<Object> c) {
			return set.get(0).extract(c);
		}
	}

}