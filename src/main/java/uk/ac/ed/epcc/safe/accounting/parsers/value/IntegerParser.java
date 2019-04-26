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
 * Parser that parses a string into an <code>Integer</code> A
 * <code>NullPointerException</code> is thrown if the string provided is
 * <code>null</code>.
 * 
 * @author jgreen4
 * 
 */

@Description("Parse an Integer")
public class IntegerParser implements ValueParser<Integer>
{
  /**
   * Useful static instance of this object to be used when one doesn't want to
   * generate lots of parsers when one will suffice
   */
  public static final IntegerParser PARSER = new IntegerParser();

	/*
	 * (non-Javadoc)
	 * @see uk.ac.ed.epcc.safe.accounting.parsers.value.ValueParser#getType()
	 */
	public Class<Integer> getType() {
		return Integer.class;
	}
  
  /*
   * (non-Javadoc)
   * @see
   * uk.ac.ed.epcc.safe.accounting.parsers.ValueParser#parse(java.lang.String)
   */
  public Integer parse(String valueString)
    throws IllegalArgumentException,
    NullPointerException
  {
    if(valueString == null)
      throw new NullPointerException("valueString cannot be null");

  
    return Integer.valueOf(valueString.trim());
 
  }

public String format(Integer value) {
	return Integer.toString(value);
}

}