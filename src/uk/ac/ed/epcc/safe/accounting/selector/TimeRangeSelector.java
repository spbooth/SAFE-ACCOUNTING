// Copyright - The University of Edinburgh 2011
package uk.ac.ed.epcc.safe.accounting.selector;

import java.util.Date;

import uk.ac.ed.epcc.safe.accounting.properties.PropertyTag;
import uk.ac.ed.epcc.webapp.jdbc.filter.MatchCondition;

/** convenience class to put the conventions for selecting
 * a record in a date range into a single place.
 * 
 * @author spb
 *
 */
@uk.ac.ed.epcc.webapp.Version("$Id: TimeRangeSelector.java,v 1.3 2014/09/15 14:32:29 spb Exp $")

public class TimeRangeSelector extends AndRecordSelector {
	/**
	 * 
	 */
	private static final long serialVersionUID = 2967109750170380868L;

	
	public TimeRangeSelector(RecordSelector sel, PropertyTag<Date> point_prop , Date start, Date end){
		if( sel != null ){
			add(sel);
		}
		if( start != null ){
			add(new SelectClause<Date>(point_prop, MatchCondition.GT,start));
		}
		if( end != null ){
			add(new SelectClause<Date>(point_prop, MatchCondition.LE,end));
		}
		lock();
	}
}