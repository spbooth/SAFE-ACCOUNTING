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
package uk.ac.ed.epcc.safe.accounting.charts;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import uk.ac.ed.epcc.safe.accounting.ErrorSet;
import uk.ac.ed.epcc.safe.accounting.properties.PropertyFinder;
import uk.ac.ed.epcc.webapp.AbstractContexed;
import uk.ac.ed.epcc.webapp.AppContext;
import uk.ac.ed.epcc.webapp.AppContextService;
import uk.ac.ed.epcc.webapp.PreRequisiteService;
import uk.ac.ed.epcc.webapp.config.ConfigService;
import uk.ac.ed.epcc.webapp.config.FilteredProperties;
/** Service to perform {@link PlotEntry} and {@link MapperEntry} lookups.
 * 
 * This class implements the default algorithm that use the {@link ConfigService}
 * (as implemented by the static factory methods in the target classes)
 * but an extended version can be 
 * substituted to customise the behaviour.
 * <p>
 * The <i>name</i> parameters may be prefixed with a mode string (separated by a <b>.</b>) to
 * set the mode parameter for the {@link FilteredProperties} used to make the {@link PlotEntry}.
 * Any parameter not set in the qualified form will be inherited from the un-qualified version.
 * 
 * @author spb
 *
 */
@PreRequisiteService(ConfigService.class)
public class ChartService extends AbstractContexed implements AppContextService<ChartService>{
	// This must not match valid prop-expressions (which can contain periods as floating point constants)
	private Pattern mode_pattern = Pattern.compile("([A-Za-z_]+)\\.([A-Za-z_]+)");
	public ChartService(AppContext c){
		super(c);
	}
	
	public PlotEntry getPlotEntry(ErrorSet errors,PropertyFinder finder,String name,String start,String end) throws Exception{
		String mode=null;
		
		Matcher m = mode_pattern.matcher(name);
		if( m.matches()) {
			mode=m.group(1);
			name=m.group(2);
		}
		return PlotEntry.getConfigPlotEntry(conn,errors, new FilteredProperties(conn.getService(ConfigService.class).getServiceProperties(),"PlotEntry",mode),finder, name, start, end);
	}
	public MapperEntry getMapperEntry(ErrorSet errors,PropertyFinder finder,String name) throws Exception{
		String mode=null;
		
		Matcher m = mode_pattern.matcher(name);
		if( m.matches()) {
			mode=m.group(1);
			name=m.group(2);
		}
		return MapperEntry.getConfigMapperEntry(conn,errors,new FilteredProperties(conn.getService(ConfigService.class).getServiceProperties(),MapperEntry.GROUP_ENTRY_BASE,mode), finder, name);
	}
	
	public void cleanup() {
		
	}

	public final Class<? super ChartService> getType() {
		return ChartService.class;
	}

}