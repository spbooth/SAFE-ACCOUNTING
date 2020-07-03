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
package uk.ac.ed.epcc.safe.accounting.reports;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URL;

import javax.xml.transform.Result;
import javax.xml.transform.sax.SAXResult;

import org.apache.fop.apps.Fop;
import org.apache.fop.apps.FopFactory;
import org.apache.fop.apps.FopFactoryBuilder;
import org.apache.fop.apps.MimeConstants;
import org.apache.fop.configuration.Configuration;
import org.apache.fop.configuration.DefaultConfigurationBuilder;

import uk.ac.ed.epcc.webapp.AppContext;

public final class PDFReportType extends ReportType {
	public PDFReportType(String name,String extension, String mime, String description) {
		super(name,extension,mime,description);
	}

	
	
	private static FopFactory fopFactory = null;
	public Result getResult(AppContext conn, OutputStream out) throws Exception{
		synchronized (getClass()) {
			
		
		if( fopFactory == null ){
			
			InputStream stream = getClass().getResourceAsStream("/fop.xconf");
			if( stream != null ){
				DefaultConfigurationBuilder builder = new DefaultConfigurationBuilder();
				Configuration cfg = builder.build(stream);
				URI font_uri=null;
				URL res = getClass().getResource(conn.getInitParameter("fop.font_resource", "/fonts"));
				if( res != null ){
					File dir = new File(res.getFile());
					font_uri = dir.toURI();
				}
				if( font_uri == null){
					font_uri=new URI("file://etc/fonts");
				}
				FopFactoryBuilder fop_builder = new FopFactoryBuilder(font_uri );
				fop_builder.setConfiguration(cfg);
				fopFactory = fop_builder.build();
			}
		}
		}
		if( fopFactory == null ){
			throw new IOException("No fop factory");
		}
		Fop fop = fopFactory.newFop(MimeConstants.MIME_PDF, out);

		return  new SAXResult(fop.getDefaultHandler());

	}
}