// Copyright - The University of Edinburgh 2011
/*******************************************************************************
 * Copyright (c) - The University of Edinburgh 2010
 *******************************************************************************/
package uk.ac.ed.epcc.safe.accounting.expr.parse;

import java.util.LinkedList;

import uk.ac.ed.epcc.safe.accounting.expr.DurationCastPropExpression;
import uk.ac.ed.epcc.safe.accounting.expr.IntPropExpression;
import uk.ac.ed.epcc.safe.accounting.expr.NamePropExpression;
import uk.ac.ed.epcc.safe.accounting.expr.ParseException;
import uk.ac.ed.epcc.safe.accounting.expr.StringPropExpression;
import uk.ac.ed.epcc.safe.accounting.properties.PropExpression;
import uk.ac.ed.epcc.safe.accounting.reference.ReferenceExpression;

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
		return new DurationCastPropExpression(inner.getFirst(),1L);
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
}