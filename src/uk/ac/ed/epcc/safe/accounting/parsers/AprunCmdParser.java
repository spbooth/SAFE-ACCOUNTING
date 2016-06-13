package uk.ac.ed.epcc.safe.accounting.parsers;

import java.util.Iterator;
import java.util.Map.Entry;

import uk.ac.ed.epcc.safe.accounting.properties.AprunPropertyTag;
import uk.ac.ed.epcc.safe.accounting.properties.PropertyFinder;
import uk.ac.ed.epcc.safe.accounting.properties.PropertyMap;
import uk.ac.ed.epcc.safe.accounting.properties.PropertyRegistry;
import uk.ac.ed.epcc.safe.accounting.properties.PropertyTag;
import uk.ac.ed.epcc.safe.accounting.reference.IndexedTag;
import uk.ac.ed.epcc.safe.accounting.reference.ReferenceTag;
import uk.ac.ed.epcc.safe.accounting.update.AbstractPropertyContainerParser;
import uk.ac.ed.epcc.safe.accounting.update.AccountingParseException;
import uk.ac.ed.epcc.safe.accounting.update.AutoTable;
import uk.ac.ed.epcc.safe.accounting.update.StringSplitter;
import uk.ac.ed.epcc.webapp.AppContext;

/** Parser for aprun commands
 * 
 * @author mrb
 *
 */

public class AprunCmdParser extends AbstractPropertyContainerParser  {
	
	private static final PropertyRegistry aprun_reg = new PropertyRegistry("aprun", "Properties from an aprun command");
	
	
	public static final IndexedTag parent_tag = new IndexedTag(aprun_reg, "Parent", null, null);
	@AutoTable(target=String.class)
	public static final AprunPropertyTag<String> ARCHITECTURE = new AprunPropertyTag<String>(aprun_reg, "architecture", new String[]{"-a", "--architecture"}, String.class);
	@AutoTable(target=Boolean.class)
	public static final AprunPropertyTag<Boolean> BYPASS_APP_TRANSFER = new AprunPropertyTag<Boolean>(aprun_reg, "bypass_app_transfer", new String[]{"-b", "--bypass-app-transfer"}, Boolean.class);
	@AutoTable(target=Boolean.class)
	public static final AprunPropertyTag<Boolean> BATCH_ARGUMENTS = new AprunPropertyTag<Boolean>(aprun_reg, "batch_args", new String[]{"-B", "--batch-args"}, Boolean.class);
	@AutoTable(target=Boolean.class)
	public static final AprunPropertyTag<Boolean> RECONNECT = new AprunPropertyTag<Boolean>(aprun_reg, "reconnect", new String[]{"-C", "--reconnect"}, Boolean.class);
	@AutoTable(target=String.class)
	public static final AprunPropertyTag<String> CPU_BINDING = new AprunPropertyTag<String>(aprun_reg, "cpu_binding", new String[]{"-cc", "--cpu-binding"}, String.class);
	@AutoTable(target=String.class)
	public static final AprunPropertyTag<String> CPU_BINDING_FILE = new AprunPropertyTag<String>(aprun_reg, "cpu_binding_file", new String[]{"-cp", "--cpu-binding-file"}, String.class);
	@AutoTable(target=String.class)
	public static final AprunPropertyTag<String> CPUS_PER_PE = new AprunPropertyTag<String>(aprun_reg, "cpus_per_pe", new String[]{"-d", "--cpus-per-pe"}, String.class);
	@AutoTable(target=String.class)
	public static final AprunPropertyTag<String> DEBUG = new AprunPropertyTag<String>(aprun_reg, "debug", new String[]{"-D", "--debug"}, String.class);
	@AutoTable(target=String.class)
	public static final AprunPropertyTag<String> ENVIRONMENT = new AprunPropertyTag<String>(aprun_reg, "environment", new String[]{"-e"}, String.class);
	@AutoTable(target=String.class)
	public static final AprunPropertyTag<String> EXCLUDE_NODE_LIST = new AprunPropertyTag<String>(aprun_reg, "exclude_node_list", new String[]{"-E", "--exclude-node-list"}, String.class);
	@AutoTable(target=String.class)
	public static final AprunPropertyTag<String> EXCLUDE_NODE_LIST_FILE = new AprunPropertyTag<String>(aprun_reg, "exclude_node_list_file", new String[]{"--exclude-node-list-file"}, String.class);
	@AutoTable(target=String.class)
	public static final AprunPropertyTag<String> CPUS_PER_CU = new AprunPropertyTag<String>(aprun_reg, "cpus", new String[]{"-j", "--CPUs"}, String.class);
	@AutoTable(target=Boolean.class)
	public static final AprunPropertyTag<Boolean> XEON_PHI_PLACEMENT = new AprunPropertyTag<Boolean>(aprun_reg, "xeon_phi_placement", new String[]{"-k"}, Boolean.class);
	@AutoTable(target=String.class)
	public static final AprunPropertyTag<String> NODE_LIST = new AprunPropertyTag<String>(aprun_reg, "node_list", new String[]{"-L", "--node-list"}, String.class);
	@AutoTable(target=String.class)
	public static final AprunPropertyTag<String> NODE_LIST_FILE = new AprunPropertyTag<String>(aprun_reg, "node_list_file", new String[]{"-l", "--node-list-file"}, String.class);
	@AutoTable(target=String.class)
	public static final AprunPropertyTag<String> MEMORY_PER_PE = new AprunPropertyTag<String>(aprun_reg, "memory_per_pe", new String[]{"-m", "--memory-per-pe"}, String.class);
	// TODO: shouldn't need this field to be unique
	@AutoTable(target=String.class, unique=true)
	public static final AprunPropertyTag<String> PE_COUNT = new AprunPropertyTag<String>(aprun_reg, "pes", new String[]{"-n", "--pes"}, String.class);
	@AutoTable(target=String.class)
	public static final AprunPropertyTag<String> PES_PER_NODE = new AprunPropertyTag<String>(aprun_reg, "pes_per_node", new String[]{"-N", "--pes-per-node"}, String.class);
	@AutoTable(target=String.class)
	public static final AprunPropertyTag<String> ACCESS_MODE = new AprunPropertyTag<String>(aprun_reg, "access_mode", new String[]{"F", "--access-mode"}, String.class);
	@AutoTable(target=String.class)
	public static final AprunPropertyTag<String> PROTECTION_DOMAIN = new AprunPropertyTag<String>(aprun_reg, "protection_domain", new String[]{"-p", "--protection-domain"}, String.class);
	@AutoTable(target=String.class)
	public static final AprunPropertyTag<String> GOVERNOR_NAME = new AprunPropertyTag<String>(aprun_reg, "p_governor", new String[]{"--p-governor"}, String.class);
	@AutoTable(target=String.class)
	public static final AprunPropertyTag<String> P_STATE = new AprunPropertyTag<String>(aprun_reg, "p_state", new String[]{"--p-state"}, String.class);
	@AutoTable(target=Boolean.class)
	public static final AprunPropertyTag<Boolean> QUIET = new AprunPropertyTag<Boolean>(aprun_reg, "quiet", new String[]{"-q", "--quiet", "--silent"}, Boolean.class);
	@AutoTable(target=String.class)
	public static final AprunPropertyTag<String> SPECIALIZED_CPUS = new AprunPropertyTag<String>(aprun_reg, "specialized_cpus", new String[]{"-r", "--specialized-cpus"}, String.class);
	@AutoTable(target=String.class)
	public static final AprunPropertyTag<String> RELAUNCH = new AprunPropertyTag<String>(aprun_reg, "relaunch", new String[]{"-R", "--relaunch"}, String.class);
	@AutoTable(target=String.class)
	public static final AprunPropertyTag<String> PES_PER_NUMA_NODE = new AprunPropertyTag<String>(aprun_reg, "pes_per_numa_node", new String[]{"-S", "--pes-per-numa-node"}, String.class);
	@AutoTable(target=String.class)
	public static final AprunPropertyTag<String> NUMA_NODE_LIST = new AprunPropertyTag<String>(aprun_reg, "numa_node_list", new String[]{"-sl", "--numa-node-list"}, String.class);
	@AutoTable(target=String.class)
	public static final AprunPropertyTag<String> NUMA_NODES_PER_NODE = new AprunPropertyTag<String>(aprun_reg, "numa_nodes_per_node", new String[]{"-sn", "--numa-nodes-per-node"}, String.class);
	@AutoTable(target=Boolean.class)
	public static final AprunPropertyTag<Boolean> STRICT_MEMORY_CONTAINMENT = new AprunPropertyTag<Boolean>(aprun_reg, "strict_memory_containment", new String[]{"-ss", "--ss", "--strict-memory-containment"}, Boolean.class);
	@AutoTable(target=Boolean.class)
	public static final AprunPropertyTag<Boolean> SYNCHRONIZE_OUTPUT = new AprunPropertyTag<Boolean>(aprun_reg, "sync_output", new String[]{"-T", "--sync-output"}, Boolean.class);
	@AutoTable(target=String.class)
	public static final AprunPropertyTag<String> CPU_TIME_LIMIT = new AprunPropertyTag<String>(aprun_reg, "cpu_time_limit", new String[]{"-t", "--cpu-time-limit"}, String.class);
	@AutoTable(target=String.class, length=512)
	public static final AprunPropertyTag<String> APP_EXE_PATH = new AprunPropertyTag<String>(aprun_reg, "app_exe", null, String.class);
	@AutoTable(target=String.class, length=512)
	public static final AprunPropertyTag<String> APP_ATTR_LIST = new AprunPropertyTag<String>(aprun_reg, "app_attrs", null, String.class);
	
	
	private static final char CHAR_COLON = ':';
	private static final char CHAR_WHITESPACE = ' ';
	private static final char CHAR_HYPHEN = '-';
	private static final String STRING_COLON = new String(new char[]{CHAR_COLON});
	
	/// This attribute is used by the parse and parseNextElement methods only.
    private String cmd = null;
    
	@Override
	/**
	 * Split the aprun command data into an iterable list of individual aprun commands,
	 * some of these will belong to the same alps log record (i.e., MPMD).
	 * 
	 * @param update, list of aprun commands separated by newline characters
	 * @return return a list of individual aprun commands that takes account of MPMD
	 */
	public Iterator<String> splitRecords(String update)
			throws AccountingParseException {
		
		StringSplitter cmds = new StringSplitter(update);
		String single_cmds = new String("");
		
		while (cmds.hasNext()) {
			
			String current_cmd = cmds.next();
			
			// strip the aprun command path
			String aprun_cmd_name = "aprun ";
			int i = current_cmd.indexOf(aprun_cmd_name);
			if (i > -1) {
				i += aprun_cmd_name.length();
				current_cmd = current_cmd.substring(i, current_cmd.length());
			}
			
			single_cmds += current_cmd;
			if (cmds.hasNext()) {
				single_cmds += " " + STRING_COLON + " ";
			}
				
		}		
		
		// split in case of MPMD
		// TODO: allow for the fact that different aprun commands belong to different alps log records
		return new StringSplitter(single_cmds, STRING_COLON);
	}
	
	
	/**
	 * Extract the next element (e.g., aprun command attribute name) from the
	 * raw aprun command string.
	 * 
	 * @param terminator, the character that signals the end of the element
	 * @return the extracted element
	 * @throws AccountingParseException
	 */
	private String parseNextElement(char terminator) throws AccountingParseException {
		int len = this.cmd.length();
		
		if (len > 0) {
			int i = 0;
			
			if (terminator == this.cmd.charAt(i)) {
				// skip terminator characters
				while (i < len && terminator == this.cmd.charAt(i)) {
					i += 1;
				}
			}
			
			if (i < len) {
				// assume this.cmd.charAt(i) returns first character of next element
				int i2 = this.cmd.indexOf(terminator, i);
				if (-1 == i2) {
					i2 = len;
				}
				
				String elem = new String(this.cmd.substring(i, i2));
				this.cmd = i2 < len ? this.cmd.substring(i2, len) : "";
				return elem;
			}
			else {
				throw new AccountingParseException("Error getting next element from command string, '" + this.cmd + "'.");
			}
		}
		else {
			throw new AccountingParseException("Error parsing next aprun element, empty command string.");
		}
		
	}
	
	
	/**
	 * Iterate through the aprun attribute registry and look for an attribute
	 * that uses an alias that matches attrAlias.
	 * 
	 * @param attrAlias
	 * @return null if alias not used by any tag in registry, otherwise
	 * return an appropriately constructed AprunPropertyTag<?> object.
	 */
	private AprunPropertyTag<?> findAprunPropertyTag(String attrAlias) {
		AprunPropertyTag<?> attrTag = null;
		
		boolean matchFound = false;
		Iterator<Entry<String, PropertyTag>> props = aprun_reg.getIterator();
		while (!matchFound && props.hasNext()) {
			PropertyTag prop = props.next().getValue();
			
			attrTag = (AprunPropertyTag<?>) aprun_reg.find(prop.getName());
			if (null != attrTag) {
				String[] aliases = attrTag.getAliases();
				
				if (null != aliases) {
					for (int i = 0; !matchFound && i < aliases.length; ++i) {
						matchFound = attrAlias.equals(aliases[i]);
					}
				}
			}
		}
		
		return matchFound ? attrTag : null;
	}
	
	
	@Override
	/**
	 * Parse a single aprun command, extracting the various elements, 
	 * aprun attribute names, aprun attribute parameter values, application executable path
	 * and the application executable arguments - this last element is extracted as a
	 * single string.
	 * 
	 * @param map, the aprun command properties
	 * @param record, the raw aprun command string
	 * @return true if aprun command is successfully parsed
	 * @throws AccountingParseException
	 */
	public boolean parse(PropertyMap map, String record) throws AccountingParseException {
		
		this.cmd = record.trim();
		
		if (this.cmd.length() == 0) {
			return false;
		}
		log.debug("aprun command is '" + this.cmd + "'.");
		
		// add in exception code
		boolean parseComplete = false;
		
		while (!parseComplete) {
			// extract next element from command string
			String attrName = parseNextElement(CHAR_WHITESPACE);
			if (null == attrName || attrName.isEmpty()) {
				throw new AccountingParseException("Missing aprun attribute, '" + this.cmd + "'.");
			}
			
			AprunPropertyTag<?> attrTag = findAprunPropertyTag(attrName);	
			if (null != attrTag) {
				// element is an aprun attribute
				Boolean attrFlag = new Boolean(true);
				if (attrTag.allow(attrFlag)) {
					// aprun attribute is a simple flag
					map.setProperty((AprunPropertyTag<Boolean>) attrTag, attrFlag);
				}
				else {
					// aprun attribute has arguments
					if (this.cmd.isEmpty()) {
						throw new AccountingParseException("Missing argument for aprun attribute, '" + attrName + "'.");
					}
						
					// TODO: allow arguments to contain whitespaces
					// assume arguments contain no whitespaces
					String attrValue = parseNextElement(CHAR_WHITESPACE);
					map.setProperty((AprunPropertyTag<String>) attrTag, attrValue);
				}
			}
			else {
				if (CHAR_HYPHEN == attrName.charAt(0)) {
					throw new AccountingParseException("Unrecognised aprun attribute, '" + attrName + "'.");
				}
				
				// assume element is the application executable path
				PropertyTag<?> appTag = aprun_reg.find(APP_EXE_PATH.getName());
				map.setProperty((PropertyTag<String>) appTag, attrName);
				
				this.cmd = this.cmd.trim();
				
				// assume that any subsequent elements are application attributes
				PropertyTag<?> appAttrsTag = aprun_reg.find(APP_ATTR_LIST.getName());
				map.setProperty((PropertyTag<String>) appAttrsTag, this.cmd);
				
				this.cmd = null;
				parseComplete = true;
			}
		}
		
		
		// iterate through the aprun attribute registry and look for those attributes
		// that have not yet been added to map
		Iterator<Entry<String, PropertyTag>> props = aprun_reg.getIterator();
		while (props.hasNext()) {
			PropertyTag prop = props.next().getValue();
			if (null == map.getProperty(prop)) {
				
				// this aprun attribute was not set on command line
				AprunPropertyTag<?> attrTag = (AprunPropertyTag<?>) aprun_reg.find(prop.getName());
				if (null != attrTag) {		
					Boolean attrFlag = new Boolean(false);
					if (attrTag.allow(attrFlag)) {
						// aprun attribute is a simple flag
						map.setProperty((AprunPropertyTag<Boolean>) attrTag, attrFlag);
					}
					else {
						map.setProperty((AprunPropertyTag<String>) attrTag, new String(""));
					}
				}
				
			}
		}
			
		return true;
	}

	@Override
	public PropertyFinder initFinder(AppContext ctx, PropertyFinder prev, String table) {
		return super.initFinder(ctx, aprun_reg);
	}

	
	
}
