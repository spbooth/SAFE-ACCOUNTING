package uk.ac.ed.epcc.safe.accounting.parsers;

import java.util.HashSet;
import java.util.Set;

import uk.ac.ed.epcc.safe.accounting.expr.DerivedPropertyMap;
import uk.ac.ed.epcc.safe.accounting.expr.PropExpressionMap;
import uk.ac.ed.epcc.safe.accounting.properties.PropertyFinder;
import uk.ac.ed.epcc.safe.accounting.properties.PropertyRegistry;
import uk.ac.ed.epcc.safe.accounting.properties.PropertyTag;
import uk.ac.ed.epcc.safe.accounting.update.AbstractPropertyContainerParser;
import uk.ac.ed.epcc.safe.accounting.update.AccountingParseException;
import uk.ac.ed.epcc.safe.accounting.update.SkipRecord;
import uk.ac.ed.epcc.webapp.AppContext;
import uk.ac.ed.epcc.webapp.exceptions.InvalidArgument;
import uk.ac.ed.epcc.webapp.jdbc.table.IntegerFieldType;
import uk.ac.ed.epcc.webapp.jdbc.table.TableSpecification;
/** LASSi is a cray developed IO stat tool. It reports lustre IO statistics in A
 * CSV format per aprun instance
 * Both peak and total values are generated in different files with the same format.
 * 
 * 
 * @author Stephen Booth
 *
 */
public class LASSiParser extends AbstractPropertyContainerParser {

	
	public static final PropertyRegistry lassi_props = new PropertyRegistry("lassi", "IO stats from LASSi");
	public static String names[] = {"ap_id","read_kb","read_ops","write_kb","write_ops","other","open","close","mknod","link","unlink","mkdir","rmdir","ren","getattr","setattr","getxattr","setxattr","statfs","sync","sdr","cdr"};
	public static final PropertyTag<Integer> tags[] = new PropertyTag[names.length];
	static {
		for(int i = 0 ; i < names.length ; i++) {
			tags[i] = new PropertyTag<Integer>(lassi_props,names[i],Integer.class);
		}
		lassi_props.lock();
	}
	@Override
	public boolean parse(DerivedPropertyMap map, String record) throws AccountingParseException {
		record= record.trim();
		if( record.isEmpty()) {
			throw new SkipRecord("Empty line");
		}
		String fields[] = record.split(",");
		if( fields.length != names.length) {
			throw new AccountingParseException("Wrong number of fields expecting "+names.length);
		}
		for(int i=0 ; i< names.length ; i++) {
			try {
			Integer value = new Integer(fields[i]);
			map.setProperty(tags[i], value);
			if( value.intValue() < 0 ) {
				throw new AccountingParseException("Negative value generated");
			}
			}catch(NumberFormatException nf) {
				throw new AccountingParseException("Not an integer", nf);
			}
		}
		return true;
	}

	@Override
	public PropertyFinder initFinder(AppContext ctx, PropertyFinder prev, String table) {
		return lassi_props;
	}
	@Override
	public TableSpecification modifyDefaultTableSpecification(AppContext c, TableSpecification spec,
			PropExpressionMap map, String table_name) {
		for(String name : names) {
			spec.setField(name, new IntegerFieldType());
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
		Set<PropertyTag> unique = new HashSet<PropertyTag>();
		unique.add(tags[0]); // just ap_id
		return unique;
	}

}
