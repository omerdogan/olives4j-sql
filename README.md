Olives4J - SQL
------------
A lightweight java library created to help building complex sql, and derivatives such as hql and jpql, queries by a more readable, writeable and maintainable way. 

Sample
--------
Sample code demonstrate using olives4j-sql api

```java

	SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd");
	
	// create sql object for the query
	SQL sql = SQL.of("select * from customer where 1=1", //
			" and store_id 	= :store_id", //
			" and active 	= :active" , //
			" and first_name like ", $("a%"), //
			" and create_date between", $(startDate),"and",$(endDate));
	
	sql.bindings().bind("store_id", null).optional();
	sql.bindings().bind("active", null).optional().defaultValue(true);
	
	System.out.println("SQL			:\n"+sql.toString());
	System.out.println("SQL DEBUG	:\n"+sql.format());
```

This code is produce the following output;

```sql

	SQL         : 
	select * from customer where 1=1  and first_name like 	  (?)  and active    		= (?)  and create_date between (?) and (?)
		
	SQL DEBUG   : 
	select * 
	from customer 
	where 1=1 
	and first_name like (? /* a%*/?) 
	and active = (? /* true*/?) 
	and create_date between (? /* Tue Feb 14 00:00:00 EET 2006*/?) 
	and(? /* Thu Feb 16 00:00:00 EET 2006*/?)
```

As you may realize the following line removed from final query. 
The reason is store_id parameter is defined as optional and provided value is null.
   
		and store_id   = :store_id

Observations from the sample code;

* We use SQL type to build an sql statement instead of StringBuilder/StringBuffer  
* SQL type accepts varargs parameters with the type of string,tree nodes, and any other object    
* SQL type support named binding variables 
* Binding variables may be defined as optional and a default value can be provided.
* SQL expressions including optional bindings removed from the final query if the provided values are null 
* SQL #format() produce formatted sql string which also includes binding parameter values placed relatively near to placeholders. It make sql debugging much easier

and using the SQL in jdbc in plain like the following example; 

```java
		Connection connection = DriverManager.getConnection("jdbc:h2:mem:./test;MV_STORE=FALSE and ;MVCC=FALSE", "sa", "");
		PreparedStatement pstmt = connection.prepareStatement(sql.toString());
		sql.bindings().apply(pstmt);
		ResultSet rs = pstmt.executeQuery();
		while (rs.next()) {
			logger.debug(rs.getString("TABLE_NAME") + " - " + rs.getString("LAST_MODIFICATION"));
		}
```

Usage
-----

There are three technique you can use to create dynamic queries with olives4j-sql

1.Create sql programaticly by providing bindings while building sql

```java

	import static tr.com.olives4j.sql.SQL.$;
	SQL sql = SQL.of("select * from customer where 1=1", 
			" and store_id   ="         ,$(null).optional(), 
			" and first_name like "     ,$("a%"), 
			" and active     = "        ,$(true), 
			" and create_date between " ,$(sdf.parse("2006-02-14")), " and ",sdf.parse("2006-02-16")));
			
```

2.Create sql programaticly with named parameters and set binding parameter values later

```java

	SQL sql = SQL.of("select * from customer where 1=1",  
				" and store_id   = :store_id",  
				" and active     = :active ",  
				" and create_date between :startdate and :enddate");

	sql.bindings().bind("store_id" , null).optional();
	sql.bindings().bind("active"   , true);
	sql.bindings().bind("startdate", sdf.parse("2006-02-14"));
	sql.bindings().bind("enddate"  , sdf.parse("2006-02-16"));	
```


3.Use an external sql script file 

-- app.sql file content -------------------

```sql

	--@NAMED search_customer
	select * 
	from customer 
	where 1=1 
	and store_id = 1			--@:store_id optional
	and first_name like 'A%'	--@:firstname optional
	and active 	= true			--@:active optional
	and create_date between (sysdate - 1) 	--@:start_date
					and		(sysdate + 1) 			--@:end_date
	;
	
	--@NAMED find_customer_by_id
	select * 
	from customer 
	where 1=1 
	and customer_id=:customer_id
	;
	....
```

-- Java code ------------------------------	

```java

	// Parse sql script file 
	SQLCollection sqls = SQLReader.read(new FileReader("src/test/sql/test1-search-query.sql"));
	SQL sql = sqls.get("search_customer");
	
	SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd");
	sql.bindings().bind("store_id"	, null).optional();
	sql.bindings().bind("active"	, true);
	sql.bindings().bind("firstname"	, "a%");
	sql.bindings().bind("start_date"	, sdf.parse("2006-02-14"));
	sql.bindings().bind("end_date"	, sdf.parse("2006-02-16"));	
```


###Overview

> Sometimes it seems like programmers will do everything to avoid SQL. 
> At the same time DSLs (Domain Specific Languages) are very popular. 
> If DSLs are so great, why are you trying to avoid SQL? 
> Or procedural extensions to SQL? SQL is a DSL for dealing with relational data." [[1]](http://www.andrejkoelewijn.com/blog/2008/10/27/sql-is-a-dsl/)


The main objectives of this project is try to remove the following handicaps of using sql with java.

#####1. Embedding sql syntax into java is too cumbersome and error-prone.

So What we can do currently
   
######1.1 Go with bare jdbc

```java

	String query = "select COF_NAME, SUP_ID, PRICE, " +
                   "SALES, TOTAL " +
                   "from " + dbName + ".COFFEES";	
	
```
	
######1.2 Use one of the sql builder libraries

```sql

	select("COF_NAME", "SUP_ID","PRICE"..)
	.from("SALES, TOTAL")
	.where("COF_NAME=? AND SUP_ID=?", 1, 2)
	.groupBy("COF_NAME")
	.orderBy("COF_NAME");
```
######1.3 Use criteria api
######1.4 Use an embedded sql DSL like JOOQ,QUERYDSL  
  
```java

	create.select(AUTHOR.FIRST_NAME, AUTHOR.LAST_NAME, count())
	.from(AUTHOR)
	.join(BOOK).on(AUTHOR.ID.equal(BOOK.AUTHOR_ID))
	.where(BOOK.LANGUAGE.eq("DE"))
	.and(BOOK.PUBLISHED.gt(date("2008-01-01")))
	.groupBy(AUTHOR.FIRST_NAME, AUTHOR.LAST_NAME)
	.having(count().gt(5))
	.orderBy(AUTHOR.LAST_NAME.asc().nullsFirst())
	.limit(2)
	.offset(1)
```

All of these techniques have share the following weaknesses 
  
  - On a change,you have to convert the related code to sql form, update and test the query on an externel sql client, then convert the query back to java form for the used technique.
  - It still error-prone , you can't be sure the code is valid until it run
  - You could not map all type sqls because of the limitation of apis
  - Some of these techinques applicaple only specific databases only

Infact, There is more clean solution having none of these weaknesses superior to these techniques 
	
######1.5  Don't try to embed sql into java and just use external sql scripts as a resource

Some of the advantages of using external sql script over embedding are;  

1. You may change the queries without recompilation
2. You dont have to change the code whenever the query change.
3. You can use the advantages of sql IDEs (syntax-highlighting,auto complete,validation)
4. Less verbose and error prone with the help of the IDEs

olives4j-sql provides a clean solution for using external sql scripts.
it is intended to be able to parse any ordinary sql/plsql script file.
it use an extension concept, SQL annotation, which allow adding extra behaviour to sql scripts to integrate sql with code easier

##### External SQL and SQL Annotation

The concept is like javadoc annotations

- SQL annotations may be defined in line or block comments 
- SQL comments must be started with @ character to be annotation 
- SQL comments must be started with @: characters to be binding annotation a shortcut form for bindings  
		
	--@[annotation_name] [annotation parameters] 
	/*@[annotation_name] [annotation parameters] */

	--@:[binding_parameter_name] [binding parameters] 
	/*@:[binding_parameter_name] [binding parameters] */
    
Processing logic for the sql annotations is responsibility of the application developer,
SQL api just provides #getAnnotations() method to inquire the annotations.  Besides, There are some special built in annotations which are handled by the library

- @NAMED <NAME> used to name sql queries.
	SQLReader create SQL with the name defined in @NAMED annotation.    
- @:<bindingParameterName> used to define bindings. 
	SQLReader create SQL with these bindings and properties      


#####2. Binding Parameters

TBC

##### Main Components 

* SQLReader; Sql script parser to parsing any sql/plsql script context into collection of SQL instances. 
* STree Api; A data structure to build and modify structural strings dynamicly as an alternative to StringBuilder 
* SQL Api  ; An extension to Stree data structure to add sql support 
* Some utilities which handle with sql strings    

- It is not provide any abstraction or functionality for database access, deal only with sql and binding.



Compare
-------
Below contains examples to compare olives4j-sql with a few well known libraries and techniques

##### Sample User Input
```java
	 Long storeId=1L;
    String firstName="O";
    String lastName="";
    Boolean active=true;
    Date startDate=sdf.parse("2006-02-14");
    Date endDate=sdf.parse("2006-02-16");
```

##### Olives4j-sql

```java

	SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
	SQL sql = SQL.of("select * from customer where 1=1", //
				" and store_id",$(storeId), //
				" and first_name like", $(lastName), //
				" and active = ", $(active), //
				" and create_date between", $(sdf.parse(startDate)), "and", $(sdf.parse(endDate)));
	
	Connection conn=getConnection();
	PreparedStatement pstmto = conn.prepareStatement(sql.toString());
	sql.bindings().apply(pstmto);
	System.out.println("Execute query :"+sql);
	ResultSet rs = pstmto.executeQuery();
```
##### Plain Jdbc <a name="comparePlainJdbc"></a>

```java
	
	Connection connection = getConnection();
	StringBuilder buffer=new StringBuilder();
	buffer.append("select * from customer where 1=1");
	if(firstName!=null){
		buffer.append("first_name like ?");
	}
	if(lastName!=null){
		buffer.append("lastName like ?");
	}
	if(active!=null){
		buffer.append("active like ?");
	}
	if(startDate!=null){
		buffer.append("createDate between ? and ?");
	}
	
	PreparedStatement pstmt = connection
	.prepareStatement(buffer.toString());
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
```

##### Hibernate/JPA <a name="compareDBUtils"></a>

TBC

##### Hibernate/JPA <a name="compareHibernate"></a>

```java
	
	Session session=getCurrentSession();
	StringBuilder buffer=new StringBuilder();
	buffer.append("from customer where 1=1");
	if(firstName!=null){
		buffer.append("first_name like :firstName");
	}
	if(lastName!=null){
		buffer.append("lastName like :lastName");
	}
	if(active!=null){
		buffer.append("active like :active");
	}
	if(startDate!=null){
		buffer.append("createDate between :startDate and :endDate");
	}
	
	Query query=session.createQuery(buffer.toString());
	
	if(firstName!=null){
		query.setParameter("first_name",firstName);
	}
	if(lastName!=null){
		query.setParameter("last_name",lastName);
	}
	if(active!=null){
		query.setParameter("active",active);
	}
	if(startDate!=null){
		query.setParameter("startDate",startDate);
		query.setParameter("endDate",endDate);
	}
	
	System.out.println("Execute query :"+buffer.toString()+" with params first_name"+firstName+"...");
	List list = query.list();
```



Motivation
---------------

Although there are huge number of jdbc related libraries, most of them focus only abstraction of jdbc api and not provide much help for building sql dynamicly. 

Lets take a scenerio for a sql related change on an ordinary database project

* Open a sql ide
* Create/Update the sql query with some inline parameters
	
    ```sql
    select * 
    from customer 
    where 1=1 
    and store_id=1
    and first_name like 'A%'
    and active = true
    and create_date between sysdate-1 and sysdate+1
    ```
	
* Execute the query and test if it is correct
* Define the bind variables and convert inline parameters to bindings

 ```sql
	select * 
	from customer 
	where 1=1 
	and store_id=:store_id
	and first_name like :first_name
	and active = :active 
	and create_date between :create_date_from and :create_date_to
```
     
* Convert the sql to target java api, mostly it will be a concataneted string otherwise will be a dsl api
	
	[See the comparison for sample ](#compareHibernate)
	

Olives4j-sql aims to change the scenerio as below

* Keep the sql scripts on sql script files as an application resource
* Open the target sql script in a sql ide
* Create/Update the sql query with bindings and annotations
	
	```sql
	--@NAMED search_customer
	select * 
	from customer 
	where 1=1 
	and store_id = 1			--@:store_id optional
	and first_name like 'A%'	--@:firstname optional
	and active 	= true			--@:active optional
	and create_date between (sysdate - 1) --@:start_date
					and		(sysdate + 1) --@:end_date
	;
	```

* Execute the query and test if it is correct
* Save the sql script
* Update a few lines in java code if there are any binding change   


Status
---------------
	
Although this library is functinally complete, it is not ready for production yet.	
it needs more testing and feedback. You can use it for non production and test codes.
 
Download
---------------

TBC

License
---------------
 
    Copyright (c) 2016, Omer Dogan.  All rights reserved.
   
    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at
   
    http://www.apache.org/licenses/LICENSE-2.0
   
    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
