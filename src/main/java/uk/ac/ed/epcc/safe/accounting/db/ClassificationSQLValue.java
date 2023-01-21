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
import java.sql.SQLException;
import java.util.List;

import uk.ac.ed.epcc.safe.accounting.expr.NamePropExpression;
import uk.ac.ed.epcc.webapp.AppContext;
import uk.ac.ed.epcc.webapp.jdbc.exception.DataException;
import uk.ac.ed.epcc.webapp.jdbc.expr.CannotFilterException;
import uk.ac.ed.epcc.webapp.jdbc.expr.FilterProvider;
import uk.ac.ed.epcc.webapp.jdbc.expr.NestedSQLValue;
import uk.ac.ed.epcc.webapp.jdbc.expr.SQLValue;
import uk.ac.ed.epcc.webapp.jdbc.filter.FalseFilter;
import uk.ac.ed.epcc.webapp.jdbc.filter.MatchCondition;
import uk.ac.ed.epcc.webapp.jdbc.filter.NoSQLFilterException;
import uk.ac.ed.epcc.webapp.jdbc.filter.PatternArgument;
import uk.ac.ed.epcc.webapp.jdbc.filter.SQLFilter;
import uk.ac.ed.epcc.webapp.model.NameFinder;
import uk.ac.ed.epcc.webapp.model.data.DataObject;
import uk.ac.ed.epcc.webapp.model.data.reference.IndexedProducer;
import uk.ac.ed.epcc.webapp.model.data.reference.IndexedReference;
/** An SQLAccessor that retrieves the string representation of an object pointed to by a reference field.
 * Unlike a simple string cast it also supports filtering.
 * It will also generate the string <b>Unknown</b> on a null reference 
 * @author spb
 * @param <H> type of owning object
 *
 * @param <T> type of remote object
 */


public class ClassificationSQLValue<H extends DataObject, T extends DataObject> implements NestedSQLValue<String,IndexedReference<T>>, FilterProvider<H,String>{
	
	// has to be at least an SQLAccessor to support any filtering.
	// 
	private final  SQLValue<IndexedReference<T>> a;
	private final AppContext ctx;
	private final IndexedProducer<T> producer;
	public ClassificationSQLValue(AppContext c,IndexedProducer<T> prod,SQLValue<IndexedReference<T>> acc) {
		
		this.a=acc;
		this.producer=prod;
		ctx=c;
	}
	public Class<String> getTarget() {
		return String.class;
	}
	@Override
	public String toString(){
		return "Name("+a.toString()+")";
	}
	
	
	@SuppressWarnings("unchecked")
	public SQLFilter<H> getFilter(MatchCondition match, String val) throws CannotFilterException, NoSQLFilterException {
		if( a instanceof FilterProvider && producer instanceof NameFinder){
			T peer = ((NameFinder<T>)producer).findFromString(val);
			IndexedReference ref = producer.makeReference(peer);
			if( peer == null ){
				return new FalseFilter<>();
			}
			return ((FilterProvider)a).getFilter(match, ref);
		}
		throw new NoSQLFilterException("Underlying value not a FilterProvider");
	}
    @SuppressWarnings("unchecked")
	public SQLFilter<H> getNullFilter(boolean is_null) throws CannotFilterException, NoSQLFilterException {
		// Name is only null if reference is null
		if( a instanceof FilterProvider){
			return ((FilterProvider)a).getNullFilter(is_null );
		}
		throw new NoSQLFilterException("Underlying value not a FilterProvider");
	}
    public SQLFilter<H> getOrderFilter(boolean descending)
			throws CannotFilterException, NoSQLFilterException {
    	if( a instanceof FilterProvider){
			return ((FilterProvider)a).getOrderFilter(descending);
		}
		throw new NoSQLFilterException("Underlying value not a FilterProvider");
	}
	public List<PatternArgument> getParameters(List<PatternArgument> list) {
		return list;
	}
	
	
	public int add(StringBuilder sb, boolean qualify) {
		return a.add(sb, qualify);
	}

	public String makeObject(ResultSet rs, int pos) throws DataException, SQLException {
		return NamePropExpression.refToName(ctx, a.makeObject(rs, pos));
	}
	
	@Override
	public String getFilterTag() {
		return a.getFilterTag();
	}
	@Override
	public SQLValue<IndexedReference<T>> getNested() {
		return a;
	}
	
	
	
}