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
package uk.ac.ed.epcc.safe.accounting.update;

import java.util.Date;



import uk.ac.ed.epcc.safe.accounting.expr.BinaryPropExpression;
import uk.ac.ed.epcc.safe.accounting.expr.ConstPropExpression;
import uk.ac.ed.epcc.safe.accounting.expr.DurationPropExpression;
import uk.ac.ed.epcc.safe.accounting.expr.MilliSecondDatePropExpression;
import uk.ac.ed.epcc.safe.accounting.expr.PropExpressionMap;
import uk.ac.ed.epcc.safe.accounting.expr.PropertyCastException;
import uk.ac.ed.epcc.safe.accounting.properties.MultiFinder;
import uk.ac.ed.epcc.safe.accounting.properties.PropertyFinder;
import uk.ac.ed.epcc.safe.accounting.properties.StandardProperties;
import uk.ac.ed.epcc.webapp.AppContext;
import uk.ac.ed.epcc.webapp.exceptions.ConsistencyError;
import uk.ac.ed.epcc.webapp.jdbc.expr.Operator;
/** Base class for implementing parsers.
 * This add the standard properties to {@link AbstractPropertyContainerParser}
 * and defines some useful methods.
 * @author spb
 *
 */
public abstract class BaseParser extends AbstractPropertyContainerParser {
	public static final long SECOND_RANGE_CUTOFF = 500000000000L; // roughly 1985-11-05 in milliseconds 16000+ in seconds
	/** parse a timestamp string (in seconds or milliseconds from unix epoch) and convert to Date.
	 * This will handle either millisecond or second resolutions (SGE changed resolution in later versions).
	 * It parses as a float so also handles fractional values.
	 * @param time Timestamp string.
	 * @return Date
	 */
	public final Date readTime(String time) {
	    double stamp = Double.parseDouble(time.trim());
	    if( stamp < SECOND_RANGE_CUTOFF) {
	    	// This must be a time in seconds, otherwise assume milliseconds
	    	stamp = stamp * 1000.0;
	    }
	    long millis = (long)stamp;
		Date date = new Date(millis);
		assert(date.getTime() == millis);
		return date;
	}
	public final Long readLong(String val) {
	    
	    return Long.valueOf(val.trim());
	}
	public final Integer readInteger(String val) {
	    
	    return Integer.valueOf(val.trim());
	}
	public final Double readDouble(String val) {
	    
	    return Double.valueOf(val.trim());
	}
	
	@Override
	public PropExpressionMap getDerivedProperties(PropExpressionMap res){
	    try {
			res.put(StandardProperties.RUNTIME_PROP, new BinaryPropExpression(
					new MilliSecondDatePropExpression(StandardProperties.ENDED_PROP),
					Operator.SUB,
					new MilliSecondDatePropExpression(StandardProperties.STARTED_PROP)));
			
			res.put(StandardProperties.ELAPSED_PROP,new BinaryPropExpression(StandardProperties.RUNTIME_PROP, Operator.DIV, new ConstPropExpression<>(Long.class,1000L)));
			res.put(StandardProperties.HOURS_PROP,new BinaryPropExpression(StandardProperties.RUNTIME_PROP, Operator.DIV, new ConstPropExpression<>(Long.class,3600000L)));
			res.put(StandardProperties.COMMAND,StandardProperties.EXECUTABLE);
			res.put(StandardProperties.DURATION_PROP, new DurationPropExpression(StandardProperties.STARTED_PROP, StandardProperties.ENDED_PROP));
		} catch (PropertyCastException e) {
			// should never happen
			throw new ConsistencyError("Cast check failed for built in propexpression",e);
		}
	    return res;
	}
	
	 
	
	  
	   
	public PropertyFinder initFinder(AppContext conn, PropertyFinder prev, String table) {
		MultiFinder finder = new MultiFinder();
		finder.addFinder(StandardProperties.time);
		finder.addFinder(StandardProperties.base);
		return finder;
	}
	
}