package uk.ac.ed.epcc.safe.accounting.reports.deferred;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;

import javax.imageio.ImageIO;

import uk.ac.ed.epcc.webapp.AppContext;
import uk.ac.ed.epcc.webapp.model.TextProvider;
import uk.ac.ed.epcc.webapp.model.data.Repository.Record;
import uk.ac.ed.epcc.webapp.model.data.Exceptions.DataFault;
import uk.ac.ed.epcc.webapp.model.data.stream.ByteArrayMimeStreamData;
import uk.ac.ed.epcc.webapp.model.data.stream.MimeStreamData;
import uk.ac.ed.epcc.webapp.model.serv.DataObjectDataProducer;

/** A {@link DataObjectDataProducer} for generating deferred charts.
 * The chart definition is set as an XML file then "upgraded" to an image on first request.
 * 
 * @author Stephen Booth
 *
 */
public class DeferredChartFactory extends DataObjectDataProducer<DeferredChartFactory.DeferredChart> {

	public static final String DEFAULT_TABLE = "DeferredCharts";
	/** mime type used for the report fragments
	 * 
	 */
	public static final String REPORT_MIME = "application/safe+xml";
	public DeferredChartFactory(AppContext c) {
		super(c, DEFAULT_TABLE);
	}

	public static class ByteArrayTextProvider extends ByteArrayOutputStream implements TextProvider{

		@Override
		public boolean hasData() {
			return size() > 0;
		}

		@Override
		public String getData() {
			return toString();
		}
		
	}
	public static class DeferredChart extends DataObjectDataProducer.MimeData{

		protected DeferredChart(Record r) {
			super(r);
		}

		@Override
		public MimeStreamData getData() throws DataFault {
			if( getMimeType().equals(REPORT_MIME)) {
				try {
					MimeStreamData current = super.getData();
					ByteArrayTextProvider text = new ByteArrayTextProvider();
					current.write(text);
					DeferredImageReportBuilder builder = new DeferredImageReportBuilder(getContext(), text);
					MimeStreamData image = builder.makeImage();
					if( image == null) {
						int width = 800;
						int height = 400;
						BufferedImage bi = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
						Graphics2D ig2 = bi.createGraphics();
					    Font font = new Font("Arial", Font.BOLD, 20);
					    ig2.setFont(font);
					    String message = "Plot contains no data";
					    FontMetrics fontMetrics = ig2.getFontMetrics();
					    int stringWidth = fontMetrics.stringWidth(message);
					    int stringHeight = fontMetrics.getAscent();
					    ig2.setPaint(Color.black);
					    ig2.drawString(message, (width - stringWidth) / 2, height / 2 + stringHeight / 4);
					    ByteArrayOutputStream out = new ByteArrayOutputStream();
					    ImageIO.write(bi, "PNG", out);
					    ByteArrayMimeStreamData i = new ByteArrayMimeStreamData(out.toByteArray());
					    i.setName("image.png");
					    i.setMimeType("image/png");
					    image=i;
					}
					setData(image);
					commit();
					return image;
				}catch(Exception e) {
					getLogger().error("Error generating image", e);
				}
				return null;
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
