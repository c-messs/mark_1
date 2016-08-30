package gov.hhs.cms.ff.fm.eps.ep.mappers;

import gov.cms.dsh.bem.GenderCodeSimpleType;
import gov.cms.dsh.bem.IndividualNameType;
import gov.cms.dsh.bem.MemberAdditionalIdentifierType;
import gov.cms.dsh.bem.MemberDemographicsType;
import gov.cms.dsh.bem.MemberNameInfoType;
import gov.cms.dsh.bem.MemberRelatedInfoType;
import gov.cms.dsh.bem.MemberType;
import gov.hhs.cms.ff.fm.eps.ep.data.util.TestDataUtil;
import gov.hhs.cms.ff.fm.eps.ep.po.PolicyMemberVersionPO;
import gov.hhs.cms.ff.fm.eps.ep.util.DateTimeUtil;

import java.time.LocalDate;

import org.junit.Test;
import org.springframework.test.util.ReflectionTestUtils;

public class PolicyMemberVersionMapperTest extends BaseMapperTest {

	private PolicyMemberVersionMapper mapper = new PolicyMemberVersionMapper();

	
	/*
	 *  Tests Mapping VO to PO and determining if PO Latest has changed.
	 *  
	 *  - Similates a VO containing data from input and overwriting (merging) latest PO data (from DB),
	 *    which in this case is null (INITIAL).
	 */
	@Test
	public void test_mapFFMToEPS_NullEPS() {
		
		Long memId = new Long(TestDataUtil.getRandom3DigitNumber());
		String name = "DAU";
		String expectedHiosId = "11111";
		String expectedState = "MS";
		MemberType expectedMember = TestDataUtil.makeMemberTypeSubscriber(memId, name, expectedHiosId, expectedState);
		
		// Initials will not have any data in EPS, therefore
		PolicyMemberVersionPO epsPO = null; //Actually empty
			
		PolicyMemberVersionPO po = mapper.mapFFMToEPS(expectedMember, epsPO);
		
		assertNotNull("PolicyMemberVersionPO", po);
		
		assertEquals("PolicyMemberEligStartDate", expectedMember.getMemberRelatedDates().getEligibilityBeginDate(),
				DateTimeUtil.getXMLGregorianCalendar(po.getPolicyMemberEligStartDate()));
		assertEquals("PolicyMemberEligEndDate",expectedMember.getMemberRelatedDates().getEligibilityEndDate(),
				DateTimeUtil.getXMLGregorianCalendar(po.getPolicyMemberEligEndDate()));	
		assertEquals("subscriberInd",expectedMember.getMemberInformation().getSubscriberIndicator().value(),
				po.getSubscriberInd());
		assertEquals("issuerAssignedMemberID",expectedMember.getMemberAdditionalIdentifier().getIssuerAssignedMemberID(),
				po.getIssuerAssignedMemberID());
		assertEquals("exchangeMemberID",expectedMember.getMemberAdditionalIdentifier().getExchangeAssignedMemberID(),
				po.getExchangeMemberID());
		// Set in FFMMemberDAO from policy.
		assertNull("maintenanceStartDateTime", po.getMaintenanceStartDateTime());
		assertEquals("maintenanceEndDateTime",DateTimeUtil.HIGHDATE, po.getMaintenanceEndDateTime());
		assertEquals("policyMemberLastNm",expectedMember.getMemberNameInformation().getMemberName().getLastName(),
				po.getPolicyMemberLastNm());
		assertEquals("policyMemberFirstNm",expectedMember.getMemberNameInformation().getMemberName().getFirstName(),
				po.getPolicyMemberFirstNm());
		assertEquals("policyMemberMiddleNm",expectedMember.getMemberNameInformation().getMemberName().getMiddleName(),
				po.getPolicyMemberMiddleNm());
		assertEquals("policyMemberSalutationNm",expectedMember.getMemberNameInformation().getMemberName().getNamePrefix(),
				po.getPolicyMemberSalutationNm());
		assertEquals("policyMemberSuffixNm",expectedMember.getMemberNameInformation().getMemberName().getNameSuffix(),
				po.getPolicyMemberSuffixNm());
		assertEquals("policyMemberSSN",expectedMember.getMemberNameInformation().getMemberName().getSocialSecurityNumber(),
				po.getPolicyMemberSSN());
		//ExchangePolicyId and SubscriberStateCd mapping is done in FFMDataService
		assertEquals("policyMemberBirthDate", expectedMember.getMemberNameInformation().getMemberDemographics().getBirthDate(),
				DateTimeUtil.getXMLGregorianCalendar(po.getPolicyMemberBirthDate()));
		assertEquals("x12GenderTypeCd",expectedMember.getMemberNameInformation().getMemberDemographics().getGenderCode(),
				GenderCodeSimpleType.valueOf(po.getX12GenderTypeCd()));

		assertTrue("policyMemberChanged", po.isPolicyMemberChanged());
	}	
	
	
	/*
	 *  Tests Mapping VO to PO and determining PO Latest has changed.
	 *  
	 */
	@Test
	public void test_mapFFMToEPS_Change() {
		
		String inputLastName = "Stevens";
		String latestLastName = "Stevenz"; // <-- 1 character change
	
		MemberType expectedMember = new MemberType();
		expectedMember.setMemberNameInformation(new MemberNameInfoType());
		expectedMember.getMemberNameInformation().setMemberName(new IndividualNameType());
		expectedMember.getMemberNameInformation().getMemberName().setLastName(inputLastName);
		
		PolicyMemberVersionPO epsPO = new PolicyMemberVersionPO();
		epsPO.setPolicyMemberLastNm(latestLastName);
			
		PolicyMemberVersionPO poActual = mapper.mapFFMToEPS(expectedMember, epsPO);
		
		assertNotNull("PolicyMemberVersionPO", poActual);
		
		// Returned PO should have been overwritten with input VO data
		assertEquals("policyMemberLastNm",expectedMember.getMemberNameInformation().getMemberName().getLastName(),
				poActual.getPolicyMemberLastNm());
		assertNotSame("policyMemberLastNm", epsPO.getPolicyMemberLastNm(),
				poActual.getPolicyMemberLastNm());

		assertTrue("policyMemberChanged", poActual.isPolicyMemberChanged());
	}	
	
	/*
	 *  Tests Mapping VO to PO and determining PO Latest has changed.
	 *  
	 */
	@Test
	public void test_mapFFMToEPS_NoChange() {
		
		String inputLastName = "Smith";
		String latestLastName = "Smith";
	
		MemberType expectedMember = new MemberType();
		expectedMember.setMemberNameInformation(new MemberNameInfoType());
		expectedMember.getMemberNameInformation().setMemberName(new IndividualNameType());
		expectedMember.getMemberNameInformation().getMemberName().setLastName(inputLastName);
		
		
		PolicyMemberVersionPO epsPO = new PolicyMemberVersionPO();
		epsPO.setPolicyMemberLastNm(latestLastName);
			
		PolicyMemberVersionPO poActual = mapper.mapFFMToEPS(expectedMember, epsPO);
		
		assertNotNull("PolicyMemberVersionPO", poActual);
		
		// Returned PO should have been overwritten with input VO data
		assertEquals("policyMemberLastNm",expectedMember.getMemberNameInformation().getMemberName().getLastName(),
				poActual.getPolicyMemberLastNm());

		// since same value, no change
		assertFalse("policyMemberChanged", poActual.isPolicyMemberChanged());
	}	
	
	@Test
	public void testMapMemberAdditionalIdentifierType_EmptyMemberAdditionalIdentifierType() {
		
		MemberType expectedMember = new MemberType();
		expectedMember.setMemberAdditionalIdentifier(new MemberAdditionalIdentifierType());
		PolicyMemberVersionPO po = new PolicyMemberVersionPO();
		ReflectionTestUtils.invokeMethod(mapper, "mapMemberAdditionalIdentifierType", new Object[] {expectedMember.getMemberAdditionalIdentifier(), po});
		assertNotNull("MemberType", expectedMember);
		assertNull("ExchangeAssignedMemberID", po.getExchangeMemberID());
		assertNull("IssuerAssignedMemberID", po.getIssuerAssignedMemberID());
	}
	
	@Test
	public void testMapMemberInformation_EmptyMemberRelatedInfoType() {
		
		MemberRelatedInfoType memInfo = new MemberRelatedInfoType();
		PolicyMemberVersionPO po = new PolicyMemberVersionPO();
		ReflectionTestUtils.invokeMethod(mapper, "mapMemberInformation", new Object[] {memInfo, po});
		assertNotNull("MemberType", po);
		assertNull("SubscriberInd", po.getSubscriberInd());
	}
	

	@Test
	public void test_MapMemberNameInformation_null() {
		
		MemberType expectedMember = new MemberType();
		PolicyMemberVersionPO po = new PolicyMemberVersionPO();
		ReflectionTestUtils.invokeMethod(mapper, "mapMemberNameInformationType", new Object[] {expectedMember, po});
		assertNotNull("MemberType", expectedMember);
		assertNull("PolicyMemberFirstNm", po.getPolicyMemberFirstNm());
		assertNull("PolicyMemberLastNm", po.getPolicyMemberLastNm());
		assertNull("PolicyMemberSSN", po.getPolicyMemberSSN());
	}
	
	@Test
	public void test_MapMemberNameInformation_null_IndividualNameType() {
		
		MemberType expectedMember = new MemberType();
		expectedMember.setMemberNameInformation(new MemberNameInfoType());
		//expectedMember.getMemberNameInformation().setMemberName(new M);
		PolicyMemberVersionPO po = new PolicyMemberVersionPO();
		ReflectionTestUtils.invokeMethod(mapper, "mapMemberNameInformationType", new Object[] {expectedMember, po});
		assertNotNull("MemberType", expectedMember);
		assertNull("PolicyMemberFirstNm", po.getPolicyMemberFirstNm());
		assertNull("PolicyMemberLastNm", po.getPolicyMemberLastNm());
		assertNull("PolicyMemberSSN", po.getPolicyMemberSSN());
	}
	
	@Test
	public void test_MapMemberNameInformation_null_LastNm() {
		
		MemberType expectedMember = new MemberType();
		expectedMember.setMemberNameInformation(new MemberNameInfoType());
		expectedMember.getMemberNameInformation().setMemberName(TestDataUtil.makeIndividualNameType(Long.valueOf("111111"), "DAU"));
		expectedMember.getMemberNameInformation().getMemberName().setLastName(null);
		PolicyMemberVersionPO po = new PolicyMemberVersionPO();
		ReflectionTestUtils.invokeMethod(mapper, "mapMemberNameInformationType", new Object[] {expectedMember, po});
		assertNotNull("MemberType", expectedMember);
		assertNotNull("PolicyMemberFirstNm", po.getPolicyMemberFirstNm());
		assertNull("PolicyMemberLastNm", po.getPolicyMemberLastNm());
		assertNotNull("PolicyMemberSSN", po.getPolicyMemberSSN());
	}
	
	@Test
	public void test_MapMemberNameInformation_Subscriber_null_Gender_Birtdate() {
		
		MemberType expectedMember = makeSubscriber();
		expectedMember.setMemberNameInformation(new MemberNameInfoType());
		expectedMember.getMemberNameInformation().setMemberDemographics(new MemberDemographicsType());
		PolicyMemberVersionPO po = new PolicyMemberVersionPO();
		ReflectionTestUtils.invokeMethod(mapper, "mapMemberNameInformationType", new Object[] {expectedMember, po});
		assertNull("GenderTypeCd", po.getX12GenderTypeCd());
		assertNull("PolicyMemberBirthDate", po.getPolicyMemberBirthDate());
	}
	
	@Test
	public void test_MapMemberNameInformation_nonSubscriber() {
		
		LocalDate expectedBD = APR_1;
		GenderCodeSimpleType expectedGender = GenderCodeSimpleType.F;
		MemberType expectedMember = new MemberType();
		expectedMember.setMemberNameInformation(new MemberNameInfoType());
		expectedMember.getMemberNameInformation().setMemberDemographics(new MemberDemographicsType());
		expectedMember.getMemberNameInformation().getMemberDemographics().setBirthDate(DateTimeUtil.getXMLGregorianCalendar(expectedBD));
		expectedMember.getMemberNameInformation().getMemberDemographics().setGenderCode(GenderCodeSimpleType.F);
		PolicyMemberVersionPO po = new PolicyMemberVersionPO();
		ReflectionTestUtils.invokeMethod(mapper, "mapMemberNameInformationType", new Object[] {expectedMember, po});
		assertEquals("PolicyMemberBirthDate", expectedBD, po.getPolicyMemberBirthDate());
		assertEquals("X12GenderTypeCd", expectedGender.value(), po.getX12GenderTypeCd());
	}
	
	
	
}
