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



public class CommandLineImageExtension extends ChartExtension {
	
    public CommandLineImageExtension(AppContext c,NumberFormat nf) 
    	throws ParserConfigurationException 
    {
		super(c,nf);
	}
	private int count=0;
	
	@Override
	public DocumentFragment addChart(Chart chart,String caption) {
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