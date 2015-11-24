// Copyright - The University of Edinburgh 2011
/*******************************************************************************
 * Copyright (c) - The University of Edinburgh 2010
 *******************************************************************************/
package uk.ac.ed.epcc.safe.accounting.db;

import java.sql.ResultSet;
import java.util.List;

import uk.ac.ed.epcc.safe.accounting.expr.NamePropExpression;
import uk.ac.ed.epcc.webapp.AppContext;
import uk.ac.ed.epcc.webapp.jdbc.exception.DataException;
import uk.ac.ed.epcc.webapp.jdbc.expr.CannotFilterException;
import uk.ac.ed.epcc.webapp.jdbc.expr.FilterProvider;
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
@uk.ac.ed.epcc.webapp.Version("$Id: ClassificationSQLValue.java,v 1.17 2015/08/13 21:52:34 spb Exp $")

public class ClassificationSQLValue<H extends DataObject, T extends DataObject> implements SQLValue<String>, FilterProvider<H,String>{
	private final Class<? super H> target;
	// has to be at least an SQLAccessor to support any filtering.
	// 
	private final  SQLValue<IndexedReference<T>> a;
	private final AppContext ctx;
	private final IndexedProducer<T> producer;
	public ClassificationSQLValue(AppContext c,Class<? super H> target,IndexedProducer<T> prod,SQLValue<IndexedReference<T>> acc) {
		this.target=target;
		this.a=acc;
		this.producer=prod;
		ctx=c;
	}
	public Class<? super String> getTarget() {
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
				return new FalseFilter<H>(target);
			}
			return ((FilterProvider)a).getFilter(match, ref);
		}
		throw new CannotFilterException("Underlying value not a FilterProvider");
	}
    @SuppressWarnings("unchecked")
	public SQLFilter<H> getNullFilter(boolean is_null) throws CannotFilterException, NoSQLFilterException {
		// Name is only null if refernce is null
		if( a instanceof FilterProvider){
			return ((FilterProvider)a).getNullFilter(is_null );
		}
		throw new CannotFilterException("Underlying value not a FilterProvider");
	}
    public SQLFilter<H> getOrderFilter(boolean descending)
			throws CannotFilterException, NoSQLFilterException {
    	if( a instanceof FilterProvider){
			return ((FilterProvider)a).getOrderFilter(descending);
		}
		throw new CannotFilterException("Underlying value not a FilterProvider");
	}
	public List<PatternArgument> getParameters(List<PatternArgument> list) {
		return list;
	}
	
	
	public int add(StringBuilder sb, boolean qualify) {
		return a.add(sb, qualify);
	}

	public String makeObject(ResultSet rs, int pos) throws DataException {
		return NamePropExpression.refToName(ctx, a.makeObject(rs, pos));
	}
	
	public SQLFilter getRequiredFilter() {
		return a.getRequiredFilter();
	}
	public Class<? super H> getFilterType() {
		return target;
	}
	
	
	
}