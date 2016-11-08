package gov.hhs.cms.ff.fm.eps.ep.validation.impl;

import static org.junit.Assert.*;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.Map;

import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Test;
import org.springframework.test.util.ReflectionTestUtils;

import gov.cms.dsh.bem.AdditionalInfoType;
import gov.cms.dsh.bem.BenefitEnrollmentMaintenanceType;
import gov.cms.dsh.bem.BooleanIndicatorSimpleType;
import gov.cms.dsh.bem.ExtractionStatusType;
import gov.cms.dsh.bem.HealthCoverageDatesType;
import gov.cms.dsh.bem.HealthCoverageType;
import gov.cms.dsh.bem.MemberAdditionalIdentifierType;
import gov.cms.dsh.bem.MemberRelatedDatesType;
import gov.cms.dsh.bem.MemberRelatedInfoType;
import gov.cms.dsh.bem.MemberType;
import gov.cms.dsh.bem.PolicyInfoType;
import gov.hhs.cms.ff.fm.eps.ep.BEMDataUtil;
import gov.hhs.cms.ff.fm.eps.ep.BenefitEnrollmentMaintenanceDTO;
import gov.hhs.cms.ff.fm.eps.ep.EPSValidationRequest;
import gov.hhs.cms.ff.fm.eps.ep.enums.EProdEnum;
import gov.hhs.cms.ff.fm.eps.ep.enums.PolicyStatus;
import gov.hhs.cms.ff.fm.eps.ep.services.PolicyDataService;
import gov.hhs.cms.ff.fm.eps.ep.services.TransMsgCompositeDAO;
import gov.hhs.cms.ff.fm.eps.ep.services.impl.FFMDataServiceImpl;
import gov.hhs.cms.ff.fm.eps.ep.util.DateTimeUtil;
import gov.hhs.cms.ff.fm.eps.ep.validation.EPSValidationTestUtil;

public class EPSValidationHandlerTest {
    private EPSValidationHandler ffmValidatorService; 
    private FFMDataServiceImpl mockPolicyDataService;
    private TransMsgCompositeDAO  mockTxnMsgService;
    private FinancialValidatorImpl financialValidator;
    PolicyDataService policyDataService;
    
    private static final LocalDate DATE = LocalDate.now();
	private static final LocalDateTime DATETIME = LocalDateTime.now();
	private static final int YEAR = DATE.getYear();
		
	private final LocalDate JAN_1 = LocalDate.of(YEAR, 1, 1);
	private final LocalDate JAN_15 = LocalDate.of(YEAR, 1, 15);
	private final LocalDate JAN_31 = LocalDate.of(YEAR, 1, 31);
	private final LocalDate FEB_1 = LocalDate.of(YEAR, 2, 1);
	private final LocalDate FEB_15 = LocalDate.of(YEAR, 2, 15);
	private final LocalDate FEB_MAX = DATE.with(TemporalAdjusters.lastDayOfMonth());
	private final LocalDate MAR_1 = LocalDate.of(YEAR, 3, 1);
	private final LocalDate MAR_14 = LocalDate.of(YEAR, 3, 14);
	private final LocalDate MAR_15 = LocalDate.of(YEAR, 3, 15);
	private final LocalDate MAR_31 = LocalDate.of(YEAR, 3, 31);
	private final LocalDate APR_1 = LocalDate.of(YEAR, 4, 1);
	private final LocalDate APR_15 = LocalDate.of(YEAR, 4, 15);
	private final LocalDate APR_30 = LocalDate.of(YEAR, 4, 30);
	private final LocalDate MAY_1 = LocalDate.of(YEAR, 5, 1);
	private final LocalDate DEC_1 = LocalDate.of(YEAR, 12, 1);
	private final LocalDate DEC_15 = LocalDate.of(YEAR, 12, 15);
	private final LocalDate DEC_31 = LocalDate.of(YEAR, 12, 31);
	
    @Before
	public void setup() throws Exception {
		ffmValidatorService = new EPSValidationHandler();
		mockPolicyDataService = EasyMock.createMock(FFMDataServiceImpl.class);
		mockTxnMsgService = EasyMock.createMock(TransMsgCompositeDAO.class);
		financialValidator = new FinancialValidatorImpl();
	
	}	
	
	@Test
	public void testValidateZeroPremium() {
		
		BigDecimal aptc = new BigDecimal("100");
        BigDecimal amt = ffmValidatorService.validateZeroPremium(fillInObject(aptc));
        assertTrue("amount greater zero ",amt.compareTo(BigDecimal.ZERO)>0);
        try{
        aptc = new BigDecimal("0");
        ffmValidatorService.validateZeroPremium(fillInObject(aptc));
        }
        catch(Exception e){
        	assertTrue("ApplicationException is here",e.getMessage().equals(EProdEnum.EPROD_37.getLogMsg()));
        }
	}

	private BenefitEnrollmentMaintenanceDTO fillInObject(BigDecimal aptc) {
		LocalDate esd = JAN_1;
		LocalDate eed = null;
		
	//	BigDecimal csr = new BigDecimal("25");
	//	BigDecimal tpa = new BigDecimal("100");
	//	BigDecimal tira = new BigDecimal("75");

		// Expected data after replacement, Record 1
		LocalDate expectedESD_EPS1 = esd;
		LocalDate expectedEED_EPS1 = eed;
		BigDecimal expectedAPTC_EPS1 = aptc;
		

		MemberType inboundSubscriber = makeSubscriberMaintenance("5555", JAN_1, null);
		
		AdditionalInfoType ait = new AdditionalInfoType();
		
		ait.setEffectiveStartDate(DateTimeUtil.getXMLGregorianCalendar(esd));
		ait.setEffectiveEndDate(DateTimeUtil.getXMLGregorianCalendar(eed));
		
		AdditionalInfoType ait1 = setAdditionalInfoTypeValue(ait, "TPA", aptc, null);
		
		
		inboundSubscriber.getAdditionalInfo().add(ait1);
		
		BenefitEnrollmentMaintenanceDTO bemDTO = new BenefitEnrollmentMaintenanceDTO();
		BenefitEnrollmentMaintenanceType bem = new BenefitEnrollmentMaintenanceType();
		bem.setPolicyInfo(makePolicyInfoType("MPGPI-5555", JAN_1, null, PolicyStatus.EFFECTUATED_2));
		bem.getMember().add(inboundSubscriber);
		bemDTO.setBem(bem);
		
		return bemDTO;
		
	}

	@Test
	public void testPerformPolicyMatch() {
		BigDecimal aptc = new BigDecimal("100");
		BenefitEnrollmentMaintenanceDTO dbBemDto = new BenefitEnrollmentMaintenanceDTO();
		EasyMock.expect(mockPolicyDataService.getLatestBEMByPolicyId(EasyMock.anyObject())).andReturn(dbBemDto);
		EasyMock.replay(mockPolicyDataService);
		BenefitEnrollmentMaintenanceDTO bfDTO= ffmValidatorService.performPolicyMatch(fillInObject(aptc), mockPolicyDataService);
		assertNotNull("BenefitEnrollmentMaintenanceDTO return type not null",bfDTO);
	}
	
	@Test
	public void testPerformPolicyMatch_NULL() {
		BigDecimal aptc = new BigDecimal("0");
		BenefitEnrollmentMaintenanceDTO dbBemDto = new BenefitEnrollmentMaintenanceDTO();
		BenefitEnrollmentMaintenanceDTO bfDTO= ffmValidatorService.performPolicyMatch(fillInObject(aptc), mockPolicyDataService);
		assertNull("BenefitEnrollmentMaintenanceDTO return type not null",bfDTO);
	}
//	BenefitEnrollmentMaintenanceDTO dbBemDto = 
	@Test
	public void validateBEM_exception_EPROD_ExtractionStatus_zero() throws Exception {

		BenefitEnrollmentMaintenanceDTO bemh = EPSValidationTestUtil.createBEMForPolicyMatch();
		bemh.getFileInformation().setGroupSenderID("GRSIDFFMTEST");

		bemh.getBem().setExtractionStatus(makeExtractionStatus(BigInteger.valueOf(0)));

		EPSValidationRequest epsValidationRequest = new EPSValidationRequest();
		epsValidationRequest.setBenefitEnrollmentMaintenance(bemh);

		ReflectionTestUtils.invokeMethod(ffmValidatorService, "validateExtractionStatus", bemh);
		assertEquals("HiosIdFromGrpSenderId", bemh, bemh);
	}
	
	@Test
	public void validateBEM_exception_EPROD_ExtractionStatus_null() throws Exception {

		BenefitEnrollmentMaintenanceDTO bemh = EPSValidationTestUtil.createBEMForPolicyMatch();
		bemh.getFileInformation().setGroupSenderID("GRSIDFFMTEST");

		EPSValidationRequest epsValidationRequest = new EPSValidationRequest();
		epsValidationRequest.setBenefitEnrollmentMaintenance(bemh);

		ReflectionTestUtils.invokeMethod(ffmValidatorService, "validateExtractionStatus", bemh);
		assertEquals("HiosIdFromGrpSenderId", bemh, bemh);
	}
	
	
	@Test
	public void test_processInboundPremiums_EED_null() {

		LocalDate esd = JAN_1;
		LocalDate eed = null;
		BigDecimal aptc = new BigDecimal("50");
		BigDecimal csr = new BigDecimal("25");
		BigDecimal tpa = new BigDecimal("100");
		BigDecimal tira = new BigDecimal("75");

		// Expected data after replacement, Record 1
		LocalDate expectedESD_EPS1 = esd;
		LocalDate expectedEED_EPS1 = eed;
		BigDecimal expectedAPTC_EPS1 = aptc;
		BigDecimal expectedCSR_EPS1 = csr;
		BigDecimal expectedTPA_EPS1 = tpa;
		BigDecimal expectedTIRA_EPS1 = tira;

		MemberType inboundSubscriber = makeSubscriberMaintenance("5555", JAN_1, null);
		
		AdditionalInfoType ait = new AdditionalInfoType();
		
		ait.setEffectiveStartDate(DateTimeUtil.getXMLGregorianCalendar(esd));
		ait.setEffectiveEndDate(DateTimeUtil.getXMLGregorianCalendar(eed));
		
		setAdditionalInfoTypeValue(ait, "TPA", tpa, null);
		setAdditionalInfoTypeValue(ait, "TIRA", tira, null);
		setAdditionalInfoTypeValue(ait, "APTC", aptc, null);
		setAdditionalInfoTypeValue(ait, "CSR", csr, null);
		
		inboundSubscriber.getAdditionalInfo().add(ait);
		
		BenefitEnrollmentMaintenanceDTO bemDTO = new BenefitEnrollmentMaintenanceDTO();
		BenefitEnrollmentMaintenanceType bem = new BenefitEnrollmentMaintenanceType();
		bem.setPolicyInfo(makePolicyInfoType("MPGPI-5555", JAN_1, null, PolicyStatus.EFFECTUATED_2));
		bem.getMember().add(inboundSubscriber);
		bemDTO.setBem(bem);

		Map<LocalDate, AdditionalInfoType> epsPremiums = ReflectionTestUtils.invokeMethod(ffmValidatorService, "processInboundPremiums", bemDTO,financialValidator);

		AdditionalInfoType actualPremium1 = epsPremiums.get(esd);

		assertEquals("ESD", expectedESD_EPS1, DateTimeUtil.getLocalDateFromXmlGC(actualPremium1.getEffectiveStartDate()));
		assertEquals("EED", expectedEED_EPS1, DateTimeUtil.getLocalDateFromXmlGC(actualPremium1.getEffectiveEndDate()));
		assertEquals("APTC", expectedAPTC_EPS1, actualPremium1.getAPTCAmount());
		assertEquals("CSR", expectedCSR_EPS1, actualPremium1.getCSRAmount());
		assertEquals("TPA", expectedTPA_EPS1, actualPremium1.getTotalPremiumAmount());
		assertEquals("TIRA", expectedTIRA_EPS1, actualPremium1.getTotalIndividualResponsibilityAmount());
	}

	/*
	 * One subscriber, with effective end date
	 */
	
	@Test
	public void test_processInboundPremiums_EED_not_null() {

		LocalDate esd = JAN_1;
		LocalDate eed = DEC_31;
		BigDecimal aptc = new BigDecimal("50");
		BigDecimal csr = new BigDecimal("25");
		BigDecimal tpa = new BigDecimal("100");
		BigDecimal tira = new BigDecimal("75");

		// Expected data after replacement, Record 1
		LocalDate expectedESD_EPS1 = esd;
		LocalDate expectedEED_EPS1 = eed;
		BigDecimal expectedAPTC_EPS1 = aptc;
		BigDecimal expectedCSR_EPS1 = csr;
		BigDecimal expectedTPA_EPS1 = tpa;
		BigDecimal expectedTIRA_EPS1 = tira;

		MemberType inboundSubscriber = makeSubscriberMaintenance("5555", JAN_1, DEC_31);
		
		AdditionalInfoType ait = new AdditionalInfoType();
		
		ait.setEffectiveStartDate(DateTimeUtil.getXMLGregorianCalendar(esd));
		ait.setEffectiveEndDate(DateTimeUtil.getXMLGregorianCalendar(eed));
		
		setAdditionalInfoTypeValue(ait, "TPA", tpa, null);
		setAdditionalInfoTypeValue(ait, "TIRA", tira, null);
		setAdditionalInfoTypeValue(ait, "APTC", aptc, null);
		setAdditionalInfoTypeValue(ait, "CSR", csr, null);
		
		inboundSubscriber.getAdditionalInfo().add(ait);
		
		BenefitEnrollmentMaintenanceDTO bemDTO = new BenefitEnrollmentMaintenanceDTO();
		BenefitEnrollmentMaintenanceType bem = new BenefitEnrollmentMaintenanceType();
		bem.setPolicyInfo(makePolicyInfoType("MPGPI-5555", JAN_1, DEC_31, PolicyStatus.EFFECTUATED_2));
		bem.getMember().add(inboundSubscriber);
		bemDTO.setBem(bem);

		Map<LocalDate, AdditionalInfoType> epsPremiums = ReflectionTestUtils.invokeMethod(ffmValidatorService, "processInboundPremiums", bemDTO,financialValidator);

		AdditionalInfoType actualPremium1 = epsPremiums.get(esd);

		assertEquals("ESD", expectedESD_EPS1, DateTimeUtil.getLocalDateFromXmlGC(actualPremium1.getEffectiveStartDate()));
		assertEquals("EED", expectedEED_EPS1, DateTimeUtil.getLocalDateFromXmlGC(actualPremium1.getEffectiveEndDate()));
		assertEquals("APTC", expectedAPTC_EPS1, actualPremium1.getAPTCAmount());
		assertEquals("CSR", expectedCSR_EPS1, actualPremium1.getCSRAmount());
		assertEquals("TPA", expectedTPA_EPS1, actualPremium1.getTotalPremiumAmount());
		assertEquals("TIRA", expectedTIRA_EPS1, actualPremium1.getTotalIndividualResponsibilityAmount());
	}

	/*
	 * One subscriber, mid month start
	 */
	
	@Test
	public void test_processInboundPremiums_EED_mid_month_start() {

		LocalDate esd = JAN_15;
		LocalDate eed = DEC_31;
		BigDecimal aptc = new BigDecimal("50");
		BigDecimal csr = new BigDecimal("25");
		BigDecimal tpa = new BigDecimal("100");
		BigDecimal tira = new BigDecimal("75");

		// Expected data after replacement, Record 1
		LocalDate expectedESD_EPS1 = esd;
		LocalDate expectedEED_EPS1 = eed;
		BigDecimal expectedAPTC_EPS1 = aptc;
		BigDecimal expectedCSR_EPS1 = csr;
		BigDecimal expectedTPA_EPS1 = tpa;
		BigDecimal expectedTIRA_EPS1 = tira;

		MemberType inboundSubscriber = makeSubscriberMaintenance("5555", JAN_15, DEC_31);
		
		AdditionalInfoType ait = new AdditionalInfoType();
		
		ait.setEffectiveStartDate(DateTimeUtil.getXMLGregorianCalendar(esd));
		ait.setEffectiveEndDate(DateTimeUtil.getXMLGregorianCalendar(eed));
		
		setAdditionalInfoTypeValue(ait, "TPA", tpa, null);
		setAdditionalInfoTypeValue(ait, "TIRA", tira, null);
		setAdditionalInfoTypeValue(ait, "APTC", aptc, null);
		setAdditionalInfoTypeValue(ait, "CSR", csr, null);
		
		inboundSubscriber.getAdditionalInfo().add(ait);
		
		BenefitEnrollmentMaintenanceDTO bemDTO = new BenefitEnrollmentMaintenanceDTO();
		BenefitEnrollmentMaintenanceType bem = new BenefitEnrollmentMaintenanceType();
		bem.setPolicyInfo(makePolicyInfoType("MPGPI-5555", JAN_15, DEC_31, PolicyStatus.EFFECTUATED_2));
		bem.getMember().add(inboundSubscriber);
		bemDTO.setBem(bem);

		Map<LocalDate, AdditionalInfoType> epsPremiums = ReflectionTestUtils.invokeMethod(ffmValidatorService, "processInboundPremiums", bemDTO,financialValidator);

		AdditionalInfoType actualPremium1 = epsPremiums.get(esd);

		assertEquals("ESD", expectedESD_EPS1, DateTimeUtil.getLocalDateFromXmlGC(actualPremium1.getEffectiveStartDate()));
		assertEquals("EED", expectedEED_EPS1, DateTimeUtil.getLocalDateFromXmlGC(actualPremium1.getEffectiveEndDate()));
		assertEquals("APTC", expectedAPTC_EPS1, actualPremium1.getAPTCAmount());
		assertEquals("CSR", expectedCSR_EPS1, actualPremium1.getCSRAmount());
		assertEquals("TPA", expectedTPA_EPS1, actualPremium1.getTotalPremiumAmount());
		assertEquals("TIRA", expectedTIRA_EPS1, actualPremium1.getTotalIndividualResponsibilityAmount());
	}

	/*
	 * One subscriber, mid month end
	 */
	@Test
	public void test_processInboundPremiums_EED_mid_month_end() {

		LocalDate esd = JAN_1;
		LocalDate eed = MAR_14;
		BigDecimal aptc = new BigDecimal("50");
		BigDecimal csr = new BigDecimal("25");
		BigDecimal tpa = new BigDecimal("100");
		BigDecimal tira = new BigDecimal("75");

		// Expected data after replacement, Record 1
		LocalDate expectedESD_EPS1 = esd;
		LocalDate expectedEED_EPS1 = eed;
		BigDecimal expectedAPTC_EPS1 = aptc;
		BigDecimal expectedCSR_EPS1 = csr;
		BigDecimal expectedTPA_EPS1 = tpa;
		BigDecimal expectedTIRA_EPS1 = tira;

		MemberType inboundSubscriber = makeSubscriberMaintenance("5555", JAN_1, MAR_14);
		
		AdditionalInfoType ait = new AdditionalInfoType();
		
		ait.setEffectiveStartDate(DateTimeUtil.getXMLGregorianCalendar(esd));
		ait.setEffectiveEndDate(DateTimeUtil.getXMLGregorianCalendar(eed));
		
		setAdditionalInfoTypeValue(ait, "TPA", tpa, null);
		setAdditionalInfoTypeValue(ait, "TIRA", tira, null);
		setAdditionalInfoTypeValue(ait, "APTC", aptc, null);
		setAdditionalInfoTypeValue(ait, "CSR", csr, null);
		
		inboundSubscriber.getAdditionalInfo().add(ait);
		
		BenefitEnrollmentMaintenanceDTO bemDTO = new BenefitEnrollmentMaintenanceDTO();
		BenefitEnrollmentMaintenanceType bem = new BenefitEnrollmentMaintenanceType();
		bem.setPolicyInfo(makePolicyInfoType("MPGPI-5555", JAN_1, MAR_14, PolicyStatus.EFFECTUATED_2));
		bem.getMember().add(inboundSubscriber);
		bemDTO.setBem(bem);

		Map<LocalDate, AdditionalInfoType> epsPremiums = ReflectionTestUtils.invokeMethod(ffmValidatorService, "processInboundPremiums", bemDTO,financialValidator);

		AdditionalInfoType actualPremium1 = epsPremiums.get(esd);

		assertEquals("ESD", expectedESD_EPS1, DateTimeUtil.getLocalDateFromXmlGC(actualPremium1.getEffectiveStartDate()));
		assertEquals("EED", expectedEED_EPS1, DateTimeUtil.getLocalDateFromXmlGC(actualPremium1.getEffectiveEndDate()));
		assertEquals("APTC", expectedAPTC_EPS1, actualPremium1.getAPTCAmount());
		assertEquals("CSR", expectedCSR_EPS1, actualPremium1.getCSRAmount());
		assertEquals("TPA", expectedTPA_EPS1, actualPremium1.getTotalPremiumAmount());
		assertEquals("TIRA", expectedTIRA_EPS1, actualPremium1.getTotalIndividualResponsibilityAmount());
	}

	/*
	 * FDD Scenario 1 - One subscriber, mid month start
	*/ 
	@Test
	public void test_processInboundPremiums_mid_month_start() {

		LocalDate esd = FEB_1;
		LocalDate eed = DEC_31;
		BigDecimal aptc = new BigDecimal("100");
		BigDecimal csr = new BigDecimal("50");
		BigDecimal tpa = new BigDecimal("150");
		BigDecimal tira = new BigDecimal("100");

		// Expected data after replacement, Record 1
		LocalDate expectedESD_EPS1 = JAN_15;
		LocalDate expectedEED_EPS1 = eed;
		BigDecimal expectedAPTC_EPS1 = aptc;
		BigDecimal expectedCSR_EPS1 = csr;
		BigDecimal expectedTPA_EPS1 = tpa;
		BigDecimal expectedTIRA_EPS1 = tira;

		MemberType inboundSubscriber = makeSubscriberMaintenance("5555", JAN_15, DEC_31);
		
		AdditionalInfoType ait = new AdditionalInfoType();
		
		ait.setEffectiveStartDate(DateTimeUtil.getXMLGregorianCalendar(esd));
		ait.setEffectiveEndDate(DateTimeUtil.getXMLGregorianCalendar(eed));
		
		setAdditionalInfoTypeValue(ait, "TPA", tpa, null);
		setAdditionalInfoTypeValue(ait, "TIRA", tira, null);
		setAdditionalInfoTypeValue(ait, "APTC", aptc, null);
		setAdditionalInfoTypeValue(ait, "CSR", csr, null);
		
		inboundSubscriber.getAdditionalInfo().add(ait);
		
		BenefitEnrollmentMaintenanceDTO bemDTO = new BenefitEnrollmentMaintenanceDTO();
		BenefitEnrollmentMaintenanceType bem = new BenefitEnrollmentMaintenanceType();
		bem.setPolicyInfo(makePolicyInfoType("MPGPI-5555", JAN_15, DEC_31, PolicyStatus.EFFECTUATED_2));
		bem.getMember().add(inboundSubscriber);
		bemDTO.setBem(bem);

		Map<LocalDate, AdditionalInfoType> epsPremiums = ReflectionTestUtils.invokeMethod(ffmValidatorService, "processInboundPremiums", bemDTO,financialValidator);

		assertEquals("Only one premium record", 1, epsPremiums.size());

		AdditionalInfoType actualPremium1 = epsPremiums.get(JAN_15);
		
		assertEquals("ESD", expectedESD_EPS1, DateTimeUtil.getLocalDateFromXmlGC(actualPremium1.getEffectiveStartDate()));
		assertEquals("EED", expectedEED_EPS1, DateTimeUtil.getLocalDateFromXmlGC(actualPremium1.getEffectiveEndDate()));
		assertEquals("APTC", expectedAPTC_EPS1, actualPremium1.getAPTCAmount());
		assertEquals("CSR", expectedCSR_EPS1, actualPremium1.getCSRAmount());
		assertEquals("TPA", expectedTPA_EPS1, actualPremium1.getTotalPremiumAmount());
		assertEquals("TIRA", expectedTIRA_EPS1, actualPremium1.getTotalIndividualResponsibilityAmount());
	}

	/*
	 * FDD Scenario 2 - One subscriber, mid month end
	 */
	
	@Test
	public void test_processInboundPremiums_mid_month_end() {

		LocalDate esd = JAN_1;
		LocalDate eed = APR_15;
		BigDecimal aptc = new BigDecimal("100");
		BigDecimal csr = new BigDecimal("50");
		BigDecimal tpa = new BigDecimal("150");
		BigDecimal tira = new BigDecimal("100");

		// Expected data after replacement, Record 1
		LocalDate expectedESD_EPS1 = esd;
		LocalDate expectedEED_EPS1 = eed;
		BigDecimal expectedAPTC_EPS1 = aptc;
		BigDecimal expectedCSR_EPS1 = csr;
		BigDecimal expectedTPA_EPS1 = tpa;
		BigDecimal expectedTIRA_EPS1 = tira;

		MemberType inboundSubscriber = makeSubscriberMaintenance("5555", JAN_1, APR_15);
		
		AdditionalInfoType ait = new AdditionalInfoType();
		
		ait.setEffectiveStartDate(DateTimeUtil.getXMLGregorianCalendar(esd));
		ait.setEffectiveEndDate(DateTimeUtil.getXMLGregorianCalendar(eed));
		
		setAdditionalInfoTypeValue(ait, "TPA", tpa, null);
		setAdditionalInfoTypeValue(ait, "TIRA", tira, null);
		setAdditionalInfoTypeValue(ait, "APTC", aptc, null);
		setAdditionalInfoTypeValue(ait, "CSR", csr, null);
		
		inboundSubscriber.getAdditionalInfo().add(ait);
		
		
		BenefitEnrollmentMaintenanceDTO bemDTO = new BenefitEnrollmentMaintenanceDTO();
		BenefitEnrollmentMaintenanceType bem = new BenefitEnrollmentMaintenanceType();
		bem.setPolicyInfo(makePolicyInfoType("MPGPI-5555", JAN_1, APR_15, PolicyStatus.EFFECTUATED_2));
		bem.getMember().add(inboundSubscriber);
		bemDTO.setBem(bem);

		Map<LocalDate, AdditionalInfoType> epsPremiums = ReflectionTestUtils.invokeMethod(ffmValidatorService, "processInboundPremiums", bemDTO,financialValidator);

		assertEquals("Only one premium record", 1, epsPremiums.size());

		AdditionalInfoType actualPremium1 = epsPremiums.get(esd);

		assertEquals("ESD", expectedESD_EPS1, DateTimeUtil.getLocalDateFromXmlGC(actualPremium1.getEffectiveStartDate()));
		assertEquals("EED", expectedEED_EPS1, DateTimeUtil.getLocalDateFromXmlGC(actualPremium1.getEffectiveEndDate()));
		assertEquals("APTC", expectedAPTC_EPS1, actualPremium1.getAPTCAmount());
		assertEquals("CSR", expectedCSR_EPS1, actualPremium1.getCSRAmount());
		assertEquals("TPA", expectedTPA_EPS1, actualPremium1.getTotalPremiumAmount());
		assertEquals("TIRA", expectedTIRA_EPS1, actualPremium1.getTotalIndividualResponsibilityAmount());
	}
     
	/*
	 * FDD Scenario 3 - One subscriber, mid month start & end
	 */
	
	@Test
	public void test_processInboundPremiums_mid_month_start_and_end_current_Ipp() {

		LocalDate esd = FEB_1;
		LocalDate eed = DEC_15;
		BigDecimal aptc = new BigDecimal("100");
		BigDecimal csr = new BigDecimal("50");
		BigDecimal tpa = new BigDecimal("150");
		BigDecimal tira = new BigDecimal("100");

		// Expected data after replacement, Record 1
		LocalDate expectedESD_EPS1 = JAN_15;
		LocalDate expectedEED_EPS1 = eed;
		BigDecimal expectedAPTC_EPS1 = aptc;
		BigDecimal expectedCSR_EPS1 = csr;
		BigDecimal expectedTPA_EPS1 = tpa;
		BigDecimal expectedTIRA_EPS1 = tira;

		MemberType inboundSubscriber = makeSubscriberMaintenance("5555", JAN_15, DEC_15);
		
		AdditionalInfoType ait = new AdditionalInfoType();
		
		ait.setEffectiveStartDate(DateTimeUtil.getXMLGregorianCalendar(esd));
		ait.setEffectiveEndDate(DateTimeUtil.getXMLGregorianCalendar(eed));
		
		setAdditionalInfoTypeValue(ait, "TPA", tpa, null);
		setAdditionalInfoTypeValue(ait, "TIRA", tira, null);
		setAdditionalInfoTypeValue(ait, "APTC", aptc, null);
		setAdditionalInfoTypeValue(ait, "CSR", csr, null);
		
		inboundSubscriber.getAdditionalInfo().add(ait);
		

		BenefitEnrollmentMaintenanceDTO bemDTO = new BenefitEnrollmentMaintenanceDTO();
		BenefitEnrollmentMaintenanceType bem = new BenefitEnrollmentMaintenanceType();
		bem.setPolicyInfo(makePolicyInfoType("MPGPI-5555", JAN_15, DEC_15, PolicyStatus.EFFECTUATED_2));
		bem.getMember().add(inboundSubscriber);
		bemDTO.setBem(bem);

		Map<LocalDate, AdditionalInfoType> epsPremiums = ReflectionTestUtils.invokeMethod(ffmValidatorService, "processInboundPremiums", bemDTO,financialValidator);

		assertEquals("Only one premium record", 1, epsPremiums.size());

		AdditionalInfoType actualPremium1 = epsPremiums.get(JAN_15);

		assertEquals("ESD", expectedESD_EPS1, DateTimeUtil.getLocalDateFromXmlGC(actualPremium1.getEffectiveStartDate()));
		assertEquals("EED", expectedEED_EPS1, DateTimeUtil.getLocalDateFromXmlGC(actualPremium1.getEffectiveEndDate()));
		assertEquals("APTC", expectedAPTC_EPS1, actualPremium1.getAPTCAmount());
		assertEquals("CSR", expectedCSR_EPS1, actualPremium1.getCSRAmount());
		assertEquals("TPA", expectedTPA_EPS1, actualPremium1.getTotalPremiumAmount());
		assertEquals("TIRA", expectedTIRA_EPS1, actualPremium1.getTotalIndividualResponsibilityAmount());
	}
	
	/*
	 * FDD Scenario 4 - One subscriber, Intra-month start & end
	 */
	
	@Test
	public void test_processInboundPremiums_intra_month_start_and_end_current_Ipp() {

		LocalDate esd = FEB_1;
		LocalDate eed = JAN_31;
		BigDecimal aptc = new BigDecimal("100");
		BigDecimal csr = new BigDecimal("50");
		BigDecimal tpa = new BigDecimal("150");
		BigDecimal tira = new BigDecimal("100");

		// Expected data after replacement, Record 1
		LocalDate expectedESD_EPS1 = JAN_15;
		LocalDate expectedEED_EPS1 = eed;
		BigDecimal expectedAPTC_EPS1 = aptc;
		BigDecimal expectedCSR_EPS1 = csr;
		BigDecimal expectedTPA_EPS1 = tpa;
		BigDecimal expectedTIRA_EPS1 = tira;

		MemberType inboundSubscriber = makeSubscriberMaintenance("5555", JAN_15, JAN_31);
		
		AdditionalInfoType ait = new AdditionalInfoType();
		
		ait.setEffectiveStartDate(DateTimeUtil.getXMLGregorianCalendar(esd));
		ait.setEffectiveEndDate(DateTimeUtil.getXMLGregorianCalendar(eed));
		
		setAdditionalInfoTypeValue(ait, "TPA", tpa, null);
		setAdditionalInfoTypeValue(ait, "TIRA", tira, null);
		setAdditionalInfoTypeValue(ait, "APTC", aptc, null);
		setAdditionalInfoTypeValue(ait, "CSR", csr, null);
		
		inboundSubscriber.getAdditionalInfo().add(ait);
		

		BenefitEnrollmentMaintenanceDTO bemDTO = new BenefitEnrollmentMaintenanceDTO();
		BenefitEnrollmentMaintenanceType bem = new BenefitEnrollmentMaintenanceType();
		bem.setPolicyInfo(makePolicyInfoType("MPGPI-5555", JAN_15, JAN_31, PolicyStatus.EFFECTUATED_2));
		bem.getMember().add(inboundSubscriber);
		bemDTO.setBem(bem);

		Map<LocalDate, AdditionalInfoType> epsPremiums = ReflectionTestUtils.invokeMethod(ffmValidatorService, "processInboundPremiums", bemDTO,financialValidator);

		assertEquals("Only one premium record", 1, epsPremiums.size());

		AdditionalInfoType actualPremium1 = epsPremiums.get(JAN_15);

		assertEquals("ESD", expectedESD_EPS1, DateTimeUtil.getLocalDateFromXmlGC(actualPremium1.getEffectiveStartDate()));
		assertEquals("EED", expectedEED_EPS1, DateTimeUtil.getLocalDateFromXmlGC(actualPremium1.getEffectiveEndDate()));
		assertEquals("APTC", expectedAPTC_EPS1, actualPremium1.getAPTCAmount());
		assertEquals("CSR", expectedCSR_EPS1, actualPremium1.getCSRAmount());
		assertEquals("TPA", expectedTPA_EPS1, actualPremium1.getTotalPremiumAmount());
		assertEquals("TIRA", expectedTIRA_EPS1, actualPremium1.getTotalIndividualResponsibilityAmount());
	}

	/*
	 * FDD Scenario 5, preceding prorated amount (second subscriber)
	 */
	
	@Test
	public void test_processInboundPremiums_preceding_prorated() {

		LocalDate esd = FEB_1;
		LocalDate eed = DEC_31;
		BigDecimal aptc = new BigDecimal("100");
		BigDecimal csr = new BigDecimal("50");
		BigDecimal tpa = new BigDecimal("150");
		BigDecimal tira = new BigDecimal("100");

		LocalDate esd2 = JAN_15;
		LocalDate eed2 = JAN_31;
		BigDecimal proratedAptc2 = new BigDecimal("54.84");
		BigDecimal proratedCsr2 = new BigDecimal("27.42");
		BigDecimal proratedTpa2 = new BigDecimal("82.26");
		BigDecimal proratedTira2 = new BigDecimal("54.84");

		// Expected data after replacement, Record 1
		LocalDate expectedESD_EPS1 = esd;
		LocalDate expectedEED_EPS1 = eed;
		BigDecimal expectedAPTC_EPS1 = aptc;
		BigDecimal expectedCSR_EPS1 = csr;
		BigDecimal expectedTPA_EPS1 = tpa;
		BigDecimal expectedTIRA_EPS1 = tira;

		BenefitEnrollmentMaintenanceDTO bemDTO = new BenefitEnrollmentMaintenanceDTO();
		BenefitEnrollmentMaintenanceType bem = new BenefitEnrollmentMaintenanceType();
		bem.setPolicyInfo(new PolicyInfoType());
		// Set same as monthly premium start/end (esd and eed).
		bem.getPolicyInfo().setPolicyStartDate(DateTimeUtil.getXMLGregorianCalendar(esd));
		bem.getPolicyInfo().setPolicyEndDate(DateTimeUtil.getXMLGregorianCalendar(eed));

		MemberType inboundSubscriber = makeSubscriberMaintenance("5555", JAN_15, DEC_31);
		
		AdditionalInfoType ait = new AdditionalInfoType();
		
		ait.setEffectiveStartDate(DateTimeUtil.getXMLGregorianCalendar(esd));
		ait.setEffectiveEndDate(DateTimeUtil.getXMLGregorianCalendar(eed));
		
		setAdditionalInfoTypeValue(ait, "TPA", tpa, null);
		setAdditionalInfoTypeValue(ait, "TIRA", tira, null);
		setAdditionalInfoTypeValue(ait, "APTC", aptc, null);
		setAdditionalInfoTypeValue(ait, "CSR", csr, null);
		
		inboundSubscriber.getAdditionalInfo().add(ait);
		bem.getMember().add(inboundSubscriber);

		MemberType inboundSubscriber2 = makeSubscriberMaintenance("6666", null, null);
		
		AdditionalInfoType ait2 = new AdditionalInfoType();
		
		ait2.setEffectiveStartDate(DateTimeUtil.getXMLGregorianCalendar(esd2));
		ait2.setEffectiveEndDate(DateTimeUtil.getXMLGregorianCalendar(eed2));
		
		setAdditionalInfoTypeValue(ait2, "TPA", tpa, null);
		setAdditionalInfoTypeValue(ait2, "TIRA", tira, null);
		setAdditionalInfoTypeValue(ait2, "APTC", aptc, null);
		setAdditionalInfoTypeValue(ait2, "CSR", csr, null);
		setAdditionalInfoTypeValue(ait2, "P-TPA", tpa, proratedTpa2);
		setAdditionalInfoTypeValue(ait2, "P-TIRA", tira, proratedTira2);
		setAdditionalInfoTypeValue(ait2, "P-APTC", aptc, proratedAptc2);
		setAdditionalInfoTypeValue(ait2, "P-CSR", csr, proratedCsr2);
		
		inboundSubscriber2.getAdditionalInfo().add(ait2);
		
		bem.getMember().add(inboundSubscriber2);

		bemDTO.setBem(bem);

		Map<LocalDate, AdditionalInfoType> epsPremiums = ReflectionTestUtils.invokeMethod(ffmValidatorService, "processInboundPremiums", bemDTO,financialValidator);

		assertEquals("Two premium records", 2, epsPremiums.size());

		AdditionalInfoType actualPremium1 = epsPremiums.get(esd);
		assertEquals("ESD", expectedESD_EPS1, DateTimeUtil.getLocalDateFromXmlGC(actualPremium1.getEffectiveStartDate()));
		assertEquals("EED", expectedEED_EPS1, DateTimeUtil.getLocalDateFromXmlGC(actualPremium1.getEffectiveEndDate()));
		assertEquals("APTC", expectedAPTC_EPS1, actualPremium1.getAPTCAmount());
		assertEquals("CSR", expectedCSR_EPS1, actualPremium1.getCSRAmount());
		assertEquals("TPA", expectedTPA_EPS1, actualPremium1.getTotalPremiumAmount());
		assertEquals("TIRA", expectedTIRA_EPS1, actualPremium1.getTotalIndividualResponsibilityAmount());

		AdditionalInfoType actualPremium2 = epsPremiums.get(esd2);
		assertEquals("ESD", esd2, DateTimeUtil.getLocalDateFromXmlGC(actualPremium2.getEffectiveStartDate()));
		assertEquals("EED", eed2, DateTimeUtil.getLocalDateFromXmlGC(actualPremium2.getEffectiveEndDate()));
		assertEquals("APTC", expectedAPTC_EPS1, actualPremium2.getAPTCAmount());
		assertEquals("CSR", expectedCSR_EPS1, actualPremium2.getCSRAmount());
		assertEquals("TPA", expectedTPA_EPS1, actualPremium2.getTotalPremiumAmount());
		assertEquals("TIRA", expectedTIRA_EPS1, actualPremium2.getTotalIndividualResponsibilityAmount());
		assertEquals("P-APTC", proratedAptc2, actualPremium2.getProratedAppliedAPTCAmount());
		assertEquals("P-CSR", proratedCsr2, actualPremium2.getProratedCSRAmount());
		assertEquals("P-TPA", proratedTpa2, actualPremium2.getProratedMonthlyPremiumAmount());
		assertEquals("P-TIRA", proratedTira2, actualPremium2.getProratedIndividualResponsibleAmount());
	}

	/*
	 * FDD Scenario 6, trailing prorated amount (second subscriber) - mid-month end with prorated amounts in FFM
	 */
	
	@Test
	public void test_processInboundPremiums_trailing_prorated() {

		LocalDate esd = JAN_1;
		LocalDate eed = APR_15;
		BigDecimal aptc = new BigDecimal("100");
		BigDecimal csr = new BigDecimal("50");
		BigDecimal tpa = new BigDecimal("150");
		BigDecimal tira = new BigDecimal("100");

		LocalDate esd2 = APR_1;
		LocalDate eed2 = APR_15;
		BigDecimal proratedAptc2 = new BigDecimal("50");
		BigDecimal proratedCsr2 = new BigDecimal("25");
		BigDecimal proratedTpa2 = new BigDecimal("75");
		BigDecimal proratedTira2 = new BigDecimal("50");

		// Expected data after replacement, Record 1
		LocalDate expectedESD_EPS1 = esd;
		LocalDate expectedEED_EPS1 = esd2.minusDays(1);
		BigDecimal expectedAPTC_EPS1 = aptc;
		BigDecimal expectedCSR_EPS1 = csr;
		BigDecimal expectedTPA_EPS1 = tpa;
		BigDecimal expectedTIRA_EPS1 = tira;

		BenefitEnrollmentMaintenanceDTO bemDTO = new BenefitEnrollmentMaintenanceDTO();
		BenefitEnrollmentMaintenanceType bem = new BenefitEnrollmentMaintenanceType();
		bem.setPolicyInfo(makePolicyInfoType("MPGPI-5555", JAN_1, APR_15, PolicyStatus.EFFECTUATED_2));

		MemberType inboundSubscriber = makeSubscriberMaintenance("5555", JAN_1, APR_15);
		
		AdditionalInfoType ait = new AdditionalInfoType();
		
		ait.setEffectiveStartDate(DateTimeUtil.getXMLGregorianCalendar(esd));
		ait.setEffectiveEndDate(DateTimeUtil.getXMLGregorianCalendar(eed));
		
		setAdditionalInfoTypeValue(ait, "TPA", tpa, null);
		setAdditionalInfoTypeValue(ait, "TIRA", tira, null);
		setAdditionalInfoTypeValue(ait, "APTC", aptc, null);
		setAdditionalInfoTypeValue(ait, "CSR", csr, null);
		
		inboundSubscriber.getAdditionalInfo().add(ait);
		bem.getMember().add(inboundSubscriber);

		MemberType inboundSubscriber2 = makeSubscriberMaintenance("6666", null, null);
		
		AdditionalInfoType ait2 = new AdditionalInfoType();
		
		ait2.setEffectiveStartDate(DateTimeUtil.getXMLGregorianCalendar(esd2));
		ait2.setEffectiveEndDate(DateTimeUtil.getXMLGregorianCalendar(eed2));
		
		setAdditionalInfoTypeValue(ait2, "TPA", tpa, null);
		setAdditionalInfoTypeValue(ait2, "TIRA", tira, null);
		setAdditionalInfoTypeValue(ait2, "APTC", aptc, null);
		setAdditionalInfoTypeValue(ait2, "CSR", csr, null);
		setAdditionalInfoTypeValue(ait2, "P-TPA", tpa, proratedTpa2);
		setAdditionalInfoTypeValue(ait2, "P-TIRA", tira, proratedTira2);
		setAdditionalInfoTypeValue(ait2, "P-APTC", aptc, proratedAptc2);
		setAdditionalInfoTypeValue(ait2, "P-CSR", csr, proratedCsr2);
		
		inboundSubscriber2.getAdditionalInfo().add(ait2);
		bem.getMember().add(inboundSubscriber2);

		bemDTO.setBem(bem);

		Map<LocalDate, AdditionalInfoType> epsPremiums = ReflectionTestUtils.invokeMethod(ffmValidatorService, "processInboundPremiums", bemDTO,financialValidator);

		assertEquals("Two premium records", 2, epsPremiums.size());

		AdditionalInfoType actualPremium1 = epsPremiums.get(esd);
		assertEquals("ESD", expectedESD_EPS1, DateTimeUtil.getLocalDateFromXmlGC(actualPremium1.getEffectiveStartDate()));
		assertEquals("EED", expectedEED_EPS1, DateTimeUtil.getLocalDateFromXmlGC(actualPremium1.getEffectiveEndDate()));
		assertEquals("APTC", expectedAPTC_EPS1, actualPremium1.getAPTCAmount());
		assertEquals("CSR", expectedCSR_EPS1, actualPremium1.getCSRAmount());
		assertEquals("TPA", expectedTPA_EPS1, actualPremium1.getTotalPremiumAmount());
		assertEquals("TIRA", expectedTIRA_EPS1, actualPremium1.getTotalIndividualResponsibilityAmount());

		AdditionalInfoType actualPremium2 = epsPremiums.get(esd2);
		assertEquals("ESD", esd2, DateTimeUtil.getLocalDateFromXmlGC(actualPremium2.getEffectiveStartDate()));
		assertEquals("EED", eed2, DateTimeUtil.getLocalDateFromXmlGC(actualPremium2.getEffectiveEndDate()));
		assertEquals("APTC", expectedAPTC_EPS1, actualPremium2.getAPTCAmount());
		assertEquals("CSR", expectedCSR_EPS1, actualPremium2.getCSRAmount());
		assertEquals("TPA", expectedTPA_EPS1, actualPremium2.getTotalPremiumAmount());
		assertEquals("TIRA", expectedTIRA_EPS1, actualPremium2.getTotalIndividualResponsibilityAmount());
		assertEquals("P-APTC", proratedAptc2, actualPremium2.getProratedAppliedAPTCAmount());
		assertEquals("P-CSR", proratedCsr2, actualPremium2.getProratedCSRAmount());
		assertEquals("P-TPA", proratedTpa2, actualPremium2.getProratedMonthlyPremiumAmount());
		assertEquals("P-TIRA", proratedTira2, actualPremium2.getProratedIndividualResponsibleAmount());

		assertEquals("Only one subscriber loops", 1, BEMDataUtil.getSubscriberOccurrances(bem).size());

		MemberType expectedSubscriber = BEMDataUtil.getSubscriberOccurrances(bem).get(0);
		assertEquals("Only one subscriber loops", inboundSubscriber.getSubscriberID(), expectedSubscriber.getSubscriberID());

	}

	/*
	 * FDD Scenario 7, mid month start and end (second, third subscriber)
	 */
	
	@Test
	public void test_processInboundPremiums_mid_month_start_and_end() {

		LocalDate esd = FEB_1;
		LocalDate eed = DEC_15;
		BigDecimal aptc = new BigDecimal("100");
		BigDecimal csr = new BigDecimal("50");
		BigDecimal tpa = new BigDecimal("150");
		BigDecimal tira = new BigDecimal("100");

		LocalDate esd2 = JAN_15;
		LocalDate eed2 = JAN_31;
		BigDecimal proratedAptc2 = new BigDecimal("54.84");
		BigDecimal proratedCsr2 = new BigDecimal("27.42");
		BigDecimal proratedTpa2 = new BigDecimal("82.26");
		BigDecimal proratedTira2 = new BigDecimal("54.84");

		LocalDate esd3 = DEC_1;
		LocalDate eed3 = DEC_15;
		BigDecimal proratedAptc3 = new BigDecimal("48.39");
		BigDecimal proratedCsr3 = new BigDecimal("24.19");
		BigDecimal proratedTpa3 = new BigDecimal("72.58");
		BigDecimal proratedTira3 = new BigDecimal("48.39");

		// Expected data after replacement, Record 1
		LocalDate expectedESD_EPS1 = esd;
		LocalDate expectedEED_EPS1 = esd3.minusDays(1);
		BigDecimal expectedAPTC_EPS1 = aptc;
		BigDecimal expectedCSR_EPS1 = csr;
		BigDecimal expectedTPA_EPS1 = tpa;
		BigDecimal expectedTIRA_EPS1 = tira;

		BenefitEnrollmentMaintenanceDTO bemDTO = new BenefitEnrollmentMaintenanceDTO();
		BenefitEnrollmentMaintenanceType bem = new BenefitEnrollmentMaintenanceType();
		bem.setPolicyInfo(makePolicyInfoType("MPGPI-5555", JAN_15, DEC_15, PolicyStatus.EFFECTUATED_2));

		MemberType inboundSubscriber = makeSubscriberMaintenance("5555", JAN_15, DEC_15);
		
		AdditionalInfoType ait = new AdditionalInfoType();
		
		ait.setEffectiveStartDate(DateTimeUtil.getXMLGregorianCalendar(esd));
		ait.setEffectiveEndDate(DateTimeUtil.getXMLGregorianCalendar(eed));
		
		setAdditionalInfoTypeValue(ait, "TPA", tpa, null);
		setAdditionalInfoTypeValue(ait, "TIRA", tira, null);
		setAdditionalInfoTypeValue(ait, "APTC", aptc, null);
		setAdditionalInfoTypeValue(ait, "CSR", csr, null);
		
		inboundSubscriber.getAdditionalInfo().add(ait);
		bem.getMember().add(inboundSubscriber);

		MemberType inboundSubscriber2 = makeSubscriberMaintenance("6666", null, null);
		
		AdditionalInfoType ait2 = new AdditionalInfoType();
		
		ait2.setEffectiveStartDate(DateTimeUtil.getXMLGregorianCalendar(esd2));
		ait2.setEffectiveEndDate(DateTimeUtil.getXMLGregorianCalendar(eed2));
		
		setAdditionalInfoTypeValue(ait2, "TPA", tpa, null);
		setAdditionalInfoTypeValue(ait2, "TIRA", tira, null);
		setAdditionalInfoTypeValue(ait2, "APTC", aptc, null);
		setAdditionalInfoTypeValue(ait2, "CSR", csr, null);
		setAdditionalInfoTypeValue(ait2, "P-TPA", tpa, proratedTpa2);
		setAdditionalInfoTypeValue(ait2, "P-TIRA", tira, proratedTira2);
		setAdditionalInfoTypeValue(ait2, "P-APTC", aptc, proratedAptc2);
		setAdditionalInfoTypeValue(ait2, "P-CSR", csr, proratedCsr2);
		
		inboundSubscriber2.getAdditionalInfo().add(ait2);
		bem.getMember().add(inboundSubscriber2);

		MemberType inboundSubscriber3 = makeSubscriberMaintenance("7777", null, null);
		
		AdditionalInfoType ait3 = new AdditionalInfoType();
		
		ait3.setEffectiveStartDate(DateTimeUtil.getXMLGregorianCalendar(esd3));
		ait3.setEffectiveEndDate(DateTimeUtil.getXMLGregorianCalendar(eed3));
		
		setAdditionalInfoTypeValue(ait3, "TPA", tpa, null);
		setAdditionalInfoTypeValue(ait3, "TIRA", tira, null);
		setAdditionalInfoTypeValue(ait3, "APTC", aptc, null);
		setAdditionalInfoTypeValue(ait3, "CSR", csr, null);
		setAdditionalInfoTypeValue(ait3, "P-TPA", tpa, proratedTpa3);
		setAdditionalInfoTypeValue(ait3, "P-TIRA", tira, proratedTira3);
		setAdditionalInfoTypeValue(ait3, "P-APTC", aptc, proratedAptc3);
		setAdditionalInfoTypeValue(ait3, "P-CSR", csr, proratedCsr3);
		
		inboundSubscriber3.getAdditionalInfo().add(ait3);
		bem.getMember().add(inboundSubscriber3);

		bemDTO.setBem(bem);

		Map<LocalDate, AdditionalInfoType> epsPremiums = ReflectionTestUtils.invokeMethod(ffmValidatorService, "processInboundPremiums", bemDTO,financialValidator);

		assertEquals("Three premium records", 3, epsPremiums.size());

		AdditionalInfoType actualPremium1 = epsPremiums.get(esd);
		assertEquals("ESD", expectedESD_EPS1, DateTimeUtil.getLocalDateFromXmlGC(actualPremium1.getEffectiveStartDate()));
		assertEquals("EED", expectedEED_EPS1, DateTimeUtil.getLocalDateFromXmlGC(actualPremium1.getEffectiveEndDate()));
		assertEquals("APTC", expectedAPTC_EPS1, actualPremium1.getAPTCAmount());
		assertEquals("CSR", expectedCSR_EPS1, actualPremium1.getCSRAmount());
		assertEquals("TPA", expectedTPA_EPS1, actualPremium1.getTotalPremiumAmount());
		assertEquals("TIRA", expectedTIRA_EPS1, actualPremium1.getTotalIndividualResponsibilityAmount());

		AdditionalInfoType actualPremium2 = epsPremiums.get(esd2);
		assertEquals("ESD", esd2, DateTimeUtil.getLocalDateFromXmlGC(actualPremium2.getEffectiveStartDate()));
		assertEquals("EED", eed2, DateTimeUtil.getLocalDateFromXmlGC(actualPremium2.getEffectiveEndDate()));
		assertEquals("APTC", expectedAPTC_EPS1, actualPremium2.getAPTCAmount());
		assertEquals("CSR", expectedCSR_EPS1, actualPremium2.getCSRAmount());
		assertEquals("TPA", expectedTPA_EPS1, actualPremium2.getTotalPremiumAmount());
		assertEquals("TIRA", expectedTIRA_EPS1, actualPremium2.getTotalIndividualResponsibilityAmount());
		assertEquals("P-APTC", proratedAptc2, actualPremium2.getProratedAppliedAPTCAmount());
		assertEquals("P-CSR", proratedCsr2, actualPremium2.getProratedCSRAmount());
		assertEquals("P-TPA", proratedTpa2, actualPremium2.getProratedMonthlyPremiumAmount());
		assertEquals("P-TIRA", proratedTira2, actualPremium2.getProratedIndividualResponsibleAmount());

		AdditionalInfoType actualPremium3 = epsPremiums.get(esd3);
		assertEquals("ESD", esd3, DateTimeUtil.getLocalDateFromXmlGC(actualPremium3.getEffectiveStartDate()));
		assertEquals("EED", eed3, DateTimeUtil.getLocalDateFromXmlGC(actualPremium3.getEffectiveEndDate()));
		assertEquals("APTC", expectedAPTC_EPS1, actualPremium3.getAPTCAmount());
		assertEquals("CSR", expectedCSR_EPS1, actualPremium3.getCSRAmount());
		assertEquals("TPA", expectedTPA_EPS1, actualPremium3.getTotalPremiumAmount());
		assertEquals("TIRA", expectedTIRA_EPS1, actualPremium3.getTotalIndividualResponsibilityAmount());
		assertEquals("P-APTC", proratedAptc3, actualPremium3.getProratedAppliedAPTCAmount());
		assertEquals("P-CSR", proratedCsr3, actualPremium3.getProratedCSRAmount());
		assertEquals("P-TPA", proratedTpa3, actualPremium3.getProratedMonthlyPremiumAmount());
		assertEquals("P-TIRA", proratedTira3, actualPremium3.getProratedIndividualResponsibleAmount());

		assertEquals("Only one subscriber loops", 1, BEMDataUtil.getSubscriberOccurrances(bem).size());

		MemberType expectedSubscriber = BEMDataUtil.getSubscriberOccurrances(bem).get(0);
		assertEquals("Only one subscriber loops", inboundSubscriber.getSubscriberID(), expectedSubscriber.getSubscriberID());
	}

	/*
	 * FDD Scenario 7A, mid month start and end (second, third subscriber)
	 */
	
	@Test
	public void test_processInboundPremiums_mid_month_start_and_end_A() {

		LocalDate esd = FEB_1;
		LocalDate eed = FEB_15;
		BigDecimal aptc = new BigDecimal("100");
		BigDecimal csr = new BigDecimal("50");
		BigDecimal tpa = new BigDecimal("150");
		BigDecimal tira = new BigDecimal("100");

		LocalDate esd2 = JAN_15;
		LocalDate eed2 = JAN_31;
		BigDecimal proratedAptc2 = new BigDecimal("54.84");
		BigDecimal proratedCsr2 = new BigDecimal("27.42");
		BigDecimal proratedTpa2 = new BigDecimal("82.26");
		BigDecimal proratedTira2 = new BigDecimal("54.84");

		LocalDate esd3 = FEB_1;
		LocalDate eed3 = FEB_15;
		BigDecimal proratedAptc3 = new BigDecimal("48.39");
		BigDecimal proratedCsr3 = new BigDecimal("24.19");
		BigDecimal proratedTpa3 = new BigDecimal("72.58");
		BigDecimal proratedTira3 = new BigDecimal("48.39");

		// Expected data after replacement, Record 1
		BigDecimal expectedAPTC_EPS1 = aptc;
		BigDecimal expectedCSR_EPS1 = csr;
		BigDecimal expectedTPA_EPS1 = tpa;
		BigDecimal expectedTIRA_EPS1 = tira;

		BenefitEnrollmentMaintenanceDTO bemDTO = new BenefitEnrollmentMaintenanceDTO();
		BenefitEnrollmentMaintenanceType bem = new BenefitEnrollmentMaintenanceType();
		bem.setPolicyInfo(makePolicyInfoType("MPGPI-5555", JAN_15, FEB_15, PolicyStatus.EFFECTUATED_2));

		MemberType inboundSubscriber = makeSubscriberMaintenance("5555", JAN_15, FEB_15);
		
		AdditionalInfoType ait = new AdditionalInfoType();
		
		ait.setEffectiveStartDate(DateTimeUtil.getXMLGregorianCalendar(esd));
		ait.setEffectiveEndDate(DateTimeUtil.getXMLGregorianCalendar(eed));
		
		setAdditionalInfoTypeValue(ait, "TPA", tpa, null);
		setAdditionalInfoTypeValue(ait, "TIRA", tira, null);
		setAdditionalInfoTypeValue(ait, "APTC", aptc, null);
		setAdditionalInfoTypeValue(ait, "CSR", csr, null);
		
		inboundSubscriber.getAdditionalInfo().add(ait);
		bem.getMember().add(inboundSubscriber);

		MemberType inboundSubscriber2 = makeSubscriberMaintenance("6666", null, null);
		
		AdditionalInfoType ait2 = new AdditionalInfoType();
		
		ait2.setEffectiveStartDate(DateTimeUtil.getXMLGregorianCalendar(esd2));
		ait2.setEffectiveEndDate(DateTimeUtil.getXMLGregorianCalendar(eed2));
		
		setAdditionalInfoTypeValue(ait2, "TPA", tpa, null);
		setAdditionalInfoTypeValue(ait2, "TIRA", tira, null);
		setAdditionalInfoTypeValue(ait2, "APTC", aptc, null);
		setAdditionalInfoTypeValue(ait2, "CSR", csr, null);
		setAdditionalInfoTypeValue(ait2, "P-TPA", tpa, proratedTpa2);
		setAdditionalInfoTypeValue(ait2, "P-TIRA", tira, proratedTira2);
		setAdditionalInfoTypeValue(ait2, "P-APTC", aptc, proratedAptc2);
		setAdditionalInfoTypeValue(ait2, "P-CSR", csr, proratedCsr2);
		
		inboundSubscriber2.getAdditionalInfo().add(ait2);
		bem.getMember().add(inboundSubscriber2);

		MemberType inboundSubscriber3 = makeSubscriberMaintenance("7777", null, null);
		
		AdditionalInfoType ait3 = new AdditionalInfoType();
		
		ait3.setEffectiveStartDate(DateTimeUtil.getXMLGregorianCalendar(esd3));
		ait3.setEffectiveEndDate(DateTimeUtil.getXMLGregorianCalendar(eed3));
		
		setAdditionalInfoTypeValue(ait3, "TPA", tpa, null);
		setAdditionalInfoTypeValue(ait3, "TIRA", tira, null);
		setAdditionalInfoTypeValue(ait3, "APTC", aptc, null);
		setAdditionalInfoTypeValue(ait3, "CSR", csr, null);
		setAdditionalInfoTypeValue(ait3, "P-TPA", tpa, proratedTpa3);
		setAdditionalInfoTypeValue(ait3, "P-TIRA", tira, proratedTira3);
		setAdditionalInfoTypeValue(ait3, "P-APTC", aptc, proratedAptc3);
		setAdditionalInfoTypeValue(ait3, "P-CSR", csr, proratedCsr3);
		
		inboundSubscriber3.getAdditionalInfo().add(ait3);
		bem.getMember().add(inboundSubscriber3);

		bemDTO.setBem(bem);

		Map<LocalDate, AdditionalInfoType> epsPremiums = ReflectionTestUtils.invokeMethod(ffmValidatorService, "processInboundPremiums", bemDTO,financialValidator);

		assertEquals("Three premium records", 2, epsPremiums.size());

		AdditionalInfoType actualPremium2 = epsPremiums.get(esd2);
		assertEquals("ESD", esd2, DateTimeUtil.getLocalDateFromXmlGC(actualPremium2.getEffectiveStartDate()));
		assertEquals("EED", eed2, DateTimeUtil.getLocalDateFromXmlGC(actualPremium2.getEffectiveEndDate()));
		assertEquals("APTC", expectedAPTC_EPS1, actualPremium2.getAPTCAmount());
		assertEquals("CSR", expectedCSR_EPS1, actualPremium2.getCSRAmount());
		assertEquals("TPA", expectedTPA_EPS1, actualPremium2.getTotalPremiumAmount());
		assertEquals("TIRA", expectedTIRA_EPS1, actualPremium2.getTotalIndividualResponsibilityAmount());
		assertEquals("P-APTC", proratedAptc2, actualPremium2.getProratedAppliedAPTCAmount());
		assertEquals("P-CSR", proratedCsr2, actualPremium2.getProratedCSRAmount());
		assertEquals("P-TPA", proratedTpa2, actualPremium2.getProratedMonthlyPremiumAmount());
		assertEquals("P-TIRA", proratedTira2, actualPremium2.getProratedIndividualResponsibleAmount());

		AdditionalInfoType actualPremium3 = epsPremiums.get(esd3);
		assertEquals("ESD", esd3, DateTimeUtil.getLocalDateFromXmlGC(actualPremium3.getEffectiveStartDate()));
		assertEquals("EED", eed3, DateTimeUtil.getLocalDateFromXmlGC(actualPremium3.getEffectiveEndDate()));
		assertEquals("APTC", expectedAPTC_EPS1, actualPremium3.getAPTCAmount());
		assertEquals("CSR", expectedCSR_EPS1, actualPremium3.getCSRAmount());
		assertEquals("TPA", expectedTPA_EPS1, actualPremium3.getTotalPremiumAmount());
		assertEquals("TIRA", expectedTIRA_EPS1, actualPremium3.getTotalIndividualResponsibilityAmount());
		assertEquals("P-APTC", proratedAptc3, actualPremium3.getProratedAppliedAPTCAmount());
		assertEquals("P-CSR", proratedCsr3, actualPremium3.getProratedCSRAmount());
		assertEquals("P-TPA", proratedTpa3, actualPremium3.getProratedMonthlyPremiumAmount());
		assertEquals("P-TIRA", proratedTira3, actualPremium3.getProratedIndividualResponsibleAmount());

		assertEquals("Only one subscriber loops", 1, BEMDataUtil.getSubscriberOccurrances(bem).size());

		MemberType expectedSubscriber = BEMDataUtil.getSubscriberOccurrances(bem).get(0);
		assertEquals("Only one subscriber loops", inboundSubscriber.getSubscriberID(), expectedSubscriber.getSubscriberID());
	}

	/*
	 * FDD Scenario 8, Intra-month start & end (second subscriber)
	 */
	
	@Test
	public void test_processInboundPremiums_intra_month_start_and_end() {

		LocalDate esd = FEB_1;
		LocalDate eed = JAN_31;
		BigDecimal aptc = new BigDecimal("100");
		BigDecimal csr = new BigDecimal("50");
		BigDecimal tpa = new BigDecimal("150");
		BigDecimal tira = new BigDecimal("100");

		LocalDate esd2 = JAN_15;
		LocalDate eed2 = JAN_31;
		BigDecimal proratedAptc2 = new BigDecimal("54.84");
		BigDecimal proratedCsr2 = new BigDecimal("27.42");
		BigDecimal proratedTpa2 = new BigDecimal("82.26");
		BigDecimal proratedTira2 = new BigDecimal("54.84");

		// Expected data after replacement, Record 1
		BigDecimal expectedAPTC_EPS1 = aptc;
		BigDecimal expectedCSR_EPS1 = csr;
		BigDecimal expectedTPA_EPS1 = tpa;
		BigDecimal expectedTIRA_EPS1 = tira;

		BenefitEnrollmentMaintenanceDTO bemDTO = new BenefitEnrollmentMaintenanceDTO();
		BenefitEnrollmentMaintenanceType bem = new BenefitEnrollmentMaintenanceType();
		bem.setPolicyInfo(makePolicyInfoType("MPGPI-5555", JAN_15, JAN_31, PolicyStatus.EFFECTUATED_2));

		MemberType dependent = new MemberType();
		dependent.setMemberInformation(new MemberRelatedInfoType());
		dependent.getMemberInformation().setSubscriberIndicator(BooleanIndicatorSimpleType.N);
		bem.getMember().add(dependent);

		MemberType inboundSubscriber = makeSubscriberMaintenance("5555", JAN_15, JAN_31);
		
		AdditionalInfoType ait = new AdditionalInfoType();
		
		ait.setEffectiveStartDate(DateTimeUtil.getXMLGregorianCalendar(esd));
		ait.setEffectiveEndDate(DateTimeUtil.getXMLGregorianCalendar(eed));
		
		setAdditionalInfoTypeValue(ait, "TPA", tpa, null);
		setAdditionalInfoTypeValue(ait, "TIRA", tira, null);
		setAdditionalInfoTypeValue(ait, "APTC", aptc, null);
		setAdditionalInfoTypeValue(ait, "CSR", csr, null);
		
		inboundSubscriber.getAdditionalInfo().add(ait);
		bem.getMember().add(inboundSubscriber);

		MemberType inboundSubscriber2 = makeSubscriberMaintenance("6666", null, null);
		
		AdditionalInfoType ait2 = new AdditionalInfoType();
		
		ait2.setEffectiveStartDate(DateTimeUtil.getXMLGregorianCalendar(esd2));
		ait2.setEffectiveEndDate(DateTimeUtil.getXMLGregorianCalendar(eed2));
		
		setAdditionalInfoTypeValue(ait2, "TPA", tpa, null);
		setAdditionalInfoTypeValue(ait2, "TIRA", tira, null);
		setAdditionalInfoTypeValue(ait2, "APTC", aptc, null);
		setAdditionalInfoTypeValue(ait2, "CSR", csr, null);
		setAdditionalInfoTypeValue(ait2, "P-TPA", tpa, proratedTpa2);
		setAdditionalInfoTypeValue(ait2, "P-TIRA", tira, proratedTira2);
		setAdditionalInfoTypeValue(ait2, "P-APTC", aptc, proratedAptc2);
		setAdditionalInfoTypeValue(ait2, "P-CSR", csr, proratedCsr2);
		
		inboundSubscriber2.getAdditionalInfo().add(ait2);
		bem.getMember().add(inboundSubscriber2);

		MemberType dependent2 = new MemberType();
		dependent2.setMemberInformation(new MemberRelatedInfoType());
		dependent2.getMemberInformation().setSubscriberIndicator(BooleanIndicatorSimpleType.N);
		bem.getMember().add(dependent2);

		bemDTO.setBem(bem);

		Map<LocalDate, AdditionalInfoType> epsPremiums = ReflectionTestUtils.invokeMethod(ffmValidatorService, "processInboundPremiums", bemDTO,financialValidator);

		assertEquals("Only one premium record", 1, epsPremiums.size());

		AdditionalInfoType actualPremium1 = epsPremiums.get(esd);
		assertNull("Only one premium record", actualPremium1);

		AdditionalInfoType actualPremium2 = epsPremiums.get(esd2);
		assertEquals("ESD", esd2, DateTimeUtil.getLocalDateFromXmlGC(actualPremium2.getEffectiveStartDate()));
		assertEquals("EED", eed2, DateTimeUtil.getLocalDateFromXmlGC(actualPremium2.getEffectiveEndDate()));
		assertEquals("APTC", expectedAPTC_EPS1, actualPremium2.getAPTCAmount());
		assertEquals("CSR", expectedCSR_EPS1, actualPremium2.getCSRAmount());
		assertEquals("TPA", expectedTPA_EPS1, actualPremium2.getTotalPremiumAmount());
		assertEquals("TIRA", expectedTIRA_EPS1, actualPremium2.getTotalIndividualResponsibilityAmount());
		assertEquals("P-APTC", proratedAptc2, actualPremium2.getProratedAppliedAPTCAmount());
		assertEquals("P-CSR", proratedCsr2, actualPremium2.getProratedCSRAmount());
		assertEquals("P-TPA", proratedTpa2, actualPremium2.getProratedMonthlyPremiumAmount());
		assertEquals("P-TIRA", proratedTira2, actualPremium2.getProratedIndividualResponsibleAmount());

		assertEquals("Only one subscriber loops", 1, BEMDataUtil.getSubscriberOccurrances(bem).size());

		MemberType expectedSubscriber = BEMDataUtil.getSubscriberOccurrances(bem).get(0);
		assertEquals("Only one subscriber loops", inboundSubscriber.getSubscriberID(), expectedSubscriber.getSubscriberID());

	}

	/*
	 * FDD Scenario 9, preceding prorated amount (second subscriber), policy start/end = monthly premium start/end
	 */
	
	@Test
	public void test_processInboundPremiums_FFM_Premium_Start_Date_Policy_Start_Date_Aligned() {

		LocalDate esd = JAN_15;
		LocalDate eed = DEC_31;
		BigDecimal aptc = new BigDecimal("100");
		BigDecimal csr = new BigDecimal("50");
		BigDecimal tpa = new BigDecimal("150");
		BigDecimal tira = new BigDecimal("100");

		LocalDate esd2 = JAN_15;
		LocalDate eed2 = JAN_31;
		BigDecimal proratedAptc2 = new BigDecimal("54.84");
		BigDecimal proratedCsr2 = new BigDecimal("27.42");
		BigDecimal proratedTpa2 = new BigDecimal("82.26");
		BigDecimal proratedTira2 = new BigDecimal("54.84");

		// Expected data after replacement, Record 1
		LocalDate expectedESD_EPS1 = FEB_1;
		LocalDate expectedEED_EPS1 = eed;
		BigDecimal expectedAPTC_EPS1 = aptc;
		BigDecimal expectedCSR_EPS1 = csr;
		BigDecimal expectedTPA_EPS1 = tpa;
		BigDecimal expectedTIRA_EPS1 = tira;

		BenefitEnrollmentMaintenanceDTO bemDTO = new BenefitEnrollmentMaintenanceDTO();
		BenefitEnrollmentMaintenanceType bem = new BenefitEnrollmentMaintenanceType();
		bem.setPolicyInfo(new PolicyInfoType());
		// Set same as monthly premium start/end (esd and eed).
		bem.getPolicyInfo().setPolicyStartDate(DateTimeUtil.getXMLGregorianCalendar(esd));
		bem.getPolicyInfo().setPolicyEndDate(DateTimeUtil.getXMLGregorianCalendar(eed));

		MemberType inboundSubscriber = makeSubscriberMaintenance("5555", JAN_15, DEC_31);
		
		AdditionalInfoType ait = new AdditionalInfoType();
		
		ait.setEffectiveStartDate(DateTimeUtil.getXMLGregorianCalendar(esd));
		ait.setEffectiveEndDate(DateTimeUtil.getXMLGregorianCalendar(eed));
		
		setAdditionalInfoTypeValue(ait, "TPA", tpa, null);
		setAdditionalInfoTypeValue(ait, "TIRA", tira, null);
		setAdditionalInfoTypeValue(ait, "APTC", aptc, null);
		setAdditionalInfoTypeValue(ait, "CSR", csr, null);
		
		inboundSubscriber.getAdditionalInfo().add(ait);
		bem.getMember().add(inboundSubscriber);

		MemberType inboundSubscriber2 = makeSubscriberMaintenance("6666", null, null);
		
		AdditionalInfoType ait2 = new AdditionalInfoType();
		
		ait2.setEffectiveStartDate(DateTimeUtil.getXMLGregorianCalendar(esd2));
		ait2.setEffectiveEndDate(DateTimeUtil.getXMLGregorianCalendar(eed2));
		
		setAdditionalInfoTypeValue(ait2, "TPA", tpa, null);
		setAdditionalInfoTypeValue(ait2, "TIRA", tira, null);
		setAdditionalInfoTypeValue(ait2, "APTC", aptc, null);
		setAdditionalInfoTypeValue(ait2, "CSR", csr, null);
		setAdditionalInfoTypeValue(ait2, "P-TPA", tpa, proratedTpa2);
		setAdditionalInfoTypeValue(ait2, "P-TIRA", tira, proratedTira2);
		setAdditionalInfoTypeValue(ait2, "P-APTC", aptc, proratedAptc2);
		setAdditionalInfoTypeValue(ait2, "P-CSR", csr, proratedCsr2);
		
		inboundSubscriber2.getAdditionalInfo().add(ait2);
		bem.getMember().add(inboundSubscriber2);

		bemDTO.setBem(bem);

		Map<LocalDate, AdditionalInfoType> epsPremiums = ReflectionTestUtils.invokeMethod(ffmValidatorService, "processInboundPremiums", bemDTO,financialValidator);

		assertEquals("Two premium records", 2, epsPremiums.size());

		AdditionalInfoType actualPremium1 = epsPremiums.get(FEB_1);
		assertEquals("ESD", expectedESD_EPS1, DateTimeUtil.getLocalDateFromXmlGC(actualPremium1.getEffectiveStartDate()));
		assertEquals("EED", expectedEED_EPS1, DateTimeUtil.getLocalDateFromXmlGC(actualPremium1.getEffectiveEndDate()));
		assertEquals("APTC", expectedAPTC_EPS1, actualPremium1.getAPTCAmount());
		assertEquals("CSR", expectedCSR_EPS1, actualPremium1.getCSRAmount());
		assertEquals("TPA", expectedTPA_EPS1, actualPremium1.getTotalPremiumAmount());
		assertEquals("TIRA", expectedTIRA_EPS1, actualPremium1.getTotalIndividualResponsibilityAmount());

		AdditionalInfoType actualPremium2 = epsPremiums.get(esd2);
		assertEquals("ESD", esd2, DateTimeUtil.getLocalDateFromXmlGC(actualPremium2.getEffectiveStartDate()));
		assertEquals("EED", eed2, DateTimeUtil.getLocalDateFromXmlGC(actualPremium2.getEffectiveEndDate()));
		assertEquals("APTC", expectedAPTC_EPS1, actualPremium2.getAPTCAmount());
		assertEquals("CSR", expectedCSR_EPS1, actualPremium2.getCSRAmount());
		assertEquals("TPA", expectedTPA_EPS1, actualPremium2.getTotalPremiumAmount());
		assertEquals("TIRA", expectedTIRA_EPS1, actualPremium2.getTotalIndividualResponsibilityAmount());
		assertEquals("P-APTC", proratedAptc2, actualPremium2.getProratedAppliedAPTCAmount());
		assertEquals("P-CSR", proratedCsr2, actualPremium2.getProratedCSRAmount());
		assertEquals("P-TPA", proratedTpa2, actualPremium2.getProratedMonthlyPremiumAmount());
		assertEquals("P-TIRA", proratedTira2, actualPremium2.getProratedIndividualResponsibleAmount());

		assertEquals("Only one subscriber loops", 1, BEMDataUtil.getSubscriberOccurrances(bem).size());

		MemberType expectedSubscriber = BEMDataUtil.getSubscriberOccurrances(bem).get(0);
		assertEquals("Only one subscriber loops", inboundSubscriber.getSubscriberID(), expectedSubscriber.getSubscriberID());

	}

	/*
	 * FDD Scenario 11, mid month start and end (second, third subscriber), policy start/end = monthly premium start/end
	 */
	
	@Test
	public void test_processInboundPremiums_mid_month_start_and_end_FFM_Premium_Start_Date_Policy_Start_Date_Aligned() {

		LocalDate esd = JAN_15;
		LocalDate eed = DEC_15;
		BigDecimal aptc = new BigDecimal("100");
		BigDecimal csr = new BigDecimal("50");
		BigDecimal tpa = new BigDecimal("150");
		BigDecimal tira = new BigDecimal("100");

		LocalDate esd2 = JAN_15;
		LocalDate eed2 = JAN_31;
		BigDecimal proratedAptc2 = new BigDecimal("54.84");
		BigDecimal proratedCsr2 = new BigDecimal("27.42");
		BigDecimal proratedTpa2 = new BigDecimal("82.26");
		BigDecimal proratedTira2 = new BigDecimal("54.84");

		LocalDate esd3 = DEC_1;
		LocalDate eed3 = DEC_15;
		BigDecimal proratedAptc3 = new BigDecimal("48.39");
		BigDecimal proratedCsr3 = new BigDecimal("24.19");
		BigDecimal proratedTpa3 = new BigDecimal("72.58");
		BigDecimal proratedTira3 = new BigDecimal("48.39");

		// Expected data after replacement, Record 1
		LocalDate expectedESD_EPS1 = FEB_1;
		LocalDate expectedEED_EPS1 = esd3.minusDays(1);
		BigDecimal expectedAPTC_EPS1 = aptc;
		BigDecimal expectedCSR_EPS1 = csr;
		BigDecimal expectedTPA_EPS1 = tpa;
		BigDecimal expectedTIRA_EPS1 = tira;

		BenefitEnrollmentMaintenanceDTO bemDTO = new BenefitEnrollmentMaintenanceDTO();
		BenefitEnrollmentMaintenanceType bem = new BenefitEnrollmentMaintenanceType();
		bem.setPolicyInfo(makePolicyInfoType("MPGPI-5555", JAN_15, DEC_15, PolicyStatus.EFFECTUATED_2));

		MemberType inboundSubscriber = makeSubscriberMaintenance("5555", JAN_15, DEC_15);
		
		AdditionalInfoType ait = new AdditionalInfoType();
		
		ait.setEffectiveStartDate(DateTimeUtil.getXMLGregorianCalendar(esd));
		ait.setEffectiveEndDate(DateTimeUtil.getXMLGregorianCalendar(eed));
		
		setAdditionalInfoTypeValue(ait, "TPA", tpa, null);
		setAdditionalInfoTypeValue(ait, "TIRA", tira, null);
		setAdditionalInfoTypeValue(ait, "APTC", aptc, null);
		setAdditionalInfoTypeValue(ait, "CSR", csr, null);
		
		inboundSubscriber.getAdditionalInfo().add(ait);
		bem.getMember().add(inboundSubscriber);

		MemberType inboundSubscriber2 = makeSubscriberMaintenance("6666", null, null);
		
		AdditionalInfoType ait2 = new AdditionalInfoType();
		
		ait2.setEffectiveStartDate(DateTimeUtil.getXMLGregorianCalendar(esd2));
		ait2.setEffectiveEndDate(DateTimeUtil.getXMLGregorianCalendar(eed2));
		
		setAdditionalInfoTypeValue(ait2, "TPA", tpa, null);
		setAdditionalInfoTypeValue(ait2, "TIRA", tira, null);
		setAdditionalInfoTypeValue(ait2, "APTC", aptc, null);
		setAdditionalInfoTypeValue(ait2, "CSR", csr, null);
		setAdditionalInfoTypeValue(ait2, "P-TPA", tpa, proratedTpa2);
		setAdditionalInfoTypeValue(ait2, "P-TIRA", tira, proratedTira2);
		setAdditionalInfoTypeValue(ait2, "P-APTC", aptc, proratedAptc2);
		setAdditionalInfoTypeValue(ait2, "P-CSR", csr, proratedCsr2);
		
		inboundSubscriber2.getAdditionalInfo().add(ait2);
		bem.getMember().add(inboundSubscriber2);

		MemberType inboundSubscriber3 = makeSubscriberMaintenance("7777", null, null);
		
		AdditionalInfoType ait3 = new AdditionalInfoType();
		
		ait3.setEffectiveStartDate(DateTimeUtil.getXMLGregorianCalendar(esd3));
		ait3.setEffectiveEndDate(DateTimeUtil.getXMLGregorianCalendar(eed3));
		
		setAdditionalInfoTypeValue(ait3, "TPA", tpa, null);
		setAdditionalInfoTypeValue(ait3, "TIRA", tira, null);
		setAdditionalInfoTypeValue(ait3, "APTC", aptc, null);
		setAdditionalInfoTypeValue(ait3, "CSR", csr, null);
		setAdditionalInfoTypeValue(ait3, "P-TPA", tpa, proratedTpa3);
		setAdditionalInfoTypeValue(ait3, "P-TIRA", tira, proratedTira3);
		setAdditionalInfoTypeValue(ait3, "P-APTC", aptc, proratedAptc3);
		setAdditionalInfoTypeValue(ait3, "P-CSR", csr, proratedCsr3);
		
		inboundSubscriber3.getAdditionalInfo().add(ait3);
		bem.getMember().add(inboundSubscriber3);

		bemDTO.setBem(bem);

		Map<LocalDate, AdditionalInfoType> epsPremiums = ReflectionTestUtils.invokeMethod(ffmValidatorService, "processInboundPremiums", bemDTO,financialValidator);

		assertEquals("Three premium records", 3, epsPremiums.size());

		AdditionalInfoType actualPremium1 = epsPremiums.get(FEB_1);
		assertEquals("ESD", expectedESD_EPS1, DateTimeUtil.getLocalDateFromXmlGC(actualPremium1.getEffectiveStartDate()));
		assertEquals("EED", expectedEED_EPS1, DateTimeUtil.getLocalDateFromXmlGC(actualPremium1.getEffectiveEndDate()));
		assertEquals("APTC", expectedAPTC_EPS1, actualPremium1.getAPTCAmount());
		assertEquals("CSR", expectedCSR_EPS1, actualPremium1.getCSRAmount());
		assertEquals("TPA", expectedTPA_EPS1, actualPremium1.getTotalPremiumAmount());
		assertEquals("TIRA", expectedTIRA_EPS1, actualPremium1.getTotalIndividualResponsibilityAmount());

		AdditionalInfoType actualPremium2 = epsPremiums.get(esd2);
		assertEquals("ESD", esd2, DateTimeUtil.getLocalDateFromXmlGC(actualPremium2.getEffectiveStartDate()));
		assertEquals("EED", eed2, DateTimeUtil.getLocalDateFromXmlGC(actualPremium2.getEffectiveEndDate()));
		assertEquals("APTC", expectedAPTC_EPS1, actualPremium2.getAPTCAmount());
		assertEquals("CSR", expectedCSR_EPS1, actualPremium2.getCSRAmount());
		assertEquals("TPA", expectedTPA_EPS1, actualPremium2.getTotalPremiumAmount());
		assertEquals("TIRA", expectedTIRA_EPS1, actualPremium2.getTotalIndividualResponsibilityAmount());
		assertEquals("P-APTC", proratedAptc2, actualPremium2.getProratedAppliedAPTCAmount());
		assertEquals("P-CSR", proratedCsr2, actualPremium2.getProratedCSRAmount());
		assertEquals("P-TPA", proratedTpa2, actualPremium2.getProratedMonthlyPremiumAmount());
		assertEquals("P-TIRA", proratedTira2, actualPremium2.getProratedIndividualResponsibleAmount());

		AdditionalInfoType actualPremium3 = epsPremiums.get(esd3);
		assertEquals("ESD", esd3, DateTimeUtil.getLocalDateFromXmlGC(actualPremium3.getEffectiveStartDate()));
		assertEquals("EED", eed3, DateTimeUtil.getLocalDateFromXmlGC(actualPremium3.getEffectiveEndDate()));
		assertEquals("APTC", expectedAPTC_EPS1, actualPremium3.getAPTCAmount());
		assertEquals("CSR", expectedCSR_EPS1, actualPremium3.getCSRAmount());
		assertEquals("TPA", expectedTPA_EPS1, actualPremium3.getTotalPremiumAmount());
		assertEquals("TIRA", expectedTIRA_EPS1, actualPremium3.getTotalIndividualResponsibilityAmount());
		assertEquals("P-APTC", proratedAptc3, actualPremium3.getProratedAppliedAPTCAmount());
		assertEquals("P-CSR", proratedCsr3, actualPremium3.getProratedCSRAmount());
		assertEquals("P-TPA", proratedTpa3, actualPremium3.getProratedMonthlyPremiumAmount());
		assertEquals("P-TIRA", proratedTira3, actualPremium3.getProratedIndividualResponsibleAmount());

		assertEquals("Only one subscriber loops", 1, BEMDataUtil.getSubscriberOccurrances(bem).size());

		MemberType expectedSubscriber = BEMDataUtil.getSubscriberOccurrances(bem).get(0);
		assertEquals("Only one subscriber loops", inboundSubscriber.getSubscriberID(), expectedSubscriber.getSubscriberID());
	}

	/*
	 * FDD Scenario 11A, mid month start and end (second, third subscriber), policy start/end = monthly premium start/end
	 */
	
	@Test
	public void test_processInboundPremiums_mid_month_start_and_end_FFM_Premium_Start_Date_Policy_Start_Date_Aligned_1() {

		LocalDate esd = JAN_15;
		LocalDate eed = FEB_15;
		BigDecimal aptc = new BigDecimal("100");
		BigDecimal csr = new BigDecimal("50");
		BigDecimal tpa = new BigDecimal("150");
		BigDecimal tira = new BigDecimal("100");

		LocalDate esd2 = JAN_15;
		LocalDate eed2 = JAN_31;
		BigDecimal proratedAptc2 = new BigDecimal("54.84");
		BigDecimal proratedCsr2 = new BigDecimal("27.42");
		BigDecimal proratedTpa2 = new BigDecimal("82.26");
		BigDecimal proratedTira2 = new BigDecimal("54.84");

		LocalDate esd3 = FEB_1;
		LocalDate eed3 = FEB_15;
		BigDecimal proratedAptc3 = new BigDecimal("48.39");
		BigDecimal proratedCsr3 = new BigDecimal("24.19");
		BigDecimal proratedTpa3 = new BigDecimal("72.58");
		BigDecimal proratedTira3 = new BigDecimal("48.39");

		// Expected data after replacement, Record 1
		BigDecimal expectedAPTC_EPS1 = aptc;
		BigDecimal expectedCSR_EPS1 = csr;
		BigDecimal expectedTPA_EPS1 = tpa;
		BigDecimal expectedTIRA_EPS1 = tira;

		BenefitEnrollmentMaintenanceDTO bemDTO = new BenefitEnrollmentMaintenanceDTO();
		BenefitEnrollmentMaintenanceType bem = new BenefitEnrollmentMaintenanceType();
		bem.setPolicyInfo(makePolicyInfoType("MPGPI-5555", JAN_15, FEB_15, PolicyStatus.EFFECTUATED_2));

		MemberType inboundSubscriber = makeSubscriberMaintenance("5555", JAN_15, FEB_15);
		
		AdditionalInfoType ait = new AdditionalInfoType();
		
		ait.setEffectiveStartDate(DateTimeUtil.getXMLGregorianCalendar(esd));
		ait.setEffectiveEndDate(DateTimeUtil.getXMLGregorianCalendar(eed));
		
		setAdditionalInfoTypeValue(ait, "TPA", tpa, null);
		setAdditionalInfoTypeValue(ait, "TIRA", tira, null);
		setAdditionalInfoTypeValue(ait, "APTC", aptc, null);
		setAdditionalInfoTypeValue(ait, "CSR", csr, null);
		
		inboundSubscriber.getAdditionalInfo().add(ait);
		bem.getMember().add(inboundSubscriber);

		MemberType inboundSubscriber2 = makeSubscriberMaintenance("6666", null, null);
		
		AdditionalInfoType ait2 = new AdditionalInfoType();
		
		ait2.setEffectiveStartDate(DateTimeUtil.getXMLGregorianCalendar(esd2));
		ait2.setEffectiveEndDate(DateTimeUtil.getXMLGregorianCalendar(eed2));
		
		setAdditionalInfoTypeValue(ait2, "TPA", tpa, null);
		setAdditionalInfoTypeValue(ait2, "TIRA", tira, null);
		setAdditionalInfoTypeValue(ait2, "APTC", aptc, null);
		setAdditionalInfoTypeValue(ait2, "CSR", csr, null);
		setAdditionalInfoTypeValue(ait2, "P-TPA", tpa, proratedTpa2);
		setAdditionalInfoTypeValue(ait2, "P-TIRA", tira, proratedTira2);
		setAdditionalInfoTypeValue(ait2, "P-APTC", aptc, proratedAptc2);
		setAdditionalInfoTypeValue(ait2, "P-CSR", csr, proratedCsr2);
		
		inboundSubscriber2.getAdditionalInfo().add(ait2);
		bem.getMember().add(inboundSubscriber2);

		MemberType inboundSubscriber3 = makeSubscriberMaintenance("7777", null, null);
		
		AdditionalInfoType ait3 = new AdditionalInfoType();
		
		ait3.setEffectiveStartDate(DateTimeUtil.getXMLGregorianCalendar(esd3));
		ait3.setEffectiveEndDate(DateTimeUtil.getXMLGregorianCalendar(eed3));
		
		setAdditionalInfoTypeValue(ait3, "TPA", tpa, null);
		setAdditionalInfoTypeValue(ait3, "TIRA", tira, null);
		setAdditionalInfoTypeValue(ait3, "APTC", aptc, null);
		setAdditionalInfoTypeValue(ait3, "CSR", csr, null);
		setAdditionalInfoTypeValue(ait3, "P-TPA", tpa, proratedTpa3);
		setAdditionalInfoTypeValue(ait3, "P-TIRA", tira, proratedTira3);
		setAdditionalInfoTypeValue(ait3, "P-APTC", aptc, proratedAptc3);
		setAdditionalInfoTypeValue(ait3, "P-CSR", csr, proratedCsr3);
		
		inboundSubscriber3.getAdditionalInfo().add(ait3);
		
		bem.getMember().add(inboundSubscriber3);

		bemDTO.setBem(bem);

		Map<LocalDate, AdditionalInfoType> epsPremiums = ReflectionTestUtils.invokeMethod(ffmValidatorService, "processInboundPremiums", bemDTO,financialValidator);

		assertEquals("Three premium records", 2, epsPremiums.size());

		AdditionalInfoType actualPremium2 = epsPremiums.get(esd2);
		assertEquals("ESD", esd2, DateTimeUtil.getLocalDateFromXmlGC(actualPremium2.getEffectiveStartDate()));
		assertEquals("EED", eed2, DateTimeUtil.getLocalDateFromXmlGC(actualPremium2.getEffectiveEndDate()));
		assertEquals("APTC", expectedAPTC_EPS1, actualPremium2.getAPTCAmount());
		assertEquals("CSR", expectedCSR_EPS1, actualPremium2.getCSRAmount());
		assertEquals("TPA", expectedTPA_EPS1, actualPremium2.getTotalPremiumAmount());
		assertEquals("TIRA", expectedTIRA_EPS1, actualPremium2.getTotalIndividualResponsibilityAmount());
		assertEquals("P-APTC", proratedAptc2, actualPremium2.getProratedAppliedAPTCAmount());
		assertEquals("P-CSR", proratedCsr2, actualPremium2.getProratedCSRAmount());
		assertEquals("P-TPA", proratedTpa2, actualPremium2.getProratedMonthlyPremiumAmount());
		assertEquals("P-TIRA", proratedTira2, actualPremium2.getProratedIndividualResponsibleAmount());

		AdditionalInfoType actualPremium3 = epsPremiums.get(esd3);
		assertEquals("ESD", esd3, DateTimeUtil.getLocalDateFromXmlGC(actualPremium3.getEffectiveStartDate()));
		assertEquals("EED", eed3, DateTimeUtil.getLocalDateFromXmlGC(actualPremium3.getEffectiveEndDate()));
		assertEquals("APTC", expectedAPTC_EPS1, actualPremium3.getAPTCAmount());
		assertEquals("CSR", expectedCSR_EPS1, actualPremium3.getCSRAmount());
		assertEquals("TPA", expectedTPA_EPS1, actualPremium3.getTotalPremiumAmount());
		assertEquals("TIRA", expectedTIRA_EPS1, actualPremium3.getTotalIndividualResponsibilityAmount());
		assertEquals("P-APTC", proratedAptc3, actualPremium3.getProratedAppliedAPTCAmount());
		assertEquals("P-CSR", proratedCsr3, actualPremium3.getProratedCSRAmount());
		assertEquals("P-TPA", proratedTpa3, actualPremium3.getProratedMonthlyPremiumAmount());
		assertEquals("P-TIRA", proratedTira3, actualPremium3.getProratedIndividualResponsibleAmount());

		assertEquals("Only one subscriber loops", 1, BEMDataUtil.getSubscriberOccurrances(bem).size());

		MemberType expectedSubscriber = BEMDataUtil.getSubscriberOccurrances(bem).get(0);
		assertEquals("Only one subscriber loops", inboundSubscriber.getSubscriberID(), expectedSubscriber.getSubscriberID());
	}

	/*
	 * FDD Scenario 12, Intra-month start & end (second subscriber), policy start/end = monthly premium start/end
	 */
	
	@Test
	public void test_processInboundPremiums_intra_month_start_and_end_FFM_Premium_Start_Date_Policy_Start_Date_Aligned() {

		LocalDate esd = JAN_15;
		LocalDate eed = JAN_31;
		BigDecimal aptc = new BigDecimal("100");
		BigDecimal csr = new BigDecimal("50");
		BigDecimal tpa = new BigDecimal("150");
		BigDecimal tira = new BigDecimal("100");

		LocalDate esd2 = JAN_15;
		LocalDate eed2 = JAN_31;
		BigDecimal proratedAptc2 = new BigDecimal("54.84");
		BigDecimal proratedCsr2 = new BigDecimal("27.42");
		BigDecimal proratedTpa2 = new BigDecimal("82.26");
		BigDecimal proratedTira2 = new BigDecimal("54.84");

		// Expected data after replacement, Record 1
		BigDecimal expectedAPTC_EPS1 = aptc;
		BigDecimal expectedCSR_EPS1 = csr;
		BigDecimal expectedTPA_EPS1 = tpa;
		BigDecimal expectedTIRA_EPS1 = tira;

		BenefitEnrollmentMaintenanceDTO bemDTO = new BenefitEnrollmentMaintenanceDTO();
		BenefitEnrollmentMaintenanceType bem = new BenefitEnrollmentMaintenanceType();
		bem.setPolicyInfo(new PolicyInfoType());
		bem.getPolicyInfo().setPolicyStartDate(DateTimeUtil.getXMLGregorianCalendar(JAN_15));
		bem.getPolicyInfo().setPolicyEndDate(DateTimeUtil.getXMLGregorianCalendar(JAN_31));

		MemberType dependent = new MemberType();
		dependent.setMemberInformation(new MemberRelatedInfoType());
		dependent.getMemberInformation().setSubscriberIndicator(BooleanIndicatorSimpleType.N);
		bem.getMember().add(dependent);

		MemberType inboundSubscriber = makeSubscriberMaintenance("5555", JAN_15, JAN_31);
		
		AdditionalInfoType ait = new AdditionalInfoType();
		
		ait.setEffectiveStartDate(DateTimeUtil.getXMLGregorianCalendar(esd));
		ait.setEffectiveEndDate(DateTimeUtil.getXMLGregorianCalendar(eed));
		
		setAdditionalInfoTypeValue(ait, "TPA", tpa, null);
		setAdditionalInfoTypeValue(ait, "TIRA", tira, null);
		setAdditionalInfoTypeValue(ait, "APTC", aptc, null);
		setAdditionalInfoTypeValue(ait, "CSR", csr, null);
		
		inboundSubscriber.getAdditionalInfo().add(ait);
		bem.getMember().add(inboundSubscriber);

		MemberType inboundSubscriber2 = makeSubscriberMaintenance("6666", null, null);
		
		AdditionalInfoType ait2 = new AdditionalInfoType();
		
		ait2.setEffectiveStartDate(DateTimeUtil.getXMLGregorianCalendar(esd2));
		ait2.setEffectiveEndDate(DateTimeUtil.getXMLGregorianCalendar(eed2));
		
		setAdditionalInfoTypeValue(ait2, "TPA", tpa, null);
		setAdditionalInfoTypeValue(ait2, "TIRA", tira, null);
		setAdditionalInfoTypeValue(ait2, "APTC", aptc, null);
		setAdditionalInfoTypeValue(ait2, "CSR", csr, null);
		setAdditionalInfoTypeValue(ait2, "P-TPA", tpa, proratedTpa2);
		setAdditionalInfoTypeValue(ait2, "P-TIRA", tira, proratedTira2);
		setAdditionalInfoTypeValue(ait2, "P-APTC", aptc, proratedAptc2);
		setAdditionalInfoTypeValue(ait2, "P-CSR", csr, proratedCsr2);
		
		inboundSubscriber2.getAdditionalInfo().add(ait2);
		bem.getMember().add(inboundSubscriber2);

		MemberType dependent2 = new MemberType();
		dependent2.setMemberInformation(new MemberRelatedInfoType());
		dependent2.getMemberInformation().setSubscriberIndicator(BooleanIndicatorSimpleType.N);
		bem.getMember().add(dependent2);

		bemDTO.setBem(bem);

		Map<LocalDate, AdditionalInfoType> epsPremiums = ReflectionTestUtils.invokeMethod(ffmValidatorService, "processInboundPremiums", bemDTO,financialValidator);

		assertEquals("Only one premium record", 1, epsPremiums.size());

		AdditionalInfoType actualPremium1 = epsPremiums.get(FEB_1);
		assertNull("Only one premium record", actualPremium1);

		AdditionalInfoType actualPremium2 = epsPremiums.get(esd2);
		assertEquals("ESD", esd2, DateTimeUtil.getLocalDateFromXmlGC(actualPremium2.getEffectiveStartDate()));
		assertEquals("EED", eed2, DateTimeUtil.getLocalDateFromXmlGC(actualPremium2.getEffectiveEndDate()));
		assertEquals("APTC", expectedAPTC_EPS1, actualPremium2.getAPTCAmount());
		assertEquals("CSR", expectedCSR_EPS1, actualPremium2.getCSRAmount());
		assertEquals("TPA", expectedTPA_EPS1, actualPremium2.getTotalPremiumAmount());
		assertEquals("TIRA", expectedTIRA_EPS1, actualPremium2.getTotalIndividualResponsibilityAmount());
		assertEquals("P-APTC", proratedAptc2, actualPremium2.getProratedAppliedAPTCAmount());
		assertEquals("P-CSR", proratedCsr2, actualPremium2.getProratedCSRAmount());
		assertEquals("P-TPA", proratedTpa2, actualPremium2.getProratedMonthlyPremiumAmount());
		assertEquals("P-TIRA", proratedTira2, actualPremium2.getProratedIndividualResponsibleAmount());

		assertEquals("Only one subscriber loops", 1, BEMDataUtil.getSubscriberOccurrances(bem).size());

		MemberType expectedSubscriber = BEMDataUtil.getSubscriberOccurrances(bem).get(0);
		assertEquals("Only one subscriber loops", inboundSubscriber.getSubscriberID(), expectedSubscriber.getSubscriberID());

	}

	private PolicyInfoType makePolicyInfoType(String mgpi, LocalDate psd, LocalDate ped, PolicyStatus policyStatus) {

		PolicyInfoType policyInfo = new PolicyInfoType();
		policyInfo.setMarketplaceGroupPolicyIdentifier(mgpi);
		policyInfo.setPolicyStartDate(DateTimeUtil.getXMLGregorianCalendar(psd));
		policyInfo.setPolicyEndDate(DateTimeUtil.getXMLGregorianCalendar(ped));
		policyInfo.setPolicyStatus(policyStatus.getValue());
		return policyInfo;
	}
	
	private ExtractionStatusType makeExtractionStatus(BigInteger status) {

		ExtractionStatusType extractionStatus = new ExtractionStatusType();
		extractionStatus.setExtractionStatusCode(status);
		extractionStatus.setExtractionStatusText("Extraction Errored out due to connection error");;
		return extractionStatus;
	}
	
	private AdditionalInfoType setAdditionalInfoTypeValue(AdditionalInfoType ait, String type, BigDecimal amt, BigDecimal proratedAmt) {

		if (type.equals("APTC")) {
			ait.setAPTCAmount(amt); 
		} else if (type.equals("TIRA")) {
			ait.setTotalIndividualResponsibilityAmount(amt);
		} else if (type.equals("TPA")) {
			ait.setTotalPremiumAmount(amt);
		} else if (type.equals("CSR")) {
			ait.setCSRAmount(amt);
		} 
		if (type.equals("P-APTC")) {
			ait.setProratedAppliedAPTCAmount(proratedAmt);
		} else if (type.equals("P-TIRA")) {
			ait.setProratedIndividualResponsibleAmount(proratedAmt);
		} else if (type.equals("P-TPA")) {
			ait.setProratedMonthlyPremiumAmount(proratedAmt);
		} else if (type.equals("P-CSR")) {
			ait.setProratedCSRAmount(proratedAmt);
		} 		
		return ait;
	}
   
	private MemberType makeSubscriberMaintenance(String id, LocalDate policyStartDt, LocalDate policyEndDate) {

		MemberType subscriber = new MemberType();
		subscriber.setMemberInformation(new MemberRelatedInfoType());
		subscriber.getMemberInformation().setSubscriberIndicator(BooleanIndicatorSimpleType.Y);
		subscriber.setMemberAdditionalIdentifier(new MemberAdditionalIdentifierType());
		subscriber.getMemberAdditionalIdentifier().setExchangeAssignedMemberID(id);
		subscriber.setSubscriberID(id);

		subscriber.setMemberRelatedDates(new MemberRelatedDatesType());

		subscriber.getHealthCoverage().add(new HealthCoverageType());
		subscriber.getHealthCoverage().get(0).setHealthCoverageDates(new HealthCoverageDatesType());

		if (policyStartDt != null)
			subscriber.getHealthCoverage().get(0).getHealthCoverageDates().setBenefitBeginDate(DateTimeUtil.getXMLGregorianCalendar(policyStartDt));

		if (policyEndDate != null) 
			subscriber.getHealthCoverage().get(0).getHealthCoverageDates().setBenefitEndDate(DateTimeUtil.getXMLGregorianCalendar(policyEndDate));

		return subscriber;
	}
		
     
}
