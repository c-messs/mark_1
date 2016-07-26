package gov.hhs.cms.ff.fm.eps.rap.dao;

import gov.hhs.cms.ff.fm.eps.rap.domain.BatchProcessLog;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import junit.framework.TestCase;

import org.joda.time.DateTime;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.transaction.annotation.Transactional;


@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={ "classpath:/rap-data-config.xml", "classpath:/test-context-data.xml" })
@TransactionConfiguration(transactionManager = "transactionManager", defaultRollback = true)
@Transactional
public class BatchProcessDAOImplTest extends TestCase {

	private final Long JOB_ID = Long.valueOf(99999);
	private final String USER = "BatchProcDaoTest";
	private final String JOB_NM = USER + "Job";
	private final String BATCH_BUSINESS_ID = USER + JOB_ID.toString();
	private final DateTime DATETIME = new DateTime();
	private final String STARTED = "STARTED";
	private final String STOPPED = "STOPPED";

	@Autowired
	BatchProcessDAO batchProcDao;

	@Autowired
	private JdbcTemplate jdbcTemplate;
	
	@Autowired
	private ApplicationContext context;

	@Before
	public void setUp()  {
		batchProcDao.setJdbcTemplate(jdbcTemplate);
	}


	/**
	 * Tests GetJobInstanceForBatchProcess for 3 scenarios with input arg as:
	 * - invalid
	 * - null
	 * - valid
	 * @throws SQLException
	 */
	@Test
	public void testGetJobInstanceForBatchProcess() throws SQLException {

		String actualBatchBusinessId = batchProcDao.getJobInstanceForBatchProcess("XXXXXXXXXX");
		Assert.assertNull("BatchBusinessId" , actualBatchBusinessId);

		actualBatchBusinessId = batchProcDao.getJobInstanceForBatchProcess(null);
		Assert.assertNull("BatchBusinessId" , actualBatchBusinessId);

		String expectedBatchBusinessId = BATCH_BUSINESS_ID;
		insertBatchProcessLog(expectedBatchBusinessId, JOB_ID, JOB_NM, STARTED);

		actualBatchBusinessId = batchProcDao.getJobInstanceForBatchProcess(BATCH_BUSINESS_ID);
		Assert.assertEquals("BatchBusinessId" , BATCH_BUSINESS_ID, actualBatchBusinessId);
	}
	
	
	@Test
	public void testGetJobInstanceForBatchProcess_MissingProperty() throws SQLException {

		String key = "PP.BatchProcessLog.Select";
	
		Properties props = context.getBean("ppCommonQueryProps", Properties.class);
		String origValue = props.getProperty(key);
		// set to empty for test
		props.setProperty(key, "");
		
		String actualBatchBusinessId = batchProcDao.getJobInstanceForBatchProcess(BATCH_BUSINESS_ID);
		
		Assert.assertNull("BatchBusinessId" , actualBatchBusinessId);
		// Set back to original so other tests will not fail.
		props.setProperty(key, origValue);
	}

	@Test
	public void testGetJobsWithStartedStatus() {

		String expectedJobNm1 = JOB_NM + "_1";
		String expectedJobNm2 = JOB_NM + "_2";

		String otherJobNm3 = JOB_NM + "_3";
		String otherJobNm4 = JOB_NM + "_4";

		insertBatchProcessLog(USER + 1, JOB_ID, expectedJobNm1, STARTED);
		insertBatchProcessLog(USER + 2, JOB_ID, expectedJobNm2, STARTED);
		insertBatchProcessLog(USER + 3, JOB_ID, otherJobNm3, STARTED);
		insertBatchProcessLog(USER + 4, JOB_ID, otherJobNm4, STOPPED);

		String jobNames = "'" + expectedJobNm1 + "','" + expectedJobNm2 + "'";

		List<BatchProcessLog> actualBatchProcLogList = batchProcDao.getJobsWithStartedStatus(jobNames);

		Assert.assertEquals("BatchProcessLog list size", 2, actualBatchProcLogList.size());

		BatchProcessLog actualBatchProcLog1 = actualBatchProcLogList.get(0);
		BatchProcessLog actualBatchProcLog2 = actualBatchProcLogList.get(1);

		Assert.assertEquals("1: BatchBusinessId" , USER + 1, actualBatchProcLog1.getBatchBusinessId());
		Assert.assertEquals("1: JobNm" , expectedJobNm1, actualBatchProcLog1.getJobNm());
		Assert.assertEquals("1: JobStatusCd" , STARTED, actualBatchProcLog1.getJobStatusCd());

		Assert.assertEquals("2: BatchBusinessId" , USER + 2, actualBatchProcLog2.getBatchBusinessId());
		Assert.assertEquals("2: JobNm" , expectedJobNm2, actualBatchProcLog2.getJobNm());
		Assert.assertEquals("2: JobStatusCd" , STARTED, actualBatchProcLog2.getJobStatusCd());

	}

	@Test
	public void testGetNextBatchBusinessIdSeq() {

		int expectedSeq = 17;

		insertBatchProcessLog(USER + "015", JOB_ID, JOB_NM, STARTED);
		insertBatchProcessLog(USER + "016", JOB_ID, JOB_NM, STARTED);

		int actualSeq = batchProcDao.getNextBatchBusinessIdSeq(USER);

		Assert.assertEquals("Next Id sequence", expectedSeq, actualSeq);
	}

	@Test
	public void testInsertBatchProcessLog() throws SQLException {
		
		BatchProcessLog batchProcessLog = new BatchProcessLog();
		batchProcessLog.setBatchBusinessId(BATCH_BUSINESS_ID);
		batchProcessLog.setJobId(JOB_ID);
		batchProcessLog.setJobNm(JOB_NM);
		batchProcessLog.setJobStatusCd(STOPPED);
		batchProcessLog.setRunByNm(USER);
		
		batchProcDao.insertBatchProcessLog(batchProcessLog);
		
		List<Map<String, Object>> rowList = jdbcTemplate.queryForList("SELECT * FROM BATCHPROCESSLOG WHERE " +
				"BATCHBUSINESSID = '" + BATCH_BUSINESS_ID + "' AND RUNBYNM = '" + USER + "'");

		Assert.assertEquals("actual BatchProcessLog row insert", 1, rowList.size());
		
		Map<String, Object> actualRow1 = rowList.get(0);
		
		Assert.assertTrue("BatchProcessLog record contains JobId", actualRow1.containsValue(new BigDecimal(JOB_ID)));
		Assert.assertTrue("BatchProcessLog record contains JobNm", actualRow1.containsValue(JOB_NM));
		Assert.assertTrue("BatchProcessLog record contains JobStatusCd ", actualRow1.containsValue(STOPPED));		
	}
	
	@Test
	public void testUpdateBatchProcessLog() throws SQLException {
		
		// Insert a record to update
		insertBatchProcessLog(BATCH_BUSINESS_ID, JOB_ID, JOB_NM, STARTED);
		
		BatchProcessLog batchProcessLog = new BatchProcessLog();
		batchProcessLog.setJobId(JOB_ID);
		batchProcessLog.setEndDateTime(DATETIME);
		batchProcessLog.setLastpolicymaintstartDateTime(DATETIME.minusDays(1));
		batchProcessLog.setJobStatusCd(STOPPED);
		
		batchProcDao.updateBatchProcessLog(batchProcessLog);
		
		// Confirm it was updated
		List<Map<String, Object>> rowList = jdbcTemplate.queryForList("SELECT * FROM BATCHPROCESSLOG WHERE " +
				"BATCHBUSINESSID = '" + BATCH_BUSINESS_ID + "' AND JOBID = '" + JOB_ID + "'");

		Assert.assertEquals("actual BatchProcessLog row insert", 1, rowList.size());
		
		Map<String, Object> actualRow1 = rowList.get(0);
		
		Assert.assertTrue("BatchProcessLog record contains EndDateTime", actualRow1.containsValue(new Timestamp(DATETIME.getMillis())));
		Assert.assertTrue("BatchProcessLog record contains JobStatusCd", actualRow1.containsValue(STOPPED));		

	}
	

	/**
	 * Inserts a minimal parent record into BATCHPROCESSLOG.
	 */
	private void insertBatchProcessLog(String batchBusId, Long id, String name, String status)  {

		String sql ="INSERT INTO BATCHPROCESSLOG(BATCHBUSINESSID, JOBID, JOBNM, JOBSTATUSCD) " +
				"VALUES ('" + batchBusId + "'," + id + ", '" + name + "', '" + status + "')";
		jdbcTemplate.execute(sql);
	}

}
