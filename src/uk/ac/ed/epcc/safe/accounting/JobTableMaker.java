// Copyright - The University of Edinburgh 2011
/*******************************************************************************
 * Copyright (c) - The University of Edinburgh 2010
 *******************************************************************************/
package uk.ac.ed.epcc.safe.accounting;

import java.util.List;

import uk.ac.ed.epcc.webapp.AppContext;
/** Class to build a table of per-job information.
 * 
 * @author spb
 *
 */
@uk.ac.ed.epcc.webapp.Version("$Id: JobTableMaker.java,v 1.34 2014/09/15 14:32:17 spb Exp $")

public class JobTableMaker<UR extends UsageRecord> extends ExpressionTargetTableMaker<UR, UsageProducer<UR>>{
	
	public JobTableMaker(AppContext c,UsageProducer up){
		super(c,up);
	}
	public JobTableMaker(AppContext c,UsageProducer up, List<ColName> props){
	    this(c,up);
		for(ColName col : props){
			addColumn(col);
		}
	}
	
	
	
	@Override
	protected Object makeKey(UR t) {
		return t.getKey();
	}
    
}