package uk.ac.ed.epcc.safe.accounting.expr;

import uk.ac.ed.epcc.safe.accounting.db.DefaultDataObjectPropertyFactory;
import uk.ac.ed.epcc.webapp.AppContext;
import uk.ac.ed.epcc.webapp.model.data.DataObject;
import uk.ac.ed.epcc.webapp.model.data.Exceptions.DataFault;
import uk.ac.ed.epcc.webapp.model.data.Repository.Record;
import uk.ac.ed.epcc.webapp.model.data.reference.IndexedReference;


public class DummyPropertyFactory extends DefaultDataObjectPropertyFactory<DummyPropertyContainer>{

	public DummyPropertyFactory(AppContext ctx, String table) {
		setContext(ctx, table);
		initAccessorMap(ctx, table);
	}
	
	@Override
	public IndexedReference makeReference(DummyPropertyContainer obj) {
		return this.makeReference(obj);
	}

	@Override
	public String getID(DummyPropertyContainer obj) {
		return this.getID(obj);
	}

	@Override
	protected DataObject makeBDO(Record res) throws DataFault {
		return null;
	}

}