//| Copyright - The University of Edinburgh 2015                            |
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
package uk.ac.ed.epcc.safe.accounting.charts;

import java.util.Date;

import uk.ac.ed.epcc.safe.accounting.NumberReductionTarget;
import uk.ac.ed.epcc.safe.accounting.OverlapHandler;
import uk.ac.ed.epcc.safe.accounting.ReductionTarget;
import uk.ac.ed.epcc.safe.accounting.UsageProducer;
import uk.ac.ed.epcc.safe.accounting.properties.PropExpression;
import uk.ac.ed.epcc.safe.accounting.selector.RecordSelector;
import uk.ac.ed.epcc.webapp.AppContext;
import uk.ac.ed.epcc.webapp.charts.strategy.RangeMapper;
import uk.ac.ed.epcc.webapp.logging.LoggerService;
/** A {@link RangeMapper} that queries a {@link UsageProducer} directly.
 * 
 * @author spb
 *
 */
public class OverlapUsageRecordRangeMapper implements RangeMapper<UsageProducer<?>> {

	public OverlapUsageRecordRangeMapper(AppContext conn,
			NumberReductionTarget tag,RecordSelector sel,
			PropExpression<Date> start,PropExpression<Date> end 
			 ) {
		super();
		this.conn=conn;
		this.start_prop = start;
		this.end_prop = end;
		this.tag = tag;
		this.sel=sel;
	}

	private final AppContext conn;
	private final PropExpression<Date> start_prop;
	private final PropExpression<Date> end_prop;
	private final NumberReductionTarget tag;
	private final RecordSelector sel;

	public float getOverlapp(UsageProducer<?> o, Date start, Date end) {
		OverlapHandler handler = new OverlapHandler(conn, o);
		try {
			return handler.getOverlapSum(tag, start_prop, end_prop, sel, start, end).floatValue();
		} catch (Exception e) {
			conn.getService(LoggerService.class).getLogger(getClass()).error("Problem getting overlap value",e);
			return 0.0F;
		}
	}

	public boolean overlapps(UsageProducer<?> o, Date start, Date end) {
		return true;
	}

}