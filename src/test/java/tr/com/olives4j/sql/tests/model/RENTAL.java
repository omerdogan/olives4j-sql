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
package tr.com.olives4j.sql.tests.model;
import static tr.com.olives4j.sql.SQL.$;

import tr.com.olives4j.sql.SQL;


public class RENTAL {
	long RENTAL_ID;
	java.sql.Timestamp RENTAL_DATE;
	long INVENTORY_ID;
	long CUSTOMER_ID;
	java.sql.Timestamp RETURN_DATE;
	long STAFF_ID;
	java.sql.Timestamp LAST_UPDATE;

	public RENTAL() {
		super();
	}

	public SQL sql() {
		SQL sql = new SQL();
		sql.append("SELECT * FROM RENTAL"//
				, " WHERE 1=1 " //
				, "  AND RENTAL_ID     =", $(RENTAL_ID)//
				, "  AND RENTAL_DATE   =", $(RENTAL_DATE)//
				, "  AND INVENTORY_ID  =", $(INVENTORY_ID)//
				, "  AND CUSTOMER_ID   =", $(CUSTOMER_ID)//
				, "  AND RETURN_DATE   =", $(RETURN_DATE)//
				, "  AND STAFF_ID      =", $(STAFF_ID)//
				, "  AND LAST_UPDATE   =", $(LAST_UPDATE));

		return sql;
	}

	@Override
	public String toString() {
		return "RENTAL [RENTAL_ID=" + RENTAL_ID + ", RENTAL_DATE=" + RENTAL_DATE + ", INVENTORY_ID=" + INVENTORY_ID
				+ ", CUSTOMER_ID=" + CUSTOMER_ID + ", RETURN_DATE=" + RETURN_DATE + ", STAFF_ID=" + STAFF_ID
				+ ", LAST_UPDATE=" + LAST_UPDATE + "]";
	}
}
