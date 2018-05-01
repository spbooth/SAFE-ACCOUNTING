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
package uk.ac.ed.epcc.safe.accounting.db;

import java.util.Set;

import uk.ac.ed.epcc.safe.accounting.ExpressionTargetFactory;
import uk.ac.ed.epcc.safe.accounting.expr.ExpressionTargetContainer;
import uk.ac.ed.epcc.safe.accounting.expr.Parser;
import uk.ac.ed.epcc.safe.accounting.properties.InvalidExpressionException;
import uk.ac.ed.epcc.safe.accounting.properties.InvalidPropertyException;
import uk.ac.ed.epcc.safe.accounting.properties.PropExpression;
import uk.ac.ed.epcc.safe.accounting.properties.PropertyContainer;
import uk.ac.ed.epcc.safe.accounting.properties.PropertyTag;
import uk.ac.ed.epcc.webapp.model.data.DataObject;
import uk.ac.ed.epcc.webapp.model.data.Owned;
import uk.ac.ed.epcc.webapp.model.data.Repository.Record;
import uk.ac.ed.epcc.webapp.model.data.reference.IndexedReference;

/** Basic extension of DataObject to support properties
 * 
 * 
 * @author spb
 *
 */


public class DataObjectPropertyContainer extends DataObject implements  Owned{
    private final DataObjectPropertyFactory<?> fac;
    private ExpressionTargetContainer proxy=null; 
	@SuppressWarnings("unchecked")
	public DataObjectPropertyContainer(DataObjectPropertyFactory<?> fac,Record r) {
		super(r);
		this.fac=fac;
	}
	
	@Override
	public final DataObjectPropertyFactory getFactory() {
		return fac;
	}
    public final ExpressionTargetContainer getProxy() {
    	if( proxy == null) {
    		proxy = getFactory().getAccessorMap().getProxy(this);
    	}
    	return proxy;
    }


}