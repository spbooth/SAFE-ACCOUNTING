package uk.ac.ed.epcc.safe.accounting.db;

import java.util.LinkedList;
import java.util.Map;

import uk.ac.ed.epcc.webapp.AppContext;
import uk.ac.ed.epcc.webapp.forms.inputs.RegexpInput;
import uk.ac.ed.epcc.webapp.jdbc.filter.AndFilter;
import uk.ac.ed.epcc.webapp.jdbc.filter.FilterVisitor;
import uk.ac.ed.epcc.webapp.jdbc.filter.OrderClause;
import uk.ac.ed.epcc.webapp.jdbc.filter.OrderFilter;
import uk.ac.ed.epcc.webapp.jdbc.filter.SQLFilter;
import uk.ac.ed.epcc.webapp.jdbc.table.StringFieldType;
import uk.ac.ed.epcc.webapp.jdbc.table.TableSpecification;
import uk.ac.ed.epcc.webapp.model.data.DataObject;
import uk.ac.ed.epcc.webapp.model.data.Repository.Record;
import uk.ac.ed.epcc.webapp.model.data.Exceptions.DataFault;

public class RegexpTargetFactory<T extends RegexpTarget> extends DefaultDataObjectPropertyFactory<T> {
	
	public static final String NAME_FIELD = "Name";
	public static final String LONG_NAME_FIELD = "LongName";
	public static final String REGEX_FIELD = "Regex";
	
	public static final String PRIMARY_LANGUAGE_FIELD = "PrimaryLanguage";
	public static final String PRIMARY_LANGUAGE_VERSION_FIELD = "PrimaryLanguageVersion";
	public static final String ACADEMIC_LICENCE_FIELD = "AcademicLicence";
	public static final String COMMERCIAL_LICENCE_FIELD = "CommericalLicence";
	public static final String CODE_TYPE_FIELD = "CodeType";
	public static final String RESEARCH_AREA_FIELD = "ResearchArea";
	public static final String PARALLEL_MODEL_FIELD = "ParallelModel";
	
		
	public RegexpTargetFactory(AppContext conn, String table){
		super();
		setContext(conn, table);
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
		translations.put(LONG_NAME_FIELD, "Description");
		translations.put(REGEX_FIELD,"Matching regular expression");
		return translations;
	}

	@Override
	protected TableSpecification getDefaultTableSpecification(AppContext c, String table) {
		
		TableSpecification spec = new TableSpecification("RegexTargetID");
		spec.setField(NAME_FIELD, new StringFieldType(false, null, 16));
		spec.setField(LONG_NAME_FIELD, new StringFieldType(true, null, 512));
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
	public Class<? super T> getTarget() {
		return RegexpTarget.class;
	}

	@Override
	protected DataObject makeBDO(Record res) throws DataFault {
		return new RegexpTarget(this, res);
	}

	@Override
	public AndFilter<T> getSelectFilter() {
	    AndFilter<T> fil = new AndFilter<T>(getTarget());
		fil.addFilter(new NameOrderFilter());
		fil.addFilter(super.getSelectFilter());
		return fil;	
	}
	
	
	public class NameOrderFilter implements OrderFilter<T>, SQLFilter<T>{
		

		@Override
		public LinkedList<OrderClause> OrderBy() {
			LinkedList<OrderClause> order = new LinkedList<OrderClause>();
			order.add(res.getOrder(NAME_FIELD, false));
			return order;
		}

		@Override
		public <X> X acceptVisitor(FilterVisitor<X, ? extends T> vis) throws Exception {
			return vis.visitOrderFilter(this);
		}

		@Override
		public void accept(T o) {
		}

		@Override
		public Class<? super T> getTarget() {
			return RegexpTargetFactory.this.getTarget();
		}
		
	}
}
