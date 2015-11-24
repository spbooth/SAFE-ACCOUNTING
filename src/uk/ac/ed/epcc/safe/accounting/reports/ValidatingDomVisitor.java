package uk.ac.ed.epcc.safe.accounting.reports;

import java.util.LinkedList;

import org.w3c.dom.Element;

import uk.ac.ed.epcc.webapp.editors.xml.AbstractDomVisitor;
import uk.ac.ed.epcc.webapp.editors.xml.DomVisitor;
/** A {@link DomVisitor} that applies a {@link TemplateValidator} to each element.
 * 
 * @author spb
 *
 */
public class ValidatingDomVisitor extends AbstractDomVisitor {
	public ValidatingDomVisitor(TemplateValidator val) {
		super();
		this.val = val;
	}

	private final TemplateValidator val;

	@Override
	public boolean beginStartElement(Element e, LinkedList<String> path)
			throws Exception {
		
		return ! val.checkNode(e);
		
	}
}
