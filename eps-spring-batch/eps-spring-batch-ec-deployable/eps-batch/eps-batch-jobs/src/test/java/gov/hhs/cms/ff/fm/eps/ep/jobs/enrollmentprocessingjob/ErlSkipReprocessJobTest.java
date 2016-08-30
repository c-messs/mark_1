package gov.hhs.cms.ff.fm.eps.ep.jobs.enrollmentprocessingjob;


import gov.cms.dsh.bem.BenefitEnrollmentMaintenanceType;
import gov.cms.dsh.bem.BenefitEnrollmentRequest;
import gov.cms.dsh.bem.FileInformationType;
import gov.hhs.cms.ff.fm.eps.ep.enums.ExchangeType;
import gov.hhs.cms.ff.fm.eps.ep.enums.PolicyStatus;
import gov.hhs.cms.ff.fm.eps.ep.enums.ProcessedToDbInd;
import gov.hhs.cms.ff.fm.eps.ep.jobs.enrollmentprocessingjob.data.util.TestDataUtil;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.Marshaller;
import javax.xml.namespace.QName;

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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
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
@ContextConfiguration(locations={"/test-batch-application-context.xml", "/test-context-eps.xml"})
@DirtiesContext(classMode = ClassMode.AFTER_EACH_TEST_METHOD)
@RunWith(SpringJUnit4ClassRunner.class)
@TestExecutionListeners({DependencyInjectionTestExecutionListener.class, 
	TransactionalTestExecutionListener.class, DirtiesContextTestExecutionListener.class })
@TransactionConfiguration(transactionManager = "transactionManager", defaultRollback = true)
public class ErlSkipReprocessJobTest extends BaseBatchTest {

	private static final String ROOT_FILE_PATH = "./UnitTestDirs/erl";
	private static final String TEST_PATH_MANIFEST = "./UnitTestDirs/erl/manifest";
	private static final String TEST_PATH_INPUT = "./UnitTestDirs/erl/input/10";
	private static final String TEST_PATH_INPUT_DIR = "./UnitTestDirs/erl/input/";
	private static final String TEST_PATH_IN_PROCESS = "./UnitTestDirs/erl/private";
	private static final String TEST_PATH_PROCESSED = "./UnitTestDirs/erl/processed";
	private static final String TEST_PATH_INVALID = "./UnitTestDirs/erl/invalid";
	private static final String TEST_PATH_MANIFEST_ARCHIVE = "./UnitTestDirs/erl/archive";


	// Creates a ZonedDateTime with micro seconds and a colon in the zone offset (XXX = -04:00).
	private final DateTimeFormatter DTF_MANIFEST_MICRO_SEC = new DateTimeFormatterBuilder().appendPattern("yyyy-MM-dd'T'HH:mm:ss")
			.appendFraction(ChronoField.MICRO_OF_SECOND, 0, 6, true).appendPattern("XXX").toFormatter();

	@Autowired
	private ApplicationContext context;	
	@Autowired
	private JobLauncherTestUtils  jobLauncherTestUtils;
	@Autowired
	private SkipBemListener skipBemListener;
	@Autowired
	private ErlManifestFileReader manifestFileReader;
	@Autowired
	private ErlManifestWriter erlManifestWriter;
	@Autowired
	private FileIngestionReader xsdValidator;
	@Autowired
	private FileIngestionWriter fileIngestionWriter;
	@Autowired
	private BEMExtractionProcessor bemExtractionProcessor;
	@Autowired
	private BEMExtractionWriter bemExtractionWriter;
	@Autowired
	BemProcessorListener bemProcessorListener;
	@Autowired
	FileMoveTasklet moveProcessedFilesTasklet;
	@Autowired
	ErlManifestFileCleanUpTasklet archiveManifest;

	@Value(TEST_PATH_MANIFEST)
	private File manifestFilesDir;	

	@Value(TEST_PATH_INPUT)
	private File inputFilesDir;

	@Value(TEST_PATH_INPUT_DIR)
	private File inputDir;

	@Value(TEST_PATH_IN_PROCESS)
	private File privateFilesDir;	

	@Value(TEST_PATH_PROCESSED)
	private File processedFilesDir;	

	@Value(TEST_PATH_INVALID)
	private File invalidFilesDir;

	@Value(TEST_PATH_MANIFEST_ARCHIVE)
	private File archiveDir;

	private String xsd = "/erl/BatchUpdateUtilitySchema.xsd";

	private static Marshaller marshallerBEM;
	private static Marshaller marshallerFileInfo;
	private JobParameters jobParameters;
	private JobInstance jobInstance;
	private Long jobIdGen = null;

	static {
		try {
			JAXBContext jaxbContextBEM = JAXBContext.newInstance(BenefitEnrollmentRequest.class);
			marshallerBEM = jaxbContextBEM.createMarshaller();
			JAXBContext jaxbContextFileInfo = JAXBContext.newInstance(FileInformationType.class);
			marshallerFileInfo = jaxbContextFileInfo.createMarshaller();
		} catch (Exception ex) {
			System.out.print("ERROR:  " + ex.getMessage());
		}
	}

	@Before
	public void setup() throws IOException {

		manifestFilesDir.mkdirs();
		manifestFileReader.setManifestDirectory(manifestFilesDir);
		//ReflectionTestUtils.setField(manifestFileReader, "isFileRead", false);

		inputFilesDir.mkdirs();
		privateFilesDir.mkdirs();
		erlManifestWriter.setSourceDirectory(inputDir);
		erlManifestWriter.setProcessingDirectory(privateFilesDir);

		xsdValidator.setXsd(xsd);
		xsdValidator.setPrivateDirectory(privateFilesDir);

		setupFiles();

		final DateFormatter dateFormatter = new DateFormatter("yyyy-MM-dd HH:mm:ss.SSS");
		final Map<String, JobParameter> params = new LinkedHashMap<String, JobParameter>();
		params.put("source", new JobParameter("ffm"));
		//params.put("jobType", new JobParameter("processor"));
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

		erlManifestWriter.setJobExecutionContext(jobExecution);
		manifestFileReader.setJobExecutionContext(jobExecution);

		bemExtractionProcessor.setJobParameters(jobParameters);
		bemExtractionProcessor.setFilePath(TEST_PATH_IN_PROCESS + "/");

		bemExtractionWriter.setJobExecution(jobExecution);

		bemProcessorListener.setSourceDirectory(privateFilesDir);
		bemProcessorListener.setInvalidFilesDirectory(invalidFilesDir);
		bemProcessorListener.setErlBatchRunControlId("10");

		processedFilesDir.mkdirs();
		moveProcessedFilesTasklet.setSourceDirectory(privateFilesDir);
		moveProcessedFilesTasklet.setDestinationDirectory(processedFilesDir);

		archiveDir.mkdirs();
		archiveManifest.setDestinationDirectory(archiveDir);

	}

	private void setupFiles() throws IOException {
		writeManifestFile();
		writeErlExtractFile();
	}

	@After
	public void tearDown() {
		FileUtils.deleteQuietly(new File(ROOT_FILE_PATH));
	}

	/**
	 * This method will test the reprocessing of skipped erl records, with successfully inserting the skipped policies to EPS.
	 * 
	 * 1. Launches bemHandler step which sets skipped records status to 'D' or 'R' accordingly when a 
	 *    previously skipped version is sent as 'this' transaction.
	 * 2. Launches the 'stageReprocess' step which inserts the 'R' records to processing queue (bem index table)
	 * 3. Launches 'reprocessSkips' step which attempts to process the 'R' records and updates the status accordingly to 
	 * 	  'Y'/'N'/'S' based on the processing result.
	 * 
	 * @throws Exception
	 */
	@Test
	public void testJob_Erl_reprocessSkips() throws Exception {

		// clean up prior and after incase previous test failed.
		getJdbc().execute("DELETE FROM DAILYBEMINDEXER WHERE EXCHANGETYPECD='FFM' AND SUBSCRIBERSTATECD = 'ZZ'");
		getJdbc().execute("DELETE FROM BATCHPROCESSLOG WHERE JOBNM='enrollmentProcessingBatchJob' AND JOBSTATUSCD = 'STARTED'");
		Long jobId1 = null;
		Long jobId = null;
		Long mockBatchId = TestDataUtil.getRandomNumber(8);
		String transMsgOriginTypCd = "FFM";
		String stateCd = "ZZ";
		String exchangePolicyId = "TESTREPROCESSSKIPSJOB";
		String hiosId = exchangePolicyId.substring(0, 5);
		int versionNum = 1;
		String versionNumStr = versionNum + "";
		LocalDateTime versionDt = MAR_1_3am;

		try {
			// Insert a batchTrans's with same policy as "this" one representing a previous transaction version that was processed as 'S'
			// pass in batchId for CREATEBY used by delete script.
			Long transMsgIdV1 = insertTransMsg(mockBatchId, transMsgOriginTypCd);
			insertBatchTransMsg(transMsgIdV1, mockBatchId, ProcessedToDbInd.S, stateCd, exchangePolicyId, hiosId, versionNumStr, versionDt);

			// Insert a skipped batchTransMsg and increment versionId representing a later "skipped" transaction for this policy
			versionNum++;
			versionNumStr = versionNum +"";

			String bemXlMsg2 = getBemTransMsg(stateCd, exchangePolicyId, hiosId, versionDt.plusSeconds(1), versionNumStr, PolicyStatus.EFFECTUATED_2);
			Long transMsgIdV2 = insertBemTransMsg(mockBatchId, transMsgOriginTypCd, bemXlMsg2);
			insertBatchTransMsg(transMsgIdV2, mockBatchId, ProcessedToDbInd.S, stateCd, exchangePolicyId, hiosId, versionNumStr, versionDt);

			// Insert a skipped batchTransMsg and increment versionId representing a later "skipped" transaction for this policy
			versionNum++;
			versionNumStr = versionNum +"";
			String bemXlMsg3 = getBemTransMsg(stateCd, exchangePolicyId, hiosId, versionDt.plusSeconds(2), versionNumStr, PolicyStatus.CANCELLED_3);
			Long transMsgIdV3 = insertBemTransMsg(mockBatchId, transMsgOriginTypCd, bemXlMsg3);
			insertBatchTransMsg(transMsgIdV3, mockBatchId, ProcessedToDbInd.S, stateCd, exchangePolicyId, hiosId, versionNumStr, versionDt);

			//Launch the job
			JobExecution run = jobLauncherTestUtils.launchJob(jobParameters);
			jobId1 = run.getJobId();

			Integer bemIdxCntAfterJob1 = getJdbc().queryForObject("SELECT Count(*) FROM DAILYBEMINDEXER WHERE EXCHANGETYPECD='FFM' AND SUBSCRIBERSTATECD = 'ZZ'", Integer.class);
			assertEquals("Bem Index is populated after file extract job", 1, bemIdxCntAfterJob1.intValue());

			String ingestStatus = getJdbc().queryForObject("SELECT INGESTCOMPLETEIND FROM BATCHRUNCONTROL WHERE BATCHRUNCONTROLID=10", String.class);
			assertEquals("Ingest Status after Manifest is processed", "Y", ingestStatus);


			final Map<String, JobParameter> params = jobParameters.getParameters();
			params.put("jobType", new JobParameter("processor"));
			params.put("timestamp", new JobParameter(new DateTime().toString()));
			jobParameters = new JobParameters(params);

			run = jobLauncherTestUtils.launchJob(jobParameters);

			jobId = run.getJobId();
			System.out.println("Job id: "+ jobId);

			assertEquals("Job Status", ExitStatus.COMPLETED, run.getExitStatus());
			assertEquals("Batch Status value is COMPLETED", BatchStatus.COMPLETED, run.getStatus());


			Integer policyCntAfterReprocessStep = getJdbc().queryForObject("SELECT Count(*) FROM POLICYVERSION WHERE CREATEBY='"+jobId+"'", Integer.class);
			assertEquals("Policy count after 'reprocess' step", 3, policyCntAfterReprocessStep.intValue());

			String procToDbCd = getJdbc().queryForObject("SELECT PROCESSEDTODBSTATUSTYPECD FROM BATCHTRANSMSG WHERE TRANSMSGID = " + transMsgIdV1 + " AND BATCHID = " + mockBatchId, String.class);
			assertEquals("ProcessedToDbIndicator for transMsgId: " + transMsgIdV1, ProcessedToDbInd.D.getValue(), procToDbCd);

			procToDbCd = getJdbc().queryForObject("SELECT PROCESSEDTODBSTATUSTYPECD FROM BATCHTRANSMSG WHERE TRANSMSGID = " + transMsgIdV2 + " AND BATCHID = " + mockBatchId, String.class);
			assertEquals("ProcessedToDbIndicator for transMsgId: " + transMsgIdV2, ProcessedToDbInd.D.getValue(), procToDbCd);
			procToDbCd = getJdbc().queryForObject("SELECT PROCESSEDTODBSTATUSTYPECD FROM BATCHTRANSMSG WHERE TRANSMSGID = " + transMsgIdV2 + " AND BATCHID = " + jobId, String.class);
			assertEquals("ProcessedToDbIndicator for transMsgId: " + transMsgIdV2, ProcessedToDbInd.Y.getValue(), procToDbCd);

			procToDbCd = getJdbc().queryForObject("SELECT PROCESSEDTODBSTATUSTYPECD FROM BATCHTRANSMSG WHERE TRANSMSGID = " + transMsgIdV3 + " AND BATCHID = " + mockBatchId, String.class);
			assertEquals("ProcessedToDbIndicator for transMsgId: " + transMsgIdV3, ProcessedToDbInd.D.getValue(), procToDbCd);
			procToDbCd = getJdbc().queryForObject("SELECT PROCESSEDTODBSTATUSTYPECD FROM BATCHTRANSMSG WHERE TRANSMSGID = " + transMsgIdV3 + " AND BATCHID = " + jobId, String.class);
			assertEquals("ProcessedToDbIndicator for transMsgId: " + transMsgIdV3, ProcessedToDbInd.Y.getValue(), procToDbCd);

			Integer bemIdxCntAfterJob = getJdbc().queryForObject("SELECT Count(*) FROM DAILYBEMINDEXER WHERE EXCHANGETYPECD='FFM' AND SUBSCRIBERSTATECD = 'ZZ'", Integer.class);
			assertEquals("Bem Index is cleaned up after 'bemPostCleanup' step", 0, bemIdxCntAfterJob.intValue());

		} finally {
			if(jobId != null) deleteTestData(jobId);
			if(jobId1 != null) deleteTestData(jobId1);
			if(jobIdGen != null) deleteTestData(jobIdGen);
			deleteTestData(mockBatchId);
			deleteSetUpData(mockBatchId);
			getJdbc().execute("DELETE FROM DAILYBEMINDEXER WHERE EXCHANGETYPECD='FFM' AND SUBSCRIBERSTATECD = 'ZZ'");
		}
	}

	/**
	 * This method will test the reprocessing of skipped erl records resulting in validation errors, 
	 * with inserting the validation failure error messages to EPS error warning tables.
	 * 
	 * 1. Launches bemHandler step which sets skipped records status to 'D' or 'R' accordingly when a 
	 *    previously skipped version is sent as 'this' transaction.
	 * 2. Launches the 'stageReprocess' step which inserts the 'R' records to processing queue (bem index table)
	 * 3. Launches 'reprocessSkips' step which attempts to process the 'R' records and updates the status accordingly to 
	 * 	  'Y'/'N'/'S' based on the processing result.
	 * 
	 * @throws Exception
	 */
	@Test
	public void testJob_Erl_reprocessSkips_validationErrors() throws Exception {

		// clean up prior and after incase previous test failed.
		getJdbc().execute("DELETE FROM DAILYBEMINDEXER WHERE EXCHANGETYPECD='FFM' AND SUBSCRIBERSTATECD = 'ZZ'");
		getJdbc().execute("DELETE FROM BATCHPROCESSLOG WHERE JOBNM='enrollmentProcessingBatchJob' AND JOBSTATUSCD = 'STARTED'");
		Long jobId1 = null;
		Long jobId = null;
		Long mockBatchId = TestDataUtil.getRandomNumber(8);
		String transMsgOriginTypCd = "FFM";
		String stateCd = "ZZ";
		String exchangePolicyId = "TESTREPROCESSSKIPSJOB";
		String hiosId = exchangePolicyId.substring(0, 5);
		int versionNum = 1;
		String versionNumStr = versionNum + "";
		LocalDateTime versionDt = MAR_1_3am;

		try {
			// Insert a batchTrans's with same policy as "this" one representing a previous transaction version that was processed as 'S'
			// pass in batchId for CREATEBY used by delete script.
			Long transMsgIdV1 = insertTransMsg(mockBatchId, transMsgOriginTypCd);
			insertBatchTransMsg(transMsgIdV1, mockBatchId, ProcessedToDbInd.S, stateCd, exchangePolicyId, hiosId, versionNumStr, versionDt);

			// Insert a skipped batchTransMsg and increment versionId representing a later "skipped" transaction for this policy
			versionNum++;
			versionNumStr = versionNum +"";
			versionDt = versionDt.plusSeconds(1);

			String bemXlMsg2 = getBemTransMsg(stateCd, exchangePolicyId, hiosId, versionDt, versionNumStr, PolicyStatus.EFFECTUATED_2)
					.replaceAll("EligibilityBeginDate>2015-", "EligibilityBeginDate>2014-");
			String fileInfoV2 = marshallFileInfo(TestDataUtil.makeFileInformationType(mockBatchId, ExchangeType.FFM));
			Long transMsgIdV2 = insertBemTransMsg(mockBatchId, transMsgOriginTypCd, bemXlMsg2, fileInfoV2);
			insertBatchTransMsg(transMsgIdV2, mockBatchId, ProcessedToDbInd.S, stateCd, exchangePolicyId, hiosId, versionNumStr, versionDt);

			// Insert a skipped batchTransMsg and increment versionId representing a later "skipped" transaction for this policy
			versionNum++;
			versionNumStr = versionNum +"";
			versionDt = versionDt.plusSeconds(1);

			
			String bemXlMsg3 = getBemTransMsg(stateCd, exchangePolicyId, hiosId, versionDt, versionNumStr, PolicyStatus.CANCELLED_3)
					.replaceAll("EligibilityBeginDate>2015-", "EligibilityBeginDate>2014-");
			String fileInfoV3 = marshallFileInfo(TestDataUtil.makeFileInformationType(mockBatchId, ExchangeType.FFM));
			Long transMsgIdV3 = insertBemTransMsg(mockBatchId, transMsgOriginTypCd, bemXlMsg3, fileInfoV3);
			insertBatchTransMsg(transMsgIdV3, mockBatchId, ProcessedToDbInd.S, stateCd, exchangePolicyId, hiosId, versionNumStr, versionDt);

			//Launch the job
			JobExecution run = jobLauncherTestUtils.launchJob(jobParameters);
			jobId1 = run.getJobId();

			Integer bemIdxCntAfterJob1 = getJdbc().queryForObject("SELECT Count(*) FROM DAILYBEMINDEXER WHERE EXCHANGETYPECD='FFM' AND SUBSCRIBERSTATECD = 'ZZ'", Integer.class);
			assertEquals("Bem Index is populated after file extract job", 1, bemIdxCntAfterJob1.intValue());

			String ingestStatus = getJdbc().queryForObject("SELECT INGESTCOMPLETEIND FROM BATCHRUNCONTROL WHERE BATCHRUNCONTROLID=10", String.class);
			assertEquals("Ingest Status after Manifest is processed", "Y", ingestStatus);

			final Map<String, JobParameter> params = jobParameters.getParameters();
			params.put("jobType", new JobParameter("processor"));
			params.put("timestamp", new JobParameter(new DateTime().toString()));
			jobParameters = new JobParameters(params);

			run = jobLauncherTestUtils.launchJob(jobParameters);

			jobId = run.getJobId();
			System.out.println("Job id: "+ jobId);

			assertEquals("Job Status", ExitStatus.COMPLETED, run.getExitStatus());
			assertEquals("Batch Status value is COMPLETED", BatchStatus.COMPLETED, run.getStatus());


			Integer policyCntAfterReprocessStep = getJdbc().queryForObject("SELECT Count(*) FROM POLICYVERSION WHERE CREATEBY='"+jobId+"'", Integer.class);
			assertEquals("Policy count after 'reprocess' step", 1, policyCntAfterReprocessStep.intValue());

			String procToDbCd = getJdbc().queryForObject("SELECT PROCESSEDTODBSTATUSTYPECD FROM BATCHTRANSMSG WHERE TRANSMSGID = " + transMsgIdV1 + " AND BATCHID = " + mockBatchId, String.class);
			assertEquals("ProcessedToDbIndicator for transMsgId: " + transMsgIdV1, ProcessedToDbInd.D.getValue(), procToDbCd);

			procToDbCd = getJdbc().queryForObject("SELECT PROCESSEDTODBSTATUSTYPECD FROM BATCHTRANSMSG WHERE TRANSMSGID = " + transMsgIdV2 + " AND BATCHID = " + mockBatchId, String.class);
			assertEquals("ProcessedToDbIndicator for transMsgId: " + transMsgIdV2, ProcessedToDbInd.D.getValue(), procToDbCd);
			procToDbCd = getJdbc().queryForObject("SELECT PROCESSEDTODBSTATUSTYPECD FROM BATCHTRANSMSG WHERE TRANSMSGID = " + transMsgIdV2 + " AND BATCHID = " + jobId, String.class);
			assertEquals("ProcessedToDbIndicator for transMsgId: " + transMsgIdV2, ProcessedToDbInd.N.getValue(), procToDbCd);

			procToDbCd = getJdbc().queryForObject("SELECT PROCESSEDTODBSTATUSTYPECD FROM BATCHTRANSMSG WHERE TRANSMSGID = " + transMsgIdV3 + " AND BATCHID = " + mockBatchId, String.class);
			assertEquals("ProcessedToDbIndicator for transMsgId: " + transMsgIdV3, ProcessedToDbInd.D.getValue(), procToDbCd);
			procToDbCd = getJdbc().queryForObject("SELECT PROCESSEDTODBSTATUSTYPECD FROM BATCHTRANSMSG WHERE TRANSMSGID = " + transMsgIdV3 + " AND BATCHID = " + jobId, String.class);
			assertEquals("ProcessedToDbIndicator for transMsgId: " + transMsgIdV3, ProcessedToDbInd.N.getValue(), procToDbCd);

			Integer bemIdxCntAfterJob = getJdbc().queryForObject("SELECT Count(*) FROM DAILYBEMINDEXER WHERE EXCHANGETYPECD='FFM' AND SUBSCRIBERSTATECD = 'ZZ'", Integer.class);
			assertEquals("Bem Index is cleaned up after 'bemPostCleanup' step", 0, bemIdxCntAfterJob.intValue());

		} finally {
			if(jobId != null) deleteTestData(jobId);
			if(jobId1 != null) deleteTestData(jobId1);
			if(jobIdGen != null) deleteTestData(jobIdGen);
			deleteTestData(mockBatchId);
			deleteSetUpData(mockBatchId);
			getJdbc().execute("DELETE FROM DAILYBEMINDEXER WHERE EXCHANGETYPECD='FFM' AND SUBSCRIBERSTATECD = 'ZZ'");
		}

	}

	/**
	 * This method will test the reprocessing of skipped erl records, with the reprocessed record also
	 * resulting in skip.
	 * 
	 * 1. Launches bemHandler step which sets skipped records status to 'D' or 'R' accordingly when a 
	 *    previously skipped version is sent as 'this' transaction.
	 * 2. Launches the 'stageReprocess' step which inserts the 'R' records to processing queue (bem index table)
	 * 3. Launches 'reprocessSkips' step which attempts to process the 'R' records and updates the status accordingly to 
	 * 	  'Y'/'N'/'S' based on the processing result.
	 * 
	 * @throws Exception
	 */
	@Test
	public void testJob_Erl_reprocessSkips_resultingSkips() throws Exception {

		// clean up prior and after incase previous test failed.
		getJdbc().execute("DELETE FROM DAILYBEMINDEXER WHERE EXCHANGETYPECD='FFM' AND SUBSCRIBERSTATECD = 'ZZ'");
		getJdbc().execute("DELETE FROM BATCHPROCESSLOG WHERE JOBNM='enrollmentProcessingBatchJob' AND JOBSTATUSCD = 'STARTED'");
		Long jobId1 = null;
		Long jobId = null;
		Long mockBatchId = TestDataUtil.getRandomNumber(8);
		String transMsgOriginTypCd = "FFM";
		String stateCd = "ZZ";
		String exchangePolicyId = "TESTREPROCESSSKIPSJOB";
		String hiosId = exchangePolicyId.substring(0, 5);
		int versionNum = 1;
		String versionNumStr = versionNum + "";
		LocalDateTime versionDt = MAR_1_3am;

		try {
			// Insert a batchTrans's with same policy as "this" one representing a previous transaction version that was processed as 'S'
			// pass in batchId for CREATEBY used by delete script.
			Long transMsgIdV1 = insertTransMsg(mockBatchId, transMsgOriginTypCd);
			insertBatchTransMsg(transMsgIdV1, mockBatchId, ProcessedToDbInd.S, stateCd, exchangePolicyId, hiosId, versionNumStr, versionDt);

			// Insert a skipped batchTransMsg and increment versionId representing a later "skipped" transaction for this policy
			versionNum++;
			versionNumStr = versionNum +"";

			String bemXlMsg2 = getBemTransMsg(stateCd, exchangePolicyId, hiosId, versionDt.plusSeconds(1), versionNumStr, PolicyStatus.EFFECTUATED_2)
					.replaceAll("<InsuranceLineCode>HLT</InsuranceLineCode>", "<InsuranceLineCode></InsuranceLineCode>");
			Long transMsgIdV2 = insertBemTransMsg(mockBatchId, transMsgOriginTypCd, bemXlMsg2);
			insertBatchTransMsg(transMsgIdV2, mockBatchId, ProcessedToDbInd.S, stateCd, exchangePolicyId, hiosId, versionNumStr, versionDt);

			// Insert a skipped batchTransMsg and increment versionId representing a later "skipped" transaction for this policy
			versionNum++;
			versionNumStr = versionNum +"";
			String bemXlMsg3 = getBemTransMsg(stateCd, exchangePolicyId, hiosId, versionDt.plusSeconds(2), versionNumStr, PolicyStatus.CANCELLED_3);
			Long transMsgIdV3 = insertBemTransMsg(mockBatchId, transMsgOriginTypCd, bemXlMsg3);
			insertBatchTransMsg(transMsgIdV3, mockBatchId, ProcessedToDbInd.S, stateCd, exchangePolicyId, hiosId, versionNumStr, versionDt);

			//Launch the job
			JobExecution run = jobLauncherTestUtils.launchJob(jobParameters);
			jobId1 = run.getJobId();


			Integer bemIdxCntAfterJob1 = getJdbc().queryForObject("SELECT Count(*) FROM DAILYBEMINDEXER WHERE EXCHANGETYPECD='FFM' AND SUBSCRIBERSTATECD = 'ZZ'", Integer.class);
			assertEquals("Bem Index is populated after file extract job", 1, bemIdxCntAfterJob1.intValue());

			String ingestStatus = getJdbc().queryForObject("SELECT INGESTCOMPLETEIND FROM BATCHRUNCONTROL WHERE BATCHRUNCONTROLID=10", String.class);
			assertEquals("Ingest Status after Manifest is processed", "Y", ingestStatus);

			final Map<String, JobParameter> params = jobParameters.getParameters();
			params.put("jobType", new JobParameter("processor"));
			params.put("timestamp", new JobParameter(new DateTime().toString()));
			jobParameters = new JobParameters(params);

			run = jobLauncherTestUtils.launchJob(jobParameters);

			jobId = run.getJobId();
			System.out.println("Job id: "+ jobId);

			assertEquals("Job Status", ExitStatus.COMPLETED, run.getExitStatus());
			assertEquals("Batch Status value is COMPLETED", BatchStatus.COMPLETED, run.getStatus());


			Integer policyCntAfterReprocessStep = getJdbc().queryForObject("SELECT Count(*) FROM POLICYVERSION WHERE CREATEBY='"+jobId+"'", Integer.class);
			assertEquals("Policy count after 'reprocess' step", 1, policyCntAfterReprocessStep.intValue());

			String procToDbCd = getJdbc().queryForObject("SELECT PROCESSEDTODBSTATUSTYPECD FROM BATCHTRANSMSG WHERE TRANSMSGID = " + transMsgIdV1 + " AND BATCHID = " + mockBatchId, String.class);
			assertEquals("ProcessedToDbIndicator for transMsgId: " + transMsgIdV1, ProcessedToDbInd.D.getValue(), procToDbCd);

			procToDbCd = getJdbc().queryForObject("SELECT PROCESSEDTODBSTATUSTYPECD FROM BATCHTRANSMSG WHERE BATCHID = " + jobId + " AND SOURCEVERSIONID = " + 1, String.class);
			assertEquals("ProcessedToDbIndicator for inbound transmsg", ProcessedToDbInd.Y.getValue(), procToDbCd);

			procToDbCd = getJdbc().queryForObject("SELECT PROCESSEDTODBSTATUSTYPECD FROM BATCHTRANSMSG WHERE TRANSMSGID = " + transMsgIdV2 + " AND BATCHID = " + mockBatchId, String.class);
			assertEquals("ProcessedToDbIndicator for transMsgId: " + transMsgIdV2, ProcessedToDbInd.D.getValue(), procToDbCd);

			procToDbCd = getJdbc().queryForObject("SELECT PROCESSEDTODBSTATUSTYPECD FROM BATCHTRANSMSG WHERE TRANSMSGID = " + transMsgIdV2 + " AND BATCHID = " + jobId, String.class);
			assertEquals("ProcessedToDbIndicator for transMsgId: " + transMsgIdV2, ProcessedToDbInd.S.getValue(), procToDbCd);

			procToDbCd = getJdbc().queryForObject("SELECT PROCESSEDTODBSTATUSTYPECD FROM BATCHTRANSMSG WHERE TRANSMSGID = " + transMsgIdV3 + " AND BATCHID = " + mockBatchId, String.class);
			assertEquals("ProcessedToDbIndicator for transMsgId: " + transMsgIdV3, ProcessedToDbInd.D.getValue(), procToDbCd);

			procToDbCd = getJdbc().queryForObject("SELECT PROCESSEDTODBSTATUSTYPECD FROM BATCHTRANSMSG WHERE TRANSMSGID = " + transMsgIdV3 + " AND BATCHID = " + jobId, String.class);
			assertEquals("ProcessedToDbIndicator for transMsgId: " + transMsgIdV3, ProcessedToDbInd.S.getValue(), procToDbCd);

			Integer bemIdxCntAfterJob = getJdbc().queryForObject("SELECT Count(*) FROM DAILYBEMINDEXER WHERE EXCHANGETYPECD='FFM' AND SUBSCRIBERSTATECD = 'ZZ'", Integer.class);
			assertEquals("Bem Index is cleaned up after 'bemPostCleanup' step", 0, bemIdxCntAfterJob.intValue());

		} finally {
			if(jobId != null) deleteTestData(jobId);
			if(jobId1 != null) deleteTestData(jobId1);
			if(jobIdGen != null) deleteTestData(jobIdGen);
			deleteTestData(mockBatchId);
			deleteSetUpData(mockBatchId);
			getJdbc().execute("DELETE FROM DAILYBEMINDEXER WHERE EXCHANGETYPECD='FFM' AND SUBSCRIBERSTATECD = 'ZZ'");
		}
	}

	public String getBemTransMsg(String stateCd, String exchangePolicyId,
			String hiosId, LocalDateTime versionDt, String versionNumStr, PolicyStatus policyStatus) throws Exception {
		BenefitEnrollmentMaintenanceType bem = makeBem(versionNumStr, versionDt, policyStatus, exchangePolicyId);
		bem.getMember().add(TestDataUtil.makeSubscriber(stateCd, exchangePolicyId, hiosId));

		String bemXml = marshallBEM(bem);
		return bemXml;
	}

	private String marshallBEM(BenefitEnrollmentMaintenanceType bem) throws Exception {

		StringWriter sw = new StringWriter();

		JAXBElement<BenefitEnrollmentMaintenanceType> jaxbElement = 
				new JAXBElement<BenefitEnrollmentMaintenanceType>(new QName("BenefitEnrollmentMaintenance"), 
						BenefitEnrollmentMaintenanceType.class, bem);
		marshallerBEM.marshal(jaxbElement, sw);

		String xmlStr = sw.toString();
		xmlStr = xmlStr.replaceAll(":ns2","");
		xmlStr = xmlStr.replaceAll("ns2:","");

		return xmlStr;
	}

	private String marshallFileInfo(FileInformationType fileInfo) throws Exception {

		StringWriter sw = new StringWriter();

		JAXBElement<FileInformationType> jaxbElement = 
				new JAXBElement<FileInformationType>(new QName("FileInformation"), 
						FileInformationType.class, fileInfo);
		marshallerFileInfo.marshal(jaxbElement, sw);

		String xmlStr = sw.toString();
		xmlStr = xmlStr.replaceAll(":ns2","");
		xmlStr = xmlStr.replaceAll("ns2:","");

		return xmlStr;
	}

	private void writeManifestFile() throws IOException {

		File manifestFile = new File(TEST_PATH_MANIFEST + File.separator + "Manifest-10.txt");
		if (!manifestFile.exists()) {
			manifestFile.createNewFile();
		}

		// Add some micros since default is 0.
		Long microSec = TestDataUtil.getRandom3DigitNumber();
		ZonedDateTime zdt = ZonedDateTime.now();
		Long micros = (microSec * 1000);
		zdt = zdt.plusNanos(micros);

		PrintWriter writer = new PrintWriter(manifestFile);
		writer.println("jobid=10");

		writer.println("BeginHighWaterMark=" + zdt.format(DTF_MANIFEST_MICRO_SEC) );
		writer.println("JobStartTime=" +zdt.minusYears(5).format(DTF_MANIFEST_MICRO_SEC));
		writer.println("JobEndTime=" + zdt.minusYears(5).plusHours(1).format(DTF_MANIFEST_MICRO_SEC));
		writer.println("EndHighWaterMark=" + zdt.plusHours(1).format(DTF_MANIFEST_MICRO_SEC));
		writer.println("RecordCount=" + 1);
		writer.println("PAETCOMPLETION=N");
		writer.write("JobStatus=SUCCESS");

		writer.flush();
		writer.close();
	}


	private void writeErlExtractFile() throws IOException {
		File inputFile = new ClassPathResource("testfiles"+File.separator+"ERLSKIP.TEST.D141218.T163855502.T.11165VA0020999").getFile();

		File destFile = new File(TEST_PATH_INPUT + File.separator + "ERLSKIP.TEST.D141218.T163855502.T.11165VA0020999");

		System.out.println("extract file: " + destFile.getPath());

		FileUtils.copyFile(inputFile, destFile);
	}
}
