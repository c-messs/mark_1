/**
 * 
 */
package gov.hhs.cms.ff.fm.eps.ep.sbm.validation;

import static gov.hhs.cms.ff.fm.eps.ep.sbm.SBMConstants.ERROR_DESC_INCORRECT_VALUE;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.springframework.test.util.ReflectionTestUtils;

import gov.cms.dsh.sbmi.FileInformationType;
import gov.cms.dsh.sbmi.PolicyType;
import gov.cms.dsh.sbmi.PolicyType.FinancialInformation;
import gov.cms.dsh.sbmi.ProratedAmountType;
import gov.hhs.cms.ff.fm.eps.ep.SBMValidationRequest;
import gov.hhs.cms.ff.fm.eps.ep.enums.SBMErrorWarningCode;
import gov.hhs.cms.ff.fm.eps.ep.sbm.SBMPolicyDTO;
import gov.hhs.cms.ff.fm.eps.ep.sbm.SbmErrWarningLogDTO;
import gov.hhs.cms.ff.fm.eps.ep.sbm.services.SBMDataService;
import gov.hhs.cms.ff.fm.eps.ep.sbm.services.impl.SBMDataServiceImpl;
import gov.hhs.cms.ff.fm.eps.ep.sbm.validation.impl.SbmBusinessValidatorImpl;
import gov.hhs.cms.ff.fm.eps.ep.sbm.validation.impl.SbmFinancialValidatorImpl;
import gov.hhs.cms.ff.fm.eps.ep.sbm.validation.impl.SbmValidationServiceImpl;
import gov.hhs.cms.ff.fm.eps.ep.sbm.validation.impl.SbmValidationUtil;
import gov.hhs.cms.ff.fm.eps.ep.util.DateTimeUtil;
import junit.framework.TestCase;

/**
 * Test class for SBMValidationServiceImpl
 * 
 * @author girish.padmanabhan
 */
@RunWith(JUnit4.class)
public class SBMValidatorTest extends TestCase {

	protected static final LocalDate DATE = LocalDate.now();
	protected static final LocalDateTime DATETIME = LocalDateTime.now();
	protected static final int YEAR = DATE.getYear();
		
	protected final LocalDate JAN_1 = LocalDate.of(YEAR, 1, 1);
	protected final LocalDate JAN_15 = LocalDate.of(YEAR, 1, 15);
	protected final LocalDate JAN_31 = LocalDate.of(YEAR, 1, 31);
	protected final LocalDate FEB_1 = LocalDate.of(YEAR, 2, 1);
	protected final LocalDate FEB_15 = LocalDate.of(YEAR, 2, 15);
	protected final LocalDate FEB_MAX = DATE.with(TemporalAdjusters.lastDayOfMonth());
	protected final LocalDate MAR_1 = LocalDate.of(YEAR, 3, 1);
	protected final LocalDate MAR_14 = LocalDate.of(YEAR, 3, 14);
	protected final LocalDate MAR_15 = LocalDate.of(YEAR, 3, 15);
	protected final LocalDate MAR_31 = LocalDate.of(YEAR, 3, 31);
	protected final LocalDate APR_1 = LocalDate.of(YEAR, 4, 1);
	protected final LocalDate APR_15 = LocalDate.of(YEAR, 4, 15);
	protected final LocalDate APR_30 = LocalDate.of(YEAR, 4, 30);
	protected final LocalDate MAY_1 = LocalDate.of(YEAR, 5, 1);
	protected final LocalDate MAY_31 = LocalDate.of(YEAR, 5, 31);
	protected final LocalDate JUN_1 = LocalDate.of(YEAR, 6, 1);
	protected final LocalDate JUN_15 = LocalDate.of(YEAR, 6, 15);
	protected final LocalDate JUN_30 = LocalDate.of(YEAR, 6, 30);
	protected final LocalDate JUL_1 = LocalDate.of(YEAR, 7, 1);
	protected final LocalDate NOV_30 = LocalDate.of(YEAR, 11, 30);
	protected final LocalDate DEC_1 = LocalDate.of(YEAR, 12, 1);
	protected final LocalDate DEC_15 = LocalDate.of(YEAR, 12, 15);
	protected final LocalDate DEC_31 = LocalDate.of(YEAR, 12, 31);


	SbmValidationServiceImpl sbmValidator;

	private SBMDataService mockSbmDataService;
	private SbmFinancialValidator mockSbmFinancialValidator;
	private SbmBusinessValidator mockSbmBusinessValidator;

	@Before
	public void setup() throws Exception {
		sbmValidator = new SbmValidationServiceImpl();

		mockSbmDataService = EasyMock.createMock(SBMDataServiceImpl.class);
		mockSbmFinancialValidator = EasyMock.createMock(SbmFinancialValidatorImpl.class);
		mockSbmBusinessValidator = EasyMock.createMock(SbmBusinessValidatorImpl.class);

		sbmValidator.setSbmDataService(mockSbmDataService);;
		sbmValidator.setSbmFinancialValidator(mockSbmFinancialValidator);
		sbmValidator.setSbmBusinessValidator(mockSbmBusinessValidator);
	}

	@Test
	public void test_validatePolicy() {
		
		SBMPolicyDTO epsPolicyDTO = new SBMPolicyDTO();

		expect(mockSbmDataService.performPolicyMatch(EasyMock.anyObject(SBMPolicyDTO.class)))
		.andReturn(epsPolicyDTO);
		replay(mockSbmDataService);		
		
		expect(mockSbmBusinessValidator.validatePolicy(EasyMock.anyObject(SBMPolicyDTO.class)))
		.andReturn(null);
		expect(mockSbmBusinessValidator.validatePolicyMembers(EasyMock.anyObject(PolicyType.class)))
		.andReturn(null);
		replay(mockSbmBusinessValidator);	
		
		expect(mockSbmFinancialValidator.validateFinancialInfo(EasyMock.anyObject(PolicyType.class)))
		.andReturn(null);
		expect(mockSbmFinancialValidator.processInboundPremiums(EasyMock.anyObject(PolicyType.class)))
		.andReturn(null);
		replay(mockSbmFinancialValidator);	
		
		SBMValidationRequest sbmValidationRequest = new SBMValidationRequest();
		
		SBMPolicyDTO policyDTO = new SBMPolicyDTO();
		PolicyType policy = new PolicyType();
		policy.setEffectuationIndicator("Y");
		policy.setPolicyStartDate(DateTimeUtil.getXMLGregorianCalendar(DATE));
		policy.setPolicyEndDate(DateTimeUtil.getXMLGregorianCalendar(DATE.plusDays(1)));
		policyDTO.setPolicy(policy);
		
		FileInformationType fileInfo = new FileInformationType();
		fileInfo.setCoverageYear(YEAR);
		policyDTO.setFileInfo(fileInfo);
		
		sbmValidationRequest.setPolicyDTO(policyDTO);
		
		sbmValidator.validatePolicy(sbmValidationRequest);
		
		assertNotNull("sbmValidationRequest", sbmValidationRequest);
		assertFalse("sbmValidationRequest", sbmValidationRequest.getPolicyDTO().isErrorFlag());
	}
	
	@Test
	public void test_validatePolicy_With_Errors() {
		
		SBMPolicyDTO epsPolicyDTO = new SBMPolicyDTO();

		expect(mockSbmDataService.performPolicyMatch(EasyMock.anyObject(SBMPolicyDTO.class)))
		.andReturn(epsPolicyDTO);
		replay(mockSbmDataService);		
		
		expect(mockSbmBusinessValidator.validatePolicy(EasyMock.anyObject(SBMPolicyDTO.class)))
		.andReturn(Arrays.asList(SbmValidationUtil.createErrorWarningLogDTO("QHPId", SBMErrorWarningCode.ER_024.getCode(), ERROR_DESC_INCORRECT_VALUE)));
		expect(mockSbmBusinessValidator.validatePolicyMembers(EasyMock.anyObject(PolicyType.class)))
		.andReturn(Arrays.asList(SbmValidationUtil.createErrorWarningLogDTO("NamePrefix", SBMErrorWarningCode.WR_004.getCode(), "")));
		replay(mockSbmBusinessValidator);	
		
		expect(mockSbmFinancialValidator.validateFinancialInfo(EasyMock.anyObject(PolicyType.class)))
		.andReturn(Arrays.asList(SbmValidationUtil.createErrorWarningLogDTO(
							"PartialMonthEffectiveEndDate", SBMErrorWarningCode.ER_045.getCode(), ERROR_DESC_INCORRECT_VALUE)));
		expect(mockSbmFinancialValidator.processInboundPremiums(EasyMock.anyObject(PolicyType.class)))
		.andReturn(null);
		replay(mockSbmFinancialValidator);	
		
		SBMValidationRequest sbmValidationRequest = new SBMValidationRequest();
		
		SBMPolicyDTO policyDTO = new SBMPolicyDTO();
		PolicyType policy = new PolicyType();
		policy.setEffectuationIndicator("Y");
		policy.setPolicyStartDate(DateTimeUtil.getXMLGregorianCalendar(DATE));
		policy.setPolicyEndDate(DateTimeUtil.getXMLGregorianCalendar(DATE.plusDays(1)));
		policyDTO.setPolicy(policy);
		
		FileInformationType fileInfo = new FileInformationType();
		fileInfo.setCoverageYear(YEAR);
		policyDTO.setFileInfo(fileInfo);
		
		sbmValidationRequest.setPolicyDTO(policyDTO);
		
		sbmValidator.validatePolicy(sbmValidationRequest);
		
		assertNotNull("sbmValidationRequest", sbmValidationRequest);
		
		assertTrue("sbmValidationRequest", sbmValidationRequest.getPolicyDTO().isErrorFlag());
	}
	
	@Test
	public void test_validatePolicy_PolicyMatched_ValidationErrors() {
		
		SBMPolicyDTO epsPolicyDTO = new SBMPolicyDTO();
		PolicyType epsPolicy = new PolicyType();
		epsPolicyDTO.setPolicyVersionId(1L);
		epsPolicyDTO.setPolicy(epsPolicy);

		expect(mockSbmDataService.performPolicyMatch(EasyMock.anyObject(SBMPolicyDTO.class)))
		.andReturn(epsPolicyDTO);
		replay(mockSbmDataService);	
		
		expect(mockSbmBusinessValidator.validatePolicy(EasyMock.anyObject(SBMPolicyDTO.class)))
		.andReturn(Arrays.asList(SbmValidationUtil.createErrorWarningLogDTO("QHPId", SBMErrorWarningCode.ER_024.getCode(), ERROR_DESC_INCORRECT_VALUE)));
		expect(mockSbmBusinessValidator.validatePolicyMembers(EasyMock.anyObject(PolicyType.class)))
		.andReturn(Arrays.asList(SbmValidationUtil.createErrorWarningLogDTO("NamePrefix", SBMErrorWarningCode.WR_004.getCode(), "")));
		expect(mockSbmBusinessValidator.validateEpsPolicy(EasyMock.anyObject(SBMPolicyDTO.class), EasyMock.anyObject(SBMPolicyDTO.class)))
		.andReturn(Arrays.asList(SbmValidationUtil.createErrorWarningLogDTO(
				"QHPId", SBMErrorWarningCode.ER_056.getCode(), ERROR_DESC_INCORRECT_VALUE)));
		replay(mockSbmBusinessValidator);	
		
		expect(mockSbmFinancialValidator.validateFinancialInfo(EasyMock.anyObject(PolicyType.class)))
		.andReturn(Arrays.asList(SbmValidationUtil.createErrorWarningLogDTO(
							"PartialMonthEffectiveEndDate", SBMErrorWarningCode.ER_045.getCode(), ERROR_DESC_INCORRECT_VALUE)));
		expect(mockSbmFinancialValidator.processInboundPremiums(EasyMock.anyObject(PolicyType.class)))
		.andReturn(null);
		replay(mockSbmFinancialValidator);	
		
		SBMValidationRequest sbmValidationRequest = new SBMValidationRequest();
		
		SBMPolicyDTO policyDTO = new SBMPolicyDTO();
		PolicyType policy = new PolicyType();
		policy.setEffectuationIndicator("Y");
		policy.setPolicyStartDate(DateTimeUtil.getXMLGregorianCalendar(DATE));
		policy.setPolicyEndDate(DateTimeUtil.getXMLGregorianCalendar(DATE.plusDays(1)));
		policyDTO.setPolicy(policy);
		
		FileInformationType fileInfo = new FileInformationType();
		fileInfo.setCoverageYear(YEAR);
		policyDTO.setFileInfo(fileInfo);
		
		sbmValidationRequest.setPolicyDTO(policyDTO);
		
		sbmValidator.validatePolicy(sbmValidationRequest);		
		
		assertTrue("sbmValidationRequest", sbmValidationRequest.getPolicyDTO().isErrorFlag());
	}
	
	@Test
	public void test_performPolicyMatchValidations_PolicyMatched() {
		
		ReflectionTestUtils.setField(sbmValidator, "errorWarningList", new ArrayList<SbmErrWarningLogDTO>());
		
		SBMPolicyDTO epsPolicyDTO = new SBMPolicyDTO();
		PolicyType epsPolicy = new PolicyType();
		epsPolicyDTO.setPolicyVersionId(1L);
		epsPolicyDTO.setPolicy(epsPolicy);

		expect(mockSbmDataService.performPolicyMatch(EasyMock.anyObject(SBMPolicyDTO.class)))
		.andReturn(epsPolicyDTO);
		replay(mockSbmDataService);		
		
		expect(mockSbmBusinessValidator.validateEpsPolicy(EasyMock.anyObject(SBMPolicyDTO.class), EasyMock.anyObject(SBMPolicyDTO.class)))
		.andReturn(null);
		replay(mockSbmBusinessValidator);	
		
		SBMPolicyDTO policyDTO = new SBMPolicyDTO();
		PolicyType policy = new PolicyType();
		policy.setEffectuationIndicator("N");
		policy.setPolicyStartDate(DateTimeUtil.getXMLGregorianCalendar(DATE));
		policy.setPolicyEndDate(DateTimeUtil.getXMLGregorianCalendar(DATE.plusDays(1)));
		policyDTO.setPolicy(policy);
		
		FileInformationType fileInfo = new FileInformationType();
		fileInfo.setCoverageYear(YEAR);
		policyDTO.setFileInfo(fileInfo);
		
		ReflectionTestUtils.invokeMethod(
				sbmValidator, "performPolicyMatchValidations", epsPolicyDTO, policyDTO);
		
		List<SbmErrWarningLogDTO> policyMatchErrors = (List<SbmErrWarningLogDTO>) ReflectionTestUtils.getField(sbmValidator, "errorWarningList");
		
		assertTrue("policyMatchErrors", policyMatchErrors.isEmpty());
	}
	
	@Test
	public void test_performPolicyMatchValidations_PolicyMatched_ValidationErrors() {
		
		ReflectionTestUtils.setField(sbmValidator, "errorWarningList", new ArrayList<SbmErrWarningLogDTO>());
		
		SBMPolicyDTO epsPolicyDTO = new SBMPolicyDTO();
		PolicyType epsPolicy = new PolicyType();
		epsPolicyDTO.setPolicyVersionId(1L);
		epsPolicyDTO.setPolicy(epsPolicy);

		expect(mockSbmDataService.performPolicyMatch(EasyMock.anyObject(SBMPolicyDTO.class)))
		.andReturn(epsPolicyDTO);
		replay(mockSbmDataService);		
		
		expect(mockSbmBusinessValidator.validateEpsPolicy(EasyMock.anyObject(SBMPolicyDTO.class), EasyMock.anyObject(SBMPolicyDTO.class)))
		.andReturn(Arrays.asList(SbmValidationUtil.createErrorWarningLogDTO(
				"QHPId", SBMErrorWarningCode.ER_056.getCode(), ERROR_DESC_INCORRECT_VALUE)));
		replay(mockSbmBusinessValidator);	
		
		SBMPolicyDTO policyDTO = new SBMPolicyDTO();
		PolicyType policy = new PolicyType();
		policy.setEffectuationIndicator("N");
		policy.setPolicyStartDate(DateTimeUtil.getXMLGregorianCalendar(DATE));
		policy.setPolicyEndDate(DateTimeUtil.getXMLGregorianCalendar(DATE.plusDays(1)));
		policyDTO.setPolicy(policy);
		
		FileInformationType fileInfo = new FileInformationType();
		fileInfo.setCoverageYear(YEAR);
		policyDTO.setFileInfo(fileInfo);
		
		ReflectionTestUtils.invokeMethod(
				sbmValidator, "performPolicyMatchValidations", epsPolicyDTO, policyDTO);
		
		List<SbmErrWarningLogDTO> policyMatchErrors = (List<SbmErrWarningLogDTO>) ReflectionTestUtils.getField(sbmValidator, "errorWarningList");
		
		assertFalse("policyMatchErrors", policyMatchErrors.isEmpty());
		
		SbmErrWarningLogDTO policyMatchError = policyMatchErrors.get(0);
		
		assertEquals("ElementInError", "QHPId", policyMatchError.getElementInError());
		assertEquals("Error Code", "ER-056", policyMatchError.getErrorWarningTypeCd());
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void test_performPolicyMatchValidations_No_PolicyMatched_UnEffectuated() {
		
		ReflectionTestUtils.setField(sbmValidator, "errorWarningList", new ArrayList<SbmErrWarningLogDTO>());
		
		SBMPolicyDTO epsPolicyDTO = new SBMPolicyDTO();

		expect(mockSbmDataService.performPolicyMatch(EasyMock.anyObject(SBMPolicyDTO.class)))
		.andReturn(epsPolicyDTO);
		replay(mockSbmDataService);		
		
		SBMPolicyDTO policyDTO = new SBMPolicyDTO();
		PolicyType policy = new PolicyType();
		policy.setEffectuationIndicator("Y");
		policy.setPolicyStartDate(DateTimeUtil.getXMLGregorianCalendar(DATE));
		policy.setPolicyEndDate(DateTimeUtil.getXMLGregorianCalendar(DATE.plusDays(1)));
		policyDTO.setPolicy(policy);
		
		FileInformationType fileInfo = new FileInformationType();
		fileInfo.setCoverageYear(YEAR);
		policyDTO.setFileInfo(fileInfo);
		
		ReflectionTestUtils.invokeMethod(
				sbmValidator, "performPolicyMatchValidations", epsPolicyDTO, policyDTO);
		
		List<SbmErrWarningLogDTO> policyMatchErrors = (List<SbmErrWarningLogDTO>) ReflectionTestUtils.getField(sbmValidator, "errorWarningList");
		
		assertTrue("policyMatchErrors", policyMatchErrors.isEmpty());
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void test_performPolicyMatchValidations_No_PolicyMatched_Effectuated() {
		
		ReflectionTestUtils.setField(sbmValidator, "errorWarningList", new ArrayList<SbmErrWarningLogDTO>());
		
		SBMPolicyDTO epsPolicyDTO = new SBMPolicyDTO();

		expect(mockSbmDataService.performPolicyMatch(EasyMock.anyObject(SBMPolicyDTO.class)))
		.andReturn(epsPolicyDTO);
		replay(mockSbmDataService);		
		
		SBMPolicyDTO policyDTO = new SBMPolicyDTO();
		PolicyType policy = new PolicyType();
		policy.setEffectuationIndicator("N");
		policy.setPolicyStartDate(DateTimeUtil.getXMLGregorianCalendar(DATE));
		policy.setPolicyEndDate(DateTimeUtil.getXMLGregorianCalendar(DATE.plusDays(1)));
		policyDTO.setPolicy(policy);
		
		FileInformationType fileInfo = new FileInformationType();
		fileInfo.setCoverageYear(YEAR);
		policyDTO.setFileInfo(fileInfo);
		
		ReflectionTestUtils.invokeMethod(
				sbmValidator, "performPolicyMatchValidations", epsPolicyDTO, policyDTO);
		
		List<SbmErrWarningLogDTO> policyMatchErrors = (List<SbmErrWarningLogDTO>) ReflectionTestUtils.getField(sbmValidator, "errorWarningList");
		
		assertFalse("policyMatchErrors", policyMatchErrors.isEmpty());
		
		SbmErrWarningLogDTO policyMatchError = policyMatchErrors.get(0);
		
		assertEquals("ElementInError", "EffectuationIndicator", policyMatchError.getElementInError());
		assertEquals("Error Code", "ER-055", policyMatchError.getErrorWarningTypeCd());
	}
	
	protected FinancialInformation setFinancialInfo(FinancialInformation fit, String type, BigDecimal amt) {

		if (type.equals("APTC")) {
			fit.setMonthlyAPTCAmount(amt); 
		} else if (type.equals("TIRA")) {
			fit.setMonthlyTotalIndividualResponsibilityAmount(amt);
		} else if (type.equals("TPA")) {
			fit.setMonthlyTotalPremiumAmount(amt);
		} else if (type.equals("CSR")) {
			fit.setMonthlyCSRAmount(amt);
		} 
		return fit;
	}
	
	protected ProratedAmountType setProratedValues(ProratedAmountType pat, String type, BigDecimal proratedAmt) {

		if (type.equals("P-APTC")) {
			pat.setPartialMonthAPTCAmount(proratedAmt);
		} else if (type.equals("P-TPA")) {
			pat.setPartialMonthPremiumAmount(proratedAmt);
		} else if (type.equals("P-CSR")) {
			pat.setPartialMonthCSRAmount(proratedAmt);
		} 		
		return pat;
	}

}
