/**
 * 
 */
package gov.hhs.cms.ff.fm.eps.ep.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeConstants;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;



/**
 * @author
 * 
 */
public class EpsDateUtils {
	
	private static DatatypeFactory dfInstance = null;
	
	public static final DateTime HIGHDATE = getHighDate();

	static {

		try {
			dfInstance = DatatypeFactory.newInstance();

		} catch (DatatypeConfigurationException ex) {
			throw new IllegalStateException(
					"Exception in getting instance of DatatypeFactory", ex);
		}
	}

	/**
	 * 
	 */
	public EpsDateUtils() {
		;
	}


	/**
	 * Creates the highest possible "Oracle" date with fractional seconds set to "000".
	 *    9999-12-31 23:12:59.000
	 * Since Pre-M834 MAINTENANCEENDDATETIME was of DATE dataType and did not have millisecond
	 * component, pre-M834 will be converted from 9999-12-31 23:12:59 to 9999-12-31 23:12:59.000000.
	 * To keep M834 data (JodaTime with only milliseconds) and future data consistent, HIGDATE is
	 * ".000" JodaTime which converts to Oracle TIMESTAMP ".000000"
	 * 
	 * @return
	 */
	private static DateTime getHighDate() {
		
		return new DateTime(9999, 12, 31, 23, 59, 59, 0);
	}


	/**
	 * Converts a java.util.Date into an instance of XMLGregorianCalendar
	 * 
	 * Marshalled output format is determined by XSD type:
	 * - xsd:dateTime  YYYY-MM-DDTHH:MM:SS (with no timezone) 
	 * - xsd:date      YYYY-MM-DD (with no time and no timezone)
	 * @param date
	 * @return
	 */
	public static XMLGregorianCalendar getXMLGregorianCalendar(
			org.joda.time.DateTime date) {
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



	/**
	 * Converts a String xml date into an instance of XMLGregorianCalendar
	 * Marshalled output format is determined by XSD type:
	 * - xsd:dateTime  YYYY-MM-DDTHH:MM:SS (with no timezone) 
	 * - xsd:date      YYYY-MM-DD (with no time and no timezone)
	 * @param strDate
	 * @return
	 */
	public static XMLGregorianCalendar getXMLGregorianCalendar(String strDate) {

		XMLGregorianCalendar xmlGregCal = null;
		GregorianCalendar gc = new GregorianCalendar();
		if (!StringUtils.isBlank(strDate)) {
			gc.setTime(stringToJavaUtilDate(strDate));
			xmlGregCal = dfInstance.newXMLGregorianCalendar(gc);
			xmlGregCal.setTimezone(DatatypeConstants.FIELD_UNDEFINED);
			xmlGregCal.setMillisecond(DatatypeConstants.FIELD_UNDEFINED);
		}
		return xmlGregCal;
	}



	/**
	 * Converts a String date to java.Util.Date
	 * @param strDate
	 * @return
	 */
	private static Date stringToJavaUtilDate(String strDate) {

		Date date = null;

		if (strDate != null) {
			try {
				date = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss",
						Locale.ENGLISH).parse(strDate);
			} catch (ParseException pex) {
				date = null;
			}
		}
		return date;
	}



	/**
	 * @param xmlGC
	 * @return
	 */
	public static DateTime getDateTimeFromXmlGC(XMLGregorianCalendar xmlGC) {

		DateTime dtTime = null;
		if (xmlGC != null ) {

			dtTime = new DateTime(getUtilDateFromXmlGC(xmlGC).getTime());
		}
		return dtTime;
	}

	/**
	 * Converts an XMLGregorianCalendar to an instance of java.util.Date
	 * @param xmlGC
	 * @return
	 */
	private static Date getUtilDateFromXmlGC(XMLGregorianCalendar xmlGC) {
		if (xmlGC == null) {
			return null;
		} else {
			return xmlGC.toGregorianCalendar().getTime();
		}
	}

	/**
	 * Get the current Date time WITHOUT milliseconds.
	 * @return
	 */
	public static DateTime getCurrentDateTime(){
		DateTime dt1 = new DateTime();
		return new DateTime(dt1.getMillis() - dt1.getMillisOfSecond());
	}

}
