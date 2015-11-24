// Copyright - The University of Edinburgh 2011
/*******************************************************************************
 * Copyright (c) - The University of Edinburgh 2010
 *******************************************************************************/
package uk.ac.ed.epcc.safe.accounting.reports;

import java.io.ByteArrayOutputStream;
import java.text.NumberFormat;

import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Element;

import uk.ac.ed.epcc.safe.accounting.servlet.ReportServlet;
import uk.ac.ed.epcc.webapp.AppContext;
import uk.ac.ed.epcc.webapp.model.data.stream.ByteArrayMimeStreamData;
import uk.ac.ed.epcc.webapp.model.serv.SettableServeDataProducer;
import uk.ac.ed.epcc.webapp.servlet.ServeDataServlet;
import uk.ac.ed.epcc.webapp.servlet.ServletService;
import uk.ac.ed.epcc.webapp.session.SessionDataProducer;
/** ChartExtension that uses the ServeDataServlet
 * 
 * @author spb
 *
 */
@uk.ac.ed.epcc.webapp.Version("$Id: ServeDataChartExtension.java,v 1.22 2015/07/21 21:23:55 spb Exp $")

public class ServeDataChartExtension extends ChartExtension {
   
	public ServeDataChartExtension(AppContext c,NumberFormat nf) throws ParserConfigurationException {
		super(c,nf);
	}

	@Override
	public DocumentFragment  addChart(Chart t,String caption) throws Exception {
		Document doc = getDocument();
		DocumentFragment result = doc.createDocumentFragment();
	
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
		
		return result;
		
	}

	@Override
	public boolean graphOutput() {
		return true;
	}

}