package uk.ac.ed.epcc.safe.accounting.expr;

import uk.ac.ed.epcc.safe.accounting.db.DataObjectPropertyContainer;
import uk.ac.ed.epcc.webapp.model.data.Repository;

public class DummyPropertyContainer extends DataObjectPropertyContainer{

	public DummyPropertyContainer(DummyPropertyFactory fac, Repository.Record r) {
		super(fac, r);
	}
	
	public EvaluatePropExpressionVisitor getEvaluator() {
		return (EvaluatePropExpressionVisitor) getFac().getAccessorMap().getProxy(this);
	}

}
