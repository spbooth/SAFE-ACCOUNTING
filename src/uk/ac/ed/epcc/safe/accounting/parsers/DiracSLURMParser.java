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

import uk.ac.ed.epcc.safe.accounting.expr.DurationSecondsPropExpression;
import uk.ac.ed.epcc.safe.accounting.expr.PropExpressionMap;
import uk.ac.ed.epcc.safe.accounting.expr.PropertyCastException;
import uk.ac.ed.epcc.safe.accounting.parsers.value.DateParser;
import uk.ac.ed.epcc.safe.accounting.parsers.value.IntegerParser;
import uk.ac.ed.epcc.safe.accounting.parsers.value.SlurmDurationParser;
import uk.ac.ed.epcc.safe.accounting.parsers.value.SlurmMemoryParser;
import uk.ac.ed.epcc.safe.accounting.parsers.value.StringParser;
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
import uk.ac.ed.epcc.webapp.model.data.Duration;

public class DiracSLURMParser extends AbstractKeyPairParser {
	private static final String SKIP_CANCELLED_SUFFIX = ".skip_cancelled";

	public static final DateParser SLURM_DATE_PARSER = new DateParser(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss"));
	
    public static final PropertyRegistry slurm_reg = new PropertyRegistry("diracslurm", "Dirac slurm properties");
    
    @AutoTable(unique=true)
    public static final PropertyTag<String> JOB_ID_PROP = new PropertyTag<String>(slurm_reg, "JobID", String.class);
	
    @AutoTable
	public static final PropertyTag<String> USER_PROP = new PropertyTag<String>(slurm_reg, "User", String.class);
    
    @AutoTable
	public static final PropertyTag<String> GROUP_PROP = new PropertyTag<String>(slurm_reg, "Group", String.class);
    
    @AutoTable
	public static final PropertyTag<String> ACCOUNT_PROP = new PropertyTag<String>(slurm_reg, "Account", String.class);
    
    @AutoTable(length=128)
	public static final PropertyTag<String> JOB_NAME_PROP = new PropertyTag<String>(slurm_reg, "JobName", String.class);
    
    @AutoTable
	public static final PropertyTag<String> PARTITION_PROP = new PropertyTag<String>(slurm_reg, "Partition", String.class);
    
    @AutoTable
	public static final PropertyTag<Date> SUBMIT_PROP = new PropertyTag<Date>(slurm_reg, "Submit", Date.class);
    
    @AutoTable
	public static final PropertyTag<Duration> RESERVED_PROP = new PropertyTag<Duration>(slurm_reg, "Reserved", Duration.class);
    
    @AutoTable(unique=true)
	public static final PropertyTag<Date> START_PROP = new PropertyTag<Date>(slurm_reg, "Start", Date.class);
    
    @AutoTable
	public static final PropertyTag<Date> END_PROP = new PropertyTag<Date>(slurm_reg, "End", Date.class);
    
    @AutoTable
	public static final PropertyTag<Duration> ELAPSED_PROP = new PropertyTag<Duration>(slurm_reg, "Elapsed", Duration.class);
    
    @AutoTable
	public static final PropertyTag<Integer> N_NODES_PROP = new PropertyTag<Integer>(slurm_reg, "NNodes", Integer.class);
    
    @AutoTable
	public static final PropertyTag<Integer> N_CPUS_PROP = new PropertyTag<Integer>(slurm_reg, "NCPUS", Integer.class);
    
    @AutoTable
	public static final PropertyTag<Duration> TIME_LIMIT_PROP = new PropertyTag<Duration>(slurm_reg, "Timelimit", Duration.class);
    
    @AutoTable(target=Long.class)
	public static final PropertyTag<Number> REQ_MEM_PROP = new PropertyTag<Number>(slurm_reg, "ReqMem", Number.class);
    @AutoTable(target=Long.class)
	public static final PropertyTag<Number> USED_MEM_PROP = new PropertyTag<Number>(slurm_reg, "resources_used_mem", Number.class);

    @AutoTable
	public static final PropertyTag<String> EXIT_CODE_PROP = new PropertyTag<String>(slurm_reg, "ExitCode", String.class);
    
    @AutoTable
	public static final PropertyTag<String> STATE_PROP = new PropertyTag<String>(slurm_reg, "State", String.class);
    
    @OptionalTable
    public static final PropertyTag<String> CLUSTER_PROP = new PropertyTag<String>(slurm_reg,"Cluster", String.class);
    
    @OptionalTable(target=Long.class)
    public static final PropertyTag<Number> CPU_TIME_RAW_PROP = new PropertyTag<Number>(slurm_reg,"CPUTimeRAW",Number.class,"Residency seconds from SLURM");
    @OptionalTable
    public static final PropertyTag<Integer> ALOCTRES_CPU_PROP = new PropertyTag<Integer>(slurm_reg,"AllocTREScpu",Integer.class,"cpu field from AllocTRES");
    @OptionalTable(target=Long.class)
    public static final PropertyTag<Number> ALOCTRES_MEM_PROP = new PropertyTag<Number>(slurm_reg,"AllocTRESmem",Number.class,"mem field from AllocTRES");
    @OptionalTable
    public static final PropertyTag<Integer> ALOCTRES_NODE_PROP = new PropertyTag<Integer>(slurm_reg,"AllocTRESnode",Integer.class,"node field from AllocTRES");
    @OptionalTable
    public static final PropertyTag<Integer> ALOCTRES_GPU_PROP = new PropertyTag<Integer>(slurm_reg,"AllocTRESgpu",Integer.class,"gres/gpu field from AllocTRES");
              
    private static final MakerMap SLURM_ATTRIBUTES = new MakerMap();
	static {
		SLURM_ATTRIBUTES.addParser(JOB_ID_PROP, StringParser.PARSER);
		SLURM_ATTRIBUTES.addParser(USER_PROP, StringParser.PARSER);
		SLURM_ATTRIBUTES.addParser(GROUP_PROP, StringParser.PARSER);
		SLURM_ATTRIBUTES.addParser(ACCOUNT_PROP, StringParser.PARSER);
		SLURM_ATTRIBUTES.addParser(JOB_NAME_PROP, StringParser.PARSER);
		SLURM_ATTRIBUTES.addParser(PARTITION_PROP, StringParser.PARSER);
		SLURM_ATTRIBUTES.addParser(SUBMIT_PROP, SLURM_DATE_PARSER);
		SLURM_ATTRIBUTES.addParser(RESERVED_PROP, SlurmDurationParser.PARSER);
		SLURM_ATTRIBUTES.addParser(START_PROP, SLURM_DATE_PARSER);
		SLURM_ATTRIBUTES.addParser(END_PROP, SLURM_DATE_PARSER);
		SLURM_ATTRIBUTES.addParser(ELAPSED_PROP, SlurmDurationParser.PARSER);
		SLURM_ATTRIBUTES.addParser(N_NODES_PROP, IntegerParser.PARSER);
		SLURM_ATTRIBUTES.addParser(N_CPUS_PROP, IntegerParser.PARSER);
		SLURM_ATTRIBUTES.addParser(TIME_LIMIT_PROP, SlurmDurationParser.PARSER);
		SLURM_ATTRIBUTES.addParser(REQ_MEM_PROP, SlurmMemoryParser.PARSER);
		SLURM_ATTRIBUTES.addParser(USED_MEM_PROP, SlurmMemoryParser.PARSER);
		SLURM_ATTRIBUTES.addParser(EXIT_CODE_PROP, StringParser.PARSER);
		SLURM_ATTRIBUTES.addParser(STATE_PROP, StringParser.PARSER);
		SLURM_ATTRIBUTES.addParser(CLUSTER_PROP, StringParser.PARSER);
		SLURM_ATTRIBUTES.addParser(CPU_TIME_RAW_PROP, IntegerParser.PARSER);
		// Nested attributes from AllocTRES
        MakerMap AllocTRES = new MakerMap();
        AllocTRES.addParser("cpu",ALOCTRES_CPU_PROP, IntegerParser.PARSER);
        AllocTRES.addParser("mem",ALOCTRES_MEM_PROP, SlurmMemoryParser.PARSER);
        AllocTRES.addParser("node",ALOCTRES_NODE_PROP, IntegerParser.PARSER);
        AllocTRES.addParser("gpu", ALOCTRES_GPU_PROP, IntegerParser.PARSER);
        SLURM_ATTRIBUTES.put("AllocTRES", new NestedContainerEntryMaker(AllocTRES));
	}
	private boolean skip_cancelled=false;
    public DiracSLURMParser(AppContext conn) {
		super(conn);
	}

	@Override
	protected ContainerEntryMaker getEntryMaker(String attr) {
		return SLURM_ATTRIBUTES.get(attr);
	}

	@Override
	public PropertyFinder initFinder(AppContext conn, PropertyFinder prev,
			String table) {
		MultiFinder finder = (MultiFinder) super.initFinder(conn, prev, table);
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
			derv.peer(BatchParser.PROC_COUNT_PROP, N_CPUS_PROP);
			derv.peer(BatchParser.JOB_NAME_PROP, JOB_NAME_PROP);
			derv.put(BatchParser.WALLCLOCK_PROP, new DurationSecondsPropExpression(ELAPSED_PROP));
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