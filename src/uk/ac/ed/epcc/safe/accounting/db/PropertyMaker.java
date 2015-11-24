// Copyright - The University of Edinburgh 2011
package uk.ac.ed.epcc.safe.accounting.db;

import uk.ac.ed.epcc.safe.accounting.expr.ExpressionTarget;
import uk.ac.ed.epcc.safe.accounting.properties.InvalidSQLPropertyException;
import uk.ac.ed.epcc.safe.accounting.properties.PropertyTag;
import uk.ac.ed.epcc.webapp.jdbc.expr.SQLValue;
import uk.ac.ed.epcc.webapp.jdbc.filter.SetMaker;
import uk.ac.ed.epcc.webapp.model.data.DataObject;
import uk.ac.ed.epcc.webapp.model.data.Repository;
import uk.ac.ed.epcc.webapp.model.data.SetMapper;
@uk.ac.ed.epcc.webapp.Version("$Id: PropertyMaker.java,v 1.10 2014/09/15 14:32:20 spb Exp $")


public class PropertyMaker<T extends DataObject&ExpressionTarget,PT> extends SetMaker<T, PT> {
	private final Repository res;
	public PropertyMaker(AccessorMap<T> map,Repository res,PropertyTag<PT> propertyTag, boolean distinct) throws InvalidSQLPropertyException {
		super(map.getContext(),map.getTarget());			
		SQLValue<PT> sqlAccessor = map.getSQLValue(propertyTag);
		if( sqlAccessor == null ){
			throw new InvalidSQLPropertyException(propertyTag);
		}
		SetMapper<PT> mapper = new SetMapper<PT>(sqlAccessor);					
		setMapper(mapper);			
		this.res=res;
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