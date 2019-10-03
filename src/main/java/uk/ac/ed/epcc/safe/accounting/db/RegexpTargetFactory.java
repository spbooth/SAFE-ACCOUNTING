package uk.ac.ed.epcc.safe.accounting.db;

import java.util.Map;

import uk.ac.ed.epcc.webapp.AppContext;
import uk.ac.ed.epcc.webapp.forms.inputs.RegexpInput;
import uk.ac.ed.epcc.webapp.jdbc.table.StringFieldType;
import uk.ac.ed.epcc.webapp.jdbc.table.TableSpecification;
import uk.ac.ed.epcc.webapp.model.data.Repository.Record;
import uk.ac.ed.epcc.webapp.model.data.Exceptions.DataFault;

public class RegexpTargetFactory<T extends RegexpTarget> extends AccountingClassificationFactory<T> {
	
	public static final String REGEX_FIELD = "Regex";
		
	public RegexpTargetFactory(AppContext conn, String table){
		super(conn,table);
	}
	
	@Override
	protected Map<String, Object> getSelectors() {
		Map<String, Object> selectors = super.getSelectors();
		RegexpInput regexp_input = new RegexpInput();
		regexp_input.setSingle(true);
		regexp_input.setBoxWidth(32);
		selectors.put(REGEX_FIELD, regexp_input);
		return selectors;
	}

	@Override
	protected Map<String, String> getTranslations() {
		Map<String, String> translations = super.getTranslations();
		translations.put(REGEX_FIELD,"Matching regular expression");
		return translations;
	}

	@Override
	public TableSpecification getDefaultTableSpecification(AppContext c, String table) {
		
		TableSpecification spec = super.getDefaultTableSpecification(c, table);

		spec.setField(REGEX_FIELD, new StringFieldType(false, null, 128));
		
		
		
		return spec;
	}

	@Override
	public Class<T> getTarget() {
		return (Class<T>) RegexpTarget.class;
	}

	@Override
	protected T makeBDO(Record res) throws DataFault {
		return (T) new RegexpTarget(this, res);
	}

}
