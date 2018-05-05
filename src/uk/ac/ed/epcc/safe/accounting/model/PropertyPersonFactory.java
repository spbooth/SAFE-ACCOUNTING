//| Copyright - The University of Edinburgh 2011                            |
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
/*******************************************************************************
 * Copyright (c) - The University of Edinburgh 2010
 *******************************************************************************/
package uk.ac.ed.epcc.safe.accounting.model;

import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import uk.ac.ed.epcc.safe.accounting.ExpressionTargetFactory;
import uk.ac.ed.epcc.safe.accounting.db.AccessorContributer;
import uk.ac.ed.epcc.safe.accounting.db.AccessorMap;
import uk.ac.ed.epcc.safe.accounting.db.CompatibleSelectVisitor;
import uk.ac.ed.epcc.safe.accounting.db.FilterSelectVisitor;
import uk.ac.ed.epcc.safe.accounting.db.PropertyMaker;
import uk.ac.ed.epcc.safe.accounting.db.RepositoryAccessorMap;
import uk.ac.ed.epcc.safe.accounting.db.UploadParseTarget;
import uk.ac.ed.epcc.safe.accounting.db.transitions.PropertyInfoGenerator;
import uk.ac.ed.epcc.safe.accounting.db.transitions.SummaryProvider;
import uk.ac.ed.epcc.safe.accounting.expr.DerivedPropertyMap;
import uk.ac.ed.epcc.safe.accounting.expr.ExpressionTargetContainer;
import uk.ac.ed.epcc.safe.accounting.expr.PropExpressionMap;
import uk.ac.ed.epcc.safe.accounting.properties.InvalidExpressionException;
import uk.ac.ed.epcc.safe.accounting.properties.MultiFinder;
import uk.ac.ed.epcc.safe.accounting.properties.PropExpression;
import uk.ac.ed.epcc.safe.accounting.properties.PropertyContainer;
import uk.ac.ed.epcc.safe.accounting.properties.PropertyFinder;
import uk.ac.ed.epcc.safe.accounting.properties.PropertyMap;
import uk.ac.ed.epcc.safe.accounting.properties.PropertyRegistry;
import uk.ac.ed.epcc.safe.accounting.properties.PropertyTag;
import uk.ac.ed.epcc.safe.accounting.properties.StandardProperties;
import uk.ac.ed.epcc.safe.accounting.reference.ReferencePropertyRegistry;
import uk.ac.ed.epcc.safe.accounting.selector.FilterSelector;
import uk.ac.ed.epcc.safe.accounting.selector.RecordSelector;
import uk.ac.ed.epcc.safe.accounting.update.AccountingParseException;
import uk.ac.ed.epcc.safe.accounting.update.ConfigPlugInOwner;
import uk.ac.ed.epcc.safe.accounting.update.NullParser;
import uk.ac.ed.epcc.safe.accounting.update.PlugInOwner;
import uk.ac.ed.epcc.safe.accounting.update.PropertyContainerParser;
import uk.ac.ed.epcc.safe.accounting.update.PropertyContainerPolicy;
import uk.ac.ed.epcc.webapp.AppContext;
import uk.ac.ed.epcc.webapp.Feature;
import uk.ac.ed.epcc.webapp.content.ContentBuilder;
import uk.ac.ed.epcc.webapp.forms.transition.Transition;
import uk.ac.ed.epcc.webapp.jdbc.exception.DataException;
import uk.ac.ed.epcc.webapp.jdbc.expr.CannotFilterException;
import uk.ac.ed.epcc.webapp.jdbc.filter.AndFilter;
import uk.ac.ed.epcc.webapp.jdbc.filter.BaseFilter;
import uk.ac.ed.epcc.webapp.jdbc.filter.CannotUseSQLException;
import uk.ac.ed.epcc.webapp.jdbc.filter.FilterConverter;
import uk.ac.ed.epcc.webapp.jdbc.filter.MatchCondition;
import uk.ac.ed.epcc.webapp.jdbc.filter.NoSQLFilterException;
import uk.ac.ed.epcc.webapp.jdbc.filter.OrderClause;
import uk.ac.ed.epcc.webapp.jdbc.table.AddClassificationReferenceTransition;
import uk.ac.ed.epcc.webapp.jdbc.table.AdminOperationKey;
import uk.ac.ed.epcc.webapp.jdbc.table.TableContentProvider;
import uk.ac.ed.epcc.webapp.jdbc.table.TableSpecification;
import uk.ac.ed.epcc.webapp.jdbc.table.TableStructureListener;
import uk.ac.ed.epcc.webapp.jdbc.table.TableTransitionContributor;
import uk.ac.ed.epcc.webapp.jdbc.table.TableTransitionKey;
import uk.ac.ed.epcc.webapp.model.data.DataObject;
import uk.ac.ed.epcc.webapp.model.data.DataObjectFactory;
import uk.ac.ed.epcc.webapp.model.data.Repository.Record;
import uk.ac.ed.epcc.webapp.model.data.Exceptions.DataFault;
import uk.ac.ed.epcc.webapp.model.data.forms.inputs.DataObjectItemInput;
import uk.ac.ed.epcc.webapp.model.data.iterator.SkipIterator;
import uk.ac.ed.epcc.webapp.session.AppUserFactory;
import uk.ac.ed.epcc.webapp.session.EmailNameFinder;
import uk.ac.ed.epcc.webapp.session.RoleUpdate;
import uk.ac.ed.epcc.webapp.session.SessionService;
import uk.ac.ed.epcc.webapp.session.SignupDateComposite;
import uk.ac.ed.epcc.webapp.session.WebNameFinder;

/** An {@link AppUserFactory} that supports accounting properties and parse mechanisms.
 * 
 * @author spb
 *
 * @param <P>
 */

public class PropertyPersonFactory<P extends PropertyPerson> extends AppUserFactory<P> implements ExpressionTargetFactory<P>, TableContentProvider, TableTransitionContributor, TableStructureListener,UploadParseTarget<String>, PlugInOwner<String>, FilterSelector<DataObjectItemInput<P>>{

	
	public static final Feature MAKE_ON_UPLOAD_FEATURE = new Feature("person.make_on_upload",true,"On a person upload unknown users will be created as well as existing ones updated");
	private static final PropertyRegistry person_registy = new PropertyRegistry("appuser","Properties associated with the Person class");
    public static final PropertyTag<String> WEBNAME_PROP = new PropertyTag<String>(person_registy,WebNameFinder.WEB_NAME,String.class,"Web authenticated REMOTE_USER name");
    public static final PropertyTag<String> EMAIL_PROP = new PropertyTag<String>(person_registy,EmailNameFinder.EMAIL,String.class,"Users Email address");
    public static final PropertyTag<Date> SIGNUP_PROP = new PropertyTag<Date>(person_registy,SignupDateComposite.SIGNUP_DATE,Date.class,"First access to system");
	public static final PropertyTag<Boolean> IS_ME_PROP = new PropertyTag<Boolean>(person_registy,"IsMe",Boolean.class,"Is this the current person");
	private PropertyFinder reg=null;
	private RepositoryAccessorMap<P> map=null;
	
    private PlugInOwner<String> plugin_owner=null;
    private PropertyTag<? extends String> match_prop=null;
	
	public PropertyPersonFactory() {
		super();
	}

	public PropertyPersonFactory(AppContext ctx, String table) {
		this();
		setContext(ctx, table);
	}

	public PropertyPersonFactory(AppContext ctx){
		this(ctx,"Person");
	}
	
	@Override
 	protected DataObject makeBDO(Record res) throws DataFault {
		return new PropertyPerson(this,res);
	}

	

	
	@Override
	protected List<OrderClause> getOrder() {
		List<OrderClause> order = super.getOrder();
		order.add(res.getOrder(WebNameFinder.WEB_NAME, false));
		return order;
	}
	
	protected void customAccessors(AccessorMap<P> mapi2,
			MultiFinder finder, PropExpressionMap derived) {
		
	}
	private void initAccessorMap(AppContext c, String tag) {
		map = new RepositoryAccessorMap<P>(this,res);
		MultiFinder finder = new MultiFinder();
		ReferencePropertyRegistry refs = ReferencePropertyRegistry.getInstance(c);
		map.makeReferences( refs);
		finder.addFinder(refs);
		finder.addFinder(person_registy);
		plugin_owner = new ConfigPlugInOwner<PropertyPersonFactory<P>,String>(c,finder ,tag);
		finder.addFinder(plugin_owner.getFinder());
		map.addDerived(c, plugin_owner.getDerivedProperties());
		String role_list = c.getExpandedProperty(RoleUpdate.ROLE_LIST_CONFIG);
		if( role_list != null ){
			@SuppressWarnings("unchecked")
			SessionService<P> serv = getContext().getService(SessionService.class);

			PropertyRegistry role_reg = new PropertyRegistry("roles", "role properties");
			for( String role : role_list.trim().split("\\s*,\\s*") ){
				PropertyTag<Boolean> role_tag = new PropertyTag<Boolean>(role_reg, role, Boolean.class);
				map.put(role_tag, new RoleAccessor<P>(serv, role));
			}
		}
		PropExpressionMap expression_map = new PropExpressionMap();
		PropertyRegistry def = new PropertyRegistry(tag,"Properties for table "+tag);
		
		
		for(AccessorContributer contrib : getComposites(AccessorContributer.class)){
			contrib.customAccessors(map, finder, expression_map);
		}
		customAccessors(map, finder, expression_map);
		
		map.populate(finder, def,false);
		finder.addFinder(def);
		
		
		
		PropertyRegistry derived = new PropertyRegistry(tag+"DerivedProperties","Derived properties for table "+tag);
		
		expression_map.addFromProperties(derived, finder, c, tag);
		map.addDerived(c, expression_map);
		finder.addFinder(derived);
		
		
		match_prop = finder.find(String.class,c.getInitParameter(tag+".match",WebNameFinder.WEB_NAME));
		reg=finder;
	}
	
	

	public PropertyFinder getFinder() {
		if( reg == null){
			initAccessorMap(getContext(), getConfigTag());
		}
		return reg;
	}

	public <X> boolean hasProperty(PropertyTag<X> tag){
		return getAccessorMap().hasProperty(tag);
	}
	
	public final RepositoryAccessorMap<P> getAccessorMap(){
		if( map == null ){
			initAccessorMap(getContext(), getConfigTag());
		}
		return map;
	}
	
	
	
	public final void resetStructure() {
		initAccessorMap(getContext(), getConfigTag());
	}


	public String getTableTransitionID() {
		return getTag();
	}

	
	public PropertyMap getGlobals(Map<String, Object> params) {
		return null;
	}

	/** Control if new records should be made when seen in an parse 
	 * 
	 * @return
	 */
	private final boolean makeOnUpload(){
		return MAKE_ON_UPLOAD_FEATURE.isEnabled(getContext());
	}
	public ExpressionTargetContainer make(PropertyContainer value) throws AccountingParseException{
		if( match_prop == null ){
			throw new AccountingParseException("No match property specified for "+getTag());
		}
		String name = value.getProperty(match_prop, null);
		if( name == null ){
			throw new AccountingParseException("No name parsed");
		}
		if( makeOnUpload()){
			try {
				return getAccessorMap().getProxy(makeFromString(name));
			} catch (Exception e) {
				throw new AccountingParseException("Cannot make new record",e);
			}
		}
		return getAccessorMap().getProxy(findFromString(name));
		
	}

	public StringBuilder endParse() {
		return new StringBuilder();
	}

	public boolean parse(DerivedPropertyMap map, String currentLine)
			throws AccountingParseException {
		return getParser().parse(map, currentLine);
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

	public PropertyContainerParser<String> getParser() {
		if(plugin_owner == null){
			initAccessorMap(getContext(), getConfigTag());
		}
		return plugin_owner.getParser();
	}

	public Set<PropertyContainerPolicy> getPolicies() {
		if(plugin_owner == null){
			initAccessorMap(getContext(), getConfigTag());
		}
		return plugin_owner.getPolicies();
	}

	public PropExpressionMap getDerivedProperties() {
		return getAccessorMap().getDerivedProperties();
	}

	@Override
	public Class<? super P> getTarget() {
		
		return PropertyPerson.class;
	}

	@SuppressWarnings("unchecked")
	public P find(PropertyMap template) throws CannotFilterException, DataException{
		AndFilter<P> fil = new AndFilter<P>(getTarget());
		AccessorMap map = getAccessorMap();
		for(PropertyTag t : template.propertySet()){
			fil.addFilter(map.getFilter(t, null, template.getProperty(t)));
		}
		return find(fil,true);
	}



	@Override
	protected TableSpecification getDefaultTableSpecification(AppContext c,String table) {
		TableSpecification spec = super.getDefaultTableSpecification(c,table);
		if( spec != null ){
			// Don't use anything that needs getContext as this does not work unless
			// factory is valid.
			PlugInOwner<String> owner = new ConfigPlugInOwner<PropertyPersonFactory<P>,String>(c,person_registy, table,NullParser.class);
			PropertyContainerParser<String> parser=owner.getParser();
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
	}

	
	public <I> boolean compatible(PropExpression<I> expr) {
		return getAccessorMap().resolves(expr,false);
	}
	
	
	public <R> BaseFilter<P> getRelationFilter(PropExpression<R> left,
			MatchCondition match, PropExpression<R> right)
			throws CannotFilterException {
		return getAccessorMap().getRelationFilter(left, match, right);
	}
	/** Get a filter from a {@link RecordSelector}
	 * 
	 * @param selector
	 * @return
	 * @throws CannotFilterException 
	 */
	
	protected BaseFilter<P> getFilter(RecordSelector selector) throws CannotFilterException {
		if( selector == null ){
			return null;
		}
		try {
			return selector.visit(new FilterSelectVisitor<P>(this));
		}catch(CannotFilterException e){
			throw e;
		} catch (Exception e) {
			throw new CannotFilterException(e);
		}
	}
	public final boolean compatible(RecordSelector sel){
		CompatibleSelectVisitor vis = new CompatibleSelectVisitor(null,getAccessorMap(),false);
		try {
			return sel.visit(vis);
		} catch (Exception e) {
			return false;
		}
	}
	public  final Iterator<P> getIterator(RecordSelector sel) throws DataFault, CannotFilterException {
		return this.new FilterIterator(getFilter(sel));
	}
	
	public final  Iterator<P> getIterator(RecordSelector sel,int skip,int count) throws DataFault, CannotFilterException {
		BaseFilter<P> filter = getFilter(sel);
		try{
			return this.new FilterIterator(FilterConverter.convert(filter),skip,count);
		}catch(NoSQLFilterException e){
			return new SkipIterator<P>(new FilterIterator(filter), skip, count);
		}
	}
	public final long getRecordCount(RecordSelector selector)
			throws Exception {
		        return getCount(getFilter(selector));
	}
	public final <PT> Set<PT> getValues(PropertyTag<PT> tag, RecordSelector selector) throws DataException, InvalidExpressionException, CannotFilterException {
		if( ! hasProperty(tag)){
			return new HashSet<PT>();
		}
		BaseFilter<P> filter = getFilter(selector);	
		try{
			PropertyMaker<P,PT> finder = new PropertyMaker<P,PT>(getAccessorMap(),res,tag, true);			
			return finder.find(FilterConverter.convert(filter));
		}catch(CannotUseSQLException e){
			Set<PT> result = new HashSet<PT>();
			for(P o : new FilterSet(filter)){
				result.add(o.getProperty(tag));
			}
			return result;
		}
		
	}

	public DataObjectItemInput<P> getInput(RecordSelector sel) throws Exception {
		
		return new DataObjectInput(sel.visit(new FilterSelectVisitor<P>(this)));
	}
	public void release() {
		if( map != null){
			map.release();
			map=null;
		}
		reg=null;
		super.release();
	}
	
	
	/* (non-Javadoc)
	 * @see uk.ac.ed.epcc.safe.accounting.db.DataObjectPropertyFactory#addSummaryContent(uk.ac.ed.epcc.webapp.content.ContentBuilder)
	 */
	@Override
	public void addSummaryContent(ContentBuilder hb) {
		PropertyInfoGenerator gen = new PropertyInfoGenerator(null, getAccessorMap());
		gen.getTableTransitionSummary(hb);
		if( plugin_owner instanceof SummaryProvider){
			((SummaryProvider) plugin_owner).getTableTransitionSummary(hb, getContext().getService(SessionService.class));
		}
	}


	/* (non-Javadoc)
	 * @see uk.ac.ed.epcc.safe.accounting.db.DataObjectPropertyFactory#getTableTransitions()
	 */
	@Override
	public Map<TableTransitionKey, Transition<? extends DataObjectFactory>> getTableTransitions() {
		Map<TableTransitionKey, Transition<? extends DataObjectFactory>> map = new LinkedHashMap<TableTransitionKey, Transition<? extends DataObjectFactory>>();
		if( getConfigTag().equals(getTag())){
			// Don't allow transitions where the config is taken from
			// a different table
			PlugInOwner owner = plugin_owner;
			if( owner instanceof TableTransitionContributor){
				map.putAll(((TableTransitionContributor)owner).getTableTransitions());
			}
		}
		map.put(new AdminOperationKey("AddClassificationReference","Add a reference to a classification"), new AddClassificationReferenceTransition());

		
		return map;
	}

	/* (non-Javadoc)
	 * @see uk.ac.ed.epcc.safe.accounting.ExpressionTargetGenerator#getExpressionTarget(java.lang.Object)
	 */
	@Override
	public ExpressionTargetContainer getExpressionTarget(P record) {
		return getAccessorMap().getProxy(record);
	}

	@Override
	public boolean isMyTarget(P record) {
		return isMine(record);
	}
	
}