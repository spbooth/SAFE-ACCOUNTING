package uk.ac.ed.epcc.safe.accounting.expr;

import uk.ac.ed.epcc.safe.accounting.properties.OwnedPropertyTarget;
import uk.ac.ed.epcc.safe.accounting.properties.PropertyContainer;
import uk.ac.ed.epcc.safe.accounting.properties.PropertyTag;
import uk.ac.ed.epcc.webapp.AppContext;
/** A wrapper to convert a {@link OwnedPropertyTarget} into an {@link ExpressionTarget}
 * 
 */
public class OwnedPropertyTargetExpressionWrapper extends AbstractExpressionTarget {

	private final OwnedPropertyTarget property_target;
	public OwnedPropertyTargetExpressionWrapper(AppContext ctx,OwnedPropertyTarget target) {
		super(ctx);
		this.property_target=target;
	}

	@Override
	public <T> T getProperty(PropertyTag<T> tag, T def) {
		return property_target.getProperty(tag, def);
	}

	

	@Override
	public Object visitPropertyTag(PropertyTag<?> tag) throws Exception {
		if( property_target instanceof PropertyContainer) {
			return ((PropertyContainer)property_target).getProperty(tag);
		}
		return property_target.getProperty(tag, null);
	}

	@Override
	public Parser getParser() {
		return new Parser(getContext(), property_target.getPropertyTargetFactory().getFinder());
	}

	

	

	

}
