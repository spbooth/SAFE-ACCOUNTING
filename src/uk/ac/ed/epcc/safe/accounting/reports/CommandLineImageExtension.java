// Copyright - The University of Edinburgh 2011
/*******************************************************************************
 * Copyright (c) - The University of Edinburgh 2010
 *******************************************************************************/
package uk.ac.ed.epcc.safe.accounting.reports;

import java.io.File;
import java.io.IOException;
import java.text.NumberFormat;

import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Element;

import uk.ac.ed.epcc.webapp.AppContext;
import uk.ac.ed.epcc.webapp.charts.ChartData;
import uk.ac.ed.epcc.webapp.logging.LoggerService;
@uk.ac.ed.epcc.webapp.Version("$Id: CommandLineImageExtension.java,v 1.14 2014/09/15 14:32:28 spb Exp $")


public class CommandLineImageExtension extends ChartExtension {
	
    public CommandLineImageExtension(AppContext c,NumberFormat nf) 
    	throws ParserConfigurationException 
    {
		super(c,nf);
	}
	private int count=0;
	
	@Override
	public DocumentFragment addChart(Chart chart,String caption) throws IOException  {
		Document doc = getDocument();
		DocumentFragment result = doc.createDocumentFragment();
		try{
			AppContext conn = getContext();
			String filename=conn.getInitParameter("java.io.tmpdir","/tmp")+"/img"+count;
			count++;
			ChartData<?> c1 = chart.chart.getChartData();
			c1.createPNG(new File(filename+".png"));
			c1.getSize();
			
			Element e = doc.createElement("Figure");
			result.appendChild(e);
			e.setAttribute("src", "file:/"+filename+".png");
			e.setAttribute("alt", "figure");
			if( caption != null && caption.trim().length() > 0 ){
				Element c = doc.createElement("Caption");
				c.appendChild(doc.createTextNode(caption));
				e.appendChild(c);
			}
		}catch(Exception e){
			getContext().getService(LoggerService.class).getLogger(getClass()).error("Problem making chart",e);
		}
		return result;
	}

	@Override
	public boolean graphOutput() {
		return true;
	}

	
	
}