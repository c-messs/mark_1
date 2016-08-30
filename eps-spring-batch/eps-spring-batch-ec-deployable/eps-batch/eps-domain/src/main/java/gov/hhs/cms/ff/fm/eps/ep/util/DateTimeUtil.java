package gov.hhs.cms.ff.fm.eps.ep.util;

import java.math.BigDecimal;
import java.math.MathContext;
import java.sql.Date;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoField;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeConstants;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import org.apache.commons.lang.StringUtils;

public class DateTimeUtil {


	/**
	 *  9999-12-31 23:59:59.000
	 */
	public static final LocalDateTime HIGHDATE = getHighDate();

	public static final int FRACTIONAL_SEC_FFM = 6;

	private static final int FRACTION_SEC_JAVA_TIME = 9;

	private static final MathContext MATH_CTX = new MathContext(FRACTIONAL_SEC_FFM);

	private static final int PWR_OF = (int) Math.pow(10, FRACTION_SEC_JAVA_TIME);

	private static DatatypeFactory dfInstance = null;


	static {

		try {
			dfInstance = DatatypeFactory.newInstance();

		} catch (DatatypeConfigurationException ex) {
			throw new IllegalStateException(
					"Exception in getting instance of DatatypeFactory", ex);
		}
	}


	/**
	 * Converts XMLGregorianCalendar to java.time.LocalDateTime with fractional seconds.
	 * @param xmlGC
	 * @return
	 */
	public static LocalDateTime getLocalDateTimeFromXmlGC(XMLGregorianCalendar xmlGC) {

		LocalDateTime localDT = null;

		if (xmlGC != null ) {

			localDT = xmlGC.toGregorianCalendar().toZonedDateTime().toLocalDateTime();
			BigDecimal xmlFracSec = xmlGC.getFractionalSecond();
			if (xmlFracSec != null) {
				BigDecimal nanoAsInt = xmlFracSec.multiply(BigDecimal.valueOf(PWR_OF));
				localDT = localDT.withNano(nanoAsInt.intValue());
			}
		}
		return localDT;
	}



	/**
	 * @param xmlGC
	 * @return
	 */
	public static LocalDate getLocalDateFromXmlGC(XMLGregorianCalendar xmlGC) {

		LocalDate dt = null;
		if (xmlGC != null ) {
			dt = xmlGC.toGregorianCalendar().toZonedDateTime().toLocalDate();
		}
		return dt;
	}


	/**
	 * Converts a java.LocalDateTime into an instance of XMLGregorianCalendar with fraction precision of 6.
	 * 
	 * Marshalled output format is determined by XSD type:
	 * - xsd:dateTime  YYYY-MM-DDTHH:MM:SS.ssssss (with no timezone) 
	 * - xsd:date      YYYY-MM-DD (with no time and no timezone)
	 * @param date
	 * @return
	 */
	public static XMLGregorianCalendar getXMLGregorianCalendar(LocalDateTime localDateTime) {

		XMLGregorianCalendar xmlGC = null;

		if (localDateTime != null) {
			DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSSSSSSS");
			xmlGC = dfInstance.newXMLGregorianCalendar(dtf.format(localDateTime));
			xmlGC.setTimezone(DatatypeConstants.FIELD_UNDEFINED);
			if (localDateTime.isSupported(ChronoField.NANO_OF_SECOND)) {
				double dblNanos = localDateTime.getNano() * .000000001;
				BigDecimal bDec = new BigDecimal(dblNanos, MATH_CTX);
				xmlGC.setFractionalSecond(bDec);
			}
		}
		return xmlGC;
	}


	/**
	 * Converts a java.LocalDateTime into an instance of XMLGregorianCalendar
	 * 
	 * Marshalled output format is determined by XSD type:
	 * - xsd:date      YYYY-MM-DD (with no time and no timezone)
	 * @param date
	 * @return
	 */
	public static XMLGregorianCalendar getXMLGregorianCalendar(LocalDate localDate) {
		XMLGregorianCalendar xmlGC = null;
		if (localDate != null) {
			xmlGC = dfInstance.newXMLGregorianCalendar(localDate.toString());
			xmlGC.setTime(0, 0, 0);
			xmlGC.setTimezone(DatatypeConstants.FIELD_UNDEFINED);
			xmlGC.setMillisecond(DatatypeConstants.FIELD_UNDEFINED);
		}
		return xmlGC;
	}



	/**
	 * Converts a String xml date into an instance of XMLGregorianCalendar
	 * Marshalled output format is determined by XSD type:
	 * - xsd:dateTime  YYYY-MM-DDTHH:MM:SS (with no timezone) 
	 * - xsd:date      YYYY-MM-DD (with no time and no timezone)
	 * @param strDate
	 * @return
	 */
	public static XMLGregorianCalendar getXMLGregorianCalendar(String strDateTime) {

		XMLGregorianCalendar xmlGC = null;
		if (!StringUtils.isBlank(strDateTime)) {
			xmlGC = dfInstance.newXMLGregorianCalendar(strDateTime);
			xmlGC.setTimezone(DatatypeConstants.FIELD_UNDEFINED);
		}
		return xmlGC;
	}


	/**
	 * If not null, converts sql.date to time.localDate
	 * @param date
	 * @return
	 */
	public static LocalDate getLocalDateFromSqlDate(java.sql.Date date) {

		LocalDate dt = null;

		if(date != null) {
			dt =  date.toLocalDate();
		}
		return dt;
	}


	/**
	 * If not null, converts sql.Timestamp to time.localDateTime
	 * @param timestamp
	 * @return
	 */
	public static LocalDateTime getLocalDateTimeFromSqlTimestamp(Timestamp timestamp) {

		LocalDateTime dt = null;

		if(timestamp != null) {
			dt =  timestamp.toLocalDateTime();
		}
		return dt;
	}
	
	/**
	 * Returns greater of given two dates
	 * @param dateTime1
	 * @param dateTime2
	 * @return
	 */
	public static LocalDateTime greaterOf(LocalDateTime dateTime1, LocalDateTime dateTime2) {
		
		if(dateTime2 != null && dateTime2.isAfter(dateTime1)) {
			return dateTime2;
		}
		
		return dateTime1;
	}
	
	/**
	 * Returns lesser of given two dates
	 * @param dateTime1
	 * @param dateTime2
	 * @return
	 */
	public static LocalDateTime lesserOf(LocalDateTime dateTime1, LocalDateTime dateTime2) {
		
		if(dateTime2 != null && dateTime2.isBefore(dateTime1)) {
			return dateTime2;
		}
		
		return dateTime1;
	}


	public static Timestamp getSqlTimestamp(LocalDateTime localDateTime) {

		Timestamp ts = null;

		if (localDateTime != null) {
			ts = Timestamp.valueOf(localDateTime);
		}
		return ts;
	}

	public static Date getSqlDate(LocalDate localDate) {

		Date dt = null;

		if (localDate != null) {
			dt = Date.valueOf(localDate);
		}
		return dt;
	}


	/**
	 * Creates the highest possible "Oracle" dateTime with fractional seconds set to "000000000".
	 *    9999-12-31 23:59:59.000
	 * Since Pre-M834 MAINTENANCEENDDATETIME was of DATE dataType and did not have nano second
	 * component, pre-M834 will be converted from 9999-12-31 23:59:59 to 9999-12-31 23:59:59.00000000 in Oracle.
	 * To keep M834 data and future data consistent, HIGDATE is Java DateTime with milliseconds = 0.
	 * 
	 * @return
	 */
	private static LocalDateTime getHighDate() {

		return LocalDateTime.of(9999, 12, 31, 23, 59, 59, 0);
	}

}
