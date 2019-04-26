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
package uk.ac.ed.epcc.safe.accounting.parsers.value;

import java.security.Principal;

import uk.ac.ed.epcc.webapp.Indexed;
import uk.ac.ed.epcc.webapp.exceptions.ConsistencyError;

/** This class implements fall-back formatting rules for objects.
 * This will be used by the ParameterExtension to domain objects produced in parameter forms.
 * 
 * If these parameters are to be used in a filter etc. Then the formatting needs to be compatible with
 * the parse methods of {@link ReferenceValueParser}
 * 
 * @author spb
 * @param <T> target type
 *
 */


public class DefaultFormatter<T> implements ValueParser<T> {

	private final  Class<T> target;
	public DefaultFormatter(Class<T> clazz){
		target = clazz;
		assert(clazz != null);
	}
	public Class<T> getType() {
		return target;
	}

	public T parse(String valueString) throws ValueParseException {
		throw new ValueParseException("DefaultValueParser only formats objects");
	}

	public String format(Object value) {
	
		if( value == null ){
			return null;
		}
		if( ! target.isAssignableFrom(value.getClass())){
			throw new ConsistencyError("Illegal argument to DefaultFormatter expecting "+target.getCanonicalName()+" passed "+value.toString()+":"+value.getClass().getCanonicalName());
		}
		if( value instanceof Principal){
			return ((Principal)value).getName();
		}
		if( value instanceof Indexed){
			return Integer.toString(((Indexed)value).getID());
		}
		return value.toString();
	}

}