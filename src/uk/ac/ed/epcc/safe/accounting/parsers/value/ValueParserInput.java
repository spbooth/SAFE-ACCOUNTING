// Copyright - The University of Edinburgh 2011
package uk.ac.ed.epcc.safe.accounting.parsers.value;

import uk.ac.ed.epcc.webapp.forms.exceptions.ParseException;
import uk.ac.ed.epcc.webapp.forms.inputs.ParseAbstractInput;
/** Adapter to convert ValueParser to Input
 * 
 * @author spb
 *
 * @param <T>
 */
@uk.ac.ed.epcc.webapp.Version("$Id: ValueParserInput.java,v 1.4 2014/09/15 14:32:26 spb Exp $")

public class ValueParserInput<T> extends ParseAbstractInput<T>  {

	private ValueParser<T> parser;
	public ValueParserInput(ValueParser<T> parser){
		this.parser=parser;
	}
	
	public void parse(String v) throws ParseException {
		if(v != null && v.length() > 0){
			try {
				setValue(parser.parse(v));
			} catch (Exception e) {
				throw new ParseException("Bad format", e);
			}
		}else{
			setValue(null);
		}
	}

	public String getString(T val){
		return parser.format(val);
	}
}