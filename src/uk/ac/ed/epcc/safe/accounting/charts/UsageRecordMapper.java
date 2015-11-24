// Copyright - The University of Edinburgh 2011
/*******************************************************************************
 * Copyright (c) - The University of Edinburgh 2010
 *******************************************************************************/
package uk.ac.ed.epcc.safe.accounting.charts;

import java.util.Date;

import uk.ac.ed.epcc.safe.accounting.IllegalReductionException;
import uk.ac.ed.epcc.safe.accounting.NumberReductionTarget;
import uk.ac.ed.epcc.safe.accounting.OverlapHandler;
import uk.ac.ed.epcc.safe.accounting.Reduction;
import uk.ac.ed.epcc.safe.accounting.UsageRecord;
import uk.ac.ed.epcc.safe.accounting.properties.InvalidExpressionException;
import uk.ac.ed.epcc.safe.accounting.properties.InvalidPropertyException;
import uk.ac.ed.epcc.safe.accounting.properties.PropExpression;
import uk.ac.ed.epcc.safe.accounting.properties.PropertyTag;
import uk.ac.ed.epcc.webapp.AppContext;
import uk.ac.ed.epcc.webapp.charts.strategy.RangeMapper;
/** Base class for mapping a UsageRecord to a time range.
 * 
 * 
 * @author spb
 *
 * @param <D> Type of output property
 */
@uk.ac.ed.epcc.webapp.Version("$Id: UsageRecordMapper.java,v 1.6 2015/03/10 16:56:02 spb Exp $")

public class UsageRecordMapper<D extends Number> implements RangeMapper<UsageRecord>{
	protected final  AppContext conn;
	protected final PropExpression<D> plot_property;
	protected final PropertyTag<Date> start_prop; // may be null if we only want to use end point
	protected final PropertyTag<Date> end_prop;
	protected final Reduction op;

	
	public UsageRecordMapper(AppContext conn,Reduction op,PropExpression<D> plot_property,PropertyTag<Date> start,PropertyTag<Date> end){
		this.conn=conn;
		this.op=op;
		this.plot_property = plot_property;
		this.start_prop=start;
		this.end_prop=end;
	}
	
	public boolean getUseOverlapp(){
		return start_prop != null;
	}
	

	public final float getOverlapp(UsageRecord o, Date p_start, Date p_end) {
		try {
			return OverlapHandler.getOverlap(o,NumberReductionTarget.getInstance(op, plot_property), start_prop, end_prop, p_start, p_end).floatValue();
		} catch (InvalidExpressionException e) {
			return 0.0F;
		} catch (IllegalReductionException e) {
			return 0.0F;
		}
	}

	public final  boolean overlapps(UsageRecord o, Date start, Date end) {
		return OverlapHandler.overlaps(o, start_prop, end_prop, start, end);
	}
    
}