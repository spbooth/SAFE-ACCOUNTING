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
package uk.ac.ed.epcc.safe.accounting;


import java.util.Date;

import uk.ac.ed.epcc.safe.accounting.db.GeneratorReductionHandler;
import uk.ac.ed.epcc.safe.accounting.db.ReductionHandler;
import uk.ac.ed.epcc.safe.accounting.expr.ExpressionTargetContainer;
import uk.ac.ed.epcc.safe.accounting.properties.PropExpression;
import uk.ac.ed.epcc.safe.accounting.selector.RecordSelector;
import uk.ac.ed.epcc.webapp.Tagged;

/** Interface implemented by classes that can provide Usage information to the reports.
 * 
 * This interface is intended to be implementable by composite objects that combine multiple tables.
 * 
 * @see ReductionHandler
 * @see GeneratorReductionHandler
 * @author spb
 * @param <UR> 
 *
 */
public interface UsageProducer<UR extends ExpressionTargetContainer> extends 
ExpressionTargetGenerator<UR>, 
Tagged, 
ReductionProducer<UR>{	    
   

}