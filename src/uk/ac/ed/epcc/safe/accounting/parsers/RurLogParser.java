package uk.ac.ed.epcc.safe.accounting.parsers;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import uk.ac.ed.epcc.safe.accounting.expr.ExpressionTargetContainer;
import uk.ac.ed.epcc.safe.accounting.parsers.value.IntegerParser;
import uk.ac.ed.epcc.safe.accounting.parsers.value.LongParser;
import uk.ac.ed.epcc.safe.accounting.parsers.value.StringParser;
import uk.ac.ed.epcc.safe.accounting.properties.AttributePropertyTag;
import uk.ac.ed.epcc.safe.accounting.properties.DynamicAttributePropertyTag;
import uk.ac.ed.epcc.safe.accounting.properties.MultiFinder;
import uk.ac.ed.epcc.safe.accounting.properties.PropertyFinder;
import uk.ac.ed.epcc.safe.accounting.properties.PropertyMap;
import uk.ac.ed.epcc.safe.accounting.properties.PropertyRegistry;
import uk.ac.ed.epcc.safe.accounting.properties.PropertyTag;
import uk.ac.ed.epcc.safe.accounting.properties.StandardProperties;
import uk.ac.ed.epcc.safe.accounting.update.AbstractPropertyContainerParser;
import uk.ac.ed.epcc.safe.accounting.update.AccountingParseException;
import uk.ac.ed.epcc.safe.accounting.update.AutoTable;
import uk.ac.ed.epcc.safe.accounting.update.IncrementalPropertyContainerParser;
import uk.ac.ed.epcc.safe.accounting.update.OptionalTable;
import uk.ac.ed.epcc.webapp.AppContext;
import uk.ac.ed.epcc.webapp.logging.Logger;
import uk.ac.ed.epcc.webapp.logging.LoggerService;

/** Parser for aprun commands
 * 
 * @author mrb
 *
 */
public class RurLogParser extends AbstractPropertyContainerParser implements IncrementalPropertyContainerParser {
	
	private static final String ENERGY_PLUGIN_NAME = "energy";
	private static final String TASKSTATS_PLUGIN_NAME = "taskstats";
	private static final String MEMORY_PLUGIN_NAME = "memory";
	private static final String TIMESTAMP_PLUGIN_NAME = "timestamp";
	//private static final String[] plugin_name_list = {ENERGY_PLUGIN_NAME, TASKSTATS_PLUGIN_NAME, MEMORY_PLUGIN_NAME, TIMESTAMP_PLUGIN_NAME};
	
	private static final PropertyRegistry rur_reg = new PropertyRegistry("rur", "Properties from rur log");
	private static final PropertyRegistry plugin_reg = new PropertyRegistry("rur_plugins", "Plugins seen in rur");
	
	@AutoTable(target=String.class, length=1)
	public static final PropertyTag<String> ENERGY_PLUGIN = new PropertyTag<String>(plugin_reg, ENERGY_PLUGIN_NAME, String.class);
	@AutoTable(target=String.class, length=1)
	public static final PropertyTag<String> TASKSTATS_PLUGIN = new PropertyTag<String>(plugin_reg, TASKSTATS_PLUGIN_NAME, String.class);
	@OptionalTable(target=String.class, length=1)
	public static final PropertyTag<String> MEMORY_PLUGIN = new PropertyTag<String>(plugin_reg, MEMORY_PLUGIN_NAME, String.class);
	@OptionalTable(target=String.class, length=1)
	public static final PropertyTag<String> TIMESTAMP_PLUGIN = new PropertyTag<String>(plugin_reg, TIMESTAMP_PLUGIN_NAME, String.class);

	
	@AutoTable(target=Integer.class, unique=true)
	public static final AttributePropertyTag<Integer> ALPS_ID = new AttributePropertyTag<Integer>(rur_reg, "apid", null, Integer.class,
			"ALPS log id", -1);
	
	// This covers us for re-use of alpds ids
	@AutoTable(target=String.class,length=32,unique=true)
	public static final PropertyTag<String> PBS_ID = new PropertyTag<String>(rur_reg, "pbs_id",String.class,"PBS Job ID");
	
	
	@OptionalTable(target=Date.class)
	public static final AttributePropertyTag<Date> START_TIMESTAMP = new AttributePropertyTag<Date>(rur_reg, "start_date_time", null, Date.class, "date time job started", new Date());
	
	@OptionalTable(target=Date.class)
	public static final AttributePropertyTag<Date> STOP_TIMESTAMP = new AttributePropertyTag<Date>(rur_reg, "stop_date_time", null, Date.class, "date time job stopped", new Date());
	
	@AutoTable(target=Long.class)
	public static final AttributePropertyTag<Long> ENERGY_USED = new AttributePropertyTag<Long>(rur_reg, "energy_used", null, Long.class,
			"Total energy (in joules) used across all nodes, including accelerators", -1L);
	@OptionalTable(target=Integer.class)
	public static final AttributePropertyTag<Integer> NODES = new AttributePropertyTag<Integer>(rur_reg, "nodes", null, Integer.class,
			"Number of nodes in job", -1);
	@OptionalTable(target=Integer.class)
	public static final AttributePropertyTag<Integer> NODES_POWER_CAPPED = new AttributePropertyTag<Integer>(rur_reg, "nodes_power_capped", null, Integer.class,
			"Number of nodes with nonzero power cap", -1);
	@OptionalTable(target=Integer.class)
	public static final AttributePropertyTag<Integer> NODES_THROTTLED = new AttributePropertyTag<Integer>(rur_reg, "nodes_throttled", null, Integer.class,
			"Number of nodes that experienced one or more throttled sockets", -1);
	@OptionalTable(target=Integer.class)
	public static final AttributePropertyTag<Integer> NODES_CHANGED_POWER_CAP = new AttributePropertyTag<Integer>(rur_reg, "nodes_with_changed_power_cap", null, Integer.class,
			"Number of nodes with power caps that changed during execution, including accelerators", -1);
	@OptionalTable(target=Integer.class)
	public static final AttributePropertyTag<Integer> MAX_POWER_CAP = new AttributePropertyTag<Integer>(rur_reg, "max_power_cap", null, Integer.class,
			"Maximum nonzero power cap", -1);
	@OptionalTable(target=Integer.class)
	public static final AttributePropertyTag<Integer> MAX_POWER_CAP_COUNT = new AttributePropertyTag<Integer>(rur_reg, "max_power_cap_count", null, Integer.class,
			"Number of nodes with the maximum nonzero power cap", -1);
	@OptionalTable(target=Integer.class)
	public static final AttributePropertyTag<Integer> MIN_POWER_CAP = new AttributePropertyTag<Integer>(rur_reg, "min_power_cap", null, Integer.class,
			"Minimum nonzero power cap", -1);
	@OptionalTable(target=Integer.class)
	public static final AttributePropertyTag<Integer> MIN_POWER_CAP_COUNT = new AttributePropertyTag<Integer>(rur_reg, "min_power_cap_count", null, Integer.class,
			"Number of nodes with the minimum nonzero power cap", -1);
	@OptionalTable(target=Integer.class)
	public static final AttributePropertyTag<Integer> ACCEL_ENERGY_USED = new AttributePropertyTag<Integer>(rur_reg, "accel_energy_used", null, Integer.class,
			"Total accelerator energy (in Joules) used", -1);
	@OptionalTable(target=Integer.class)
	public static final AttributePropertyTag<Integer> NODES_ACCEL_POWER_CAPPED = new AttributePropertyTag<Integer>(rur_reg, "nodes_accel_power_capped",  null, Integer.class,
			"Number of accelerators with nonzero power cap", -1);
	@OptionalTable(target=Integer.class)
	public static final AttributePropertyTag<Integer> MAX_ACCEL_POWER_CAP = new AttributePropertyTag<Integer>(rur_reg, "max_accel_power_cap", null, Integer.class,
			"Maximum nonzero accelerator power cap", -1);
	@OptionalTable(target=Integer.class)
	public static final AttributePropertyTag<Integer> MAX_ACCEL_POWER_CAP_COUNT = new AttributePropertyTag<Integer>(rur_reg, "max_accel_power_cap_count", null, Integer.class,
			"Number of accelerators with the maximum nonzero power cap", -1);
	@OptionalTable(target=Integer.class)
	public static final AttributePropertyTag<Integer> MIN_ACCEL_POWER_CAP = new AttributePropertyTag<Integer>(rur_reg, "min_accel_power_cap", null, Integer.class,
			"Minimum nonzero accelerator power cap", -1);
	@OptionalTable(target=Integer.class)
	public static final AttributePropertyTag<Integer> MIN_ACCEL_POWER_CAP_COUNT = new AttributePropertyTag<Integer>(rur_reg, "min_accel_power_cap_count", null, Integer.class,
			"Number of accelerators with the minimum nonzero power cap", -1);
	
	
	@AutoTable(target=Long.class)
	public static final AttributePropertyTag<Long> START_TIME = new AttributePropertyTag<Long>(rur_reg, "btime", null, Long.class,
			"UNIX time when process started", -1L);
	@AutoTable(target=Long.class)
	public static final AttributePropertyTag<Long> ELAPSED_TIME = new AttributePropertyTag<Long>(rur_reg, "etime", null, Long.class,
			"Total elapsed time in microseconds", -1L);
	@AutoTable(target=Long.class)
	public static final AttributePropertyTag<Long> SYSTEM_TIME = new AttributePropertyTag<Long>(rur_reg, "stime", null, Long.class,
			"System time", -1L);
	@AutoTable(target=Long.class)
	public static final AttributePropertyTag<Long> USER_TIME = new AttributePropertyTag<Long>(rur_reg, "utime", null, Long.class,
			"User time", -1L);
	@AutoTable(target=Long.class)
	public static final AttributePropertyTag<Long> TOTAL_IO_DELAY_TIME = new AttributePropertyTag<Long>(rur_reg, "bkiowait", null, Long.class,
			"Total delay time (ns) waiting for synchronous block I/O to complete", -1L);
	@OptionalTable(target=Long.class)
	public static final AttributePropertyTag<Long> HIGH_MEMORY_USED = new AttributePropertyTag<Long>(rur_reg, "rss", null, Long.class,
			"RSS highwater mark", -1L);
	@AutoTable(target=Long.class)
	public static final AttributePropertyTag<Long> MAX_MEMORY_USED = new AttributePropertyTag<Long>(rur_reg, "max_rss", null, Long.class,
			"Maximum memory used", -1L);
	@AutoTable(target=Long.class)
	public static final AttributePropertyTag<Long> MEMORY_USED_INTEGRAL = new AttributePropertyTag<Long>(rur_reg, "coremem", null, Long.class,
			"Integral of RSS used by process in MB-usec", -1L);
	@AutoTable(target=Long.class)
	public static final AttributePropertyTag<Long> VIRTUAL_MEMORY_INTEGRAL = new AttributePropertyTag<Long>(rur_reg, "vm", null, Long.class,
			"Integral of virtual memory used by process in MB-usecs", -1L);
	@AutoTable(target=Long.class)
	public static final AttributePropertyTag<Long> MAX_VIRTUAL_MEMORY_INTEGRAL = new AttributePropertyTag<Long>(rur_reg, "max_vm", null, Long.class,
			"Number of accelerators with the minimum nonzero power cap", -1L);		
	@AutoTable(target=Long.class)
	public static final AttributePropertyTag<Long> SWAPPED_PAGE_COUNT = new AttributePropertyTag<Long>(rur_reg, "pgswapcnt", null, Long.class,
			"Number of pages swapped", -1L);
	@AutoTable(target=Long.class)
	public static final AttributePropertyTag<Long> MINOR_PAGE_FAULT_COUNT = new AttributePropertyTag<Long>(rur_reg, "minfault", null, Long.class,
			"Number of minor page faults", -1L);
	@AutoTable(target=Long.class)
	public static final AttributePropertyTag<Long> MAJOR_PAGE_FAULT_COUNT = new AttributePropertyTag<Long>(rur_reg, "majfault", null, Long.class,
			"Number of major page faults", -1L);
	@AutoTable(target=Long.class)
	public static final AttributePropertyTag<Long> CHAR_READ_COUNT = new AttributePropertyTag<Long>(rur_reg, "rchar", null, Long.class,
			"Characters read by process", -1L);
	@AutoTable(target=Long.class)
	public static final AttributePropertyTag<Long> CHAR_WRITE_COUNT = new AttributePropertyTag<Long>(rur_reg, "wchar", null, Long.class,
			"Characters written by process", -1L);
	@AutoTable(target=Long.class)
	public static final AttributePropertyTag<Long> SYS_READ_CALLS_COUNT = new AttributePropertyTag<Long>(rur_reg, "rcalls", null, Long.class,
			"Number of read system calls", -1L);
	@AutoTable(target=Long.class)
	public static final AttributePropertyTag<Long> SYS_WRITE_CALLS_COUNT = new AttributePropertyTag<Long>(rur_reg, "wcalls", null, Long.class,
			"Number of write system calls", -1L);
	@AutoTable(target=String.class, length=1024)
	public static final AttributePropertyTag<String> EXIT_CODE_LIST = new AttributePropertyTag<String>(rur_reg, "exitcode_signal", new String[]{"exitcode:signal"}, String.class,
			"Exit code list", "");
	@AutoTable(target=String.class, length=1024)
	public static final AttributePropertyTag<String> ABORT_INFO = new AttributePropertyTag<String>(rur_reg, "abortinfo", null, String.class,
			"If abnormal termination occurs, a list of abort_info fields is reported", "");
	@AutoTable(target=Integer.class)
	public static final AttributePropertyTag<Integer> CORE_DUMP_FLAG = new AttributePropertyTag<Integer>(rur_reg, "core", null, Integer.class,
			"Core dump flag", -1);
	@OptionalTable(target=Integer.class)
	public static final AttributePropertyTag<Integer> PROCESS_EXIT_CODE = new AttributePropertyTag<Integer>(rur_reg, "ecode", null, Integer.class,
			"Process exit code", -1);
	@OptionalTable(target=Integer.class)
	public static final AttributePropertyTag<Integer> NODE_SCHEDULING_DISCIPLINE = new AttributePropertyTag<Integer>(rur_reg, "sched", null, Integer.class,
			"Scheduling discipline used on node", -1);
	@OptionalTable(target=Integer.class)
	public static final AttributePropertyTag<Integer> POSIX_NICE_VALUE = new AttributePropertyTag<Integer>(rur_reg, "nice", null, Integer.class,
			"POSIX nice value of process", -1);
	@OptionalTable(target=String.class)
	public static final AttributePropertyTag<String> APPLICATION_ID = new AttributePropertyTag<String>(rur_reg, "appid", new String[]{"apid"}, String.class,
			"Application ID as defined by application launcher", "");
	@OptionalTable(target=String.class)
	public static final AttributePropertyTag<String> USER_ID = new AttributePropertyTag<String>(rur_reg, "uid", null, String.class,
			"User ID", "");
	@OptionalTable(target=String.class)
	public static final AttributePropertyTag<String> PROCESS_ID = new AttributePropertyTag<String>(rur_reg, "pid", null, String.class,
			"Process ID", "");
	@OptionalTable(target=String.class)
	public static final AttributePropertyTag<String> JOB_ID = new AttributePropertyTag<String>(rur_reg, "jid", null, String.class,
			"Job ID", "");
	@OptionalTable(target=String.class)
	public static final AttributePropertyTag<String> GROUP_ID = new AttributePropertyTag<String>(rur_reg, "gid", null, String.class,
			"Group ID", "");
	@OptionalTable(target=String.class)
	public static final AttributePropertyTag<String> NODE_ID = new AttributePropertyTag<String>(rur_reg, "nid", null, String.class,
			"Node ID", "");
	@OptionalTable(target=String.class)
	public static final AttributePropertyTag<String> PARENT_JOB_ID = new AttributePropertyTag<String>(rur_reg, "pjid", null, String.class,
			"Parent job ID", "");
	@OptionalTable(target=String.class)
	public static final AttributePropertyTag<String> JOB_PROJECT_ID = new AttributePropertyTag<String>(rur_reg, "prid", null, String.class,
			"Job project ID", "");
	@OptionalTable(target=String.class)
	public static final AttributePropertyTag<String> PARENT_PROCESS_ID = new AttributePropertyTag<String>(rur_reg, "ppid", null, String.class,
			"Parent process ID", "");
	@OptionalTable(target=String.class)
	public static final AttributePropertyTag<String> PROCESS_NAME = new AttributePropertyTag<String>(rur_reg, "comm", null, String.class,
			"String containing process name", "");
	
	
	
	@OptionalTable(target=String.class, length=1024)
	public static final AttributePropertyTag<String> BOOT_MEM_PERCENTAGE = new AttributePropertyTag<String>(rur_reg, "boot_mem_percent", new String[]{"%_of_boot_mem"}, String.class,
			"The percentage of boot memory for each order chunk in /proc/buddyinfo summed across all memory zones", "");
	@OptionalTable(target=Long.class)
	public static final AttributePropertyTag<Long> BOOT_FREE_MEM_CONTENTS = new AttributePropertyTag<Long>(rur_reg, "boot_freemem", null, Long.class,
			"Contents of /proc/boot_freemem", -1L);
	@OptionalTable(target=Long.class)
	public static final AttributePropertyTag<Long> CURRENT_FREE_MEM_CONTENTS = new AttributePropertyTag<Long>(rur_reg, "current_freemem", null, Long.class,
			"Contents of /proc/current_freemem", -1L);
	
	
	@OptionalTable(target=Long.class)
	public static final AttributePropertyTag<Long> ACTIVE_MEM_TOTAL = new AttributePropertyTag<Long>(rur_reg, "meminfo_active_anon", new String[]{"Active(anon)"}, Long.class,
			"Total amount of memory in active use by the application", -1L);
	@OptionalTable(target=Long.class)
	public static final AttributePropertyTag<Long> ACTIVE_FILE_MEM_TOTAL = new AttributePropertyTag<Long>(rur_reg, "meminfo_active_file", new String[]{"Active(file)"}, Long.class,
			"Total amount of memory in active use by cache and buffers", -1L);
	@OptionalTable(target=Long.class)
	public static final AttributePropertyTag<Long> INACTIVE_MEM_TOTAL = new AttributePropertyTag<Long>(rur_reg, "meminfo_inactive_anon", new String[]{"Inactive(anon)"}, Long.class,
			"Total amount of memory that is candidate to be swapped out", -1L);
	@OptionalTable(target=Long.class)
	public static final AttributePropertyTag<Long> INACTIVE_FILE_MEM_TOTAL = new AttributePropertyTag<Long>(rur_reg, "meminfo_inactive_file", new String[]{"Inactive(file)"}, Long.class,
			"Total amount of memory that is candidate to be dropped from cache", -1L);
	@OptionalTable(target=Long.class)
	public static final AttributePropertyTag<Long> KERNEL_MEM_TOTAL = new AttributePropertyTag<Long>(rur_reg, "meminfo_slab", new String[]{"Slab"}, Long.class,
			"Total amount of memory used by the kernel", -1L);
	
	public static final AttributePropertyTag<String> MEM_INFO = new AttributePropertyTag<String>(rur_reg, "meminfo", null, String.class,
			"Memory information", "");
	
		
	@OptionalTable(target=String.class)
	public static final AttributePropertyTag<String> HUGEPAGES_NAME = new AttributePropertyTag<String>(rur_reg, "hugepagesname", null, String.class,
			"Hugepages name", "");
	@OptionalTable(target=Long.class)
	public static final AttributePropertyTag<Long> HUGEPAGES_SIZE = new AttributePropertyTag<Long>(rur_reg, "hugepagessize", null, Long.class,
			"Hugepages size", -1L);
	@OptionalTable(target=String.class)
	public static final AttributePropertyTag<String> HUGEPAGES_SIZE_UNIT = new AttributePropertyTag<String>(rur_reg, "hugepagessizeunit", null, String.class,
			"Number of hugepages that exist at this point", "");
	@OptionalTable(target=Integer.class)
	public static final AttributePropertyTag<Integer> HUGEPAGES_COUNT = new AttributePropertyTag<Integer>(rur_reg, "hugepages_nr", new String[]{"nr"}, Integer.class,
			"Number of hugepages that exist at this point", -1);
	@OptionalTable(target=Integer.class)
	public static final AttributePropertyTag<Integer> SURPLUS_HUGEPAGES_COUNT = new AttributePropertyTag<Integer>(rur_reg, "hugepages_surplus", new String[]{"surplus"}, Integer.class,
			"Number of hugepages above nr", -1);
	@OptionalTable(target=Integer.class)
	public static final AttributePropertyTag<Integer> UNALLOCATED_HUGEPAGES_COUNT = new AttributePropertyTag<Integer>(rur_reg, "hugepages_free", new String[]{"free"}, Integer.class,
			"Number of hugepages that are not yet allocated", -1);	
	@OptionalTable(target=Integer.class)
	public static final AttributePropertyTag<Integer> RESERVED_HUGEPAGES_COUNT = new AttributePropertyTag<Integer>(rur_reg, "hugepages_resv", new String[]{"resv"}, Integer.class,
			"Number of hugepages committed for allocation, but no allocation has occurred", -1);
	
	public static final DynamicAttributePropertyTag<String> HUGEPAGES = new DynamicAttributePropertyTag<String>(rur_reg, "hugepages",
			"hugepages-(?<"+HUGEPAGES_SIZE.getName()+">\\d+)(?<"+HUGEPAGES_SIZE_UNIT.getName()+">[kMG]B)", null, String.class,
			"The hugepages size for the select entries from /sys/kernel/mm/hugepages/hugepages-*B/*", "", true, false);
	
	
	@OptionalTable(target=String.class, length=1024)
	public static final AttributePropertyTag<String> NODEZONES_NAME = new AttributePropertyTag<String>(rur_reg, "nodezonesname", null, String.class,
			"Nodezones name", "");
	@OptionalTable(target=String.class)
	public static final AttributePropertyTag<String> NODEZONES_NUMBER = new AttributePropertyTag<String>(rur_reg, "nodezonesnumber", null, String.class,
			"Nodezones number", "");
	@OptionalTable(target=String.class)
	public static final AttributePropertyTag<String> NODEZONES_TYPE = new AttributePropertyTag<String>(rur_reg, "nodezonestype", null, String.class,
			"Nodezones type", "");
	@OptionalTable(target=String.class, length=2048)
	public static final AttributePropertyTag<String> NODEZONES_DATA = new AttributePropertyTag<String>(rur_reg, "nodezones_data", null, String.class,
			"Nodezones data", "");
	
	public static final DynamicAttributePropertyTag<String> NODEZONES = new DynamicAttributePropertyTag<String>(rur_reg, "nodezones",
			"Node_(?<"+NODEZONES_NUMBER.getName()+">\\d+)_zone_(?<"+NODEZONES_TYPE.getName()+">\\S+)", null, String.class,
			"Node zone information", "", false, true);
	
	
	/**
	 * If a Python exception occurs during the post or staging scripts, the following data is reported for the energy and memory plugins.
	 */
	@OptionalTable(target=String.class)
	public static final AttributePropertyTag<String> ERROR_TRACEBACK = new AttributePropertyTag<String>(rur_reg, "error_traceback", new String[]{"traceback"}, String.class,
			"Stack frame list", "");
	@OptionalTable(target=String.class)
	public static final AttributePropertyTag<String> ERROR_TYPE = new AttributePropertyTag<String>(rur_reg, "error_type", new String[]{"type"}, String.class,
			"Python exception type", "");
	@OptionalTable(target=String.class)
	public static final AttributePropertyTag<String> ERROR_VALUE = new AttributePropertyTag<String>(rur_reg, "error_value", new String[]{"value"}, String.class,
			"Python exception parameter", "");
	@OptionalTable(target=String.class)
	public static final AttributePropertyTag<String> ERROR_NID = new AttributePropertyTag<String>(rur_reg, "error_nid", new String[]{"nid"}, String.class,
			"Id of node on which exception occurred", "");
	@OptionalTable(target=String.class)
	public static final AttributePropertyTag<String> ERROR_CNAME = new AttributePropertyTag<String>(rur_reg, "error_cname", new String[]{"cname"}, String.class,
			"Node on which exception occurred", "");
	
	public static final AttributePropertyTag<String> ERROR_INFO = new AttributePropertyTag<String>(rur_reg, "error", null, String.class,
			"Error information", "");
	
	
	private static final MakerMap STANDARD_ATTRIBUTES = new MakerMap();
	static {
		STANDARD_ATTRIBUTES.addParser(ENERGY_USED, LongParser.PARSER);
		STANDARD_ATTRIBUTES.addParser(NODES, IntegerParser.PARSER);
		STANDARD_ATTRIBUTES.addParser(NODES_POWER_CAPPED, IntegerParser.PARSER);
		STANDARD_ATTRIBUTES.addParser(NODES_THROTTLED, IntegerParser.PARSER);
		STANDARD_ATTRIBUTES.addParser(NODES_CHANGED_POWER_CAP, IntegerParser.PARSER);
		STANDARD_ATTRIBUTES.addParser(MAX_POWER_CAP, IntegerParser.PARSER);
		STANDARD_ATTRIBUTES.addParser(MAX_POWER_CAP_COUNT, IntegerParser.PARSER);
		STANDARD_ATTRIBUTES.addParser(MIN_POWER_CAP, IntegerParser.PARSER);
		STANDARD_ATTRIBUTES.addParser(MIN_POWER_CAP_COUNT, IntegerParser.PARSER);
		STANDARD_ATTRIBUTES.addParser(ACCEL_ENERGY_USED, IntegerParser.PARSER);
		STANDARD_ATTRIBUTES.addParser(NODES_ACCEL_POWER_CAPPED, IntegerParser.PARSER);
		STANDARD_ATTRIBUTES.addParser(MAX_ACCEL_POWER_CAP, IntegerParser.PARSER);
		STANDARD_ATTRIBUTES.addParser(MAX_ACCEL_POWER_CAP_COUNT, IntegerParser.PARSER);
		STANDARD_ATTRIBUTES.addParser(MIN_ACCEL_POWER_CAP, IntegerParser.PARSER);
		STANDARD_ATTRIBUTES.addParser(MIN_ACCEL_POWER_CAP_COUNT, IntegerParser.PARSER);
		
		STANDARD_ATTRIBUTES.addParser(START_TIME, LongParser.PARSER);
		STANDARD_ATTRIBUTES.addParser(ELAPSED_TIME, LongParser.PARSER);
		STANDARD_ATTRIBUTES.addParser(SYSTEM_TIME, LongParser.PARSER);
		STANDARD_ATTRIBUTES.addParser(USER_TIME, LongParser.PARSER);
		STANDARD_ATTRIBUTES.addParser(TOTAL_IO_DELAY_TIME, LongParser.PARSER);
		STANDARD_ATTRIBUTES.addParser(HIGH_MEMORY_USED, LongParser.PARSER);
		STANDARD_ATTRIBUTES.addParser(MAX_MEMORY_USED, LongParser.PARSER);
		STANDARD_ATTRIBUTES.addParser(MEMORY_USED_INTEGRAL, LongParser.PARSER);
		STANDARD_ATTRIBUTES.addParser(VIRTUAL_MEMORY_INTEGRAL, LongParser.PARSER);
		STANDARD_ATTRIBUTES.addParser(MAX_VIRTUAL_MEMORY_INTEGRAL, LongParser.PARSER);
		STANDARD_ATTRIBUTES.addParser(SWAPPED_PAGE_COUNT, LongParser.PARSER);
		STANDARD_ATTRIBUTES.addParser(MINOR_PAGE_FAULT_COUNT, LongParser.PARSER);
		STANDARD_ATTRIBUTES.addParser(MAJOR_PAGE_FAULT_COUNT, LongParser.PARSER);
		STANDARD_ATTRIBUTES.addParser(CHAR_READ_COUNT, LongParser.PARSER);
		STANDARD_ATTRIBUTES.addParser(CHAR_WRITE_COUNT, LongParser.PARSER);
		STANDARD_ATTRIBUTES.addParser(SYS_READ_CALLS_COUNT, LongParser.PARSER);
		STANDARD_ATTRIBUTES.addParser(SYS_WRITE_CALLS_COUNT, LongParser.PARSER);
		STANDARD_ATTRIBUTES.addParser(EXIT_CODE_LIST, StringParser.PARSER);
		STANDARD_ATTRIBUTES.addParser(ABORT_INFO, StringParser.PARSER);
		STANDARD_ATTRIBUTES.addParser(CORE_DUMP_FLAG, IntegerParser.PARSER);
		STANDARD_ATTRIBUTES.addParser(PROCESS_EXIT_CODE, IntegerParser.PARSER);
		STANDARD_ATTRIBUTES.addParser(NODE_SCHEDULING_DISCIPLINE, IntegerParser.PARSER);
		STANDARD_ATTRIBUTES.addParser(POSIX_NICE_VALUE, IntegerParser.PARSER);
		STANDARD_ATTRIBUTES.addParser(APPLICATION_ID, StringParser.PARSER);
		STANDARD_ATTRIBUTES.addParser(USER_ID, StringParser.PARSER);
		STANDARD_ATTRIBUTES.addParser(PROCESS_ID, StringParser.PARSER);
		STANDARD_ATTRIBUTES.addParser(JOB_ID, StringParser.PARSER);
		STANDARD_ATTRIBUTES.addParser(GROUP_ID, StringParser.PARSER);
		STANDARD_ATTRIBUTES.addParser(NODE_ID, StringParser.PARSER);
		STANDARD_ATTRIBUTES.addParser(PARENT_JOB_ID, StringParser.PARSER);
		STANDARD_ATTRIBUTES.addParser(JOB_PROJECT_ID, StringParser.PARSER);
		STANDARD_ATTRIBUTES.addParser(PARENT_PROCESS_ID, StringParser.PARSER);
		STANDARD_ATTRIBUTES.addParser(PROCESS_NAME, StringParser.PARSER);
		
		STANDARD_ATTRIBUTES.addParser(BOOT_MEM_PERCENTAGE, StringParser.PARSER);
		STANDARD_ATTRIBUTES.addParser(BOOT_FREE_MEM_CONTENTS, LongParser.PARSER);
		STANDARD_ATTRIBUTES.addParser(CURRENT_FREE_MEM_CONTENTS, LongParser.PARSER);
		
		STANDARD_ATTRIBUTES.addParser(ACTIVE_MEM_TOTAL, LongParser.PARSER);
		STANDARD_ATTRIBUTES.addParser(ACTIVE_FILE_MEM_TOTAL, LongParser.PARSER);
		STANDARD_ATTRIBUTES.addParser(INACTIVE_MEM_TOTAL, LongParser.PARSER);
		STANDARD_ATTRIBUTES.addParser(INACTIVE_FILE_MEM_TOTAL, LongParser.PARSER);
		STANDARD_ATTRIBUTES.addParser(KERNEL_MEM_TOTAL, LongParser.PARSER);
		
		STANDARD_ATTRIBUTES.addParser(HUGEPAGES_NAME, StringParser.PARSER);
		STANDARD_ATTRIBUTES.addParser(HUGEPAGES_SIZE, LongParser.PARSER);
		STANDARD_ATTRIBUTES.addParser(HUGEPAGES_SIZE_UNIT, StringParser.PARSER);
		STANDARD_ATTRIBUTES.addParser(HUGEPAGES_COUNT, IntegerParser.PARSER);
		STANDARD_ATTRIBUTES.addParser(SURPLUS_HUGEPAGES_COUNT, IntegerParser.PARSER);
		STANDARD_ATTRIBUTES.addParser(UNALLOCATED_HUGEPAGES_COUNT, IntegerParser.PARSER);
		STANDARD_ATTRIBUTES.addParser(RESERVED_HUGEPAGES_COUNT, IntegerParser.PARSER);
		
		STANDARD_ATTRIBUTES.addParser(NODEZONES_NAME, StringParser.PARSER);
		STANDARD_ATTRIBUTES.addParser(NODEZONES_NUMBER, StringParser.PARSER);
		STANDARD_ATTRIBUTES.addParser(NODEZONES_TYPE, StringParser.PARSER);
		STANDARD_ATTRIBUTES.addParser(NODEZONES_DATA, StringParser.PARSER);
		
		STANDARD_ATTRIBUTES.addParser(ERROR_TRACEBACK, StringParser.PARSER);
		STANDARD_ATTRIBUTES.addParser(ERROR_TYPE, StringParser.PARSER);
		STANDARD_ATTRIBUTES.addParser(ERROR_VALUE, StringParser.PARSER);
		STANDARD_ATTRIBUTES.addParser(ERROR_NID, StringParser.PARSER);
		STANDARD_ATTRIBUTES.addParser(ERROR_CNAME, StringParser.PARSER);
		STANDARD_ATTRIBUTES.addParser(ERROR_INFO, StringParser.PARSER);
	}
	
	
	protected Logger log;
	private boolean parse_timezone=true;
	
	/**
	 * The full parse pattern is based on the format specification given in Chapter 9 of the Cray document entitled
	 * Manage System Software for Cray Linux Environment, S-2393-5202axx. 
	 */
	private static final Pattern full_parse_pattern = Pattern.compile("\\S+ (?<TIMESTAMP>\\S+) (?<HOSTNAME>\\S+) (?<RECORDTYPE>\\S+) (?<TAG>\\S+) (?<SUBMISSION>\\S+)"
		+ " \\S+ " + "uid: (?<USERID>\\S+), apid: (?<APID>\\S+), jobid: (?<PBSID>\\S+), cmdname: (?<APRUNCMD>\\S+), (?<PLUGINS>.*)");
	
	/**
	 * This parse pattern is based on the format specification followed by the ARCHER TDS data file located at /work/rurtest. 
	 */
	private static final Pattern parse_pattern = Pattern.compile("uid: (?<USERID>\\S+), apid: (?<APID>\\S+), jobid: (?<PBSID>\\S+), "
		+ "cmdname: (?<APRUNCMD>\\S*), (?<PLUGINS>.*)");
	
    private static final Pattern plugin_pattern = Pattern.compile("plugin: (?<PLUGINNAME>\\S+) (([{\\[](?<ATTRS>.*)[\\]}])|(?<TIMESTAMP>.*))(,\\s)?");
    
    private static final SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssz");
    private static final SimpleDateFormat df_no_tz = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
    private static final Pattern timestamp_pattern = Pattern.compile("APP_START (?<START>\\S+) APP_STOP (?<STOP>\\S+)");
    
    private static final Pattern attribute_pattern = Pattern.compile("['\"](?<ATTRNAME>\\S+)['\"][,:] ([{](?<SUBATTRS>[^}]+)|[\\[](?<ATTRLIST>[^\\]]+)|['\"](?<ATTRSTRING>[^\"]+)|(?<ATTRVALUE>[^,]+))");
    
    private static final Pattern sub_attribute_pattern = Pattern.compile("['\"](?<SUBATTRNAME>\\S+)['\"][,:] (['\"]{1}(?<SUBATTRSTRING>[^\"]+)|(?<SUBATTRVALUE>[^,]+))");
    		
    
	@Override
	/**
	 * Parse a single entry from the RUR log.
	 * 
	 * @param map, the rur attribute properties
	 * @param record, the raw rur string
	 * @return true if rur entry is successfully parsed
	 * @throws AccountingParseException
	 */
	public boolean parse(PropertyMap map, String record) throws AccountingParseException {
		
		record = record.trim();
		
		if (record.length() == 0) {
			return false;
		}
		log.debug("rur record is '" + record + "'.");
		
		// parse the record into the declared properties and set them in the property map
		Matcher m = full_parse_pattern.matcher(record);
		if (!m.matches()) {
			m = parse_pattern.matcher(record);
		}
		
		if (m.matches()) {
			
			// check record type if it exists
			try {
				String rec_type = m.group("RECORDTYPE");
				if (!rec_type.equals("RUR")) {
					return false;
				}
			} catch (IllegalArgumentException x) {
				log.debug("rur record does not contain record type.");
			}
			
			String apid = m.group("APID");
			map.setProperty(ALPS_ID, Integer.valueOf(apid));		
			map.setProperty(PBS_ID, m.group("PBSID"));			
			Matcher plugin_matcher = plugin_pattern.matcher(m.group("PLUGINS"));
			while (plugin_matcher.find()) {
				
			
				String name = plugin_matcher.group("PLUGINNAME");
				PropertyTag<String> plugin_tag = (PropertyTag<String>) plugin_reg.find(name);
				
				
				if (plugin_tag == null ) {
					log.debug("plugin " + name + " not supported.");
					continue;
				}
				map.setProperty(plugin_tag, "Y");
				if (name.equals(TIMESTAMP_PLUGIN_NAME)) {
					Matcher ts_matcher = timestamp_pattern.matcher(plugin_matcher.group("TIMESTAMP"));
					if (ts_matcher.find()) {
						SimpleDateFormat rur_df = parse_timezone ? df : df_no_tz;
						try {
							Date start = rur_df.parse(ts_matcher.group("START"));
							map.setProperty(START_TIMESTAMP, start);
						} catch (ParseException e) {
							throw new AccountingParseException("bad start date format", e);
						}
						try {
							Date stop = rur_df.parse(ts_matcher.group("STOP"));
							map.setProperty(STOP_TIMESTAMP, stop);
						} catch (ParseException e) {
							throw new AccountingParseException("bad stop date format", e);
						}
					}
					else {
						throw new AccountingParseException("bad timestamp format");
					}
					continue;
				}
				
				Matcher attr_matcher = attribute_pattern.matcher(plugin_matcher.group("ATTRS"));
				while (attr_matcher.find()) {
				
					String alias = attr_matcher.group("ATTRNAME");
					AttributePropertyTag<?> tag = AttributePropertyTag.findAttribute(rur_reg, alias, true);
					if (null != tag) {
						name = tag.getName();
						
						String value = attr_matcher.group("ATTRVALUE");
						if (null == value) {
							value = attr_matcher.group("ATTRSTRING");
							if (null == value) {
								value = attr_matcher.group("ATTRLIST");
								if (null == value) {
									value = attr_matcher.group("SUBATTRS");
									if (null != value) {
										parseSubattrs(map, tag, value);
									}
								}
							}
						}
						
						if (null == value) {
							throw new AccountingParseException("Cannot find value for attribute '" + name + "'.");
						}
					
						tag.setValue(STANDARD_ATTRIBUTES, map, name, value);
					}
					else {
						throw new AccountingParseException("Unrecognised attribute name '" + alias + "'.");
					}
				}
			}
			
		} else {
			throw new AccountingParseException("Unexpected line format");
		}
		
		return true;
	}

	
	
	
	public void parseSubattrs(PropertyMap map, AttributePropertyTag<?> attrParentTag, String attrs) throws AccountingParseException {
		
		// parse the record into the declared properties and set them in the property map
		Matcher m = sub_attribute_pattern.matcher(attrs);
		while (m.find()) {
					
		    String alias = m.group("SUBATTRNAME");
			AttributePropertyTag<?> tag = AttributePropertyTag.findAttribute(rur_reg, alias, true);
			if (null != tag) {
				String name = tag.getName();
						
				String value = m.group("SUBATTRVALUE");
				if (null == value) {
					value = m.group("SUBATTRSTRING");
				}
							
				if (null == value) {
					throw new AccountingParseException("Cannot find value for subattribute '" + name
						+ "' of attribute '" + attrParentTag.getName() + "'.");
				}
					
				tag.setValue(STANDARD_ATTRIBUTES, map, name, value);
			}
			
		}
			
	}

	@Override
	public PropertyFinder initFinder(AppContext ctx, PropertyFinder prev, String table) {
		log = ctx.getService(LoggerService.class).getLogger(getClass());
		MultiFinder finder = new MultiFinder();
		finder.addFinder(rur_reg);
		finder.addFinder(plugin_reg);
		finder.addFinder(StandardProperties.time);
		parse_timezone=ctx.getBooleanParameter(table+".parse_timezone", true);
		return finder;
	}


	@Override
	public boolean isComplete(ExpressionTargetContainer record) {
		// All supported plugins must be parsed
		PropertyTag<?>[] attrs = {ALPS_ID, ENERGY_PLUGIN,TASKSTATS_PLUGIN,MEMORY_PLUGIN,TIMESTAMP_PLUGIN};
		return super.isComplete(record, attrs);
	}


	@Override
	public void postComplete(ExpressionTargetContainer record) throws Exception {
		// do nothing
	}
	
	
	
}
