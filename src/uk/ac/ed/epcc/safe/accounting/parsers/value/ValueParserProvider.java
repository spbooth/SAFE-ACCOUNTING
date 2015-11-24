package uk.ac.ed.epcc.safe.accounting.parsers.value;

import uk.ac.ed.epcc.safe.accounting.properties.PropExpression;
import uk.ac.ed.epcc.webapp.AppContext;

/** Interface that can be implemented by a {@link uk.ac.ed.epcc.safe.accounting.properties.PropertyTag}
 * to allow it to provide a custom {@link ValueParser} for the property.
 * 
 * @author spb
 * @param <T> expression type
 *
 */
public interface ValueParserProvider<T> extends PropExpression<T> {
	// Note we have to allow super-type as ValueParsers are
	// type with the bare type with generics removed.
	ValueParser<? super T> getValueParser(AppContext c);
}
