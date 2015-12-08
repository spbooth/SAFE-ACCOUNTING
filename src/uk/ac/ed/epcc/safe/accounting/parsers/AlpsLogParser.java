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
	@AutoTable(target=String.class, unique=true)
	public static final PropertyTag<String> CMD_LINE = new PropertyTag<String>(alps_reg, "cmd_line", String.class, "Command line");
	@AutoTable(target=String.class, unique=true)
	public static final PropertyTag<String> CWD = new PropertyTag<String>(alps_reg, "cwd", String.class, "Current working directory");
	@AutoTable(target=String.class, unique=true)
	public static final PropertyTag<String> NODE_LIST = new PropertyTag<String>(alps_reg, "node_list", String.class, "Node list");
	@AutoTable(target=Integer.class, unique=true)
	public static final PropertyTag<Integer> NUM_NODES = new PropertyTag<Integer>(alps_reg, "num_nodes", Integer.class, "Number of nodes");
	@AutoTable(target=String.class, unique=true)
	public static final PropertyTag<String> HOSTNAME = new PropertyTag<String>(alps_reg, "hostname", String.class, "Hostname");
	@AutoTable(target=Date.class)
	public static final PropertyTag<Date> APRUN_START_TIMESTAMP = new PropertyTag<Date>(alps_reg, "aprunStartTime", Date.class, "Timestamp of starting aprun record");
	@AutoTable(target=Date.class)
	public static final PropertyTag<Date> APSYS_END_TIMESTAMP = new PropertyTag<Date>(alps_reg, "apsysEndTime", Date.class, "Timestamp of finishing apsys record");
	
	
	@Override
	public boolean parse(PropertyMap map, String record) throws AccountingParseException {
		throw new AccountingParseException();
		// TODO Auto-generated method stub
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
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public void postComplete(UsageRecord record) throws Exception {
		// TODO Auto-generated method stub

	}
	
}
