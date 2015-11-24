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
