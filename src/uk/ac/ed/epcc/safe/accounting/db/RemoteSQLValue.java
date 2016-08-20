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
package uk.ac.ed.epcc.safe.accounting.db;

import java.sql.ResultSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import uk.ac.ed.epcc.safe.accounting.expr.PropertyCastException;
import uk.ac.ed.epcc.webapp.AppContext;
import uk.ac.ed.epcc.webapp.Feature;
import uk.ac.ed.epcc.webapp.jdbc.exception.DataException;
import uk.ac.ed.epcc.webapp.jdbc.expr.FilterProvider;
import uk.ac.ed.epcc.webapp.jdbc.expr.IndexedSQLValue;
import uk.ac.ed.epcc.webapp.jdbc.expr.SQLValue;
import uk.ac.ed.epcc.webapp.jdbc.filter.PatternArgument;
import uk.ac.ed.epcc.webapp.jdbc.filter.SQLFilter;
import uk.ac.ed.epcc.webapp.logging.Logger;
import uk.ac.ed.epcc.webapp.logging.LoggerService;
import uk.ac.ed.epcc.webapp.model.data.DataObject;
import uk.ac.ed.epcc.webapp.model.data.IndexedFieldValue;
import uk.ac.ed.epcc.webapp.model.data.reference.IndexedReference;

/** An SQLAccessor that needs to follow a remote reference to access its data.
 * This class performs the de-reference in java rather than using a join so the assumption is that
 * remote table in relatively small.
 * 
 * @author spb
 * @param <H> type of owning object
 * @param <R> Type of remote object
 * @param <T> target type of accessor
 *
 */
public abstract class RemoteSQLValue<H extends DataObject,R extends DataObject, T>  implements SQLValue<T>, FilterProvider<H,T>{
	public static final Feature CACHE_REMOTE_ACCESSOR_FEATURE = new Feature("cache.remote-accessor",true,"cache expression results when implementing remore expression as a SQLValue");
	private final IndexedSQLValue<H, R> a;
	private final AppContext c;
	private Map<IndexedReference<R>,T> cache=null;
 	public RemoteSQLValue(AppContext c,IndexedSQLValue<H, R> a) throws PropertyCastException{

		this.c=c;
		this.a=a;
		if( CACHE_REMOTE_ACCESSOR_FEATURE.isEnabled(c)){
			cache = new HashMap<IndexedReference<R>,T>();
		}
	}
	
	/** SQLValue for remote object Reference
	 * 
	 * @return DataObjectRefefenceAccessor
	 */
	public IndexedSQLValue<H, R> getReferenceValue(){
		return a;
	}
	public T getRemoteValueFromReference(IndexedReference<R> ref){
		if(cache != null ){
			T result = cache.get(ref);
			if( result != null ){
				return result;
			}
			if( ref.isNull() ){
				result = getRemoteValueFromNull();
			}else{
				result = getRemoteValue(ref.getIndexed(c));
			}
			cache.put(ref,result);
			return result;
		}
		if( ref.isNull() ){
			return getRemoteValueFromNull();
		}
		return getRemoteValue(ref.getIndexed(c));
	}
	/** Get the remote value from the referenced object
	 * 
	 * @param o
	 * @return remote value
	 */
	public abstract T getRemoteValue(R o);
	/** get the remote value corresponding to a null reference.
	 * 
	 * @return remote value
	 */
	public abstract T getRemoteValueFromNull();

	
	public AppContext getContext(){
		return c;
	}
	protected Logger getLogger(){
		return c.getService(LoggerService.class).getLogger(getClass());
	}
	
	public final T makeObject(ResultSet rs, int pos) throws DataException {
		//Logger log = getContext().getService(LoggerService.class).getLogger(getClass());
		IndexedReference<R> ref = a.makeObject(rs, pos);
		T res = getRemoteValueFromReference(ref);
		//log.debug("RemoteAccessor on "+ref.toString()+" generates "+res+" class "+res.getClass());
		return res;
	}
	public int add(StringBuilder sb, boolean qualify) {
		return a.add(sb,qualify);
		
	}
	public List<PatternArgument> getParameters(List<PatternArgument> list) {
		return a.getParameters(list);
	}
	
	public SQLFilter getRequiredFilter() {

		return a.getRequiredFilter();
	}
	
}