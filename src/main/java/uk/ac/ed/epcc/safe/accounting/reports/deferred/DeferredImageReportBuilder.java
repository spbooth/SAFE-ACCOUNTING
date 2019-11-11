//| Copyright - The University of Edinburgh 2015                            |
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
package uk.ac.ed.epcc.safe.accounting.reports.deferred;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.imageio.ImageIO;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactoryConfigurationError;

import uk.ac.ed.epcc.safe.accounting.ErrorSet;
import uk.ac.ed.epcc.safe.accounting.reports.ChartExtension;
import uk.ac.ed.epcc.safe.accounting.reports.CidChartExtension;
import uk.ac.ed.epcc.safe.accounting.reports.DeveloperReportType;
import uk.ac.ed.epcc.safe.accounting.reports.ReportBuilder;
import uk.ac.ed.epcc.safe.accounting.reports.ReportType;
import uk.ac.ed.epcc.safe.accounting.reports.ReportTypeRegistry;
import uk.ac.ed.epcc.safe.accounting.reports.exceptions.ReportException;
import uk.ac.ed.epcc.webapp.AppContext;
import uk.ac.ed.epcc.webapp.exceptions.InvalidArgument;
import uk.ac.ed.epcc.webapp.logging.Logger;
import uk.ac.ed.epcc.webapp.model.TextProvider;
import uk.ac.ed.epcc.webapp.model.data.Exceptions.DataFault;
import uk.ac.ed.epcc.webapp.model.data.stream.ByteArrayMimeStreamData;
import uk.ac.ed.epcc.webapp.model.data.stream.ByteArrayStreamData;
import uk.ac.ed.epcc.webapp.model.data.stream.MimeStreamData;

/** A varient of {@link ReportBuilder} 
 * This if for deferred image generations so the report is assumed to need no
 * parameters and to contain a single image.
 * 
 * @author spb
 *
 */
public class DeferredImageReportBuilder extends ReportBuilder{
	
	
	public DeferredImageReportBuilder(AppContext conn,TextProvider template) throws URISyntaxException, ParserConfigurationException {
		super(new DeferredImageTypeRegistry(conn));
		setTemplate(template);
		log_source=true;
		
	}
    public DeferredImageReportBuilder(AppContext conn,String template) throws URISyntaxException, ParserConfigurationException, DataFault, InvalidArgument, TransformerFactoryConfigurationError, TransformerException {
    	super(new DeferredImageTypeRegistry(conn));
    	setTemplate(template);
    }
	
 	public MimeStreamData makeImage() throws Exception {
		HashMap<String, Object> params = new HashMap<String, Object>();
		ReportType type = DeferredImageTypeRegistry.DPNG;
		Logger log = getLogger();
		setupExtensions(type,params);
		renderXML(type, params, type.getResult(getContext(),new OutputStream() {
			
			@Override
			public void write(int b) throws IOException {
				// just discard all bytes.
			}
		}));
		if( hasErrors()){
			Set<ErrorSet> errors = getErrors();
			for(ErrorSet s : errors){
				if( s.size() > 0){
					s.report(log);
				}
			}
			throw new ReportException("Error making report");
		}
		// actually igore the report output we are only interested in the image
		ChartExtension ext = (ChartExtension) params.get("ChartExtension");
		if( ext instanceof CidChartExtension){
			Map<String,MimeStreamData> images = ((CidChartExtension)ext).getData();
			if( ! images.isEmpty()){
				return images.values().iterator().next();
			}else {
				getLogger().debug("No images stored in extension");
			}
		}
		int width = 800;
		int height = 400;
		BufferedImage bi = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		Graphics2D ig2 = bi.createGraphics();
		//ig2.setPaint(new GradientPaint(0.0f, 0.0F, new Color(0.75F, 0.75F, 1.0F), 0.0F, 100.0F, Color.white, false));
		//ig2.fillRect(0, 0, width, height);
	    Font font = new Font("Arial", Font.BOLD, 14);
	    ig2.setFont(font);
	    String message = "This plot contained no data";
	    FontMetrics fontMetrics = ig2.getFontMetrics();
	    int stringWidth = fontMetrics.stringWidth(message);
	    int stringHeight = fontMetrics.getAscent();
	    ig2.setPaint(Color.black);
	    ig2.drawString(message, (width - stringWidth) / 2, height / 2 + stringHeight / 4);
	    ByteArrayOutputStream out = new ByteArrayOutputStream();
	    ImageIO.write(bi, "PNG", out);
	    ByteArrayMimeStreamData i = new ByteArrayMimeStreamData(out.toByteArray());
	    i.setName("image.png");
	    i.setMimeType("image/png");
		return i;
	}
	
	
				
				
	
	
	public static class DeferredImageTypeRegistry extends ReportTypeRegistry{
		
		public static final ReportType	DPNG = new DeveloperReportType("DPNG","png","image/png","PNG Image");
	//  
		public DeferredImageTypeRegistry(AppContext conn) {
			super(conn);
		}
		@Override
		protected Set<ReportType> getSpecialReportTypes() {
			Set<ReportType> s = super.getSpecialReportTypes();
			s.add(DPNG);
			return s;
		}
	}

}