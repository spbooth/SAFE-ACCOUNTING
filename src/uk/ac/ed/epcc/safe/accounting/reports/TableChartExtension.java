// Copyright - The University of Edinburgh 2011
/*******************************************************************************
 * Copyright (c) - The University of Edinburgh 2010
 *******************************************************************************/
package uk.ac.ed.epcc.safe.accounting.reports;

import java.text.NumberFormat;

import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.DocumentFragment;

import uk.ac.ed.epcc.webapp.AppContext;
/** Generate the table form of a plot for non graphical output
 * 
 * @author spb
 *
 */
@uk.ac.ed.epcc.webapp.Version("$Id: TableChartExtension.java,v 1.17 2014/11/24 09:25:01 spb Exp $")

public class TableChartExtension extends ChartExtension {

	public TableChartExtension(AppContext c,NumberFormat nf) throws ParserConfigurationException {
		super(c,nf);
	}

	
	@Override
	public DocumentFragment addChart(Chart chart,String caption) throws Exception {
		return addChartTable(chart, caption);
	}


	@Override
	public boolean graphOutput() {
		return false;
	}
}