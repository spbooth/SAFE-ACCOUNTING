//| Copyright - The University of Edinburgh 2011                            |
//|                                                                         |
//| Licensed under the Apache License, Version 2.0 (the "License");         |
//| you may not use this file except in compliance with the License.        |
//| You may obtain a copy of the License at                                 |
//|                                                                         |
//|    http://www.apache.org/licenses/LICENSE-2.0                           |
//|                                                                         |
//| Unless required by applicable law or agreed to in writing, software     |
//| distributed under the License is distributed on an "AS IS" BASIS,       |
//| WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.|
//| See the License for the specific language governing permissions and     |
//| limitations under the License.                                          |
/*******************************************************************************
 * Copyright (c) - The University of Edinburgh 2010
 *******************************************************************************/
package uk.ac.ed.epcc.safe.accounting.db;

import uk.ac.ed.epcc.safe.accounting.expr.PropExpressionMap;
import uk.ac.ed.epcc.safe.accounting.properties.MultiFinder;
import uk.ac.ed.epcc.webapp.AppContext;
import uk.ac.ed.epcc.webapp.jdbc.table.DataBaseHandlerService;
import uk.ac.ed.epcc.webapp.jdbc.table.TableSpecification;
import uk.ac.ed.epcc.webapp.logging.Logger;
import uk.ac.ed.epcc.webapp.logging.LoggerService;
import uk.ac.ed.epcc.webapp.model.data.Exceptions.DataFault;

/** Base class for UsageProducers that directly parse input.
 * 
 * Most of the specific behaviour is delegated to 
 * UsageRecordParser, UsageRecordPolicy objects which need to be defiend by sub-classes
 * 
 * By default this class will populate the AccessorMap by looking for database field names that match
 * PropertyTag names. This can be customised by setting a property
 * <pre>
 * accounting.[table-name].[field-name]=[property-name]
 * </pre>
 * The UsageRecordParser and UsageRecordPolicy objects are also given the ability 
 * define derived properties as simple expressions of other properties.
 * 
 * 
 * @author spb
 * @param <T> class of UsageRecord
 * @param <R> type of parser IR
 *
 */

public abstract class ParseUsageRecordFactory<T extends UsageRecordFactory.Use,R> extends UsageRecordFactory<T> implements  AccessorContributer<T> {


	
	
	protected ParseUsageRecordFactory() {
		super();
	}
	
    	
	

	
	/** bootstrap the table.
	 * This is called explicitly be the create accounting table action even if 
	 * auto_create tables is off.
	 * 
	 * @param c
	 * @param table
	 * @param spec
	 * @throws DataFault
	 */
	public static  void bootstrapTable(AppContext c, String table,
			TableSpecification spec) throws DataFault {

		if( spec == null ){
			Logger log = c.getService(LoggerService.class).getLogger(ParseUsageRecordFactory.class);
			log.error("No table specification in bootstrapTable");
			return;
		}
		DataBaseHandlerService dbh = c.getService(DataBaseHandlerService.class);
		if( dbh == null ){
			return;
		}
		dbh.createTable(table, spec);
	}
	

  
	
	  

	/** Extension point to allow custom Accessors and the corresponding PropertyFinders to be added.
	 * Derived properties can be added directly to the  
	 * @param mapi2 AccessorMap modified 
	 * @param finder MultiFinder modified
	 */
	public  void customAccessors(AccessorMap<T> mapi2, MultiFinder finder, PropExpressionMap derived) {
		
	}



}