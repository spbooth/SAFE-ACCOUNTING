//| Copyright - The University of Edinburgh 2015                            |
//|                                                                         |
//| Licensed under the Apache License, Version 2.0 (the "License");         |
//| you may not use this file except in compliance with the License.        |
//| You may obtain a copy of the License at                                 |
//|                                                                         |
//|    http://www.apache.org/licenses/LICENSE-2.0                           |
//|                                                                         |
//| Unless required by applicable law or agreed to in writing, software     |
//| distributed under the License is distributed on an "AS IS" BASIS,       |
//| WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.|
//| See the License for the specific language governing permissions and     |
//| limitations under the License.                                          |
package uk.ac.ed.epcc.safe.accounting.parsers;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import uk.ac.ed.epcc.safe.accounting.ErrorSet;
import uk.ac.ed.epcc.safe.accounting.properties.PropertyContainer;
import uk.ac.ed.epcc.safe.accounting.properties.PropertyMap;
import uk.ac.ed.epcc.safe.accounting.update.AccountingParseException;
import uk.ac.ed.epcc.safe.accounting.update.BatchParser;
import uk.ac.ed.epcc.safe.accounting.update.PropertyContainerParser;
import uk.ac.ed.epcc.webapp.AppContext;
import uk.ac.ed.epcc.webapp.Contexed;
import uk.ac.ed.epcc.webapp.logging.Logger;
import uk.ac.ed.epcc.webapp.logging.LoggerService;
/** A generic parser that accepts lines containing name=value pairs on
 * 
 * This is essentially similar to the attribute parse of a PBS record but without
 * the mandatory fields.
 * @see AbstractPbsParser
 * @author spb
 *
 */
public abstract class AbstractKeyPairParser extends BatchParser implements Contexed{
	private final AppContext conn;
	
	/**
	 * Errors reported in this <code>ErrorSet</code> will be reported as
	 * <em>info</em> entries in the logger.
	 */
	private ErrorSet info;
	
	/**
	 * Errors reported in this <code>ErrorSet</code> will be reported as
	 * <em>warning</em> entries in the logger.
	 */
	private ErrorSet warnings;
	
	/**
	 * Errors reported in this <code>ErrorSet</code> will be returned by the
	 * {@link #endParse()} method
	 */
	private ErrorSet errors;
	
	private boolean errors_fatal=true;

	static final Pattern ATTR_PATTERN=Pattern.compile("(\\w+)=(\\S+|(?:\"[^\"]+\"))(\\s|$)");
	
	public AbstractKeyPairParser(AppContext conn) {
		this.conn=conn;
	}

	
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * uk.ac.ed.epcc.safe.accounting.BaseParser#startParse(uk.ac.ed.epcc.safe.
	 * accounting.PropertyContainer)
	 */
	@Override
	public void startParse(PropertyContainer defaults) {
		this.errors = new ErrorSet();
		this.info = new ErrorSet();
		this.warnings = new ErrorSet();
		
	}
	
	public boolean parse(PropertyMap map, String record)
			throws AccountingParseException {
		// split the record into keyword/value pairs
		// fetch a ContainerEntryMaker from the keyword.
		// use this to parse the value into the map.
		Matcher m = ATTR_PATTERN.matcher(record);
	    while(m.find()){
	    	String attrName=m.group(1);
	    	String attrValue=m.group(2);
	    	if( attrValue.startsWith("\"")){
	    		attrValue=attrValue.substring(1,attrValue.length()-1);
	    	}
	    	ContainerEntryMaker maker = getEntryMaker(attrName);
			if (maker == null) {
				String errorMessage = "unrecognised attribute '" + attrName + "'";
				this.warnings.add(errorMessage, record);
			} else {
				try {
					maker.setValue(map, attrValue);
				} catch (IllegalArgumentException e) {
					if( errors_fatal ){
						throw e;
					}
					this.errors.add("Problem with attribute '" + attrValue
							+ "': Unable to parse value '" + attrValue + "'", record, e);
				}
			}
	    }
		
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see uk.ac.ed.epcc.safe.accounting.BaseParser#endParse()
	 */
	@Override
	public String endParse() {
		LoggerService loggerService = this.conn.getService(LoggerService.class);
		Logger logger = loggerService.getLogger(AbstractPbsParser.class);

		/*
		 * Info and warnings are sent to the logger. Errors are returned - they are
		 * more important and so are passed back to be shown directly to the caller
		 * of the parse
		 */
		String info = this.info.toString();
		if (info.length() > 0)
			logger.info(info);

		String warnings = this.warnings.toString();
		if (warnings.length() > 0)
			logger.warn(warnings);

		return this.errors.toString();
	}
	
	public AppContext getContext() {
		return conn;
	}
	protected final Logger getLogger(){
		return conn.getService(LoggerService.class).getLogger(getClass());
	}

	/** get the {@link ContainerEntryMaker} for the attribute name.
	 * 
	 * @param attr
	 * @return
	 */
	protected abstract ContainerEntryMaker getEntryMaker(String attr);


	/**
	 * @return the errors_fatal
	 */
	private boolean areErrorsFate() {
		return errors_fatal;
	}


	/**
	 * @param errors_fatal the errors_fatal to set
	 */
	private void setErrorsFatal(boolean errors_fatal) {
		this.errors_fatal = errors_fatal;
	}
}