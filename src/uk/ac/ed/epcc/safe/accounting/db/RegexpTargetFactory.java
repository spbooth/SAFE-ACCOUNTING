package uk.ac.ed.epcc.safe.accounting.db;

import java.util.List;
import java.util.Map;

import uk.ac.ed.epcc.webapp.AppContext;
import uk.ac.ed.epcc.webapp.forms.inputs.RegexpInput;
import uk.ac.ed.epcc.webapp.jdbc.filter.OrderClause;
import uk.ac.ed.epcc.webapp.jdbc.table.StringFieldType;
import uk.ac.ed.epcc.webapp.jdbc.table.TableSpecification;
import uk.ac.ed.epcc.webapp.model.data.DataObject;
import uk.ac.ed.epcc.webapp.model.data.Repository.Record;
import uk.ac.ed.epcc.webapp.model.data.Exceptions.DataFault;

public class RegexpTargetFactory<T extends RegexpTarget> extends AccountingClassificationFactory<T> {
	
	public static final String REGEX_FIELD = "Regex";
	
	public static final String PRIMARY_LANGUAGE_FIELD = "PrimaryLanguage";
	public static final String PRIMARY_LANGUAGE_VERSION_FIELD = "PrimaryLanguageVersion";
	public static final String ACADEMIC_LICENCE_FIELD = "AcademicLicence";
	public static final String COMMERCIAL_LICENCE_FIELD = "CommericalLicence";
	public static final String CODE_TYPE_FIELD = "CodeType";
	public static final String RESEARCH_AREA_FIELD = "ResearchArea";
	public static final String PARALLEL_MODEL_FIELD = "ParallelModel";
	
		
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
		
		spec.setField(PRIMARY_LANGUAGE_FIELD, new StringFieldType(true, null, 128));
		spec.setField(PRIMARY_LANGUAGE_VERSION_FIELD, new StringFieldType(true, null, 128));
		spec.setField(ACADEMIC_LICENCE_FIELD, new StringFieldType(true, null, 128));
		spec.setField(COMMERCIAL_LICENCE_FIELD, new StringFieldType(true, null, 128));
		spec.setField(CODE_TYPE_FIELD, new StringFieldType(true, null, 128));
		spec.setField(RESEARCH_AREA_FIELD, new StringFieldType(true, null, 128));
		spec.setField(PARALLEL_MODEL_FIELD, new StringFieldType(true, null, 128));
		
		return spec;
	}

	@Override
	public Class<T> getTarget() {
		return (Class<T>) RegexpTarget.class;
	}

	@Override
	protected DataObject makeBDO(Record res) throws DataFault {
		return new RegexpTarget(this, res);
	}

}
