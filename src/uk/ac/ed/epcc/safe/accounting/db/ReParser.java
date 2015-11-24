package uk.ac.ed.epcc.safe.accounting.db;

import java.util.Date;
import java.util.Iterator;

import uk.ac.ed.epcc.safe.accounting.ErrorSet;
import uk.ac.ed.epcc.safe.accounting.expr.DerivedPropertyMap;
import uk.ac.ed.epcc.safe.accounting.properties.PropertyMap;
import uk.ac.ed.epcc.safe.accounting.properties.StandardProperties;
import uk.ac.ed.epcc.safe.accounting.selector.RecordSelector;
import uk.ac.ed.epcc.webapp.AppContext;
/** Class to perform a record replace based on a stored Text field
 * 
 * @author spb
 *
 * @param <T>
 */
public class ReParser<T extends UsageRecordFactory.Use> {
	private final ParseUsageRecordFactory<T> target;
	public ReParser(ParseUsageRecordFactory<T> target) {
		this.target=target;
	}

	public String reparse(PropertyMap meta_data,RecordSelector sel) throws Exception{
		AppContext conn = target.getContext();
		ErrorSet errors = new ErrorSet();
		if( target.hasProperty(StandardProperties.TEXT_PROP)){
			
			Date start = new Date();
			int count=0;
			int error_count=0;
			target.startParse(meta_data);
			for(Iterator<T> it = target.getIterator(sel); it.hasNext();){
				T record = it.next();
				String current_line = record.getProperty(StandardProperties.TEXT_PROP);
				try{
					DerivedPropertyMap map = new DerivedPropertyMap(conn);
					meta_data.setContainer(map);
					


					if( target.parse(map, current_line) ){
						// add date and text
						map.setProperty(StandardProperties.INSERTED_PROP, start);
						map.setProperty(StandardProperties.TEXT_PROP, current_line);
						// make an un-commited record from the map
						T new_record = target.prepareRecord(map);
						target.deleteRecord(record);
						target.commitRecord(map, new_record);
						count++;
					}
				}catch(Exception e){
					conn.error(e,"Error in reparse");
					error_count++;
					if( error_count > 10 ){
						return "Too many errors";
					}
					errors.add("Re-parse error", current_line);
				}
			}
			StringBuilder result = target.endParse();
			result.append(errors.toString());
			result.append("Lines reparsed: "+count);
			return result.toString();
		}
		return "Text field not saved";
	}
}
