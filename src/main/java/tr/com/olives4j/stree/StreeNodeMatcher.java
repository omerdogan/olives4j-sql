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

import tr.com.olives4j.sql.util.Functionals.Predicate;

/**
 * @author omer.dogan
 *
 * @param <T>
 */
public class StreeNodeMatcher<T extends StreeNode> implements Predicate<StreeNode> {
	Class<T> targetType;
	private Boolean exclude;

	/**
	 * @param targetType
	 * @param selectExcludes
	 */
	public StreeNodeMatcher(Class<T> targetType, Boolean selectExcludes) {
		super();
		this.targetType = targetType;
		this.exclude = selectExcludes;
	}
	
	public static <T extends StreeNode> StreeNodeMatcher<T> of(Class<T> targetType,Boolean selectExcludes){
		return new StreeNodeMatcher<T>(targetType, selectExcludes);
	}

	/**
	 * @param exclude
	 */
	public void selectExcluded(boolean exclude) {
		this.exclude = exclude;
	}

	/**
	 * @param node
	 * @return
	 */
	public boolean test(StreeNode node) {
		final boolean checkExclude = this.exclude == null ? true : node.isExclude() == this.exclude;

		if (targetType != null) {
			if (node != null) {
				return checkExclude && targetType.isAssignableFrom(node.getClass());
			}
		}
		return checkExclude;
	}
}