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

import uk.ac.ed.epcc.safe.accounting.db.MatchSelectVisitor;
import uk.ac.ed.epcc.safe.accounting.properties.PropertyTag;
import uk.ac.ed.epcc.safe.accounting.properties.PropertyTarget;
import uk.ac.ed.epcc.safe.accounting.selector.RecordSelector;
import uk.ac.ed.epcc.webapp.AppContext;
import uk.ac.ed.epcc.webapp.jdbc.expr.CannotFilterException;

/** visitor to evaluate an expression on a simple {@link PropertyTarget}
 * 
 * @author spb
 *
 */
public class PropertyTargetEvaluatePropExpressionVisitor extends
		EvaluatePropExpressionVisitor  {

	public PropertyTargetEvaluatePropExpressionVisitor(AppContext ctx, PropertyTarget target) {
		super(ctx);
		this.target=target;
	}

	private final PropertyTarget target;
	
	public Object visitPropertyTag(PropertyTag<?> tag) throws Exception {
		return target.getProperty(tag,null);
	}

	@Override
	protected boolean matches(RecordSelector sel) throws Exception {
		if( target instanceof ExpressionTarget){
			MatchSelectVisitor<ExpressionTarget> vis = new MatchSelectVisitor<ExpressionTarget>((ExpressionTarget)target);
			return sel.visit(vis).booleanValue();
		}
		throw new CannotFilterException("Cannot apply RecordSelector to PropertyTarget");
	}


	
}