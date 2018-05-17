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

import java.util.List;

import uk.ac.ed.epcc.safe.accounting.db.ExpressionTargetFactoryComposite;
import uk.ac.ed.epcc.webapp.AppContext;
import uk.ac.ed.epcc.webapp.jdbc.filter.OrderClause;
import uk.ac.ed.epcc.webapp.session.AppUser;
import uk.ac.ed.epcc.webapp.session.AppUserFactory;
import uk.ac.ed.epcc.webapp.session.WebNameFinder;

/** An {@link AppUserFactory} that supports accounting properties and parse mechanisms.
 * 
 * @author spb
 *
 * @param <P>
 */

public class PropertyPersonFactory<P extends AppUser> extends AppUserFactory<P> {

	private ExpressionTargetFactoryComposite<P> etf = new ExpressionTargetFactoryComposite<>(this);
	private AppUserUploadParseTargetPlugin<P, ?> parse_plugin = new AppUserUploadParseTargetPlugin<>(this);
	
	
	//private WebNameFinder<P> web_name = new WebNameFinder<>(this);
	
    
    
	
	public PropertyPersonFactory() {
		super();
	}

	public PropertyPersonFactory(AppContext ctx, String table) {
		this();
		setContext(ctx, table);
	}

	public PropertyPersonFactory(AppContext ctx){
		this(ctx,"Person");
	}
	
	
	

	
	@Override
	protected List<OrderClause> getOrder() {
		List<OrderClause> order = super.getOrder();
		order.add(res.getOrder(WebNameFinder.WEB_NAME, false));
		return order;
	}
	
	

	
	
}