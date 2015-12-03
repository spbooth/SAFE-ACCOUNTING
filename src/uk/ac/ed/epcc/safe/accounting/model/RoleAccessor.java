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
package uk.ac.ed.epcc.safe.accounting.model;

import uk.ac.ed.epcc.safe.accounting.properties.PropertyTarget;
import uk.ac.ed.epcc.webapp.jdbc.expr.Accessor;
import uk.ac.ed.epcc.webapp.jdbc.expr.CannotFilterException;
import uk.ac.ed.epcc.webapp.jdbc.expr.FilterProvider;
import uk.ac.ed.epcc.webapp.jdbc.filter.BaseFilter;
import uk.ac.ed.epcc.webapp.jdbc.filter.MatchCondition;
import uk.ac.ed.epcc.webapp.jdbc.filter.OrderFilter;
import uk.ac.ed.epcc.webapp.jdbc.filter.SQLFilter;
import uk.ac.ed.epcc.webapp.session.AppUser;
import uk.ac.ed.epcc.webapp.session.AppUserFactory;
import uk.ac.ed.epcc.webapp.session.SessionService;

/** Accessor to access Relationship info.
 * 
 * @author spb
 *
 * @param <T> type relationship applies to.
 */


public class RoleAccessor<T extends AppUser & PropertyTarget> implements Accessor<Boolean,T>, FilterProvider<T,Boolean> {
    private final SessionService<T> serv;
	private final String role;
	RoleAccessor(SessionService<T> serv, String property){
		this.serv=serv;
		this.role=property;
	}
	public Class<? super Boolean> getTarget() {
		
		return Boolean.class;
	}

	public Boolean getValue(T r) {
		return serv.canHaveRole(r, role);
	}

	@Override
	public String toString(){
		return "canHaveRole("+role+")";
	}
	public SQLFilter<T> getFilter(MatchCondition match,
			Boolean val) throws CannotFilterException {
		AppUserFactory<T> fac = serv.getLoginFactory();
		return fac.getRoleFilter(role);
	}
	public SQLFilter<T> getNullFilter(boolean is_null) throws CannotFilterException{
		throw new CannotFilterException("Cannot check role for null");
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
		return serv.getLoginFactory().getTarget();
	}
	

}