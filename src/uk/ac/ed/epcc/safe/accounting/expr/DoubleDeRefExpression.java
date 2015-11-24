// Copyright - The University of Edinburgh 2011
/*******************************************************************************
 * Copyright (c) - The University of Edinburgh 2010
 *******************************************************************************/
package uk.ac.ed.epcc.safe.accounting.expr;

import uk.ac.ed.epcc.safe.accounting.reference.ReferenceExpression;
import uk.ac.ed.epcc.webapp.AppContext;
import uk.ac.ed.epcc.webapp.Indexed;
import uk.ac.ed.epcc.webapp.model.data.DataObject;
import uk.ac.ed.epcc.webapp.model.data.reference.IndexedProducer;
import uk.ac.ed.epcc.webapp.model.data.reference.IndexedReference;

/** A de-reference expression that is itself a reference expression.
 * 
 * @author spb
 *
 * @param <R>
 * @param <T>
 */
@uk.ac.ed.epcc.webapp.Version("$Id: DoubleDeRefExpression.java,v 1.10 2014/09/15 14:32:21 spb Exp $")

public class DoubleDeRefExpression<R extends DataObject & ExpressionTarget,T extends Indexed> extends DeRefExpression<R, IndexedReference<T>> implements ReferenceExpression<T>{

	public DoubleDeRefExpression(ReferenceExpression<R> tag,
			ReferenceExpression<T> expr) {
		super(tag, expr);
	}

	public ReferenceExpression<T> getNext(){
		return (ReferenceExpression<T>) getExpression();
	}
	public IndexedProducer<T> getFactory(AppContext c) {
		return getNext().getFactory(c);
	}

	public Class<? extends IndexedProducer> getFactoryClass() {
		return getNext().getFactoryClass();
	}

	public String getTable() {
		return getNext().getTable();
	}
	public DoubleDeRefExpression<R, T> copy(){
		return this;
	}
	
}