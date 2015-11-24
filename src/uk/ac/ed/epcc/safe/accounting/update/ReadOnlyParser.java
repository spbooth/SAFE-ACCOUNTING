// Copyright - The University of Edinburgh 2011
/*******************************************************************************
 * Copyright (c) - The University of Edinburgh 2010
 *******************************************************************************/
package uk.ac.ed.epcc.safe.accounting.update;

import java.util.Date;

import uk.ac.ed.epcc.safe.accounting.expr.PropExpressionMap;
import uk.ac.ed.epcc.safe.accounting.properties.PropertyFinder;
import uk.ac.ed.epcc.safe.accounting.properties.PropertyMap;
import uk.ac.ed.epcc.safe.accounting.properties.StandardProperties;
import uk.ac.ed.epcc.webapp.AppContext;
import uk.ac.ed.epcc.webapp.jdbc.table.DateFieldType;
import uk.ac.ed.epcc.webapp.jdbc.table.TableSpecification;

/** A Placeholder parser for read-only tables 
 * 
 * @author spb
 *
 */
@uk.ac.ed.epcc.webapp.Version("$Id: ReadOnlyParser.java,v 1.5 2014/12/01 16:28:25 spb Exp $")

public class ReadOnlyParser extends BaseParser {

	@Override
	public TableSpecification modifyDefaultTableSpecification(AppContext c,
			TableSpecification spec, PropExpressionMap map, String table_name) {
		spec.setField(StandardProperties.STARTED_TIMESTAMP, new DateFieldType(false, new Date(0L)));
		spec.setField(StandardProperties.COMPLETED_TIMESTAMP, new DateFieldType(false, new Date(Long.MAX_VALUE)));
		return spec;
	}

	public boolean parse(PropertyMap map, String record)
			throws AccountingParseException {
		return false;
	}

	@Override
	public PropertyFinder initFinder(AppContext conn, PropertyFinder prev,
			String table) {
		return StandardProperties.time;
	}

	@Override
	public PropExpressionMap getDerivedProperties(PropExpressionMap previous) {
		return previous;
	}

}