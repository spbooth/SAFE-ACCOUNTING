// Copyright - The University of Edinburgh 2011
/*******************************************************************************
 * Copyright (c) - The University of Edinburgh 2010
 *******************************************************************************/
package uk.ac.ed.epcc.safe.accounting.properties;

import uk.ac.ed.epcc.webapp.AppContext;
import uk.ac.ed.epcc.webapp.Targetted;

/** Encodes an expression over properties. 
 * 
 * Note that a single property (i.e. a {@link PropertyTag}) is also a {@link PropExpression}.
 * 
 * Classes that implement {@link PropExpression} should not reference the {@link AppContext}
 * so they can safely be cached in the user session.
 * 
 * @author spb
 * @param <T> type of expression
 * 
 *
 */
public interface PropExpression<T> extends Targetted<T>{
	
	/** Visitor pattern. Have the {@link BasePropExpressionVisitor} visit this expression
	 * @param <R> type of return
	 * @param vis visitor
	 * @return result of type R
	 * @throws Exception 
	 */
    public abstract <R> R accept(BasePropExpressionVisitor<R> vis) throws Exception;
    /** Generate an equivalent (and where possible immutable) copy of
     * this expression. 
     * 
     * Most implementing types should themselves be immutable, calling copy on their
     * arguments and returning a self-reference for copy. In this case the return type of
     * the function should be narrowed to the implemeting sub-type. 
     * 
     * @return PropExpression
     */
    public abstract PropExpression<T> copy();
    
}