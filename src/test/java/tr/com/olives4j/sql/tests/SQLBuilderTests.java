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

import static tr.com.olives4j.sql.SQL.$;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.junit.Assert;
import org.junit.Test;

import tr.com.olives4j.sql.SQL;
import tr.com.olives4j.sql.SQLBind;
import tr.com.olives4j.sql.SQLBindNode;
import tr.com.olives4j.sql.SQLBindings;
import tr.com.olives4j.stree.StreeMark;
import tr.com.olives4j.stree.StreeNode;

/**
 * 
 * @author omer.dogan
 *
 */
public class SQLBuilderTests extends TestBase {
	static Logger logger = Logger.getLogger(SQLBuilderTests.class);
	static {
		String pattern1 = "%d %r [%t] %-5p %c %x - %m%n ";
		String pattern2 = "%m%n ";
		BasicConfigurator.configure(new ConsoleAppender(new PatternLayout(pattern2)));
	}

	boolean optional, excluded;
	Integer numofValues, excludedClauseIndex;
	Object defaultValue, value;

	/**
	 * 
	 */
	@Test
	public void testBindingNode() {
		SQL sql = SQL.of("select * from customer where active = TRUE and customer_id in ", $(1, 2, 3, 4));
		debugQuery(sql, 1);

		Assert.assertEquals(countChar(sql.toString(), '?'), 4);
		Assert.assertEquals(sql.bindings().size(), 1);

		checkBind(sql, 0, numofValues = 4, optional = false, excluded = false, excludedClauseIndex = null, defaultValue = null,value=Arrays.asList(1,2,3,4));
	}

	/**
	 * 
	 */
	@Test
	public void testBindingNodeOptional() {
		SQL sql = SQL.of("select * from customer where 1=1", "and customer_id in", $(null).optional());
		debugQuery(sql, 1);

		Assert.assertEquals(countChar(sql.toString(), '?'), 0);
		Assert.assertEquals(sql.bindings().size(), 1);

		checkBind(sql, 0, numofValues = 1, optional = true, excluded = true, excludedClauseIndex = 1, defaultValue = null,value=null);

	}

	/**
	 * 
	 */
	@Test
	public void testBindingNodeOptionalWithDefault() {
		SQL sql = SQL.of("select * from customer where 1=1 and customer_id in :bindCustIds and target_customer_id in :bindCustIds");
		sql.bindings().bind("bindCustIds", null).optional().defaultValue(Arrays.asList(1, 2, 3));
		debugQuery(sql, 1);

		Assert.assertEquals(countChar(sql.toString(), '?'), 6);
		Assert.assertEquals(sql.bindings().size(), 2);

		checkBind(sql, 0, numofValues = 3, optional = true, excluded = false, excludedClauseIndex = null, defaultValue = Arrays.asList(1, 2, 3),value=null);
	}

	/**
	 * 
	 */
	@Test
	public void testBindingNodeOverride() {
		SQL sql = SQL.of("select * from customer where 1=1", " and customer_id in ", $(1, 2, 3, 4));
		debugQuery(sql, 1);

		Assert.assertEquals(countChar(sql.toString(), '?'), 4);
		Assert.assertEquals(sql.bindings().size(), 1);
		checkBind(sql, 0, numofValues = 4, optional = false, excluded = false, excludedClauseIndex = null, defaultValue = null,Arrays.asList(1,2,3,4));

		sql.bindings().bind(0, null).optional();
		debugQuery(sql, 2);
		checkBind(sql, 0, numofValues = 1, optional = true, excluded = true, excludedClauseIndex = 1, defaultValue = null,value=null);

		sql.bindings().get(0).defaultValue(1);
		debugQuery(sql, 3);
		checkBind(sql, 0, numofValues = 1, optional = true, excluded = false, excludedClauseIndex = null, defaultValue = 1,value=1);
	}

	/**
	 * 
	 * @throws ParseException
	 */
	@Test
	public void testBindingComplexWithNamedBinds() throws ParseException {
		SQL sql = SQL.of("select * from customer where 1=1", //
				" and store_id 	= :store_id", //
				" and first_name like :firstname", //
				" and active    = :active ", //
				" and create_date between :startdate and :enddate");

		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		SQLBindings bindings = sql.bindings();
		bindings.bind("store_id", null).optional();
		bindings.bind("active", true);
		bindings.bind("firstname", "a%");
		bindings.bind("startdate", sdf.parse("2006-02-14"));
		bindings.bind("enddate", sdf.parse("2006-02-16"));

		debugQuery(sql, 1);
		checkBind(sql, "store_id", numofValues = 1, optional = true, excluded = true, excludedClauseIndex = null, defaultValue = null,null);
		checkBind(sql, "active", numofValues = 1, optional = false, excluded = false, excludedClauseIndex = null, defaultValue = null,true);
		checkBind(sql, "firstname", numofValues = 1, optional = false, excluded = false, excludedClauseIndex = null, defaultValue = null,"a%");
		checkBind(sql, "startdate", numofValues = 1, optional = false, excluded = false, excludedClauseIndex = null, defaultValue = null,sdf.parse("2006-02-14"));
		checkBind(sql, "enddate", numofValues = 1, optional = false, excluded = false, excludedClauseIndex = null, defaultValue = null,sdf.parse("2006-02-16"));

	}

	/**
	 * 
	 * @throws ParseException
	 */
	@Test
	public void testBindingComplexWithInlineVars() throws ParseException {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		SQL sql = SQL.of("select * from customer where 1=1", //
				" and store_id 			=:store_id", //
				" and first_name like 	  ", $("a%"), //
				" and active    		= ", $(true), //
				" and create_date between", $(sdf.parse("2006-02-14")), "and", $(sdf.parse("2006-02-16")));

		sql.bindings().bind("store_id", 1).optional().inline(true);
		debugQuery(sql, 1);
	}

	/**
	 * 
	 */
	@Test
	public void testBindingContext() {
		Foo foo = new Foo("1", "test_user", true, new Date(), new Date());

		SQL sql = SQL.of("select * from customer where 1=1", //
				" and store_id 	= :store_id", //
				" and first_name like :first_name", //
				" and active = :active ", //
				" and create_date between :startdate and :enddate");

		sql.bindings().map(foo, SQLBind.BEANMAPPER);
		debugQuery(sql, 1);
		
		checkBind(sql, "store_id", numofValues = 1, optional = false, excluded = false, excludedClauseIndex = null, defaultValue = null,value=foo.store_id);
		checkBind(sql, "active", numofValues = 1, optional = false, excluded = false, excludedClauseIndex = null, defaultValue = null,value=foo.active);
		checkBind(sql, "first_name", numofValues = 1, optional = false, excluded = false, excludedClauseIndex = null, defaultValue = null,foo.first_name);
		checkBind(sql, "startdate", numofValues = 1, optional = false, excluded = false, excludedClauseIndex = null, defaultValue = null,foo.startdate);
		checkBind(sql, "enddate", numofValues = 1, optional = false, excluded = false, excludedClauseIndex = null, defaultValue = null,foo.enddate);

	}

	/**
	 * 
	 * @throws Exception
	 */
	@Test
	public void testClone() throws Exception {
		SQL sql = SQL.of("select * from customer where 1=1", //
				" and store_id 	= :store_id", //
				" and first_name like :firstname", //
				" and active    = :active ", //
				" and create_date between :startdate and :enddate");

		SQL clone1 = sql.clone();
		SQL clone2 = sql.clone();

		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		clone1.bindings().bind("store_id", null).optional();
		clone1.bindings().bind("active", true);
		clone1.bindings().bind("firstname", "a%");
		clone1.bindings().bind("startdate", sdf.parse("2006-02-14"));
		clone1.bindings().bind("enddate", sdf.parse("2006-02-16"));

		clone2.bindings().bind("active", null).optional();

		Iterator<SQLBindNode> bindings1 = clone1.bindings().iterator();
		Iterator<SQLBindNode> bindings2 = clone2.bindings().iterator();

		logger.debug(clone1.format());
		logger.debug(clone2.format());

		StringBuilder buffer = new StringBuilder();
		logger.debug(toStringRecursive(clone1, 0));
		logger.debug("=========================");
		logger.debug(toStringRecursive(clone2, 0));

	}

	/**
	 * 
	 */
	@Test
	public void testMark() {
		SQL sql = new SQL("sakila-find-overdue-dvds");
		// @formatter:off
		StreeMark markCustomer = StreeMark.create("customer");
		sql.append("SELECT ",
				"CONCAT(customer.last_name,', ',customer.first_name) AS customer,",
				"address.phone,", 
				"film.title", 
				"FROM rental",
				markCustomer,
				"INNER JOIN customer ON (rental.customer_id = customer.customer_id and customer.create_date>:createdate)", 
				"INNER JOIN address ON customer.address_id = address.address_id",
				"INNER JOIN inventory ON rental.inventory_id = inventory.inventory_id", 
				"INNER JOIN film ON inventory.film_id = film.film_id",
				"WHERE 1=1",
				markCustomer,
				"and customer.id in :customerId",
				"and rental.return_date IS NULL", 
				"and rental_date +INTERVAL film.rental_duration DAY< SYSDATE()");
		// @formatter:on

		int i = 0;
		Iterator<StreeNode> nodes = sql.getNodes().iterator();
		while (nodes.hasNext()) {
			logger.debug(i + " - " + nodes.next().toString());
			i++;
		}

		StringBuilder buffer = new StringBuilder();
		buffer.append("1.1 query         	: " + sql.toString());
		buffer.append("\n1.2 formatted     	: " + sql.format());
		buffer.append("\n1.3 Bindings      	: " + lines(sql.bindings()));
		buffer.append("\nCustomer Mark  	:" + markCustomer);
		logger.debug(buffer);
	}

	/**
	 * 
	 */
	@Test
	public void simpleTracerTest() {
		SQL sql = new SQL();
		sql.append("select * from x where 1=1");
		sql.append(" and y in (:params)");

		logger.debug(sql.toString());
		logger.debug(sql.format());
		logger.debug(lines(sql.bindings().iterator()));
	}

	/**
	 * 
	 */
	@Test
	public void unionTest() {
		SQL sql = new SQL();
		sql.append("select * from customer  where 1=1 AND active=false and customer_id=", $(9), "\n");
		SQL clone1 = sql.clone();
		sql.append(" union \n").append(clone1);
		sql.append(" AND active= ", $(true));

		debugQuery(sql, 1);
	}

	/**
	 * 
	 */
	public static class Foo {
		public String store_id;
		public String first_name;
		public boolean active;
		public Date startdate;
		public Date enddate;

		public Foo() {
			super();
		}

		public Foo(String store_id, String first_name, boolean active, Date startdate, Date enddate) {
			super();
			this.store_id = store_id;
			this.first_name = first_name;
			this.active = active;
			this.startdate = startdate;
			this.enddate = enddate;
		}

		public String getStore_id() {
			return store_id;
		}

		public void setStore_id(String store_id) {
			this.store_id = store_id;
		}

		public String getFirst_name() {
			return first_name;
		}

		public void setFirst_name(String first_name) {
			this.first_name = first_name;
		}

		public boolean isActive() {
			return active;
		}

		public void setActive(boolean active) {
			this.active = active;
		}

		public Date getStartdate() {
			return startdate;
		}

		public void setStartdate(Date startdate) {
			this.startdate = startdate;
		}

		public Date getEnddate() {
			return enddate;
		}

		public void setEnddate(Date enddate) {
			this.enddate = enddate;
		}
	}
}
