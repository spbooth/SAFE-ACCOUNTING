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
package uk.ac.ed.epcc.safe.accounting.parsers;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import uk.ac.ed.epcc.safe.accounting.db.DataObjectPropertyContainer;
import uk.ac.ed.epcc.safe.accounting.db.UsageRecordFactory;
import uk.ac.ed.epcc.safe.accounting.db.UsageRecordFactory.Use;
import uk.ac.ed.epcc.safe.accounting.expr.DerivedPropertyMap;
import uk.ac.ed.epcc.safe.accounting.expr.ExpressionTargetContainer;
import uk.ac.ed.epcc.safe.accounting.policy.LinkPolicy;
import uk.ac.ed.epcc.safe.accounting.properties.MultiFinder;
import uk.ac.ed.epcc.safe.accounting.properties.PropertyContainer;
import uk.ac.ed.epcc.safe.accounting.properties.PropertyFinder;
import uk.ac.ed.epcc.safe.accounting.properties.PropertyRegistry;
import uk.ac.ed.epcc.safe.accounting.properties.PropertyTag;
import uk.ac.ed.epcc.safe.accounting.properties.StandardProperties;
import uk.ac.ed.epcc.safe.accounting.reference.ReferenceTag;
import uk.ac.ed.epcc.safe.accounting.selector.AndRecordSelector;
import uk.ac.ed.epcc.safe.accounting.selector.SelectClause;
import uk.ac.ed.epcc.safe.accounting.update.AbstractPropertyContainerParser;
import uk.ac.ed.epcc.safe.accounting.update.AccountingParseException;
import uk.ac.ed.epcc.safe.accounting.update.AutoTable;
import uk.ac.ed.epcc.safe.accounting.update.BatchParser;
import uk.ac.ed.epcc.safe.accounting.update.IncrementalPropertyContainerParser;
import uk.ac.ed.epcc.safe.accounting.update.OptionalTable;
import uk.ac.ed.epcc.webapp.AppContext;
import uk.ac.ed.epcc.webapp.jdbc.filter.MatchCondition;
import uk.ac.ed.epcc.webapp.logging.Logger;
import uk.ac.ed.epcc.webapp.logging.LoggerService;
import uk.ac.ed.epcc.webapp.model.data.reference.IndexedReference;
/** Parser for Globus JobManager records. 
 * As these are event logs we need to use an incremental parser.
 * 
 * Optionally other accounting records can be linked to the entries of this table in 
 * a manner similar to {@link LinkPolicy}. The remote table is identified based on the jobmanager type.
 * If the property;
 * 
 * <b>GlobusJobManagerParser.<i>table</i>.<i>jobmanager-type</i></b> is set then this is used as the
 * table name of manager table (Assumed to support the batch:JobID and batch:SubmittedTimestamp and batch:JobId properties).
 * When a remote table is configured the record will not evaluate as complete until the master record has been located.
 * 
 * 
 * 
 * @author spb
 *
 */


public class GlobusJobManagerParser extends AbstractPropertyContainerParser implements IncrementalPropertyContainerParser {

	private static final PropertyRegistry globus_reg = new PropertyRegistry("globus","Properties from the globus jobmanager log");
	@AutoTable(target=String.class)
	public static final PropertyTag<String> GLOBUS_USERNAME = new PropertyTag<String>(globus_reg, "UserName",String.class,"Local username mapped to");
	@AutoTable(target=String.class,length=128)
	public static final PropertyTag<String> GLOBUS_DN = new PropertyTag<String>(globus_reg, "Dn",String.class,"DN used to submit job");
	@AutoTable(target=String.class,length=512)
	public static final PropertyTag<String> GLOBUS_SUBMIT_HOST = new PropertyTag<String>(globus_reg, "SubmitHost",String.class);
	@AutoTable(target=String.class)
	public static final PropertyTag<String> GLOBUS_MANAGER = new PropertyTag<String>(globus_reg, "Manager",String.class);
	@AutoTable(target=String.class)
	public static final PropertyTag<String> GLOBUS_JOBID = new PropertyTag<String>(globus_reg, "JobID",String.class,"Batch system id");
	@AutoTable(target=String.class,unique=true,length=128)
	public static final PropertyTag<String> GLOBUS_JOBTYPEID = new PropertyTag<String>(globus_reg, "JobTypeID",String.class);
	@OptionalTable
	public static final PropertyTag<String> GLOBUS_JOBTYPE = new PropertyTag<String>(globus_reg, "JobType",String.class);

	@AutoTable(target=Date.class)
	public static final PropertyTag<Date> GLOBUS_START_DATE = new PropertyTag<Date>(globus_reg, "StartTime",Date.class);
	@AutoTable(target=Date.class)
	public static final PropertyTag<Date> GLOBUS_END_DATE = new PropertyTag<Date>(globus_reg, "EndTime",Date.class);
	@AutoTable(target=Date.class)
	public static final PropertyTag<Date> GLOBUS_EXEC_DATE = new PropertyTag<Date>(globus_reg, "ExecTime",Date.class);
	/** Id of parent record. This can't be a reference tag as there may be multiple parent tables.
	 * This field is needed in order to identify complete records/
	 */
	@AutoTable(target=Integer.class)
	public static final PropertyTag<Integer> GLOBUS_PARENT_ID = new PropertyTag<Integer>(globus_reg, "ParentID", Integer.class);
	static{
		globus_reg.lock();
	}
	private final long grace_millis=300000L;
	private static final SimpleDateFormat df = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
	private static final Pattern parse_patten=Pattern.compile(
			"\\S+ (\\d\\d\\d\\d/\\d\\d/\\d\\d \\d\\d:\\d\\d:\\d\\d) "+
			"(\\S+) (\\S+) (.*)"
			);
	private static final Pattern dn_msg = Pattern.compile("for (.*) on (\\S+)");
	private static final Pattern user_msg = Pattern.compile("mapped to (\\w+) \\((\\d+),\\s*(\\d+)\\)");
	// vidar just has the batch job here but
	// eddie has batchid|stdout|stderr
	// so exclude pipe symbol from possible job ids and discard anything after a pipe
	private static final Pattern jobid_msg = Pattern.compile("has GRAM_SCRIPT_JOB_ID ([\\S&&[^|]]+)(?:|\\S*) manager type (\\S+)");
	private static final Pattern exit_msg = Pattern.compile("JM exiting");
	
	private Map<String,UsageRecordFactory<?>> manager_factories=null;
	private String tag;
	private AppContext conn;
	Logger log;
	@Override
	public boolean parse(DerivedPropertyMap map, String record)
			throws AccountingParseException {
	
		if(record.trim().length()==0){
			return false;
		}
		log.debug("line is "+record);
		Matcher m = parse_patten.matcher(record);
		if( m.matches()){
			try {
				Date d = df.parse(m.group(1));
				String job_type=m.group(2);
				String job_type_id=m.group(3);
				String message = m.group(4);
				
				map.setProperty(GLOBUS_JOBTYPE, job_type);
				map.setProperty(GLOBUS_JOBTYPEID, job_type_id);
				
				Matcher dn_matcher = dn_msg.matcher(message);
				if( dn_matcher.matches()){
					log.debug("Dn matches");
					map.setProperty(GLOBUS_START_DATE, d);
					map.setProperty(GLOBUS_DN, dn_matcher.group(1));
					map.setProperty(GLOBUS_SUBMIT_HOST,dn_matcher.group(2));
					return true;
				}else{
					Matcher user_matcher = user_msg.matcher(message);
					if( user_matcher.matches()){
						log.debug("user matches");
						map.setProperty(GLOBUS_USERNAME, user_matcher.group(1));
						return true;
					}else{
						Matcher jobid_matcher = jobid_msg.matcher(message);
						if( jobid_matcher.matches()){
							log.debug("Jobid matches");
							map.setProperty(GLOBUS_EXEC_DATE, d);
							String job_id = jobid_matcher.group(1);
							map.setProperty(GLOBUS_JOBID, job_id);
							String jobmanager = jobid_matcher.group(2);
							map.setProperty(GLOBUS_MANAGER,jobmanager );
							UsageRecordFactory fac = manager_factories.get(jobmanager);
							if( fac == null ){
								log.debug(jobmanager+" not a tracked factory");
								// not a tracked factory
								map.setProperty(GLOBUS_PARENT_ID,Integer.valueOf(0));
							}else{
								long point = d.getTime();
								AndRecordSelector sel = new AndRecordSelector();
								sel.add(new SelectClause<String>(BatchParser.JOB_ID_PROP, job_id));
								sel.add(new SelectClause<Date>(BatchParser.SUBMITTED_PROP, MatchCondition.GT, new Date(point-grace_millis)));
								sel.add(new SelectClause<Date>(BatchParser.SUBMITTED_PROP, MatchCondition.LT, new Date(point+grace_millis)));
								UsageRecordFactory.Use master;
								try {
									master = (Use) fac.find(fac.getFilter(sel));
								} catch (Exception e) {
									throw new AccountingParseException("Error finding master", e);
								}
								if( master != null ){
									log.debug("found master "+master.getIdentifier());
									Integer id = Integer.valueOf(master.getID());
									assert(id.intValue() > 0);
									map.setProperty(GLOBUS_PARENT_ID, id);
								}else{
									log.debug("No master found for "+job_id);
								}
							}
							return true;
						}else{
							Matcher exit_matcher=exit_msg.matcher(message);
							if( exit_matcher.matches()){
								log.debug("exit matches");
								map.setProperty(GLOBUS_END_DATE, d);
								return true;
							}
						}
					}
				}
				log.debug("Line does not match");
				return false;
				
				
			} catch (ParseException e) {
				throw new AccountingParseException("bad date format", e);
			}
		}else{
			throw new AccountingParseException("Unexpected line format");
		}
	}

	public PropertyFinder initFinder(AppContext ctx, PropertyFinder prev,
			String table) {
		conn=ctx;
		log = conn.getService(LoggerService.class).getLogger(getClass());
		tag=table;
		MultiFinder mf = new MultiFinder();
		mf.addFinder(StandardProperties.time);
		mf.addFinder(StandardProperties.base);
		mf.addFinder(globus_reg);
		return mf;
	}

	public boolean isComplete(ExpressionTargetContainer record) {
		// We need the set of properties common to ALL records
		
		Date end_date = record.getProperty(GLOBUS_END_DATE,null);
		if( end_date==null){
			log.debug("No end date");
			return false;
		}
		log.debug("End date is "+end_date);
		Date start_date = record.getProperty(GLOBUS_START_DATE,null);
		if( start_date==null){
			log.debug("No start date");
			return false;
		}
		log.debug("start date is "+start_date);
		String dn = record.getProperty(GLOBUS_DN,null);
		if( dn==null){
			log.debug("No globus dn");
			return false;
		}
		log.debug("dn is "+dn);
		String globus_username = record.getProperty(GLOBUS_USERNAME,null);
		if( globus_username==null){
			log.debug("No username");
			return false;
		}
		log.debug("globus username is "+globus_username);
		Integer parent_id = record.getProperty(GLOBUS_PARENT_ID,null);
		if( parent_id==null){
			log.debug("No parent id");
			return false;
		}
		assert(parent_id.intValue()>=0);
		log.debug("parent id is "+parent_id);
		log.debug("Record is complete");
		return true;
	}

	@Override
	public String endParse() {
		manager_factories.clear();
		manager_factories=null;
		return super.endParse();
	}

	@Override
	public void startParse(PropertyContainer staticProps) throws Exception {
		super.startParse(staticProps);
		
		manager_factories = new HashMap<String,UsageRecordFactory<?>>();
		String prefix = "GlobusJobmanagerParser."+tag+".";
		Map<String,String> props = conn.getInitParameters(prefix);
		log.debug(prefix+" returns "+props.size()+" props");
		for(String key : props.keySet()){
			log.debug("key is "+key);
			String name = key.substring(prefix.length());
			log.debug("name is "+name); 
			String tag = props.get(key);
			log.debug("tag is "+tag);
			UsageRecordFactory<?> fac = conn.makeObjectWithDefault(UsageRecordFactory.class, null, tag);
			if( fac != null ){
				manager_factories.put(name, fac);
			}else{
				log.error("No master factory found for "+key+"->"+tag);
			}
		}
	}

	@SuppressWarnings("unchecked")
	public void postComplete(ExpressionTargetContainer record) throws Exception{
		log.debug("In postComplete");
		UsageRecordFactory fac = manager_factories.get(record.getProperty(GLOBUS_MANAGER));
		if(fac != null ){
			log.debug("Setting master");
			ExpressionTargetContainer proxy = fac.getExpressionTarget((DataObjectPropertyContainer) fac.find(record.getProperty(GLOBUS_PARENT_ID)));
			ReferenceTag ref = (ReferenceTag) fac.getFinder().find(IndexedReference.class,tag);
			
			proxy.setProperty(ref,record.getProperty(ref));
			proxy.commit();
			log.debug("Master set");
		}
	}

}