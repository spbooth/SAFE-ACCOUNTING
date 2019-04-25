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

import java.text.SimpleDateFormat;


/**
   * Parses a string into a <code>Date</code>. The string should be a date in
   * the format used by PBS records. That is : MM/dd/yyyy HH:mm:ss. For example:
   * 01/20/2001 21:23:52
   * 
   * @author jgreen4
   * 
   */


  public class PbsDateParser extends DateParser
  {
    /**
     * Useful static instance of this object to be used when one doesn't want to
     * generate lots of parsers when one will suffice
     */
    public static final PbsDateParser PARSER = new PbsDateParser();

    public PbsDateParser()
    {
      /**
       * The date format used by PBS records
       */
      super(new SimpleDateFormat("MM/dd/yyyy HH:mm:ss"));
    }
  }