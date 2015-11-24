// Copyright - The University of Edinburgh 2011
/*******************************************************************************
 * Copyright (c) - The University of Edinburgh 2010
 *******************************************************************************/
package uk.ac.ed.epcc.safe.accounting.db;

import java.util.Iterator;
import java.util.Map;

import uk.ac.ed.epcc.safe.accounting.ErrorSet;
import uk.ac.ed.epcc.safe.accounting.expr.DerivedPropertyMap;
import uk.ac.ed.epcc.safe.accounting.properties.PropertyContainer;
import uk.ac.ed.epcc.safe.accounting.properties.PropertyMap;
import uk.ac.ed.epcc.safe.accounting.update.AccountingParseException;
import uk.ac.ed.epcc.safe.accounting.update.SkipRecord;
import uk.ac.ed.epcc.webapp.AppContext;
import uk.ac.ed.epcc.webapp.model.data.DataObject;
/** Parses an external source of classification data.
 * 
 * Classification data can also be generated on the fly as part of the accounting parse process
 * but this is needed if there is additional data than needs to be associated with the classification 
 * objects that is not available during the parse.
 * 
 * @author spb
 *
 * @param <T>
 */

@uk.ac.ed.epcc.webapp.Version("$Id: ClassificationUpdater.java,v 1.15 2014/09/15 14:32:19 spb Exp $")


public class ClassificationUpdater<T extends DataObject & PropertyContainer> {
	private final AppContext conn;
	private final ClassificationParseTarget<T> target;
	// These defined as object attributes to allow unit tests
	int n_lines=0;
	int skip=0;
	int updates=0;
    public ClassificationUpdater(AppContext c, ClassificationParseTarget<T> target){
    	this.conn=c;
    	this.target=target;
    }
    public String receiveData(Map<String,Object> params, String update){
    	ErrorSet errors = new ErrorSet();
    	ErrorSet skip_list = new ErrorSet();
    	return receiveData(params, update, errors, skip_list);
    }
    public int getLineCount(){
    	return n_lines;
    }
    public int getSkipCount(){
    	return skip;
    }
    public int getUpdateCount(){
    	return updates;
    }
	public String receiveData(Map<String, Object> params, String update,
			ErrorSet errors, ErrorSet skip_list) {
		PropertyMap meta_data = target.getGlobals(params);
    	
    	
    	
    	n_lines=0;
    	skip=0;
    	updates=0;
		Iterator<String> lines;
    	
    
    	try{
    		lines = target.splitRecords(update);
    		
    		target.startParse(meta_data);
    		
    	
    	}catch(Exception e){
    		errors.add("Initialisation error", update, e);
    		conn.error(e,"Error initialising parse");
    		return "Error initialising parse";
    	}
    	while (lines.hasNext()) {
    		n_lines++;
    		String current_line =lines.next();
    		try{
    			DerivedPropertyMap map = new DerivedPropertyMap(conn);
    			if( meta_data != null ){
    				meta_data.setContainer(map);
    			}
    			if( target.parse(map, current_line) ){
    			    T record = target.make(map);
    			   
    			    if( record == null ){
    			    	throw new AccountingParseException("No record made");
    			    }
    			    map.setContainer(record);
    			    if( record.commit()){
    			    	updates++;
    			    }
    			}
    		}catch (SkipRecord s){
    			skip_list.add(s.getMessage(),current_line);
    			skip++;
    		}catch(AccountingParseException pe){
    			errors.add(pe.getMessage(), current_line);
    		}catch(Exception e){
    			errors.add("Unexpected parse error",current_line);
    			conn.error(e,"Unexpected Error parsing line "+current_line);
    		}
    		
    	}
    	StringBuilder error_text= target.endParse();
    	error_text.append(errors.toString());
    	StringBuilder sb = new StringBuilder();
    	sb.append("Total lines: ");
    	sb.append(n_lines);
    	sb.append('\n');
    	sb.append("Updated lines: ");
    	sb.append(updates);
    	sb.append('\n');
    	sb.append("Skipped lines: ");
    	sb.append(skip);
    	sb.append('\n');
    	
    	sb.append(skip_list.details(0));
    	if( error_text.length() > 0 ){
    		sb.append(error_text);
    		conn.error("Error in parse\n"+error_text.toString());
    	}
    	return sb.toString();
	}
}