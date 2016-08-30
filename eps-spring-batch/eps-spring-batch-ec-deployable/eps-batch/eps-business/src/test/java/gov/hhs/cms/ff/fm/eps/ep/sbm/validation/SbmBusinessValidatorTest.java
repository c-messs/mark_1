/**
 * 
 */
package gov.hhs.cms.ff.fm.eps.ep.sbm.validation;

import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang.RandomStringUtils;
import org.easymock.EasyMock;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.springframework.test.util.ReflectionTestUtils;

import gov.cms.dsh.sbmi.FileInformationType;
import gov.cms.dsh.sbmi.FileInformationType.IssuerFileInformation;
import gov.cms.dsh.sbmi.PolicyMemberType;
import gov.cms.dsh.sbmi.PolicyMemberType.MemberDates;
import gov.cms.dsh.sbmi.PolicyType;
import gov.hhs.cms.ff.fm.eps.ep.sbm.SBMCache;
import gov.hhs.cms.ff.fm.eps.ep.sbm.SBMPolicyDTO;
import gov.hhs.cms.ff.fm.eps.ep.sbm.SbmErrWarningLogDTO;
import gov.hhs.cms.ff.fm.eps.ep.sbm.services.SBMDataService;
import gov.hhs.cms.ff.fm.eps.ep.sbm.services.impl.SBMDataServiceImpl;
import gov.hhs.cms.ff.fm.eps.ep.sbm.validation.impl.SbmBusinessValidatorImpl;
import gov.hhs.cms.ff.fm.eps.ep.util.DateTimeUtil;
import junit.framework.TestCase;

/**
 * Test class for SBMValidationServiceImpl
 * 
 * @author girish.padmanabhan
 */
@RunWith(JUnit4.class)
public class SbmBusinessValidatorTest extends TestCase {

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


	private SbmBusinessValidatorImpl sbmBusinessValidator;
	private SBMDataService mockSbmDataService;

	@Before
	public void setup() throws Exception {

		sbmBusinessValidator = new SbmBusinessValidatorImpl();
		mockSbmDataService = EasyMock.createMock(SBMDataServiceImpl.class);
		
		sbmBusinessValidator.setSbmDataService(mockSbmDataService);
	}


	/*
	 * validatePolicy
	 */
	@Test
	public void test_validatePolicy() {
		
		List<String> bizRules = Arrays.asList("R034");
		
		expect(mockSbmDataService.checkQhpIdExistsForPolicyYear(EasyMock.anyString(), EasyMock.anyString()))
		.andReturn(true);
		replay(mockSbmDataService);	
		
		ReflectionTestUtils.setField(sbmBusinessValidator, "sbmBusinessRules", bizRules);

		SBMPolicyDTO policyDTO = new SBMPolicyDTO();
		PolicyType policy = new PolicyType();
		policy.setPolicyStartDate(DateTimeUtil.getXMLGregorianCalendar(DATE));
		policy.setPolicyEndDate(DateTimeUtil.getXMLGregorianCalendar(DATE.plusDays(1)));
		policy.setEffectuationIndicator("Y");
		String qhpId = RandomStringUtils.randomAlphanumeric(16).toUpperCase();
		policy.setQHPId(qhpId);
		
		policyDTO.setPolicy(policy);
		
		IssuerFileInformation issuerFileInfo = new IssuerFileInformation();
		FileInformationType fileInfo = new FileInformationType();
		fileInfo.setIssuerFileInformation(issuerFileInfo);
		
		policyDTO.setFileInfo(fileInfo);
		
		List<SbmErrWarningLogDTO> policyErrors = sbmBusinessValidator.validatePolicy(policyDTO);
		
		assertTrue("no policy Errors", policyErrors.isEmpty());

	}
	
	/*
	 * validateQhpId
	 */
	@Test
	public void test_validateQhpId_Blank_QhpId() {

		PolicyType policy = new PolicyType();
		
		List<SbmErrWarningLogDTO> qhpErrors = ReflectionTestUtils.invokeMethod(
				sbmBusinessValidator, "validateQhpId", policy, null);
		
		assertNull("no qhp Errors", qhpErrors);
	}
	
	/*
	 * validateQhpId
	 */
	@Test
	public void test_validateQhpId_Valid_QhpId() {

		List<String> bizRules = Arrays.asList("R034");
		
		expect(mockSbmDataService.checkQhpIdExistsForPolicyYear(EasyMock.anyString(), EasyMock.anyString()))
		.andReturn(true);
		replay(mockSbmDataService);	
		
		ReflectionTestUtils.setField(sbmBusinessValidator, "sbmBusinessRules", bizRules);
		
		PolicyType policy = new PolicyType();
		policy.setPolicyStartDate(DateTimeUtil.getXMLGregorianCalendar(DATE));
		
		String qhpId = RandomStringUtils.randomAlphanumeric(16).toUpperCase();
		policy.setQHPId(qhpId);
		
		List<SbmErrWarningLogDTO> qhpErrors = ReflectionTestUtils.invokeMethod(
				sbmBusinessValidator, "validateQhpId", policy, null);
		
		assertTrue("no qhp Errors", qhpErrors.isEmpty());
	}
	
	/*
	 * validateQhpId
	 */
	@Test
	public void test_validateQhpId_Valid_QhpId_Cached() {
		
		List<String> bizRules = Arrays.asList("R034");
		ReflectionTestUtils.setField(sbmBusinessValidator, "sbmBusinessRules", bizRules);

		PolicyType policy = new PolicyType();
		
		policy.setPolicyStartDate(DateTimeUtil.getXMLGregorianCalendar(DATE));
		
		String qhpId = RandomStringUtils.randomAlphanumeric(16).toUpperCase();;
		policy.setQHPId(qhpId);
		
		SBMCache.addToQhpIdMap(qhpId, String.valueOf(DATE.getYear()));
		
		SBMPolicyDTO policyDTO = new SBMPolicyDTO();
		policyDTO.setPolicy(policy);
		
		List<SbmErrWarningLogDTO> qhpErrors = ReflectionTestUtils.invokeMethod(
				sbmBusinessValidator, "validateQhpId", policy, null);
		
		assertTrue("no qhp Errors", qhpErrors.isEmpty());
	}
	
	/*
	 * validateQhpId
	 */
	@Test
	public void test_validateQhpId_InValid_QhpId() {
		
		expect(mockSbmDataService.checkQhpIdExistsForPolicyYear(EasyMock.anyString(), EasyMock.anyString()))
		.andReturn(false);
		replay(mockSbmDataService);	
		
		List<String> bizRules = Arrays.asList("R034");
		ReflectionTestUtils.setField(sbmBusinessValidator, "sbmBusinessRules", bizRules);
		
		PolicyType policy = new PolicyType();
		policy.setPolicyStartDate(DateTimeUtil.getXMLGregorianCalendar(DATE));
		
		String qhpId = RandomStringUtils.randomAlphanumeric(16).toUpperCase();
		policy.setQHPId(qhpId);
		
		List<SbmErrWarningLogDTO> qhpErrors = ReflectionTestUtils.invokeMethod(
				sbmBusinessValidator, "validateQhpId", policy, null);
		
		assertFalse("qhp Errors", qhpErrors.isEmpty());
		assertEquals("qhp Errors", 1, qhpErrors.size());
		
		SbmErrWarningLogDTO qhpError = qhpErrors.get(0);
		
		assertEquals("ElementInError", "QHPId", qhpError.getElementInError());
		assertEquals("Error Code", "ER-024", qhpError.getErrorWarningTypeCd());

	}
	
	/*
	 * validateQhpId
	 */
	@Test
	public void test_validateIssuerId_FileInfo_Null() {

		expect(mockSbmDataService.checkQhpIdExistsForPolicyYear(EasyMock.anyString(), EasyMock.anyString()))
		.andReturn(true);
		replay(mockSbmDataService);	
		
		List<String> bizRules = Arrays.asList("R036");
		ReflectionTestUtils.setField(sbmBusinessValidator, "sbmBusinessRules", bizRules);
		
		PolicyType policy = new PolicyType();
		policy.setPolicyStartDate(DateTimeUtil.getXMLGregorianCalendar(DATE));
		
		String qhpId = RandomStringUtils.randomAlphanumeric(16).toUpperCase();
		policy.setQHPId(qhpId);
		
		List<SbmErrWarningLogDTO> issuerErrors = ReflectionTestUtils.invokeMethod(
				sbmBusinessValidator, "validateQhpId", policy, null);
		
		assertTrue("no issuer id Errors", issuerErrors.isEmpty());

	}
	
	/*
	 * validateQhpId
	 */
	@Test
	public void test_validateIssuerId_IssuerId_Null() {
		
		expect(mockSbmDataService.checkQhpIdExistsForPolicyYear(EasyMock.anyString(), EasyMock.anyString()))
		.andReturn(true);
		replay(mockSbmDataService);	
		
		List<String> bizRules = Arrays.asList("R036");
		ReflectionTestUtils.setField(sbmBusinessValidator, "sbmBusinessRules", bizRules);

		
		PolicyType policy = new PolicyType();
		policy.setPolicyStartDate(DateTimeUtil.getXMLGregorianCalendar(DATE));
		
		String qhpId = RandomStringUtils.randomAlphanumeric(16).toUpperCase();
		policy.setQHPId(qhpId);
		
		IssuerFileInformation issuerFileInfo = new IssuerFileInformation();
		
		List<SbmErrWarningLogDTO> issuerErrors = ReflectionTestUtils.invokeMethod(
				sbmBusinessValidator, "validateQhpId", policy, issuerFileInfo);
		
		assertTrue("no issuer id Errors", issuerErrors.isEmpty());
	}
	
	/*
	 * validateQhpId
	 */
	@Test
	public void test_validateIssuerId_IssuerId_Invalid() {
		
		expect(mockSbmDataService.checkQhpIdExistsForPolicyYear(EasyMock.anyString(), EasyMock.anyString()))
		.andReturn(true);
		replay(mockSbmDataService);
		
		List<String> bizRules = Arrays.asList("R036");
		ReflectionTestUtils.setField(sbmBusinessValidator, "sbmBusinessRules", bizRules);
		
		PolicyType policy = new PolicyType();
		policy.setPolicyStartDate(DateTimeUtil.getXMLGregorianCalendar(DATE));
		
		String qhpId = RandomStringUtils.randomAlphabetic(16).toUpperCase();
		policy.setQHPId(qhpId);
		
		IssuerFileInformation issuerFileInfo = new IssuerFileInformation();
		String issuerId = RandomStringUtils.randomNumeric(5);
		issuerFileInfo.setIssuerId(issuerId);
		
		List<SbmErrWarningLogDTO> issuerErrors = ReflectionTestUtils.invokeMethod(
				sbmBusinessValidator, "validateQhpId", policy, issuerFileInfo);
		
		assertFalse("issuer id Errors", issuerErrors.isEmpty());
		assertEquals("issuer id Errors", 1, issuerErrors.size());
		
		SbmErrWarningLogDTO issuerError = issuerErrors.get(0);
		
		assertEquals("ElementInError", "IssuerId", issuerError.getElementInError());
		assertEquals("Error Code", "ER-025", issuerError.getErrorWarningTypeCd());

	}
	
	/*
	 * validateQhpId
	 */
	@Test
	public void test_validateIssuerId_IssuerId_Valid() {
		
		expect(mockSbmDataService.checkQhpIdExistsForPolicyYear(EasyMock.anyString(), EasyMock.anyString()))
		.andReturn(true);
		replay(mockSbmDataService);	
		
		List<String> bizRules = Arrays.asList("R036");
		ReflectionTestUtils.setField(sbmBusinessValidator, "sbmBusinessRules", bizRules);
		
		PolicyType policy = new PolicyType();
		policy.setPolicyStartDate(DateTimeUtil.getXMLGregorianCalendar(DATE));
		
		String qhpId = RandomStringUtils.randomAlphanumeric(16).toUpperCase();
		policy.setQHPId(qhpId);
		
		IssuerFileInformation issuerFileInfo = new IssuerFileInformation();
		String issuerId = qhpId.substring(0,  5);
		issuerFileInfo.setIssuerId(issuerId);
		
		List<SbmErrWarningLogDTO> issuerErrors = ReflectionTestUtils.invokeMethod(
				sbmBusinessValidator, "validateQhpId", policy, issuerFileInfo);
		
		assertTrue("no issuer id Errors", issuerErrors.isEmpty());
	}
	
	/*
	 * validateIssuerAssignedPolicyId
	 */
	@Test
	public void test_validateIssuerPolicyIdLength_null() {
		
		List<String> bizRules = Arrays.asList("R038");
		ReflectionTestUtils.setField(sbmBusinessValidator, "sbmBusinessRules", bizRules);
		
		PolicyType policy = new PolicyType();
		
		List<SbmErrWarningLogDTO> issuerPolicyIdWarning = ReflectionTestUtils.invokeMethod(
				sbmBusinessValidator, "validateIssuerFieldsLength", policy);
		
		assertTrue("no issuer policy id Errors", issuerPolicyIdWarning.isEmpty());
	}
	
	/*
	 * validateIssuerAssignedPolicyId
	 */
	@Test
	public void test_validateIssuerPolicyIdLength_NOT_GT_50() {
		
		List<String> bizRules = Arrays.asList("R038");
		ReflectionTestUtils.setField(sbmBusinessValidator, "sbmBusinessRules", bizRules);
		
		PolicyType policy = new PolicyType();
		
		String issuerPolicyId = RandomStringUtils.randomAlphanumeric(50).toUpperCase();
		policy.setIssuerAssignedPolicyId(issuerPolicyId);
		
		List<SbmErrWarningLogDTO> issuerPolicyIdWarning = ReflectionTestUtils.invokeMethod(
				sbmBusinessValidator, "validateIssuerFieldsLength", policy);
		
		assertTrue("no issuer policy id Errors", issuerPolicyIdWarning.isEmpty());
	}
	
	/*
	 * validateIssuerAssignedPolicyId
	 */
	@Test
	public void test_validateIssuerPolicyIdLength_GT_50() {
		
		List<String> bizRules = Arrays.asList("R038");
		ReflectionTestUtils.setField(sbmBusinessValidator, "sbmBusinessRules", bizRules);
		
		PolicyType policy = new PolicyType();
		
		String issuerPolicyId = RandomStringUtils.randomAlphanumeric(51).toUpperCase();
		policy.setIssuerAssignedPolicyId(issuerPolicyId);
		
		List<SbmErrWarningLogDTO> issuerPolicyIdWarnings = ReflectionTestUtils.invokeMethod(
				sbmBusinessValidator, "validateIssuerFieldsLength", policy);
		
		assertFalse("issuer policy id Errors", issuerPolicyIdWarnings.isEmpty());
		assertEquals("issuer id Errors", 1, issuerPolicyIdWarnings.size());
		
		SbmErrWarningLogDTO issuerPolicyIdWarning = issuerPolicyIdWarnings.get(0);
		
		assertEquals("ElementInError", "IssuerAssignedPolicyId", issuerPolicyIdWarning.getElementInError());
		assertEquals("Error Code", "WR-004", issuerPolicyIdWarning.getErrorWarningTypeCd());
		
		assertEquals("IssuerAssignedPolicyId length", 50, policy.getIssuerAssignedPolicyId().length());
		assertEquals("IssuerAssignedPolicyId", issuerPolicyId.substring(0, 50), policy.getIssuerAssignedPolicyId());
	}
	
	/*
	 * validateIssuerAssignedSubscriberId
	 */
	@Test
	public void test_validateIssuerSubscriberIdLength_null() {
		
		List<String> bizRules = Arrays.asList("R039");
		ReflectionTestUtils.setField(sbmBusinessValidator, "sbmBusinessRules", bizRules);
		
		PolicyType policy = new PolicyType();
		
		List<SbmErrWarningLogDTO> issuerSubscriberIdWarning = ReflectionTestUtils.invokeMethod(
				sbmBusinessValidator, "validateIssuerFieldsLength", policy);
		
		assertTrue("no issuer policy id Errors", issuerSubscriberIdWarning.isEmpty());
	}
	
	/*
	 * validateIssuerAssignedSubscriberId
	 */
	@Test
	public void test_validateIssuerSubscriberIdLength_NOT_GT_50() {
		
		List<String> bizRules = Arrays.asList("R039");
		ReflectionTestUtils.setField(sbmBusinessValidator, "sbmBusinessRules", bizRules);
		
		PolicyType policy = new PolicyType();
		
		String issuerSubscriberId = RandomStringUtils.randomAlphanumeric(50).toUpperCase();
		policy.setIssuerAssignedSubscriberId(issuerSubscriberId);
		
		List<SbmErrWarningLogDTO> issuerSubscriberIdWarning = ReflectionTestUtils.invokeMethod(
				sbmBusinessValidator, "validateIssuerFieldsLength", policy);
		
		assertTrue("no issuer Subscriber id Errors", issuerSubscriberIdWarning.isEmpty());
	}
	
	/*
	 * validateIssuerAssignedSubscriberId
	 */
	@Test
	public void test_validateIssuerSubscriberIdLength_GT_50() {
		
		List<String> bizRules = Arrays.asList("R039");
		ReflectionTestUtils.setField(sbmBusinessValidator, "sbmBusinessRules", bizRules);
		
		PolicyType policy = new PolicyType();
		
		String issuerSubscriberId = RandomStringUtils.randomAlphanumeric(51).toUpperCase();
		policy.setIssuerAssignedSubscriberId(issuerSubscriberId);
		
		System.out.println(issuerSubscriberId.length());
		
		
		List<SbmErrWarningLogDTO> issuerSubscriberIdWarnings = ReflectionTestUtils.invokeMethod(
				sbmBusinessValidator, "validateIssuerFieldsLength", policy);
		
		assertFalse("issuer Subscriber id Errors", issuerSubscriberIdWarnings.isEmpty());
		assertEquals("issuer Subscriber id Errors", 1, issuerSubscriberIdWarnings.size());
		
		SbmErrWarningLogDTO issuerSubscriberIdWarning = issuerSubscriberIdWarnings.get(0);
		
		assertEquals("ElementInError", "IssuerAssignedSubscriberId", issuerSubscriberIdWarning.getElementInError());
		assertEquals("Error Code", "WR-004", issuerSubscriberIdWarning.getErrorWarningTypeCd());

		assertEquals("IssuerAssignedSubscriberId length", 50, policy.getIssuerAssignedSubscriberId().length());
		assertEquals("IssuerAssignedSubscriberId", issuerSubscriberId.substring(0, 50), policy.getIssuerAssignedSubscriberId());
	}
	
	/*
	 * validatePolicyDates
	 */
	@Test
	public void test_validatePolicyDates_PSD_EQ_CoverageYear() {
		
		List<String> bizRules = Arrays.asList("R040");
		ReflectionTestUtils.setField(sbmBusinessValidator, "sbmBusinessRules", bizRules);
		
		PolicyType policy = new PolicyType();
		policy.setPolicyStartDate(DateTimeUtil.getXMLGregorianCalendar(DATE));
		policy.setPolicyEndDate(DateTimeUtil.getXMLGregorianCalendar(DATE.plusDays(1)));
		policy.setEffectuationIndicator("Y");
		
		int coverageYear = DATE.getYear();
		
		List<SbmErrWarningLogDTO> policyDatesErrors = ReflectionTestUtils.invokeMethod(
				sbmBusinessValidator, "validatePolicyDates", policy, coverageYear);
		
		assertTrue("Policy Dates Errors", policyDatesErrors.isEmpty());
	}
	
	/*
	 * validatePolicyDates
	 */
	@Test
	public void test_validatePolicyDates_PSD_LT_CoverageYear() {
		
		List<String> bizRules = Arrays.asList("R040");
		ReflectionTestUtils.setField(sbmBusinessValidator, "sbmBusinessRules", bizRules);
		
		PolicyType policy = new PolicyType();
		policy.setPolicyStartDate(DateTimeUtil.getXMLGregorianCalendar(DATE));
		policy.setPolicyEndDate(DateTimeUtil.getXMLGregorianCalendar(DATE.plusDays(1)));
		policy.setEffectuationIndicator("Y");
		
		int coverageYear = DATE.getYear() + 1;
		
		List<SbmErrWarningLogDTO> policyDatesErrors = ReflectionTestUtils.invokeMethod(
				sbmBusinessValidator, "validatePolicyDates", policy, coverageYear);
		
		assertTrue("Policy Dates Errors", policyDatesErrors.isEmpty());
	}
	
	/*
	 * validatePolicyDates
	 */
	@Test
	public void test_validatePolicyDates_PSD_GT_CoverageYear() {
		
		List<String> bizRules = Arrays.asList("R040");
		ReflectionTestUtils.setField(sbmBusinessValidator, "sbmBusinessRules", bizRules);
		
		PolicyType policy = new PolicyType();
		policy.setPolicyStartDate(DateTimeUtil.getXMLGregorianCalendar(DATE.plusYears(1)));
		policy.setPolicyEndDate(DateTimeUtil.getXMLGregorianCalendar(DATE.plusYears(1).plusDays(1)));
		policy.setEffectuationIndicator("Y");
		
		int coverageYear = DATE.getYear();
		
		List<SbmErrWarningLogDTO> policyDatesErrors = ReflectionTestUtils.invokeMethod(
				sbmBusinessValidator, "validatePolicyDates", policy, coverageYear);
		
		assertFalse("Policy Dates Errors", policyDatesErrors.isEmpty());
		assertEquals("Policy Date Errors", 1, policyDatesErrors.size());
		
		SbmErrWarningLogDTO policyDatesError = policyDatesErrors.get(0);
		
		assertEquals("ElementInError", "PolicyStartDate", policyDatesError.getElementInError());
		assertEquals("Error Code", "ER-026", policyDatesError.getErrorWarningTypeCd());
	}
	
	/*
	 * validatePolicyDates
	 */
	@Test
	public void test_validatePolicyDates_EFF_IND_N_PED_LTEQ_PSD() {
		
		List<String> bizRules = Arrays.asList("R042");
		ReflectionTestUtils.setField(sbmBusinessValidator, "sbmBusinessRules", bizRules);
		
		PolicyType policy = new PolicyType();
		policy.setPolicyStartDate(DateTimeUtil.getXMLGregorianCalendar(DATE));
		policy.setPolicyEndDate(DateTimeUtil.getXMLGregorianCalendar(DATE.minusDays(1)));
		policy.setEffectuationIndicator("N");
		
		int coverageYear = DATE.getYear();
		
		List<SbmErrWarningLogDTO> policyDatesErrors = ReflectionTestUtils.invokeMethod(
				sbmBusinessValidator, "validatePolicyDates", policy, coverageYear);
		
		assertTrue("Policy Dates Errors", policyDatesErrors.isEmpty());
	}
	
	/*
	 * validatePolicyDates
	 */
	@Test
	public void test_validatePolicyDates_EFF_IND_N_PED_GT_PSD() {
		
		List<String> bizRules = Arrays.asList("R042");
		ReflectionTestUtils.setField(sbmBusinessValidator, "sbmBusinessRules", bizRules);
		
		PolicyType policy = new PolicyType();
		policy.setPolicyStartDate(DateTimeUtil.getXMLGregorianCalendar(DATE));
		policy.setPolicyEndDate(DateTimeUtil.getXMLGregorianCalendar(DATE.plusDays(1)));
		policy.setEffectuationIndicator("N");
		
		int coverageYear = DATE.getYear();
		
		List<SbmErrWarningLogDTO> policyDatesErrors = ReflectionTestUtils.invokeMethod(
				sbmBusinessValidator, "validatePolicyDates", policy, coverageYear);
		
		assertFalse("Policy Dates Errors", policyDatesErrors.isEmpty());
		assertEquals("Policy Date Errors", 1, policyDatesErrors.size());
		
		SbmErrWarningLogDTO policyDatesError = policyDatesErrors.get(0);
		
		assertEquals("ElementInError", "PolicyEndDate", policyDatesError.getElementInError());
		assertEquals("Error Code", "ER-027", policyDatesError.getErrorWarningTypeCd());
	}
	
	/*
	 * validatePolicyDates
	 */
	@Test
	public void test_validatePolicyDates_EFF_IND_Y_PED_GT_PSD() {
		
		List<String> bizRules = Arrays.asList("R043");
		ReflectionTestUtils.setField(sbmBusinessValidator, "sbmBusinessRules", bizRules);
		
		PolicyType policy = new PolicyType();
		policy.setPolicyStartDate(DateTimeUtil.getXMLGregorianCalendar(DATE));
		policy.setPolicyEndDate(DateTimeUtil.getXMLGregorianCalendar(DATE.plusDays(1)));
		policy.setEffectuationIndicator("Y");
		
		int coverageYear = DATE.getYear();
		
		List<SbmErrWarningLogDTO> policyDatesErrors = ReflectionTestUtils.invokeMethod(
				sbmBusinessValidator, "validatePolicyDates", policy, coverageYear);
		
		assertTrue("Policy Dates Errors", policyDatesErrors.isEmpty());
	}
	
	/*
	 * validatePolicyDates
	 */
	@Test
	public void test_validatePolicyDates_EFF_IND_Y_PED_EQ_PSD() {
		
		List<String> bizRules = Arrays.asList("R043");
		ReflectionTestUtils.setField(sbmBusinessValidator, "sbmBusinessRules", bizRules);
		
		PolicyType policy = new PolicyType();
		policy.setPolicyStartDate(DateTimeUtil.getXMLGregorianCalendar(DATE));
		policy.setPolicyEndDate(DateTimeUtil.getXMLGregorianCalendar(DATE));
		policy.setEffectuationIndicator("Y");
		
		int coverageYear = DATE.getYear();
		
		List<SbmErrWarningLogDTO> policyDatesErrors = ReflectionTestUtils.invokeMethod(
				sbmBusinessValidator, "validatePolicyDates", policy, coverageYear);
		
		assertTrue("Policy Dates Errors", policyDatesErrors.isEmpty());
	}
	
	/*
	 * validatePolicyDates
	 */
	@Test
	public void test_validatePolicyDates_EFF_IND_Y_PED_LT_PSD() {
		
		List<String> bizRules = Arrays.asList("R043");
		ReflectionTestUtils.setField(sbmBusinessValidator, "sbmBusinessRules", bizRules);
		
		PolicyType policy = new PolicyType();
		policy.setPolicyStartDate(DateTimeUtil.getXMLGregorianCalendar(DATE));
		policy.setPolicyEndDate(DateTimeUtil.getXMLGregorianCalendar(DATE.minusDays(1)));
		policy.setEffectuationIndicator("Y");
		
		int coverageYear = DATE.getYear();
		
		List<SbmErrWarningLogDTO> policyDatesErrors = ReflectionTestUtils.invokeMethod(
				sbmBusinessValidator, "validatePolicyDates", policy, coverageYear);
		
		assertFalse("Policy Dates Errors", policyDatesErrors.isEmpty());
		assertEquals("Policy Date Errors", 1, policyDatesErrors.size());
		
		SbmErrWarningLogDTO policyDatesError = policyDatesErrors.get(0);
		
		assertEquals("ElementInError", "PolicyEndDate", policyDatesError.getElementInError());
		assertEquals("Error Code", "ER-062", policyDatesError.getErrorWarningTypeCd());
	}
	
	/*
	 * validatePolicyMembers
	 */
	@Test
	public void test_validatePolicyMembers() {
		
		List<String> bizRules = Arrays.asList("R044");
		ReflectionTestUtils.setField(sbmBusinessValidator, "sbmBusinessRules", bizRules);

		PolicyType policy = new PolicyType();
		
		PolicyMemberType subscriber = new PolicyMemberType();
		subscriber.setSubscriberIndicator("Y");
		subscriber.setNonCoveredSubscriberInd("Y");
		policy.getMemberInformation().add(subscriber);
		
/*		PolicyMemberType dependent = new PolicyMemberType();
		dependent.setSubscriberIndicator("N");
		//dependent.setNonCoveredSubscriberInd("N");
		policy.getMemberInformation().add(dependent);*/
		
		List<SbmErrWarningLogDTO> policyMemErrors = sbmBusinessValidator.validatePolicyMembers(policy);
		
		assertTrue("no policy Member Errors", policyMemErrors.isEmpty());
	}
	
	/*
	 * validateNumSubscribers
	 */
	@Test
	public void test_validateNumSubscribers_Single_Subscriber() {
		
		List<String> bizRules = Arrays.asList("R045");
		ReflectionTestUtils.setField(sbmBusinessValidator, "sbmBusinessRules", bizRules);

		PolicyType policy = new PolicyType();
		
		PolicyMemberType subscriber = new PolicyMemberType();
		subscriber.setSubscriberIndicator("Y");
		policy.getMemberInformation().add(subscriber);
		
		List<SbmErrWarningLogDTO> numSubscriberErrors = ReflectionTestUtils.invokeMethod(
				sbmBusinessValidator, "validateNumSubscribers", policy);
		
		assertTrue("numSubscriberErrors", numSubscriberErrors.isEmpty());
	}
	
	/*
	 * validateNumSubscribers
	 */
	@Test
	public void test_validateNumSubscribers_No_Subscriber() {
		
		List<String> bizRules = Arrays.asList("R044");
		ReflectionTestUtils.setField(sbmBusinessValidator, "sbmBusinessRules", bizRules);

		PolicyType policy = new PolicyType();
		
		List<SbmErrWarningLogDTO> numSubscriberErrors = ReflectionTestUtils.invokeMethod(
				sbmBusinessValidator, "validateNumSubscribers", policy);
		
		assertFalse("numSubscriberErrors", numSubscriberErrors.isEmpty());
		
		assertEquals("numSubscriberErrors size", 1, numSubscriberErrors.size());
		
		SbmErrWarningLogDTO numSubscriberError = numSubscriberErrors.get(0);
		
		assertEquals("ElementInError", "Subscriber", numSubscriberError.getElementInError());
		assertEquals("Error Code", "ER-060", numSubscriberError.getErrorWarningTypeCd());
	}
	
	/*
	 * validateNumSubscribers
	 */
	@Test
	public void test_validateNumSubscribers_Multiple_Subscribers() {
		
		List<String> bizRules = Arrays.asList("R045");
		ReflectionTestUtils.setField(sbmBusinessValidator, "sbmBusinessRules", bizRules);

		PolicyType policy = new PolicyType();
		
		PolicyMemberType subscriber = new PolicyMemberType();
		subscriber.setSubscriberIndicator("Y");
		policy.getMemberInformation().add(subscriber);
		
		PolicyMemberType subscriber2 = new PolicyMemberType();
		subscriber2.setSubscriberIndicator("Y");
		policy.getMemberInformation().add(subscriber2);
		
		List<SbmErrWarningLogDTO> numSubscriberErrors = ReflectionTestUtils.invokeMethod(
				sbmBusinessValidator, "validateNumSubscribers", policy);
		
		assertFalse("numSubscriberErrors", numSubscriberErrors.isEmpty());
		
		assertEquals("numSubscriberErrors size", 1, numSubscriberErrors.size());
		
		SbmErrWarningLogDTO numSubscriberError = numSubscriberErrors.get(0);
		
		assertEquals("ElementInError", "Subscriber", numSubscriberError.getElementInError());
		assertEquals("Error Code", "ER-061", numSubscriberError.getErrorWarningTypeCd());
	}
	
	/*
	 * validateIssuerAssignedMemberId length
	 */
	@Test
	public void test_validateMemberLevelFieldLength_IssuerAssignedMemberId_GT_50() {
		
		List<String> bizRules = Arrays.asList("R048");
		ReflectionTestUtils.setField(sbmBusinessValidator, "sbmBusinessRules", bizRules);

		PolicyType policy = new PolicyType();
		
		PolicyMemberType subscriber = new PolicyMemberType();
		subscriber.setSubscriberIndicator("Y");
		policy.getMemberInformation().add(subscriber);
		
		PolicyMemberType member = new PolicyMemberType();
		
		String issuerMemberId = RandomStringUtils.randomAlphanumeric(51).toUpperCase();
		member.setIssuerAssignedMemberId(issuerMemberId);
		policy.getMemberInformation().add(member);
		
		List<SbmErrWarningLogDTO> issuerMemberIdlengthErrors = ReflectionTestUtils.invokeMethod(
				sbmBusinessValidator, "validateMemberLevelFieldLength", member);
		
		assertFalse("issuerMemberIdlengthErrors", issuerMemberIdlengthErrors.isEmpty());
		assertEquals("issuerMemberIdlengthErrors size", 1, issuerMemberIdlengthErrors.size());
		
		SbmErrWarningLogDTO issuerMemberIdlengthError = issuerMemberIdlengthErrors.get(0);
		
		assertEquals("ElementInError", "IssuerAssignedMemberId", issuerMemberIdlengthError.getElementInError());
		assertEquals("Error Code", "WR-004", issuerMemberIdlengthError.getErrorWarningTypeCd());
		
		assertEquals("IssuerAssignedMemberId length", 50, member.getIssuerAssignedMemberId().length());
		assertEquals("IssuerAssignedMemberId", issuerMemberId.substring(0,  50), member.getIssuerAssignedMemberId());
	}
	
	/*
	 * validateIssuerAssignedMemberId length
	 */
	@Test
	public void test_validateMemberLevelFieldLength_IssuerAssignedMemberId_Not_GT_50() {
		
		List<String> bizRules = Arrays.asList("R048");
		ReflectionTestUtils.setField(sbmBusinessValidator, "sbmBusinessRules", bizRules);

		PolicyMemberType member = new PolicyMemberType();
		
		String issuerMemberId = RandomStringUtils.randomAlphanumeric(50).toUpperCase();
		member.setIssuerAssignedMemberId(issuerMemberId);
		
		List<SbmErrWarningLogDTO> issuerMemberIdlengthErrors = ReflectionTestUtils.invokeMethod(
				sbmBusinessValidator, "validateMemberLevelFieldLength", member);
		
		assertTrue("issuerMemberIdlengthErrors", issuerMemberIdlengthErrors.isEmpty());
	}
	
	/*
	 * validate Member NamePrefix length
	 */
	@Test
	public void test_validateMemberLevelFieldLength_NamePrefix_GT_10() {
		
		List<String> bizRules = Arrays.asList("R049");
		ReflectionTestUtils.setField(sbmBusinessValidator, "sbmBusinessRules", bizRules);

		PolicyType policy = new PolicyType();
		
		PolicyMemberType subscriber = new PolicyMemberType();
		subscriber.setSubscriberIndicator("Y");
		policy.getMemberInformation().add(subscriber);
		
		PolicyMemberType member = new PolicyMemberType();
		
		String namePrefix = RandomStringUtils.randomAlphanumeric(11).toUpperCase();
		member.setNamePrefix(namePrefix);
		policy.getMemberInformation().add(member);
		
		List<SbmErrWarningLogDTO> namePrefixLengthErrors = ReflectionTestUtils.invokeMethod(
				sbmBusinessValidator, "validateMemberLevelFieldLength", member);
		
		assertFalse("NamePrefixlengthErrors", namePrefixLengthErrors.isEmpty());
		assertEquals("NamePrefixlengthErrors size", 1, namePrefixLengthErrors.size());
		
		SbmErrWarningLogDTO namePrefixLengthError = namePrefixLengthErrors.get(0);
		
		assertEquals("ElementInError", "NamePrefix", namePrefixLengthError.getElementInError());
		assertEquals("Error Code", "WR-004", namePrefixLengthError.getErrorWarningTypeCd());
		
		assertEquals("NamePrefix length", 10, member.getNamePrefix().length());
		assertEquals("Name Prefix", namePrefix.substring(0, 10), member.getNamePrefix());
	}
	
	/*
	 * validate Member NamePrefix length
	 */
	@Test
	public void test_validateMemberLevelFieldLength_NamePrefix_Not_GT_10() {
		
		List<String> bizRules = Arrays.asList("R049");
		ReflectionTestUtils.setField(sbmBusinessValidator, "sbmBusinessRules", bizRules);

		PolicyMemberType member = new PolicyMemberType();
		
		String namePrefix = RandomStringUtils.randomAlphanumeric(10).toUpperCase();
		member.setNamePrefix(namePrefix);
		
		List<SbmErrWarningLogDTO> namePrefixLengthErrors = ReflectionTestUtils.invokeMethod(
				sbmBusinessValidator, "validateMemberLevelFieldLength", member);
		
		assertTrue("namePrefixlengthErrors", namePrefixLengthErrors.isEmpty());
	}
	
	/*
	 * validate Member FirstName length
	 */
	@Test
	public void test_validateMemberLevelFieldLength_FirstName_GT_35() {

		List<String> bizRules = Arrays.asList("R065");
		ReflectionTestUtils.setField(sbmBusinessValidator, "sbmBusinessRules", bizRules);
		
		PolicyMemberType subscriber = new PolicyMemberType();
		subscriber.setSubscriberIndicator("Y");
		
		PolicyMemberType member = new PolicyMemberType();
		
		String firstName = RandomStringUtils.randomAlphanumeric(37).toUpperCase();
		member.setMemberFirstName(firstName);
		
		List<SbmErrWarningLogDTO> firstNameLengthErrors = ReflectionTestUtils.invokeMethod(
				sbmBusinessValidator, "validateMemberLevelFieldLength", member);
		
		assertFalse("FirstNameLengthErrors", firstNameLengthErrors.isEmpty());
		assertEquals("FirstNameLengthErrors size", 1, firstNameLengthErrors.size());
		
		SbmErrWarningLogDTO firstNameLengthError = firstNameLengthErrors.get(0);
		
		assertEquals("ElementInError", "MemberFirstName", firstNameLengthError.getElementInError());
		assertEquals("Error Code", "WR-004", firstNameLengthError.getErrorWarningTypeCd());
		
		assertEquals("NamePrefix length", 35, member.getMemberFirstName().length());
		assertEquals("Name Prefix", firstName.substring(0, 35), member.getMemberFirstName());
	}
	
	/*
	 * validate Member FirstName length
	 */
	@Test
	public void test_validateMemberLevelFieldLength_FirstName_Not_GT_35() {
		
		List<String> bizRules = Arrays.asList("R065");
		ReflectionTestUtils.setField(sbmBusinessValidator, "sbmBusinessRules", bizRules);
		
		PolicyMemberType member = new PolicyMemberType();
		
		String firstName = RandomStringUtils.randomAlphanumeric(34).toUpperCase();
		member.setMemberFirstName(firstName);
		
		List<SbmErrWarningLogDTO> firstNameLengthErrors = ReflectionTestUtils.invokeMethod(
				sbmBusinessValidator, "validateMemberLevelFieldLength", member);
		
		assertTrue("FirstNameLengthErrors", firstNameLengthErrors.isEmpty());
	}
	
	/*
	 * validate Member MiddleName length
	 */
	@Test
	public void test_validateMemberLevelFieldLength_MiddleName_GT_25() {
		
		List<String> bizRules = Arrays.asList("R050");
		ReflectionTestUtils.setField(sbmBusinessValidator, "sbmBusinessRules", bizRules);

		PolicyMemberType subscriber = new PolicyMemberType();
		subscriber.setSubscriberIndicator("Y");
		
		PolicyMemberType member = new PolicyMemberType();
		
		String middleName = RandomStringUtils.randomAlphanumeric(26).toUpperCase();
		member.setMemberMiddleName(middleName);
		
		List<SbmErrWarningLogDTO> middleNameLengthErrors = ReflectionTestUtils.invokeMethod(
				sbmBusinessValidator, "validateMemberLevelFieldLength", member);
		
		assertFalse("MiddleNameLengthErrors", middleNameLengthErrors.isEmpty());
		assertEquals("MiddleNameLengthErrors size", 1, middleNameLengthErrors.size());
		
		SbmErrWarningLogDTO middleNameLengthError = middleNameLengthErrors.get(0);
		
		assertEquals("ElementInError", "MemberMiddleName", middleNameLengthError.getElementInError());
		assertEquals("Error Code", "WR-004", middleNameLengthError.getErrorWarningTypeCd());
		
		assertEquals("NamePrefix length", 25, member.getMemberMiddleName().length());
		assertEquals("Name Prefix", middleName.substring(0, 25), member.getMemberMiddleName());
	}
	
	/*
	 * validate Member MiddleName length
	 */
	@Test
	public void test_validateMemberLevelFieldLength_MiddleName_Not_GT_25() {
		
		List<String> bizRules = Arrays.asList("R050");
		ReflectionTestUtils.setField(sbmBusinessValidator, "sbmBusinessRules", bizRules);

		PolicyMemberType member = new PolicyMemberType();
		
		String middleName = RandomStringUtils.randomAlphanumeric(25).toUpperCase();
		member.setMemberMiddleName(middleName);
		
		List<SbmErrWarningLogDTO> middleNameLengthErrors = ReflectionTestUtils.invokeMethod(
				sbmBusinessValidator, "validateMemberLevelFieldLength", member);
		
		assertTrue("MiddleNameLengthErrors", middleNameLengthErrors.isEmpty());
	}
	
	/*
	 * validate Member NamePrefix length
	 */
	@Test
	public void test_validateMemberLevelFieldLength_NameSuffix_GT_10() {
		
		List<String> bizRules = Arrays.asList("R051");
		ReflectionTestUtils.setField(sbmBusinessValidator, "sbmBusinessRules", bizRules);

		PolicyType policy = new PolicyType();
		
		PolicyMemberType subscriber = new PolicyMemberType();
		subscriber.setSubscriberIndicator("Y");
		policy.getMemberInformation().add(subscriber);
		
		PolicyMemberType member = new PolicyMemberType();
		
		String nameSuffix = RandomStringUtils.randomAlphanumeric(11).toUpperCase();
		member.setNameSuffix(nameSuffix);
		policy.getMemberInformation().add(member);
		
		List<SbmErrWarningLogDTO> nameSuffixLengthErrors = ReflectionTestUtils.invokeMethod(
				sbmBusinessValidator, "validateMemberLevelFieldLength", member);
		
		assertFalse("NameSuffixlengthErrors", nameSuffixLengthErrors.isEmpty());
		assertEquals("NameSuffixlengthErrors size", 1, nameSuffixLengthErrors.size());
		
		SbmErrWarningLogDTO nameSuffixLengthError = nameSuffixLengthErrors.get(0);
		
		assertEquals("ElementInError", "NameSuffix", nameSuffixLengthError.getElementInError());
		assertEquals("Error Code", "WR-004", nameSuffixLengthError.getErrorWarningTypeCd());
		
		assertEquals("NameSuffix length", 10, member.getNameSuffix().length());
		assertEquals("Name Suffix", nameSuffix.substring(0, 10), member.getNameSuffix());
	}
	
	/*
	 * validate Member NameSuffix length
	 */
	@Test
	public void test_validateMemberLevelFieldLength_NameSuffix_Not_GT_10() {
		
		List<String> bizRules = Arrays.asList("R051");
		ReflectionTestUtils.setField(sbmBusinessValidator, "sbmBusinessRules", bizRules);

		PolicyMemberType member = new PolicyMemberType();
		
		String nameSuffix = RandomStringUtils.randomAlphanumeric(10).toUpperCase();
		member.setNameSuffix(nameSuffix);
		
		List<SbmErrWarningLogDTO> nameSuffixLengthErrors = ReflectionTestUtils.invokeMethod(
				sbmBusinessValidator, "validateMemberLevelFieldLength", member);
		
		assertTrue("nameSuffixlengthErrors", nameSuffixLengthErrors.isEmpty());
	}
	
	/*
	 * validateOptionalFieldValues
	 */
	@Test
	public void test_validateOptionalFieldValues_Ssn_Invalid() {
		
		List<String> bizRules = Arrays.asList("R053");
		ReflectionTestUtils.setField(sbmBusinessValidator, "sbmBusinessRules", bizRules);

		PolicyMemberType member = new PolicyMemberType();
		
		String ssn = RandomStringUtils.randomNumeric(10);
		member.setSocialSecurityNumber(ssn);
		
		List<SbmErrWarningLogDTO> ssnFormatErrors = ReflectionTestUtils.invokeMethod(
				sbmBusinessValidator, "validateOptionalFieldValues", member);
		
		assertFalse("ssnFormatErrors", ssnFormatErrors.isEmpty());
		assertEquals("ssnFormatErrors size", 1, ssnFormatErrors.size());
		
		SbmErrWarningLogDTO ssnFormatError = ssnFormatErrors.get(0);
		
		assertEquals("ElementInError", "SocialSecurityNumber", ssnFormatError.getElementInError());
		assertEquals("Error Code", "WR-005", ssnFormatError.getErrorWarningTypeCd());
		
		assertNull("ssn", member.getSocialSecurityNumber());
	}
	
	/*
	 * validateOptionalFieldValues
	 */
	@Test
	public void test_validateOptionalFieldValues_Ssn_Valid() {
		
		List<String> bizRules = Arrays.asList("R053");
		ReflectionTestUtils.setField(sbmBusinessValidator, "sbmBusinessRules", bizRules);

		PolicyMemberType member = new PolicyMemberType();
		
		String ssn = RandomStringUtils.randomNumeric(9);
		member.setSocialSecurityNumber(ssn);
		
		List<SbmErrWarningLogDTO> ssnFormatErrors = ReflectionTestUtils.invokeMethod(
				sbmBusinessValidator, "validateOptionalFieldValues", member);
		
		assertTrue("ssnFormatErrors", ssnFormatErrors.isEmpty());
	}
	
	/*
	 * validateOptionalFieldValues
	 */
	@Test
	public void test_validateOptionalFieldValues_LanguageQualifierCode_Invalid() {
		
		List<String> bizRules = Arrays.asList("R054");
		ReflectionTestUtils.setField(sbmBusinessValidator, "sbmBusinessRules", bizRules);

		PolicyMemberType member = new PolicyMemberType();
		
		String languageQualifierCd = "ZZ";
		member.setLanguageQualifierCode(languageQualifierCd);
		
		List<SbmErrWarningLogDTO> langQualifierCdErrors = ReflectionTestUtils.invokeMethod(
				sbmBusinessValidator, "validateOptionalFieldValues", member);
		
		assertFalse("ssnFormatErrors", langQualifierCdErrors.isEmpty());
		assertEquals("ssnFormatErrors size", 1, langQualifierCdErrors.size());
		
		SbmErrWarningLogDTO langQualifierCdError = langQualifierCdErrors.get(0);
		
		assertEquals("ElementInError", "LanguageQualifierCode", langQualifierCdError.getElementInError());
		assertEquals("Error Code", "WR-005", langQualifierCdError.getErrorWarningTypeCd());
		
		assertNull("LanguageQualifierCode", member.getLanguageQualifierCode());
	}
	
	/*
	 * validateOptionalFieldValues
	 */
	@Test
	public void test_validateOptionalFieldValues_LanguageQualifierCode_Valid_LD() {
		
		List<String> bizRules = Arrays.asList("R054");
		ReflectionTestUtils.setField(sbmBusinessValidator, "sbmBusinessRules", bizRules);

		PolicyMemberType member = new PolicyMemberType();
		
		String languageQualifierCd = "LD";
		member.setLanguageQualifierCode(languageQualifierCd);
		
		List<SbmErrWarningLogDTO> langQualifierCdErrors = ReflectionTestUtils.invokeMethod(
				sbmBusinessValidator, "validateOptionalFieldValues", member);
		
		assertTrue("langQualifierCdErrors", langQualifierCdErrors.isEmpty());
	}
	
	/*
	 * validateOptionalFieldValues
	 */
	@Test
	public void test_validateOptionalFieldValues_LanguageQualifierCode_Valid_LE() {
		
		List<String> bizRules = Arrays.asList("R054");
		ReflectionTestUtils.setField(sbmBusinessValidator, "sbmBusinessRules", bizRules);

		PolicyMemberType member = new PolicyMemberType();
		
		String languageQualifierCd = "LE";
		member.setLanguageQualifierCode(languageQualifierCd);
		
		List<SbmErrWarningLogDTO> langQualifierCdErrors = ReflectionTestUtils.invokeMethod(
				sbmBusinessValidator, "validateOptionalFieldValues", member);
		
		assertTrue("langQualifierCdErrors", langQualifierCdErrors.isEmpty());
	}
	
	/*
	 * validateOptionalFieldValues
	 */
	@Test
	public void test_validateOptionalFieldValues_LanguageCode_Invalid() {
		
		List<String> bizRules = Arrays.asList("R055");
		ReflectionTestUtils.setField(sbmBusinessValidator, "sbmBusinessRules", bizRules);

		PolicyMemberType member = new PolicyMemberType();
		
		String languageCd = "ZZ";
		member.setLanguageCode(languageCd);
		
		List<SbmErrWarningLogDTO> languageCdCdErrors = ReflectionTestUtils.invokeMethod(
				sbmBusinessValidator, "validateOptionalFieldValues", member);
		
		assertFalse("LanguageCdErrors", languageCdCdErrors.isEmpty());
		assertEquals("LanguageCdErrors size", 1, languageCdCdErrors.size());
		
		SbmErrWarningLogDTO languageCdCdError = languageCdCdErrors.get(0);
		
		assertEquals("ElementInError", "LanguageCode", languageCdCdError.getElementInError());
		assertEquals("Error Code", "WR-005", languageCdCdError.getErrorWarningTypeCd());
		
		assertNull("LanguageCd", member.getLanguageCode());
	}
	
	/*
	 * validateOptionalFieldValues
	 */
	@Test
	public void test_validateOptionalFieldValues_LanguageCode_Valid() {
		
		List<String> bizRules = Arrays.asList("R055");
		ReflectionTestUtils.setField(sbmBusinessValidator, "sbmBusinessRules", bizRules);

		PolicyMemberType member = new PolicyMemberType();
		
		//T
		String languageCd = "All";
		member.setLanguageCode(languageCd);
		
		SBMCache.getLanguageCodes().add(languageCd);
		
		List<SbmErrWarningLogDTO> languageCdErrors = ReflectionTestUtils.invokeMethod(
				sbmBusinessValidator, "validateOptionalFieldValues", member);
		
		assertTrue("LanguageCdErrors", languageCdErrors.isEmpty());
		
		//Null
		languageCd = null;
		member.setLanguageCode(languageCd);
		
		languageCdErrors = ReflectionTestUtils.invokeMethod(
				sbmBusinessValidator, "validateOptionalFieldValues", member);
		
		assertTrue("raceEthnicityCdErrors", languageCdErrors.isEmpty());
	}
	
	
	/*
	 * validateOptionalFieldValues
	 */
	@Test
	public void test_validateOptionalFieldValues_GenderCode_Invalid() {
		
		List<String> bizRules = Arrays.asList("R056");
		ReflectionTestUtils.setField(sbmBusinessValidator, "sbmBusinessRules", bizRules);

		PolicyMemberType member = new PolicyMemberType();
		
		String genderCd = "ZZ";
		member.setGenderCode(genderCd);
		
		List<SbmErrWarningLogDTO> genderCdErrors = ReflectionTestUtils.invokeMethod(
				sbmBusinessValidator, "validateOptionalFieldValues", member);
		
		assertFalse("GenderCdErrors", genderCdErrors.isEmpty());
		assertEquals("GenderCdErrors size", 1, genderCdErrors.size());
		
		SbmErrWarningLogDTO genderCdError = genderCdErrors.get(0);
		
		assertEquals("ElementInError", "GenderCode", genderCdError.getElementInError());
		assertEquals("Error Code", "WR-005", genderCdError.getErrorWarningTypeCd());
		
		assertNull("GenderCode", member.getGenderCode());
	}
	
	/*
	 * validateOptionalFieldValues
	 */
	@Test
	public void test_validateOptionalFieldValues_GenderCode_Valid() {
		
		List<String> bizRules = Arrays.asList("R056");
		ReflectionTestUtils.setField(sbmBusinessValidator, "sbmBusinessRules", bizRules);

		PolicyMemberType member = new PolicyMemberType();
		
		//F
		String genderCd = "F";
		member.setGenderCode(genderCd);
		
		List<SbmErrWarningLogDTO> genderCdErrors = ReflectionTestUtils.invokeMethod(
				sbmBusinessValidator, "validateOptionalFieldValues", member);
		
		assertTrue("genderCdErrors", genderCdErrors.isEmpty());
		
		//M
		genderCd = "M";
		member.setGenderCode(genderCd);
		
		genderCdErrors = ReflectionTestUtils.invokeMethod(
				sbmBusinessValidator, "validateOptionalFieldValues", member);
		
		assertTrue("genderCdErrors", genderCdErrors.isEmpty());
		
		//U
		genderCd = "U";
		member.setGenderCode(genderCd);
		
		genderCdErrors = ReflectionTestUtils.invokeMethod(
				sbmBusinessValidator, "validateOptionalFieldValues", member);
		
		assertTrue("genderCdErrors", genderCdErrors.isEmpty());
	}
	
	/*
	 * validateOptionalFieldValues
	 */
	@Test
	public void test_validateOptionalFieldValues_RaceEthnicityCode_Invalid() {
		
		List<String> bizRules = Arrays.asList("R057");
		ReflectionTestUtils.setField(sbmBusinessValidator, "sbmBusinessRules", bizRules);

		PolicyMemberType member = new PolicyMemberType();
		
		String raceEthnicityCd = "ZZ";
		member.setRaceEthnicityCode(raceEthnicityCd);
		
		List<SbmErrWarningLogDTO> raceEthnicityCdErrors = ReflectionTestUtils.invokeMethod(
				sbmBusinessValidator, "validateOptionalFieldValues", member);
		
		assertFalse("RaceEthnicityCdErrors", raceEthnicityCdErrors.isEmpty());
		assertEquals("RaceEthnicityCdErrors size", 1, raceEthnicityCdErrors.size());
		
		SbmErrWarningLogDTO raceEthnicityCdError = raceEthnicityCdErrors.get(0);
		
		assertEquals("ElementInError", "RaceEthnicity", raceEthnicityCdError.getElementInError());
		assertEquals("Error Code", "WR-005", raceEthnicityCdError.getErrorWarningTypeCd());
		
		assertNull("raceEthnicityCd", member.getRaceEthnicityCode());
	}
	
	/*
	 * validateOptionalFieldValues
	 */
	@Test
	public void test_validateOptionalFieldValues_RaceEthnicityCode_Valid() {
		
		List<String> bizRules = Arrays.asList("R057");
		ReflectionTestUtils.setField(sbmBusinessValidator, "sbmBusinessRules", bizRules);

		PolicyMemberType member = new PolicyMemberType();
		
		//T
		String raceEthnicityCd = "T";
		member.setRaceEthnicityCode(raceEthnicityCd);
		
		SBMCache.getRaceEthnicityCodes().add(raceEthnicityCd);
		
		List<SbmErrWarningLogDTO> raceEthnicityCdErrors = ReflectionTestUtils.invokeMethod(
				sbmBusinessValidator, "validateOptionalFieldValues", member);
		
		assertTrue("RaceEthnicityCdErrors", raceEthnicityCdErrors.isEmpty());
		
		//Null
		raceEthnicityCd = null;
		member.setRaceEthnicityCode(raceEthnicityCd);
		
		raceEthnicityCdErrors = ReflectionTestUtils.invokeMethod(
				sbmBusinessValidator, "validateOptionalFieldValues", member);
		
		assertTrue("raceEthnicityCdErrors", raceEthnicityCdErrors.isEmpty());
	}
	
	/*
	 * validateOptionalFieldValues
	 */
	@Test
	public void test_validateOptionalFieldValues_TobaccoUseCode_Invalid() {
		
		List<String> bizRules = Arrays.asList("R058");
		ReflectionTestUtils.setField(sbmBusinessValidator, "sbmBusinessRules", bizRules);

		PolicyMemberType member = new PolicyMemberType();
		
		String tobaccoUseCd = "ZZ";
		member.setTobaccoUseCode(tobaccoUseCd);
		
		List<SbmErrWarningLogDTO> tobaccoUseCdErrors = ReflectionTestUtils.invokeMethod(
				sbmBusinessValidator, "validateOptionalFieldValues", member);
		
		assertFalse("TobaccoUseCdErrors", tobaccoUseCdErrors.isEmpty());
		assertEquals("TobaccoUseCdErrors size", 1, tobaccoUseCdErrors.size());
		
		SbmErrWarningLogDTO tobaccoUseCdError = tobaccoUseCdErrors.get(0);
		
		assertEquals("ElementInError", "TobaccoUseCode", tobaccoUseCdError.getElementInError());
		assertEquals("Error Code", "WR-005", tobaccoUseCdError.getErrorWarningTypeCd());
		
		assertNull("TobaccoUseCode", member.getTobaccoUseCode());
	}
	
	/*
	 * validateOptionalFieldValues
	 */
	@Test
	public void test_validateOptionalFieldValues_TobaccoUseCode_Valid() {
		
		List<String> bizRules = Arrays.asList("R058");
		ReflectionTestUtils.setField(sbmBusinessValidator, "sbmBusinessRules", bizRules);

		PolicyMemberType member = new PolicyMemberType();
		
		//T
		String tobaccoUseCd = "T";
		member.setTobaccoUseCode(tobaccoUseCd);
		
		List<SbmErrWarningLogDTO> tobaccoUseCdErrors = ReflectionTestUtils.invokeMethod(
				sbmBusinessValidator, "validateOptionalFieldValues", member);
		
		assertTrue("tobaccoUseCdErrors", tobaccoUseCdErrors.isEmpty());
		
		//N
		tobaccoUseCd = "N";
		member.setTobaccoUseCode(tobaccoUseCd);
		
		tobaccoUseCdErrors = ReflectionTestUtils.invokeMethod(
				sbmBusinessValidator, "validateOptionalFieldValues", member);
		
		assertTrue("tobaccoUseCdErrors", tobaccoUseCdErrors.isEmpty());
		
		//U
		tobaccoUseCd = "U";
		member.setTobaccoUseCode(tobaccoUseCd);
		
		tobaccoUseCdErrors = ReflectionTestUtils.invokeMethod(
				sbmBusinessValidator, "validateOptionalFieldValues", member);
		
		assertTrue("tobaccoUseCdErrors", tobaccoUseCdErrors.isEmpty());
	}
	
	/*
	 * validateOptionalFieldValues
	 */
	@Test
	public void test_validateOptionalFieldValues_NonCoveredSubscriberInd_Invalid() {
		
		List<String> bizRules = Arrays.asList("R059");
		ReflectionTestUtils.setField(sbmBusinessValidator, "sbmBusinessRules", bizRules);

		PolicyMemberType member = new PolicyMemberType();
		
		String nonCoveredSubscriberInd = "N";
		member.setNonCoveredSubscriberInd(nonCoveredSubscriberInd);
		
		List<SbmErrWarningLogDTO> nonCoveredSubscriberIndErrors = ReflectionTestUtils.invokeMethod(
				sbmBusinessValidator, "validateOptionalFieldValues", member);
		
		assertFalse("NonCoveredSubscriberIndErrors", nonCoveredSubscriberIndErrors.isEmpty());
		assertEquals("NonCoveredSubscriberIndErrors size", 1, nonCoveredSubscriberIndErrors.size());
		
		SbmErrWarningLogDTO nonCoveredSubscriberIndError = nonCoveredSubscriberIndErrors.get(0);
		
		assertEquals("ElementInError", "NonCoveredSubscriberInd", nonCoveredSubscriberIndError.getElementInError());
		assertEquals("Error Code", "WR-005", nonCoveredSubscriberIndError.getErrorWarningTypeCd());
		
		assertNull("NonCoveredSubscriberInd", member.getNonCoveredSubscriberInd());
	}
	
	/*
	 * validateOptionalFieldValues
	 */
	@Test
	public void test_validateOptionalFieldValues_NonCoveredSubscriberInd_Valid() {
		
		List<String> bizRules = Arrays.asList("R059");
		ReflectionTestUtils.setField(sbmBusinessValidator, "sbmBusinessRules", bizRules);

		PolicyMemberType member = new PolicyMemberType();
		
		//T
		String nonCoveredSubscriberInd = "Y";
		member.setNonCoveredSubscriberInd(nonCoveredSubscriberInd);
		
		List<SbmErrWarningLogDTO> nonCoveredSubscriberIndErrors = ReflectionTestUtils.invokeMethod(
				sbmBusinessValidator, "validateOptionalFieldValues", member);
		
		assertTrue("nonCoveredSubscriberIndErrors", nonCoveredSubscriberIndErrors.isEmpty());
		
		//N
		nonCoveredSubscriberInd = "";
		member.setNonCoveredSubscriberInd(nonCoveredSubscriberInd);
		
		nonCoveredSubscriberIndErrors = ReflectionTestUtils.invokeMethod(
				sbmBusinessValidator, "validateOptionalFieldValues", member);
		
		assertTrue("nonCoveredSubscriberIndErrors", nonCoveredSubscriberIndErrors.isEmpty());
	}
	
	/*
	 * validatememberDates
	 */
	@Test
	public void test_validatememberDates_MemberStartDateMissing_NonSubscriber() {
		
		List<String> bizRules = Arrays.asList("R060");
		ReflectionTestUtils.setField(sbmBusinessValidator, "sbmBusinessRules", bizRules);
		
		PolicyType policy = new PolicyType();
		policy.setPolicyStartDate(DateTimeUtil.getXMLGregorianCalendar(DATE));
		policy.setPolicyEndDate(DateTimeUtil.getXMLGregorianCalendar(DATE));
		
		PolicyMemberType dependent = new PolicyMemberType();
		dependent.setSubscriberIndicator("N");
		policy.getMemberInformation().add(dependent);
		
		List<SbmErrWarningLogDTO> memberDatesErrors = ReflectionTestUtils.invokeMethod(
				sbmBusinessValidator, "validatememberDates", policy, dependent);
		
		assertFalse("Member Dates Errors", memberDatesErrors.isEmpty());
		assertEquals("Member Date Errors", 1, memberDatesErrors.size());
		
		SbmErrWarningLogDTO memberDatesError = memberDatesErrors.get(0);
		
		assertEquals("ElementInError", "MemberStartDate", memberDatesError.getElementInError());
		assertEquals("Error Code", "ER-028", memberDatesError.getErrorWarningTypeCd());
	}
	
	/*
	 * validatememberDates
	 */
	@Test
	public void test_validatememberDates_MemberStartDateMissing_Subscriber_NonCoveredSubscriberInd_Null() {
		
		List<String> bizRules = Arrays.asList("R061");
		ReflectionTestUtils.setField(sbmBusinessValidator, "sbmBusinessRules", bizRules);
		
		PolicyType policy = new PolicyType();
		policy.setPolicyStartDate(DateTimeUtil.getXMLGregorianCalendar(DATE));
		policy.setPolicyEndDate(DateTimeUtil.getXMLGregorianCalendar(DATE));
		
		PolicyMemberType subscriber = new PolicyMemberType();
		subscriber.setSubscriberIndicator("Y");
		policy.getMemberInformation().add(subscriber);
		
		List<SbmErrWarningLogDTO> memberDatesErrors = ReflectionTestUtils.invokeMethod(
				sbmBusinessValidator, "validatememberDates", policy, subscriber);
		
		assertFalse("Member Dates Errors", memberDatesErrors.isEmpty());
		assertEquals("Member Date Errors", 1, memberDatesErrors.size());
		
		SbmErrWarningLogDTO memberDatesError = memberDatesErrors.get(0);
		
		assertEquals("ElementInError", "MemberStartDate", memberDatesError.getElementInError());
		assertEquals("Error Code", "ER-028", memberDatesError.getErrorWarningTypeCd());
	}
	
	/*
	 * validatememberDates
	 */
	@Test
	public void test_validatememberDates_MemberStartDateMissing_Subscriber_Not_NonCoveredSubscriber() {
		
		List<String> bizRules = Arrays.asList("R066");
		ReflectionTestUtils.setField(sbmBusinessValidator, "sbmBusinessRules", bizRules);
		
		PolicyType policy = new PolicyType();
		policy.setPolicyStartDate(DateTimeUtil.getXMLGregorianCalendar(DATE));
		policy.setPolicyEndDate(DateTimeUtil.getXMLGregorianCalendar(DATE));
		
		PolicyMemberType subscriber = new PolicyMemberType();
		subscriber.setSubscriberIndicator("Y");
		subscriber.setNonCoveredSubscriberInd("N");
		policy.getMemberInformation().add(subscriber);
		
		List<SbmErrWarningLogDTO> memberDatesErrors = ReflectionTestUtils.invokeMethod(
				sbmBusinessValidator, "validatememberDates", policy, subscriber);
		
		assertFalse("Member Dates Errors", memberDatesErrors.isEmpty());
		assertEquals("Member Date Errors", 1, memberDatesErrors.size());
		
		SbmErrWarningLogDTO memberDatesError = memberDatesErrors.get(0);
		
		assertEquals("ElementInError", "MemberStartDate", memberDatesError.getElementInError());
		assertEquals("Error Code", "ER-028", memberDatesError.getErrorWarningTypeCd());
	}
	
	/*
	 * validatememberDates
	 */
	@Test
	public void test_validatememberDates_MemberStartDateMissing_Subscriber_NonCoveredSubscriber() {
		
		List<String> bizRules = Arrays.asList("R066");
		ReflectionTestUtils.setField(sbmBusinessValidator, "sbmBusinessRules", bizRules);
		
		PolicyType policy = new PolicyType();
		policy.setPolicyStartDate(DateTimeUtil.getXMLGregorianCalendar(DATE));
		policy.setPolicyEndDate(DateTimeUtil.getXMLGregorianCalendar(DATE));
		
		PolicyMemberType subscriber = new PolicyMemberType();
		subscriber.setSubscriberIndicator("Y");
		subscriber.setNonCoveredSubscriberInd("Y");
		policy.getMemberInformation().add(subscriber);
		
		List<SbmErrWarningLogDTO> memberDatesErrors = ReflectionTestUtils.invokeMethod(
				sbmBusinessValidator, "validatememberDates", policy, subscriber);
		
		assertTrue("Member Dates Errors", memberDatesErrors.isEmpty());
	}
	
	/*
	 * validatememberDates
	 */
	@Test
	public void test_validatememberDates_MemberStartDateBeforePolicyStart() {
		
		List<String> bizRules = Arrays.asList("R062");
		ReflectionTestUtils.setField(sbmBusinessValidator, "sbmBusinessRules", bizRules);
		
		PolicyType policy = new PolicyType();
		policy.setPolicyStartDate(DateTimeUtil.getXMLGregorianCalendar(DATE));
		policy.setPolicyEndDate(DateTimeUtil.getXMLGregorianCalendar(DATE.plusDays(1)));
		//policy.setEffectuationIndicator("Y");
		
		PolicyMemberType subscriber = new PolicyMemberType();
		subscriber.setSubscriberIndicator("Y");
		
		MemberDates memberDate = new MemberDates();
		memberDate.setMemberStartDate(DateTimeUtil.getXMLGregorianCalendar(DATE.minusDays(1)));
		memberDate.setMemberEndDate(DateTimeUtil.getXMLGregorianCalendar(DATE));
		subscriber.getMemberDates().add(memberDate);
		
		policy.getMemberInformation().add(subscriber);
		
		List<SbmErrWarningLogDTO> memberDatesErrors = ReflectionTestUtils.invokeMethod(
				sbmBusinessValidator, "validatememberDates", policy, subscriber);
		
		assertFalse("Member Dates Errors", memberDatesErrors.isEmpty());
		assertEquals("Member Date Errors", 1, memberDatesErrors.size());
		
		SbmErrWarningLogDTO memberDatesError = memberDatesErrors.get(0);
		
		assertEquals("ElementInError", "MemberStartDate", memberDatesError.getElementInError());
		assertEquals("Error Code", "ER-031", memberDatesError.getErrorWarningTypeCd());
	}
	
	/*
	 * validatememberDates
	 */
	@Test
	public void test_validatememberDates_MemberStartDateEqualPolicyStart() {
		
		List<String> bizRules = Arrays.asList("R062");
		ReflectionTestUtils.setField(sbmBusinessValidator, "sbmBusinessRules", bizRules);
		
		PolicyType policy = new PolicyType();
		policy.setPolicyStartDate(DateTimeUtil.getXMLGregorianCalendar(DATE));
		policy.setPolicyEndDate(DateTimeUtil.getXMLGregorianCalendar(DATE.plusDays(1)));
		
		PolicyMemberType subscriber = new PolicyMemberType();
		subscriber.setSubscriberIndicator("Y");
		
		MemberDates memberDate = new MemberDates();
		memberDate.setMemberStartDate(DateTimeUtil.getXMLGregorianCalendar(DATE));
		subscriber.getMemberDates().add(memberDate);
		
		policy.getMemberInformation().add(subscriber);
		
		List<SbmErrWarningLogDTO> memberDatesErrors = ReflectionTestUtils.invokeMethod(
				sbmBusinessValidator, "validatememberDates", policy, subscriber);
		
		assertTrue("Member Dates Errors", memberDatesErrors.isEmpty());
	}
	
	/*
	 * validatememberDates
	 */
	@Test
	public void test_validatememberDates_MemberStartDateAfterPolicyEnd() {
		
		List<String> bizRules = Arrays.asList("R069");
		ReflectionTestUtils.setField(sbmBusinessValidator, "sbmBusinessRules", bizRules);
		
		PolicyType policy = new PolicyType();
		policy.setPolicyStartDate(DateTimeUtil.getXMLGregorianCalendar(DATE));
		policy.setPolicyEndDate(DateTimeUtil.getXMLGregorianCalendar(DATE.plusDays(1)));
		//policy.setEffectuationIndicator("Y");
		
		PolicyMemberType subscriber = new PolicyMemberType();
		subscriber.setSubscriberIndicator("Y");
		
		MemberDates memberDate = new MemberDates();
		memberDate.setMemberStartDate(DateTimeUtil.getXMLGregorianCalendar(DATE.plusDays(2)));
		memberDate.setMemberEndDate(DateTimeUtil.getXMLGregorianCalendar(DATE.plusDays(3)));
		subscriber.getMemberDates().add(memberDate);
		
		policy.getMemberInformation().add(subscriber);
		
		List<SbmErrWarningLogDTO> memberDatesErrors = ReflectionTestUtils.invokeMethod(
				sbmBusinessValidator, "validatememberDates", policy, subscriber);
		
		assertFalse("Member Dates Errors", memberDatesErrors.isEmpty());
		assertEquals("Member Date Errors", 1, memberDatesErrors.size());
		
		SbmErrWarningLogDTO memberDatesError = memberDatesErrors.get(0);
		
		assertEquals("ElementInError", "MemberStartDate", memberDatesError.getElementInError());
		assertEquals("Error Code", "ER-064", memberDatesError.getErrorWarningTypeCd());
	}
	
	/*
	 * validatememberDates
	 */
	@Test
	public void test_validatememberDates_MemberStartDateEqualPolicyEnd() {
		
		List<String> bizRules = Arrays.asList("R069");
		ReflectionTestUtils.setField(sbmBusinessValidator, "sbmBusinessRules", bizRules);
		
		PolicyType policy = new PolicyType();
		policy.setPolicyStartDate(DateTimeUtil.getXMLGregorianCalendar(DATE));
		policy.setPolicyEndDate(DateTimeUtil.getXMLGregorianCalendar(DATE.plusDays(1)));
		
		PolicyMemberType subscriber = new PolicyMemberType();
		subscriber.setSubscriberIndicator("Y");
		
		MemberDates memberDate = new MemberDates();
		memberDate.setMemberStartDate(DateTimeUtil.getXMLGregorianCalendar(DATE.plusDays(1)));
		subscriber.getMemberDates().add(memberDate);
		
		policy.getMemberInformation().add(subscriber);
		
		List<SbmErrWarningLogDTO> memberDatesErrors = ReflectionTestUtils.invokeMethod(
				sbmBusinessValidator, "validatememberDates", policy, subscriber);
		
		assertTrue("Member Dates Errors", memberDatesErrors.isEmpty());
	}
	
	/*
	 * validatememberDates
	 */
	@Test
	public void test_validatememberDates_MemberEndDateAfterPolicyEnd_Effectuated() {
		
		List<String> bizRules = Arrays.asList("R063");
		ReflectionTestUtils.setField(sbmBusinessValidator, "sbmBusinessRules", bizRules);
		
		PolicyType policy = new PolicyType();
		policy.setPolicyStartDate(DateTimeUtil.getXMLGregorianCalendar(DATE));
		policy.setPolicyEndDate(DateTimeUtil.getXMLGregorianCalendar(DATE.plusDays(1)));
		policy.setEffectuationIndicator("Y");
		
		PolicyMemberType subscriber = new PolicyMemberType();
		subscriber.setSubscriberIndicator("Y");
		
		MemberDates memberDate = new MemberDates();
		memberDate.setMemberStartDate(DateTimeUtil.getXMLGregorianCalendar(DATE));
		memberDate.setMemberEndDate(DateTimeUtil.getXMLGregorianCalendar(DATE.plusDays(2)));
		subscriber.getMemberDates().add(memberDate);
		
		policy.getMemberInformation().add(subscriber);
		
		List<SbmErrWarningLogDTO> memberDatesErrors = ReflectionTestUtils.invokeMethod(
				sbmBusinessValidator, "validatememberDates", policy, subscriber);
		
		assertFalse("Member Dates Errors", memberDatesErrors.isEmpty());
		assertEquals("Member Date Errors", 1, memberDatesErrors.size());
		
		SbmErrWarningLogDTO memberDatesError = memberDatesErrors.get(0);
		
		assertEquals("ElementInError", "MemberEndDate", memberDatesError.getElementInError());
		assertEquals("Error Code", "ER-032", memberDatesError.getErrorWarningTypeCd());
	}
	
	/*
	 * validatememberDates
	 */
	@Test
	public void test_validatememberDates_MemberEndDateAfterPolicyEnd_UnEffectuated() {
		
		List<String> bizRules = Arrays.asList("R063");
		ReflectionTestUtils.setField(sbmBusinessValidator, "sbmBusinessRules", bizRules);
		
		PolicyType policy = new PolicyType();
		policy.setPolicyStartDate(DateTimeUtil.getXMLGregorianCalendar(DATE));
		policy.setPolicyEndDate(DateTimeUtil.getXMLGregorianCalendar(DATE.plusDays(1)));
		policy.setEffectuationIndicator("N");
		
		PolicyMemberType subscriber = new PolicyMemberType();
		subscriber.setSubscriberIndicator("Y");
		
		MemberDates memberDate = new MemberDates();
		memberDate.setMemberStartDate(DateTimeUtil.getXMLGregorianCalendar(DATE));
		memberDate.setMemberEndDate(DateTimeUtil.getXMLGregorianCalendar(DATE.plusDays(2)));
		subscriber.getMemberDates().add(memberDate);
		
		policy.getMemberInformation().add(subscriber);
		
		List<SbmErrWarningLogDTO> memberDatesErrors = ReflectionTestUtils.invokeMethod(
				sbmBusinessValidator, "validatememberDates", policy, subscriber);
		
		assertTrue("Member Dates Errors", memberDatesErrors.isEmpty());
	}
	
	/*
	 * validatememberDates
	 */
	@Test
	public void test_validatememberDates_MemberEndDateBeforePolicyEnd_Effectuated() {
		
		List<String> bizRules = Arrays.asList("R063");
		ReflectionTestUtils.setField(sbmBusinessValidator, "sbmBusinessRules", bizRules);
		
		PolicyType policy = new PolicyType();
		policy.setPolicyStartDate(DateTimeUtil.getXMLGregorianCalendar(DATE));
		policy.setPolicyEndDate(DateTimeUtil.getXMLGregorianCalendar(DATE.plusDays(1)));
		policy.setEffectuationIndicator("N");
		
		PolicyMemberType subscriber = new PolicyMemberType();
		subscriber.setSubscriberIndicator("Y");
		
		MemberDates memberDate = new MemberDates();
		memberDate.setMemberStartDate(DateTimeUtil.getXMLGregorianCalendar(DATE));
		memberDate.setMemberEndDate(DateTimeUtil.getXMLGregorianCalendar(DATE.plusDays(1)));
		subscriber.getMemberDates().add(memberDate);
		
		policy.getMemberInformation().add(subscriber);
		
		List<SbmErrWarningLogDTO> memberDatesErrors = ReflectionTestUtils.invokeMethod(
				sbmBusinessValidator, "validatememberDates", policy, subscriber);
		
		assertTrue("Member Dates Errors", memberDatesErrors.isEmpty());
	}

	/*
	 * validatememberDates
	 */
	@Test
	public void test_validatememberDates_MultipleMemberDates_MemberEndDateMisaligned_EQ() {
		
		List<String> bizRules = Arrays.asList("R064");
		ReflectionTestUtils.setField(sbmBusinessValidator, "sbmBusinessRules", bizRules);
		
		PolicyType policy = new PolicyType();
		policy.setPolicyStartDate(DateTimeUtil.getXMLGregorianCalendar(DATE));
		policy.setPolicyEndDate(DateTimeUtil.getXMLGregorianCalendar(DATE.plusMonths(1)));
		policy.setEffectuationIndicator("Y");
		
		PolicyMemberType subscriber = new PolicyMemberType();
		subscriber.setSubscriberIndicator("Y");
		
		MemberDates memberDate = new MemberDates();
		memberDate.setMemberStartDate(DateTimeUtil.getXMLGregorianCalendar(DATE));
		memberDate.setMemberEndDate(DateTimeUtil.getXMLGregorianCalendar(DATE.plusDays(15)));
		subscriber.getMemberDates().add(memberDate);
		
		MemberDates memberDate2 = new MemberDates();
		memberDate2.setMemberStartDate(DateTimeUtil.getXMLGregorianCalendar(DATE.plusDays(15)));
		memberDate2.setMemberEndDate(DateTimeUtil.getXMLGregorianCalendar(DATE.plusMonths(1)));
		subscriber.getMemberDates().add(memberDate2);
		
		policy.getMemberInformation().add(subscriber);
		
		List<SbmErrWarningLogDTO> memberDatesErrors = ReflectionTestUtils.invokeMethod(
				sbmBusinessValidator, "validatememberDates", policy, subscriber);
		
		assertFalse("Member Dates Errors", memberDatesErrors.isEmpty());
		assertEquals("Member Date Errors", 1, memberDatesErrors.size());
		
		SbmErrWarningLogDTO memberDatesError = memberDatesErrors.get(0);
		
		assertEquals("ElementInError", "MemberEndDate", memberDatesError.getElementInError());
		assertEquals("Error Code", "ER-054", memberDatesError.getErrorWarningTypeCd());
	}
	
	/*
	 * validatememberDates
	 */
	@Test
	public void test_validatememberDates_MultipleMemberDates_MemberEndDateMisaligned_GT() {
		
		List<String> bizRules = Arrays.asList("R064");
		ReflectionTestUtils.setField(sbmBusinessValidator, "sbmBusinessRules", bizRules);
		
		PolicyType policy = new PolicyType();
		policy.setPolicyStartDate(DateTimeUtil.getXMLGregorianCalendar(DATE));
		policy.setPolicyEndDate(DateTimeUtil.getXMLGregorianCalendar(DATE.plusMonths(1)));
		policy.setEffectuationIndicator("Y");
		
		PolicyMemberType subscriber = new PolicyMemberType();
		subscriber.setSubscriberIndicator("Y");
		
		MemberDates memberDate = new MemberDates();
		memberDate.setMemberStartDate(DateTimeUtil.getXMLGregorianCalendar(DATE));
		memberDate.setMemberEndDate(DateTimeUtil.getXMLGregorianCalendar(DATE.plusDays(20)));
		subscriber.getMemberDates().add(memberDate);
		
		MemberDates memberDate2 = new MemberDates();
		memberDate2.setMemberStartDate(DateTimeUtil.getXMLGregorianCalendar(DATE.plusDays(15)));
		memberDate2.setMemberEndDate(DateTimeUtil.getXMLGregorianCalendar(DATE.plusMonths(1)));
		subscriber.getMemberDates().add(memberDate2);
		
		policy.getMemberInformation().add(subscriber);
		
		List<SbmErrWarningLogDTO> memberDatesErrors = ReflectionTestUtils.invokeMethod(
				sbmBusinessValidator, "validatememberDates", policy, subscriber);
		
		assertFalse("Member Dates Errors", memberDatesErrors.isEmpty());
		assertEquals("Member Date Errors", 1, memberDatesErrors.size());
		
		SbmErrWarningLogDTO memberDatesError = memberDatesErrors.get(0);
		
		assertEquals("ElementInError", "MemberEndDate", memberDatesError.getElementInError());
		assertEquals("Error Code", "ER-054", memberDatesError.getErrorWarningTypeCd());
	}
	
	/*
	 * validatememberDates
	 */
	@Test
	public void test_validatememberDates_MultipleMemberDates_MemberEndDateMisaligned_Valid() {
		
		List<String> bizRules = Arrays.asList("R064");
		ReflectionTestUtils.setField(sbmBusinessValidator, "sbmBusinessRules", bizRules);
		
		PolicyType policy = new PolicyType();
		policy.setPolicyStartDate(DateTimeUtil.getXMLGregorianCalendar(DATE));
		policy.setPolicyEndDate(DateTimeUtil.getXMLGregorianCalendar(DATE.plusMonths(1)));
		policy.setEffectuationIndicator("Y");
		
		PolicyMemberType subscriber = new PolicyMemberType();
		subscriber.setSubscriberIndicator("Y");
		
		MemberDates memberDate = new MemberDates();
		memberDate.setMemberStartDate(DateTimeUtil.getXMLGregorianCalendar(DATE));
		memberDate.setMemberEndDate(DateTimeUtil.getXMLGregorianCalendar(DATE.plusDays(14)));
		subscriber.getMemberDates().add(memberDate);
		
		MemberDates memberDate2 = new MemberDates();
		memberDate2.setMemberStartDate(DateTimeUtil.getXMLGregorianCalendar(DATE.plusDays(15)));
		memberDate2.setMemberEndDate(DateTimeUtil.getXMLGregorianCalendar(DATE.plusMonths(1)));
		subscriber.getMemberDates().add(memberDate2);
		
		policy.getMemberInformation().add(subscriber);
		
		List<SbmErrWarningLogDTO> memberDatesErrors = ReflectionTestUtils.invokeMethod(
				sbmBusinessValidator, "validatememberDates", policy, subscriber);
		
		assertTrue("Member Dates Errors", memberDatesErrors.isEmpty());
	}
	
	/*
	 * validatememberDates
	 */
	@Test
	public void test_validatememberDates_SingleMemberDate_MemberEndDateMissing() {
		
		List<String> bizRules = Arrays.asList("");
		ReflectionTestUtils.setField(sbmBusinessValidator, "sbmBusinessRules", bizRules);
		
		PolicyType policy = new PolicyType();
		policy.setPolicyStartDate(DateTimeUtil.getXMLGregorianCalendar(DATE));
		policy.setPolicyEndDate(DateTimeUtil.getXMLGregorianCalendar(DATE.plusMonths(1)));
		policy.setEffectuationIndicator("Y");
		
		PolicyMemberType subscriber = new PolicyMemberType();
		subscriber.setSubscriberIndicator("Y");
		
		MemberDates memberDate = new MemberDates();
		memberDate.setMemberStartDate(DateTimeUtil.getXMLGregorianCalendar(DATE));
		subscriber.getMemberDates().add(memberDate);
		
		policy.getMemberInformation().add(subscriber);
		
		List<SbmErrWarningLogDTO> memberDatesErrors = ReflectionTestUtils.invokeMethod(
				sbmBusinessValidator, "validatememberDates", policy, subscriber);
		
		assertTrue("Member Dates Errors", memberDatesErrors.isEmpty());
	}
	
	
	@Test
	public void test_validateEpsPolicy() {
		
		String qhpId = RandomStringUtils.randomAlphanumeric(14);
		String insrncLineCd = "HLT";
		
		SBMPolicyDTO  dbSBMPolicyDTO = new SBMPolicyDTO();
		PolicyType epsPolicy = new PolicyType();
		epsPolicy.setQHPId(qhpId);
		epsPolicy.setInsuranceLineCode(insrncLineCd);
		dbSBMPolicyDTO.setPolicy(epsPolicy);
		
		SBMPolicyDTO policyDTO = new SBMPolicyDTO();
		PolicyType policy = new PolicyType();
		policy.setQHPId(qhpId);
		policy.setInsuranceLineCode(insrncLineCd);
		policyDTO.setPolicy(policy);
		
		List<SbmErrWarningLogDTO> policyMatchErrors = sbmBusinessValidator.validateEpsPolicy(dbSBMPolicyDTO, policyDTO);
		
		assertTrue("No policy Match Errors", policyMatchErrors.isEmpty());
	}
	
	@Test
	public void test_validateEpsPolicy_Errors() {
		
		String qhpId = RandomStringUtils.randomAlphanumeric(14);
		String insrncLineCd = "HLT";
		
		SBMPolicyDTO  dbSBMPolicyDTO = new SBMPolicyDTO();
		PolicyType epsPolicy = new PolicyType();
		epsPolicy.setQHPId(qhpId);
		epsPolicy.setInsuranceLineCode(insrncLineCd);
		dbSBMPolicyDTO.setPolicy(epsPolicy);
		
		SBMPolicyDTO policyDTO = new SBMPolicyDTO();
		PolicyType policy = new PolicyType();
		policy.setQHPId("epsQhpId");
		policy.setInsuranceLineCode("DEN");
		policyDTO.setPolicy(policy);
		
		List<SbmErrWarningLogDTO> policyMatchErrors = sbmBusinessValidator.validateEpsPolicy(dbSBMPolicyDTO, policyDTO);
		
		assertFalse("No policy Match Errors", policyMatchErrors.isEmpty());
		
		assertEquals("2 policy Match Errors", 2, policyMatchErrors.size());
	}
	
	/*
	 * validateQhpIdToEps
	 */
	@Test
	public void test_validateQhpIdToEps_InValid_QhpId() {
		
		List<SbmErrWarningLogDTO> qhpErrors = new ArrayList<SbmErrWarningLogDTO>();
		
		ReflectionTestUtils.invokeMethod(
				sbmBusinessValidator, "validateQhpIdToEps", "qhpId", "epsQhpId", qhpErrors);
		
		assertFalse("qhp Errors", qhpErrors.isEmpty());
		assertEquals("qhp Errors", 1, qhpErrors.size());
		
		SbmErrWarningLogDTO qhpError = qhpErrors.get(0);
		
		assertEquals("ElementInError", "QHPId", qhpError.getElementInError());
		assertEquals("Error Code", "ER-056", qhpError.getErrorWarningTypeCd());
	}
	
	/*
	 * validateQhpIdToEps
	 */
	@Test
	public void test_validateQhpIdToEps_Valid_QhpId() {
		
		List<SbmErrWarningLogDTO> qhpErrors = new ArrayList<SbmErrWarningLogDTO>();
		
		ReflectionTestUtils.invokeMethod(
				sbmBusinessValidator, "validateQhpIdToEps", "epsQhpId", "epsQhpId", qhpErrors);
		
		assertTrue("qhp Errors", qhpErrors.isEmpty());
	}
	
	/*
	 * validateInsuranceLineCodeToEps
	 */
	@Test
	public void test_validateInsuranceLineCodeToEps_InValid() {
		
		List<SbmErrWarningLogDTO> insuranceLineCdWarnings = new ArrayList<SbmErrWarningLogDTO>();
		
		ReflectionTestUtils.invokeMethod(
				sbmBusinessValidator, "validateInsuranceLineCd", "InsuranceLineCd", "epsInsuranceLineCd", insuranceLineCdWarnings);
		
		assertFalse("insuranceLineCdWarnings", insuranceLineCdWarnings.isEmpty());
		assertEquals("insuranceLineCdWarnings size", 1, insuranceLineCdWarnings.size());
		
		SbmErrWarningLogDTO insuranceLineCdWarning = insuranceLineCdWarnings.get(0);
		
		assertEquals("ElementInError", "InsuranceLineCode", insuranceLineCdWarning.getElementInError());
		assertEquals("Error Code", "WR-009", insuranceLineCdWarning.getErrorWarningTypeCd());
	}
	
	/*
	 * validateInsuranceLineCodeToEps
	 */
	@Test
	public void test_validateInsuranceLineCodeToEps_Valid() {
		
		List<SbmErrWarningLogDTO> insuranceLineCdWarnings = new ArrayList<SbmErrWarningLogDTO>();
		
		ReflectionTestUtils.invokeMethod(
				sbmBusinessValidator, "validateInsuranceLineCd", "epsQhpId", "epsQhpId", insuranceLineCdWarnings);
		
		assertTrue("insuranceLineCd Warnings", insuranceLineCdWarnings.isEmpty());
	}
	
	/*
	 * validateMissingMembers
	 */
	@Test
	public void test_validateMissingMembers_Valid() {
		
		List<SbmErrWarningLogDTO> missingMemberWarnings = new ArrayList<SbmErrWarningLogDTO>();
		
		List<PolicyMemberType> mockPolicyMembers = createPolicyMemberList("Member1", "Member2", "Member3");
		
		List<PolicyMemberType> policyMembers = new ArrayList<PolicyMemberType>();
		policyMembers.addAll(mockPolicyMembers);
		
		List<PolicyMemberType> epsPolicyMembers = new ArrayList<PolicyMemberType>();
		epsPolicyMembers.addAll(mockPolicyMembers);
		
		ReflectionTestUtils.invokeMethod(
				sbmBusinessValidator, "validateMissingMembers", policyMembers, epsPolicyMembers, missingMemberWarnings);
		
		assertTrue("No missing Member Warnings", missingMemberWarnings.isEmpty());
	}
	
	/*
	 * validateMissingMembers
	 */
	@Test
	public void test_validateMissingMembers_inValid() {
		
		List<SbmErrWarningLogDTO> missingMemberWarnings = new ArrayList<SbmErrWarningLogDTO>();
		
		List<PolicyMemberType> mockPolicyMembers = createPolicyMemberList("Member1", "Member2", "Member3");
		
		List<PolicyMemberType> policyMembers = new ArrayList<PolicyMemberType>();
		policyMembers.addAll(createPolicyMemberList("Member1", "Member2"));
		
		List<PolicyMemberType> epsPolicyMembers = new ArrayList<PolicyMemberType>();
		epsPolicyMembers.addAll(mockPolicyMembers);
		
		ReflectionTestUtils.invokeMethod(
				sbmBusinessValidator, "validateMissingMembers", policyMembers, epsPolicyMembers, missingMemberWarnings);
		
		assertFalse("missing Member Warnings", missingMemberWarnings.isEmpty());
		
		assertEquals("missing Member Warnings", 1, missingMemberWarnings.size());
		
		SbmErrWarningLogDTO missingMemberWarning = missingMemberWarnings.get(0);
		
		assertEquals("ElementInError", "ExchangeAssignedMemberID", missingMemberWarning.getElementInError());
		assertEquals("Error Code", "WR-010", missingMemberWarning.getErrorWarningTypeCd());
	}


	/*
	 * Create List of Members for the input memberIds
	 */
	private List<PolicyMemberType> createPolicyMemberList(String... memberIds) {

		List<PolicyMemberType> members = new ArrayList<PolicyMemberType>();
		
		for (String exchangeMemberId : memberIds) {
			
			PolicyMemberType member = new PolicyMemberType();
			member.setExchangeAssignedMemberId(exchangeMemberId);
			
			members.add(member);
		}
		
		return members;
	}

	@After
	public void tearDown() {
		SBMCache.getBusinessRulesMap().clear();
	}
}
