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
			add(new SelectClause<>(point_prop, MatchCondition.GT,start));
		}
		if( end != null ){
			add(new SelectClause<>(point_prop, MatchCondition.LE,end));
		}
		lock();
	}
}