package uk.ac.ed.epcc.safe.accounting.expr;

import uk.ac.ed.epcc.webapp.AppContext;
import uk.ac.ed.epcc.webapp.jdbc.table.StringFieldType;
import uk.ac.ed.epcc.webapp.jdbc.table.TableSpecification;
import uk.ac.ed.epcc.webapp.model.data.DataObject;
import uk.ac.ed.epcc.webapp.model.data.DataObjectFactory;
import uk.ac.ed.epcc.webapp.model.data.Repository.Record;
import uk.ac.ed.epcc.webapp.model.data.Exceptions.DataFault;

public class DummyDataObjectFactory extends DataObjectFactory<DummyDataObjectFactory.DummyObject> {

	public static final String TABLE_NAME = "DummyTable";

	public DummyDataObjectFactory(AppContext conn){
		setContext(conn, TABLE_NAME);
	}

	private static final String NAME = "Name";
	private static final String DESCRIPTION = "Description";

	@Override
	public Class<? super DummyObject> getTarget() {
		return DummyObject.class;
	}

	@Override
	protected DataObject makeBDO(Record res) throws DataFault {
		return new DummyObject(res);
	}

	@Override
	protected TableSpecification getDefaultTableSpecification(AppContext c,	String table) {
		TableSpecification spec = new TableSpecification(TABLE_NAME);
		spec.setField(NAME, new StringFieldType(false, null, 16));
		spec.setField(DESCRIPTION, new StringFieldType(false, null, 512));
		return spec;
	}

	
	public static class DummyObject extends DataObject{
	
		protected DummyObject(Record r) {
			super(r);
		}
		
		public void setName(String n){
			record.setProperty(NAME, n);
		}
		
		public void setDescription(String d){
			record.setProperty(DESCRIPTION, d);
		}
		
		public String getName(){
			return record.getStringProperty(NAME);
		}
		
		public String getDescription(){
			return record.getStringProperty(DESCRIPTION);
		}
		
		public Record getRecord(){
			return record;
		}
	}
	
}
