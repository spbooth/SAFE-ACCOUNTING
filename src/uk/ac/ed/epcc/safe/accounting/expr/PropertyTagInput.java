// Copyright - The University of Edinburgh 2011
/*******************************************************************************
 * Copyright (c) - The University of Edinburgh 2010
 *******************************************************************************/
package uk.ac.ed.epcc.safe.accounting.expr;

import java.util.Iterator;

import uk.ac.ed.epcc.safe.accounting.properties.PropertyFinder;
import uk.ac.ed.epcc.safe.accounting.properties.PropertyTag;
import uk.ac.ed.epcc.webapp.forms.inputs.InputVisitor;
import uk.ac.ed.epcc.webapp.forms.inputs.ListInput;
import uk.ac.ed.epcc.webapp.forms.inputs.TextInput;
/** input for selecting a {@link PropertyTag} from a {@link PropertyFinder}
 * 
 * @author spb
 *
 */
@uk.ac.ed.epcc.webapp.Version("$Id: PropertyTagInput.java,v 1.9 2014/09/15 14:32:22 spb Exp $")

public class PropertyTagInput extends TextInput implements
		ListInput<String, PropertyTag> {
   private PropertyFinder finder;
   public PropertyTagInput(PropertyFinder finder){
	   this.finder=finder;
   }
public PropertyTag getItembyValue(String value) {
	if( value == null ){
		return null;
	}
	return finder.find(value);
}
public Iterator<PropertyTag> getItems() {
	return finder.getProperties().iterator();
}
public int getCount(){
	return finder.getProperties().size();
}
public String getTagByItem(PropertyTag item) {
	return item.getFullName();
}
public String getTagByValue(String value) {
	return value;
}
public String getText(PropertyTag item){
	if( item != null ){
		return item.getName()+": "+item.getDescription();
	}
	return null;
}
public PropertyTag getItem() {
	return getItembyValue(getValue());
}
public void setItem(PropertyTag item) {
	if( item == null ){
		setValue(null);
	}else{
		setValue(item.getFullName());
	}
	
}
@Override
public <R> R accept(InputVisitor<R> vis) throws Exception {
	return vis.visitListInput(this);
}
}