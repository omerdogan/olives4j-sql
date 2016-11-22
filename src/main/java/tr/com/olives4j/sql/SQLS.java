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

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

/**
 * 
 * @author omer.dogan
 *
 */
public class SQLS {
	/**
	 * 
	 * @return
	 * @throws SQLException 
	 */
	public static String generateInsert(String targetTable, ResultSet rs) throws SQLException {
		ResultSetMetaData mtdt = rs.getMetaData();
		int columnCount = mtdt.getColumnCount();
		StringBuffer sql = new StringBuffer();
		sql.append("insert into " + targetTable);
		sql.append("(");
		for (int i = 1; i <= columnCount; i++) {
			sql.append(mtdt.getColumnName(i));
			sql.append(",");
		}
		sql.deleteCharAt(sql.length() - 1);
		sql.append(")");
	
		sql.append(" values (" + repeat("?", ",", columnCount) + ")");
		
		return sql.toString();
	}

	/**
	 * 
	 * @param ch
	 * @param sep
	 * @param count
	 * @return
	 */
	private static String repeat(String ch, String sep, int count) {
		StringBuilder buffer = new StringBuilder();
		for (int i = 0; i < count; i++) {
			buffer.append(ch).append(sep);
		}
		if (count > 0) {
			buffer.setLength(buffer.length() - sep.length());
		}

		return buffer.toString();
	}
}
