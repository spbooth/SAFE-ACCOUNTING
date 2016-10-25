package uk.ac.ed.epcc.safe.accounting.expr;

import uk.ac.ed.epcc.safe.accounting.db.DefaultDataObjectPropertyFactory;
import uk.ac.ed.epcc.webapp.AppContext;
import uk.ac.ed.epcc.webapp.jdbc.exception.DataException;
import uk.ac.ed.epcc.webapp.jdbc.expr.SQLValue;
import uk.ac.ed.epcc.webapp.jdbc.expr.ValueResultMapper;
import uk.ac.ed.epcc.webapp.jdbc.filter.SQLFilter;
import uk.ac.ed.epcc.webapp.jdbc.table.IntegerFieldType;
import uk.ac.ed.epcc.webapp.jdbc.table.StringFieldType;
import uk.ac.ed.epcc.webapp.jdbc.table.TableSpecification;
import uk.ac.ed.epcc.webapp.model.data.DataObject;
import uk.ac.ed.epcc.webapp.model.data.Repository.Record;
import uk.ac.ed.epcc.webapp.model.data.Exceptions.DataFault;
import uk.ac.ed.epcc.webapp.model.data.filter.SelfReferenceFilter;
import uk.ac.ed.epcc.webapp.model.data.reference.IndexedReference;


public class DummyPropertyFactory extends DefaultDataObjectPropertyFactory<DummyPropertyContainer>{

	static final String OFFSET_FIELD = "OffsetField";
	static final String DATA_FIELD = "DataField";
	static final String SEARCH_FIELD = "SearchField";
	public DummyPropertyFactory(AppContext ctx){
		this(ctx,"DummyPropertyTable");
	}
	public DummyPropertyFactory(AppContext ctx, String table) {
		setContext(ctx, table);
		initAccessorMap(ctx, table);
	}
	
	
	@Override
	protected DataObject makeBDO(Record res) throws DataFault {
		return new DummyPropertyContainer(this, res);
	}

	@Override
	public Class<? super DummyPropertyContainer> getTarget() {
		return DummyPropertyContainer.class;
	}
	@Override
	protected TableSpecification getDefaultTableSpecification(AppContext c, String table) {
		TableSpecification spec = new TableSpecification();
		spec.setField(SEARCH_FIELD, new StringFieldType(true, null, 64));
		spec.setField(DATA_FIELD, new StringFieldType(true, null, 64));
		spec.setField(OFFSET_FIELD,new  IntegerFieldType());
		return spec;
	}

	public class ExpressionFinder<X> extends AbstractFinder<X>{
		public ExpressionFinder(SQLValue<X> val){
			setMapper(new ValueResultMapper<X>(val));
		}
	}
	
	public <X> X evaluate(SQLFilter<DummyPropertyContainer> fil, SQLValue v ) throws DataException{
		ExpressionFinder<X> finder = new ExpressionFinder<>(v);
		return finder.find(fil , true);
	}
	
	public SQLFilter<DummyPropertyContainer> getReferenceFilter(IndexedReference<DummyPropertyContainer> ref){
		return new SelfReferenceFilter<>(getTarget(), res, ref);
	}
}