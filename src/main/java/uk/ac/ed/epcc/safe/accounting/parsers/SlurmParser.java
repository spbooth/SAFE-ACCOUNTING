package uk.ac.ed.epcc.safe.accounting.parsers;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Set;

import uk.ac.ed.epcc.safe.accounting.expr.DerivedPropertyMap;
import uk.ac.ed.epcc.safe.accounting.expr.DurationSecondsPropExpression;
import uk.ac.ed.epcc.safe.accounting.expr.LabelPropExpression;
import uk.ac.ed.epcc.safe.accounting.expr.PropExpressionMap;
import uk.ac.ed.epcc.safe.accounting.expr.PropertyCastException;
import uk.ac.ed.epcc.safe.accounting.parsers.value.BigDecimalParser;
import uk.ac.ed.epcc.safe.accounting.parsers.value.DateParser;
import uk.ac.ed.epcc.safe.accounting.parsers.value.DoubleParser;
import uk.ac.ed.epcc.safe.accounting.parsers.value.IntegerParser;
import uk.ac.ed.epcc.safe.accounting.parsers.value.SlurmDurationParser;
import uk.ac.ed.epcc.safe.accounting.parsers.value.SlurmMemoryParser;
import uk.ac.ed.epcc.safe.accounting.parsers.value.StringParser;
import uk.ac.ed.epcc.safe.accounting.parsers.value.ValueParser;
import uk.ac.ed.epcc.safe.accounting.parsers.value.ValueParserService;
import uk.ac.ed.epcc.safe.accounting.properties.InvalidPropertyException;
import uk.ac.ed.epcc.safe.accounting.properties.MultiFinder;
import uk.ac.ed.epcc.safe.accounting.properties.PropertyContainer;
import uk.ac.ed.epcc.safe.accounting.properties.PropertyFinder;
import uk.ac.ed.epcc.safe.accounting.properties.PropertyMap;
import uk.ac.ed.epcc.safe.accounting.properties.PropertyRegistry;
import uk.ac.ed.epcc.safe.accounting.properties.PropertyTag;
import uk.ac.ed.epcc.safe.accounting.properties.StandardProperties;
import uk.ac.ed.epcc.safe.accounting.update.AccountingParseException;
import uk.ac.ed.epcc.safe.accounting.update.AutoTable;
import uk.ac.ed.epcc.safe.accounting.update.BatchParser;
import uk.ac.ed.epcc.safe.accounting.update.FailProperty;
import uk.ac.ed.epcc.safe.accounting.update.OptionalTable;
import uk.ac.ed.epcc.webapp.AppContext;
import uk.ac.ed.epcc.webapp.Contexed;
import uk.ac.ed.epcc.webapp.config.FilteredProperties;
import uk.ac.ed.epcc.webapp.content.Labeller;
import uk.ac.ed.epcc.webapp.logging.Logger;
import uk.ac.ed.epcc.webapp.model.data.ConfigParamProvider;
import uk.ac.ed.epcc.webapp.model.data.Duration;
/** Generic field based parser of the SLURM accounting output
 * e.g.
 * <pre>
 sacct -a -p -o \
"jobid,jobidraw,user,group,account,jobname,partition,qos,start,end,submit,alloccpus,allocgres,NNodes,Elapsed,timelimit,state"
  </pre>
 * 
 * The parser is configured using the same names as the <b>sacct</b> fields. These correspond to the generated
 * propertytags. By default (if no format list is specified) the first line is assumed to be
 * a header line which is used to set the field values. This allows the uploading process to control the
 * fields sent.
 * 
 * 
 * <p>
 * Properties:
 * <ul>
 * <li><i>table</i><b>.parse_format</b> comma separated list of fields to parse (optional)</li>
 * <li><i>table</i><b>.skip_cancelled</b> skip jobs with cancelled status</li>
 * <li><b>slurm.<i>table</i>.resource<i>[.mode].name</i></b> type of nested resource parser
 * </ul>
 * 
 * The AllocGRES AllocTRES and ReqGRES fields can be parsed to additional properties by setting a
 * property of the form <b>slurm.<i>table</i>.resource<i>[.mode].name</i></b> (where mode is 
 * the field being parsed. This generated a property called <b>name<b> in a {@link PropertyRegistry} named
 * after the original field. The value of the property defines the {@link ValueParser} to use.
 * The mode value can be omitted to set a default for all of the parsed fields.
 * 
 * @author spb
 *
 */
public class SlurmParser extends BatchParser implements  Contexed,ConfigParamProvider {
	private static final String TRIVIAL_PARSE_SUFFIX = ".trivial_parse";
	private static final String PARSE_FORMAT_SUFFIX = ".parse.format";
	private static final String SKIP_CANCELLED_SUFFIX = ".skip_cancelled";
	private static final String SKIP_FAILED_SUFFIX = ".skip_failed";
	private static final String SKIP_NODE_FAILED_SUFFIX = ".skip_node_failed";
	private static final String SKIP_SUB_JOBS_SUFFIX = ".skip_sub_jobs";
	private static final String SKIP_TOP_JOBS_SUFFIX = ".skip_top_jobs";
	// Mak jobs within a reservation as sub-jobs (which will supress charging)
	private static final String MARK_RESERVATION_AS_SUB_JOB_SUFFIX = ".make_reservation_as_sub_job";
	private static final String DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss";
	
	public SlurmParser(AppContext conn) {
		super(conn);
	}
	public static  final PropertyRegistry slurm = 
			new PropertyRegistry("sacct","The SLURM sacct properties");

	@AutoTable
	public static final PropertyTag<Integer> ALLOC_CPUS = new PropertyTag<>(slurm, "AllocCPUS", Integer.class,"Allocated cpus");
	@OptionalTable(length=128)
    public static final PropertyTag<String> ALLOCGRES_PROP = new PropertyTag<>(slurm,"AllocGRES",String.class);
      @AutoTable
	public static final PropertyTag<Integer> ALLOC_NODES = new PropertyTag<>(slurm, "AllocNodes", Integer.class,"Allocated nodes");

    @OptionalTable(length=128)
    public static final PropertyTag<String> ALLOCTRES_PROP = new PropertyTag<>(slurm,"AllocTRES",String.class);
 
    @AutoTable(length=64)
	public static final PropertyTag<String> ACCOUNT_PROP = new PropertyTag<>(slurm, "Account", String.class,"Account the job ran under. ");

    @OptionalTable
    public static final PropertyTag<Integer> AssocID = new PropertyTag<>(slurm,"AssocID",Integer
    		.class,"Reference to the association of user, account and cluster. ");
    
	@OptionalTable
	public static final PropertyTag<Duration> AveCPU = new PropertyTag<>(slurm,"AveCPU",Duration.class,"Average (system + user) CPU time of all tasks in job.");
	
	@OptionalTable
	public static final PropertyTag<Long> AveCPUFreq = new PropertyTag<>(slurm,"AveCPUFreq",Long.class,"Average weighted CPU frequency of all tasks in job, in kHz.");
	
	@OptionalTable
	public static final PropertyTag<Long> AveDiskRead = new PropertyTag<>(slurm,"AveDiskRead",Long.class,"Average number of bytes read by all tasks in job.");
	@OptionalTable
	public static final PropertyTag<Long> AveDiskWrite = new PropertyTag<>(slurm,"AveDiskWrite",Long.class,"Average number of bytes written by all tasks in job.");
	@OptionalTable
	public static final PropertyTag<Long> AvePages = new PropertyTag<>(slurm,"AvePages",Long.class,"Average number of page faults of all tasks in job.");
	
	@OptionalTable
	public static final PropertyTag<Long> AveRSS = new PropertyTag<>(slurm,"AveRSS",Long.class,"Average resident set size of all tasks in job.");
	@OptionalTable
	public static final PropertyTag<Long> AveVMSize = new PropertyTag<>(slurm,"AveVMSize",Long.class,"Average Virtual Memory size of all tasks in job. ");
	@OptionalTable
	public static final PropertyTag<String> CLUSTER_PROP = new PropertyTag<>(slurm,"Cluster", String.class,"Cluster name.");
	@OptionalTable(length = 128)
	public static final PropertyTag<String> COMMENT_PROP = new PropertyTag<>(slurm,"Comment", String.class,"The job's comment string when the AccountingStoreJobComment parameter in the slurm.conf file is set (or defaults) to YES. The Comment string can be modified by invoking sacctmgr modify job or the specialized sjobexitmod command. ");
	
	// Note we have seen these values overflow a long so use double
	@OptionalTable
	public static final PropertyTag<Double> ConsumedEnergy = new PropertyTag<>(slurm,"ConsumedEnergy",Double.class,"Total energy consumed by all tasks in job, in joules. Note: Only in case of exclusive job allocation this value reflects the jobs' real energy consumption. ");
	@OptionalTable
	public static final PropertyTag<Double> ConsumedEnergyRaw = new PropertyTag<>(slurm,"ConsumedEnergyRaw",Double.class,"Total raw energy consumed by all tasks in job, in joules. Note: Only in case of exclusive job allocation this value reflects the jobs' real energy consumption. ");
	
	@OptionalTable
    public static final PropertyTag<Duration> CPU_TIME_PROP = new PropertyTag<>(slurm,"CPUTime",Duration.class,"Time used (Elapsed time * CPU count) by a job or step in HH:MM:SS format.");
   
	@OptionalTable
    public static final PropertyTag<Long> CPU_TIME_RAW_PROP = new PropertyTag<>(slurm,"CPUTimeRAW",Long.class,"Time used (Elapsed time * CPU count) by a job or step in cpu-seconds. ");
   
	@OptionalTable
	public static final PropertyTag<String> DerivedExitCode = new PropertyTag<>(slurm,"DerivedExitCode",String.class,"The highest exit code returned by the job's job steps (srun invocations). Following the colon is the signal that caused the process to terminate if it was terminated by a signal. The DerivedExitCode can be modified by invoking sacctmgr modify job or the specialized sjobexitmod command. ");
	
	@AutoTable
	public static final PropertyTag<Duration> ELAPSED_PROP = new PropertyTag<>(slurm, "Elapsed", Duration.class,"The jobs elapsed time.  [DD-[HH:]]MM:SS");
	@OptionalTable
	public static final PropertyTag<Date> ELIGIBLE_PROP = new PropertyTag<>(slurm,"Eligible",Date.class,"When the job became eligible to run");
	@AutoTable
	public static final PropertyTag<Date> END_PROP = new PropertyTag<>(slurm,"End",Date.class,"Job end");
	@AutoTable
	public static final PropertyTag<String> EXIT_CODE_PROP = new PropertyTag<>(slurm, "ExitCode", String.class,"The exit code returned by the job script or salloc, typically as set by the exit() function. Following the colon is the signal that caused the process to terminate if it was terminated by a signal. ");
	
	@OptionalTable
	public static final PropertyTag<Integer> EXIT_RETURN_PROP = new PropertyTag<>(slurm,"ExitCodeReturn",Integer.class,"The exit code returned by the job script or salloc, typically as set by the exit() function.");
	@OptionalTable
	public static final PropertyTag<Integer> EXIT_SIGNAL_PROP = new PropertyTag<>(slurm,"ExitCodeReturn",Integer.class,"The signal that caused the process to terminate if it was terminated by a signal.");

	
	@OptionalTable    
	public static final PropertyTag<Boolean> SUCCESS_PROP = new PropertyTag<>(slurm,"Success",Boolean.class,"Job did not return a failed state");
	
	@OptionalTable
	public static final PropertyTag<Boolean> NODE_FAIL_PROP = new FailProperty(slurm,"NodeFail","Did the job terminate due to a node failure");
	
	@OptionalTable
	public static final PropertyTag<Integer> GID_PROP = new PropertyTag<>(slurm,"GID",Integer.class,"The group identifier of the user who ran the job.");
	@AutoTable
	public static final PropertyTag<String> GROUP_PROP = new PropertyTag<>(slurm, "Group", String.class,"Group name");

	@AutoTable(unique=true,length=128)
	public static final PropertyTag<String> JOB_ID = new PropertyTag<>(slurm, "JobID", String.class,"Slurm JOB id");
	@AutoTable
	public static final PropertyTag<Boolean> SUB_JOB = new PropertyTag<>(slurm,"SubJob",Boolean.class,"Is this a slurm sub-job");
	@OptionalTable
	public static final PropertyTag<Boolean> SCRIPT_JOB = new PropertyTag<>(slurm,"ScriptJob",Boolean.class,"Is this the slurm batch script sub-job");
	@OptionalTable
	public static final PropertyTag<Boolean> EXTERN_JOB = new PropertyTag<>(slurm,"ExternJob",Boolean.class,"Is this the slurm extern sub-job");
	@OptionalTable(length=32)
	public static final PropertyTag<String> JOB_ID_BASE = new PropertyTag<>(slurm, "JobIDBase", String.class,"Slurm JOB id without any sub-job suffix");
	@OptionalTable(length=128)
	public static final PropertyTag<String> JOB_ID_RAW = new PropertyTag<>(slurm, "JobIDRaw", String.class,"RAW Slurm JOB id");
	@AutoTable(length=200)
	public static final PropertyTag<String> JOB_NAME_PROP = new PropertyTag<>(slurm, "JobName", String.class,"Job name");

	@OptionalTable
	public static final PropertyTag<String> LAYOUT_PROP = new PropertyTag<>(slurm, "Layout", String.class,"What the layout of a step was when it was running. ");

    @OptionalTable
    public static final PropertyTag<Long> MaxDiskRead = new PropertyTag<>(slurm,"MaxDiskRead", Long.class,"Maximum number of bytes read by all tasks in job. ");
    @OptionalTable
    public static final PropertyTag<String> MaxDiskReadNode = new PropertyTag<String>(slurm, "MaxDiskReadNode", String.class, "The node on which the maxdiskread occurred. ");
	@OptionalTable
	public static final PropertyTag<Integer> MaxDiskReadTask = new PropertyTag<Integer>(slurm,"MaxDiskReadTask",Integer.class,"The task ID where the maxdiskread occurred. ");
	@OptionalTable
    public static final PropertyTag<Long> MaxDiskWrite = new PropertyTag<>(slurm,"MaxDiskWrite", Long.class,"Maximum number of bytes written by all tasks in job. ");
    @OptionalTable
    public static final PropertyTag<String> MaxDiskWriteNode = new PropertyTag<String>(slurm, "MaxDiskWriteNode", String.class, "The node on which the maxdiskwrite occurred. ");
	@OptionalTable
	public static final PropertyTag<Integer> MaxDiskWriteTask = new PropertyTag<Integer>(slurm,"MaxDiskWriteTask",Integer.class,"The task ID where the maxdiskwrite occurred. ");
	
	@OptionalTable
	public static final PropertyTag<Long> MaxPages = new PropertyTag<>(slurm,"MaxPages",Long.class,"Maximum number of page faults of all tasks in job. ");
	@OptionalTable
    public static final PropertyTag<String> MaxPagesNode = new PropertyTag<String>(slurm, "MaxPagesNode", String.class, "The node on which the maxpages occurred. ");
	@OptionalTable
	public static final PropertyTag<Integer> MaxPagesTask = new PropertyTag<Integer>(slurm,"MaxPagesTask",Integer.class,"The task ID where the maxpages occurred. ");
	
	@OptionalTable
    public static final PropertyTag<Long> MaxRSS = new PropertyTag<>(slurm,"MaxRSS", Long.class,"Maximum resident set size of all tasks in job.");
    @OptionalTable
    public static final PropertyTag<String> MaxRSSNode = new PropertyTag<String>(slurm, "MaxRSSNode", String.class, "The node on which the maxrss occurred. ");
	@OptionalTable
	public static final PropertyTag<Integer> MaxRSSTask = new PropertyTag<Integer>(slurm,"MaxRSSTask",Integer.class,"The task ID where the maxrss occurred. ");
	
	@OptionalTable
    public static final PropertyTag<Long> MaxVMSize = new PropertyTag<>(slurm,"MaxVMSize", Long.class,"Maximum Virtual Memory size of all tasks in job.");
    @OptionalTable
    public static final PropertyTag<String> MaxVMSizeNode = new PropertyTag<String>(slurm, "MaxVMSizeNode", String.class, "The node on which the maxvmsize occurred. ");
	@OptionalTable
	public static final PropertyTag<Integer> MaxVMSizeTask = new PropertyTag<Integer>(slurm,"MaxVMSizeTask",Integer.class,"The task ID where the maxvmsize occurred. ");
	
	@OptionalTable
    public static final PropertyTag<Duration> MinCPU = new PropertyTag<>(slurm,"MinCPU", Duration.class,"Minimum (system + user) CPU time of all tasks in job. ");
    @OptionalTable
    public static final PropertyTag<String> MinCPUNode = new PropertyTag<String>(slurm, "MinCPUNode", String.class, "The node on which the mincpu occurred. ");
	@OptionalTable
	public static final PropertyTag<Integer> MinCPUTask = new PropertyTag<Integer>(slurm,"MinCPUTask",Integer.class,"The task ID where the mincpu occurred. ");
	@OptionalTable
	public static final PropertyTag<Integer> N_CPUS_PROP = new PropertyTag<>(slurm, "NCPUS", Integer.class,"Total number of CPUs allocated to the job. Equivalent to AllocCPUS.");
	
	@OptionalTable(length=128)
	public static final PropertyTag<String> NodeList = new PropertyTag<>(slurm,"NodeList",String.class,"List of nodes in job/step. ");
	@OptionalTable
    public static final PropertyTag<Integer> N_NODES_PROP = new PropertyTag<>(slurm, "NNodes", Integer.class,"Number of nodes in a job or step.");
	@AutoTable
    public static final PropertyTag<Integer> N_TASKS_PROP = new PropertyTag<>(slurm, "NTasks", Integer.class,"Total number of tasks in a job or step. ");
	@OptionalTable
	public static final PropertyTag<Integer> Priority = new PropertyTag<>(slurm,"Priority",Integer.class,"Slurm priority.");
	@AutoTable
	public static final PropertyTag<String> PARTITION_PROP = new PropertyTag<>(slurm, "Partition", String.class);
	@AutoTable
	public static final PropertyTag<String> QOS = new PropertyTag<>(slurm, "QOS", String.class,"Quality of service");
	@OptionalTable
	public static final PropertyTag<Integer> QOSRAW = new PropertyTag<>(slurm, "QOSRAW", Integer.class,"Numeric id of Quality of Service.");

	@OptionalTable
	public static final PropertyTag<Duration> RESERVED_PROP = new PropertyTag<>(slurm, "Reserved", Duration.class);
	
	@OptionalTable
	public static final PropertyTag<Long> ReqCPUFreq = new PropertyTag<>(slurm,"ReqCPUFreq",Long.class,"Requested CPU frequency for the step, in kHz.");
	@OptionalTable
	public static final PropertyTag<Long> ReqCPUFreqGov = new PropertyTag<>(slurm,"ReqCPUFreqGov",Long.class);
	@OptionalTable
	public static final PropertyTag<Long> ReqCPUFreqMax = new PropertyTag<>(slurm,"ReqCPUFreqMax",Long.class);
	@OptionalTable
	public static final PropertyTag<Long> ReqCPUFreqMin = new PropertyTag<>(slurm,"ReqCPUFreqMin",Long.class);

	@OptionalTable
	public static final PropertyTag<Integer> ReqCPUS = new PropertyTag<>(slurm,"ReqCPUS",Integer.class,"Number of requested CPUs. ");
	@OptionalTable(length=128)
	public static final PropertyTag<String> ReqGRES = new PropertyTag<>(slurm,"ReqGRES",String.class);
	
	@OptionalTable
	public static final PropertyTag<Long> REQ_MEM_PROP = new PropertyTag<>(slurm, "ReqMem", Long.class);
	@OptionalTable
	public static final PropertyTag<Integer> ReqNodes = new PropertyTag<>(slurm,"ReqNodes",Integer.class,"Requested minimum Node count for the job/step.");

	@AutoTable(length = 128)
	public static final PropertyTag<String> Reservation = new PropertyTag<>(slurm,"Reservation",String.class,"Reservation Name");
	@OptionalTable
	public static final PropertyTag<Integer> ReservationId = new PropertyTag<>(slurm,"ReservationId",Integer.class,"Reservation Id");
	@OptionalTable
	public static final PropertyTag<Duration> Reserved = new PropertyTag<>(slurm,"Reserved",Duration.class,"How much wall clock time was used as reserved time for this job.");
	@OptionalTable
	public static final PropertyTag<Duration> ResvCPU = new PropertyTag<>(slurm,"ResvCPU",Duration.class,"How many CPU seconds were used as reserved time for this job. ");
	
	@OptionalTable
	public static final PropertyTag<Long> ResvCPURAW = new PropertyTag<>(slurm,"ResvCPURAW",Long.class,"How many CPU seconds were used as reserved time for this job. Format is in processor seconds. ");
	
	@AutoTable(unique=true)
	public static final PropertyTag<Date> START_PROP = new PropertyTag<>(slurm,"Start",Date.class,"Job start");
	@OptionalTable
	public static final PropertyTag<String> STATE_PROP = new PropertyTag<>(slurm, "State", String.class);
	@AutoTable
	public static final PropertyTag<Date> SUBMIT_PROP = new PropertyTag<>(slurm,"Submit",Date.class,"Job submit");
	@OptionalTable
	public static final PropertyTag<Duration> Suspended = new PropertyTag<>(slurm, "Suspended", Duration.class);
	@OptionalTable
	public static final PropertyTag<Duration> SystemCPU = new PropertyTag<>(slurm, "SystemCPU", Duration.class);

	@AutoTable
	public static final PropertyTag<Duration> TIME_LIMIT_PROP = new PropertyTag<>(slurm, "Timelimit", Duration.class);

	@OptionalTable
	public static final PropertyTag<Integer> TimelimitRaw = new PropertyTag<>(slurm,"TimelimitRaw",Integer.class,"What the timelimit was/is for the job. Format is in number of minutes. ");
	
	@OptionalTable
	public static final PropertyTag<Duration> TotalCPU = new PropertyTag<>(slurm, "TotalCPU", Duration.class,"The sum of the SystemCPU and UserCPU time used by the job or job step. The total CPU time of the job may exceed the job's elapsed time for jobs that include multiple job steps. ");

	
	
	@OptionalTable
	public static final PropertyTag<Integer> UID = new PropertyTag<>(slurm,"UID",Integer.class);
	
	@AutoTable
	public static final PropertyTag<String> USER_PROP = new PropertyTag<>(slurm, "User", String.class,"User name");
	
	@OptionalTable
	public static final PropertyTag<Integer> UserCPU = new PropertyTag<>(slurm,"UserCPU",Integer.class,"The amount of user CPU time used by the job or job step.");
	
	@OptionalTable
	public static final PropertyTag<String> WCKey = new PropertyTag<>(slurm,"WCKey",String.class,"Workload Characterization Key. Arbitrary string for grouping orthogonal accounts together. ");
	
	@OptionalTable
	public static final PropertyTag<Integer> WCKeyID = new PropertyTag<>(slurm,"WCKeyID",Integer.class,"Reference to the wckey. ");
	private  final MakerMap SLURM_ATTRIBUTES = new MakerMap();
	public static class SlurmIDParser implements ContainerEntryMaker{

		@Override
		public void setValue(PropertyContainer container, String valueString) throws IllegalArgumentException,
				InvalidPropertyException, NullPointerException, AccountingParseException {
			
			container.setProperty(JOB_ID, valueString);
			boolean sub_job = valueString.contains(".");
			container.setProperty(SUB_JOB, sub_job);
			if( sub_job) {
				int pos = valueString.indexOf(".");
				String base = valueString.substring(0, pos);
				String suffix = valueString.substring(pos+1);
				container.setProperty(JOB_ID_BASE, base);
				container.setProperty(SCRIPT_JOB, suffix.equals("batch"));
				container.setProperty(EXTERN_JOB, suffix.equals("extern"));
			}else {
				container.setProperty(SCRIPT_JOB, false);
				container.setProperty(EXTERN_JOB, false);
				container.setProperty(JOB_ID_BASE, valueString);
			}
		}
	}
	public static class SlurmExitParser implements ContainerEntryMaker{

			@Override
			public void setValue(PropertyContainer container, String valueString) throws InvalidPropertyException {
				
				container.setProperty(EXIT_CODE_PROP, valueString);
				if( valueString == null || valueString.isEmpty()) {
					return;
				}
				int pos = valueString.indexOf(':');
				if( pos < 0 ) {
					return;
				}
				
				int exit = Integer.valueOf(valueString.substring(0, pos));
				int sig = Integer.valueOf(valueString.substring(pos+1));
				container.setProperty(EXIT_RETURN_PROP, exit);
				container.setProperty(EXIT_SIGNAL_PROP, sig);
				
			}

		@Override
		public void setValue(PropertyMap map, String valueString)
				 {
			map.setProperty(EXIT_CODE_PROP, valueString);
			if( valueString == null || valueString.isEmpty()) {
				return;
			}
			int pos = valueString.indexOf(':');
			if( pos < 0 ) {
				return;
			}
			
			int exit = Integer.valueOf(valueString.substring(0, pos));
			int sig = Integer.valueOf(valueString.substring(pos+1));
			map.setProperty(EXIT_RETURN_PROP, exit);
			map.setProperty(EXIT_SIGNAL_PROP, sig);
			
		}
		
	}
	public static class ReturnLabeller implements Labeller<String,Integer>{

		@Override
		public Class<Integer> getTarget() {
			return Integer.class;
		}

		@Override
		public Integer getLabel(AppContext conn, String key) {
			if( key == null ) {
				return null;
			}
			int pos = key.indexOf(':');
			if( pos < 1) {
				return null;
			}
			try {
				return Integer.valueOf(key.substring(0,pos));
			}catch(Exception e) {
				return null;
			}
		}

		@Override
		public boolean accepts(Object o) {
			return o instanceof String;
		}
		
	}
	public static class SignalLabeller implements Labeller<String,Integer>{

		@Override
		public Class<Integer> getTarget() {
			return Integer.class;
		}

		@Override
		public Integer getLabel(AppContext conn, String key) {
			if( key == null ) {
				return null;
			}
			int pos = key.indexOf(':');
			if( pos < 1) {
				return null;
			}
			try {
				return Integer.valueOf(key.substring(pos+1));
			}catch(Exception e) {
				return null;
			}
		}

		@Override
		public boolean accepts(Object o) {
			return o instanceof String;
		}
		
	}
	// not static as SimpleDateFormat not thread safe
	public final DateParser SLURM_DATE_PARSER = new DateParser(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss"));
	
	
	
	private boolean skip_cancelled=false;
	private boolean skip_failed=false;
	private boolean skip_node_failed=false;
	private boolean skip_sub_jobs=true; // usually want sub jobs to a different table
	private boolean skip_top_jobs=false;
	private boolean trivial_parse=false; // Just accept all records without parsing the text, this is for a parser invoked from a SubJobPolicy
	                                  // though the additional saving is small it means we don't need to have the fields set statically
									  // as a sanity check it checks that a JOB_ID parameter is already set in the record
	                                  // this is so direct parse to a sub-job table will also be possible.
	private boolean mark_reservation_as_subjobs=false;
	private String table;
	private String tags[];
	private int count;
	
	


	@Override
	public boolean parse(DerivedPropertyMap map, String record) throws AccountingParseException {
		if( trivial_parse  && map.getProperty(JOB_ID) != null) {
			return true;
		}
		
		String fields[] = record.trim().split("\\|",-1);
		if( tags == null && count==0) {
			// set tags from header;
			tags=fields;
			return false;
		}
		
		if( tags == null || tags.length == 0) {
			throw new AccountingParseException("No parse fields configured");
		}
		
	    if( fields.length != tags.length){
	    	throw new AccountingParseException("Wrong number of fields "+fields.length);
	    }
	    // check for header line
	    if( fields[0].equals(tags[0])){
	    	return false;
	    }
	  
	    
	    
	    try{
	    	for(int i=0; i< tags.length ; i++) {
	    		String dat = fields[i];
	    		if(dat != null && ! dat.isEmpty() && ! dat.equalsIgnoreCase("Unknown")) {
	    			ContainerEntryMaker maker = SLURM_ATTRIBUTES.get(tags[i]);
	    			if( maker != null ) {
	    				maker.setValue(map, dat);
	    			}else {
	    				//getLogger().debug("Not parsing field "+tags[i]);
	    			}
	    		}
	    	}
		    String state = map.getProperty(STATE_PROP);
		
		// Illegal states
		if( state == null || state.startsWith("RUNNING") || state.startsWith("PENDING") || state.startsWith("REQUEUED")){
			return false;
		}
		if( skip_cancelled && state.startsWith("CANCELLED")){
			return false;
		}
		if(state.startsWith("FAILED")) {
			if( skip_failed ){
				return false;
			}
			map.setOptionalProperty(SUCCESS_PROP, Boolean.FALSE);
		}else {
			map.setOptionalProperty(SUCCESS_PROP, Boolean.TRUE);
		}
		if( state.startsWith("NODE_FAIL" )){
			map.setOptionalProperty(NODE_FAIL_PROP, Boolean.TRUE);
			map.setOptionalProperty(SUCCESS_PROP, Boolean.FALSE); // should not count as success
			if( skip_node_failed) {
				return false;
			}
		}else {
			map.setOptionalProperty(NODE_FAIL_PROP, Boolean.FALSE);
		}
		Boolean sub_job = map.getProperty(SUB_JOB);
		if( skip_sub_jobs && sub_job != null && sub_job) {
			return false;
		}
		if( skip_top_jobs && (sub_job == null || ! sub_job)) {
			return false;
		}
		
		Integer n  = map.getProperty(ReservationId);
		if( mark_reservation_as_subjobs && n != null ) {
			// Do this after skip processing. This allows us
			// to only record reservation jobs as sub-jobs
			map.setOptionalProperty(SUB_JOB, Boolean.TRUE);
		}
	    }catch(NumberFormatException e){
	    	throw new AccountingParseException(e);
	    }catch( IllegalArgumentException e2){
	    	throw new AccountingParseException(e2);
	    }catch(AccountingParseException e3){
	    	throw e3;
	    }
	    count++;
		return true;
	}
	
	
	@Override
	public PropExpressionMap getDerivedProperties(PropExpressionMap previous) {
		PropExpressionMap derv = super.getDerivedProperties(previous);
		try {
			derv.peer(StandardProperties.STARTED_PROP, START_PROP);
			derv.peer(StandardProperties.ENDED_PROP, END_PROP);
			derv.peer(BatchParser.SUBMITTED_PROP, SUBMIT_PROP);
			derv.peer(BatchParser.ACCOUNT_PROP,ACCOUNT_PROP);
			derv.peer(BatchParser.JOB_ID_PROP, JOB_ID);
			derv.peer(BatchParser.SUBJOB_PROP,SUB_JOB);
			derv.peer(StandardProperties.USERNAME_PROP, USER_PROP);
			derv.peer(StandardProperties.GROUPNAME_PROP, GROUP_PROP);
			
			derv.peer(BatchParser.PARTITION_PROP, PARTITION_PROP);
			derv.peer(BatchParser.RESERVATION_NAME_PROP, Reservation);
			
			// we expect the Alloc variants to be used so we alias these to 
			// BatchParser generics
			// add a one way aliases so all 3 can be resolved if only
			// one is defined
			derv.peer(BatchParser.NODE_COUNT_PROP, ALLOC_NODES);
			derv.put(ALLOC_NODES, N_NODES_PROP); 
			derv.put(N_NODES_PROP, BatchParser.NODE_COUNT_PROP);
			derv.peer(BatchParser.PROC_COUNT_PROP, ALLOC_CPUS);
			derv.put(ALLOC_CPUS, N_CPUS_PROP);
			derv.put(N_CPUS_PROP, BatchParser.PROC_COUNT_PROP);
			
			derv.peer(BatchParser.TASK_COUNT_PROP, N_TASKS_PROP);
			
			derv.peer(BatchParser.JOB_NAME_PROP, JOB_NAME_PROP);
			derv.put(BatchParser.WALLCLOCK_PROP, new DurationSecondsPropExpression(ELAPSED_PROP));
			derv.peer(BatchParser.QUEUE_PROP,QOS);
			
			derv.peer(BatchParser.SUCCESS_PROP,SUCCESS_PROP);
			derv.peer(BatchParser.FAULT_PROP,NODE_FAIL_PROP);
			
			// support extracting return/signal in code
			derv.put(EXIT_RETURN_PROP, new LabelPropExpression<String, Integer>(new ReturnLabeller(), EXIT_CODE_PROP));
			derv.put(EXIT_SIGNAL_PROP, new LabelPropExpression<String, Integer>(new SignalLabeller(), EXIT_CODE_PROP));
		} catch (PropertyCastException e) {
			getLogger().error("Error setting standard derived props",e);
		}
		return derv;
	}
	/* (non-Javadoc)
	 * @see uk.ac.ed.epcc.safe.accounting.update.BaseParser#initFinder(uk.ac.ed.epcc.webapp.AppContext, uk.ac.ed.epcc.safe.accounting.properties.PropertyFinder, java.lang.String)
	 */
	@Override
	public PropertyFinder initFinder( PropertyFinder prev, String table) {
		
		MultiFinder finder = new MultiFinder();
		finder.addFinder(slurm);
		finder.addFinder(batch);
		finder.addFinder(StandardProperties.time);
		finder.addFinder(StandardProperties.base);
		this.table=table;
		skip_cancelled = conn.getBooleanParameter(table+SKIP_CANCELLED_SUFFIX, false);
		skip_failed = conn.getBooleanParameter(table+SKIP_FAILED_SUFFIX, false);
		skip_node_failed = conn.getBooleanParameter(table+SKIP_NODE_FAILED_SUFFIX, false);
		skip_sub_jobs = conn.getBooleanParameter(table+SKIP_SUB_JOBS_SUFFIX, true);
		skip_top_jobs = conn.getBooleanParameter(table+SKIP_TOP_JOBS_SUFFIX, false);
		trivial_parse = conn.getBooleanParameter(table+TRIVIAL_PARSE_SUFFIX, false);
		mark_reservation_as_subjobs = conn.getBooleanParameter(table+MARK_RESERVATION_AS_SUB_JOB_SUFFIX, false);
		Logger log = getLogger();
		for(PropertyTag tag : slurm) {

			log.debug("Setting maker for tag "+tag.getFullName());


			if( tag.getTarget() == String.class) {
				SLURM_ATTRIBUTES.addParser(tag, StringParser.PARSER);
			}else if( tag.getTarget() == Date.class) {
				SLURM_ATTRIBUTES.addParser(tag, SLURM_DATE_PARSER);
			}else if( tag.getTarget().isAssignableFrom(Duration.class)) {
				SLURM_ATTRIBUTES.addParser(tag, SlurmDurationParser.PARSER);
			}else if( tag.getTarget() == Long.class) {
				// the memory parser can parse bare long values if necessary
				// but most slurm fields can user K/M/G formatting
				SLURM_ATTRIBUTES.addParser(tag, SlurmMemoryParser.PARSER);
			}else if( tag.getTarget() == Integer.class) {
				SLURM_ATTRIBUTES.addParser(tag, IntegerParser.PARSER);
			}else if( tag.getTarget() == BigDecimal.class) {
				SLURM_ATTRIBUTES.addParser(tag, BigDecimalParser.PARSER);
			}else if( Number.class.isAssignableFrom(tag.getTarget())) {
				SLURM_ATTRIBUTES.addParser(tag, DoubleParser.PARSER);
			}

		}
		// detect sub-jobs
		SLURM_ATTRIBUTES.put(JOB_ID.getName(), new SlurmIDParser());
		SLURM_ATTRIBUTES.put(EXIT_CODE_PROP.getName(), new SlurmExitParser());
       
        PropertyTag res[] = {ALLOCGRES_PROP, ALLOCTRES_PROP, ReqGRES};
        ValueParserService serv = getContext().getService(ValueParserService.class);
        for(PropertyTag t : res) {
        	 FilteredProperties props = new FilteredProperties(getContext().getProperties(), "slurm."+table+".resource",t.getName());
        	PropertyRegistry reg = new PropertyRegistry(t.getName(), "Sub properties for "+t.getFullName());
        	MakerMap map = new MakerMap();
        	for(String name : props.names()) {
        		String type = props.getProperty(name);
        		ValueParser parser = serv.getValueParser(type);
        		if( parser != null ) {
        			PropertyTag tag = new PropertyTag(reg,t.getName()+name,parser.getType());
        			map.addParser(name,tag, parser);
        		}
        	}
        	SLURM_ATTRIBUTES.put(t.getName(), new NestedContainerEntryMaker(t, map));
        	finder.addFinder(reg);
        }
        
		
		return finder;
	}
	@Override
	public void addConfigParameters(Set<String> params) {
		params.add(table+SKIP_CANCELLED_SUFFIX);
		params.add(table+SKIP_FAILED_SUFFIX);
		params.add(table+SKIP_NODE_FAILED_SUFFIX);
		params.add(table+SKIP_SUB_JOBS_SUFFIX);
		params.add(table+SKIP_TOP_JOBS_SUFFIX);
		params.add(table+PARSE_FORMAT_SUFFIX);
		params.add(table+TRIVIAL_PARSE_SUFFIX);
		
	}


	@Override
	public String endParse() {
		tags=null;
		return "";
	}


	@Override
	public void startParse(PropertyContainer staticProps) throws Exception {
		super.startParse(staticProps);
		String list = conn.getInitParameter(table+PARSE_FORMAT_SUFFIX);
		if( list != null ) {
			tags = list.split("\\s*,\\s*");
		}else {
			tags = null;
	        				
		}
		count=0;
	}
}
