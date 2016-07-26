package gov.hhs.cms.ff.fm.eps.ep.validation;

import java.util.GregorianCalendar;

import javax.xml.datatype.DatatypeConstants;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import junit.framework.TestCase;

import org.joda.time.DateTime;

public abstract class BaseValidationTest extends TestCase {


	private static final DateTime DATETIME = new DateTime();
	private static final int YEAR = DATETIME.getYear();

	protected final DateTime JAN_1 = new DateTime(YEAR, 1, 1, 0, 0);
	protected final DateTime JAN_15 = new DateTime(YEAR, 1, 15, 0, 0);
	protected final DateTime JAN_31 = new DateTime(YEAR, 1, 31, 0, 0);
	protected final DateTime FEB_1 = new DateTime(YEAR, 2, 1, 0, 0);
	protected final DateTime FEB_MAX = new DateTime(YEAR, 2, FEB_1.dayOfMonth().getMaximumValue(), 0, 0);
	protected final DateTime MAR_1 = new DateTime(YEAR, 3, 1, 0, 0);
	protected final DateTime MAR_31 = new DateTime(YEAR, 3, 31, 0, 0);
	protected final DateTime APR_1 = new DateTime(YEAR, 1, 26, 0, 0);
	
	private DatatypeFactory dfInstance = null;

	@Override
	public void setUp() throws Exception {

		dfInstance = DatatypeFactory.newInstance();
	}

	@Override
	public void tearDown() throws Exception {

	}
	
	protected XMLGregorianCalendar getXMLGregorianCalendar(org.joda.time.DateTime date) {
		XMLGregorianCalendar xmlGregCal = null;
		if (date == null) {
			return xmlGregCal;
		} else {
			GregorianCalendar gc = new GregorianCalendar();
			gc.setTimeInMillis(date.getMillis());
			xmlGregCal = dfInstance.newXMLGregorianCalendar(gc);
			xmlGregCal.setTimezone(DatatypeConstants.FIELD_UNDEFINED);
			xmlGregCal.setMillisecond(DatatypeConstants.FIELD_UNDEFINED);
			return xmlGregCal;
		}
	}


}
