package uk.ac.ed.epcc.safe.accounting.model;

import java.util.LinkedHashMap;
import java.util.Map;

import uk.ac.ed.epcc.safe.accounting.ExpressionTargetFactory;
import uk.ac.ed.epcc.safe.accounting.expr.ExpressionCast;
import uk.ac.ed.epcc.safe.accounting.expr.ExpressionTarget;
import uk.ac.ed.epcc.safe.accounting.expr.Parser;
import uk.ac.ed.epcc.safe.accounting.properties.InvalidExpressionException;
import uk.ac.ed.epcc.safe.accounting.properties.PropExpression;
import uk.ac.ed.epcc.webapp.model.IndexTableContributor;
import uk.ac.ed.epcc.webapp.model.data.Composite;
import uk.ac.ed.epcc.webapp.model.data.DataObject;
import uk.ac.ed.epcc.webapp.model.data.DataObjectFactory;
/** An {@link IndexTableContributor} that adds attributes to the summary table or index table based on properties.
 * 
 * The list of attributes to add to the summary table is defined in <b><i>config_tag</i>.property_attributes</b>
 * 
 * The list of attributes to add to the index table is defined in <b><i>config_tag</i>.index_property_attributes</b>
 * 
 * The label for the attribute can be customised by setting <b><i>config_tab.name</i>.label</b> defaulting to name.
 * The prop-expression for the attribute can be customised by setting <b><i>config_tab.name</i>.expression</b> defaulting to name.
 * @author Stephen Booth
 *
 * @param <T>
 */
public class PropertyIndexContributor<T extends DataObject> extends Composite<T, PropertyIndexContributor> implements IndexTableContributor<T> {

	private Map<String,PropExpression> attr = null;
	private Map<String,PropExpression> index_attr = null;
	public PropertyIndexContributor(DataObjectFactory<T> fac) {
		super(fac);
		
	}

	@Override
	public void addAttributes(Map<String, Object> attributes, T target) {
		ExpressionTarget et = ExpressionCast.getExpressionTarget(getContext(), target);
		if( et == null ) {
			return;
		}
		for(Map.Entry<String,PropExpression> e : getAttr().entrySet()) {
			try {
				attributes.put(e.getKey(), et.evaluateExpression(e.getValue()));
			} catch (InvalidExpressionException e1) {
				getLogger().error("Error evaluating expression", e1);
			}
		}
		
	}
	
	@Override
	public void addIndexAttributes(Map<String, Object> attributes, T target) {
		ExpressionTarget et = ExpressionCast.getExpressionTarget(getContext(), target);
		if( et == null ) {
			return;
		}
		for(Map.Entry<String,PropExpression> e : getIndexAttr().entrySet()) {
			try {
				attributes.put(e.getKey(), et.evaluateExpression(e.getValue()));
			} catch (InvalidExpressionException e1) {
				getLogger().error("Error evaluating expression", e1);
			}
		}
	}

	
	@Override
	protected Class<? super PropertyIndexContributor> getType() {
		return PropertyIndexContributor.class;
	}

	private Map<String,PropExpression> getAttr() {
		if( attr == null ) {
			attr = new LinkedHashMap<String, PropExpression>();
			ExpressionTargetFactory<T> etf = ExpressionCast.getExpressionTargetFactory(fac);
			if( etf == null ) {
				return attr;
			}
			Parser p = new Parser(getContext(),etf.getFinder());
			String attribute_list = getContext().getInitParameter(getFactory().getConfigTag()+".property_attributes");
			if( attribute_list != null && ! attribute_list.isEmpty()) {
				for(String name : attribute_list.split("\\s*,\\s*")) {
					String label = getContext().getInitParameter(fac.getConfigTag()+"."+name+".label",name);
					String expression = getContext().getInitParameter(fac.getConfigTag()+"."+name+".expression",name);
					try {
						PropExpression pe = p.parse(expression);
						if( pe != null) {
							attr.put(label, pe);
						}
					} catch (Exception e) {
						getLogger().error("Error getting expression", e);
					}
				}
			}
		}
		return attr;
	}

	private Map<String,PropExpression> getIndexAttr() {
		if( index_attr == null ) {
			index_attr = new LinkedHashMap<String, PropExpression>();
			ExpressionTargetFactory<T> etf = ExpressionCast.getExpressionTargetFactory(fac);
			if( etf == null ) {
				return index_attr;
			}
			Parser p = new Parser(getContext(),etf.getFinder());
			String attribute_list = getContext().getInitParameter(getFactory().getConfigTag()+".index_property_attributes");
			if( attribute_list != null && ! attribute_list.isEmpty()) {
				for(String name : attribute_list.split("\\s*,\\s*")) {
					String label = getContext().getInitParameter(fac.getConfigTag()+"."+name+".label",name);
					String expression = getContext().getInitParameter(fac.getConfigTag()+"."+name+".expression",name);
					try {
						PropExpression pe = p.parse(expression);
						if( pe != null) {
							index_attr.put(label, pe);
						}
					} catch (Exception e) {
						getLogger().error("Error getting expression", e);
					}
				}
			}
		}
		return index_attr;
	}

	

}
