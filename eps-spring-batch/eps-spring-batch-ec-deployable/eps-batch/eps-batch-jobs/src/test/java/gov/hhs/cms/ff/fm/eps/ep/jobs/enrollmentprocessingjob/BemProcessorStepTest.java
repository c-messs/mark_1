package gov.hhs.cms.ff.fm.eps.ep.jobs.enrollmentprocessingjob;

import java.io.IOException;
import java.io.StringWriter;
import java.time.LocalDate;
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
import org.springframework.context.ApplicationContext;
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
 * @author j.radziewski
 */
@ContextConfiguration(locations={"/test-batch-application-context.xml", "/test-context-eps.xml"})
@DirtiesContext(classMode = ClassMode.AFTER_EACH_TEST_METHOD)
@RunWith(SpringJUnit4ClassRunner.class)
@TestExecutionListeners({DependencyInjectionTestExecutionListener.class, 
	TransactionalTestExecutionListener.class, DirtiesContextTestExecutionListener.class })
@TransactionConfiguration(transactionManager = "transactionManager", defaultRollback = true)
public class BemProcessorStepTest extends BaseBatchTest {

	@Autowired
	private JobLauncherTestUtils  jobLauncherTestUtils;

	@Autowired
	private JdbcBatchItemWriter<BenefitEnrollmentMaintenanceDTO> bemIndexWriter;

	@Autowired
	private String erlBEMIndexInsert;

	@Autowired
	ApplicationContext context;

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
	public void setup() throws IOException {	}

	@After
	public void tearDown() {
/*		Ehcache cache = (Ehcache) context.getBean("cache");
        cache.getCacheManager().clearAll();*/
	}


	@Test
	public void testDoStep_bemHandlerFFM_EPROD29() throws Exception {

		// clean up prior and after incase previous test failed.
		getJdbc().execute("DELETE FROM STAGINGPLANLOCK ");
		getJdbc().execute("DELETE FROM DAILYBEMINDEXER WHERE EXCHANGETYPECD='FFM' AND SUBSCRIBERSTATECD = 'VA'");
		
		Long mockBatchId = TestDataUtil.getRandomNumber(8);
		StepExecution stepExec = null;
		Long jobId = null;
		String expectedFileNm = makeFileNameERL(APR_1_4am);
		LocalDateTime expectedFileDateTime = getFileNameDateTime(expectedFileNm);
		ExchangeType expectedExchangeType = ExchangeType.FFM;
		ProcessedToDbInd expectedProcessedToDbInd = ProcessedToDbInd.S;

		String stateCd = "VA";
		String exchangePolicyId = TestDataUtil.getRandomNumberAsString(8);
		String hiosId = exchangePolicyId.substring(0, 5);
		int versionNum = TestDataUtil.getRandom3DigitNumber().intValue();
		String versionNumStr = versionNum + "";
		LocalDateTime versionDt = MAR_1_3am;

		try {
			insertBatchRunControl("N");
			// Insert a batchTrans's with same policy as "this" one representing a previous transaction version that was processed as 'Y'
			// pass in batchId for CREATEBY used by delete script.
			Long transMsgId = insertTransMsg(mockBatchId);
			insertBatchTransMsg(transMsgId, mockBatchId, ProcessedToDbInd.Y, stateCd, exchangePolicyId, hiosId, versionNumStr, versionDt);
	
			// Insert a skipped batchTransMsg and increment versionId representing a previous "skipped" transaction for this policy
			transMsgId = insertTransMsg(mockBatchId);
			versionNum++;
			versionNumStr = versionNum +"";
			insertBatchTransMsg(transMsgId, mockBatchId, ProcessedToDbInd.S, stateCd, exchangePolicyId, hiosId, versionNumStr, versionDt);
	
	
			// Insert a transMsg and Batch and increment versionId representing "this" transaction that will be processed.
			transMsgId = insertTransMsg(mockBatchId);
			insertBatchTransMsg(transMsgId, mockBatchId);
			versionNum++;
			versionNumStr = versionNum +"";
	
			// Insert "this" transaction into the bemIndexer for processing
			BenefitEnrollmentMaintenanceDTO bemDTO = makeBemDTO(mockBatchId, transMsgId, versionNumStr, versionDt, PolicyStatus.EFFECTUATED_2, exchangePolicyId);
			bemDTO.getBem().getMember().add(TestDataUtil.makeSubscriber(stateCd, exchangePolicyId, hiosId));
	
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
			
			String procToDbCd = getJdbc().queryForObject("SELECT PROCESSEDTODBSTATUSTYPECD FROM BATCHTRANSMSG WHERE TRANSMSGID = " + transMsgId + " AND BATCHID = " + jobId, String.class);
			
			assertEquals("ProcessedToDbIndicator for transMsgId: " + transMsgId, expectedProcessedToDbInd.getValue(), procToDbCd);
	
			assertEquals("Batch Status value is COMPLETED", BatchStatus.COMPLETED, jobExec.getStatus());

		} finally {
			if(jobId != null) deleteBatchData(stepExec, jobId);
			if(jobId != null) deleteTestData(jobId);
			deleteSetUpData(mockBatchId);
			getJdbc().execute("DELETE FROM DAILYBEMINDEXER WHERE EXCHANGETYPECD='FFM' AND SUBSCRIBERSTATECD = 'VA'");
		}
	}

	@Test
	public void testDoStep_bemHandlerFFM_setSkipsToReprocess() throws Exception {

		// clean up prior and after incase previous test failed.
		getJdbc().execute("DELETE FROM STAGINGPLANLOCK ");
		getJdbc().execute("DELETE FROM DAILYBEMINDEXER WHERE EXCHANGETYPECD='FFM' AND SUBSCRIBERSTATECD = 'ZZ'");
		StepExecution stepExec = null;
		Long jobId = null;
		Long mockBatchId = TestDataUtil.getRandomNumber(8);

		String expectedFileNm = makeFileNameERL(APR_1_4am);
		LocalDateTime expectedFileDateTime = getFileNameDateTime(expectedFileNm);
		ExchangeType expectedExchangeType = ExchangeType.FFM;
		//String expectedErrMsg = "EPROD-29";
		ProcessedToDbInd expectedProcessedToDbInd = ProcessedToDbInd.R;

		String stateCd = "ZZ";
		String exchangePolicyId = "REPROCESSFLAGTEST";//TestDataUtil.getRandomNumberAsString(8);
		String hiosId = exchangePolicyId.substring(0, 5);
		int versionNum = 1;
		String versionNumStr = versionNum + "";
		LocalDateTime versionDt = MAR_1_3am;
		
		try {
			insertBatchRunControl("N");
			// Insert a batchTrans's with same policy as "this" one representing a previous transaction version that was processed as 'S'
			// pass in batchId for CREATEBY used by delete script.
			Long transMsgIdV1 = insertTransMsg(mockBatchId);
			insertBatchTransMsg(transMsgIdV1, mockBatchId, ProcessedToDbInd.S, stateCd, exchangePolicyId, hiosId, versionNumStr, versionDt);
	
			// Insert a skipped batchTransMsg and increment versionId representing a later "skipped" transaction for this policy
			Long transMsgIdV2 = insertTransMsg(mockBatchId);
			versionNum++;
			versionNumStr = versionNum +"";
			insertBatchTransMsg(transMsgIdV2, mockBatchId, ProcessedToDbInd.S, stateCd, exchangePolicyId, hiosId, versionNumStr, versionDt);
			// Insert a skipped batchTransMsg and increment versionId representing a later "skipped" transaction for this policy
			Long transMsgIdV3 = insertTransMsg(mockBatchId);
			versionNum++;
			versionNumStr = versionNum +"";
			insertBatchTransMsg(transMsgIdV3, mockBatchId, ProcessedToDbInd.S, stateCd, exchangePolicyId, hiosId, versionNumStr, versionDt);
			
	
			// Insert a transMsg and Batch and use versionId representing the first transaction that was skipped and will be processed as "this" transaction.
			Long transMsgIdV1a = insertTransMsg(mockBatchId);
			insertBatchTransMsg(transMsgIdV1a, mockBatchId);
			//versionNum++;
			//versionNumStr = versionNum +"";
	
			// Insert "this" transaction into the bemIndexer for processing
			BenefitEnrollmentMaintenanceDTO bemDTO = makeBemDTO(mockBatchId, transMsgIdV1a, "1", versionDt, PolicyStatus.EFFECTUATED_2, exchangePolicyId);
			bemDTO.getBem().getMember().add(TestDataUtil.makeSubscriber(stateCd, exchangePolicyId, hiosId));
	
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
			
			String procToDbCd = getJdbc().queryForObject("SELECT PROCESSEDTODBSTATUSTYPECD FROM BATCHTRANSMSG WHERE TRANSMSGID = " + transMsgIdV1 + " AND BATCHID = " + mockBatchId, String.class);
			assertEquals("ProcessedToDbIndicator for transMsgId V1: " + transMsgIdV1, ProcessedToDbInd.D.getValue(), procToDbCd);
			
			procToDbCd = getJdbc().queryForObject("SELECT PROCESSEDTODBSTATUSTYPECD FROM BATCHTRANSMSG WHERE TRANSMSGID = " + transMsgIdV1a + " AND BATCHID = " + jobId, String.class);
			assertEquals("ProcessedToDbIndicator for transMsgId V1a: " + transMsgIdV1a, expectedProcessedToDbInd.getValue(), procToDbCd);

			procToDbCd = getJdbc().queryForObject("SELECT PROCESSEDTODBSTATUSTYPECD FROM BATCHTRANSMSG WHERE TRANSMSGID = " + transMsgIdV2 + " AND BATCHID = " + mockBatchId, String.class);
			assertEquals("ProcessedToDbIndicator for transMsgId V2: " + transMsgIdV2, expectedProcessedToDbInd.getValue(), procToDbCd);

			procToDbCd = getJdbc().queryForObject("SELECT PROCESSEDTODBSTATUSTYPECD FROM BATCHTRANSMSG WHERE TRANSMSGID = " + transMsgIdV3 + " AND BATCHID = " + mockBatchId, String.class);
			assertEquals("ProcessedToDbIndicator for transMsgId V3: " + transMsgIdV3, expectedProcessedToDbInd.getValue(), procToDbCd);
			
			assertEquals("Batch Status value is COMPLETED", BatchStatus.COMPLETED, jobExec.getStatus());
			
		} finally {
			if(jobId != null) deleteBatchData(stepExec, jobId);
			if(jobId != null) deleteTestData(jobId);
			deleteSetUpData(mockBatchId);
			getJdbc().execute("DELETE FROM DAILYBEMINDEXER WHERE EXCHANGETYPECD='FFM' AND SUBSCRIBERSTATECD = 'ZZ'");
		}
		
	}
	
	@Test
	public void testDoStep_bemHandlerFFM_setSkipsToReprocess_multipleVersionsInbound() throws Exception {

		// clean up prior and after incase previous test failed.
		getJdbc().execute("DELETE FROM STAGINGPLANLOCK ");
		getJdbc().execute("DELETE FROM DAILYBEMINDEXER WHERE EXCHANGETYPECD='FFM' AND SUBSCRIBERSTATECD = 'ZZ'");
		StepExecution stepExec = null;
		Long jobId = null;
		Long mockBatchId = TestDataUtil.getRandomNumber(8);

		String expectedFileNm = makeFileNameERL(APR_1_4am);
		LocalDateTime expectedFileDateTime = getFileNameDateTime(expectedFileNm);
		ExchangeType expectedExchangeType = ExchangeType.FFM;
		//String expectedErrMsg = "EPROD-29";
		ProcessedToDbInd expectedProcessedToDbInd = ProcessedToDbInd.R;

		String stateCd = "ZZ";
		String exchangePolicyId = "REPROCESSFLAGTEST";//TestDataUtil.getRandomNumberAsString(8);
		String hiosId = exchangePolicyId.substring(0, 5);
		int versionNum = 1;
		String versionNumStr = versionNum + "";
		LocalDateTime versionDt = MAR_1_3am;
		
		try {
			insertBatchRunControl("N");
			// Insert a batchTrans's with same policy as "this" one representing a previous transaction version that was processed as 'S'
			// pass in batchId for CREATEBY used by delete script.
			Long transMsgIdV1 = insertTransMsg(mockBatchId);
			insertBatchTransMsg(transMsgIdV1, mockBatchId, ProcessedToDbInd.S, stateCd, exchangePolicyId, hiosId, versionNumStr, versionDt);
	
			// Insert a skipped batchTransMsg and increment versionId representing a later "skipped" transaction for this policy
			Long transMsgIdV2 = insertTransMsg(mockBatchId);
			versionNum++;
			versionNumStr = versionNum +"";
			insertBatchTransMsg(transMsgIdV2, mockBatchId, ProcessedToDbInd.S, stateCd, exchangePolicyId, hiosId, versionNumStr, versionDt);
			// Insert a skipped batchTransMsg and increment versionId representing a later "skipped" transaction for this policy
			Long transMsgIdV3 = insertTransMsg(mockBatchId);
			versionNum++;
			versionNumStr = versionNum +"";
			insertBatchTransMsg(transMsgIdV3, mockBatchId, ProcessedToDbInd.S, stateCd, exchangePolicyId, hiosId, versionNumStr, versionDt);
			
			bemIndexWriter.setSql(erlBEMIndexInsert);
			List<BenefitEnrollmentMaintenanceDTO> bemDTOList = new ArrayList<BenefitEnrollmentMaintenanceDTO>();
	
			// Insert a transMsg and Batch and use versionId representing the first transaction that was skipped and will be processed as "this" transaction.
			Long transMsgIdV1a = insertTransMsg(mockBatchId);
			insertBatchTransMsg(transMsgIdV1a, mockBatchId);
			//versionNum++;
			//versionNumStr = versionNum +"";
	
			// Insert "this" transaction into the bemIndexer for processing
			BenefitEnrollmentMaintenanceDTO bemDTOV1 = makeBemDTO(mockBatchId, transMsgIdV1a, "1", versionDt, PolicyStatus.EFFECTUATED_2, exchangePolicyId);
			bemDTOV1.getBem().getMember().add(TestDataUtil.makeSubscriber(stateCd, exchangePolicyId, hiosId));
	
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
			Long transMsgIdV3a = insertTransMsg(mockBatchId);
			insertBatchTransMsg(transMsgIdV3a, mockBatchId);
			//versionNum++;
			//versionNumStr = versionNum +"";
	
			// Insert "this" transaction into the bemIndexer for processing
			BenefitEnrollmentMaintenanceDTO bemDTOV3 = makeBemDTO(mockBatchId, transMsgIdV3a, "3", versionDt, PolicyStatus.EFFECTUATED_2, exchangePolicyId);
			bemDTOV3.getBem().getMember().add(TestDataUtil.makeSubscriber(stateCd, exchangePolicyId, hiosId));
	
			bemDTOV3.setBemXml(marshallBEM(bemDTOV3.getBem()));
	
			bemDTOV3.setFileNm(expectedFileNm);
			bemDTOV3.setFileNmDateTime(expectedFileDateTime);
			bemDTOV3.setExchangeTypeCd(ExchangeType.FFM.getValue());
			//FileInformationType fileInfo = TestDataUtil.makeFileInformationType(mockBatchId, expectedExchangeType);
			bemDTOV3.setFileInfoXml(marshallFileInfo(fileInfo));
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
			
			String procToDbCd = getJdbc().queryForObject("SELECT PROCESSEDTODBSTATUSTYPECD FROM BATCHTRANSMSG WHERE TRANSMSGID = " + transMsgIdV1 + " AND BATCHID = " + mockBatchId, String.class);
			assertEquals("ProcessedToDbIndicator for transMsgId V1: " + transMsgIdV1, ProcessedToDbInd.D.getValue(), procToDbCd);
			
			procToDbCd = getJdbc().queryForObject("SELECT PROCESSEDTODBSTATUSTYPECD FROM BATCHTRANSMSG WHERE TRANSMSGID = " + transMsgIdV1a + " AND BATCHID = " + jobId, String.class);
			assertEquals("ProcessedToDbIndicator for transMsgId V1a: " + transMsgIdV1a, expectedProcessedToDbInd.getValue(), procToDbCd);

			procToDbCd = getJdbc().queryForObject("SELECT PROCESSEDTODBSTATUSTYPECD FROM BATCHTRANSMSG WHERE TRANSMSGID = " + transMsgIdV2 + " AND BATCHID = " + mockBatchId, String.class);
			assertEquals("ProcessedToDbIndicator for transMsgId V2: " + transMsgIdV2, expectedProcessedToDbInd.getValue(), procToDbCd);

			procToDbCd = getJdbc().queryForObject("SELECT PROCESSEDTODBSTATUSTYPECD FROM BATCHTRANSMSG WHERE TRANSMSGID = " + transMsgIdV3 + " AND BATCHID = " + mockBatchId, String.class);
			assertEquals("ProcessedToDbIndicator for transMsgId V3: " + transMsgIdV3, ProcessedToDbInd.D.getValue(), procToDbCd);
			
			procToDbCd = getJdbc().queryForObject("SELECT PROCESSEDTODBSTATUSTYPECD FROM BATCHTRANSMSG WHERE TRANSMSGID = " + transMsgIdV3a + " AND BATCHID = " + jobId, String.class);
			assertEquals("ProcessedToDbIndicator for transMsgId V3a: " + transMsgIdV3a, expectedProcessedToDbInd.getValue(), procToDbCd);			
			
			assertEquals("Batch Status value is COMPLETED", BatchStatus.COMPLETED, jobExec.getStatus());
			
		} finally {
			if(jobId != null) deleteBatchData(stepExec, jobId);
			if(jobId != null) deleteTestData(jobId);
			deleteSetUpData(mockBatchId);
			getJdbc().execute("DELETE FROM DAILYBEMINDEXER WHERE EXCHANGETYPECD='FFM' AND SUBSCRIBERSTATECD = 'ZZ'");
		}
		
	}
	
	@Test
	public void testDoStep_bemHandlerFFM_EPROD_35_LaterVersionsExist() throws Exception {

		// clean up prior and after incase previous test failed.
		getJdbc().execute("DELETE FROM STAGINGPLANLOCK ");
		getJdbc().execute("DELETE FROM DAILYBEMINDEXER WHERE EXCHANGETYPECD='FFM' AND SUBSCRIBERSTATECD = 'ZZ'");
		StepExecution stepExec = null;
		Long jobId = null;
		Long mockBatchId = TestDataUtil.getRandomNumber(8);

		String expectedFileNm = makeFileNameERL(APR_1_4am);
		LocalDateTime expectedFileDateTime = getFileNameDateTime(expectedFileNm);
		ExchangeType expectedExchangeType = ExchangeType.FFM;
		//String expectedErrMsg = "EPROD-29";

		String stateCd = "ZZ";
		String exchangePolicyId = "ERLINGESTIONEPROD35TEST";
		String hiosId = exchangePolicyId.substring(0, 5);
		String planId = exchangePolicyId.substring(0, 14);
		int versionNum = 1;
		String versionNumStr = versionNum + "";
		LocalDateTime versionDt = MAR_1_3am;
		
		try {
			insertBatchRunControl("N");
			super.JOB_ID = mockBatchId;
			// Insert a batchTrans's with same policy as "this" one representing a previous transaction version that was processed as 'S'
			// pass in batchId for CREATEBY used by delete script.
/*			Long transMsgIdV1 = insertTransMsg(mockBatchId);
			insertBatchTransMsg(transMsgIdV1, mockBatchId, ProcessedToDbInd.S, stateCd, exchangePolicyId, hiosId, versionNumStr, versionDt);*/
	
			// Insert a batchTransMsg and increment versionId representing a later version for this policy
			Long transMsgIdV2 = insertTransMsg(mockBatchId);
			versionNum++;
			versionNumStr = versionNum +"";
			insertBatchTransMsg(transMsgIdV2, mockBatchId, ProcessedToDbInd.Y, stateCd, exchangePolicyId, hiosId, versionNumStr, versionDt);
			insertPolicyVersion(transMsgIdV2, LocalDateTime.now(), LocalDate.now(), LocalDate.now().plusYears(1), stateCd, exchangePolicyId, hiosId, planId, new Integer(2));
			
			// Insert a transMsg and Batch and use versionId representing the first transaction that was skipped and will be processed as "this" transaction.
			Long transMsgIdV1 = insertTransMsg(mockBatchId);
			//insertBatchTransMsg(transMsgIdV1, mockBatchId);
			//versionNum++;
			//versionNumStr = versionNum +"";
	
			// Insert "this" transaction into the bemIndexer for processing
			BenefitEnrollmentMaintenanceDTO bemDTO = makeBemDTO(mockBatchId, transMsgIdV1, "1", versionDt, PolicyStatus.EFFECTUATED_2, exchangePolicyId);
			bemDTO.getBem().getMember().add(TestDataUtil.makeSubscriber(stateCd, exchangePolicyId, hiosId));
	
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
			
			String procToDbCd = getJdbc().queryForObject("SELECT PROCESSEDTODBSTATUSTYPECD FROM BATCHTRANSMSG WHERE TRANSMSGID = " + transMsgIdV1 + " AND BATCHID = " + jobId, String.class);
			assertEquals("ProcessedToDbIndicator for transMsgId V1: " + transMsgIdV1, ProcessedToDbInd.I.getValue(), procToDbCd);
			
			procToDbCd = getJdbc().queryForObject("SELECT PROCESSEDTODBSTATUSTYPECD FROM BATCHTRANSMSG WHERE TRANSMSGID = " + transMsgIdV2 + " AND BATCHID = " + mockBatchId, String.class);
			assertEquals("ProcessedToDbIndicator for transMsgId V2: " + transMsgIdV2, ProcessedToDbInd.Y.getValue(), procToDbCd);

			assertEquals("Batch Status value is COMPLETED", BatchStatus.COMPLETED, jobExec.getStatus());
			
		} finally {
			if(jobId != null) deleteBatchData(stepExec, jobId);
			if(jobId != null) deleteTestData(jobId);
			if(jobId != null) deleteTestData(mockBatchId);
			deleteSetUpData(mockBatchId);
			getJdbc().execute("DELETE FROM DAILYBEMINDEXER WHERE EXCHANGETYPECD='FFM' AND SUBSCRIBERSTATECD = 'ZZ'");
		}
		
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
