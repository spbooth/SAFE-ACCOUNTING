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
package uk.ac.ed.epcc.safe.accounting.aggregation;

import java.util.Calendar;
import java.util.Date;

import uk.ac.ed.epcc.safe.accounting.db.UsageRecordFactory;
import uk.ac.ed.epcc.webapp.AppContext;

/** An {@link AggregateUsageRecordFactory} that aggregates on day boundaries.
 * 
 * @author spb
 *
 */



public final class DailyAggregateUsageRecordFactory extends
		AggregateUsageRecordFactory {
    Calendar scratch = Calendar.getInstance();
	public DailyAggregateUsageRecordFactory(AppContext c, String table, UsageRecordFactory fac) {
		super(fac);
		setContext(c, table);
		
	}
	public DailyAggregateUsageRecordFactory(AppContext c, String table) {
		super(null);
		setContext(c, table);
	}
	@Override
	public Date mapEnd(Date point) {
		scratch.setTime(point);
		scratch.set(Calendar.MILLISECOND,0);
		scratch.set(Calendar.SECOND,0);
		scratch.set(Calendar.MINUTE, 0);
		scratch.set(Calendar.HOUR_OF_DAY,0);
		if( scratch.getTime().before(point)){
		   scratch.add(Calendar.DAY_OF_YEAR,1);
		}
		return scratch.getTime();
	}

	@Override
	public Date mapStart(Date point) {
		scratch.setTime(point);
		scratch.set(Calendar.MILLISECOND,0);
		scratch.set(Calendar.SECOND,0);
		scratch.set(Calendar.MINUTE, 0);
		scratch.set(Calendar.HOUR_OF_DAY,0);
		if( ! scratch.getTime().before(point)){
			scratch.add(Calendar.DAY_OF_YEAR,-1);
		}
		return scratch.getTime();
	}

	

}