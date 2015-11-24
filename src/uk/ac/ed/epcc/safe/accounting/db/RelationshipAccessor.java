  // Copyright - The University of Edinburgh 2011
/*******************************************************************************
 * Copyright (c) - The University of Edinburgh 2010
 *******************************************************************************/
package uk.ac.ed.epcc.safe.accounting.db;

import uk.ac.ed.epcc.safe.accounting.properties.PropertyTarget;
import uk.ac.ed.epcc.webapp.AppContext;
import uk.ac.ed.epcc.webapp.jdbc.expr.Accessor;
import uk.ac.ed.epcc.webapp.jdbc.expr.CannotFilterException;
import uk.ac.ed.epcc.webapp.jdbc.expr.FilterProvider;
import uk.ac.ed.epcc.webapp.jdbc.filter.MatchCondition;
import uk.ac.ed.epcc.webapp.jdbc.filter.SQLFilter;
import uk.ac.ed.epcc.webapp.logging.Logger;
import uk.ac.ed.epcc.webapp.logging.LoggerService;
import uk.ac.ed.epcc.webapp.model.data.DataObject;
import uk.ac.ed.epcc.webapp.model.relationship.RelationshipProvider;
import uk.ac.ed.epcc.webapp.session.AppUser;
import uk.ac.ed.epcc.webapp.session.SessionService;

/** Accessor to access Relationship info.
 * 
 * @author spb
 *
 * @param <T> type relationship applies to.
 */
@uk.ac.ed.epcc.webapp.Version("$Id: RelationshipAccessor.java,v 1.22 2014/11/27 21:34:18 spb Exp $")

public class RelationshipAccessor<T extends DataObject & PropertyTarget> implements Accessor<Boolean,T>, FilterProvider<T,Boolean> {
    
	private RelationshipProvider<?,T> rel;
	private String role;
	RelationshipAccessor(RelationshipProvider<?,T> rel, String property){
		this.rel=rel;
		this.role=property;
	}
	public Class<? super Boolean> getTarget() {
		
		return Boolean.class;
	}

	public Boolean getValue(T r) {
		if( rel == null ){
			return false;
		}
		
		return Boolean.valueOf(rel.hasRole(rel.getContext().getService(SessionService.class),r, role));
	}

	@Override
	public String toString(){
		return "hasRole("+rel.getTag()+","+role+")";
	}
	public SQLFilter<T> getFilter(MatchCondition match,
			Boolean val) throws CannotFilterException {
		AppContext context = rel.getContext();
		AppUser user = context.getService(SessionService.class).getCurrentPerson();
		Logger log = context.getService(LoggerService.class).getLogger(getClass());
		log.debug("RelationshipAccessor getFilter "+role+" "+match+" "+val);
		boolean check = val.booleanValue();
		if( match != null){
			check = ! check;
		}
		if( check){
		    return rel.getTargetFilter(user,role);
		}else{
			throw new CannotFilterException("Cannot negate relationship");
		}
	}
	public SQLFilter<T> getNullFilter(boolean is_null) throws CannotFilterException{
		throw new CannotFilterException("Cannot check relationship for null");
	}
	public boolean canSet() {
		
		return false;
	}
	public void setValue(T r, Boolean value) {
		throw new UnsupportedOperationException("Set not supported");
		
	}
	public SQLFilter<T> getOrderFilter(boolean descending)
			throws CannotFilterException {
		throw new CannotFilterException("Order filter not supported");
	}
	public Class<? super T> getFilterType() {
		return rel.getTargetFactory().getTarget();
	}
	

}