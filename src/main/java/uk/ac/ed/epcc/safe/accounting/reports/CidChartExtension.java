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
import java.text.NumberFormat;
import java.util.HashMap;
import java.util.LinkedHashMap;
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


public class CidChartExtension extends ChartExtension {
    private final Map<String,MimeStreamData>  data = new LinkedHashMap<>();
    private int count=0;
    private Random rng;
	public CidChartExtension(AppContext c,NumberFormat nf) throws ParserConfigurationException {
		super(c,nf);
		rng=new Random();
	}

	@Override
	public DocumentFragment  addChart(Chart t,String caption){
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
		}catch(Exception e) {
			addError("Bad Plot", "Error adding chart as cid reference", e);
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