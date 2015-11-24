// Copyright - The University of Edinburgh 2011
/*******************************************************************************
 * Copyright (c) - The University of Edinburgh 2010
 *******************************************************************************/
package uk.ac.ed.epcc.safe.accounting.reference;

import uk.ac.ed.epcc.safe.accounting.properties.PropExpression;
import uk.ac.ed.epcc.webapp.AppContext;
import uk.ac.ed.epcc.webapp.Indexed;
import uk.ac.ed.epcc.webapp.model.data.reference.IndexedProducer;
import uk.ac.ed.epcc.webapp.model.data.reference.IndexedReference;
/** A prop-expression for references
 * 
 * In this case the getTarget method is insufficient to define the type of the
 * result fully so we add additional methods to provide the missing information.
 * 
 * @author spb
 *
 * @param <I> type of reference
 */
public interface ReferenceExpression<I extends Indexed> extends PropExpression<IndexedReference<I>> {

	public IndexedProducer<I> getFactory(AppContext c);
	
	public Class<? extends IndexedProducer> getFactoryClass();
	
	public String getTable();
	
	public ReferenceExpression<I> copy();
}