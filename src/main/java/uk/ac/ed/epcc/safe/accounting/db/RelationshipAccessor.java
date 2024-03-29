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

import uk.ac.ed.epcc.webapp.AppContext;
import uk.ac.ed.epcc.webapp.jdbc.expr.Accessor;
import uk.ac.ed.epcc.webapp.jdbc.expr.CannotFilterException;
import uk.ac.ed.epcc.webapp.jdbc.expr.FilterProvider;
import uk.ac.ed.epcc.webapp.jdbc.filter.BaseFilter;
import uk.ac.ed.epcc.webapp.jdbc.filter.FilterConverter;
import uk.ac.ed.epcc.webapp.jdbc.filter.MatchCondition;
import uk.ac.ed.epcc.webapp.jdbc.filter.SQLFilter;
import uk.ac.ed.epcc.webapp.logging.Logger;
import uk.ac.ed.epcc.webapp.logging.LoggerService;
import uk.ac.ed.epcc.webapp.model.data.DataObject;
import uk.ac.ed.epcc.webapp.model.data.DataObjectFactory;
import uk.ac.ed.epcc.webapp.session.SessionService;
import uk.ac.ed.epcc.webapp.session.UnknownRelationshipException;

/** {@link Accessor} to access Relationship info.
 * 
 * @author spb
 *
 * @param <T> type relationship applies to.
 */


public class RelationshipAccessor<T extends DataObject> implements Accessor<Boolean,T>, FilterProvider<T,Boolean> {
    
	private DataObjectFactory<T> fac;
	private String role;
	RelationshipAccessor(DataObjectFactory<T> fac, String property){
		this.fac=fac;
		this.role=property;
	}
	public Class<Boolean> getTarget() {
		
		return Boolean.class;
	}

	public Boolean getValue(T r) {
		if( fac == null ){
			return false;
		}
		SessionService<?> sess = fac.getContext().getService(SessionService.class);
		try {
			return Boolean.valueOf(
					fac.matches(sess.getRelationshipRoleFilter(fac, role), r)		
			);
		} catch (UnknownRelationshipException e) {
			fac.getContext().getService(LoggerService.class).getLogger(getClass()).error("Error getting value", e);
			return Boolean.FALSE;
		}
	}

	@Override
	public String toString(){
		return "hasRole("+fac.getTag()+","+role+")";
	}
	@Override
	public SQLFilter<T> getFilter(MatchCondition match,
			Boolean val) throws CannotFilterException {
		AppContext context = fac.getContext();
		SessionService user = context.getService(SessionService.class);
		Logger log = context.getService(LoggerService.class).getLogger(getClass());
		log.debug("RelationshipAccessor getFilter "+role+" "+match+" "+val);
		boolean check = val.booleanValue();
		if( match != null){
			check = ! check;
		}
		if( check){
			try {
				BaseFilter<T> fil = user.getRelationshipRoleFilter(fac, role);
				return fil.acceptVisitor(new FilterConverter<T>());
			} catch (Exception e) {
				throw new CannotFilterException("Cannot make relationship filter", e);
			}
		}else{
			throw new CannotFilterException("Cannot negate relationship");
		}
	}
	@Override
	public SQLFilter<T> getNullFilter(boolean is_null) throws CannotFilterException{
		throw new CannotFilterException("Cannot check relationship for null");
	}
	@Override
	public SQLFilter<T> getOrderFilter(boolean descending)
			throws CannotFilterException {
		throw new CannotFilterException("Order filter not supported");
	}
	@Override
	public String getFilterTag() {
		return fac.getTag();
	}
	

}