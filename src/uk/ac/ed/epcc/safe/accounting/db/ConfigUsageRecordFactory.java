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

import uk.ac.ed.epcc.safe.accounting.update.ConfigPlugInOwner;
import uk.ac.ed.epcc.webapp.AppContext;
/** A generic UsageRecordFactory configured from the Config properties

 * @see ConfigPlugInOwner
 *  
 * 
 * @author spb
 * @param <T> type of usage record
 * @param <R> type of parser IR
 *
 */


public class ConfigUsageRecordFactory<T extends UsageRecordFactory.Use,R> extends ParseUsageRecordFactory<T,R> {
	public final ConfigUsageRecordParseTargetPlugIn<T, R> parse_plugin = new ConfigUsageRecordParseTargetPlugIn<T,R>(this);

	public ConfigUsageRecordFactory(AppContext ctx, String table){
    	super();
    	setContext(ctx,table);
    }

	
	
	

	
	

}