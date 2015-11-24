// Copyright - The University of Edinburgh 2011
/*******************************************************************************
 * Copyright (c) - The University of Edinburgh 2010
 *******************************************************************************/
package uk.ac.ed.epcc.safe.accounting.db;

import java.sql.ResultSet;
import java.util.LinkedList;
import java.util.List;

import uk.ac.ed.epcc.safe.accounting.properties.InvalidSQLPropertyException;
import uk.ac.ed.epcc.safe.accounting.properties.PropertyContainer;
import uk.ac.ed.epcc.safe.accounting.properties.PropertyMap;
import uk.ac.ed.epcc.safe.accounting.properties.PropertyTag;
import uk.ac.ed.epcc.webapp.jdbc.exception.DataException;
import uk.ac.ed.epcc.webapp.jdbc.expr.SQLGroupMapper;

/** Version of the SQLGroupMapper that uses PropertyTag and 
 * AccessorMap
 * @author spb
 *
 */
@uk.ac.ed.epcc.webapp.Version("$Id: PropertyContainerMapper.java,v 1.18 2014/09/15 14:32:20 spb Exp $")

public class PropertyContainerMapper extends SQLGroupMapper<PropertyContainer> {

	private final List<PropertyTag> fields;
	private final AccessorMap<?> m;
	
	public PropertyContainerMapper(AccessorMap<?> m){
		super(m.getContext());
		this.m=m;
		fields=new LinkedList<PropertyTag>();
	}
	public <N>void addKey(PropertyTag<N> t) throws InvalidSQLPropertyException {
		fields.add(t);
		addKey(m.getSQLValue(t),t.getName());
	}
	public final <N extends Number> void addSum(PropertyTag<N> t) throws InvalidSQLPropertyException {
		fields.add(t);
		addSum(m.getSQLExpression(t), t.getName());
	}
	public PropertyContainer makeDefault() {
		return new PropertyMap();
	}

	@SuppressWarnings("unchecked")
	public PropertyContainer makeObject(ResultSet rs) throws DataException {
		PropertyMap res = new PropertyMap();
		int pos=0;
		for(PropertyTag t : fields){
		    res.setProperty(t, getTargetObject(pos++, rs));	
		}
		return res;
	}
	

}