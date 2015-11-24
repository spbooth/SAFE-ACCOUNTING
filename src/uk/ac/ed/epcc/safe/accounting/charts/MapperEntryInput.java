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
/** Form Input for selecting a MapperEntry
 *  
 * @author spb
 *
 */
public class MapperEntryInput extends ParseAbstractInput<String> implements ListInput<String, MapperEntry> {
	
	private final Map<String,MapperEntry> items;
	public MapperEntryInput(AppContext conn,UsageProducer producer,String tag){
		
		items = new LinkedHashMap<String, MapperEntry>();
		for( MapperEntry e : MapperEntry.getMappers(conn, producer, tag)){
			items.put(e.getName(),e);
		}
	}
	
	public MapperEntry getItem() {
		return getItembyValue(getValue());
	}
	
	public void setItem(MapperEntry item) {
		if( item == null ){
			setValue(null);
		}else{
			setValue(item.getName());
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
		return item.getName();
	}

	public String getTagByValue(String value) {
		return value;
	}
	
	public String getText(MapperEntry item) {
		return item.getDescription();
	}
	@Override
	public <R> R accept(InputVisitor<R> vis) throws Exception {
		return vis.visitListInput(this);
	}
}
