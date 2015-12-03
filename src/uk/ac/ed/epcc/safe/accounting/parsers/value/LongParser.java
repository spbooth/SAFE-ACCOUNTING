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
 * Parser that parses a string into a <code>Long/code> A
 * <code>NullPointerException</code> is thrown if the string provided is
 * <code>null</code>.
 * 
 * @author jgreen4
 * 
 */

@Description("Parse a Long")
public class LongParser implements ValueParser<Long>
{
  /**
   * Useful static instance of this object to be used when one doesn't want to
   * generate lots of parsers when one will suffice
   */
  public static final LongParser PARSER = new LongParser();

	/*
	 * (non-Javadoc)
	 * @see uk.ac.ed.epcc.safe.accounting.parsers.value.ValueParser#getType()
	 */
	public Class<Long> getType() {
		return Long.class;
	}
  
  /*
   * (non-Javadoc)
   * @see
   * uk.ac.ed.epcc.safe.accounting.parsers.ValueParser#parse(java.lang.String)
   */
  public Long parse(String valueString)
    throws IllegalArgumentException,
    NullPointerException
  {
    if(valueString == null)
      throw new NullPointerException("valueString cannot be null");

    return Long.valueOf(valueString.trim());
  }

public String format(Long value) {
	return Long.toString(value);
}

}