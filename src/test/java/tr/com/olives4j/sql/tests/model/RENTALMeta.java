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

public class RENTALMeta {
	public  static RENTALMeta.COLUMNS RENTAL_ID=COLUMNS.RENTAL_ID;
	public  static  RENTALMeta.COLUMNS RENTAL_DATA=COLUMNS.RENTAL_DATE;
	public  static  RENTALMeta.COLUMNS INVENTORY_ID=COLUMNS.INVENTORY_ID;
	public  static  RENTALMeta.COLUMNS CUSTOMER_ID=COLUMNS.CUSTOMER_ID;
	public  static  RENTALMeta.COLUMNS RETURN_DATE=COLUMNS.RETURN_DATE;
	public  static  RENTALMeta.COLUMNS STAFF_ID=COLUMNS.STAFF_ID;
	public  static  RENTALMeta.COLUMNS LAST_UPDATE=COLUMNS.LAST_UPDATE;

	public static enum COLUMNS {
		RENTAL_ID, RENTAL_DATE, INVENTORY_ID, CUSTOMER_ID, RETURN_DATE, STAFF_ID, LAST_UPDATE
	}
	
}