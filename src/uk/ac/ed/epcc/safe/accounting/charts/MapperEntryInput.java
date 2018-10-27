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
import uk.ac.ed.epcc.webapp.forms.exceptions.ParseException;
import uk.ac.ed.epcc.webapp.forms.inputs.InputVisitor;
import uk.ac.ed.epcc.webapp.forms.inputs.ListInput;
import uk.ac.ed.epcc.webapp.forms.inputs.ParseAbstractInput;
import uk.ac.ed.epcc.webapp.logging.LoggerService;
/** Form Input for selecting a MapperEntry
 *  
 *  * {@link MapperEntry}s are built using {@link FilteredProperties} if a <b>tag</b> is specified
 * this is used to set the <i>mode</i> allowing a customised set of {@link MapperEntry}s and the
 * string values generated are qualified using the same tag.
 * @author spb
 *
 */
public class MapperEntryInput extends ParseAbstractInput<String> implements ListInput<String, MapperEntry> {
	
	private final Map<String,MapperEntry> items;
	private final String prefix;
	public MapperEntryInput(AppContext conn,UsageProducer producer,String tag){
		if( tag != null && ! tag.isEmpty()) {
			prefix=tag+".";
		}else {
			prefix="";
		}
		items = new LinkedHashMap<String, MapperEntry>();
		if( producer != null){
			for( MapperEntry e : MapperEntry.getMappers(conn, producer, tag)){
				items.put(prefix+e.getName(),e);
			}
		}else{
			conn.getService(LoggerService.class).getLogger(getClass()).warn("No UsageProducer in MapperEntryInput");
		}
	}
	
	public MapperEntry getItem() {
		return getItembyValue(getValue());
	}
	
	public void setItem(MapperEntry item) {
		if( item == null ){
			setValue(null);
		}else{
			setValue(getTagByItem(item));
		}
	}
	
	public void parse(String v) throws ParseException {
		if( v != null && items.containsKey(v)){
			setValue(v);
		}else if ( v == null || v.trim().length() == 0){
			setValue(null);
		}else{
			throw new ParseException("Invalid MapperEntry ");
		}
		
	}
	
	public MapperEntry getItembyValue(String value) {
		return items.get(value);
	}
	
	public Iterator<MapperEntry> getItems() {
		return items.values().iterator();
	}
	public int getCount(){
		return items.size();
	}
	public String getTagByItem(MapperEntry item) {
		return prefix+item.getName();
	}

	public String getTagByValue(String value) {
		return value;
	}
	
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
	public <R> R accept(InputVisitor<R> vis) throws Exception {
		return vis.visitListInput(this);
	}

	@Override
	public boolean isValid(MapperEntry item) {
		return items.containsValue(item);
	}
}