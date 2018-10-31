package uk.ac.ed.epcc.safe.accounting.parsers;

import java.util.HashSet;
import java.util.Set;

import org.apache.fop.util.GenerationHelperContentHandler;

import uk.ac.ed.epcc.safe.accounting.expr.DerivedPropertyMap;
import uk.ac.ed.epcc.safe.accounting.expr.PropExpressionMap;
import uk.ac.ed.epcc.safe.accounting.parsers.value.ValueParser;
import uk.ac.ed.epcc.safe.accounting.parsers.value.ValueParserService;
import uk.ac.ed.epcc.safe.accounting.properties.PropertyContainer;
import uk.ac.ed.epcc.safe.accounting.properties.PropertyFinder;
import uk.ac.ed.epcc.safe.accounting.properties.PropertyRegistry;
import uk.ac.ed.epcc.safe.accounting.properties.PropertyTag;
import uk.ac.ed.epcc.safe.accounting.update.AbstractPropertyContainerParser;
import uk.ac.ed.epcc.safe.accounting.update.AccountingParseException;
import uk.ac.ed.epcc.safe.accounting.update.SkipRecord;
import uk.ac.ed.epcc.webapp.AppContext;
import uk.ac.ed.epcc.webapp.exceptions.InvalidArgument;
import uk.ac.ed.epcc.webapp.jdbc.table.IntegerFieldType;
import uk.ac.ed.epcc.webapp.jdbc.table.LongFieldType;
import uk.ac.ed.epcc.webapp.jdbc.table.TableSpecification;
import uk.ac.ed.epcc.webapp.model.data.ConfigParamProvider;
/** LASSi is a cray developed IO stat tool. It reports lustre IO statistics in A
 * CSV format per aprun instance
 * Both peak and total values are generated in different files with the same format. For convenience the sameparser can be used
 * for both generating different properties.
 * The {@link PropertyRegistry} parsed can be switched by setting <b>lassi.use_peak.<i>table-name</i></b>
 * 
 * @author Stephen Booth
 *
 */
public class LASSiParser extends AbstractPropertyContainerParser implements ConfigParamProvider{

	
	private static final String LASSI_USE_PEAK = "lassi.use_peak.";
	public static final PropertyRegistry lassi_props = new PropertyRegistry("lassi", "IO stats from LASSi");
	public static final PropertyRegistry lassi_peak_props = new PropertyRegistry("peaklassi", "Peak IO stats from LASSi");
	public static String names[] = {"ap_id","read_kb","read_ops","write_kb","write_ops","other","open","close","mknod","link","unlink","mkdir","rmdir","ren","getattr","setattr","getxattr","setxattr","statfs","sync","sdr","cdr"};
	public static final PropertyTag tags[] = new PropertyTag[names.length];
	public static final PropertyTag peak_tags[] = new PropertyTag[names.length];
	static {
		tags[0] = new PropertyTag<>(lassi_props,names[0],Integer.class);
		peak_tags[0] = new PropertyTag<>(lassi_peak_props,names[0],Integer.class);
		for(int i = 1 ; i < names.length ; i++) {
			tags[i] = new PropertyTag<>(lassi_props,names[i],Long.class);
			peak_tags[i] = new PropertyTag<>(lassi_peak_props,names[i],Long.class);
		}
		lassi_props.lock();
	}
	private boolean use_peak=false;
	@Override
	public boolean parse(DerivedPropertyMap map, String record) throws AccountingParseException {
		record= record.trim();
		if( record.isEmpty()) {
			return false;
		}
		String fields[] = record.split(",");
		if( fields.length != names.length) {
			throw new AccountingParseException("Wrong number of fields expecting "+names.length);
		}
		if( fields[0].equals(names[0])) {
			// header line
			return false;
		}
		for(int i=0 ; i< names.length ; i++) {
			try {
				PropertyTag tag = use_peak ? peak_tags[i] : tags[i];
				if( tag.getTarget().equals(Integer.class)) {
					Integer value = new Integer(fields[i]);
					map.setProperty(tag,value);
					if( value.intValue() < 0 ) {
						throw new AccountingParseException("Negative value generated");
					}
				}else {
					Long value = new Long(fields[i]);
					map.setProperty(tag,value);
					if( value.longValue() < 0L ) {
						throw new AccountingParseException("Negative value generated");
					}
				}
			}catch(NumberFormatException nf) {
				throw new AccountingParseException("Number parse error", nf);
			}
		}
		return true;
	}
		private String my_table;
	@Override
	public PropertyFinder initFinder(AppContext ctx, PropertyFinder prev, String table) {
		use_peak = ctx.getBooleanParameter(LASSI_USE_PEAK+table, false);
		if( use_peak) {
			return lassi_peak_props;
		}
		my_table=table;
		return lassi_props;
	}
	
	@Override
	public void startParse(PropertyContainer staticProps) throws Exception {
		
	}
	@Override
	public TableSpecification modifyDefaultTableSpecification(AppContext c, TableSpecification spec,
			PropExpressionMap map, String table_name) {
		for(PropertyTag tag : tags) {
			if( tag.getTarget().equals(Long.class)) {
				spec.setField(tag.getName(), new LongFieldType(true,null));
			}else {
				spec.setField(tag.getName(), new IntegerFieldType(true,null));
			}
		}
		try {
			spec.new Index("aprun_index",true, names[0]);
		} catch (InvalidArgument e) {
			getLogger(c).error("Invalid index", e);
		}
		return spec;
	}

	@Override
	public Set<PropertyTag> getDefaultUniqueProperties() {
		Set<PropertyTag> unique = new HashSet<>();
		unique.add(use_peak ? peak_tags[0]: tags[0]); // just ap_id
		return unique;
	}

	@Override
	public void addConfigParameters(Set<String> params) {
		if( my_table != null ) {
		params.add(LASSI_USE_PEAK+my_table);
		}
	}

}
