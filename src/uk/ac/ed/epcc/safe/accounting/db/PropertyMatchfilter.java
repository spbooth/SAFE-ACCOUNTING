// Copyright - The University of Edinburgh 2011
package uk.ac.ed.epcc.safe.accounting.db;

import uk.ac.ed.epcc.safe.accounting.expr.ExpressionTarget;
import uk.ac.ed.epcc.safe.accounting.properties.InvalidExpressionException;
import uk.ac.ed.epcc.safe.accounting.properties.InvalidSQLPropertyException;
import uk.ac.ed.epcc.safe.accounting.properties.PropertyContainer;
import uk.ac.ed.epcc.safe.accounting.properties.PropertyTag;
import uk.ac.ed.epcc.webapp.jdbc.expr.SQLExpressionFilter;
import uk.ac.ed.epcc.webapp.jdbc.filter.MatchCondition;
import uk.ac.ed.epcc.webapp.model.data.DataObject;
@uk.ac.ed.epcc.webapp.Version("$Id: PropertyMatchfilter.java,v 1.9 2015/03/11 10:16:44 spb Exp $")


public class PropertyMatchfilter<T extends DataObject&ExpressionTarget,P> extends SQLExpressionFilter<T,P>{
	public PropertyMatchfilter(AccessorMap<T> map,PropertyTag<P> key, MatchCondition match ,PropertyContainer c) throws InvalidSQLPropertyException, InvalidExpressionException{
		super(map.getTarget(),map.getSQLExpression(key),match,c.getProperty(key));
	}
	public PropertyMatchfilter(AccessorMap<T> map,PropertyTag<P> key,MatchCondition match, P value) throws InvalidSQLPropertyException{
		super(map.getTarget(),map.getSQLExpression(key),match,value);
	}
}