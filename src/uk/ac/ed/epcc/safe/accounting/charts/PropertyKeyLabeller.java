// Copyright - The University of Edinburgh 2011
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
@uk.ac.ed.epcc.webapp.Version("$Id: PropertyKeyLabeller.java,v 1.8 2015/03/10 16:56:02 spb Exp $")

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