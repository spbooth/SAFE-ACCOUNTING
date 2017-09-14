package uk.ac.ed.epcc.safe.accounting.db;

import java.util.Set;

import uk.ac.ed.epcc.webapp.AppContext;
import uk.ac.ed.epcc.webapp.config.ConfigService;
import uk.ac.ed.epcc.webapp.forms.Form;
import uk.ac.ed.epcc.webapp.forms.action.FormAction;
import uk.ac.ed.epcc.webapp.forms.exceptions.ActionException;
import uk.ac.ed.epcc.webapp.forms.exceptions.TransitionException;
import uk.ac.ed.epcc.webapp.forms.inputs.TextInput;
import uk.ac.ed.epcc.webapp.forms.result.FormResult;
import uk.ac.ed.epcc.webapp.forms.transition.AbstractFormTransition;
import uk.ac.ed.epcc.webapp.jdbc.table.TableTransitionTarget;
import uk.ac.ed.epcc.webapp.jdbc.table.ViewTableResult;

public class ConfigTransition<T extends TableTransitionTarget> extends AbstractFormTransition<T> {

	private final AppContext conn;
	public ConfigTransition(AppContext conn,Set<String> params) {
		super();
		this.conn=conn;
		this.params = params;
	}

	private final Set<String> params;

	private class UpdateConfig extends FormAction{
		public UpdateConfig(T target) {
			super();
			this.target = target;
		}

		private final T target;
		
		@Override
		public FormResult action(Form f) throws ActionException {
			ConfigService cfg = conn.getService(ConfigService.class);
			for(String param : params){
				String value = (String) f.get(param);
				if(value == null || value.isEmpty()){
					cfg.setProperty(param, "");
				}else{
					cfg.setProperty(param, value);
				}
			}
			return new ViewTableResult(target);
		}
		
	}
	/* (non-Javadoc)
	 * @see uk.ac.ed.epcc.webapp.forms.transition.BaseFormTransition#buildForm(uk.ac.ed.epcc.webapp.forms.Form, java.lang.Object, uk.ac.ed.epcc.webapp.AppContext)
	 */
	@Override
	public void buildForm(Form f, T target, AppContext conn) throws TransitionException {
		for(String param : params){
			TextInput input = new TextInput(true);
			input.setOptional(true);
			input.setSingle(true);
			input.setValue(conn.getInitParameter(param));
			f.addInput(param, param, input);
		}
		f.addAction("Update", new UpdateConfig(target));
	}

}
