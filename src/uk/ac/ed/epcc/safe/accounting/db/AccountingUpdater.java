// Copyright - The University of Edinburgh 2011
/*******************************************************************************
 * Copyright (c) - The University of Edinburgh 2010
 *******************************************************************************/
package uk.ac.ed.epcc.safe.accounting.db;

import java.util.Date;
import java.util.Iterator;

import uk.ac.ed.epcc.safe.accounting.ErrorSet;
import uk.ac.ed.epcc.safe.accounting.expr.DerivedPropertyMap;
import uk.ac.ed.epcc.safe.accounting.expr.PropExpressionMap;
import uk.ac.ed.epcc.safe.accounting.properties.PropertyMap;
import uk.ac.ed.epcc.safe.accounting.properties.PropertyTag;
import uk.ac.ed.epcc.safe.accounting.properties.StandardProperties;
import uk.ac.ed.epcc.safe.accounting.update.AccountingParseException;
import uk.ac.ed.epcc.safe.accounting.update.SkipRecord;
import uk.ac.ed.epcc.webapp.AppContext;
import uk.ac.ed.epcc.webapp.Feature;
import uk.ac.ed.epcc.webapp.logging.Logger;
import uk.ac.ed.epcc.webapp.logging.LoggerService;
/** Class to import new accounting data and store it in a {@link UsageRecordFactory}
 * 
 * @author spb
 *
 * @param <T>
 */

@uk.ac.ed.epcc.webapp.Version("$Id: AccountingUpdater.java,v 1.31 2015/02/02 10:38:07 spb Exp $")


public class AccountingUpdater<T extends UsageRecordFactory.Use> {
	private UsageRecordParseTarget<T> target;
	private AppContext conn;
	private PropertyMap meta_data;
	/** Create an AccountingUpdater. 
	 * This sets the ParseTarget class used to parse the records and a set of MetaData 
	 * properties. These are properties that come from the surrounding code (such as the person performing 
	 * the upload or the machine the data is from if this is not included in the individual records). 
	 * MetaProperties must be the same for every record parsed in this pass but
	 * data from a different upload may have different values.
	 * 
	 * @param conn AppContext
	 * @param meta_data MetaData properties
	 * @param t ParseTarget
	 */
	public AccountingUpdater(AppContext conn,PropertyMap meta_data,UsageRecordParseTarget<T> t){
		this.conn=conn;
		this.meta_data=meta_data;
		this.target=t;
	}
	/** Parse new accounting data 
 	 * 
	 * @param update update text one line per record
	 * @param replace should existing records be replaced
	 * @param verify existing records should be verified against post data
	 * @param augment missing fields should be added to existing records
	 * @return String status message
	 */
	public synchronized  final String receiveAccountingData( String update, boolean replace, boolean verify, boolean augment){
		int n_lines=0;
		int skip=0;
		int duplicate=0;
		int insert=0;
		int n_replace=0;
		int n_partial=0;
		int n_bad_verify=0;
		int n_update_dup=0;
		Date first=null;
		Date last= null;
		Date bad_date = new Date(100000000000L); //. roughly 1973-03-03
    	Logger log = conn.getService(LoggerService.class).getLogger(getClass());
    	Iterator<String> lines;
    
    	boolean check_exists=conn.getBooleanParameter("accounting.checkduplicate", true);
    	Date start=new Date();
    	
    	ErrorSet errors = new ErrorSet();
    	ErrorSet skip_list = new ErrorSet();
    	ErrorSet verify_list = new ErrorSet();
    
    
    	try{
    		lines = target.splitRecords(update);
    		
    		target.startParse(meta_data);
    		
    	}catch(Exception e){
    		log.error("Error initialising parse",e);
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
    				// add date and text
    				map.setProperty(StandardProperties.INSERTED_PROP, start);
    				map.setProperty(StandardProperties.TEXT_PROP, current_line);
    				
    				Date point = map.getProperty(StandardProperties.ENDED_PROP, null);
    				if( point != null ){
    					if( point.getTime() == 0L){
    						// A zero complete data is a common indicator of a 
    						// failed or incomplete record in multiple batch systems.
    						// 
    						throw new SkipRecord("Zero completion time");
    					}
    					// Note may be incremetnal parse without end date
    					if( point.getTime() < 0L){
    						
    						throw new AccountingParseException("Bad completion time <0");
    					}
    					if(point.before(bad_date)){
    						// Clocks on back end must not be set
    						throw new AccountingParseException("Bad completion time (too low)");
    					}
    					if( first == null || point.before(first)){
    						first=point;
    					}
    					if( last == null || point.after(last)){
    						last=point;
    					}
    				}
                    // make an un-commited record from the map
    				T record = target.prepareRecord(map);
    				
    				
    				
    				T old_record = null;
    				if( replace || check_exists){
    					old_record = target.findDuplicate(record);
    				}
    				if( old_record != null && target.isComplete(old_record)){
    					// we already have this record
    					if( replace ){
    						if( target.allowReplace(map, old_record)){
    							target.deleteRecord(old_record);
    							old_record=null;
    							// new record
    							target.commitRecord(map, record);
    							n_replace++;
    						}else{
    							throw new SkipRecord("replace veto");
    						}
    					}else{
    						duplicate++;
    						if( verify ){
    							try{
    								boolean ok=true;
    								for(PropertyTag t : old_record.getDefinedProperties()){
    									Object old_value = old_record.getProperty(t, null);
    									Object new_value = record.getProperty(t, null);
    									if(compare(old_value, new_value)){
    										verify_list.add(t.getName()+" differs", t.getName()+" "+old_value+"->"+new_value+" :"+current_line);
    										ok=false;
    									}
    								}
    								if( ! ok ){
    									n_bad_verify++;
    								}
    							}catch(Throwable t){
    								verify_list.add("Error in verify", current_line, t);
    							}
    						}
    						if( augment ){
    							// update any missing properties (writable properties currently at null)
    							// ie additional fields added to the database. Existing parsed values are
    							// not changed.
    							try{
    								boolean unchanged=true;
    								for(PropertyTag t : old_record.getDefinedProperties()){
    									Object old_value = old_record.getProperty(t, null);
    									Object new_value = record.getProperty(t, null);
    									if(old_record.writable(t) && old_value == null && new_value != null){
    										old_record.setProperty(t, new_value);
    										unchanged=false;
    									}
    								}
    								if( ! unchanged ){
    									old_record.commit();
    									n_update_dup++;
    								}
    							}catch(Throwable t){
    								verify_list.add("Error in augment existing", current_line, t);
    							}
    						}
    					}
    				}else{
    					if( old_record != null ){
    						// must be a partial record
    						// add in new info
    						int num_set = map.setContainer(old_record);
    						if( num_set > 0 ){
    							if( target.commitRecord(map, old_record) ){
    								insert++;
    							}else{
    								n_partial++;
    							}
    						}else{
    							duplicate++;
    						}
    					}else{
    						if(  target.commitRecord(map, record) ){
    							insert++;
    						}else{
    							n_partial++;
    						}
    						
    					}
    					
    				}
    				record.release();
    			}
    		}catch (SkipRecord s){
    			skip_list.add(s.getMessage(),current_line);
    			skip++;
    		}catch(AccountingParseException pe){
    			errors.add(pe.getMessage(), current_line);
    		}catch(Exception e){
    			errors.add("Unexpected parse error",current_line);
    			log.error("Unexpected Error parsing line "+current_line,e);
    		}
    		
    	}
    	StringBuilder error_text= target.endParse();
    	error_text.append(errors.toString());
    	StringBuilder sb = new StringBuilder();
    	sb.append("Inserted lines: ");
    	sb.append(insert);
    	sb.append('\n');
    	sb.append("Replaced lines: ");
    	sb.append(n_replace);
    	sb.append('\n');
    	sb.append("Duplicate lines: ");
    	sb.append(duplicate);
    	sb.append('\n');
    	sb.append("Partial lines: ");
    	sb.append(n_partial);
    	sb.append('\n');
    	sb.append("Skipped lines: ");
    	sb.append(skip);
    	sb.append('\n');
    	if( augment){
    		sb.append("Augmented lines: ");
        	sb.append(n_update_dup);
        	sb.append('\n');
    	}
    	sb.append(skip_list.details(conn.getIntegerParameter("upload.n_skip_to_print", 0)));
    	if( verify){
    		sb.append("Bad verify: ");
        	sb.append(n_bad_verify);
        	sb.append('\n');
        	sb.append(verify_list.details(conn.getIntegerParameter("upload.n_verify_to_print", 20)));
    	}
    
    	if( first != null ){
    		sb.append("First record (completed): ");
    		sb.append(first.toString());
    		sb.append("\n");
    	}
    	if( last != null ){
    		sb.append("Last record (completed): ");
    		sb.append(last.toString());
    		sb.append("\n");
    	}
    	if( error_text.length() > 0 ){
    		sb.append(error_text);
    		log.warn("Error in accounting parse\n"+error_text.toString());
    	}
    	return sb.toString();
	}
	/** compares two values, ignoring any differences due to null or
	 * numerical type. We are looking for significant data differences not non-persisted values or
	 * trivial type conversions.
	 * @param old_value
	 * @param new_value
	 * @return true if different
	 */
	public boolean compare(Object old_value, Object new_value) {
		if( old_value == null || new_value == null){
			return false;
		}
		if( old_value instanceof Number && new_value instanceof Number){
			return ((Number)old_value).doubleValue() != ((Number)new_value).doubleValue();
		}
		return  ! old_value.equals(new_value);
	}
}