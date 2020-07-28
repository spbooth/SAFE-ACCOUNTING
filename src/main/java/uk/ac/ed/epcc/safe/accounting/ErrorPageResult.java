package uk.ac.ed.epcc.safe.accounting;

import uk.ac.ed.epcc.webapp.AppContext;
import uk.ac.ed.epcc.webapp.content.ContentBuilder;
import uk.ac.ed.epcc.webapp.forms.result.CustomPageResult;

public class ErrorPageResult extends CustomPageResult {
	public ErrorPageResult(String title, Object content, ErrorSet err) {
		super();
		this.title = title;
		this.content = content;
		this.err = err;
	}

	private final String title;
	private final Object content;
    private final ErrorSet err;
	

	@Override
	public String getTitle() {
		return title;
	}

	@Override
	public ContentBuilder addContent(AppContext conn, ContentBuilder cb) {
		cb.addHeading(1, title);
		if( content != null) {
			cb.addObject(content);
		}
		err.addContent(cb, -1);
		return cb;
	}

}
