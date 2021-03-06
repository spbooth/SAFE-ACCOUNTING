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
package uk.ac.ed.epcc.safe.accounting.charts;

import java.util.Date;

import uk.ac.ed.epcc.safe.accounting.IllegalReductionException;
import uk.ac.ed.epcc.safe.accounting.NumberReductionTarget;
import uk.ac.ed.epcc.safe.accounting.OverlapHandler;
import uk.ac.ed.epcc.safe.accounting.expr.ExpressionTargetContainer;
import uk.ac.ed.epcc.safe.accounting.properties.InvalidExpressionException;
import uk.ac.ed.epcc.safe.accounting.properties.InvalidPropertyException;
import uk.ac.ed.epcc.safe.accounting.properties.PropExpression;
import uk.ac.ed.epcc.safe.accounting.properties.PropertyTag;
import uk.ac.ed.epcc.webapp.AppContext;
import uk.ac.ed.epcc.webapp.charts.strategy.RangeMapper;
import uk.ac.ed.epcc.webapp.jdbc.expr.Reduction;
import uk.ac.ed.epcc.webapp.time.Period;
/** Base class for mapping a UsageRecord to a time range.
 * 
 * 
 * @author spb
 *
 * @param <D> Type of output property
 */


public class UsageRecordMapper<D extends Number> implements RangeMapper<ExpressionTargetContainer>{
	protected final  AppContext conn;
	protected final PropExpression<D> plot_property;
	protected final PropExpression<Date> start_prop; // may be null if we only want to use end point
	protected final PropExpression<Date> end_prop;
	protected final Reduction op;

	
	public UsageRecordMapper(AppContext conn,Reduction op,PropExpression<D> plot_property,PropExpression<Date> start,PropExpression<Date> end){
		this.conn=conn;
		this.op=op;
		this.plot_property = plot_property;
		this.start_prop=start;
		this.end_prop=end;
	}
	
	public boolean getUseOverlapp(){
		return start_prop != null;
	}
	

	public final float getOverlapp(ExpressionTargetContainer o, Date p_start, Date p_end) throws IllegalReductionException, InvalidExpressionException {
		
		NumberReductionTarget r = NumberReductionTarget.getInstance(op, plot_property);
		if( start_prop == null &&  op.equals(Reduction.AVG)) {
			// This is not a time average as only one time bound
			// won't give the right result if we cast a AverageValue to
			// float
			throw new IllegalReductionException("Reduction "+op+" generates non-native type");
		}
		return OverlapHandler.getOverlap(o,r, start_prop, end_prop, new Period(p_start, p_end)).floatValue();
	}

	public final  boolean overlapps(ExpressionTargetContainer o, Date start, Date end) {
		return OverlapHandler.overlaps(o, start_prop, end_prop, start, end);
	}
    
}