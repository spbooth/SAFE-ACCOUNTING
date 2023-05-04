package uk.ac.ed.epcc.safe.accounting.db;

import java.util.Map;

import uk.ac.ed.epcc.webapp.AppContext;
import uk.ac.ed.epcc.webapp.forms.inputs.Input;
import uk.ac.ed.epcc.webapp.forms.inputs.RegexpInput;
import uk.ac.ed.epcc.webapp.jdbc.table.StringFieldType;
import uk.ac.ed.epcc.webapp.jdbc.table.TableSpecification;
import uk.ac.ed.epcc.webapp.model.data.ConfigTag;
import uk.ac.ed.epcc.webapp.model.data.FieldHandler;
import uk.ac.ed.epcc.webapp.model.data.Repository.Record;
import uk.ac.ed.epcc.webapp.model.data.Exceptions.DataFault;
import uk.ac.ed.epcc.webapp.model.data.forms.Selector;

public class RegexpTargetFactory<T extends RegexpTarget> extends AccountingClassificationFactory<T> implements FieldHandler{
	@ConfigTag("RegexpTargetFactory")
	public static final String REGEX_FIELD = "Regex";
		
	public RegexpTargetFactory(AppContext conn, String table){
		super(conn,table);
	}
	
	@Override
	protected Map<String, Selector> getSelectors() {
		Map<String, Selector> selectors = super.getSelectors();
		
		selectors.put(REGEX_FIELD, new Selector() {

			@Override
			public Input getInput() {
				RegexpInput regexp_input = new RegexpInput();
				regexp_input.setSingle(true);
				regexp_input.setBoxWidth(32);
				return regexp_input;
			}
			
		});
		return selectors;
	}

	@Override
	public TableSpecification getDefaultTableSpecification(AppContext c, String table) {
		
		TableSpecification spec = super.getDefaultTableSpecification(c, table);

		spec.setField(REGEX_FIELD, new StringFieldType(false, null, 128));
		
		
		
		return spec;
	}

	

	@Override
	protected T makeBDO(Record res) throws DataFault {
		return (T) new RegexpTarget(this, res);
	}

}
