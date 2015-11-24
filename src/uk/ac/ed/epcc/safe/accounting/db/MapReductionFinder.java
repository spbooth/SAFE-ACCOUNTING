// Copyright - The University of Edinburgh 2011
package uk.ac.ed.epcc.safe.accounting.db;

import java.util.Map;

import uk.ac.ed.epcc.safe.accounting.ReductionTarget;
import uk.ac.ed.epcc.safe.accounting.expr.ExpressionTarget;
import uk.ac.ed.epcc.safe.accounting.properties.InvalidSQLPropertyException;
import uk.ac.ed.epcc.safe.accounting.properties.PropExpression;
import uk.ac.ed.epcc.webapp.jdbc.expr.AverageMapMapper;
import uk.ac.ed.epcc.webapp.jdbc.expr.MaximumMapMapper;
import uk.ac.ed.epcc.webapp.jdbc.expr.MinimumMapMapper;
import uk.ac.ed.epcc.webapp.jdbc.expr.SQLExpression;
import uk.ac.ed.epcc.webapp.jdbc.expr.SQLValue;
import uk.ac.ed.epcc.webapp.jdbc.expr.SumMapMapper;
import uk.ac.ed.epcc.webapp.jdbc.filter.FilterFinder;
import uk.ac.ed.epcc.webapp.model.data.DataObject;
import uk.ac.ed.epcc.webapp.model.data.FieldValue;
@uk.ac.ed.epcc.webapp.Version("$Id: MapReductionFinder.java,v 1.14 2014/09/15 14:32:20 spb Exp $")


public class MapReductionFinder<T extends DataObject&ExpressionTarget,K> extends FilterFinder<T, Map<K,Number>> {
	private final AccessorMap<T> map;
	public MapReductionFinder(AccessorMap<T> map, PropExpression<K> key,
			ReductionTarget<Number> value) throws InvalidSQLPropertyException {
		super(map.getContext(), map.getTarget(),true); // can return null
		this.map=map;
		assert(key != null);
		assert(value != null);
		
		SQLValue<K> a = map.getSQLValue(key);
		String key_name=null;
		if( ! (a instanceof FieldValue)){
			key_name = key.toString();
		}
		
		SQLExpression<? extends Number> e  = map.getSQLExpression(value.getExpression());
		String value_name=null;
		if( ! (e instanceof FieldValue)){
			value_name=value.toString();
		}
		
		switch(value.getReduction()){
		case SUM: setMapper(new SumMapMapper<K>(map.getContext(),a,key_name,e,value_name)); break;
		case MIN: setMapper(new MinimumMapMapper<K>(map.getContext(),a,key_name,e,value_name)); break;
		case MAX: setMapper(new MaximumMapMapper<K>(map.getContext(),a,key_name,e,value_name)); break;
		case AVG: setMapper(new AverageMapMapper<K>(map.getContext(),a,key_name,e,value_name)); break;
		}
		
	}
	@Override
	protected void addSource(StringBuilder sb) {
		map.addSource(sb);
		
	}
	@Override
	protected String getDBTag() {
		return map.getDBTag();
	}
	
}