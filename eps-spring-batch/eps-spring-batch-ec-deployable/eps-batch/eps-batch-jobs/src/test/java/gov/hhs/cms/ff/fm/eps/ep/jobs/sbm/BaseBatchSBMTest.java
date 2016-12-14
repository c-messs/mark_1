package gov.hhs.cms.ff.fm.eps.ep.jobs.sbm;

import java.io.File;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.List;

import org.junit.Test;
import org.springframework.format.datetime.DateFormatter;

import junit.framework.TestCase;

public class BaseBatchSBMTest extends TestCase {

	
	protected final LocalDate DATE = LocalDate.now();
	protected final LocalDateTime DATETIME = LocalDateTime.now();
	protected final int YEAR = DATE.getYear();
	protected final SimpleDateFormat sdf = new SimpleDateFormat("'D'yyMMdd'.T'HHmmssSSS");
	protected final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("'D'yyMMdd'.T'HHmmssSSS");
	protected final DateFormatter dateFormatter = new DateFormatter("yyyy-MM-dd HH:mm:ss.SSS");
	
	protected final LocalDate JAN_1 = LocalDate.of(YEAR, 1, 1);
	protected final LocalDate JAN_15 = LocalDate.of(YEAR, 1, 15);
	protected final LocalDate JAN_31 = LocalDate.of(YEAR, 1, 31);
	protected final LocalDate FEB_1 = LocalDate.of(YEAR, 2, 1);
	protected final LocalDate FEB_MAX = DATE.with(TemporalAdjusters.lastDayOfMonth());
	protected final LocalDate MAR_1 = LocalDate.of(YEAR, 3, 1);
	protected final LocalDate MAR_31 = LocalDate.of(YEAR, 3, 31);
	protected final LocalDate APR_1 = LocalDate.of(YEAR, 4, 1);
	
	protected final LocalDate NOV_1 = LocalDate.of(YEAR, 11, 1);
	protected final LocalDate DEC_1 = LocalDate.of(YEAR, 12, 1);
	protected final LocalDate DEC_31 = LocalDate.of(YEAR, 12, 31);
	
	protected final LocalDateTime JAN_1_1am = LocalDateTime.of(YEAR, 1, 1, 1, 0, 0, 111111000);
	protected final LocalDateTime FEB_1_2am = LocalDateTime.of(YEAR, 2, 1, 2, 0, 0, 222222000);
	protected final LocalDateTime MAR_1_3am = LocalDateTime.of(YEAR, 3, 1, 3, 0, 0, 333333000);
	protected final LocalDateTime APR_1_4am = LocalDateTime.of(YEAR, 4, 1, 4, 0, 0, 444444000);
	protected final LocalDateTime JUN_1_1am = LocalDateTime.of(YEAR, 6, 1, 1, 0, 0, 666666000);
	

	
	protected void assertFolderFileList(File folder, List<String> expectedList, int expectedCount) {

		String[] actualArr = folder.list();

		assertEquals("Number of files in folder:  " + folder.getName(), expectedCount, actualArr.length);

		for (int i = 0; i < actualArr.length; ++i) {
			String privateFileNm = actualArr[i].substring(0,  actualArr[i].indexOf("_ZIP"));
			assertEquals(folder.getName() + " File " + (i + 1), expectedList.get(i), privateFileNm);
		}
	}

	@Test
	public void test_nothing() {
		assertTrue("Yep, it tests nothing", true);
	}

}
