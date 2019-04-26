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

import uk.ac.ed.epcc.webapp.Description;


/**
 * The most simple of all the <code>ValueParser</code>s. This parser returns the
 * string that was given to it. However, it will throw a
 * <code>NullPointerException</code> if the string is <code>null</code>.
 * 
 * @author jgreen4
 * 
 */

@Description("Parse a string")
public class StringParser implements ValueParser<String>
{
	/*
	 * (non-Javadoc)
	 * @see uk.ac.ed.epcc.safe.accounting.parsers.value.ValueParser#getType()
	 */
	public Class<String> getType() {
		return String.class;
	}
	
  /**
   * Useful static instance of this object to be used when one doesn't want to
   * generate lots of parsers when one will suffice
   */
  public static final StringParser PARSER = new StringParser();

  /*
   * (non-Javadoc)
   * @see
   * uk.ac.ed.epcc.safe.accounting.parsers.ValueParser#parse(java.lang.String)
   */
  public String parse(String valueString)
    throws IllegalArgumentException,
    NullPointerException
  {
    if(valueString == null)
      throw new NullPointerException("valueString cannot be null");

    return valueString;
  }

public String format(String value) {
	return value;
}
}