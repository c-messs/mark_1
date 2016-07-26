package gov.hhs.cms.ff.fm.eps.ep.jobs.enrollmentprocessingjob;

import gov.cms.dsh.bem.BenefitEnrollmentMaintenanceType;
import gov.cms.dsh.bem.PolicyInfoType;
import gov.cms.dsh.bem.TransactionInformationType;
import gov.hhs.cms.ff.fm.eps.ep.BenefitEnrollmentMaintenanceDTO;
import gov.hhs.cms.ff.fm.eps.ep.enums.EProdEnum;
import gov.hhs.cms.ff.fm.eps.ep.enums.ProcessedToDbInd;
import gov.hhs.cms.ff.fm.eps.ep.enums.TxnMessageDirectionType;
import gov.hhs.cms.ff.fm.eps.ep.enums.TxnMessageType;
import gov.hhs.cms.ff.fm.eps.ep.jobs.enrollmentprocessingjob.data.util.TestDataUtil;
import gov.hhs.cms.ff.fm.eps.ep.po.BatchTransMsgPO;
import gov.hhs.cms.ff.fm.eps.ep.services.TransMsgCompositeDAO;
import gov.hhs.cms.ff.fm.eps.ep.util.EpsDateUtils;

import java.io.File;
import java.io.IOException;
import java.sql.SQLSyntaxErrorException;

import org.apache.commons.io.FileUtils;
import org.joda.time.DateTime;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobInstance;
import org.springframework.batch.core.StepExecution;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.BadSqlGrammarException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.support.DirtiesContextTestExecutionListener;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.test.context.transaction.TransactionalTestExecutionListener;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.annotation.Transactional;

import com.accenture.foundation.common.exception.ApplicationException;

@ContextConfiguration(locations={"classpath:/test-batch-application-context.xml"})
@DirtiesContext(classMode = ClassMode.AFTER_EACH_TEST_METHOD)
@RunWith(SpringJUnit4ClassRunner.class)
@TestExecutionListeners({DependencyInjectionTestExecutionListener.class, 
	TransactionalTestExecutionListener.class, DirtiesContextTestExecutionListener.class })
@TransactionConfiguration(transactionManager = "transactionManager", defaultRollback = true)
@Transactional
public class SkipBemListenerTest extends BaseBatchTest {

	private static final Long BATCH_ID = new Long("9999999999001");

	private static String ROOT_TEST_PATH = "./Test834files";

	private SkipBemListener skipBemListener;

	@Autowired
	@Qualifier("transMsgCompositeDAO")
	private TransMsgCompositeDAO transMsgCompositeDAO;

	@Value("./Test834files/skipped")
	private File skippedFilesDirectory;

	@Autowired
	JdbcTemplate jdbcTemplate;


	@Before
	public void setup() throws IOException {

		skipBemListener = new SkipBemListener();
		skippedFilesDirectory.mkdirs();
		skipBemListener.setTransMsgService(transMsgCompositeDAO);

		JobInstance jobInst = new JobInstance(BATCH_ID,"quizJob");
		JobExecution jobEx = new JobExecution(jobInst, null);
		StepExecution stepEx = new StepExecution("anyStep", jobEx);
		skipBemListener.beforeStep(stepEx);

	}

	@After
	public void tearDown() {
		FileUtils.deleteQuietly(new File(ROOT_TEST_PATH));
	}

	/*
	 *  Tests skip in process when there is no EPROD for the ApplicationException 
	 */
	@Test
	public void test_onSkipInProcess() throws Exception {

		EProdEnum expectedCode = EProdEnum.EPROD_10;
		String expectedSkipReasonDesc = "ORA-88888: Cant put bad data into a good system.";

		Throwable expectedT = new BadSqlGrammarException("statement","PUT DATA INTO THE YELLOW ROUND THING", new SQLSyntaxErrorException(expectedSkipReasonDesc));
		// EPSDataLogger catch all Oracle exceptions
		Throwable appEx = new ApplicationException(expectedT, expectedCode.getCode());

		String expectedFileNm = "SBMEOC.IC834.D140531.T145543452.T";
		DateTime expectedFileDateTime = getFileNameDateTime(expectedFileNm);
		String expectedExchangeTypeCd = "SBM_EO";
		String expectedFileInfoXML = "<TEST>Unit test file info xml</TEST>";

		BenefitEnrollmentMaintenanceDTO bemDTO = new BenefitEnrollmentMaintenanceDTO();
		bemDTO.setBatchId(BATCH_ID);
		bemDTO.setTxnMessageDirectionType(TxnMessageDirectionType.INBOUND);
		bemDTO.setTxnMessageType(TxnMessageType.MSG_834);
		bemDTO.setBemXml("<BEM>SkipListenerTest</BEM>");
		bemDTO.setFileNm(expectedFileNm);
		bemDTO.setFileNmDateTime(expectedFileDateTime);
		bemDTO.setExchangeTypeCd(expectedExchangeTypeCd);
		bemDTO.setFileInfoXml(expectedFileInfoXML);

		// Insert a transMsg and BatchTransMsg so this test can update.
		Long transMsgId = insertTransMsg(BATCH_ID);
		insertBatchTransMsg(transMsgId, BATCH_ID);
		assertNotNull("TransMsgId for test transMsg", transMsgId);
		bemDTO.setTransMsgId(transMsgId);

		skipBemListener.onSkipInProcess(bemDTO, appEx);	

		BatchTransMsgPO po = transMsgCompositeDAO.getBatchTransMsg(BATCH_ID, transMsgId);
		assertEquals("BatchTransMsg ProcessToDbIndicator", ProcessedToDbInd.S.getValue(), po.getProcessedToDbStatusTypeCd());
		assertEquals("BatchTransMsg TransMsgSkipReasonTypeCd", expectedCode.getCode(), po.getTransMsgSkipReasonTypeCd());
		assertEquals("BatchTransMsg TransMsgSkipReasonTypeCd", expectedSkipReasonDesc, po.getTransMsgSkipReasonDesc());
	}

	/*
	 * Tests skip in process when there is no EPROD for the ApplicationException 
	 * SkipListener will write EPROD-99 to EPS and write the message as TransMsgSkipReasonDesc.
	 * @throws Exception
	 */
	@Test
	public void test_onSkipInProcess_NoEPRODCode() throws Exception {

		String expectedCode = EProdEnum.EPROD_99.getCode();
		String expectedErrMsg = "Any error message not starting with EPROD";
		Throwable t = new ApplicationException(expectedErrMsg);

		String expectedFileNm = "SBMEOC.IC834.D140531.T145543452.T";
		DateTime expectedFileDateTime = getFileNameDateTime(expectedFileNm);
		String expectedExchangeTypeCd = "SBM_EO";
		String expectedFileInfoXML = "<TEST>Unit test file info xml</TEST>";

		BenefitEnrollmentMaintenanceDTO bemDTO = new BenefitEnrollmentMaintenanceDTO();
		bemDTO.setBatchId(BATCH_ID);
		bemDTO.setTxnMessageDirectionType(TxnMessageDirectionType.INBOUND);
		bemDTO.setTxnMessageType(TxnMessageType.MSG_834);
		bemDTO.setBemXml("<BEM>SkipListenerTest</BEM>");
		bemDTO.setFileNm(expectedFileNm);
		bemDTO.setFileNmDateTime(expectedFileDateTime);
		bemDTO.setExchangeTypeCd(expectedExchangeTypeCd);
		bemDTO.setFileInfoXml(expectedFileInfoXML);

		// Insert a transMsg and BatchTransMsg so this test can update.
		Long transMsgId = insertTransMsg(BATCH_ID);
		insertBatchTransMsg(transMsgId, BATCH_ID);
		assertNotNull("TransMsgId for test transMsg", transMsgId);
		bemDTO.setTransMsgId(transMsgId);

		skipBemListener.onSkipInProcess(bemDTO, t);	

		BatchTransMsgPO po = transMsgCompositeDAO.getBatchTransMsg(BATCH_ID, transMsgId);

		assertEquals("BatchTransMsg ProcessToDbIndicator", ProcessedToDbInd.S.getValue(), po.getProcessedToDbStatusTypeCd());
		assertEquals("BatchTransMsg TransMsgSkipReasonTypeCd", expectedCode, po.getTransMsgSkipReasonTypeCd());
		assertEquals("BatchTransMsg TransMsgSkipReasonDesc", expectedErrMsg, po.getTransMsgSkipReasonDesc());
	}

	/*
	 * Tests skip in process when there is no message for the ApplicationException 
	 * @throws Exception
	 */
	@Test
	public void test_onSkipInProcess_SkipReason_Null() throws Exception {

		String expectedCode = EProdEnum.EPROD_99.getCode();
		String expectedErrMsg = null;
		Throwable t = new ApplicationException(expectedErrMsg);

		String expectedFileNm = "SBMEOC.IC834.D140531.T145543452.T";
		DateTime expectedFileDateTime = getFileNameDateTime(expectedFileNm);
		String expectedExchangeTypeCd = "SBM_EO";
		String expectedFileInfoXML = "<TEST>Unit test file info xml</TEST>";

		BenefitEnrollmentMaintenanceDTO bemDTO = new BenefitEnrollmentMaintenanceDTO();
		bemDTO.setBatchId(BATCH_ID);
		bemDTO.setTxnMessageDirectionType(TxnMessageDirectionType.INBOUND);
		bemDTO.setTxnMessageType(TxnMessageType.MSG_834);
		bemDTO.setBemXml("<BEM>SkipListenerTest</BEM>");
		bemDTO.setFileNm(expectedFileNm);
		bemDTO.setFileNmDateTime(expectedFileDateTime);
		bemDTO.setExchangeTypeCd(expectedExchangeTypeCd);
		bemDTO.setFileInfoXml(expectedFileInfoXML);

		// Insert a transMsg and BatchTransMsg so this test can update.
		Long transMsgId = insertTransMsg(BATCH_ID);
		insertBatchTransMsg(transMsgId, BATCH_ID);
		assertNotNull("TransMsgId for test transMsg", transMsgId);
		bemDTO.setTransMsgId(transMsgId);

		skipBemListener.onSkipInProcess(bemDTO, t);	

		BatchTransMsgPO po = transMsgCompositeDAO.getBatchTransMsg(BATCH_ID, transMsgId);

		assertEquals("BatchTransMsg ProcessToDbIndicator", ProcessedToDbInd.S.getValue(), po.getProcessedToDbStatusTypeCd());
		assertEquals("BatchTransMsg TransMsgSkipReasonTypeCd", expectedCode, po.getTransMsgSkipReasonTypeCd());
	}

	/*
	 * Tests skip in process when there is no skip reason code for the ApplicationException
	 * but the message starts with EPROD code 
	 * @throws Exception
	 */
	@Test
	public void test_onSkipInProcess_SkipReasonDesc_NotNull() throws Exception {

		//String expectedCode = EPSConstants.EPROD_99;
		String expectedErrMsg = "EPROD-36: Invalid BEM Transaction Type";
		Throwable t = new ApplicationException(expectedErrMsg);

		String expectedFileNm = "SBMEOC.IC834.D140531.T145543452.T";
		DateTime expectedFileDateTime = getFileNameDateTime(expectedFileNm);
		String expectedExchangeTypeCd = "SBM_EO";
		String expectedFileInfoXML = "<TEST>Unit test file info xml</TEST>";

		BenefitEnrollmentMaintenanceDTO bemDTO = new BenefitEnrollmentMaintenanceDTO();
		bemDTO.setBatchId(BATCH_ID);
		bemDTO.setTxnMessageDirectionType(TxnMessageDirectionType.INBOUND);
		bemDTO.setTxnMessageType(TxnMessageType.MSG_834);
		bemDTO.setBemXml("<BEM>SkipListenerTest</BEM>");
		bemDTO.setFileNm(expectedFileNm);
		bemDTO.setFileNmDateTime(expectedFileDateTime);
		bemDTO.setExchangeTypeCd(expectedExchangeTypeCd);
		bemDTO.setFileInfoXml(expectedFileInfoXML);

		// Insert a transMsg and BatchTransMsg so this test can update.
		Long transMsgId = insertTransMsg(BATCH_ID);
		insertBatchTransMsg(transMsgId, BATCH_ID);
		assertNotNull("TransMsgId for test transMsg", transMsgId);
		bemDTO.setTransMsgId(transMsgId);

		skipBemListener.onSkipInProcess(bemDTO, t);	

		BatchTransMsgPO po = transMsgCompositeDAO.getBatchTransMsg(BATCH_ID, transMsgId);

		assertEquals("BatchTransMsg ProcessToDbIndicator", ProcessedToDbInd.S.getValue(), po.getProcessedToDbStatusTypeCd());
	}

	/*
	 * Tests skip in process when there is no Skip Reason for the ApplicationException 
	 * @throws Exception
	 */
	@Test
	public void test_onSkipInProcess_NPE() throws Exception {

		String expectedCode = EProdEnum.EPROD_22.getCode();
		String expectedErrMsg = null;

		String expectedFileNm = "SBMEOC.IC834.D140531.T145543452.T";
		DateTime expectedFileDateTime = getFileNameDateTime(expectedFileNm);
		String expectedExchangeTypeCd = "SBM_EO";
		String expectedFileInfoXML = "<TEST>Unit test file info xml</TEST>";

		StackTraceElement[] stackTrace = new StackTraceElement[1];
		stackTrace[0] = new StackTraceElement("EPSProcessor", "putDataIntoEPS", expectedFileNm, 9999);


		Throwable t = new NullPointerException(expectedErrMsg);

		t.setStackTrace(stackTrace);

		BenefitEnrollmentMaintenanceDTO bemDTO = new BenefitEnrollmentMaintenanceDTO();
		bemDTO.setBatchId(BATCH_ID);
		bemDTO.setTxnMessageDirectionType(TxnMessageDirectionType.INBOUND);
		bemDTO.setTxnMessageType(TxnMessageType.MSG_834);
		bemDTO.setBemXml("<BEM>SkipListenerTest</BEM>");
		bemDTO.setFileNm(expectedFileNm);
		bemDTO.setFileNmDateTime(expectedFileDateTime);
		bemDTO.setExchangeTypeCd(expectedExchangeTypeCd);
		bemDTO.setFileInfoXml(expectedFileInfoXML);

		// Insert a transMsg and BatchTransMsg so this test can update.
		Long transMsgId = insertTransMsg(BATCH_ID);
		insertBatchTransMsg(transMsgId, BATCH_ID);
		assertNotNull("TransMsgId for test transMsg", transMsgId);
		bemDTO.setTransMsgId(transMsgId);

		skipBemListener.onSkipInProcess(bemDTO, t);	

		BatchTransMsgPO po = transMsgCompositeDAO.getBatchTransMsg(BATCH_ID, transMsgId);

		assertEquals("BatchTransMsg ProcessToDbIndicator", ProcessedToDbInd.S.getValue(), po.getProcessedToDbStatusTypeCd());
		assertEquals("BatchTransMsg TransMsgSkipReasonTypeCd", expectedCode, po.getTransMsgSkipReasonTypeCd());
	}


	/*
	 * Tests skip in process when EPROD-29, updating the previous versions status to 'D'
	 * @throws Exception
	 */
	@Test
	public void test_onSkipInProcess_EPRODCode_29() throws Exception {

		Long expectedBatchIdPrevious = TestDataUtil.getRandomNumber(9);
		Long expectedBatchId = BATCH_ID; Long.valueOf((expectedBatchIdPrevious.intValue() + 1));

		EProdEnum expectedCodePrevious = EProdEnum.EPROD_10;
		String expectedSkipReasonPrevious = "ORA-9999: Some contraint violation.";

		EProdEnum expectedCode = EProdEnum.EPROD_29;
		String expectedCodeLogMsg = "Previously skipped version updated to 'D'";


		Throwable t = new ApplicationException(expectedCodeLogMsg, expectedCode.getCode());
		String expectedFileNm = "FFM.IS834.D140531.T145543452.T";

		DateTime expectedFileDateTime = getFileNameDateTime(expectedFileNm);
		String expectedExchangeTypeCd = "FFM";
		String expectedFileInfoXML = "<TEST>Unit test file info xml</TEST>";

		String stateCd = "VA";
		String exchangePolicyId = TestDataUtil.getRandomNumberAsString(8);
		String hiosId = exchangePolicyId.substring(0, 5);
		int versionNum = TestDataUtil.getRandomNumber(7).intValue();
		String versionNumStr = versionNum + "";
		DateTime versionDt = MAR_1;

		// Insert a "previously skipped Version" of this transaction from a previous batch into transMsg 
		// and BatchTransMsg so this test can update it from SkipBemListener to 'D'.
		Long transMsgIdPrevious = insertTransMsg(expectedBatchIdPrevious);
		assertNotNull("TransMsgId for previously skipped test transMsg", transMsgIdPrevious);
		insertBatchTransMsg(transMsgIdPrevious, expectedBatchIdPrevious, ProcessedToDbInd.S, stateCd, 
				exchangePolicyId, hiosId, versionNumStr, versionDt, expectedCodePrevious.getCode(), expectedSkipReasonPrevious);


		// Insert this transaction into transMsg and BatchTransMsg so this test can 
		// update it from SkipBemListener to 'S'.
		Long transMsgId = insertTransMsg(expectedBatchId);
		assertNotNull("TransMsgId for this test transMsg", transMsgId);
		// use the same snapShotversionNum
		insertBatchTransMsg(transMsgId, expectedBatchId, null, stateCd, exchangePolicyId, hiosId, versionNumStr, versionDt);

		BenefitEnrollmentMaintenanceDTO bemDTO = new BenefitEnrollmentMaintenanceDTO();
		bemDTO.setBatchId(expectedBatchId);
		bemDTO.setTxnMessageDirectionType(TxnMessageDirectionType.INBOUND);
		bemDTO.setTxnMessageType(TxnMessageType.MSG_834);
		bemDTO.setBemXml("<BEM>SkipListenerTest</BEM>");
		bemDTO.setFileNm(expectedFileNm);
		bemDTO.setFileNmDateTime(expectedFileDateTime);
		bemDTO.setExchangeTypeCd(expectedExchangeTypeCd);
		bemDTO.setFileInfoXml(expectedFileInfoXML);

		BenefitEnrollmentMaintenanceType bem = new BenefitEnrollmentMaintenanceType();
		bem.setPolicyInfo(new PolicyInfoType());
		bem.getPolicyInfo().setGroupPolicyNumber(exchangePolicyId);
		bem.setTransactionInformation(new TransactionInformationType());
		bem.getTransactionInformation().setPolicySnapshotVersionNumber(versionNumStr);
		bem.getTransactionInformation().setPolicySnapshotDateTime(EpsDateUtils.getXMLGregorianCalendar(versionDt));
		bem.getMember().add(TestDataUtil.makeSubscriber(stateCd, exchangePolicyId, hiosId, versionDt));

		bemDTO.setTransMsgId(transMsgId);
		bemDTO.setBem(bem);
		ReflectionTestUtils.setField(skipBemListener, "source", "ffm");
		skipBemListener.onSkipInProcess(bemDTO, t);	

		BatchTransMsgPO poPrev = transMsgCompositeDAO.getBatchTransMsg(expectedBatchIdPrevious, transMsgIdPrevious);
		assertEquals("BatchTransMsg ProcessToDbIndicator for previously skipped transaction", ProcessedToDbInd.D.getValue(), poPrev.getProcessedToDbStatusTypeCd());

		BatchTransMsgPO po = transMsgCompositeDAO.getBatchTransMsg(expectedBatchId, transMsgId);
		assertEquals("BatchTransMsg ProcessToDbIndicator for this transaction", ProcessedToDbInd.S.getValue(), po.getProcessedToDbStatusTypeCd());

		String[]  fileList = skippedFilesDirectory.list();

		assertNotNull("Skipped file list", fileList);

	}

	@Test
	public void test_onSkipInWrite_SkipReason_Null() throws Exception {

		String expectedErrMsg = null;
		Throwable t = new ApplicationException(expectedErrMsg);

		String expectedFileNm = "SBMEOC.IC834.D140531.T145543452.T";
		DateTime expectedFileDateTime = getFileNameDateTime(expectedFileNm);
		String expectedExchangeTypeCd = "SBM_EO";
		String expectedFileInfoXML = "<TEST>Unit test file info xml</TEST>";

		BenefitEnrollmentMaintenanceDTO bemDTO = new BenefitEnrollmentMaintenanceDTO();
		bemDTO.setBatchId(BATCH_ID);
		bemDTO.setTxnMessageDirectionType(TxnMessageDirectionType.INBOUND);
		bemDTO.setTxnMessageType(TxnMessageType.MSG_834);
		bemDTO.setBemXml("<BEM>SkipListenerTest</BEM>");
		bemDTO.setFileNm(expectedFileNm);
		bemDTO.setFileNmDateTime(expectedFileDateTime);
		bemDTO.setExchangeTypeCd(expectedExchangeTypeCd);
		bemDTO.setFileInfoXml(expectedFileInfoXML);

		// Insert a transMsg and BatchTransMsg so this test can update.
		Long transMsgId = insertTransMsg(BATCH_ID);
		insertBatchTransMsg(transMsgId, BATCH_ID);
		assertNotNull("TransMsgId for test transMsg", transMsgId);
		bemDTO.setTransMsgId(transMsgId);

		skipBemListener.onSkipInWrite(bemDTO, t);	

		BatchTransMsgPO po = transMsgCompositeDAO.getBatchTransMsg(BATCH_ID, transMsgId);

		assertEquals("BatchTransMsg ProcessToDbIndicator", ProcessedToDbInd.S.getValue(), po.getProcessedToDbStatusTypeCd());
	}


	/**
	 * Tests OnSkipInRead.  
	 */
	@Test
	public void testOnSkipInRead() {
		ApplicationException appEx = new ApplicationException("Skip in Reader");

		skipBemListener.onSkipInRead(appEx);
		assertNotNull("appEx", appEx.getMessage());
	}

	/**
	 * Test method for afterStep() implementation of StepExecutionListener interface
	 *
	 * SHOULD return null, since its unused interface implementation method.
	 * MODIFY this test case when implementation is added
	 */
	@Test
	public void testAfterStep() {
		JobInstance jobInst = new JobInstance(9999L,"epsJob");
		JobExecution jobEx = new JobExecution(jobInst, null);
		StepExecution stepExecution = new StepExecution("skipBemListener", jobEx);

		ExitStatus status = skipBemListener.afterStep(stepExecution);
		assertNull("stepExecution not null", status);
	}

}
