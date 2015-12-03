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

import uk.ac.ed.epcc.safe.accounting.properties.PropertyFinder;
import uk.ac.ed.epcc.safe.accounting.update.ConfigPlugInOwner;
import uk.ac.ed.epcc.safe.accounting.update.PlugInOwner;
import uk.ac.ed.epcc.webapp.AppContext;
/** A generic UsageRecordFactory configured from the Config properties
 * <ul>
 * <li> policies.<i>table</i> - list of policy class names </li>
 * <li> class.parser.<i>table</i> - parser class name </li>
 * <li> unique-properties.<i>table</i> - list of property names </li>
 * </ul>
 * If two records have all the unique properties the same they are considered to be
 * duplicates. If the property is not set the parser may be able to provide a default
 * set of properties. 
 * @see UsageRecordParseTargetPlugIn
 *  
 * 
 * @author spb
 * @param <T> type of usage record
 *
 */


public class ConfigUsageRecordFactory<T extends UsageRecordFactory.Use> extends ParseUsageRecordFactory<T> {

	public ConfigUsageRecordFactory(AppContext ctx, String table){
    	super(ctx,table);
    	
    	

    }
	
	
	
	@Deprecated
	public String getDescription(){
    	return getContext().getInitParameter("description."+getConfigTag(),getConfigTag());
    }
	@Override
	protected PlugInOwner makePlugInOwner(AppContext c,PropertyFinder prev, String tag) {
		// For accounting record tables default to no parser
		// This will supress auto-table generation for unconfigured tables.
		// This is important as we may try to construct this class based on
		// a user input tag and we don't want to auto-create randomly named tables.
		return new ConfigPlugInOwner(c, prev,tag);
	}



	
	

}