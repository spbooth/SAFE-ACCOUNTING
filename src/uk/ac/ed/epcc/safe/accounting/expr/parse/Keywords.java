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

import java.util.LinkedList;

import uk.ac.ed.epcc.safe.accounting.expr.ArrayFuncPropExpression;
import uk.ac.ed.epcc.safe.accounting.expr.DurationCastPropExpression;
import uk.ac.ed.epcc.safe.accounting.expr.IntPropExpression;
import uk.ac.ed.epcc.safe.accounting.expr.LocatePropExpression;
import uk.ac.ed.epcc.safe.accounting.expr.NamePropExpression;
import uk.ac.ed.epcc.safe.accounting.expr.ParseException;
import uk.ac.ed.epcc.safe.accounting.expr.StringPropExpression;
import uk.ac.ed.epcc.safe.accounting.properties.PropExpression;
import uk.ac.ed.epcc.safe.accounting.reference.ReferenceExpression;
import uk.ac.ed.epcc.webapp.jdbc.expr.ArrayFunc;


/** Conversion functions on propexpressions.
 * 
 * @author spb
 *
 */
public enum Keywords {
	
	NAME {
		@SuppressWarnings("unchecked")
		@Override
		public PropExpression getExpression(LinkedList<PropExpression> list)
				throws ParseException {
			PropExpression inner = list.getFirst();
			if( inner instanceof ReferenceExpression){
				return new NamePropExpression((ReferenceExpression) inner);
			}
			throw new ParseException("Expression "+inner.toString()+" not a reference");
		}
	},
	
	STRING {
		@SuppressWarnings("unchecked")
		@Override
		public PropExpression getExpression(LinkedList<PropExpression> inner)
				throws ParseException {
			
			return new StringPropExpression(inner.getFirst());
		}
	},
	
	INT {

		@SuppressWarnings("unchecked")
		@Override
		public PropExpression getExpression(LinkedList<PropExpression> inner)
				throws ParseException {
			return new IntPropExpression(inner.getFirst());
		}
	
	},
	
	DURATION_CAST {

		@SuppressWarnings("unchecked")
		@Override
		public PropExpression getExpression(LinkedList<PropExpression> inner)
				throws ParseException {
			return new DurationCastPropExpression(cast(Number.class,inner.getFirst()),1L);
		}
	
	},
	
	LOCATE {
		@SuppressWarnings("unchecked")
		@Override
		public PropExpression getExpression(LinkedList<PropExpression> inner)
				throws ParseException {
			return new LocatePropExpression(cast(String.class,inner.getFirst()),cast(String.class,inner.get(1)),cast(Number.class,inner.getLast()));
		}
	},
	GREATEST{
		public PropExpression getExpression(LinkedList<PropExpression> inner){
			return ArrayFuncPropExpression.makeArrayFunc(ArrayFunc.GREATEST,inner );
		}
	},
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
	
	public <T> PropExpression<T> cast(Class<T> clazz,PropExpression expr) throws ParseException{
		if( clazz.isAssignableFrom(expr.getTarget())){
			return expr;
		}
		throw new ParseException(expr.toString()+" not a "+clazz.getSimpleName()+" expression");
	}
	
}