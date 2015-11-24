// Copyright - The University of Edinburgh 2011
/*******************************************************************************
 * Copyright (c) - The University of Edinburgh 2010
 *******************************************************************************/
package uk.ac.ed.epcc.safe.accounting;

import uk.ac.ed.epcc.safe.accounting.db.AccessorMap;
import uk.ac.ed.epcc.safe.accounting.expr.ExpressionTarget;
import uk.ac.ed.epcc.safe.accounting.properties.PropertyTargetFactory;
import uk.ac.ed.epcc.webapp.jdbc.expr.SQLExpression;
import uk.ac.ed.epcc.webapp.model.data.DataObject;
/** Interface that combines {@link ExpressionFilterTarget} and
 * {@link PropertyTargetFactory} and supports the generation of
 * {@link SQLExpression}s.
 * 
 * @author spb
 *
 * @param <T>
 */
public interface ExpressionTargetFactory<T extends DataObject&ExpressionTarget> extends ExpressionTargetGenerator<T>,
		ExpressionFilterTarget<T> {
	
	public AccessorMap<T> getAccessorMap();
}