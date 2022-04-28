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
import uk.ac.ed.epcc.safe.accounting.properties.PropertyRegistry;
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
import uk.ac.ed.epcc.webapp.timer.TimeClosable;
/** Class to import new accounting data and store it in a {@link UsageRecordFactory}
 * 
 * @author spb
 * @see UsageRecordParseTarget
 * @param <T>
 */




public class AccountingUpdater<T extends UsageRecordFactory.Use,R> {
	
	public static final PropertyRegistry parse_ctx = new PropertyRegistry("context", "Properties for the parse mode, used to pass context to policies");
	public static final PropertyTag<Boolean> REPLACE_PROP = new PropertyTag<>(parse_ctx,"Replace",Boolean.class,"Replace existing records");
	public static final PropertyTag<Boolean> VERIFY_PROP = new PropertyTag<>(parse_ctx,"Verify",Boolean.class,"Verify existing records");
	public static final PropertyTag<Boolean> AUGMENT_PROP = new PropertyTag<>(parse_ctx,"Augment",Boolean.class,"Augment existing records");
	
	
	private UsageRecordParseTarget<R> target;
	private AppContext conn;
	private final PropertyMap initial_meta_data;
	private int n_lines=0;
	private int skip=0;
	private int duplicate=0;
	private int insert=0;
	private int n_replace=0;
	private int n_partial=0;
	private int n_bad_verify=0;
	private int n_update_dup=0;
	private Date first=null;
	private Date last= null;
	static private final Date bad_date = new Date(100000000000L); //. roughly 1973-03-03
	private final Logger log;
	private final DatabaseService db_serv;
	private final Date start;
	private final boolean check_exists;
	private final ErrorSet errors = new ErrorSet();
	private final ErrorSet skip_list = new ErrorSet();
	private final ErrorSet verify_list = new ErrorSet();
	private final int max_skip_details;
	private final int max_verify_details;
	private final boolean replace, verify, augment;
	private final PropertyContainerParser<R> parser;
	private final DerivedPropertyMap meta_data;
	private final PropExpressionMap expr;
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
	 * @throws Exception 
	 */
	public AccountingUpdater(AppContext conn,PropertyMap initial_meta_data,UsageRecordParseTarget<R> t, boolean replace, boolean verify, boolean augment) throws Exception{
		this.conn=conn;
		this.initial_meta_data = initial_meta_data;
		this.target=t;
		this.replace=replace;
		this.verify=verify;
		this.augment=augment;
		log=  conn.getService(LoggerService.class).getLogger(getClass());
		db_serv = conn.getService(DatabaseService.class);
		CurrentTimeService time = conn.getService(CurrentTimeService.class);
    	start=time.getCurrentTime();
    	check_exists=conn.getBooleanParameter("accounting.checkduplicate", true);
    	max_skip_details = conn.getIntegerParameter("upload.n_skip_to_print", 0);
    	
    	skip_list.setMaxDetails(max_skip_details);
    	
    	max_verify_details = conn.getIntegerParameter("upload.n_verify_to_print", 20);
    	
    	verify_list.setMaxDetails(max_verify_details);
    	parser = target.getParser();
    	// Final set of dervided properties from the ParseTarget
    	expr=target.getDerivedProperties();
    	
    	//Meta-data should include all derived properties to support
    	// derivations from the defined values and 
    	// constants.
    	meta_data = new DerivedPropertyMap(conn);
    	meta_data.setAll(initial_meta_data);
    	meta_data.setProperty(REPLACE_PROP, replace);
    	meta_data.setProperty(VERIFY_PROP, verify);
    	meta_data.setProperty(AUGMENT_PROP, augment);
    	meta_data.addDerived(expr);
    	target.startParse(meta_data);
	}
	/** Parse new accounting data 
 	 * 
	 * @param update update text one line per record
	 * @param replace should existing records be replaced
	 * @param verify existing records should be verified against post data
	 * @param augment missing fields should be added to existing records
	 * @return String status message
	 */
	public final String receiveAccountingData( InputStream update){
    	Iterator<R> lines;
    	try(TimeClosable split = new TimeClosable(conn, "splitRecords")){
			lines = parser.splitRecords(update);
    	}catch(Exception e){
    		log.error("Error initialising parse",e);
    		return "Error initialising parse";
    	}
    	while (lines.hasNext()) {
    		receiveRecord(meta_data,lines.next());
    	}
    	return getReport();
	}
	public String getReport() {
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
	/** Import a single record.
	 * 
	 * @param input_data  {@link DerivedPropertyMap} pre-parse properties 
	 * @param current_line data to parse
	 */
	public void receiveRecord(DerivedPropertyMap input_data,R current_line) {
		String fmt = parser.formatRecord(current_line);
		n_lines++;
		try(TimeClosable rr= new TimeClosable(conn, "receiveRecord")){
			DerivedPropertyMap map = new DerivedPropertyMap(conn);
			if( input_data != null ){
				// make sure no derived properties are promoted to non-derived
				input_data.setDerivedPropertyMap(map);
				if( expr != null ){
					// start by installing the final set of derivations.
					// the parse operation will re-install after each parse stage
					// to handle the case of definitions overridden within the stack
					map.addDerived(expr);
				}
			}
			// add date and text
			map.setProperty(StandardProperties.INSERTED_PROP, start);
			map.setProperty(StandardProperties.TEXT_PROP, fmt.trim());
			if( target.parse(map, current_line) ){
				
				
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
				//ExpressionTargetContainer record = target.prepareRecord(map);
				
				
				
				ExpressionTargetContainer old_record = null;
				if( replace || check_exists){
					old_record = target.findDuplicate(map);
				}
				if( old_record != null && target.isComplete(old_record)){
					// we already have this record
					if( replace ){
						target.lateParse(map);
						if( target.allowReplace(map, old_record)){
							target.deleteRecord(old_record);
							old_record.release();
							old_record=null;
							// new record
							target.commitRecord(map,target.prepareRecord(map));
							n_replace++;
						}else{
							throw new SkipRecord("replace veto");
						}
					}else{
						duplicate++;
						if( verify ){
							target.lateParse(map);
							try{
								boolean ok=true;
								for(PropertyTag t : old_record.getDefinedProperties()){
									Object old_value = old_record.getProperty(t, null);
									Object new_value = map.getProperty(t, null);
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
							target.lateParse(map);
							// update any missing properties (writable properties currently at null)
							// ie additional fields added to the database. Existing parsed values are
							// not changed.
							try{
								boolean unchanged=true;
								for(PropertyTag t : old_record.getDefinedProperties()){
									Object old_value = old_record.getProperty(t, null);
									Object new_value = map.getProperty(t, null);
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
						target.lateParse(map);
						int num_set = map.setContainer(old_record);
						if( num_set > 0 ){
							if( target.commitRecord(map,old_record) ){
								insert++;
							}else{
								n_partial++;
							}
						}else{
							duplicate++;
						}
						old_record.release();
					}else{
						target.lateParse(map);
						if(  target.commitRecord(map,target.prepareRecord(map)) ){
							insert++;
						}else{
							n_partial++;
						}
						
					}
					
				}
			}
			map.release();
			// We don't want very long held locks so commit between records.
			try(TimeClosable cm = new TimeClosable(conn, "Commit transaction")){
				db_serv.commitTransaction();
			}
		}catch (SkipRecord s){
			skip_list.add(s.getMessage(),fmt);
			skip++;
		}catch(AccountingParseException pe){
			errors.add(pe.getMessage(), fmt);
		}catch(Exception e){
			errors.add("Unexpected parse error",fmt);
			log.error("Unexpected Error parsing line "+fmt,e);
		}
		
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
	/** Convert the text version of the recrod (from the {@link StandardProperties.TEXT_PROP} property)
	 * into an input record.
	 * 
	 * @param map
	 * @return
	 */
	public R getRecord(DerivedPropertyMap map) {
		String text = map.getProperty(StandardProperties.TEXT_PROP, null);
		if( text == null) {
			return null;
		}
		return parser.getRecord(text);
	}
}