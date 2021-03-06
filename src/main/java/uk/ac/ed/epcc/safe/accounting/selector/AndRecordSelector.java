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
package uk.ac.ed.epcc.safe.accounting.selector;


/** A class that encodes a selection expression for a set of UsageRecords.
 * 
 * Essentially this is an ordered set of SelectClause objects. We use an ordered set so that
 * different instances of the same selector don't randomly permute the order of the clauses in SQL 
 * statements improving the chance of query caching working.
 * 
 * @author spb
 *
 */


public class AndRecordSelector extends CombiningRecordSelector {
	
	
	public AndRecordSelector() {
		super("and");
	}

	public AndRecordSelector(RecordSelector sel){
		super("and" ,sel);
	}

	public <R> R visit(SelectorVisitor<R> visitor) throws Exception {
		if( visitor.lockOnVisit()) {
			lock();
		}
		return visitor.visitAndRecordSelector(this);
	}

	public AndRecordSelector copy() {
		if( isLocked()){
			return this;
		}
		AndRecordSelector copy = new AndRecordSelector(this);
		copy.lock();
		return copy;
	}

	


	

	
}