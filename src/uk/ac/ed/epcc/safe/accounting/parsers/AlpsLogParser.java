package uk.ac.ed.epcc.safe.accounting.parsers;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import uk.ac.ed.epcc.safe.accounting.expr.DerivedPropertyMap;
import uk.ac.ed.epcc.safe.accounting.expr.ExpressionTargetContainer;
import uk.ac.ed.epcc.safe.accounting.expr.PropExpressionMap;
import uk.ac.ed.epcc.safe.accounting.expr.PropertyCastException;
import uk.ac.ed.epcc.safe.accounting.parsers.value.IntegerParser;
import uk.ac.ed.epcc.safe.accounting.parsers.value.StringParser;
import uk.ac.ed.epcc.safe.accounting.properties.InvalidPropertyException;
import uk.ac.ed.epcc.safe.accounting.properties.MultiFinder;
import uk.ac.ed.epcc.safe.accounting.properties.PropertyContainer;
import uk.ac.ed.epcc.safe.accounting.properties.PropertyFinder;
import uk.ac.ed.epcc.safe.accounting.properties.PropertyMap;
import uk.ac.ed.epcc.safe.accounting.properties.PropertyRegistry;
import uk.ac.ed.epcc.safe.accounting.properties.PropertyTag;
import uk.ac.ed.epcc.safe.accounting.properties.StandardProperties;
import uk.ac.ed.epcc.safe.accounting.update.AbstractPropertyContainerParser;
import uk.ac.ed.epcc.safe.accounting.update.AccountingParseException;
import uk.ac.ed.epcc.safe.accounting.update.AutoTable;
import uk.ac.ed.epcc.safe.accounting.update.IncrementalPropertyContainerParser;
import uk.ac.ed.epcc.webapp.AppContext;
import uk.ac.ed.epcc.webapp.exceptions.ConsistencyError;
import uk.ac.ed.epcc.webapp.exceptions.InvalidArgument;
import uk.ac.ed.epcc.webapp.jdbc.table.TableSpecification;
import uk.ac.ed.epcc.webapp.logging.Logger;
import uk.ac.ed.epcc.webapp.logging.LoggerService;


/** Parser for alps syslog information
 * 
 *  Setting the <b><i>table_name</i>.parse_timezone</b> to <b>false</b> will suppress parsing
 *  of the timezone part of the timestamps (for systems where the TZ information is unreliable).
 * @author spb
 *
 */

public class AlpsLogParser extends AbstractPropertyContainerParser implements IncrementalPropertyContainerParser {

	private static final PropertyRegistry alps_reg = new PropertyRegistry("alps", "Properties from the alps log");
	@AutoTable(target=Integer.class, unique=true)
	public static final PropertyTag<Integer> ALPS_ID = new PropertyTag<Integer>(alps_reg, "apid", Integer.class, "Alps job id");
	@AutoTable(target=Date.class, unique=true)
	public static final PropertyTag<Date> SUBMISSION_TIMESTAMP = new PropertyTag<Date>(alps_reg, "alpsBootTime", Date.class, "Time alps system started");

	
	@AutoTable(target=String.class)
	public static final PropertyTag<String> APRUN_TAG = new PropertyTag<String>(alps_reg, "aprun_entry_tag", String.class, "Alps log aprun tag");
	@AutoTable(target=String.class)
	public static final PropertyTag<String> APSYS_TAG = new PropertyTag<String>(alps_reg, "apsys_entry_tag", String.class, "Alps log apsys tag");
	

	@AutoTable(target=String.class, length=32)
	public static final PropertyTag<String> PBS_STRING_ID = new PropertyTag<String>(alps_reg,"batch_string_id",String.class,"Full Batch job identifier");
	@AutoTable(target=Integer.class)
	public static final PropertyTag<Integer> PBS_ID = new PropertyTag<Integer>(alps_reg, "batch_id", Integer.class, "Batch job id");
	@AutoTable(target=String.class)
	public static final PropertyTag<String> PBS_ARRAY_INDEX = new PropertyTag<String>(alps_reg, "batch_array_index", String.class, "Batch job array index");

	
	@AutoTable(target=Integer.class)
	public static final PropertyTag<Integer> USER_ID = new PropertyTag<Integer>(alps_reg, "user", Integer.class, "User");
	@AutoTable(target=String.class)
	public static final PropertyTag<String> HOST_NAME = new PropertyTag<String>(alps_reg, "hostname", String.class, "Hostname");
	@AutoTable(target=Integer.class)
	public static final PropertyTag<Integer> NODE_COUNT = new PropertyTag<Integer>(alps_reg, "num_nodes", Integer.class, "Number of nodes");
	@AutoTable(target=String.class, length=1024)
	public static final PropertyTag<String> NODE_LIST = new PropertyTag<String>(alps_reg, "node_list", String.class, "Node list");
	
		
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
	public static class PBSIdParser implements ContainerEntryMaker{

		@Override
		public void setValue(PropertyContainer map, String attrValue) throws IllegalArgumentException,
				InvalidPropertyException, NullPointerException, AccountingParseException {
			
			map.setProperty(PBS_STRING_ID, attrValue);
			// strip the ".sdb"
			if( attrValue.endsWith(".sdb")){
				attrValue = attrValue.substring(0, attrValue.length()-4);
			}
			String indexValue = "";
			int i = attrValue.indexOf('[', 0);
			if (-1 != i) {
				int i2 = attrValue.indexOf(']', i+1);
				if (-1 == i2) {
					throw new AccountingParseException("Error PBS array index is malformed, '" + attrValue + "'.");
				}
				
				indexValue = attrValue.substring(i+1, i2);
				attrValue = attrValue.substring(0, i);
			}
		
			map.setProperty(PBS_ARRAY_INDEX, indexValue);
			map.setProperty(PBS_ID, Integer.valueOf(attrValue.trim()));
		}

		@Override
		public void setValue(PropertyMap map, String valueString)
				throws IllegalArgumentException, NullPointerException, AccountingParseException {
			try {
				setValue((PropertyContainer)map,valueString);
			} catch (InvalidPropertyException e) {
				
			}
		}
		
	}
	
	private static final MakerMap STANDARD_ATTRIBUTES = new MakerMap();
	static {
		STANDARD_ATTRIBUTES.addParser(PBS_ID, IntegerParser.PARSER);
		STANDARD_ATTRIBUTES.addParser(PBS_ARRAY_INDEX, StringParser.PARSER);
		STANDARD_ATTRIBUTES.addParser(USER_ID, IntegerParser.PARSER);
		STANDARD_ATTRIBUTES.addParser(NODE_COUNT, IntegerParser.PARSER);
		STANDARD_ATTRIBUTES.addParser(NODE_LIST, StringParser.PARSER);
		STANDARD_ATTRIBUTES.addParser(CWD, StringParser.PARSER);
		STANDARD_ATTRIBUTES.addParser(APRUN_CMD_STRING, StringParser.PARSER);
		STANDARD_ATTRIBUTES.addParser(EXIT_CODE, StringParser.PARSER);
		STANDARD_ATTRIBUTES.addParser(EXIT_CODE_ARRAY, StringParser.PARSER);
		STANDARD_ATTRIBUTES.addParser(EXIT_SIGNAL_ARRAY, StringParser.PARSER);
		STANDARD_ATTRIBUTES.put(PBS_ID.getName(),new PBSIdParser());
	}
	
	
	private static final SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
	private static final SimpleDateFormat df_no_tz = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");
	private static final SimpleDateFormat sf = new SimpleDateFormat("'p0-'yyyyMMdd't'HHmmss");
	
	private static final Pattern parse_pattern = Pattern.compile("\\S+ (?<TIMESTAMP>\\S+) (?<HOSTNAME>\\S+) (?<RECORDTYPE>\\S+) (?<TAG>\\S+) (?<SUBMISSION>\\S+)"
			+ " \\S+ " + "apid=(?<APID>\\S+), (?<MESSAGE>\\w+)(?<ATTRS>.*)");
			
	// This pattern matches an attribute pair. Note, an unquoted value can contain comma separators 
	// so the pattern must match commas except in the last position where the comma is separating the attributes;
	// as we also have to match 1 and 2 char values, we assume a quote is illegal everywhere in unquoted values
	// (we could just have stripped trailing commas after match as well).
	private static final Pattern attribute_pattern = Pattern.compile("(?<ATTRNAME>\\w+)=(?<ATTRVALUE>(?:[^\"\\s]*[^,\"\\s])|(?:\"[^\"]*\"))");
	                            
	private Logger log;
	private boolean parse_timezone=true;
	@Override
	public boolean parse(DerivedPropertyMap map, String record) throws AccountingParseException {
		
		if (record.trim().length() == 0) {
			return false;
		}
		
		log.debug("alps line is " + record);
		
		// parse the record into the declared properties and set them in the property map
		Matcher m = parse_pattern.matcher(record);
		if (m.matches()) {
		
			if (m.group("MESSAGE").equals("Error")) {
				return false;
			}
			
			map.setProperty(ALPS_ID, Integer.valueOf(m.group("APID")));
			map.setProperty(HOST_NAME, m.group("HOSTNAME"));
						
			String record_type = m.group("RECORDTYPE");
			String tag = m.group("TAG");
			
			String submission = m.group("SUBMISSION");
			try {
				Date start = sf.parse(submission);
				map.setProperty(SUBMISSION_TIMESTAMP, start);
			} catch (ParseException e) {
				throw new AccountingParseException("bad submission date format", e);
			}
			
			String timestamp = m.group("TIMESTAMP");
			
			
			if (record_type.equals("aprun")) {
				map.setProperty(APRUN_TAG, tag);
				try {
					Date start = parseDate(parse_timezone,timestamp);
					map.setProperty(APRUN_START_TIMESTAMP, start);
				} catch (ParseException e) {
					throw new AccountingParseException("bad start date format", e);
				}
			} else if (record_type.equals("apsys")) {
				map.setProperty(APSYS_TAG, tag);
				try {
					Date end = parseDate(parse_timezone,timestamp);
					map.setProperty(APSYS_END_TIMESTAMP, end);
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
// This won't work as we only ever have one of the two values in a single line
//			Date start = map.getProperty(APRUN_START_TIMESTAMP);
//			Date end = map.getProperty(APSYS_END_TIMESTAMP);
//			if( start != null && end != null && end.before(start) ){
//				throw new AccountingParseException("reversed time bounds");
//			}
		} else {
			throw new AccountingParseException("Unexpected line format");
		}
		
		return true;
	}

	/**
	 * @param timestamp
	 * @return
	 * @throws ParseException
	 */
	public static Date parseDate(boolean parse_timezone, String timestamp) throws ParseException {
		if( parse_timezone){
			// Remove sub-milisecond values
			timestamp = new StringBuffer(timestamp).delete(23, 26).toString();
			return df.parse(timestamp);
		}else{
			timestamp = timestamp.substring(0, 23);
			return df_no_tz.parse(timestamp);
		}
	}

	@Override
	public PropertyFinder initFinder(AppContext ctx, PropertyFinder prev, String table) {
		log = ctx.getService(LoggerService.class).getLogger(getClass());
		MultiFinder finder = new MultiFinder();
		finder.addFinder(alps_reg);
		finder.addFinder(StandardProperties.time);
		parse_timezone=ctx.getBooleanParameter(table+".parse_timezone", true);
		return finder;
	}

	@Override
	public boolean isComplete(ExpressionTargetContainer record) {
		// set of minimal required properties from both record types, aprun and apsys
		// Don't include the batch ids as testing apruns not run from batch won't complete
		PropertyTag<?>[] attrs = {APRUN_TAG, APSYS_TAG,  ALPS_ID,
				SUBMISSION_TIMESTAMP, APRUN_START_TIMESTAMP, APSYS_END_TIMESTAMP,
				CWD, APRUN_CMD_STRING};
		
		return super.isComplete(record, attrs);
	}
	
	protected ContainerEntryMaker getEntryMaker(String attr){
		return STANDARD_ATTRIBUTES.get(attr);
	}
	
	@Override
	public void postComplete(ExpressionTargetContainer record) throws Exception {
		// do nothing
	}

	/* (non-Javadoc)
	 * @see uk.ac.ed.epcc.safe.accounting.update.AbstractPropertyContainerUpdater#getDerivedProperties(uk.ac.ed.epcc.safe.accounting.expr.PropExpressionMap)
	 */
	@Override
	public PropExpressionMap getDerivedProperties(PropExpressionMap previous) {
		PropExpressionMap derived = new PropExpressionMap(previous);
		try{
			derived.put(StandardProperties.STARTED_PROP, APRUN_START_TIMESTAMP);
			derived.put(StandardProperties.ENDED_PROP, APSYS_END_TIMESTAMP);
		}catch(PropertyCastException e){
			throw new ConsistencyError("Type inconsistency", e);
		}
		return derived;
	}

	@Override
	public TableSpecification modifyDefaultTableSpecification(AppContext c, TableSpecification spec,
			PropExpressionMap map, String table_name) {
		TableSpecification ss = super.modifyDefaultTableSpecification(c, spec, map, table_name);
		try {
			ss.new Index("time",false,APSYS_END_TIMESTAMP.getName());
			ss.new Index("alps_time",false,APRUN_START_TIMESTAMP.getName(), APSYS_END_TIMESTAMP.getName());
			
		} catch (InvalidArgument e) {
			c.getService(LoggerService.class).getLogger(getClass()).error("Error adding time index",e);
		}
		return ss;
	}
	
}
