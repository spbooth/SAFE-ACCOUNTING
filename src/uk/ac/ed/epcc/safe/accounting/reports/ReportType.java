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

import java.io.OutputStream;
import java.text.NumberFormat;

import javax.xml.transform.Result;
import javax.xml.transform.stream.StreamResult;

import uk.ac.ed.epcc.webapp.AppContext;
import uk.ac.ed.epcc.webapp.Feature;
import uk.ac.ed.epcc.webapp.session.SessionService;

public class ReportType {
	public static final Feature REPORT_NUMBER_GROUP_FEATURE = new Feature("report.number_group", true, "Should the default number format use grouping");
	private final String name;
	private final String extension;
	private final String mime;
	final String description;

	public ReportType(String name,String extension, String mime,String description) {
		this.name = name;
		this.extension = extension;
		this.mime = mime;
		this.description=description;
		
	}
	public String name(){
		return name;
	}
	public String getExtension() {
		return extension;
	}

	public boolean allowSelect(SessionService sess){
		return true;
	}
	public String getMimeType() {
		return mime;
	}
	
	public NumberFormat getNumberFormat(AppContext conn){
		NumberFormat nf = NumberFormat.getInstance();
		nf.setGroupingUsed(REPORT_NUMBER_GROUP_FEATURE.isEnabled(conn)); // global off for now tests assume this
		nf.setMinimumFractionDigits(conn.getIntegerParameter("report.min.fractional", 0));
		nf.setMaximumFractionDigits(conn.getIntegerParameter("report.max.fractional", 3));
		return nf;
	}

	public Result getResult(AppContext conn, OutputStream out) throws Exception{
		return new StreamResult(out);
	}
	@Override
	public boolean equals(Object obj) {
		if( obj instanceof ReportType){
			return name.equals(((ReportType)obj).name);
		}
		return false;
	}
	@Override
	public int hashCode() {
		return name.hashCode();
	}
	@Override
	public final String toString() {
		return name.toString();
	}
	
}