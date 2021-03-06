//| Copyright - The University of Edinburgh 2015                            |
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
package uk.ac.ed.epcc.safe.accounting.allocations;

import java.util.Map;
import java.util.Set;

import uk.ac.ed.epcc.webapp.AbstractContexed;
import uk.ac.ed.epcc.webapp.AppContext;
import uk.ac.ed.epcc.webapp.content.ContentBuilder;
import uk.ac.ed.epcc.webapp.content.UIGenerator;
import uk.ac.ed.epcc.webapp.forms.result.IndexTransitionResult;
import uk.ac.ed.epcc.webapp.session.SessionService;

public class AllocationList extends AbstractContexed implements UIGenerator {

	public AllocationList(AppContext c) {
		super(c);
	}
	public ContentBuilder addContent(ContentBuilder builder) {
		if( getContext().getService(SessionService.class).hasRoleFromList(AllocationManager.ALLOCATION_ADMIN_ROLE,AllocationManager.ALLOCATION_VIEW_ROLE)){
			ContentBuilder div = builder.getPanel("block");
			div.addHeading(2, "View Allocations");
			div.addText("This page lists all of the configured types of allocation.");
			
			Map<String, Class> map = conn.getClassMap(AllocationManager.class);
			Set<String> tables = map.keySet();
			for(String table : tables){
				try {	
					AllocationPeriodTransitionProvider prov = new AllocationPeriodTransitionProvider(conn.makeObject(AllocationManager.class, table));
					if( prov.allowTransition(getContext(), null, prov.getIndexTransition())) {
						div.addButton(conn, table, new IndexTransitionResult(prov));
					}
				} catch (Exception e) {
					getLogger().error("Error making AllocationTransitionProvider",e);
				}
			}
			
			div.addParent();
		}
		return builder;
	}

	
}