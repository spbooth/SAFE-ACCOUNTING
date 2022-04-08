package uk.ac.ed.epcc.safe.accounting.policy;

import java.util.Set;

import uk.ac.ed.epcc.safe.accounting.db.AccountingUpdater;
import uk.ac.ed.epcc.safe.accounting.db.UsageRecordFactory;
import uk.ac.ed.epcc.safe.accounting.db.UsageRecordParseTarget;
import uk.ac.ed.epcc.safe.accounting.db.transitions.SummaryProvider;
import uk.ac.ed.epcc.safe.accounting.expr.DerivedPropertyMap;
import uk.ac.ed.epcc.safe.accounting.parsers.TrivialParser;
import uk.ac.ed.epcc.safe.accounting.properties.PropertyContainer;
import uk.ac.ed.epcc.safe.accounting.properties.PropertyFinder;
import uk.ac.ed.epcc.safe.accounting.properties.PropertyMap;
import uk.ac.ed.epcc.safe.accounting.update.AccountingParseException;
import uk.ac.ed.epcc.safe.accounting.update.BatchParser;
import uk.ac.ed.epcc.safe.accounting.update.PropertyContainerPolicy;
import uk.ac.ed.epcc.safe.accounting.update.SkipRecord;
import uk.ac.ed.epcc.webapp.AppContext;
import uk.ac.ed.epcc.webapp.content.ContentBuilder;
import uk.ac.ed.epcc.webapp.model.data.ConfigParamProvider;
import uk.ac.ed.epcc.webapp.session.SessionService;
/** A {@link PropertyContainerPolicy} that skips sub-jobs in the parse stream
 * and optionally parses them to a separate table.
 * 
 * The original parsed properties are passed as pre-parse properties so the SubTable has the option of
 * using a {@link TrivialParser} to speed up processing. However this will prevent re-scan (not a problem if the Text field is not stored)
 * @author Stephen Booth
 *
 */
public class SubJobPolicy<U extends UsageRecordFactory.Use,R> extends BasePolicy implements SummaryProvider,ConfigParamProvider{

	private static final String SUB_JOB_TABLE_SUFFIX = ".sub_job_table";
	public SubJobPolicy(AppContext conn) {
		super(conn);
	}

	@Override
	public PropertyFinder initFinder(PropertyFinder prev, String table) {
		this.table=table;
		String sub_job_table = conn.getInitParameter(table+SUB_JOB_TABLE_SUFFIX);
		if( sub_job_table != null ) {
			target = UsageRecordParseTarget.getParseTarget(conn, sub_job_table);
			if( target == null) {
				getLogger().error("Failed to make UsageRecordParseTarget from "+sub_job_table);
			}
		}
		return null;
	}

	private UsageRecordParseTarget<R> target=null;
	private AccountingUpdater<U, R> updater=null;
	private String table;
	@Override
	public void parse(DerivedPropertyMap rec) throws AccountingParseException {
		Boolean sub_job = rec.getProperty(BatchParser.SUBJOB_PROP, false);
		if( ! sub_job) {
			return;
		}
		// This is a sub-job record
		
		if( updater != null ) {
			// parse record into new table.
			R record = updater.getRecord(rec);
			if( record == null) {
				throw new SkipRecord("Skipping null record");
			}
			updater.receiveRecord(rec, record);
		}
		
		throw new SkipRecord("Skipping sub-job");
	}

	@Override
	public String endParse() {
		StringBuilder sb = new StringBuilder();
		if( updater != null ) {
			String sub_job_message = updater.getReport();
			sub_job_message= sub_job_message.trim();
			if( ! sub_job_message.isEmpty()) {
				for(String line : sub_job_message.split("\n")) {
					sb.append("sub-job-parse: ");
					sb.append(line);
					sb.append("\n");
				}
			}
		}
		updater=null;
		return sb.toString();
	}

	@Override
	public void startParse(PropertyContainer staticProps) throws Exception {
		if( target != null ) {
			boolean replace = staticProps.getProperty(AccountingUpdater.REPLACE_PROP, false);
			boolean verify = staticProps.getProperty(AccountingUpdater.VERIFY_PROP, false);
			boolean augment = staticProps.getProperty(AccountingUpdater.AUGMENT_PROP, false);
			PropertyMap map;
			if( staticProps instanceof PropertyMap) {
				map = (PropertyMap) staticProps;
			}else {
				map = new DerivedPropertyMap(conn);
				map.setAll(staticProps);
			}
			updater=new AccountingUpdater<U, R>(conn, map, target, replace, verify, augment);
		}
	}

	@Override
	public void addConfigParameters(Set<String> params) {
		params.add(table+SUB_JOB_TABLE_SUFFIX);
		
	}

	@Override
	public void getTableTransitionSummary(ContentBuilder hb, SessionService operator) {
		hb.addText("This policy rejects all batch sub-jobs. "+ 
				"Optionally the sub-jos can be redirected to a different table by setting the property "+table+SUB_JOB_TABLE_SUFFIX+". "+
				"The sub-job table should have its own parser and policies installed though the previously parsed properties are "
				+ "pre-populated so it should be possible to use a TrivialParser.");
				
	}

}
