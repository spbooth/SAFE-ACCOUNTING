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

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import uk.ac.ed.epcc.safe.accounting.ExpressionTargetFactory;
import uk.ac.ed.epcc.safe.accounting.expr.DerivedPropertyMap;
import uk.ac.ed.epcc.safe.accounting.expr.ExpressionTargetContainer;
import uk.ac.ed.epcc.safe.accounting.expr.PropExpressionMap;
import uk.ac.ed.epcc.safe.accounting.properties.InvalidPropertyException;
import uk.ac.ed.epcc.safe.accounting.properties.PropertyContainer;
import uk.ac.ed.epcc.safe.accounting.properties.PropertyFinder;
import uk.ac.ed.epcc.safe.accounting.properties.PropertyMap;
import uk.ac.ed.epcc.safe.accounting.properties.PropertyTag;
import uk.ac.ed.epcc.safe.accounting.selector.AndRecordSelector;
import uk.ac.ed.epcc.safe.accounting.selector.SelectClause;
import uk.ac.ed.epcc.safe.accounting.update.AccountingParseException;
import uk.ac.ed.epcc.safe.accounting.update.IncrementalPropertyContainerParser;
import uk.ac.ed.epcc.safe.accounting.update.PlugInOwner;
import uk.ac.ed.epcc.safe.accounting.update.PropertyContainerParser;
import uk.ac.ed.epcc.safe.accounting.update.PropertyContainerPolicy;
import uk.ac.ed.epcc.safe.accounting.update.UsageRecordPolicy;
import uk.ac.ed.epcc.webapp.Contexed;
import uk.ac.ed.epcc.webapp.exceptions.ConsistencyError;
import uk.ac.ed.epcc.webapp.model.data.Composite;
import uk.ac.ed.epcc.webapp.model.data.DataObjectFactory;
import uk.ac.ed.epcc.webapp.model.data.Exceptions.DataFault;
import uk.ac.ed.epcc.webapp.model.data.reference.IndexedReference;
/** Class to implement {@link UsageRecordParseTarget} using a {@link PlugInOwner} and
 * a {@link UsageRecordFactory}.  This is usually used in composition so that the UsageRecordFactory
 * can implement the interface directly.
 * 
 * Note that the default implementation of {@link #findDuplicate(PropertyContainer)} method
 * provided in this class is independent of the other methods in UsageParseTarget so sub-classes can override and 
 * Factories using this class in composition can re-implement this method.
 * @author spb
 *
 * @param <T> type of usage record
 * @param <R> Parser IR type
 */
public class UsageRecordParseTargetPlugIn<T extends UsageRecordFactory.Use,R> extends Composite<T, UsageRecordParseTargetPlugIn>implements
		UsageRecordParseTarget<R>,Contexed{

	
	
	public UsageRecordParseTargetPlugIn(UsageRecordFactory<T> fac){
		super(fac);
	}
	
	public boolean parse(DerivedPropertyMap map, R current_line)
			throws AccountingParseException {
		PlugInOwner<R> plugin_owner = getPlugInOwner();
		// Note each stage of the parse sees the derived properties 
				// as defined in the previous stage. Once its own parse is complete
				// It can then override the definition if it wants to
		PropExpressionMap derived = new PropExpressionMap();
		PropertyContainerParser<R> parser = plugin_owner.getParser();
		if( parser.parse(map, current_line) ){
			derived = parser.getDerivedProperties(derived);
			map.addDerived(derived);
			// apply policy
			for(PropertyContainerPolicy pol : plugin_owner.getPolicies()){
	    		pol.parse(map);
	    		derived = pol.getDerivedProperties(derived);
	    		map.addDerived(derived);
	    	}
			Set<PropertyTag> unique = getUniqueProperties();
			if( unique != null ){
				for(PropertyTag<?> t : unique){
					if( map.getProperty(t) == null){
						throw new AccountingParseException("Missing key property "+t.getFullName());
					}
				}
			}
			return true;
		}else{
			return false;
		}
	}

	public PropertyContainerParser<R> getParser(){
		return getPlugInOwner().getParser();
	}
	private PlugInOwner<R> getPlugInOwner(){
		DataObjectFactory<T> fac = getFactory();
		if( fac instanceof PlugInOwner) {
			return  (PlugInOwner<R>) fac;
		}
		// TODO look for composites that implement PlugInOwner
		throw new ConsistencyError("No PlugInOwner");
	}
	private ExpressionTargetFactory<T> getExpressionTargetFactory(){
		DataObjectFactory<T> fac = getFactory();
		if( fac instanceof ExpressionTargetFactory) {
			return (ExpressionTargetFactory<T>) fac;
		}
		ExpressionTargetFactory<T> comp =fac.getComposite(ExpressionTargetFactoryComposite.class);
		if( comp != null) {
			return comp;
		}
		throw new ConsistencyError("No ExpressionTargetFactory");
	}
 
	public void startParse(PropertyMap defaults) throws Exception {
		PlugInOwner<R> plugin_owner = getPlugInOwner();
		PropertyContainerParser tmp_parser = plugin_owner.getParser();
		tmp_parser.startParse(defaults);
		for(PropertyContainerPolicy pol : plugin_owner.getPolicies()){
			pol.startParse(defaults);
		}
	}

	public StringBuilder endParse() {
		PlugInOwner<R> plugin_owner = getPlugInOwner();
		StringBuilder tmp = new StringBuilder();
    	for(PropertyContainerPolicy pol : plugin_owner.getPolicies()){
    		//tmp.append(pol.getClass().getCanonicalName());
    		//tmp.append("\n");
    		tmp.append(pol.endParse());
    		//tmp.append("-----------------------\n");
    	}
    	PropertyContainerParser parser = plugin_owner.getParser();
    	//tmp.append(parser.getClass().getCanonicalName());
		//tmp.append("\n");
    	tmp.append(parser.endParse());
    	//tmp.append("-----------------------\n");
		return tmp;
	}

	public PropertyFinder getFinder() {
		return getExpressionTargetFactory().getFinder();
	}
	public static final String UNIQUE_PROPERTIES_PREFIX = "unique-properties.";
	Set<PropertyTag> unique_properties=null; // records where all properties match are considered duplicates
	protected Set<PropertyTag> parsePropertyList(String list) throws InvalidPropertyException{
    	HashSet<PropertyTag> res = new HashSet<PropertyTag>();
    	ExpressionTargetFactory<T> etf = getExpressionTargetFactory();
    	PropertyFinder finder = getFinder();
    	if( finder != null ){
    		for( String name : list.trim().split(",")){
    			PropertyTag<?> t = finder.make(name);
    			if( ! etf.hasProperty(t)){
    				throw new InvalidPropertyException(t);
    			}
    			res.add(t);
    		}
    	}
    	return res;
    }
	/** Get the set of unique properties to use for detecting re-inserts.
	 * 
	 * @return
	 */
	protected Set<PropertyTag> getUniqueProperties(){
		PlugInOwner<R> plugin_owner = getPlugInOwner();
		if(unique_properties == null ){
			// Must evaluate late as we need to parser/policies to define supported properties
	    	String unique_prop_list = getContext().getInitParameter(UNIQUE_PROPERTIES_PREFIX+getFactory().getConfigTag());
	    	if( unique_prop_list != null ){ 
	    		try {
					unique_properties=parsePropertyList(unique_prop_list);
				} catch (InvalidPropertyException e) {
					getLogger().error("Invalid property specified as unique",e);
				}
	    	}else{
	    		unique_properties = plugin_owner.getParser().getDefaultUniqueProperties();
	    	}
	    	if( unique_properties == null){
	    		throw new ConsistencyError("No unique properties defined for "+getFactory().getConfigTag());
	    	}
		}
		return unique_properties;
	}
	@SuppressWarnings("unchecked")
	public ExpressionTargetContainer findDuplicate(PropertyContainer r) throws Exception {
		ExpressionTargetFactory<T> etf = getExpressionTargetFactory();
		// This is a default implementation. Many factories 
		// implement this method directly ignoring this implementation
		try{
			AndRecordSelector sel = new AndRecordSelector();
			for(PropertyTag<?> t : getUniqueProperties()){
				if( etf.hasProperty(t)){
					sel.add(new SelectClause(t,r));
				}
			}
			T record = getFactory().find(sel.visit(new FilterSelectVisitor<>(etf)),true);
			if( record == null) {
				return null;
			}
			AccessorMap<T> map = etf.getAccessorMap();
			return map.getProxy(record);
		}catch(Exception e){
			throw new ConsistencyError("Required property not present",e);
		}
	}

	public boolean isComplete(ExpressionTargetContainer r) {
		PlugInOwner<R> plugin_owner = getPlugInOwner();
		PropertyContainerParser parser= plugin_owner.getParser();
		if( parser instanceof IncrementalPropertyContainerParser){
		   return ((IncrementalPropertyContainerParser)parser).isComplete(r);	
		}
		// for non incremental records are always complete
		return true;
	}

	public void deleteRecord(ExpressionTargetContainer old_record) throws Exception, DataFault {
		PlugInOwner<R> plugin_owner = getPlugInOwner();
		for(PropertyContainerPolicy pol : plugin_owner.getPolicies()){
			if( pol instanceof UsageRecordPolicy){
				((UsageRecordPolicy)pol).preDelete(old_record);
			}
		}
		old_record.delete();
	}

	public boolean commitRecord(PropertyContainer map, ExpressionTargetContainer record)
			throws DataFault {
		PlugInOwner<R> plugin_owner = getPlugInOwner();
		record.commit(); // create it
		if( isComplete(record)){
			// apply post create once complete
			try{
				PropertyContainerParser parser= plugin_owner.getParser();
				if( parser instanceof IncrementalPropertyContainerParser){
				   ((IncrementalPropertyContainerParser)parser).postComplete(record);
				}
				for(PropertyContainerPolicy pol : plugin_owner.getPolicies()){
					if( pol instanceof UsageRecordPolicy){
						((UsageRecordPolicy)pol).postCreate(map,record);
					}
				}
			}catch(Exception e){
				getLogger().error("Error in record post-create",e);
			}
			return true;
		}
		return false;
	}
	public boolean updateRecord(DerivedPropertyMap map, ExpressionTargetContainer record)
			throws Exception {
		PlugInOwner<R> plugin_owner = getPlugInOwner();
		// incomplete records have not called postCreate
		if( isComplete(record)){
			
			// revert the side effects based on old state.
			for(PropertyContainerPolicy pol : plugin_owner.getPolicies()){
				if( pol instanceof UsageRecordPolicy){
					((UsageRecordPolicy)pol).preDelete(record);
				}
			}
			
			// Note record is the old record being replaced.
			// overwrite any properties generated in the new parse
			map.setContainer(record);
			record.commit(); // update it
			
			// apply side effects for new state
			try{
				PropertyContainerParser parser= plugin_owner.getParser();
				if( parser instanceof IncrementalPropertyContainerParser){
				   ((IncrementalPropertyContainerParser)parser).postComplete(record);
				}
				for(PropertyContainerPolicy pol : plugin_owner.getPolicies()){
					if( pol instanceof UsageRecordPolicy){
						((UsageRecordPolicy)pol).postCreate(map,record);
					}
				}
			}catch(Exception e){
				getLogger().error("Error in record post-create",e);
			}
			
		}else{
			// do a rescan of an incomplete record (no side effects)
			// Note record is the old record being replaced.
			// overwrite any properties generated in the new parse
			map.setContainer(record);
			return record.commit(); // update it
		}
		return false;
	}

	public boolean allowReplace(DerivedPropertyMap map, ExpressionTargetContainer record) {
		PlugInOwner<R> plugin_owner = getPlugInOwner();
		for(PropertyContainerPolicy pol : plugin_owner.getPolicies()){
			if( pol instanceof UsageRecordPolicy){
				if( ! ((UsageRecordPolicy)pol).allowReplace(map, record)){
					return false;
				}
			}
		}
		return true;
	}


	public ExpressionTargetContainer prepareRecord(DerivedPropertyMap map) throws DataFault,
			InvalidPropertyException, AccountingParseException {
		T record =  getFactory().makeBDO();
		ExpressionTargetContainer proxy = getExpressionTargetFactory().getAccessorMap().getProxy(record);
		int count=map.setContainer(proxy);
		
		
		if( count == 0 ){
			throw new AccountingParseException("No properties match parse");
		}
//		if( record.getProperty(BaseParser.ENDED_PROP, null)== null ){
//			throw new AccountingParseException("No end date");
//		}
		return proxy;
	}
	@Deprecated
	public String getUniqueID(ExpressionTargetContainer r) throws Exception {
		StringBuilder sb = new StringBuilder();
		sb.append(getFactory().getTag());
		for(PropertyTag t : getUniqueProperties()){
			Object o = r.getProperty(t, null);
			if( o != null ){
				sb.append("-");
				if(o instanceof IndexedReference){
					sb.append(((IndexedReference)o).getID());
				}else if( o instanceof Date){
					sb.append(((Date)o).getTime());
				}else{
					sb.append(o.toString());
				}
			}
		}
		return sb.toString();
	}
	@Override
	protected Class<? super UsageRecordParseTargetPlugIn> getType() {
		return UsageRecordParseTargetPlugIn.class;
	}
	
	
}