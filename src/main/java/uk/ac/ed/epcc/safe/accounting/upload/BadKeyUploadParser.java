package uk.ac.ed.epcc.safe.accounting.upload;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.Map;

import uk.ac.ed.epcc.safe.accounting.update.StreamLineSplitter;
import uk.ac.ed.epcc.webapp.AbstractContexed;
import uk.ac.ed.epcc.webapp.AppContext;
import uk.ac.ed.epcc.webapp.model.data.stream.StreamData;
import uk.ac.ed.epcc.webapp.ssh.BadKeyFactory;

public class BadKeyUploadParser extends AbstractContexed implements UploadParser{

	public BadKeyUploadParser(AppContext conn) {
		super(conn);
	}

	@Override
	public String upload(Map<String, Object> parameters) throws UploadException {
		Object o = parameters.get("update");
		InputStream update = null;
		if( o != null) {
			if( o instanceof InputStream) {
				update = (InputStream) o;
			}else if( o instanceof String ) {
				update = new ByteArrayInputStream(((String)o).getBytes());
			}else if( o instanceof StreamData) {
				update = ((StreamData)o).getInputStream();
			}
		}
		if( update == null ){
			throw new UploadException("No update data");
		}
		int count=0;
		try {
			Iterator<String> it = new StreamLineSplitter(getContext(), update);
			BadKeyFactory fac = new BadKeyFactory(getContext());
			while(it.hasNext()) {
				String line = it.next();
				try {
					fac.forbid(line);
					count++;
				} catch (Exception e) {
					getLogger().error("Error processing key "+line,e);
				}
			}
		} catch (IOException e) {
			getLogger().error("Error in upload", e);
			return "Error in upload";
		}
		return ""+count+" keys uploaded to forbidden list";
	}

}
