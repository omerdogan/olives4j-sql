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

import java.io.FileReader;
import java.text.SimpleDateFormat;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.junit.Test;

import tr.com.olives4j.sql.SQL;
import tr.com.olives4j.sql.SQLBindNode;
import tr.com.olives4j.sql.SQLCollection;
import tr.com.olives4j.sql.SQLReader;

/**
 * 
 * @author omer.dogan
 *
 */
public class SQLReaderTest extends TestBase {
	static Logger logger = Logger.getLogger(SQLReaderTest.class);
	static {
		BasicConfigurator.configure(new ConsoleAppender(new PatternLayout("%r [%t] %-5p %c %x - %m%n ")));
	}

	/**
	 * 
	 */
	public SQLReaderTest() {
		super();
	}

	/**
	 * 
	 * @throws Exception
	 */
	@Test
	public void testParseSQLScript() throws Exception {
		SQLCollection sqls = SQLReader.read(new FileReader("src/test/sql/test1-search-query.sql"));
		SQL sql = sqls.get("search_customer");
		
		SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd");
		//sql.bindings().bind("store_id"	, null).optional(true);
		sql.bindings().bind("active"	, true);
		sql.bindings().bind("firstname"	, "a%");
		sql.bindings().bind("start_date", sdf.parse("2006-02-14"));
		sql.bindings().bind("end_date"	, sdf.parse("2006-02-16"));
		
		String query = sql.toString();
		Iterator<SQLBindNode> bindings = sql.bindings().iterator();
		List<SQLBindNode> list = toList(bindings);

		StringBuilder buffer = new StringBuilder();
		buffer.append("1.1 query         : " + query);
		buffer.append("\n1.2 formatted     : " + sql.format());
		buffer.append("\n1.3 Bindings      : \n" + lines(list));
		System.out.println(buffer);
	}
}