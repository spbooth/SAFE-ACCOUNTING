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

import uk.ac.ed.epcc.safe.accounting.ExpressionTargetFactory;
import uk.ac.ed.epcc.safe.accounting.expr.ExpressionCast;
import uk.ac.ed.epcc.safe.accounting.expr.ExpressionTargetContainer;
import uk.ac.ed.epcc.webapp.model.data.DataObject;
import uk.ac.ed.epcc.webapp.model.data.DataObjectFactory;
import uk.ac.ed.epcc.webapp.model.data.Owned;
import uk.ac.ed.epcc.webapp.model.data.Repository.Record;

/** Basic extension of DataObject to implement {@link ProxyOwner}.
 * If desired this can be extended to implement {@link ExpressionTargetContainer} directly
 * using the nested proxy. Easiest way to do this is to implement {@link ProxyOwnerContainer}
 * which has default methods to perform the forwarding.
 * The factory still need to be able to generate an {@link ExpressionTargetFactory}
 * either by implementing it directly or via a composite
 * 
 * 
 * @author spb
 *
 */


public class DataObjectPropertyContainer extends DataObject implements  Owned, ProxyOwner{
    private final DataObjectFactory<?> fac;
    private ExpressionTargetContainer proxy=null; 
	@SuppressWarnings("unchecked")
	public DataObjectPropertyContainer(DataObjectFactory<?> fac,Record r) {
		super(r);
		this.fac=fac;
	}
	
	@Override
	public final DataObjectFactory getFactory() {
		return fac;
	}
    public final ExpressionTargetContainer getProxy() {
    	if( proxy == null) {
    		proxy = getExpressionTargetFactory().getAccessorMap().getProxy(this);
    	}
    	return proxy;
    }

	public ExpressionTargetFactory getExpressionTargetFactory() {
		return ExpressionCast.getExpressionTargetFactory(getFactory());
	}

	@Override
	public final void release() {
		ExpressionTargetContainer tmp = proxy;
		proxy=null;
		if( tmp != null ) {
			tmp.release();
		}
		preRelease();
		super.release();
	}
    public void preRelease() {
    	
    }

}