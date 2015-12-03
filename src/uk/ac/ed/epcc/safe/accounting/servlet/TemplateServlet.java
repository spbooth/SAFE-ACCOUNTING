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
package uk.ac.ed.epcc.safe.accounting.servlet;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import uk.ac.ed.epcc.safe.accounting.reports.ReportBuilder;
import uk.ac.ed.epcc.webapp.AppContext;
import uk.ac.ed.epcc.webapp.model.TextFileOverlay;
import uk.ac.ed.epcc.webapp.model.TextFileOverlay.TextFile;
import uk.ac.ed.epcc.webapp.servlet.ServletService;
import uk.ac.ed.epcc.webapp.servlet.SessionServlet;
import uk.ac.ed.epcc.webapp.servlet.session.ServletSessionService;
import uk.ac.ed.epcc.webapp.session.SessionService;
/** Servlet to give access to template/schema files 
 * for use by client side editors
 * 
 * @author spb
 *
 */
public class TemplateServlet extends SessionServlet {

	
	@Override
	public void doPost(HttpServletRequest req, HttpServletResponse res,
			AppContext conn, SessionService person) throws Exception {
		ServletService serv = conn.getService(ServletService.class);
		LinkedList<String> args=serv.getArgs();
		Map params = serv.getParams();
		if( args.size() < 2 ){
			res.sendError(HttpServletResponse.SC_BAD_REQUEST, "File path required");
			return;
		}
		String type = args.pop();
		String name = args.pop();
		ReportBuilder builder = ReportBuilder.getInstance(conn);
		TextFileOverlay overlay;
		String group;
		boolean update=false;
		if( type.equals("schema")){
			overlay=builder.getSchemaOverlay();
			group=builder.SCHEMA_GROUP;
		}else if( type.equals("report")){
			overlay=builder.getReportOverlay();
			group=ReportBuilder.REPORT_TEMPLATE_GROUP;
		}else if( type.equals("update")){
			overlay=builder.getReportOverlay();
			group=ReportBuilder.REPORT_TEMPLATE_GROUP;
			update=true;
		}else{
			res.sendError(HttpServletResponse.SC_NO_CONTENT);
			return;
		}
		res.setContentType("application/xml");
		TextFile file = overlay.find(group, name);
		if( file == null ){
			res.sendError(HttpServletResponse.SC_NO_CONTENT);
			return;
		}
		if( update ){
			// post method update.
			getLogger(conn).debug("Update of "+name);
			InputStreamReader reader = new InputStreamReader(req.getInputStream());
			StringBuilder sb = new StringBuilder();
			for(int c=reader.read(); c != -1 ; c=reader.read()){
				sb.append((char)c);
			}
			file.setText(sb.toString());
			file.commit();
		}else{
			getLogger(conn).debug("Fetch of "+name);
			String data = file.getData();
			res.getWriter().append(data);
		}
	}

	@Override
	public void doPut(HttpServletRequest req, HttpServletResponse res,
			AppContext conn, SessionService person)
			throws Exception {
		ServletService serv = conn.getService(ServletService.class);
		LinkedList<String> args=serv.getArgs();
		if( args.size() < 2 ){
			res.sendError(HttpServletResponse.SC_BAD_REQUEST, "File path required");
			return;
		}
		String type = args.pop();
		String name = args.pop();
		ReportBuilder builder = ReportBuilder.getInstance(conn);
		TextFileOverlay overlay;
		String group;
		if( type.equals("report")){
			overlay=builder.getReportOverlay();
			group=ReportBuilder.REPORT_TEMPLATE_GROUP;
		}else{
			res.sendError(HttpServletResponse.SC_NO_CONTENT);
			return;
		}
		getLogger(conn).debug("Post to "+name);
		TextFile file = overlay.find(group, name);
		if( file == null ){
			res.sendError(HttpServletResponse.SC_NO_CONTENT);
			return;
		}
		InputStreamReader reader = new InputStreamReader(req.getInputStream());
		StringBuilder sb = new StringBuilder();
		for(int c=reader.read(); c != -1 ; c=reader.read()){
			sb.append((char)c);
		}
		file.setText(sb.toString());
		file.commit();
	}

	@Override
	protected boolean authorized(ServletSessionService serv) {
		return serv != null && serv.hasRoleFromList(ReportBuilder.REPORT_DEVELOPER,SessionService.ADMIN_ROLE);
	}

}