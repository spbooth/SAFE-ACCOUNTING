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
package uk.ac.ed.epcc.safe.accounting.properties;
/** Visitor that can only handle single property values.
 * 
 * This interface is extended for visitors that handle a wider set of
 * {@link PropExpression}s.
 * 
 * * All {@link PropExpression}s have to implement an accept method for this interface 
 * so we can't add new PropExpression types without adding it to some sub-interface or 
 * extending an existing type of PropExpression. 
 * Code that we want to to be updated when new types of expression are added should implement
 * the sub-interface so that this requirement is explicit. This base-interface only exists so that
 * the {@link PropertyTag} can count as a {@link PropExpression} while still allowing this package to
 * operate without the rest of the expression code.
 * @author spb
 *
 * @param <R>
 */
public interface BasePropExpressionVisitor<R>  {
	  public R visitPropertyTag(PropertyTag<?> tag) throws Exception;
}