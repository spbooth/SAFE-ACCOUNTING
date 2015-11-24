// Copyright - The University of Edinburgh 2011
package uk.ac.ed.epcc.safe.accounting.db;

import java.sql.ResultSet;
import java.util.List;

import uk.ac.ed.epcc.webapp.AppContext;
import uk.ac.ed.epcc.webapp.Contexed;
import uk.ac.ed.epcc.webapp.jdbc.exception.DataException;
import uk.ac.ed.epcc.webapp.jdbc.expr.FuncExpression;
import uk.ac.ed.epcc.webapp.jdbc.expr.SQLExpression;
import uk.ac.ed.epcc.webapp.jdbc.expr.SQLFunc;
import uk.ac.ed.epcc.webapp.jdbc.filter.PatternArgument;
import uk.ac.ed.epcc.webapp.jdbc.filter.ResultMapper;
import uk.ac.ed.epcc.webapp.jdbc.filter.SQLFilter;
import uk.ac.ed.epcc.webapp.model.data.Exceptions.DataFault;
@uk.ac.ed.epcc.webapp.Version("$Id: FunctionMapper.java,v 1.8 2014/09/22 12:06:56 spb Exp $")


public class FunctionMapper<N extends Number> implements ResultMapper<Number>,Contexed{
	private final AppContext conn;
	private final SQLExpression<? extends Number> exp;
	private final SQLFunc func;
	private boolean qualify=false;
	public FunctionMapper(AppContext conn,SQLExpression<N> exp,SQLFunc func){
		this.conn=conn;
		this.exp=exp;
		this.func=func;
	}
	public String getTarget() {
		StringBuilder sb = new StringBuilder();
		SQLExpression expr = FuncExpression.apply(conn,SQLFunc.AVG,Number.class,exp);
		expr.add(sb, qualify);
		return sb.toString();
	}

	public Number makeDefault() {
		return getZero(exp.getTarget());
	}
	private Number getZero(Class clazz){
		if( clazz == Double.class || clazz == Number.class){
			return Double.valueOf(0.0);
		}else if( clazz == Float.class){
			return Float.valueOf(0.0F);
		}else if( clazz == Long.class){
			return Long.valueOf(0L);
		}else if( clazz == Integer.class){
			return Integer.valueOf(0);
		}
		return Double.valueOf(0.0);
	}
	public String getModify() {
		return null;
	}
	public Number makeObject(ResultSet rs) throws DataException {
		return exp.makeObject(rs, 1);
	}

	public boolean setQualify(boolean qualify) {
		boolean old_q=this.qualify;
		this.qualify=qualify;
		return old_q;
	}
	public SQLFilter getRequiredFilter() {
		return exp.getRequiredFilter();
	}

	public List<PatternArgument> getTargetParameters(
			List<PatternArgument> list) {
		return exp.getParameters(list);
	}

	public List<PatternArgument> getModifyParameters(
			List<PatternArgument> list) {
		return list;
	}
	public AppContext getContext() {
		return conn;
	}
}