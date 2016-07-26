package gov.hhs.cms.ff.fm.eps.ep.util;

import java.util.Date;
import java.util.GregorianCalendar;

import javax.xml.datatype.XMLGregorianCalendar;

import junit.framework.TestCase;

import org.joda.time.DateTime;
import org.junit.Test;
import org.springframework.test.util.ReflectionTestUtils;

public class EpsDateUtilsTest extends TestCase {

	private static final DateTime DATETIME = new DateTime();
	
	private GregorianCalendar gc = new GregorianCalendar();
	
	@Override
	public void setUp() throws Exception {

		gc.setTimeInMillis(DATETIME.getMillis());

	}
	

	@Test
	public void testStringToJavaUtilDate_Null() {
		
		Date expected = null;
		String strDate = null;
		Date actual = (Date) ReflectionTestUtils.invokeMethod(new EpsDateUtils(), "stringToJavaUtilDate", new Object[] {strDate});
		assertEquals("Java Util Date from string", expected, actual);
	}
	
	
	@Test
	public void testStringToJavaUtilDate_Exception() {
		
		Date expected = null;
		String strDate = "888888kkkkkkk8888888";
		Date actual = (Date) ReflectionTestUtils.invokeMethod(new EpsDateUtils(), "stringToJavaUtilDate", new Object[] {strDate});
		assertEquals("Java Util Date from string", expected, actual);
	}
	
	@Test
	public void testGetXMLGregorianCalendar_Null() {
		
		XMLGregorianCalendar expected = null;
		String strDate = null;
		XMLGregorianCalendar actual = EpsDateUtils.getXMLGregorianCalendar(strDate);
		assertEquals("XMLGregorianCalendar from string", expected, actual);
	}
	
	@Test
	public void testGetXMLGregorianCalendar_Empty() {
		
		XMLGregorianCalendar expected = null;
		String strDate = " ";
		XMLGregorianCalendar actual = EpsDateUtils.getXMLGregorianCalendar(strDate);
		assertEquals("XMLGregorianCalendar from string", expected, actual);
	}
	
	@Test
	public void test_getUtilDateFromXmlGC() {
		
		Date expected = null;
		XMLGregorianCalendar xmlGC = null;
		Date actual = (Date) ReflectionTestUtils.invokeMethod(new EpsDateUtils(), "getUtilDateFromXmlGC", new Object[] {xmlGC});
		assertEquals("Java Util Date from null XMLGregorianCalendar", expected, actual);
	}
	
	
}
