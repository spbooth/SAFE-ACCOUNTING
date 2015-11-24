// Copyright - The University of Edinburgh 2011
/*******************************************************************************
 * Copyright (c) - The University of Edinburgh 2010
 *******************************************************************************/
package uk.ac.ed.epcc.safe.accounting.selector;

import uk.ac.ed.epcc.safe.accounting.properties.PropertyTargetFactory;
import uk.ac.ed.epcc.webapp.forms.inputs.Input;
/** Class that generates an input based on a record selector.
 * 
 * @author spb
 *
 * @param <T>
 */
public interface FilterSelector<T extends Input> extends PropertyTargetFactory{

	public T getInput(RecordSelector sel) throws Exception;
}