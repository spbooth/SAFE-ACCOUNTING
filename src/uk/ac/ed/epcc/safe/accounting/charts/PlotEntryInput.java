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
import uk.ac.ed.epcc.webapp.forms.exceptions.ParseException;
import uk.ac.ed.epcc.webapp.forms.inputs.InputVisitor;
import uk.ac.ed.epcc.webapp.forms.inputs.ListInput;
import uk.ac.ed.epcc.webapp.forms.inputs.ParseAbstractInput;
/** Form Input for selecting a PlotEntry
 *  
 * @author spb
 *
 */
public class PlotEntryInput extends ParseAbstractInput<String> implements ListInput<String, PlotEntry> {
	
	private final Map<String,PlotEntry> items;
	
	public PlotEntryInput(AppContext conn,UsageProducer producer,String tag){
		
		items = new LinkedHashMap<String, PlotEntry>();
		for( PlotEntry e : PlotEntry.getPlotSet(producer.getFinder(),conn, tag)){
			if( e.compatible(producer)){
				items.put(e.getName(),e);
			}
		}
	}
	
	public PlotEntry getItem() {
		return getItembyValue(getValue());
	}
	
	public void setItem(PlotEntry item) {
		if( item == null ){
			setValue(null);
		}else{
			setValue(item.getName());
		}
	}
	
	public void parse(String v) throws ParseException {
		if( v != null && items.containsKey(v)){
			setValue(v);
		}else if( v == null || v.trim().length() == 0){
			setValue(null);
		}else{
			if( v != null){
				// check for descriptions
				for(PlotEntry e : items.values()){
					if(v.equals(e.getDescription()) ){
						setValue(e.getName());
					}
				}
			}
			throw new ParseException("Invalid PlotEntry ");
		}

	}
	
	public PlotEntry getItembyValue(String value) {
		return items.get(value);
	}
	
	public Iterator<PlotEntry> getItems() {
		return items.values().iterator();
	}
	public int getCount(){
		return items.size();
	}
	public String getTagByItem(PlotEntry item) {
		return item.getName();
	}
	
	public String getTagByValue(String value) {
		return value;
	}
	
	public String getText(PlotEntry item) {
		String text = item.getDescription();
		if( text == null ){
			text=item.getName();
		}
		return text;
	}

	@Override
	public <R> R accept(InputVisitor<R> vis) throws Exception {
		return vis.visitListInput(this);
	}

	@Override
	public boolean isValid(PlotEntry item) {
		return items.containsValue(item);
	}
	
}