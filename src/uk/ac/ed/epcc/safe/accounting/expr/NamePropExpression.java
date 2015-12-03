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
package uk.ac.ed.epcc.safe.accounting.expr;

import java.security.Principal;

import uk.ac.ed.epcc.safe.accounting.properties.BasePropExpressionVisitor;
import uk.ac.ed.epcc.safe.accounting.properties.PropExpression;
import uk.ac.ed.epcc.safe.accounting.reference.ReferenceExpression;
import uk.ac.ed.epcc.webapp.AppContext;
import uk.ac.ed.epcc.webapp.model.Classification;
import uk.ac.ed.epcc.webapp.model.data.DataObject;
import uk.ac.ed.epcc.webapp.model.data.reference.IndexedReference;

/** A {@link PropExpression} that evaluates to the name of a referenced object.
 * 

 * @author spb
 *
 */


public class NamePropExpression implements PropExpression<String> {
    private final ReferenceExpression<? extends DataObject> target_ref;
	public static final String UNKNOWN = "Unknown";
    public NamePropExpression(ReferenceExpression<? extends DataObject> tag){
    	this.target_ref=tag.copy();
    }
    public ReferenceExpression<? extends DataObject> getTargetRef(){
    	return target_ref;
    }
	public Class<String> getTarget() {
		// unless dereferenced these resolve to String
		return String.class;
	}
    @Override
	public String toString(){
    	return target_ref.toString();
    }
	public <R> R accept(BasePropExpressionVisitor<R> vis) throws Exception {
		if( vis instanceof PropExpressionVisitor){
		return ((PropExpressionVisitor<R>)vis).visitNamePropExpression(this);
		}
		throw new UnsupportedExpressionException(this);
	}
	@Override
	public boolean equals(Object obj) {
		if( obj != null && obj.getClass() == getClass()){
			NamePropExpression peer = (NamePropExpression) obj;
			return target_ref.equals(peer.target_ref);
		}
		return false;
	}
	@Override
	public int hashCode() {
		return target_ref.hashCode();
	}
	public NamePropExpression copy() {
		return this;
	}
	public static <T extends DataObject>  String refToName(AppContext c,IndexedReference<T> ref) {
		if( ref == null || ref.isNull()){
			return UNKNOWN;
		}
		String result=null;
		T obj=ref.getIndexed(c);
		if(obj != null){
	
			if( obj instanceof Principal){
				//log.debug("Is classification");
				result = ((Principal)obj).getName();
			}else{
				//log.debug("using identifier");
				result= obj.getIdentifier();
			}
		}
		if( result != null ){
			return result;
		}
		return UNKNOWN;
	}
}