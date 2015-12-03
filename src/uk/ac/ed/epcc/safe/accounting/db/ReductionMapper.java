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
package uk.ac.ed.epcc.safe.accounting.db;

import java.sql.ResultSet;
import java.util.List;

import uk.ac.ed.epcc.safe.accounting.Reduction;
import uk.ac.ed.epcc.webapp.AppContext;
import uk.ac.ed.epcc.webapp.Contexed;
import uk.ac.ed.epcc.webapp.exceptions.ConsistencyError;
import uk.ac.ed.epcc.webapp.jdbc.exception.DataException;
import uk.ac.ed.epcc.webapp.jdbc.expr.FuncExpression;
import uk.ac.ed.epcc.webapp.jdbc.expr.SQLExpression;
import uk.ac.ed.epcc.webapp.jdbc.expr.SQLFunc;
import uk.ac.ed.epcc.webapp.jdbc.filter.PatternArgument;
import uk.ac.ed.epcc.webapp.jdbc.filter.ResultMapper;
import uk.ac.ed.epcc.webapp.jdbc.filter.SQLFilter;



public class ReductionMapper<R> implements ResultMapper<R> , Contexed{
	private final AppContext conn;
	private final Reduction op;
	private final Class<R> target;
	private final SQLExpression<? extends R> exp;
	private final R def;
	private boolean qualify=false;

	public ReductionMapper(AppContext c,Class<R> target, Reduction op,R def,SQLExpression<? extends R> exp) {
		this.conn=c;
		this.target=target;
		this.op=op;
		this.exp=exp;
		this.def=def;
	}

	public String getModify() {
		return null;
	}

	public String getTarget() {
		StringBuilder sb = new StringBuilder();
		SQLExpression expr=null;
		switch(op){
		case SUM:  expr = FuncExpression.apply(conn,SQLFunc.SUM,target,exp);break;
		case MIN:  expr = FuncExpression.apply(conn,SQLFunc.MIN,target,exp);break;
		case MAX:  expr = FuncExpression.apply(conn,SQLFunc.MAX,target,exp);break;
		case AVG:  expr = FuncExpression.apply(conn,SQLFunc.AVG,target,exp);break;
		}
		if( expr == null ){
			throw new ConsistencyError("reduction did not generate exrpression");
		}
		expr.add(sb, qualify);
		return sb.toString();
	}

	public R makeDefault() {
		return def;
	}

	public R makeObject(ResultSet rs) throws DataException {
		return exp.makeObject(rs, 1);
	}

	public boolean setQualify(boolean qualify) {
		boolean old_q = this.qualify;
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