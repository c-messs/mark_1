package gov.hhs.cms.ff.fm.eps.ep.jobs.enrollmentprocessingjob;


import java.io.IOException;
import java.io.StringWriter;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.Marshaller;
import javax.xml.namespace.QName;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameter;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.beans.factory.annotation.Autowired;
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

import gov.cms.dsh.bem.BenefitEnrollmentMaintenanceType;
import gov.cms.dsh.bem.BenefitEnrollmentRequest;
import gov.cms.dsh.bem.FileInformationType;
import gov.hhs.cms.ff.fm.eps.ep.BenefitEnrollmentMaintenanceDTO;
import gov.hhs.cms.ff.fm.eps.ep.enums.ExchangeType;
import gov.hhs.cms.ff.fm.eps.ep.enums.PolicyStatus;
import gov.hhs.cms.ff.fm.eps.ep.enums.ProcessedToDbInd;
import gov.hhs.cms.ff.fm.eps.ep.jobs.enrollmentprocessingjob.data.util.TestDataUtil;

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
public class ErlSkipReprocessStepTest extends BaseBatchTest {

	@Autowired
	private JobLauncherTestUtils  jobLauncherTestUtils;

	@Autowired
	private JdbcBatchItemWriter<BenefitEnrollmentMaintenanceDTO> bemIndexWriter;

	@Autowired
	private String erlBEMIndexInsert;

	private static Marshaller marshallerBEM;
	private static Marshaller marshallerFileInfo;

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
	}

	@After
	public void tearDown() {
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
	public void testDoStep_bemHandlerFFM_reprocessSkips() throws Exception {

		// clean up prior and after incase previous test failed.
		getJdbc().execute("DELETE FROM STAGINGPLANLOCK ");
		getJdbc().execute("DELETE FROM DAILYBEMINDEXER WHERE EXCHANGETYPECD='FFM' AND SUBSCRIBERSTATECD = 'ZZ'");
		StepExecution stepExec = null;
		Long jobId = null;
		Long mockBatchId = TestDataUtil.getRandomNumber(8);
		Long jobIdReprocess = null;

		String expectedFileNm = makeFileNameERL(APR_1_4am);
		LocalDateTime expectedFileDateTime = getFileNameDateTime(expectedFileNm);
		ExchangeType expectedExchangeType = ExchangeType.FFM;
		ProcessedToDbInd expectedProcessedToDbInd = ProcessedToDbInd.R;

		String transMsgOriginTypCd = "FFM";
		String stateCd = "ZZ";
		String exchangePolicyId = "REPROCESSSKIPSTEST";
		String hiosId = exchangePolicyId.substring(0, 5);
		int versionNum = 1;
		String versionNumStr = versionNum + "";
		LocalDateTime versionDt = MAR_1_3am;

		try {
			insertBatchRunControl("N");
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

			// Insert a transMsg and Batch and use versionId representing the first transaction that was skipped and will be processed as "this" transaction.
			String bemXmlMsg1a = getBemTransMsg(stateCd, exchangePolicyId, hiosId, versionDt.plusSeconds(3), "1", PolicyStatus.INITIAL_1);
			Long transMsgIdV1a = insertBemTransMsg(mockBatchId, transMsgOriginTypCd, bemXmlMsg1a);

			// Insert "this" transaction into the bemIndexer for processing
			BenefitEnrollmentMaintenanceDTO bemDTO = makeBemDTO(mockBatchId, transMsgIdV1a, "1", versionDt, PolicyStatus.INITIAL_1, exchangePolicyId);
			bemDTO.getBem().getMember().add(TestDataUtil.makeSubscriber(stateCd, exchangePolicyId, hiosId, versionDt.toLocalDate(), PolicyStatus.INITIAL_1));

			bemDTO.setBemXml(marshallBEM(bemDTO.getBem()));

			bemDTO.setFileNm(expectedFileNm);
			bemDTO.setFileNmDateTime(expectedFileDateTime);
			bemDTO.setExchangeTypeCd(ExchangeType.FFM.getValue());
			FileInformationType fileInfo = TestDataUtil.makeFileInformationType(mockBatchId, expectedExchangeType);
			bemDTO.setFileInfoXml(marshallFileInfo(fileInfo));
			bemDTO.setSubscriberStateCd(stateCd);
			bemDTO.setPlanId(fileInfo.getGroupSenderID());
			bemDTO.setBatchRunControlId("100");

			bemIndexWriter.setSql(erlBEMIndexInsert);
			List<BenefitEnrollmentMaintenanceDTO> bemDTOList = new ArrayList<BenefitEnrollmentMaintenanceDTO>();
			bemDTOList.add(bemDTO);

			bemIndexWriter.write(bemDTOList);

			final DateFormatter dateFormatter = new DateFormatter("yyyy-MM-dd HH:mm:ss.SSS");
			final Map<String, JobParameter> params = new LinkedHashMap<String, JobParameter>();
			params.put("source", new JobParameter("ffm"));
			params.put("jobType", new JobParameter("processor"));
			params.put("timestamp", new JobParameter(dateFormatter.print(new Date(), Locale.US)));

			JobExecution jobExec = jobLauncherTestUtils.launchStep("bemHandlerFFM", new JobParameters(params));
			stepExec = getStepExecution(jobExec);
			jobId = stepExec.getJobExecution().getJobId();
			//
			String procToDbCd = getJdbc().queryForObject("SELECT PROCESSEDTODBSTATUSTYPECD FROM BATCHTRANSMSG WHERE TRANSMSGID = " + transMsgIdV1 + " AND BATCHID = " + mockBatchId, String.class);
			assertEquals("ProcessedToDbIndicator for transMsgId: " + transMsgIdV1, ProcessedToDbInd.D.getValue(), procToDbCd);

			procToDbCd = getJdbc().queryForObject("SELECT PROCESSEDTODBSTATUSTYPECD FROM BATCHTRANSMSG WHERE TRANSMSGID = " + transMsgIdV1a + " AND BATCHID = " + jobId, String.class);
			assertEquals("ProcessedToDbIndicator for transMsgId: " + transMsgIdV1a, expectedProcessedToDbInd.getValue(), procToDbCd);

			procToDbCd = getJdbc().queryForObject("SELECT PROCESSEDTODBSTATUSTYPECD FROM BATCHTRANSMSG WHERE TRANSMSGID = " + transMsgIdV2 + " AND BATCHID = " + mockBatchId, String.class);
			assertEquals("ProcessedToDbIndicator for transMsgId: " + transMsgIdV2, expectedProcessedToDbInd.getValue(), procToDbCd);

			procToDbCd = getJdbc().queryForObject("SELECT PROCESSEDTODBSTATUSTYPECD FROM BATCHTRANSMSG WHERE TRANSMSGID = " + transMsgIdV3 + " AND BATCHID = " + mockBatchId, String.class);
			assertEquals("ProcessedToDbIndicator for transMsgId: " + transMsgIdV3, expectedProcessedToDbInd.getValue(), procToDbCd);

			/* Launch Bem Post Cleanup step*/
			/*			Integer bemIndexCountBeforeCleanupStep = getJdbc().queryForObject("SELECT Count(*) FROM DAILYBEMINDEXER WHERE EXCHANGETYPECD='FFM' AND SUBSCRIBERSTATECD = 'ZZ'", Integer.class);
			assertTrue("Bem Index count before 'bemPostCleanup' step", bemIndexCountBeforeCleanupStep.intValue()>0);

			params.put("timestamp", new JobParameter(dateFormatter.print(new Date(), Locale.US)));
			jobLauncherTestUtils.launchStep("bemPostCleanupErl", new JobParameters(params));*/

			Integer bemIdxCntAfterCleanUpStep = getJdbc().queryForObject("SELECT Count(*) FROM DAILYBEMINDEXER WHERE EXCHANGETYPECD='FFM' AND SUBSCRIBERSTATECD = 'ZZ'", Integer.class);
			assertEquals("Bem Index is cleaned up after 'bemPostCleanup' step", 0, bemIdxCntAfterCleanUpStep.intValue());

			////////////////////////////////First pass complete, over to second pass //////////////// 

			/* Launch stageReprocess step*/
			/*			params.put("timestamp", new JobParameter(dateFormatter.print(new Date(), Locale.US)));
			jobLauncherTestUtils.launchStep("stageReprocess", new JobParameters(params));

			Integer bemIdxCntAfterStageReprocessStep = getJdbc().queryForObject("SELECT Count(*) FROM DAILYBEMINDEXER WHERE EXCHANGETYPECD='FFM' AND TRANSMSGID IN("
					+ transMsgIdV1a + "," + transMsgIdV2 + "," + transMsgIdV3 + ")"
					, Integer.class);
			assertEquals("Bem Index is populated after 'stageReprocess' step", 3, bemIdxCntAfterStageReprocessStep.intValue());
			 */			
			/* Launch reProcessSkips step*/
			params.put("timestamp", new JobParameter(dateFormatter.print(new Date(), Locale.US)));
			JobExecution jobExecReprocess = jobLauncherTestUtils.launchStep("reProcessSkips", new JobParameters(params));
			StepExecution stepExecReprocess = getStepExecution(jobExecReprocess);
			jobIdReprocess = stepExecReprocess.getJobExecution().getJobId();

			Integer policyCntAfterReprocessStep = getJdbc().queryForObject("SELECT Count(*) FROM POLICYVERSION WHERE CREATEBY='"+jobIdReprocess+"'", Integer.class);
			assertEquals("Policy count after 'reprocess' step", 3, policyCntAfterReprocessStep.intValue());

			procToDbCd = getJdbc().queryForObject("SELECT PROCESSEDTODBSTATUSTYPECD FROM BATCHTRANSMSG WHERE TRANSMSGID = " + transMsgIdV1a + " AND BATCHID = " + jobIdReprocess, String.class);
			assertEquals("ProcessedToDbIndicator for transMsgId: " + transMsgIdV1a, ProcessedToDbInd.Y.getValue(), procToDbCd);

			procToDbCd = getJdbc().queryForObject("SELECT PROCESSEDTODBSTATUSTYPECD FROM BATCHTRANSMSG WHERE TRANSMSGID = " + transMsgIdV2 + " AND BATCHID = " + mockBatchId, String.class);
			assertEquals("ProcessedToDbIndicator for transMsgId: " + transMsgIdV2, ProcessedToDbInd.D.getValue(), procToDbCd);
			procToDbCd = getJdbc().queryForObject("SELECT PROCESSEDTODBSTATUSTYPECD FROM BATCHTRANSMSG WHERE TRANSMSGID = " + transMsgIdV2 + " AND BATCHID = " + jobIdReprocess, String.class);
			assertEquals("ProcessedToDbIndicator for transMsgId: " + transMsgIdV2, ProcessedToDbInd.Y.getValue(), procToDbCd);

			procToDbCd = getJdbc().queryForObject("SELECT PROCESSEDTODBSTATUSTYPECD FROM BATCHTRANSMSG WHERE TRANSMSGID = " + transMsgIdV3 + " AND BATCHID = " + mockBatchId, String.class);
			assertEquals("ProcessedToDbIndicator for transMsgId: " + transMsgIdV3, ProcessedToDbInd.D.getValue(), procToDbCd);
			procToDbCd = getJdbc().queryForObject("SELECT PROCESSEDTODBSTATUSTYPECD FROM BATCHTRANSMSG WHERE TRANSMSGID = " + transMsgIdV3 + " AND BATCHID = " + jobIdReprocess, String.class);
			assertEquals("ProcessedToDbIndicator for transMsgId: " + transMsgIdV3, ProcessedToDbInd.Y.getValue(), procToDbCd);


			assertEquals("Batch Status value is COMPLETED", BatchStatus.COMPLETED, jobExec.getStatus());

		} finally {
			if(jobId != null) deleteTestData(jobId);
			if(jobIdReprocess != null) deleteTestData(jobIdReprocess);
			deleteTestData(mockBatchId);
			deleteSetUpData(mockBatchId);
			getJdbc().execute("DELETE FROM DAILYBEMINDEXER WHERE EXCHANGETYPECD='FFM' AND SUBSCRIBERSTATECD = 'ZZ'");
		}

	}

	/**
	 * This method will test the reprocessing of skipped erl records, with certain policies generating validation errors.
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
	public void testDoStep_bemHandlerFFM_reprocessSkips_validationErros() throws Exception {

		// clean up prior and after incase previous test failed.
		getJdbc().execute("DELETE FROM STAGINGPLANLOCK ");
		getJdbc().execute("DELETE FROM DAILYBEMINDEXER WHERE EXCHANGETYPECD='FFM' AND SUBSCRIBERSTATECD = 'ZZ'");
		StepExecution stepExec = null;
		Long jobId = null;
		Long mockBatchId = TestDataUtil.getRandomNumber(8);
		Long jobIdReprocess = null;

		String expectedFileNm = makeFileNameERL(APR_1_4am);
		LocalDateTime expectedFileDateTime = getFileNameDateTime(expectedFileNm);
		ExchangeType expectedExchangeType = ExchangeType.FFM;
		ProcessedToDbInd expectedProcessedToDbInd = ProcessedToDbInd.R;

		String transMsgOriginTypCd = "FFM";
		String stateCd = "ZZ";
		String exchangePolicyId = "REPROCESSSKIPSTEST";
		String hiosId = exchangePolicyId.substring(0, 5);
		int versionNum = 1;
		String versionNumStr = versionNum + "";
		LocalDateTime versionDt = MAR_1_3am;

		try {
			insertBatchRunControl("N");
			// Insert a batchTrans's with same policy as "this" one representing a previous transaction version that was processed as 'S'
			// pass in batchId for CREATEBY used by delete script.
			Long transMsgIdV1 = insertTransMsg(mockBatchId, transMsgOriginTypCd);
			insertBatchTransMsg(transMsgIdV1, mockBatchId, ProcessedToDbInd.S, stateCd, exchangePolicyId, hiosId, versionNumStr, versionDt);

			// Insert a skipped batchTransMsg and increment versionId representing a later "skipped" transaction for this policy
			versionNum++;
			versionNumStr = versionNum +"";

			String bemXlMsg2 = getBemTransMsg(stateCd, exchangePolicyId, hiosId, versionDt.plusSeconds(1), versionNumStr, PolicyStatus.EFFECTUATED_2)
					.replaceAll("EligibilityBeginDate>2015-", "EligibilityBeginDate>2014-");
			String fileInfoV2 = marshallFileInfo(TestDataUtil.makeFileInformationType(mockBatchId, expectedExchangeType));
			Long transMsgIdV2 = insertBemTransMsg(mockBatchId, transMsgOriginTypCd, bemXlMsg2, fileInfoV2);
			insertBatchTransMsg(transMsgIdV2, mockBatchId, ProcessedToDbInd.S, stateCd, exchangePolicyId, hiosId, versionNumStr, versionDt);

			// Insert a skipped batchTransMsg and increment versionId representing a later "skipped" transaction for this policy
			versionNum++;
			versionNumStr = versionNum +"";
			String bemXlMsg3 = getBemTransMsg(stateCd, exchangePolicyId, hiosId, versionDt.plusSeconds(2), versionNumStr, PolicyStatus.CANCELLED_3)
					.replaceAll("EligibilityBeginDate>2015-", "EligibilityBeginDate>2014-");
			String fileInfoV3 = marshallFileInfo(TestDataUtil.makeFileInformationType(mockBatchId, expectedExchangeType));
			Long transMsgIdV3 = insertBemTransMsg(mockBatchId, transMsgOriginTypCd, bemXlMsg3, fileInfoV3);
			insertBatchTransMsg(transMsgIdV3, mockBatchId, ProcessedToDbInd.S, stateCd, exchangePolicyId, hiosId, versionNumStr, versionDt);

			// Insert a transMsg and Batch and use versionId representing the first transaction that was skipped and will be processed as "this" transaction.
			String bemXmlMsg1a = getBemTransMsg(stateCd, exchangePolicyId, hiosId, versionDt.plusSeconds(3), "1", PolicyStatus.INITIAL_1);
			Long transMsgIdV1a = insertBemTransMsg(mockBatchId, transMsgOriginTypCd, bemXmlMsg1a);

			// Insert "this" transaction into the bemIndexer for processing
			BenefitEnrollmentMaintenanceDTO bemDTO = makeBemDTO(mockBatchId, transMsgIdV1a, "1", versionDt, PolicyStatus.INITIAL_1, exchangePolicyId);
			bemDTO.getBem().getMember().add(TestDataUtil.makeSubscriber(stateCd, exchangePolicyId, hiosId, versionDt.toLocalDate(), PolicyStatus.INITIAL_1));

			bemDTO.setBemXml(marshallBEM(bemDTO.getBem()));

			bemDTO.setFileNm(expectedFileNm);
			bemDTO.setFileNmDateTime(expectedFileDateTime);
			bemDTO.setExchangeTypeCd(ExchangeType.FFM.getValue());
			FileInformationType fileInfo = TestDataUtil.makeFileInformationType(mockBatchId, expectedExchangeType);
			bemDTO.setFileInfoXml(marshallFileInfo(fileInfo));
			bemDTO.setSubscriberStateCd(stateCd);
			bemDTO.setPlanId(fileInfo.getGroupSenderID());

			bemIndexWriter.setSql(erlBEMIndexInsert);
			List<BenefitEnrollmentMaintenanceDTO> bemDTOList = new ArrayList<BenefitEnrollmentMaintenanceDTO>();
			bemDTOList.add(bemDTO);

			bemIndexWriter.write(bemDTOList);

			final DateFormatter dateFormatter = new DateFormatter("yyyy-MM-dd HH:mm:ss.SSS");
			final Map<String, JobParameter> params = new LinkedHashMap<String, JobParameter>();
			params.put("source", new JobParameter("ffm"));
			params.put("jobType", new JobParameter("processor"));
			params.put("timestamp", new JobParameter(dateFormatter.print(new Date(), Locale.US)));

			JobExecution jobExec = jobLauncherTestUtils.launchStep("bemHandlerFFM", new JobParameters(params));
			stepExec = getStepExecution(jobExec);
			jobId = stepExec.getJobExecution().getJobId();
			//
			String procToDbCd = getJdbc().queryForObject("SELECT PROCESSEDTODBSTATUSTYPECD FROM BATCHTRANSMSG WHERE TRANSMSGID = " + transMsgIdV1 + " AND BATCHID = " + mockBatchId, String.class);
			assertEquals("ProcessedToDbIndicator for transMsgId: " + transMsgIdV1, ProcessedToDbInd.D.getValue(), procToDbCd);

			procToDbCd = getJdbc().queryForObject("SELECT PROCESSEDTODBSTATUSTYPECD FROM BATCHTRANSMSG WHERE TRANSMSGID = " + transMsgIdV1a + " AND BATCHID = " + jobId, String.class);
			assertEquals("ProcessedToDbIndicator for transMsgId: " + transMsgIdV1a, expectedProcessedToDbInd.getValue(), procToDbCd);

			procToDbCd = getJdbc().queryForObject("SELECT PROCESSEDTODBSTATUSTYPECD FROM BATCHTRANSMSG WHERE TRANSMSGID = " + transMsgIdV2 + " AND BATCHID = " + mockBatchId, String.class);
			assertEquals("ProcessedToDbIndicator for transMsgId: " + transMsgIdV2, expectedProcessedToDbInd.getValue(), procToDbCd);

			procToDbCd = getJdbc().queryForObject("SELECT PROCESSEDTODBSTATUSTYPECD FROM BATCHTRANSMSG WHERE TRANSMSGID = " + transMsgIdV3 + " AND BATCHID = " + mockBatchId, String.class);
			assertEquals("ProcessedToDbIndicator for transMsgId: " + transMsgIdV3, expectedProcessedToDbInd.getValue(), procToDbCd);

			Integer bemIdxCntAfterCleanUpStep = getJdbc().queryForObject("SELECT Count(*) FROM DAILYBEMINDEXER WHERE EXCHANGETYPECD='FFM' AND SUBSCRIBERSTATECD = 'ZZ'", Integer.class);
			assertEquals("Bem Index is cleaned up after 'bemPostCleanup' step", 0, bemIdxCntAfterCleanUpStep.intValue());

			////////////////////////////////First pass complete, over to second pass //////////////// 

			/* Launch reProcessSkips step*/
			params.put("timestamp", new JobParameter(dateFormatter.print(new Date(), Locale.US)));
			JobExecution jobExecReprocess = jobLauncherTestUtils.launchStep("reProcessSkips", new JobParameters(params));
			StepExecution stepExecReprocess = getStepExecution(jobExecReprocess);
			jobIdReprocess = stepExecReprocess.getJobExecution().getJobId();

			Integer policyCntAfterReprocessStep = getJdbc().queryForObject("SELECT Count(*) FROM POLICYVERSION WHERE CREATEBY='"+jobIdReprocess+"'", Integer.class);
			assertEquals("Policy count after 'reprocess' step", 1, policyCntAfterReprocessStep.intValue());

			procToDbCd = getJdbc().queryForObject("SELECT PROCESSEDTODBSTATUSTYPECD FROM BATCHTRANSMSG WHERE TRANSMSGID = " + transMsgIdV1a + " AND BATCHID = " + jobIdReprocess, String.class);
			assertEquals("ProcessedToDbIndicator for transMsgId: " + transMsgIdV1a, ProcessedToDbInd.Y.getValue(), procToDbCd);

			procToDbCd = getJdbc().queryForObject("SELECT PROCESSEDTODBSTATUSTYPECD FROM BATCHTRANSMSG WHERE TRANSMSGID = " + transMsgIdV2 + " AND BATCHID = " + mockBatchId, String.class);
			assertEquals("ProcessedToDbIndicator for transMsgId: " + transMsgIdV2, ProcessedToDbInd.D.getValue(), procToDbCd);
			procToDbCd = getJdbc().queryForObject("SELECT PROCESSEDTODBSTATUSTYPECD FROM BATCHTRANSMSG WHERE TRANSMSGID = " + transMsgIdV2 + " AND BATCHID = " + jobIdReprocess, String.class);
			assertEquals("ProcessedToDbIndicator for transMsgId: " + transMsgIdV2, ProcessedToDbInd.N.getValue(), procToDbCd);
			String errorRecV2 = getJdbc().queryForObject("SELECT BIZAPPACKERRORCD FROM ERRORWARNINGLOG WHERE TRANSMSGID = " + transMsgIdV2 + " AND BATCHID = " + jobIdReprocess, String.class);
			assertEquals("BIZAPPACKERRORCD for transMsgId: " + transMsgIdV2, "E050", errorRecV2);

			procToDbCd = getJdbc().queryForObject("SELECT PROCESSEDTODBSTATUSTYPECD FROM BATCHTRANSMSG WHERE TRANSMSGID = " + transMsgIdV3 + " AND BATCHID = " + mockBatchId, String.class);
			assertEquals("ProcessedToDbIndicator for transMsgId: " + transMsgIdV3, ProcessedToDbInd.D.getValue(), procToDbCd);
			procToDbCd = getJdbc().queryForObject("SELECT PROCESSEDTODBSTATUSTYPECD FROM BATCHTRANSMSG WHERE TRANSMSGID = " + transMsgIdV3 + " AND BATCHID = " + jobIdReprocess, String.class);
			assertEquals("ProcessedToDbIndicator for transMsgId: " + transMsgIdV3, ProcessedToDbInd.N.getValue(), procToDbCd);
			String errorRecV3 = getJdbc().queryForObject("SELECT BIZAPPACKERRORCD FROM ERRORWARNINGLOG WHERE TRANSMSGID = " + transMsgIdV3 + " AND BATCHID = " + jobIdReprocess, String.class);
			assertEquals("BIZAPPACKERRORCD for transMsgId: " + transMsgIdV3, "E050", errorRecV3);

			assertEquals("Batch Status value is COMPLETED", BatchStatus.COMPLETED, jobExec.getStatus());

		} finally {
			if(jobId != null) deleteTestData(jobId);
			if(jobIdReprocess != null) deleteTestData(jobIdReprocess);
			deleteTestData(mockBatchId);
			deleteSetUpData(mockBatchId);
			getJdbc().execute("DELETE FROM DAILYBEMINDEXER WHERE EXCHANGETYPECD='FFM' AND SUBSCRIBERSTATECD = 'ZZ'");
		}

	}

	/**
	 * This method will test the reprocessing of skipped erl records, with successfully inserting the skipped policies to EPS,
	 * when more than one version of same policy is part of the inbound transaction.
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
	public void testDoStep_bemHandlerFFM_reprocessSkips_multipleInboundVersions() throws Exception {

		// clean up prior and after incase previous test failed.
		getJdbc().execute("DELETE FROM STAGINGPLANLOCK ");
		getJdbc().execute("DELETE FROM DAILYBEMINDEXER WHERE EXCHANGETYPECD='FFM' AND SUBSCRIBERSTATECD = 'ZZ'");
		StepExecution stepExec = null;
		Long jobId = null;
		Long mockBatchId = TestDataUtil.getRandomNumber(8);
		Long jobIdReprocess = null;

		String expectedFileNm = makeFileNameERL(APR_1_4am);
		LocalDateTime expectedFileDateTime = getFileNameDateTime(expectedFileNm);
		ExchangeType expectedExchangeType = ExchangeType.FFM;
		ProcessedToDbInd expectedProcessedToDbInd = ProcessedToDbInd.R;

		String transMsgOriginTypCd = "FFM";
		String stateCd = "ZZ";
		String exchangePolicyId = "REPROCESSSKIPSTEST";
		String hiosId = exchangePolicyId.substring(0, 5);
		int versionNum = 1;
		String versionNumStr = versionNum + "";
		LocalDateTime versionDt = MAR_1_3am;

		try {
			insertBatchRunControl("N");
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

			bemIndexWriter.setSql(erlBEMIndexInsert);
			List<BenefitEnrollmentMaintenanceDTO> bemDTOList = new ArrayList<BenefitEnrollmentMaintenanceDTO>();

			// Insert a transMsg and Batch and use versionId representing the first transaction that was skipped and will be processed as "this" transaction.
			String bemXmlMsg1a = getBemTransMsg(stateCd, exchangePolicyId, hiosId, versionDt.plusSeconds(3), "1", PolicyStatus.INITIAL_1);
			Long transMsgIdV1a = insertBemTransMsg(mockBatchId, transMsgOriginTypCd, bemXmlMsg1a);

			// Insert "this" transaction into the bemIndexer for processing
			BenefitEnrollmentMaintenanceDTO bemDTOV1 = makeBemDTO(mockBatchId, transMsgIdV1a, "1", versionDt, PolicyStatus.INITIAL_1, exchangePolicyId);
			bemDTOV1.getBem().getMember().add(TestDataUtil.makeSubscriber(stateCd, exchangePolicyId, hiosId, versionDt.toLocalDate(), PolicyStatus.INITIAL_1));

			bemDTOV1.setBemXml(marshallBEM(bemDTOV1.getBem()));

			bemDTOV1.setFileNm(expectedFileNm);
			bemDTOV1.setFileNmDateTime(expectedFileDateTime);
			bemDTOV1.setExchangeTypeCd(ExchangeType.FFM.getValue());
			FileInformationType fileInfo = TestDataUtil.makeFileInformationType(mockBatchId, expectedExchangeType);
			bemDTOV1.setFileInfoXml(marshallFileInfo(fileInfo));
			bemDTOV1.setSubscriberStateCd(stateCd);
			bemDTOV1.setPlanId(fileInfo.getGroupSenderID());

			bemDTOList.add(bemDTOV1);

			// Insert a transMsg and Batch and use versionId representing the first transaction that was skipped and will be processed as "this" transaction.
			String bemXmlMsg3a = getBemTransMsg(stateCd, exchangePolicyId, hiosId, versionDt.plusSeconds(4), "3", PolicyStatus.CANCELLED_3);
			Long transMsgIdV3a = insertBemTransMsg(mockBatchId, transMsgOriginTypCd, bemXmlMsg3a);

			// Insert "this" transaction into the bemIndexer for processing
			BenefitEnrollmentMaintenanceDTO bemDTOV3 = makeBemDTO(mockBatchId, transMsgIdV3a, "3", versionDt, PolicyStatus.CANCELLED_3, exchangePolicyId);
			bemDTOV3.getBem().getMember().add(TestDataUtil.makeSubscriber(stateCd, exchangePolicyId, hiosId, versionDt.toLocalDate(), PolicyStatus.CANCELLED_3));

			bemDTOV3.setBemXml(marshallBEM(bemDTOV3.getBem()));

			bemDTOV3.setFileNm(expectedFileNm);
			bemDTOV3.setFileNmDateTime(expectedFileDateTime);
			bemDTOV3.setExchangeTypeCd(ExchangeType.FFM.getValue());
			FileInformationType fileInfoV3 = TestDataUtil.makeFileInformationType(mockBatchId, expectedExchangeType);
			bemDTOV3.setFileInfoXml(marshallFileInfo(fileInfoV3));
			bemDTOV3.setSubscriberStateCd(stateCd);
			bemDTOV3.setPlanId(fileInfo.getGroupSenderID());

			bemDTOList.add(bemDTOV3);


			bemIndexWriter.write(bemDTOList);

			final DateFormatter dateFormatter = new DateFormatter("yyyy-MM-dd HH:mm:ss.SSS");
			final Map<String, JobParameter> params = new LinkedHashMap<String, JobParameter>();
			params.put("source", new JobParameter("ffm"));
			params.put("jobType", new JobParameter("processor"));
			params.put("timestamp", new JobParameter(dateFormatter.print(new Date(), Locale.US)));

			JobExecution jobExec = jobLauncherTestUtils.launchStep("bemHandlerFFM", new JobParameters(params));
			stepExec = getStepExecution(jobExec);
			jobId = stepExec.getJobExecution().getJobId();
			//
			String procToDbCd = getJdbc().queryForObject("SELECT PROCESSEDTODBSTATUSTYPECD FROM BATCHTRANSMSG WHERE TRANSMSGID = " + transMsgIdV1 + " AND BATCHID = " + mockBatchId, String.class);
			assertEquals("ProcessedToDbIndicator for transMsgId: " + transMsgIdV1, ProcessedToDbInd.D.getValue(), procToDbCd);

			procToDbCd = getJdbc().queryForObject("SELECT PROCESSEDTODBSTATUSTYPECD FROM BATCHTRANSMSG WHERE TRANSMSGID = " + transMsgIdV1a + " AND BATCHID = " + jobId, String.class);
			assertEquals("ProcessedToDbIndicator for transMsgId: " + transMsgIdV1a, expectedProcessedToDbInd.getValue(), procToDbCd);

			procToDbCd = getJdbc().queryForObject("SELECT PROCESSEDTODBSTATUSTYPECD FROM BATCHTRANSMSG WHERE TRANSMSGID = " + transMsgIdV2 + " AND BATCHID = " + mockBatchId, String.class);
			assertEquals("ProcessedToDbIndicator for transMsgId: " + transMsgIdV2, expectedProcessedToDbInd.getValue(), procToDbCd);

			procToDbCd = getJdbc().queryForObject("SELECT PROCESSEDTODBSTATUSTYPECD FROM BATCHTRANSMSG WHERE TRANSMSGID = " + transMsgIdV3 + " AND BATCHID = " + mockBatchId, String.class);
			assertEquals("ProcessedToDbIndicator for transMsgId: " + transMsgIdV3, ProcessedToDbInd.D.getValue(), procToDbCd);

			procToDbCd = getJdbc().queryForObject("SELECT PROCESSEDTODBSTATUSTYPECD FROM BATCHTRANSMSG WHERE TRANSMSGID = " + transMsgIdV3a + " AND BATCHID = " + jobId, String.class);
			assertEquals("ProcessedToDbIndicator for transMsgId: " + transMsgIdV3a, ProcessedToDbInd.R.getValue(), procToDbCd);

			/* Launch Bem Post Cleanup step*/
			/*			Integer bemIndexCountBeforeCleanupStep = getJdbc().queryForObject("SELECT Count(*) FROM DAILYBEMINDEXER WHERE EXCHANGETYPECD='FFM' AND SUBSCRIBERSTATECD = 'ZZ'", Integer.class);
			assertTrue("Bem Index count before 'bemPostCleanup' step", bemIndexCountBeforeCleanupStep.intValue()>0);

			params.put("timestamp", new JobParameter(dateFormatter.print(new Date(), Locale.US)));
			jobLauncherTestUtils.launchStep("bemPostCleanupErl", new JobParameters(params));*/

			Integer bemIdxCntAfterCleanUpStep = getJdbc().queryForObject("SELECT Count(*) FROM DAILYBEMINDEXER WHERE EXCHANGETYPECD='FFM' AND SUBSCRIBERSTATECD = 'ZZ'", Integer.class);
			assertEquals("Bem Index is cleaned up after 'bemPostCleanup' step", 0, bemIdxCntAfterCleanUpStep.intValue());

			////////////////////////////////First pass complete, over to second pass //////////////// 

			/* Launch stageReprocess step*/
			/*			params.put("timestamp", new JobParameter(dateFormatter.print(new Date(), Locale.US)));
			jobLauncherTestUtils.launchStep("stageReprocess", new JobParameters(params));

			Integer bemIdxCntAfterStageReprocessStep = getJdbc().queryForObject("SELECT Count(*) FROM DAILYBEMINDEXER WHERE EXCHANGETYPECD='FFM' AND TRANSMSGID IN("
					+ transMsgIdV1a + "," + transMsgIdV2 + "," + transMsgIdV3a + ")"
					, Integer.class);
			assertEquals("Bem Index is populated after 'stageReprocess' step", 3, bemIdxCntAfterStageReprocessStep.intValue());
			 */			
			/* Launch reProcessSkips step*/
			params.put("timestamp", new JobParameter(dateFormatter.print(new Date(), Locale.US)));
			JobExecution jobExecReprocess = jobLauncherTestUtils.launchStep("reProcessSkips", new JobParameters(params));
			StepExecution stepExecReprocess = getStepExecution(jobExecReprocess);
			jobIdReprocess = stepExecReprocess.getJobExecution().getJobId();

			Integer policyCntAfterReprocessStep = getJdbc().queryForObject("SELECT Count(*) FROM POLICYVERSION WHERE CREATEBY='"+jobIdReprocess+"'", Integer.class);
			assertEquals("Policy count after 'reprocess' step", 3, policyCntAfterReprocessStep.intValue());

			procToDbCd = getJdbc().queryForObject("SELECT PROCESSEDTODBSTATUSTYPECD FROM BATCHTRANSMSG WHERE TRANSMSGID = " + transMsgIdV1a + " AND BATCHID = " + jobIdReprocess, String.class);
			assertEquals("ProcessedToDbIndicator for transMsgId: " + transMsgIdV1a, ProcessedToDbInd.Y.getValue(), procToDbCd);

			procToDbCd = getJdbc().queryForObject("SELECT PROCESSEDTODBSTATUSTYPECD FROM BATCHTRANSMSG WHERE TRANSMSGID = " + transMsgIdV2 + " AND BATCHID = " + mockBatchId, String.class);
			assertEquals("ProcessedToDbIndicator for transMsgId: " + transMsgIdV2, ProcessedToDbInd.D.getValue(), procToDbCd);
			procToDbCd = getJdbc().queryForObject("SELECT PROCESSEDTODBSTATUSTYPECD FROM BATCHTRANSMSG WHERE TRANSMSGID = " + transMsgIdV2 + " AND BATCHID = " + jobIdReprocess, String.class);
			assertEquals("ProcessedToDbIndicator for transMsgId: " + transMsgIdV2, ProcessedToDbInd.Y.getValue(), procToDbCd);

			procToDbCd = getJdbc().queryForObject("SELECT PROCESSEDTODBSTATUSTYPECD FROM BATCHTRANSMSG WHERE TRANSMSGID = " + transMsgIdV3 + " AND BATCHID = " + mockBatchId, String.class);
			assertEquals("ProcessedToDbIndicator for transMsgId: " + transMsgIdV3, ProcessedToDbInd.D.getValue(), procToDbCd);
			procToDbCd = getJdbc().queryForObject("SELECT PROCESSEDTODBSTATUSTYPECD FROM BATCHTRANSMSG WHERE TRANSMSGID = " + transMsgIdV3a + " AND BATCHID = " + jobIdReprocess, String.class);
			assertEquals("ProcessedToDbIndicator for transMsgId: " + transMsgIdV3a, ProcessedToDbInd.Y.getValue(), procToDbCd);


			assertEquals("Batch Status value is COMPLETED", BatchStatus.COMPLETED, jobExec.getStatus());

		} finally {
			if(jobId != null) deleteTestData(jobId);
			if(jobIdReprocess != null) deleteTestData(jobIdReprocess);
			deleteTestData(mockBatchId);
			deleteSetUpData(mockBatchId);
			getJdbc().execute("DELETE FROM DAILYBEMINDEXER WHERE EXCHANGETYPECD='FFM' AND SUBSCRIBERSTATECD = 'ZZ'");
		}

	}

	/**
	 * This method will test the reprocessing of skipped erl records, with reprocessed version getting skipped.
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
	public void testDoStep_bemHandlerFFM_reprocessSkips_resultingSkips() throws Exception {

		// clean up prior and after incase previous test failed.
		getJdbc().execute("DELETE FROM STAGINGPLANLOCK ");
		getJdbc().execute("DELETE FROM DAILYBEMINDEXER WHERE EXCHANGETYPECD='FFM' AND SUBSCRIBERSTATECD = 'ZZ'");
		StepExecution stepExec = null;
		Long jobId = null;
		Long mockBatchId = TestDataUtil.getRandomNumber(8);
		Long jobIdReprocess = null;

		String expectedFileNm = makeFileNameERL(APR_1_4am);
		LocalDateTime expectedFileDateTime = getFileNameDateTime(expectedFileNm);
		ExchangeType expectedExchangeType = ExchangeType.FFM;
		ProcessedToDbInd expectedProcessedToDbInd = ProcessedToDbInd.R;

		String transMsgOriginTypCd = "FFM";
		String stateCd = "ZZ";
		String exchangePolicyId = "REPROCESSSKIPSTEST";
		String hiosId = exchangePolicyId.substring(0, 5);
		int versionNum = 1;
		String versionNumStr = versionNum + "";
		LocalDateTime versionDt = MAR_1_3am;

		try {
			insertBatchRunControl("N");
			// Insert a batchTrans's with same policy as "this" one representing a previous transaction version that was processed as 'S'
			// pass in batchId for CREATEBY used by delete script.
			Long transMsgIdV1 = insertTransMsg(mockBatchId, transMsgOriginTypCd);
			insertBatchTransMsg(transMsgIdV1, mockBatchId, ProcessedToDbInd.S, stateCd, exchangePolicyId, hiosId, versionNumStr, versionDt);

			// Insert a skipped batchTransMsg and increment versionId representing a later "skipped" transaction for this policy
			versionNum++;
			versionNumStr = versionNum +"";

			String bemXlMsg2 = getBemTransMsg(stateCd, exchangePolicyId, hiosId, versionDt.plusSeconds(1), versionNumStr, PolicyStatus.EFFECTUATED_2)
					.replaceAll("<PolicyStatus>2</PolicyStatus>", "<PolicyStatus>0</PolicyStatus>");
			Long transMsgIdV2 = insertBemTransMsg(mockBatchId, transMsgOriginTypCd, bemXlMsg2);
			insertBatchTransMsg(transMsgIdV2, mockBatchId, ProcessedToDbInd.S, stateCd, exchangePolicyId, hiosId, versionNumStr, versionDt);

			// Insert a skipped batchTransMsg and increment versionId representing a later "skipped" transaction for this policy
			versionNum++;
			versionNumStr = versionNum +"";
			String bemXlMsg3 = getBemTransMsg(stateCd, exchangePolicyId, hiosId, versionDt.plusSeconds(2), versionNumStr, PolicyStatus.CANCELLED_3);
			Long transMsgIdV3 = insertBemTransMsg(mockBatchId, transMsgOriginTypCd, bemXlMsg3);
			insertBatchTransMsg(transMsgIdV3, mockBatchId, ProcessedToDbInd.S, stateCd, exchangePolicyId, hiosId, versionNumStr, versionDt);

			// Insert a transMsg and Batch and use versionId representing the first transaction that was skipped and will be processed as "this" transaction.
			String bemXmlMsg1a = getBemTransMsg(stateCd, exchangePolicyId, hiosId, versionDt.plusSeconds(3), "1", PolicyStatus.INITIAL_1);
			Long transMsgIdV1a = insertBemTransMsg(mockBatchId, transMsgOriginTypCd, bemXmlMsg1a);

			// Insert "this" transaction into the bemIndexer for processing
			BenefitEnrollmentMaintenanceDTO bemDTO = makeBemDTO(mockBatchId, transMsgIdV1a, "1", versionDt, PolicyStatus.INITIAL_1, exchangePolicyId);
			bemDTO.getBem().getMember().add(TestDataUtil.makeSubscriber(stateCd, exchangePolicyId, hiosId, versionDt.toLocalDate(), PolicyStatus.INITIAL_1));

			bemDTO.setBemXml(marshallBEM(bemDTO.getBem()));

			bemDTO.setFileNm(expectedFileNm);
			bemDTO.setFileNmDateTime(expectedFileDateTime);
			bemDTO.setExchangeTypeCd(ExchangeType.FFM.getValue());
			FileInformationType fileInfo = TestDataUtil.makeFileInformationType(mockBatchId, expectedExchangeType);
			bemDTO.setFileInfoXml(marshallFileInfo(fileInfo));
			bemDTO.setSubscriberStateCd(stateCd);
			bemDTO.setPlanId(fileInfo.getGroupSenderID());

			bemIndexWriter.setSql(erlBEMIndexInsert);
			List<BenefitEnrollmentMaintenanceDTO> bemDTOList = new ArrayList<BenefitEnrollmentMaintenanceDTO>();
			bemDTOList.add(bemDTO);

			bemIndexWriter.write(bemDTOList);

			final DateFormatter dateFormatter = new DateFormatter("yyyy-MM-dd HH:mm:ss.SSS");
			final Map<String, JobParameter> params = new LinkedHashMap<String, JobParameter>();
			params.put("source", new JobParameter("ffm"));
			params.put("jobType", new JobParameter("processor"));
			params.put("timestamp", new JobParameter(dateFormatter.print(new Date(), Locale.US)));

			JobExecution jobExec = jobLauncherTestUtils.launchStep("bemHandlerFFM", new JobParameters(params));
			stepExec = getStepExecution(jobExec);
			jobId = stepExec.getJobExecution().getJobId();
			//
			String procToDbCd = getJdbc().queryForObject("SELECT PROCESSEDTODBSTATUSTYPECD FROM BATCHTRANSMSG WHERE TRANSMSGID = " + transMsgIdV1 + " AND BATCHID = " + mockBatchId, String.class);
			assertEquals("ProcessedToDbIndicator for transMsgId: " + transMsgIdV1, ProcessedToDbInd.D.getValue(), procToDbCd);

			procToDbCd = getJdbc().queryForObject("SELECT PROCESSEDTODBSTATUSTYPECD FROM BATCHTRANSMSG WHERE TRANSMSGID = " + transMsgIdV1a + " AND BATCHID = " + jobId, String.class);
			assertEquals("ProcessedToDbIndicator for transMsgId: " + transMsgIdV1a, expectedProcessedToDbInd.getValue(), procToDbCd);

			procToDbCd = getJdbc().queryForObject("SELECT PROCESSEDTODBSTATUSTYPECD FROM BATCHTRANSMSG WHERE TRANSMSGID = " + transMsgIdV2 + " AND BATCHID = " + mockBatchId, String.class);
			assertEquals("ProcessedToDbIndicator for transMsgId: " + transMsgIdV2, expectedProcessedToDbInd.getValue(), procToDbCd);

			procToDbCd = getJdbc().queryForObject("SELECT PROCESSEDTODBSTATUSTYPECD FROM BATCHTRANSMSG WHERE TRANSMSGID = " + transMsgIdV3 + " AND BATCHID = " + mockBatchId, String.class);
			assertEquals("ProcessedToDbIndicator for transMsgId: " + transMsgIdV3, expectedProcessedToDbInd.getValue(), procToDbCd);

			Integer bemIdxCntAfterCleanUpStep = getJdbc().queryForObject("SELECT Count(*) FROM DAILYBEMINDEXER WHERE EXCHANGETYPECD='FFM' AND SUBSCRIBERSTATECD = 'ZZ'", Integer.class);
			assertEquals("Bem Index is cleaned up after 'bemPostCleanup' step", 0, bemIdxCntAfterCleanUpStep.intValue());

			////////////////////////////////First pass complete, over to second pass //////////////// 

			/* Launch reProcessSkips step*/
			params.put("timestamp", new JobParameter(dateFormatter.print(new Date(), Locale.US)));
			JobExecution jobExecReprocess = jobLauncherTestUtils.launchStep("reProcessSkips", new JobParameters(params));
			StepExecution stepExecReprocess = getStepExecution(jobExecReprocess);
			jobIdReprocess = stepExecReprocess.getJobExecution().getJobId();

			Integer policyCntAfterReprocessStep = getJdbc().queryForObject("SELECT Count(*) FROM POLICYVERSION WHERE CREATEBY='"+jobIdReprocess+"'", Integer.class);
			assertEquals("Policy count after 'reprocess' step", 1, policyCntAfterReprocessStep.intValue());

			procToDbCd = getJdbc().queryForObject("SELECT PROCESSEDTODBSTATUSTYPECD FROM BATCHTRANSMSG WHERE TRANSMSGID = " + transMsgIdV1a + " AND BATCHID = " + jobIdReprocess, String.class);
			assertEquals("ProcessedToDbIndicator for transMsgId: " + transMsgIdV1a, ProcessedToDbInd.Y.getValue(), procToDbCd);

			procToDbCd = getJdbc().queryForObject("SELECT PROCESSEDTODBSTATUSTYPECD FROM BATCHTRANSMSG WHERE TRANSMSGID = " + transMsgIdV2 + " AND BATCHID = " + mockBatchId, String.class);
			assertEquals("ProcessedToDbIndicator for transMsgId: " + transMsgIdV2, ProcessedToDbInd.D.getValue(), procToDbCd);

			procToDbCd = getJdbc().queryForObject("SELECT PROCESSEDTODBSTATUSTYPECD FROM BATCHTRANSMSG WHERE TRANSMSGID = " + transMsgIdV2 + " AND BATCHID = " + jobIdReprocess, String.class);
			assertEquals("ProcessedToDbIndicator for transMsgId: " + transMsgIdV2, ProcessedToDbInd.S.getValue(), procToDbCd);

			procToDbCd = getJdbc().queryForObject("SELECT PROCESSEDTODBSTATUSTYPECD FROM BATCHTRANSMSG WHERE TRANSMSGID = " + transMsgIdV3 + " AND BATCHID = " + mockBatchId, String.class);
			assertEquals("ProcessedToDbIndicator for transMsgId: " + transMsgIdV3, ProcessedToDbInd.D.getValue(), procToDbCd);

			procToDbCd = getJdbc().queryForObject("SELECT PROCESSEDTODBSTATUSTYPECD FROM BATCHTRANSMSG WHERE TRANSMSGID = " + transMsgIdV3 + " AND BATCHID = " + jobIdReprocess, String.class);
			assertEquals("ProcessedToDbIndicator for transMsgId: " + transMsgIdV3, ProcessedToDbInd.S.getValue(), procToDbCd);

			assertEquals("Batch Status value is COMPLETED", BatchStatus.COMPLETED, jobExec.getStatus());

		} finally {
			if(jobId != null) deleteTestData(jobId);
			if(jobIdReprocess != null) deleteTestData(jobIdReprocess);
			deleteTestData(mockBatchId);
			deleteSetUpData(mockBatchId);
			getJdbc().execute("DELETE FROM DAILYBEMINDEXER WHERE EXCHANGETYPECD='FFM' AND SUBSCRIBERSTATECD = 'ZZ'");
		}

	}

	/**
	 * This method will test the reprocessing of skipped erl records, with a later version(V2) being sent in
	 * for reprocessing instead of the first skipped version(V1).
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
	public void testDoStep_bemHandlerFFM_reprocessSkips_resultingSkips_V2Inbound() throws Exception {

		// clean up prior and after incase previous test failed.
		getJdbc().execute("DELETE FROM STAGINGPLANLOCK ");
		getJdbc().execute("DELETE FROM DAILYBEMINDEXER WHERE EXCHANGETYPECD='FFM' AND SUBSCRIBERSTATECD = 'ZZ'");
		StepExecution stepExec = null;
		Long jobId = null;
		Long mockBatchId = TestDataUtil.getRandomNumber(8);
		Long jobIdReprocess = null;

		String expectedFileNm = makeFileNameERL(APR_1_4am);
		LocalDateTime expectedFileDateTime = getFileNameDateTime(expectedFileNm);
		ExchangeType expectedExchangeType = ExchangeType.FFM;

		String transMsgOriginTypCd = "FFM";
		String stateCd = "ZZ";
		String exchangePolicyId = "REPROCESSSKIPSTEST";
		String hiosId = exchangePolicyId.substring(0, 5);
		int versionNum = 1;
		String versionNumStr = versionNum + "";
		LocalDateTime versionDt = MAR_1_3am;

		try {
			insertBatchRunControl("N");
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

			// Insert a transMsg and Batch and use versionId representing the second transaction that was skipped and will be processed as "this" transaction.
			String bemXmlMsg2a = getBemTransMsg(stateCd, exchangePolicyId, hiosId, versionDt.plusSeconds(3), "2", PolicyStatus.INITIAL_1);
			Long transMsgIdV2a = insertBemTransMsg(mockBatchId, transMsgOriginTypCd, bemXmlMsg2a);

			// Insert "this" transaction into the bemIndexer for processing
			BenefitEnrollmentMaintenanceDTO bemDTO = makeBemDTO(mockBatchId, transMsgIdV2a, "2", versionDt, PolicyStatus.INITIAL_1, exchangePolicyId);
			bemDTO.getBem().getMember().add(TestDataUtil.makeSubscriber(stateCd, exchangePolicyId, hiosId, versionDt.toLocalDate(), PolicyStatus.INITIAL_1));

			bemDTO.setBemXml(marshallBEM(bemDTO.getBem()));

			bemDTO.setFileNm(expectedFileNm);
			bemDTO.setFileNmDateTime(expectedFileDateTime);
			bemDTO.setExchangeTypeCd(ExchangeType.FFM.getValue());
			FileInformationType fileInfo = TestDataUtil.makeFileInformationType(mockBatchId, expectedExchangeType);
			bemDTO.setFileInfoXml(marshallFileInfo(fileInfo));
			bemDTO.setSubscriberStateCd(stateCd);
			bemDTO.setPlanId(fileInfo.getGroupSenderID());

			bemIndexWriter.setSql(erlBEMIndexInsert);
			List<BenefitEnrollmentMaintenanceDTO> bemDTOList = new ArrayList<BenefitEnrollmentMaintenanceDTO>();
			bemDTOList.add(bemDTO);

			bemIndexWriter.write(bemDTOList);

			final DateFormatter dateFormatter = new DateFormatter("yyyy-MM-dd HH:mm:ss.SSS");
			final Map<String, JobParameter> params = new LinkedHashMap<String, JobParameter>();
			params.put("source", new JobParameter("ffm"));
			params.put("jobType", new JobParameter("processor"));
			params.put("timestamp", new JobParameter(dateFormatter.print(new Date(), Locale.US)));

			JobExecution jobExec = jobLauncherTestUtils.launchStep("bemHandlerFFM", new JobParameters(params));
			stepExec = getStepExecution(jobExec);
			jobId = stepExec.getJobExecution().getJobId();
			//
			String procToDbCd = getJdbc().queryForObject("SELECT PROCESSEDTODBSTATUSTYPECD FROM BATCHTRANSMSG WHERE TRANSMSGID = " + transMsgIdV1 + " AND BATCHID = " + mockBatchId, String.class);
			assertEquals("ProcessedToDbIndicator for transMsgId: " + transMsgIdV1, ProcessedToDbInd.S.getValue(), procToDbCd);

			procToDbCd = getJdbc().queryForObject("SELECT PROCESSEDTODBSTATUSTYPECD FROM BATCHTRANSMSG WHERE TRANSMSGID = " + transMsgIdV2 + " AND BATCHID = " + mockBatchId, String.class);
			assertEquals("ProcessedToDbIndicator for transMsgId: " + transMsgIdV2, ProcessedToDbInd.D.getValue(), procToDbCd);

			procToDbCd = getJdbc().queryForObject("SELECT PROCESSEDTODBSTATUSTYPECD FROM BATCHTRANSMSG WHERE TRANSMSGID = " + transMsgIdV2a + " AND BATCHID = " + jobId, String.class);
			assertEquals("ProcessedToDbIndicator for transMsgId: " + transMsgIdV2a, ProcessedToDbInd.S.getValue(), procToDbCd);

			procToDbCd = getJdbc().queryForObject("SELECT PROCESSEDTODBSTATUSTYPECD FROM BATCHTRANSMSG WHERE TRANSMSGID = " + transMsgIdV3 + " AND BATCHID = " + mockBatchId, String.class);
			assertEquals("ProcessedToDbIndicator for transMsgId: " + transMsgIdV3, ProcessedToDbInd.S.getValue(), procToDbCd);

			/* Launch Bem Post Cleanup step*/
			/*			Integer bemIndexCountBeforeCleanupStep = getJdbc().queryForObject("SELECT Count(*) FROM DAILYBEMINDEXER WHERE EXCHANGETYPECD='FFM' AND SUBSCRIBERSTATECD = 'ZZ'", Integer.class);
			assertTrue("Bem Index count before 'bemPostCleanup' step", bemIndexCountBeforeCleanupStep.intValue()>0);

			params.put("timestamp", new JobParameter(dateFormatter.print(new Date(), Locale.US)));
			jobLauncherTestUtils.launchStep("bemPostCleanupErl", new JobParameters(params));*/

			Integer bemIdxCntAfterCleanUpStep = getJdbc().queryForObject("SELECT Count(*) FROM DAILYBEMINDEXER WHERE EXCHANGETYPECD='FFM' AND SUBSCRIBERSTATECD = 'ZZ'", Integer.class);
			assertEquals("Bem Index is cleaned up after 'bemPostCleanup' step", 0, bemIdxCntAfterCleanUpStep.intValue());

			////////////////////////////////First pass complete, over to second pass //////////////// 

			/* Launch stageReprocess step*/
			/*			params.put("timestamp", new JobParameter(dateFormatter.print(new Date(), Locale.US)));
			jobLauncherTestUtils.launchStep("stageReprocess", new JobParameters(params));

			Integer bemIdxCntAfterStageReprocessStep = getJdbc().queryForObject("SELECT Count(*) FROM DAILYBEMINDEXER WHERE EXCHANGETYPECD='FFM' AND TRANSMSGID IN("
					+ transMsgIdV2a + "," + transMsgIdV3 + ")"
					, Integer.class);
			assertEquals("Bem Index is populated after 'stageReprocess' step", 0, bemIdxCntAfterStageReprocessStep.intValue());
			 */			
			/* Launch reProcessSkips step*/
			params.put("timestamp", new JobParameter(dateFormatter.print(new Date(), Locale.US)));
			JobExecution jobExecReprocess = jobLauncherTestUtils.launchStep("reProcessSkips", new JobParameters(params));
			StepExecution stepExecReprocess = getStepExecution(jobExecReprocess);
			jobIdReprocess = stepExecReprocess.getJobExecution().getJobId();

			Integer policyCntAfterReprocessStep = getJdbc().queryForObject("SELECT Count(*) FROM POLICYVERSION WHERE CREATEBY='"+jobIdReprocess+"'", Integer.class);
			assertEquals("Policy count after 'reprocess' step", 0, policyCntAfterReprocessStep.intValue());

			procToDbCd = getJdbc().queryForObject("SELECT PROCESSEDTODBSTATUSTYPECD FROM BATCHTRANSMSG WHERE TRANSMSGID = " + transMsgIdV1 + " AND BATCHID = " + mockBatchId, String.class);
			assertEquals("ProcessedToDbIndicator for transMsgId: " + transMsgIdV1, ProcessedToDbInd.S.getValue(), procToDbCd);

			procToDbCd = getJdbc().queryForObject("SELECT PROCESSEDTODBSTATUSTYPECD FROM BATCHTRANSMSG WHERE TRANSMSGID = " + transMsgIdV2 + " AND BATCHID = " + mockBatchId, String.class);
			assertEquals("ProcessedToDbIndicator for transMsgId: " + transMsgIdV2, ProcessedToDbInd.D.getValue(), procToDbCd);

			procToDbCd = getJdbc().queryForObject("SELECT PROCESSEDTODBSTATUSTYPECD FROM BATCHTRANSMSG WHERE TRANSMSGID = " + transMsgIdV2a + " AND BATCHID = " + jobId, String.class);
			assertEquals("ProcessedToDbIndicator for transMsgId: " + transMsgIdV2a, ProcessedToDbInd.S.getValue(), procToDbCd);

			procToDbCd = getJdbc().queryForObject("SELECT PROCESSEDTODBSTATUSTYPECD FROM BATCHTRANSMSG WHERE TRANSMSGID = " + transMsgIdV3 + " AND BATCHID = " + mockBatchId, String.class);
			assertEquals("ProcessedToDbIndicator for transMsgId: " + transMsgIdV3, ProcessedToDbInd.S.getValue(), procToDbCd);


			assertEquals("Batch Status value is COMPLETED", BatchStatus.COMPLETED, jobExec.getStatus());

		} finally {
			if(jobId != null) deleteTestData(jobId);
			if(jobIdReprocess != null) deleteTestData(jobIdReprocess);
			deleteTestData(mockBatchId);
			deleteSetUpData(mockBatchId);
			getJdbc().execute("DELETE FROM DAILYBEMINDEXER WHERE EXCHANGETYPECD='FFM' AND SUBSCRIBERSTATECD = 'ZZ'");
		}

	}

	public String getBemTransMsg(String stateCd, String exchangePolicyId,
			String hiosId, LocalDateTime versionDt, String versionNumStr, PolicyStatus policyStatus) throws Exception {
		BenefitEnrollmentMaintenanceType bem = makeBem(versionNumStr, versionDt, policyStatus, exchangePolicyId);
		bem.getMember().add(TestDataUtil.makeSubscriber(stateCd, exchangePolicyId, hiosId, versionDt.toLocalDate(), policyStatus));

		String bemXml = marshallBEM(bem);
		return bemXml;
	}

	private StepExecution getStepExecution(JobExecution jobExec) {

		Collection<StepExecution> colStepExec = jobExec.getStepExecutions();
		Iterator<StepExecution> iter = colStepExec.iterator();
		// only one step launched
		return iter.next();
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

}
