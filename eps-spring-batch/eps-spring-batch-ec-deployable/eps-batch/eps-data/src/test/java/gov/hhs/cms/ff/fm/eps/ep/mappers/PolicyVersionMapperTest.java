package gov.hhs.cms.ff.fm.eps.ep.mappers;

import gov.cms.dsh.bem.BenefitEnrollmentMaintenanceType;
import gov.cms.dsh.bem.BooleanIndicatorSimpleType;
import gov.cms.dsh.bem.ExchangeCodeSimpleType;
import gov.cms.dsh.bem.HealthCoverageDatesType;
import gov.cms.dsh.bem.HealthCoverageInfoType;
import gov.cms.dsh.bem.HealthCoveragePolicyNumberType;
import gov.cms.dsh.bem.HealthCoverageType;
import gov.cms.dsh.bem.IssuerType;
import gov.cms.dsh.bem.MemberRelatedDatesType;
import gov.cms.dsh.bem.MemberRelatedInfoType;
import gov.cms.dsh.bem.MemberType;
import gov.cms.dsh.bem.PolicyInfoType;
import gov.cms.dsh.bem.TransactionInformationType;
import gov.hhs.cms.ff.fm.eps.ep.BEMDataUtil;
import gov.hhs.cms.ff.fm.eps.ep.data.util.TestDataUtil;
import gov.hhs.cms.ff.fm.eps.ep.enums.InsuranceApplicationType;
import gov.hhs.cms.ff.fm.eps.ep.enums.PolicyStatus;
import gov.hhs.cms.ff.fm.eps.ep.po.PolicyVersionPO;
import gov.hhs.cms.ff.fm.eps.ep.util.DateTimeUtil;

import java.time.LocalDate;

import org.junit.Test;
import org.springframework.test.util.ReflectionTestUtils;

import com.accenture.foundation.common.exception.ApplicationException;

public class PolicyVersionMapperTest extends BaseMapperTest {


	private PolicyVersionMapper mapper = new PolicyVersionMapper();
	/*
	 * Test mapping VO to PO setting one subscriber member
	 */

	@Test
	public void test_mapFFMToEPS() {

		Long bemId = TestDataUtil.getRandomNumber(8);
		Long memId = TestDataUtil.getRandomNumber(8);
		String name = "ZOE";

		String expectedMGPI = "MGPI";
		LocalDate expectedPSD = JAN_1;
		LocalDate expectedPED = JUN_30;
		String expectedExchangePolicyId = TestDataUtil.getRandomNumberAsString(9);
		BenefitEnrollmentMaintenanceType expectedBem = TestDataUtil.makeBenefitEnrollmentMaintenanceType(bemId, expectedMGPI, expectedPSD, expectedPED, PolicyStatus.EFFECTUATED_2, expectedExchangePolicyId);
		
		expectedBem.getTransactionInformation().setPolicySnapshotVersionNumber(TestDataUtil.getRandomNumberAsString(6));
		expectedBem.getTransactionInformation().setPolicySnapshotDateTime(DateTimeUtil.getXMLGregorianCalendar(JUN_1_1am));
		
		String expectedHiosId = "22222";
		String expectedState = "VA";

		// Add subscriber member
		MemberType expectedMember = TestDataUtil.makeMemberTypeSubscriber(memId, name, expectedHiosId, expectedState);
		expectedMember.getMemberInformation().setSubscriberIndicator(BooleanIndicatorSimpleType.Y);
		expectedBem.getMember().add(expectedMember);

		PolicyVersionPO po = mapper.mapFFMToEPS(expectedBem);

		assertNotNull("PolicyVersionPO", po);
		
		assertEquals("policyStartDate", expectedPSD, po.getPolicyStartDate());
		assertEquals("policyEndDate", expectedPED, po.getPolicyEndDate());

		assertEquals("MarketplaceGroupPolicyId", expectedMGPI, po.getMarketplaceGroupPolicyId());

		assertNotNull("maintenanceStartDateTime", po.getMaintenanceStartDateTime());
		assertEquals("maintenanceEndDateTime",DateTimeUtil.HIGHDATE, po.getMaintenanceEndDateTime());

		assertEquals("subscriberStateCd", expectedState , po.getSubscriberStateCd());
		assertEquals("issuerPolicyId", BEMDataUtil.getInternalControlNumber(expectedMember), po.getIssuerPolicyId());
		assertEquals("issuerHiosId", BEMDataUtil.getIssuerHIOSID(expectedMember), po.getIssuerHiosId());
		assertEquals("issuerSubscriberID", BEMDataUtil.getIssuerSubscriberID(expectedMember), po.getIssuerSubscriberID());
		assertEquals("exchangePolicyId", expectedExchangePolicyId, po.getExchangePolicyId());
		assertEquals("exchangeAssignedSubscriberID", expectedMember.getSubscriberID(), po.getExchangeAssignedSubscriberID());
		assertEquals("TransDateTime", expectedBem.getTransactionInformation().getCurrentTimeStamp(), DateTimeUtil.getXMLGregorianCalendar(po.getTransDateTime()));
		assertEquals("TransControlNum", expectedBem.getTransactionInformation().getControlNumber(), po.getTransControlNum());
		assertEquals("EligibilityStartDate", expectedMember.getMemberRelatedDates().getEligibilityBeginDate(), DateTimeUtil.getXMLGregorianCalendar(po.getEligibilityStartDate()));
		assertEquals("EligibilityEndDate", expectedMember.getMemberRelatedDates().getEligibilityEndDate(), DateTimeUtil.getXMLGregorianCalendar(po.getEligibilityEndDate()));
		
		HealthCoverageDatesType hcDates = BEMDataUtil.getHealthCoverageDatesType(expectedMember);

		assertEquals("premiumPaidToEndDate", hcDates.getPremiumPaidToDateEnd(), DateTimeUtil.getXMLGregorianCalendar(po.getPremiumPaidToEndDate()));
		assertEquals("lastPremiumPaidDate", hcDates.getLastPremiumPaidDate(), DateTimeUtil.getXMLGregorianCalendar(po.getLastPremiumPaidDate()));
		
		assertEquals("planID", BEMDataUtil.getPlanID(expectedMember), po.getPlanID());

		HealthCoverageType hcType = BEMDataUtil.getHealthCoverageType(expectedMember);

		assertEquals("x12InsrncLineTypeCd", hcType.getHealthCoverageInformation().getInsuranceLineCode().value(), po.getX12InsrncLineTypeCd());
		assertEquals("insrncAplctnTypeCd", expectedBem.getTransactionInformation().getExchangeCode().value().toUpperCase(),
				InsuranceApplicationType.getEnum(po.getInsrncAplctnTypeCd()).toString());
		
		assertEquals("issuerNm", expectedBem.getIssuer().getName(), po.getIssuerNm());
		assertEquals("issuerTaxPayerId", expectedBem.getIssuer().getTaxPayerIdentificationNumber(), po.getIssuerTaxPayerId());
	
		assertEquals("PolicySnapshotVersionNumber", expectedBem.getTransactionInformation().getPolicySnapshotVersionNumber(),
				po.getSourceVersionId().toString());
		assertEquals("PolicySnapshotDateTime", expectedBem.getTransactionInformation().getPolicySnapshotDateTime(),
				DateTimeUtil.getXMLGregorianCalendar(po.getSourceVersionDateTime()));

	}

	@Test
	public void test_mapFFMToEPS_Exception() {

		String expectedError = "EPROD";
		String actualError = null;
		BenefitEnrollmentMaintenanceType expectedBem = new BenefitEnrollmentMaintenanceType();
		expectedBem.setTransactionInformation(new TransactionInformationType());
		expectedBem.getTransactionInformation().setPolicySnapshotVersionNumber(TestDataUtil.getRandomNumberAsString(6) + "xxx");
		expectedBem.getTransactionInformation().setPolicySnapshotDateTime(DateTimeUtil.getXMLGregorianCalendar(FEB_1));		

		try {
			mapper.mapFFMToEPS(expectedBem);
		} catch (ApplicationException appEx) {
			assertTrue("ApplicationException thrown for NumberFormatException from PolicySnapshotVersionNumber", true);
			actualError = appEx.getMessage().substring(0, expectedError.length());
		}
		assertEquals("Error", expectedError, actualError);
	}

	@Test
	public void test_mapFFMToEPS_NullBem() {
		BenefitEnrollmentMaintenanceType bem = null;
		PolicyVersionPO po = mapper.mapFFMToEPS(bem);
		assertNotNull("PolicyVersionPO", po);
		assertAllAttributesNull(po);
	}
	
	@Test
	public void test_mapFFMToEPS_EmptyPolicyInfo() {

		BenefitEnrollmentMaintenanceType bem = new BenefitEnrollmentMaintenanceType();
		bem.setPolicyInfo(new PolicyInfoType());
		PolicyVersionPO po = mapper.mapFFMToEPS(bem);
		assertNotNull("PolicyVersionPO", po);
		assertAllAttributesNull(po);
		assertNotNull("maintenanceStartDateTime", po.getMaintenanceStartDateTime());
		assertEquals("maintenanceEndDateTime",DateTimeUtil.HIGHDATE, po.getMaintenanceEndDateTime());
	}

	@Test
	public void test_mapFFMToEPS_EmptyTransactionInfoType() {

		BenefitEnrollmentMaintenanceType bem = new BenefitEnrollmentMaintenanceType();
		bem.setTransactionInformation(new TransactionInformationType());
		PolicyVersionPO po = mapper.mapFFMToEPS(bem);
		assertNotNull("PolicyVersionPO", po);
		assertAllAttributesNull(po);
		assertNotNull("maintenanceStartDateTime", po.getMaintenanceStartDateTime());
		assertEquals("maintenanceEndDateTime",DateTimeUtil.HIGHDATE, po.getMaintenanceEndDateTime());
	}

	@Test
	public void test_mapFFMToEPS_EmptyIssuer() {

		BenefitEnrollmentMaintenanceType bem = new BenefitEnrollmentMaintenanceType();
		bem.setIssuer(new IssuerType());
		PolicyVersionPO po = mapper.mapFFMToEPS(bem);
		assertNotNull("PolicyVersionPO", po);
		assertAllAttributesNull(po);
		assertNotNull("maintenanceStartDateTime", po.getMaintenanceStartDateTime());
		assertEquals("maintenanceEndDateTime",DateTimeUtil.HIGHDATE, po.getMaintenanceEndDateTime());
	}

	@Test
	public void test_mapFFMToEPS_EmptySubscriber() {

		BenefitEnrollmentMaintenanceType bem= new BenefitEnrollmentMaintenanceType();
		bem.getMember().add(new MemberType());
		bem.getMember().get(0).setMemberInformation(new MemberRelatedInfoType());
		bem.getMember().get(0).getMemberInformation().setSubscriberIndicator(BooleanIndicatorSimpleType.Y);
		bem.getMember().get(0).setMemberRelatedDates(new MemberRelatedDatesType());
		bem.getMember().get(0).getHealthCoverage().add(new HealthCoverageType());
		bem.getMember().get(0).getHealthCoverage().get(0).setHealthCoverageDates(new HealthCoverageDatesType());
		bem.getMember().get(0).getHealthCoverage().get(0).setHealthCoverageInformation(new HealthCoverageInfoType());
		PolicyVersionPO po = mapper.mapFFMToEPS(bem);
		assertNotNull("PolicyVersionPO", po);
		assertAllAttributesNull(po);
		assertNotNull("maintenanceStartDateTime", po.getMaintenanceStartDateTime());
		assertEquals("maintenanceEndDateTime",DateTimeUtil.HIGHDATE, po.getMaintenanceEndDateTime());
	}


	public void testMapPOToVO() {

		String expectedIssuerHiosId = "HIOS-737";
		PolicyVersionPO po = new PolicyVersionPO();
		po.setIssuerHiosId(expectedIssuerHiosId);
		
		BenefitEnrollmentMaintenanceType bemLatest = new BenefitEnrollmentMaintenanceType();

		BenefitEnrollmentMaintenanceType actualBem = mapper.mapPOToVO(bemLatest, po);
		
		assertEquals("IssuerHiosID", expectedIssuerHiosId, actualBem.getIssuer().getHIOSID());
	}


	@Test
	public void test_setSubcriberAttributesToPo_HC_null() {
		
		PolicyVersionPO actual = new PolicyVersionPO();
		MemberType subscriber = makeSubscriber();
		
		ReflectionTestUtils.invokeMethod(mapper, "setSubcriberAttributesToPo", actual, subscriber);
		
		assertNotNull("PolicyVersionPO", actual);
		assertNull("X12InsrncLineTypeCd ", actual.getX12InsrncLineTypeCd());
		assertNull("X12CoverageLevelTypeCd ", actual.getX12CoverageLevelTypeCd());
	}
	
	@Test
	public void test_setSubcriberAttributesToPo_HC_Empty() {
		
		PolicyVersionPO actual = new PolicyVersionPO();
		MemberType subscriber = makeSubscriber();
		subscriber.getHealthCoverage().add(new HealthCoverageType());

		ReflectionTestUtils.invokeMethod(mapper, "setSubcriberAttributesToPo", actual, subscriber);
		
		assertNotNull("PolicyVersionPO", actual);
		assertNull("X12InsrncLineTypeCd ", actual.getX12InsrncLineTypeCd());
		assertNull("X12CoverageLevelTypeCd ", actual.getX12CoverageLevelTypeCd());
	}
	
	@Test
	public void test_setSubcriberAttributesToPo_HC_DatesNumbersInfo_empty() {
		
		PolicyVersionPO actual = new PolicyVersionPO();
		MemberType subscriber = makeSubscriber();
		subscriber.getHealthCoverage().add(new HealthCoverageType());
		subscriber.getHealthCoverage().get(0).setHealthCoverageDates(new HealthCoverageDatesType());
		subscriber.getHealthCoverage().get(0).setHealthCoveragePolicyNumber(new HealthCoveragePolicyNumberType());
		subscriber.getHealthCoverage().get(0).setHealthCoverageInformation(new HealthCoverageInfoType());
		
		ReflectionTestUtils.invokeMethod(mapper, "setSubcriberAttributesToPo", actual, subscriber);
		
		assertNotNull("PolicyVersionPO", actual);
		assertNull("X12InsrncLineTypeCd ", actual.getX12InsrncLineTypeCd());
		assertNull("X12CoverageLevelTypeCd ", actual.getX12CoverageLevelTypeCd());
	}
	
	@Test
	public void test_setSubcriberAttributesToPo_PlanId_LT_14() {
		
		String expectedHiosId = "11111";
		String expectedState = "NC";
		String expectedExchangePolicyId = "22";
		PolicyVersionPO actual = new PolicyVersionPO();
		MemberType subscriber = makeSubscriber();
		subscriber.getHealthCoverage().add(new HealthCoverageType());
		subscriber.getHealthCoverage().get(0).setHealthCoveragePolicyNumber(new HealthCoveragePolicyNumberType());
		subscriber.getHealthCoverage().get(0).getHealthCoveragePolicyNumber().setContractCode(expectedHiosId + expectedState + expectedExchangePolicyId);
		
		ReflectionTestUtils.invokeMethod(mapper, "setSubcriberAttributesToPo", actual, subscriber);
		
		assertNotNull("PolicyVersionPO", actual);
		assertNull("X12InsrncLineTypeCd ", actual.getX12InsrncLineTypeCd());
		assertNull("X12CoverageLevelTypeCd ", actual.getX12CoverageLevelTypeCd());
	}
	
	@Test
	public void test_getInsuranceApplicationTypeCd_TransInfo_null() {
		
		String expected = null;
		TransactionInformationType transInfo = null;
		String actual = (String) ReflectionTestUtils.invokeMethod(mapper, "getInsuranceApplicationTypeCd", transInfo);
		assertEquals("InsuranceApplicationTypeCd", expected, actual);
	}
	
	@Test
	public void test_getInsuranceApplicationTypeCd_TransInfo_empty() {
		
		String expected = null;
		TransactionInformationType transInfo = new TransactionInformationType();
		String actual = (String) ReflectionTestUtils.invokeMethod(mapper, "getInsuranceApplicationTypeCd", transInfo);
		assertEquals("InsuranceApplicationTypeCd", expected, actual);
	}
	
	@Test
	public void test_getInsuranceApplicationTypeCd_blank() {
		
		String expected = null;
		TransactionInformationType transInfo = new TransactionInformationType();
		transInfo.setExchangeCode(null);
		String actual = (String) ReflectionTestUtils.invokeMethod(mapper, "getInsuranceApplicationTypeCd", transInfo);
		assertEquals("InsuranceApplicationTypeCd", expected, actual);
	}
	
	
	@Test
	public void test_getInsuranceApplicationTypeCd_INDIVIDUAL() {
		
		String expected = InsuranceApplicationType.INDIVIDUAL.getValue();
		TransactionInformationType transInfo = new TransactionInformationType();
		transInfo.setExchangeCode(ExchangeCodeSimpleType.INDIVIDUAL);
		String actual = (String) ReflectionTestUtils.invokeMethod(mapper, "getInsuranceApplicationTypeCd", transInfo);
		assertEquals("InsuranceApplicationTypeCd", expected, actual);
	}
	
	@Test
	public void test_getInsuranceApplicationTypeCd_SHOP() {
		
		String expected = InsuranceApplicationType.SHOP.getValue();
		TransactionInformationType transInfo = new TransactionInformationType();
		transInfo.setExchangeCode(ExchangeCodeSimpleType.SHOP);
		String actual = (String) ReflectionTestUtils.invokeMethod(mapper, "getInsuranceApplicationTypeCd", transInfo);
		assertEquals("InsuranceApplicationTypeCd", expected, actual);
	}
	
	
	

	private void assertAllAttributesNull(PolicyVersionPO po) {

		assertNull("subscriberStateCd", po.getSubscriberStateCd());
		assertNull("issuerPolicyId", po.getIssuerPolicyId() );
		assertNull("issuerHiosId", po.getIssuerHiosId());
		assertNull("issuerSubscriberID", po.getIssuerSubscriberID());
		assertNull("exchangePolicyId", po.getExchangePolicyId());
		assertNull("exchangeAssignedSubscriberID", po.getExchangeAssignedSubscriberID());
		assertNull("TransDateTime", po.getTransDateTime());
		assertNull("TransControlNum", po.getTransControlNum());
		assertNull("policyStartDate", po.getPolicyStartDate());
		assertNull("policyEndDate", po.getPolicyEndDate());	
		assertNull("premiumPaidToEndDate", po.getPremiumPaidToEndDate());
		assertNull("lastPremiumPaidDate", po.getLastPremiumPaidDate());
		assertNull("EligibilityStartDate", po.getEligibilityStartDate());
		assertNull("EligibilityEndDate", po.getEligibilityEndDate());
		assertNull("planID", po.getPlanID());
		assertNull("employerGroupNum", po.getEmployerGroupNum());
		assertNull("x12InsrncLineTypeCd", po.getX12InsrncLineTypeCd());
		assertNull("insrncAplctnTypeCd", po.getInsrncAplctnTypeCd());
		assertNull("employerIdentificationNum", po.getEmployerIdentificationNum());
		assertNull("transMsgID", po.getTransMsgID());
		assertNull("issuerNm", po.getIssuerNm());
		assertNull("issuerTaxPayerId", po.getIssuerTaxPayerId());
		assertNull("changeReportedDate", po.getChangeReportedDate());
		assertNull("PolicySnapshotVersionNumber", po.getSourceVersionId());
		assertNull("PolicySnapshotDateTime", po.getSourceVersionDateTime());

	}


}
