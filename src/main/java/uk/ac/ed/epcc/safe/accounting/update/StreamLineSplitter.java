package uk.ac.ed.epcc.safe.accounting.update;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Iterator;

import uk.ac.ed.epcc.webapp.AbstractContexed;
import uk.ac.ed.epcc.webapp.AppContext;
import uk.ac.ed.epcc.webapp.model.data.CloseableIterator;
/** A wrapper class to iterate over the (non empty) lines in a stream
 * 
 * @author Stephen Booth
 *
 */
public class StreamLineSplitter extends AbstractContexed implements CloseableIterator<String> {
	private BufferedReader reader;
	private String next;
	public StreamLineSplitter(AppContext conn,InputStream stream) throws IOException {
		super(conn);
		reader = new BufferedReader(new InputStreamReader(stream));
		next = nextLine();
	}

	protected String nextLine() throws IOException {
		while(true) {
			String tmp = reader.readLine();
			if( tmp == null ) {
				return null;
			}
			tmp = clean(tmp);
			if( ! tmp.isEmpty()) {
				return tmp;
			}
		}
	}

	protected String clean(String in) {
		return in.trim();
	}
	@Override
	public boolean hasNext() {
		return next != null;
	}

	@Override
	public String next() {
		String result = next;
		try {
			next = nextLine();
		} catch (IOException e) {
			next=null;
			getLogger().error("Error reading next line", e);
		}
		return result;
	}

	@Override
	public void close() throws Exception {
		reader.close();
	}

}
