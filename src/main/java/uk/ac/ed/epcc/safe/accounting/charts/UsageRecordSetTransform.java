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
import java.util.Date;

import uk.ac.ed.epcc.safe.accounting.expr.ExpressionTargetContainer;
import uk.ac.ed.epcc.safe.accounting.properties.PropExpression;
import uk.ac.ed.epcc.webapp.AppContext;
import uk.ac.ed.epcc.webapp.charts.strategy.FixedSetRangeMapper;
import uk.ac.ed.epcc.webapp.charts.strategy.SetRangeMapper;
import uk.ac.ed.epcc.webapp.jdbc.expr.Reduction;



public class UsageRecordSetTransform<D extends Number> extends UsageRecordMapper<D> implements FixedSetRangeMapper<ExpressionTargetContainer>{
	public UsageRecordSetTransform(AppContext conn,int set,Reduction op,
			PropExpression<D> plot_property, PropExpression<Date> start, PropExpression<Date> end) {
		super(conn, op,plot_property, start, end);
		this.set=set;
	
	}
	private final int set;
	public int getSet(ExpressionTargetContainer o) {
		return set;
	}
	@Override
	public int getFixedSet() {
		return set;
	}

}