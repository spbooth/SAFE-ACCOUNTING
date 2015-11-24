package uk.ac.ed.epcc.safe.accounting.allocations;

import java.util.Map;
import java.util.Set;

import uk.ac.ed.epcc.webapp.AppContext;
import uk.ac.ed.epcc.webapp.Contexed;
import uk.ac.ed.epcc.webapp.content.ContentBuilder;
import uk.ac.ed.epcc.webapp.content.UIGenerator;
import uk.ac.ed.epcc.webapp.forms.result.IndexTransitionResult;
import uk.ac.ed.epcc.webapp.session.SessionService;

public class AllocationList implements UIGenerator, Contexed {

	private final AppContext conn;
	public AllocationList(AppContext c) {
		this.conn=c;
	}

	public AppContext getContext() {
		return conn;
	}

	public ContentBuilder addContent(ContentBuilder builder) {
		if( getContext().getService(SessionService.class).hasRole(AllocationManager.ALLOCATION_ADMIN)){
			ContentBuilder div = builder.getPanel("block");
			div.addHeading(2, "View Allocations");
			div.addText("This page lists all of the configured types of allocation.");
			
			Map<String, Class> map = conn.getClassMap(AllocationManager.class);
			Set<String> tables = map.keySet();
			for(String table : tables){
				try {	
					AllocationPeriodTransitionProvider prov = new AllocationPeriodTransitionProvider(conn.makeObject(AllocationManager.class, table));
					div.addButton(conn, table, new IndexTransitionResult(prov));
				} catch (Exception e) {
					conn.error(e,"Error making AllocationTransitionProvider");
				}
			}
			
			div.addParent();
		}
		return builder;
	}

	
}
