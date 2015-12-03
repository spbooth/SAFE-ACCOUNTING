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

import uk.ac.ed.epcc.safe.accounting.UsageRecord;
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


public class PropertyKeyLabeller<K> extends KeyLabeller<UsageRecord, K> {
    final private Labeller<K,?> labeller;
    final private PropExpression<K> key_property;
	public PropertyKeyLabeller(AppContext c,PropExpression<K> key_property, Labeller<K,?> l) {
		super(c);
		this.key_property=key_property;
		labeller=l;
	}

	@Override
	public Object getLabel(K key) {
		
		if( labeller == null){
			if( key == null ){
				return "Unknown";
			}
			return key;
		}
		return labeller.getLabel(getContext(),key);
	}

	@Override
	public K getKey(UsageRecord r) {
		try {
			return r.evaluateExpression(key_property);
		} catch (InvalidExpressionException e) {
			return null;
		}
	}

}