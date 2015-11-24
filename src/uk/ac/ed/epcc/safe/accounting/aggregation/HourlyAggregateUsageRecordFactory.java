// Copyright - The University of Edinburgh 2011
/*******************************************************************************
 * Copyright (c) - The University of Edinburgh 2010
 *******************************************************************************/
package uk.ac.ed.epcc.safe.accounting.aggregation;

import java.util.Calendar;
import java.util.Date;

import uk.ac.ed.epcc.safe.accounting.db.UsageRecordFactory;
import uk.ac.ed.epcc.webapp.AppContext;
/** A {@link AggregateUsageRecordFactory} that aggregates on hour boundaries.
 * 
 * @author spb
 *
 */
@uk.ac.ed.epcc.webapp.Version("$Id: HourlyAggregateUsageRecordFactory.java,v 1.7 2014/09/15 14:32:18 spb Exp $")


public class HourlyAggregateUsageRecordFactory extends
		AggregateUsageRecordFactory {
    Calendar scratch = Calendar.getInstance();
	public HourlyAggregateUsageRecordFactory(AppContext c, String table,UsageRecordFactory fac) {
		super(c, table,fac);
		
	}
	public HourlyAggregateUsageRecordFactory(AppContext c, String table) {
		super(c, table,null);
	}
	@Override
	public Date mapEnd(Date point) {
		scratch.setTime(point);
		scratch.set(Calendar.MILLISECOND,0);
		scratch.set(Calendar.SECOND,0);
		scratch.set(Calendar.MINUTE, 0);
		if( scratch.getTime().before(point)){
		   scratch.add(Calendar.HOUR,1);
		}
		return scratch.getTime();
	}

	@Override
	public Date mapStart(Date point) {
		scratch.setTime(point);
		scratch.set(Calendar.MILLISECOND,0);
		scratch.set(Calendar.SECOND,0);
		scratch.set(Calendar.MINUTE, 0);
		if( ! scratch.getTime().before(point)){
			scratch.add(Calendar.HOUR,-1);
		}
		return scratch.getTime();
	}

	

}