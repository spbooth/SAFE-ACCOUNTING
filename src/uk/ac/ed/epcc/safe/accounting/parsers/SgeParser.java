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

import java.util.Date;

import uk.ac.ed.epcc.safe.accounting.expr.BinaryPropExpression;
import uk.ac.ed.epcc.safe.accounting.expr.ConstPropExpression;
import uk.ac.ed.epcc.safe.accounting.expr.DerivedPropertyMap;
import uk.ac.ed.epcc.safe.accounting.expr.PropExpressionMap;
import uk.ac.ed.epcc.safe.accounting.expr.PropertyCastException;
import uk.ac.ed.epcc.safe.accounting.expr.StringPropExpression;
import uk.ac.ed.epcc.safe.accounting.properties.InvalidExpressionException;
import uk.ac.ed.epcc.safe.accounting.properties.MultiFinder;
import uk.ac.ed.epcc.safe.accounting.properties.PropertyContainer;
import uk.ac.ed.epcc.safe.accounting.properties.PropertyFinder;
import uk.ac.ed.epcc.safe.accounting.properties.PropertyRegistry;
import uk.ac.ed.epcc.safe.accounting.properties.PropertyTag;
import uk.ac.ed.epcc.safe.accounting.properties.StandardProperties;
import uk.ac.ed.epcc.safe.accounting.update.AccountingParseException;
import uk.ac.ed.epcc.safe.accounting.update.AutoTable;
import uk.ac.ed.epcc.safe.accounting.update.BatchParser;
import uk.ac.ed.epcc.safe.accounting.update.OptionalTable;
import uk.ac.ed.epcc.safe.accounting.update.SkipRecord;
import uk.ac.ed.epcc.webapp.AppContext;
import uk.ac.ed.epcc.webapp.Contexed;
import uk.ac.ed.epcc.webapp.exceptions.ConsistencyError;
import uk.ac.ed.epcc.webapp.jdbc.exception.DataException;
import uk.ac.ed.epcc.webapp.jdbc.expr.Operator;
import uk.ac.ed.epcc.webapp.logging.LoggerService;



public class SgeParser extends BatchParser implements Contexed {
	private final AppContext c;
	private String table;
	private static final boolean DEFAULT_SKIP_INNER=true;
	private static final boolean DEFAULT_SKIP_FAILED=false;
	private static final boolean DEFAULT_SKIP_SUBTASK=true;
	private boolean skip_inner_job = DEFAULT_SKIP_INNER;
	private boolean skip_failed_job = DEFAULT_SKIP_FAILED;
	private boolean skip_sub_task = DEFAULT_SKIP_SUBTASK;
	public SgeParser(AppContext c){
		this.c=c;
	}
	public static  final PropertyRegistry sge = new PropertyRegistry("sge","The SGE batch parser accounting properties");

	@AutoTable
	public static final PropertyTag<String> SGE_QUEUE_PROP = new PropertyTag<String>(sge,"Queue",String.class,"SGE queue");
	@OptionalTable
	public static final PropertyTag<String> SGE_HOSTNAME_PROP = new PropertyTag<String>(sge,"Hostname",String.class,"SGE hostname");
	@AutoTable
	public static final PropertyTag<String> SGE_GROUPNAME_PROP = new PropertyTag<String>(sge,"GroupName",String.class,"SGE group name");
	@AutoTable
	public static final PropertyTag<String> SGE_USERNAME_PROP = new PropertyTag<String>(sge,"UserName",String.class,"SGE user name");
	@AutoTable(length=128)
	public static final PropertyTag<String> SGE_JOBNAME_PROP = new PropertyTag<String>(sge,"JobName",String.class,"SGE job name");
	@AutoTable
	public static final PropertyTag<String> SGE_ACCOUNT_PROP = new PropertyTag<String>(sge,"Account",String.class,"SGE Account");
	@OptionalTable
	public static final PropertyTag<Number> SGE_PRIORITY_PROP=new PropertyTag<Number>(sge,"Priority",Number.class,"Priority value assigned to job");
	@AutoTable
	public static final PropertyTag<String> SGE_PE_PROP = new PropertyTag<String>(sge,"PE",String.class,"SGE PE used for job");
	@AutoTable
	public static final PropertyTag<Date> SGE_SUBMITTED_PROP = new PropertyTag<Date>(sge,"SubmittedTimestamp",Date.class,"Time job was submitted");
	@AutoTable
	public static final PropertyTag<Date> SGE_STARTED_PROP = new PropertyTag<Date>(sge,"StartedTimestamp",Date.class,"Time job was started");
	// In field order ended goes here but we want to force order in auto-created table index.
	@AutoTable(target=Integer.class,unique=true)
	public static final PropertyTag<Number> SGE_JOB_ID = new PropertyTag<Number>(sge,"SgeId",Number.class,"Integer Job id used by SGE");
	@AutoTable(target=Integer.class,unique=true)
	public static final PropertyTag<Number> SGE_TASK_PROP = new PropertyTag<Number>(sge,"Task",Number.class,"SGE Task id, this is the index of this task within an array job");

	@AutoTable(unique=true)
	public static final PropertyTag<Date> SGE_ENDED_PROP = new PropertyTag<Date>(sge,"CompletedTimestamp",Date.class,"Time job completed");
	
	
	@OptionalTable
	public static final PropertyTag<Integer> SGE_EXIT_PROP = new PropertyTag<Integer>(sge,"Exit",Integer.class,"Numerical exit status of job or command");
	@OptionalTable
	public static  final PropertyTag<String> SGE_PE_TASK_PROP = new PropertyTag<String>(sge,"PeTaskId",String.class,"SGE PE Task id, this is the index of an MPI sub-task started through SGE");
	@OptionalTable
	public static final PropertyTag<Number> SGE_FAILED_PROP = new PropertyTag<Number>(sge,"Failed",Number.class);
    @AutoTable
	public static final PropertyTag<Number> SGE_WALLCLOCK_PROP = new PropertyTag<Number>(sge,"Wall",Number.class,"Wallclock in seconds as reported directly by SGE");
	@OptionalTable
    public static final PropertyTag<String> SGE_CATEGORY_PROP = new PropertyTag<String>(sge,"Category",String.class,"SGE job Category");
	@AutoTable(target=Integer.class)
	public static final PropertyTag<Integer> SGE_SLOTS = new PropertyTag<Integer>(sge,"Slots",Integer.class,"Number of SGE slots allocated to the job");
	@AutoTable
	public static final PropertyTag<String> SGE_PROJECT_PROP = new PropertyTag<String>(sge,"Project",String.class,"SGE Project");
	@AutoTable(target=Double.class)
	public static final PropertyTag<Number> SGE_CPU_TIME_PROP = new PropertyTag<Number>(sge,"CPUTime",Number.class,"Total CPU time (seconds) used by all processes");
	@AutoTable
	public static final PropertyTag<String> SGE_DEPARTMENT_PROP = new PropertyTag<String>(sge,"Department",String.class,"SGE Department");


	public static final PropertyTag<Number> SGE_MEMORY_PROP = new PropertyTag<Number>(sge,"Mem",Number.class,"SGE measured memory usage in Gbyte cpu seconds");

	public static final PropertyTag<Number> SGE_IOWAIT_PROP = new PropertyTag<Number>(sge,"IOWait",Number.class,"SGE IO wait time in seconds");

	public static final PropertyTag<Number> SGE_IODATA_PROP = new PropertyTag<Number>(sge,"IOData",Number.class,"SGE IO data transfered");


	public static final PropertyTag<Number> SGE_VMEM_PROP = new PropertyTag<Number>(sge,"Vmem",Number.class,"SGE max Vmem in bytes");

	public static final PropertyTag<Number> SGE_ARID_PROP = new PropertyTag<Number>(sge,"ARID",Number.class,"SGE Advance reservation identifier");
	public static final PropertyTag<Number> SGE_AR_TIME_PROP = new PropertyTag<Number>(sge,"ARTime",Number.class,"SGE Advance reservation submission time");

	public static final PropertyTag<Number> SGE_UTIME_PROP = new PropertyTag<Number>(sge,"ru_utime",Number.class,"SGE ru_utime field");
	public static final PropertyTag<Number> SGE_IDRSS_PROP = new PropertyTag<Number>(sge,"ru_idrss",Number.class,"SGE ru_idrss field");
	public static final PropertyTag<Number> SGE_INBLOCK_PROP = new PropertyTag<Number>(sge,"ru_inblock",Number.class,"SGE ru_inblock field");
	public static final PropertyTag<Number> SGE_ISMRSS_PROP = new PropertyTag<Number>(sge,"ru_ismrss",Number.class,"SGE ru_ismrss field");
	public static final PropertyTag<Number> SGE_IXRSS_PROP = new PropertyTag<Number>(sge,"ru_ixrss",Number.class,"SGE ru_ixrss field");
	public static final PropertyTag<Number> SGE_MAJFLT_PROP = new PropertyTag<Number>(sge,"ru_majflt",Number.class,"SGE ru_majflt field");
	public static final PropertyTag<Number> SGE_ISRSS_PROP = new PropertyTag<Number>(sge,"ru_isrss",Number.class,"SGE ru_isrss field");
	public static final PropertyTag<Number> SGE_MAXRSS_PROP = new PropertyTag<Number>(sge,"ru_maxrss",Number.class,"SGE ru_maxrss field");
	public static final PropertyTag<Number> SGE_MINFLT_PROP = new PropertyTag<Number>(sge,"ru_minflt",Number.class,"SGE ru_minflt field");
	public static final PropertyTag<Number> SGE_MSGRCV_PROP = new PropertyTag<Number>(sge,"ru_msgrcv",Number.class,"SGE ru_msgrcv field");
	public static final PropertyTag<Number> SGE_MSGSND_PROP = new PropertyTag<Number>(sge,"ru_msgsnd",Number.class,"SGE ru_msgsnd field");
	public static final PropertyTag<Number> SGE_NIVCSW_PROP = new PropertyTag<Number>(sge,"ru_nivcsw",Number.class,"SGE ru_nivcsw field");   
	public static final PropertyTag<Number> SGE_NSIGNALS_PROP = new PropertyTag<Number>(sge,"ru_nsignals",Number.class,"SGE ru_nsignals field");
	public static final PropertyTag<Number> SGE_STIME_PROP = new PropertyTag<Number>(sge,"ru_stime",Number.class,"SGE ru_stime field");
	public static final PropertyTag<Number> SGE_OUTBLOCK_PROP = new PropertyTag<Number>(sge,"ru_outblock",Number.class,"SGE ru_outblock field");
	public static final PropertyTag<Number> SGE_NSWAP_PROP = new PropertyTag<Number>(sge,"ru_nswap",Number.class,"SGE ru_nswap field");
	public static final PropertyTag<Number> SGE_NVCSW_PROP = new PropertyTag<Number>(sge,"ru_nvcsw",Number.class,"SGE ru_nvcsw field");

	static{
		sge.lock();
	}
	@Override
	public boolean parse(DerivedPropertyMap r,String record) throws AccountingParseException {
		boolean is_inner=false;
		boolean is_sub_task=false;
		
		if( record.startsWith("#")){
			return false;
		}
		String args[] = record.split(":");
		if( args.length < 43 ){
			throw new AccountingParseException("Record too short");
		}


		r.setProperty(SGE_QUEUE_PROP, args[0].trim());
		r.setProperty(SGE_HOSTNAME_PROP,args[1].trim());
		r.setProperty(SGE_GROUPNAME_PROP,args[2].trim());
		r.setProperty(SGE_USERNAME_PROP,args[3].trim());
		r.setProperty(SGE_JOBNAME_PROP, args[4].trim());
		r.setProperty(SGE_JOB_ID, readInteger(args[5]));
		r.setProperty(SGE_ACCOUNT_PROP,args[6].trim());

		r.setProperty(SGE_PRIORITY_PROP,readInteger(args[7]));

		Date submission = readTime(args[8]);
		is_inner = (submission.getTime() == 0L);  // inner job
		if(  is_inner && skip_inner_job ){
			throw new SkipRecord("Inner SGE job");
		}
		r.setProperty(SGE_SUBMITTED_PROP,submission);

		r.setProperty(SGE_STARTED_PROP,readTime(args[9]));

		r.setProperty(SGE_ENDED_PROP,readTime(args[10]));

		// indicates a failed job do not charge
		Integer failed = readInteger(args[11]);
		if( failed.intValue() != 0 && skip_failed_job){
			c.getService(LoggerService.class).getLogger(getClass()).warn("failed sge job "+record);
			throw new SkipRecord("Failed SGE job");
		}
		r.setProperty(BatchParser.SUCCESS_PROP, failed.intValue() == 0);
		
		r.setProperty(SGE_FAILED_PROP, failed);
		r.setProperty(SGE_EXIT_PROP,readInteger(args[12]));
		// wallclock, same as difference between start and end
		Integer wall = readInteger(args[13]);
		r.setProperty(SGE_WALLCLOCK_PROP, wall);

		// getrusage fields
		Double ru_utime = readDouble(args[14]);
		r.setProperty(SGE_UTIME_PROP,ru_utime);

		Double ru_stime = readDouble(args[15]);
		r.setProperty(SGE_STIME_PROP,ru_stime);

		Double ru_maxrss = readDouble(args[16]);
		r.setProperty(SGE_MAXRSS_PROP,ru_maxrss);
		Long ru_ixrss = readLong(args[17]);
		r.setProperty(SGE_IXRSS_PROP,ru_ixrss);
		Long ru_ismrss = readLong(args[18]);
		r.setProperty(SGE_ISMRSS_PROP,ru_ismrss);
		Long ru_idrss = readLong(args[19]);
		r.setProperty(SGE_IDRSS_PROP,ru_idrss);
		Long ru_isrss = readLong(args[20]);
		r.setProperty(SGE_ISRSS_PROP,ru_isrss);
		Long ru_minflt = readLong(args[21]);
		r.setProperty(SGE_MINFLT_PROP,ru_minflt);
		Long ru_majflt = readLong(args[22]);
		r.setProperty(SGE_MAJFLT_PROP,ru_majflt);
		Long ru_nswap = readLong(args[23]);
		r.setProperty(SGE_NSWAP_PROP,ru_nswap);
		Double ru_inblock = readDouble(args[24]);
		r.setProperty(SGE_INBLOCK_PROP,ru_inblock);
		Long ru_outblock = readLong(args[25]);
		r.setProperty(SGE_OUTBLOCK_PROP,ru_outblock);
		Long ru_msgsnd = readLong(args[26]);
		r.setProperty(SGE_MSGSND_PROP,ru_msgsnd);
		Long ru_msgrcv = readLong(args[27]);
		r.setProperty(SGE_MSGRCV_PROP,ru_msgrcv);
		Long ru_nsignals = readLong(args[28]);
		r.setProperty(SGE_NSIGNALS_PROP,ru_nsignals);
		Long ru_nvcsw = readLong(args[29]);
		r.setProperty(SGE_NVCSW_PROP,ru_nvcsw);
		Long ru_nivcsw = readLong(args[30]);
		r.setProperty(SGE_NIVCSW_PROP,ru_nivcsw);

		String project = args[31];
		r.setProperty(SGE_PROJECT_PROP, project);
		String dept = args[32];
		r.setProperty(SGE_DEPARTMENT_PROP, dept);
		String pe = args[33];
		r.setProperty(SGE_PE_PROP, pe);

		// SGE slots
		r.setProperty(SGE_SLOTS,readInteger(args[34]));

		// task id for an array job
		Integer task = readInteger(args[35]);
		r.setProperty(SGE_TASK_PROP,task);

		Double cpu = readDouble(args[36]);
		r.setProperty(SGE_CPU_TIME_PROP, cpu);

		// Gb seconds
		Double mem = readDouble(args[37]);
		r.setProperty(SGE_MEMORY_PROP, mem);
		// total data transfered in io input/output
		Double io = readDouble(args[38]);
		r.setProperty(SGE_IODATA_PROP, io);
		String category = args[39].trim();
		r.setProperty(SGE_CATEGORY_PROP, category);
		//io wait time seconds
		Double iow = readDouble(args[40]);
		r.setProperty(SGE_IOWAIT_PROP, iow);
		// process rank for mpi tasks started through sge
		String pe_taskid = args[41].trim();
		is_sub_task = pe_taskid.compareToIgnoreCase("NONE") != 0;
		if( skip_sub_task && is_sub_task){
			throw new SkipRecord("SGE sub-task");
		}
		r.setProperty(SGE_PE_TASK_PROP,pe_taskid);
		// maximum virtual memory in bytes
		Double maxvmem = readDouble(args[42]);
		r.setProperty(SGE_VMEM_PROP, maxvmem);

		
		// The below are new properties in SGE 6.2 and above so we include them 
		// but check that we are not parsing data from older versions of SGE to 
		// ensure it is still compatible.
		// AdrianJ 20/05/2010
		if(args.length > 43){
			Integer arid = readInteger(args[43]);
			r.setProperty(SGE_ARID_PROP, arid);
		}

		if(args.length > 44){
			Integer ar_time = readInteger(args[44]);
			r.setProperty(SGE_AR_TIME_PROP, ar_time);
		}

		return true;
	}

	@Override
	public void startParse(PropertyContainer defaults) throws DataException,
	InvalidExpressionException {
		String machine_name = defaults.getProperty(StandardProperties.MACHINE_NAME_PROP);
		if( machine_name != null ){
			skip_inner_job = c.getBooleanParameter("skip_inner_job."+machine_name, DEFAULT_SKIP_INNER);
			skip_failed_job = c.getBooleanParameter("skip_failed_job."+machine_name, DEFAULT_SKIP_FAILED);
			skip_sub_task = c.getBooleanParameter("skip_sub_task."+machine_name, DEFAULT_SKIP_SUBTASK);
		}
	}

	@Override
	public String endParse() {
		skip_inner_job=DEFAULT_SKIP_INNER;
		skip_failed_job=DEFAULT_SKIP_FAILED;
		skip_sub_task=DEFAULT_SKIP_SUBTASK;
		return "";
	}

	public AppContext getContext() {
		return c;
	}

	@Override
	public PropertyFinder initFinder(AppContext conn, PropertyFinder prev, String table){
		MultiFinder finder=new MultiFinder();
		finder.addFinder(StandardProperties.time);
		finder.addFinder(StandardProperties.base);
		finder.addFinder(batch);
		finder.addFinder(sge);
		this.table=table;
		return finder;
	}

	private PropExpressionMap derived=null;
	/* (non-Javadoc)
	 * @see uk.ac.ed.epcc.safe.accounting.parsers.BatchParser#getDerivedProperties()
	 */
	@Override
	public PropExpressionMap getDerivedProperties(PropExpressionMap previous) {
		if( derived == null){
			derived = super.getDerivedProperties(previous);
			// convert SGE_JOB_ID to string for JOB_ID
			try {
				derived.put(JOB_ID_PROP, new StringPropExpression<Number>(SGE_JOB_ID));
				addAlias(derived,SGE_QUEUE_PROP,QUEUE_PROP);
				addAlias(derived, StandardProperties.GROUPNAME_PROP, SGE_GROUPNAME_PROP);
				addAlias(derived, SGE_USERNAME_PROP, StandardProperties.USERNAME_PROP);
				addAlias(derived, SGE_JOBNAME_PROP, JOB_NAME_PROP);
				if( getContext().getBooleanParameter("sge_parser.account_via_project."+table, false)){
					addAlias(derived,SGE_PROJECT_PROP,ACCOUNT_PROP);
				}else{
					addAlias(derived, SGE_ACCOUNT_PROP, ACCOUNT_PROP);
				}
				if( getContext().getBooleanParameter("sge_parser.wallclock_runtime."+table, true)){
					// Use the SGE computed wallclock for charging calc
					derived.put( StandardProperties.RUNTIME_PROP, new BinaryPropExpression(SGE_WALLCLOCK_PROP, Operator.MUL,
							new ConstPropExpression<Long>(Long.class,1000L)));
				}

				addAlias(derived, SGE_SUBMITTED_PROP, SUBMITTED_PROP);
				addAlias(derived,SGE_STARTED_PROP,StandardProperties.STARTED_PROP);
				addAlias(derived, SGE_ENDED_PROP, StandardProperties.ENDED_PROP);
				addAlias(derived, SGE_EXIT_PROP, StandardProperties.EXIT_PROP);
				addAlias(derived, SGE_WALLCLOCK_PROP, WALLCLOCK_PROP);
				addAlias(derived, SGE_SLOTS, PROC_COUNT_PROP);
				addAlias(derived, SGE_CPU_TIME_PROP,StandardProperties.CPU_TIME_PROP);
			} catch (PropertyCastException e) {
				throw new ConsistencyError("Error aliasing JOB_ID to SGE_JOB_ID",e);
			}
		}
		return new PropExpressionMap(derived);
	}

	private <T> void addAlias(PropExpressionMap result,PropertyTag<T> a, PropertyTag<T> b)
	throws PropertyCastException {
		result.put(a,b);
		result.put(b,a);
	}


}