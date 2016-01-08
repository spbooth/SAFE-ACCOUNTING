package uk.ac.ed.epcc.safe.accounting.parsers;

import java.util.Date;

import uk.ac.ed.epcc.safe.accounting.UsageRecord;
import uk.ac.ed.epcc.safe.accounting.properties.MultiFinder;
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

/** Parser for alps syslog information
 * 
 * @author spb
 *
 */

public class AlpsLogParser extends AbstractPropertyContainerParser implements IncrementalPropertyContainerParser {

	private static final PropertyRegistry alps_reg = new PropertyRegistry("alps","Properties from the alps log");
	@AutoTable(target=String.class, unique=true)
	public static final PropertyTag<String> BATCH_ID = new PropertyTag<String>(alps_reg, "batch_id",String.class,"Batch job id");
	@AutoTable(target=Integer.class, unique=true)
	public static final PropertyTag<Integer> ALPS_ID = new PropertyTag<Integer>(alps_reg, "apid",Integer.class,"Alps job id");
	@AutoTable(target=Integer.class, unique=true)
	public static final PropertyTag<Integer> USER = new PropertyTag<Integer>(alps_reg, "user", Integer.class, "User");
	@AutoTable(target=String.class)
	public static final PropertyTag<String> CMD_LINE = new PropertyTag<String>(alps_reg, "cmd_line", String.class, "Command line");
	@AutoTable(target=String.class)
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
	
	Logger log;
	
	@Override
	public boolean parse(PropertyMap map, String record) throws AccountingParseException {
		
		if(record.trim().length() == 0) {
			return false;
		}
		log.debug("line is " + record);
		
		// TODO parse the record into the declared properties and set them in the property map
		
		throw new AccountingParseException();
		//return true;
	}

	private AppContext conn;
	private String tag;
	@Override
	public PropertyFinder initFinder(AppContext ctx, PropertyFinder prev, String table) {
		conn=ctx;
		tag=table;
		MultiFinder mf = new MultiFinder();
		mf.addFinder(alps_reg);
		return mf;
	}

	@Override
	public boolean isComplete(UsageRecord record) {
		// We need the set of properties from both record types, aprun and apsys
				
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

		// APRUN_START_TIMESTAMP
		Date aprunStartTime = record.getProperty(APRUN_START_TIMESTAMP, null);
		if(aprunStartTime == null) {
			log.debug("No aprunStartTime");
			return false;
		}
		log.debug("aprunStartTime is " + aprunStartTime);
		
		// APSYS_END_TIMESTAMP
		Date apsysEndTime = record.getProperty(APSYS_END_TIMESTAMP, null);
		if(apsysEndTime == null) {
			log.debug("No apsysEndTime");
			return false;
		}
		log.debug("apsysEndTime is " + apsysEndTime);
		
		log.debug("Record is complete");
		return true;
	}

	@Override
	public void postComplete(UsageRecord record) throws Exception {
		// not needed for this parser
	}
	
}
