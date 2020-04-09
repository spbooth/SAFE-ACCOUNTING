package uk.ac.ed.epcc.safe.accounting.update;

import java.io.IOException;
import java.io.InputStream;

import uk.ac.ed.epcc.webapp.AppContext;

public class UnixStreamLineSpliter extends StreamLineSplitter {

	public UnixStreamLineSpliter(AppContext conn, InputStream stream) throws IOException {
		super(conn, stream);
	}

	@Override
	protected String clean(String in) {
		int pos = in.indexOf('#');
		if( pos == 0) {
			return "";
		}
		if( pos > 0  ) {
			in = in.substring(0, pos);
		}
		return in.trim();
	}

}
