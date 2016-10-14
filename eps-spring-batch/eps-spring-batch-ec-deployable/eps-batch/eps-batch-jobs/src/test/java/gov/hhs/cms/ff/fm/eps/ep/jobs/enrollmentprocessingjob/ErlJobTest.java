package gov.hhs.cms.ff.fm.eps.ep.jobs.enrollmentprocessingjob;

import gov.hhs.cms.ff.fm.eps.ep.EPSConstants;
import gov.hhs.cms.ff.fm.eps.ep.jobs.enrollmentprocessingjob.data.util.ErlTestFileGenerator;

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
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;



/**
 * @author j.radziewski
 *
 */
@ContextConfiguration(locations={"/test-batch-application-context.xml"})
@RunWith(SpringJUnit4ClassRunner.class)
public class ErlJobTest extends BaseBatchTest {

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

	@Test
	public void test_Multiple_Manifests() throws Exception {

		int manifestCnt = 3;
		int berCnt = 1;
		int bemCnt = 1;
		int memCnt = 1;
		// Make both an INITIAL and EFFECTUATION per ExchangePolicyId.
		boolean makeBoth = false;
		List<String> expectedArchiveFileNmList = new ArrayList<String>();
		String manifestFileNum = null;

		int expectedInvalid = 0;
		// Since all should process, the archive count will be same as manifest count.
		int expectedArchive = manifestCnt;
		int expectedSkipped = 0;
		int expectedProcessed = manifestCnt * berCnt * bemCnt;

		ErlTestFileGenerator fileGenerator = new ErlTestFileGenerator(berCnt, bemCnt, memCnt, makeBoth, manifestDir, inputDir);
		for (int i = 0; i < manifestCnt; ++i) {
			manifestFileNum = fileGenerator.makeFiles(); 
			expectedArchiveFileNmList.add(manifestFileNum);
		}
		try {
			//Launch Injestion job
			JobExecution jobExInjestion = jobLauncherTestUtils.launchJob(jobParametersInjestion);
			jobIdInjestion = jobExInjestion.getJobId();
			assertJobCompleted(jobExInjestion);

			// If fails, more than likely the ErlTestFileGenerator needs to be updated.
			assertEquals("No 'invalid' files put in directory: " + invalidDir.getName(), expectedInvalid, invalidDir.list().length);
			// If fails, review logs or query BATCHTRANSMSG TRANSMSGSKIPREASONTYPECD and/or TRANSMSGSKIPREASONDESC to 
			// determine reason for skip (ApplicationException).
			assertEquals("No 'skipped' files put in directory: " + skippedDir.getName(), expectedSkipped, skippedDir.list().length);
			assertEquals("No 'private' files remain in directory: " + privateDir.getName(), 0, privateDir.list().length);
			assertEquals("Correct number of 'processed' files put in directory: " + processedDir.getName(), expectedProcessed, processedDir.list().length);
			assertArchiveFileList(expectedArchiveFileNmList, expectedArchive);


			Integer bemIdxCntAfterJob1 = getJdbc().queryForObject("SELECT Count(*) FROM DAILYBEMINDEXER WHERE EXCHANGETYPECD='FFM' and INGESTJOBID ="+jobIdInjestion, Integer.class);
			assertEquals("Bem Index is populated after file extract job", fileGenerator.getExcectedBemCount(), bemIdxCntAfterJob1.intValue());

			JobExecution jobExProcessor = jobLauncherTestUtils.launchJob(jobParametersProcessor);
			jobIdProcessor = jobExProcessor.getJobId();
			assertJobCompleted(jobExProcessor);	

			assertMemberVersionCount(jobIdProcessor, fileGenerator.getExpectedMemberVersionCount());

			assertPolicyVersionCount(jobIdProcessor, fileGenerator.getExcectedBemCount());

			bemIdxCntAfterJob1 = getJdbc().queryForObject("SELECT Count(*) FROM DAILYBEMINDEXER WHERE EXCHANGETYPECD='FFM' and INGESTJOBID ="+jobIdInjestion, Integer.class);
			assertEquals("Bem Index is populated after file extract job", 0, bemIdxCntAfterJob1.intValue());
			
			Integer stagingPlanLockAfterJob = getJdbc().queryForObject("SELECT Count(*) FROM STAGINGPLANLOCK WHERE BATCHID = " + jobIdProcessor, Integer.class);
			assertEquals("staging PlanLock is cleaned up after 'bemPostCleanup' step", 0, stagingPlanLockAfterJob.intValue());

		} finally {

			if(jobIdProcessor != null) {
				deleteTestData(jobIdProcessor);
			}
			if(jobIdInjestion != null) {
				deleteSetUpData(jobIdInjestion);
			}
			getJdbc().execute("DELETE BATCHRUNCONTROL WHERE JOB_INSTANCE_ID IS NULL");
			for (String manifestNum : expectedArchiveFileNmList) {
				getJdbc().execute("DELETE BATCHRUNCONTROL WHERE CREATEBY = 'ERL_INGESTION' AND BATCHRUNCONTROLID = " + manifestNum);				
			}
		}
	}


	@Test
	public void test_Multiple_Manifests_PAET_Y() throws Exception {

		int manifestCnt = 3;
		int berCnt = 1;
		int bemCnt = 1;
		int memCnt = 1;
		// Make both an INITIAL and EFFECTUATION per ExchangePolicyId.
		boolean makeBoth = false;
		List<String> expectedArchiveFileNmList = new ArrayList<String>();
		List<String> expectedManifestDirNmList = new ArrayList<String>();
		String manifestFileNum = null;

		int expectedInvalid = 0;
		// Since the second manifest of 3 will be set to PAET=Y, 2 will be processed and archived.
		int expectedArchive = 2;
		int expectedSkipped = 0;
		int expectedProcessed = 2;
		// the 3rd set/file will remain in input.
		int expectedInput = 1;
		int expectedPolicyCnt = expectedProcessed * bemCnt;
		int expectedMemberCnt = expectedPolicyCnt * memCnt;
		int expectedBemIdxCnt = 2;
		int expectedBemIdxCntAfterJob = 0;

		String paet = EPSConstants.N;

		ErlTestFileGenerator fileGenerator = new ErlTestFileGenerator(berCnt, bemCnt, memCnt, makeBoth, manifestDir, inputDir);

		for (int i = 0; i < manifestCnt; ++i) {
			// make the second set of files/manifest with PAET=Y
			paet = (i == 1) ? EPSConstants.Y : EPSConstants.N;
			manifestFileNum = fileGenerator.makeFiles(paet); 
			expectedArchiveFileNmList.add(manifestFileNum);
			if (i == (1 + 1)) {
				// set aside for later assertion.  The "next" input directory after the PAET=Y will remain in input.
				expectedManifestDirNmList.add(manifestFileNum);
			}
		}
		try {
			//Launch Injestion job
			JobExecution jobExInjestion = jobLauncherTestUtils.launchJob(jobParametersInjestion);
			jobIdInjestion = jobExInjestion.getJobId();
			assertJobCompleted(jobExInjestion);

			// If fails, more than likely the ErlTestFileGenerator needs to be updated.
			assertEquals("No 'invalid' files put in directory: " + invalidDir.getName(), expectedInvalid, invalidDir.list().length);
			// If fails, review logs or query BATCHTRANSMSG TRANSMSGSKIPREASONTYPECD and/or TRANSMSGSKIPREASONDESC to 
			// determine reason for skip (ApplicationException).
			assertEquals("No 'skipped' files put in directory: " + skippedDir.getName(), expectedSkipped, skippedDir.list().length);
			assertEquals("No 'private' files remain in directory: " + privateDir.getName(), 0, privateDir.list().length);

			assertEquals("Correct number of 'processed' files put in directory: " + processedDir.getName(), expectedProcessed, processedDir.list().length);
			assertArchiveFileList(expectedArchiveFileNmList, expectedArchive);

			assertEquals("Correct number 'input' manifest directories remain in " + inputDir.getName() + ".", expectedInput, inputDir.list().length);

			assertInputDirectory(expectedManifestDirNmList);

			Integer bemIdxCntAfterJob1 = getJdbc().queryForObject("SELECT Count(*) FROM DAILYBEMINDEXER WHERE EXCHANGETYPECD='FFM' and INGESTJOBID ="+jobIdInjestion, Integer.class);
			assertEquals("Bem Index is populated after file extract job", expectedBemIdxCnt, bemIdxCntAfterJob1.intValue());

			JobExecution jobExProcessor = jobLauncherTestUtils.launchJob(jobParametersProcessor);
			jobIdProcessor = jobExProcessor.getJobId();
			assertJobCompleted(jobExProcessor);	

			assertMemberVersionCount(jobIdProcessor, expectedMemberCnt);

			assertPolicyVersionCount(jobIdProcessor, expectedPolicyCnt);

			bemIdxCntAfterJob1 = getJdbc().queryForObject("SELECT Count(*) FROM DAILYBEMINDEXER WHERE EXCHANGETYPECD='FFM' and INGESTJOBID ="+jobIdInjestion, Integer.class);
			assertEquals("Bem Index is populated after file extract job", expectedBemIdxCntAfterJob, bemIdxCntAfterJob1.intValue());
			
			Integer stagingPlanLockAfterJob = getJdbc().queryForObject("SELECT Count(*) FROM STAGINGPLANLOCK WHERE BATCHID = " + jobIdProcessor, Integer.class);
			assertEquals("staging PlanLock is cleaned up after 'bemPostCleanup' step", 0, stagingPlanLockAfterJob.intValue());

		} finally {

			if(jobIdProcessor != null) {
				deleteTestData(jobIdProcessor);
			}
			if(jobIdInjestion != null) {
				deleteSetUpData(jobIdInjestion);
			}
			getJdbc().execute("DELETE BATCHRUNCONTROL WHERE JOB_INSTANCE_ID IS NULL");
			for (String manifestNum : expectedArchiveFileNmList) {
				getJdbc().execute("DELETE BATCHRUNCONTROL WHERE CREATEBY = 'ERL_INGESTION' AND BATCHRUNCONTROLID = " + manifestNum);				
			}
		}
	}

	@Test
	public void test_Multiple_Manifests_JobStatus_Failure() throws Exception {

		int manifestCnt = 3;
		int berCnt = 1;
		int bemCnt = 1;
		int memCnt = 1;
		// Make both an INITIAL and EFFECTUATION per ExchangePolicyId.
		boolean makeBoth = false;
		List<String> expectedArchiveFileNmList = new ArrayList<String>();
		List<String> expectedManifestDirNmList = new ArrayList<String>();
		String manifestFileNum = null;

		int expectedInvalid = 0;
		// Since the second manifest of 3 will be set to PAET=Y, 2 will be processed and archived.
		int expectedArchive = 2;
		int expectedSkipped = 0;
		int expectedProcessed = 2;
		// the 3rd set/file will remain in input.
		int expectedInput = 1;
		int expectedPolicyCnt = expectedProcessed * bemCnt;
		int expectedMemberCnt = expectedPolicyCnt * memCnt;
		int expectedBemIdxCnt = 2;
		int expectedBemIdxCntAfterJob = 0;

		ErlTestFileGenerator fileGenerator = new ErlTestFileGenerator(berCnt, bemCnt, memCnt, makeBoth, manifestDir, inputDir);

		for (int i = 0; i < manifestCnt; ++i) {
			manifestFileNum = fileGenerator.makeFiles(); 
			// The last manifest file is expected to remain in manifest for this test.
			if (i == (manifestCnt - 1)) {
				expectedManifestDirNmList.add(manifestFileNum);
			} else {
				// All others should go to archive.
				expectedArchiveFileNmList.add(manifestFileNum);
			}
		}

		// update the last manifest file from "SUCCESS" to "FAIL"
		String manifestFileNm = "Manifest-" + manifestFileNum + ".txt";
		File manifestFileToFail = new File(manifestDir + File.separator + manifestFileNm);
		List<String> lines = FileUtils.readLines(manifestFileToFail);
		lines.remove(lines.size() - 1);
		lines.add("JobStatus=FAIL");
		FileUtils.writeLines(manifestFileToFail, lines, "\r\n", false);

		try {
			//Launch Injestion job
			JobExecution jobExInjestion = jobLauncherTestUtils.launchJob(jobParametersInjestion);
			jobIdInjestion = jobExInjestion.getJobId();
			assertJobCompleted(jobExInjestion);

			// If fails, more than likely the ErlTestFileGenerator needs to be updated.
			assertEquals("No 'invalid' files put in directory: " + invalidDir.getName(), expectedInvalid, invalidDir.list().length);
			// If fails, review logs or query BATCHTRANSMSG TRANSMSGSKIPREASONTYPECD and/or TRANSMSGSKIPREASONDESC to 
			// determine reason for skip (ApplicationException).
			assertEquals("No 'skipped' files put in directory: " + skippedDir.getName(), expectedSkipped, skippedDir.list().length);
			assertEquals("No 'private' files remain in directory: " + privateDir.getName(), 0, privateDir.list().length);

			assertEquals("Correct number of 'processed' files put in directory: " + processedDir.getName(), expectedProcessed, processedDir.list().length);
			assertArchiveFileList(expectedArchiveFileNmList, expectedArchive);

			assertEquals("Correct number 'input' manifest directories remain in " + inputDir.getName() + ".", expectedInput, inputDir.list().length);

			assertInputDirectory(expectedManifestDirNmList);

			Integer bemIdxCntAfterJob1 = getJdbc().queryForObject("SELECT Count(*) FROM DAILYBEMINDEXER WHERE EXCHANGETYPECD='FFM' and INGESTJOBID ="+jobIdInjestion, Integer.class);
			assertEquals("Bem Index is populated after file extract job", expectedBemIdxCnt, bemIdxCntAfterJob1.intValue());

			JobExecution jobExProcessor = jobLauncherTestUtils.launchJob(jobParametersProcessor);
			jobIdProcessor = jobExProcessor.getJobId();
			assertJobCompleted(jobExProcessor);	

			assertMemberVersionCount(jobIdProcessor, expectedMemberCnt);

			assertPolicyVersionCount(jobIdProcessor, expectedPolicyCnt);

			bemIdxCntAfterJob1 = getJdbc().queryForObject("SELECT Count(*) FROM DAILYBEMINDEXER WHERE EXCHANGETYPECD='FFM' and INGESTJOBID ="+jobIdInjestion, Integer.class);
			assertEquals("Bem Index is populated after file extract job", expectedBemIdxCntAfterJob, bemIdxCntAfterJob1.intValue());
			
			Integer stagingPlanLockAfterJob = getJdbc().queryForObject("SELECT Count(*) FROM STAGINGPLANLOCK WHERE BATCHID = " + jobIdProcessor, Integer.class);
			assertEquals("staging PlanLock is cleaned up after 'bemPostCleanup' step", 0, stagingPlanLockAfterJob.intValue());

		} finally {

			if(jobIdProcessor != null) {
				deleteTestData(jobIdProcessor);
			}
			if(jobIdInjestion != null) {
				deleteSetUpData(jobIdInjestion);
			}
			getJdbc().execute("DELETE BATCHRUNCONTROL WHERE JOB_INSTANCE_ID IS NULL");
			for (String manifestNum : expectedArchiveFileNmList) {
				getJdbc().execute("DELETE BATCHRUNCONTROL WHERE CREATEBY = 'ERL_INGESTION' AND BATCHRUNCONTROLID = " + manifestNum);				
			}
		}
	}


}
