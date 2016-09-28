package gov.hhs.cms.ff.fm.eps.ep.jobs.enrollmentprocessingjob;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameter;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.support.DirtiesContextTestExecutionListener;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.test.context.transaction.TransactionalTestExecutionListener;

import gov.hhs.cms.ff.fm.eps.ep.jobs.enrollmentprocessingjob.data.util.ErlTestFileGenerator;
import gov.hhs.cms.ff.fm.eps.ep.jobs.enrollmentprocessingjob.data.util.TestDataUtil;


/**
 *
 *
 */
@ContextConfiguration(locations={"/test-batch-application-context.xml"})
@DirtiesContext(classMode = ClassMode.AFTER_EACH_TEST_METHOD)
@TestExecutionListeners({DependencyInjectionTestExecutionListener.class, 
	TransactionalTestExecutionListener.class, DirtiesContextTestExecutionListener.class })
@TransactionConfiguration(transactionManager = "transactionManager", defaultRollback = true)
@RunWith(SpringJUnit4ClassRunner.class)
public class ErlPAETCutOffFileIngestJobTest extends BaseBatchTest {

	@Autowired
	private JobLauncherTestUtils  jobLauncherTestUtils;

	private JobParameters jobParametersInjestion;
	private JobParameters jobParametersProcessor;

	private Long jobIdInjestion;
	private Long jobIdProcessor;

	@Before
	public void setup() throws IOException {

		makeERLDirectories();

		final Map<String, JobParameter> paramsInjestion = new LinkedHashMap<String, JobParameter>();
		paramsInjestion.put("source", new JobParameter("ffm"));
		paramsInjestion.put("jobType", new JobParameter(""));
		paramsInjestion.put("timestamp", new JobParameter(dateFormatter.print(new Date(), Locale.US)));
		jobParametersInjestion = new JobParameters(paramsInjestion);

		final Map<String, JobParameter> paramsProcessor = new LinkedHashMap<String, JobParameter>();
		paramsProcessor.put("source", new JobParameter("ffm"));
		paramsProcessor.put("jobType", new JobParameter("processor"));
		paramsProcessor.put("timestamp", new JobParameter(dateFormatter.print(new Date(), Locale.US)));
		jobParametersProcessor = new JobParameters(paramsProcessor);
		
		// Clean up from any other jobs that might have failed.
		clearDailyBEMIndexer();
	}

	@After
	public void tearDown() {

		FileUtils.deleteQuietly(new File(ROOT_FILE_PATH));
	}

	/**
	 * PAETCompletion Indicator = N Ingestion Rule..
	 * System shall check the PAETCompletion Indicator of >the previous job run and continue loading the 
	 * IPP records extracted for ingestion if the PAETCompletion indicator of previous FileIngest Job was N
	 */
//	@Test
//	public void testJob_ErlFileIngestion_PAETInd_N() throws Exception {
//
//		int berCnt = 1;
//		int bemCnt = 1;
//		int memCnt = 1;
//		// Make both an INITIAL and EFFECTUATION per ExchangePolicyId.
//		boolean makeBoth = false;
//		List<String> expectedArchiveFileNmList = new ArrayList<String>();
//		String manifestFileNum = null;
//
//		int expectedInvalid = 0;
//		int expectedArchive = 1;
//		int expectedSkipped = 0;
//		int expectedProcessed = expectedArchive * berCnt * bemCnt;
//
//		if (makeBoth) {
//			expectedProcessed *= 2;
//		}
//
//		ErlTestFileGenerator fileGenerator = new ErlTestFileGenerator(berCnt, bemCnt, memCnt, makeBoth, manifestDir, inputDir);
//		for (int i = 0; i < expectedArchive; ++i) {
//			manifestFileNum = fileGenerator.makeFiles(); 
//			expectedArchiveFileNmList.add(manifestFileNum);
//		}
//		
//		try {
//			insertBatchRunControl("N");
//			
//			//Launch Injestion job
//			JobExecution jobExInjestion = jobLauncherTestUtils.launchJob(jobParametersInjestion);
//			jobIdInjestion = jobExInjestion.getJobId();
//			assertJobCompleted(jobExInjestion);
//
//			// If fails, more than likely the ErlTestFileGenerator needs to be updated.
//			assertEquals("No 'invalid' files put in directory: " + invalidDir.getName(), expectedInvalid, invalidDir.list().length);
//			// If fails, review logs or query BATCHTRANSMSG TRANSMSGSKIPREASONTYPECD and/or TRANSMSGSKIPREASONDESC to 
//			// determine reason for skip (ApplicationException).
//			assertEquals("No 'skipped' files put in directory: " + skippedDir.getName(), expectedSkipped, skippedDir.list().length);
//			assertEquals("Correct number of files put in directory: " + processedDir.getName(), expectedProcessed, processedDir.list().length);
//			assertArchiveFileList(expectedArchiveFileNmList, expectedArchive);
//			assertEquals("No files remain in directory: " + privateDir.getName(), 0, privateDir.list().length);
//
//			Integer bemIdxCntAfterJob1 = getJdbc().queryForObject("SELECT Count(*) FROM DAILYBEMINDEXER WHERE EXCHANGETYPECD='FFM' and INGESTJOBID ="+jobIdInjestion, Integer.class);
//			assertEquals("Bem Index is populated after file extract job", fileGenerator.getExcectedBemCount(), bemIdxCntAfterJob1.intValue());
//
//			JobExecution jobExProcessor = jobLauncherTestUtils.launchJob(jobParametersProcessor);
//			jobIdProcessor = jobExProcessor.getJobId();
//			assertJobCompleted(jobExProcessor);	
//
//			assertMemberVersionCount(jobIdProcessor, fileGenerator.getExpectedMemberVersionCount());
//
//			assertPolicyVersionCount(jobIdProcessor, fileGenerator.getExcectedBemCount());
//			
//			bemIdxCntAfterJob1 = getJdbc().queryForObject("SELECT Count(*) FROM DAILYBEMINDEXER WHERE EXCHANGETYPECD='FFM' and INGESTJOBID ="+jobIdInjestion, Integer.class);
//			assertEquals("Bem Index is populated after file extract job", 0, bemIdxCntAfterJob1.intValue());
//
//		} finally {
//
//			if(jobIdProcessor != null) {
//				deleteTestData(jobIdProcessor);
//			}
//			if(jobIdInjestion != null) {
//				deleteSetUpData(jobIdInjestion);
//			}
//			getJdbc().execute("DELETE BATCHRUNCONTROL WHERE JOB_INSTANCE_ID IS NULL");
//			for (String manifestNum : expectedArchiveFileNmList) {
//				getJdbc().execute("DELETE BATCHRUNCONTROL WHERE CREATEBY = 'ERL_INGESTION' AND BATCHRUNCONTROLID = " + manifestNum);				
//			}
//		}
//	}
//	
	/**
	 * PAETCompletion Indicator = Y..
	 * System shall Query and Log the Max MaintenanceStartDateTime if the DailyBemIndexer queue is empty prior to loading the next set of records for ingestion
	 */
//	@Test
//	public void testJob_ErlFileIngestion_PAETInd_Y_MaxPolicyMSDT_exits() throws Exception {
//		
//		int berCnt = 1;
//		int bemCnt = 1;
//		int memCnt = 1;
//		// Make both an INITIAL and EFFECTUATION per ExchangePolicyId.
//		boolean makeBoth = false;
//		List<String> expectedArchiveFileNmList = new ArrayList<String>();
//		String manifestFileNum = null;
//
//		int expectedInvalid = 0;
//		int expectedArchive = 1;
//		int expectedSkipped = 0;
//		int expectedProcessed = expectedArchive * berCnt * bemCnt;
//
//		if (makeBoth) {
//			expectedProcessed *= 2;
//		}
//
//		ErlTestFileGenerator fileGenerator = new ErlTestFileGenerator(berCnt, bemCnt, memCnt, makeBoth, manifestDir, inputDir);
//		for (int i = 0; i < expectedArchive; ++i) {
//			manifestFileNum = fileGenerator.makeFiles(); 
//			expectedArchiveFileNmList.add(manifestFileNum);
//		}
//		
//		try {
//			insertBatchRunControl("Y");
//			
//			Long transMsgId = insertTransMsg(JOB_ID, "FFM");
//			insertPolicyVersion(transMsgId, LocalDateTime.now(), LocalDate.now(), LocalDate.now().plusYears(1), "ZZ", "EXCHPOLICY", "HIOSID", "planId", new Integer(1));
//			
//			//Launch Injestion job
//			JobExecution jobExInjestion = jobLauncherTestUtils.launchJob(jobParametersInjestion);
//			jobIdInjestion = jobExInjestion.getJobId();
//			assertJobCompleted(jobExInjestion);
//
//			// If fails, more than likely the ErlTestFileGenerator needs to be updated.
//			assertEquals("No 'invalid' files put in directory: " + invalidDir.getName(), expectedInvalid, invalidDir.list().length);
//			// If fails, review logs or query BATCHTRANSMSG TRANSMSGSKIPREASONTYPECD and/or TRANSMSGSKIPREASONDESC to 
//			// determine reason for skip (ApplicationException).
//			assertEquals("No 'skipped' files put in directory: " + skippedDir.getName(), expectedSkipped, skippedDir.list().length);
//			assertEquals("Correct number of files put in directory: " + processedDir.getName(), expectedProcessed, processedDir.list().length);
//			assertArchiveFileList(expectedArchiveFileNmList, expectedArchive);
//			assertEquals("No files remain in directory: " + privateDir.getName(), 0, privateDir.list().length);
//
//			Integer bemIdxCntAfterJob1 = getJdbc().queryForObject("SELECT Count(*) FROM DAILYBEMINDEXER WHERE EXCHANGETYPECD='FFM' and INGESTJOBID ="+jobIdInjestion, Integer.class);
//			assertEquals("Bem Index is populated after file extract job", fileGenerator.getExcectedBemCount(), bemIdxCntAfterJob1.intValue());
//
//			JobExecution jobExProcessor = jobLauncherTestUtils.launchJob(jobParametersProcessor);
//			jobIdProcessor = jobExProcessor.getJobId();
//			assertJobCompleted(jobExProcessor);	
//
//			assertMemberVersionCount(jobIdProcessor, fileGenerator.getExpectedMemberVersionCount());
//
//			assertPolicyVersionCount(jobIdProcessor, fileGenerator.getExcectedBemCount());
//			
//			bemIdxCntAfterJob1 = getJdbc().queryForObject("SELECT Count(*) FROM DAILYBEMINDEXER WHERE EXCHANGETYPECD='FFM' and INGESTJOBID ="+jobIdInjestion, Integer.class);
//			assertEquals("Bem Index is populated after file extract job", 0, bemIdxCntAfterJob1.intValue());
//
//		} finally {
//
//			if(jobIdProcessor != null) {
//				deleteTestData(jobIdProcessor);
//			}
//			if(jobIdInjestion != null) {
//				deleteSetUpData(jobIdInjestion);
//			}
//			deleteTestData(JOB_ID);
//			//deleteSetUpData(JOB_ID);
//	
//			getJdbc().execute("DELETE BATCHRUNCONTROL WHERE JOB_INSTANCE_ID IS NULL");
//			for (String manifestNum : expectedArchiveFileNmList) {
//				getJdbc().execute("DELETE BATCHRUNCONTROL WHERE CREATEBY = 'ERL_INGESTION' AND BATCHRUNCONTROLID = " + manifestNum);				
//			}
//		}
//	}

	/**
	 * PAETCompletion Indicator = Y..
	 * System shall Query and Log the Max MaintenanceStartDateTime if the DailyBemIndexer queue is empty prior to loading the next set of records for ingestion.
	 * If MSDT is null exit the FileIngestion job without loading the records corresponding to the manifest file
	 */
	@Test
	public void testJob_ErlFileIngestion_PAETInd_Y_MaxPolicyMSDT_null() throws Exception {
		
		int berCnt = 1;
		int bemCnt = 1;
		int memCnt = 1;
		// Make both an INITIAL and EFFECTUATION per ExchangePolicyId.
		boolean makeBoth = false;
		List<String> expectedArchiveFileNmList = new ArrayList<String>();

		int expectedInvalid = 0;
		int expectedArchive = 0;
		int expectedManifestCnt = 1;
		int expectedSkipped = 0;
		int expectedProcessed = expectedArchive * berCnt * bemCnt;

		if (makeBoth) {
			expectedProcessed *= 2;
		}

		ErlTestFileGenerator fileGenerator = new ErlTestFileGenerator(berCnt, bemCnt, memCnt, makeBoth, manifestDir, inputDir);
		for (int i = 0; i < expectedManifestCnt; ++i) {
			fileGenerator.makeFiles(); 
		}
		
		try {
			insertBatchRunControl("Y");
			
			//Launch Injestion job
			JobExecution jobExInjestion = jobLauncherTestUtils.launchJob(jobParametersInjestion);
			jobIdInjestion = jobExInjestion.getJobId();
			assertJobCompleted(jobExInjestion);

			// If fails, more than likely the ErlTestFileGenerator needs to be updated.
			assertEquals("No 'invalid' files put in directory: " + invalidDir.getName(), expectedInvalid, invalidDir.list().length);
			// If fails, review logs or query BATCHTRANSMSG TRANSMSGSKIPREASONTYPECD and/or TRANSMSGSKIPREASONDESC to 
			// determine reason for skip (ApplicationException).
			assertEquals("No 'skipped' files put in directory: " + skippedDir.getName(), expectedSkipped, skippedDir.list().length);
			assertEquals("Correct number of files put in directory: " + processedDir.getName(), expectedProcessed, processedDir.list().length);
			assertArchiveFileList(expectedArchiveFileNmList, expectedArchive);
			assertEquals("No files remain in directory: " + privateDir.getName(), 0, privateDir.list().length);

			assertEquals("Manifest files remain in directory: " + manifestDir.getName(), expectedManifestCnt, manifestDir.list().length);

			Integer bemIdxCntAfterJob1 = getJdbc().queryForObject("SELECT Count(*) FROM DAILYBEMINDEXER WHERE EXCHANGETYPECD='FFM' and INGESTJOBID ="+jobIdInjestion, Integer.class);
			assertEquals("Bem Index is populated after file extract job", 0, bemIdxCntAfterJob1.intValue());

		} finally {

			if(jobIdProcessor != null) {
				deleteTestData(jobIdProcessor);
			}
			if(jobIdInjestion != null) {
				deleteSetUpData(jobIdInjestion);
			}
			deleteTestData(JOB_ID);
			//deleteSetUpData(JOB_ID);
	
			getJdbc().execute("DELETE BATCHRUNCONTROL WHERE JOB_INSTANCE_ID IS NULL");
			for (String manifestNum : expectedArchiveFileNmList) {
				getJdbc().execute("DELETE BATCHRUNCONTROL WHERE CREATEBY = 'ERL_INGESTION' AND BATCHRUNCONTROLID = " + manifestNum);				
			}
		}
	}

	/**
	 * PAETCompletion Indicator = Y..
	 * System shall exit the job run and not load any new extracted records to the DailyBemIndexer if the queue is not empty and the PAETCompletion indicator of the previous job was Y
	 */
	@Test
	public void testJob_ErlFileIngestion_PAETInd_Y_BemIndex_NotEmpty() throws Exception {
		
		int berCnt = 1;
		int bemCnt = 1;
		int memCnt = 1;
		// Make both an INITIAL and EFFECTUATION per ExchangePolicyId.
		boolean makeBoth = false;
		List<String> expectedArchiveFileNmList = new ArrayList<String>();

		int expectedInvalid = 0;
		int expectedArchive = 0;
		int expectedManifestCnt = 1;
		int expectedSkipped = 0;
		int expectedProcessed = expectedArchive * berCnt * bemCnt;

		if (makeBoth) {
			expectedProcessed *= 2;
		}

		ErlTestFileGenerator fileGenerator = new ErlTestFileGenerator(berCnt, bemCnt, memCnt, makeBoth, manifestDir, inputDir);
		for (int i = 0; i < expectedManifestCnt; ++i) {
			fileGenerator.makeFiles(); 
		}
		
		try {
			insertBatchRunControl("Y");
			
			
			Long transMsgId = TestDataUtil.getRandomNumber(8);
			insertMinRecordToDailyBemIndexer(transMsgId);
			
			//Launch Injestion job
			JobExecution jobExInjestion = jobLauncherTestUtils.launchJob(jobParametersInjestion);
			jobIdInjestion = jobExInjestion.getJobId();
			assertJobCompleted(jobExInjestion);

			// If fails, more than likely the ErlTestFileGenerator needs to be updated.
			assertEquals("No 'invalid' files put in directory: " + invalidDir.getName(), expectedInvalid, invalidDir.list().length);
			// If fails, review logs or query BATCHTRANSMSG TRANSMSGSKIPREASONTYPECD and/or TRANSMSGSKIPREASONDESC to 
			// determine reason for skip (ApplicationException).
			assertEquals("No 'skipped' files put in directory: " + skippedDir.getName(), expectedSkipped, skippedDir.list().length);
			assertEquals("Correct number of files put in directory: " + processedDir.getName(), expectedProcessed, processedDir.list().length);
			assertArchiveFileList(expectedArchiveFileNmList, expectedArchive);
			assertEquals("No files remain in directory: " + privateDir.getName(), 0, privateDir.list().length);

			assertEquals("Manifest files remain in directory: " + manifestDir.getName(), expectedManifestCnt, manifestDir.list().length);

			Integer bemIdxCntAfterJob1 = getJdbc().queryForObject("SELECT Count(*) FROM DAILYBEMINDEXER WHERE EXCHANGETYPECD='FFM' and TRANSMSGID =" + transMsgId, Integer.class);
			assertEquals("Bem Index is retained after file extract job", 1, bemIdxCntAfterJob1.intValue());

		} finally {

			if(jobIdProcessor != null) {
				deleteTestData(jobIdProcessor);
			}
			if(jobIdInjestion != null) {
				deleteSetUpData(jobIdInjestion);
			}
			deleteTestData(JOB_ID);
			//deleteSetUpData(JOB_ID);
	
			getJdbc().execute("DELETE BATCHRUNCONTROL WHERE JOB_INSTANCE_ID IS NULL");
			for (String manifestNum : expectedArchiveFileNmList) {
				getJdbc().execute("DELETE BATCHRUNCONTROL WHERE CREATEBY = 'ERL_INGESTION' AND BATCHRUNCONTROLID = " + manifestNum);				
			}
			getJdbc().execute("DELETE FROM DAILYBEMINDEXER WHERE EXCHANGETYPECD='FFM'");
		}
	}

}
