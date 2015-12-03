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
package uk.ac.ed.epcc.safe.accounting.update;

import java.lang.reflect.Field;
import java.util.Date;
import java.util.Map;

import uk.ac.ed.epcc.safe.accounting.expr.PropExpressionMap;
import uk.ac.ed.epcc.safe.accounting.properties.PropertyContainer;
import uk.ac.ed.epcc.safe.accounting.properties.PropertyTag;
import uk.ac.ed.epcc.safe.accounting.reference.IndexedTag;
import uk.ac.ed.epcc.webapp.AppContext;
import uk.ac.ed.epcc.webapp.exceptions.ConsistencyError;
import uk.ac.ed.epcc.webapp.jdbc.table.BooleanFieldType;
import uk.ac.ed.epcc.webapp.jdbc.table.DateFieldType;
import uk.ac.ed.epcc.webapp.jdbc.table.DoubleFieldType;
import uk.ac.ed.epcc.webapp.jdbc.table.FieldType;
import uk.ac.ed.epcc.webapp.jdbc.table.FloatFieldType;
import uk.ac.ed.epcc.webapp.jdbc.table.IntegerFieldType;
import uk.ac.ed.epcc.webapp.jdbc.table.LongFieldType;
import uk.ac.ed.epcc.webapp.jdbc.table.ReferenceFieldType;
import uk.ac.ed.epcc.webapp.jdbc.table.StringFieldType;
import uk.ac.ed.epcc.webapp.jdbc.table.TableSpecification;
import uk.ac.ed.epcc.webapp.model.data.Duration;
/** Base class for implementing {@link PropertyContainerUpdater}
 * 
 * Note this contains the logic for building a default table specification by reflection and/or
 * querying the config service.
 * @see AutoTable
 * @see OptionalTable
 * @author spb
 *
 */
public abstract class AbstractPropertyContainerUpdater implements PropertyContainerUpdater {
public TableSpecification modifyDefaultTableSpecification(AppContext c,TableSpecification spec,PropExpressionMap map,String table_name) {
		
		Class myclass= getClass();
		// process static fields with AutoTable annotations
        for( Field f : myclass.getFields()){
        	//log.debug("consider field "+f.getName()+" "+f.getType().getCanonicalName());
        	if( PropertyTag.class.isAssignableFrom(f.getType()) ){
        		if( f.isAnnotationPresent(AutoTable.class)){

        			try {
        				PropertyTag tag = (PropertyTag) f.get(this);
        				// Allow a property to supress an AutoTable tag for a specified table
        				if( tag != null ){
        					String name = tag.getName();
        					Class target = tag.getTarget();
        					AutoTable t = f.getAnnotation(AutoTable.class);
        					if( t.target() != Object.class){
        						target = t.target();
        					}
        					final int length = t.length();
        					FieldType type= makeFieldType(c, tag, target, length);
        					if( type == null){
        						c.error("Can't resolve field type for "+name);
        					}else{
        						// Note that AccessorMap will try to 
        						// locate this property from the name of the field so 
        						// they need to match.
        						// we really want tags processed in the order
        						// of reverse priority so in the case of
        						// clashing names the field matches the highest priority match
        						if(c.getBooleanParameter("auto_tag."+table_name+"."+tag.getFullName(), true)){
        							spec.setField(name, type);
        							if( t.unique()){
        								// This only makes sense for parsers
        								unique(name);
        							}
        						}else{
        							spec.setOptionalField(name, type);
        						}
        					}
        				}
        			} catch (Exception e) {
        				throw new ConsistencyError("Error creating parser default table spec",e);
        			}
        		}else if( f.isAnnotationPresent(OptionalTable.class)){
        			try {
        				PropertyTag tag = (PropertyTag) f.get(this);
        				// Allow a property to force an AutoTable tag for a specified table
        				if( tag != null ){
        					String name = tag.getName();
        					Class target = tag.getTarget();
        					OptionalTable t = f.getAnnotation(OptionalTable.class);
        					if( t.target() != Object.class){
        						target = t.target();
        					}
        					final int length = t.length();
        					FieldType type= makeFieldType(c, tag, target, length);
        					if( type == null){
        						c.error("Can't resolve field type for "+name);
        					}else{
        						if( c.getBooleanParameter("auto_tag."+table_name+"."+tag.getFullName(), false)){
        							spec.setField(name, type);
        						}else{
        						
        							spec.setOptionalField(name, type);
        						}
        					}
        				}
        			} catch (Exception e) {
        				throw new ConsistencyError("Error creating parser default table spec",e);
        			}
        		}
        	}
        }
        // now look for fields from configuration parameters
        // this is now done in the DataBaseHandlerService but we
        // keep this in case we are specifying the table entirely in properties
        String prefix = "create_table."+table_name+".";
		Map<String,String> params = c.getInitParameters(prefix);
		spec.setFromParameters(prefix,params);
       
		if( spec.getFieldNames().size() == 0 ){
		   return null;
		}

		return spec;
	}
private FieldType makeFieldType(AppContext c, PropertyTag tag,
		Class target, final int length) {
	FieldType type;
	// Note these should default to null so that fields added dyamically
	// can be added with an augment operation. A non-null default is indistinguishable
	// from existing parsed data and can only be changed by replacing old values.
	if( target == String.class){
		type = new StringFieldType(true, null, length);
	}else if( target == Date.class){
		type = new DateFieldType(true, null);
	}else if ( target == Integer.class){
		type = new IntegerFieldType(true, null);
	}else if ( target == Long.class){
		type = new LongFieldType(true, null);
	}else if ( target == Duration.class){
		type = new IntegerFieldType(true, null);
	}else if ( target == Float.class){
		type = new FloatFieldType(true, null);
	}else if ( target == Double.class || target == Number.class){
		type = new DoubleFieldType(true, null);
	}else if( tag instanceof IndexedTag){
		type = new ReferenceFieldType(((IndexedTag) tag).getTable());
	}else if( target == Boolean.class ){
		type = new BooleanFieldType(false, false);
	}else{
		
		type=null;
	}
	return type;
}
/** extension point called when creating the table specification when a field 
 * is labelled as part of the unique key.
 * 
 * @param name  Field name
 */
	protected void unique(String name) {
	
	
	}
	public String endParse() {
		
		return "";
	}
	public PropExpressionMap getDerivedProperties(PropExpressionMap previous) {
		return previous;
	}
	
	

	public void startParse(PropertyContainer staticProps) throws Exception {
		

	}


}