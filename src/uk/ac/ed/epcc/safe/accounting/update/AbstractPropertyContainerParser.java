//| Copyright - The University of Edinburgh 2011                            |
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
/*******************************************************************************
 * Copyright (c) - The University of Edinburgh 2010
 *******************************************************************************/
package uk.ac.ed.epcc.safe.accounting.update;

import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import uk.ac.ed.epcc.safe.accounting.expr.PropExpressionMap;
import uk.ac.ed.epcc.safe.accounting.properties.PropertyTag;
import uk.ac.ed.epcc.webapp.AppContext;
import uk.ac.ed.epcc.webapp.exceptions.ConsistencyError;
import uk.ac.ed.epcc.webapp.exceptions.InvalidArgument;
import uk.ac.ed.epcc.webapp.jdbc.table.TableSpecification;
import uk.ac.ed.epcc.webapp.logging.LoggerService;
/** template class for {@link PropertyContainerParser} using Strings as the intermediate representation.
 * 
 * @author spb
 *
 */
public abstract class AbstractPropertyContainerParser extends AbstractPropertyContainerUpdater implements PropertyContainerParser<String> {
	public static final String DUPLICATE_KEY = "duplicate_key";
    private Set<String> unique=null;
	@Override
	protected final void unique(String name) {
		unique.add(name);
	}
	/* (non-Javadoc)
	 * @see uk.ac.ed.epcc.safe.accounting.UsageRecordParser#getDefaultTableSpecification()
	 */
	public  TableSpecification modifyDefaultTableSpecification(AppContext c,TableSpecification spec,PropExpressionMap map,String table_name) {
		
		unique = new HashSet<String>();
		TableSpecification result = super.modifyDefaultTableSpecification(c, spec,map, table_name);
		if( result == null ){
			return result;
		}
		//		try {
//			// Note this will only work when the field name follows the 
//			// name of the BaseParser property
//			spec.new Index("complete_key", false, ENDED_PROP.getName());
//		} catch (InvalidArgument e) {
//			// just ignore index
//			c.getService(LoggerService.class).getLogger(getClass()).warn("CompletedTimestamp not mapped to table",e);
//		}
		try {
			if( unique.size() > 0){
				spec.new Index(DUPLICATE_KEY,true, unique.toArray(new String[unique.size()]));
			}
		} catch (InvalidArgument e) {
			// just ignore index
			c.getService(LoggerService.class).getLogger(getClass()).warn("Error making unique key",e);
		}
		return spec;
	}
	public Set<PropertyTag> getDefaultUniqueProperties() {
		Set<PropertyTag> unique = new HashSet<PropertyTag>();
		Class myclass= getClass();
        for( Field f : myclass.getFields()){
        	//log.debug("consider field "+f.getName()+" "+f.getType().getCanonicalName());
        	if( PropertyTag.class.isAssignableFrom(f.getType()) && f.isAnnotationPresent(AutoTable.class)){
        		try {
					PropertyTag tag = (PropertyTag) f.get(this);
					
					AutoTable t = f.getAnnotation(AutoTable.class);
					
					if( t != null && t.unique()){
						unique.add(tag);
					}
				} catch (Exception e) {
					throw new ConsistencyError("Error creating default unique properties",e);
				}
        		
        	}
        }
		return unique;
	}
	
	
	/** split a multiple record update into individual records 
	 * in the way appropriate to this accountng scheme.
	 * Usually records are one per line but some schmemes may
	 * use other formats like XML
	 * 
	 * @param update
	 * @return Iterator<String>
   * @throws AccountingParseException If a problem occurs while splitting the records
	 */
	public Iterator<String> splitRecords(String update)
			throws AccountingParseException {
		return new StringSplitter(update);
	}
	/* (non-Javadoc)
	 * @see uk.ac.ed.epcc.safe.accounting.update.PropertyContainerParser#formatRecord(java.lang.Object)
	 */
	@Override
	public String formatRecord(String record) {
		return record;
	}
	@Override
	public String getRecord(String text){
		return text;
	}

	
}