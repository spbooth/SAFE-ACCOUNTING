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

import java.util.List;

import uk.ac.ed.epcc.webapp.AppContext;
/** Class to build a table of per-job information.
 * 
 * @author spb
 * @param <UR> Type of usage record
 *
 */


public class JobTableMaker<UR> extends ExpressionTargetTableMaker<UR, UsageProducer<UR>>{
	
	public JobTableMaker(AppContext c,UsageProducer up){
		super(c,up);
	}
	public JobTableMaker(AppContext c,UsageProducer up, List<ColName> props){
	    this(c,up);
		for(ColName col : props){
			addColumn(col);
		}
	}
	
	
	
	@Override
	protected Object makeKey(UR t) {
		return t;
	}
    
}