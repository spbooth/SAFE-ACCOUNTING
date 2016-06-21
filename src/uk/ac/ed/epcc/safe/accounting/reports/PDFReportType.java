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
import java.io.OutputStream;
import java.net.URL;

import javax.imageio.IIOException;
import javax.xml.transform.Result;
import javax.xml.transform.sax.SAXResult;

import org.apache.fop.apps.Fop;
import org.apache.fop.apps.FopConfParser;
import org.apache.fop.apps.FopFactory;
import org.apache.fop.apps.MimeConstants;

import uk.ac.ed.epcc.webapp.AppContext;

public final class PDFReportType extends ReportType {
	public PDFReportType(String name,String extension, String mime, String description) {
		super(name,extension,mime,description);
	}

	private FopFactory fopFactory = null;
	public Result getResult(AppContext conn, OutputStream out) throws Exception{
		
		if( fopFactory == null ){
			URL uri = getClass().getResource("/fop.xconf");
			if( uri != null  && uri.getFile() != null ){
				File file = new File(uri.getFile());
				if( file.exists()){
					FopConfParser parser = new FopConfParser(file);
					fopFactory = parser.getFopFactoryBuilder().build();
				}
			}
		}
		if( fopFactory == null ){
			throw new IIOException("No fop factory");
		}
		Fop fop = fopFactory.newFop(MimeConstants.MIME_PDF, out);

		return  new SAXResult(fop.getDefaultHandler());

	}
}