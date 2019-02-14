package uk.ac.ed.epcc.safe.accounting.expr;

import uk.ac.ed.epcc.safe.accounting.properties.PropertyFinder;
import uk.ac.ed.epcc.safe.accounting.properties.PropertyRegistry;
import uk.ac.ed.epcc.webapp.AppContext;
import uk.ac.ed.epcc.webapp.forms.Form;
import uk.ac.ed.epcc.webapp.forms.action.FormAction;
import uk.ac.ed.epcc.webapp.forms.exceptions.ActionException;
import uk.ac.ed.epcc.webapp.forms.exceptions.TransitionException;
import uk.ac.ed.epcc.webapp.forms.inputs.TextInput;
import uk.ac.ed.epcc.webapp.forms.result.FormResult;
import uk.ac.ed.epcc.webapp.forms.transition.AbstractFormTransition;
import uk.ac.ed.epcc.webapp.jdbc.table.ViewTableResult;
import uk.ac.ed.epcc.webapp.model.data.DataObjectFactory;

public class AddDerivedTransition extends AbstractFormTransition<DataObjectFactory>{
	
	/**
	 * @param defs
	 * @param reg
	 * @param finder
	 * @param table
	 */
	public AddDerivedTransition(PropExpressionMap defs, PropertyRegistry reg, PropertyFinder finder, String table) {
		super();
		this.defs = defs;
		this.reg = reg;
		this.finder = finder;
		this.table = table;
	}

	private final PropExpressionMap defs;
	private final PropertyRegistry reg;
	private final PropertyFinder finder;
	private final String table;
	public final class AddDerivedAction extends FormAction {
		private final DataObjectFactory target;
		
		public AddDerivedAction(DataObjectFactory target){
			this.target=target;
		}
		@Override
		public FormResult action(Form f)
				throws uk.ac.ed.epcc.webapp.forms.exceptions.ActionException {
			try {
				defs.addConfigProperty(target.getContext(), reg,finder, table, (String)f.get("Name"), (String)f.get("Expr"));
			} catch (ParseException e) {
				throw new ActionException("Operation failed",e);
			}
			return new ViewTableResult(target);
		}
	}

	public void buildForm(Form f, DataObjectFactory target,
			AppContext ctx) throws TransitionException {
		f.addInput("Name", "Name of new property", new TextInput(false));
		f.addInput("Expr", "Definition", new PropExpressionInput(target.getContext(),finder));
		f.addAction("Add", new AddDerivedAction(target));
	}
}