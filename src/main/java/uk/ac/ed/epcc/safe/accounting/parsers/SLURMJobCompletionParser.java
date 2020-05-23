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

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import uk.ac.ed.epcc.safe.accounting.expr.PropExpressionMap;
import uk.ac.ed.epcc.safe.accounting.expr.PropertyCastException;
import uk.ac.ed.epcc.safe.accounting.parsers.value.DateParser;
import uk.ac.ed.epcc.safe.accounting.parsers.value.IntegerParser;
import uk.ac.ed.epcc.safe.accounting.parsers.value.SlurmDurationParser;
import uk.ac.ed.epcc.safe.accounting.parsers.value.StringParser;
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
import uk.ac.ed.epcc.safe.accounting.update.OptionalTable;
import uk.ac.ed.epcc.webapp.AppContext;
import uk.ac.ed.epcc.webapp.model.data.Duration;
/** PArser for the 
 * 
 * @author Stephen Booth
 *
 */
public class SLURMJobCompletionParser extends AbstractKeyPairParser {
	private static final String SKIP_CANCELLED_SUFFIX = ".skip_cancelled";

	public static final DateParser SLURM_DATE_PARSER = new DateParser(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss"));
	
    public static final PropertyRegistry slurm_reg = new PropertyRegistry("slurmjob", "slurm Job completion properties");
    
    @AutoTable(unique=true)
    public static final PropertyTag<String> JOB_ID_PROP = new PropertyTag<>(slurm_reg, "JobId", String.class);
	
    @AutoTable
	public static final PropertyTag<String> USER_PROP = new PropertyTag<>(slurm_reg, "User", String.class);
    public static final PropertyTag<Integer> UID_PROP = new PropertyTag<>(slurm_reg,"UID",Integer.class);
   
    
    @AutoTable
	public static final PropertyTag<String> GROUP_PROP = new PropertyTag<>(slurm_reg, "Group", String.class);
    public static final PropertyTag<Integer> GUID_PROP = new PropertyTag<>(slurm_reg,"GUID",Integer.class);
    
    @AutoTable(length=128)
   	public static final PropertyTag<String> JOB_NAME_PROP = new PropertyTag<>(slurm_reg, "Name", String.class);
    
    @AutoTable
	public static final PropertyTag<String> STATE_PROP = new PropertyTag<>(slurm_reg, "JobState", String.class);
    @AutoTable
  	public static final PropertyTag<String> PARTITION_PROP = new PropertyTag<>(slurm_reg, "Partition", String.class);
    @AutoTable
   	public static final PropertyTag<Duration> TIME_LIMIT_PROP = new PropertyTag<>(slurm_reg, "TimeLimit", Duration.class);
    @AutoTable(unique=true)
  	public static final PropertyTag<Date> START_PROP = new PropertyTag<>(slurm_reg, "StartTime", Date.class);
    @AutoTable
   	public static final PropertyTag<Date> END_PROP = new PropertyTag<>(slurm_reg, "EndTime", Date.class);
	public static final PropertyTag<String> NODE_LIST_PROP = new PropertyTag<>(slurm_reg, "NodeList", String.class);
	@AutoTable
	public static final PropertyTag<Integer> N_NODES_PROP = new PropertyTag<>(slurm_reg, "NodeCnt", Integer.class);
	@AutoTable
	public static final PropertyTag<Integer> PROC_COUNT_PROP = new PropertyTag<>(slurm_reg, "ProcCnt", Integer.class);
	public static final PropertyTag<String> WORK_DIR_PROP = new PropertyTag<>(slurm_reg, "WorkDir", String.class);
	public static final PropertyTag<String> RESERVATION_PROP = new PropertyTag<>(slurm_reg, "ReservationName", String.class);
	
	public static final PropertyTag<String> GRES_PROP = new PropertyTag<>(slurm_reg, "Gres", String.class);
    @AutoTable
	public static final PropertyTag<String> ACCOUNT_PROP = new PropertyTag<>(slurm_reg, "Account", String.class);
    
    public static final PropertyTag<String> QOS_PROP = new PropertyTag<>(slurm_reg, "QOS", String.class);
    
    public static final PropertyTag<String> WCKEY_PROP = new PropertyTag<>(slurm_reg, "WcKey", String.class);
  
    @OptionalTable
    public static final PropertyTag<String> CLUSTER_PROP = new PropertyTag<>(slurm_reg,"Cluster", String.class);
    
    
    @AutoTable
	public static final PropertyTag<Date> SUBMIT_PROP = new PropertyTag<>(slurm_reg, "SubmitTime", Date.class);
    
   
	public static final PropertyTag<Date> ELIGABLE_PROP = new PropertyTag<>(slurm_reg, "EligibleTime", Date.class);
    
    public static final PropertyTag<Integer> ARRAY_JOB_ID = new PropertyTag<>(slurm_reg,"ArrayJobId",Integer.class);
    
    public static final PropertyTag<Integer> ARRAY_TASK_ID = new PropertyTag<>(slurm_reg,"ArrayTaskId",Integer.class);
   
    
  
    @AutoTable
	public static final PropertyTag<String> EXIT_CODE_PROP = new PropertyTag<>(slurm_reg, "ExitCode", String.class);
    
    public static final PropertyTag<String> DERIVED_EXIT_CODE_PROP = new PropertyTag<>(slurm_reg, "DerivedExitCode", String.class);
    
  
    public static class NameIDMaker implements ContainerEntryMaker{
    	/**
		 * @param name_tag
		 * @param id_tag
		 */
		public NameIDMaker(PropertyTag<String> name_tag, PropertyTag<Integer> id_tag) {
			super();
			this.name_tag = name_tag;
			this.id_tag = id_tag;
		}

		private final PropertyTag<String> name_tag;
    	private final PropertyTag<Integer> id_tag;
    	private static final Pattern p = Pattern.compile("\\S+(\\d+)");
		@Override
		public void setValue(PropertyContainer contanier, String valueString) throws IllegalArgumentException,
				InvalidPropertyException, NullPointerException, AccountingParseException {
			Matcher m = p.matcher(valueString);
			if( m.matches()) {
				contanier.setOptionalProperty(name_tag, m.group(1));
				contanier.setOptionalProperty(id_tag, Integer.parseInt(m.group(2)));
			}else {
				throw new AccountingParseException("Illegal NameId");
			}
			
		}

		@Override
		public void setValue(PropertyMap map, String valueString)
				throws IllegalArgumentException, NullPointerException, AccountingParseException {
			Matcher m = p.matcher(valueString);
			if( m.matches()) {
				map.setOptionalProperty(name_tag, m.group(1));
				map.setOptionalProperty(id_tag, Integer.parseInt(m.group(2)));
			}else {
				throw new AccountingParseException("Illegal NameId");
			}
			
		}
    	
    }
   
              
    private static final MakerMap SLURM_ATTRIBUTES = new MakerMap();
	static {
		SLURM_ATTRIBUTES.addParser(JOB_ID_PROP, StringParser.PARSER);
		SLURM_ATTRIBUTES.put("UserId", new NameIDMaker(USER_PROP, UID_PROP));
		SLURM_ATTRIBUTES.put("GroupId", new NameIDMaker(GROUP_PROP, GUID_PROP));
		SLURM_ATTRIBUTES.addParser(JOB_NAME_PROP, StringParser.PARSER);
		SLURM_ATTRIBUTES.addParser(STATE_PROP, StringParser.PARSER);
		SLURM_ATTRIBUTES.addParser(PARTITION_PROP, StringParser.PARSER);
		SLURM_ATTRIBUTES.addParser(TIME_LIMIT_PROP, SlurmDurationParser.PARSER);
		SLURM_ATTRIBUTES.addParser(START_PROP, SLURM_DATE_PARSER);
		SLURM_ATTRIBUTES.addParser(END_PROP, SLURM_DATE_PARSER);
		SLURM_ATTRIBUTES.addParser(NODE_LIST_PROP, StringParser.PARSER);
		SLURM_ATTRIBUTES.addParser(N_NODES_PROP, IntegerParser.PARSER);
		SLURM_ATTRIBUTES.addParser(PROC_COUNT_PROP, IntegerParser.PARSER);
		SLURM_ATTRIBUTES.addParser(WORK_DIR_PROP, StringParser.PARSER);
		SLURM_ATTRIBUTES.addParser(RESERVATION_PROP, StringParser.PARSER);
		SLURM_ATTRIBUTES.addParser(GRES_PROP, StringParser.PARSER);
		SLURM_ATTRIBUTES.addParser(ACCOUNT_PROP, StringParser.PARSER);
		SLURM_ATTRIBUTES.addParser(QOS_PROP, StringParser.PARSER);
		SLURM_ATTRIBUTES.addParser(WCKEY_PROP, StringParser.PARSER);
		SLURM_ATTRIBUTES.addParser(CLUSTER_PROP, StringParser.PARSER);
		
		SLURM_ATTRIBUTES.addParser(SUBMIT_PROP, SLURM_DATE_PARSER);
		SLURM_ATTRIBUTES.addParser(ELIGABLE_PROP, SLURM_DATE_PARSER);
		SLURM_ATTRIBUTES.addParser(ARRAY_JOB_ID, IntegerParser.PARSER);
		SLURM_ATTRIBUTES.addParser(ARRAY_TASK_ID, IntegerParser.PARSER);
		
		
		
		SLURM_ATTRIBUTES.addParser(DERIVED_EXIT_CODE_PROP, StringParser.PARSER);
		
		
		SLURM_ATTRIBUTES.addParser(EXIT_CODE_PROP, StringParser.PARSER);
		
		
		
		// Nested attributes from AllocTRES
//        MakerMap AllocTRES = new MakerMap();
//        AllocTRES.addParser("cpu",ALOCTRES_CPU_PROP, IntegerParser.PARSER);
//        AllocTRES.addParser("mem",ALOCTRES_MEM_PROP, SlurmMemoryParser.PARSER);
//        AllocTRES.addParser("node",ALOCTRES_NODE_PROP, IntegerParser.PARSER);
//        AllocTRES.addParser("gpu", ALOCTRES_GPU_PROP, IntegerParser.PARSER);
//        SLURM_ATTRIBUTES.put("AllocTRES", new NestedContainerEntryMaker(AllocTRES));
	}
	private boolean skip_cancelled=false;
    public SLURMJobCompletionParser(AppContext conn) {
		super(conn);
	}

	@Override
	protected ContainerEntryMaker getEntryMaker(String attr) {
		return SLURM_ATTRIBUTES.get(attr);
	}

	@Override
	public PropertyFinder initFinder(PropertyFinder prev,
			String table) {
		MultiFinder finder = (MultiFinder) super.initFinder( prev, table);
		finder.addFinder(BatchParser.batch);
		finder.addFinder(slurm_reg);
		skip_cancelled = conn.getBooleanParameter(table+SKIP_CANCELLED_SUFFIX, false);
		return finder;
	}

	@Override
	public PropExpressionMap getDerivedProperties(PropExpressionMap previous) {
		PropExpressionMap derv = super.getDerivedProperties(previous);
		try {
			derv.peer(StandardProperties.STARTED_PROP, START_PROP);
			derv.peer(StandardProperties.ENDED_PROP, END_PROP);
			derv.peer(BatchParser.SUBMITTED_PROP, SUBMIT_PROP);
			derv.peer(BatchParser.ACCOUNT_PROP,ACCOUNT_PROP);
			derv.peer(BatchParser.JOB_ID_PROP, JOB_ID_PROP);
			derv.peer(StandardProperties.USERNAME_PROP, USER_PROP);
			derv.peer(StandardProperties.GROUPNAME_PROP, GROUP_PROP);
			derv.peer(BatchParser.JOB_ID_PROP, JOB_ID_PROP);
			derv.peer(BatchParser.PARTITION_PROP, PARTITION_PROP);
			derv.peer(BatchParser.NODE_COUNT_PROP, N_NODES_PROP);
			derv.peer(BatchParser.PROC_COUNT_PROP, PROC_COUNT_PROP);
			derv.peer(BatchParser.JOB_NAME_PROP, JOB_NAME_PROP);
			
		} catch (PropertyCastException e) {
			getLogger().error("Error setting standard derived props",e);
		}
		return derv;
	}

	/* (non-Javadoc)
	 * @see uk.ac.ed.epcc.safe.accounting.parsers.AbstractKeyPairParser#skipCheck(uk.ac.ed.epcc.safe.accounting.properties.PropertyMap)
	 */
	@Override
	public boolean skipCheck(PropertyMap map)throws AccountingParseException {
		String state = map.getProperty(STATE_PROP);
		if( state == null ) {
			throw new AccountingParseException("No state field");
		}
		// Illegal states
		if( state.startsWith("RUNNING") || state.startsWith("PENDING")){
			return false;
		}
		if( skip_cancelled && state.startsWith("CANCELLED")){
			return false;
		}

		return true;
	}

}