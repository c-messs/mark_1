package gov.hhs.cms.ff.fm.eps.ep.jobs.sbm;

import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.EasyMock.replay;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.io.IOException;
import java.sql.SQLSyntaxErrorException;

import javax.xml.bind.JAXBException;

import org.easymock.EasyMock;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.springframework.jdbc.BadSqlGrammarException;

import com.accenture.foundation.common.exception.ApplicationException;

import gov.hhs.cms.ff.fm.eps.ep.enums.SBMErrorWarningCode;
import gov.hhs.cms.ff.fm.eps.ep.sbm.SBMPolicyDTO;
import gov.hhs.cms.ff.fm.eps.ep.sbm.services.SbmXprService;

@RunWith(JUnit4.class)
public class SbmXprSkipListenerTest {

	private static final Long BATCH_ID = new Long("9999999999001");

	private SbmXprSkipListener xprSkipListener;

	private SbmXprService mockSbmXprService;


	@Before
	public void setup() throws IOException {

		xprSkipListener = new SbmXprSkipListener();
		
		mockSbmXprService = EasyMock.createMock(SbmXprService.class);
		xprSkipListener.setSbmXprService(mockSbmXprService);
	}

	@After
	public void tearDown() {
	}

	/*
	 *  Tests skip in process when there is no EPROD for the ApplicationException 
	 */
	@Test
	public void test_onSkipInProcess() throws Exception {

		mockSbmXprService.saveXprSkippedTransaction(EasyMock.anyObject(SBMPolicyDTO.class));
		expectLastCall();
		replay(mockSbmXprService);
		
		SBMErrorWarningCode expectedErrCode = SBMErrorWarningCode.SYSTEM_ERROR_999;
		String expectedSkipReasonDesc = "JAXBException: Cannot Unmarshall";

		Throwable expectedT = new JAXBException(expectedSkipReasonDesc);
		// EPSDataLogger catch all Oracle exceptions
		Throwable appEx = new ApplicationException(expectedT, expectedErrCode.getCode());

		SBMPolicyDTO sbmPolicyDTO = new SBMPolicyDTO();
		
		sbmPolicyDTO.setBatchId(BATCH_ID);
		sbmPolicyDTO.setSbmFileInfoId(702L);

		assertNull("BatchTransMsg Id Before", sbmPolicyDTO.getSbmTransMsgId());
		
		xprSkipListener.onSkipInProcess(sbmPolicyDTO, appEx);	

		assertNotNull("BatchTransMsg Id After", sbmPolicyDTO.getBatchId());
		//assertNotNull("BatchTransMsg Id After", sbmPolicyDTO.getSbmTransMsgId());
	}

	@Test
	public void test_onSkipInProcess_NPE() throws Exception {

		mockSbmXprService.saveXprSkippedTransaction(EasyMock.anyObject(SBMPolicyDTO.class));
		expectLastCall();
		replay(mockSbmXprService);
		
		SBMErrorWarningCode expectedErrCode = SBMErrorWarningCode.SYSTEM_ERROR_999;
		String expectedSkipReasonDesc = "NullPointer";

		Throwable expectedT = new NullPointerException(expectedSkipReasonDesc);

		SBMPolicyDTO sbmPolicyDTO = new SBMPolicyDTO();
		
		sbmPolicyDTO.setBatchId(BATCH_ID);
		sbmPolicyDTO.setSbmFileInfoId(702L);

		assertNull("BatchTransMsg Id Before", sbmPolicyDTO.getSbmTransMsgId());
		
		xprSkipListener.onSkipInProcess(sbmPolicyDTO, expectedT);	

		assertNotNull("BatchTransMsg Id After", sbmPolicyDTO.getBatchId());
		//assertNotNull("BatchTransMsg Id After", sbmPolicyDTO.getSbmTransMsgId());
	}


	/*
	 * Tests skip in process when there is no Skip Reason for the ApplicationException 
	 * @throws Exception
	 */
//	@Test
//	public void test_onSkipInProcess_NPE() throws Exception {
//
//		String expectedCode = EProdEnum.EPROD_22.getCode();
//		String expectedErrMsg = null;
//
//		String expectedFileNm = "SBMEOC.IC834.D140531.T145543452.T";
//		LocalDateTime expectedFileDateTime = getFileNameDateTime(expectedFileNm);
//		String expectedExchangeTypeCd = "SBM_EO";
//		String expectedFileInfoXML = "<TEST>Unit test file info xml</TEST>";
//
//		StackTraceElement[] stackTrace = new StackTraceElement[1];
//		stackTrace[0] = new StackTraceElement("EPSProcessor", "putDataIntoEPS", expectedFileNm, 9999);
//
//
//		Throwable t = new NullPointerException(expectedErrMsg);
//
//		t.setStackTrace(stackTrace);
//
//		BenefitEnrollmentMaintenanceDTO bemDTO = new BenefitEnrollmentMaintenanceDTO();
//		bemDTO.setBatchId(BATCH_ID);
//		bemDTO.setTxnMessageDirectionType(TxnMessageDirectionType.INBOUND);
//		bemDTO.setTxnMessageType(TxnMessageType.MSG_834);
//		bemDTO.setBemXml("<BEM>SkipListenerTest</BEM>");
//		bemDTO.setFileNm(expectedFileNm);
//		bemDTO.setFileNmDateTime(expectedFileDateTime);
//		bemDTO.setExchangeTypeCd(expectedExchangeTypeCd);
//		bemDTO.setFileInfoXml(expectedFileInfoXML);
//
//		// Insert a transMsg and BatchTransMsg so this test can update.
//		Long transMsgId = insertTransMsg(BATCH_ID);
//		insertBatchTransMsg(transMsgId, BATCH_ID);
//		assertNotNull("TransMsgId for test transMsg", transMsgId);
//		bemDTO.setTransMsgId(transMsgId);
//
//		skipBemListener.onSkipInProcess(bemDTO, t);	
//
//		BatchTransMsgPO po = transMsgCompositeDAO.getBatchTransMsg(BATCH_ID, transMsgId);
//
//		assertEquals("BatchTransMsg ProcessToDbIndicator", ProcessedToDbInd.S.getValue(), po.getProcessedToDbStatusTypeCd());
//		assertEquals("BatchTransMsg TransMsgSkipReasonTypeCd", expectedCode, po.getTransMsgSkipReasonTypeCd());
//	}



//	@Test
//	public void test_onSkipInWrite_SkipReason_Null() throws Exception {
//
//		String expectedErrMsg = null;
//		Throwable t = new ApplicationException(expectedErrMsg);
//
//		String expectedFileNm = "SBMEOC.IC834.D140531.T145543452.T";
//		LocalDateTime expectedFileDateTime = getFileNameDateTime(expectedFileNm);
//		String expectedExchangeTypeCd = "SBM_EO";
//		String expectedFileInfoXML = "<TEST>Unit test file info xml</TEST>";
//
//		BenefitEnrollmentMaintenanceDTO bemDTO = new BenefitEnrollmentMaintenanceDTO();
//		bemDTO.setBatchId(BATCH_ID);
//		bemDTO.setTxnMessageDirectionType(TxnMessageDirectionType.INBOUND);
//		bemDTO.setTxnMessageType(TxnMessageType.MSG_834);
//		bemDTO.setBemXml("<BEM>SkipListenerTest</BEM>");
//		bemDTO.setFileNm(expectedFileNm);
//		bemDTO.setFileNmDateTime(expectedFileDateTime);
//		bemDTO.setExchangeTypeCd(expectedExchangeTypeCd);
//		bemDTO.setFileInfoXml(expectedFileInfoXML);
//
//		// Insert a transMsg and BatchTransMsg so this test can update.
//		Long transMsgId = insertTransMsg(BATCH_ID);
//		insertBatchTransMsg(transMsgId, BATCH_ID);
//		assertNotNull("TransMsgId for test transMsg", transMsgId);
//		bemDTO.setTransMsgId(transMsgId);
//
//		skipBemListener.onSkipInWrite(bemDTO, t);	
//
//		BatchTransMsgPO po = transMsgCompositeDAO.getBatchTransMsg(BATCH_ID, transMsgId);
//
//		assertEquals("BatchTransMsg ProcessToDbIndicator", ProcessedToDbInd.S.getValue(), po.getProcessedToDbStatusTypeCd());
//	}
//

	/**
	 * Tests OnSkipInRead.  
	 */
	@Test
	public void testOnSkipInRead() {
		ApplicationException appEx = new ApplicationException("Skip in Reader");

		xprSkipListener.onSkipInRead(appEx);
		assertNotNull("appEx", appEx.getMessage());
	}

	/**
	 * Tests OnSkipInWrite.  
	 */
	@Test
	public void testOnSkipInWrite() {

		SBMErrorWarningCode expectedErrCode = SBMErrorWarningCode.SYSTEM_ERROR_999;
		String expectedSkipReasonDesc = "ORA-88888: Cant put bad data into a good system.";

		Throwable expectedT = new BadSqlGrammarException("statement","PUT DATA INTO THE YELLOW ROUND THING", new SQLSyntaxErrorException(expectedSkipReasonDesc));
		// EPSDataLogger catch all Oracle exceptions
		Throwable appEx = new ApplicationException(expectedT, expectedErrCode.getCode());

		SBMPolicyDTO sbmPolicyDTO = new SBMPolicyDTO();
		
		sbmPolicyDTO.setBatchId(BATCH_ID);
		sbmPolicyDTO.setSbmFileInfoId(702L);
		
		xprSkipListener.onSkipInWrite(sbmPolicyDTO, appEx);
		assertNotNull("BatchTransMsg Id After", sbmPolicyDTO.getBatchId());
	}

}
