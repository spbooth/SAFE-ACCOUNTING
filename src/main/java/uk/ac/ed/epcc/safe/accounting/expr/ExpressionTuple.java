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
package uk.ac.ed.epcc.safe.accounting.expr;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import uk.ac.ed.epcc.safe.accounting.properties.InvalidExpressionException;
import uk.ac.ed.epcc.safe.accounting.properties.PropExpression;
import uk.ac.ed.epcc.safe.accounting.properties.PropertyTuple;



/** A class representing a set of PropExpressions and associated values.
 * This object is immutable and can be used to key Maps.
 * like {@link PropertyTuple} but for expressions.
 * 
 * @author spb
 *
 */


public final class ExpressionTuple implements Comparable<ExpressionTuple>{
	private final Map<PropExpression,Object> data;
	private final int hash;
	public ExpressionTuple(Map<PropExpression,Object> values){
		data=new LinkedHashMap<>();
		int tmp=0;
		for(PropExpression e : values.keySet()){
			Object val = values.get(e);
			if( val != null ){
				if( ! e.getTarget().isAssignableFrom(val.getClass())){
					throw new ClassCastException("Invalid value for ExpressionTuple "+val.getClass().getCanonicalName()+" not assignable to "+e.getTarget().getCanonicalName());
				}
				data.put(e, val);
				tmp += val.hashCode();
			}
		}
		hash=tmp;
	}
	public ExpressionTuple(Set<PropExpression> keys, ExpressionTarget rec) throws InvalidExpressionException {
		data=new LinkedHashMap<>();
		int tmp=0;
		for(PropExpression e : keys){
			@SuppressWarnings("unchecked")
			Object val = rec.evaluateExpression(e);
			if( val != null ){
				data.put(e, val);
				tmp += val.hashCode();
			}
		}
		hash=tmp;
	}
	public Set<PropExpression> expressionSet(){
		return data.keySet();
	}
	@SuppressWarnings("unchecked")
	public <X> X get(PropExpression<X> expr){
		return (X) data.get(expr);
	}
	/** get a (modifiable) map of the tuple contents
	 * 
	 * @return
	 */
	public Map<PropExpression,Object> getMap(){
		return new HashMap(data);
	}
	
	@Override
	public boolean equals(Object obj) {
		if( obj == this ){
			return true;
		}
		if( ! (obj instanceof ExpressionTuple)){
			return false;
		}
		ExpressionTuple t = (ExpressionTuple) obj;
		if( t.hashCode() != hash || t.data.size() != data.size()){
			return false;
		}
		for(PropExpression<?> tag: data.keySet()){
			Object my_data = data.get(tag);
			Object peer_data = t.data.get(tag);
			if( my_data == null && peer_data != null){
				return false;
			}
			if( peer_data == null && my_data != null ){
				return false;
			}
			if( my_data != null && peer_data != null && ! my_data.equals(peer_data)){
				return false;
			}
		}
		
		return true;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return hash;
	}
	/** Get a table key from the PropertyTuple.
	 *  If there is more than one property set the key is the propertyTuple itself.
	 *  If there is a single property then return the value. This is to allow
	 *  compound tables to be joined using different properties with the same value space.
	 * @return object.
	 */
	public Object getKey(){
		if( data.size() == 1){
		   return data.values().iterator().next();
		}
		return this;
	}
	@Override
	public int compareTo(ExpressionTuple o) {
		for(PropExpression<?> tag: data.keySet()){
			Object my_data = data.get(tag);
			Object peer_data = o.data.get(tag);
			if( my_data == null && peer_data != null ) {
				return -1;
			}
			if( my_data != null && peer_data == null ) {
				return 1;
			}
			if( my_data != null && peer_data != null ) {
				if( my_data instanceof Comparable) {
					int res = ((Comparable)my_data).compareTo(peer_data);
					if( res != 0 ) {
						return res;
					}
				}else {
					if( ! my_data.equals(peer_data)) {
						// arbitrary order
						int res = my_data.hashCode() - peer_data.hashCode();
						if( res != 0) {
							return res;
						}
					}
				}
			}
		}
		return 0;
	}
}