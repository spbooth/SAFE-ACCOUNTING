// Copyright - The University of Edinburgh 2011
/*******************************************************************************
 * Copyright (c) - The University of Edinburgh 2010
 *******************************************************************************/
package uk.ac.ed.epcc.safe.accounting.upload;

import java.util.Map;

import uk.ac.ed.epcc.safe.accounting.db.AccountingUpdater;
import uk.ac.ed.epcc.safe.accounting.db.UsageRecordParseTarget;
import uk.ac.ed.epcc.safe.accounting.parsers.value.ValueParser;
import uk.ac.ed.epcc.safe.accounting.parsers.value.ValueParserPolicy;
import uk.ac.ed.epcc.safe.accounting.properties.PropertyFinder;
import uk.ac.ed.epcc.safe.accounting.properties.PropertyMap;
import uk.ac.ed.epcc.safe.accounting.properties.PropertyTag;
import uk.ac.ed.epcc.webapp.AppContext;
import uk.ac.ed.epcc.webapp.Contexed;
/** Class to upload an accounting table.
 * Upload parameters are passed as a map.
 * <ul>
 * <li> table - class tag to update</li>
 * <li> update - update data</i>
 * <li> replace - should new data replace old</li>
 * </ul>
 * Other parameters can be used to set default property tags.
 * When the property is a ParseTag the parse method will be used to
 * convert String parameter values.
 * 
 * @author spb
 *
 */
@uk.ac.ed.epcc.webapp.Version("$Id: AccountingUploadParser.java,v 1.12 2014/09/15 14:32:30 spb Exp $")

public class AccountingUploadParser implements UploadParser, Contexed {
	private AppContext conn;
	public AccountingUploadParser(AppContext c){
		this.conn=c;
	}
	@SuppressWarnings("unchecked")
	public String upload(Map<String, Object> parameters) throws UploadException {

		
        String table = (String) parameters.get("table");
        String update = (String) parameters.get("update");
       
        PropertyMap defaults = new PropertyMap();       
        
        if( update == null ){
        	throw new UploadException("No upload text");
        }
        if( table == null ){
        	throw new UploadException("No destination table specified");
        }
		
        UsageRecordParseTarget fac = conn.makeObject(UsageRecordParseTarget.class, table);
        if( fac == null ){
        	throw new UploadException("Table "+table+" does not have an accounting table configured");
        }
        PropertyFinder finder = fac.getFinder();
        ValueParserPolicy vis = new ValueParserPolicy(getContext());
        for(String s : parameters.keySet()){
        	PropertyTag tag = finder.find(s);
        	if( tag != null){
        		Object val = parameters.get(s);
        		if( val instanceof String){
        			try {
						val = ((ValueParser)tag.accept(vis)).parse((String) val);
					} catch (Exception e) {
						conn.error(e,"Error parsing fixed parameter "+tag.getName());
					}
        		}
        		defaults.setProperty(tag, val);
        	}
        	
        }
        
        boolean replace = (parameters.get("replace") != null);
        boolean augment = (parameters.get("augment") != null);
        boolean verify = (parameters.get("verify") != null);
        String result = new AccountingUpdater(conn,defaults,fac).receiveAccountingData( update, replace,verify,augment);
		
		return result;
	}

	public AppContext getContext() {
		return conn;
	}

}