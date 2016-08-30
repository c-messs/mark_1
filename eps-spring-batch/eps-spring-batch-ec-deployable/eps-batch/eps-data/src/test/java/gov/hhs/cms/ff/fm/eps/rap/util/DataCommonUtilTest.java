package gov.hhs.cms.ff.fm.eps.rap.util;

import java.sql.Date;
import java.util.Calendar;
import java.util.GregorianCalendar;

import junit.framework.TestCase;

import org.joda.time.DateTime;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class DataCommonUtilTest extends TestCase {
	
	private final Calendar CAL_BASE = Calendar.getInstance();
	
	
	@Test
	public void testDataCommonUtil() {
		
		// Since not static, but methods are
		DataCommonUtil dataComUtil = new DataCommonUtil();
		assertNotNull("DataCommonUtil", dataComUtil);	
	}
	
	@Test
	public void testConvertToDateTime () {
		
		int expectedYear = CAL_BASE.get(Calendar.YEAR);
		int expectedMonth = CAL_BASE.get(Calendar.MONTH) + 1;
		int expectedDay = CAL_BASE.get(Calendar.DATE);
		
		Date expectedSqlDate = new Date(CAL_BASE.getTimeInMillis());
		
		DateTime actualDateTime = DataCommonUtil.convertToDateTime(expectedSqlDate);

		assertEquals("Joda DateTime Year", expectedYear, actualDateTime.getYear());
		assertEquals("Joda DateTime Month", expectedMonth, actualDateTime.getMonthOfYear());
		assertEquals("Joda DateTime Day", expectedDay, actualDateTime.getDayOfMonth());
	}
	
	
	@Test
	public void testConvertToDateTime_Null () {
		
		DateTime actualDateTime = DataCommonUtil.convertToDateTime(null);
		assertNull("Joda DateTime",actualDateTime);
	}
	
	
	@Test
	public void testConvertToDate () {
		
		int expectedYear = CAL_BASE.get(Calendar.YEAR);
		int expectedMonth = CAL_BASE.get(Calendar.MONTH) + 1;
		int expectedDate = CAL_BASE.get(Calendar.DATE);
		
		DateTime expectedDateTime = new DateTime(expectedYear, expectedMonth, expectedDate, 0, 0);
		
		Date actualDate = DataCommonUtil.convertToDate(expectedDateTime);
		Calendar cal = new GregorianCalendar();
        cal.setTime(actualDate);
        
		assertEquals("SQL Date", expectedDateTime.getYear() , cal.get(Calendar.YEAR));
		assertEquals("SQL Date", expectedDateTime.getMonthOfYear() , cal.get(Calendar.MONTH)+1);
		assertEquals("SQL Date", expectedDateTime.getDayOfMonth(), cal.get(Calendar.DAY_OF_MONTH));
	}


}
