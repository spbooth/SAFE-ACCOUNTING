package uk.ac.ed.epcc.safe.accounting.reports;

import org.w3c.dom.Element;

/** Interface for classes that validate an unprocessed template 
 * (In DOM form)
 * The aim is to add additional validation beyond what can
 * be achieved via schema validation alone. For example 
 * to check the syntax of  PropExpressions. The checks need to be conservative 
 * to not flag problems due to unexpanded elements etc.
 * The validator is allowed to deference up into the surrounding document to find context.
 * 
 * 
 * 
 * @author spb
 *
 */
public interface TemplateValidator {
	/** Consider this element for validity.
	 * If the method returns true the node has been checked and need not be recursed into.
	 * If it returns false then either the Validator does not recognise the node or validates at a lower
	 * level of the document tree. In this case the surrounding framework should recurse into the child elements.
	 * 
	 * 
	 * @param e Element to be considered
	 * @return boolean status.
	 * @throws TemplateValidateException
	 */
	public boolean checkNode(Element e) throws TemplateValidateException;
}
