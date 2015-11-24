// Copyright - The University of Edinburgh 2011
/*******************************************************************************
 * Copyright (c) - The University of Edinburgh 2010
 *******************************************************************************/
package uk.ac.ed.epcc.safe.accounting.charts;
import java.util.Date;

import uk.ac.ed.epcc.safe.accounting.Reduction;
import uk.ac.ed.epcc.safe.accounting.UsageRecord;
import uk.ac.ed.epcc.safe.accounting.properties.PropExpression;
import uk.ac.ed.epcc.safe.accounting.properties.PropertyTag;
import uk.ac.ed.epcc.webapp.AppContext;
import uk.ac.ed.epcc.webapp.charts.strategy.SetRangeMapper;
@uk.ac.ed.epcc.webapp.Version("$Id: UsageRecordSetTransform.java,v 1.5 2014/09/15 14:32:19 spb Exp $")


public class UsageRecordSetTransform<D extends Number> extends UsageRecordMapper<D> implements SetRangeMapper<UsageRecord>{
	public UsageRecordSetTransform(AppContext conn,int set,Reduction op,
			PropExpression<D> plot_property, PropertyTag<Date> start, PropertyTag<Date> end) {
		super(conn, op,plot_property, start, end);
		this.set=set;
	
	}
	private final int set;
	public int getSet(UsageRecord o) {
		return set;
	}

}