// Copyright - The University of Edinburgh 2011
package uk.ac.ed.epcc.safe.accounting.update;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import uk.ac.ed.epcc.safe.accounting.expr.PropExpressionMap;
import uk.ac.ed.epcc.safe.accounting.properties.PropertyContainer;
import uk.ac.ed.epcc.safe.accounting.properties.PropertyFinder;
import uk.ac.ed.epcc.safe.accounting.properties.PropertyMap;
import uk.ac.ed.epcc.safe.accounting.properties.PropertyTag;
import uk.ac.ed.epcc.webapp.AppContext;
import uk.ac.ed.epcc.webapp.jdbc.table.TableSpecification;
@uk.ac.ed.epcc.webapp.Version("$Id: NullParser.java,v 1.5 2014/12/01 16:28:25 spb Exp $")

/** A placeholder do-nothing parser.
 * 
 * @author spb
 *
 */
public class NullParser implements PropertyContainerParser {

	
	public PropertyFinder initFinder(AppContext ctx, PropertyFinder prev,
			String table) {
		return prev;
	}


	public PropExpressionMap getDerivedProperties(PropExpressionMap previous) {
		return previous;
	}


	public void startParse(PropertyContainer static_props) throws Exception {
		
	}


	public String endParse() {
		return null;
	}

	public TableSpecification modifyDefaultTableSpecification(AppContext conn,
			TableSpecification t, PropExpressionMap map,
			String table_name) {
		return t;
	}

	
	public boolean parse(PropertyMap map, String record)
			throws AccountingParseException {
		return false;
	}


	public Iterator<String> splitRecords(String update)
			throws AccountingParseException {
		return null;
	}


	public Set<PropertyTag> getDefaultUniqueProperties() {
		return new HashSet<PropertyTag>();
	}

}