package uk.ac.ed.epcc.safe.accounting.servlet;

import java.util.Set;

import uk.ac.ed.epcc.safe.accounting.ErrorSet;
import uk.ac.ed.epcc.webapp.AppContext;
import uk.ac.ed.epcc.webapp.content.ContentBuilder;
import uk.ac.ed.epcc.webapp.forms.result.CustomPageResult;
import uk.ac.ed.epcc.webapp.forms.result.FormResult;

/** A {@link CustomPageResult} to provide additional information
 *  for a Report Developer.
  * 
 * @author spb
 *
 */
public class DeveloperResult  extends CustomPageResult {

	public DeveloperResult(FormResult result, FormResult logs,
			Set<ErrorSet> errors, boolean has_error) {
		super();
		this.result = result;
		this.logs = logs;
		this.errors = errors;
		this.has_error = has_error;
	}

	public final FormResult result;
	public final FormResult logs;
	public final Set<ErrorSet> errors;
	
	public final boolean has_error;

	public String getTitle() {
		return "Report Developer result";
	}

	public ContentBuilder addContent(AppContext conn, ContentBuilder cb) {
		cb = cb.getPanel("block");
		cb.addHeading(2, "Report Developer Information");
		cb.addText("You have the ReportDeveloper role active. This page gives access to additional information to aid in debugging the reports");
		if( has_error ){
			cb.addText("This report resulted in an error");
			if( result != null){
				cb=cb.getHeading(5);
				cb.addLink(conn, "Processed template", result);
				cb=cb.addParent();
			}
			if( errors != null){
				for(ErrorSet error : errors){
					error.addContent(cb, -1);
				}
			}
		}else{
			cb.addText("This report completed without error");
			cb=cb.getHeading(5);
			cb.addLink(conn, "Report result", result);
			cb=cb.addParent();
		}
		if( logs != null ){
			cb=cb.getHeading(5);
			cb.addLink(conn, "Logs from report generation", logs);
			cb=cb.addParent();
		}
		cb = cb.addParent();
		return cb;
	}
	

}
