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

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.PatternLayout;
import org.junit.Assert;
import org.junit.Test;

import tr.com.olives4j.sql.SQL;
import tr.com.olives4j.sql.SQLBind;
import tr.com.olives4j.sql.SQLBindNode;
import tr.com.olives4j.stree.StreeMark;
import tr.com.olives4j.stree.StreeNode;

public class H2ExecutionTests extends TestBase {
	static {
		BasicConfigurator.configure(new ConsoleAppender(new PatternLayout("%d %r [%t] %-5p %c %x - %m%n ")));
	}
	
	public Connection  getConnection() throws SQLException{
		String url="jdbc:h2:tcp://localhost/sakila";
		String username="sa";
		String password="";
		Connection connection = DriverManager.getConnection(url,username,password);
		return connection;
	}

	@Test
	public void testBindingNode() throws SQLException {
		SQL sql = SQL.of("select * from customer where active = TRUE and customer_id in ", $(1, 2, 3, 4));

		String query = sql.toString();
		Assert.assertEquals(countChar(query, '?'), 4);

		Iterator<SQLBindNode> bindings = sql.bindings().iterator();
		List<SQLBindNode> list = toList(bindings);
		Assert.assertEquals(list.size(), 1);
		Assert.assertEquals(list.get(0).extract().size(), 4);

		StringBuilder buffer = new StringBuilder();

		buffer.append("1.1 query         : " + query);
		buffer.append("\n1.2 formatted    : " + sql.format());
		buffer.append("\n1.3 Bindings     : " + lines(sql.bindings()));
		System.out.println(buffer);
		
		Connection conn=getConnection();
		PreparedStatement pstmt = conn.prepareStatement(sql.toString());
		sql.bindings().apply(pstmt);
		ResultSet rs = pstmt.executeQuery();
		while(rs.next()){
			System.out.println(rs.getString(1));
		}
	}

	@Test
	public void testBindingNodeOverride() throws SQLException {
		SQL sql = SQL.of();
		sql.append("select * from customer where customer_id in ", $(1, 2, 3, 4));

		String query = sql.toString();
		Assert.assertEquals(countChar(query, '?'), 4);

		Iterator<SQLBindNode> bindings = sql.bindings().iterator();
		List<SQLBindNode> list = toList(bindings);
		StringBuilder buffer = new StringBuilder();
		buffer.append("1.1 query         : " + query);
		buffer.append("\n1.2 formatted     : " + sql.format());
		buffer.append("\n1.3 Bindings      : " + lines(list));
		System.out.println(buffer);

		Assert.assertEquals(list.size(), 1);
		Assert.assertEquals(list.get(0).extract().size(), 4);

		sql.bindings().bind(0, 1);
		query = sql.toString();
		Assert.assertEquals(countChar(query, '?'), 1);

		bindings = sql.bindings().iterator();
		list = toList(bindings);
		Assert.assertEquals(list.size(), 1);
		Assert.assertEquals(list.get(0).extract().size(), 1);
		
		Connection conn=getConnection();
		PreparedStatement pstmt = conn.prepareStatement(sql.toString());
		sql.bindings().apply(pstmt);
		ResultSet rs = pstmt.executeQuery();
		while(rs.next()){
			System.out.println(rs.getString(1));
		}

	}

	@Test
	public void testBindingNodeOptional() throws SQLException {
		Object var = null;
		SQL sql = SQL.of("select * from customer where 1=1", "and customer_id in", $(var).optional());

		String query = sql.toString();
		Iterator<SQLBindNode> bindings = sql.bindings().iterator();
		List<SQLBindNode> list = toList(bindings);

		Assert.assertEquals(list.size(), 1);

		StringBuilder buffer = new StringBuilder();
		buffer.append("1.1 query         : " + query);
		buffer.append("\n1.2 formatted     : " + sql.format());
		buffer.append("\n1.3 Bindings      : " + lines(list));
		System.out.println(buffer);
		
		Connection conn=getConnection();
		PreparedStatement pstmt = conn.prepareStatement(sql.toString());
		sql.bindings().apply(pstmt);
		ResultSet rs = pstmt.executeQuery();
		while(rs.next()){
			System.out.println(rs.getString(1));
		}
	}

	@Test
	public void testBindingNodeOptionalWithDefault() throws SQLException {
		Object var = null;

		SQL sql = SQL.of("select * from customer where 1=1 and customer_id in :bindCustIds and store_id in :bindCustIds");
		sql.bindings().bind("bindCustIds", var).optional().defaultValue(Arrays.asList(1, 2, 3));

		String query = sql.toString();
		Iterator<SQLBindNode> bindings = sql.bindings().iterator();
		List<SQLBindNode> list = toList(bindings);

		Assert.assertEquals(list.size(), 2);
		Assert.assertEquals(list.get(0).extract().size(), 3);
		Assert.assertEquals(list.get(0).extract(), Arrays.asList(1, 2, 3));

		StringBuilder buffer = new StringBuilder();
		buffer.append("1.1 query         : " + query);
		buffer.append("\n1.2 formatted     : " + sql.format());
		buffer.append("\n1.3 Bindings      : " + lines(list));
		System.out.println(buffer);
		
		Connection conn=getConnection();
		PreparedStatement pstmt = conn.prepareStatement(sql.toString());
		sql.bindings().apply(pstmt);
		ResultSet rs = pstmt.executeQuery();
		while(rs.next()){
			System.out.println(rs.getString(1));
		}
	}

	@Test
	public void testBindingComplexWithNamedBinds() throws SQLException, ParseException {
		SQL sql = SQL.of(
				"select * from customer where 1=1", //
				" and store_id 	= :store_id", //
				" and first_name like :firstname", //
				" and active    = :active ", //
				" and create_date between :startdate and :enddate"
				);

		SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd");
		sql.bindings().bind("startdate"	, sdf.parse("2006-02-14"));
		sql.bindings().bind("enddate"	, sdf.parse("2006-02-16"));
		sql.bindings().bind("store_id"	, null).optional();
		sql.bindings().bind("active"	, true);
		sql.bindings().bind("firstname"	, "A%");
		
		sql.bindings().get("startdate").exclude(true);
		sql.bindings().get("enddate").exclude(true);

		String query = sql.toString();
		Iterator<SQLBindNode> bindings = sql.bindings().iterator();
		List<SQLBindNode> list = toList(bindings);

		StringBuilder buffer = new StringBuilder();
		buffer.append("1.1 query         : " + query);
		buffer.append("\n1.2 formatted     : " + sql.format());
		buffer.append("\n1.3 Bindings      : \n" + lines(list));
		System.out.println(buffer);
		
		Connection conn=getConnection();
		PreparedStatement pstmt = conn.prepareStatement(sql.toString());
		sql.bindings().apply(pstmt);
		ResultSet rs = pstmt.executeQuery();
		while(rs.next()){
			System.out.println(rs.getString(1));
		}
	}
	
	@Test
	public void testBindingComplexWithInlineVars() {
		SQL sql = SQL.of(
				"select * from customer where 1=1", //
				" and store_id 			=",$(null).optional(), //
				" and first_name like 	 ",$("a%"), //
				" and active    		= ",$(true), //
				" and create_date between ",$(new Date()), " and ",$(new Date()));

		String query = sql.toString();
		Iterator<SQLBindNode> bindings = sql.bindings().iterator();
		List<SQLBindNode> list = toList(bindings);

		StringBuilder buffer = new StringBuilder();
		buffer.append("1.1 query         : " + query);
		buffer.append("\n1.2 formatted     : " + sql.format());
		buffer.append("\n1.3 Bindings      : \n" + lines(list));
		System.out.println(buffer);
	}
	

	@Test
	public void testBindingContext() {
		Foo foo = new Foo("1", "test_user", true, new Date(), new Date());

		SQL sql = SQL.of("select * from customer where 1=1", //
				" and store_id 	= :store_id", //
				" and first_name like :first_name", //
				" and active = :active ", //
				" and create_date between :startdate and :enddate");

		sql.bindings().map(foo, SQLBind.BEANMAPPER);

		String query = sql.toString();
		Iterator<SQLBindNode> bindings = sql.bindings().iterator();
		List<SQLBindNode> list = toList(bindings);

		StringBuilder buffer = new StringBuilder();
		buffer.append("1.1 query         : " + query);
		buffer.append("\n1.2 formatted     : " + sql.format());
		buffer.append("\n1.3 Bindings      : \n" + lines(list));
		System.out.println(buffer);
	}

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
		
		String query = sql.toString();
		Iterator<SQLBindNode> bindings = sql.bindings().iterator();
		List<SQLBindNode> list = toList(bindings);
		
		int i=0;
		Iterator<StreeNode> nodes = sql.getNodes().iterator();
		while(nodes.hasNext()) {
			System.out.println(i+" - "+nodes.next().toString());
			i++;
		}
		
		StringBuilder buffer = new StringBuilder();
		buffer.append("1.1 query         : " + query);
		buffer.append("\n1.2 formatted     : " + sql.format());
		buffer.append("\n1.3 Bindings      : " + lines(list));
		buffer.append("\nCustomer Mark  :"+markCustomer);
		System.out.println(buffer);
		
		
		// @formatter:on
	}

	@Test
	public void simpleTracerTest() {
		SQL sql = new SQL();
		sql.append("select * from x where 1=1");
		sql.append(" and y in (:params)");

		System.out.println(sql.toString());
		System.out.println(sql.format());
		System.out.println(lines(sql.bindings().iterator()));
	}

	@Test
	public void unionTest() {
		SQL sql = new SQL();
		sql = new SQL();
		sql.append("select * from x  where 1=1 AND x=?", $(9), "\n");
		sql.append(" union ").append(sql);
		sql.append(" AND 1=? ", $(3));

		CharSequence string = sql.format();
		System.out.println(string);
		System.out.println(lines(sql.bindings().iterator()));
	}
	
	@Test
	public void testCompare() throws ParseException, SQLException {
		SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd");
		
		String firstName="O";
		String lastName="Do";
		Boolean active=true;
		Date startDate=sdf.parse("2006-02-14");
		Date endDate=sdf.parse("2006-02-16");
		
		
		Connection connection = getConnection();
		StringBuilder buffer=new StringBuilder();
		buffer.append("select * from customer where 1=1");
		if(firstName!=null){
			buffer.append("first_name like ?");
		}
		if(lastName!=null){
			buffer.append("first_name like ?");
		}
		if(active!=null){
			buffer.append("active like ?");
		}
		if(startDate!=null){
			buffer.append("createDate between ? and ?");
		}
		
		PreparedStatement pstmt = connection.prepareStatement(buffer.toString());
		int index=0;
		
		if(firstName!=null){
			pstmt.setString(++index, firstName);
		}
		if(lastName!=null){
			pstmt.setString(++index, lastName);
		}
		if(active!=null){
			pstmt.setBoolean(++index, active);
		}
		if(startDate!=null){
			pstmt.setDate(++index, new java.sql.Date(startDate.getTime()));
			pstmt.setDate(++index, new java.sql.Date(endDate.getTime()));
		}		
		
		System.out.println("Execute query :"+buffer.toString()+" with params first_name"+firstName+"...");
		ResultSet executeQuery = pstmt.executeQuery();
		
		 
		
		// olive-sql
		
		SQL sql = SQL.of(
				"select * from customer where 1=1", //
				" and first_name like 	 ",$(firstName+"%"), //
				" and last_name  like  	 ",$(lastName+"%"), //
				" and active   		   = ",$(true), //
				" and create_date between ",$(new Date()), " and ",$(new Date()));

		Connection conn=getConnection();
		PreparedStatement pstmto = conn.prepareStatement(sql.toString());
		sql.bindings().apply(pstmto);
		System.out.println("Execute query :"+sql);
		ResultSet rs = pstmto.executeQuery();
	}

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
