// Copyright - The University of Edinburgh 2011
/*******************************************************************************
 * Copyright (c) - The University of Edinburgh 2010
 *******************************************************************************/
package uk.ac.ed.epcc.safe.accounting.reports;

import java.io.ByteArrayOutputStream;
import java.text.NumberFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Element;

import uk.ac.ed.epcc.safe.accounting.servlet.ReportServlet;
import uk.ac.ed.epcc.webapp.AppContext;
import uk.ac.ed.epcc.webapp.model.data.stream.ByteArrayMimeStreamData;
import uk.ac.ed.epcc.webapp.model.data.stream.MimeStreamData;
import uk.ac.ed.epcc.webapp.model.serv.SettableServeDataProducer;
import uk.ac.ed.epcc.webapp.servlet.ServletService;
import uk.ac.ed.epcc.webapp.session.SessionDataProducer;
/** ChartExtension that generates cid: urls and stores the attachments in the extension
 * 
 * @author spb
 *
 */
@uk.ac.ed.epcc.webapp.Version("$Id: CidChartExtension.java,v 1.3 2015/07/21 21:23:56 spb Exp $")

public class CidChartExtension extends ChartExtension {
    private final Map<String,MimeStreamData>  data = new HashMap<String, MimeStreamData>();
    private int count=0;
    private Random rng;
	public CidChartExtension(AppContext c,NumberFormat nf) throws ParserConfigurationException {
		super(c,nf);
		rng=new Random();
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
			String tag="image"+(count++)+"."+rng.nextInt(1024)+getContext().getInitParameter("email.from_address", "saf@epcc.ed.ac.uk");
			data.put(tag, msd);
			Element e = doc.createElement("Figure");
			result.appendChild(e);
			ServletService serv = conn.getService(ServletService.class);
			e.setAttribute("src", "cid:"+tag);
			
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

	public Map<String, MimeStreamData> getData() {
		return data;
	}

}