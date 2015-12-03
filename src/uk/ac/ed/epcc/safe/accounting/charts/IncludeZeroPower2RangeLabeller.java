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

import uk.ac.ed.epcc.webapp.AppContext;
import uk.ac.ed.epcc.webapp.content.Labeller;


/** Labeller to map a number to a power of two range. The lowest range is always formatted as <b>&le; max</b>
 * @author spb
 *
 * @param <T>
 */
public class IncludeZeroPower2RangeLabeller<T extends Number> implements Labeller<T,IncludeZeroPower2RangeLabeller<T>.Range> {

	private Range lowest;
	public class Range implements Comparable<Range>{
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
			if( lowest != null && this.equals(lowest)){
				return "<="+upper;
			}
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
		Range range = new Range((upper/2)+1,upper);
		if(  lowest == null || range.compareTo(lowest) < 0){
			lowest=range;
		}
		return range;
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