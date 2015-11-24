// Copyright - The University of Edinburgh 2011
/*******************************************************************************
 * Copyright (c) - The University of Edinburgh 2010
 *******************************************************************************/
package uk.ac.ed.epcc.safe.accounting.charts;

import uk.ac.ed.epcc.webapp.AppContext;
import uk.ac.ed.epcc.webapp.content.Labeller;
@uk.ac.ed.epcc.webapp.Version("$Id: Power2RangeLabeller.java,v 1.4 2014/09/15 14:32:19 spb Exp $")

/** Labeller to map a number to a power of two range.
 * @author spb
 *
 * @param <T>
 */
public class Power2RangeLabeller<T extends Number> implements Labeller<T,Power2RangeLabeller.Range> {

	public static class Range implements Comparable<Range>{
		public Range(int lower, int upper) {
			super();
			this.lower = lower;
			this.upper = upper;
		}
		private final int lower;
		private final int upper;
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + lower;
			result = prime * result + upper;
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
			Range other = (Range) obj;
			if (lower != other.lower)
				return false;
			if (upper != other.upper)
				return false;
			return true;
		}
		@Override
		public String toString() {
			if( lower == upper){
				return Integer.toString(lower);
			}
			return ""+lower+"-"+upper;
		}
		public int compareTo(Range arg0) {
			if( lower < arg0.lower){
				return -1;
			}
			if( lower > arg0.lower){
				return +1;
			}
			if( upper < arg0.upper){
				return -1;
			}
			if( upper > arg0.upper){
				return +1;
			}
			return 0;
		}
		
	}
	public Range getLabel(AppContext conn, T key) {
		int i=key.intValue();
		int upper=1;
		while(upper< i){
			upper*=2;
		}
		return new Range((upper/2)+1,upper);
	}
	public boolean accepts(Object o) {
		if( o != null && o instanceof Number){
			return true;
		}
		return false;
	}
	public Class<? super Range> getTarget(){
		return Range.class;
	}
}