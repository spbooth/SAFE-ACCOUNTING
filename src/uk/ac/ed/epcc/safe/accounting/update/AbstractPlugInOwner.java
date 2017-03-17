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

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import uk.ac.ed.epcc.safe.accounting.db.ConfigParamProvider;
import uk.ac.ed.epcc.safe.accounting.db.transitions.SummaryProvider;
import uk.ac.ed.epcc.safe.accounting.expr.PropExpressionMap;
import uk.ac.ed.epcc.safe.accounting.properties.MultiFinder;
import uk.ac.ed.epcc.safe.accounting.properties.PropertyFinder;
import uk.ac.ed.epcc.safe.accounting.properties.PropertyTag;
import uk.ac.ed.epcc.webapp.AppContext;
import uk.ac.ed.epcc.webapp.Contexed;
import uk.ac.ed.epcc.webapp.content.ContentBuilder;
import uk.ac.ed.epcc.webapp.forms.transition.Transition;
import uk.ac.ed.epcc.webapp.jdbc.table.TableTransitionKey;
import uk.ac.ed.epcc.webapp.jdbc.table.TableTransitionTarget;
import uk.ac.ed.epcc.webapp.jdbc.table.TransitionSource;
import uk.ac.ed.epcc.webapp.logging.Logger;
import uk.ac.ed.epcc.webapp.logging.LoggerService;
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
public abstract class AbstractPlugInOwner<T extends PlugInOwner<R> & TableTransitionTarget,R> implements Contexed, PlugInOwner<R>, SummaryProvider, TransitionSource<T>, ConfigParamProvider {
  
	private final AppContext c;
    private final String tag;
    private final PropertyFinder prev;
	private PropertyContainerParser<R> parser=null;
	private Set<PropertyContainerPolicy> policies=null;
	private PropertyFinder finder=null;
	private PropExpressionMap derived=null;
	
	public AbstractPlugInOwner(AppContext c,PropertyFinder prev, String tag){
		this.prev=prev;
		this.c=c;
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
			multi.addFinder(par.initFinder(c, prev, tag));
		}
		for(PropertyContainerPolicy pol : getPolicies()){
			multi.addFinder(pol.initFinder(c, multi, tag));
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
	
	public final AppContext getContext(){
		return c;
	}
    public final String getTag(){
    	return tag;
    }
    public void getTableTransitionSummary(ContentBuilder hb,
			SessionService operator) {
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

@SuppressWarnings("unchecked")
public Map<TableTransitionKey<T>, Transition<T>> getTransitions() {
	Map<TableTransitionKey<T>, Transition<T>> res = new HashMap<TableTransitionKey<T>, Transition<T>>();
	PropertyContainerParser parser = getParser();
	if( parser != null && parser instanceof TransitionSource){
		res.putAll(((TransitionSource) parser).getTransitions());
	}
	for(PropertyContainerPolicy pol : getPolicies()){
		if( pol instanceof TransitionSource){
			res.putAll(((TransitionSource) pol).getTransitions());
		}
	}
	return res;
}

	protected Logger getLogger(){
		return c.getService(LoggerService.class).getLogger(getClass());
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