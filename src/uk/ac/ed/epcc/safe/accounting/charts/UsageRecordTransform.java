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
import java.util.Vector;

import uk.ac.ed.epcc.safe.accounting.Reduction;
import uk.ac.ed.epcc.safe.accounting.UsageRecord;
import uk.ac.ed.epcc.safe.accounting.properties.PropExpression;
import uk.ac.ed.epcc.safe.accounting.properties.PropertyTag;
import uk.ac.ed.epcc.webapp.AppContext;
import uk.ac.ed.epcc.webapp.charts.strategy.LabelledSetRangeMapper;
/** A LabellerTransform for mapping UsageRecords to charts
 * 
 * @author spb
 *
 * @param <K>
 * @param <D>
 */


public class UsageRecordTransform<K, D extends Number> extends UsageRecordMapper<D> implements
		LabelledSetRangeMapper<UsageRecord> {
	
	final private PropertyKeyLabeller<K> labeller;
	public UsageRecordTransform(AppContext conn, Reduction op,PropExpression<K> key_property,
			PropExpression<D> plot_property,
			PropertyTag<Date> start, PropertyTag<Date> end,
			PropertyKeyLabeller<K> labeller
	) {
		super(conn, op,plot_property, start, end);
		this.labeller=labeller;
	}

	public Vector<String> getLabels() {
		return labeller.getLabels();
	}

	public int nSets() {
		return labeller.nSets();
	}

	public int getSet(UsageRecord o) {
		return labeller.getSet(o);
	}
}