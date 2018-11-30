package uk.ac.ed.epcc.safe.accounting.db;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import uk.ac.ed.epcc.safe.accounting.ExpressionTargetFactory;
import uk.ac.ed.epcc.safe.accounting.expr.DerivedPropertyMap;
import uk.ac.ed.epcc.safe.accounting.expr.ExpressionTargetContainer;
import uk.ac.ed.epcc.safe.accounting.properties.InvalidPropertyException;
import uk.ac.ed.epcc.safe.accounting.properties.PropertyContainer;
import uk.ac.ed.epcc.safe.accounting.properties.PropertyFinder;
import uk.ac.ed.epcc.safe.accounting.properties.PropertyTag;
import uk.ac.ed.epcc.safe.accounting.selector.AndRecordSelector;
import uk.ac.ed.epcc.safe.accounting.selector.SelectClause;
import uk.ac.ed.epcc.safe.accounting.update.AccountingParseException;
import uk.ac.ed.epcc.safe.accounting.update.PlugInOwner;
import uk.ac.ed.epcc.webapp.AppContext;
import uk.ac.ed.epcc.webapp.content.ContentBuilder;
import uk.ac.ed.epcc.webapp.exceptions.ConsistencyError;
import uk.ac.ed.epcc.webapp.model.data.DataObject;
import uk.ac.ed.epcc.webapp.model.data.DataObjectFactory;
import uk.ac.ed.epcc.webapp.model.data.reference.IndexedReference;
/** A {@link PropertyContainerParseTargetComposite} that matches on the set of unique properties
 * 
 * @author Stephen Booth
 *
 * @param <T>
 * @param <R>
 */
public abstract class MatcherPropertyContainerParseTargetComposite<T extends DataObject, R>
		extends PropertyContainerParseTargetComposite<T, R> {

	public static final String UNIQUE_PROPERTIES_PREFIX = "unique-properties.";
	Set<PropertyTag> unique_properties = null;

	public MatcherPropertyContainerParseTargetComposite(DataObjectFactory<T> fac) {
		super(fac);
	}

	protected Set<PropertyTag> parsePropertyList(String list) throws InvalidPropertyException {
		HashSet<PropertyTag> res = new HashSet<>();
		ExpressionTargetFactory<T> etf = getExpressionTargetFactory();
		PropertyFinder finder = getFinder();
		if (finder != null) {
			for (String name : list.trim().split(",")) {
				PropertyTag<?> t = finder.make(name);
				if (!etf.hasProperty(t)) {
					throw new InvalidPropertyException(t);
				}
				res.add(t);
			}
		}
		return res;
	}

	/**
	 * Get the set of unique properties to use for detecting re-inserts.
	 * 
	 * @return
	 */
	protected Set<PropertyTag> getUniqueProperties() {
		PlugInOwner<R> plugin_owner = getPlugInOwner();
		if (unique_properties == null) {
			// Must evaluate late as we need to parser/policies to define supported
			// properties
			String unique_prop_list = getContext()
					.getInitParameter(UNIQUE_PROPERTIES_PREFIX + getFactory().getConfigTag());
			if (unique_prop_list != null) {
				try {
					unique_properties = parsePropertyList(unique_prop_list);
				} catch (InvalidPropertyException e) {
					getLogger().error("Invalid property specified as unique", e);
				}
			} else {
				unique_properties = plugin_owner.getParser().getDefaultUniqueProperties();
			}
			if (unique_properties == null) {
				throw new ConsistencyError("No unique properties defined for " + getFactory().getConfigTag());
			}
		}
		return unique_properties;
	}

	@SuppressWarnings("unchecked")
	public ExpressionTargetContainer findDuplicate(PropertyContainer r) throws Exception {
		ExpressionTargetFactory<T> etf = getExpressionTargetFactory();
		// This is a default implementation. Many factories
		// implement this method directly ignoring this implementation
		try {
			AndRecordSelector sel = new AndRecordSelector();
			for (PropertyTag<?> t : getUniqueProperties()) {
				if (etf.hasProperty(t)) {
					sel.add(new SelectClause(t, r));
				}
			}
			T record = getFactory().find(etf.getAccessorMap().getFilter(sel), true);
			if (record == null) {
				return null;
			}
			AccessorMap<T> map = etf.getAccessorMap();
			return map.getProxy(record);
		} catch (Exception e) {
			throw new ConsistencyError("Required property not present", e);
		}
	}

	@Deprecated
	public String getUniqueID(ExpressionTargetContainer r) throws Exception {
		StringBuilder sb = new StringBuilder();
		sb.append(getFactory().getTag());
		for (PropertyTag t : getUniqueProperties()) {
			Object o = r.getProperty(t, null);
			if (o != null) {
				sb.append("-");
				if (o instanceof IndexedReference) {
					sb.append(((IndexedReference) o).getID());
				} else if (o instanceof Date) {
					sb.append(((Date) o).getTime());
				} else {
					sb.append(o.toString());
				}
			}
		}
		return sb.toString();
	}
	@Override
	public void addSummaryContent(ContentBuilder cb) {
		super.addSummaryContent(cb);
		cb.addHeading(3, "Unique properties");
		cb.addList(getUniqueProperties());
	}



	@Override
	public void addConfigParameters(Set<String> params) {
		super.addConfigParameters(params);
		params.add(UNIQUE_PROPERTIES_PREFIX+getFactory().getConfigTag());
	}
}