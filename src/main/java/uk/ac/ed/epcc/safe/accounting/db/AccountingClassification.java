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

import uk.ac.ed.epcc.webapp.model.Classification;
import uk.ac.ed.epcc.webapp.model.data.DataObjectFactory;
import uk.ac.ed.epcc.webapp.model.data.Repository.Record;
import uk.ac.ed.epcc.webapp.model.data.reference.IndexedReference;
import uk.ac.ed.epcc.webapp.model.data.reference.ReferenceProvider;
/** Represents an additional table of data used to classify accounting records into sets.
 * This is achieved by having the accounting record reference an entry in the classification table.
 * 
 * AccountingClassification objects also have properties associated with them which can be accessed
 * in a PropExpression. 
 * 
 * Note that this can be constructed by both a {@link ParseAccountingClassificationFactory} or an {@link AccountingClassificationFactory} 
 * @author spb
 *
 */


public class AccountingClassification extends Classification implements ReferenceProvider{
  
  
	@SuppressWarnings("unchecked")
	protected AccountingClassification(PropertyClassificationFactory<?> fac,Record res) {
		super(res, fac);
	}

	
    
	

	@SuppressWarnings("unchecked")
	public IndexedReference getReference() {
		return ((DataObjectFactory)fac).makeReference(this);
	}



}