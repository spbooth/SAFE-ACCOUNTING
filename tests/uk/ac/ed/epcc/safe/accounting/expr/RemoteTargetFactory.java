package uk.ac.ed.epcc.safe.accounting.expr;

import java.util.Date;

import uk.ac.ed.epcc.safe.accounting.db.AccessorMap;
import uk.ac.ed.epcc.safe.accounting.db.DataObjectPropertyContainer;
import uk.ac.ed.epcc.safe.accounting.db.DefaultDataObjectPropertyFactory;
import uk.ac.ed.epcc.safe.accounting.properties.MultiFinder;
import uk.ac.ed.epcc.safe.accounting.properties.PropertyRegistry;
import uk.ac.ed.epcc.safe.accounting.properties.PropertyTag;
import uk.ac.ed.epcc.webapp.AppContext;
import uk.ac.ed.epcc.webapp.jdbc.table.DateFieldType;
import uk.ac.ed.epcc.webapp.jdbc.table.DoubleFieldType;
import uk.ac.ed.epcc.webapp.jdbc.table.IntegerFieldType;
import uk.ac.ed.epcc.webapp.jdbc.table.TableSpecification;
import uk.ac.ed.epcc.webapp.model.data.DataObject;
import uk.ac.ed.epcc.webapp.model.data.Repository.Record;
import uk.ac.ed.epcc.webapp.model.data.Exceptions.DataFault;

public class RemoteTargetFactory<T extends DataObjectPropertyContainer> extends DefaultDataObjectPropertyFactory<T> {
	
	public static final PropertyRegistry test = new PropertyRegistry("remotetest", "Remote Test registry");
	
	public static final PropertyTag<Integer> INT_A = new PropertyTag<Integer>(test,"IntA",Integer.class);
	
	public static final PropertyTag<Integer> INT_B = new PropertyTag<Integer>(test,"IntB",Integer.class);
	
	public static final PropertyTag<Double> DOUBLE_A = new PropertyTag<Double>(test,"DoubleA",Double.class);
	
	public static final PropertyTag<Double> DOUBLE_B = new PropertyTag<Double>(test,"DoubleB",Double.class);
	
	public static final PropertyTag<Date> DATE_A = new PropertyTag<Date>(test,"DateA",Date.class);

	public static final PropertyTag<Date> DATE_B = new PropertyTag<Date>(test,"DateB",Date.class);

	public static final String DEFAULT_TABLE="RemoteTest";
	public RemoteTargetFactory(AppContext conn) {
		super();
		setContext(conn, DEFAULT_TABLE);
	}

	@Override
	protected DataObject makeBDO(Record res) throws DataFault {
		return new DataObjectPropertyContainer(this, res);
	}

	/* (non-Javadoc)
	 * @see uk.ac.ed.epcc.safe.accounting.db.DefaultDataObjectPropertyFactory#customAccessors(uk.ac.ed.epcc.safe.accounting.db.AccessorMap, uk.ac.ed.epcc.safe.accounting.properties.MultiFinder, uk.ac.ed.epcc.safe.accounting.expr.PropExpressionMap)
	 */
	@Override
	public void customAccessors(AccessorMap<T> mapi2, MultiFinder finder, PropExpressionMap derived) {
		super.customAccessors(mapi2, finder, derived);
		finder.addFinder(test);
	}

	/* (non-Javadoc)
	 * @see uk.ac.ed.epcc.webapp.model.data.DataObjectFactory#getDefaultTableSpecification(uk.ac.ed.epcc.webapp.AppContext, java.lang.String)
	 */
	@Override
	protected TableSpecification getDefaultTableSpecification(AppContext c, String table) {
		TableSpecification spec = new TableSpecification();
		spec.setField(INT_A.getName(), new IntegerFieldType());
		spec.setField(INT_B.getName(), new IntegerFieldType());
		spec.setField(DOUBLE_A.getName(), new DoubleFieldType(true,null));
		spec.setField(DOUBLE_B.getName(), new DoubleFieldType(true,null));
		spec.setField(DATE_A.getName(), new DateFieldType(true, null));
		spec.setField(DATE_B.getName(), new DateFieldType(true, null));
		return spec;
	}

	

}
