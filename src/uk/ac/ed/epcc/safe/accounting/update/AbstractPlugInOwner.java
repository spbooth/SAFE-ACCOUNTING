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
package uk.ac.ed.epcc.safe.accounting.update;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import uk.ac.ed.epcc.safe.accounting.db.transitions.SummaryProvider;
import uk.ac.ed.epcc.safe.accounting.expr.PropExpressionMap;
import uk.ac.ed.epcc.safe.accounting.properties.MultiFinder;
import uk.ac.ed.epcc.safe.accounting.properties.PropertyFinder;
import uk.ac.ed.epcc.safe.accounting.properties.PropertyTag;
import uk.ac.ed.epcc.webapp.AbstractContexed;
import uk.ac.ed.epcc.webapp.AppContext;
import uk.ac.ed.epcc.webapp.content.ContentBuilder;
import uk.ac.ed.epcc.webapp.forms.transition.Transition;
import uk.ac.ed.epcc.webapp.jdbc.table.TableContentProvider;
import uk.ac.ed.epcc.webapp.jdbc.table.TableTransitionContributor;
import uk.ac.ed.epcc.webapp.jdbc.table.TableTransitionKey;
import uk.ac.ed.epcc.webapp.model.data.ConfigParamProvider;
import uk.ac.ed.epcc.webapp.model.data.DataObjectFactory;
import uk.ac.ed.epcc.webapp.session.SessionService;
/** Base class for implementing {@link PlugInOwner}.
 * 
 * This also implements {@link TransitionSource}
 * 
 * @author spb
 *
 * @param <T> target type for {@link TransitionSource}
 * @param <R> {@link PropertyContainerParser} IR type
 */
public abstract class AbstractPlugInOwner<T extends DataObjectFactory,R> extends AbstractContexed implements PlugInOwner<R>, TableContentProvider, TableTransitionContributor, ConfigParamProvider {
  
	
    private final String tag;
    private final PropertyFinder prev;
	private PropertyContainerParser<R> parser=null;
	private Set<PropertyContainerPolicy> policies=null;
	private PropertyFinder finder=null;
	private PropExpressionMap derived=null;
	
	public AbstractPlugInOwner(AppContext c,PropertyFinder prev, String tag){
		super(c);
		this.prev=prev;
		this.tag=tag;
	}
	public final PropertyContainerParser<R> getParser() {
		if( parser == null){
			parser = makeParser();
		}
		
		return parser;
	}

	protected abstract PropertyContainerParser<R> makeParser();

	public final Set<PropertyContainerPolicy> getPolicies() {
		if( policies == null){
			policies = makePolicies();
		}
		return policies;
	}

	protected abstract Set<PropertyContainerPolicy> makePolicies();
	protected PropertyFinder makeFinder() {
		MultiFinder multi = new MultiFinder();
		multi.addFinder(prev);
		PropertyContainerParser par = getParser();
		if( par != null ){
			multi.addFinder(par.initFinder(getContext(), prev, tag));
		}
		for(PropertyContainerPolicy pol : getPolicies()){
			multi.addFinder(pol.initFinder(getContext(), multi, tag));
		}
		return multi;
	}
	public final PropertyFinder getFinder() {
		if( finder == null ){
			finder = makeFinder();
		}
		return finder;
	}
	public <X> boolean hasProperty(PropertyTag<X> tag){
		return getFinder().hasProperty(tag);
	}
	protected PropExpressionMap makeDerived() {
		PropExpressionMap map = new PropExpressionMap();
		PropertyContainerParser par = getParser();
		if( par != null ){
			map=par.getDerivedProperties(map);
		}
		for(PropertyContainerPolicy pol : getPolicies()){
			map=pol.getDerivedProperties(map);
		}
		return map;
	}
	public final PropExpressionMap getDerivedProperties() {
		if( derived == null){
			derived = makeDerived();
		}
		return derived;
	}
	
	
    public final String getTag(){
    	return tag;
    }
    public void addSummaryContent(ContentBuilder hb) {
    	SessionService operator = getContext().getService(SessionService.class);
		hb.addHeading(3,"Parser");
		PropertyContainerParser parser = getParser();
		if( parser != null ){
			hb.addText(parser.getClass().getSimpleName());
			if( parser instanceof SummaryProvider){
				((SummaryProvider)parser).getTableTransitionSummary(hb, operator);
			}
		}else{
			hb.addText("No parser");
		}
		hb.addHeading(3,"Policies");
		
		for(PropertyContainerPolicy pol : getPolicies()){
			hb.addHeading(4,pol.getClass().getSimpleName());
			if( pol instanceof SummaryProvider){
				((SummaryProvider)pol).getTableTransitionSummary(hb, operator);
			}
		}
	}


public Map<TableTransitionKey, Transition> getTableTransitions() {
	Map<TableTransitionKey, Transition> res = new LinkedHashMap<TableTransitionKey, Transition>();
	PropertyContainerParser parser = getParser();
	if( parser != null && parser instanceof TableTransitionContributor){
		res.putAll(((TableTransitionContributor) parser).getTableTransitions());
	}
	for(PropertyContainerPolicy pol : getPolicies()){
		if( pol instanceof TableTransitionContributor){
			res.putAll(((TableTransitionContributor) pol).getTableTransitions());
		}
	}
	return res;
}

	/* (non-Javadoc)
	 * @see uk.ac.ed.epcc.safe.accounting.db.ConfigParamProvider#addConfigParameters(java.util.Set)
	 */
	@Override
	public void addConfigParameters(Set<String> params) {
		
		PropertyContainerParser parser = getParser();
		if( parser != null && parser instanceof ConfigParamProvider){
			((ConfigParamProvider)parser).addConfigParameters(params);
		}
		for(PropertyContainerPolicy pol : getPolicies()){
			if( pol instanceof ConfigParamProvider){
				((ConfigParamProvider)pol).addConfigParameters(params);
			}
		}
	}
}