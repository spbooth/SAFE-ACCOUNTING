package uk.ac.ed.epcc.safe.accounting.expr;

import uk.ac.ed.epcc.safe.accounting.db.DataObjectPropertyContainer;
import uk.ac.ed.epcc.webapp.model.data.Repository;

public class DummyPropertyContainer extends DataObjectPropertyContainer{

	public DummyPropertyContainer(DummyPropertyFactory fac, Repository.Record r) {
		super(fac, r);
	}

	public String getSearch(){
		return record.getStringProperty(DummyPropertyFactory.SEARCH_FIELD);
	}
	public void setSearch(String s){
		record.setProperty(DummyPropertyFactory.SEARCH_FIELD, s);
	}
	
	public String getData(){
		return record.getStringProperty(DummyPropertyFactory.DATA_FIELD);
	}
	public void setData(String s){
		record.setProperty(DummyPropertyFactory.DATA_FIELD, s);
	}
	public int getOffset(){
		return record.getIntProperty(DummyPropertyFactory.OFFSET_FIELD);
	}
	public void setOffset(int i){
		record.setProperty(DummyPropertyFactory.OFFSET_FIELD, i);
	}
}
