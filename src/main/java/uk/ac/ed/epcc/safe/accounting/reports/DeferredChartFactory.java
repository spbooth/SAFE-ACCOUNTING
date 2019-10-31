package uk.ac.ed.epcc.safe.accounting.reports;

import uk.ac.ed.epcc.webapp.AppContext;
import uk.ac.ed.epcc.webapp.model.data.Repository.Record;
import uk.ac.ed.epcc.webapp.model.data.Exceptions.DataFault;
import uk.ac.ed.epcc.webapp.model.data.stream.MimeStreamData;
import uk.ac.ed.epcc.webapp.model.serv.DataObjectDataProducer;

/** A {@link DataObjectDataProducer} for generating deferred charts.
 * The chart definition is set as an XML file then "upgraded" to an image on first request.
 * 
 * @author Stephen Booth
 *
 */
public class DeferredChartFactory extends DataObjectDataProducer<DeferredChartFactory.DeferredChart> {

	public static final String DEFAULT_TABLE = "DeferredCahrts";
	/** mime type used for the report fragments
	 * 
	 */
	public static final String REPORT_MIME = "application/safe+xml";
	public DeferredChartFactory(AppContext c) {
		super(c, DEFAULT_TABLE);
	}

	public static class DeferredChart extends DataObjectDataProducer.MimeData{

		protected DeferredChart(Record r) {
			super(r);
		}

		@Override
		public MimeStreamData getData() throws DataFault {
			if( getMimeType().equals(REPORT_MIME)) {
				//TODO upgrade to image
			}
			return super.getData();
		}
		
	}

	@Override
	protected DeferredChart makeBDO(Record res) throws DataFault {
		return new DeferredChart(res);
	}

	@Override
	public Class<DeferredChart> getTarget() {
		return DeferredChart.class;
	}

}
