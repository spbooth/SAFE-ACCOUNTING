package uk.ac.ed.epcc.safe.accounting.reports;
/** Exception to report a validation from from a
 * {@link TemplateValidator}. This should always contain a message
 * to be reported to the user.
 * 
 * @author spb
 *
 */
public class TemplateValidateException extends Exception {

	public TemplateValidateException(String arg0, Throwable arg1) {
		super(arg0, arg1);
	}

	public TemplateValidateException(String arg0) {
		super(arg0);
	}
	
}
