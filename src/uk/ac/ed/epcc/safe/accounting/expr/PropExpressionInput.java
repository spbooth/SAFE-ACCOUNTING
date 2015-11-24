// Copyright - The University of Edinburgh 2011
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
/** Input for propExpressions
 * 
 * @author spb
 *
 */
@uk.ac.ed.epcc.webapp.Version("$Id: PropExpressionInput.java,v 1.12 2014/09/15 14:32:22 spb Exp $")

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
			parser.getContext().error(e,"Error parsing prop expression");
			throw new ValidateException(e.getMessage());
		} catch (InvalidPropertyException e) {
			parser.getContext().error(e,"Error parsing prop expression");
			throw new ValidateException(e.getMessage());
		}
	}
}
}