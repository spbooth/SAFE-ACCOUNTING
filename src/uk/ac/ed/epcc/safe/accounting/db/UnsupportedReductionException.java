package uk.ac.ed.epcc.safe.accounting.db;
/** A requested reduction cannot be performed
 * 
 * @author Stephen Booth
 *
 */
public class UnsupportedReductionException extends Exception {

	public UnsupportedReductionException() {
		
	}

	public UnsupportedReductionException(String arg0) {
		super(arg0);
		
	}

	public UnsupportedReductionException(Throwable cause) {
		super(cause);
		
	}

	public UnsupportedReductionException(String message, Throwable cause) {
		super(message, cause);
		
	}

	public UnsupportedReductionException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
		
	}

}
