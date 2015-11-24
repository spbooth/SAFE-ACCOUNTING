// Copyright - The University of Edinburgh 2011
package uk.ac.ed.epcc.safe.accounting.expr;

import java.util.HashMap;
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
@uk.ac.ed.epcc.webapp.Version("$Id: ExpressionTuple.java,v 1.7 2015/03/10 16:56:02 spb Exp $")

public final class ExpressionTuple {
	private final Map<PropExpression,Object> data;
	private final int hash;
	public ExpressionTuple(Map<PropExpression,Object> values){
		data=new HashMap<PropExpression, Object>();
		int tmp=0;
		for(PropExpression e : values.keySet()){
			Object val = values.get(e);
			if( val != null ){
				if( ! e.getTarget().isAssignableFrom(val.getClass())){
					throw new ClassCastException("Invalid value for ExpressionTuple");
				}
				data.put(e, val);
				tmp += val.hashCode();
			}
		}
		hash=tmp;
	}
	public ExpressionTuple(Set<PropExpression> keys, ExpressionTarget rec) throws InvalidExpressionException {
		data=new HashMap<PropExpression, Object>();
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
}