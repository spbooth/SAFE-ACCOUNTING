package uk.ac.ed.epcc.safe.accounting.reports;

import org.w3c.dom.Document;
/** A java level document transformation including content expansion.
 * 
 * @author Stephen Booth
 *
 */
public interface DomTransform {

	void transform(Document source, Document destination);

}