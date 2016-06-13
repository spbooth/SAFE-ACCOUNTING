package uk.ac.ed.epcc.safe.accounting.parsers;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import uk.ac.ed.epcc.safe.accounting.UsageRecord;
import uk.ac.ed.epcc.safe.accounting.parsers.value.IntegerParser;
import uk.ac.ed.epcc.safe.accounting.parsers.value.StringParser;
import uk.ac.ed.epcc.safe.accounting.properties.PropertyFinder;
import uk.ac.ed.epcc.safe.accounting.properties.PropertyMap;
import uk.ac.ed.epcc.safe.accounting.properties.PropertyRegistry;
import uk.ac.ed.epcc.safe.accounting.properties.PropertyTag;
import uk.ac.ed.epcc.safe.accounting.update.AbstractPropertyContainerParser;
import uk.ac.ed.epcc.safe.accounting.update.AccountingParseException;
import uk.ac.ed.epcc.safe.accounting.update.AutoTable;
import uk.ac.ed.epcc.safe.accounting.update.IncrementalPropertyContainerParser;
import uk.ac.ed.epcc.webapp.AppContext;


/** Parser for alps syslog information
 * 
 * @author spb
 *
 */

public class AlpsLogParser extends AbstractPropertyContainerParser implements IncrementalPropertyContainerParser {

	private static final PropertyRegistry alps_reg = new PropertyRegistry("alps", "Properties from the alps log");
	
	@AutoTable(target=String.class)
	public static final PropertyTag<String> APRUN_TAG = new PropertyTag<String>(alps_reg, "aprun_entry_tag", String.class, "Alps log aprun tag");
	@AutoTable(target=String.class)
	public static final PropertyTag<String> APSYS_TAG = new PropertyTag<String>(alps_reg, "apsys_entry_tag", String.class, "Alps log apsys tag");
	@AutoTable(target=String.class, unique=true)
	public static final PropertyTag<String> PBS_ID = new PropertyTag<String>(alps_reg, "batch_id", String.class, "Batch job id");
	@AutoTable(target=Integer.class, unique=true)
	public static final PropertyTag<Integer> ALPS_ID = new PropertyTag<Integer>(alps_reg, "apid", Integer.class, "Alps job id");
	@AutoTable(target=Integer.class, unique=true)
	public static final PropertyTag<Integer> USER_ID = new PropertyTag<Integer>(alps_reg, "user", Integer.class, "User");
	@AutoTable(target=String.class, unique=true)
	public static final PropertyTag<String> HOST_NAME = new PropertyTag<String>(alps_reg, "hostname", String.class, "Hostname");
	@AutoTable(target=Integer.class)
	public static final PropertyTag<Integer> NODE_COUNT = new PropertyTag<Integer>(alps_reg, "num_nodes", Integer.class, "Number of nodes");
	@AutoTable(target=String.class, length=1024)
	public static final PropertyTag<String> NODE_LIST = new PropertyTag<String>(alps_reg, "node_list", String.class, "Node list");
	
	@AutoTable(target=Date.class)
	public static final PropertyTag<Date> SUBMISSION_TIMESTAMP = new PropertyTag<Date>(alps_reg, "aprunSubmissionTime", Date.class, "Timestamp of aprun submission");
	
	@AutoTable(target=Date.class)
	public static final PropertyTag<Date> APRUN_START_TIMESTAMP = new PropertyTag<Date>(alps_reg, "aprunStartTime", Date.class, "Timestamp of starting aprun record");
	@AutoTable(target=Date.class)
	public static final PropertyTag<Date> APSYS_END_TIMESTAMP = new PropertyTag<Date>(alps_reg, "apsysEndTime", Date.class, "Timestamp of finishing apsys record");
	@AutoTable(target=String.class, length=512)
	public static final PropertyTag<String> CWD = new PropertyTag<String>(alps_reg, "cwd", String.class, "Current working directory");
	@AutoTable(target=String.class, length=4096)
	public static final PropertyTag<String> APRUN_CMD_STRING = new PropertyTag<String>(alps_reg, "cmd_line", String.class, "Command line");
	
	@AutoTable(target=String.class)
	public static final PropertyTag<String> EXIT_CODE = new PropertyTag<String>(alps_reg, "exit_code", String.class, "Exit code");
	@AutoTable(target=String.class, length=512)
	public static final PropertyTag<String> EXIT_CODE_ARRAY = new PropertyTag<String>(alps_reg, "exitcode_array", String.class, "Exit code array");
	@AutoTable(target=String.class, length=512)
	public static final PropertyTag<String> EXIT_SIGNAL_ARRAY = new PropertyTag<String>(alps_reg, "exitsignal_array", String.class, "Exit signal array");
	
	
	private static final MakerMap STANDARD_ATTRIBUTES = new MakerMap();
	static {
		STANDARD_ATTRIBUTES.addParser(PBS_ID, StringParser.PARSER);
		STANDARD_ATTRIBUTES.addParser(USER_ID, IntegerParser.PARSER);
		STANDARD_ATTRIBUTES.addParser(NODE_COUNT, IntegerParser.PARSER);
		STANDARD_ATTRIBUTES.addParser(NODE_LIST, StringParser.PARSER);
		STANDARD_ATTRIBUTES.addParser(CWD, StringParser.PARSER);
		STANDARD_ATTRIBUTES.addParser(APRUN_CMD_STRING, StringParser.PARSER);
		STANDARD_ATTRIBUTES.addParser(EXIT_CODE, StringParser.PARSER);
		STANDARD_ATTRIBUTES.addParser(EXIT_CODE_ARRAY, StringParser.PARSER);
		STANDARD_ATTRIBUTES.addParser(EXIT_SIGNAL_ARRAY, StringParser.PARSER);
	}
	
	
	private static final SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
	private static final SimpleDateFormat sf = new SimpleDateFormat("'p0-'yyyyMMdd't'HHmmss");
	
	private static final Pattern parse_pattern = Pattern.compile("\\S+ (?<TIMESTAMP>\\S+) (?<HOSTNAME>\\S+) (?<RECORDTYPE>\\S+) (?<TAG>\\S+) (?<SUBMISSION>\\S+)"
			+ " \\S+ " + "apid=(?<APID>\\d+), (?<MESSAGE>\\w+)(?<ATTRS>.*)");
	
	// This pattern matches an attribute pair. Note, an unquoted value can contain comma separators 
	// so the pattern must match commas except in the last position where the comma is separating the attributes;
	// as we also have to match 1 and 2 char values, we assume a quote is illegal everywhere in unquoted values
	// (we could just have stripped trailing commas after match as well).
	private static final Pattern attribute_pattern = Pattern.compile("(?<ATTRNAME>\\w+)=(?<ATTRVALUE>(?:[^\"\\s]*[^,\"\\s])|(?:\"[^\"]*\"))");
	                            
	
	@Override
	public boolean parse(PropertyMap map, String record) throws AccountingParseException {
		
		if (record.trim().length() == 0) {
			return false;
		}
		
		log.debug("alps line is " + record);
		
		// parse the record into the declared properties and set them in the property map
		Matcher m = parse_pattern.matcher(record);
		if (m.matches()) {
		
			map.setProperty(ALPS_ID, Integer.valueOf(m.group("APID")));
			map.setProperty(HOST_NAME, m.group("HOSTNAME"));
						
			String record_type = m.group("RECORDTYPE");
			String tag = m.group("TAG");
			
			String submission = m.group("SUBMISSION");
			try {
				map.setProperty(SUBMISSION_TIMESTAMP, sf.parse(submission));
			} catch (ParseException e) {
				throw new AccountingParseException("bad submission date format", e);
			}
			
			String timestamp = m.group("TIMESTAMP");
			// removes microsecond part since Date has millisecond precision
			timestamp = new StringBuffer(timestamp).delete(23, 26).toString();
			
			if (record_type.equals("aprun")) {
				map.setProperty(APRUN_TAG, tag);
				try {
					map.setProperty(APRUN_START_TIMESTAMP, df.parse(timestamp));
				} catch (ParseException e) {
					throw new AccountingParseException("bad start date format", e);
				}
			} else if (record_type.equals("apsys")) {
				map.setProperty(APSYS_TAG, tag);
				try {
					map.setProperty(APSYS_END_TIMESTAMP, df.parse(timestamp));
				} catch (ParseException e) {
					throw new AccountingParseException("bad end date format", e);
				}
			} else {
				return false;
			}
			
			Matcher attr_matcher = attribute_pattern.matcher(m.group("ATTRS"));
			while (attr_matcher.find()) {
				String attrName = attr_matcher.group("ATTRNAME");
				String attrValue = attr_matcher.group("ATTRVALUE");
				
				if( attrValue.startsWith("\"")){
					attrValue=attrValue.substring(1, attrValue.length()-1);
				}
				
				if (attrName.equals(PBS_ID.getName())) {
					// strip the ".sdb"
					attrValue=attrValue.substring(0, attrValue.length()-4);
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

	@Override
	public PropertyFinder initFinder(AppContext ctx, PropertyFinder prev, String table) {
		return super.initFinder(ctx, alps_reg);
	}

	@Override
	public boolean isComplete(UsageRecord record) {
		// set of properties from both record types, aprun and apsys
		PropertyTag<?>[] attrs = {APRUN_TAG, APSYS_TAG, PBS_ID, ALPS_ID, USER_ID,
				HOST_NAME, NODE_COUNT, NODE_LIST,
				SUBMISSION_TIMESTAMP, APRUN_START_TIMESTAMP, APSYS_END_TIMESTAMP,
				CWD, APRUN_CMD_STRING};
		
		return super.isComplete(record, attrs);
	}
	
	protected ContainerEntryMaker getEntryMaker(String attr){
		return STANDARD_ATTRIBUTES.get(attr);
	}
	
	@Override
	public void postComplete(UsageRecord record) throws Exception {
		// do nothing
	}
	
}
