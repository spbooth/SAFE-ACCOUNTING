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
package uk.ac.ed.epcc.safe.accounting;

import java.util.Iterator;

import uk.ac.ed.epcc.safe.accounting.expr.ExpressionTarget;
import uk.ac.ed.epcc.safe.accounting.expr.ExpressionTargetContainer;
import uk.ac.ed.epcc.safe.accounting.properties.PropExpression;
import uk.ac.ed.epcc.safe.accounting.properties.PropertyTag;
import uk.ac.ed.epcc.safe.accounting.selector.PropertyTargetGenerator;
import uk.ac.ed.epcc.safe.accounting.selector.RecordSelector;
import uk.ac.ed.epcc.webapp.model.data.CloseableIterator;
/** An {@link PropertyTargetGenerator} where the target is an {@link ExpressionTarget}
 * 
 * @author spb
 *
 * @param <T> type of target
 */
public interface ExpressionTargetGenerator<T> extends PropertyTargetGenerator<T> {
	/** Check if the expression is compatible with this class.
	 * A false value indicates no record can ever evaluate the expression. For example if it includes
	 * properties that are not generated by the class.
	 * 
	 * For a {@link PropertyTag} this is equivalent to {@link #hasProperty(PropertyTag)}
	 * 
	 * @param expr
	 * @return boolean
	 */
	public  <I> boolean compatible(PropExpression<I> expr);
	
	/** Get an {@link ExpressionTarget} for the target object.
	    * This could be the record itself or a wrapper object.
	    * If passed null it should return null.
	    * 
	    * @param record
	    * @return
	    */
	   public ExpressionTargetContainer getExpressionTarget(T record);
	   
	  
	   
	   /** Is this a record from this {@link ExpressionTargetGenerator}.
	    * 
	    * @param record
	    * @return
	    */
	   public boolean isMyTarget(T record);
	   /** get an iterator over the {@link ExpressionTargetContainer}s
	    * 
	    * @param sel
	    * @return
	    * @throws Exception
	    */
	   public CloseableIterator<ExpressionTargetContainer> getExpressionIterator(RecordSelector sel) throws Exception;
	   
	   /** find an {@link ExpressionTargetContainer} that matches the selector
	    * 
	    * @param sel
	    * @return
	    * @throws Exception
	    */
	   public default ExpressionTargetContainer findExpression(RecordSelector sel) throws Exception{
		   try(CloseableIterator<ExpressionTargetContainer> it = getExpressionIterator(sel)){
			   if( it.hasNext()) {
				   return it.next();
			   }
			   return null;
		   }
	   }
}