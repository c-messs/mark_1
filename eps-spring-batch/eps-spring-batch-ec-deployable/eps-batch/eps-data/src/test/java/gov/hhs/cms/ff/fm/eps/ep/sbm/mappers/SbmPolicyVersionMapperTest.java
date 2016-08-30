package gov.hhs.cms.ff.fm.eps.ep.sbm.mappers;

import org.junit.Test;

import gov.cms.dsh.sbmi.PolicyType;
import gov.hhs.cms.ff.fm.eps.ep.po.SbmPolicyVersionPO;
import gov.hhs.cms.ff.fm.eps.ep.sbm.SBMPolicyDTO;
import gov.hhs.cms.ff.fm.eps.ep.util.DateTimeUtil;
import gov.hhs.cms.ff.fm.eps.ep.util.sbm.TestDataSBMUtility;

public class SbmPolicyVersionMapperTest extends SBMBaseMapperTest {
	
	private SbmPolicyVersionMapper mapper = new SbmPolicyVersionMapper();
	
	@Test
	public void test_mapSbmToStaging() {
		
		String issuerId = "88888";
		String tenantId = "NY0";
		String exchangePolicyId = "0000000013";
		int rcn = 1; 
		String qhpId = TestDataSBMUtility.makeQhpId(issuerId, tenantId);
		PolicyType expected = TestDataSBMUtility.makePolicyType(rcn, qhpId, exchangePolicyId);
		
		SBMPolicyDTO inboundDTO = new SBMPolicyDTO();
		inboundDTO.setFileInfo(TestDataSBMUtility.makeFileInformationType(tenantId, issuerId, TestDataSBMUtility.FILES_STATE_WIDE));
		inboundDTO.setPolicy(expected);
		
		SbmPolicyVersionPO actual = mapper.mapSbmToStaging(inboundDTO, new SbmPolicyVersionPO());
		
		assertNotNull("StagingPolicyVersionPO should not be null", actual);
		
		assertEquals("policyStartDate", DateTimeUtil.getLocalDateFromXmlGC(expected.getPolicyStartDate()), actual.getPolicyStartDate());
		assertEquals("policyEndDate", DateTimeUtil.getLocalDateFromXmlGC(expected.getPolicyEndDate()), actual.getPolicyEndDate());
		assertEquals("subscriberStateCd", tenantId.substring(0, 2) , actual.getSubscriberStateCd());
		assertEquals("issuerHiosId", issuerId, actual.getIssuerHiosId());
		assertEquals("issuerSubscriberID", expected.getIssuerAssignedSubscriberId(), actual.getIssuerSubscriberID());
		assertEquals("exchangePolicyId", expected.getIssuerAssignedPolicyId(), actual.getExchangePolicyId());
		assertEquals("TransDateTime", inboundDTO.getFileInfo().getFileCreateDateTime(), DateTimeUtil.getXMLGregorianCalendar(actual.getTransDateTime()));
		assertEquals("TransControlNum", String.valueOf(expected.getRecordControlNumber()), actual.getTransControlNum());
		assertEquals("planID", expected.getQHPId(), actual.getPlanID());
   	    assertEquals("insrncAplctnTypeCd", expected.getInsuranceLineCode(), actual.getX12InsrncLineTypeCd());
	
		assertTrue("isPolicyChanged", actual.isPolicyChanged());
	}
	
	
	@Test
	public void test_mapSbmToStaging_NoChange() {
		
		String state = "NY";
		String issuerId = "88877";
		String tenantId = state + "0";
		String exchangePolicyId = "0009913";
		int rcn = 1; 
		String qhpId = TestDataSBMUtility.makeQhpId(issuerId, tenantId);
		PolicyType expected = TestDataSBMUtility.makePolicyType(rcn, qhpId, exchangePolicyId);
		
		SBMPolicyDTO inboundDTO = new SBMPolicyDTO();
		inboundDTO.setFileInfo(TestDataSBMUtility.makeFileInformationType(tenantId, issuerId, TestDataSBMUtility.FILES_STATE_WIDE));
		inboundDTO.setPolicy(expected);
		
		SbmPolicyVersionPO epsPO = new SbmPolicyVersionPO();
		
		epsPO.setExchangePolicyId(exchangePolicyId);
		epsPO.setPolicyStartDate(DateTimeUtil.getLocalDateFromXmlGC(expected.getPolicyStartDate()));
		epsPO.setPolicyEndDate(DateTimeUtil.getLocalDateFromXmlGC(expected.getPolicyEndDate()));
		epsPO.setSubscriberStateCd(state);
		epsPO.setIssuerHiosId(issuerId);
		epsPO.setIssuerPolicyId(expected.getIssuerAssignedPolicyId());
		epsPO.setIssuerSubscriberID(expected.getIssuerAssignedSubscriberId());
		epsPO.setExchangeAssignedSubscriberID(expected.getExchangeAssignedSubscriberId());
		epsPO.setTransControlNum(String.valueOf(expected.getRecordControlNumber()));
		epsPO.setPlanID(qhpId);
		epsPO.setX12InsrncLineTypeCd(expected.getInsuranceLineCode());
		epsPO.setSourceExchangeId(tenantId);
		
		SbmPolicyVersionPO actual = mapper.mapSbmToStaging(inboundDTO, epsPO);
		
		assertNotNull("StagingPolicyVersionPO should not be null", actual);
		
		assertFalse("isPolicyChanged", actual.isPolicyChanged());
	}

}
