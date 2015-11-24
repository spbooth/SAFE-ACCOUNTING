// Copyright - The University of Edinburgh 2011
package uk.ac.ed.epcc.safe.accounting.formatters.value;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

import uk.ac.ed.epcc.webapp.Targetted;

/** interface for classes that format an object into XML
 * 
 * @param <T> input type for formatter.
 * @author spb
 *
 */
public interface DomFormatter<T> extends Targetted<T>{

	/** Get the target type for this formatter.
	 * The input values must be assignable to this type.
	 * 
	 * @return Class of target
	 */
	Class<? super T> getTarget();
	
	/** format the input value as a Dom Node.
	 * @param doc document to create result in
	 * @param value
	 * @return Node or null
	 * @throws Exception 
	 */
	Node format(Document doc, T value) throws Exception;
}