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
package uk.ac.ed.epcc.safe.accounting.expr.parse;

import java.util.Date;
import java.util.LinkedList;

import uk.ac.ed.epcc.safe.accounting.expr.ArrayFuncPropExpression;
import uk.ac.ed.epcc.safe.accounting.expr.ConstPropExpression;
import uk.ac.ed.epcc.safe.accounting.expr.ConvertMillisecondToDatePropExpression;
import uk.ac.ed.epcc.safe.accounting.expr.DoubleCastPropExpression;
import uk.ac.ed.epcc.safe.accounting.expr.DurationCastPropExpression;
import uk.ac.ed.epcc.safe.accounting.expr.DurationPropExpression;
import uk.ac.ed.epcc.safe.accounting.expr.DurationSecondsPropExpression;
import uk.ac.ed.epcc.safe.accounting.expr.IntPropExpression;
import uk.ac.ed.epcc.safe.accounting.expr.LocatePropExpression;
import uk.ac.ed.epcc.safe.accounting.expr.LongCastPropExpression;
import uk.ac.ed.epcc.safe.accounting.expr.MilliSecondDatePropExpression;
import uk.ac.ed.epcc.safe.accounting.expr.NamePropExpression;
import uk.ac.ed.epcc.safe.accounting.expr.ParseException;
import uk.ac.ed.epcc.safe.accounting.expr.PropertyCastException;
import uk.ac.ed.epcc.safe.accounting.expr.StringPropExpression;
import uk.ac.ed.epcc.safe.accounting.properties.PropExpression;
import uk.ac.ed.epcc.safe.accounting.reference.ReferenceExpression;
import uk.ac.ed.epcc.webapp.jdbc.expr.ArrayFunc;
import uk.ac.ed.epcc.webapp.model.data.Duration;


/** Conversion functions on propexpressions.
 * 
 * @author spb
 *
 */
public enum Keywords {
	/** Generates a {@link NamePropExpression}
	 * 
	 */
	NAME {
		@SuppressWarnings("unchecked")
		@Override
		public PropExpression getExpression(LinkedList<PropExpression> list)
				throws ParseException {
			checkSingleArg(list);
			PropExpression inner = list.getFirst();
			if( inner instanceof ReferenceExpression){
				return new NamePropExpression((ReferenceExpression) inner);
			}
			throw new ParseException("Expression "+inner.toString()+" not a reference");
		}

		
	},
	/** generate a {@link StringPropExpression}
	 * 
	 */
	STRING {
		@SuppressWarnings("unchecked")
		@Override
		public PropExpression getExpression(LinkedList<PropExpression> inner)
				throws ParseException {
			checkSingleArg(inner);
			return new StringPropExpression(inner.getFirst());
		}
	},
	/** generate a {@link IntPropExpression}
	 * 
	 */
	INT {

		@SuppressWarnings("unchecked")
		@Override
		public PropExpression getExpression(LinkedList<PropExpression> inner)
				throws ParseException {
			checkSingleArg(inner);
			return new IntPropExpression(inner.getFirst());
		}
	
	},
	/** generate a {@link DurationCastPropExpression}
	 * 
	 */
	DURATION_CAST {

		@SuppressWarnings("unchecked")
		@Override
		public PropExpression getExpression(LinkedList<PropExpression> inner)
				throws ParseException {
			checkSingleArg(inner);
			return new DurationCastPropExpression(cast(Number.class,inner.getFirst()),1L);
		}
	
	},
	DURATION {
		@SuppressWarnings("unchecked")
		@Override
		public PropExpression getExpression(LinkedList<PropExpression> inner)
				throws ParseException {
			int argcount = inner.size();
			if( argcount != 2) {
				throw new ParseException("Wrong number of arguments got:"+argcount+" expected 2");
			}
			return new DurationPropExpression(cast(Date.class,inner.get(0)), cast(Date.class,inner.get(1)));
		}
	},
	LONG_CAST {

		@SuppressWarnings("unchecked")
		@Override
		public PropExpression getExpression(LinkedList<PropExpression> inner)
				throws ParseException {
			checkSingleArg(inner);
			return new LongCastPropExpression(inner.getFirst());
		}
	
	},
	DOUBLE_CAST {

		@SuppressWarnings("unchecked")
		@Override
		public PropExpression getExpression(LinkedList<PropExpression> inner)
				throws ParseException {
			checkSingleArg(inner);
			return new DoubleCastPropExpression(inner.getFirst());
		}
	
	},
	DURATION_SECONDS {

		@SuppressWarnings("unchecked")
		@Override
		public PropExpression getExpression(LinkedList<PropExpression> inner)
				throws ParseException {
			checkSingleArg(inner);
			return new DurationSecondsPropExpression(cast(Duration.class,inner.getFirst()));
		}
	
	},
	MILLIS {

		@SuppressWarnings("unchecked")
		@Override
		public PropExpression getExpression(LinkedList<PropExpression> inner)
				throws ParseException {
			checkSingleArg(inner);
			try {
				return new MilliSecondDatePropExpression(cast(Date.class,inner.getFirst()));
			} catch (PropertyCastException e) {
				throw new ParseException(e);
			}
		}
	
	},
	DATE {

		@SuppressWarnings("unchecked")
		@Override
		public PropExpression getExpression(LinkedList<PropExpression> inner)
				throws ParseException {
			checkSingleArg(inner);
			
			return new ConvertMillisecondToDatePropExpression(cast(Number.class,inner.getFirst()));
			
		}
	
	},
	/** generate a {@link LocatePropExpression}
	 * arguments:
	 * <ul>
	 * <li> string to search for</li>
	 * <li> string to search in</li>
	 * <li> start position of search (from 1)</li>
	 * </ul>
	 */
	LOCATE {
		@SuppressWarnings("unchecked")
		@Override
		public PropExpression getExpression(LinkedList<PropExpression> inner)
				throws ParseException {
			PropExpression<Number> pos;
			int argcount = inner.size();
			if( argcount < 2 || argcount > 3) {
				throw new ParseException("Wrong number of arguments got:"+argcount+" expected 2 or 3, (search-val, string-to-search [, start-pos(indexes from 1)])");
			}
			if( argcount == 2) {
				pos = new ConstPropExpression(Number.class, Integer.valueOf(1));
			}else {
				pos = cast(Number.class,inner.getLast());
			}
			return new LocatePropExpression(cast(String.class,inner.getFirst()),cast(String.class,inner.get(1)),pos);
		}
	},
	/** return the largest of a set of expressions
	 * 
	 */
	GREATEST{
		public PropExpression getExpression(LinkedList<PropExpression> inner){
			return ArrayFuncPropExpression.makeArrayFunc(ArrayFunc.GREATEST,inner );
		}
	},
	/** generate ate least of a set of expressions
	 * 
	 */
	LEAST {
		public PropExpression getExpression(LinkedList<PropExpression> inner){
			return ArrayFuncPropExpression.makeArrayFunc(ArrayFunc.LEAST,inner );
		}
	};

	public abstract PropExpression getExpression(LinkedList<PropExpression> inner) throws ParseException;
	
   /** regular expression to match any keyword
    * 
    * @return regular expression
    */
	public static String getRegexp(){
		StringBuilder sb = new StringBuilder();
		for( Keywords k : values()){
			if( sb.length() > 0){
				sb.append("|");
			}
			sb.append("(?:");
			sb.append(k.toString());
			sb.append(")");
		}
		return sb.toString();
	}
	/** check type of expression is assignable to a type
	 * 
	 * @param clazz
	 * @param expr
	 * @return
	 * @throws ParseException
	 */
	public <T> PropExpression<T> cast(Class<T> clazz,PropExpression expr) throws ParseException{
		if( clazz.isAssignableFrom(expr.getTarget())){
			return expr;
		}
		throw new ParseException(expr.toString()+" not a "+clazz.getSimpleName()+" expression");
	}
	/**
	 * @param list
	 * @throws ParseException
	 */
	private static void checkSingleArg(LinkedList<PropExpression> list) throws ParseException {
		if( list.size() != 1) {
			throw new ParseException("Expecting a single argument");
		}
	}
	
}