package uk.ac.ed.epcc.safe.accounting.parsers;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Set;
import java.util.regex.Pattern;

import uk.ac.ed.epcc.safe.accounting.expr.DerivedPropertyMap;
import uk.ac.ed.epcc.safe.accounting.expr.DurationSecondsPropExpression;
import uk.ac.ed.epcc.safe.accounting.expr.PropExpressionMap;
import uk.ac.ed.epcc.safe.accounting.expr.PropertyCastException;
import uk.ac.ed.epcc.safe.accounting.parsers.value.DateParser;
import uk.ac.ed.epcc.safe.accounting.parsers.value.IntegerParser;
import uk.ac.ed.epcc.safe.accounting.parsers.value.SlurmDurationParser;
import uk.ac.ed.epcc.safe.accounting.parsers.value.ValueParseException;
import uk.ac.ed.epcc.safe.accounting.properties.MultiFinder;
import uk.ac.ed.epcc.safe.accounting.properties.PropertyFinder;
import uk.ac.ed.epcc.safe.accounting.properties.PropertyMap;
import uk.ac.ed.epcc.safe.accounting.properties.PropertyRegistry;
import uk.ac.ed.epcc.safe.accounting.properties.PropertyTag;
import uk.ac.ed.epcc.safe.accounting.properties.StandardProperties;
import uk.ac.ed.epcc.safe.accounting.update.AccountingParseException;
import uk.ac.ed.epcc.safe.accounting.update.AutoTable;
import uk.ac.ed.epcc.safe.accounting.update.BatchParser;
import uk.ac.ed.epcc.safe.accounting.update.OptionalTable;
import uk.ac.ed.epcc.webapp.AppContext;
import uk.ac.ed.epcc.webapp.Contexed;
import uk.ac.ed.epcc.webapp.model.data.ConfigParamProvider;
import uk.ac.ed.epcc.webapp.model.data.Duration;
/** Simple field based parser of the SLURM accounting output
 * 
 * <pre>
 sacct -a -p -o \
"jobid,jobidraw,user,group,account,jobname,partition,qos,start,end,submit,alloccpus,allocgres,NNodes,Elapsed,timelimit,state"
  </pre>
 * 
 * @author spb
 *
 */
public class OxfordSlurmParser extends BatchParser implements  Contexed,ConfigParamProvider {
	private static final String SKIP_CANCELLED_SUFFIX = ".skip_cancelled";

	
	public OxfordSlurmParser(AppContext conn) {
		super(conn);
	}
	public static  final PropertyRegistry slurm = 
			new PropertyRegistry("slurm","The SLURM batch properties");

	@AutoTable(unique=true)
	public static final PropertyTag<String> JOB_ID = new PropertyTag<>(slurm, "JobID", String.class,"Slurm JOB id");
	@AutoTable
	public static final PropertyTag<String> JOB_ID_RAW = new PropertyTag<>(slurm, "JobIDRaw", String.class,"RAW Slurm JOB id");
	@AutoTable
	public static final PropertyTag<String> UserName = new PropertyTag<>(slurm, "User", String.class,"User name");
	@AutoTable
	public static final PropertyTag<String> GroupName = new PropertyTag<>(slurm, "GroupName", String.class,"Group name");
	@AutoTable(length=64)
	public static final PropertyTag<String> AccountName = new PropertyTag<>(slurm, "Account", String.class,"Account name");
	@AutoTable(unique=true,length=200)
	public static final PropertyTag<String> JobName = new PropertyTag<>(slurm, "JobName", String.class,"Job name");
	@AutoTable
	public static final PropertyTag<String> Partition = new PropertyTag<>(slurm, "Partition", String.class);
	@AutoTable
	public static final PropertyTag<String> QOS = new PropertyTag<>(slurm, "QOS", String.class,"Quality of service");

	@AutoTable(unique=true)
	public static final PropertyTag<Date> Start = new PropertyTag<>(slurm,"Start",Date.class,"Job start");
	@AutoTable
	public static final PropertyTag<Date> End = new PropertyTag<>(slurm,"End",Date.class,"Job end");
	@AutoTable
	public static final PropertyTag<Date> Submit = new PropertyTag<>(slurm,"Submit",Date.class,"Job submit");

	@AutoTable
	public static final PropertyTag<Integer> ALLOC_CPUS = new PropertyTag<>(slurm, "AllocCPUS", Integer.class,"Allocated cpus");
	@AutoTable
	public static final PropertyTag<String> ALLOC_GRES = new PropertyTag<>(slurm, "AllocGRES", String.class,"Allocated GRES");
	@AutoTable
	public static final PropertyTag<Integer> NNODE = new PropertyTag<>(slurm, "NNodes", Integer.class,"Nodes");
	@AutoTable
	public static final PropertyTag<Duration> ELAPSED_PROP = new PropertyTag<>(slurm, "Elapsed", Duration.class);
	@AutoTable
	public static final PropertyTag<Duration> TIMELIMIT_PROP = new PropertyTag<>(slurm, "Timelimit", Duration.class);
	@AutoTable
	public static final PropertyTag<String> STATE_PROP = new PropertyTag<>(slurm, "State", String.class);
	@OptionalTable
    public static final PropertyTag<Integer> ALOCGRES_GPU_PROP = new PropertyTag<>(slurm,"AllocGRESgpu",Integer.class,"gres/gpu field");
  
	// not static as SimpleDateFormat not thread safe
	public final DateParser SLURM_DATE_PARSER = new DateParser(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss"));
	private static MakerMap GRES = new MakerMap();
	private static final Pattern ATTR_PATTERN=Pattern.compile("(\\w+):([^,\\s]*)");
	private static ContainerEntryMaker gres_parser = new AbstractNestedContainerEntryMaker(GRES) {
		/**
		 * @return the attrPattern
		 */
		protected Pattern getAttrPattern() {
			return ATTR_PATTERN;
		}
		
	};
	static {
        GRES.addParser("gpu", ALOCGRES_GPU_PROP, IntegerParser.PARSER);
	}
	private boolean skip_cancelled=false;
	private String table;
	
	@Override
	public boolean parse(DerivedPropertyMap map, String record) throws AccountingParseException {
		String fields[] = record.trim().split("\\|");
		String state;
		int pos=0;
	    if( fields.length != 17){
	    	throw new AccountingParseException("Wrong number of fields "+fields.length);
	    }
	    // check for header line
	    if( fields[0].equals("JobID")){
	    	return false;
	    }
	    
	    // Skip sub-jobs
	    if( fields[0].contains(".")){
	    	// its a sub-job
	    	return false;
	    }
	    
	    try{
		map.setProperty(JOB_ID,fields[pos++]);
		map.setProperty(JOB_ID_RAW,fields[pos++]);
		map.setProperty(UserName, fields[pos++]);
		map.setProperty(GroupName, fields[pos++]);
		map.setProperty(AccountName, fields[pos++]);
		map.setProperty(JobName, fields[pos++]);
		map.setProperty(Partition, fields[pos++]);
		map.setProperty(QOS, fields[pos++]);
		parseDate(map,Start, fields[pos++]);
		parseDate(map,End, fields[pos++]);
		parseDate(map,Submit, fields[pos++]);
		parseInteger(map,ALLOC_CPUS,fields[pos++]);
		String gres= fields[pos++];
		map.setProperty(ALLOC_GRES,gres);
		gres_parser.setValue(map, gres);
		parseInteger(map,NNODE,fields[pos++]);
		parseDuration(map,ELAPSED_PROP,fields[pos++]);
		parseDuration(map,TIMELIMIT_PROP,fields[pos++]);
		map.setProperty(STATE_PROP, state=fields[pos++]);
		
		// Illegal states
		if( state.startsWith("RUNNING") || state.startsWith("PENDING") || state.startsWith("REQUEUED")){
			return false;
		}
		if( skip_cancelled && state.startsWith("CANCELLED")){
			return false;
		}
	    }catch(NumberFormatException e){
	    	throw new AccountingParseException(e);
	    }catch( IllegalArgumentException e2){
	    	throw new AccountingParseException(e2);
	    }catch(ValueParseException e3){
	    	throw new AccountingParseException(e3);
	    }
		return true;
	}
	private void parseInteger(PropertyMap map, PropertyTag<Integer> tag, String field){
		if( field.length() > 0 ){
			map.setProperty(tag, new Integer(field));
		}
	}
	private void parseDate(PropertyMap map, PropertyTag<Date> tag, String field) throws ValueParseException{
		if( field.length() > 0 ){
			map.setProperty(tag, SLURM_DATE_PARSER.parse(field));
		}
	}
	private void parseDuration(PropertyMap map, PropertyTag<Duration> tag, String field) throws ValueParseException{
		if( field.length() > 0 ){
			map.setProperty(tag, SlurmDurationParser.PARSER.parse(field));
		}
	}
	
	@Override
	public PropExpressionMap getDerivedProperties(PropExpressionMap previous) {
		PropExpressionMap derv = super.getDerivedProperties(previous);
		try {
			derv.peer(StandardProperties.STARTED_PROP, Start);
			derv.peer(StandardProperties.ENDED_PROP, End);
			derv.peer(BatchParser.SUBMITTED_PROP, Submit);
			derv.peer(BatchParser.ACCOUNT_PROP,AccountName);
			derv.peer(BatchParser.JOB_ID_PROP, JOB_ID);
			derv.peer(StandardProperties.USERNAME_PROP, UserName);
			derv.peer(StandardProperties.GROUPNAME_PROP, GroupName);
			
			derv.peer(BatchParser.PARTITION_PROP, Partition);
			derv.peer(BatchParser.NODE_COUNT_PROP, NNODE);
			derv.peer(BatchParser.PROC_COUNT_PROP, ALLOC_CPUS);
			derv.peer(BatchParser.JOB_NAME_PROP, JobName);
			derv.put(BatchParser.WALLCLOCK_PROP, new DurationSecondsPropExpression(ELAPSED_PROP));
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
		return finder;
	}
	@Override
	public void addConfigParameters(Set<String> params) {
		params.add(table+SKIP_CANCELLED_SUFFIX);
		
	}
}
