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

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import uk.ac.ed.epcc.safe.accounting.UsageProducer;
import uk.ac.ed.epcc.webapp.AppContext;
import uk.ac.ed.epcc.webapp.config.FilteredProperties;
import uk.ac.ed.epcc.webapp.forms.inputs.SimpleListInput;
import uk.ac.ed.epcc.webapp.logging.LoggerService;
/** Form Input for selecting a MapperEntry
 *  
 *  * {@link MapperEntry}s are built using {@link FilteredProperties} if a <b>tag</b> is specified
 * this is used to set the <i>mode</i> allowing a customised set of {@link MapperEntry}s and the
 * string values generated are qualified using the same tag.
 * @author spb
 *
 */
public class MapperEntryInput extends SimpleListInput<MapperEntry> {
	
	private final Map<String,MapperEntry> items;
	private final String prefix;
	public MapperEntryInput(AppContext conn,UsageProducer producer,String tag){
		if( tag != null && ! tag.isEmpty()) {
			prefix=tag+".";
		}else {
			prefix="";
		}
		items = new LinkedHashMap<>();
		if( producer != null){
			for( MapperEntry e : MapperEntry.getMappers(conn, producer, tag)){
				items.put(prefix+e.getName(),e);
			}
		}else{
			conn.getService(LoggerService.class).getLogger(getClass()).warn("No UsageProducer in MapperEntryInput");
		}
	}
	@Override
	public MapperEntry getItem() {
		return getItembyValue(getValue());
	}
	
	@Override
	public MapperEntry getItembyValue(String value) {
		return items.get(value);
	}
	@Override
	public Iterator<MapperEntry> getItems() {
		return items.values().iterator();
	}
	@Override
	public int getCount(){
		return items.size();
	}
	@Override
	public String getTagByItem(MapperEntry item) {
		return prefix+item.getName();
	}
	
	@Override
	public String getText(MapperEntry item) {
		if(item == null) {
			return null;
		}
		String description = item.getDescription();
		if( description != null && ! description.isEmpty()) {
			return description;
		}
		return item.getName();
	}
	
	@Override
	public boolean isValid(MapperEntry item) {
		return items.containsValue(item);
	}
}