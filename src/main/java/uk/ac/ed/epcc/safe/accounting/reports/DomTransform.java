package uk.ac.ed.epcc.safe.accounting.reports;

import javax.xml.parsers.DocumentBuilder;

import org.w3c.dom.Document;
/** A java level document transformation including content expansion.
 * 
 * @author Stephen Booth
 *
 */
public interface DomTransform {

	/** Transform the source document creating
	 * 
	 * @param builder {@link DocumentBuilder} builder for creating new document
	 * @param source Input {@link Document}
	 * @return transformed {@link Document}
	 */
	Document transform(DocumentBuilder builder,Document source);

}