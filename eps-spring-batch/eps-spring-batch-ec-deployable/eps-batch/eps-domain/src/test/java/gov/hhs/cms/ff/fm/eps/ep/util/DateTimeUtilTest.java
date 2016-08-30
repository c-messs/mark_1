package gov.hhs.cms.ff.fm.eps.ep.util;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Date;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoField;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import junit.framework.TestCase;

import org.junit.Test;

public class DateTimeUtilTest extends TestCase {

	private static final int YEAR = LocalDate.now().getYear();

	private static DatatypeFactory dfInstance = null;

	static {

		try {
			dfInstance = DatatypeFactory.newInstance();

		} catch (DatatypeConfigurationException ex) {
			throw new IllegalStateException(
					"Exception in getting instance of DatatypeFactory", ex);
		}
	}


	@Test
	public void test_HighDate() {

		int expectedYear = 9999;
		int expectedMonth = 12;
		int expectedDate = 31;
		int expectedHour = 23;
		int expectedMinute = 59;
		int expectedSecond = 59;
		int expectedMicro = 0;

		LocalDateTime highDate = DateTimeUtil.HIGHDATE;

		assertEquals("HIGHDATE Year", expectedYear, highDate.getYear());
		assertEquals("HIGHDATE Month", expectedMonth, highDate.getMonthValue());
		assertEquals("HIGHDATE Date", expectedDate, highDate.getDayOfMonth());
		assertEquals("HIGHDATE Hour", expectedHour, highDate.getHour());
		assertEquals("HIGHDATE Minute", expectedMinute, highDate.getMinute());
		assertEquals("HIGHDATE Second", expectedSecond, highDate.getSecond());
		assertEquals("HIGHDATE Nano Seconds", expectedMicro, highDate.get(ChronoField.MICRO_OF_SECOND));
	}


	@Test
	public void test_getDateTimeFromXmlGC_null() {

		LocalDateTime expected = null;
		LocalDateTime actual = DateTimeUtil.getLocalDateTimeFromXmlGC(null);
		assertEquals("LocalDateTime from XMLGC", expected, actual);
	}

	@Test
	public void test_getDateTimeFromXmlGC() {

		BigInteger expectedYear = BigInteger.valueOf(YEAR);
		int expectedMonth = 4;
		int expectedDate = 1;
		int expectedHour = 3;
		int expectedMinute = 21;
		int expectedSecond = 42;
		BigDecimal expectedFractional = null;
		int expectedFractionInt = 0;
		int expectedTimezone = 4;

		XMLGregorianCalendar xmlGC = dfInstance.newXMLGregorianCalendar(expectedYear, expectedMonth, expectedDate, expectedHour, 
				expectedMinute, expectedSecond, expectedFractional, expectedTimezone);

		LocalDateTime actual = DateTimeUtil.getLocalDateTimeFromXmlGC(xmlGC);

		assertEquals("Year", expectedYear.intValue(), actual.getYear());
		assertEquals("Month", expectedMonth, actual.getMonthValue());
		assertEquals("Date", expectedDate, actual.getDayOfMonth());
		assertEquals("Hour", expectedHour, actual.getHour());
		assertEquals("Minute", expectedMinute, actual.getMinute());
		assertEquals("Second", expectedSecond, actual.getSecond());
		assertEquals("Nano Fractional Seconds", expectedFractionInt, actual.get(ChronoField.NANO_OF_SECOND));
		assertEquals("Micro Fractional Seconds", expectedFractionInt, actual.get(ChronoField.MICRO_OF_SECOND));
		assertEquals("Milli Fractional Seconds", expectedFractionInt, actual.get(ChronoField.MILLI_OF_SECOND));
	}

	@Test
	public void test_getDateTimeFromXmlGC_Nanos() {

		BigInteger expectedYear = BigInteger.valueOf(YEAR);
		int expectedMonth = 4;
		int expectedDate = 1;
		int expectedHour = 3;
		int expectedMinute = 21;
		int expectedSecond = 42;
		BigDecimal expectedFractional = new BigDecimal(".123456789");
		int expectedTimezone = 4;

		XMLGregorianCalendar xmlGC = dfInstance.newXMLGregorianCalendar(expectedYear, expectedMonth, expectedDate, expectedHour, 
				expectedMinute, expectedSecond, expectedFractional, expectedTimezone);

		LocalDateTime actual = DateTimeUtil.getLocalDateTimeFromXmlGC(xmlGC);

		assertEquals("Year", expectedYear.intValue(), actual.getYear());
		assertEquals("Month", expectedMonth, actual.getMonthValue());
		assertEquals("Date", expectedDate, actual.getDayOfMonth());
		assertEquals("Hour", expectedHour, actual.getHour());
		assertEquals("Minute", expectedMinute, actual.getMinute());
		assertEquals("Second", expectedSecond, actual.getSecond());
		assertEquals("Nano Seconds", (expectedFractional.multiply(BigDecimal.valueOf(1000000000))).intValue(), actual.get(ChronoField.NANO_OF_SECOND));
	}


	@Test
	public void test_getDateTimeFromXmlGC_Micros() {

		BigInteger expectedYear = BigInteger.valueOf(YEAR);
		int expectedMonth = 4;
		int expectedDate = 1;
		int expectedHour = 3;
		int expectedMinute = 21;
		int expectedSecond = 42;
		BigDecimal expectedFractional = new BigDecimal(".123456");
		int expectedTimezone = 4;

		XMLGregorianCalendar xmlGC = dfInstance.newXMLGregorianCalendar(expectedYear, expectedMonth, expectedDate, expectedHour, 
				expectedMinute, expectedSecond, expectedFractional, expectedTimezone);

		LocalDateTime actual = DateTimeUtil.getLocalDateTimeFromXmlGC(xmlGC);

		assertEquals("Year", expectedYear.intValue(), actual.getYear());
		assertEquals("Month", expectedMonth, actual.getMonthValue());
		assertEquals("Date", expectedDate, actual.getDayOfMonth());
		assertEquals("Hour", expectedHour, actual.getHour());
		assertEquals("Minute", expectedMinute, actual.getMinute());
		assertEquals("Second", expectedSecond, actual.getSecond());
		assertEquals("Micro Seconds", (expectedFractional.multiply(BigDecimal.valueOf(1000000))).intValue(), actual.get(ChronoField.MICRO_OF_SECOND));
	}

	@Test
	public void test_getDateTimeFromXmlGC_Millis() {

		int expectedYear = YEAR;
		int expectedMonth = 4;
		int expectedDate = 1;
		int expectedHour = 3;
		int expectedMinute = 21;
		int expectedSecond = 42;
		int expectedFractional = 123;
		int expectedTimezone = 4;

		XMLGregorianCalendar xmlGC = dfInstance.newXMLGregorianCalendar(expectedYear, expectedMonth, expectedDate, expectedHour, 
				expectedMinute, expectedSecond, expectedFractional, expectedTimezone);

		LocalDateTime actual = DateTimeUtil.getLocalDateTimeFromXmlGC(xmlGC);

		assertEquals("Year", expectedYear, actual.getYear());
		assertEquals("Month", expectedMonth, actual.getMonthValue());
		assertEquals("Date", expectedDate, actual.getDayOfMonth());
		assertEquals("Hour", expectedHour, actual.getHour());
		assertEquals("Minute", expectedMinute, actual.getMinute());
		assertEquals("Second", expectedSecond, actual.getSecond());
		assertEquals("Milli Seconds", expectedFractional, actual.get(ChronoField.MILLI_OF_SECOND));
	}

	@Test
	public void test_getLocalDateFromXmlGC() {

		int expectedYear = YEAR;
		int expectedMonth = 7;
		int expectedDate = 2;
		int expectedTimezone = 4;
		XMLGregorianCalendar xmlGC = dfInstance.newXMLGregorianCalendarDate(expectedYear,expectedMonth, expectedDate, expectedTimezone);

		LocalDate actual = DateTimeUtil.getLocalDateFromXmlGC(xmlGC);

		assertEquals("Year", expectedYear, actual.getYear());
		assertEquals("Month", expectedMonth, actual.getMonthValue());
		assertEquals("Date", expectedDate, actual.getDayOfMonth());
	}

	@Test
	public void test_getLocalDateFromXmlGC_null() {

		LocalDate expected = null;
		XMLGregorianCalendar xmlGC = null;
		LocalDate actual = DateTimeUtil.getLocalDateFromXmlGC(xmlGC);
		assertEquals("LocalDate from null XMLGregorianCalendar", expected, actual);
	}


	@Test
	public void test_getXMLGregorianCalendar() {

		int expectedYear = YEAR;
		int expectedMonth = 6;
		int expectedDate = 11;
		int expectedHour = 22;
		int expectedMinute = 33;
		int expectedSecond = 44;
		int expectedFractional = 123;

		LocalDateTime localDT = LocalDateTime.of(expectedYear, expectedMonth, expectedDate, expectedHour, expectedMinute, 
				expectedSecond, (expectedFractional * 1000000));

		XMLGregorianCalendar actual = DateTimeUtil.getXMLGregorianCalendar(localDT);

		assertEquals("Year", expectedYear, actual.getYear());
		assertEquals("Month", expectedMonth, actual.getMonth());
		assertEquals("Date", expectedDate, actual.getDay());
		assertEquals("Hour", expectedHour, actual.getHour());
		assertEquals("Minute", expectedMinute, actual.getMinute());
		assertEquals("Second", expectedSecond, actual.getSecond());
		assertEquals("Milli Seconds", expectedFractional, actual.getMillisecond());	
	}

	@Test
	public void test_getXMLGregorianCalendar_noonTime() {

		int expectedYear = YEAR;
		int expectedMonth = 6;
		int expectedDate = 11;
		int expectedHour = 12;
		int expectedMinute = 0;
		int expectedSecond = 0;
		int expectedFractional = 0;

		LocalDateTime localDT = LocalDateTime.of(expectedYear, expectedMonth, expectedDate, expectedHour, expectedMinute, 
				expectedSecond, (expectedFractional * 1000000));
		
		XMLGregorianCalendar actual = DateTimeUtil.getXMLGregorianCalendar(localDT);

		assertEquals("Year", expectedYear, actual.getYear());
		assertEquals("Month", expectedMonth, actual.getMonth());
		assertEquals("Date", expectedDate, actual.getDay());
		assertEquals("Hour", expectedHour, actual.getHour());
		assertEquals("Minute", expectedMinute, actual.getMinute());
		assertEquals("Second", expectedSecond, actual.getSecond());
		assertEquals("Milli Seconds", expectedFractional, actual.getMillisecond());	
	}
	
	@Test
	public void test_getXMLGregorianCalendar_NoMillis() {

		int expectedYear = YEAR;
		int expectedMonth = 6;
		int expectedDate = 11;
		int expectedHour = 22;
		int expectedMinute = 33;
		int expectedSecond = 44;
		int expectedFractional = 0;

		LocalDateTime localDT = LocalDateTime.of(expectedYear, expectedMonth, expectedDate, expectedHour, expectedMinute, 
				expectedSecond, expectedFractional);

		XMLGregorianCalendar actual = DateTimeUtil.getXMLGregorianCalendar(localDT);

		assertEquals("Year", expectedYear, actual.getYear());
		assertEquals("Month", expectedMonth, actual.getMonth());
		assertEquals("Date", expectedDate, actual.getDay());
		assertEquals("Hour", expectedHour, actual.getHour());
		assertEquals("Minute", expectedMinute, actual.getMinute());
		assertEquals("Second", expectedSecond, actual.getSecond());
		assertEquals("Milli Seconds", expectedFractional, actual.getMillisecond());

	}

	@Test
	public void test_getXMLGregorianCalendar_Null() {

		XMLGregorianCalendar expected = null;
		LocalDateTime localDateTime = null;
		XMLGregorianCalendar actual = DateTimeUtil.getXMLGregorianCalendar(localDateTime);
		assertEquals("Year", expected, actual);		
	}

	@Test
	public void test_getXMLGregorianCalendar_LocalDate() {

		int expectedYear = YEAR;
		int expectedMonth = 12;
		int expectedDate = 31;
		LocalDate localDate = LocalDate.of(expectedYear, expectedMonth, expectedDate);
		XMLGregorianCalendar actual = DateTimeUtil.getXMLGregorianCalendar(localDate);
		assertEquals("Year", expectedYear, actual.getYear());
		assertEquals("Month", expectedMonth, actual.getMonth());
		assertEquals("Date", expectedDate, actual.getDay());		
	}

	@Test
	public void test_getXMLGregorianCalendar_LocalDate_null() {

		XMLGregorianCalendar expected = null;
		LocalDate localDate = null;
		XMLGregorianCalendar actual = DateTimeUtil.getXMLGregorianCalendar(localDate);
		assertEquals("Year", expected, actual);	
	}

	@Test
	public void test_getXMLGregorianCalendar_String_Seconds() {

		DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
		BigDecimal expectedMillis = null;
		LocalDateTime expected = LocalDateTime.now();
		String strDateTime = expected.format(dtf);

		XMLGregorianCalendar actual = DateTimeUtil.getXMLGregorianCalendar(strDateTime);

		assertEquals("Year", expected.getYear(), actual.getYear());
		assertEquals("Month", expected.getMonthValue(), actual.getMonth());
		assertEquals("Date", expected.getDayOfMonth(), actual.getDay());
		assertEquals("Hour", expected.getHour(), actual.getHour());
		assertEquals("Minute", expected.getMinute(), actual.getMinute());
		assertEquals("Second", expected.getSecond(), actual.getSecond());
		assertEquals("Milli Seconds", expectedMillis, actual.getFractionalSecond());
	}

	@Test
	public void test_getXMLGregorianCalendar_String_TimeZone() {

		//2016-06-02T14:26:52-04:00
		DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssXXX");
		BigDecimal expectedMillis = null;
		LocalDateTime expected = LocalDateTime.now();
		ZonedDateTime zDT = expected.atZone(ZoneId.systemDefault());
		String strDateTime = zDT.format(dtf);
		XMLGregorianCalendar actual = DateTimeUtil.getXMLGregorianCalendar(strDateTime);

		assertEquals("Year", expected.getYear(), actual.getYear());
		assertEquals("Month", expected.getMonthValue(), actual.getMonth());
		assertEquals("Date", expected.getDayOfMonth(), actual.getDay());
		assertEquals("Hour", expected.getHour(), actual.getHour());
		assertEquals("Minute", expected.getMinute(), actual.getMinute());
		assertEquals("Second", expected.getSecond(), actual.getSecond());
		assertEquals("Milli Seconds", expectedMillis, actual.getFractionalSecond());
	}

	@Test
	public void test_getXMLGregorianCalendar_String_Millis() {

		DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS");
		LocalDateTime expected = LocalDateTime.now();
		String strDateTime = expected.format(dtf);

		XMLGregorianCalendar actual = DateTimeUtil.getXMLGregorianCalendar(strDateTime);

		assertEquals("Year", expected.getYear(), actual.getYear());
		assertEquals("Month", expected.getMonthValue(), actual.getMonth());
		assertEquals("Date", expected.getDayOfMonth(), actual.getDay());
		assertEquals("Hour", expected.getHour(), actual.getHour());
		assertEquals("Minute", expected.getMinute(), actual.getMinute());
		assertEquals("Second", expected.getSecond(), actual.getSecond());
		BigDecimal actualMillisBD = actual.getFractionalSecond().multiply(new BigDecimal("1000"));
		assertEquals("Micro Seconds", expected.get(ChronoField.MILLI_OF_SECOND), actualMillisBD.intValue());
	}

	@Test
	public void test_getXMLGregorianCalendar_String_Micros() {

		DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSSSS");
		LocalDateTime expected = LocalDateTime.now();
		expected = expected.plusNanos(123456);
		String strDateTime = expected.format(dtf);

		XMLGregorianCalendar actual = DateTimeUtil.getXMLGregorianCalendar(strDateTime);

		assertEquals("Year", expected.getYear(), actual.getYear());
		assertEquals("Month", expected.getMonthValue(), actual.getMonth());
		assertEquals("Date", expected.getDayOfMonth(), actual.getDay());
		assertEquals("Hour", expected.getHour(), actual.getHour());
		assertEquals("Minute", expected.getMinute(), actual.getMinute());
		assertEquals("Second", expected.getSecond(), actual.getSecond());
		BigDecimal actualMicrosBD = actual.getFractionalSecond().multiply(new BigDecimal("1000000"));
		assertEquals("Micro Seconds", expected.get(ChronoField.MICRO_OF_SECOND), actualMicrosBD.intValue());
	}

	@Test
	public void test_getXMLGregorianCalendar_String_Nanos() {

		DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSSSSSSS");
		LocalDateTime expected = LocalDateTime.now();
		expected = expected.plusNanos(123456789);
		String strDateTime = expected.format(dtf);

		XMLGregorianCalendar actual = DateTimeUtil.getXMLGregorianCalendar(strDateTime);

		assertEquals("Year", expected.getYear(), actual.getYear());
		assertEquals("Month", expected.getMonthValue(), actual.getMonth());
		assertEquals("Date", expected.getDayOfMonth(), actual.getDay());
		assertEquals("Hour", expected.getHour(), actual.getHour());
		assertEquals("Minute", expected.getMinute(), actual.getMinute());
		assertEquals("Second", expected.getSecond(), actual.getSecond());
		BigDecimal actualNanosBD = actual.getFractionalSecond().multiply(new BigDecimal("1000000000"));
		assertEquals("Micro Seconds", expected.get(ChronoField.NANO_OF_SECOND), actualNanosBD.intValue());
	}


	@Test
	public void test_getXMLGregorianCalendar_String_null() {

		LocalDateTime expected = null;
		String strDateTime = null;
		XMLGregorianCalendar actual = DateTimeUtil.getXMLGregorianCalendar(strDateTime);
		assertEquals("null StrDAte", expected, actual);	
	}


	@Test
	public void test_getLocalDateFromSqlDate() {
		
		int expectedYear = YEAR;
		int expectedMonth = 7;
		int expectedDate = 22;
		Date sqlDate = Date.valueOf(expectedYear + "-" + expectedMonth + "-" + expectedDate);
		LocalDate actual = DateTimeUtil.getLocalDateFromSqlDate(sqlDate);
		assertEquals("Year", expectedYear, actual.getYear());
		assertEquals("Month", expectedMonth, actual.getMonthValue());
		assertEquals("Date", expectedDate, actual.getDayOfMonth());
	}
	
	@Test
	public void test_getLocalDateFromSql_null() {
		
		LocalDate expected = null;
		Date sqlDate = null;
		LocalDate actual = DateTimeUtil.getLocalDateFromSqlDate(sqlDate);
		assertEquals("Null sql date", expected, actual);	
	}

	
	@Test
	public void test_getLocalDateFromSqlTimestamp() {
		
		int expectedYear = YEAR;
		int expectedMonth = 7;
		int expectedDate = 22;
		int expectedHour = 3;
		int expectedMinute = 21;
		int expectedSecond = 42;
		int expectedFractional = 123456;
		Timestamp sqlTS = Timestamp.valueOf(expectedYear + "-" + expectedMonth + "-" + expectedDate + " " + 
		expectedHour + ":" + expectedMinute + ":" + expectedSecond + "." + expectedFractional);
		LocalDateTime actual = DateTimeUtil.getLocalDateTimeFromSqlTimestamp(sqlTS);
		assertEquals("Year", expectedYear, actual.getYear());
		assertEquals("Month", expectedMonth, actual.getMonthValue());
		assertEquals("Date", expectedDate, actual.getDayOfMonth());
		assertEquals("Hour", expectedHour, actual.getHour());
		assertEquals("Minute", expectedMinute, actual.getMinute());
		assertEquals("Second", expectedSecond, actual.getSecond());
		assertEquals("Micro Fractional Seconds", expectedFractional * 1000, actual.getNano());
	}
	
	@Test
	public void test_getLocalDateFromSqlTimestamp_null() {
		
		LocalDate expected = null;
		Timestamp sqlTS = null;
		LocalDateTime actual = DateTimeUtil.getLocalDateTimeFromSqlTimestamp(sqlTS);
		assertEquals("Null sql date", expected, actual);	
	}


}
