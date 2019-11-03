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

import uk.ac.ed.epcc.safe.accounting.expr.ExpressionTargetContainer;
import uk.ac.ed.epcc.safe.accounting.properties.InvalidExpressionException;
import uk.ac.ed.epcc.safe.accounting.properties.PropExpression;
import uk.ac.ed.epcc.webapp.AppContext;
import uk.ac.ed.epcc.webapp.charts.strategy.KeyLabeller;
import uk.ac.ed.epcc.webapp.content.Labeller;
/** Adaptor class that converts a {@link Labeller} into the {@link KeyLabeller} needed for the Chart classes
 * If no Labeller is provided it uses toString on the key property.
 * 
 * @author spb
 *
 * @param <K>
 */


public class PropertyKeyLabeller<K> extends KeyLabeller<ExpressionTargetContainer, K> {
    final private Labeller<K,?> labeller;
    final private PropExpression<K> key_property;
	public PropertyKeyLabeller(AppContext c,PropExpression<K> key_property, Labeller<K,?> l) {
		super(c);
		this.key_property=key_property;
		labeller=l;
	}

	@Override
	public Object getLabel(K key) {
		
		if( labeller == null || ! labeller.accepts(key)){
			if( key == null ){
				return "Unknown";
			}
			return key;
		}
		
		return labeller.getLabel(getContext(),key);
		
	}

	@Override
	public K getKey(ExpressionTargetContainer r) {
		try {
			return r.evaluateExpression(key_property);
		} catch (InvalidExpressionException e) {
			return null;
		}
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((key_property == null) ? 0 : key_property.hashCode());
		result = prime * result + ((labeller == null) ? 0 : labeller.hashCode());
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
		PropertyKeyLabeller other = (PropertyKeyLabeller) obj;
		if (key_property == null) {
			if (other.key_property != null)
				return false;
		} else if (!key_property.equals(other.key_property))
			return false;
		if (labeller == null) {
			if (other.labeller != null)
				return false;
		} else if (!labeller.equals(other.labeller))
			return false;
		return true;
	}

}