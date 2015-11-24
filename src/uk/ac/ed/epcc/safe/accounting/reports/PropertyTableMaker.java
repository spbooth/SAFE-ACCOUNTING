// Copyright - The University of Edinburgh 2011
package uk.ac.ed.epcc.safe.accounting.reports;

import uk.ac.ed.epcc.safe.accounting.parsers.value.ValueParser;
import uk.ac.ed.epcc.safe.accounting.parsers.value.ValueParserPolicy;
import uk.ac.ed.epcc.safe.accounting.properties.PropertyContainer;
import uk.ac.ed.epcc.safe.accounting.properties.PropertyFinder;
import uk.ac.ed.epcc.safe.accounting.properties.PropertyTag;
import uk.ac.ed.epcc.webapp.AppContext;
import uk.ac.ed.epcc.webapp.Contexed;
import uk.ac.ed.epcc.webapp.content.Table;

/** Class that generates Table summaries of a PropertyContainer
 * 
 * @author spb
 *
 */
@uk.ac.ed.epcc.webapp.Version("$Id: PropertyTableMaker.java,v 1.6 2014/09/15 14:32:28 spb Exp $")

public class PropertyTableMaker implements Contexed {
	private final AppContext conn;
	private final PropertyFinder finder;
	private ValueParserPolicy policy;
	
	public PropertyTableMaker(AppContext conn,PropertyFinder finder){
		this.conn=conn;
		this.finder=finder;
		policy = new ValueParserPolicy(conn);
	}

	public Table getTable(PropertyContainer pc){
		Table<String,PropertyTag> t = new Table<String,PropertyTag>();
		for(PropertyTag<?> tag : finder.getProperties()){
			if( pc.supports(tag)){
				addProperty(tag, t, pc);
			}
		}
		
	
		return t;
	}
	private <T> void addProperty(PropertyTag<T> tag, Table<String,PropertyTag> t, PropertyContainer pc){
		T dat = pc.getProperty(tag, null);
		if( dat != null ){
			try{
			@SuppressWarnings("unchecked")
			ValueParser<T> vp = (ValueParser<T>)policy.visitPropertyTag(tag);
			t.put("Name", tag, tag.getName());
			t.put("Description",tag,tag.getDescription());
			t.put("Value",tag,vp.format(dat));
			}catch(Throwable te){
				conn.error(te,"Error formatting property");
			}
		}
	}
	public AppContext getContext() {
		return conn;
	}
	
}