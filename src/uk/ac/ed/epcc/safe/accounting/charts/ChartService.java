package uk.ac.ed.epcc.safe.accounting.charts;

import uk.ac.ed.epcc.safe.accounting.properties.PropertyFinder;
import uk.ac.ed.epcc.webapp.AppContext;
import uk.ac.ed.epcc.webapp.AppContextService;
import uk.ac.ed.epcc.webapp.Contexed;
import uk.ac.ed.epcc.webapp.PreRequisiteService;
import uk.ac.ed.epcc.webapp.config.ConfigService;
import uk.ac.ed.epcc.webapp.config.FilteredProperties;
/** Service to perform {@link PlotEntry} and {@link MapperEntry} lookups.
 * 
 * This class implements the default algorithm that use the {@link ConfigService}
 * (as implemented by the static factory methods in the target classes)
 * but an extended version can be 
 * substituted to customise the behaviour.
 * 
 * @author spb
 *
 */
@PreRequisiteService(ConfigService.class)
public class ChartService implements Contexed, AppContextService<ChartService>{

	private final AppContext conn;
	
	public ChartService(AppContext c){
		this.conn=c;
	}
	
	public PlotEntry getPlotEntry(PropertyFinder finder,String name,String start,String end) throws Exception{
		return PlotEntry.getConfigPlotEntry(conn, new FilteredProperties(conn.getService(ConfigService.class).getServiceProperties(),"PlotEntry"),finder, name, start, end);
	}
	public MapperEntry getMapperEntry(PropertyFinder finder,String name) throws Exception{
		return MapperEntry.getConfigMapperEntry(conn,new FilteredProperties(conn.getService(ConfigService.class).getServiceProperties(),MapperEntry.GROUP_ENTRY_BASE), finder, name);
	}
	
	public void cleanup() {
		
	}

	public final Class<? super ChartService> getType() {
		return ChartService.class;
	}

	public AppContext getContext() {
		return conn;
	}

}
