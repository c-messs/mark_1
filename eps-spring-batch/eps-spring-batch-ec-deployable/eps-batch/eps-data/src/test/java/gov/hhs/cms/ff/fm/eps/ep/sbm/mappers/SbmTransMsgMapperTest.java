package gov.hhs.cms.ff.fm.eps.ep.sbm.mappers;

import org.junit.Test;
import org.springframework.test.util.ReflectionTestUtils;

import gov.cms.dsh.sbmi.PolicyType;
import gov.cms.dsh.sbmr.PolicyErrorType;
import gov.hhs.cms.ff.fm.eps.ep.enums.SBMErrorWarningCode;
import gov.hhs.cms.ff.fm.eps.ep.enums.SBMFileStatus;
import gov.hhs.cms.ff.fm.eps.ep.enums.SbmTransMsgStatus;
import gov.hhs.cms.ff.fm.eps.ep.enums.TxnMessageDirectionType;
import gov.hhs.cms.ff.fm.eps.ep.enums.TxnMessageType;
import gov.hhs.cms.ff.fm.eps.ep.po.SbmTransMsgPO;
import gov.hhs.cms.ff.fm.eps.ep.sbm.SBMErrorDTO;
import gov.hhs.cms.ff.fm.eps.ep.sbm.SBMPolicyDTO;
import gov.hhs.cms.ff.fm.eps.ep.sbm.SbmDataUtil;
import gov.hhs.cms.ff.fm.eps.ep.sbm.SbmErrWarningLogDTO;
import gov.hhs.cms.ff.fm.eps.ep.util.DateTimeUtil;
import gov.hhs.cms.ff.fm.eps.ep.util.sbm.TestDataSBMUtility;

public class SbmTransMsgMapperTest extends SBMBaseMapperTest {
	
	SbmTransMsgMapper mapper = new SbmTransMsgMapper();
	
	@Test
	public void test_mapSBMToEPS() {
		
		SBMPolicyDTO expected = new SBMPolicyDTO();
		SbmTransMsgStatus expectedStatus = SbmTransMsgStatus.ACCEPTED_WITH_SBM_CHANGE;
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
		
		SbmTransMsgStatus expectedStatus = SbmTransMsgStatus.REJECTED;
		SBMPolicyDTO expectedDTO = new SBMPolicyDTO();
		SbmErrWarningLogDTO errWarnDTO = new SbmErrWarningLogDTO();
		expectedDTO.setErrorFlag(true);
		errWarnDTO.setErrorWarningTypeCd(SBMErrorWarningCode.ER_001.getCode());
		expectedDTO.getErrorList().add(errWarnDTO);
		SbmTransMsgPO actual = mapper.mapSBMToEPS(expectedDTO);
		assertNotNull("SbmTransMsgPO", actual);
		assertEquals("SbmTransMsg Status", expectedStatus.getCode(), actual.getSbmTransMsgProcStatusTypeCd());
	}
	
	@Test
	public void test_mapSBMToEPS_Policy_Warnings() {
		
		SbmTransMsgStatus expectedStatus = SbmTransMsgStatus.ACCEPTED_WITH_EPS_CHANGE;
		SBMPolicyDTO expectedDTO = new SBMPolicyDTO();
		SbmErrWarningLogDTO errWarnDTO = new SbmErrWarningLogDTO();
		errWarnDTO.setErrorWarningTypeCd(SBMErrorWarningCode.WR_009.getCode());
		expectedDTO.getErrorList().add(errWarnDTO);
		SbmTransMsgPO actual = mapper.mapSBMToEPS(expectedDTO);
		assertNotNull("SbmTransMsgPO", actual);
		assertEquals("SbmTransMsg Status", expectedStatus.getCode(), actual.getSbmTransMsgProcStatusTypeCd());
	}
	
	@Test
	public void test_RJC() {
	
		SbmTransMsgStatus expectedStatus = SbmTransMsgStatus.REJECTED;
		SBMPolicyDTO inboundPolicyDTO = new SBMPolicyDTO();
		SbmErrWarningLogDTO errWarnDTO = new SbmErrWarningLogDTO();
		inboundPolicyDTO.setErrorFlag(true);
		errWarnDTO.setErrorWarningTypeCd(SBMErrorWarningCode.ER_001.getCode());
		inboundPolicyDTO.getErrorList().add(errWarnDTO);
		
		SbmTransMsgStatus actual = (SbmTransMsgStatus) ReflectionTestUtils.invokeMethod(mapper, "determineSbmTransMsgStatus", inboundPolicyDTO);
		assertEquals("SbmTransMsg Status", expectedStatus.getCode(), actual.getCode());
	}
	
	@Test
	public void test_RJC_Schema_Error() {
	
		SbmTransMsgStatus expectedStatus = SbmTransMsgStatus.REJECTED;
		SBMPolicyDTO inboundPolicyDTO = new SBMPolicyDTO();
		SBMErrorDTO errDTO = new SBMErrorDTO();
		
		errDTO.setSbmErrorWarningTypeCd(SBMErrorWarningCode.ER_003.getCode());
		inboundPolicyDTO.getSchemaErrorList().add(errDTO);
		
		SbmTransMsgStatus actual = (SbmTransMsgStatus) ReflectionTestUtils.invokeMethod(mapper, "determineSbmTransMsgStatus", inboundPolicyDTO);
		assertEquals("SbmTransMsg Status", expectedStatus.getCode(), actual.getCode());
	}
	
	@Test
	public void test_ACC() {
	
		SbmTransMsgStatus expectedStatus = SbmTransMsgStatus.ACCEPTED_WITH_SBM_CHANGE;
		SBMPolicyDTO inboundPolicyDTO = new SBMPolicyDTO();
	
		SbmTransMsgStatus actual = (SbmTransMsgStatus) ReflectionTestUtils.invokeMethod(mapper, "determineSbmTransMsgStatus", inboundPolicyDTO);
		assertEquals("SbmTransMsg Status", expectedStatus.getCode(), actual.getCode());
	}
	
	@Test
	public void test_ACC_With_Warning() {
	
		SbmTransMsgStatus expectedStatus = SbmTransMsgStatus.ACCEPTED_WITH_SBM_CHANGE;
		SBMPolicyDTO inboundPolicyDTO = new SBMPolicyDTO();
		SbmErrWarningLogDTO errWarnDTO = new SbmErrWarningLogDTO();
		// WR-001 is NOT due to EPS change.
		errWarnDTO.setErrorWarningTypeCd(SBMErrorWarningCode.WR_001.getCode());
		inboundPolicyDTO.getErrorList().add(errWarnDTO);
		
		SbmTransMsgStatus actual = (SbmTransMsgStatus) ReflectionTestUtils.invokeMethod(mapper, "determineSbmTransMsgStatus", inboundPolicyDTO);
		assertEquals("SbmTransMsg Status", expectedStatus.getCode(), actual.getCode());
	}
	
	@Test
	public void test_AEC() {
	
		SbmTransMsgStatus expectedStatus = SbmTransMsgStatus.ACCEPTED_WITH_EPS_CHANGE;
		SBMPolicyDTO inboundPolicyDTO = new SBMPolicyDTO();
		SbmErrWarningLogDTO errWarnDTO = new SbmErrWarningLogDTO();
		errWarnDTO.setErrorWarningTypeCd(SBMErrorWarningCode.WR_004.getCode());
		inboundPolicyDTO.getErrorList().add(errWarnDTO);
		
		SbmTransMsgStatus actual = (SbmTransMsgStatus) ReflectionTestUtils.invokeMethod(mapper, "determineSbmTransMsgStatus", inboundPolicyDTO);
		assertEquals("SbmTransMsg Status", expectedStatus.getCode(), actual.getCode());
	}
	
	
	@Test
	public void test_mapEpsToSbmr() {
		
		int expectedRCN = 88;
		String expectedQHPId = "11111VT012354";
		String expectedExPolid = "EXPOLID-11111";
		String expectedExSubId = "EXSUBID-11111";
		SbmTransMsgPO po = new SbmTransMsgPO();
		po.setRecordControlNum(expectedRCN);
		po.setPlanId(expectedQHPId);
		po.setExchangeAssignedPolicyId(expectedExPolid);
		po.setExchangeAssignedSubscriberId(expectedExSubId);
		
		PolicyErrorType actual = mapper.mapEpsToSbmr(po);
		
		assertEquals("", expectedRCN, actual.getRecordControlNumber());
		assertEquals("", expectedQHPId, actual.getQHPId());
		assertEquals("", expectedExPolid, actual.getExchangeAssignedPolicyId());
		assertEquals("", expectedExSubId, actual.getExchangeAssignedSubscriberId());
	}


}
