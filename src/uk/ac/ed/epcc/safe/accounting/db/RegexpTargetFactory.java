package uk.ac.ed.epcc.safe.accounting.db;

import java.util.Map;

import uk.ac.ed.epcc.webapp.AppContext;
import uk.ac.ed.epcc.webapp.forms.inputs.RegexpInput;
import uk.ac.ed.epcc.webapp.jdbc.table.StringFieldType;
import uk.ac.ed.epcc.webapp.jdbc.table.TableSpecification;
import uk.ac.ed.epcc.webapp.model.data.DataObject;
import uk.ac.ed.epcc.webapp.model.data.Repository.Record;
import uk.ac.ed.epcc.webapp.model.data.Exceptions.DataFault;

public class RegexpTargetFactory<T extends RegexpTarget> extends DefaultDataObjectPropertyFactory<T> {

	public static final String NAME_FIELD = "Name";
	public static final String LONG_NAME_FIELD = "LongName";
	public static final String REGEX_FIELD = "Regex";
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
		translations.put(LONG_NAME_FIELD, "Description");
		translations.put(REGEX_FIELD,"Matching regular expression");
		return translations;
	}

	@Override
	protected TableSpecification getDefaultTableSpecification(AppContext c, String table) {
		
		TableSpecification spec = new TableSpecification("RegexTargetID");
		spec.setField(NAME_FIELD, new StringFieldType(false, null, 32));
		spec.setField(LONG_NAME_FIELD, new StringFieldType(true, null, 128));
		spec.setField(REGEX_FIELD, new StringFieldType(false, null, 256));
		return spec;
	}

	@Override
	public Class<? super T> getTarget() {
		return RegexpTarget.class;
	}

	@Override
	protected DataObject makeBDO(Record res) throws DataFault {
		return new RegexpTarget(this, res);
	}

}
