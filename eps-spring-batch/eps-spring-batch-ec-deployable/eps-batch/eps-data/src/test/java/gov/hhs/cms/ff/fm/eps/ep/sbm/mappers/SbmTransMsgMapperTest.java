package gov.hhs.cms.ff.fm.eps.ep.sbm.mappers;

import org.junit.Test;

import gov.cms.dsh.sbmi.PolicyType;
import gov.hhs.cms.ff.fm.eps.ep.enums.SBMErrorWarningCode;
import gov.hhs.cms.ff.fm.eps.ep.enums.SBMFileStatus;
import gov.hhs.cms.ff.fm.eps.ep.enums.SbmTransMsgStatus;
import gov.hhs.cms.ff.fm.eps.ep.enums.TxnMessageDirectionType;
import gov.hhs.cms.ff.fm.eps.ep.enums.TxnMessageType;
import gov.hhs.cms.ff.fm.eps.ep.po.SbmTransMsgPO;
import gov.hhs.cms.ff.fm.eps.ep.sbm.SBMPolicyDTO;
import gov.hhs.cms.ff.fm.eps.ep.sbm.SbmErrWarningLogDTO;
import gov.hhs.cms.ff.fm.eps.ep.util.DateTimeUtil;
import gov.hhs.cms.ff.fm.eps.ep.util.sbm.TestDataSBMUtility;

public class SbmTransMsgMapperTest extends SBMBaseMapperTest {
	
	SbmTransMsgMapper mapper = new SbmTransMsgMapper();
	
	@Test
	public void test_mapSBMToEPS() {
		
		SBMPolicyDTO expected = new SBMPolicyDTO();
		SbmTransMsgStatus expectedStatus = SbmTransMsgStatus.ACCEPTED_NO_CHANGE;
		Long fileId = TestDataSBMUtility.getRandomNumberAsLong(5);
		String tenantId = "TX0";
		int covYr = YEAR;
		String issuerId = TestDataSBMUtility.getRandomNumberAsString(5);
		int issuerFileType = TestDataSBMUtility.FILES_STATE_WIDE;
		String qhpId = TestDataSBMUtility.makeQhpId(issuerId, tenantId);
		
		expected.setSbmFileInfoId(TestDataSBMUtility.getRandomNumberAsLong(3));
		expected.setFileInfo(TestDataSBMUtility.makeFileInformationType(fileId, tenantId, covYr, issuerId, issuerFileType));
		
		PolicyType policy = new PolicyType();
		policy.setRecordControlNumber(99);
		policy.setQHPId(qhpId);
		policy.setExchangeAssignedPolicyId("888888888");
		
		expected.setPolicy(policy);
		expected.setFileStatus(SBMFileStatus.ACCEPTED);
		
		SbmTransMsgPO actual = mapper.mapSBMToEPS(expected);
		
		assertNotNull("SbmTransMsgPO", actual);
		assertEquals("SbmFileId", expected.getSbmFileInfoId(), actual.getSbmFileInfoId());
		assertEquals("FileCreateDateTime", expected.getFileInfo().getFileCreateDateTime(), DateTimeUtil.getXMLGregorianCalendar(actual.getTransMsgDateTime()));
		assertEquals("SubscriberStateCd", tenantId.substring(0, 2), actual.getSubscriberStateCd());
		assertEquals("TransMsgDirectionTypeCd", TxnMessageDirectionType.INBOUND.getValue() , actual.getTransMsgDirectionTypeCd());
		assertEquals("TransMsgTypeCd", TxnMessageType.MSG_SBMI.getValue(), actual.getTransMsgTypeCd());
		assertEquals("SbmFileInfoId", expected.getSbmFileInfoId(), actual.getSbmFileInfoId());
		assertEquals("RecordControlNumber", expected.getPolicy().getRecordControlNumber(), actual.getRecordControlNum().intValue());
		assertEquals("PlanId", expected.getPolicy().getQHPId().substring(0,  14), actual.getPlanId());
		assertEquals("ExchangeAssignedPolicyId", expected.getPolicy().getExchangeAssignedPolicyId(), actual.getExchangeAssignedPolicyId());
		assertEquals("SbmTransMsgProcStatusTypeCd", expectedStatus.getCode(), actual.getSbmTransMsgProcStatusTypeCd());
		
	}
	
	@Test
	public void test_mapSBMToEPS_Empty_DTO() {
		
		SBMPolicyDTO expectedDTO = new SBMPolicyDTO();
		SbmTransMsgPO actual = mapper.mapSBMToEPS(expectedDTO);
		assertNotNull("SbmTransMsgPO", actual);
	}
	
	@Test
	public void test_mapSBMToEPS_Policy_Errors() {
		
		SbmTransMsgStatus expectedStatus = SbmTransMsgStatus.ACCEPTED_WITH_CHANGES;
		SBMPolicyDTO expectedDTO = new SBMPolicyDTO();
		SbmErrWarningLogDTO errWarnDTO = new SbmErrWarningLogDTO();
		errWarnDTO.setErrorWarningTypeCd(SBMErrorWarningCode.ER_001.getCode());
		expectedDTO.getErrorList().add(errWarnDTO);
		SbmTransMsgPO actual = mapper.mapSBMToEPS(expectedDTO);
		assertNotNull("SbmTransMsgPO", actual);
		assertEquals("SbmTransMsg Status", expectedStatus.getCode(), actual.getSbmTransMsgProcStatusTypeCd());
	}
	
	

}
