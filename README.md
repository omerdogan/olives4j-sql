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


###Overview

Olives4j-sql includes ;

* SQLReader; Sql script parser to parsing any sql/plsql script context into collection of SQL instances. 
* STree Api; A data structure to build and modify structural strings dynamicly as an alternative to StringBuilder 
* SQL Api  ; An extension to Stree data structure to add sql support 
* Some utilities which handle with sql strings    

- It is not provide any abstraction or functionality for database access, deal only with sql and binding.

Usage
-----

There are three technique you can use to create dynamic queries with olives4j-sql

1. Create sql programaticly by providing bindings while building sql

```java

	import static tr.com.olives4j.sql.SQL.$;
	SQL sql = SQL.of("select * from customer where 1=1", 
			" and store_id   ="         ,$(null).optional(), 
			" and first_name like "     ,$("a%"), 
			" and active     = "        ,$(true), 
			" and create_date between " ,$(sdf.parse("2006-02-14")), " and ",sdf.parse("2006-02-16")));
			
```

2. Create sql programaticly with named parameters and set binding parameter values later

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


3. Use an external sql script file 

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

Compare
-------

An application working with a relational data use one or more of the techniques below.
+ plain jdbc. 
+ an abstraction api (apache dbutils)
+ object mapping api with sql or sql like query language (hql,jpql)
+ ~~object mapping api without any query.~~

Below contains examples to compare olives4j-sql with a few well known libraries used for these techniques

##### Sample User Input
```java

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
				" and store_id =:store_id ", //
				" and first_name like ", $("a%"), //
				" and active = ", $(true), //
				" and create_date between", $(sdf.parse("2006-02-14")), "and", $(sdf.parse("2006-02-16")));
	
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

External SQL and SQL Annotation
-----

Olives4j-sql is intended to be able to parse any ordinary sql/plsql script file.
it use an extension concept, SQL annotation, which allow adding extra behaviour to sql scripts to integrate sql with code easier
The SQL annotation is like javadoc annotations

- SQL annotations may be defined in line or block comments 
- SQL comments must be started with @ sign to be annotation and started with @: binding annotation  
		
	--@[annotation_name] [annotation parameters] 
	/*@[annotation_name] [annotation parameters]

	--@:[binding_parameter_name] [binding parameters] 
	/*@:[binding_parameter_name] [binding parameters]
    
There are some special annotations provided by default with the olives4j-sql. 

- @NAMED <NAME> used to name sql queries.
	SQLReader create SQL with the name defined in @NAMED annotation.    
- @:<bindingParameterName> used to define bindings. 
	SQLReader create SQL with these bindings and properties      

SQL api provides #getAnnotations() method to inquire the annotations.


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
* Define the binding parameters and convert inline parameters to bindings

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
