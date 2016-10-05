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

import uk.ac.ed.epcc.safe.accounting.properties.BasePropExpressionVisitor;
import uk.ac.ed.epcc.safe.accounting.properties.PropExpression;
import uk.ac.ed.epcc.webapp.model.data.Duration;

/** A cast an index expression to {@link Locate}.
 * 
 * @author spb
 *
 */


public class LocatePropExpression implements PropExpression<Integer> {
	public final PropExpression<String> str;
	public final PropExpression<String> col;
    public final PropExpression<Integer> pos; // starting index
    public LocatePropExpression(PropExpression<String> str, PropExpression<String> col, PropExpression<Integer> pos){
    	this.str=str.copy();
    	this.col=col.copy();
    	this.pos=pos.copy();
    }
   	
	public Class<? super Integer> getTarget() {
		return Integer.class;
	}
	
	public PropExpression<String> getString(){
		return str;
	}
	public PropExpression<String> getColumn(){
		return col;
	}
	public PropExpression<Integer> getPosition(){
		return pos;
	}
 
	public PropExpression<Integer> getLocation(){
		return pos;
	}
	
	@Override
	public String toString(){
		return "Locate('"+str.toString()+"',"+col.toString()+","+pos.toString()+")";
	}
	public <R> R accept(BasePropExpressionVisitor<R> vis) throws Exception {
		if( vis instanceof PropExpressionVisitor){
			return ((PropExpressionVisitor<R>)vis).visitLocatePropExpression(this);
		}
		throw new UnsupportedExpressionException(this);
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((str == null) ? 0 : str.hashCode());
		result = prime * result + ((col == null) ? 0 : col.hashCode());
		result = prime * result + ((pos == null) ? 0 : pos.hashCode());
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
		LocatePropExpression other = (LocatePropExpression) obj;
		if (str == null) {
			if (other.str != null)
				return false;
		} else if (!str.equals(other.str))
			return false;
		if (col == null) {
			if (other.col != null)
				return false;
		} else if (!col.equals(other.col))
			return false;
		if (pos != other.pos)
			return false;
		return true;
	}

	@Override
	public LocatePropExpression copy() {
		return this;
	}
}