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
package uk.ac.ed.epcc.safe.accounting.charts;

import uk.ac.ed.epcc.webapp.AppContext;
import uk.ac.ed.epcc.webapp.Indexed;
import uk.ac.ed.epcc.webapp.content.Labeller;
import uk.ac.ed.epcc.webapp.forms.Identified;
import uk.ac.ed.epcc.webapp.model.data.reference.IndexedReference;



public class ReferenceLabeller<D extends Indexed> implements Labeller<IndexedReference<D>,String> {
    private static final String DEFAULT_LABEL = "Unknown";
	
  
	public final String getLabel(AppContext conn, IndexedReference<D> key) {
		if( key == null || key.isNull()){
			return getDefaultLabel();
		}
		assert(key instanceof IndexedReference);
		D res = key.getIndexed(conn);
		if( res != null ){
			return getLabel(res);
		}
		return getDefaultLabel();
	}
	public final Class<? super String> getTarget(){
		return String.class;
	}
	/** Default label to generate
	 * @return
	 */
	public String getDefaultLabel() {
		return DEFAULT_LABEL;
	}
	public String getLabel(D val){
		if( val instanceof Identified){
			return ((Identified)val).getIdentifier();
		}
		return val.toString();
	}
	public boolean accepts(Object o) {
		if( o != null && o instanceof IndexedReference){
			return true;
		}
		return false;
	}
}