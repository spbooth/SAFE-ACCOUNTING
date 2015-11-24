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
