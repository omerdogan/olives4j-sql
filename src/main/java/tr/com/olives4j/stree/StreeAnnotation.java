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
 * Base class for annotation nodes
 * 
 * @author odogan
 *
 */
public class StreeAnnotation extends StreeNode {
	/**
	 * 
	 */
	public String expression;

	/**
	 * 
	 */
	public StreeAnnotation() {
		super();
	}

	/**
	 * 
	 * @param expression
	 */
	public StreeAnnotation(String expression) {
		super();
		this.expression = expression;
	}

	/**
	 * 
	 */
	@Override
	public String toString() {
		return "StreeAnnotation [expression=" + expression + "]";
	}
}