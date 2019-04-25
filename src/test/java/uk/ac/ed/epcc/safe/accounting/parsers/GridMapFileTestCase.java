package uk.ac.ed.epcc.safe.accounting.parsers;

import static org.junit.Assert.assertEquals;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import uk.ac.ed.epcc.safe.accounting.db.ParseAccountingClassificationFactoryTestCase;
import uk.ac.ed.epcc.safe.accounting.parsers.GridMapParser;
import uk.ac.ed.epcc.safe.accounting.properties.PropertyMap;
import uk.ac.ed.epcc.safe.accounting.update.UploadContext;
import uk.ac.ed.epcc.webapp.model.data.DataObjectFactory;

public class GridMapFileTestCase extends ParseAccountingClassificationFactoryTestCase {

	private UploadContext upload = new UploadContext(){

		@Override
		public String getUpdateText() {
			return "\"/c=UK/o=eScience/ou=Edinburgh/l=NeSC/cn=stephen booth\" spb\n"+
				   "\"/C=UK/O=eScience/OU=Edinburgh/L=NeSC/CN=stephen \\,booth\" spb2\n"+// escaped commas
			       "\"/DC=uk/DC=ac/DC=ceda/O=STFC RAL/CN=https://ceda.ac.uk/openid/Stephen.Pascoe\" pascoe\n";
		}

		@Override
		public String getExceptionText() {
			return "";
		}

		@Override
		public String getSkipText() {
			return "# A comment \n\n";
		}

		@Override
		public PropertyMap getDefaults() {
			return new PropertyMap();
		}

		@Override
		public Map<String, Object> getDefaultParams() {
			return new HashMap<String, Object>();
		}

		@Override
		public String getExpectedResource() {
			return "gridmap.xml";
		}
		
	};

	
	@Test
	public void testConfiguredParser() {
		assertEquals(GridMapParser.class,getPluginOwner().getParser().getClass());
	}
	@Override
	public UploadContext getUploadContext() {
		return upload;
	}

	@Override
	public DataObjectFactory getFactory() {
		return ctx.makeObject(DataObjectFactory.class, "Gridmapfile");
	}

}
