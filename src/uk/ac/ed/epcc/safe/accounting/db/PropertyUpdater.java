package uk.ac.ed.epcc.safe.accounting.db;

import uk.ac.ed.epcc.safe.accounting.ExpressionFilterTarget;
import uk.ac.ed.epcc.safe.accounting.ExpressionTargetFactory;
import uk.ac.ed.epcc.safe.accounting.expr.ExpressionCast;
import uk.ac.ed.epcc.safe.accounting.expr.ExpressionTargetContainer;
import uk.ac.ed.epcc.safe.accounting.properties.InvalidExpressionException;
import uk.ac.ed.epcc.safe.accounting.properties.InvalidPropertyException;
import uk.ac.ed.epcc.safe.accounting.properties.PropExpression;
import uk.ac.ed.epcc.safe.accounting.properties.PropertyTag;
import uk.ac.ed.epcc.safe.accounting.selector.RecordSelector;
import uk.ac.ed.epcc.webapp.content.InvalidArgument;
import uk.ac.ed.epcc.webapp.jdbc.expr.CannotFilterException;
import uk.ac.ed.epcc.webapp.jdbc.expr.SQLExpression;
import uk.ac.ed.epcc.webapp.jdbc.filter.BaseFilter;
import uk.ac.ed.epcc.webapp.jdbc.filter.FilterConverter;
import uk.ac.ed.epcc.webapp.jdbc.filter.SQLFilter;
import uk.ac.ed.epcc.webapp.model.data.DataObject;
import uk.ac.ed.epcc.webapp.model.data.DataObjectFactory;
import uk.ac.ed.epcc.webapp.model.data.FieldSQLExpression;
import uk.ac.ed.epcc.webapp.model.data.FieldValue;

import uk.ac.ed.epcc.webapp.model.data.Exceptions.DataFault;
import uk.ac.ed.epcc.webapp.model.data.filter.FilterUpdate;

/** class for performing in-database updates using properties
 * 
 * @author spb
 *
 * @param <T>
 */
public class PropertyUpdater<T extends DataObject> {

	public PropertyUpdater(DataObjectFactory<T> fac) throws InvalidArgument {
		super();
		this.fac=fac;
		ExpressionTargetFactory<T> etf = ExpressionCast.getExpressionTargetFactory(fac);
		if( etf == null) {
			throw new InvalidArgument("No ExpressionTargetFactory "+fac.getTag());
		}
		this.map = (RepositoryAccessorMap<T>) etf.getAccessorMap();
	}

	private DataObjectFactory<T> fac;
	private RepositoryAccessorMap<T> map;

	
	public BaseFilter<T> getFilter(RecordSelector sel) throws CannotFilterException{
		return map.getFilter(sel);
	}
	/**  Perform an update in the database
	 * 
	 * @param <X>
	 * @param tag
	 * @param value
	 * @param sel
	 * @return number of records modified
	 * @throws DataFault 
	 * @throws InvalidPropertyException 
	 * @throws CannotFilterException 
	 */
	@SuppressWarnings("unchecked")
	public final <X> int update(PropertyTag<X> tag, X value, RecordSelector sel) throws DataFault, InvalidPropertyException, CannotFilterException{
		BaseFilter<T> filter = getFilter(sel);
		AccessorMap<T> m = map;
		if( m.hasProperty(tag) && m.writable(tag)){
		try{
			SQLFilter<T> sql_filter = FilterConverter.convert(filter);
			FilterUpdate<T> update = map.getFilterUpdate();
			SQLExpression<X> targ = m.getSQLExpression(tag);
		
			if( targ != null && targ instanceof FieldValue){
			
				return update.update((FieldSQLExpression<X,T>)targ, value, sql_filter);
			}
		}catch(Exception e){
			//do things the hard way
			int count=0;
			for(T ur : fac.getResult(filter)){
				ExpressionTargetContainer proxy = map.getProxy(ur);
				proxy.setProperty(tag, value);
				if( ur.commit()){
					count++;
				}
				proxy.release();
				ur.release();
			}
			return count;
		}
		}
		throw new InvalidPropertyException(fac.getConfigTag(),tag);
	}
	/**  Perform an update in the database 
	 * 
	 * 
	 * @param <X>
	 * @param tag
	 * @param value
	 * @param sel
	 * @return number of records modified
	 * @throws DataFault 
	 * @throws CannotFilterException 
	 * @throws InvalidExpressionException 
	 */
	@SuppressWarnings("unchecked")
	public final <X> int update(PropertyTag<X> tag, PropExpression<X> value, RecordSelector sel) throws DataFault, CannotFilterException, InvalidExpressionException{
		BaseFilter<T> filter = getFilter(sel);
		AccessorMap m = map;
		if( m.hasProperty(tag) && m.writable(tag)){
		try{
			SQLFilter<T> sql_filter = FilterConverter.convert(filter);
			FilterUpdate<T> update = map.getFilterUpdate();
			SQLExpression<X> targ = m.getSQLExpression(tag);
			SQLExpression<X> value_expr = m.getSQLExpression(value);
			if( targ != null && targ instanceof FieldValue){
			
				return update.updateExpression((FieldSQLExpression<X,T>)targ, value_expr, sql_filter);
			}
		}catch(Exception e){
			//do things the hard way
			int count=0;
			for(T dat : fac.getResult(filter)){
				ExpressionTargetContainer ur = map.getProxy(dat);
				ur.setProperty(tag, ur.evaluateExpression(value));
				if( dat.commit()){
					count++;
				}
				ur.release();
				dat.release();
			}
			return count;
		}
		}
		throw new InvalidPropertyException(fac.getConfigTag(),tag);
	}
}
