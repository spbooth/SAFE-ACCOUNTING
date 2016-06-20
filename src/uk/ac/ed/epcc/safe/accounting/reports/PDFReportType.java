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

	public Result getResult(AppContext conn, OutputStream out) throws Exception{
		
		FopConfParser parser = new FopConfParser(new File(getClass().getResource("fop.xconf").getFile()));
		FopFactory fopFactory = parser.getFopFactoryBuilder().build();
		Fop fop = fopFactory.newFop(MimeConstants.MIME_PDF, out);

		return  new SAXResult(fop.getDefaultHandler());

	}
}