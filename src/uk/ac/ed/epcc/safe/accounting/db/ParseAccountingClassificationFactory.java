// Copyright - The University of Edinburgh 2011
/*******************************************************************************
 * Copyright (c) - The University of Edinburgh 2010
 *******************************************************************************/
package uk.ac.ed.epcc.safe.accounting.db;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import uk.ac.ed.epcc.safe.accounting.db.transitions.SummaryProvider;
import uk.ac.ed.epcc.safe.accounting.expr.DerivedPropertyMap;
import uk.ac.ed.epcc.safe.accounting.expr.PropExpressionMap;
import uk.ac.ed.epcc.safe.accounting.expr.PropertyCastException;
import uk.ac.ed.epcc.safe.accounting.properties.MultiFinder;
import uk.ac.ed.epcc.safe.accounting.properties.PropertyContainer;
import uk.ac.ed.epcc.safe.accounting.properties.PropertyFinder;
import uk.ac.ed.epcc.safe.accounting.properties.PropertyMap;
import uk.ac.ed.epcc.safe.accounting.properties.PropertyRegistry;
import uk.ac.ed.epcc.safe.accounting.properties.PropertyTag;
import uk.ac.ed.epcc.safe.accounting.reference.ReferencePropertyRegistry;
import uk.ac.ed.epcc.safe.accounting.selector.FilterSelector;
import uk.ac.ed.epcc.safe.accounting.selector.RecordSelector;
import uk.ac.ed.epcc.safe.accounting.update.AccountingParseException;
import uk.ac.ed.epcc.safe.accounting.update.ConfigPlugInOwner;
import uk.ac.ed.epcc.safe.accounting.update.PlugInOwner;
import uk.ac.ed.epcc.safe.accounting.update.PropertyContainerParser;
import uk.ac.ed.epcc.safe.accounting.update.PropertyContainerPolicy;
import uk.ac.ed.epcc.webapp.AppContext;
import uk.ac.ed.epcc.webapp.content.ContentBuilder;
import uk.ac.ed.epcc.webapp.jdbc.table.TableSpecification;
import uk.ac.ed.epcc.webapp.jdbc.table.TransitionSource;
import uk.ac.ed.epcc.webapp.model.Classification;
import uk.ac.ed.epcc.webapp.model.data.DataObject;
import uk.ac.ed.epcc.webapp.model.data.Repository.Record;
import uk.ac.ed.epcc.webapp.model.data.Exceptions.DataFault;
import uk.ac.ed.epcc.webapp.model.data.forms.inputs.DataObjectItemInput;
import uk.ac.ed.epcc.webapp.session.SessionService;
/** Factory class for AccountingClassification objects with a defined parser.
 * 
 * By default the properties are generated from the Database fields but additional properties can be
 * defined as derived properties
 * @author spb
 *
 * @param <T>
 */
@uk.ac.ed.epcc.webapp.Version("$Id: ParseAccountingClassificationFactory.java,v 1.53 2015/08/13 21:52:34 spb Exp $")

public class ParseAccountingClassificationFactory<T extends AccountingClassification>
extends PropertyTargetClassificationFactory<T> implements PlugInOwner ,
ClassificationParseTarget<T>, FilterSelector<DataObjectItemInput<T>>{
	private PropertyFinder reg=null;
	private AccessorMap<T> map=null;
	
	private PlugInOwner plugin_owner=null;
	private PropertyTag<String> match_prop=null;

	public ParseAccountingClassificationFactory(AppContext c, String table) {
		super(c, table);
		// Logger log = getLogger();

		//defer creation
		//initAccessorMap(c, table);

	}

	
	@SuppressWarnings("unchecked")
	private void initAccessorMap(AppContext c, String table) {
		map = new AccessorMap<T>(getTarget(),res,table);
		MultiFinder finder = new MultiFinder();
		finder.addFinder(AccountingClassificationFactory.classification);
		ReferencePropertyRegistry refs = ReferencePropertyRegistry.getInstance(c);
		map.makeReferences(refs);
		finder.addFinder(refs);
		plugin_owner= new ConfigPlugInOwner<ParseAccountingClassificationFactory<T>>(c,finder ,table);
		finder.addFinder(getPluginOwner().getFinder());


		PropertyRegistry def = new PropertyRegistry(table,"Properties for table "+table);
		map.populate(finder, def,false);
		map.addDerived(c, getPluginOwner().getDerivedProperties());
		finder.addFinder(def);

		finder.addFinder(map.setRelationshipProperties(table));
		match_prop = (PropertyTag<String>) finder.find(String.class,c.getInitParameter(table+".match",Classification.NAME));
		if( match_prop == null ){
			c.error("No match property defined");
		}
		
		PropertyRegistry derived = new PropertyRegistry(table+"DerivedProperties","Derived properties for table "+table);
		PropExpressionMap expression_map = new PropExpressionMap();
		expression_map.addFromProperties(derived, finder, c, table);
		if( match_prop != null && ! match_prop.equals(AccountingClassificationFactory.NAME_PROP)){
			try {
				expression_map.put(AccountingClassificationFactory.NAME_PROP, match_prop);
			} catch (PropertyCastException e) {
				c.error(e,"Error adding derived mapping for name");
			}
		}
		map.addDerived(c, expression_map);
		finder.addFinder(derived);

		
		reg=finder;
	}
	public PropertyFinder getFinder() {
		if( reg == null){
			initAccessorMap(getContext(), getConfigTag());
		}
		return reg;
	}
	
	public final AccessorMap<T> getAccessorMap(){
		if( map == null ){
			initAccessorMap(getContext(), getConfigTag());
		}
		return map;
	}
	



	public class ParseClassificationRegistry extends PropertyTargetClassificationTableRegistry{

		@SuppressWarnings("unchecked")
		public ParseClassificationRegistry() {
			if( getPluginOwner() instanceof TransitionSource){
				addTransitionSource((TransitionSource) getPluginOwner());
			}

		}

		@Override
		public void getTableTransitionSummary(ContentBuilder hb, SessionService operator) {
			super.getTableTransitionSummary(hb, operator);
			if( getPluginOwner() instanceof SummaryProvider){
				((SummaryProvider) getPluginOwner()).getTableTransitionSummary(hb, operator);
			}

		}

	}

	@Override
	public PropertyTargetClassificationTableRegistry makeTableRegistry(){
		return new ParseClassificationRegistry();
	}

	@Override
	public void resetStructure() {
		super.resetStructure();
		initAccessorMap(getContext(), getConfigTag());

	}
	public PropertyContainerParser getParser() {
		return getPluginOwner().getParser();
	}

	public Set<PropertyContainerPolicy> getPolicies() {
		return getPluginOwner().getPolicies();
	}

	@Override
	public PropExpressionMap getDerivedProperties() {
		return getAccessorMap().getDerivedProperties();
	}
	public PropertyMap getGlobals(Map<String, Object> params) {
		return null;
	}

	public T make(PropertyContainer value) throws AccountingParseException {
		
		if( match_prop == null ){
			throw new AccountingParseException("No match property specified for "+getTag());
		}
		String name = value.getProperty(match_prop, null);
	
		if( name == null ){
			throw new AccountingParseException("No name parsed");
		}


		try {
			// we don't use makeByName here as it also applies the policies.
			T record = findFromString(name);
			if( record == null ){
				record = makeBDO();
				record.setName(name);
			}
			return record;
		} catch (DataFault e) {
			throw new AccountingParseException("Error making name: " + e.getMessage());
		}

	}

	@Override
	protected void postMakeByName(T c, String name) {
		// apply policies in case they apply actions based on the name.
		Set<PropertyContainerPolicy> pol = getPolicies();
		if(match_prop != null && pol != null && pol.size() > 0){
			try{
			PropertyMap map = new PropertyMap();
			map.setProperty(match_prop, name);
			for(PropertyContainerPolicy p : pol){
				p.startParse(null);
			}
			for(PropertyContainerPolicy p : pol){
				p.parse(map);
			}
			for(PropertyContainerPolicy p : pol){
				p.endParse();
			}
			map.setContainer(c);
			}catch(Exception e){
				getContext().error(e,"Error applying policies in postMakeByName");
			}
		}
	}

	public StringBuilder endParse() {
		StringBuilder sb = new StringBuilder();
		sb.append( getParser().endParse());
		for(PropertyContainerPolicy pol : getPolicies()){
			sb.append(pol.endParse());
		}
		return sb;
	}

	public boolean parse(DerivedPropertyMap map, String currentLine)
	throws AccountingParseException {
		// Note each stage of the parse sees the derived properties 
		// as defined in the previous stage. Once its own parse is complete
		// It can then override the definition if it wants to
		PropExpressionMap derived = new PropExpressionMap();
		PropertyContainerParser parser = getParser();
		if( parser.parse(map, currentLine) ){
			derived=parser.getDerivedProperties(derived);
			map.addDerived(derived);
			for(PropertyContainerPolicy pol : getPolicies()){
				pol.parse(map);
				derived = pol.getDerivedProperties(derived);
				map.addDerived(derived);
			}
			return true;
		}
		return false;
	}

	public Iterator<String> splitRecords(String update)
	throws AccountingParseException {

		return getParser().splitRecords(update);
	}

	public void startParse(PropertyMap defaults) throws Exception {
		PropertyContainerParser p = getParser();
		p.startParse(defaults);
		for(PropertyContainerPolicy pol : getPolicies()){
			pol.startParse(defaults);
		}
	}

	@Override
	public Class<? super T> getTarget() {
		return AccountingClassification.class;
	}

	@Override
	protected DataObject makeBDO(Record res) throws DataFault {
		return new AccountingClassification(this,res);
	}

	@Override
	public TableSpecification getDefaultTableSpecification(AppContext c,String table) {
		try{
		TableSpecification spec = super.getDefaultTableSpecification(c,table);
		if( spec != null ){
			// Don't use anything that needs getContext as this does not work unless
			// factory is valid.
			PlugInOwner owner = new ConfigPlugInOwner<ParseAccountingClassificationFactory<T>>(c,null, table);
			PropertyContainerParser parser=owner.getParser();
			if( parser == null){
				return null;
			}
			PropExpressionMap map = new PropExpressionMap();
			map = parser.getDerivedProperties(map);
			for(PropertyContainerPolicy pol : owner.getPolicies()){
				map = pol.getDerivedProperties(map);
			}
			spec = parser.modifyDefaultTableSpecification(c,spec,map,table);
			if( spec != null ){
				for(PropertyContainerPolicy pol : owner.getPolicies()){
					spec = pol.modifyDefaultTableSpecification(c,spec,map,table);
				}
			}
		}
		return spec;
		}catch(Throwable t){
			c.error(t,"Error in getDefaultTableSpecification");
			return null;
		}
	}

	public DataObjectItemInput<T> getInput(RecordSelector sel) throws Exception {
		
		return new DataObjectInput(sel.visit(new FilterSelectVisitor<T>(this)));
	}


	private PlugInOwner getPluginOwner() {
		if( plugin_owner == null ){
			initAccessorMap(getContext(), getConfigTag());
		}
		return plugin_owner;
	}

	@Override
	public void release() {
		if( map != null){
			map.release();
			map=null;
		}
		reg=null;
		super.release();
	}
	



}