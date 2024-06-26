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
package uk.ac.ed.epcc.safe.accounting.reports;

import java.text.NumberFormat;
import java.util.*;

import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import uk.ac.ed.epcc.safe.accounting.CountReduction;
import uk.ac.ed.epcc.safe.accounting.DateReductionTarget;
import uk.ac.ed.epcc.safe.accounting.IllegalReductionException;
import uk.ac.ed.epcc.safe.accounting.NumberReductionTarget;
import uk.ac.ed.epcc.safe.accounting.OverlapHandler;
import uk.ac.ed.epcc.safe.accounting.ReductionTarget;
import uk.ac.ed.epcc.safe.accounting.UsageProducer;
import uk.ac.ed.epcc.safe.accounting.properties.PropExpression;
import uk.ac.ed.epcc.safe.accounting.properties.PropertyFinder;
import uk.ac.ed.epcc.safe.accounting.properties.PropertyTag;
import uk.ac.ed.epcc.safe.accounting.properties.StandardProperties;
import uk.ac.ed.epcc.safe.accounting.reports.exceptions.ParseException;
import uk.ac.ed.epcc.safe.accounting.reports.exceptions.ReportException;
import uk.ac.ed.epcc.safe.accounting.selector.AndRecordSelector;
import uk.ac.ed.epcc.safe.accounting.selector.RecordSelector;
import uk.ac.ed.epcc.webapp.AppContext;
import uk.ac.ed.epcc.webapp.Feature;
import uk.ac.ed.epcc.webapp.jdbc.expr.Operator;
import uk.ac.ed.epcc.webapp.jdbc.expr.Reduction;
import uk.ac.ed.epcc.webapp.model.data.Duration;
import uk.ac.ed.epcc.webapp.time.Period;

/**
 * Extension object for creating an manipulating single values
 * 
 * Multiple queries can be combined together in a single pass 
 * but they all must use the same filter/period environment.
 * @author nix
 * 
 */


public class AtomExtension extends ReportExtension {
    public static final String ATOM_LOC="http://safe.epcc.ed.ac.uk/atom";
	public static final String PERIOD_DURATION_ELEMENT = "PeriodDuration";
	private static final String ATOM_PLUGIN_ELEMENT = "AtomPlugin";
	private static final String OVERLAP_AVERAGE_ELEMENT = "OverlapAverage";
	private static final String OVERLAP_SUM_ELEMENT = "OverlapSum";
	private static final String DIV_ELEMENT = "Div";
	private static final String MUL_ELEMENT = "Mul";
	private static final String SUB_ELEMENT = "Sub";
	private static final String ADD_ELEMENT = "Add";
	private static final String COUNT_ELEMENT = "Count";
	private static final String MAXIMUM_ELEMENT = "Maximum";
	private static final String MINIMUM_ELEMENT = "Minimum";
	private static final String AVERAGE_ELEMENT = "Average";
	private static final String MEDIAN_ELEMENT = "Median";
	private static final String SUM_ELEMENT = "Sum";
	private static final String DISTINCT_ELEMENT = "Distinct";
	public static final Feature CACHE_ATOM_RESULTS = new Feature("cache.atom_results",true,"Cache atom results to optimise repeated queries in the same report");

	private Map<CacheKey,AtomResult> result_cache=new HashMap<>();
	/** simple composite of a value and the corresponding expression (if known)
	 * this allows the expression to be passed up to the formatting code.
	 * 
	 */
	public static class AtomResult<N>{
		@Override
		public String toString() {
			return "AtomResult [expr=" + expr + ", value=" + value + "]";
		}
		public AtomResult(PropExpression<N> expr, N value) {
			super();
			this.expr = expr;
			this.value = value;
		}
		public final PropExpression<N> expr;
		public final N value;
	}
	static class CacheKey{
		/**
		 * @param set
		 * @param period
		 * @param target
		 */
		public CacheKey(ObjectSet set, Period period, ReductionTarget target) {
			super();
			this.set = set;
			this.period = period;
			this.target = target;
		}
		public final ObjectSet set;
		public final Period period;
		public final ReductionTarget target;
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result
					+ ((period == null) ? 0 : period.hashCode());
			result = prime * result + ((set == null) ? 0 : set.hashCode());
			result = prime * result
					+ ((target == null) ? 0 : target.hashCode());
			return result;
		}
		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			CacheKey other = (CacheKey) obj;
			if (period == null) {
				if (other.period != null)
					return false;
			} else if (!period.equals(other.period))
				return false;
			if (set == null) {
				if (other.set != null)
					return false;
			} else if (!set.equals(other.set))
				return false;
			if (target == null) {
				if (other.target != null)
					return false;
			} else if (!target.equals(other.target))
				return false;
			return true;
		}
	}
	public AtomExtension(AppContext conn,ReportType type) throws ParserConfigurationException {
		super(conn,type);
	}
	
	public AtomResult expandNumberGroup(Period period,RecordSet set, Element element) throws IllegalReductionException, Exception{
		if( set.hasError()) {
			throw new ReportException("Bad record set");
		}
		if( element == null ){
			throw new ParseException("Expecting number group");
		}
		
		UsageProducer<?> producer = set.getUsageProducer();
		RecordSelector selector = set.getPeriodSelector(period);
	
		String name = element.getLocalName();
		if( name.equals("Atom") || name.equals("AtomValue")){
			return expandAtom(period,set,element);
			
		}else if( name.equals("Value") || name.equals("Number")){
			Number n =  getNumberParam(null, null, element);
			if( n == null ){
				addError("Bad Atom","Cannot parse Value", element);
				throw new ParseException("Invalid Number Element");
			}
			log.debug("Parsed number atom value="+n);
			return new AtomResult<>(null,n);
		}else if (SUM_ELEMENT.equals(name)) {
			return expandSimpleReduction(element, set, period, Reduction.SUM);
		}else if (DISTINCT_ELEMENT.equals(name)) {
			return expandSimpleReduction(element, set, period, Reduction.DISTINCT);	
		} else if (AVERAGE_ELEMENT.equals(name)) {
			return expandSimpleReduction(element, set, period, Reduction.AVG);
		} else if (MEDIAN_ELEMENT.equals(name)) {
			return expandSimpleReduction(element, set, period, Reduction.MEDIAN);	
		} else if (MINIMUM_ELEMENT.equals(name)) {
			return expandSimpleReduction(element, set, period, Reduction.MIN);
		} else if (MAXIMUM_ELEMENT.equals(name)) {
			return expandSimpleReduction(element, set, period, Reduction.MAX);
		} else if (COUNT_ELEMENT.equals(name)) {
			  return new AtomResult<>(StandardProperties.COUNT_PROP,expandCount(set,period, element));
		} else if (ADD_ELEMENT.equals(name)){
			return combineAll(Operator.ADD, period, set, element);
		}else if (SUB_ELEMENT.equals(name)){
			return combine(Operator.SUB, period, set, element);
		}else if (MUL_ELEMENT.equals(name)){
			return combineAll(Operator.MUL, period, set, element);
		}else if (DIV_ELEMENT.equals(name)){
			return combine(Operator.DIV, period, set, element);
		}else if (ATOM_PLUGIN_ELEMENT.equals(name)) {
			return plugin(period,set,element);
		}else if( PERIOD_DURATION_ELEMENT.equals(name)) {
			return new AtomResult<Duration>(null, new Duration(period.getStart(),period.getEnd()));
		}
		
		
		throw new ParseException("unexpected content");
	}
	private AtomResult plugin(Period period, RecordSet set, Element element) throws Exception {
		String name = element.getAttribute("name");
		if( name == null || name.isEmpty()) {
			throw new ReportException("No name for AtomPlugin");
		}
		AtomPlugin p = getContext().makeObject(AtomPlugin.class, name);
		if( p == null ) {
			throw new ReportException("No AtomPlugin resulved name="+name);
		}
		return p.evaluate(this,element,period, set);
	}

	public AtomResult expandAtom(Period period,RecordSet set,Node element) throws IllegalReductionException, Exception{
		if( set.hasError()) {
			throw new ReportException("Bad record set");
		}
		// have to look for additional filters etc.
		set = new RecordSet(set); // copy
		// add filters.
		NodeList list = element.getChildNodes();
		for(int i=0 ; i < list.getLength(); i++){
			Node n = list.item(i);
			short nodeType = n.getNodeType();
			String localName = n.getLocalName();
			String namespaceURI = n.getNamespaceURI();
			if( nodeType==Node.ELEMENT_NODE && localName.equals(FILTER_ELEMENT) && namespaceURI.equals(FILTER_LOC)){
				set = addFilterElement(set, (Element) n);
			}else if( nodeType==Node.ELEMENT_NODE && localName.equals(PERIOD_ELEMENT)&& namespaceURI.equals(PERIOD_NS)){
				period = makePeriod(n);
			} else {
				String parentNamespace = element.getNamespaceURI();
				if( nodeType==Node.ELEMENT_NODE && namespaceURI.equals(parentNamespace)){
					return expandNumberGroup(period, set,(Element) n);
				}
			}
		}
		throw new ParseException("No nested atom element");
	}
	private long expandCount(RecordSet set,Period period, Node element) throws Exception {
		// count is allowed to contain filter elements
		
		// have to look for additional filters etc.
		set = new RecordSet(set); // copy
		// add filters.
		NodeList list = element.getChildNodes();
		for(int i=0 ; i < list.getLength(); i++){
			Node n = list.item(i);
			short nodeType = n.getNodeType();
			String localName = n.getLocalName();
			String namespaceURI = n.getNamespaceURI();
			if( nodeType==Node.ELEMENT_NODE && localName.equals(FILTER_ELEMENT) && namespaceURI.equals(FILTER_LOC)){
				set = addFilterElement(set, (Element) n);
			}else if( nodeType==Node.ELEMENT_NODE){
				throw new ParseException("Unexpected content in Count "+localName);
			}
		}
		UsageProducer<?> producer = set.getUsageProducer();
		if( producer == null) {
			return 0L;
		}
		long res = producer.getRecordCount(set.getPeriodSelector(period));
		log.debug("count returns "+res);
		return res;
		
	}
	public AtomResult combine(Operator op,Period period,RecordSet set, Node element) throws IllegalReductionException, Exception{
		Element[] arg = getBinaryArgs(element);
		Number a = (Number)expandNumberGroup(period, set, arg[0]).value;
		Number b = (Number)expandNumberGroup(period, set, arg[1]).value;
		Number res = op.operate(a, b);
		log.debug("combine: "+a+op.text()+b+"->"+res);
		return new AtomResult<>(null,res);
	}
	public AtomResult combineAll(Operator op,Period period,RecordSet set, Node element) throws IllegalReductionException, Exception{
		LinkedList<Element> arg = getUnboundedArgs(element);
		try {
			Number res = (Number)expandNumberGroup(period, set, arg.pop()).value;
			log.debug("First object "+res);
			for(Element e : arg){
				Number b = (Number)expandNumberGroup(period, set, e).value;
				Number prev = res;
				res = op.operate(res, b);
				log.debug("combine: "+prev+op.text()+b+"->"+res);
			}
			return new AtomResult<>(null,res);
		}catch(NoSuchElementException nse) {
			throw new ReportException("atom:CombineAll no child nodes", nse);
		}
	}
	protected Element[] getBinaryArgs(Node element) throws ReportException {
		int pos=0;
		Element arg[] = new Element[2];
		NodeList list = element.getChildNodes();
		for( int i=0;i<list.getLength();i++){
			Node n = list.item(i);
			if( n.getNodeType()==Node.ELEMENT_NODE && n.getNamespaceURI()==element.getNamespaceURI()){
				if( pos >= 2 ){
					throw new ReportException( "atom:Combine Expecting two child nodes");	
				}
				arg[pos]=(Element)n;
				pos++;
			}
		}
		if( pos != 2 ){
			throw new ReportException( "atom:Combine Expecting two child nodes");	
		}
		return arg;
	}
	protected LinkedList<Element> getUnboundedArgs(Node element) throws ReportException {
		LinkedList<Element> args = new LinkedList<>();
		NodeList list = element.getChildNodes();
		for( int i=0;i<list.getLength();i++){
			Node n = list.item(i);
			if( n.getNodeType()==Node.ELEMENT_NODE && n.getNamespaceURI()==element.getNamespaceURI()){
				args.add((Element)n);
			}
		}
		return args;
	}
	public String percent(Period period, RecordSet set, Node e) {
		NumberFormat pf = NumberFormat.getPercentInstance();
		if( e instanceof Element){
			try{
				Element elem = (Element)e;
				String min = getAttribute("min_fraction", elem);
				if( min != null && min.trim().length() > 0){
					pf.setMinimumFractionDigits(Integer.parseInt(min));
				}
				String max = getAttribute("max_fraction", elem);
				if( max != null && max.trim().length() > 0){
					pf.setMaximumFractionDigits(Integer.parseInt(max));
				}
			}catch(Exception t){
				addError("bad format","Invalid percentage format", t);
			}
		}
		try {
			Element[] arg = getBinaryArgs(e);
			Number a = (Number)expandNumberGroup(period, set, arg[0]).value;
			Number b = (Number)expandNumberGroup(period, set, arg[1]).value;
			// always do divide as double for percentage
			double frac = 0.0;
			if( b.doubleValue() > 0.0 ) {
				frac = a.doubleValue()/b.doubleValue();
			}
			return pf.format(frac);
		} catch (Exception e1) {
			addError("bad percentage", "Error calculating percentage",e,e1);
			return "";
		}
	}

	
	public String define(String name,Period period,RecordSet set,Node def) {
		// def is the Define element
		 NodeList list = def.getChildNodes();
		 String parent_namespace = def.getNamespaceURI();
		 for(int i=0; i<list.getLength();i++){
			  Node n = list.item(i);
			  String namespace = n.getNamespaceURI();
			  if( n.getNodeType() == Node.ELEMENT_NODE && (parent_namespace == null  || namespace == null || parent_namespace.equals(namespace))){
				  try{
					  Number num = (Number) expandNumberGroup(period, set, (Element)n).value;
					  if( parameter_names != null){
						  if( parameter_names.contains(name)){
							  addError("Bad Defined Atom Name", "The name "+name+" is already in use");
							  return "";
						  }
						  parameter_names.add(name);
					  }
					  params.put(name, num);
					  return "";
				  }catch(Exception t){
					  addError("BadDefine", "Error calculating value",def,t);
				  }
			  }
		 }
		 addError("BadDefine", "No content found in Define element");
		 return "";
	}
	public String formatPropertyList(Period period, RecordSet set, Node node){
		try{
			
			Element element =(Element) node;
			UsageProducer<?> producer = set.getUsageProducer();
			if( producer == null) {
				return "";
			}
			RecordSelector selector = set.getPeriodSelector(period);
			
			String name = element.getLocalName();
			
			
				PropertyTag data_tag = getTag(producer.getFinder(),element);
				if( data_tag == null ){
					addError("Bad property", "No property found",element);
					return "";
				}	
				String values = "";			
				Set properties = producer.getValues(data_tag, selector);
				Iterator iterator = properties.iterator();
				while (iterator.hasNext()) {
					values += iterator.next().toString();
					if (iterator.hasNext()) {
						values += ", ";					
					}
					
				}
				return values;	
				
			

		} catch (Exception tr) {
			addError("Error retrieving atomic value", tr.getClass().getCanonicalName(),tr);	
		}

		return "";
	}
	
	
	/** evaluate an Atom and format for display
	 * 
	 * @param period
	 * @param set
	 * @param node
	 * @return String
	 */
	public String formatAtom(Period period, RecordSet set, Node node){
		try{
			Element element =(Element) node;
			AtomResult ar = expandNumberGroup(period, set, element);
			String result = display(getNumberFormat(node),ar.expr, ar.value);
			return result;
			
		} catch (Exception tr) {
			addError("Error retrieving atomic value", tr.getClass().getCanonicalName(),tr);	
		}

		return "";
		
	}
	/** Evaluate an atom and return the unformatted raw representation
	 * 
	 * @param period
	 * @param set
	 * @param node
	 * @return
	 */
    public String rawAtom(Period period, RecordSet set, Node node){
		try{
			Element element =(Element) node;
			AtomResult ar = expandNumberGroup(period, set, element);
			String result = ar.value.toString();
			return result;
			
		} catch (Exception tr) {
			addError("Error retrieving atomic value", tr.getClass().getCanonicalName(),tr);	
		}

		return "";
		
	}
	
	
	
	/** Evaluate a simple reduction.
	 * 
	 * @param element
	 * @param set
	 * @param period
	 * @param reduction
	 * @return
	 * @throws ReportException
	 * @throws Exception
	 * @throws IllegalReductionException
	 */
	private AtomResult expandSimpleReduction(Element element, 
			RecordSet set, Period period,Reduction reduction)
			throws ReportException, Exception, IllegalReductionException {
		
		AndRecordSelector selector = set.getPeriodSelector(period);
		PropertyFinder finder = set.getFinder();
		String exp = getText(element);
		if(exp != null && exp.trim().length() > 0){
			PropExpression expression = getExpression(finder,exp);
			if( expression == null ){
				throw new ParseException("No property found for "+exp);
			}
			ReductionTarget red;
			if( Number.class.isAssignableFrom(expression.getTarget())){
				red = NumberReductionTarget.getInstance(reduction,expression);
			}else if( Date.class.isAssignableFrom(expression.getTarget())){
				red=new DateReductionTarget(reduction, expression);
			}else if( reduction == Reduction.DISTINCT) {
				red = new CountReduction(expression);
			}else{
				throw new IllegalReductionException("Unsupported reduction type "+exp);
			}
			UsageProducer<?> producer = set.getUsageProducer();
			if( producer == null) {
				return new AtomResult(expression,red.getDefault());
			}
			AtomResult result=null;
			CacheKey key = new CacheKey(set, period, red);
			result = result_cache.get(key);
			if(result == null || ! CACHE_ATOM_RESULTS.isEnabled(getContext())){
				if( set.useOverlap() && period.getStart().before(period.getEnd())){
					OverlapHandler handler = new OverlapHandler(getContext(), producer);
					PropExpression<Date> bounds[] = set.getBounds();
					result =  new AtomResult(expression,handler.getOverlapSum((NumberReductionTarget) red, 
							bounds[0], bounds[1], selector, period.getStart(), period.getEnd()));
				}else{
					result = new AtomResult(expression, producer.getReduction(red, selector));
				}
				result_cache.put(key, result);
			}
			return result;
		}
		throw new ParseException("No reduction expression specified");
	}

	@Override
	public boolean wantReplace(Element e) {
		return ATOM_LOC.equals(e.getNamespaceURI());
	}

	@Override
	public Node replace(Element e) {
		String name = e.getLocalName();
		switch(name) {
		case "Define": 
			try {
			define(e.getAttribute("name"), 
				makePeriod(ElementSet.ancestors_self(e).select(new Match(PERIOD_NS,PERIOD_ELEMENT))
						.merge(
								ElementSet.select(e,new Match(null,"*")).select(new Match(PERIOD_NS,PERIOD_ELEMENT))
								).pollLast()),
				addFilterElementSet(makeSelector(), ElementSet.ancestors_self(e).select(new Match(FILTER_LOC,FILTER_ELEMENT)).merge(ElementSet.select(e,new Match(null,"*")).select(new Match(FILTER_LOC,FILTER_ELEMENT))))
				,  e);
			}catch(Exception e1) {
				addError("Error in atom:define", "Unexpected exception", e, e1);
			}
			return null;
		case "Percentage":
			try {
			return addText(
					percent(makePeriod(ElementSet.ancestors_self(e).select(new Match(PERIOD_NS,PERIOD_ELEMENT)).pollLast()),
							addFilterElementSet(makeSelector(), ElementSet.ancestors_self(e).select(new Match(FILTER_LOC,FILTER_ELEMENT))),
							e)
					);
			}catch(Exception e1) {
				addError("Error in atom:Percentage", "Unexpected exception", e, e1);
				return null;
			}
		case "Sum":
		case "Distinct":
		case "Average":
		case "Median":
		case "Minimum":
		case "Maximum":
		case "Count":
		case "Number":
		case "Value":
		case "Add":
		case "Sub":
		case "Mul":
		case "Div":
		case "Atom":
		case "AtomPlugin":
			try {
				return addText(
						formatAtom(makePeriod(ElementSet.ancestors_self(e).select(new Match(PERIOD_NS,PERIOD_ELEMENT)).pollLast()),
								addFilterElementSet(makeSelector(), ElementSet.ancestors_self(e).select(new Match(FILTER_LOC,FILTER_ELEMENT))),
								e)
						);
				}catch(Exception e1) {
					addError("Error in atom:"+name, "Unexpected exception", e, e1);
					return null;
				}
		case "AtomValue":
			try {
				return addText(
						rawAtom(makePeriod(ElementSet.ancestors_self(e).select(new Match(PERIOD_NS,PERIOD_ELEMENT)).pollLast()),
								addFilterElementSet(makeSelector(), ElementSet.ancestors_self(e).select(new Match(FILTER_LOC,FILTER_ELEMENT))),
								e)
						);
				}catch(Exception e1) {
					addError("Error in atom:"+name, "Unexpected exception", e, e1);
					return null;
				}
		case "Property":
			try {
				return addText(
						formatPropertyList(makePeriod(ElementSet.ancestors_self(e).select(new Match(PERIOD_NS,PERIOD_ELEMENT)).pollLast()),
								addFilterElementSet(makeSelector(), ElementSet.ancestors_self(e).select(new Match(FILTER_LOC,FILTER_ELEMENT))),
								e)
						);
				}catch(Exception e1) {
					addError("Error in atom:"+name, "Unexpected exception", e, e1);
					return null;
				}
		case "IfRecords":
			try {
				if( hasRecords(makePeriod(ElementSet.ancestors(e).select(new Match(PERIOD_NS,PERIOD_ELEMENT)).pollLast()),
									addFilterElementSet(makeSelector(), ElementSet.ancestors(e).select(new Match(FILTER_LOC,FILTER_ELEMENT))))) {
					return transformElementSet(ElementSet.select(e,new Match(null,"*")));
				}else {
					return null;
				}
			} catch (Exception e1) {
				addError("Error in atom:"+name, "Unexpected exception", e, e1);
				return null;
			}
		}
		
		
		return super.replace(e);
	}	

}