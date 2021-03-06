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
package uk.ac.ed.epcc.safe.accounting.selector;

import uk.ac.ed.epcc.safe.accounting.properties.InvalidExpressionException;
import uk.ac.ed.epcc.safe.accounting.properties.PropExpression;
import uk.ac.ed.epcc.safe.accounting.properties.PropertyContainer;
import uk.ac.ed.epcc.safe.accounting.properties.PropertyTag;
import uk.ac.ed.epcc.webapp.jdbc.filter.MatchCondition;
/** Encodes a relationship between a property and a data value.
 * This is used as a template to create select filters from a concrete Factory.
 * A null {@link MatchCondition} is used to signify an equality match.
 * A null {@link PropExpression} should match no records
 * @author spb
 *
 * @param <T>
 */


public final class SelectClause<T> implements RecordSelector{
	public final PropExpression<T> tag;
	public final T data;
	public final MatchCondition match;
	public SelectClause(PropertyTag<T> tag, T data){
		this(tag,null,data);
	}
	public SelectClause(PropertyTag<T> tag, PropertyContainer map) throws InvalidExpressionException{
		this(tag,null,map.getProperty(tag));
	}
	public SelectClause(PropExpression<T> tag, MatchCondition m,T data){
		this.tag=tag.copy();
		this.match=m;
		assert(data != null);
		this.data=data;
		if( data != null && ! tag.getTarget().isAssignableFrom(data.getClass())){
			throw new ClassCastException("Incompatible data and property in SelectClause "+tag.toString()+" "+tag.getTarget().getCanonicalName()+" "+data.getClass().getCanonicalName());
		}
	}
	public SelectClause(PropertyTag<T> tag, MatchCondition m,PropertyContainer map) throws InvalidExpressionException{
		this(tag,m,map.getProperty(tag));
	}
	/** Create a non matching clause
	 * 
	 */
	public SelectClause(){
		this.tag=null;
		this.match=null;
		this.data=null;
	}
	@Override
	public String toString(){
		if( match == null ){
			return "("+tag+"="+data+")";
		}
		return "("+tag+" "+match+" "+data+")";
	}
	public <R> R visit(SelectorVisitor<R> visitor) throws Exception {
		return visitor.visitClause(this);
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((data == null) ? 0 : data.hashCode());
		result = prime * result + ((match == null) ? 0 : match.hashCode());
		result = prime * result + ((tag == null) ? 0 : tag.hashCode());
		return result;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		SelectClause other = (SelectClause) obj;
		if (data == null) {
			if (other.data != null)
				return false;
		} else if (!data.equals(other.data))
			return false;
		if (match != other.match)
			return false;
		if (tag == null) {
			if (other.tag != null)
				return false;
		} else if (!tag.equals(other.tag))
			return false;
		return true;
	}
	public SelectClause<T> copy() {
		return this;
	}
	
}