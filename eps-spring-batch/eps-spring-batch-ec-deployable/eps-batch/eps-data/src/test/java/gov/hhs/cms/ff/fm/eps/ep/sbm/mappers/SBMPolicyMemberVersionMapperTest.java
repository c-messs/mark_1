package gov.hhs.cms.ff.fm.eps.ep.sbm.mappers;


import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import gov.cms.dsh.sbmi.PolicyMemberType;
import gov.hhs.cms.ff.fm.eps.ep.po.SbmPolicyMemberVersionPO;
import gov.hhs.cms.ff.fm.eps.ep.util.DateTimeUtil;
import gov.hhs.cms.ff.fm.eps.ep.util.sbm.TestDataSBMUtility;

public class SBMPolicyMemberVersionMapperTest extends SBMBaseMapperTest {

	private SbmPolicyMemberVersionMapper mapper = new SbmPolicyMemberVersionMapper();

	
	/*
	 *  Tests Mapping inbound SBM XPR member to EPS PO and determining if PO Latest has changed.
	 *  
	 *  - Similates a VO containing data from input and overwriting latest EPS PO data,
	 *    which in this case is null.
	 */
	@Test
	public void test_mapSBMToStaging_NullEPS() {
		
		Long memId = Long.valueOf("888888888");
		String name = "DAU";
		String expectedExchangePolicyId = "1111111111";
		boolean isSubscriber = false;
		boolean expectedIsChanged = true;
		PolicyMemberType expectedMember = TestDataSBMUtility.makePolicyMemberType(expectedExchangePolicyId, memId, name, isSubscriber);
		
		// Initials will not have any data in EPS, therefore
		SbmPolicyMemberVersionPO epsPO = null; //Actually empty
			
		SbmPolicyMemberVersionPO actual = mapper.mapSbmToStaging(expectedMember, epsPO);
		
		assertNotNull("SbmPolicyMemberVersionPO", actual);
		
		assertEquals("SubscriberInd",expectedMember.getSubscriberIndicator(), actual.getSubscriberInd());
		assertEquals("IssuerAssignedMemberID",expectedMember.getIssuerAssignedMemberId(), actual.getIssuerAssignedMemberID());
		assertEquals("ExchangeAssignedMemberId",expectedMember.getExchangeAssignedMemberId(), actual.getExchangeMemberID());
		// Set in SBMMemberDAO from policy.
		assertNull("MaintenanceStartDateTime", actual.getMaintenanceStartDateTime());
		// MaintenanceEndDateTime uses Oracle default value.
		assertEquals("PolicyMemberLastNm",expectedMember.getMemberLastName(), actual.getPolicyMemberLastNm());
		assertEquals("PolicyMemberFirstNm",expectedMember.getMemberFirstName(), actual.getPolicyMemberFirstNm());
		assertEquals("PolicyMemberMiddleNm",expectedMember.getMemberMiddleName(), actual.getPolicyMemberMiddleNm());
		assertEquals("PolicyMemberSalutationNm",expectedMember.getNamePrefix(), actual.getPolicyMemberSalutationNm());
		assertEquals("PolicyMemberSuffixNm",expectedMember.getNameSuffix(), actual.getPolicyMemberSuffixNm());
		assertEquals("PolicyMemberSSN",expectedMember.getSocialSecurityNumber(), actual.getPolicyMemberSSN());
		//ExchangePolicyId and SubscriberStateCd mapping is done in SBMDataService
		assertEquals("PolicyMemberBirthDate", expectedMember.getBirthDate(), DateTimeUtil.getXMLGregorianCalendar(actual.getPolicyMemberBirthDate()));
		assertEquals("x12GenderTypeCd", expectedMember.getGenderCode(), actual.getX12GenderTypeCd());
		assertEquals("PolicyMember Changed", expectedIsChanged, actual.isPolicyMemberChanged());
		
		// set in SbmXprServiceImpl, so should still be null after mapping.
		assertNull("SbmTransMsgId", actual.getSbmTransMsgID());
	}	
	
	
	/*
	 *  Tests Mapping inbound SBM XPR to PO and determining if Latest EPS PO has changed.
	 *  
	 */
	@Test
	public void test_mapSBMToStaging_Change() {
		
		String inputLastName = "Stevens";
		String latestLastName = "Stevenz"; // <-- 1 character change
		boolean expectedIsChanged = true;
	
		PolicyMemberType expectedMember = new PolicyMemberType();
		expectedMember.setMemberLastName(inputLastName);
		
		SbmPolicyMemberVersionPO epsPO = new SbmPolicyMemberVersionPO();
		epsPO.setPolicyMemberLastNm(latestLastName);
			
		SbmPolicyMemberVersionPO actual = mapper.mapSbmToStaging(expectedMember, epsPO);
		
		assertNotNull("SbmPolicyMemberVersionPO", actual);
		
		// Returned PO should have been overwritten with input VO data
		assertEquals("PolicyMemberLastNm", inputLastName, actual.getPolicyMemberLastNm());
		assertNotSame("policyMemberLastNm", latestLastName, actual.getPolicyMemberLastNm());

		assertEquals("PolicyMember Changed", expectedIsChanged, actual.isPolicyMemberChanged());
	}	
	
	/*
	 *  Tests Mapping VO to PO and determining PO Latest has changed.
	 *  
	 */
	@Test
	public void test_mapSBMToStaging_NoChange() {
		
		String inputLastName = "Smith";
		String latestLastName = "Smith";
		boolean expectedIsChanged = false;
	
		PolicyMemberType expectedMember = new PolicyMemberType();
		expectedMember.setMemberLastName(inputLastName);
		
		SbmPolicyMemberVersionPO epsPO = new SbmPolicyMemberVersionPO();
		epsPO.setPolicyMemberLastNm(latestLastName);
			
		SbmPolicyMemberVersionPO actual = mapper.mapSbmToStaging(expectedMember, epsPO);
		
		assertNotNull("SbmPolicyMemberVersionPO", actual);
		
		// Returned PO should have been overwritten with input VO data
		assertEquals("PolicyMemberLastNm",inputLastName, actual.getPolicyMemberLastNm());

		// since same value, no change
		assertEquals("PolicyMember Changed", expectedIsChanged, actual.isPolicyMemberChanged());
	}
	
	
	@Test
	public void test_mapEpsToSbm() {
		
		String expectedExchangeMemberId = "98989898";
		
		// Currently this mapping is only for PolicyMatch, so only one attribute currently needed.
		
		List<SbmPolicyMemberVersionPO> epsPOList = new ArrayList<SbmPolicyMemberVersionPO>();
		SbmPolicyMemberVersionPO epsPO = new SbmPolicyMemberVersionPO();
		epsPO.setExchangeMemberID(expectedExchangeMemberId);
		
		epsPOList.add(epsPO);
		
		List<PolicyMemberType> actualList = mapper.mapEpsToSbm(epsPOList);
		
		assertEquals("SbmPolicyMemberVersionPO list size", 1, actualList.size());
		PolicyMemberType actual = actualList.get(0);
		
		assertEquals("ExchangeAssignedMemberId", expectedExchangeMemberId, actual.getExchangeAssignedMemberId());
			
	}
	
	
}
