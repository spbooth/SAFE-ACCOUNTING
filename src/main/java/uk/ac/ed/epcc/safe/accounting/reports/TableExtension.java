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

import java.text.DateFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;
import java.util.TreeMap;

import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import uk.ac.ed.epcc.safe.accounting.ColName;
import uk.ac.ed.epcc.safe.accounting.CountReduction;
import uk.ac.ed.epcc.safe.accounting.DateReductionTarget;
import uk.ac.ed.epcc.safe.accounting.ExpressionTargetFactory;
import uk.ac.ed.epcc.safe.accounting.ExpressionTargetGenerator;
import uk.ac.ed.epcc.safe.accounting.ExpressionTargetTableMaker;
import uk.ac.ed.epcc.safe.accounting.IndexReduction;
import uk.ac.ed.epcc.safe.accounting.JobTableMaker;
import uk.ac.ed.epcc.safe.accounting.NumberAverageReductionTarget;
import uk.ac.ed.epcc.safe.accounting.NumberMaxReductionTarget;
import uk.ac.ed.epcc.safe.accounting.NumberMedianReductionTarget;
import uk.ac.ed.epcc.safe.accounting.NumberMinReductionTarget;
import uk.ac.ed.epcc.safe.accounting.NumberSumReductionTarget;
import uk.ac.ed.epcc.safe.accounting.OverlapHandler;
import uk.ac.ed.epcc.safe.accounting.ReductionMapResult;
import uk.ac.ed.epcc.safe.accounting.ReductionTarget;
import uk.ac.ed.epcc.safe.accounting.SelectReduction;
import uk.ac.ed.epcc.safe.accounting.UsageProducer;
import uk.ac.ed.epcc.safe.accounting.db.ReductionHandler;
import uk.ac.ed.epcc.safe.accounting.expr.ExpressionTarget;
import uk.ac.ed.epcc.safe.accounting.expr.ExpressionTuple;
import uk.ac.ed.epcc.safe.accounting.expr.LabelPropExpression;
import uk.ac.ed.epcc.safe.accounting.parsers.value.ValueParser;
import uk.ac.ed.epcc.safe.accounting.parsers.value.ValueParserTransform;
import uk.ac.ed.epcc.safe.accounting.properties.InvalidPropertyException;
import uk.ac.ed.epcc.safe.accounting.properties.PropExpression;
import uk.ac.ed.epcc.safe.accounting.properties.PropertyFinder;
import uk.ac.ed.epcc.safe.accounting.properties.PropertyTag;
import uk.ac.ed.epcc.safe.accounting.reports.exceptions.ExpressionException;
import uk.ac.ed.epcc.safe.accounting.reports.exceptions.ReportException;
import uk.ac.ed.epcc.safe.accounting.selector.AndRecordSelector;
import uk.ac.ed.epcc.safe.accounting.selector.PeriodOverlapRecordSelector;
import uk.ac.ed.epcc.safe.accounting.selector.RecordSelector;
import uk.ac.ed.epcc.safe.accounting.selector.SelectClause;
import uk.ac.ed.epcc.webapp.AppContext;
import uk.ac.ed.epcc.webapp.content.BlankTransform;
import uk.ac.ed.epcc.webapp.content.FormatDateTransform;
import uk.ac.ed.epcc.webapp.content.FormatProvider;
import uk.ac.ed.epcc.webapp.content.InvalidArgument;
import uk.ac.ed.epcc.webapp.content.Labeller;
import uk.ac.ed.epcc.webapp.content.LabellerTransform;
import uk.ac.ed.epcc.webapp.content.NumberFormatTransform;
import uk.ac.ed.epcc.webapp.content.Operator;
import uk.ac.ed.epcc.webapp.content.Table;
import uk.ac.ed.epcc.webapp.content.Transform;
import uk.ac.ed.epcc.webapp.jdbc.exception.DataException;
import uk.ac.ed.epcc.webapp.jdbc.expr.Reduction;
import uk.ac.ed.epcc.webapp.jdbc.filter.MatchCondition;
import uk.ac.ed.epcc.webapp.logging.Logger;
import uk.ac.ed.epcc.webapp.logging.LoggerService;
import uk.ac.ed.epcc.webapp.time.Period;

/**
 * Extension object for creating an manipulating Tables
 * 
 * @author spb
 * 
 */


public class TableExtension extends ReportExtension {
	//private static final String PROPERTY_ELEMENT = "Property";
	public static final String CURRENT_PERIOD_ATTR = "current.period";
	private static final String TABLE_LOC = "http://safe.epcc.ed.ac.uk/table";

	public final class TotalTransform implements Transform {
		public Object convert(Object old) {
			
			try {
				return displayByClass(old.getClass(), old)+" Total";
			} catch (Exception e) {
				return old.toString()+" Total";
			}
		}
	}
	public static class BadTableException extends ReportException{

		public BadTableException() {
			super();
			
		}

		public BadTableException(String message, Throwable cause) {
			super(message, cause);
			
		}

		public BadTableException(String message) {
			super(message);
			
		}

		public BadTableException(Throwable cause) {
			super(cause);
			
		}
		
	}
	/** Interface for objects that represent an intermediate table
	 * 
	 * @author spb
	 *
	 */
	public static interface TableProxy{
		/** Apply processing instructions.
		 * If the object has a parent Compoundtable
		 * the results are added to the parent and null is returned.
		 * otherwide the table is returned.
		 * 
		 * This is the last method called on the lifecycle of the object so we should try to drop
		 * data references before returning from this routine.
		 * 
		 * @param instructions
		 * @return null or Table
		 * @throws BadTableException 
		 */
		public Table postProcess(Node instructions) throws BadTableException;
	}
	/** A {@link TableProxy} for a combining multiple generated tables into one composite table.
	 * 
	 * This is needed if you are going to build a table using multiple queries.
	 * 
	 * @author spb
	 *
	 */
	public static class CompoundTable implements TableProxy {
		CompoundTable parent;
		TableExtension extension;
		Table<String,Object> table;
		public CompoundTable(TableExtension extension){
			this(extension,null);
		}
		public CompoundTable(TableExtension extension,CompoundTable parent) {
			this.extension = extension;
			this.parent=parent;
			// Not actually expecting anything to process in the Table node...
			table = new Table<>();
			
		}
		
		public void addTable(Table<String,Object> table) {
			mergeKeys(table);
			this.table.add(table);			
		}
		/**
		 * @param table
		 */
		protected void mergeKeys(Table<String, Object> table) {
			String currentKeyName = this.table.getKeyName();
			
			if (currentKeyName == null) {
				this.table.setKeyName(table.getKeyName());
				
			} else if (!currentKeyName.equals(table.getKeyName())) {					
				this.table.setKeyName(
						this.table.getKeyName()+" or "+table.getKeyName());
				
			}
		}
		
		public Table postProcess(Node instructions) {
			if( parent == null ){
				Table result = extension.processTable(table, instructions);
				table=null;
				return result;
			}else{
				parent.addTable(extension.processTable(table, instructions));
				parent=null;
				table=null;
				return null;
			}
		}
		
		@Override
		public String toString() {
			String string = "CompoundTable[";
			Iterator<String> columns = table.getColumNames().iterator();
			while (columns.hasNext()) {
				String columnName = columns.next();
				string += columnName;
				if (columns.hasNext()) {
					string += ",";
				}
			}
			string += "]";
			return string;
		}
		
	}
	/** A {@link TableProxy} base class
	 * 
	 * @author spb
	 *
	 */
    public static abstract class AbstractTable implements TableProxy{
		
		
		CompoundTable compoundTable;
		TableExtension extension;
		
		
		
		public AbstractTable(TableExtension extension, CompoundTable compoundTable)  {
			this.compoundTable = compoundTable;
			this.extension = extension;
			
			
		}
		
	
		
		/**
		 * @param columnNode
		 * @param property
		 * @return
		 */
		protected PropExpression addLabeller(Element columnNode, PropExpression property) {
			// Optionally use a labeller
			String labeller = extension.getAttribute("labeller", columnNode);
			if( labeller != null && labeller.length() > 0){
				Labeller lab = extension.getContext().makeObjectWithDefault(Labeller.class, null, labeller);
				if( lab != null){
					property = new LabelPropExpression(lab, property);
				}else{
					extension.addError("bad labeller", "Labeller "+labeller+" did not resolve", columnNode);
				}
			}
			return property;
		}
		
		
		
	}
	/** A {@link TableProxy} that generates a table using a {@link JobTableMaker}.
	 * 
	 * Each row of the table comes from a separate job record. 
	 * @author spb
	 *
	 */
	public static class SimpleTable extends ObjectTable{
		
		
		private static final String SKIP_ROWS="SkipRows";
		
		private static final String DEFAULT_LIST = "DefaultPropertyList";
	
		Period period;
		
		
		public SimpleTable(TableExtension extension, CompoundTable compoundTable, 
				Period period, RecordSet recordSet, Node tableNode) throws BadTableException {
			super(extension,compoundTable,recordSet,tableNode,new JobTableMaker(extension.getContext(), recordSet.getUsageProducer()));
			this.period = period;
			
			UsageProducer up = recordSet.getUsageProducer();
			PropertyFinder finder = recordSet.getFinder();
			

			try{
				Element tableElement = (Element)tableNode;
				// use hasChild as element is empty
				if( extension.hasChild(DEFAULT_LIST, tableElement)){
					String prefix="jobtable";
					Element list =  extension.getParamElementNS(tableElement.getNamespaceURI(),DEFAULT_LIST,tableElement);
					String type = extension.getParam("Type", list);
					if( type != null && ! type.isEmpty()) {
						prefix=prefix+"."+type;
					}
					AppContext conn = extension.getContext();
					String config_name = prefix+".properties";
					String prop_list = conn.getExpandedProperty(config_name,"");
					if( up != null) {
						for(String tag : prop_list.split(",")){
							tag = tag.trim();
							if( ! tag.isEmpty()) {
								String prop_name = conn.getExpandedProperty(prefix+".property."+tag, tag);
								PropertyTag t = finder.find(prop_name);
								if( t != null ){
									// can use name or tag for label
									String label = conn.getExpandedProperty("propertylabel."+tag,conn.getExpandedProperty("propertylabel."+t.getName()));
									Transform tr = conn.makeObjectWithDefault(Transform.class, null, "propertytransform", t.getName());
									if( tr == null){
										Labeller l = conn.makeObjectWithDefault(Labeller.class, null, "propertylabeller", t.getName());
										if( l != null ){
											tr = new LabellerTransform(conn, l);
										}
									}
									tableMaker.addColumn(new ColName(t, label,null,tr));
								}else {
									extension.addError("bad default property", prop_name);
								}
							}
						}
					}
				}
			}catch(Exception e){
				extension.addError("Bad Table", "Error setting "+DEFAULT_LIST, e);
			}
			try{
				// See if SkipRows is set
				Element tableElement = (Element)tableNode;
				if (extension.hasParam(SKIP_ROWS, tableElement)) {
					tableMaker.setSkipDataPoints(
							Integer.parseInt(extension.getParam(SKIP_ROWS, tableElement).trim()));
				}
			}catch(Exception e){
				throw new BadTableException("Error setting SkipRows", e);
			}
			
		}
	
		
	
		
		protected Table makeRawTable() throws BadTableException { 
			
			AppContext conn = extension.getContext();
			//store period to custom formatters can retreive
			conn.setAttribute(CURRENT_PERIOD_ATTR, period);
			try {
				RecordSet recordSet = (RecordSet) set;
				AndRecordSelector selector = recordSet.getPeriodSelector(period);
				UsageProducer<?> up = recordSet.getUsageProducer();
				if( up == null ) {
					// assume narrowed composite producer
					return new Table<>();
				}else {
					return tableMaker.makeTable(selector);
				}
			} catch (Exception e) {
				throw new BadTableException("Error making JobTable", e);
			}		
			
		
		}		
		
		@Override
		public String toString() {
			String string = "SimpleTable[]";
			return string;
		}
		
	}
	/** A {@link TableProxy} that is populated by a reduction query.
	 * 
	 * @author spb
	 *
	 */
	public static class SummaryTable extends SummaryObjectTable{

	

		Period period;
		PropExpression<Date>[] dates;
		
		
		
		public SummaryTable(TableExtension extension, 
				CompoundTable compoundTable, Period period, 
				RecordSet recordSet, Node tableNode) throws ReportException {
			super(extension,compoundTable,recordSet,tableNode);
			this.period = period;
			Element tableElement = (Element)tableNode;
			
			// Get the dates, if there are any, default to the dates set in recordSet
			this.dates = recordSet.getBounds();
			this.use_overlap = recordSet.useOverlap() && period.getStart().before(period.getEnd());
			Element paramElement = extension.getParamElement("Date", tableElement);
			if( paramElement != null){
				DateBounds dateProperties = extension.getDateProperties(recordSet,paramElement);
				this.dates = 
						dateProperties.bounds;
				this.use_overlap=dateProperties.overlap;
			}
			
		}

		

		

			
		public Table postProcess(Node instructions) {	
			UsageProducer<?> producer = (UsageProducer<?>) recordSet.getGenerator();
			Table<String,Object> table = new Table<>();
			if(producer == null) {
				// Assume this is a narrowed compostie producer
				return table;
			}
			final AppContext conn = extension.getContext();
			@SuppressWarnings("unchecked")
			OverlapHandler<?> handler = new OverlapHandler(conn, producer);
			// Note create the time selector explicitly based on the time bounds.
			// we can set the time bounds using the generic filter element but
			// we can't apply a filter based on them as the table specific elment
			// may override this and not be a subset of the default time bounds
			RecordSelector selector = recordSet.getRecordSelector();
			
			
			if( ! producer.compatible(selector)){
				extension.addError("Selector not compatible with producer", "Producer: "+producer.getTag()+" not compatible with "+ selector.toString());
				return table;
			}
			
			try{
				Map<ExpressionTuple,ReductionMapResult> data;
                
				if( dates.length == 1){
					AndRecordSelector sel = new AndRecordSelector(selector);
					sel.add(new SelectClause<>(dates[0], MatchCondition.GT,
							period.getStart()));
					sel.add(new SelectClause<>(dates[0], MatchCondition.LE,
							period.getEnd()));
					data = producer.getIndexedReductionMap(reductions, sel);
				}else if( dates.length ==2){
					if( use_overlap ){
						data = handler.getOverlapIndexedReductionMap( reductions, 
							dates[0], dates[1], 
							period.getStart(), period.getEnd(), 
							selector);
					}else{
						// explicitly asked for no overlap calc
						AndRecordSelector sel = new AndRecordSelector(selector);
						sel.add(new PeriodOverlapRecordSelector(period, dates[0],dates[1]));
						data = producer.getIndexedReductionMap(reductions, sel);
					}
//					if( period.getStart().equals(period.getEnd())){
//						// zero length period we must be trying to pick up records
//						// that overlap a specific point in time.
//					}
				}else{
					data = producer.getIndexedReductionMap(reductions, selector);
				}
				populateTable(conn, table, data);
				
			} catch (DataException e) {
				//e.printStackTrace();
				getLogger(conn).error("Error making table",e);
				extension.addError("Data Error", "Error making summary table", e);
			} catch (InvalidPropertyException e) {
				getLogger(conn).error("Unsupported property in table",e);
				extension.addError("Property Error", "Unsupported property in summary table", e);
			}catch(Exception t){
				getLogger(conn).error("Error making table",t);
				extension.addError("Internal error", "Error making table",t);
			}
			//store period to custom formatters can retreive
			conn.setAttribute(CURRENT_PERIOD_ATTR, period);
			table = extension.processTable(table, instructions);
			conn.removeAttribute(CURRENT_PERIOD_ATTR);
			
			period=null; // for GC
			dates=null;
			table =addToCompound(table);
			
			return table;

		}



	}
	/** A {@link TableProxy} that is populated by a reduction query.
	 * 
	 * @author spb
	 *
	 */
	public static class SummaryObjectTable extends AbstractTable{
		boolean use_overlap=false;
	
		
		ObjectSet recordSet;
		List<String> col_names;
		Map<String,ReductionTarget> cols;
		Map<IndexReduction,ReductionTarget> expr_cols;
		Map<IndexReduction,String> expr_groups;
		Map<String,ReductionTarget> dynamic_cols;
		TreeMap<Object,String> dynamic_sort_data;
		Set<ReductionTarget> reductions;
		Set<IndexReduction> indexes;
		
		public SummaryObjectTable(TableExtension extension, 
				CompoundTable compoundTable,  
				ObjectSet recordSet, Node tableNode) throws BadTableException {
			super(extension,compoundTable);
			this.recordSet = recordSet;

			if( recordSet == null) {
				throw new BadTableException("No ObjectSet defined");
			}
			if( recordSet.hasError()) {
				throw new BadTableException("ObjectSet has error");
			}
//			if( ! recordSet.hasGenerator()) {
//				throw new BadTableException("No generator in ObjectSet");
//			}
			col_names = new LinkedList<>();
			
			dynamic_cols = new LinkedHashMap<>();
			
			// sort by natural order if Comparable otherwise by string order
			dynamic_sort_data = new TreeMap<>(new Comparator() {

				@Override
				public int compare(Object o1, Object o2) {
					if( o1 instanceof Comparable && o2 instanceof Comparable) {
						return ((Comparable)o1).compareTo(o2);
					}
					return o1.toString().compareTo(o2.toString());
				}
			});
			
			cols = new HashMap<>();
			
			expr_cols = new HashMap<>();
			
			expr_groups = new HashMap<>();
			
			reductions = new LinkedHashSet<>();
			
			indexes = new LinkedHashSet<>();
			
		}
		protected final Logger getLogger(AppContext conn){
			return conn.getService(LoggerService.class).getLogger(getClass());
		}
		

		

		/**
		 * Add a single column of data to a SummaryTable In principle we can have
		 * different RecordSet objects for different columns so each column has to
		 * be added separately.
		 * @param columnNode 
		 * @return String
		 */
		@SuppressWarnings("unchecked")
		public final String addColumn(Node columnNode) {
			try {
				if( recordSet == null) {
					extension.addError("bad recordset", "record set is null", columnNode);
					return "";
				}
				ExpressionTargetGenerator producer = recordSet.getGenerator();
				if( producer == null) {
					extension.addError("bad recordset", "producer is null", columnNode);
					return "";
				}
				if( TABLE_LOC.equals(columnNode.getNamespaceURI())){
					String columnType = columnNode.getLocalName();	
					PropExpression property = extension.getPropertyExpression(columnNode, producer.getFinder());
					if (property == null) {
						throw new BadTableException("Missing or illegal Property element");		
					}

					String name;
					if( property instanceof PropertyTag){
						name=((PropertyTag)property).getName();
					}else{
						name=property.toString();
					}
					// use Plot property name as the default
					String col_name = extension.getParamWithDefault("Name", name, (Element)columnNode);
					// unless we have a NameExpression element
					IndexReduction name_red = null;
					if( extension.hasChild("NameExpression", (Element)columnNode)) {
						Element ne = extension.getParamElement("NameExpression", (Element) columnNode);
						PropExpression name_prop = extension.getPropertyExpression(ne,producer.getFinder());
						if( name_prop == null ) {
							throw new BadTableException("Illegal NameExpression element");
						}
						name_prop= addLabeller(ne, name_prop);
						name_red = new IndexReduction(name_prop);
						String group = extension.getAttribute("name", ne);
						if( group != null) {
							expr_groups.put(name_red, group);
						}
						col_name = null; 
					}
					
					
					boolean isColumn = columnType.equals("Column");
					boolean isIndex = columnType.equals("Index");
				
					ReductionTarget red=null;
					if(isColumn || isIndex ){
						property = addLabeller((Element)columnNode, property);
						// printing index
						
						if( isColumn){
							red = new SelectReduction(property);
						}else{
							red = new IndexReduction(property);
							indexes.add((IndexReduction) red);
						}
						
					}else if(columnType.equals("SumColumn")){
						red = new NumberSumReductionTarget(property);
					}else if(columnType.equals("AverageColumn")){
						red = new NumberAverageReductionTarget( property);
					}else if(columnType.equals("MedianColumn")){
						red = new NumberMedianReductionTarget( property);
					}else if(columnType.equals("MinColumn")){
						if( Number.class.isAssignableFrom(property.getTarget()) ){
							red = new NumberMinReductionTarget(property);
						}else if( Date.class.isAssignableFrom(property.getTarget())){
							red = new DateReductionTarget(Reduction.MIN, property);
						}else{
							throw new BadTableException("Illegal min property "+property.toString());
						}
					}else if(columnType.equals("MaxColumn")){
						if( Number.class.isAssignableFrom(property.getTarget()) ){
							red = new NumberMaxReductionTarget( property);
						}else if( Date.class.isAssignableFrom(property.getTarget())){
							red = new DateReductionTarget(Reduction.MAX, property);
						}else{
							throw new BadTableException("Illegal max property "+property.toString());
						}
					}else if(columnType.equals("CountDistinctColumn")) {
						red = new CountReduction(property);
					}else{
						extension.addError("Bad column", "Unexpected Column type "+columnType);
					}
					if( red != null ) {
						reductions.add(red);
						if( col_name != null ) {
							col_names.add(col_name);
							cols.put(col_name,red);
						}
						if( name_red != null) {
							reductions.add(name_red);
							expr_cols.put(name_red, red);
						}
					}
				}else{
					extension.addError("Bad Namespace", "Unexpected namespace "+columnNode.getNamespaceURI());
				}
			} catch (Exception tr) {
				extension.addError("Error adding Data Column to table", 
						tr.getClass().getCanonicalName(), tr);

			}
			return "";

		}
		
		
		public Table postProcess(Node instructions) {	
			ExpressionTargetFactory<?> ef = (ExpressionTargetFactory<?>) recordSet.getGenerator();
			ReductionHandler<?,?> red_hand = new ReductionHandler(ef,false);
			final AppContext conn = extension.getContext();
			
			RecordSelector selector = recordSet.getRecordSelector();
			Table<String,Object> table = new Table<>();
			if( ! ef.compatible(selector)){
				extension.addError("Selector not compatible with ExpressionTargetFactory", selector.toString());
				return table;
			}
		    if( indexes.size() == 0 ){
		    	extension.addError("No indexes in compound table", Integer.toString(indexes.size()));
				return table;
		    }
			try{
				Map<ExpressionTuple,ReductionMapResult> data;
                
				
				data = red_hand.getIndexedReductionMap(reductions, selector);
				
				populateTable(conn, table, data);
			} catch (DataException e) {
				//e.printStackTrace();
				getLogger(conn).error("Error making table",e);
				extension.addError("Data Error", "Error making summary table", e);
			} catch (InvalidPropertyException e) {
				getLogger(conn).error("Unsupported property in table",e);
				extension.addError("Property Error", "Unsupported property in summary table", e);
			}catch(Exception t){
				getLogger(conn).error("Error making table",t);
				extension.addError("Internal error", "Error making table",t);
			}
			table = extension.processTable(table, instructions);
			conn.removeAttribute(CURRENT_PERIOD_ATTR);
			table = addToCompound(table);	
			return table;

		}
		/**
		 * @param table
		 * @return
		 */
		protected Table<String, Object> addToCompound(Table<String, Object> table) {
			if (compoundTable != null) {
				// we must only merge columns using the appropriate Operator
				compoundTable.mergeKeys(table);
				compoundTable.table.addRows(table);
				// process all columns remaining in the table
				for(String col : table.getCols()) {
					if( cols.containsKey(col)) {
						// This is a reduction col
						Reduction red = cols.get(col).getReduction();
						if( use_overlap && red == Reduction.AVG){
							// These have been mapped to time average 
							red = Reduction.SUM;
						}
						compoundTable.table.getCol(col).combine(red.operator(), table.getCol(col));
					}else if( dynamic_cols.containsKey(col)) {
						// A dynamic col from a name expression
						Reduction red = dynamic_cols.get(col).getReduction();
						if( use_overlap && red == Reduction.AVG){
							// These have been mapped to time average 
							red = Reduction.SUM;
						}
						compoundTable.table.getCol(col).combine(red.operator(), table.getCol(col));
					}else {
						// generated by a processing instruction
						compoundTable.table.getCol(col).combine(Operator.MERGE, table.getCol(col));
					}
				}
				
				
				//compoundTable.addTable(table);
				table=null;
				
			}
			return table;
		}
		/**
		 * @param conn
		 * @param table
		 * @param data
		 */
		protected void populateTable(final AppContext conn, Table<String, Object> table,
				Map<ExpressionTuple, ReductionMapResult> data) {
			// copy data into table keyed by tuple.
			if(data != null ){
				for(ExpressionTuple tup : data.keySet()){
					Object key;
					if( expr_cols.isEmpty()) {
						key=tup.getKey();
					}else {
						// need to exclude name expressions from table key unless they are also
						// explicitly indexes.
						Map<PropExpression,Object> map = tup.getMap();
						for(IndexReduction n : expr_cols.keySet()) {
							if( ! indexes.contains(n)) {
								map.remove(n.getExpression());
							}
						}
						ExpressionTuple t = new ExpressionTuple(map);
						key = t.getKey();
					}
					
					
					Map<ReductionTarget,Object> row = data.get(tup);

					for(String col : col_names){
						ReductionTarget o = cols.get(col);
						Object value = row.get(o);
						// Its OK for value to be null here and we want to pass this on to the table
						// to ensure columns are created in the correct order
						table.put(col, key, value);
					}
					// now expression name columns
					// merge in case different index values have the same string rep
					// also allows us to extend later to add a labeller
					for(Map.Entry<IndexReduction,ReductionTarget> e : expr_cols.entrySet()) {
						IndexReduction sel = e.getKey();
						PropExpression exp = sel.getExpression();
						Object raw = row.get(sel);
						if( exp instanceof FormatProvider) {
							Labeller l = ((FormatProvider)exp).getLabeller();
							raw = l.getLabel(conn, raw);
						}
						if( raw != null) {
							String col= raw.toString();
							dynamic_sort_data.put( raw,col); // This is so we can sort dynamic cols by raw object

							ReductionTarget target = e.getValue();
							Object value = row.get(target);
							Object merge = table.get(col, key);
							dynamic_cols.put(col,target);
							table.put(col, key, target.combine(value, merge));
							String group = expr_groups.get(sel);
							if( group != null) {
								try {
									table.addToGroup(group, col);
								} catch (InvalidArgument e1) {
									getLogger(conn).error("Error adding to column group", e1);
								}
							}
						}
					}
				}
			}
			// set col formats
			for(String col : col_names){
				ReductionTarget o = cols.get(col);
				setColFormat(conn, table, col, o);
			}
			for(Map.Entry<String,ReductionTarget> e : dynamic_cols.entrySet()) {
				String col = e.getKey();
				ReductionTarget target = e.getValue();
				setColFormat(conn, table, col, target);
				
			}
			// Dynamic data always goes at the end sorted
			// by the raw data
			for(String col : dynamic_sort_data.values()) {
				table.setColLast(col);
			}
		}
		/**
		 * @param conn
		 * @param table
		 * @param col
		 * @param t
		 */
		protected void setColFormat(final AppContext conn, Table<String, Object> table, String col, ReductionTarget target) {
			if( target != null && table.hasCol(col) ){
				PropExpression t = target.getExpression();
				if( Number.class.isAssignableFrom(target.getTarget())){
					table.setColFormat(col, new Transform() {
						
						public Object convert(Object old) {
							if( old == null ){
								return Double.valueOf(0.0);
							}
							return old;
						}
					});
				}else if( t instanceof FormatProvider ){
					table.setColFormat(col, new LabellerTransform(conn,((FormatProvider)t).getLabeller()));
				}
			}
		}



	}
	/** A {@link TableProxy} that generates a table using a {@link ObjectSet}.
	 * 
	 * Each row of the table comes from a separate {@link ExpressionTarget}
	 * 
	 * @author spb
	 *
	 */
	public static class ObjectTable extends AbstractTable{
		
		private static final String MAX_ROWS_ELEMENT = "MaxRows";
		private static final String WARNING_ELEMENT = "Warning";
		
		ObjectSet set;
		ExpressionTargetTableMaker tableMaker;
		public ObjectTable(TableExtension extension, CompoundTable compoundTable, 
				ObjectSet set,Node tableNode) {
			this(extension,compoundTable,set,tableNode,new ExpressionTargetTableMaker(extension.getContext(),set.getGenerator()));
		}
		public ObjectTable(TableExtension extension, CompoundTable compoundTable, 
				ObjectSet set,Node tableNode,ExpressionTargetTableMaker tableMaker) {
			super(extension,compoundTable);
			this.set=set;
			this.tableMaker = tableMaker;

			try{
				// See if MaxRows is set
				Element tableElement = (Element)tableNode;
				if (extension.hasParam(MAX_ROWS_ELEMENT, tableElement)) {
					tableMaker.setMaxDataPoints(
							Integer.parseInt(extension.getParam(MAX_ROWS_ELEMENT, tableElement)));
				}
			}catch(Exception e){
				extension.addError("Bad Table", "Error setting MaxRows", e);
			}
			try{
				// See if Warning
				Element tableElement = (Element)tableNode;
				if (extension.hasParam(WARNING_ELEMENT, tableElement)) {
					tableMaker.setWarningExpression(extension.getExpression(set.getGenerator().getFinder(), extension.getParam(WARNING_ELEMENT, tableElement)));
				}
			}catch(Exception e){
				extension.addError("Bad Table", "Error setting Warning", e);
			}
		}
	
		/**
		 * Configure the TableMaker by adding column
		 * 
		 * @param columnNode
		 * @return String
		 */
		public String addColumn(Node columnNode) {
			Element data_element = (Element) columnNode;

			try {
				if( set == null) {
					extension.addError("bad recordset", "record set is null", columnNode);
					return "";
				}
				ExpressionTargetGenerator producer = set.getGenerator();
				if( producer == null) {
					extension.addError("bad recordset", "producer is null", columnNode);
					return "";
				}
				if( TABLE_LOC.equals(columnNode.getNamespaceURI())){
					PropExpression property = extension.getPropertyExpression(columnNode, producer.getFinder());
					if (property == null) {
						throw new BadTableException("Missing or illegal Property element");		
					}

					String name;
					if( property instanceof PropertyTag){
						name=((PropertyTag)property).getName();
					}else{
						name=property.toString();
					}
					// use  property name as the default
					String col_name = extension.getParamWithDefault("Name", name, (Element)columnNode);
					PropExpression name_prop=null;
					// unless we have a NameExpression element
					if( extension.hasChild("NameExpression", (Element)columnNode)) {
						Element ne = extension.getParamElement("NameExpression", (Element) columnNode);
						name_prop = extension.getPropertyExpression(ne,producer.getFinder());
						if( name_prop == null ) {
							throw new BadTableException("Illegal NameExpression element");
						}
						name_prop= addLabeller(ne, name_prop);

						String group = extension.getAttribute("name", ne);
						if( group != null) {
							col_name=group;
						}
					}

					tableMaker.addColumn(new ColName(property,col_name,name_prop));
				}else{
					extension.addError("Bad Namespace", "Unexpected namespace "+columnNode.getNamespaceURI());
				}
			} catch (Exception tr) {
				extension.addError("Error adding Data Column to table", 
						tr.getClass().getCanonicalName(), tr);

			}
			return "";

		}
	
		protected Table makeRawTable() throws BadTableException {
			try {
				// We ignore the period for a ObjectTable
				return tableMaker.makeTable(set.getRecordSelector());
			} catch (Exception e) {
				throw new BadTableException("Error making ObjectTable", e);
			}	
		}
		
		public Table postProcess(Node instructions) throws BadTableException {
			Table<String,Object> table = makeRawTable();
				
			AppContext conn = extension.getContext();
			if (compoundTable == null) {
				Table result = extension.processTable(table, instructions);
				table=null;
				return result;
			} else {
				compoundTable.addTable(
						extension.processTable(table, instructions));
				table=null;
				compoundTable=null;
				return null;
				
			}			
		
		
		}		
		
		@Override
		public String toString() {
			String string = "SimpleTable[]";
			return string;
		}
		
	}
	public TableExtension(AppContext conn,NumberFormat nf) throws ParserConfigurationException {
		super(conn,nf);
	}
	
	public CompoundTable newCompoundTable() {
		return new CompoundTable(this);
	}
	public CompoundTable newCompoundTable(CompoundTable parent){
		return new CompoundTable(this, parent);
	}
	
	public SimpleTable newSimpleTable(Period period, RecordSet recordSet, 
			Node node) throws BadTableException {
		return new SimpleTable(this, null, period, recordSet, node);
	}

	public SimpleTable newSimpleTable(CompoundTable compountTable, 
			Period period, RecordSet recordSet, Node node) throws BadTableException {
		return new SimpleTable(this, compountTable, period, recordSet, node);
	}

	public SummaryTable newSummaryTable(Period period, RecordSet recordSet, 
			Node node) throws ReportException {
		
			return new SummaryTable(this, null, period,recordSet,node);	
	}

	public SummaryTable newSummaryTable(CompoundTable compountTable, 
			Period period, RecordSet recordSet, Node node) throws ReportException {
		return new SummaryTable(this, compountTable, period,recordSet,node);
	}
	public SummaryObjectTable newSummaryObjectTable(CompoundTable compoundTable, ObjectSet recordSet, Node tableNode) throws BadTableException{
		return new SummaryObjectTable(this, compoundTable, recordSet, tableNode);
	}
	public SummaryObjectTable newSummaryObjectTable(ObjectSet recordSet, Node tableNode) throws BadTableException{
		return new SummaryObjectTable(this, null, recordSet, tableNode);
	}
	public ObjectTable newObjectTable(ObjectSet recordSet, 
			Node node) {
		return new ObjectTable(this, null, recordSet,node);
	}

	public ObjectTable newObjectTable(CompoundTable compountTable, 
			ObjectSet recordSet, Node node) {
		return new ObjectTable(this, compountTable, recordSet,node);
	}
	public void addColumn(SimpleTable table,  Node node) {
		table.addColumn(node);
	}

	public void addColumn(SummaryObjectTable table,  Node node) {
		table.addColumn(node);
	}
	public void addColumn(SummaryTable table,  Node node) {
		table.addColumn(node);
	}
	public void addColumn(ObjectTable table,  Node node) {
		table.addColumn(node);
	}
	
	public DocumentFragment postProcess(TableProxy proxy,  Node instructions) throws BadTableException {
		try {
			startTimer("postProcess "+proxy.getClass().getSimpleName());
			Table table = proxy.postProcess(instructions);		

			if (table != null) {
				return format(table,instructions);

			} else {
				return getDocument().createDocumentFragment();

			}
		}finally {
			stopTimer("postProcess "+proxy.getClass().getSimpleName());
		}
		
	}

	

	@SuppressWarnings("unchecked")
	public DocumentFragment postProcess(Table table,  Node instructions) {			
		if (table != null) {
			return format(processTable(table, instructions),instructions);
			
		} else {
			return getDocument().createDocumentFragment();
			
		}
		
	}
	
	private PropExpression getPropertyExpression(Node node, PropertyFinder finder) throws ExpressionException {
		return getPropertyExpression(node, finder, PROPERTY_ELEMENT);
	}
	private Table<String,Object> processTable(Table<String,Object> table, Node instructions) {
		if (table == null) {
			table = new Table<>();
		}
		NodeList list = instructions.getChildNodes();
		for (int i = 0; i < list.getLength(); i++) {
			Node n = list.item(i);
			if (n.getNodeType() == Node.ELEMENT_NODE) {
				Element e = (Element) n;
				table=processTable(table, e);
			}
		}
		return table;

	}

	/**
	 * Process.
	 * 
	 * @param target
	 *            the target
	 * @param inst
	 *            the inst
	 */
	private Table processTable(Table<String,Object> target, Element inst) {
		String instruction = inst.getLocalName();
		try {

			if (instruction.equals("KeyName")) {
				String indexName = getParam("Name", inst);
				target.setKeyName(indexName);

			} else 
				if (instruction.equals("PercentColumn")) {
				String name = getParam("Name", inst);
				String column = getParam("Column", inst);
				Number totalOver = getNumberParam("TotalOver",null, inst);

				NumberFormat pf = NumberFormat.getPercentInstance();
				// same default as Table class
				pf.setMaximumFractionDigits(1);
				int max_frac = getIntParam("MaximumFractionDigits", -1,inst);
				if( max_frac > -1 ){
					pf.setMaximumFractionDigits(max_frac);
				}
				int min_frac = getIntParam("MinimumFractionDigits", -1,inst);
				if( min_frac > -1 ){
					pf.setMinimumFractionDigits(min_frac);
				}
				if (totalOver == null) {
					target.addPercentCol(column, name,pf);
				} else {
					target.addPercentCol(column, name, totalOver.doubleValue(),pf);
				}

			} else if (instruction.equals("TotalColumn")) {
				String name = getParam("Name", inst);
				String column = getParam("Column", inst);
				target.addTotalToCol(column, name);
			} else if (instruction.equals("CategoryTotals")) {
				String cat_col = getParam("Category", inst);
				String column = getParam("Column", inst);
				String lable_col = getParam("Label", inst);
				Boolean highlight = null;
				String hl = getParam("Highlight", inst);
				if( hl != null ){
					highlight = Boolean.valueOf(hl);
				}
				Transform cat_to_key=null;
				String key_transform = getParam("KeyTransform", inst);
				if( key_transform != null ){
					cat_to_key = getContext().makeObjectWithDefault(Transform.class, null, key_transform);
				}
				if( cat_to_key == null ){
					// inner class can't create in makeObjectWithDefault
					cat_to_key= new TotalTransform();
				}
				target.addCategoryTotals(column, cat_col, cat_to_key, lable_col, highlight);
			} else if (instruction.equals("CategorySet")) {
				String cat_col = getParam("Category", inst);
				String column = getParam("Column", inst);
				
				
				Transform cat_to_key=null;
				String key_transform = getParam("KeyTransform", inst);
				if( key_transform != null ){
					cat_to_key = getContext().makeObjectWithDefault(Transform.class, null, key_transform);
				}
				if( cat_to_key == null ){
					// inner class can't create in makeObjectWithDefault
					cat_to_key= new TotalTransform();
				}
				Transform cat_to_val=null;
				String val_transform = getParam("ValueTransform", inst);
				if( val_transform != null ){
					cat_to_val = getContext().makeObjectWithDefault(Transform.class, null, val_transform);
				}
				if( cat_to_val == null ){
				cat_to_val= new BlankTransform();
				}
				target.setCategoryRow(column, cat_col, cat_to_key, cat_to_val);	
			} else if (instruction.equals("SetColumnAt")) {
				String col = getParam("Column", inst);
				int pos = Integer.parseInt(getParam("At", inst));
				target.setCol(col, pos);

			} else if (instruction.equals("SetColumnAfter")) {
				String col = getParam("Column", inst);
				String after = getParam("After", inst);
				target.setColAfter(after, col);

			} else if (instruction.equals("SetColumnLast")) {
				String col = getParam("Column", inst);
				target.setColLast(col);
			} else if (instruction.equals("SetColumnName")) {
				String col = getParam("Column", inst);
				String name = getParam("Name", inst);
				target.setColName(col, name);
			} else if (instruction.equals("SetRowAt")) {
				String row = getParam("Row", inst);
				int pos = Integer.parseInt(getParam("At", inst));
				target.setRow(row, pos);

			} else if (instruction.equals("SetRowAfter")) {
				String row = getParam("Row", inst);
				String after = getParam("After", inst);
				target.setRowAfter(row, after);
				
			} else if (instruction.equals("SetRowLast")) {
				String row = getParam("Row", inst);
				target.setRowLast(row);

			} else if (instruction.equals("Set")){
			    String row = getParam("Row", inst);
			    String col = getParam("Column", inst);
			    String val = getParam("Value", inst);
			    target.put(col,row,val);
			}else if ( instruction.equals("SortColumns")) {
				target.sortCols(null);
			}else if ( instruction.equals("SortRows")) {
				target.sortRows();
			}else if (instruction.equals("SortBy")) {
			

				String columns = getParam("Columns", inst);				
				String columnsList[] = columns.split("\\s*,\\s*");				

				String comparators = getParam("Comparators",inst);
				
				Comparator c[]=null;
				if( comparators != null ){
					String list[] = comparators.split("\\s*,\\s*");
					c= new Comparator[list.length];
					for(int i=0 ; i < list.length; i++){
						c[i] = getContext().makeObjectWithDefault(Comparator.class, null,"Comparator", list[i]);
					}
				}
				String reverse = getParam("Reverse", inst);
				boolean dir=false;
				if (reverse != null && reverse.equalsIgnoreCase("true")) {
					dir=true;
				}
				
				Table.Sorter s;
				if( c == null ){
					s = new Table.Sorter(columnsList, target, dir);
				}else{
					s = new Table.Sorter(columnsList, c, target, dir);
				}
				target.sortRows(s);
				

			}else if(instruction.equals("Remove")){
				String col = getParam("Column",inst);
				if( col != null ){
					target.removeCol(col);
				}
				String row = getParam("Row",inst);
				if( row != null ){
					target.removeRow(row);
				}
			}else if( instruction.equals("GlobalFormat")) {
				String type = normalise(getText(inst));
				Transform transform = makeTableTransform(inst, type);
				if( transform != null ){
					target.setFormat( transform);
				}
			}else if(instruction.equals("Format")){
				String col = getParam("Column",inst);
				String type = getParam("Type",inst);
				Transform transform = makeTableTransform(inst, type);
				if( transform != null ){
					target.setColFormat(col, transform);
				}
			}else if(instruction.equals("Transform")){
				String col = getParam("Column",inst);
				String type = getParam("Type",inst);
				Transform transform = makeTableTransform(inst, type);
				if( transform != null ){
					target.transformCol(col, transform);
				}
			}else if(instruction.equals("TransformKey")){
				String col = getParam("Column",inst);
				String type = getParam("Type",inst);
				Transform transform = makeTableTransform(inst, type);
				if( transform != null ){
					target.transformKeys(col, transform);
				}
			}else if(instruction.equals("NumberFormat")){
				String col = getParam("Column",inst);
				NumberFormat tf = (NumberFormat) nf.clone();
				int max_int = getIntParam("MaximumIntegerDigits", -1,inst);
				if( max_int > -1 ){
					tf.setMaximumIntegerDigits(max_int);
				}
				int min_int = getIntParam("MinimumIntegerDigits", -1,inst);
				if( min_int > -1 ){
					tf.setMinimumIntegerDigits(min_int);
				}
				int max_frac = getIntParam("MaximumFractionDigits", -1,inst);
				if( max_frac > -1 ){
					tf.setMaximumFractionDigits(max_frac);
				}
				int min_frac = getIntParam("MinimumFractionDigits", -1,inst);
				if( min_frac > -1 ){
					tf.setMinimumFractionDigits(min_frac);
				}
				target.setColFormat(col, new NumberFormatTransform(tf));
			}else if(instruction.equals("DateFormat")){
				String col = getParam("Column",inst);
				DateFormat df =new SimpleDateFormat(getParamWithDefault("Format","yyyy-MM-dd hh:mm:ss",inst));
				String zone = getParam("Timezone", inst);
				if( zone != null ){
					df.setTimeZone(TimeZone.getTimeZone(zone.trim()));
				}
				target.setColFormat(col, new FormatDateTransform(df));
			}else if(instruction.equals("PercentFormat")){
				String col = getParam("Column",inst);
				NumberFormat pf = NumberFormat.getPercentInstance();
				int max_frac = getIntParam("MaximumFractionDigits", -1,inst);
				if( max_frac > -1 ){
					pf.setMaximumFractionDigits(max_frac);
				}
				int min_frac = getIntParam("MinimumFractionDigits", -1,inst);
				if( min_frac > -1 ){
					pf.setMinimumFractionDigits(min_frac);
				}
				target.setColFormat(col, new NumberFormatTransform(pf));
			}else if(instruction.equals("HighlightRow")){
			
				String row = normalise(getText(inst));
				target.setHighlight(row, true);
			}else if(instruction.equals("CombineColumn")){
				String dest=getParam("Dest", inst);
				Operator op = Operator.valueOf(getParam("Operator", inst));
				String a = getParam("Arg1",inst);
				String b = getParam("Arg2",inst);
				target.colOperation(dest, op, a, b);
			}else if(instruction.equals("CombineRow")){
				// Note row key might not be strings.
				String dest=getParam("Dest", inst);
				Operator op = Operator.valueOf(getParam("Operator", inst));
				String a = getParam("Arg1",inst);
				String b = getParam("Arg2",inst);
				target.rowOperation(dest, op, a, b);
			}else if( instruction.equals("MergeRows")) {
				String key = getParam("NewKey", inst);
				Table<String,Object> old = target;
				target = new Table<>();
				target.addTable(key, old);
			}else if( instruction.equals("ThresholdRows")) {
				String col = getParam("Column", inst);
				Number min = getNumberParam("Minimum",null, inst);
				Number max = getNumberParam("Maximum",null, inst);
				if(col != null) {
					if( min != null  ) {
						try {
							target.thresholdRows(col, MatchCondition.LT, min);
						}catch(Exception t) {
							addError("Bad threshold", "Minimum", t);
						}
					}
					if( max != null ) {
						try {
							target.thresholdRows(col, MatchCondition.GT, max);
						}catch(Exception t) {
							addError("Bad threshold", "Maximum", t);
						}
					}
				}
			}else if(instruction.equals("PrintHeadings")){
				target.setPrintHeadings(getBooleanParam("Value", true, inst));
			}
		} catch (Exception t) {
			getLogger().error( "Error processing table " + instruction,t);

		}
		return target;
	}

	/**
	 * @param inst
	 * @param type
	 * @return
	 */
	protected Transform makeTableTransform(Element inst, String type) {
	    if( type.equals("TotalTransform")){
	    	// inner class so implement explicitly
	    	// we need to support this to have a CompountTable that
	    	// augments the total rows
	    	return new TotalTransform();
	    }
		Transform transform;
		transform= getContext().makeObjectWithDefault(Transform.class, null,type);
		if( transform == null ){
			Labeller lab = getContext().makeObjectWithDefault(Labeller.class, null , type);
			if( lab != null ){
				transform=new LabellerTransform(getContext(), lab);
			}else{
				ValueParser parser = parse_vis.getValueParser(Object.class, type);
				if( parser != null ){
					transform = new ValueParserTransform(parser);
				}else{
					addError("Bad Format", "Type "+type+" does not map to a supported format", inst);
				}
			}
		}
		return transform;
	}

	/**
	 * Return the Table object as a DocumentFragment
	 * 
	 * @param table
	 *            Table to format
	 * @param n
	 * 	          Node being formatted.
	 * @return DocumentFragment
	 */
	private DocumentFragment format(Table table,Node n) {
		String type=null;
		if(n != null &&  n instanceof Element){
			try {
				type=getParam("TableFormat", (Element) n);
			} catch (Exception e) {
				addError("Bad Table", "Error setting TableFormat", e);
			}
		}
		
		DocumentFragment frag = format(table, type,getReportPrefix(n));
		return frag;
	}

}