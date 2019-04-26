//| Copyright - The University of Edinburgh 2015                            |
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
package uk.ac.ed.epcc.safe.accounting.expr;

import uk.ac.ed.epcc.safe.accounting.properties.PropExpression;

/** Exception thrown if an expression is unsupported
 * 
 * @author spb
 *
 */
public class UnsupportedExpressionException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4445691984890652620L;
	PropExpression e;
	public UnsupportedExpressionException(PropExpression e) {
		super("Unsupported expression "+e.toString());
		this.e=e;
	}

	public PropExpression geExpression(){
		return e;
	}
	

}