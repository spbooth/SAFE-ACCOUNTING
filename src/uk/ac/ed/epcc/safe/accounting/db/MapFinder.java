// Copyright - The University of Edinburgh 2011
package uk.ac.ed.epcc.safe.accounting.db;

import java.util.Map;

import uk.ac.ed.epcc.safe.accounting.expr.ExpressionTarget;
import uk.ac.ed.epcc.safe.accounting.properties.InvalidPropertyException;
import uk.ac.ed.epcc.safe.accounting.properties.InvalidSQLPropertyException;
import uk.ac.ed.epcc.safe.accounting.properties.PropertyTag;
import uk.ac.ed.epcc.webapp.jdbc.expr.MapMapper;
import uk.ac.ed.epcc.webapp.jdbc.expr.SQLValue;
import uk.ac.ed.epcc.webapp.jdbc.filter.FilterFinder;
import uk.ac.ed.epcc.webapp.model.data.DataObject;
import uk.ac.ed.epcc.webapp.model.data.FieldValue;
import uk.ac.ed.epcc.webapp.model.data.Repository;
@uk.ac.ed.epcc.webapp.Version("$Id: MapFinder.java,v 1.10 2014/09/15 14:32:20 spb Exp $")


public class MapFinder<T extends DataObject&ExpressionTarget,K,R> extends FilterFinder<T, Map<K,R>> {
	private final Repository res;
	public MapFinder(AccessorMap<T> map,Repository res, PropertyTag<K> key,
			PropertyTag<R> value) throws InvalidSQLPropertyException {
		super(map.getContext(),map.getTarget(), true); // can return null
		this.res=res;
		assert(key != null);
		assert(value != null);
		
		SQLValue<K> a = map.getSQLValue(key);
		String key_name=null;
		if( ! (a instanceof FieldValue)){
			key_name = key.toString();
		}
		
		SQLValue<R> e  = map.getSQLValue(value);
		String value_name=null;
		if( ! (e instanceof FieldValue)){
			value_name=value.toString();
		}
		MapMapper<K,R> mapper = new MapMapper<K,R>(map.getContext(),a,key_name,e,value_name);
		setMapper(mapper);
	}
	@Override
	protected void addSource(StringBuilder sb) {
		res.addTable(sb, true);
		
	}
	@Override
	protected String getDBTag() {
		return res.getDBTag();
	}
}