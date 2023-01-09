//| Copyright - The University of Edinburgh 2015                            |
//|                                                                         |
//| Licensed under the Apache License, Version 2.0 (the "License");         |
//| you may not use this file except in compliance with the License.        |
//| You may obtain a copy of the License at                                 |
//|                                                                         |
//|    http://www.apache.org/licenses/LICENSE-2.0                           |
//|                                                                         |
//| Unless required by applicable law or agreed to in writing, software     |
//| distributed under the License is distributed on an "AS IS" BASIS,       |
//| WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.|
//| See the License for the specific language governing permissions and     |
//| limitations under the License.                                          |
package uk.ac.ed.epcc.safe.accounting.db;

import java.util.Map;
import java.util.Set;

import uk.ac.ed.epcc.safe.accounting.ExpressionTargetFactory;
import uk.ac.ed.epcc.safe.accounting.expr.DerivedPropertyMap;
import uk.ac.ed.epcc.safe.accounting.expr.ExpressionTargetContainer;
import uk.ac.ed.epcc.safe.accounting.expr.PropExpressionMap;
import uk.ac.ed.epcc.safe.accounting.properties.InvalidPropertyException;
import uk.ac.ed.epcc.safe.accounting.properties.PropertyContainer;
import uk.ac.ed.epcc.safe.accounting.properties.PropertyMap;
import uk.ac.ed.epcc.safe.accounting.properties.PropertyTag;
import uk.ac.ed.epcc.safe.accounting.properties.StandardProperties;
import uk.ac.ed.epcc.safe.accounting.selector.AndRecordSelector;
import uk.ac.ed.epcc.safe.accounting.selector.NullSelector;
import uk.ac.ed.epcc.safe.accounting.selector.PeriodOverlapRecordSelector;
import uk.ac.ed.epcc.safe.accounting.selector.RecordSelector;
import uk.ac.ed.epcc.safe.accounting.update.AccountingParseException;
import uk.ac.ed.epcc.safe.accounting.update.IncrementalPropertyContainerParser;
import uk.ac.ed.epcc.safe.accounting.update.PlugInOwner;
import uk.ac.ed.epcc.safe.accounting.update.PropertyContainerParser;
import uk.ac.ed.epcc.safe.accounting.update.PropertyContainerPolicy;
import uk.ac.ed.epcc.safe.accounting.update.UsageRecordPolicy;
import uk.ac.ed.epcc.webapp.AppContext;
import uk.ac.ed.epcc.webapp.Contexed;
import uk.ac.ed.epcc.webapp.CurrentTimeService;
import uk.ac.ed.epcc.webapp.content.ContentBuilder;
import uk.ac.ed.epcc.webapp.exceptions.ConsistencyError;
import uk.ac.ed.epcc.webapp.forms.Form;
import uk.ac.ed.epcc.webapp.forms.action.FormAction;
import uk.ac.ed.epcc.webapp.forms.exceptions.ActionException;
import uk.ac.ed.epcc.webapp.forms.exceptions.TransitionException;
import uk.ac.ed.epcc.webapp.forms.inputs.SimplePeriodInput;
import uk.ac.ed.epcc.webapp.forms.result.FormResult;
import uk.ac.ed.epcc.webapp.forms.result.MessageResult;
import uk.ac.ed.epcc.webapp.forms.transition.AbstractFormTransition;
import uk.ac.ed.epcc.webapp.forms.transition.ExtraFormTransition;
import uk.ac.ed.epcc.webapp.forms.transition.Transition;
import uk.ac.ed.epcc.webapp.jdbc.DatabaseService;
import uk.ac.ed.epcc.webapp.jdbc.table.AddClassificationReferenceTransition;
import uk.ac.ed.epcc.webapp.jdbc.table.AdminOperationKey;
import uk.ac.ed.epcc.webapp.jdbc.table.StringFieldType;
import uk.ac.ed.epcc.webapp.jdbc.table.TableSpecification;
import uk.ac.ed.epcc.webapp.jdbc.table.TableTransitionContributor;
import uk.ac.ed.epcc.webapp.jdbc.table.TableTransitionKey;
import uk.ac.ed.epcc.webapp.logging.LoggerService;
import uk.ac.ed.epcc.webapp.model.data.CloseableIterator;
import uk.ac.ed.epcc.webapp.model.data.DataObjectFactory;
import uk.ac.ed.epcc.webapp.model.data.Exceptions.DataFault;
import uk.ac.ed.epcc.webapp.session.SessionService;
import uk.ac.ed.epcc.webapp.time.Period;
import uk.ac.ed.epcc.webapp.timer.TimeClosable;

/**
 * Class to implement {@link UsageRecordParseTarget} using a {@link PlugInOwner}
 * and a {@link UsageRecordFactory}. This is usually used in composition so that
 * the UsageRecordFactory can implement the interface directly.
 * 
 * Note that the default implementation of
 * {@link #findDuplicate(PropertyContainer)} method provided in this class is
 * independent of the other methods in UsageParseTarget so sub-classes can
 * override and Factories using this class in composition can re-implement this
 * method.
 * 
 * @author spb
 *
 * @param <T>
 *            type of usage record
 * @param <R>
 *            Parser IR type
 */
public abstract class UsageRecordParseTargetPlugIn<T extends UsageRecordFactory.Use, R>
		extends PropertyContainerParseTargetComposite<T, R>
		implements UsageRecordParseTarget<R>, Contexed, TableTransitionContributor {

	public UsageRecordParseTargetPlugIn(UsageRecordFactory<T> fac) {
		super(fac);
	}

	@Override
	public boolean parse(DerivedPropertyMap map, R current_line) throws AccountingParseException {
		PlugInOwner<R> plugin_owner = getPlugInOwner();
		// Note each stage of the parse sees the derived properties
		// as defined in the previous stage. Once its own parse is complete
		// It can then override the definition if it wants to.
		// When created by the calling code the map should have the final
		// set of derivations installed. (This is needed to handle constant defaults)
		// so the definition seen by a stage is either
		// 1) the most recent definition set by a previous stage
		// 2) the latest definition set by itself or subsequent stages 
		PropExpressionMap derived = new PropExpressionMap();
		PropertyContainerParser<R> parser = plugin_owner.getParser();
		if( parser == null) {
			throw new AccountingParseException("No PropertyContainerParser");
		}
		if (parser.parse(map, current_line)) {
			derived = parser.getDerivedProperties(derived);
			map.addDerived(derived);
			// apply policy
			for (PropertyContainerPolicy pol : plugin_owner.getPolicies()) {
				pol.parse(map);
				// update the derived properties, mostly this will re-install the same definitions
				// unless the policy is overriding 
				derived = pol.getDerivedProperties(derived);
				map.addDerived(derived);
			}
			Set<PropertyTag> unique = getUniqueProperties();
			if (unique != null) {
				for (PropertyTag<?> t : unique) {
					if (map.getProperty(t) == null) {
						throw new AccountingParseException("Missing key property " + t.getFullName());
					}
				}
			}
			return true;
		} else {
			return false;
		}
	}

	@Override
	public void lateParse(DerivedPropertyMap map) throws AccountingParseException {
		PlugInOwner<R> plugin_owner = getPlugInOwner();
		for (PropertyContainerPolicy pol : plugin_owner.getPolicies()) {
			pol.lateParse(map);
		}
		
	}

	@Override
	public boolean isComplete(ExpressionTargetContainer r) {
		PlugInOwner<R> plugin_owner = getPlugInOwner();
		PropertyContainerParser parser = plugin_owner.getParser();
		if (parser instanceof IncrementalPropertyContainerParser) {
			return ((IncrementalPropertyContainerParser) parser).isComplete(r);
		}
		// for non incremental records are always complete
		return true;
	}

	@Override
	public void deleteRecord(ExpressionTargetContainer old_record) throws Exception, DataFault {
		PlugInOwner<R> plugin_owner = getPlugInOwner();
		for (PropertyContainerPolicy pol : plugin_owner.getPolicies()) {
			if (pol instanceof UsageRecordPolicy) {
				((UsageRecordPolicy) pol).preDelete(old_record);
			}
		}
		old_record.delete();
	}

	@Override
	public boolean commitRecord(PropertyMap map,ExpressionTargetContainer record) throws DataFault {
		PlugInOwner<R> plugin_owner = getPlugInOwner();
		
		record.commit(); // create it
		if (isComplete(record)) {
			// apply post create once complete
			try(TimeClosable cr = new TimeClosable(getContext(), "commitRecord")) {
				PropertyContainerParser parser = plugin_owner.getParser();
				if (parser instanceof IncrementalPropertyContainerParser) {
					((IncrementalPropertyContainerParser) parser).postComplete(record);
				}
				for (PropertyContainerPolicy pol : plugin_owner.getPolicies()) {
					if (pol instanceof UsageRecordPolicy) {
						((UsageRecordPolicy) pol).postCreate(map, record);
					}
				}
			} catch (Exception e) {
				getLogger().error("Error in record post-create", e);
			}
			return true;
		}
		return false;
	}

	/** update an existing record with values from a new {@link DerivedPropertyMap}
	 * @param map
	 * @param record
	 * @return true if record modified
	 */
	@Override
	public boolean updateRecord(DerivedPropertyMap map, ExpressionTargetContainer record) throws Exception {
		PlugInOwner<R> plugin_owner = getPlugInOwner();
		// incomplete records have not called postCreate
		if (isComplete(record)) {

			// revert the side effects based on old state.
			for (PropertyContainerPolicy pol : plugin_owner.getPolicies()) {
				if (pol instanceof UsageRecordPolicy) {
					((UsageRecordPolicy) pol).preDelete(record);
				}
			}

			// Note record is the old record being replaced.
			// overwrite any properties generated in the new parse
			map.setContainer(record);
			record.commit(); // update it

			// apply side effects for new state
			try {
				PropertyContainerParser parser = plugin_owner.getParser();
				if (parser instanceof IncrementalPropertyContainerParser) {
					((IncrementalPropertyContainerParser) parser).postComplete(record);
				}
				for (PropertyContainerPolicy pol : plugin_owner.getPolicies()) {
					if (pol instanceof UsageRecordPolicy) {
						((UsageRecordPolicy) pol).postCreate(map, record);
					}
				}
			} catch (Exception e) {
				getLogger().error("Error in record post-create", e);
			}
			// assme an update becasue of side effects
			return true;
		} else {
			// do a rescan of an incomplete record (no side effects)
			// we are assuming a rescan can't generate the missing properties
			// Note record is the old record being replaced.
			// overwrite any properties generated in the new parse
			map.setContainer(record);
			return record.commit(); // update it
		}
		
	}

	@Override
	public boolean allowReplace(DerivedPropertyMap map, ExpressionTargetContainer record) {
		PlugInOwner<R> plugin_owner = getPlugInOwner();
		for (PropertyContainerPolicy pol : plugin_owner.getPolicies()) {
			if (pol instanceof UsageRecordPolicy) {
				if (!((UsageRecordPolicy) pol).allowReplace(map, record)) {
					return false;
				}
			}
		}
		return true;
	}

	@Override
	public ExpressionTargetContainer prepareRecord(DerivedPropertyMap map)
			throws DataFault, InvalidPropertyException, AccountingParseException {
		T record = getFactory().makeBDO();
		ExpressionTargetContainer proxy = getExpressionTargetFactory().getAccessorMap().getContainer(record);
		int count = map.setContainer(proxy);

		if (count == 0) {
			throw new AccountingParseException("No properties match parse");
		}
		// if( record.getProperty(BaseParser.ENDED_PROP, null)== null ){
		// throw new AccountingParseException("No end date");
		// }
		return proxy;
	}

	protected static final String TEXT = "Text"; // raw text of line

	@Override
	public TableSpecification modifyDefaultTableSpecification(TableSpecification spec, String table) {
		TableSpecification my_spec = super.modifyDefaultTableSpecification(spec, table);
		if (my_spec != null) {
			spec.setOptionalField(TEXT, new StringFieldType(true, null, 512));
		}
		return my_spec;
	}

	/**
	 * Re-scan a set of records with a Text field. This will only work if any
	 * meta-data properties consumed by parsers or policies in the startParse method
	 * have been persisted within the database. This regenerates explicitly parsed
	 * values and merges them with the set of properties from the original record so
	 * any values stored in the database that came from derived properties won't be
	 * overwritten even if the values used in the derivation have been changed.
	 * 
	 * @param sel
	 * @return array or status counts [good,fails,updates]
	 * 
	 * @throws Exception
	 */
	public int[] rescan(RecordSelector sel) throws Exception {
		int count = 0;
		int good = 0;
		int fail = 0;
		int updates = 0;
		ExpressionTargetFactory<T> etf = getExpressionTargetFactory();
		if (etf.hasProperty(StandardProperties.TEXT_PROP)) {
			AccessorMap<T> amap = etf.getAccessorMap();
			DatabaseService db = getContext().getService(DatabaseService.class);
			try(CloseableIterator<T> it = etf.getIterator(sel)){
				while(it.hasNext()) {

					T o = it.next();
					ExpressionTargetContainer rec = amap.getContainer(o);
					count++;
					try {
						// make all previous props available to start parse
						// as we may need some initial properties to perfrom setup
						DerivedPropertyMap map = new DerivedPropertyMap(getContext());
						map.setAll(rec);
						startParse(map);
						String text = rec.getProperty(StandardProperties.TEXT_PROP, null);
						if (text != null && text.trim().length() > 0) {
							R ir = getParser().getRecord(text);
							if (parse(map, ir)) {
								lateParse(map);
								if( allowReplace(map, rec)) {
									if (updateRecord(map, rec)) {
										updates++;
									}
								}
								good++;
							} else {
								fail++;
							}
						}
					} catch (AccountingParseException e) {
						fail++;
						getLogger().error("Error in re-parse", e);
					}
					if( db != null) {
						db.commitTransaction();
					}
				}
			}
		}
		return new int[] { good, fail, updates };
	}
	/**
	 * Re-apply side effects for a set of records. This will only work if any
	 * meta-data properties consumed by parsers or policies in the startParse method
	 * have been persisted within the database. 
	 * Unlike {@link #rescan(RecordSelector)} this does not require a text field to re-parse
	 * 
	 * @param sel
	 * @return array or status counts [good,fails,updates]
	 * 
	 * @throws Exception
	 */
	public int[] reEvaluate(RecordSelector sel) throws Exception {
		int count = 0;
		int good = 0;
		int fail = 0;
		int updates = 0;
		ExpressionTargetFactory<T> etf = getExpressionTargetFactory();
		
			AccessorMap<T> amap = etf.getAccessorMap();
			try(CloseableIterator<T> it = etf.getIterator(sel)){
				while(it.hasNext()) {

					T o = it.next();
					ExpressionTargetContainer rec = amap.getContainer(o);
					count++;
					try {
						// make all previous props available to start parse
						// as we may need some initial properties to perfrom setup
						DerivedPropertyMap map = new DerivedPropertyMap(getContext());
						map.setAll(rec);
						startParse(map);

						if( allowReplace(map, rec)) {
							if (updateRecord(map, rec)) {
								updates++;
							}
						}
						good++;

					} catch (AccountingParseException e) {
						fail++;
						getLogger().error("Error in re-evaluate", e);
					}
				}
			}

			return new int[] { good, fail, updates };
	}
	public static class RescanTableTransition<P extends DataObjectFactory<?>> extends AbstractFormTransition<P>
			implements ExtraFormTransition<P> {

		private static final String PERIOD = "Period";

		public  class RescanAction extends FormAction {
			private final UsageRecordParseTargetPlugIn target;

			public RescanAction(UsageRecordParseTargetPlugIn target) {
				this.target = target;
			}

			@Override
			public FormResult action(Form f) throws ActionException {
				Period p = (Period) f.get(PERIOD);
				try {
					AndRecordSelector sel = new AndRecordSelector();
					sel.add(new PeriodOverlapRecordSelector(p, StandardProperties.ENDED_PROP));
					sel.add(new NullSelector<>(StandardProperties.TEXT_PROP, false));
					int result[] = target.rescan(sel);
					return new MessageResult("data_loaded", "Stored text", Integer.toString(result[0]),
							Integer.toString(result[1]), Integer.toString(result[2]));
				} catch (Exception e) {
					target.getContext().getService(LoggerService.class).getLogger(getClass()).error("Error rescaning",
							e);
					return new MessageResult("internal_error");
				}
			}

		}

		public void buildForm(Form f, P target, AppContext conn) throws TransitionException {
			f.addInput(PERIOD, PERIOD, new SimplePeriodInput(conn.getService(CurrentTimeService.class).getCurrentTime()));
			PropertyContainerParseTargetComposite plugin = target.getComposite(PropertyContainerParseTargetComposite.class);
			if( plugin == null || ! (plugin instanceof UsageRecordParseTargetPlugIn)) {
				throw new ConsistencyError("Expected composite not found");
			}
			f.addAction("Regenerate", new RescanAction((UsageRecordParseTargetPlugIn) plugin));
		}

		@Override
		public <X extends ContentBuilder> X getExtraHtml(X cb, SessionService<?> op, P target) {
			cb.addText("This operation will delete all selected records"
					+ " and replace them with a new record parsed from the stored text from the original upload");
			return cb;
		}
	}
	public static class ReEvaluateTableTransition<P extends DataObjectFactory<?>> extends AbstractFormTransition<P>
	implements ExtraFormTransition<P> {

private static final String PERIOD = "Period";

public  class ReEvaluateAction extends FormAction {
	private final UsageRecordParseTargetPlugIn target;

	public ReEvaluateAction(UsageRecordParseTargetPlugIn target) {
		this.target = target;
	}

	@Override
	public FormResult action(Form f) throws ActionException {
		Period p = (Period) f.get(PERIOD);
		try {
			AndRecordSelector sel = new AndRecordSelector();
			sel.add(new PeriodOverlapRecordSelector(p, StandardProperties.ENDED_PROP));
			int result[] = target.reEvaluate(sel);
			return new MessageResult("re_evaluate",  Integer.toString(result[0]),
					Integer.toString(result[1]), Integer.toString(result[2]));
		} catch (Exception e) {
			target.getContext().getService(LoggerService.class).getLogger(getClass()).error("Error rescaning",
					e);
			return new MessageResult("internal_error");
		}
	}

}

public void buildForm(Form f, P target, AppContext conn) throws TransitionException {
	f.addInput(PERIOD, PERIOD, new SimplePeriodInput(conn.getService(CurrentTimeService.class).getCurrentTime()));
	PropertyContainerParseTargetComposite plugin = target.getComposite(PropertyContainerParseTargetComposite.class);
	if( plugin == null || ! (plugin instanceof UsageRecordParseTargetPlugIn)) {
		throw new ConsistencyError("Expected composite not found");
	}
	f.addAction("Re-evaluate", new ReEvaluateAction((UsageRecordParseTargetPlugIn) plugin));
}

@Override
public <X extends ContentBuilder> X getExtraHtml(X cb, SessionService<?> op, P target) {
	cb.addText("This operation will re-apply side effects such as charging to the selected records");
	return cb;
}
}
	// public String getUniqueID(T r) throws Exception {
	//
	// return getParseTarget().getUniqueID(r);
	// }

	// public String getUniqueID(T r) throws Exception {
	//
	// return getParseTarget().getUniqueID(r);
	// }

	/*
	 * (non-Javadoc)
	 * 
	 * @see uk.ac.ed.epcc.safe.accounting.db.DataObjectPropertyFactory#
	 * getTableTransitions()
	 */
	@Override
		public Map<TableTransitionKey, Transition> getTableTransitions() {
			ExpressionTargetFactory<T> etf = getExpressionTargetFactory();
			DataObjectFactory<T> fac = getFactory();
			Map<TableTransitionKey, Transition> map = super.getTableTransitions();
			if( fac.getConfigTag().equals(fac.getTag())){
				// Don't allow transitions where the config is taken from
				// a different table
				if( etf.hasProperty(StandardProperties.TEXT_PROP) && etf.hasProperty(StandardProperties.ENDED_PROP)){
					map.put(new AdminOperationKey("Rescan", "Rescan all records stored as text"), new RescanTableTransition());
				}
				if( etf.hasProperty(StandardProperties.ENDED_PROP)){
					map.put(new AdminOperationKey("ReEvaluate", "Re-apply post-create side effects"), new ReEvaluateTableTransition());
				}
			}
			map.put(new AdminOperationKey("AddClassificationReference","Add a reference to a classification"), new AddClassificationReferenceTransition());
	
			
			return map;
		}

	

	

}