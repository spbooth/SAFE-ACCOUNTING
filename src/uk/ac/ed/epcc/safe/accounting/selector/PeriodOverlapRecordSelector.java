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
package uk.ac.ed.epcc.safe.accounting.selector;

import java.util.Date;

import uk.ac.ed.epcc.safe.accounting.UsageProducer;
import uk.ac.ed.epcc.safe.accounting.properties.PropExpression;
import uk.ac.ed.epcc.webapp.time.Period;
import uk.ac.ed.epcc.webapp.time.TimePeriod;
/** Select a set of records that overlap with a time period.
 * This could be done with {@link SelectClause} but this allows
 * underlying classes to optimise the selection using knowledge of the
 * underlying data.
 * 
 * 
 * The {@link PeriodOverlapRecordSelector} includes an advisory cutoff value
 * (length of the longest known record) but optionally a {@link UsageProducer} can ignore this
 * and optimise using its own table specific value.
 * @author spb
 *
 */
public class PeriodOverlapRecordSelector implements RecordSelector {
	
	/** create a {@link PeriodOverlapRecordSelector}
	 * 
	 * @param period	Period to overlap
	 * @param start		Start property may be null
	 * @param end		End property
	 * @param type      {@link OverlapType} not used if start is null
	 * @param cutoff 	longest record length, use zero for unknown
	 */
	public PeriodOverlapRecordSelector(TimePeriod period,
			PropExpression<Date> start,
			PropExpression<Date> end,
			OverlapType type, long cutoff) {
		super();
		this.period = new Period(period);
		this.start = start;
		this.end = end;
		this.type = type;
		this.cutoff=cutoff;
	}
	/** make a selector that selects any record that overlaps
	 * in any way with the period.
	 * 
	 * @param period
	 * @param start
	 * @param end
	 */
	public PeriodOverlapRecordSelector(TimePeriod period,
			PropExpression<Date> start,
			PropExpression<Date> end
			){
		this(period,start,end,OverlapType.ANY,0L);
	}
	/** simple check for single property within period
	 * 
	 * @param period
	 * @param end
	 */
	public PeriodOverlapRecordSelector(TimePeriod period,
			PropExpression<Date> end
			){
		this(period,null,end,OverlapType.ANY,0L);
	}

		private final Period period;
		// USe PropertyTag to ensure equals works.
		private final PropExpression<Date> start;
		private final PropExpression<Date> end;
		private final OverlapType type;
		private final long cutoff; // advisary only
	
		public final <R> R visit(SelectorVisitor<R> visitor) throws Exception {
		return visitor.visitPeriodOverlapRecordSelector(this);
	}

		
		
		public final Period getPeriod() {
			return period;
		}


		public final PropExpression<Date> getStart() {
			return start;
		}
		

		public final PropExpression<Date> getEnd() {
			return end;
		}

		public final OverlapType getType(){
			return type;
		}

		public PeriodOverlapRecordSelector copy() {
			return this;
		}
		
		public long getCutoff(){
			return cutoff;
		}
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + (int) (cutoff ^ (cutoff >>> 32));
			result = prime * result + ((end == null) ? 0 : end.hashCode());
			result = prime * result
					+ ((period == null) ? 0 : period.hashCode());
			result = prime * result + ((start == null) ? 0 : start.hashCode());
			result = prime * result + ((type == null) ? 0 : type.hashCode());
			return result;
		}
		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			PeriodOverlapRecordSelector other = (PeriodOverlapRecordSelector) obj;
			if (cutoff != other.cutoff)
				return false;
			if (end == null) {
				if (other.end != null)
					return false;
			} else if (!end.equals(other.end))
				return false;
			if (period == null) {
				if (other.period != null)
					return false;
			} else if (!period.equals(other.period))
				return false;
			if (start == null) {
				if (other.start != null)
					return false;
			} else if (!start.equals(other.start))
				return false;
			if (type != other.type)
				return false;
			return true;
		}

}