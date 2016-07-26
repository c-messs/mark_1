package gov.hhs.cms.ff.fm.eps.ep.jobs.enrollmentprocessingjob;


import gov.hhs.cms.ff.fm.eps.ep.EPSConstants;
import gov.hhs.cms.ff.fm.eps.ep.enums.ProcessedToDbInd;
import gov.hhs.cms.ff.fm.eps.ep.jobs.enrollmentprocessingjob.data.util.ErlTestFileGenerator;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.math.RandomUtils;
import org.joda.time.DateTime;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobInstance;
import org.springframework.batch.core.JobParameter;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.format.datetime.DateFormatter;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.support.DirtiesContextTestExecutionListener;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.test.context.transaction.TransactionalTestExecutionListener;

/**
 * @author girish.padmanabhan
 *
 */
@ContextConfiguration(locations={"/test-batch-application-context.xml"})
@DirtiesContext(classMode = ClassMode.AFTER_EACH_TEST_METHOD)
@RunWith(SpringJUnit4ClassRunner.class)
@TestExecutionListeners({DependencyInjectionTestExecutionListener.class, 
	TransactionalTestExecutionListener.class, DirtiesContextTestExecutionListener.class })
@TransactionConfiguration(transactionManager = "transactionManager", defaultRollback = true)
public class ErlIngestionJobTest extends BaseBatchTest {

	private static final String ROOT_FILE_PATH = "./UnitTestDirs/erl";
	private static final String TEST_PATH_INPUT = "./UnitTestDirs/erl/input/10";

	@Autowired
	private JobLauncherTestUtils  jobLauncherTestUtils;

	private JobParameters jobParameters;
	private JobInstance jobInstance;
	private Long jobIdGen = null;

	@Before
	public void setup() throws IOException {

		makeERLDirectories();

		ErlTestFileGenerator fileGenerator = new ErlTestFileGenerator();
		fileGenerator.writeManifestFile(manifestDir, 10, 1, EPSConstants.N);

		final DateFormatter dateFormatter = new DateFormatter("yyyy-MM-dd HH:mm:ss.SSS");
		final Map<String, JobParameter> params = new LinkedHashMap<String, JobParameter>();
		params.put("source", new JobParameter("ffm"));
		params.put("program", new JobParameter("Junit test"));
		params.put("timestamp", new JobParameter(dateFormatter.print(new Date(), Locale.US)));
		jobParameters = new JobParameters(params);

		JobExecution jobExecution = new JobExecution(9999L, jobParameters);
		ExecutionContext ctx = new ExecutionContext();
		ctx.putString("BATCH_RUNCONTROL_ID", "10");
		jobExecution.setExecutionContext(ctx);

		jobIdGen = RandomUtils.nextLong();
		jobInstance = new JobInstance(jobIdGen, "enrollmentProcessingBatchJob");
		jobExecution.setJobInstance(jobInstance);

		// Clean up from any other jobs that might have failed.
		clearDailyBEMIndexer();
	}


	@After
	public void tearDown() {
		FileUtils.deleteQuietly(new File(ROOT_FILE_PATH));
	}

	/**
	 * This method will test the successful ingestion of erl records, inserting the policies to EPS.
	 * 
	 * Launches ERL ingest job which reads the manifest file and the BER file and inserts the data into EPS tables.
	 * 
	 * @throws Exception
	 */
	@Test
	public void testJob_ErlIngestion() throws Exception {
		//Create the extract files containing the Bem Transaction
		writeErlExtractFile("ERL.TEST.D141218.T163855502.T.11165VA0020999");

		// clean up prior and after incase previous test failed.
		getJdbc().execute("DELETE FROM BATCHPROCESSLOG WHERE JOBNM='enrollmentProcessingBatchJob' AND JOBSTATUSCD = 'STARTED'");
		Long jobId1 = null;
		Long jobId = null;
		String stateCd = "ZZ";
		String exchangePolicyId = "TESTERLINGESTIONJOB";
		String hiosId = exchangePolicyId.substring(0, 5);
		int versionNum = 1;

		try {
			//Launch the job
			JobExecution run = jobLauncherTestUtils.launchJob(jobParameters);
			jobId1 = run.getJobId();

			Integer bemIdxCntAfterJob1 = getJdbc().queryForObject("SELECT Count(*) FROM DAILYBEMINDEXER WHERE EXCHANGETYPECD='FFM' AND SUBSCRIBERSTATECD = 'ZZ'", Integer.class);
			assertEquals("Bem Index is populated after file extract job", 1, bemIdxCntAfterJob1.intValue());

			final Map<String, JobParameter> params = jobParameters.getParameters();
			params.put("jobType", new JobParameter("processor"));
			params.put("timestamp", new JobParameter(new DateTime().toString()));
			jobParameters = new JobParameters(params);

			run = jobLauncherTestUtils.launchJob(jobParameters);

			jobId = run.getJobId();
			System.out.println("Job id: "+ jobId);

			assertJobCompleted(run);	
			
			String ingestStatus = getJdbc().queryForObject("SELECT INGESTCOMPLETEIND FROM BATCHRUNCONTROL WHERE BATCHRUNCONTROLID=10", String.class);
			assertEquals("Ingest Status after Manifest is processed", "Y", ingestStatus);


			Integer policyCntAfterJob = getJdbc().queryForObject("SELECT Count(*) FROM POLICYVERSION WHERE CREATEBY='"+jobId+"'", Integer.class);
			assertEquals("Policy count after job", 1, policyCntAfterJob.intValue());

			String policyIdAfterJob = getJdbc().queryForObject("SELECT EXCHANGEPOLICYID FROM POLICYVERSION WHERE CREATEBY='"+jobId+"'", String.class);
			assertEquals("EPS Policy id after job", exchangePolicyId, policyIdAfterJob);

			String stateCdAfterJob = getJdbc().queryForObject("SELECT SUBSCRIBERSTATECD FROM POLICYVERSION WHERE CREATEBY='"+jobId+"'", String.class);
			assertEquals("EPS Policy state code after job", stateCd, stateCdAfterJob);

			String hiosIdAfterJob = getJdbc().queryForObject("SELECT ISSUERHIOSID FROM POLICYVERSION WHERE CREATEBY='"+jobId+"'", String.class);
			assertEquals("EPS Policy hios id after job", hiosId, hiosIdAfterJob);

			Integer versionIdAfterJob = getJdbc().queryForObject("SELECT SOURCEVERSIONID FROM POLICYVERSION WHERE CREATEBY='"+jobId+"'", Integer.class);
			assertEquals("EPS Policy source version Id after job", versionNum, versionIdAfterJob.intValue());

			String procToDbCd = getJdbc().queryForObject("SELECT PROCESSEDTODBSTATUSTYPECD FROM BATCHTRANSMSG WHERE BATCHID = " + jobId, String.class);
			assertEquals("ProcessedToDbIndicator for transMsg: ", ProcessedToDbInd.Y.getValue(), procToDbCd);

			Integer bemIdxCntAfterJob = getJdbc().queryForObject("SELECT Count(*) FROM DAILYBEMINDEXER WHERE EXCHANGETYPECD='FFM' AND SUBSCRIBERSTATECD = 'ZZ'", Integer.class);
			assertEquals("Bem Index is cleaned up after 'bemPostCleanup' step", 0, bemIdxCntAfterJob.intValue());
			
			Integer stagingPlanLockAfterJob = getJdbc().queryForObject("SELECT Count(*) FROM STAGINGPLANLOCK WHERE BATCHID = " + jobId, Integer.class);
			assertEquals("staging PlanLock is cleaned up after 'bemPostCleanup' step", 0, stagingPlanLockAfterJob.intValue());


		} finally {
			if(jobId != null) deleteTestData(jobId);
			if(jobId1 != null) deleteTestData(jobId1);
			if(jobIdGen != null) deleteTestData(jobIdGen);
			getJdbc().execute("DELETE BATCHRUNCONTROL WHERE JOB_INSTANCE_ID IS NULL");
			getJdbc().execute("DELETE FROM DAILYBEMINDEXER WHERE EXCHANGETYPECD='FFM' AND SUBSCRIBERSTATECD = 'ZZ'");
		}
	}

	/**
	 * This method will test the successful ingestion of erl records, inserting the policies to EPS.
	 * 
	 * Launches ERL ingest job which reads the manifest file and the BER file and inserts the data into EPS tables.
	 * 
	 * @throws Exception
	 */
	@Test
	public void testJob_ErlIngestion_Proration() throws Exception {
		//Create the extract files containing the Bem Transaction
		writeErlExtractFile("ERLPRORATN.TEST.D150101.T102222546.T.65192ID0050008");

		// clean up prior and after incase previous test failed.
		getJdbc().execute("DELETE FROM DAILYBEMINDEXER WHERE EXCHANGETYPECD='FFM' AND SUBSCRIBERSTATECD = 'ZZ'");
		getJdbc().execute("DELETE FROM BATCHPROCESSLOG WHERE JOBNM='enrollmentProcessingBatchJob' AND JOBSTATUSCD = 'STARTED'");
		Long jobId1 = null;
		Long jobId = null;
		String stateCd = "ZZ";
		String exchangePolicyId = "TESTERLINGESTION_PRORATION";
		String hiosId = exchangePolicyId.substring(0, 5);
		int versionNum = 1;

		try {
			//Launch the job
			JobExecution run = jobLauncherTestUtils.launchJob(jobParameters);
			jobId1 = run.getJobId();

			Integer bemIdxCntAfterJob1 = getJdbc().queryForObject("SELECT Count(*) FROM DAILYBEMINDEXER WHERE EXCHANGETYPECD='FFM' AND SUBSCRIBERSTATECD = 'ZZ'", Integer.class);
			assertEquals("Bem Index is populated after file extract job", 1, bemIdxCntAfterJob1.intValue());

			final Map<String, JobParameter> params = jobParameters.getParameters();
			params.put("jobType", new JobParameter("processor"));
			params.put("timestamp", new JobParameter(new DateTime().toString()));
			jobParameters = new JobParameters(params);

			run = jobLauncherTestUtils.launchJob(jobParameters);

			jobId = run.getJobId();
			System.out.println("Job id: "+ jobId);
			
			String ingestStatus = getJdbc().queryForObject("SELECT INGESTCOMPLETEIND FROM BATCHRUNCONTROL WHERE BATCHRUNCONTROLID=10", String.class);
			assertEquals("Ingest Status after Manifest is processed", "Y", ingestStatus);

			assertEquals("Job Status", ExitStatus.COMPLETED, run.getExitStatus());
			assertEquals("Batch Status value is COMPLETED", BatchStatus.COMPLETED, run.getStatus());


			Integer policyCntAfterJob = getJdbc().queryForObject("SELECT Count(*) FROM POLICYVERSION WHERE CREATEBY='"+jobId+"'", Integer.class);
			assertEquals("Policy count after job", 1, policyCntAfterJob.intValue());

			String policyIdAfterJob = getJdbc().queryForObject("SELECT EXCHANGEPOLICYID FROM POLICYVERSION WHERE CREATEBY='"+jobId+"'", String.class);
			assertEquals("EPS Policy id after job", exchangePolicyId, policyIdAfterJob);

			String policyVersionIdAfterJob = getJdbc().queryForObject("SELECT POLICYVERSIONID FROM POLICYVERSION WHERE CREATEBY='"+jobId+"'", String.class);
			assertNotNull("EPS Policy version id after job", policyVersionIdAfterJob);

			Integer policyIPremiumCountAfterJob = getJdbc().queryForObject("SELECT Count(*) FROM POLICYPREMIUM WHERE CREATEBY='"+jobId+"'", Integer.class);
			assertTrue("EPS Policy premium record count > 1", policyIPremiumCountAfterJob > 1);

			String stateCdAfterJob = getJdbc().queryForObject("SELECT SUBSCRIBERSTATECD FROM POLICYVERSION WHERE CREATEBY='"+jobId+"'", String.class);
			assertEquals("EPS Policy state code after job", stateCd, stateCdAfterJob);

			String hiosIdAfterJob = getJdbc().queryForObject("SELECT ISSUERHIOSID FROM POLICYVERSION WHERE CREATEBY='"+jobId+"'", String.class);
			assertEquals("EPS Policy hios id after job", hiosId, hiosIdAfterJob);

			Integer versionIdAfterJob = getJdbc().queryForObject("SELECT SOURCEVERSIONID FROM POLICYVERSION WHERE CREATEBY='"+jobId+"'", Integer.class);
			assertEquals("EPS Policy source version Id after job", versionNum, versionIdAfterJob.intValue());

			String procToDbCd = getJdbc().queryForObject("SELECT PROCESSEDTODBSTATUSTYPECD FROM BATCHTRANSMSG WHERE BATCHID = " + jobId, String.class);
			assertEquals("ProcessedToDbIndicator for transMsg: ", ProcessedToDbInd.Y.getValue(), procToDbCd);

			Integer bemIdxCntAfterJob = getJdbc().queryForObject("SELECT Count(*) FROM DAILYBEMINDEXER WHERE EXCHANGETYPECD='FFM' AND SUBSCRIBERSTATECD = 'ZZ'", Integer.class);
			assertEquals("Bem Index is cleaned up after 'bemPostCleanup' step", 0, bemIdxCntAfterJob.intValue());

			Integer stagingPlanLockAfterJob = getJdbc().queryForObject("SELECT Count(*) FROM STAGINGPLANLOCK WHERE BATCHID = " + jobId, Integer.class);
			assertEquals("staging PlanLock is cleaned up after 'bemPostCleanup' step", 0, stagingPlanLockAfterJob.intValue());
			
		} finally {
			if(jobId != null) deleteTestData(jobId);
			if(jobId1 != null) deleteTestData(jobId1);
			if(jobIdGen != null) deleteTestData(jobIdGen);
			getJdbc().execute("DELETE BATCHRUNCONTROL WHERE JOB_INSTANCE_ID IS NULL");
			getJdbc().execute("DELETE FROM DAILYBEMINDEXER WHERE EXCHANGETYPECD='FFM' AND SUBSCRIBERSTATECD = 'ZZ'");
		}
	}

	/**
	 * This method will test the skipping of erl records, when any of policyid, state code, hios id, version num is missing.
	 * 
	 * Launches ERL ingest job which reads the manifest file and the BER file and inserts the skip record into EPS tables.
	 * 
	 * @throws Exception
	 */
	@Test
	public void testJob_ErlIngestion_ExchangePolicyId_Null() throws Exception {
		//Create the extract files containing the Bem Transaction
		writeErlExtractFile("ERLSKIP.POLIDNULL.D150101.T102222546.T.65192ID0050008");

		// clean up prior and after incase previous test failed.
		getJdbc().execute("DELETE FROM DAILYBEMINDEXER WHERE EXCHANGETYPECD='FFM' AND SUBSCRIBERSTATECD = 'ZZ'");
		getJdbc().execute("DELETE FROM BATCHPROCESSLOG WHERE JOBNM='enrollmentProcessingBatchJob' AND JOBSTATUSCD = 'STARTED'");
		Long jobId1 = null;
		Long jobId = null;

		try {
			//Launch the job
			JobExecution run = jobLauncherTestUtils.launchJob(jobParameters);
			jobId1 = run.getJobId();

			Integer bemIdxCntAfterJob1 = getJdbc().queryForObject("SELECT Count(*) FROM DAILYBEMINDEXER WHERE EXCHANGETYPECD='FFM' AND SUBSCRIBERSTATECD = 'ZZ'", Integer.class);
			assertEquals("Bem Index is populated after file extract job", 1, bemIdxCntAfterJob1.intValue());

			final Map<String, JobParameter> params = jobParameters.getParameters();
			params.put("jobType", new JobParameter("processor"));
			params.put("timestamp", new JobParameter(new DateTime().toString()));
			jobParameters = new JobParameters(params);

			run = jobLauncherTestUtils.launchJob(jobParameters);

			jobId = run.getJobId();
			System.out.println("Job id: "+ jobId);

			assertEquals("Job Status", ExitStatus.COMPLETED, run.getExitStatus());
			assertEquals("Batch Status value is COMPLETED", BatchStatus.COMPLETED, run.getStatus());


			String ingestStatus = getJdbc().queryForObject("SELECT INGESTCOMPLETEIND FROM BATCHRUNCONTROL WHERE BATCHRUNCONTROLID=10", String.class);
			assertEquals("Ingest Status after Manifest is processed", "Y", ingestStatus);
			
			Integer policyCntAfterJob = getJdbc().queryForObject("SELECT Count(*) FROM POLICYVERSION WHERE CREATEBY='"+jobId+"'", Integer.class);
			assertEquals("Policy count after job", 0, policyCntAfterJob.intValue());

			String procToDbCd = getJdbc().queryForObject("SELECT PROCESSEDTODBSTATUSTYPECD FROM BATCHTRANSMSG WHERE BATCHID = " + jobId, String.class);
			assertEquals("ProcessedToDbIndicator for transMsg: ", ProcessedToDbInd.S.getValue(), procToDbCd);

			Integer bemIdxCntAfterJob = getJdbc().queryForObject("SELECT Count(*) FROM DAILYBEMINDEXER WHERE EXCHANGETYPECD='FFM' AND SUBSCRIBERSTATECD = 'ZZ'", Integer.class);
			assertEquals("Bem Index is cleaned up after 'bemPostCleanup' step", 0, bemIdxCntAfterJob.intValue());

			Integer stagingPlanLockAfterJob = getJdbc().queryForObject("SELECT Count(*) FROM STAGINGPLANLOCK WHERE BATCHID = " + jobId, Integer.class);
			assertEquals("staging PlanLock is cleaned up after 'bemPostCleanup' step", 0, stagingPlanLockAfterJob.intValue());
			
		} finally {
			if(jobId != null) deleteTestData(jobId);
			if(jobId1 != null) deleteTestData(jobId1);
			if(jobIdGen != null) deleteTestData(jobIdGen);
			getJdbc().execute("DELETE BATCHRUNCONTROL WHERE JOB_INSTANCE_ID IS NULL");
			getJdbc().execute("DELETE FROM DAILYBEMINDEXER WHERE EXCHANGETYPECD='FFM' AND SUBSCRIBERSTATECD = 'ZZ'");
		}
	}
	
	/**
	 * This method will test the skipping of erl records, when Extraction Status code in the Bem has value of 1.
	 * 
	 * Launches ERL ingest job which reads the manifest file and the BER file and inserts the skip record into EPS tables.
	 * 
	 * @throws Exception
	 */
	@Test
	public void testJob_ErlIngestion_Skip_ExtractionError() throws Exception {
		//Create the extract files containing the Bem Transaction
		writeErlExtractFile("ERLSKIP.EXTRNERR.D141218.T163855502.T.11165VA0020999");

		// clean up prior and after incase previous test failed.
		getJdbc().execute("DELETE FROM DAILYBEMINDEXER WHERE EXCHANGETYPECD='FFM' AND SUBSCRIBERSTATECD = 'ZZ'");
		getJdbc().execute("DELETE FROM BATCHPROCESSLOG WHERE JOBNM='enrollmentProcessingBatchJob' AND JOBSTATUSCD = 'STARTED'");
		Long jobId1 = null;
		Long jobId = null;

		try {
			//Launch the job
			JobExecution run = jobLauncherTestUtils.launchJob(jobParameters);
			jobId1 = run.getJobId();

			Integer bemIdxCntAfterJob1 = getJdbc().queryForObject("SELECT Count(*) FROM DAILYBEMINDEXER WHERE EXCHANGETYPECD='FFM' AND SUBSCRIBERSTATECD = 'ZZ'", Integer.class);
			assertEquals("Bem Index is populated after file extract job", 1, bemIdxCntAfterJob1.intValue());

			final Map<String, JobParameter> params = jobParameters.getParameters();
			params.put("jobType", new JobParameter("processor"));
			params.put("timestamp", new JobParameter(new DateTime().toString()));
			jobParameters = new JobParameters(params);

			run = jobLauncherTestUtils.launchJob(jobParameters);

			jobId = run.getJobId();
			System.out.println("Job id: "+ jobId);

			assertEquals("Job Status", ExitStatus.COMPLETED, run.getExitStatus());
			assertEquals("Batch Status value is COMPLETED", BatchStatus.COMPLETED, run.getStatus());


			String ingestStatus = getJdbc().queryForObject("SELECT INGESTCOMPLETEIND FROM BATCHRUNCONTROL WHERE BATCHRUNCONTROLID=10", String.class);
			assertEquals("Ingest Status after Manifest is processed", "Y", ingestStatus);
			
			Integer policyCntAfterJob = getJdbc().queryForObject("SELECT Count(*) FROM POLICYVERSION WHERE CREATEBY='"+jobId+"'", Integer.class);
			assertEquals("Policy count after job", 0, policyCntAfterJob.intValue());

			String procToDbCd = getJdbc().queryForObject("SELECT PROCESSEDTODBSTATUSTYPECD FROM BATCHTRANSMSG WHERE BATCHID = " + jobId, String.class);
			assertEquals("ProcessedToDbIndicator for transMsg: ", ProcessedToDbInd.S.getValue(), procToDbCd);

			Integer bemIdxCntAfterJob = getJdbc().queryForObject("SELECT Count(*) FROM DAILYBEMINDEXER WHERE EXCHANGETYPECD='FFM' AND SUBSCRIBERSTATECD = 'ZZ'", Integer.class);
			assertEquals("Bem Index is cleaned up after 'bemPostCleanup' step", 0, bemIdxCntAfterJob.intValue());

			Integer stagingPlanLockAfterJob = getJdbc().queryForObject("SELECT Count(*) FROM STAGINGPLANLOCK WHERE BATCHID = " + jobId, Integer.class);
			assertEquals("staging PlanLock is cleaned up after 'bemPostCleanup' step", 0, stagingPlanLockAfterJob.intValue());
			
		} finally {
			if(jobId != null) deleteTestData(jobId);
			if(jobId1 != null) deleteTestData(jobId1);
			if(jobIdGen != null) deleteTestData(jobIdGen);
			getJdbc().execute("DELETE BATCHRUNCONTROL WHERE JOB_INSTANCE_ID IS NULL");
			getJdbc().execute("DELETE FROM DAILYBEMINDEXER WHERE EXCHANGETYPECD='FFM' AND SUBSCRIBERSTATECD = 'ZZ'");
		}
	}


	private void writeErlExtractFile(String fileName) throws IOException {
		File inputFile = new ClassPathResource("testfiles"+File.separator+fileName).getFile();
		File destFile = new File(TEST_PATH_INPUT + File.separator + fileName);
		System.out.println("extract file: " + destFile.getPath());

		FileUtils.copyFile(inputFile, destFile);
	}
}
