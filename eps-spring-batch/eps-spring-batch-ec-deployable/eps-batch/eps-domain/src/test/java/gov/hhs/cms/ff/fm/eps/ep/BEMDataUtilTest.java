package gov.hhs.cms.ff.fm.eps.ep;

import gov.cms.dsh.bem.AdditionalInfoType;
import gov.cms.dsh.bem.BenefitEnrollmentMaintenanceType;
import gov.cms.dsh.bem.BooleanIndicatorSimpleType;
import gov.cms.dsh.bem.HealthCoverageDatesType;
import gov.cms.dsh.bem.HealthCoverageInfoType;
import gov.cms.dsh.bem.HealthCoveragePolicyNumberType;
import gov.cms.dsh.bem.HealthCoverageType;
import gov.cms.dsh.bem.InsuranceLineCodeSimpleType;
import gov.cms.dsh.bem.MemberAdditionalIdentifierType;
import gov.cms.dsh.bem.MemberRelatedDatesType;
import gov.cms.dsh.bem.MemberRelatedInfoType;
import gov.cms.dsh.bem.MemberType;
import gov.cms.dsh.bem.PolicyInfoType;
import gov.cms.dsh.bem.TransactionInformationType;
import gov.hhs.cms.ff.fm.eps.ep.enums.PolicyStatus;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import javax.xml.datatype.XMLGregorianCalendar;

import org.joda.time.DateTime;
import org.junit.Assert;
import org.junit.Test;

import com.accenture.foundation.common.exception.ApplicationException;


public class BEMDataUtilTest extends BaseUtilTest {


	@Test
	public void test_getBenefitBeginDate_NullMember() {

		DateTime expected = null;
		MemberType member = null;
		DateTime actual = BEMDataUtil.getBenefitBeginDate(member);
		assertEquals("BenefitBeginDate", expected, actual);
	}

	@Test
	public void test_getBenefitBeginDate_EmptyHC() {

		DateTime expected = null;
		MemberType member = new MemberType();
		member.getHealthCoverage().add(new HealthCoverageType());
		DateTime actual = BEMDataUtil.getBenefitBeginDate(member);
		assertEquals("BenefitBeginDate", expected, actual);
	}

	@Test
	public void test_getBenefitBeginDate_EmptyHCDates() {

		DateTime expected = null;
		MemberType member = new MemberType();
		member.getHealthCoverage().add(new HealthCoverageType());
		member.getHealthCoverage().get(0).setHealthCoverageDates(new HealthCoverageDatesType());
		DateTime actual = BEMDataUtil.getBenefitBeginDate(member);
		assertEquals("BenefitBeginDate", expected, actual);
	}

	@Test
	public void test_getBenefitBeginDate() {

		DateTime expected = MAR_1;
		MemberType member = new MemberType();
		member.getHealthCoverage().add(new HealthCoverageType());
		member.getHealthCoverage().get(0).setHealthCoverageDates(new HealthCoverageDatesType());
		member.getHealthCoverage().get(0).getHealthCoverageDates().setBenefitBeginDate(getXMLGregorianCalendar(expected));
		DateTime actual = BEMDataUtil.getBenefitBeginDate(member);
		assertEquals("BenefitBeginDate", expected, actual);
	}
	
	
	@Test
	public void test_getBenefitEndDate_NullMember() {

		DateTime expected = null;
		MemberType member = null;
		DateTime actual = BEMDataUtil.getBenefitEndDate(member);
		assertEquals("BenefitEndDate", expected, actual);
	}

	@Test
	public void test_getBenefitEndDate_EmptyHC() {

		DateTime expected = null;
		MemberType member = new MemberType();
		member.getHealthCoverage().add(new HealthCoverageType());
		DateTime actual = BEMDataUtil.getBenefitEndDate(member);
		assertEquals("BenefitEndDate", expected, actual);
	}

	@Test
	public void test_getBenefitEndDate_EmptyHCDates() {

		DateTime expected = null;
		MemberType member = new MemberType();
		member.getHealthCoverage().add(new HealthCoverageType());
		member.getHealthCoverage().get(0).setHealthCoverageDates(new HealthCoverageDatesType());
		DateTime actual = BEMDataUtil.getBenefitEndDate(member);
		assertEquals("BenefitEndDate", expected, actual);
	}

	@Test
	public void test_getBenefitEndDate() {

		DateTime expected = MAR_1;
		MemberType member = new MemberType();
		member.getHealthCoverage().add(new HealthCoverageType());
		member.getHealthCoverage().get(0).setHealthCoverageDates(new HealthCoverageDatesType());
		member.getHealthCoverage().get(0).getHealthCoverageDates().setBenefitEndDate(getXMLGregorianCalendar(expected));
		DateTime actual = BEMDataUtil.getBenefitEndDate(member);
		assertEquals("BenefitEndDate", expected, actual);
	}

	@Test
	public void test_getEligibilityBeginDate_NullMember() {

		XMLGregorianCalendar expectedEligBegin = null;
		MemberType member = null;
		DateTime actualEligBegin = BEMDataUtil.getEligibilityBeginDate(member);
		assertEquals("EligibilityBeginDate", expectedEligBegin, actualEligBegin);
	}

	@Test
	public void test_getEligibilityBeginDate_NullDates() {

		XMLGregorianCalendar expectedEligBegin = null;
		MemberType member = new MemberType();
		DateTime actualEligBegin = BEMDataUtil.getEligibilityBeginDate(member);
		assertEquals("EligibilityBeginDate", expectedEligBegin, actualEligBegin);
	}

	@Test
	public void test_getEligibilityBeginDate_NullEligibilityBegin() {

		XMLGregorianCalendar expectedEligBegin = null;
		MemberType member = new MemberType();
		member.setMemberRelatedDates(new MemberRelatedDatesType());
		DateTime actualEligBegin = BEMDataUtil.getEligibilityBeginDate(member);
		assertEquals("EligibilityBeginDate", expectedEligBegin, actualEligBegin);
	}

	@Test
	public void test_getEligibilityBeginDate() {

		DateTime expected = JAN_1;
		MemberType member = new MemberType();
		member.setMemberRelatedDates(new MemberRelatedDatesType());
		member.getMemberRelatedDates().setEligibilityBeginDate(getXMLGregorianCalendar(expected));
		DateTime actualEligBegin = BEMDataUtil.getEligibilityBeginDate(member);
		assertEquals("EligibilityBeginDate", expected, actualEligBegin); 
	}
	
	@Test
	public void test_getEligibilityEndDate_NullMember() {

		XMLGregorianCalendar expected = null;
		MemberType member = null;
		DateTime actual = BEMDataUtil.getEligibilityEndDate(member);
		assertEquals("EligibilityEndDate", expected, actual);
	}


	@Test
	public void test_getEligibilityEndDate_NullDates() {

		XMLGregorianCalendar expected = null;
		MemberType member = new MemberType();
		DateTime actual = BEMDataUtil.getEligibilityEndDate(member);
		assertEquals("EligibilityEndDate", expected, actual);
	}

	@Test
	public void test_getEligibilityEndDate_NullEligibilityEnd() {

		XMLGregorianCalendar expected = null;
		MemberType member = new MemberType();
		member.setMemberRelatedDates(new MemberRelatedDatesType());
		DateTime actual = BEMDataUtil.getEligibilityEndDate(member);
		assertEquals("EligibilityEndDate", expected, actual);
	}

	@Test
	public void test_getEligibilityEndDate() {

		DateTime expected = JAN_1;
		MemberType member = new MemberType();
		member.setMemberRelatedDates(new MemberRelatedDatesType());
		member.getMemberRelatedDates().setEligibilityEndDate(getXMLGregorianCalendar(expected));
		DateTime actual = BEMDataUtil.getEligibilityEndDate(member);
		assertEquals("EligibilityEndDate", expected, actual); 
	}

	
	@Test
	public void test_getPolicyStartDate_null() {
		DateTime expected = null;
		BenefitEnrollmentMaintenanceType bem = null;
		DateTime actual = BEMDataUtil.getPolicyStartDate(bem);
		Assert.assertEquals("getPolicyStartDate null", expected, actual);
	}

	@Test
	public void test_getPolicyStartDate_empty() {
		DateTime expected = null;
		BenefitEnrollmentMaintenanceType bem = new BenefitEnrollmentMaintenanceType();
		DateTime actual = BEMDataUtil.getPolicyStartDate(bem);
		Assert.assertEquals("getPolicyStartDate null", expected, actual);
	}

	@Test
	public void test_getPolicyStartDate_empty_dates() {
		DateTime expected = null;
		BenefitEnrollmentMaintenanceType bem = new BenefitEnrollmentMaintenanceType();
		bem.setPolicyInfo(new PolicyInfoType());
		DateTime actual = BEMDataUtil.getPolicyStartDate(bem);
		Assert.assertEquals("getPolicyStartDate null", expected, actual);
	}

	@Test
	public void test_getPolicyStartDate() {
		DateTime expected = JAN_15;
		BenefitEnrollmentMaintenanceType bem = new BenefitEnrollmentMaintenanceType();
		bem.setPolicyInfo(new PolicyInfoType());
		bem.getPolicyInfo().setPolicyStartDate(getXMLGregorianCalendar(expected));
		DateTime actual = BEMDataUtil.getPolicyStartDate(bem);
		Assert.assertEquals("getPolicyStartDate null", expected, actual);
	}


	@Test
	public void test_getHealthCoverageType_NullMember() {

		HealthCoverageType expectedHC = null;
		MemberType member = null;
		HealthCoverageType actualHC = BEMDataUtil.getHealthCoverageType(member);
		assertEquals("HealthCoverageType", expectedHC, actualHC);
	}

	@Test
	public void test_getHealthCoverageType_NullSubscriber() {

		HealthCoverageType expectedHC = null;
		MemberType subscriber = null;
		HealthCoverageType actualHC = BEMDataUtil.getHealthCoverageType(subscriber);
		assertEquals("HealthCoverageType", expectedHC, actualHC);
	}

	@Test
	public void test_getHealthCoverageType_EmptyHC() {

		HealthCoverageType expectedHC = null;
		MemberType subscriber = new MemberType();
		subscriber.setMemberInformation(new MemberRelatedInfoType());
		subscriber.getMemberInformation().setSubscriberIndicator(BooleanIndicatorSimpleType.Y);
		HealthCoverageType actualHC = BEMDataUtil.getHealthCoverageType(subscriber);
		assertEquals("HealthCoverageType", expectedHC, actualHC);
	}

	@Test
	public void test_getHealthCoverageType() {

		MemberType subscriber = new MemberType();
		subscriber.setMemberInformation(new MemberRelatedInfoType());
		subscriber.getMemberInformation().setSubscriberIndicator(BooleanIndicatorSimpleType.Y);
		subscriber.getHealthCoverage().add(new HealthCoverageType());
		HealthCoverageType actualHC = BEMDataUtil.getHealthCoverageType(subscriber);
		assertNotNull("HealthCoverageType", actualHC);
	}
	
	@Test
	public void test_getHealthCoverageDatesType_NullMember() {

		HealthCoverageDatesType expectedHC = null;
		MemberType member = null;
		HealthCoverageDatesType actualHC = BEMDataUtil.getHealthCoverageDatesType(member);
		assertEquals("HealthCoverageType", expectedHC, actualHC);
	}

	@Test
	public void test_getHealthCoverageDatesType_NullSubscriber() {

		HealthCoverageDatesType expectedHC = null;
		MemberType subscriber = null;
		HealthCoverageDatesType actualHC = BEMDataUtil.getHealthCoverageDatesType(subscriber);
		assertEquals("HealthCoverageType", expectedHC, actualHC);
	}

	@Test
	public void test_getHealthCoverageDatesType_EmptyHC() {

		HealthCoverageDatesType expectedHC = null;
		MemberType subscriber = new MemberType();
		subscriber.setMemberInformation(new MemberRelatedInfoType());
		subscriber.getMemberInformation().setSubscriberIndicator(BooleanIndicatorSimpleType.Y);
		HealthCoverageDatesType actualHC = BEMDataUtil.getHealthCoverageDatesType(subscriber);
		assertEquals("HealthCoverageType", expectedHC, actualHC);
	}

	@Test
	public void test_getHealthCoverageDatesType() {

		MemberType subscriber = new MemberType();
		subscriber.setMemberInformation(new MemberRelatedInfoType());
		subscriber.getMemberInformation().setSubscriberIndicator(BooleanIndicatorSimpleType.Y);
		subscriber.getHealthCoverage().add(new HealthCoverageType());
		subscriber.getHealthCoverage().get(0).setHealthCoverageDates(new HealthCoverageDatesType());
		HealthCoverageDatesType actualHC = BEMDataUtil.getHealthCoverageDatesType(subscriber);
		assertNotNull("HealthCoverageType", actualHC);
	}
	
	
	/*
	
	public static HealthCoverageDatesType getHealthCoverageDatesType(MemberType member) {

		HealthCoverageDatesType healthCoverageDates = null;

		HealthCoverageType healthCoverage = getHealthCoverageType(member);

		if (healthCoverage != null) {

			healthCoverageDates = healthCoverage.getHealthCoverageDates();
		}
		return healthCoverageDates;
	}
	 */


	@Test
	public void test_getInsuranceLineCode_NullMember() {

		String expectedILC = null;
		MemberType member = null;
		String actualILC = BEMDataUtil.getInsuranceLineCode(member);
		assertEquals("InsuranceLineCode", expectedILC, actualILC);
	}

	@Test
	public void test_getInsuranceLineCode_EmptyMember() {

		String expectedILC = null;
		MemberType member = new MemberType();
		String actualILC = BEMDataUtil.getInsuranceLineCode(member);
		assertEquals("InsuranceLineCode", expectedILC, actualILC);
	}

	@Test
	public void test_getInsuranceLineCode_EmptyHC() {

		String expectedILC = null;
		MemberType member = new MemberType();
		member.getHealthCoverage().add(new HealthCoverageType());
		String actualILC = BEMDataUtil.getInsuranceLineCode(member);
		assertEquals("InsuranceLineCode", expectedILC, actualILC);
	}

	// TODO fix NPE in method.
	@Test
	public void test_getInsuranceLineCode_EmptyHCInfo() {

		MemberType member = new MemberType();
		member.getHealthCoverage().add(new HealthCoverageType());
		member.getHealthCoverage().get(0).setHealthCoverageInformation(new HealthCoverageInfoType());
		try {
			BEMDataUtil.getInsuranceLineCode(member);
		} catch (NullPointerException npe) {
			assertTrue("NPE is thrown with empty HealthCoverageInfoType", true);
		}
	}

	@Test
	public void test_getInsuranceLineCode() {

		InsuranceLineCodeSimpleType expectedILC = InsuranceLineCodeSimpleType.HLT;
		MemberType member = new MemberType();
		member.getHealthCoverage().add(new HealthCoverageType());
		member.getHealthCoverage().get(0).setHealthCoverageInformation(new HealthCoverageInfoType());
		member.getHealthCoverage().get(0).getHealthCoverageInformation().setInsuranceLineCode(expectedILC);
		String actualILC = BEMDataUtil.getInsuranceLineCode(member);
		assertEquals("InsuranceLineCode", expectedILC.value(), actualILC);
	}

	@Test
	public void test_getContractCode_NullMember() {

		String expectedContractCode = null;
		MemberType member = null;
		String actualContractCode = BEMDataUtil.getContractCode(member);
		assertEquals("ContractCode", expectedContractCode, actualContractCode);
	}

	@Test
	public void test_getContractCode_EmptyMember() {

		String expectedContractCode = null;
		MemberType member = new MemberType();
		String actualContractCode = BEMDataUtil.getContractCode(member);
		assertEquals("ContractCode", expectedContractCode, actualContractCode);
	}

	@Test
	public void test_getContractCode_EmptyHCPolicyNumber() {

		String expectedContractCode = null;
		MemberType member = new MemberType();
		member.getHealthCoverage().add(new HealthCoverageType());
		member.getHealthCoverage().get(0).setHealthCoveragePolicyNumber(new HealthCoveragePolicyNumberType());
		String actualContractCode = BEMDataUtil.getContractCode(member);
		assertEquals("ContractCode", expectedContractCode, actualContractCode);
	}


	@Test
	public void test_getContractCode() {

		String expectedContractCode = "CC9999";
		MemberType member = new MemberType();
		member.getHealthCoverage().add(new HealthCoverageType());
		member.getHealthCoverage().get(0).setHealthCoveragePolicyNumber(new HealthCoveragePolicyNumberType());
		member.getHealthCoverage().get(0).getHealthCoveragePolicyNumber().setContractCode(expectedContractCode);
		String actualContractCode = BEMDataUtil.getContractCode(member);
		assertEquals("ContractCode", expectedContractCode, actualContractCode);
	}


	@Test
	public void test_getTotalPremiumAmount() {

		BigDecimal expectedTPA = BigDecimal.valueOf(88.88);
		MemberType member = new MemberType();
		AdditionalInfoType aitTPA = new AdditionalInfoType();
		aitTPA.setTotalPremiumAmount(expectedTPA);
		member.getAdditionalInfo().add(aitTPA);
		BigDecimal actualTPA = BEMDataUtil.getTotalPremiumAmount(member);
		assertEquals("TotalPremiumAmount", expectedTPA, actualTPA);
	}

	@Test
	public void test_getTotalPremiumAmount_NullMember() {

		BigDecimal expectedTPA = null;
		MemberType member = null;
		BigDecimal actualTPA = BEMDataUtil.getTotalPremiumAmount(member);
		assertEquals("TotalPremiumAmount", expectedTPA, actualTPA);
	}

	@Test
	public void test_getTotalPremiumAmount_EmptyList() {

		BigDecimal expectedTPA = null;
		MemberType member = new MemberType();
		BigDecimal actualTPA = BEMDataUtil.getTotalPremiumAmount(member);
		assertEquals("TotalPremiumAmount", expectedTPA, actualTPA);
	}

	@Test
	public void test_getTotalPremiumAmount_NoTPA() {

		BigDecimal expectedTPA = null;
		MemberType member = new MemberType();
		AdditionalInfoType aitAPTC = new AdditionalInfoType();
		aitAPTC.setAPTCAmount(BigDecimal.valueOf(66.66));
		member.getAdditionalInfo().add(aitAPTC);
		BigDecimal actualTPA = BEMDataUtil.getTotalPremiumAmount(member);
		assertEquals("TotalPremiumAmount", expectedTPA, actualTPA);
	}



	@Test
	public void testSortAdditionalInfos_EmptyList() {

		List<AdditionalInfoType> aitList = new ArrayList<AdditionalInfoType>();
		BEMDataUtil.sortAdditionalInfos(aitList);
		assertNotNull("AdditionalInfoType", aitList);	
	}

	@Test
	public void testSortAdditionalInfos() {

		List<AdditionalInfoType> aitList = new ArrayList<AdditionalInfoType>();

		BigDecimal expectedAPTC = BigDecimal.valueOf(33.33);
		BigDecimal expectedTPA = BigDecimal.valueOf(11.11);
		BigDecimal expectedTIRA = BigDecimal.valueOf(22.22);

		AdditionalInfoType aitAPTC = new AdditionalInfoType();
		aitAPTC.setAPTCAmount(expectedAPTC);
		aitAPTC.setEffectiveStartDate(getXMLGregorianCalendar(MAR_1));

		AdditionalInfoType aitTPA = new AdditionalInfoType();
		aitTPA.setTotalPremiumAmount(expectedTPA);
		aitTPA.setEffectiveStartDate(getXMLGregorianCalendar(JAN_1));

		AdditionalInfoType aitTIRA = new AdditionalInfoType();
		aitTIRA.setTotalIndividualResponsibilityAmount(expectedTIRA);
		aitTIRA.setEffectiveStartDate(getXMLGregorianCalendar(FEB_1));

		aitList.add(aitAPTC);
		aitList.add(aitTPA);
		aitList.add(aitTIRA);

		BEMDataUtil.sortAdditionalInfos(aitList);

		assertNotNull("AdditionalInfoType", aitList);	
		assertEquals("TotalPremiumAmount (element now 0)", expectedTPA, aitList.get(0).getTotalPremiumAmount());
		assertEquals("TotalIndividualResponsibilityAmount (element now 1)", expectedTIRA, aitList.get(1).getTotalIndividualResponsibilityAmount());
		assertEquals("APTCAmount (element now 2)", expectedAPTC, aitList.get(2).getAPTCAmount());
	}


	/*
	 * NOTE:  This test fails when run with JRE 1.8
	 * 
	 * Passes for jdk1.6.0_29_x64
	 */
	@Test
	public void testSortAdditionalInfos_NullEffectiveStarts() {

		List<AdditionalInfoType> aitList = new ArrayList<AdditionalInfoType>();

		BigDecimal expectedAPTC = BigDecimal.valueOf(33.33);
		BigDecimal expectedTPA = BigDecimal.valueOf(11.11);
		BigDecimal expectedTIRA = BigDecimal.valueOf(22.22);

		AdditionalInfoType ait1 = new AdditionalInfoType();
		ait1.setAPTCAmount(expectedAPTC);
		ait1.setEffectiveStartDate(getXMLGregorianCalendar(MAR_1));

		AdditionalInfoType ait2 = new AdditionalInfoType();
		ait2.setTotalPremiumAmount(expectedTPA);

		AdditionalInfoType ait3 = new AdditionalInfoType();
		ait3.setTotalIndividualResponsibilityAmount(expectedTIRA);
		ait3.setEffectiveStartDate(getXMLGregorianCalendar(FEB_1));

		aitList.add(ait1);
		aitList.add(ait2);
		aitList.add(ait3);

		BEMDataUtil.sortAdditionalInfos(aitList);

		assertNotNull("AdditionalInfoType", aitList);	
		assertEquals("TotalIndividualResponsibilityAmount (element now 0)", expectedTIRA, aitList.get(0).getTotalIndividualResponsibilityAmount());
		assertEquals("APTCAmount (element now 1)", expectedAPTC, aitList.get(1).getAPTCAmount());
		assertEquals("TotalPremiumAmount (element now 2)", expectedTPA, aitList.get(2).getTotalPremiumAmount());
	}

	@Test
	public void test_getSubscriberMember_NullBem() {

		BenefitEnrollmentMaintenanceType bem = null;
		MemberType subscriber = BEMDataUtil.getSubscriberMember(bem);
		Assert.assertNull("Subscriber", subscriber);
	}

	@Test
	public void test_getSubscriberMember_NullMember() {

		BenefitEnrollmentMaintenanceType bem = new BenefitEnrollmentMaintenanceType();
		MemberType member = null;
		bem.getMember().add(member);
		MemberType subscriber = BEMDataUtil.getSubscriberMember(bem);
		Assert.assertNull("Subscriber", subscriber);
	}

	@Test
	public void test_getSubscriberMember_NullMemberInformation() {

		BenefitEnrollmentMaintenanceType bem = new BenefitEnrollmentMaintenanceType();
		MemberType member = new MemberType();
		bem.getMember().add(member);
		MemberType subscriber = BEMDataUtil.getSubscriberMember(bem);
		Assert.assertNull("Subscriber", subscriber);
	}

	@Test
	public void test_getSubscriberMember_NullSubscriberIndicator() {

		BenefitEnrollmentMaintenanceType bem = new BenefitEnrollmentMaintenanceType();
		MemberType member = new MemberType();
		member.setMemberInformation(new MemberRelatedInfoType());
		bem.getMember().add(member);
		MemberType subscriber = BEMDataUtil.getSubscriberMember(bem);
		Assert.assertNull("Subscriber", subscriber);
	}

	@Test
	public void test_getSubscriberMember_SubscriberIndicator() {

		BenefitEnrollmentMaintenanceType bem = new BenefitEnrollmentMaintenanceType();
		MemberType member = new MemberType();
		member.setMemberInformation(new MemberRelatedInfoType());
		member.getMemberInformation().setSubscriberIndicator(BooleanIndicatorSimpleType.N);
		bem.getMember().add(member);
		MemberType subscriber = BEMDataUtil.getSubscriberMember(bem);
		Assert.assertNull("Subscriber", subscriber);

		member.getMemberInformation().setSubscriberIndicator(BooleanIndicatorSimpleType.Y);
		subscriber = BEMDataUtil.getSubscriberMember(bem);
		Assert.assertNotNull("Subscriber", subscriber);
	}


	@Test
	public void testExchangeAssignedMemberId_NullMember() {

		MemberType member = null;
		String id = BEMDataUtil.getExchangeAssignedMemberId(member);
		Assert.assertNull("ExchangeAssignedMemberId", id);
	}

	@Test
	public void testExchangeAssignedMemberId_NullMemberAdditionalIdentifier() {

		MemberType member = new MemberType();
		String id = BEMDataUtil.getExchangeAssignedMemberId(member);
		Assert.assertNull("ExchangeAssignedMemberId", id);
	}

	@Test
	public void testExchangeAssignedMemberId() {

		MemberType member = new MemberType();
		member.setMemberAdditionalIdentifier(new MemberAdditionalIdentifierType());
		String id = BEMDataUtil.getExchangeAssignedMemberId(member);
		Assert.assertNull("ExchangeAssignedMemberId", id);

		String expectedId = "888888888";
		member.getMemberAdditionalIdentifier().setExchangeAssignedMemberID(expectedId);
		id = BEMDataUtil.getExchangeAssignedMemberId(member);
		Assert.assertEquals("ExchangeAssignedMemberId", expectedId, id);
	}


	@Test
	public void testIsSubscriber_NullMember() {

		MemberType member = null;
		boolean isSubscriber = BEMDataUtil.getIsSubscriber(member);
		Assert.assertFalse("Subscriber", isSubscriber);
	}

	@Test
	public void testIsSubscriber_NullMemberInformation() {

		MemberType member =  new MemberType();
		boolean isSubscriber = BEMDataUtil.getIsSubscriber(member);
		Assert.assertFalse("Subscriber", isSubscriber);
	}

	@Test
	public void testIsSubscriber_NullSubscriberIndicator() {

		MemberType member =  new MemberType();
		member.setMemberInformation(new MemberRelatedInfoType());
		boolean isSubscriber = BEMDataUtil.getIsSubscriber(member);
		Assert.assertFalse("Subscriber", isSubscriber);
	}

	@Test
	public void testIsSubscriber_No() {

		MemberType member =  new MemberType();
		member.setMemberInformation(new MemberRelatedInfoType());
		member.getMemberInformation().setSubscriberIndicator(BooleanIndicatorSimpleType.N);
		boolean isSubscriber = BEMDataUtil.getIsSubscriber(member);
		Assert.assertFalse("Subscriber", isSubscriber);
	}

	@Test
	public void testIsSubscriber_Yes() {

		MemberType member =  new MemberType();
		member.setMemberInformation(new MemberRelatedInfoType());
		member.getMemberInformation().setSubscriberIndicator(BooleanIndicatorSimpleType.Y);
		boolean isSubscriber = BEMDataUtil.getIsSubscriber(member);
		Assert.assertTrue("Subscriber", isSubscriber);
	}

	@Test
	public void testIsKeyFinancialElement_True() {

		boolean expectedB = true;
		BigDecimal anyAmt = new BigDecimal("99.99");

		AdditionalInfoType aitAPTC = new AdditionalInfoType();
		aitAPTC.setAPTCAmount(anyAmt);
		boolean actualB = BEMDataUtil.isKeyFinancialElement(aitAPTC);
		Assert.assertEquals("APTC is KeyFinancialElement", expectedB, actualB);

		AdditionalInfoType aitCSR = new AdditionalInfoType();
		aitCSR.setCSRAmount(anyAmt);
		actualB = BEMDataUtil.isKeyFinancialElement(aitCSR);
		Assert.assertEquals("CSR is KeyFinancialElement", expectedB, actualB);

		AdditionalInfoType aitTPA = new AdditionalInfoType();
		aitTPA.setTotalPremiumAmount(anyAmt);
		actualB = BEMDataUtil.isKeyFinancialElement(aitTPA);
		Assert.assertEquals("TPA is KeyFinancialElement", expectedB, actualB);

		AdditionalInfoType aitTIRA = new AdditionalInfoType();
		aitTIRA.setTotalIndividualResponsibilityAmount(anyAmt);
		actualB = BEMDataUtil.isKeyFinancialElement(aitTIRA);
		Assert.assertEquals("TIRA is KeyFinancialElement", expectedB, actualB);

	}


	@Test
	public void testIsKeyFinancialElement_False() {

		boolean expectedB = false;
		String anyTxt = "ANY TEXT";

		AdditionalInfoType aitRA = new AdditionalInfoType();
		aitRA.setRatingArea(anyTxt);
		boolean actualB = BEMDataUtil.isKeyFinancialElement(aitRA);
		Assert.assertEquals("RA is KeyFinancialElement", expectedB, actualB);

	}

	@Test
	public void test_getVariantId_Member() {

		String expected = "01";
		MemberType member = makeSubscriber("1111", expected);
		String actual = BEMDataUtil.getVariantId(member);
		Assert.assertEquals("Variant id from member", expected, actual);
	}

	@Test
	public void test_getVariantId_Member_null() {

		String expected = null;
		MemberType member = null;
		String actual = BEMDataUtil.getVariantId(member);
		Assert.assertEquals("Variant id from member", expected, actual);
	}
	
	@Test
	public void test_getVariantId_Member_Empty() {

		String expected = null;
		MemberType member = new MemberType();
		String actual = BEMDataUtil.getVariantId(member);
		Assert.assertEquals("Variant id from member", expected, actual);
	}

	@Test
	public void test_getVariantId_Member_InvalidContractCode() {

		String expected = null;
		MemberType member = makeSubscriber("1111", "Bogus");
		String actual = BEMDataUtil.getVariantId(member);
		Assert.assertEquals("Variant id from member", expected, actual);
	}


	@Test
	public void test_getSubscriberOccurrances() {

		int expectedSize = 2;
		BenefitEnrollmentMaintenanceType bem = new BenefitEnrollmentMaintenanceType();

		bem.getMember().add(makeSubscriber("1111"));
		bem.getMember().add(new MemberType());
		bem.getMember().add(makeSubscriber("1111"));
		MemberType member = new MemberType();
		member.setMemberInformation(new MemberRelatedInfoType());
		bem.getMember().add(member);
		MemberType member2 = new MemberType();
		member2.setMemberInformation(new MemberRelatedInfoType());
		member2.getMemberInformation().setSubscriberIndicator(BooleanIndicatorSimpleType.N);
		bem.getMember().add(member2);
		bem.getMember().add(new MemberType());

		List<MemberType> actualList = BEMDataUtil.getSubscriberOccurrances(bem);
		Assert.assertEquals("Subscriber occurences", expectedSize, actualList.size());
	}

	@Test
	public void test_getSubscriberOccurrances_bem_null() {

		int expectedSize = 0;
		BenefitEnrollmentMaintenanceType bem = null;
		List<MemberType> actualList = BEMDataUtil.getSubscriberOccurrances(bem);
		Assert.assertEquals("Subscriber occurences", expectedSize, actualList.size());
	}

	@Test
	public void test_getSubscriberOccurrances_None() {

		int expectedSize = 0;
		BenefitEnrollmentMaintenanceType bem = new BenefitEnrollmentMaintenanceType();

		bem.getMember().add(new MemberType());
		MemberType member = new MemberType();
		member.setMemberInformation(new MemberRelatedInfoType());
		bem.getMember().add(member);
		MemberType member2 = new MemberType();
		member2.setMemberInformation(new MemberRelatedInfoType());
		member2.getMemberInformation().setSubscriberIndicator(BooleanIndicatorSimpleType.N);
		bem.getMember().add(member2);
		bem.getMember().add(new MemberType());

		List<MemberType> actualList = BEMDataUtil.getSubscriberOccurrances(bem);
		Assert.assertEquals("Subscriber occurences", expectedSize, actualList.size());
	}

	@Test
	public void test_getFinancialElements_empty() {

		AdditionalInfoType expected = null;
		MemberType subscriber = new MemberType();
		DateTime esd = JAN_1;
		DateTime eed = null;
		String txt = "VA0";
		subscriber.getAdditionalInfo().add(makeAdditionalInfoType(RA, esd, eed, txt));
		AdditionalInfoType actual = BEMDataUtil.getFinancialElements(subscriber.getAdditionalInfo());
		Assert.assertEquals("getStateCdFromSourceExchangeId", expected, actual);
	}
	
	@Test
	public void test_getFinancialElements_Null() {

		AdditionalInfoType expected = null;
		AdditionalInfoType actual = BEMDataUtil.getFinancialElements(null);
		Assert.assertEquals("getFinancialElement", expected, actual);
	}

	@Test
	public void test_getFinancialElements() {

		BigDecimal expectedAmount = new BigDecimal("1.11");
		DateTime esd = JAN_1;
		DateTime eed = null;

		AdditionalInfoType expected = makeAdditionalInfoType(TPA, esd, eed, expectedAmount);

		MemberType subscriber = new MemberType();

		subscriber.getAdditionalInfo().add(expected);
		AdditionalInfoType actual = BEMDataUtil.getFinancialElements(subscriber.getAdditionalInfo());
		Assert.assertEquals("getFinancialElements TPA", expectedAmount, actual.getTotalPremiumAmount());
	}


	@Test
	public void test_getPolicyEndDate_null() {
		DateTime expected = null;
		BenefitEnrollmentMaintenanceType bem = null;
		DateTime actual = BEMDataUtil.getPolicyEndDate(bem);
		Assert.assertEquals("getPolicyEndDate null", expected, actual);
	}

	@Test
	public void test_getPolicyEndDate_empty() {
		DateTime expected = null;
		BenefitEnrollmentMaintenanceType bem = new BenefitEnrollmentMaintenanceType();
		DateTime actual = BEMDataUtil.getPolicyEndDate(bem);
		Assert.assertEquals("getPolicyEndDate null", expected, actual);
	}
	
	/**
	 * Coverage Only
	 */
	@Test
	public void test_BEMDataUtil(){
		BEMDataUtil bemDataUtil = new BEMDataUtil();
		Assert.assertNotNull("bemDataUtil", bemDataUtil);
	}

	@Test
	public void test_getPolicyEndDate_empty_dates() {
		DateTime expected = null;
		BenefitEnrollmentMaintenanceType bem = new BenefitEnrollmentMaintenanceType();
		bem.setPolicyInfo(new PolicyInfoType());
		bem.getPolicyInfo().setPolicyEndDate(getXMLGregorianCalendar(expected));
		DateTime actual = BEMDataUtil.getPolicyEndDate(bem);
		Assert.assertEquals("getPolicyEndDate null", expected, actual);
	}

	@Test
	public void test_getPolicyEndDate() {
		DateTime expected = JAN_15;
		BenefitEnrollmentMaintenanceType bem = new BenefitEnrollmentMaintenanceType();
		bem.setPolicyInfo(new PolicyInfoType());
		bem.getPolicyInfo().setPolicyEndDate(getXMLGregorianCalendar(expected));
		DateTime actual = BEMDataUtil.getPolicyEndDate(bem);
		Assert.assertEquals("getPolicyEndDate null", expected, actual);
	}

	@Test
	public void test_isKeyFinancialElementPresent() {
		boolean expected = true;
		DateTime esd = JAN_1;
		DateTime eed = null;
		BigDecimal anyAmt = new BigDecimal(".99");
		MemberType member = new MemberType();
		member.getAdditionalInfo().add(makeAdditionalInfoType(PA1, esd, eed, anyAmt));
		member.getAdditionalInfo().add(makeAdditionalInfoType(TPA, esd, eed, anyAmt));
		boolean actual = BEMDataUtil.isKeyFinancialElementPresent(member);
		Assert.assertEquals("isKeyFinancialElementPresent", expected, actual);
	}

	@Test
	public void test_isKeyFinancialElementPresent_False() {
		boolean expected = false;
		DateTime esd = JAN_1;
		DateTime eed = null;
		BigDecimal anyAmt = new BigDecimal(".99");
		MemberType member = new MemberType();
		member.getAdditionalInfo().add(makeAdditionalInfoType(PA1, esd, eed, anyAmt));
		boolean actual = BEMDataUtil.isKeyFinancialElementPresent(member);
		Assert.assertEquals("isKeyFinancialElementPresent_False", expected, actual);
	}

	@Test
	public void test_isKeyFinancialElementPresent_Member_null() {
		boolean expected = false;
		MemberType member = null;
		boolean actual = BEMDataUtil.isKeyFinancialElementPresent(member);
		Assert.assertEquals("isKeyFinancialElementPresent_Member_null", expected, actual);
	}

	@Test
	public void test_isKeyFinancialElementPresent_Member_empty() {
		boolean expected = false;
		MemberType member = new MemberType();
		boolean actual = BEMDataUtil.isKeyFinancialElementPresent(member);
		Assert.assertEquals("isKeyFinancialElementPresent_Member_empty", expected, actual);
	}


	@Test
	public void test_getSubscriberID() {
		String expected = "1111";
		MemberType member = new MemberType();
		member.setSubscriberID(expected);
		String actual = BEMDataUtil.getSubscriberID(member);
		Assert.assertEquals("getSubscriberID", expected, actual);
	}

	@Test
	public void test_getSubscriberID_Member_null() {
		String expected = null;
		MemberType member = null;
		String actual = BEMDataUtil.getSubscriberID(member);
		Assert.assertEquals("getSubscriberID member null", expected, actual);
	}

	@Test
	public void test_getSubscriberID_Member_empty() {
		String expected = null;
		MemberType member = new MemberType();
		String actual = BEMDataUtil.getSubscriberID(member);
		Assert.assertEquals("getSubscriberID member empty", expected, actual);
	}

	@Test
	public void test_getSubscriberID_EmptyString() {
		String expected = null;
		MemberType member = new MemberType();
		member.setSubscriberID("     ");
		String actual = BEMDataUtil.getSubscriberID(member);
		Assert.assertEquals("getSubscriberID", expected, actual);
	}


	@Test
	public void test_getPolicyStatus() {
		
		PolicyStatus expected = PolicyStatus.INITIAL_1;
		BenefitEnrollmentMaintenanceType bem = new BenefitEnrollmentMaintenanceType();
		bem.setPolicyInfo(new PolicyInfoType());
		bem.getPolicyInfo().setPolicyStatus(expected.getValue());
		PolicyStatus actual = BEMDataUtil.getPolicyStatus(bem);
		assertEquals("PolicyStatus", expected, actual);
	}
	
	@Test
	public void test_getPolicyStatus_Null_BEM() {
		
		PolicyStatus expected = null;
		BenefitEnrollmentMaintenanceType bem = null;		
		PolicyStatus actual = BEMDataUtil.getPolicyStatus(bem);
		assertEquals("PolicyStatus", expected, actual);
	}
	
	@Test
	public void test_getPolicyStatusNull_Status() {
		
		PolicyStatus expected = null;
		BenefitEnrollmentMaintenanceType bem = new BenefitEnrollmentMaintenanceType();
		PolicyStatus actual = BEMDataUtil.getPolicyStatus(bem);
		assertEquals("PolicyStatus", expected, actual);
	}
	
	@Test
	public void test_getPolicyStatus_Null() {
		
		BenefitEnrollmentMaintenanceType bem = new BenefitEnrollmentMaintenanceType();
		bem.setPolicyInfo(new PolicyInfoType());

		try {
			BEMDataUtil.getPolicyStatus(bem);
		} catch(ApplicationException ae) {
			assertEquals("Application exception", "EPROD-36", ae.getInformationCode());
		}
		
	}
	
	@Test
	public void test_getPolicyStatus_BemDTO() {
		
		PolicyStatus expected = PolicyStatus.EFFECTUATED_2;
		BenefitEnrollmentMaintenanceDTO bemDTO = new BenefitEnrollmentMaintenanceDTO();
		BenefitEnrollmentMaintenanceType bem = new BenefitEnrollmentMaintenanceType();
		bem.setPolicyInfo(new PolicyInfoType());
		bem.getPolicyInfo().setPolicyStatus(expected.getValue());
		bemDTO.setBem(bem);
		PolicyStatus actual = BEMDataUtil.getPolicyStatus(bemDTO);
		assertEquals("PolicyStatus", expected, actual);
	}
	
	@Test
	public void test_getCurrentTimeStamp() {
		
		XMLGregorianCalendar expected = getXMLGregorianCalendar(new DateTime()); 
		BenefitEnrollmentMaintenanceType bem = new BenefitEnrollmentMaintenanceType();
		bem.setTransactionInformation(new TransactionInformationType());
		bem.getTransactionInformation().setCurrentTimeStamp(expected);
		XMLGregorianCalendar actual = BEMDataUtil.getCurrentTimeStamp(bem);
		assertEquals("CurrentTimeStamp", expected, actual);
	}
	
	@Test
	public void test_getCurrentTimeStamp_Null_BEM() {
		
		XMLGregorianCalendar expected = null;
		BenefitEnrollmentMaintenanceType bem = null;
		XMLGregorianCalendar actual = BEMDataUtil.getCurrentTimeStamp(bem);
		assertEquals("CurrentTimeStamp", expected, actual);
	}
	
	@Test
	public void test_getCurrentTimeStamp_Null_TransInfo() {
		
		XMLGregorianCalendar expected = null;
		BenefitEnrollmentMaintenanceType bem = new BenefitEnrollmentMaintenanceType();
		XMLGregorianCalendar actual = BEMDataUtil.getCurrentTimeStamp(bem);
		assertEquals("CurrentTimeStamp", expected, actual);
	}
	
	@Test
	public void test_MarketplaceGroupPolicyIdentifier_null() {
		String expected = null;
		BenefitEnrollmentMaintenanceType bem = null;
		String actual = BEMDataUtil.getMarketplaceGroupPolicyIdentifier(bem);
		Assert.assertEquals("getPolicyStartDate null", expected, actual);
	}

	@Test
	public void test_MarketplaceGroupPolicyIdentifier_empty() {
		String expected = null;
		BenefitEnrollmentMaintenanceType bem = new BenefitEnrollmentMaintenanceType();
		String actual = BEMDataUtil.getMarketplaceGroupPolicyIdentifier(bem);
		Assert.assertEquals("getPolicyStartDate null", expected, actual);
	}

	@Test
	public void test_MarketplaceGroupPolicyIdentifier_null_Id() {
		String expected = null;
		BenefitEnrollmentMaintenanceType bem = new BenefitEnrollmentMaintenanceType();
		bem.setPolicyInfo(new PolicyInfoType());
		String actual = BEMDataUtil.getMarketplaceGroupPolicyIdentifier(bem);
		Assert.assertEquals("getPolicyStartDate null", expected, actual);
	}

	@Test
	public void test_MarketplaceGroupPolicyIdentifier() {
		String expected = "MGPID-8888888";
		BenefitEnrollmentMaintenanceType bem = new BenefitEnrollmentMaintenanceType();
		bem.setPolicyInfo(new PolicyInfoType());
		bem.getPolicyInfo().setMarketplaceGroupPolicyIdentifier(expected);
		String actual = BEMDataUtil.getMarketplaceGroupPolicyIdentifier(bem);
		Assert.assertEquals("getPolicyStartDate null", expected, actual);
	}
	
	@Test
	public void test_getSubscriberStateCode_ContractCode_LT_7() {
		
		String expected = null;
		MemberType subscriber = new MemberType();
		subscriber.setMemberInformation(new MemberRelatedInfoType());
		subscriber.getMemberInformation().setSubscriberIndicator(BooleanIndicatorSimpleType.Y);
		subscriber.getHealthCoverage().add(new HealthCoverageType());
		subscriber.getHealthCoverage().get(0).setHealthCoveragePolicyNumber(new HealthCoveragePolicyNumberType());
		subscriber.getHealthCoverage().get(0).getHealthCoveragePolicyNumber().setContractCode("123456");
		String actual = BEMDataUtil.getSubscriberStateCode(subscriber);
		assertEquals("SubscriberStateCode", expected, actual);
	}
	
	@Test
	public void test_getSubscriberStateCode() {
		
		String contractCd = "11111TN000001";
		String expected = "TN";
		MemberType subscriber = new MemberType();
		subscriber.setMemberInformation(new MemberRelatedInfoType());
		subscriber.getMemberInformation().setSubscriberIndicator(BooleanIndicatorSimpleType.Y);
		subscriber.getHealthCoverage().add(new HealthCoverageType());
		subscriber.getHealthCoverage().get(0).setHealthCoveragePolicyNumber(new HealthCoveragePolicyNumberType());
		subscriber.getHealthCoverage().get(0).getHealthCoveragePolicyNumber().setContractCode(contractCd);
		String actual = BEMDataUtil.getSubscriberStateCode(subscriber);
		assertEquals("SubscriberStateCode", expected, actual);
	}
	
	@Test
	public void test_getSubscriberStateCode_PlanId_null() {
		
		String expected = null;
		MemberType subscriber = makeSubscriber();
		String actual = BEMDataUtil.getSubscriberStateCode(subscriber);
		assertEquals("SubscriberStateCode", expected, actual);
	}
	
	@Test
	public void test_getSubscriberStateCode_emptyMember() {
		
		String expected = null;
		MemberType subscriber = new MemberType();
		String actual = BEMDataUtil.getSubscriberStateCode(subscriber);
		assertEquals("SubscriberStateCode", expected, actual);
	}
	
	@Test
	public void test_getSubscriberStateCode_nonSubscriber() {
		
		String expected = null;
		MemberType member = new MemberType();
		member.setMemberInformation(new MemberRelatedInfoType());
		member.getMemberInformation().setSubscriberIndicator(BooleanIndicatorSimpleType.N);
		String actual = BEMDataUtil.getSubscriberStateCode(member);
		assertEquals("SubscriberStateCode", expected, actual);
	}

	
	@Test
	public void test_getIssuerSubscriberID_subscriber_null() {
		
		String expected = null;
		MemberType subscriber = null;
		String actual = BEMDataUtil.getIssuerSubscriberID(subscriber);
		assertEquals("IssuerSubscriberID", expected, actual);
	}
	
	@Test
	public void test_getIssuerSubscriberID_subscriber_empty() {
		
		String expected = null;
		MemberType subscriber = makeSubscriber();
		String actual = BEMDataUtil.getIssuerSubscriberID(subscriber);
		assertEquals("IssuerSubscriberID", expected, actual);
	}
	
	@Test
	public void test_getIssuerSubscriberID_memAddlInfo_empty() {
		
		String expected = null;
		MemberType subscriber = makeSubscriber();
		subscriber.setMemberAdditionalIdentifier(new MemberAdditionalIdentifierType());
		String actual = BEMDataUtil.getIssuerSubscriberID(subscriber);
		assertEquals("IssuerSubscriberID", expected, actual);
	}
	
	@Test
	public void test_getIssuerSubscriberID() {
		
		String expected = "ISS_SUB_ID-1234";
		MemberType subscriber = makeSubscriber();
		subscriber.setMemberAdditionalIdentifier(new MemberAdditionalIdentifierType());
		subscriber.getMemberAdditionalIdentifier().setIssuerAssignedSubscriberID(expected);
		String actual = BEMDataUtil.getIssuerSubscriberID(subscriber);
		assertEquals("IssuerSubscriberID", expected, actual);
	}
	
	
	
	
	@Test
	public void test_getExchangePolicyID_bem_null() {
		
		String expected = null;
		BenefitEnrollmentMaintenanceType bem = null;
		String actual = BEMDataUtil.getExchangePolicyID(bem);
		assertEquals("ExchangePolicyID", expected, actual);
	}
	
	@Test
	public void test_getExchangePolicyID_bem_empty() {
		
		String expected = null;
		BenefitEnrollmentMaintenanceType bem = new BenefitEnrollmentMaintenanceType();
		String actual = BEMDataUtil.getExchangePolicyID(bem);
		assertEquals("ExchangePolicyID", expected, actual);
	}
	
	@Test
	public void test_getExchangePolicyID_policyInfo_empty() {
		
		String expected = null;
		BenefitEnrollmentMaintenanceType bem = new BenefitEnrollmentMaintenanceType();
		bem.setPolicyInfo(new PolicyInfoType());
		String actual = BEMDataUtil.getExchangePolicyID(bem);
		assertEquals("ExchangePolicyID", expected, actual);
	}
	
	@Test
	public void test_getExchangePolicyID_policyInfo() {
		
		String expected = "GPN-1234";
		BenefitEnrollmentMaintenanceType bem = new BenefitEnrollmentMaintenanceType();
		bem.setPolicyInfo(new PolicyInfoType());
		bem.getPolicyInfo().setGroupPolicyNumber(expected);
		String actual = BEMDataUtil.getExchangePolicyID(bem);
		assertEquals("ExchangePolicyID", expected, actual);
	}
	
	
	@Test
	public void test_getIssuerHIOSID_ContractCode_null() {
	
		String expected = null;
		MemberType subscriber = makeSubscriber();
		subscriber.getHealthCoverage().add(new HealthCoverageType());
		subscriber.getHealthCoverage().get(0).setHealthCoveragePolicyNumber(new HealthCoveragePolicyNumberType());
		String actual = BEMDataUtil.getIssuerHIOSID(subscriber);
		assertEquals("IssuerHIOSID", expected, actual);
	}
	
	@Test
	public void test_getIssuerHIOSID_ContractCode_LT_5() {
		
		String expected = null;
		MemberType subscriber = makeSubscriber();
		subscriber.getHealthCoverage().add(new HealthCoverageType());
		subscriber.getHealthCoverage().get(0).setHealthCoveragePolicyNumber(new HealthCoveragePolicyNumberType());
		subscriber.getHealthCoverage().get(0).getHealthCoveragePolicyNumber().setContractCode("1234");
		String actual = BEMDataUtil.getIssuerHIOSID(subscriber);
		assertEquals("IssuerHIOSID", expected, actual);
	}
	
	@Test
	public void test_getIssuerHIOSID_ContractCode() {
		
		String contractCd = "123456789";
		String expected = contractCd.substring(0, 5);
		MemberType subscriber = makeSubscriber();
		subscriber.getHealthCoverage().add(new HealthCoverageType());
		subscriber.getHealthCoverage().get(0).setHealthCoveragePolicyNumber(new HealthCoveragePolicyNumberType());
		subscriber.getHealthCoverage().get(0).getHealthCoveragePolicyNumber().setContractCode(contractCd);
		String actual = BEMDataUtil.getIssuerHIOSID(subscriber);
		assertEquals("IssuerHIOSID", expected, actual);
	}

	@Test
	public void test_getHealthCoverageType_member_null() {
		
		HealthCoverageType expected = null;
		MemberType member = null;
		HealthCoverageType actual = BEMDataUtil.getHealthCoverageType(member);
		assertEquals("HealthCoverageType", expected, actual);
	}
	
	@Test
	public void test_getInternalControlNumber_subscriber_null() {
		
		String expected = null;
		MemberType subscriber = null;
		String actual = BEMDataUtil.getInternalControlNumber(subscriber);
		assertEquals("InternalControlNumber (EPS IssuerPolicyID)", expected, actual);
	}
	
	@Test
	public void test_getInternalControlNumber_subscriber_empty() {
		
		String expected = null;
		MemberType subscriber = makeSubscriber();
		String actual = BEMDataUtil.getInternalControlNumber(subscriber);
		assertEquals("InternalControlNumber (EPS IssuerPolicyID)", expected, actual);
	}
	
	@Test
	public void test_getInternalControlNumber_subscriber_HC() {
		
		String expected = null;
		MemberType subscriber = makeSubscriber();
		subscriber.getHealthCoverage().add(new HealthCoverageType());
		String actual = BEMDataUtil.getInternalControlNumber(subscriber);
		assertEquals("InternalControlNumber (EPS IssuerPolicyID)", expected, actual);
	}
	
	@Test
	public void test_getInternalControlNumber() {
		
		String expected = "ICN-0001";
		MemberType subscriber = makeSubscriber();
		subscriber.getHealthCoverage().add(new HealthCoverageType());
		subscriber.getHealthCoverage().get(0).setHealthCoveragePolicyNumber(new HealthCoveragePolicyNumberType());
		subscriber.getHealthCoverage().get(0).getHealthCoveragePolicyNumber().setInternalControlNumber(expected);
		String actual = BEMDataUtil.getInternalControlNumber(subscriber);
		assertEquals("InternalControlNumber (EPS IssuerPolicyID)", expected, actual);
	}
	
	
	@Test
	public void test_getPlanID_null_member() {
		
		String expected = null;
		MemberType subscriber = null;
		String actual = BEMDataUtil.getPlanID(subscriber);
		assertEquals("EPS PlanId", expected, actual);
	}
	
	@Test
	public void test_getPlanID_empty_member() {
		
		String expected = null;
		MemberType subscriber = new MemberType();
		String actual = BEMDataUtil.getPlanID(subscriber);
		assertEquals("EPS PlanId", expected, actual);
	}
	
	@Test
	public void test_getPlanID_empty_HC() {
		
		String expected = null;
		MemberType subscriber = new MemberType();
		subscriber.getHealthCoverage().add(new HealthCoverageType());
		subscriber.getHealthCoverage().get(0).setHealthCoveragePolicyNumber(new HealthCoveragePolicyNumberType());
		String actual = BEMDataUtil.getPlanID(subscriber);
		assertEquals("EPS PlanId", expected, actual);
	}
	
	@Test
	public void test_getPlanID_ContractCd_LT_14() {
		
		String contractCd = "1234567890123";
		String expected = null;
		MemberType subscriber = new MemberType();
		subscriber.getHealthCoverage().add(new HealthCoverageType());
		subscriber.getHealthCoverage().get(0).setHealthCoveragePolicyNumber(new HealthCoveragePolicyNumberType());
		subscriber.getHealthCoverage().get(0).getHealthCoveragePolicyNumber().setContractCode(contractCd);
		String actual = BEMDataUtil.getPlanID(subscriber);
		assertEquals("EPS PlanId", expected, actual);
	}
	
	@Test
	public void test_getPlanID() {
		
		String contractCd = "1234567890123456";
		String expected = "12345678901234";
		MemberType subscriber = new MemberType();
		subscriber.getHealthCoverage().add(new HealthCoverageType());
		subscriber.getHealthCoverage().get(0).setHealthCoveragePolicyNumber(new HealthCoveragePolicyNumberType());
		subscriber.getHealthCoverage().get(0).getHealthCoveragePolicyNumber().setContractCode(contractCd);
		String actual = BEMDataUtil.getPlanID(subscriber);
		assertEquals("EPS PlanId", expected, actual);
	}
	
	
	
	/*
	 * public static String getPlanID(MemberType subscriber) {

		String planID = null;

		String contractCode = BEMDataUtil.getContractCode(subscriber);

		if (StringUtils.isNotBlank(contractCode) && contractCode.length() > 14) {
			planID = contractCode.substring(0, 14);
		}

		return planID;
	}
	 */
	
	
	
	
	

	

	
}
