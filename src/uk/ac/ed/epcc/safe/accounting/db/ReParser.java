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
package uk.ac.ed.epcc.safe.accounting.db;

import java.util.Date;
import java.util.Iterator;

import uk.ac.ed.epcc.safe.accounting.ErrorSet;
import uk.ac.ed.epcc.safe.accounting.expr.DerivedPropertyMap;
import uk.ac.ed.epcc.safe.accounting.properties.PropertyMap;
import uk.ac.ed.epcc.safe.accounting.properties.StandardProperties;
import uk.ac.ed.epcc.safe.accounting.selector.RecordSelector;
import uk.ac.ed.epcc.safe.accounting.update.PropertyContainerParser;
import uk.ac.ed.epcc.webapp.AppContext;
/** Class to perform a record replace based on a stored Text field
 * 
 * @author spb
 *
 * @param <T> Type of output record
 * @param <R> Parser IR type
 */
public class ReParser<T extends UsageRecordFactory.Use,R> {
	private final ParseUsageRecordFactory<T,R> target;
	public ReParser(ParseUsageRecordFactory<T,R> target) {
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
			PropertyContainerParser<R> parser = target.getParser();
			for(Iterator<T> it = target.getIterator(sel); it.hasNext();){
				T record = it.next();
				String current_line = record.getProperty(StandardProperties.TEXT_PROP);
				try{
					DerivedPropertyMap map = new DerivedPropertyMap(conn);
					meta_data.setContainer(map);
					

					R r= parser.getRecord(current_line);
					if( target.parse(map, r) ){
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