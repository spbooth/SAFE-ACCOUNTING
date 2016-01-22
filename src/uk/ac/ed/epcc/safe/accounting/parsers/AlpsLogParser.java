package uk.ac.ed.epcc.safe.accounting.parsers;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;



import uk.ac.ed.epcc.safe.accounting.UsageRecord;
import uk.ac.ed.epcc.safe.accounting.parsers.value.IntegerParser;
import uk.ac.ed.epcc.safe.accounting.parsers.value.StringParser;
import uk.ac.ed.epcc.safe.accounting.properties.InvalidPropertyException;
import uk.ac.ed.epcc.safe.accounting.properties.MultiFinder;
import uk.ac.ed.epcc.safe.accounting.properties.PropertyContainer;
import uk.ac.ed.epcc.safe.accounting.properties.PropertyFinder;
import uk.ac.ed.epcc.safe.accounting.properties.PropertyMap;
import uk.ac.ed.epcc.safe.accounting.properties.PropertyRegistry;
import uk.ac.ed.epcc.safe.accounting.properties.PropertyTag;
import uk.ac.ed.epcc.safe.accounting.update.AbstractPropertyContainerParser;
import uk.ac.ed.epcc.safe.accounting.update.AccountingParseException;
import uk.ac.ed.epcc.safe.accounting.update.AutoTable;
import uk.ac.ed.epcc.safe.accounting.update.IncrementalPropertyContainerParser;
import uk.ac.ed.epcc.webapp.AppContext;
import uk.ac.ed.epcc.webapp.logging.Logger;
import uk.ac.ed.epcc.webapp.logging.LoggerService;

/** Parser for alps syslog information
 * 
 * @author spb
 *
 */

public class AlpsLogParser extends AbstractPropertyContainerParser implements IncrementalPropertyContainerParser {

	private static final PropertyRegistry alps_reg = new PropertyRegistry("alps", "Properties from the alps log");
	@AutoTable(target=String.class, unique=true)
	public static final PropertyTag<String> BATCH_ID = new PropertyTag<String>(alps_reg, "batch_id", String.class, "Batch job id");
	@AutoTable(target=Integer.class, unique=true)
	public static final PropertyTag<Integer> ALPS_ID = new PropertyTag<Integer>(alps_reg, "apid", Integer.class, "Alps job id");
	@AutoTable(target=Integer.class, unique=true)
	public static final PropertyTag<Integer> USER = new PropertyTag<Integer>(alps_reg, "user", Integer.class, "User");
	@AutoTable(target=String.class, length=4096)
	public static final PropertyTag<String> CMD_LINE = new PropertyTag<String>(alps_reg, "cmd_line", String.class, "Command line");
	@AutoTable(target=String.class, length=512)
	public static final PropertyTag<String> CWD = new PropertyTag<String>(alps_reg, "cwd", String.class, "Current working directory");
	@AutoTable(target=String.class)
	public static final PropertyTag<String> NODE_LIST = new PropertyTag<String>(alps_reg, "node_list", String.class, "Node list");
	@AutoTable(target=Integer.class)
	public static final PropertyTag<Integer> NUM_NODES = new PropertyTag<Integer>(alps_reg, "num_nodes", Integer.class, "Number of nodes");
	@AutoTable(target=String.class)
	public static final PropertyTag<String> HOSTNAME = new PropertyTag<String>(alps_reg, "hostname", String.class, "Hostname");
	@AutoTable(target=Date.class)
	public static final PropertyTag<Date> APRUN_START_TIMESTAMP = new PropertyTag<Date>(alps_reg, "aprunStartTime", Date.class, "Timestamp of starting aprun record");
	@AutoTable(target=Date.class)
	public static final PropertyTag<Date> APSYS_END_TIMESTAMP = new PropertyTag<Date>(alps_reg, "apsysEndTime", Date.class, "Timestamp of finishing apsys record");
	private static final MakerMap STANDARD_ATTRIBUTES = new MakerMap();
	static {
		STANDARD_ATTRIBUTES.addParser(BATCH_ID, StringParser.PARSER);
		STANDARD_ATTRIBUTES.addParser(ALPS_ID, IntegerParser.PARSER);
		STANDARD_ATTRIBUTES.addParser(USER, IntegerParser.PARSER);
		STANDARD_ATTRIBUTES.addParser(CMD_LINE, StringParser.PARSER);
		STANDARD_ATTRIBUTES.addParser(CWD, StringParser.PARSER);
		STANDARD_ATTRIBUTES.addParser(NODE_LIST, StringParser.PARSER);
		STANDARD_ATTRIBUTES.addParser(NUM_NODES, IntegerParser.PARSER);
		STANDARD_ATTRIBUTES.addParser(HOSTNAME, StringParser.PARSER);
	}
	Logger log;
	
	// This is very specific to a single log format and might possibly change if the log-file format changes
	//private static final Pattern parse_pattern = Pattern.compile("\\S+ (?<TIMESTAMP>\\S+) (?<HOSTNAME>\\S+) (?<RECORDTYPE>\\S+) \\S+ \\S+ \\S+ " +
	//		"apid=(?<APID>\\d+), \\S+ user=(?<USER>\\d+), batch_id=(?<BATCHID>\\d+\\.sdb)" +
	//		"(, \\S+ \\S+ \\S+)?(, cmd_line=\"(?<CMDLINE>.+)\", num_nodes=(?<NUMNODES>\\d+), node_list=(?<NODELIST>\\S+), cwd=\"(?<CWD>.+)\")?");
	
	// This pattern matches the common prefix
	private static final Pattern general_parse_pattern = Pattern.compile("\\S+ (?<TIMESTAMP>\\S+) (?<HOSTNAME>\\S+) (?<RECORDTYPE>\\S+) \\S+ \\S+ \\S+ " +
			"apid=(?<APID>\\d+), (?<MESSAGE>\\w+)(?<ATTRS>.*)");
	// This pattern matches an attribute pair note an unquoted value can contain comma seperators 
	// so the pattern must match commas except in the last position where the comma is seperating the attributes
	// as we also have to match 1 and 2 char values we assume a quota is illegal everywhere in unquoted values
	// we could just have stripped trailing commas after match as well.
	private static final Pattern attribute_pattern = Pattern.compile("(?<PROP>\\w+)=(?<VALUE>(?:[^\"\\s]*[^,\"\\s])|(?:\"[^\"]*\"))");
	
	
	
	private static final SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
	
	public static class AprunEntryMaker implements ContainerEntryMaker{

		@Override
		public void setValue(PropertyContainer data, String valueString) throws IllegalArgumentException,
				InvalidPropertyException, NullPointerException, AccountingParseException {
			data.setProperty(CMD_LINE, valueString);
			if( valueString.startsWith("aprun")){
				// This is an aprun command
			}
		}

		@Override
		public void setValue(PropertyMap map, String valueString)
				throws IllegalArgumentException, NullPointerException, AccountingParseException {
			// TODO Auto-generated method stub
			
		}
		
	}
	
	@Override
	public boolean parse(PropertyMap map, String record) throws AccountingParseException {
		
		if(record.trim().length() == 0) {
			return false;
		}
		log.debug("line is " + record);
		
		// parse the record into the declared properties and set them in the property map
		Matcher m = general_parse_pattern.matcher(record);
		if (m.matches()) {
		
			// ALPS_ID
			map.setProperty(ALPS_ID, Integer.valueOf(m.group("APID")));
			// HOSTNAME
			map.setProperty(HOSTNAME, m.group("HOSTNAME"));
			String record_type = m.group("RECORDTYPE");
			String timestamp = m.group("TIMESTAMP");
			String string = new StringBuffer(timestamp).delete(23, 26).toString();
			if( record_type.equals("aprun")){
				// APRUN_START_TIMESTAMP
				try {
					map.setProperty(APRUN_START_TIMESTAMP, df.parse(string)); // removes microsecond part since Date has millisecond precision
				} catch (ParseException e) {
					throw new AccountingParseException("bad date format", e);
				}
			}else if( record_type.equals("apsys")){
				try {
					map.setProperty(APSYS_END_TIMESTAMP, df.parse(string)); // removes microsecond part since Date has millisecond precision
				} catch (ParseException e) {
					throw new AccountingParseException("bad date format", e);
				}
			}else{
				return false;
			}
			Matcher attr_matcher = attribute_pattern.matcher(m.group("ATTRS"));
			while( attr_matcher.find()){
				String attrName = attr_matcher.group("PROP");
				String attrValue = attr_matcher.group("VALUE");
				if( attrValue.startsWith("\"")){
					attrValue=attrValue.substring(1, attrValue.length()-1);
				}
				ContainerEntryMaker maker = getEntryMaker(attrName);
				
				
				if (maker != null) {
				
					try {
						maker.setValue(map, attrValue);
					} catch (IllegalArgumentException e) {
						throw new AccountingParseException("Problem with attribute '" + attrName
								+ "': Unable to parse value '" + attrValue + "'", e);
					}
				}
				
			}
			
		} else {
			throw new AccountingParseException("Unexpected line format");
		}
		
		return true;
	}

	private AppContext conn;
	private String tag;
	@Override
	public PropertyFinder initFinder(AppContext ctx, PropertyFinder prev, String table) {
		conn=ctx;
		tag=table;
		MultiFinder mf = new MultiFinder();
		mf.addFinder(alps_reg);
		log = ctx.getService(LoggerService.class).getLogger(getClass());
		return mf;
	}

	@Override
	public boolean isComplete(UsageRecord record) {
		// We need the set of properties from both record types, aprun and apsys
		
		// APSYS_END_TIMESTAMP
		Date apsysEndTime = record.getProperty(APSYS_END_TIMESTAMP, null);
		if(apsysEndTime == null) {
			log.debug("No apsysEndTime");
			return false;
		}
		log.debug("apsysEndTime is " + apsysEndTime);
		
		// APRUN_START_TIMESTAMP
		Date aprunStartTime = record.getProperty(APRUN_START_TIMESTAMP, null);
		if(aprunStartTime == null) {
			log.debug("No aprunStartTime");
			return false;
		}
		log.debug("aprunStartTime is " + aprunStartTime);
		
		// BATCH_ID
		String batch_id = record.getProperty(BATCH_ID, null);
		if(batch_id == null) {
			log.debug("No batch_id");
			return false;
		}
		log.debug("batch_id is " + batch_id);
		
		// ALPS_ID
		Integer apid = record.getProperty(ALPS_ID, null);
		if(apid == null) {
			log.debug("No apid");
			return false;
		}
		log.debug("apid is " + apid);
		
		// USER
		Integer user = record.getProperty(USER, null);
		if(user == null) {
			log.debug("No user");
			return false;
		}
		log.debug("user is " + user);
		
		// CMD_LINE
		String cmd_line = record.getProperty(CMD_LINE, null);
		if(cmd_line == null) {
			log.debug("No cmd_line");
			return false;
		}
		log.debug("cmd_line is " + cmd_line);
		
		// CWD
		String cwd = record.getProperty(CWD, null);
		if(cwd == null) {
			log.debug("No cwd");
			return false;
		}
		log.debug("cwd is " + cwd);
		
		// NODE_LIST
		String node_list = record.getProperty(NODE_LIST, null);
		if(node_list == null) {
			log.debug("No node_list");
			return false;
		}
		log.debug("node_list is " + node_list);
		
		// NUM_NODES
		Integer num_nodes = record.getProperty(NUM_NODES, null);
		if(num_nodes == null) {
			log.debug("No num_nodes");
			return false;
		}
		log.debug("num_nodes is " + num_nodes);
		
		// HOSTNAME
		String hostname = record.getProperty(HOSTNAME, null);
		if(hostname == null) {
			log.debug("No hostname");
			return false;
		}
		log.debug("hostname is " + hostname);

		log.debug("Record is complete");
		return true;
	}
	protected ContainerEntryMaker getEntryMaker(String attr){
		return STANDARD_ATTRIBUTES.get(attr);
	}
	@Override
	public void postComplete(UsageRecord record) throws Exception {
		// not needed for this parser
	}
	
}
