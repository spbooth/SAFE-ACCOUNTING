// Copyright - The University of Edinburgh 2011
/*******************************************************************************
 * Copyright (c) - The University of Edinburgh 2010
 *******************************************************************************/
package uk.ac.ed.epcc.safe.accounting.parsers;

import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import uk.ac.ed.epcc.safe.accounting.properties.PropertyFinder;
import uk.ac.ed.epcc.safe.accounting.properties.PropertyMap;
import uk.ac.ed.epcc.safe.accounting.properties.PropertyRegistry;
import uk.ac.ed.epcc.safe.accounting.properties.PropertyTag;
import uk.ac.ed.epcc.safe.accounting.update.AbstractPropertyContainerParser;
import uk.ac.ed.epcc.safe.accounting.update.AccountingParseException;
import uk.ac.ed.epcc.webapp.AppContext;
@uk.ac.ed.epcc.webapp.Version("$Id: GridMapParser.java,v 1.9 2014/09/15 14:32:24 spb Exp $")

/** Parser for a Globus Grid-map file.
 * 
 * 
 * @author spb
 *
 */
public class GridMapParser extends AbstractPropertyContainerParser {

	public static final PropertyRegistry gridmap_reg = new PropertyRegistry("gridmap", "Properties from the gridmap file");
	public static final PropertyTag<String> GRIDMAP_USER = new PropertyTag<String>(gridmap_reg, "UserName",String.class);
	public static final PropertyTag<String> GRIDMAP_DN = new PropertyTag<String>(gridmap_reg, "Dn",String.class);
	private static final Pattern parse_pattern = Pattern.compile("\"(.+)\"\\s+(\\w+)");
	public boolean parse(PropertyMap map, String record)
			throws AccountingParseException {
		Matcher m = parse_pattern.matcher(record);
		if( m.matches()){
			String dn = m.group(1);
			String name=m.group(2);
			map.setProperty(GRIDMAP_DN, dn);
			map.setProperty(GRIDMAP_USER, name);
			return true;
		}
		return false;
	}

	public PropertyFinder initFinder(AppContext ctx, PropertyFinder prev,
			String table) {
		return gridmap_reg;
	}

	@Override
	public Iterator<String> splitRecords(String update)
			throws AccountingParseException {
		return new UnixFileSplitter(update);
	}

}