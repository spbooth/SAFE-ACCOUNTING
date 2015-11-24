package uk.ac.ed.epcc.safe.accounting.reports;

import java.text.NumberFormat;

import javax.xml.parsers.ParserConfigurationException;

import uk.ac.ed.epcc.webapp.AppContext;
import uk.ac.ed.epcc.webapp.content.TableFormatPolicy;

public class DataTableExtension extends TableExtension {

	public DataTableExtension(AppContext conn, NumberFormat nf)
			throws ParserConfigurationException {
		super(conn, nf);
	}

	@Override
	protected Class<? extends TableFormatPolicy> getDefaultTableFormatPolicy() {
		return uk.ac.ed.epcc.webapp.content.TableXMLDataFormatter.class;
	}

}
