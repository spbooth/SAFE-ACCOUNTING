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
package uk.ac.ed.epcc.safe.accounting.expr;

import java.util.Iterator;

import uk.ac.ed.epcc.safe.accounting.properties.PropertyFinder;
import uk.ac.ed.epcc.safe.accounting.properties.PropertyTag;
import uk.ac.ed.epcc.webapp.forms.inputs.*;
/** input for selecting a {@link PropertyTag} from a {@link PropertyFinder}
 * 
 * @author spb
 *
 */


public class PropertyTagInput extends CodeListInput<PropertyTag> {
   private PropertyFinder finder;
   public PropertyTagInput(PropertyFinder finder){
	   this.finder=finder;
   }
public PropertyTag getItemByTag(String value) {
	if( value == null ){
		return null;
	}
	return finder.find(value);
}
public String getTagByItem(PropertyTag item) {
	return item.getFullName();
}
public Iterator<PropertyTag> getItems() {
	return finder.getProperties().iterator();
}
public int getCount(){
	return finder.getProperties().size();
}


public String getText(PropertyTag item){
	if( item != null ){
		return item.getFullName()+": "+item.getDescription();
	}
	return null;
}


@Override
public boolean isValid(PropertyTag item) {
	return finder.hasProperty(item);
}
}