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

import uk.ac.ed.epcc.safe.accounting.properties.InvalidPropertyException;
import uk.ac.ed.epcc.safe.accounting.properties.PropertyFinder;
import uk.ac.ed.epcc.webapp.AppContext;
import uk.ac.ed.epcc.webapp.forms.exceptions.FieldException;
import uk.ac.ed.epcc.webapp.forms.exceptions.ValidateException;
import uk.ac.ed.epcc.webapp.forms.inputs.TextInput;
import uk.ac.ed.epcc.webapp.logging.Logger;
import uk.ac.ed.epcc.webapp.logging.LoggerService;
/** Input for propExpressions
 * 
 * @author spb
 *
 */


public class PropExpressionInput extends TextInput {
  private final Parser parser;
  public PropExpressionInput(AppContext c, PropertyFinder finder){
	  parser = new Parser(c, finder);
	  setMaxResultLength(256);
	  setSingle(true);
  }
/* (non-Javadoc)
 * @see uk.ac.ed.epcc.webapp.model.data.forms.TextInput#validate(boolean)
 */
@Override
public void validate() throws FieldException {
	super.validate();
	String value = getValue();
	if( value != null ){
		try {
			parser.parse(value);
		} catch (ParseException e) {
			getLogger().error("Error parsing prop expression",e);
			throw new ValidateException(e.getMessage());
		} catch (InvalidPropertyException e) {
			getLogger().error("Error parsing prop expression",e);
			throw new ValidateException(e.getMessage());
		}
	}
}
/**
 * @return
 */
protected Logger getLogger() {
	return parser.getContext().getService(LoggerService.class).getLogger(getClass());
}
}