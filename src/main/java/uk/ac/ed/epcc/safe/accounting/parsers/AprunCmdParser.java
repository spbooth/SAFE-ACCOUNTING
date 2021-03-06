package uk.ac.ed.epcc.safe.accounting.parsers;

import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;

import uk.ac.ed.epcc.safe.accounting.expr.DerivedPropertyMap;
import uk.ac.ed.epcc.safe.accounting.parsers.value.BooleanParser;
import uk.ac.ed.epcc.safe.accounting.parsers.value.IntegerParser;
import uk.ac.ed.epcc.safe.accounting.parsers.value.StringParser;
import uk.ac.ed.epcc.safe.accounting.properties.AttributePropertyTag;
import uk.ac.ed.epcc.safe.accounting.properties.PropertyFinder;
import uk.ac.ed.epcc.safe.accounting.properties.PropertyRegistry;
import uk.ac.ed.epcc.safe.accounting.update.AbstractPropertyContainerParser;
import uk.ac.ed.epcc.safe.accounting.update.AccountingParseException;
import uk.ac.ed.epcc.safe.accounting.update.AutoTable;
import uk.ac.ed.epcc.safe.accounting.update.StreamLineSplitter;
import uk.ac.ed.epcc.safe.accounting.update.StringSplitter;
import uk.ac.ed.epcc.webapp.AppContext;
import uk.ac.ed.epcc.webapp.logging.Logger;
import uk.ac.ed.epcc.webapp.logging.LoggerService;

/** Parser for aprun commands
 * 
 * @author mrb
 * @param <T>
 *
 */

public class AprunCmdParser<T> extends AbstractPropertyContainerParser  {
	
	public AprunCmdParser(AppContext conn) {
		super(conn);
	}

	private static final PropertyRegistry aprun_reg = new PropertyRegistry("aprun", "Properties from an aprun command");
	
	@AutoTable(target=Integer.class, unique=true)
	public static final AttributePropertyTag<Integer> ALPS_ID = new AttributePropertyTag<>(aprun_reg, "alps_id", new String[]{"apid"}, Integer.class, 0);
	@AutoTable(target=Integer.class, unique=true)
	public static final AttributePropertyTag<Integer> APRUN_CMD_NUM = new AttributePropertyTag<>(aprun_reg, "aprun_cmd_num", new String[]{"apnum"}, Integer.class, 0);
	
	@AutoTable(target=String.class)
	public static final AttributePropertyTag<String> ARCHITECTURE = new AttributePropertyTag<>(aprun_reg, "architecture", new String[]{"-a", "--architecture"}, String.class, "");
	@AutoTable(target=Boolean.class)
	public static final AttributePropertyTag<Boolean> BYPASS_APP_TRANSFER = new AttributePropertyTag<>(aprun_reg, "bypass_app_transfer", new String[]{"-b", "--bypass-app-transfer"}, Boolean.class, false);
	@AutoTable(target=Boolean.class)
	public static final AttributePropertyTag<Boolean> BATCH_ARGUMENTS = new AttributePropertyTag<>(aprun_reg, "batch_args", new String[]{"-B", "--batch-args"}, Boolean.class, false);
	@AutoTable(target=Boolean.class)
	public static final AttributePropertyTag<Boolean> RECONNECT = new AttributePropertyTag<>(aprun_reg, "reconnect", new String[]{"-C", "--reconnect"}, Boolean.class, false);
	@AutoTable(target=String.class,length=128)
	public static final AttributePropertyTag<String> CPU_BINDING = new AttributePropertyTag<>(aprun_reg, "cpu_binding", new String[]{"-cc", "--cc", "--cpu-binding"}, String.class, "");
	@AutoTable(target=String.class)
	public static final AttributePropertyTag<String> CPU_BINDING_FILE = new AttributePropertyTag<>(aprun_reg, "cpu_binding_file", new String[]{"-cp", "--cp", "--cpu-binding-file"}, String.class, "");
	@AutoTable(target=Integer.class)
	public static final AttributePropertyTag<Integer> CPUS_PER_PE = new AttributePropertyTag<>(aprun_reg, "cpus_per_pe", new String[]{"-d", "--cpus-per-pe"}, Integer.class, 1);
	@AutoTable(target=Integer.class)
	public static final AttributePropertyTag<Integer> DEBUG = new AttributePropertyTag<>(aprun_reg, "debug", new String[]{"-D", "--debug"}, Integer.class, 0);
	@AutoTable(target=String.class)
	public static final AttributePropertyTag<String> ENVIRONMENT = new AttributePropertyTag<>(aprun_reg, "environment", new String[]{"-e", "env"}, String.class, "");
	@AutoTable(target=String.class,length=128)
	public static final AttributePropertyTag<String> EXCLUDE_NODE_LIST = new AttributePropertyTag<>(aprun_reg, "exclude_node_list", new String[]{"-E", "--exclude-node-list"}, String.class, "");
	@AutoTable(target=String.class)
	public static final AttributePropertyTag<String> EXCLUDE_NODE_LIST_FILE = new AttributePropertyTag<>(aprun_reg, "exclude_node_list_file", new String[]{"--exclude-node-list-file"}, String.class, "");
	@AutoTable(target=Integer.class)
	public static final AttributePropertyTag<Integer> CPUS_PER_CU = new AttributePropertyTag<>(aprun_reg, "cpus", new String[]{"-j", "--CPUs"}, Integer.class, 0);
	@AutoTable(target=Boolean.class)
	public static final AttributePropertyTag<Boolean> XEON_PHI_PLACEMENT = new AttributePropertyTag<>(aprun_reg, "xeon_phi_placement", new String[]{"-k"}, Boolean.class, false);
	@AutoTable(target=String.class,length=128)
	public static final AttributePropertyTag<String> NODE_LIST = new AttributePropertyTag<>(aprun_reg, "node_list", new String[]{"-L", "--node-list"}, String.class, "");
	@AutoTable(target=String.class)
	public static final AttributePropertyTag<String> NODE_LIST_FILE = new AttributePropertyTag<>(aprun_reg, "node_list_file", new String[]{"-l", "--node-list-file"}, String.class, "");
	@AutoTable(target=String.class)
	public static final AttributePropertyTag<String> MEMORY_PER_PE = new AttributePropertyTag<>(aprun_reg, "memory_per_pe", new String[]{"-m", "--memory-per-pe"}, String.class, "2.6G");
	@AutoTable(target=Integer.class)
	public static final AttributePropertyTag<Integer> PE_COUNT = new AttributePropertyTag<>(aprun_reg, "pes", new String[]{"-n", "--pes"}, Integer.class, 1);
	@AutoTable(target=Integer.class)
	public static final AttributePropertyTag<Integer> PES_PER_NODE = new AttributePropertyTag<>(aprun_reg, "pes_per_node", new String[]{"-N", "--pes-per-node"}, Integer.class, 24);
	@AutoTable(target=String.class)
	public static final AttributePropertyTag<String> ACCESS_MODE = new AttributePropertyTag<>(aprun_reg, "access_mode", new String[]{"F", "--access-mode"}, String.class, "exclusive");
	@AutoTable(target=String.class)
	public static final AttributePropertyTag<String> PROTECTION_DOMAIN = new AttributePropertyTag<>(aprun_reg, "protection_domain", new String[]{"-p", "--protection-domain"}, String.class, "");
	@AutoTable(target=String.class)
	public static final AttributePropertyTag<String> GOVERNOR_NAME = new AttributePropertyTag<>(aprun_reg, "p_governor", new String[]{"--p-governor"}, String.class, "performance");
	@AutoTable(target=String.class)
	public static final AttributePropertyTag<String> P_STATE = new AttributePropertyTag<>(aprun_reg, "p_state", new String[]{"--p-state"}, String.class, "");
	@AutoTable(target=Boolean.class)
	public static final AttributePropertyTag<Boolean> QUIET = new AttributePropertyTag<>(aprun_reg, "quiet", new String[]{"-q", "--quiet", "--silent"}, Boolean.class, false);
	@AutoTable(target=String.class)
	public static final AttributePropertyTag<String> SPECIALIZED_CPUS = new AttributePropertyTag<>(aprun_reg, "specialized_cpus", new String[]{"-r", "--specialized-cpus"}, String.class, "");
	@AutoTable(target=String.class)
	public static final AttributePropertyTag<String> RELAUNCH = new AttributePropertyTag<>(aprun_reg, "relaunch", new String[]{"-R", "--relaunch"}, String.class, "");
	@AutoTable(target=Integer.class)
	public static final AttributePropertyTag<Integer> PES_PER_NUMA_NODE = new AttributePropertyTag<>(aprun_reg, "pes_per_numa_node", new String[]{"-S", "--pes-per-numa-node"}, Integer.class, 12);
	@AutoTable(target=String.class)
	public static final AttributePropertyTag<String> NUMA_NODE_LIST = new AttributePropertyTag<>(aprun_reg, "numa_node_list", new String[]{"-sl", "--sl", "--numa-node-list"}, String.class, "");
	@AutoTable(target=Integer.class)
	public static final AttributePropertyTag<Integer> NUMA_NODES_PER_NODE = new AttributePropertyTag<>(aprun_reg, "numa_nodes_per_node", new String[]{"-sn", "--sn", "--numa-nodes-per-node"}, Integer.class, 2);
	@AutoTable(target=Boolean.class)
	public static final AttributePropertyTag<Boolean> STRICT_MEMORY_CONTAINMENT = new AttributePropertyTag<>(aprun_reg, "strict_memory_containment", new String[]{"-ss", "--ss", "--strict-memory-containment"}, Boolean.class, false);
	@AutoTable(target=Boolean.class)
	public static final AttributePropertyTag<Boolean> SYNCHRONIZE_OUTPUT = new AttributePropertyTag<>(aprun_reg, "sync_output", new String[]{"-T", "--sync-output"}, Boolean.class, false);
	@AutoTable(target=Integer.class)
	public static final AttributePropertyTag<Integer> CPU_TIME_LIMIT = new AttributePropertyTag<>(aprun_reg, "cpu_time_limit", new String[]{"-t", "--cpu-time-limit"}, Integer.class, 0);
	@AutoTable(target=String.class, length=512)
	public static final AttributePropertyTag<String> APP_EXE_PATH = new AttributePropertyTag<>(aprun_reg, "app_exe_path", null, String.class, "");
	@AutoTable(target=String.class, length=128)
	public static final AttributePropertyTag<String> APP_EXE_NAME = new AttributePropertyTag<>(aprun_reg, "app_exe_name", null, String.class, "");
	@AutoTable(target=String.class, length=512)
	public static final AttributePropertyTag<String> APP_ATTR_LIST = new AttributePropertyTag<>(aprun_reg, "app_attrs", null, String.class, "");
	@AutoTable(target=Integer.class)
	public static final AttributePropertyTag<Integer> APP_ID = new AttributePropertyTag<>(aprun_reg, "app_id", null, Integer.class, 0);
	
	private static final MakerMap STANDARD_ATTRIBUTES = new MakerMap();
	static {
		STANDARD_ATTRIBUTES.addParser(ALPS_ID, IntegerParser.PARSER);
		STANDARD_ATTRIBUTES.addParser(APRUN_CMD_NUM, IntegerParser.PARSER);
		STANDARD_ATTRIBUTES.addParser(ARCHITECTURE, StringParser.PARSER);
		STANDARD_ATTRIBUTES.addParser(BYPASS_APP_TRANSFER, BooleanParser.PARSER);
		STANDARD_ATTRIBUTES.addParser(BATCH_ARGUMENTS, BooleanParser.PARSER);
		STANDARD_ATTRIBUTES.addParser(RECONNECT, BooleanParser.PARSER);
		STANDARD_ATTRIBUTES.addParser(CPU_BINDING, StringParser.PARSER);
		STANDARD_ATTRIBUTES.addParser(CPU_BINDING_FILE, StringParser.PARSER);
		STANDARD_ATTRIBUTES.addParser(CPUS_PER_PE, IntegerParser.PARSER);
		STANDARD_ATTRIBUTES.addParser(DEBUG, IntegerParser.PARSER);
		STANDARD_ATTRIBUTES.addParser(ENVIRONMENT, StringParser.PARSER);
		STANDARD_ATTRIBUTES.addParser(EXCLUDE_NODE_LIST, StringParser.PARSER);
		STANDARD_ATTRIBUTES.addParser(EXCLUDE_NODE_LIST_FILE, StringParser.PARSER);
		STANDARD_ATTRIBUTES.addParser(CPUS_PER_CU, IntegerParser.PARSER);
		STANDARD_ATTRIBUTES.addParser(XEON_PHI_PLACEMENT, BooleanParser.PARSER);
		STANDARD_ATTRIBUTES.addParser(NODE_LIST, StringParser.PARSER);
		STANDARD_ATTRIBUTES.addParser(NODE_LIST_FILE, StringParser.PARSER);
		STANDARD_ATTRIBUTES.addParser(MEMORY_PER_PE, StringParser.PARSER);
		STANDARD_ATTRIBUTES.addParser(PE_COUNT, IntegerParser.PARSER);
		STANDARD_ATTRIBUTES.addParser(PES_PER_NODE, IntegerParser.PARSER);
		STANDARD_ATTRIBUTES.addParser(ACCESS_MODE, StringParser.PARSER);
		STANDARD_ATTRIBUTES.addParser(PROTECTION_DOMAIN, StringParser.PARSER);
		STANDARD_ATTRIBUTES.addParser(GOVERNOR_NAME, StringParser.PARSER);
		STANDARD_ATTRIBUTES.addParser(P_STATE, StringParser.PARSER);
		STANDARD_ATTRIBUTES.addParser(QUIET, BooleanParser.PARSER);
		STANDARD_ATTRIBUTES.addParser(SPECIALIZED_CPUS, StringParser.PARSER);
		STANDARD_ATTRIBUTES.addParser(RELAUNCH, StringParser.PARSER);
		STANDARD_ATTRIBUTES.addParser(PES_PER_NUMA_NODE, IntegerParser.PARSER);
		STANDARD_ATTRIBUTES.addParser(NUMA_NODE_LIST, StringParser.PARSER);
		STANDARD_ATTRIBUTES.addParser(NUMA_NODES_PER_NODE, IntegerParser.PARSER);
		STANDARD_ATTRIBUTES.addParser(STRICT_MEMORY_CONTAINMENT, BooleanParser.PARSER);
		STANDARD_ATTRIBUTES.addParser(SYNCHRONIZE_OUTPUT, BooleanParser.PARSER);
		STANDARD_ATTRIBUTES.addParser(CPU_TIME_LIMIT, IntegerParser.PARSER);
		STANDARD_ATTRIBUTES.addParser(APP_EXE_PATH, StringParser.PARSER);
		STANDARD_ATTRIBUTES.addParser(APP_EXE_NAME, StringParser.PARSER);
		STANDARD_ATTRIBUTES.addParser(APP_ID, IntegerParser.PARSER);
		STANDARD_ATTRIBUTES.addParser(APP_ATTR_LIST, StringParser.PARSER);
	}
	
	
	private static final char CHAR_COLON = ':';
	private static final char CHAR_SEMICOLON = ';';
	private static final char CHAR_WHITESPACE = ' ';
	private static final char CHAR_HYPHEN = '-';
	private static final char CHAR_EQUALS = '=';
	private static final char CHAR_COMMA = ',';
	private static final char CHAR_FORWARD_SLASH = '/';
	private static final String STRING_COLON = new String(new char[]{CHAR_COLON});
	private static final String STRING_SEMICOLON = new String(new char[]{CHAR_SEMICOLON});
	private static final String STRING_WHITESPACE = new String(new char[]{CHAR_WHITESPACE});
	private static final String STRING_EQUALS = new String(new char[]{CHAR_EQUALS});
	private static final String STRING_COMMA = new String(new char[]{CHAR_COMMA});
	private static final String REGEX_EQUALS = "(\\s*" + STRING_EQUALS + "\\s*)";
	private static final String REGEX_COMMA = "(\\s*" + STRING_COMMA + "\\s*)";
	
	public static final String APRUN_CMD_NAME = "aprun ";
	
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
	public Iterator<String> splitRecords(InputStream update)
			throws AccountingParseException {

		String full_cmd = new String("");
		String attrApid = null;

		// iterate through a set of aprun commands separated by newlines
		StreamLineSplitter cmds;
		try {
			cmds= new StreamLineSplitter(getContext(),update);

			while (cmds.hasNext()) {

				String current_cmd = cmds.next();

				// extract the apid (placed by NestedParsePolicy.postCreate method)
				// and strip the aprun command path
				int i = current_cmd.indexOf(ALPS_ID.getAlias(0));
				if (-1 == i) {
					throw new AccountingParseException("Error alps id not found in, '" + update + "'.");
				}
				else if (0 != i) {
					throw new AccountingParseException("Error alps id not found at start of, '" + update + "'.");
				}
				attrApid = current_cmd.substring(i, current_cmd.indexOf(CHAR_WHITESPACE));

				i = current_cmd.indexOf(APRUN_CMD_NAME);
				if (-1 == i) {
					throw new AccountingParseException("Error aprun command name not found in, '" + update + "'.");
				}
				i += APRUN_CMD_NAME.length();
				current_cmd = current_cmd.substring(i, current_cmd.length());

				// distinguish those colons that are being used to format a MPMD aprun command
				// from those colons that are simply used within the application argument list and
				// replace the latter with semi-colons
				i = 0;
				int len = current_cmd.length();
				do {
					i = current_cmd.indexOf(CHAR_COLON, i);
					if (-1 == i) {
						break;
					}

					int i2 = i+1;
					while (i2 < len && CHAR_WHITESPACE == current_cmd.charAt(i2)) {
						i2++;
					}

					if (len == i2) {
						current_cmd = current_cmd.substring(0, i) + STRING_WHITESPACE
								+ current_cmd.substring(i+1);
					}
					else if (CHAR_HYPHEN != current_cmd.charAt(i2)) {
						current_cmd = current_cmd.substring(0, i) + STRING_SEMICOLON
								+ current_cmd.substring(i+1);
					}

					i++;
				} while (i < len);


				// iterate through the individual commands that make up a MPMD aprun command
				Integer apnum = (-1 == current_cmd.indexOf(CHAR_COLON)) ? -1 : 0;
				StringSplitter single_cmds = new StringSplitter(current_cmd, STRING_COLON);

				while (single_cmds.hasNext()) {
					// prepend the alps id and aprun command number to the aprun command string
					// these two fields are unique to each aprun command stored in AprunCommandLog
					attrApid = attrApid.replace(CHAR_EQUALS, CHAR_WHITESPACE);

					String single_cmd = attrApid + " " + APRUN_CMD_NUM.getAlias(0) + " " + apnum.toString() + " ";
					single_cmd += single_cmds.next();

					full_cmd += single_cmd;
					if (single_cmds.hasNext()) {
						apnum += 1;
						full_cmd += " " + STRING_COLON + " ";
					}
				}
				attrApid = null;

				if (cmds.hasNext()) {
					full_cmd += " " + STRING_COLON + " ";
				}

			}	

			// remove extraneous spaces surrounding equals signs and commas
			// that are sometimes used within attribute arguments
			full_cmd = full_cmd.replaceAll(REGEX_EQUALS, STRING_EQUALS);
			full_cmd = full_cmd.replaceAll(REGEX_COMMA, STRING_COMMA);

			return new StringSplitter(full_cmd, STRING_COLON);
		} catch (IOException e) {
			throw new AccountingParseException(e);
		}
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
				i2 += 1; // skip terminator
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
	public boolean parse(DerivedPropertyMap map, String record) throws AccountingParseException {
		
		this.cmd = record.trim();
		
		if (this.cmd.length() == 0) {
			return false;
		}
		getLogger().debug("aprun command is '" + this.cmd + "'.");
		
		// add in exception code
		boolean parseComplete = false;
		
		while (!parseComplete) {
			// extract next element from command string
			String attrAlias = parseNextElement(CHAR_WHITESPACE);
			if (null == attrAlias || attrAlias.isEmpty()) {
				throw new AccountingParseException("Missing aprun attribute, '" + this.cmd + "'.");
			}
			
			AttributePropertyTag<?> attrTag = AttributePropertyTag.findAttribute(aprun_reg, attrAlias);	
			if (null != attrTag) {
				String attrName = attrTag.getName();
				// element is an aprun attribute
				Boolean attrFlag = new Boolean(true);
				if (attrTag.allow(attrFlag)) {
					// aprun attribute is a simple flag
					map.setProperty((AttributePropertyTag<Boolean>) attrTag, attrFlag);
				}
				else {
					// aprun attribute has arguments
					if (this.cmd.isEmpty()) {
						throw new AccountingParseException("Missing argument for aprun attribute, '" + attrName + "'.");
					}
					String attrValue = parseNextElement(CHAR_WHITESPACE);					
					attrTag.setValue(STANDARD_ATTRIBUTES, map, attrName, attrValue);
				}
			}
			else {
				if (CHAR_HYPHEN == attrAlias.charAt(0)) {
					// allow for the situation where there are no whitespaces
					// between attribute name and argument
					// note, this is only allowed for two-character attribute
					// names, where the first character is a hyphen
					String attrValue = attrAlias.substring(2, attrAlias.length());
					attrAlias = attrAlias.substring(0, 2);
					attrTag = AttributePropertyTag.findAttribute(aprun_reg, attrAlias);
					if (null != attrTag) {
						attrTag.setValue(STANDARD_ATTRIBUTES, map, attrTag.getName(), attrValue);
					}
					else {
						throw new AccountingParseException("Unrecognised aprun attribute, '" + attrAlias + "'.");
					}
				}
				else {
					// assume element is the application executable path
					String attrValue = attrAlias;
					String attrName = APP_EXE_PATH.getName();
					AttributePropertyTag<?> appPathTag = AttributePropertyTag.findAttribute(aprun_reg, attrName, true);
					appPathTag.setValue(STANDARD_ATTRIBUTES, map, attrName, attrValue);
					
					int i = attrValue.lastIndexOf(CHAR_FORWARD_SLASH);
					if (i > 0 && i < attrValue.length()-1) {
						// strip path
						attrValue = attrValue.substring(i+1, attrValue.length());
					}
					attrName = APP_EXE_NAME.getName();
					AttributePropertyTag<?> appNameTag = AttributePropertyTag.findAttribute(aprun_reg, attrName, true);
					appNameTag.setValue(STANDARD_ATTRIBUTES, map, attrName, attrValue);
					
					// set the app_id property to zero, which will not be changed,
					// should RegexLinkParsePolicy be unable to associate the
					// app_exe_name with an entry in the AprunApplication table
					attrName = APP_ID.getName();
					AttributePropertyTag<?> appIdTag = AttributePropertyTag.findAttribute(aprun_reg, attrName, true);
					appIdTag.setValue(STANDARD_ATTRIBUTES, map, attrName, "0");
					
					// assume that any subsequent elements are application attributes
					attrName = APP_ATTR_LIST.getName();
					attrValue = this.cmd.trim();
					AttributePropertyTag<?> appAttrsTag = AttributePropertyTag.findAttribute(aprun_reg, attrName, true);
					appAttrsTag.setValue(STANDARD_ATTRIBUTES, map, attrName, attrValue);
					
					this.cmd = null;
					parseComplete = true;
				}
			}
		}
		
		AttributePropertyTag.completePropertyMap(aprun_reg, STANDARD_ATTRIBUTES, map);
			
		return true;
	}

	@Override
	public PropertyFinder initFinder(PropertyFinder prev, String table) {
		
		return aprun_reg;
	}
	
	
	
}
