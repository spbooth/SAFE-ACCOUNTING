//| Copyright - The University of Edinburgh 2011                            |
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
/*******************************************************************************
 * Copyright (c) - The University of Edinburgh 2010
 *******************************************************************************/
package uk.ac.ed.epcc.safe.accounting.db;

import java.io.InputStream;
import java.util.Date;
import java.util.Iterator;

import uk.ac.ed.epcc.safe.accounting.ErrorSet;
import uk.ac.ed.epcc.safe.accounting.expr.DerivedPropertyMap;
import uk.ac.ed.epcc.safe.accounting.expr.ExpressionTargetContainer;
import uk.ac.ed.epcc.safe.accounting.expr.PropExpressionMap;
import uk.ac.ed.epcc.safe.accounting.properties.PropertyMap;
import uk.ac.ed.epcc.safe.accounting.properties.PropertyTag;
import uk.ac.ed.epcc.safe.accounting.properties.StandardProperties;
import uk.ac.ed.epcc.safe.accounting.update.AccountingParseException;
import uk.ac.ed.epcc.safe.accounting.update.PropertyContainerParser;
import uk.ac.ed.epcc.safe.accounting.update.SkipRecord;
import uk.ac.ed.epcc.webapp.AppContext;
import uk.ac.ed.epcc.webapp.CurrentTimeService;
import uk.ac.ed.epcc.webapp.jdbc.DatabaseService;
import uk.ac.ed.epcc.webapp.logging.Logger;
import uk.ac.ed.epcc.webapp.logging.LoggerService;
/** Class to import new accounting data and store it in a {@link UsageRecordFactory}
 * 
 * @author spb
 * @see UsageRecordParseTarget
 * @param <T>
 */




public class AccountingUpdater<T extends UsageRecordFactory.Use,R> {
	private UsageRecordParseTarget<R> target;
	private AppContext conn;
	private final PropertyMap initial_meta_data;
	/** Create an AccountingUpdater. 
	 * This sets the ParseTarget class used to parse the records and a set of MetaData 
	 * properties. These are properties that come from the surrounding code (such as the person performing 
	 * the upload or the machine the data is from if this is not included in the individual records). 
	 * MetaProperties must be the same for every record parsed in this pass but
	 * data from a different upload may have different values.
	 * 
	 * @param conn AppContext
	 * @param initial_meta_data MetaData properties
	 * @param t ParseTarget
	 */
	public AccountingUpdater(AppContext conn,PropertyMap initial_meta_data,UsageRecordParseTarget<R> t){
		this.conn=conn;
		this.initial_meta_data = initial_meta_data;
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
	public synchronized  final String receiveAccountingData( InputStream update, boolean replace, boolean verify, boolean augment){
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
    	DatabaseService db_serv = conn.getService(DatabaseService.class);
    	Iterator<R> lines;
    
    	boolean check_exists=conn.getBooleanParameter("accounting.checkduplicate", true);
    	CurrentTimeService time = conn.getService(CurrentTimeService.class);
    	Date start=time.getCurrentTime();
    	
    	ErrorSet errors = new ErrorSet();
    	int max_skip_details = conn.getIntegerParameter("upload.n_skip_to_print", 0);
    	ErrorSet skip_list = new ErrorSet();
    	skip_list.setMaxDetails(max_skip_details);
    	
    	int max_verify_details = conn.getIntegerParameter("upload.n_verify_to_print", 20);
    	ErrorSet verify_list = new ErrorSet();
    	verify_list.setMaxDetails(max_verify_details);
    	
    	PropertyContainerParser<R> parser = target.getParser();
    	// Final set of dervided properties from the ParseTarget
    	PropExpressionMap expr=target.getDerivedProperties();
    	
    	//Meta-data should include all derived properties to support
    	// derivations from the defined values and 
    	// constants.
    	DerivedPropertyMap meta_data = new DerivedPropertyMap(conn);
    	meta_data.setAll(initial_meta_data);
    	meta_data.addDerived(expr);
    
    	try{
    		
			lines = parser.splitRecords(update);
    		
    		target.startParse(meta_data);
    		
    	}catch(Exception e){
    		log.error("Error initialising parse",e);
    		return "Error initialising parse";
    	}
    	while (lines.hasNext()) {
    		n_lines++;
    		R current_line =lines.next();
    		String fmt = parser.formatRecord(current_line);
			try{
    			DerivedPropertyMap map = new DerivedPropertyMap(conn);
    			if( meta_data != null ){
    				
    				meta_data.setContainer(map);
    				if( expr != null ){
    					// start by installing the final set of derivations.
    					// the parse operation will re-install after each parse stage
    					// to handle the case of definitions overridden within the stack
    					map.addDerived(expr);
    				}
    			}
    			
    			if( target.parse(map, current_line) ){
    				// add date and text
    				map.setProperty(StandardProperties.INSERTED_PROP, start);
    				map.setProperty(StandardProperties.TEXT_PROP, fmt.trim());
    				
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
    					Date start_point = map.getProperty(StandardProperties.STARTED_PROP);
    					if( start_point != null && point.before(start_point)){
    						throw new AccountingParseException("Reversed time bounds");
    					}
    				}
    				
                    // make an un-commited record from the map
    				ExpressionTargetContainer record = target.prepareRecord(map);
    				
    				
    				
    				ExpressionTargetContainer old_record = null;
    				if( replace || check_exists){
    					old_record = target.findDuplicate(record);
    				}
    				if( old_record != null && target.isComplete(old_record)){
    					// we already have this record
    					if( replace ){
    						if( target.allowReplace(map, old_record)){
    							target.deleteRecord(old_record);
    							old_record.release();
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
    							}catch(Exception t){
    								verify_list.add("Error in verify", fmt, t);
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
    							}catch(Exception t){
    								verify_list.add("Error in augment existing", fmt, t);
    							}
    						}
    						old_record.release();
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
    						old_record.release();
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
    			map.release();
    		}catch (SkipRecord s){
    			skip_list.add(s.getMessage(),fmt);
    			skip++;
    		}catch(AccountingParseException pe){
    			errors.add(pe.getMessage(), fmt);
    		}catch(Exception e){
    			errors.add("Unexpected parse error",fmt);
    			log.error("Unexpected Error parsing line "+fmt,e);
    		}
    		// We don't want very long held locks so commit between records.
			db_serv.commitTransaction();
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
    	sb.append(skip_list.details(max_skip_details));
    	if( verify){
    		sb.append("Bad verify: ");
        	sb.append(n_bad_verify);
        	sb.append('\n');
        	
			sb.append(verify_list.details(max_verify_details));
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
    	errors.clear();
    	skip_list.clear();
    	verify_list.clear();
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