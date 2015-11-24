// Copyright - The University of Edinburgh 2011
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
@uk.ac.ed.epcc.webapp.Version("$Id: ConfigUsageRecordFactory.java,v 1.60 2014/09/15 14:32:20 spb Exp $")

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