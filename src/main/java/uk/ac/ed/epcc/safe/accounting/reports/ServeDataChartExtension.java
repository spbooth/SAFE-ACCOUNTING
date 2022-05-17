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

import java.io.ByteArrayOutputStream;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.*;

import uk.ac.ed.epcc.safe.accounting.reports.deferred.DeferredChartFactory;
import uk.ac.ed.epcc.safe.accounting.servlet.ReportServlet;
import uk.ac.ed.epcc.webapp.AppContext;
import uk.ac.ed.epcc.webapp.Feature;
import uk.ac.ed.epcc.webapp.model.data.stream.ByteArrayMimeStreamData;
import uk.ac.ed.epcc.webapp.model.serv.SettableServeDataProducer;
import uk.ac.ed.epcc.webapp.preferences.Preference;
import uk.ac.ed.epcc.webapp.servlet.ServeDataServlet;
import uk.ac.ed.epcc.webapp.servlet.ServletService;
import uk.ac.ed.epcc.webapp.session.SessionDataProducer;
/** ChartExtension that uses the ServeDataServlet
 * 
 * @author spb
 *
 */


public class ServeDataChartExtension extends ChartExtension {
    public static final Feature DEFERRED_CHARTS= new Preference("reports.html.deferred_charts", false, "Defer chart generation in reports until image is requested by browser");
	
    public ServeDataChartExtension(AppContext c,ReportType type) throws ParserConfigurationException {
		super(c,type);
	}

	@Override
	public DocumentFragment  addChart(Chart t,String caption) {
		Document doc = getDocument();
		DocumentFragment result = doc.createDocumentFragment();
	    try {
			AppContext conn =  getContext();
			SettableServeDataProducer producer = conn.makeObjectWithDefault(SettableServeDataProducer.class, SessionDataProducer.class, ReportServlet.SERVE_DATA_DEFAULT_TAG);
			ByteArrayOutputStream res = new ByteArrayOutputStream();
			t.chart.getChartData().createPNG(res);
			
			
			ByteArrayMimeStreamData msd = new ByteArrayMimeStreamData(res.toByteArray());
			msd.setName("image.png");
			msd.setMimeType("image/png");
			Element e = doc.createElement("Figure");
			result.appendChild(e);
			ServletService serv = conn.getService(ServletService.class);
			if( serv != null){
				e.setAttribute("src", serv.encodeURL(ServeDataServlet.getURL(conn,producer, producer.setData(msd))));
			}else{
				e.setAttribute("src", ServeDataServlet.getURL(conn,producer, producer.setData(msd)));
			}
			e.setAttribute("alt", "figure");
			if( caption != null && caption.trim().length() > 0 ){
				Element c = doc.createElement("Caption");
				c.appendChild(doc.createTextNode(caption));
				e.appendChild(c);
			}
	    }catch(Exception e) {
	    	addError("Bad Plot", "Error adding chart as ServeData link", e);
	    }
		return result;
		
	}

	@Override
	public boolean graphOutput() {
		return true;
	}

	@Override
	public boolean deferrCharts() {
		return DEFERRED_CHARTS.isEnabled(getContext());
	}

	
	@Override
	public Node emitDeferredChart(Node spec, String caption) {
		Document doc = getDocument();
		DocumentFragment result = doc.createDocumentFragment();
		if( spec == null || ! spec.hasChildNodes() ) {
			addError("Bad Plot", "No chart specification");
			return result;
		}
		try {
			
			DeferredChartFactory producer = new DeferredChartFactory(getContext());
			ByteArrayOutputStream res = new ByteArrayOutputStream();
			// write the content into xml file


			TransformerFactory transformerFactory = TransformerFactory.newInstance();
			Transformer transformer = transformerFactory.newTransformer();
			DOMSource source = new DOMSource(spec);
			StreamResult out = new StreamResult(res);
			transformer.transform(source, out);
			getLogger().debug(res.toString());
			ByteArrayMimeStreamData msd = new ByteArrayMimeStreamData(res.toByteArray());
			msd.setName("image.png");
			msd.setMimeType(DeferredChartFactory.REPORT_MIME);
			Element e = doc.createElement("Figure");
			result.appendChild(e);
			ServletService serv = conn.getService(ServletService.class);
			if( serv != null){
				e.setAttribute("src", serv.encodeURL(ServeDataServlet.getURL(conn,producer, producer.setData(msd))));
			}else{
				e.setAttribute("src", ServeDataServlet.getURL(conn,producer, producer.setData(msd)));
			}
			String alt = "dynamically generated graph";
			
			if( caption != null && caption.trim().length() > 0 ){
				Element c = doc.createElement("Caption");
				c.appendChild(doc.createTextNode(caption));
				e.appendChild(c);
				alt = alt+" ["+caption+"]";
			}
			e.setAttribute("alt", alt);
		}catch(Exception e) {
			addError("Bad Plot", "Error adding deferred chart as ServeData link", e);
		}
		
		return result;
	}
	
}