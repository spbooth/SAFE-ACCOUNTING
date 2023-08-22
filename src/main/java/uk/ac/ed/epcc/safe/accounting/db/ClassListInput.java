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
/**
 * 
 */
package uk.ac.ed.epcc.safe.accounting.db;

import java.util.Iterator;
import java.util.Map;

import uk.ac.ed.epcc.webapp.forms.exceptions.*;
import uk.ac.ed.epcc.webapp.forms.inputs.*;
import uk.ac.ed.epcc.webapp.validation.FieldValidator;

/** An input to select a class from a pre-defined set.
 * 
 * @author spb
 *
 */



public class ClassListInput extends AbstractStringInput implements ListInput<String, Class>{
    private final Map<String,Class> map;
    public ClassListInput(Map<String,Class> m){
    	this.map=m;
    	addValidator(new FieldValidator<String>() {
			
			@Override
			public void validate(String data) throws FieldException {
				if( ! map.containsKey(data)) {
					throw new ValidateException("Illegal value "+data);
				}
				
			}
		});
    }
	public Class getItembyValue(String value) {
		return map.get(value);
	}

	public Iterator<Class> getItems() {
		return map.values().iterator();
	}
	public int getCount(){
		return map.size();
	}

	public String getTagByItem(Class item) {
		if( item == null ){
			return null;
		}
		for(String tag : map.keySet()){
		   if( map.get(tag) == item){
			   return tag;
		   }
		}
		return null;
	}

	public String getTagByValue(String value) {
		return value;
	}

	public String getText(Class item) {
		return getTagByItem(item);
	}

	public String convert(Object v)  {
          if( v == null ){
        	  return null;
          }
		return v.toString();
	}

	

	public String getPrettyString(String value) {
		return value;
	}

	public String getString(String value) {
		return value;
	}

	

	
	
	public Class getItem() {
		return getItembyValue(getValue());
	}

	public void setItem(Class item) {
		try {
			setValue(getTagByItem(item));
		} catch (TypeException e) {
			throw new TypeError(e);
		}
	}

	public <R> R accept(InputVisitor<R> vis) throws Exception {
		return vis.visitListInput(this);
	}

	@Override
	public boolean isValid(Class item) {
		return map.values().contains(item);
	}
	
}