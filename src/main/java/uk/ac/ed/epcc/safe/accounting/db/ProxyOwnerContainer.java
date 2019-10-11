package uk.ac.ed.epcc.safe.accounting.db;

import java.util.Set;

import uk.ac.ed.epcc.safe.accounting.expr.ExpressionTargetContainer;
import uk.ac.ed.epcc.safe.accounting.expr.Parser;
import uk.ac.ed.epcc.safe.accounting.properties.InvalidExpressionException;
import uk.ac.ed.epcc.safe.accounting.properties.InvalidPropertyException;
import uk.ac.ed.epcc.safe.accounting.properties.PropExpression;
import uk.ac.ed.epcc.safe.accounting.properties.PropertyContainer;
import uk.ac.ed.epcc.safe.accounting.properties.PropertyTag;

/** Interface that extends {@link ProxyOwner} to implement {@link ExpressionTargetContainer}
 * with default methods to forward onto the nested proxy.
 * 
 * @author Stephen Booth
 *
 */
public interface ProxyOwnerContainer extends ProxyOwner, ExpressionTargetContainer {

	@Override
	default boolean supports(PropertyTag<?> tag) {
		return getProxy().supports(tag);
	}

	@Override
	default boolean writable(PropertyTag<?> tag) {
		return getProxy().writable(tag);
	}

	@Override
	default <T> T getProperty(PropertyTag<T> key) throws InvalidExpressionException {
		return getProxy().getProperty(key);
	}

	@Override
	default <T> void setProperty(PropertyTag<? super T> key, T value) throws InvalidPropertyException {
		getProxy().setProperty(key, value);
	}

	@Override
	default <T> void setOptionalProperty(PropertyTag<? super T> key, T value) {
		getProxy().setOptionalProperty(key, value);
	}

	@Override
	default Set<PropertyTag> getDefinedProperties() {
		return getProxy().getDefinedProperties();
	}

	@Override
	default void setAll(PropertyContainer source) {
		getProxy().setAll(source);
		
	}

	

	@Override
	default Parser getParser() {
		return getProxy().getParser();
	}

	@Override
	default <T> T evaluateExpression(PropExpression<T> expr) throws InvalidExpressionException {
		return getProxy().evaluateExpression(expr);
	}

	@Override
	default <T> T evaluateExpression(PropExpression<T> expr, T def) {
		return getProxy().evaluateExpression(expr, def);
	}

	@Override
	default <T> T getProperty(PropertyTag<T> tag, T def) {
		return getProxy().getProperty(tag, def);
	}

}
