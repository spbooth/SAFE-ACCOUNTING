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

import uk.ac.ed.epcc.webapp.time.TimePeriod;

/** different overlap conditions
 * 
 * @author spb
 *
 */
public enum OverlapType {
	/** any overlap at all
	 * 
	 */
	ANY{
		public boolean overlaps(TimePeriod a, TimePeriod b){
			return a.getStart().before(b.getEnd()) && a.getEnd().after(b.getStart());
		}
	},	
	/**
	 *  object totally inside period
	 */
	INNER{
		public boolean overlaps(TimePeriod a, TimePeriod b){
			return !a.getStart().before(b.getStart()) && ! a.getEnd().after(b.getEnd());
		}
	},	
	/**
	 * object overlaps start but not end
	 */
	LOWER
	{
		public boolean overlaps(TimePeriod a, TimePeriod b){
			return a.getStart().before(b.getStart()) && a.getEnd().after(b.getStart()) &&  a.getEnd().before(b.getEnd());
		}
	},	
	/**
	 * object overlaps end but not start (not used in current implementations)
	 */
	UPPER
	{
		public boolean overlaps(TimePeriod a, TimePeriod b){
			return a.getStart().before(b.getEnd()) && a.getEnd().after(b.getEnd()) && ! a.getStart().before(b.getStart());
		}
	},	
	/**
	 * object encloses period (not used in current implementations)
	 */
	OUTER
	{
		public boolean overlaps(TimePeriod a, TimePeriod b){
			return a.getStart().before(b.getStart()) && a.getEnd().after(b.getEnd());
		}
	},	
	/**object overlaps end
	 * 
	 */
	UPPER_OUTER	{
		public boolean overlaps(TimePeriod a, TimePeriod b){
			return a.getStart().before(b.getEnd()) && ! a.getEnd().before(b.getEnd());
		}
	};
	
	public abstract boolean overlaps(TimePeriod obj, TimePeriod period);
}