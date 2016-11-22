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
 * 
 */
public class StreeClause extends StreeNode {
	/**
	 * Holds content 
	 */
	public CharSequence content;

	/**
	 * 
	 */
	public StreeClause() {
		super();
	}

	/**
	 * 
	 */
	public StreeClause(StreeClause clause) {
		super(clause.parent());
		this.content = clause.content;
	}

	/**
	 * 
	 * @param strings
	 */
	public StreeClause(CharSequence sqlPart) {
		super();
		this.content = sqlPart;
	}

	/**
	 * 
	 */
	public StreeClause clone() {
		return new StreeClause(this);
	}

	/**
	 * 
	 * @param another
	 * @return
	 */
	public StreeNode merge(StringBuilder buffer) {
		super.merge(buffer);
		if (this.isExclude()) {
			return this;
		}

		if (content != null) {
			if (buffer.length() == 0 || Character.isWhitespace(buffer.charAt(buffer.length() - 1))) {
				buffer.append(content);
			} else {
				buffer.append(" ").append(content);
			}
		}
		return this;
	}

	/**
	 * 
	 */
	public StreeClause replace(CharSequence target, CharSequence replacement) {
		content = content.toString().replace(target, replacement);
		return this;
	}

	/**
	 * 
	 */
	@Override
	public String toString() {
		return "StreeClause ["+ content + "]";
	}
}