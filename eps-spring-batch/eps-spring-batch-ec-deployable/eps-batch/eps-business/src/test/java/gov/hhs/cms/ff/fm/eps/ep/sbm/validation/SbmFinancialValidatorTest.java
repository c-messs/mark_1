/**
 * 
 */
package gov.hhs.cms.ff.fm.eps.ep.sbm.validation;

import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.RandomStringUtils;
import org.easymock.EasyMock;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.springframework.test.util.ReflectionTestUtils;

import gov.cms.dsh.sbmi.PolicyType;
import gov.cms.dsh.sbmi.PolicyType.FinancialInformation;
import gov.cms.dsh.sbmi.ProratedAmountType;
import gov.hhs.cms.ff.fm.eps.ep.sbm.SBMCache;
import gov.hhs.cms.ff.fm.eps.ep.sbm.SBMPolicyDTO;
import gov.hhs.cms.ff.fm.eps.ep.sbm.SBMPremium;
import gov.hhs.cms.ff.fm.eps.ep.sbm.SbmErrWarningLogDTO;
import gov.hhs.cms.ff.fm.eps.ep.sbm.SbmMetalLevelType;
import gov.hhs.cms.ff.fm.eps.ep.sbm.services.SBMDataService;
import gov.hhs.cms.ff.fm.eps.ep.sbm.services.impl.SBMDataServiceImpl;
import gov.hhs.cms.ff.fm.eps.ep.sbm.validation.impl.SbmFinancialValidatorImpl;
import gov.hhs.cms.ff.fm.eps.ep.util.DateTimeUtil;
import junit.framework.TestCase;

/**
 * Test class for SBMValidationServiceImpl
 * 
 * @author girish.padmanabhan
 */
@RunWith(JUnit4.class)
public class SbmFinancialValidatorTest extends TestCase {

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
	protected final LocalDate JUL_15 = LocalDate.of(YEAR, 7, 15);
	protected final LocalDate JUL_31 = LocalDate.of(YEAR, 7, 31);
	protected final LocalDate AUG_1 = LocalDate.of(YEAR, 8, 1);
	protected final LocalDate NOV_30 = LocalDate.of(YEAR, 11, 30);
	protected final LocalDate DEC_1 = LocalDate.of(YEAR, 12, 1);
	protected final LocalDate DEC_15 = LocalDate.of(YEAR, 12, 15);
	protected final LocalDate DEC_31 = LocalDate.of(YEAR, 12, 31);

	private SbmFinancialValidatorImpl sbmFinancialValidator;
	private SBMDataService mockSbmDataService;

	@Before
	public void setup() throws Exception {
		sbmFinancialValidator = new SbmFinancialValidatorImpl();
		
		mockSbmDataService = EasyMock.createMock(SBMDataServiceImpl.class);
		
		sbmFinancialValidator.setSbmDataService(mockSbmDataService);
	}

	/*
	 * Scenario 3.10.1	Mid-Month Start- Simple History - (SBMI Sent Feb '17) 
	 * - SBM Prorates Mid-Month Start
	 */
	@Test
	public void test_processInboundPremiums_SBM_Prorates_Mid_Month_Start() {

		LocalDate esd = JAN_15;
		LocalDate eed = DEC_31;
		BigDecimal aptc = new BigDecimal("100");
		BigDecimal tpa = new BigDecimal("200");
		BigDecimal tira = new BigDecimal("100");

		// Expected data after replacement, Record 1
		BigDecimal expectedAPTC_EPS1 = aptc;
		BigDecimal expectedTPA_EPS1 = tpa;
		BigDecimal expectedTIRA_EPS1 = tira;

		PolicyType policy = new PolicyType();
		policy.setPolicyStartDate(DateTimeUtil.getXMLGregorianCalendar(esd));
		policy.setPolicyEndDate(DateTimeUtil.getXMLGregorianCalendar(eed));
		
		FinancialInformation fit = new FinancialInformation();
		
		fit.setFinancialEffectiveStartDate(DateTimeUtil.getXMLGregorianCalendar(esd));
		fit.setFinancialEffectiveEndDate(DateTimeUtil.getXMLGregorianCalendar(eed));
		
		setFinancialInfo(fit, "TPA", tpa);
		setFinancialInfo(fit, "TIRA", tira);
		setFinancialInfo(fit, "APTC", aptc);
		
		ProratedAmountType pat = new ProratedAmountType();
		pat.setPartialMonthEffectiveStartDate(DateTimeUtil.getXMLGregorianCalendar(JAN_15));
		pat.setPartialMonthEffectiveEndDate(DateTimeUtil.getXMLGregorianCalendar(JAN_31));
		
		setProratedValues(pat, "P-APTC", new BigDecimal("50"));
		setProratedValues(pat, "P-TPA", new BigDecimal("100"));
		
		fit.getProratedAmount().add(pat);
		
		policy.getFinancialInformation().add(fit);
		
		SBMPolicyDTO policyDTO = new SBMPolicyDTO();
		policyDTO.setPolicy(policy);

		Map<LocalDate, SBMPremium> epsPremiums = sbmFinancialValidator.processInboundPremiums(policy);
		
		assertTrue("Premiums list", MapUtils.isNotEmpty(epsPremiums));
		assertEquals("Premiums list", 2, epsPremiums.size());

		SBMPremium actualPremium1 = epsPremiums.get(JAN_15);

		assertEquals("ESD", JAN_15, actualPremium1.getEffectiveStartDate());
		assertEquals("EED", JAN_31, actualPremium1.getEffectiveEndDate());
		assertEquals("APTC", expectedAPTC_EPS1, actualPremium1.getAptc());
		assertEquals("TPA", expectedTPA_EPS1, actualPremium1.getTotalPremium());
		assertEquals("TIRA", expectedTIRA_EPS1, actualPremium1.getIndividualResponsibleAmt());
		assertEquals("APTC", new BigDecimal("50"), actualPremium1.getProratedAptc());
		assertEquals("TPA", new BigDecimal("100"), actualPremium1.getProratedPremium());
		
		SBMPremium actualPremium2 = epsPremiums.get(FEB_1);

		assertEquals("ESD", FEB_1, actualPremium2.getEffectiveStartDate());
		assertEquals("EED", DEC_31, actualPremium2.getEffectiveEndDate());
		assertEquals("APTC", expectedAPTC_EPS1, actualPremium2.getAptc());
		assertEquals("TPA", expectedTPA_EPS1, actualPremium2.getTotalPremium());
		assertEquals("TIRA", expectedTIRA_EPS1, actualPremium2.getIndividualResponsibleAmt());
	}
	
	/*
	 * Scenario 3.10.2	Mid-Month Start & End- Simple History 
	 * - SBM Prorates ALL Partial periods (SBMI sent Jan'18) 
	 */
	@Test
	public void test_processInboundPremiums_SBM_Prorates_ALL_Partial_periods() {

		LocalDate esd = JAN_15;
		LocalDate eed = DEC_15;
		BigDecimal aptc = new BigDecimal("100");
		BigDecimal tpa = new BigDecimal("200");
		BigDecimal tira = new BigDecimal("100");

		// Expected data after replacement, Record 1
		BigDecimal expectedAPTC_EPS1 = aptc;
		BigDecimal expectedTPA_EPS1 = tpa;
		BigDecimal expectedTIRA_EPS1 = tira;

		PolicyType policy = new PolicyType();
		policy.setPolicyStartDate(DateTimeUtil.getXMLGregorianCalendar(esd));
		policy.setPolicyEndDate(DateTimeUtil.getXMLGregorianCalendar(eed));
		
		FinancialInformation fit = new FinancialInformation();
		
		fit.setFinancialEffectiveStartDate(DateTimeUtil.getXMLGregorianCalendar(esd));
		fit.setFinancialEffectiveEndDate(DateTimeUtil.getXMLGregorianCalendar(eed));
		
		setFinancialInfo(fit, "TPA", tpa);
		setFinancialInfo(fit, "TIRA", tira);
		setFinancialInfo(fit, "APTC", aptc);
		
		ProratedAmountType pat = new ProratedAmountType();
		pat.setPartialMonthEffectiveStartDate(DateTimeUtil.getXMLGregorianCalendar(JAN_15));
		pat.setPartialMonthEffectiveEndDate(DateTimeUtil.getXMLGregorianCalendar(JAN_31));
		
		setProratedValues(pat, "P-APTC", new BigDecimal("50"));
		setProratedValues(pat, "P-TPA", new BigDecimal("100"));
		
		fit.getProratedAmount().add(pat);
		
		
		ProratedAmountType pat2 = new ProratedAmountType();
		pat2.setPartialMonthEffectiveStartDate(DateTimeUtil.getXMLGregorianCalendar(DEC_1));
		pat2.setPartialMonthEffectiveEndDate(DateTimeUtil.getXMLGregorianCalendar(DEC_15));
		
		setProratedValues(pat2, "P-APTC", new BigDecimal("50"));
		setProratedValues(pat2, "P-TPA", new BigDecimal("100"));
		
		fit.getProratedAmount().add(pat2);
		
		policy.getFinancialInformation().add(fit);
		
		SBMPolicyDTO policyDTO = new SBMPolicyDTO();
		policyDTO.setPolicy(policy);

		Map<LocalDate, SBMPremium> epsPremiums = sbmFinancialValidator.processInboundPremiums(policy);
		
		assertTrue("Premiums list", MapUtils.isNotEmpty(epsPremiums));
		assertEquals("Premiums list", 3, epsPremiums.size());

		SBMPremium actualPremium1 = epsPremiums.get(JAN_15);

		assertEquals("ESD", JAN_15, actualPremium1.getEffectiveStartDate());
		assertEquals("EED", JAN_31, actualPremium1.getEffectiveEndDate());
		assertEquals("APTC", expectedAPTC_EPS1, actualPremium1.getAptc());
		assertEquals("TPA", expectedTPA_EPS1, actualPremium1.getTotalPremium());
		assertEquals("TIRA", expectedTIRA_EPS1, actualPremium1.getIndividualResponsibleAmt());
		assertEquals("P-APTC", new BigDecimal("50"), actualPremium1.getProratedAptc());
		assertEquals("P-TPA", new BigDecimal("100"), actualPremium1.getProratedPremium());
		
		SBMPremium actualPremium2 = epsPremiums.get(FEB_1);

		assertEquals("ESD", FEB_1, actualPremium2.getEffectiveStartDate());
		assertEquals("EED", NOV_30, actualPremium2.getEffectiveEndDate());
		assertEquals("APTC", expectedAPTC_EPS1, actualPremium2.getAptc());
		assertEquals("TPA", expectedTPA_EPS1, actualPremium2.getTotalPremium());
		assertEquals("TIRA", expectedTIRA_EPS1, actualPremium2.getIndividualResponsibleAmt());
		
		SBMPremium actualPremium3 = epsPremiums.get(DEC_1);

		assertEquals("ESD", DEC_1, actualPremium3.getEffectiveStartDate());
		assertEquals("EED", DEC_15, actualPremium3.getEffectiveEndDate());
		assertEquals("APTC", expectedAPTC_EPS1, actualPremium3.getAptc());
		assertEquals("TPA", expectedTPA_EPS1, actualPremium3.getTotalPremium());
		assertEquals("TIRA", expectedTIRA_EPS1, actualPremium3.getIndividualResponsibleAmt());
		assertEquals("P-APTC", new BigDecimal("50"), actualPremium3.getProratedAptc());
		assertEquals("P-TPA", new BigDecimal("100"), actualPremium3.getProratedPremium());

	}

	/*
	 * Scenario 3.10.3	Mid-Month Start & End- Simple History 
	 * - SBM Prorates Only Mid-Month Start (SBMI sent Jan'18) 
	 */
	@Test
	public void test_processInboundPremiums_SBM_Prorates_Only_Mid_Month_Start() {

		LocalDate esd = JAN_15;
		LocalDate eed = DEC_15;
		BigDecimal aptc = new BigDecimal("100");
		BigDecimal tpa = new BigDecimal("200");
		BigDecimal tira = new BigDecimal("100");

		// Expected data after replacement, Record 1
		BigDecimal expectedAPTC_EPS1 = aptc;
		BigDecimal expectedTPA_EPS1 = tpa;
		BigDecimal expectedTIRA_EPS1 = tira;

		PolicyType policy = new PolicyType();
		policy.setPolicyStartDate(DateTimeUtil.getXMLGregorianCalendar(esd));
		policy.setPolicyEndDate(DateTimeUtil.getXMLGregorianCalendar(eed));
		
		FinancialInformation fit = new FinancialInformation();
		
		fit.setFinancialEffectiveStartDate(DateTimeUtil.getXMLGregorianCalendar(esd));
		fit.setFinancialEffectiveEndDate(DateTimeUtil.getXMLGregorianCalendar(eed));
		
		setFinancialInfo(fit, "TPA", tpa);
		setFinancialInfo(fit, "TIRA", tira);
		setFinancialInfo(fit, "APTC", aptc);
		
		ProratedAmountType pat = new ProratedAmountType();
		pat.setPartialMonthEffectiveStartDate(DateTimeUtil.getXMLGregorianCalendar(JAN_15));
		pat.setPartialMonthEffectiveEndDate(DateTimeUtil.getXMLGregorianCalendar(JAN_31));
		
		setProratedValues(pat, "P-APTC", new BigDecimal("50"));
		setProratedValues(pat, "P-TPA", new BigDecimal("100"));
		
		fit.getProratedAmount().add(pat);
		
		policy.getFinancialInformation().add(fit);
		
		SBMPolicyDTO policyDTO = new SBMPolicyDTO();
		policyDTO.setPolicy(policy);

		Map<LocalDate, SBMPremium> epsPremiums = sbmFinancialValidator.processInboundPremiums(policy);
		
		assertTrue("Premiums list", MapUtils.isNotEmpty(epsPremiums));
		assertEquals("Premiums list", 2, epsPremiums.size());

		SBMPremium actualPremium1 = epsPremiums.get(JAN_15);

		assertEquals("ESD", JAN_15, actualPremium1.getEffectiveStartDate());
		assertEquals("EED", JAN_31, actualPremium1.getEffectiveEndDate());
		assertEquals("APTC", expectedAPTC_EPS1, actualPremium1.getAptc());
		assertEquals("TPA", expectedTPA_EPS1, actualPremium1.getTotalPremium());
		assertEquals("TIRA", expectedTIRA_EPS1, actualPremium1.getIndividualResponsibleAmt());
		assertEquals("P-APTC", new BigDecimal("50"), actualPremium1.getProratedAptc());
		assertEquals("P-TPA", new BigDecimal("100"), actualPremium1.getProratedPremium());
		
		SBMPremium actualPremium2 = epsPremiums.get(FEB_1);

		assertEquals("ESD", FEB_1, actualPremium2.getEffectiveStartDate());
		assertEquals("EED", DEC_15, actualPremium2.getEffectiveEndDate());
		assertEquals("APTC", expectedAPTC_EPS1, actualPremium2.getAptc());
		assertEquals("TPA", expectedTPA_EPS1, actualPremium2.getTotalPremium());
		assertEquals("TIRA", expectedTIRA_EPS1, actualPremium2.getIndividualResponsibleAmt());

	}

	/*
	 * Scenario 3.10.4	Mid-Month Start- Simple History - (SBMI Sent Feb '17) 
	 * - SBM Does not Prorate Mid-Month Start 
	 */
	@Test
	public void test_processInboundPremiums_SBM_Does_not_Prorate_Mid_Month_Start() {

		LocalDate esd = JAN_15;
		LocalDate eed = DEC_31;
		BigDecimal aptc = new BigDecimal("100");
		BigDecimal tpa = new BigDecimal("200");
		BigDecimal tira = new BigDecimal("100");

		// Expected data after replacement, Record 1
		BigDecimal expectedAPTC_EPS1 = aptc;
		BigDecimal expectedTPA_EPS1 = tpa;
		BigDecimal expectedTIRA_EPS1 = tira;

		PolicyType policy = new PolicyType();
		policy.setPolicyStartDate(DateTimeUtil.getXMLGregorianCalendar(esd));
		policy.setPolicyEndDate(DateTimeUtil.getXMLGregorianCalendar(eed));
		
		FinancialInformation fit = new FinancialInformation();
		
		fit.setFinancialEffectiveStartDate(DateTimeUtil.getXMLGregorianCalendar(esd));
		fit.setFinancialEffectiveEndDate(DateTimeUtil.getXMLGregorianCalendar(eed));
		
		setFinancialInfo(fit, "TPA", tpa);
		setFinancialInfo(fit, "TIRA", tira);
		setFinancialInfo(fit, "APTC", aptc);
		
		policy.getFinancialInformation().add(fit);
		
		SBMPolicyDTO policyDTO = new SBMPolicyDTO();
		policyDTO.setPolicy(policy);

		Map<LocalDate, SBMPremium> epsPremiums = sbmFinancialValidator.processInboundPremiums(policy);
		
		assertTrue("Premiums list", MapUtils.isNotEmpty(epsPremiums));
		assertEquals("Premiums list", 1, epsPremiums.size());

		SBMPremium actualPremium1 = epsPremiums.get(JAN_15);

		assertEquals("ESD", JAN_15, actualPremium1.getEffectiveStartDate());
		assertEquals("EED", DEC_31, actualPremium1.getEffectiveEndDate());
		assertEquals("APTC", expectedAPTC_EPS1, actualPremium1.getAptc());
		assertEquals("TPA", expectedTPA_EPS1, actualPremium1.getTotalPremium());
		assertEquals("TIRA", expectedTIRA_EPS1, actualPremium1.getIndividualResponsibleAmt());

	}
	
	/*
	 * Scenario 3.10.5	Mid-Month Start & End 
	 * - SBM Prorates Mid-Month End Only (SBMI Sent Jan'18) 
	 */
	@Test
	public void test_processInboundPremiums_SBM_Prorates_Mid_Month_End_Only() {

		LocalDate esd = JAN_15;
		LocalDate eed = DEC_15;
		BigDecimal aptc = new BigDecimal("100");
		BigDecimal tpa = new BigDecimal("200");
		BigDecimal tira = new BigDecimal("100");

		// Expected data after replacement, Record 1
		BigDecimal expectedAPTC_EPS1 = aptc;
		BigDecimal expectedTPA_EPS1 = tpa;
		BigDecimal expectedTIRA_EPS1 = tira;

		PolicyType policy = new PolicyType();
		policy.setPolicyStartDate(DateTimeUtil.getXMLGregorianCalendar(esd));
		policy.setPolicyEndDate(DateTimeUtil.getXMLGregorianCalendar(eed));
		
		FinancialInformation fit = new FinancialInformation();
		
		fit.setFinancialEffectiveStartDate(DateTimeUtil.getXMLGregorianCalendar(esd));
		fit.setFinancialEffectiveEndDate(DateTimeUtil.getXMLGregorianCalendar(eed));
		
		setFinancialInfo(fit, "TPA", tpa);
		setFinancialInfo(fit, "TIRA", tira);
		setFinancialInfo(fit, "APTC", aptc);
		
		ProratedAmountType pat = new ProratedAmountType();
		pat.setPartialMonthEffectiveStartDate(DateTimeUtil.getXMLGregorianCalendar(DEC_1));
		pat.setPartialMonthEffectiveEndDate(DateTimeUtil.getXMLGregorianCalendar(DEC_15));
		
		setProratedValues(pat, "P-APTC", new BigDecimal("50"));
		setProratedValues(pat, "P-TPA", new BigDecimal("100"));
		
		fit.getProratedAmount().add(pat);
		
		policy.getFinancialInformation().add(fit);
		
		SBMPolicyDTO policyDTO = new SBMPolicyDTO();
		policyDTO.setPolicy(policy);

		Map<LocalDate, SBMPremium> epsPremiums = sbmFinancialValidator.processInboundPremiums(policy);
		
		assertTrue("Premiums list", MapUtils.isNotEmpty(epsPremiums));
		assertEquals("Premiums list", 2, epsPremiums.size());

		SBMPremium actualPremium1 = epsPremiums.get(JAN_15);

		assertEquals("ESD", JAN_15, actualPremium1.getEffectiveStartDate());
		assertEquals("EED", NOV_30, actualPremium1.getEffectiveEndDate());
		assertEquals("APTC", expectedAPTC_EPS1, actualPremium1.getAptc());
		assertEquals("TPA", expectedTPA_EPS1, actualPremium1.getTotalPremium());
		
		SBMPremium actualPremium2 = epsPremiums.get(DEC_1);

		assertEquals("ESD", DEC_1, actualPremium2.getEffectiveStartDate());
		assertEquals("EED", DEC_15, actualPremium2.getEffectiveEndDate());
		assertEquals("APTC", expectedAPTC_EPS1, actualPremium2.getAptc());
		assertEquals("TPA", expectedTPA_EPS1, actualPremium2.getTotalPremium());
		assertEquals("TIRA", expectedTIRA_EPS1, actualPremium2.getIndividualResponsibleAmt());
		assertEquals("P-APTC", new BigDecimal("50"), actualPremium2.getProratedAptc());
		assertEquals("P-TPA", new BigDecimal("100"), actualPremium2.getProratedPremium());

	}
	
	/*
	 * Scenario 3.10.6	Mid-Month Start & End 
	 * - SBM Does not Prorate (SBMI Sent Jan'18) 
	 */
	@Test
	public void test_processInboundPremiums_SBM_Does_not_Prorate() {

		LocalDate esd = JAN_15;
		LocalDate eed = DEC_15;
		BigDecimal aptc = new BigDecimal("100");
		BigDecimal tpa = new BigDecimal("200");
		BigDecimal tira = new BigDecimal("100");

		// Expected data after replacement, Record 1
		BigDecimal expectedAPTC_EPS1 = aptc;
		BigDecimal expectedTPA_EPS1 = tpa;
		BigDecimal expectedTIRA_EPS1 = tira;

		PolicyType policy = new PolicyType();
		policy.setPolicyStartDate(DateTimeUtil.getXMLGregorianCalendar(esd));
		policy.setPolicyEndDate(DateTimeUtil.getXMLGregorianCalendar(eed));
		
		FinancialInformation fit = new FinancialInformation();
		
		fit.setFinancialEffectiveStartDate(DateTimeUtil.getXMLGregorianCalendar(esd));
		fit.setFinancialEffectiveEndDate(DateTimeUtil.getXMLGregorianCalendar(eed));
		
		setFinancialInfo(fit, "TPA", tpa);
		setFinancialInfo(fit, "TIRA", tira);
		setFinancialInfo(fit, "APTC", aptc);
		
		policy.getFinancialInformation().add(fit);
		
		SBMPolicyDTO policyDTO = new SBMPolicyDTO();
		policyDTO.setPolicy(policy);

		Map<LocalDate, SBMPremium> epsPremiums = sbmFinancialValidator.processInboundPremiums(policy);
		
		assertTrue("Premiums list", MapUtils.isNotEmpty(epsPremiums));
		assertEquals("Premiums list", 1, epsPremiums.size());

		SBMPremium actualPremium1 = epsPremiums.get(JAN_15);

		assertEquals("ESD", JAN_15, actualPremium1.getEffectiveStartDate());
		assertEquals("EED", DEC_15, actualPremium1.getEffectiveEndDate());
		assertEquals("APTC", expectedAPTC_EPS1, actualPremium1.getAptc());
		assertEquals("TPA", expectedTPA_EPS1, actualPremium1.getTotalPremium());
		assertEquals("TIRA", expectedTIRA_EPS1, actualPremium1.getIndividualResponsibleAmt());
	}
	
	/*
	 * Scenario 3.10.2	Mid-Month Start & End- COMPLEX HISTORY- Baby mid-June 
	 * - SBM Prorates ALL Partial periods (SBMI sent Jan'18) 
	 */
	@Test
	public void test_processInboundPremiums_SBM_Prorates_ALL_Partial_periods_MultipleFinancialNodes() {

		LocalDate esd = JAN_15;
		LocalDate eed = DEC_15;
		BigDecimal aptc = new BigDecimal("100");
		BigDecimal tpa = new BigDecimal("200");
		BigDecimal tira = new BigDecimal("100");

		// Expected data after replacement, Record 1
		BigDecimal expectedAPTC_EPS1 = aptc;
		BigDecimal expectedTPA_EPS1 = tpa;
		BigDecimal expectedTIRA_EPS1 = tira;

		PolicyType policy = new PolicyType();
		policy.setPolicyStartDate(DateTimeUtil.getXMLGregorianCalendar(esd));
		policy.setPolicyEndDate(DateTimeUtil.getXMLGregorianCalendar(eed));
		
		FinancialInformation fit = new FinancialInformation();
		
		fit.setFinancialEffectiveStartDate(DateTimeUtil.getXMLGregorianCalendar(esd));
		fit.setFinancialEffectiveEndDate(DateTimeUtil.getXMLGregorianCalendar(JUN_15));
		
		setFinancialInfo(fit, "TPA", tpa);
		setFinancialInfo(fit, "TIRA", tira);
		setFinancialInfo(fit, "APTC", aptc);
		
		ProratedAmountType pat = new ProratedAmountType();
		pat.setPartialMonthEffectiveStartDate(DateTimeUtil.getXMLGregorianCalendar(JAN_15));
		pat.setPartialMonthEffectiveEndDate(DateTimeUtil.getXMLGregorianCalendar(JAN_31));
		
		setProratedValues(pat, "P-APTC", new BigDecimal("50"));
		setProratedValues(pat, "P-TPA", new BigDecimal("100"));
		
		fit.getProratedAmount().add(pat);
		
		
		ProratedAmountType pat2 = new ProratedAmountType();
		pat2.setPartialMonthEffectiveStartDate(DateTimeUtil.getXMLGregorianCalendar(JUN_1));
		pat2.setPartialMonthEffectiveEndDate(DateTimeUtil.getXMLGregorianCalendar(JUN_15));
		
		setProratedValues(pat2, "P-APTC", new BigDecimal("50"));
		setProratedValues(pat2, "P-TPA", new BigDecimal("100"));
		
		fit.getProratedAmount().add(pat2);
		
		policy.getFinancialInformation().add(fit);
		
		//Financial node 2
		FinancialInformation fit2 = new FinancialInformation();
		
		fit2.setFinancialEffectiveStartDate(DateTimeUtil.getXMLGregorianCalendar(JUN_15.plusDays(1)));
		fit2.setFinancialEffectiveEndDate(DateTimeUtil.getXMLGregorianCalendar(DEC_15));
		
		setFinancialInfo(fit2, "TPA", new BigDecimal("300"));
		setFinancialInfo(fit2, "TIRA", new BigDecimal("150"));
		setFinancialInfo(fit2, "APTC", new BigDecimal("150"));
		
		ProratedAmountType pat21 = new ProratedAmountType();
		pat21.setPartialMonthEffectiveStartDate(DateTimeUtil.getXMLGregorianCalendar(JUN_15.plusDays(1)));
		pat21.setPartialMonthEffectiveEndDate(DateTimeUtil.getXMLGregorianCalendar(JUN_30));
		
		setProratedValues(pat21, "P-APTC", new BigDecimal("75"));
		setProratedValues(pat21, "P-TPA", new BigDecimal("150"));
		
		fit2.getProratedAmount().add(pat21);
		
		
		ProratedAmountType pat22 = new ProratedAmountType();
		pat22.setPartialMonthEffectiveStartDate(DateTimeUtil.getXMLGregorianCalendar(DEC_1));
		pat22.setPartialMonthEffectiveEndDate(DateTimeUtil.getXMLGregorianCalendar(DEC_15));
		
		setProratedValues(pat22, "P-APTC", new BigDecimal("75"));
		setProratedValues(pat22, "P-TPA", new BigDecimal("150"));
		
		fit2.getProratedAmount().add(pat22);
		
		policy.getFinancialInformation().add(fit2);
		
		
		SBMPolicyDTO policyDTO = new SBMPolicyDTO();
		policyDTO.setPolicy(policy);

		Map<LocalDate, SBMPremium> epsPremiums = sbmFinancialValidator.processInboundPremiums(policy);
		
		assertTrue("Premiums list", MapUtils.isNotEmpty(epsPremiums));
		assertEquals("Premiums list", 6, epsPremiums.size());

		SBMPremium actualPremium1 = epsPremiums.get(JAN_15);

		assertEquals("ESD", JAN_15, actualPremium1.getEffectiveStartDate());
		assertEquals("EED", JAN_31, actualPremium1.getEffectiveEndDate());
		assertEquals("APTC", expectedAPTC_EPS1, actualPremium1.getAptc());
		assertEquals("TPA", expectedTPA_EPS1, actualPremium1.getTotalPremium());
		assertEquals("TIRA", expectedTIRA_EPS1, actualPremium1.getIndividualResponsibleAmt());
		assertEquals("P-APTC", new BigDecimal("50"), actualPremium1.getProratedAptc());
		assertEquals("P-TPA", new BigDecimal("100"), actualPremium1.getProratedPremium());
		
		SBMPremium actualPremium2 = epsPremiums.get(FEB_1);

		assertEquals("ESD", FEB_1, actualPremium2.getEffectiveStartDate());
		assertEquals("EED", MAY_31, actualPremium2.getEffectiveEndDate());
		assertEquals("APTC", expectedAPTC_EPS1, actualPremium2.getAptc());
		assertEquals("TPA", expectedTPA_EPS1, actualPremium2.getTotalPremium());
		assertEquals("TIRA", expectedTIRA_EPS1, actualPremium2.getIndividualResponsibleAmt());
		
		SBMPremium actualPremium3 = epsPremiums.get(JUN_1);

		assertEquals("ESD", JUN_1, actualPremium3.getEffectiveStartDate());
		assertEquals("EED", JUN_15, actualPremium3.getEffectiveEndDate());
		assertEquals("APTC", expectedAPTC_EPS1, actualPremium3.getAptc());
		assertEquals("TPA", expectedTPA_EPS1, actualPremium3.getTotalPremium());
		assertEquals("TIRA", expectedTIRA_EPS1, actualPremium3.getIndividualResponsibleAmt());
		assertEquals("P-APTC", new BigDecimal("50"), actualPremium3.getProratedAptc());
		assertEquals("P-TPA", new BigDecimal("100"), actualPremium3.getProratedPremium());
		
		SBMPremium actualPremium4 = epsPremiums.get(JUN_15.plusDays(1));

		assertEquals("ESD", JUN_15.plusDays(1), actualPremium4.getEffectiveStartDate());
		assertEquals("EED", JUN_30, actualPremium4.getEffectiveEndDate());
		assertEquals("APTC", new BigDecimal("150"), actualPremium4.getAptc());
		assertEquals("TPA", new BigDecimal("300"), actualPremium4.getTotalPremium());
		assertEquals("P-APTC", new BigDecimal("75"), actualPremium4.getProratedAptc());
		assertEquals("P-TPA", new BigDecimal("150"), actualPremium4.getProratedPremium());
		
		SBMPremium actualPremium5 = epsPremiums.get(JUL_1);

		assertEquals("ESD", JUL_1, actualPremium5.getEffectiveStartDate());
		assertEquals("EED", NOV_30, actualPremium5.getEffectiveEndDate());
		assertEquals("APTC", new BigDecimal("150"), actualPremium5.getAptc());
		assertEquals("TPA", new BigDecimal("300"), actualPremium5.getTotalPremium());
		
		SBMPremium actualPremium6 = epsPremiums.get(DEC_1);

		assertEquals("ESD", DEC_1, actualPremium6.getEffectiveStartDate());
		assertEquals("EED", DEC_15, actualPremium6.getEffectiveEndDate());
		assertEquals("APTC", new BigDecimal("150"), actualPremium6.getAptc());
		assertEquals("TPA", new BigDecimal("300"), actualPremium6.getTotalPremium());
		assertEquals("P-APTC", new BigDecimal("75"), actualPremium6.getProratedAptc());
		assertEquals("P-TPA", new BigDecimal("150"), actualPremium6.getProratedPremium());

	}

	/*
	 * Scenario 3.10.8	3.9.83.10.8	Mid-Month End- Simple History 
	 * - SBM Does not Prorate Mid-Month End (SBMI sent May'17)
	 */
	@Test
	public void test_processInboundPremiums_SBM_does_not_Prorate_Mid_Month_End() {

		LocalDate esd = JAN_1;
		LocalDate eed = APR_30;
		BigDecimal aptc = new BigDecimal("100");
		BigDecimal tpa = new BigDecimal("200");
		BigDecimal tira = new BigDecimal("100");

		// Expected data after replacement, Record 1
		BigDecimal expectedAPTC_EPS1 = aptc;
		BigDecimal expectedTPA_EPS1 = tpa;
		BigDecimal expectedTIRA_EPS1 = tira;

		PolicyType policy = new PolicyType();
		policy.setPolicyStartDate(DateTimeUtil.getXMLGregorianCalendar(esd));
		policy.setPolicyEndDate(DateTimeUtil.getXMLGregorianCalendar(APR_15));
		
		FinancialInformation fit = new FinancialInformation();
		
		fit.setFinancialEffectiveStartDate(DateTimeUtil.getXMLGregorianCalendar(esd));
		fit.setFinancialEffectiveEndDate(DateTimeUtil.getXMLGregorianCalendar(eed));
		
		setFinancialInfo(fit, "TPA", tpa);
		setFinancialInfo(fit, "TIRA", tira);
		setFinancialInfo(fit, "APTC", aptc);
		
		policy.getFinancialInformation().add(fit);
		
		SBMPolicyDTO policyDTO = new SBMPolicyDTO();
		policyDTO.setPolicy(policy);

		Map<LocalDate, SBMPremium> epsPremiums = sbmFinancialValidator.processInboundPremiums(policy);
		
		assertTrue("Premiums list", MapUtils.isNotEmpty(epsPremiums));
		assertEquals("Premiums list", 1, epsPremiums.size());

		SBMPremium actualPremium1 = epsPremiums.get(JAN_1);

		assertEquals("ESD", JAN_1, actualPremium1.getEffectiveStartDate());
		assertEquals("EED", APR_30, actualPremium1.getEffectiveEndDate());
		assertEquals("APTC", expectedAPTC_EPS1, actualPremium1.getAptc());
		assertEquals("TPA", expectedTPA_EPS1, actualPremium1.getTotalPremium());
		assertEquals("TIRA", expectedTIRA_EPS1, actualPremium1.getIndividualResponsibleAmt());
	}
	
	/*
	 * validateFinancialInfo
	 */
	@Test
	public void test_validateFinancialInfo_Overlap() {
		
		String qhpId = RandomStringUtils.randomAlphanumeric(16).toUpperCase();
		String stateCd = qhpId.substring(5, 7);
		
		List<String> bizRules = Arrays.asList("R001");
		SBMCache.getBusinessRules(stateCd).addAll(bizRules);
		
		expect(mockSbmDataService.getMetalLevelByQhpid(EasyMock.anyString(), EasyMock.anyString()))
		.andReturn(null).anyTimes();
		replay(mockSbmDataService);
		
		PolicyType policy = new PolicyType();
		policy.setPolicyStartDate(DateTimeUtil.getXMLGregorianCalendar(JAN_1));
		policy.setPolicyEndDate(DateTimeUtil.getXMLGregorianCalendar(APR_30));
		policy.setQHPId(qhpId);
		
		FinancialInformation fit = new FinancialInformation();
		
		fit.setFinancialEffectiveStartDate(DateTimeUtil.getXMLGregorianCalendar(JAN_1));
		fit.setFinancialEffectiveEndDate(DateTimeUtil.getXMLGregorianCalendar(JAN_31));
		
		policy.getFinancialInformation().add(fit);
		
		FinancialInformation fit2 = new FinancialInformation();
		
		fit2.setFinancialEffectiveStartDate(DateTimeUtil.getXMLGregorianCalendar(JAN_15));
		fit2.setFinancialEffectiveEndDate(DateTimeUtil.getXMLGregorianCalendar(APR_30));
		
		policy.getFinancialInformation().add(fit2);
		
		List<SbmErrWarningLogDTO> financialErrors = sbmFinancialValidator.validateFinancialInfo(policy);
		
		assertFalse("financialErrors", financialErrors.isEmpty());
		assertEquals("financialErrors", 1, financialErrors.size());
		
		SbmErrWarningLogDTO financialError = financialErrors.get(0);
		
		assertEquals("ElementInError", "FinancialEffectiveStartDate", financialError.getElementInError());
		assertEquals("Error Code", "ER-033", financialError.getErrorWarningTypeCd());
	}
	
	/*
	 * validateFinancialInfo
	 */
	@Test
	public void test_validateFinancialInfo_No_Overlap() {
		
		String qhpId = RandomStringUtils.randomAlphanumeric(16).toUpperCase();
		String stateCd = qhpId.substring(5, 7);
		
		List<String> bizRules = Arrays.asList("R001");
		SBMCache.getBusinessRules(stateCd).addAll(bizRules);
		
		expect(mockSbmDataService.getMetalLevelByQhpid(EasyMock.anyString(), EasyMock.anyString()))
		.andReturn(null).anyTimes();
		replay(mockSbmDataService);
		
		PolicyType policy = new PolicyType();
		policy.setPolicyStartDate(DateTimeUtil.getXMLGregorianCalendar(JAN_1));
		policy.setPolicyEndDate(DateTimeUtil.getXMLGregorianCalendar(APR_30));
		policy.setQHPId(RandomStringUtils.randomAlphanumeric(16).toUpperCase());
		
		FinancialInformation fit = new FinancialInformation();
		
		fit.setFinancialEffectiveStartDate(DateTimeUtil.getXMLGregorianCalendar(JAN_1));
		fit.setFinancialEffectiveEndDate(DateTimeUtil.getXMLGregorianCalendar(JAN_31));
		
		policy.getFinancialInformation().add(fit);
		
		FinancialInformation fit2 = new FinancialInformation();
		
		fit2.setFinancialEffectiveStartDate(DateTimeUtil.getXMLGregorianCalendar(FEB_1));
		fit2.setFinancialEffectiveEndDate(DateTimeUtil.getXMLGregorianCalendar(APR_30));
		
		policy.getFinancialInformation().add(fit2);
		
		List<SbmErrWarningLogDTO> financialErrors = sbmFinancialValidator.validateFinancialInfo(policy);
		
		assertTrue("financialErrors", financialErrors.isEmpty());
	}
	
	/*
	 * validateFinancialInfo
	 */
	@Test
	public void test_validateFinancialInfo_Gap() {
		
		String qhpId = RandomStringUtils.randomAlphanumeric(16).toUpperCase();
		String stateCd = qhpId.substring(5, 7);
		
		List<String> bizRules = Arrays.asList("R002");
		SBMCache.getBusinessRules(stateCd).addAll(bizRules);
		
		expect(mockSbmDataService.getMetalLevelByQhpid(EasyMock.anyString(), EasyMock.anyString()))
		.andReturn(null).anyTimes();
		replay(mockSbmDataService);
		
		PolicyType policy = new PolicyType();
		policy.setPolicyStartDate(DateTimeUtil.getXMLGregorianCalendar(JAN_1));
		policy.setPolicyEndDate(DateTimeUtil.getXMLGregorianCalendar(APR_30));
		policy.setQHPId(qhpId);
		
		FinancialInformation fit = new FinancialInformation();
		
		fit.setFinancialEffectiveStartDate(DateTimeUtil.getXMLGregorianCalendar(JAN_1));
		fit.setFinancialEffectiveEndDate(DateTimeUtil.getXMLGregorianCalendar(JAN_31));
		
		policy.getFinancialInformation().add(fit);
		
		FinancialInformation fit2 = new FinancialInformation();
		
		fit2.setFinancialEffectiveStartDate(DateTimeUtil.getXMLGregorianCalendar(FEB_15));
		fit2.setFinancialEffectiveEndDate(DateTimeUtil.getXMLGregorianCalendar(APR_30));
		
		policy.getFinancialInformation().add(fit2);
		
		List<SbmErrWarningLogDTO> financialErrors = sbmFinancialValidator.validateFinancialInfo(policy);
		
		assertFalse("financialErrors", financialErrors.isEmpty());
		assertEquals("financialErrors", 1, financialErrors.size());
		
		SbmErrWarningLogDTO financialError = financialErrors.get(0);
		
		assertEquals("ElementInError", "FinancialEffectiveStartDate", financialError.getElementInError());
		assertEquals("Error Code", "ER-034", financialError.getErrorWarningTypeCd());
	}
	
	/*
	 * validateFinancialInfo
	 */
	@Test
	public void test_validateFinancialInfo_No_Gap() {
		
		String qhpId = RandomStringUtils.randomAlphanumeric(16).toUpperCase();
		String stateCd = qhpId.substring(5, 7);
		
		List<String> bizRules = Arrays.asList("R002");
		SBMCache.getBusinessRules(stateCd).addAll(bizRules);
		
		expect(mockSbmDataService.getMetalLevelByQhpid(EasyMock.anyString(), EasyMock.anyString()))
		.andReturn(null).anyTimes();
		replay(mockSbmDataService);
		
		PolicyType policy = new PolicyType();
		policy.setPolicyStartDate(DateTimeUtil.getXMLGregorianCalendar(JAN_1));
		policy.setPolicyEndDate(DateTimeUtil.getXMLGregorianCalendar(APR_30));
		policy.setQHPId(qhpId);
		
		FinancialInformation fit = new FinancialInformation();
		
		fit.setFinancialEffectiveStartDate(DateTimeUtil.getXMLGregorianCalendar(JAN_1));
		fit.setFinancialEffectiveEndDate(DateTimeUtil.getXMLGregorianCalendar(JAN_31));
		
		policy.getFinancialInformation().add(fit);
		
		FinancialInformation fit2 = new FinancialInformation();
		
		fit2.setFinancialEffectiveStartDate(DateTimeUtil.getXMLGregorianCalendar(FEB_1));
		fit2.setFinancialEffectiveEndDate(DateTimeUtil.getXMLGregorianCalendar(APR_30));
		
		policy.getFinancialInformation().add(fit2);
		
		List<SbmErrWarningLogDTO> financialErrors = sbmFinancialValidator.validateFinancialInfo(policy);
		
		assertTrue("financialErrors", financialErrors.isEmpty());
	}
	
	/*
	 * performStateSpecificFinancialValidations
	 */
	@Test
	public void test_performStateSpecificValidations_R003_Invalid() {
		
		String qhpId = RandomStringUtils.randomAlphanumeric(16).toUpperCase();
		String stateCd = qhpId.substring(5, 7);
		
		List<String> bizRules = Arrays.asList("R003");
		SBMCache.getBusinessRules(stateCd).addAll(bizRules);
		
		expect(mockSbmDataService.getMetalLevelByQhpid(EasyMock.anyString(), EasyMock.anyString()))
		.andReturn(null).anyTimes();
		replay(mockSbmDataService);	
		
		PolicyType policy = new PolicyType();
		policy.setPolicyStartDate(DateTimeUtil.getXMLGregorianCalendar(JAN_1));
		policy.setPolicyEndDate(DateTimeUtil.getXMLGregorianCalendar(APR_30));
		policy.setQHPId(qhpId);
		
		FinancialInformation fit = new FinancialInformation();
		
		fit.setFinancialEffectiveStartDate(DateTimeUtil.getXMLGregorianCalendar(JAN_15));
		fit.setFinancialEffectiveEndDate(DateTimeUtil.getXMLGregorianCalendar(JAN_31));
		
		policy.getFinancialInformation().add(fit);
		
		List<SbmErrWarningLogDTO> stateFinErrors = sbmFinancialValidator.validateFinancialInfo(policy);
		
		assertFalse("stateFinErrors", stateFinErrors.isEmpty());
		assertEquals("financialErrors", 1, stateFinErrors.size());
		
		SbmErrWarningLogDTO financialError = stateFinErrors.get(0);
		
		assertEquals("ElementInError", "FinancialEffectiveStartDate", financialError.getElementInError());
		assertEquals("Error Code", "ER-035", financialError.getErrorWarningTypeCd());
	}
	
	/*
	 * performStateSpecificFinancialValidations
	 */
	@Test
	public void test_performStateSpecificValidations_R003_Valid() {
		
		String qhpId = RandomStringUtils.randomAlphanumeric(16).toUpperCase();
		String stateCd = qhpId.substring(5, 7);
		
		List<String> bizRules = Arrays.asList("R003");
		SBMCache.getBusinessRules(stateCd).addAll(bizRules);
		
		expect(mockSbmDataService.getMetalLevelByQhpid(EasyMock.anyString(), EasyMock.anyString()))
		.andReturn(null).anyTimes();
		replay(mockSbmDataService);	
		
		PolicyType policy = new PolicyType();
		policy.setPolicyStartDate(DateTimeUtil.getXMLGregorianCalendar(JAN_1));
		policy.setPolicyEndDate(DateTimeUtil.getXMLGregorianCalendar(APR_30));
		policy.setQHPId(qhpId);
		
		FinancialInformation fit = new FinancialInformation();
		
		fit.setFinancialEffectiveStartDate(DateTimeUtil.getXMLGregorianCalendar(JAN_1));
		fit.setFinancialEffectiveEndDate(DateTimeUtil.getXMLGregorianCalendar(JAN_31));
		
		policy.getFinancialInformation().add(fit);
		
		List<SbmErrWarningLogDTO> stateFinErrors = sbmFinancialValidator.validateFinancialInfo(policy);
		
		assertTrue("stateFinErrors", stateFinErrors.isEmpty());
	}
	
	/*
	 * performStateSpecificFinancialValidations
	 */
	@Test
	public void test_performStateSpecificValidations_R006_Invalid() {
		
		String qhpId = RandomStringUtils.randomAlphanumeric(16).toUpperCase();
		String stateCd = qhpId.substring(5, 7);
		
		List<String> bizRules = Arrays.asList("R006");
		SBMCache.getBusinessRules(stateCd).addAll(bizRules);
		
		expect(mockSbmDataService.getMetalLevelByQhpid(EasyMock.anyString(), EasyMock.anyString()))
		.andReturn(null).anyTimes();
		replay(mockSbmDataService);	
		
		PolicyType policy = new PolicyType();
		policy.setPolicyStartDate(DateTimeUtil.getXMLGregorianCalendar(JAN_1));
		policy.setPolicyEndDate(DateTimeUtil.getXMLGregorianCalendar(APR_30));
		policy.setQHPId(qhpId);
		
		FinancialInformation fit = new FinancialInformation();
		
		fit.setFinancialEffectiveStartDate(DateTimeUtil.getXMLGregorianCalendar(MAR_15));
		fit.setFinancialEffectiveEndDate(DateTimeUtil.getXMLGregorianCalendar(APR_30));
		
		policy.getFinancialInformation().add(fit);
		
		List<SbmErrWarningLogDTO> stateFinErrors = sbmFinancialValidator.validateFinancialInfo(policy);
		
		assertFalse("stateFinErrors", stateFinErrors.isEmpty());
		assertEquals("financialErrors", 1, stateFinErrors.size());
		
		SbmErrWarningLogDTO financialError = stateFinErrors.get(0);
		
		assertEquals("ElementInError", "FinancialEffectiveStartDate", financialError.getElementInError());
		assertEquals("Error Code", "ER-058", financialError.getErrorWarningTypeCd());
	}
	
	/*
	 * performStateSpecificFinancialValidations
	 */
	@Test
	public void test_performStateSpecificValidations_R006_Valid() {

		String qhpId = RandomStringUtils.randomAlphanumeric(16).toUpperCase();
		String stateCd = qhpId.substring(5, 7);
		
		List<String> bizRules = Arrays.asList("R006");
		SBMCache.getBusinessRules(stateCd).addAll(bizRules);
		
		expect(mockSbmDataService.getMetalLevelByQhpid(EasyMock.anyString(), EasyMock.anyString()))
		.andReturn(null).anyTimes();
		replay(mockSbmDataService);	
		
		PolicyType policy = new PolicyType();
		policy.setPolicyStartDate(DateTimeUtil.getXMLGregorianCalendar(JAN_1));
		policy.setPolicyEndDate(DateTimeUtil.getXMLGregorianCalendar(APR_30));
		policy.setQHPId(qhpId);
		
		FinancialInformation fit = new FinancialInformation();
		
		fit.setFinancialEffectiveStartDate(DateTimeUtil.getXMLGregorianCalendar(FEB_1));
		fit.setFinancialEffectiveEndDate(DateTimeUtil.getXMLGregorianCalendar(APR_30));
		
		policy.getFinancialInformation().add(fit);
		
		List<SbmErrWarningLogDTO> stateFinErrors = sbmFinancialValidator.validateFinancialInfo(policy);
		
		assertTrue("stateFinErrors", stateFinErrors.isEmpty());
	}
	
	/*
	 * performStateSpecificFinancialValidations
	 */
	@Test
	public void test_performStateSpecificValidations_R006_Valid_SameMonth() {
		
		String qhpId = RandomStringUtils.randomAlphanumeric(16).toUpperCase();
		String stateCd = qhpId.substring(5, 7);
		
		List<String> bizRules = Arrays.asList("R006");
		SBMCache.getBusinessRules(stateCd).addAll(bizRules);
		
		expect(mockSbmDataService.getMetalLevelByQhpid(EasyMock.anyString(), EasyMock.anyString()))
		.andReturn(null).anyTimes();
		replay(mockSbmDataService);	
		
		PolicyType policy = new PolicyType();
		policy.setPolicyStartDate(DateTimeUtil.getXMLGregorianCalendar(JAN_1));
		policy.setPolicyEndDate(DateTimeUtil.getXMLGregorianCalendar(APR_30));
		policy.setQHPId(qhpId);
		
		FinancialInformation fit = new FinancialInformation();
		
		fit.setFinancialEffectiveStartDate(DateTimeUtil.getXMLGregorianCalendar(JAN_15));
		fit.setFinancialEffectiveEndDate(DateTimeUtil.getXMLGregorianCalendar(APR_30));
		
		policy.getFinancialInformation().add(fit);
		
		List<SbmErrWarningLogDTO> stateFinErrors = sbmFinancialValidator.validateFinancialInfo(policy);		
		assertTrue("stateFinErrors", stateFinErrors.isEmpty());
	}
	
	/*
	 * performStateSpecificFinancialValidations
	 */
	@Test
	public void test_performStateSpecificValidations_R007_Invalid() {
		
		String qhpId = RandomStringUtils.randomAlphanumeric(16).toUpperCase();
		String stateCd = qhpId.substring(5, 7);
		
		List<String> bizRules = Arrays.asList("R007");
		SBMCache.getBusinessRules(stateCd).addAll(bizRules);
		
		expect(mockSbmDataService.getMetalLevelByQhpid(EasyMock.anyString(), EasyMock.anyString()))
		.andReturn(null).anyTimes();
		replay(mockSbmDataService);	
		
		PolicyType policy = new PolicyType();
		policy.setPolicyStartDate(DateTimeUtil.getXMLGregorianCalendar(FEB_1));
		policy.setPolicyEndDate(DateTimeUtil.getXMLGregorianCalendar(APR_30));
		policy.setQHPId(qhpId);
		
		FinancialInformation fit = new FinancialInformation();
		
		fit.setFinancialEffectiveStartDate(DateTimeUtil.getXMLGregorianCalendar(JAN_15));
		fit.setFinancialEffectiveEndDate(DateTimeUtil.getXMLGregorianCalendar(APR_30));
		
		policy.getFinancialInformation().add(fit);
		
		List<SbmErrWarningLogDTO> stateFinErrors = sbmFinancialValidator.validateFinancialInfo(policy);
		
		assertFalse("stateFinErrors", stateFinErrors.isEmpty());
		assertEquals("financialErrors", 1, stateFinErrors.size());
		
		SbmErrWarningLogDTO financialError = stateFinErrors.get(0);
		
		assertEquals("ElementInError", "FinancialEffectiveStartDate", financialError.getElementInError());
		assertEquals("Error Code", "ER-059", financialError.getErrorWarningTypeCd());
	}
	
	/*
	 * performStateSpecificFinancialValidations
	 */
	@Test
	public void test_performStateSpecificValidations_R007_Valid() {
		
		String qhpId = RandomStringUtils.randomAlphanumeric(16).toUpperCase();
		String stateCd = qhpId.substring(5, 7);
		
		List<String> bizRules = Arrays.asList("R007");
		SBMCache.getBusinessRules(stateCd).addAll(bizRules);
		
		expect(mockSbmDataService.getMetalLevelByQhpid(EasyMock.anyString(), EasyMock.anyString()))
		.andReturn(null).anyTimes();
		replay(mockSbmDataService);	
		
		PolicyType policy = new PolicyType();
		policy.setPolicyStartDate(DateTimeUtil.getXMLGregorianCalendar(JAN_1));
		policy.setPolicyEndDate(DateTimeUtil.getXMLGregorianCalendar(APR_30));
		policy.setQHPId(qhpId);
		
		FinancialInformation fit = new FinancialInformation();
		
		fit.setFinancialEffectiveStartDate(DateTimeUtil.getXMLGregorianCalendar(JAN_1));
		fit.setFinancialEffectiveEndDate(DateTimeUtil.getXMLGregorianCalendar(APR_30));
		
		policy.getFinancialInformation().add(fit);
		
		List<SbmErrWarningLogDTO> stateFinErrors = sbmFinancialValidator.validateFinancialInfo(policy);		
		assertTrue("stateFinErrors", stateFinErrors.isEmpty());
	}
	
	/*
	 * performStateSpecificFinancialValidations
	 */
	@Test
	public void test_performStateSpecificValidations_R007_Valid_SameMonth() {

		String qhpId = RandomStringUtils.randomAlphanumeric(16).toUpperCase();
		String stateCd = qhpId.substring(5, 7);
		
		List<String> bizRules = Arrays.asList("R007");
		SBMCache.getBusinessRules(stateCd).addAll(bizRules);
		
		expect(mockSbmDataService.getMetalLevelByQhpid(EasyMock.anyString(), EasyMock.anyString()))
		.andReturn(null).anyTimes();
		replay(mockSbmDataService);	
		
		PolicyType policy = new PolicyType();
		policy.setPolicyStartDate(DateTimeUtil.getXMLGregorianCalendar(JAN_1));
		policy.setPolicyEndDate(DateTimeUtil.getXMLGregorianCalendar(APR_30));
		policy.setQHPId(qhpId);
		
		FinancialInformation fit = new FinancialInformation();
		
		fit.setFinancialEffectiveStartDate(DateTimeUtil.getXMLGregorianCalendar(JAN_15));
		fit.setFinancialEffectiveEndDate(DateTimeUtil.getXMLGregorianCalendar(APR_30));
		
		policy.getFinancialInformation().add(fit);
		
		List<SbmErrWarningLogDTO> stateFinErrors = sbmFinancialValidator.validateFinancialInfo(policy);	
		
		assertTrue("stateFinErrors", stateFinErrors.isEmpty());
	}
	
	/*
	 * performStateSpecificFinancialValidations
	 */
	@Test
	public void test_performStateSpecificValidations_R004_Invalid_Lesser_FinEnd() {

		String qhpId = RandomStringUtils.randomAlphanumeric(16).toUpperCase();
		String stateCd = qhpId.substring(5, 7);
		
		List<String> bizRules = Arrays.asList("R004");
		SBMCache.getBusinessRules(stateCd).addAll(bizRules);
		
		expect(mockSbmDataService.getMetalLevelByQhpid(EasyMock.anyString(), EasyMock.anyString()))
		.andReturn(null).anyTimes();
		replay(mockSbmDataService);	
		
		PolicyType policy = new PolicyType();
		policy.setPolicyStartDate(DateTimeUtil.getXMLGregorianCalendar(JAN_1));
		policy.setPolicyEndDate(DateTimeUtil.getXMLGregorianCalendar(APR_30));
		policy.setQHPId(qhpId);
		policy.setEffectuationIndicator("Y");
		
		FinancialInformation fit = new FinancialInformation();
		fit.setFinancialEffectiveStartDate(DateTimeUtil.getXMLGregorianCalendar(JAN_1));
		fit.setFinancialEffectiveEndDate(DateTimeUtil.getXMLGregorianCalendar(JAN_31));
		policy.getFinancialInformation().add(fit);
		
		FinancialInformation fit2 = new FinancialInformation();
		fit2.setFinancialEffectiveStartDate(DateTimeUtil.getXMLGregorianCalendar(FEB_1));
		fit2.setFinancialEffectiveEndDate(DateTimeUtil.getXMLGregorianCalendar(MAR_31));
		policy.getFinancialInformation().add(fit2);
		
		List<SbmErrWarningLogDTO> stateFinErrors = sbmFinancialValidator.validateFinancialInfo(policy);	
		
		assertFalse("stateFinErrors", stateFinErrors.isEmpty());
		assertEquals("financialErrors", 1, stateFinErrors.size());
		
		SbmErrWarningLogDTO financialError = stateFinErrors.get(0);
		
		assertEquals("ElementInError", "FinancialEffectiveEndDate", financialError.getElementInError());
		assertEquals("Error Code", "ER-036", financialError.getErrorWarningTypeCd());
	}
	
	/*
	 * performStateSpecificFinancialValidations
	 */
	@Test
	public void test_performStateSpecificValidations_R004_Invalid_Greater_FinEnd() {
		
		String qhpId = RandomStringUtils.randomAlphanumeric(16).toUpperCase();
		String stateCd = qhpId.substring(5, 7);
		
		List<String> bizRules = Arrays.asList("R004");
		SBMCache.getBusinessRules(stateCd).addAll(bizRules);
		
		expect(mockSbmDataService.getMetalLevelByQhpid(EasyMock.anyString(), EasyMock.anyString()))
		.andReturn(null).anyTimes();
		replay(mockSbmDataService);	
		
		PolicyType policy = new PolicyType();
		policy.setPolicyStartDate(DateTimeUtil.getXMLGregorianCalendar(JAN_1));
		policy.setPolicyEndDate(DateTimeUtil.getXMLGregorianCalendar(APR_30));
		policy.setQHPId(qhpId);
		policy.setEffectuationIndicator("Y");
		
		FinancialInformation fit = new FinancialInformation();
		fit.setFinancialEffectiveStartDate(DateTimeUtil.getXMLGregorianCalendar(JAN_1));
		fit.setFinancialEffectiveEndDate(DateTimeUtil.getXMLGregorianCalendar(JAN_31));
		policy.getFinancialInformation().add(fit);
		
		FinancialInformation fit2 = new FinancialInformation();
		fit2.setFinancialEffectiveStartDate(DateTimeUtil.getXMLGregorianCalendar(FEB_1));
		fit2.setFinancialEffectiveEndDate(DateTimeUtil.getXMLGregorianCalendar(JUN_30));
		policy.getFinancialInformation().add(fit2);
		
		List<SbmErrWarningLogDTO> stateFinErrors = sbmFinancialValidator.validateFinancialInfo(policy);	
		
		assertFalse("stateFinErrors", stateFinErrors.isEmpty());
		assertEquals("financialErrors", 1, stateFinErrors.size());
		
		SbmErrWarningLogDTO financialError = stateFinErrors.get(0);
		
		assertEquals("ElementInError", "FinancialEffectiveEndDate", financialError.getElementInError());
		assertEquals("Error Code", "ER-036", financialError.getErrorWarningTypeCd());
	}
	
	/*
	 * performStateSpecificFinancialValidations
	 */
	@Test
	public void test_performStateSpecificValidations_R004_Valid_SameMonth_FinEnd() {
		
		String qhpId = RandomStringUtils.randomAlphanumeric(16).toUpperCase();
		String stateCd = qhpId.substring(5, 7);
		
		List<String> bizRules = Arrays.asList("R004");
		SBMCache.getBusinessRules(stateCd).addAll(bizRules);
		
		expect(mockSbmDataService.getMetalLevelByQhpid(EasyMock.anyString(), EasyMock.anyString()))
		.andReturn(null).anyTimes();
		replay(mockSbmDataService);	
		
		PolicyType policy = new PolicyType();
		policy.setPolicyStartDate(DateTimeUtil.getXMLGregorianCalendar(JAN_1));
		policy.setPolicyEndDate(DateTimeUtil.getXMLGregorianCalendar(APR_30));
		policy.setQHPId(qhpId);
		policy.setEffectuationIndicator("Y");
		
		FinancialInformation fit = new FinancialInformation();
		fit.setFinancialEffectiveStartDate(DateTimeUtil.getXMLGregorianCalendar(JAN_1));
		fit.setFinancialEffectiveEndDate(DateTimeUtil.getXMLGregorianCalendar(JAN_31));
		policy.getFinancialInformation().add(fit);
		
		FinancialInformation fit2 = new FinancialInformation();
		fit2.setFinancialEffectiveStartDate(DateTimeUtil.getXMLGregorianCalendar(FEB_1));
		fit2.setFinancialEffectiveEndDate(DateTimeUtil.getXMLGregorianCalendar(APR_30.minusDays(10)));
		policy.getFinancialInformation().add(fit2);
		
		List<SbmErrWarningLogDTO> stateFinErrors = sbmFinancialValidator.validateFinancialInfo(policy);	
		
		assertTrue("stateFinErrors", stateFinErrors.isEmpty());
	}
	
	/*
	 * performStateSpecificFinancialValidations
	 */
	@Test
	public void test_performStateSpecificValidations_R004_Valid_Same_FinEndDt() {

		String qhpId = RandomStringUtils.randomAlphanumeric(16).toUpperCase();
		String stateCd = qhpId.substring(5, 7);
		
		List<String> bizRules = Arrays.asList("R004");
		SBMCache.getBusinessRules(stateCd).addAll(bizRules);
		
		expect(mockSbmDataService.getMetalLevelByQhpid(EasyMock.anyString(), EasyMock.anyString()))
		.andReturn(null).anyTimes();
		replay(mockSbmDataService);	
		
		PolicyType policy = new PolicyType();
		policy.setPolicyStartDate(DateTimeUtil.getXMLGregorianCalendar(JAN_1));
		policy.setPolicyEndDate(DateTimeUtil.getXMLGregorianCalendar(APR_30));
		policy.setQHPId(qhpId);
		policy.setEffectuationIndicator("Y");
		
		FinancialInformation fit = new FinancialInformation();
		fit.setFinancialEffectiveStartDate(DateTimeUtil.getXMLGregorianCalendar(JAN_1));
		fit.setFinancialEffectiveEndDate(DateTimeUtil.getXMLGregorianCalendar(JAN_31));
		policy.getFinancialInformation().add(fit);
		
		FinancialInformation fit2 = new FinancialInformation();
		fit2.setFinancialEffectiveStartDate(DateTimeUtil.getXMLGregorianCalendar(FEB_1));
		fit2.setFinancialEffectiveEndDate(DateTimeUtil.getXMLGregorianCalendar(APR_30));
		policy.getFinancialInformation().add(fit2);
		
		List<SbmErrWarningLogDTO> stateFinErrors = sbmFinancialValidator.validateFinancialInfo(policy);	
		
		assertTrue("stateFinErrors", stateFinErrors.isEmpty());
	}
	
	/*
	 * performStateSpecificFinancialValidations
	 */
	@Test
	public void test_performStateSpecificValidations_R004_Valid_UnEffectuated() {
		
		String qhpId = RandomStringUtils.randomAlphanumeric(16).toUpperCase();
		String stateCd = qhpId.substring(5, 7);
		
		List<String> bizRules = Arrays.asList("R004");
		SBMCache.getBusinessRules(stateCd).addAll(bizRules);
		
		expect(mockSbmDataService.getMetalLevelByQhpid(EasyMock.anyString(), EasyMock.anyString()))
		.andReturn(null).anyTimes();
		replay(mockSbmDataService);	
		
		PolicyType policy = new PolicyType();
		policy.setPolicyStartDate(DateTimeUtil.getXMLGregorianCalendar(JAN_1));
		policy.setPolicyEndDate(DateTimeUtil.getXMLGregorianCalendar(APR_30));
		policy.setQHPId(qhpId);
		policy.setEffectuationIndicator("N");
		
		FinancialInformation fit = new FinancialInformation();
		fit.setFinancialEffectiveStartDate(DateTimeUtil.getXMLGregorianCalendar(JAN_1));
		fit.setFinancialEffectiveEndDate(DateTimeUtil.getXMLGregorianCalendar(JAN_31));
		policy.getFinancialInformation().add(fit);
		
		FinancialInformation fit2 = new FinancialInformation();
		fit2.setFinancialEffectiveStartDate(DateTimeUtil.getXMLGregorianCalendar(FEB_1));
		fit2.setFinancialEffectiveEndDate(DateTimeUtil.getXMLGregorianCalendar(JUN_30));
		policy.getFinancialInformation().add(fit2);
		
		List<SbmErrWarningLogDTO> stateFinErrors = sbmFinancialValidator.validateFinancialInfo(policy);	
		
		assertTrue("stateFinErrors", stateFinErrors.isEmpty());
	}
	
	/*
	 * performStateSpecificFinancialValidations
	 */
	@Test
	public void test_performStateSpecificValidations_R008_Invalid_FinStart() {

		String qhpId = RandomStringUtils.randomAlphanumeric(16).toUpperCase();
		String stateCd = qhpId.substring(5, 7);
		
		List<String> bizRules = Arrays.asList("R008");
		SBMCache.getBusinessRules(stateCd).addAll(bizRules);
		
		expect(mockSbmDataService.getMetalLevelByQhpid(EasyMock.anyString(), EasyMock.anyString()))
		.andReturn(null).anyTimes();
		replay(mockSbmDataService);	
		
		PolicyType policy = new PolicyType();
		policy.setPolicyStartDate(DateTimeUtil.getXMLGregorianCalendar(JAN_1));
		policy.setPolicyEndDate(DateTimeUtil.getXMLGregorianCalendar(APR_30));
		policy.setQHPId(qhpId);
		policy.setEffectuationIndicator("Y");
		
		FinancialInformation fit = new FinancialInformation();
		fit.setFinancialEffectiveStartDate(DateTimeUtil.getXMLGregorianCalendar(MAY_1));
		fit.setFinancialEffectiveEndDate(DateTimeUtil.getXMLGregorianCalendar(MAY_31));
		policy.getFinancialInformation().add(fit);
		
		List<SbmErrWarningLogDTO> stateFinErrors = sbmFinancialValidator.validateFinancialInfo(policy);	
		
		assertFalse("stateFinErrors", stateFinErrors.isEmpty());
		assertEquals("financialErrors", 1, stateFinErrors.size());
		
		SbmErrWarningLogDTO financialError = stateFinErrors.get(0);
		
		assertEquals("ElementInError", "FinancialEffectiveStartDate", financialError.getElementInError());
		assertEquals("Error Code", "ER-037", financialError.getErrorWarningTypeCd());
	}
	
	/*
	 * performStateSpecificFinancialValidations
	 */
	@Test
	public void test_performStateSpecificValidations_R008_Valid_Lesser_FinStart() {
		
		String qhpId = RandomStringUtils.randomAlphanumeric(16).toUpperCase();
		String stateCd = qhpId.substring(5, 7);
		
		List<String> bizRules = Arrays.asList("R008");
		SBMCache.getBusinessRules(stateCd).addAll(bizRules);
		
		expect(mockSbmDataService.getMetalLevelByQhpid(EasyMock.anyString(), EasyMock.anyString()))
		.andReturn(null).anyTimes();
		replay(mockSbmDataService);	
		
		PolicyType policy = new PolicyType();
		policy.setPolicyStartDate(DateTimeUtil.getXMLGregorianCalendar(JAN_1));
		policy.setPolicyEndDate(DateTimeUtil.getXMLGregorianCalendar(APR_30));
		policy.setQHPId(qhpId);
		policy.setEffectuationIndicator("Y");
		
		FinancialInformation fit = new FinancialInformation();
		fit.setFinancialEffectiveStartDate(DateTimeUtil.getXMLGregorianCalendar(APR_1));
		fit.setFinancialEffectiveEndDate(DateTimeUtil.getXMLGregorianCalendar(APR_30));
		policy.getFinancialInformation().add(fit);
		
		List<SbmErrWarningLogDTO> stateFinErrors = sbmFinancialValidator.validateFinancialInfo(policy);
		
		assertTrue("stateFinErrors", stateFinErrors.isEmpty());
	}
	
	/*
	 * performStateSpecificFinancialValidations
	 */
	@Test
	public void test_performStateSpecificValidations_R008_Valid_Same_FinStartDt() {
		
		String qhpId = RandomStringUtils.randomAlphanumeric(16).toUpperCase();
		String stateCd = qhpId.substring(5, 7);
		
		List<String> bizRules = Arrays.asList("R008");
		SBMCache.getBusinessRules(stateCd).addAll(bizRules);
		
		expect(mockSbmDataService.getMetalLevelByQhpid(EasyMock.anyString(), EasyMock.anyString()))
		.andReturn(null).anyTimes();
		replay(mockSbmDataService);	
		
		PolicyType policy = new PolicyType();
		policy.setPolicyStartDate(DateTimeUtil.getXMLGregorianCalendar(JAN_1));
		policy.setPolicyEndDate(DateTimeUtil.getXMLGregorianCalendar(APR_30));
		policy.setQHPId(qhpId);
		policy.setEffectuationIndicator("Y");
		
		FinancialInformation fit = new FinancialInformation();
		fit.setFinancialEffectiveStartDate(DateTimeUtil.getXMLGregorianCalendar(APR_30));
		fit.setFinancialEffectiveEndDate(DateTimeUtil.getXMLGregorianCalendar(APR_30));
		policy.getFinancialInformation().add(fit);
		
		FinancialInformation fit2 = new FinancialInformation();
		fit2.setFinancialEffectiveStartDate(DateTimeUtil.getXMLGregorianCalendar(FEB_1));
		fit2.setFinancialEffectiveEndDate(DateTimeUtil.getXMLGregorianCalendar(APR_30.minusDays(10)));
		policy.getFinancialInformation().add(fit2);
		
		List<SbmErrWarningLogDTO> stateFinErrors = sbmFinancialValidator.validateFinancialInfo(policy);
		
		assertTrue("stateFinErrors", stateFinErrors.isEmpty());
	}

	/*
	 * performStateSpecificFinancialValidations
	 */
	@Test
	public void test_performStateSpecificValidations_R008_Valid_UnEffectuated() {
		
		String qhpId = RandomStringUtils.randomAlphanumeric(16).toUpperCase();
		String stateCd = qhpId.substring(5, 7);
		
		List<String> bizRules = Arrays.asList("R008");
		SBMCache.getBusinessRules(stateCd).addAll(bizRules);
		
		expect(mockSbmDataService.getMetalLevelByQhpid(EasyMock.anyString(), EasyMock.anyString()))
		.andReturn(null).anyTimes();
		replay(mockSbmDataService);	
		
		PolicyType policy = new PolicyType();
		policy.setPolicyStartDate(DateTimeUtil.getXMLGregorianCalendar(JAN_1));
		policy.setPolicyEndDate(DateTimeUtil.getXMLGregorianCalendar(APR_30));
		policy.setQHPId(qhpId);
		policy.setEffectuationIndicator("N");
		
		FinancialInformation fit = new FinancialInformation();
		fit.setFinancialEffectiveStartDate(DateTimeUtil.getXMLGregorianCalendar(JUN_1));
		fit.setFinancialEffectiveEndDate(DateTimeUtil.getXMLGregorianCalendar(JUN_30));
		policy.getFinancialInformation().add(fit);
		
		List<SbmErrWarningLogDTO> stateFinErrors = sbmFinancialValidator.validateFinancialInfo(policy);
		
		assertTrue("stateFinErrors", stateFinErrors.isEmpty());
	}
	
	/*
	 * performStateSpecificFinancialValidations
	 */
	@Test
	public void test_performStateSpecificValidations_R008_Valid_Null_EffectuationInd() {

		String qhpId = RandomStringUtils.randomAlphanumeric(16).toUpperCase();
		String stateCd = qhpId.substring(5, 7);
		
		List<String> bizRules = Arrays.asList("R008");
		SBMCache.getBusinessRules(stateCd).addAll(bizRules);
		
		expect(mockSbmDataService.getMetalLevelByQhpid(EasyMock.anyString(), EasyMock.anyString()))
		.andReturn(null).anyTimes();
		replay(mockSbmDataService);	
		
		PolicyType policy = new PolicyType();
		policy.setPolicyStartDate(DateTimeUtil.getXMLGregorianCalendar(JAN_1));
		policy.setPolicyEndDate(DateTimeUtil.getXMLGregorianCalendar(APR_30));
		policy.setQHPId(qhpId);
		
		FinancialInformation fit = new FinancialInformation();
		fit.setFinancialEffectiveStartDate(DateTimeUtil.getXMLGregorianCalendar(JUN_1));
		fit.setFinancialEffectiveEndDate(DateTimeUtil.getXMLGregorianCalendar(JUN_30));
		policy.getFinancialInformation().add(fit);
		
		List<SbmErrWarningLogDTO> stateFinErrors = sbmFinancialValidator.validateFinancialInfo(policy);
		
		assertTrue("stateFinErrors", stateFinErrors.isEmpty());
	}
	
	/*
	 * performStateSpecificFinancialValidations
	 */
	@Test
	public void test_performStateSpecificValidations_R009_inValid() {

		String qhpId = RandomStringUtils.randomAlphanumeric(16).toUpperCase();
		String stateCd = qhpId.substring(5, 7);
		
		List<String> bizRules = Arrays.asList("R009");
		SBMCache.getBusinessRules(stateCd).addAll(bizRules);
		
		expect(mockSbmDataService.getMetalLevelByQhpid(EasyMock.anyString(), EasyMock.anyString()))
		.andReturn(null).anyTimes();
		replay(mockSbmDataService);	
		
		PolicyType policy = new PolicyType();
		policy.setPolicyStartDate(DateTimeUtil.getXMLGregorianCalendar(JAN_1));
		policy.setPolicyEndDate(DateTimeUtil.getXMLGregorianCalendar(APR_30));
		policy.setQHPId(qhpId);
		policy.setEffectuationIndicator("Y");
		
		FinancialInformation fit = new FinancialInformation();
		fit.setFinancialEffectiveStartDate(DateTimeUtil.getXMLGregorianCalendar(JAN_1));
		fit.setFinancialEffectiveEndDate(DateTimeUtil.getXMLGregorianCalendar(JAN_31));
		fit.setMonthlyAPTCAmount(null);
		fit.setMonthlyTotalPremiumAmount(new BigDecimal("500.00"));
		policy.getFinancialInformation().add(fit);
		
		FinancialInformation fit2 = new FinancialInformation();
		fit2.setFinancialEffectiveStartDate(DateTimeUtil.getXMLGregorianCalendar(FEB_1));
		fit2.setFinancialEffectiveEndDate(DateTimeUtil.getXMLGregorianCalendar(APR_30));
		fit2.setMonthlyAPTCAmount(new BigDecimal("550.00"));
		fit2.setMonthlyTotalPremiumAmount(new BigDecimal("500.00"));
		policy.getFinancialInformation().add(fit2);
		
		List<SbmErrWarningLogDTO> stateFinErrors = sbmFinancialValidator.validateFinancialInfo(policy);
		
		assertFalse("stateFinErrors", stateFinErrors.isEmpty());
		assertEquals("financialErrors", 1, stateFinErrors.size());
		
		SbmErrWarningLogDTO financialError = stateFinErrors.get(0);
		
		assertEquals("ElementInError", "MonthlyAPTCAmount", financialError.getElementInError());
		assertEquals("Error Code", "ER-038", financialError.getErrorWarningTypeCd());

	}
	
	/*
	 * performStateSpecificFinancialValidations
	 */
	@Test
	public void test_performStateSpecificValidations_R009_Valid() {
		
		String qhpId = RandomStringUtils.randomAlphanumeric(16).toUpperCase();
		String stateCd = qhpId.substring(5, 7);
		
		List<String> bizRules = Arrays.asList("R009");
		SBMCache.getBusinessRules(stateCd).addAll(bizRules);
		
		expect(mockSbmDataService.getMetalLevelByQhpid(EasyMock.anyString(), EasyMock.anyString()))
		.andReturn(null).anyTimes();
		replay(mockSbmDataService);	
		
		PolicyType policy = new PolicyType();
		policy.setPolicyStartDate(DateTimeUtil.getXMLGregorianCalendar(JAN_1));
		policy.setPolicyEndDate(DateTimeUtil.getXMLGregorianCalendar(APR_30));
		policy.setQHPId(qhpId);
		policy.setEffectuationIndicator("Y");
		
		FinancialInformation fit = new FinancialInformation();
		fit.setFinancialEffectiveStartDate(DateTimeUtil.getXMLGregorianCalendar(JAN_1));
		fit.setFinancialEffectiveEndDate(DateTimeUtil.getXMLGregorianCalendar(JAN_31));
		fit.setMonthlyAPTCAmount(new BigDecimal("400.00"));
		fit.setMonthlyTotalPremiumAmount(new BigDecimal("450.00"));
		policy.getFinancialInformation().add(fit);
		
		FinancialInformation fit2 = new FinancialInformation();
		fit2.setFinancialEffectiveStartDate(DateTimeUtil.getXMLGregorianCalendar(FEB_1));
		fit2.setFinancialEffectiveEndDate(DateTimeUtil.getXMLGregorianCalendar(APR_30));
		fit2.setMonthlyAPTCAmount(new BigDecimal("500.00"));
		fit2.setMonthlyTotalPremiumAmount(new BigDecimal("500.00"));
		policy.getFinancialInformation().add(fit2);
		
		List<SbmErrWarningLogDTO> stateFinErrors = sbmFinancialValidator.validateFinancialInfo(policy);
		
		assertTrue("stateFinErrors", stateFinErrors.isEmpty());
	}
	
	/*
	 * performStateSpecificFinancialValidations
	 */
	@Test
	public void test_performStateSpecificValidations_R010_Invalid() {
		
		String qhpId = RandomStringUtils.randomAlphanumeric(16).toUpperCase();
		String stateCd = qhpId.substring(5, 7);
		
		List<String> bizRules = Arrays.asList("R010");
		SBMCache.getBusinessRules(stateCd).addAll(bizRules);
		
		expect(mockSbmDataService.getMetalLevelByQhpid(EasyMock.anyString(), EasyMock.anyString()))
		.andReturn(null).anyTimes();
		replay(mockSbmDataService);	
		
		PolicyType policy = new PolicyType();
		policy.setPolicyStartDate(DateTimeUtil.getXMLGregorianCalendar(JAN_1));
		policy.setPolicyEndDate(DateTimeUtil.getXMLGregorianCalendar(APR_30));
		policy.setQHPId(qhpId);
		policy.setEffectuationIndicator("Y");
		
		FinancialInformation fit = new FinancialInformation();
		fit.setFinancialEffectiveStartDate(DateTimeUtil.getXMLGregorianCalendar(JAN_1));
		fit.setFinancialEffectiveEndDate(DateTimeUtil.getXMLGregorianCalendar(JAN_31));
		fit.setMonthlyTotalPremiumAmount(new BigDecimal("450.00"));
		fit.setMonthlyTotalIndividualResponsibilityAmount(new BigDecimal("400.00"));
		fit.setMonthlyOtherPaymentAmount1(new BigDecimal("25.00"));
		policy.getFinancialInformation().add(fit);
		
		FinancialInformation fit2 = new FinancialInformation();
		fit2.setFinancialEffectiveStartDate(DateTimeUtil.getXMLGregorianCalendar(FEB_1));
		fit2.setFinancialEffectiveEndDate(DateTimeUtil.getXMLGregorianCalendar(APR_30));
		fit2.setMonthlyTotalPremiumAmount(new BigDecimal("500.00"));
		fit2.setMonthlyTotalIndividualResponsibilityAmount(new BigDecimal("25.00"));
		fit2.setMonthlyAPTCAmount(new BigDecimal("500.00"));
		fit2.setMonthlyOtherPaymentAmount2(new BigDecimal("25.00"));
		policy.getFinancialInformation().add(fit2);
		
		List<SbmErrWarningLogDTO> stateFinErrors = sbmFinancialValidator.validateFinancialInfo(policy);
		
		assertFalse("stateFinErrors", stateFinErrors.isEmpty());
		assertEquals("financialErrors", 2, stateFinErrors.size());
		
		SbmErrWarningLogDTO financialError = stateFinErrors.get(0);
		
		assertEquals("ElementInError", "MonthlyTotalPremiumAmount", financialError.getElementInError());
		assertEquals("Error Code", "ER-039", financialError.getErrorWarningTypeCd());
		
		assertEquals("ElementInError", "MonthlyTotalPremiumAmount", stateFinErrors.get(1).getElementInError());
		assertEquals("Error Code", "ER-039", stateFinErrors.get(1).getErrorWarningTypeCd());
	}
	
	/*
	 * performStateSpecificFinancialValidations
	 */
	@Test
	public void test_performStateSpecificValidations_R010_Valid() {
		
		String qhpId = RandomStringUtils.randomAlphanumeric(16).toUpperCase();
		String stateCd = qhpId.substring(5, 7);
		
		List<String> bizRules = Arrays.asList("R010");
		SBMCache.getBusinessRules(stateCd).addAll(bizRules);
		
		expect(mockSbmDataService.getMetalLevelByQhpid(EasyMock.anyString(), EasyMock.anyString()))
		.andReturn(null).anyTimes();
		replay(mockSbmDataService);	
		
		PolicyType policy = new PolicyType();
		policy.setPolicyStartDate(DateTimeUtil.getXMLGregorianCalendar(JAN_1));
		policy.setPolicyEndDate(DateTimeUtil.getXMLGregorianCalendar(APR_30));
		policy.setQHPId(qhpId);
		policy.setEffectuationIndicator("Y");
		
		FinancialInformation fit = new FinancialInformation();
		fit.setFinancialEffectiveStartDate(DateTimeUtil.getXMLGregorianCalendar(JAN_1));
		fit.setFinancialEffectiveEndDate(DateTimeUtil.getXMLGregorianCalendar(JAN_31));
		fit.setMonthlyTotalPremiumAmount(new BigDecimal("450.00"));
		fit.setMonthlyTotalIndividualResponsibilityAmount(new BigDecimal("450.00"));
		policy.getFinancialInformation().add(fit);
		
		FinancialInformation fit2 = new FinancialInformation();
		fit2.setFinancialEffectiveStartDate(DateTimeUtil.getXMLGregorianCalendar(FEB_1));
		fit2.setFinancialEffectiveEndDate(DateTimeUtil.getXMLGregorianCalendar(APR_30));
		fit2.setMonthlyTotalPremiumAmount(new BigDecimal("500.00"));
		fit2.setMonthlyTotalIndividualResponsibilityAmount(new BigDecimal("50.00"));
		fit2.setMonthlyAPTCAmount(new BigDecimal("400.00"));
		fit2.setMonthlyOtherPaymentAmount1(new BigDecimal("25.00"));
		fit2.setMonthlyOtherPaymentAmount2(new BigDecimal("25.00"));
		policy.getFinancialInformation().add(fit2);
		
		List<SbmErrWarningLogDTO> stateFinErrors = sbmFinancialValidator.validateFinancialInfo(policy);
		
		assertTrue("stateFinErrors", stateFinErrors.isEmpty());
	}
	
	/*
	 * performStateSpecificFinancialValidations
	 */
	@Test
	public void test_performStateSpecificValidations_R012_Invalid() {
		
		String qhpId = RandomStringUtils.randomAlphanumeric(16).toUpperCase();
		String stateCd = qhpId.substring(5, 7);
		
		List<String> bizRules = Arrays.asList("R012");
		SBMCache.getBusinessRules(stateCd).addAll(bizRules);
		
		expect(mockSbmDataService.getMetalLevelByQhpid(EasyMock.anyString(), EasyMock.anyString()))
		.andReturn(null).anyTimes();
		replay(mockSbmDataService);	
		
		PolicyType policy = new PolicyType();
		policy.setPolicyStartDate(DateTimeUtil.getXMLGregorianCalendar(JAN_1));
		policy.setPolicyEndDate(DateTimeUtil.getXMLGregorianCalendar(APR_30));
		policy.setQHPId(qhpId);
		policy.setEffectuationIndicator("Y");
		
		FinancialInformation fit = new FinancialInformation();
		fit.setFinancialEffectiveStartDate(DateTimeUtil.getXMLGregorianCalendar(JAN_1));
		fit.setFinancialEffectiveEndDate(DateTimeUtil.getXMLGregorianCalendar(APR_30));
		fit.setMonthlyTotalPremiumAmount(new BigDecimal("450.00"));
		fit.setMonthlyTotalIndividualResponsibilityAmount(new BigDecimal("400.00"));
		fit.setCSRVariantId("00");
		policy.getFinancialInformation().add(fit);
		
		List<SbmErrWarningLogDTO> stateFinErrors = sbmFinancialValidator.validateFinancialInfo(policy);
		
		assertFalse("stateFinErrors", stateFinErrors.isEmpty());
		assertEquals("financialErrors", 1, stateFinErrors.size());
		
		SbmErrWarningLogDTO financialError = stateFinErrors.get(0);
		
		assertEquals("ElementInError", "MonthlyCSRAmount", financialError.getElementInError());
		assertEquals("Error Code", "ER-040", financialError.getErrorWarningTypeCd());
	}
	
	/*
	 * performStateSpecificFinancialValidations
	 */
	@Test
	public void test_performStateSpecificValidations_R012_Valid() {
		
		String qhpId = RandomStringUtils.randomAlphanumeric(16).toUpperCase();
		String stateCd = qhpId.substring(5, 7);
		
		List<String> bizRules = Arrays.asList("R012");
		SBMCache.getBusinessRules(stateCd).addAll(bizRules);
		
		expect(mockSbmDataService.getMetalLevelByQhpid(EasyMock.anyString(), EasyMock.anyString()))
		.andReturn(null).anyTimes();
		replay(mockSbmDataService);	
		
		PolicyType policy = new PolicyType();
		policy.setPolicyStartDate(DateTimeUtil.getXMLGregorianCalendar(JAN_1));
		policy.setPolicyEndDate(DateTimeUtil.getXMLGregorianCalendar(APR_30));
		policy.setQHPId(qhpId);
		policy.setEffectuationIndicator("Y");
		
		FinancialInformation fit = new FinancialInformation();
		fit.setFinancialEffectiveStartDate(DateTimeUtil.getXMLGregorianCalendar(JAN_1));
		fit.setFinancialEffectiveEndDate(DateTimeUtil.getXMLGregorianCalendar(JAN_31));
		fit.setMonthlyTotalPremiumAmount(new BigDecimal("450.00"));
		fit.setMonthlyTotalIndividualResponsibilityAmount(new BigDecimal("450.00"));
		fit.setCSRVariantId("01");
		policy.getFinancialInformation().add(fit);
		
		FinancialInformation fit2 = new FinancialInformation();
		fit2.setFinancialEffectiveStartDate(DateTimeUtil.getXMLGregorianCalendar(FEB_1));
		fit2.setFinancialEffectiveEndDate(DateTimeUtil.getXMLGregorianCalendar(APR_30));
		fit2.setMonthlyTotalPremiumAmount(new BigDecimal("500.00"));
		fit2.setMonthlyTotalIndividualResponsibilityAmount(new BigDecimal("50.00"));
		fit2.setMonthlyCSRAmount(new BigDecimal("400.00"));
		policy.getFinancialInformation().add(fit2);
		
		List<SbmErrWarningLogDTO> stateFinErrors = sbmFinancialValidator.validateFinancialInfo(policy);
		
		assertTrue("stateFinErrors", stateFinErrors.isEmpty());
	}
	
	/*
	 * performStateSpecificFinancialValidations
	 */
	@Test
	public void test_performStateSpecificValidations_R011_Invalid() {
		
		String qhpId = RandomStringUtils.randomAlphanumeric(16).toUpperCase();
		String stateCd = qhpId.substring(5, 7);
		
		List<String> bizRules = Arrays.asList("R011");
		SBMCache.getBusinessRules(stateCd).addAll(bizRules);
		
		expect(mockSbmDataService.getMetalLevelByQhpid(EasyMock.anyString(), EasyMock.anyString()))
		.andReturn(null).anyTimes();
		replay(mockSbmDataService);	
		
		PolicyType policy = new PolicyType();
		policy.setPolicyStartDate(DateTimeUtil.getXMLGregorianCalendar(JAN_1));
		policy.setPolicyEndDate(DateTimeUtil.getXMLGregorianCalendar(JAN_31));
		policy.setQHPId(qhpId);
		policy.setEffectuationIndicator("Y");
		
		FinancialInformation fit = new FinancialInformation();
		fit.setFinancialEffectiveStartDate(DateTimeUtil.getXMLGregorianCalendar(JAN_1));
		fit.setFinancialEffectiveEndDate(DateTimeUtil.getXMLGregorianCalendar(JAN_31));
		fit.setMonthlyTotalPremiumAmount(new BigDecimal("450.00"));
		fit.setMonthlyTotalIndividualResponsibilityAmount(new BigDecimal("400.00"));
		fit.setMonthlyCSRAmount(new BigDecimal("400.00"));
		fit.setCSRVariantId("00");
		policy.getFinancialInformation().add(fit);
		
		FinancialInformation fit2 = new FinancialInformation();
		fit2.setFinancialEffectiveStartDate(DateTimeUtil.getXMLGregorianCalendar(FEB_1));
		fit2.setFinancialEffectiveEndDate(DateTimeUtil.getXMLGregorianCalendar(APR_30));
		fit2.setMonthlyTotalPremiumAmount(new BigDecimal("450.00"));
		fit2.setMonthlyTotalIndividualResponsibilityAmount(new BigDecimal("400.00"));
		fit2.setMonthlyCSRAmount(new BigDecimal("500.00"));
		fit2.setCSRVariantId("01");
		policy.getFinancialInformation().add(fit2);
		
		List<SbmErrWarningLogDTO> stateFinErrors = sbmFinancialValidator.validateFinancialInfo(policy);
		
		assertFalse("stateFinErrors", stateFinErrors.isEmpty());
		assertEquals("financialErrors", 2, stateFinErrors.size());
		
		SbmErrWarningLogDTO financialError = stateFinErrors.get(0);
		
		assertEquals("ElementInError", "MonthlyCSRAmount", financialError.getElementInError());
		assertEquals("Error Code", "ER-041", financialError.getErrorWarningTypeCd());
	}
	
	/*
	 * performStateSpecificFinancialValidations
	 */
	@Test
	public void test_performStateSpecificValidations_R011_Valid() {

		String qhpId = RandomStringUtils.randomAlphanumeric(16).toUpperCase();
		String stateCd = qhpId.substring(5, 7);
		
		List<String> bizRules = Arrays.asList("R011");
		SBMCache.getBusinessRules(stateCd).addAll(bizRules);
		
		expect(mockSbmDataService.getMetalLevelByQhpid(EasyMock.anyString(), EasyMock.anyString()))
		.andReturn(null).anyTimes();
		replay(mockSbmDataService);	
		
		PolicyType policy = new PolicyType();
		policy.setPolicyStartDate(DateTimeUtil.getXMLGregorianCalendar(JAN_1));
		policy.setPolicyEndDate(DateTimeUtil.getXMLGregorianCalendar(APR_30));
		policy.setQHPId(qhpId);
		policy.setEffectuationIndicator("Y");
		
		FinancialInformation fit = new FinancialInformation();
		fit.setFinancialEffectiveStartDate(DateTimeUtil.getXMLGregorianCalendar(JAN_1));
		fit.setFinancialEffectiveEndDate(DateTimeUtil.getXMLGregorianCalendar(JAN_31));
		fit.setMonthlyTotalPremiumAmount(new BigDecimal("450.00"));
		fit.setMonthlyTotalIndividualResponsibilityAmount(new BigDecimal("450.00"));
		fit.setCSRVariantId("02");
		policy.getFinancialInformation().add(fit);
		
		FinancialInformation fit1 = new FinancialInformation();
		fit1.setFinancialEffectiveStartDate(DateTimeUtil.getXMLGregorianCalendar(FEB_1));
		fit1.setFinancialEffectiveEndDate(DateTimeUtil.getXMLGregorianCalendar(MAR_31));
		fit1.setMonthlyTotalPremiumAmount(new BigDecimal("450.00"));
		fit1.setMonthlyTotalIndividualResponsibilityAmount(new BigDecimal("450.00"));
		fit1.setMonthlyCSRAmount(new BigDecimal("0.00"));
		fit1.setCSRVariantId("02");
		policy.getFinancialInformation().add(fit1);
		
		FinancialInformation fit2 = new FinancialInformation();
		fit2.setFinancialEffectiveStartDate(DateTimeUtil.getXMLGregorianCalendar(APR_1));
		fit2.setFinancialEffectiveEndDate(DateTimeUtil.getXMLGregorianCalendar(APR_30));
		fit2.setMonthlyTotalPremiumAmount(new BigDecimal("500.00"));
		fit2.setMonthlyTotalIndividualResponsibilityAmount(new BigDecimal("50.00"));
		fit2.setMonthlyCSRAmount(new BigDecimal("400.00"));
		fit2.setCSRVariantId("06");
		policy.getFinancialInformation().add(fit2);
		
		List<SbmErrWarningLogDTO> stateFinErrors = sbmFinancialValidator.validateFinancialInfo(policy);
		
		assertTrue("stateFinErrors", stateFinErrors.isEmpty());
	}
	
	/*
	 * performStateSpecificFinancialValidations
	 */
	@Test
	public void test_performStateSpecificValidations_R013_Invalid() {

		String qhpId = RandomStringUtils.randomAlphanumeric(16).toUpperCase();
		String stateCd = qhpId.substring(5, 7);
		
		List<String> bizRules = Arrays.asList("R013");
		SBMCache.getBusinessRules(stateCd).addAll(bizRules);
		
		expect(mockSbmDataService.getMetalLevelByQhpid(EasyMock.anyString(), EasyMock.anyString()))
		.andReturn(null).anyTimes();
		replay(mockSbmDataService);	
		
		PolicyType policy = new PolicyType();
		policy.setPolicyStartDate(DateTimeUtil.getXMLGregorianCalendar(JAN_1));
		policy.setPolicyEndDate(DateTimeUtil.getXMLGregorianCalendar(JAN_31));
		policy.setQHPId(qhpId);
		policy.setEffectuationIndicator("Y");
		
		FinancialInformation fit = new FinancialInformation();
		fit.setFinancialEffectiveStartDate(DateTimeUtil.getXMLGregorianCalendar(JAN_1));
		fit.setFinancialEffectiveEndDate(DateTimeUtil.getXMLGregorianCalendar(JAN_31));
		fit.setMonthlyTotalPremiumAmount(new BigDecimal("450.00"));
		fit.setMonthlyTotalIndividualResponsibilityAmount(new BigDecimal("400.00"));
		fit.setMonthlyCSRAmount(new BigDecimal("400.00"));
		fit.setCSRVariantId("01");
		fit.setMonthlyCSRAmount(new BigDecimal("0.00"));
		policy.getFinancialInformation().add(fit);
		
		List<SbmErrWarningLogDTO> stateFinErrors = sbmFinancialValidator.validateFinancialInfo(policy);
		
		assertFalse("stateFinErrors", stateFinErrors.isEmpty());
		assertEquals("financialErrors", 1, stateFinErrors.size());
		
		SbmErrWarningLogDTO financialError = stateFinErrors.get(0);
		
		assertEquals("ElementInError", "MonthlyCSRAmount", financialError.getElementInError());
		assertEquals("Error Code", "WR-006", financialError.getErrorWarningTypeCd());
	}
	
	/*
	 * performStateSpecificFinancialValidations
	 */
	@Test
	public void test_performStateSpecificValidations_R013_Valid() {
		
		String qhpId = RandomStringUtils.randomAlphanumeric(16).toUpperCase();
		String stateCd = qhpId.substring(5, 7);
		
		List<String> bizRules = Arrays.asList("R013");
		SBMCache.getBusinessRules(stateCd).addAll(bizRules);
		
		expect(mockSbmDataService.getMetalLevelByQhpid(EasyMock.anyString(), EasyMock.anyString()))
		.andReturn(null).anyTimes();
		replay(mockSbmDataService);	
		
		PolicyType policy = new PolicyType();
		policy.setPolicyStartDate(DateTimeUtil.getXMLGregorianCalendar(JAN_1));
		policy.setPolicyEndDate(DateTimeUtil.getXMLGregorianCalendar(APR_30));
		policy.setQHPId(qhpId);
		policy.setEffectuationIndicator("Y");
		
		FinancialInformation fit = new FinancialInformation();
		fit.setFinancialEffectiveStartDate(DateTimeUtil.getXMLGregorianCalendar(JAN_1));
		fit.setFinancialEffectiveEndDate(DateTimeUtil.getXMLGregorianCalendar(JAN_31));
		fit.setMonthlyTotalPremiumAmount(new BigDecimal("450.00"));
		fit.setMonthlyTotalIndividualResponsibilityAmount(new BigDecimal("450.00"));
		fit.setCSRVariantId("02");
		policy.getFinancialInformation().add(fit);
		
		FinancialInformation fit2 = new FinancialInformation();
		fit2.setFinancialEffectiveStartDate(DateTimeUtil.getXMLGregorianCalendar(FEB_1));
		fit2.setFinancialEffectiveEndDate(DateTimeUtil.getXMLGregorianCalendar(MAR_31));
		fit2.setMonthlyTotalPremiumAmount(new BigDecimal("500.00"));
		fit2.setMonthlyTotalIndividualResponsibilityAmount(new BigDecimal("50.00"));
		fit2.setMonthlyCSRAmount(new BigDecimal("400.00"));
		fit2.setCSRVariantId("06");
		policy.getFinancialInformation().add(fit2);
		
		FinancialInformation fit3 = new FinancialInformation();
		fit3.setFinancialEffectiveStartDate(DateTimeUtil.getXMLGregorianCalendar(JAN_1));
		fit3.setFinancialEffectiveEndDate(DateTimeUtil.getXMLGregorianCalendar(JAN_31));
		fit3.setMonthlyTotalPremiumAmount(new BigDecimal("450.00"));
		fit3.setMonthlyTotalIndividualResponsibilityAmount(new BigDecimal("400.00"));
		fit3.setMonthlyCSRAmount(new BigDecimal("400.00"));
		fit3.setCSRVariantId("01");
		fit3.setMonthlyCSRAmount(new BigDecimal("10.00"));
		policy.getFinancialInformation().add(fit3);
		
		List<SbmErrWarningLogDTO> stateFinErrors = sbmFinancialValidator.validateFinancialInfo(policy);
		
		assertTrue("stateFinErrors", stateFinErrors.isEmpty());
	}
	
	/*
	 * performStateSpecificFinancialValidations
	 */
	@Test
	public void test_performStateSpecificValidations_R014_Invalid() {
		
		String qhpId = RandomStringUtils.randomAlphanumeric(16).toUpperCase();
		String stateCd = qhpId.substring(5, 7);
		
		List<String> bizRules = Arrays.asList("R014");
		SBMCache.getBusinessRules(stateCd).addAll(bizRules);
		
		expect(mockSbmDataService.getMetalLevelByQhpid(EasyMock.anyString(), EasyMock.anyString()))
		.andReturn(SbmMetalLevelType.GOLD.getValue()).anyTimes();
		replay(mockSbmDataService);	
		
		PolicyType policy = new PolicyType();
		policy.setPolicyStartDate(DateTimeUtil.getXMLGregorianCalendar(JAN_1));
		policy.setPolicyEndDate(DateTimeUtil.getXMLGregorianCalendar(APR_30));
		policy.setQHPId(qhpId);
		policy.setEffectuationIndicator("Y");
		
		FinancialInformation fit = new FinancialInformation();
		fit.setFinancialEffectiveStartDate(DateTimeUtil.getXMLGregorianCalendar(JAN_1));
		fit.setFinancialEffectiveEndDate(DateTimeUtil.getXMLGregorianCalendar(JAN_31));
		fit.setMonthlyTotalPremiumAmount(new BigDecimal("450.00"));
		fit.setMonthlyTotalIndividualResponsibilityAmount(new BigDecimal("400.00"));
		fit.setMonthlyCSRAmount(new BigDecimal("400.00"));
		fit.setCSRVariantId("04");
		fit.setMonthlyCSRAmount(new BigDecimal("0.00"));
		policy.getFinancialInformation().add(fit);
		
		FinancialInformation fit2 = new FinancialInformation();
		fit2.setFinancialEffectiveStartDate(DateTimeUtil.getXMLGregorianCalendar(FEB_1));
		fit2.setFinancialEffectiveEndDate(DateTimeUtil.getXMLGregorianCalendar(MAR_1.minusDays(1)));
		fit2.setMonthlyTotalPremiumAmount(new BigDecimal("450.00"));
		fit2.setMonthlyTotalIndividualResponsibilityAmount(new BigDecimal("400.00"));
		fit2.setMonthlyCSRAmount(new BigDecimal("400.00"));
		fit2.setCSRVariantId("05");
		fit2.setMonthlyCSRAmount(new BigDecimal("0.00"));
		policy.getFinancialInformation().add(fit2);
		
		FinancialInformation fit3 = new FinancialInformation();
		fit3.setFinancialEffectiveStartDate(DateTimeUtil.getXMLGregorianCalendar(MAR_1));
		fit3.setFinancialEffectiveEndDate(DateTimeUtil.getXMLGregorianCalendar(MAR_31));
		fit3.setMonthlyTotalPremiumAmount(new BigDecimal("450.00"));
		fit3.setMonthlyTotalIndividualResponsibilityAmount(new BigDecimal("400.00"));
		fit3.setMonthlyCSRAmount(new BigDecimal("400.00"));
		fit3.setCSRVariantId("06");
		fit3.setMonthlyCSRAmount(new BigDecimal("0.00"));
		policy.getFinancialInformation().add(fit3);
		
		List<SbmErrWarningLogDTO> stateFinErrors = sbmFinancialValidator.validateFinancialInfo(policy);
		
		assertFalse("stateFinErrors", stateFinErrors.isEmpty());
		assertEquals("financialErrors", 3, stateFinErrors.size());
		
		SbmErrWarningLogDTO financialError = stateFinErrors.get(0);
		
		assertEquals("ElementInError", "QHPId", financialError.getElementInError());
		assertEquals("Error Code", "ER-042", financialError.getErrorWarningTypeCd());
	}
	
	/*
	 * performStateSpecificFinancialValidations
	 */
	@Test
	public void test_performStateSpecificValidations_R014_Valid() {

		String qhpId = RandomStringUtils.randomAlphanumeric(16).toUpperCase();
		String stateCd = qhpId.substring(5, 7);
		
		List<String> bizRules = Arrays.asList("R014");
		SBMCache.getBusinessRules(stateCd).addAll(bizRules);
		
		expect(mockSbmDataService.getMetalLevelByQhpid(EasyMock.anyString(), EasyMock.anyString()))
		.andReturn(SbmMetalLevelType.SILVER.getValue()).anyTimes();
		replay(mockSbmDataService);	
		
		PolicyType policy = new PolicyType();
		policy.setPolicyStartDate(DateTimeUtil.getXMLGregorianCalendar(JAN_1));
		policy.setPolicyEndDate(DateTimeUtil.getXMLGregorianCalendar(APR_30));
		policy.setQHPId(qhpId);
		policy.setEffectuationIndicator("Y");
		
		FinancialInformation fit = new FinancialInformation();
		fit.setFinancialEffectiveStartDate(DateTimeUtil.getXMLGregorianCalendar(JAN_1));
		fit.setFinancialEffectiveEndDate(DateTimeUtil.getXMLGregorianCalendar(JAN_31));
		fit.setMonthlyTotalPremiumAmount(new BigDecimal("450.00"));
		fit.setMonthlyTotalIndividualResponsibilityAmount(new BigDecimal("450.00"));
		fit.setCSRVariantId("01");
		policy.getFinancialInformation().add(fit);
		
		FinancialInformation fit2 = new FinancialInformation();
		fit2.setFinancialEffectiveStartDate(DateTimeUtil.getXMLGregorianCalendar(FEB_1));
		fit2.setFinancialEffectiveEndDate(DateTimeUtil.getXMLGregorianCalendar(MAR_31));
		fit2.setMonthlyTotalPremiumAmount(new BigDecimal("500.00"));
		fit2.setMonthlyTotalIndividualResponsibilityAmount(new BigDecimal("50.00"));
		fit2.setMonthlyCSRAmount(new BigDecimal("400.00"));
		fit2.setCSRVariantId("04");
		policy.getFinancialInformation().add(fit2);

		List<SbmErrWarningLogDTO> stateFinErrors = sbmFinancialValidator.validateFinancialInfo(policy);
		
		assertTrue("stateFinErrors", stateFinErrors.isEmpty());
	}
	
	/*
	 * performStateSpecificFinancialValidations
	 */
	@Test
	public void test_performStateSpecificValidations_R014_Valid_NullMetallevel() {
		
		String qhpId = RandomStringUtils.randomAlphanumeric(16).toUpperCase();
		String stateCd = qhpId.substring(5, 7);
		
		List<String> bizRules = Arrays.asList("R014");
		SBMCache.getBusinessRules(stateCd).addAll(bizRules);
		
		expect(mockSbmDataService.getMetalLevelByQhpid(EasyMock.anyString(), EasyMock.anyString()))
		.andReturn(null).anyTimes();
		replay(mockSbmDataService);	
		
		PolicyType policy = new PolicyType();
		policy.setPolicyStartDate(DateTimeUtil.getXMLGregorianCalendar(JAN_1));
		policy.setPolicyEndDate(DateTimeUtil.getXMLGregorianCalendar(APR_30));
		policy.setQHPId(qhpId);
		policy.setEffectuationIndicator("Y");
		
		FinancialInformation fit = new FinancialInformation();
		fit.setFinancialEffectiveStartDate(DateTimeUtil.getXMLGregorianCalendar(JAN_1));
		fit.setFinancialEffectiveEndDate(DateTimeUtil.getXMLGregorianCalendar(JAN_31));
		fit.setMonthlyTotalPremiumAmount(new BigDecimal("450.00"));
		fit.setMonthlyTotalIndividualResponsibilityAmount(new BigDecimal("450.00"));
		fit.setCSRVariantId("01");
		policy.getFinancialInformation().add(fit);
		
		FinancialInformation fit2 = new FinancialInformation();
		fit2.setFinancialEffectiveStartDate(DateTimeUtil.getXMLGregorianCalendar(FEB_1));
		fit2.setFinancialEffectiveEndDate(DateTimeUtil.getXMLGregorianCalendar(MAR_31));
		fit2.setMonthlyTotalPremiumAmount(new BigDecimal("500.00"));
		fit2.setMonthlyTotalIndividualResponsibilityAmount(new BigDecimal("50.00"));
		fit2.setMonthlyCSRAmount(new BigDecimal("400.00"));
		fit2.setCSRVariantId("04");
		policy.getFinancialInformation().add(fit2);

		List<SbmErrWarningLogDTO> stateFinErrors = sbmFinancialValidator.validateFinancialInfo(policy);
		
		assertTrue("stateFinErrors", stateFinErrors.isEmpty());
	}
	
	/*
	 * performStateSpecificFinancialValidations
	 */
	@Test
	public void test_performStateSpecificValidations_validateMetalLevelForCsr0203_R068_Invalid() {
		
		String qhpId = RandomStringUtils.randomAlphanumeric(16).toUpperCase();
		String stateCd = qhpId.substring(5, 7);
		
		List<String> bizRules = Arrays.asList("R068");
		SBMCache.getBusinessRules(stateCd).addAll(bizRules);
		
		expect(mockSbmDataService.getMetalLevelByQhpid(EasyMock.anyString(), EasyMock.anyString()))
		.andReturn(SbmMetalLevelType.CATASTROPHIC.getValue()).anyTimes();
		replay(mockSbmDataService);	
		
		PolicyType policy = new PolicyType();
		policy.setPolicyStartDate(DateTimeUtil.getXMLGregorianCalendar(JAN_1));
		policy.setPolicyEndDate(DateTimeUtil.getXMLGregorianCalendar(APR_30));
		policy.setQHPId(qhpId);
		policy.setEffectuationIndicator("Y");
		
		FinancialInformation fit = new FinancialInformation();
		fit.setFinancialEffectiveStartDate(DateTimeUtil.getXMLGregorianCalendar(JAN_1));
		fit.setFinancialEffectiveEndDate(DateTimeUtil.getXMLGregorianCalendar(JAN_31));
		fit.setMonthlyTotalPremiumAmount(new BigDecimal("450.00"));
		fit.setMonthlyTotalIndividualResponsibilityAmount(new BigDecimal("400.00"));
		fit.setMonthlyCSRAmount(new BigDecimal("400.00"));
		fit.setCSRVariantId("02");
		fit.setMonthlyCSRAmount(new BigDecimal("0.00"));
		policy.getFinancialInformation().add(fit);
		
		FinancialInformation fit2 = new FinancialInformation();
		fit2.setFinancialEffectiveStartDate(DateTimeUtil.getXMLGregorianCalendar(FEB_1));
		fit2.setFinancialEffectiveEndDate(DateTimeUtil.getXMLGregorianCalendar(MAR_1.minusDays(1)));
		fit2.setMonthlyTotalPremiumAmount(new BigDecimal("450.00"));
		fit2.setMonthlyTotalIndividualResponsibilityAmount(new BigDecimal("400.00"));
		fit2.setMonthlyCSRAmount(new BigDecimal("400.00"));
		fit2.setCSRVariantId("03");
		fit2.setMonthlyCSRAmount(new BigDecimal("0.00"));
		policy.getFinancialInformation().add(fit2);
		
		FinancialInformation fit3 = new FinancialInformation();
		fit3.setFinancialEffectiveStartDate(DateTimeUtil.getXMLGregorianCalendar(MAR_1));
		fit3.setFinancialEffectiveEndDate(DateTimeUtil.getXMLGregorianCalendar(MAR_31));
		fit3.setMonthlyTotalPremiumAmount(new BigDecimal("450.00"));
		fit3.setMonthlyTotalIndividualResponsibilityAmount(new BigDecimal("400.00"));
		fit3.setMonthlyCSRAmount(new BigDecimal("400.00"));
		fit3.setCSRVariantId("06");
		fit3.setMonthlyCSRAmount(new BigDecimal("0.00"));
		policy.getFinancialInformation().add(fit3);
		
		List<SbmErrWarningLogDTO> stateFinErrors = sbmFinancialValidator.validateFinancialInfo(policy);
		
		assertFalse("stateFinErrors", stateFinErrors.isEmpty());
		assertEquals("financialErrors", 2, stateFinErrors.size());
		
		SbmErrWarningLogDTO financialError = stateFinErrors.get(0);
		
		assertEquals("ElementInError", "QHPId", financialError.getElementInError());
		assertEquals("Error Code", "ER-042", financialError.getErrorWarningTypeCd());
	}
	
	/*
	 * performStateSpecificFinancialValidations
	 */
	@Test
	public void test_performStateSpecificValidations_validateMetalLevelForCsr0203_R068_Valid_Silver() {

		String qhpId = RandomStringUtils.randomAlphanumeric(16).toUpperCase();
		String stateCd = qhpId.substring(5, 7);
		
		List<String> bizRules = Arrays.asList("R068");
		SBMCache.getBusinessRules(stateCd).addAll(bizRules);
		
		expect(mockSbmDataService.getMetalLevelByQhpid(EasyMock.anyString(), EasyMock.anyString()))
		.andReturn(SbmMetalLevelType.SILVER.getValue()).anyTimes();
		replay(mockSbmDataService);	
		
		PolicyType policy = new PolicyType();
		policy.setPolicyStartDate(DateTimeUtil.getXMLGregorianCalendar(JAN_1));
		policy.setPolicyEndDate(DateTimeUtil.getXMLGregorianCalendar(APR_30));
		policy.setQHPId(qhpId);
		policy.setEffectuationIndicator("Y");
		
		FinancialInformation fit = new FinancialInformation();
		fit.setFinancialEffectiveStartDate(DateTimeUtil.getXMLGregorianCalendar(JAN_1));
		fit.setFinancialEffectiveEndDate(DateTimeUtil.getXMLGregorianCalendar(JAN_31));
		fit.setMonthlyTotalPremiumAmount(new BigDecimal("450.00"));
		fit.setMonthlyTotalIndividualResponsibilityAmount(new BigDecimal("450.00"));
		fit.setCSRVariantId("02");
		policy.getFinancialInformation().add(fit);
		
		FinancialInformation fit2 = new FinancialInformation();
		fit2.setFinancialEffectiveStartDate(DateTimeUtil.getXMLGregorianCalendar(FEB_1));
		fit2.setFinancialEffectiveEndDate(DateTimeUtil.getXMLGregorianCalendar(MAR_31));
		fit2.setMonthlyTotalPremiumAmount(new BigDecimal("500.00"));
		fit2.setMonthlyTotalIndividualResponsibilityAmount(new BigDecimal("50.00"));
		fit2.setMonthlyCSRAmount(new BigDecimal("400.00"));
		fit2.setCSRVariantId("03");
		policy.getFinancialInformation().add(fit2);

		List<SbmErrWarningLogDTO> stateFinErrors = sbmFinancialValidator.validateFinancialInfo(policy);
		
		assertTrue("stateFinErrors", stateFinErrors.isEmpty());
	}
	
	/*
	 * performStateSpecificFinancialValidations
	 */
	@Test
	public void test_performStateSpecificValidations_validateMetalLevelForCsr0203_R068_Valid_Platinum() {

		String qhpId = RandomStringUtils.randomAlphanumeric(16).toUpperCase();
		String stateCd = qhpId.substring(5, 7);
		
		List<String> bizRules = Arrays.asList("R068");
		SBMCache.getBusinessRules(stateCd).addAll(bizRules);
		
		expect(mockSbmDataService.getMetalLevelByQhpid(EasyMock.anyString(), EasyMock.anyString()))
		.andReturn(SbmMetalLevelType.PLATINUM.getValue()).anyTimes();
		replay(mockSbmDataService);	
		
		PolicyType policy = new PolicyType();
		policy.setPolicyStartDate(DateTimeUtil.getXMLGregorianCalendar(JAN_1));
		policy.setPolicyEndDate(DateTimeUtil.getXMLGregorianCalendar(APR_30));
		policy.setQHPId(qhpId);
		policy.setEffectuationIndicator("Y");
		
		FinancialInformation fit = new FinancialInformation();
		fit.setFinancialEffectiveStartDate(DateTimeUtil.getXMLGregorianCalendar(JAN_1));
		fit.setFinancialEffectiveEndDate(DateTimeUtil.getXMLGregorianCalendar(JAN_31));
		fit.setMonthlyTotalPremiumAmount(new BigDecimal("450.00"));
		fit.setMonthlyTotalIndividualResponsibilityAmount(new BigDecimal("450.00"));
		fit.setCSRVariantId("02");
		policy.getFinancialInformation().add(fit);
		
		FinancialInformation fit2 = new FinancialInformation();
		fit2.setFinancialEffectiveStartDate(DateTimeUtil.getXMLGregorianCalendar(FEB_1));
		fit2.setFinancialEffectiveEndDate(DateTimeUtil.getXMLGregorianCalendar(MAR_31));
		fit2.setMonthlyTotalPremiumAmount(new BigDecimal("500.00"));
		fit2.setMonthlyTotalIndividualResponsibilityAmount(new BigDecimal("50.00"));
		fit2.setMonthlyCSRAmount(new BigDecimal("400.00"));
		fit2.setCSRVariantId("03");
		policy.getFinancialInformation().add(fit2);

		List<SbmErrWarningLogDTO> stateFinErrors = sbmFinancialValidator.validateFinancialInfo(policy);
		
		assertTrue("stateFinErrors", stateFinErrors.isEmpty());
	}
	
	/*
	 * performStateSpecificFinancialValidations
	 */
	@Test
	public void test_performStateSpecificValidations_validateMetalLevelForCsr0203_R068_Valid_Bronze() {

		String qhpId = RandomStringUtils.randomAlphanumeric(16).toUpperCase();
		String stateCd = qhpId.substring(5, 7);
		
		List<String> bizRules = Arrays.asList("R068");
		SBMCache.getBusinessRules(stateCd).addAll(bizRules);
		
		expect(mockSbmDataService.getMetalLevelByQhpid(EasyMock.anyString(), EasyMock.anyString()))
		.andReturn(SbmMetalLevelType.BRONZE.getValue()).anyTimes();
		replay(mockSbmDataService);	
		
		PolicyType policy = new PolicyType();
		policy.setPolicyStartDate(DateTimeUtil.getXMLGregorianCalendar(JAN_1));
		policy.setPolicyEndDate(DateTimeUtil.getXMLGregorianCalendar(APR_30));
		policy.setQHPId(qhpId);
		policy.setEffectuationIndicator("Y");
		
		FinancialInformation fit = new FinancialInformation();
		fit.setFinancialEffectiveStartDate(DateTimeUtil.getXMLGregorianCalendar(JAN_1));
		fit.setFinancialEffectiveEndDate(DateTimeUtil.getXMLGregorianCalendar(JAN_31));
		fit.setMonthlyTotalPremiumAmount(new BigDecimal("450.00"));
		fit.setMonthlyTotalIndividualResponsibilityAmount(new BigDecimal("450.00"));
		fit.setCSRVariantId("02");
		policy.getFinancialInformation().add(fit);
		
		FinancialInformation fit2 = new FinancialInformation();
		fit2.setFinancialEffectiveStartDate(DateTimeUtil.getXMLGregorianCalendar(FEB_1));
		fit2.setFinancialEffectiveEndDate(DateTimeUtil.getXMLGregorianCalendar(MAR_31));
		fit2.setMonthlyTotalPremiumAmount(new BigDecimal("500.00"));
		fit2.setMonthlyTotalIndividualResponsibilityAmount(new BigDecimal("50.00"));
		fit2.setMonthlyCSRAmount(new BigDecimal("400.00"));
		fit2.setCSRVariantId("03");
		policy.getFinancialInformation().add(fit2);

		List<SbmErrWarningLogDTO> stateFinErrors = sbmFinancialValidator.validateFinancialInfo(policy);
		
		assertTrue("stateFinErrors", stateFinErrors.isEmpty());
	}
	
	/*
	 * performStateSpecificFinancialValidations
	 */
	@Test
	public void test_performStateSpecificValidations_validateMetalLevelForCsr0203_R068_Valid_NullMetallevel() {
		
		String qhpId = RandomStringUtils.randomAlphanumeric(16).toUpperCase();
		String stateCd = qhpId.substring(5, 7);
		
		List<String> bizRules = Arrays.asList("R068");
		SBMCache.getBusinessRules(stateCd).addAll(bizRules);
		
		expect(mockSbmDataService.getMetalLevelByQhpid(EasyMock.anyString(), EasyMock.anyString()))
		.andReturn(null).anyTimes();
		replay(mockSbmDataService);	
		
		PolicyType policy = new PolicyType();
		policy.setPolicyStartDate(DateTimeUtil.getXMLGregorianCalendar(JAN_1));
		policy.setPolicyEndDate(DateTimeUtil.getXMLGregorianCalendar(APR_30));
		policy.setQHPId(qhpId);
		policy.setEffectuationIndicator("Y");
		
		FinancialInformation fit = new FinancialInformation();
		fit.setFinancialEffectiveStartDate(DateTimeUtil.getXMLGregorianCalendar(JAN_1));
		fit.setFinancialEffectiveEndDate(DateTimeUtil.getXMLGregorianCalendar(JAN_31));
		fit.setMonthlyTotalPremiumAmount(new BigDecimal("450.00"));
		fit.setMonthlyTotalIndividualResponsibilityAmount(new BigDecimal("450.00"));
		fit.setCSRVariantId("02");
		policy.getFinancialInformation().add(fit);
		
		FinancialInformation fit2 = new FinancialInformation();
		fit2.setFinancialEffectiveStartDate(DateTimeUtil.getXMLGregorianCalendar(FEB_1));
		fit2.setFinancialEffectiveEndDate(DateTimeUtil.getXMLGregorianCalendar(MAR_31));
		fit2.setMonthlyTotalPremiumAmount(new BigDecimal("500.00"));
		fit2.setMonthlyTotalIndividualResponsibilityAmount(new BigDecimal("50.00"));
		fit2.setMonthlyCSRAmount(new BigDecimal("400.00"));
		fit2.setCSRVariantId("03");
		policy.getFinancialInformation().add(fit2);

		List<SbmErrWarningLogDTO> stateFinErrors = sbmFinancialValidator.validateFinancialInfo(policy);
		
		assertTrue("stateFinErrors", stateFinErrors.isEmpty());
	}
	
	/*
	 * performStateSpecificFinancialValidations
	 */
	@Test
	public void test_performStateSpecificValidations_R015_Warning_Variant_04_05_06() {

		BigDecimal csrMultiplier = new BigDecimal("1.11");
		
		String qhpId = RandomStringUtils.randomAlphanumeric(16).toUpperCase();
		String stateCd = qhpId.substring(5, 7);
		
		List<String> bizRules = Arrays.asList("R015");
		SBMCache.getBusinessRules(stateCd).addAll(bizRules);
		
		expect(mockSbmDataService.getMetalLevelByQhpid(EasyMock.anyString(), EasyMock.anyString()))
		.andReturn(SbmMetalLevelType.GOLD.getValue()).anyTimes();
		expect(mockSbmDataService.getCsrMultiplierByVariant(EasyMock.anyString(), EasyMock.anyString()))
		.andReturn(csrMultiplier).anyTimes();
		replay(mockSbmDataService);	
		
		PolicyType policy = new PolicyType();
		policy.setPolicyStartDate(DateTimeUtil.getXMLGregorianCalendar(JAN_1));
		policy.setPolicyEndDate(DateTimeUtil.getXMLGregorianCalendar(APR_30));
		policy.setQHPId(qhpId);
		policy.setEffectuationIndicator("Y");
		
		FinancialInformation fit = new FinancialInformation();
		fit.setFinancialEffectiveStartDate(DateTimeUtil.getXMLGregorianCalendar(JAN_1));
		fit.setFinancialEffectiveEndDate(DateTimeUtil.getXMLGregorianCalendar(JAN_31));
		fit.setMonthlyTotalPremiumAmount(new BigDecimal("450.00"));
		fit.setMonthlyTotalIndividualResponsibilityAmount(new BigDecimal("400.00"));
		fit.setMonthlyCSRAmount(new BigDecimal("400.00"));
		fit.setCSRVariantId("04");
		policy.getFinancialInformation().add(fit);
		
		FinancialInformation fit2 = new FinancialInformation();
		fit2.setFinancialEffectiveStartDate(DateTimeUtil.getXMLGregorianCalendar(FEB_1));
		fit2.setFinancialEffectiveEndDate(DateTimeUtil.getXMLGregorianCalendar(MAR_1.minusDays(1)));
		fit2.setMonthlyTotalPremiumAmount(new BigDecimal("450.50"));
		fit2.setMonthlyTotalIndividualResponsibilityAmount(new BigDecimal("400.00"));
		fit2.setMonthlyCSRAmount(new BigDecimal("400.50"));
		fit2.setCSRVariantId("05");
		policy.getFinancialInformation().add(fit2);
		
		FinancialInformation fit3 = new FinancialInformation();
		fit3.setFinancialEffectiveStartDate(DateTimeUtil.getXMLGregorianCalendar(MAR_1));
		fit3.setFinancialEffectiveEndDate(DateTimeUtil.getXMLGregorianCalendar(MAR_31));
		fit3.setMonthlyTotalPremiumAmount(new BigDecimal("450.89"));
		fit3.setMonthlyTotalIndividualResponsibilityAmount(new BigDecimal("400.00"));
		fit3.setMonthlyCSRAmount(new BigDecimal("400.89"));
		fit3.setCSRVariantId("06");
		policy.getFinancialInformation().add(fit3);
		
		List<SbmErrWarningLogDTO> stateFinWarnings = sbmFinancialValidator.validateFinancialInfo(policy);
		
		assertFalse("stateFinErrors", stateFinWarnings.isEmpty());
		assertEquals("financialErrors", 3, stateFinWarnings.size());
		
		SbmErrWarningLogDTO financialFinWarning = stateFinWarnings.get(0);
		
		assertEquals("ElementInError", "MonthlyCSRAmount", financialFinWarning.getElementInError());
		assertEquals("Error Code", "WR-007", financialFinWarning.getErrorWarningTypeCd());
		
		assertEquals("fit1 CSR", 
				fit.getMonthlyTotalPremiumAmount().multiply(csrMultiplier).setScale(2, RoundingMode.HALF_UP), 
				fit.getMonthlyCSRAmount());
		
		assertEquals("fit2 CSR", 
				fit2.getMonthlyTotalPremiumAmount().multiply(csrMultiplier).setScale(2, RoundingMode.HALF_UP), 
				fit2.getMonthlyCSRAmount());
		
		assertEquals("fit3 CSR", 
				fit3.getMonthlyTotalPremiumAmount().multiply(csrMultiplier).setScale(2, RoundingMode.HALF_UP), 
				fit3.getMonthlyCSRAmount());
	}
	
	/*
	 * performStateSpecificFinancialValidations
	 */
	@Test
	public void test_performStateSpecificValidations_R015_Warning_Variant_02_03() {

		BigDecimal csrMultiplier = new BigDecimal("1.11");
		
		String qhpId = RandomStringUtils.randomAlphanumeric(16).toUpperCase();
		String stateCd = qhpId.substring(5, 7);
		
		List<String> bizRules = Arrays.asList("R015");
		SBMCache.getBusinessRules(stateCd).addAll(bizRules);
		
		expect(mockSbmDataService.getMetalLevelByQhpid(EasyMock.anyString(), EasyMock.anyString()))
		.andReturn(SbmMetalLevelType.GOLD.getValue()).anyTimes();
		expect(mockSbmDataService.getCsrMultiplierByVariantAndMetal(EasyMock.anyString(), EasyMock.anyString(), EasyMock.anyString()))
		.andReturn(csrMultiplier).anyTimes();
		replay(mockSbmDataService);	
		
		PolicyType policy = new PolicyType();
		policy.setPolicyStartDate(DateTimeUtil.getXMLGregorianCalendar(JAN_1));
		policy.setPolicyEndDate(DateTimeUtil.getXMLGregorianCalendar(APR_30));
		policy.setQHPId(qhpId);
		policy.setEffectuationIndicator("Y");
		
		FinancialInformation fit = new FinancialInformation();
		fit.setFinancialEffectiveStartDate(DateTimeUtil.getXMLGregorianCalendar(JAN_1));
		fit.setFinancialEffectiveEndDate(DateTimeUtil.getXMLGregorianCalendar(JAN_31));
		fit.setMonthlyTotalPremiumAmount(new BigDecimal("450.00"));
		fit.setMonthlyTotalIndividualResponsibilityAmount(new BigDecimal("400.00"));
		fit.setMonthlyCSRAmount(new BigDecimal("400.00"));
		fit.setCSRVariantId("02");
		policy.getFinancialInformation().add(fit);
		
		FinancialInformation fit2 = new FinancialInformation();
		fit2.setFinancialEffectiveStartDate(DateTimeUtil.getXMLGregorianCalendar(FEB_1));
		fit2.setFinancialEffectiveEndDate(DateTimeUtil.getXMLGregorianCalendar(MAR_1.minusDays(1)));
		fit2.setMonthlyTotalPremiumAmount(new BigDecimal("450.50"));
		fit2.setMonthlyTotalIndividualResponsibilityAmount(new BigDecimal("400.00"));
		fit2.setMonthlyCSRAmount(new BigDecimal("400.50"));
		fit2.setCSRVariantId("03");
		policy.getFinancialInformation().add(fit2);
		
		List<SbmErrWarningLogDTO> stateFinWarnings = sbmFinancialValidator.validateFinancialInfo(policy);
		
		assertFalse("stateFinErrors", stateFinWarnings.isEmpty());
		assertEquals("financialErrors", 2, stateFinWarnings.size());
		
		SbmErrWarningLogDTO financialFinWarning = stateFinWarnings.get(0);
		
		assertEquals("ElementInError", "MonthlyCSRAmount", financialFinWarning.getElementInError());
		assertEquals("Error Code", "WR-007", financialFinWarning.getErrorWarningTypeCd());
		
		assertEquals("fit1 CSR", 
				fit.getMonthlyTotalPremiumAmount().multiply(csrMultiplier).setScale(2, RoundingMode.HALF_UP), 
				fit.getMonthlyCSRAmount());
		
		assertEquals("fit2 CSR", 
				fit2.getMonthlyTotalPremiumAmount().multiply(csrMultiplier).setScale(2, RoundingMode.HALF_UP), 
				fit2.getMonthlyCSRAmount());
	}
	
	/*
	 * performStateSpecificFinancialValidations
	 */
	@Test
	public void test_performStateSpecificValidations_R015_No_Warning() {

		BigDecimal csrMultiplier = new BigDecimal("1.11");
		
		String qhpId = RandomStringUtils.randomAlphanumeric(16).toUpperCase();
		String stateCd = qhpId.substring(5, 7);
		
		List<String> bizRules = Arrays.asList("R015");
		SBMCache.getBusinessRules(stateCd).addAll(bizRules);
		
		expect(mockSbmDataService.getMetalLevelByQhpid(EasyMock.anyString(), EasyMock.anyString()))
		.andReturn(SbmMetalLevelType.GOLD.getValue()).anyTimes();
		expect(mockSbmDataService.getCsrMultiplierByVariant(EasyMock.anyString(), EasyMock.anyString()))
		.andReturn(csrMultiplier).anyTimes();
		expect(mockSbmDataService.getCsrMultiplierByVariantAndMetal(EasyMock.anyString(), EasyMock.anyString(), EasyMock.anyString()))
		.andReturn(csrMultiplier).anyTimes();
		replay(mockSbmDataService);	
		
		PolicyType policy = new PolicyType();
		policy.setPolicyStartDate(DateTimeUtil.getXMLGregorianCalendar(JAN_1));
		policy.setPolicyEndDate(DateTimeUtil.getXMLGregorianCalendar(APR_30));
		policy.setQHPId(qhpId);
		policy.setEffectuationIndicator("Y");
		
		FinancialInformation fit = new FinancialInformation();
		fit.setFinancialEffectiveStartDate(DateTimeUtil.getXMLGregorianCalendar(JAN_1));
		fit.setFinancialEffectiveEndDate(DateTimeUtil.getXMLGregorianCalendar(JAN_31));
		fit.setMonthlyTotalPremiumAmount(new BigDecimal("450.00"));
		fit.setMonthlyTotalIndividualResponsibilityAmount(new BigDecimal("400.00"));
		fit.setMonthlyCSRAmount(new BigDecimal("499.50"));
		fit.setCSRVariantId("02");
		policy.getFinancialInformation().add(fit);
		
		FinancialInformation fit2 = new FinancialInformation();
		fit2.setFinancialEffectiveStartDate(DateTimeUtil.getXMLGregorianCalendar(FEB_1));
		fit2.setFinancialEffectiveEndDate(DateTimeUtil.getXMLGregorianCalendar(MAR_1.minusDays(1)));
		fit2.setMonthlyTotalPremiumAmount(new BigDecimal("450.50"));
		fit2.setMonthlyTotalIndividualResponsibilityAmount(new BigDecimal("400.00"));
		fit2.setMonthlyCSRAmount(new BigDecimal("500.06"));
		fit2.setCSRVariantId("04");
		policy.getFinancialInformation().add(fit2);
		
		List<SbmErrWarningLogDTO> stateFinWarnings = sbmFinancialValidator.validateFinancialInfo(policy);
		
		assertTrue("stateFinErrors", stateFinWarnings.isEmpty());
		
		assertEquals("fit1 CSR", 
				fit.getMonthlyTotalPremiumAmount().multiply(csrMultiplier).setScale(2, RoundingMode.HALF_UP), 
				fit.getMonthlyCSRAmount());
		
		assertEquals("fit2 CSR", 
				fit2.getMonthlyTotalPremiumAmount().multiply(csrMultiplier).setScale(2, RoundingMode.HALF_UP), 
				fit2.getMonthlyCSRAmount());
	}
	
	/*
	 * performStateSpecificFinancialValidations
	 */
	@Test
	public void test_performStateSpecificValidations_R015_No_Warning_NonCsrVariant() {
		
		String qhpId = RandomStringUtils.randomAlphanumeric(16).toUpperCase();
		String stateCd = qhpId.substring(5, 7);
		
		List<String> bizRules = Arrays.asList("R015");
		SBMCache.getBusinessRules(stateCd).addAll(bizRules);
		
		expect(mockSbmDataService.getMetalLevelByQhpid(EasyMock.anyString(), EasyMock.anyString()))
		.andReturn(SbmMetalLevelType.SILVER.getValue()).anyTimes();
		replay(mockSbmDataService);	
		
		PolicyType policy = new PolicyType();
		policy.setPolicyStartDate(DateTimeUtil.getXMLGregorianCalendar(JAN_1));
		policy.setPolicyEndDate(DateTimeUtil.getXMLGregorianCalendar(APR_30));
		policy.setQHPId(qhpId);
		policy.setEffectuationIndicator("Y");
		
		FinancialInformation fit = new FinancialInformation();
		fit.setFinancialEffectiveStartDate(DateTimeUtil.getXMLGregorianCalendar(JAN_1));
		fit.setFinancialEffectiveEndDate(DateTimeUtil.getXMLGregorianCalendar(JAN_31));
		fit.setMonthlyTotalPremiumAmount(new BigDecimal("450.00"));
		fit.setMonthlyTotalIndividualResponsibilityAmount(new BigDecimal("450.00"));
		fit.setCSRVariantId("01");
		policy.getFinancialInformation().add(fit);
		
		FinancialInformation fit2 = new FinancialInformation();
		fit2.setFinancialEffectiveStartDate(DateTimeUtil.getXMLGregorianCalendar(FEB_1));
		fit2.setFinancialEffectiveEndDate(DateTimeUtil.getXMLGregorianCalendar(MAR_31));
		fit2.setMonthlyTotalPremiumAmount(new BigDecimal("500.00"));
		fit2.setMonthlyTotalIndividualResponsibilityAmount(new BigDecimal("50.00"));
		fit2.setMonthlyCSRAmount(new BigDecimal("400.00"));
		fit2.setCSRVariantId("00");
		policy.getFinancialInformation().add(fit2);

		
		List<SbmErrWarningLogDTO> stateFinWarnings = sbmFinancialValidator.validateFinancialInfo(policy);	
		
		assertTrue("stateFinErrors", stateFinWarnings.isEmpty());
	}
	
	/*
	 * performStateSpecificFinancialValidations
	 */
	@Test
	public void test_performStateSpecificValidations_R015_No_Warning_NullMetallevel() {
		
		String qhpId = RandomStringUtils.randomAlphanumeric(16).toUpperCase();
		String stateCd = qhpId.substring(5, 7);
		
		List<String> bizRules = Arrays.asList("R015");
		SBMCache.getBusinessRules(stateCd).addAll(bizRules);
		
		expect(mockSbmDataService.getMetalLevelByQhpid(EasyMock.anyString(), EasyMock.anyString()))
		.andReturn(null).anyTimes();
		replay(mockSbmDataService);	
		
		PolicyType policy = new PolicyType();
		policy.setPolicyStartDate(DateTimeUtil.getXMLGregorianCalendar(JAN_1));
		policy.setPolicyEndDate(DateTimeUtil.getXMLGregorianCalendar(APR_30));
		policy.setQHPId(qhpId);
		policy.setEffectuationIndicator("Y");
		
		FinancialInformation fit = new FinancialInformation();
		fit.setFinancialEffectiveStartDate(DateTimeUtil.getXMLGregorianCalendar(JAN_1));
		fit.setFinancialEffectiveEndDate(DateTimeUtil.getXMLGregorianCalendar(JAN_31));
		fit.setMonthlyTotalPremiumAmount(new BigDecimal("450.00"));
		fit.setMonthlyTotalIndividualResponsibilityAmount(new BigDecimal("450.00"));
		fit.setCSRVariantId("02");
		policy.getFinancialInformation().add(fit);
		
		FinancialInformation fit2 = new FinancialInformation();
		fit2.setFinancialEffectiveStartDate(DateTimeUtil.getXMLGregorianCalendar(FEB_1));
		fit2.setFinancialEffectiveEndDate(DateTimeUtil.getXMLGregorianCalendar(MAR_31));
		fit2.setMonthlyTotalPremiumAmount(new BigDecimal("500.00"));
		fit2.setMonthlyTotalIndividualResponsibilityAmount(new BigDecimal("50.00"));
		fit2.setMonthlyCSRAmount(new BigDecimal("400.00"));
		fit2.setCSRVariantId("03");
		policy.getFinancialInformation().add(fit2);

		List<SbmErrWarningLogDTO> financialErrors = sbmFinancialValidator.validateFinancialInfo(policy);
		
		assertTrue("financialErrors", financialErrors.isEmpty());
	}
	
	/*
	 * validateRatingAreaLength
	 */
	@Test
	public void test_validateRatingAreaFormat_R067_null() {
		
		String qhpId = RandomStringUtils.randomAlphanumeric(16).toUpperCase();
		String stateCd = qhpId.substring(5, 7);
		//TODO - get the db code
		List<String> bizRules = Arrays.asList("R067");
		SBMCache.getBusinessRules(stateCd).addAll(bizRules);
		
		expect(mockSbmDataService.getMetalLevelByQhpid(EasyMock.anyString(), EasyMock.anyString()))
		.andReturn(null).anyTimes();
		replay(mockSbmDataService);
		
		PolicyType policy = new PolicyType();
		policy.setPolicyStartDate(DateTimeUtil.getXMLGregorianCalendar(JAN_1));
		policy.setPolicyEndDate(DateTimeUtil.getXMLGregorianCalendar(APR_30));
		policy.setQHPId(qhpId);
		policy.setEffectuationIndicator("Y");
		
		FinancialInformation fit = new FinancialInformation();
		fit.setFinancialEffectiveStartDate(DateTimeUtil.getXMLGregorianCalendar(JAN_1));
		fit.setFinancialEffectiveEndDate(DateTimeUtil.getXMLGregorianCalendar(JAN_31));
		fit.setMonthlyTotalPremiumAmount(new BigDecimal("450.00"));
		fit.setMonthlyTotalIndividualResponsibilityAmount(new BigDecimal("450.00"));
		policy.getFinancialInformation().add(fit);
		
		List<SbmErrWarningLogDTO> stateFinWarnings = sbmFinancialValidator.validateFinancialInfo(policy);
		
		assertTrue("stateFinErrors", stateFinWarnings.isEmpty());

	}
	
	/*
	 * validateRatingAreaLength
	 */
	@Test
	public void test_validateRatingAreaFormat_R067_valid() {
		
		String qhpId = RandomStringUtils.randomAlphanumeric(16).toUpperCase();
		String stateCd = qhpId.substring(5, 7);
		//TODO - get the db code
		List<String> bizRules = Arrays.asList("R067");
		SBMCache.getBusinessRules(stateCd).addAll(bizRules);
		
		expect(mockSbmDataService.getMetalLevelByQhpid(EasyMock.anyString(), EasyMock.anyString()))
		.andReturn(null).anyTimes();
		replay(mockSbmDataService);
		
		PolicyType policy = new PolicyType();
		policy.setPolicyStartDate(DateTimeUtil.getXMLGregorianCalendar(JAN_1));
		policy.setPolicyEndDate(DateTimeUtil.getXMLGregorianCalendar(APR_30));
		policy.setQHPId(qhpId);
		policy.setEffectuationIndicator("Y");
		
		FinancialInformation fit = new FinancialInformation();
		fit.setFinancialEffectiveStartDate(DateTimeUtil.getXMLGregorianCalendar(JAN_1));
		fit.setFinancialEffectiveEndDate(DateTimeUtil.getXMLGregorianCalendar(JAN_31));
		fit.setMonthlyTotalPremiumAmount(new BigDecimal("450.00"));
		fit.setMonthlyTotalIndividualResponsibilityAmount(new BigDecimal("450.00"));
		fit.setRatingArea("R-VA001");
		policy.getFinancialInformation().add(fit);
		
		List<SbmErrWarningLogDTO> stateFinWarnings = sbmFinancialValidator.validateFinancialInfo(policy);
		
		assertTrue("no Warnings", stateFinWarnings.isEmpty());
	}
	
	/*
	 * validateRatingAreaLength
	 */
	@Test
	public void test_validateRatingArea_R067_invalid() {
		
		String qhpId = RandomStringUtils.randomAlphanumeric(16).toUpperCase();
		String stateCd = qhpId.substring(5, 7);
		//TODO - get the db code
		List<String> bizRules = Arrays.asList("R067");
		SBMCache.getBusinessRules(stateCd).addAll(bizRules);
		
		expect(mockSbmDataService.getMetalLevelByQhpid(EasyMock.anyString(), EasyMock.anyString()))
		.andReturn(null).anyTimes();
		replay(mockSbmDataService);
		
		String ratingArea = "R-VA0";
		
		PolicyType policy = new PolicyType();
		policy.setPolicyStartDate(DateTimeUtil.getXMLGregorianCalendar(JAN_1));
		policy.setPolicyEndDate(DateTimeUtil.getXMLGregorianCalendar(APR_30));
		policy.setQHPId(qhpId);
		policy.setEffectuationIndicator("Y");
		
		FinancialInformation fit = new FinancialInformation();
		fit.setFinancialEffectiveStartDate(DateTimeUtil.getXMLGregorianCalendar(JAN_1));
		fit.setFinancialEffectiveEndDate(DateTimeUtil.getXMLGregorianCalendar(JAN_31));
		fit.setMonthlyTotalPremiumAmount(new BigDecimal("450.00"));
		fit.setMonthlyTotalIndividualResponsibilityAmount(new BigDecimal("450.00"));
		fit.setRatingArea(ratingArea);
		policy.getFinancialInformation().add(fit);
		
		List<SbmErrWarningLogDTO> stateFinWarnings = sbmFinancialValidator.validateFinancialInfo(policy);
		
		assertFalse("issuer policy id Errors", stateFinWarnings.isEmpty());
		assertEquals("issuer id Errors", 1, stateFinWarnings.size());
		
		SbmErrWarningLogDTO stateFinWarning = stateFinWarnings.get(0);
		
		assertEquals("ElementInError", "RatingArea", stateFinWarning.getElementInError());
		assertEquals("Error Code", "WR-004", stateFinWarning.getErrorWarningTypeCd());
		
		assertNull("ratingArea", policy.getFinancialInformation().get(0).getRatingArea());
	}
	
	/*
	 * validateFinancialInfo
	 */
	@Test
	public void test_validateProratedAmounts_Overlap() {
		
		String qhpId = RandomStringUtils.randomAlphanumeric(16).toUpperCase();
		String stateCd = qhpId.substring(5, 7);

		List<String> bizRules = Arrays.asList("R016");
		SBMCache.getBusinessRules(stateCd).addAll(bizRules);
		
		expect(mockSbmDataService.getMetalLevelByQhpid(EasyMock.anyString(), EasyMock.anyString()))
		.andReturn(null).anyTimes();
		replay(mockSbmDataService);
		
		PolicyType policy = new PolicyType();
		policy.setPolicyStartDate(DateTimeUtil.getXMLGregorianCalendar(JAN_1));
		policy.setPolicyEndDate(DateTimeUtil.getXMLGregorianCalendar(APR_30));
		policy.setQHPId(qhpId);
		
		FinancialInformation fit = new FinancialInformation();
		
		fit.setFinancialEffectiveStartDate(DateTimeUtil.getXMLGregorianCalendar(JAN_1));
		fit.setFinancialEffectiveEndDate(DateTimeUtil.getXMLGregorianCalendar(JAN_15));
		
		ProratedAmountType proratedAmt11 = new ProratedAmountType();
		proratedAmt11.setPartialMonthEffectiveStartDate(DateTimeUtil.getXMLGregorianCalendar(JAN_1));
		proratedAmt11.setPartialMonthEffectiveEndDate(DateTimeUtil.getXMLGregorianCalendar(JAN_15));
		fit.getProratedAmount().add(proratedAmt11);
		
		ProratedAmountType proratedAmt12 = new ProratedAmountType();
		proratedAmt12.setPartialMonthEffectiveStartDate(DateTimeUtil.getXMLGregorianCalendar(JAN_15.plusDays(1)));
		proratedAmt12.setPartialMonthEffectiveEndDate(DateTimeUtil.getXMLGregorianCalendar(JAN_31));
		fit.getProratedAmount().add(proratedAmt12);
		
		policy.getFinancialInformation().add(fit);
		
		FinancialInformation fit2 = new FinancialInformation();
		
		fit2.setFinancialEffectiveStartDate(DateTimeUtil.getXMLGregorianCalendar(JAN_15.plusDays(1)));
		fit2.setFinancialEffectiveEndDate(DateTimeUtil.getXMLGregorianCalendar(APR_30));
		
		ProratedAmountType proratedAmt21 = new ProratedAmountType();
		proratedAmt21.setPartialMonthEffectiveStartDate(DateTimeUtil.getXMLGregorianCalendar(JAN_15.plusDays(1)));
		proratedAmt21.setPartialMonthEffectiveEndDate(DateTimeUtil.getXMLGregorianCalendar(JAN_31));
		fit2.getProratedAmount().add(proratedAmt21);
		
		ProratedAmountType proratedAmt22 = new ProratedAmountType();
		proratedAmt22.setPartialMonthEffectiveStartDate(DateTimeUtil.getXMLGregorianCalendar(FEB_1));
		proratedAmt22.setPartialMonthEffectiveEndDate(DateTimeUtil.getXMLGregorianCalendar(APR_30));
		fit2.getProratedAmount().add(proratedAmt22);
		
		policy.getFinancialInformation().add(fit2);
		
		List<SbmErrWarningLogDTO> financialErrors = sbmFinancialValidator.validateFinancialInfo(policy);
		
		assertFalse("financialErrors", financialErrors.isEmpty());
		assertEquals("financialErrors", 1, financialErrors.size());
		
		SbmErrWarningLogDTO financialError = financialErrors.get(0);
		
		assertEquals("ElementInError", "PartialMonthEffectiveStartDate", financialError.getElementInError());
		assertEquals("Error Code", "ER-043", financialError.getErrorWarningTypeCd());
	}
	
	/*
	 * validateFinancialInfo
	 */
	@Test
	public void test_validateProratedAmounts_No_Overlap() {
		
		String qhpId = RandomStringUtils.randomAlphanumeric(16).toUpperCase();
		String stateCd = qhpId.substring(5, 7);

		List<String> bizRules = Arrays.asList("R016");
		SBMCache.getBusinessRules(stateCd).addAll(bizRules);
		
		expect(mockSbmDataService.getMetalLevelByQhpid(EasyMock.anyString(), EasyMock.anyString()))
		.andReturn(null).anyTimes();
		replay(mockSbmDataService);
		
		PolicyType policy = new PolicyType();
		policy.setPolicyStartDate(DateTimeUtil.getXMLGregorianCalendar(JAN_1));
		policy.setPolicyEndDate(DateTimeUtil.getXMLGregorianCalendar(APR_30));
		policy.setQHPId(qhpId);
		
		FinancialInformation fit = new FinancialInformation();
		
		fit.setFinancialEffectiveStartDate(DateTimeUtil.getXMLGregorianCalendar(JAN_1));
		fit.setFinancialEffectiveEndDate(DateTimeUtil.getXMLGregorianCalendar(JAN_15));
		
		ProratedAmountType proratedAmt11 = new ProratedAmountType();
		proratedAmt11.setPartialMonthEffectiveStartDate(DateTimeUtil.getXMLGregorianCalendar(JAN_1));
		proratedAmt11.setPartialMonthEffectiveEndDate(DateTimeUtil.getXMLGregorianCalendar(JAN_15));
		fit.getProratedAmount().add(proratedAmt11);
		
		ProratedAmountType proratedAmt12 = new ProratedAmountType();
		proratedAmt12.setPartialMonthEffectiveStartDate(DateTimeUtil.getXMLGregorianCalendar(JAN_15.plusDays(1)));
		proratedAmt12.setPartialMonthEffectiveEndDate(DateTimeUtil.getXMLGregorianCalendar(JAN_31));
		fit.getProratedAmount().add(proratedAmt12);
		
		policy.getFinancialInformation().add(fit);
		
		FinancialInformation fit2 = new FinancialInformation();
		
		fit2.setFinancialEffectiveStartDate(DateTimeUtil.getXMLGregorianCalendar(JAN_15.plusDays(1)));
		fit2.setFinancialEffectiveEndDate(DateTimeUtil.getXMLGregorianCalendar(APR_30));
		
		ProratedAmountType proratedAmt21 = new ProratedAmountType();
		proratedAmt21.setPartialMonthEffectiveStartDate(DateTimeUtil.getXMLGregorianCalendar(FEB_1));
		proratedAmt21.setPartialMonthEffectiveEndDate(DateTimeUtil.getXMLGregorianCalendar(MAR_31));
		fit2.getProratedAmount().add(proratedAmt21);
		
		ProratedAmountType proratedAmt22 = new ProratedAmountType();
		proratedAmt22.setPartialMonthEffectiveStartDate(DateTimeUtil.getXMLGregorianCalendar(APR_1));
		proratedAmt22.setPartialMonthEffectiveEndDate(DateTimeUtil.getXMLGregorianCalendar(APR_30));
		fit2.getProratedAmount().add(proratedAmt22);
		
		policy.getFinancialInformation().add(fit2);
		
		List<SbmErrWarningLogDTO> financialErrors = sbmFinancialValidator.validateFinancialInfo(policy);
		
		assertTrue("financialErrors", financialErrors.isEmpty());
	}
	
	/*
	 * validateFinancialInfo
	 */
	@Test
	public void test_validateProratedAmounts_Gap() {
		
		String qhpId = RandomStringUtils.randomAlphanumeric(16).toUpperCase();
		String stateCd = qhpId.substring(5, 7);

		List<String> bizRules = Arrays.asList("R017");
		SBMCache.getBusinessRules(stateCd).addAll(bizRules);
		
		expect(mockSbmDataService.getMetalLevelByQhpid(EasyMock.anyString(), EasyMock.anyString()))
		.andReturn(null).anyTimes();
		replay(mockSbmDataService);
		
		PolicyType policy = new PolicyType();
		policy.setPolicyStartDate(DateTimeUtil.getXMLGregorianCalendar(JAN_1));
		policy.setPolicyEndDate(DateTimeUtil.getXMLGregorianCalendar(APR_30));
		policy.setQHPId(qhpId);
		
		FinancialInformation fit = new FinancialInformation();
		
		fit.setFinancialEffectiveStartDate(DateTimeUtil.getXMLGregorianCalendar(JAN_1));
		fit.setFinancialEffectiveEndDate(DateTimeUtil.getXMLGregorianCalendar(FEB_1));
		
		ProratedAmountType proratedAmt11 = new ProratedAmountType();
		proratedAmt11.setPartialMonthEffectiveStartDate(DateTimeUtil.getXMLGregorianCalendar(JAN_1));
		proratedAmt11.setPartialMonthEffectiveEndDate(DateTimeUtil.getXMLGregorianCalendar(JAN_15));
		fit.getProratedAmount().add(proratedAmt11);
		
		ProratedAmountType proratedAmt12 = new ProratedAmountType();
		proratedAmt12.setPartialMonthEffectiveStartDate(DateTimeUtil.getXMLGregorianCalendar(JAN_15.plusDays(1)));
		proratedAmt12.setPartialMonthEffectiveEndDate(DateTimeUtil.getXMLGregorianCalendar(FEB_1));
		fit.getProratedAmount().add(proratedAmt12);
		
		policy.getFinancialInformation().add(fit);
		
		FinancialInformation fit2 = new FinancialInformation();
		
		fit2.setFinancialEffectiveStartDate(DateTimeUtil.getXMLGregorianCalendar(FEB_15));
		fit2.setFinancialEffectiveEndDate(DateTimeUtil.getXMLGregorianCalendar(APR_30));
		
		ProratedAmountType proratedAmt21 = new ProratedAmountType();
		proratedAmt21.setPartialMonthEffectiveStartDate(DateTimeUtil.getXMLGregorianCalendar(FEB_15));
		proratedAmt21.setPartialMonthEffectiveEndDate(DateTimeUtil.getXMLGregorianCalendar(MAR_31));
		fit2.getProratedAmount().add(proratedAmt21);
		
		ProratedAmountType proratedAmt22 = new ProratedAmountType();
		proratedAmt22.setPartialMonthEffectiveStartDate(DateTimeUtil.getXMLGregorianCalendar(APR_1));
		proratedAmt22.setPartialMonthEffectiveEndDate(DateTimeUtil.getXMLGregorianCalendar(APR_30));
		fit2.getProratedAmount().add(proratedAmt22);
		
		policy.getFinancialInformation().add(fit2);
		
		List<SbmErrWarningLogDTO> financialErrors = sbmFinancialValidator.validateFinancialInfo(policy);
		
		assertFalse("financialErrors", financialErrors.isEmpty());
		assertEquals("financialErrors", 1, financialErrors.size());
		
		SbmErrWarningLogDTO financialError = financialErrors.get(0);
		
		assertEquals("ElementInError", "PartialMonthEffectiveStartDate", financialError.getElementInError());
		assertEquals("Error Code", "ER-044", financialError.getErrorWarningTypeCd());
	}
	
	/*
	 * validateFinancialInfo
	 */
	@Test
	public void test_validateProratedAmounts_Gap_Diff_Month() {
		
		String qhpId = RandomStringUtils.randomAlphanumeric(16).toUpperCase();
		String stateCd = qhpId.substring(5, 7);

		List<String> bizRules = Arrays.asList("R017");
		SBMCache.getBusinessRules(stateCd).addAll(bizRules);
		
		expect(mockSbmDataService.getMetalLevelByQhpid(EasyMock.anyString(), EasyMock.anyString()))
		.andReturn(null).anyTimes();
		replay(mockSbmDataService);
		
		PolicyType policy = new PolicyType();
		policy.setPolicyStartDate(DateTimeUtil.getXMLGregorianCalendar(JAN_1));
		policy.setPolicyEndDate(DateTimeUtil.getXMLGregorianCalendar(APR_30));
		policy.setQHPId(qhpId);
		
		FinancialInformation fit = new FinancialInformation();
		
		fit.setFinancialEffectiveStartDate(DateTimeUtil.getXMLGregorianCalendar(JAN_1));
		fit.setFinancialEffectiveEndDate(DateTimeUtil.getXMLGregorianCalendar(JAN_31));
		
		ProratedAmountType proratedAmt11 = new ProratedAmountType();
		proratedAmt11.setPartialMonthEffectiveStartDate(DateTimeUtil.getXMLGregorianCalendar(JAN_1));
		proratedAmt11.setPartialMonthEffectiveEndDate(DateTimeUtil.getXMLGregorianCalendar(JAN_15));
		fit.getProratedAmount().add(proratedAmt11);
		
		ProratedAmountType proratedAmt12 = new ProratedAmountType();
		proratedAmt12.setPartialMonthEffectiveStartDate(DateTimeUtil.getXMLGregorianCalendar(JAN_15.plusDays(1)));
		proratedAmt12.setPartialMonthEffectiveEndDate(DateTimeUtil.getXMLGregorianCalendar(JAN_31));
		fit.getProratedAmount().add(proratedAmt12);
		
		policy.getFinancialInformation().add(fit);
		
		FinancialInformation fit2 = new FinancialInformation();
		
		fit2.setFinancialEffectiveStartDate(DateTimeUtil.getXMLGregorianCalendar(FEB_15));
		fit2.setFinancialEffectiveEndDate(DateTimeUtil.getXMLGregorianCalendar(APR_30));
		
		ProratedAmountType proratedAmt21 = new ProratedAmountType();
		proratedAmt21.setPartialMonthEffectiveStartDate(DateTimeUtil.getXMLGregorianCalendar(FEB_15));
		proratedAmt21.setPartialMonthEffectiveEndDate(DateTimeUtil.getXMLGregorianCalendar(MAR_31));
		fit2.getProratedAmount().add(proratedAmt21);
		
		ProratedAmountType proratedAmt22 = new ProratedAmountType();
		proratedAmt22.setPartialMonthEffectiveStartDate(DateTimeUtil.getXMLGregorianCalendar(APR_1));
		proratedAmt22.setPartialMonthEffectiveEndDate(DateTimeUtil.getXMLGregorianCalendar(APR_30));
		fit2.getProratedAmount().add(proratedAmt22);
		
		policy.getFinancialInformation().add(fit2);
		
		List<SbmErrWarningLogDTO> financialErrors = sbmFinancialValidator.validateFinancialInfo(policy);
		
		assertTrue("financialErrors", financialErrors.isEmpty());
	}
	
	/*
	 * validateFinancialInfo
	 */
	@Test
	public void test_validateProratedAmounts_No_Gap() {
		
		String qhpId = RandomStringUtils.randomAlphanumeric(16).toUpperCase();
		String stateCd = qhpId.substring(5, 7);

		List<String> bizRules = Arrays.asList("R017");
		SBMCache.getBusinessRules(stateCd).addAll(bizRules);
		
		expect(mockSbmDataService.getMetalLevelByQhpid(EasyMock.anyString(), EasyMock.anyString()))
		.andReturn(null).anyTimes();
		replay(mockSbmDataService);
		
		PolicyType policy = new PolicyType();
		policy.setPolicyStartDate(DateTimeUtil.getXMLGregorianCalendar(JAN_1));
		policy.setPolicyEndDate(DateTimeUtil.getXMLGregorianCalendar(APR_30));
		policy.setQHPId(qhpId);
		
		FinancialInformation fit = new FinancialInformation();
		
		fit.setFinancialEffectiveStartDate(DateTimeUtil.getXMLGregorianCalendar(JAN_1));
		fit.setFinancialEffectiveEndDate(DateTimeUtil.getXMLGregorianCalendar(FEB_1));

		ProratedAmountType proratedAmt11 = new ProratedAmountType();
		proratedAmt11.setPartialMonthEffectiveStartDate(DateTimeUtil.getXMLGregorianCalendar(JAN_1));
		proratedAmt11.setPartialMonthEffectiveEndDate(DateTimeUtil.getXMLGregorianCalendar(JAN_15));
		fit.getProratedAmount().add(proratedAmt11);
		
		ProratedAmountType proratedAmt12 = new ProratedAmountType();
		proratedAmt12.setPartialMonthEffectiveStartDate(DateTimeUtil.getXMLGregorianCalendar(JAN_15.plusDays(1)));
		proratedAmt12.setPartialMonthEffectiveEndDate(DateTimeUtil.getXMLGregorianCalendar(FEB_1));
		fit.getProratedAmount().add(proratedAmt12);
		
		policy.getFinancialInformation().add(fit);
		
		FinancialInformation fit2 = new FinancialInformation();
		
		fit2.setFinancialEffectiveStartDate(DateTimeUtil.getXMLGregorianCalendar(FEB_1));
		fit2.setFinancialEffectiveEndDate(DateTimeUtil.getXMLGregorianCalendar(APR_30));
		
		ProratedAmountType proratedAmt21 = new ProratedAmountType();
		proratedAmt21.setPartialMonthEffectiveStartDate(DateTimeUtil.getXMLGregorianCalendar(FEB_1));
		proratedAmt21.setPartialMonthEffectiveEndDate(DateTimeUtil.getXMLGregorianCalendar(MAR_31));
		fit2.getProratedAmount().add(proratedAmt21);
		
		ProratedAmountType proratedAmt22 = new ProratedAmountType();
		proratedAmt22.setPartialMonthEffectiveStartDate(DateTimeUtil.getXMLGregorianCalendar(APR_1));
		proratedAmt22.setPartialMonthEffectiveEndDate(DateTimeUtil.getXMLGregorianCalendar(APR_30));
		fit2.getProratedAmount().add(proratedAmt22);
		
		policy.getFinancialInformation().add(fit2);
		
		List<SbmErrWarningLogDTO> financialErrors = sbmFinancialValidator.validateFinancialInfo(policy);
		
		assertTrue("financialErrors", financialErrors.isEmpty());
	}
	
	/*
	 * validateFinancialInfo
	 */
	@Test
	public void test_validateProratedAmounts_R018_invalid() {
		
		String qhpId = RandomStringUtils.randomAlphanumeric(16).toUpperCase();
		String stateCd = qhpId.substring(5, 7);

		List<String> bizRules = Arrays.asList("R018");
		SBMCache.getBusinessRules(stateCd).addAll(bizRules);
		
		expect(mockSbmDataService.getMetalLevelByQhpid(EasyMock.anyString(), EasyMock.anyString()))
		.andReturn(null).anyTimes();
		replay(mockSbmDataService);
		
		PolicyType policy = new PolicyType();
		policy.setPolicyStartDate(DateTimeUtil.getXMLGregorianCalendar(JAN_1));
		policy.setPolicyEndDate(DateTimeUtil.getXMLGregorianCalendar(APR_30));
		policy.setQHPId(qhpId);
		
		FinancialInformation fit = new FinancialInformation();
		
		fit.setFinancialEffectiveStartDate(DateTimeUtil.getXMLGregorianCalendar(JAN_1));
		fit.setFinancialEffectiveEndDate(DateTimeUtil.getXMLGregorianCalendar(MAR_15));
		
		ProratedAmountType proratedAmt11 = new ProratedAmountType();
		proratedAmt11.setPartialMonthEffectiveStartDate(DateTimeUtil.getXMLGregorianCalendar(MAR_1));
		proratedAmt11.setPartialMonthEffectiveEndDate(DateTimeUtil.getXMLGregorianCalendar(MAR_31));
		fit.getProratedAmount().add(proratedAmt11);
		
		policy.getFinancialInformation().add(fit);
		
		FinancialInformation fit2 = new FinancialInformation();
		
		fit2.setFinancialEffectiveStartDate(DateTimeUtil.getXMLGregorianCalendar(MAR_15.plusDays(1)));
		fit2.setFinancialEffectiveEndDate(DateTimeUtil.getXMLGregorianCalendar(APR_30));
		
		ProratedAmountType proratedAmt21 = new ProratedAmountType();
		proratedAmt21.setPartialMonthEffectiveStartDate(DateTimeUtil.getXMLGregorianCalendar(MAR_1));
		proratedAmt21.setPartialMonthEffectiveEndDate(DateTimeUtil.getXMLGregorianCalendar(MAR_31));
		fit2.getProratedAmount().add(proratedAmt21);
		
		policy.getFinancialInformation().add(fit2);
		
		List<SbmErrWarningLogDTO> financialErrors = sbmFinancialValidator.validateFinancialInfo(policy);
		
		assertFalse("financialErrors", financialErrors.isEmpty());
		assertEquals("financialErrors", 4, financialErrors.size());
		
		SbmErrWarningLogDTO financialError = financialErrors.get(0);
		
		assertEquals("ElementInError", "PartialMonthEffectiveStartDate", financialError.getElementInError());
		assertEquals("Error Code", "ER-045", financialError.getErrorWarningTypeCd());
		
		financialError = financialErrors.get(1);
		
		assertEquals("ElementInError", "PartialMonthEffectiveEndDate", financialError.getElementInError());
		assertEquals("Error Code", "ER-045", financialError.getErrorWarningTypeCd());
		
		financialError = financialErrors.get(2);
		
		assertEquals("ElementInError", "PartialMonthEffectiveStartDate", financialError.getElementInError());
		assertEquals("Error Code", "ER-045", financialError.getErrorWarningTypeCd());
		
		financialError = financialErrors.get(3);
		
		assertEquals("ElementInError", "PartialMonthEffectiveEndDate", financialError.getElementInError());
		assertEquals("Error Code", "ER-045", financialError.getErrorWarningTypeCd());
		
	}
	
	/*
	 * validateFinancialInfo
	 */
	@Test
	public void test_validateProratedAmounts_R018_invalid_2() {
		
		String qhpId = RandomStringUtils.randomAlphanumeric(16).toUpperCase();
		String stateCd = qhpId.substring(5, 7);

		List<String> bizRules = Arrays.asList("R018");
		SBMCache.getBusinessRules(stateCd).addAll(bizRules);
		
		expect(mockSbmDataService.getMetalLevelByQhpid(EasyMock.anyString(), EasyMock.anyString()))
		.andReturn(null).anyTimes();
		replay(mockSbmDataService);
		
		PolicyType policy = new PolicyType();
		policy.setPolicyStartDate(DateTimeUtil.getXMLGregorianCalendar(JAN_1));
		policy.setPolicyEndDate(DateTimeUtil.getXMLGregorianCalendar(APR_30));
		policy.setQHPId(qhpId);
		
		FinancialInformation fit = new FinancialInformation();
		
		fit.setFinancialEffectiveStartDate(DateTimeUtil.getXMLGregorianCalendar(JAN_1));
		fit.setFinancialEffectiveEndDate(DateTimeUtil.getXMLGregorianCalendar(MAR_15));
		
		policy.getFinancialInformation().add(fit);
		
		FinancialInformation fit2 = new FinancialInformation();
		
		fit2.setFinancialEffectiveStartDate(DateTimeUtil.getXMLGregorianCalendar(MAR_15.plusDays(1)));
		fit2.setFinancialEffectiveEndDate(DateTimeUtil.getXMLGregorianCalendar(APR_30));
		
		ProratedAmountType proratedAmt21 = new ProratedAmountType();
		proratedAmt21.setPartialMonthEffectiveStartDate(DateTimeUtil.getXMLGregorianCalendar(APR_1));
		proratedAmt21.setPartialMonthEffectiveEndDate(DateTimeUtil.getXMLGregorianCalendar(APR_15));
		fit2.getProratedAmount().add(proratedAmt21);
		
		policy.getFinancialInformation().add(fit2);
		
		List<SbmErrWarningLogDTO> financialErrors = sbmFinancialValidator.validateFinancialInfo(policy);
		
		assertFalse("financialErrors", financialErrors.isEmpty());
		assertEquals("financialErrors", 2, financialErrors.size());
		
		SbmErrWarningLogDTO financialError = financialErrors.get(0);
		
		assertEquals("ElementInError", "PartialMonthEffectiveStartDate", financialError.getElementInError());
		assertEquals("Error Code", "ER-045", financialError.getErrorWarningTypeCd());
		
		financialError = financialErrors.get(1);
		
		assertEquals("ElementInError", "PartialMonthEffectiveEndDate", financialError.getElementInError());
		assertEquals("Error Code", "ER-045", financialError.getErrorWarningTypeCd());
	}
	
	/*
	 * 2 proration nodes each
	 */
	@Test
	public void test_validateProratedAmounts_R018_invalid_3() {
		
		String qhpId = RandomStringUtils.randomAlphanumeric(16).toUpperCase();
		String stateCd = qhpId.substring(5, 7);

		List<String> bizRules = Arrays.asList("R018");
		SBMCache.getBusinessRules(stateCd).addAll(bizRules);
		
		expect(mockSbmDataService.getMetalLevelByQhpid(EasyMock.anyString(), EasyMock.anyString()))
		.andReturn(null).anyTimes();
		replay(mockSbmDataService);
		
		PolicyType policy = new PolicyType();
		policy.setPolicyStartDate(DateTimeUtil.getXMLGregorianCalendar(JAN_15));
		policy.setPolicyEndDate(DateTimeUtil.getXMLGregorianCalendar(APR_30));
		policy.setQHPId(qhpId);
		
		FinancialInformation fit = new FinancialInformation();
		
		fit.setFinancialEffectiveStartDate(DateTimeUtil.getXMLGregorianCalendar(JAN_15));
		fit.setFinancialEffectiveEndDate(DateTimeUtil.getXMLGregorianCalendar(MAR_15));
		
		ProratedAmountType proratedAmt11 = new ProratedAmountType();
		proratedAmt11.setPartialMonthEffectiveStartDate(DateTimeUtil.getXMLGregorianCalendar(JAN_15));
		proratedAmt11.setPartialMonthEffectiveEndDate(DateTimeUtil.getXMLGregorianCalendar(JAN_31));
		fit.getProratedAmount().add(proratedAmt11);
		
		ProratedAmountType proratedAmt12 = new ProratedAmountType();
		proratedAmt12.setPartialMonthEffectiveStartDate(DateTimeUtil.getXMLGregorianCalendar(MAR_1));
		proratedAmt12.setPartialMonthEffectiveEndDate(DateTimeUtil.getXMLGregorianCalendar(MAR_15.minusDays(1)));
		fit.getProratedAmount().add(proratedAmt12);
		
		policy.getFinancialInformation().add(fit);
		
		FinancialInformation fit2 = new FinancialInformation();
		
		fit2.setFinancialEffectiveStartDate(DateTimeUtil.getXMLGregorianCalendar(MAR_15.plusDays(1)));
		fit2.setFinancialEffectiveEndDate(DateTimeUtil.getXMLGregorianCalendar(APR_30));
		
		ProratedAmountType proratedAmt22 = new ProratedAmountType();
		proratedAmt22.setPartialMonthEffectiveStartDate(DateTimeUtil.getXMLGregorianCalendar(MAR_15));
		proratedAmt22.setPartialMonthEffectiveEndDate(DateTimeUtil.getXMLGregorianCalendar(MAR_31));
		fit2.getProratedAmount().add(proratedAmt22);
		
		policy.getFinancialInformation().add(fit2);
		
		List<SbmErrWarningLogDTO> financialErrors = sbmFinancialValidator.validateFinancialInfo(policy);
		
		assertFalse("financialErrors", financialErrors.isEmpty());
		assertEquals("financialErrors", 4, financialErrors.size());
		
		SbmErrWarningLogDTO financialError = financialErrors.get(0);
		
		assertEquals("ElementInError", "PartialMonthEffectiveStartDate", financialError.getElementInError());
		assertEquals("Error Code", "ER-045", financialError.getErrorWarningTypeCd());
		
		financialError = financialErrors.get(1);
		
		assertEquals("ElementInError", "PartialMonthEffectiveEndDate", financialError.getElementInError());
		assertEquals("Error Code", "ER-045", financialError.getErrorWarningTypeCd());
		
		financialError = financialErrors.get(2);
		assertEquals("ElementInError", "PartialMonthEffectiveStartDate", financialError.getElementInError());
		assertEquals("Error Code", "ER-045", financialError.getErrorWarningTypeCd());
		
		financialError = financialErrors.get(3);
		assertEquals("ElementInError", "PartialMonthEffectiveEndDate", financialError.getElementInError());
		assertEquals("Error Code", "ER-045", financialError.getErrorWarningTypeCd());
		
	}
	
	/*
	 * validateFinancialInfo
	 */
	@Test
	public void test_validateProratedAmounts_R018_Valid() {
		
		String qhpId = RandomStringUtils.randomAlphanumeric(16).toUpperCase();
		String stateCd = qhpId.substring(5, 7);

		List<String> bizRules = Arrays.asList("R018");
		SBMCache.getBusinessRules(stateCd).addAll(bizRules);
		
		expect(mockSbmDataService.getMetalLevelByQhpid(EasyMock.anyString(), EasyMock.anyString()))
		.andReturn(null).anyTimes();
		replay(mockSbmDataService);
		
		PolicyType policy = new PolicyType();
		policy.setPolicyStartDate(DateTimeUtil.getXMLGregorianCalendar(JAN_1));
		policy.setPolicyEndDate(DateTimeUtil.getXMLGregorianCalendar(APR_30));
		policy.setQHPId(qhpId);
		
		FinancialInformation fit = new FinancialInformation();
		
		fit.setFinancialEffectiveStartDate(DateTimeUtil.getXMLGregorianCalendar(JAN_1));
		fit.setFinancialEffectiveEndDate(DateTimeUtil.getXMLGregorianCalendar(MAR_15));
		
		ProratedAmountType proratedAmt11 = new ProratedAmountType();
		proratedAmt11.setPartialMonthEffectiveStartDate(DateTimeUtil.getXMLGregorianCalendar(MAR_1));
		proratedAmt11.setPartialMonthEffectiveEndDate(DateTimeUtil.getXMLGregorianCalendar(MAR_15));
		fit.getProratedAmount().add(proratedAmt11);
		
		policy.getFinancialInformation().add(fit);
		
		FinancialInformation fit2 = new FinancialInformation();
		
		fit2.setFinancialEffectiveStartDate(DateTimeUtil.getXMLGregorianCalendar(MAR_15.plusDays(1)));
		fit2.setFinancialEffectiveEndDate(DateTimeUtil.getXMLGregorianCalendar(APR_30));
		
		ProratedAmountType proratedAmt21 = new ProratedAmountType();
		proratedAmt21.setPartialMonthEffectiveStartDate(DateTimeUtil.getXMLGregorianCalendar(MAR_15.plusDays(1)));
		proratedAmt21.setPartialMonthEffectiveEndDate(DateTimeUtil.getXMLGregorianCalendar(MAR_31));
		fit2.getProratedAmount().add(proratedAmt21);
		
		policy.getFinancialInformation().add(fit2);
		
		List<SbmErrWarningLogDTO> financialErrors = sbmFinancialValidator.validateFinancialInfo(policy);
		
		assertTrue("financialErrors", financialErrors.isEmpty());
	}
	
	/*
	 * validateFinancialInfo
	 */
	@Test
	public void test_validateProratedAmounts_R019_invalid_null_Premium() {
		
		String qhpId = RandomStringUtils.randomAlphanumeric(16).toUpperCase();
		String stateCd = qhpId.substring(5, 7);

		List<String> bizRules = Arrays.asList("R019");
		SBMCache.getBusinessRules(stateCd).addAll(bizRules);
		
		expect(mockSbmDataService.getMetalLevelByQhpid(EasyMock.anyString(), EasyMock.anyString()))
		.andReturn(null).anyTimes();
		replay(mockSbmDataService);
		
		PolicyType policy = new PolicyType();
		policy.setPolicyStartDate(DateTimeUtil.getXMLGregorianCalendar(JAN_1));
		policy.setPolicyEndDate(DateTimeUtil.getXMLGregorianCalendar(DEC_31));
		policy.setQHPId(qhpId);
		
		FinancialInformation fit = new FinancialInformation();
		
		fit.setFinancialEffectiveStartDate(DateTimeUtil.getXMLGregorianCalendar(JAN_1));
		fit.setFinancialEffectiveEndDate(DateTimeUtil.getXMLGregorianCalendar(MAR_15));
		fit.setMonthlyTotalPremiumAmount(new BigDecimal("100"));
		fit.setMonthlyAPTCAmount(new BigDecimal("100"));
		fit.setMonthlyCSRAmount(new BigDecimal("20"));
		
		ProratedAmountType proratedAmt11 = new ProratedAmountType();
		proratedAmt11.setPartialMonthEffectiveStartDate(DateTimeUtil.getXMLGregorianCalendar(MAR_1));
		proratedAmt11.setPartialMonthEffectiveEndDate(DateTimeUtil.getXMLGregorianCalendar(MAR_15));
		proratedAmt11.setPartialMonthPremiumAmount(new BigDecimal("50"));
		proratedAmt11.setPartialMonthAPTCAmount(new BigDecimal("25"));
		proratedAmt11.setPartialMonthCSRAmount(new BigDecimal("10"));
		fit.getProratedAmount().add(proratedAmt11);
		
		policy.getFinancialInformation().add(fit);
		
		
		FinancialInformation fit2 = new FinancialInformation();
		
		fit2.setFinancialEffectiveStartDate(DateTimeUtil.getXMLGregorianCalendar(MAR_15.plusDays(1)));
		fit2.setFinancialEffectiveEndDate(DateTimeUtil.getXMLGregorianCalendar(DEC_31));
		fit2.setMonthlyTotalPremiumAmount(new BigDecimal("110"));
		fit2.setMonthlyAPTCAmount(new BigDecimal("60"));
		fit2.setMonthlyCSRAmount(new BigDecimal("30"));
		
		ProratedAmountType proratedAmt21 = new ProratedAmountType();
		proratedAmt21.setPartialMonthEffectiveStartDate(DateTimeUtil.getXMLGregorianCalendar(MAR_15.plusDays(1)));
		proratedAmt21.setPartialMonthEffectiveEndDate(DateTimeUtil.getXMLGregorianCalendar(MAR_31));
		//proratedAmt21.setPartialMonthPremiumAmount(new BigDecimal("0"));
		proratedAmt21.setPartialMonthAPTCAmount(new BigDecimal("30"));
		proratedAmt21.setPartialMonthCSRAmount(new BigDecimal("15"));
		fit2.getProratedAmount().add(proratedAmt21);
		
		policy.getFinancialInformation().add(fit2);
		
		List<SbmErrWarningLogDTO> financialErrors = sbmFinancialValidator.validateFinancialInfo(policy);
		
		assertFalse("financialErrors", financialErrors.isEmpty());
		assertEquals("financialErrors", 1, financialErrors.size());
		
		SbmErrWarningLogDTO financialError = financialErrors.get(0);
		
		assertEquals("ElementInError", "FinancialInformation", financialError.getElementInError());
		assertEquals("Error Code", "ER-046", financialError.getErrorWarningTypeCd());
		
	}
	
	/*
	 * validateFinancialInfo
	 */
	@Test
	public void test_validateProratedAmounts_R019_invalid_zero_Premium() {
		
		String qhpId = RandomStringUtils.randomAlphanumeric(16).toUpperCase();
		String stateCd = qhpId.substring(5, 7);

		List<String> bizRules = Arrays.asList("R019");
		SBMCache.getBusinessRules(stateCd).addAll(bizRules);
		
		expect(mockSbmDataService.getMetalLevelByQhpid(EasyMock.anyString(), EasyMock.anyString()))
		.andReturn(null).anyTimes();
		replay(mockSbmDataService);
		
		PolicyType policy = new PolicyType();
		policy.setPolicyStartDate(DateTimeUtil.getXMLGregorianCalendar(JAN_1));
		policy.setPolicyEndDate(DateTimeUtil.getXMLGregorianCalendar(DEC_31));
		policy.setQHPId(qhpId);
		
		FinancialInformation fit = new FinancialInformation();
		
		fit.setFinancialEffectiveStartDate(DateTimeUtil.getXMLGregorianCalendar(JAN_1));
		fit.setFinancialEffectiveEndDate(DateTimeUtil.getXMLGregorianCalendar(MAR_15));
		fit.setMonthlyTotalPremiumAmount(new BigDecimal("100"));
		fit.setMonthlyAPTCAmount(new BigDecimal("100"));
		fit.setMonthlyCSRAmount(new BigDecimal("20"));
		
		ProratedAmountType proratedAmt11 = new ProratedAmountType();
		proratedAmt11.setPartialMonthEffectiveStartDate(DateTimeUtil.getXMLGregorianCalendar(MAR_1));
		proratedAmt11.setPartialMonthEffectiveEndDate(DateTimeUtil.getXMLGregorianCalendar(MAR_15));
		proratedAmt11.setPartialMonthPremiumAmount(new BigDecimal("50"));
		proratedAmt11.setPartialMonthAPTCAmount(new BigDecimal("25"));
		proratedAmt11.setPartialMonthCSRAmount(new BigDecimal("10"));
		fit.getProratedAmount().add(proratedAmt11);
		
		policy.getFinancialInformation().add(fit);
		
		
		FinancialInformation fit2 = new FinancialInformation();
		
		fit2.setFinancialEffectiveStartDate(DateTimeUtil.getXMLGregorianCalendar(MAR_15.plusDays(1)));
		fit2.setFinancialEffectiveEndDate(DateTimeUtil.getXMLGregorianCalendar(DEC_31));
		fit2.setMonthlyTotalPremiumAmount(new BigDecimal("110"));
		fit2.setMonthlyAPTCAmount(new BigDecimal("60"));
		fit2.setMonthlyCSRAmount(new BigDecimal("30"));
		
		ProratedAmountType proratedAmt21 = new ProratedAmountType();
		proratedAmt21.setPartialMonthEffectiveStartDate(DateTimeUtil.getXMLGregorianCalendar(MAR_15.plusDays(1)));
		proratedAmt21.setPartialMonthEffectiveEndDate(DateTimeUtil.getXMLGregorianCalendar(MAR_31));
		proratedAmt21.setPartialMonthPremiumAmount(new BigDecimal("0"));
		proratedAmt21.setPartialMonthAPTCAmount(new BigDecimal("30"));
		proratedAmt21.setPartialMonthCSRAmount(new BigDecimal("15"));
		fit2.getProratedAmount().add(proratedAmt21);
		
		policy.getFinancialInformation().add(fit2);
		
		List<SbmErrWarningLogDTO> financialErrors = sbmFinancialValidator.validateFinancialInfo(policy);
		
		assertFalse("financialErrors", financialErrors.isEmpty());
		assertEquals("financialErrors", 1, financialErrors.size());
		
		SbmErrWarningLogDTO financialError = financialErrors.get(0);
		
		assertEquals("ElementInError", "FinancialInformation", financialError.getElementInError());
		assertEquals("Error Code", "ER-046", financialError.getErrorWarningTypeCd());
	}
	
	/*
	 * validateFinancialInfo
	 */
	@Test
	public void test_validateProratedAmounts_R019_invalid_null_aptc() {
		
		String qhpId = RandomStringUtils.randomAlphanumeric(16).toUpperCase();
		String stateCd = qhpId.substring(5, 7);

		List<String> bizRules = Arrays.asList("R019");
		SBMCache.getBusinessRules(stateCd).addAll(bizRules);
		
		expect(mockSbmDataService.getMetalLevelByQhpid(EasyMock.anyString(), EasyMock.anyString()))
		.andReturn(null).anyTimes();
		replay(mockSbmDataService);
		
		PolicyType policy = new PolicyType();
		policy.setPolicyStartDate(DateTimeUtil.getXMLGregorianCalendar(JAN_1));
		policy.setPolicyEndDate(DateTimeUtil.getXMLGregorianCalendar(DEC_31));
		policy.setQHPId(qhpId);
		
		FinancialInformation fit = new FinancialInformation();
		
		fit.setFinancialEffectiveStartDate(DateTimeUtil.getXMLGregorianCalendar(JAN_1));
		fit.setFinancialEffectiveEndDate(DateTimeUtil.getXMLGregorianCalendar(MAR_15));
		fit.setMonthlyTotalPremiumAmount(new BigDecimal("100"));
		fit.setMonthlyAPTCAmount(new BigDecimal("100"));
		fit.setMonthlyCSRAmount(new BigDecimal("20"));
		
		ProratedAmountType proratedAmt11 = new ProratedAmountType();
		proratedAmt11.setPartialMonthEffectiveStartDate(DateTimeUtil.getXMLGregorianCalendar(MAR_1));
		proratedAmt11.setPartialMonthEffectiveEndDate(DateTimeUtil.getXMLGregorianCalendar(MAR_15));
		proratedAmt11.setPartialMonthPremiumAmount(new BigDecimal("50"));
		proratedAmt11.setPartialMonthAPTCAmount(new BigDecimal("25"));
		proratedAmt11.setPartialMonthCSRAmount(new BigDecimal("10"));
		fit.getProratedAmount().add(proratedAmt11);
		
		policy.getFinancialInformation().add(fit);
		
		
		FinancialInformation fit2 = new FinancialInformation();
		
		fit2.setFinancialEffectiveStartDate(DateTimeUtil.getXMLGregorianCalendar(MAR_15.plusDays(1)));
		fit2.setFinancialEffectiveEndDate(DateTimeUtil.getXMLGregorianCalendar(DEC_31));
		fit2.setMonthlyTotalPremiumAmount(new BigDecimal("110"));
		fit2.setMonthlyAPTCAmount(new BigDecimal("60"));
		fit2.setMonthlyCSRAmount(new BigDecimal("30"));
		
		ProratedAmountType proratedAmt21 = new ProratedAmountType();
		proratedAmt21.setPartialMonthEffectiveStartDate(DateTimeUtil.getXMLGregorianCalendar(MAR_15.plusDays(1)));
		proratedAmt21.setPartialMonthEffectiveEndDate(DateTimeUtil.getXMLGregorianCalendar(MAR_31));
		proratedAmt21.setPartialMonthPremiumAmount(new BigDecimal("55"));
		//proratedAmt21.setPartialMonthAPTCAmount(new BigDecimal("30"));
		proratedAmt21.setPartialMonthCSRAmount(new BigDecimal("15"));
		fit2.getProratedAmount().add(proratedAmt21);
		
		policy.getFinancialInformation().add(fit2);
		
		List<SbmErrWarningLogDTO> financialErrors = sbmFinancialValidator.validateFinancialInfo(policy);
		
		assertFalse("financialErrors", financialErrors.isEmpty());
		assertEquals("financialErrors", 1, financialErrors.size());
		
		SbmErrWarningLogDTO financialError = financialErrors.get(0);
		
		assertEquals("ElementInError", "FinancialInformation", financialError.getElementInError());
		assertEquals("Error Code", "ER-046", financialError.getErrorWarningTypeCd());
	}
	
	/*
	 * validateFinancialInfo
	 */
	@Test
	public void test_validateProratedAmounts_R019_invalid_zero_aptc() {
		
		String qhpId = RandomStringUtils.randomAlphanumeric(16).toUpperCase();
		String stateCd = qhpId.substring(5, 7);

		List<String> bizRules = Arrays.asList("R019");
		SBMCache.getBusinessRules(stateCd).addAll(bizRules);
		
		expect(mockSbmDataService.getMetalLevelByQhpid(EasyMock.anyString(), EasyMock.anyString()))
		.andReturn(null).anyTimes();
		replay(mockSbmDataService);
		
		PolicyType policy = new PolicyType();
		policy.setPolicyStartDate(DateTimeUtil.getXMLGregorianCalendar(JAN_1));
		policy.setPolicyEndDate(DateTimeUtil.getXMLGregorianCalendar(DEC_31));
		policy.setQHPId(qhpId);
		
		FinancialInformation fit = new FinancialInformation();
		
		fit.setFinancialEffectiveStartDate(DateTimeUtil.getXMLGregorianCalendar(JAN_1));
		fit.setFinancialEffectiveEndDate(DateTimeUtil.getXMLGregorianCalendar(MAR_15));
		fit.setMonthlyTotalPremiumAmount(new BigDecimal("100"));
		fit.setMonthlyAPTCAmount(new BigDecimal("100"));
		fit.setMonthlyCSRAmount(new BigDecimal("20"));
		
		ProratedAmountType proratedAmt11 = new ProratedAmountType();
		proratedAmt11.setPartialMonthEffectiveStartDate(DateTimeUtil.getXMLGregorianCalendar(MAR_1));
		proratedAmt11.setPartialMonthEffectiveEndDate(DateTimeUtil.getXMLGregorianCalendar(MAR_15));
		proratedAmt11.setPartialMonthPremiumAmount(new BigDecimal("50"));
		proratedAmt11.setPartialMonthAPTCAmount(new BigDecimal("25"));
		proratedAmt11.setPartialMonthCSRAmount(new BigDecimal("10"));
		fit.getProratedAmount().add(proratedAmt11);
		
		policy.getFinancialInformation().add(fit);
		
		
		FinancialInformation fit2 = new FinancialInformation();
		
		fit2.setFinancialEffectiveStartDate(DateTimeUtil.getXMLGregorianCalendar(MAR_15.plusDays(1)));
		fit2.setFinancialEffectiveEndDate(DateTimeUtil.getXMLGregorianCalendar(DEC_31));
		fit2.setMonthlyTotalPremiumAmount(new BigDecimal("110"));
		fit2.setMonthlyAPTCAmount(new BigDecimal("60"));
		fit2.setMonthlyCSRAmount(new BigDecimal("30"));
		
		ProratedAmountType proratedAmt21 = new ProratedAmountType();
		proratedAmt21.setPartialMonthEffectiveStartDate(DateTimeUtil.getXMLGregorianCalendar(MAR_15.plusDays(1)));
		proratedAmt21.setPartialMonthEffectiveEndDate(DateTimeUtil.getXMLGregorianCalendar(MAR_31));
		proratedAmt21.setPartialMonthPremiumAmount(new BigDecimal("55"));
		proratedAmt21.setPartialMonthAPTCAmount(new BigDecimal("0"));
		proratedAmt21.setPartialMonthCSRAmount(new BigDecimal("15"));
		fit2.getProratedAmount().add(proratedAmt21);
		
		policy.getFinancialInformation().add(fit2);
		
		List<SbmErrWarningLogDTO> financialErrors = sbmFinancialValidator.validateFinancialInfo(policy);
		
		assertFalse("financialErrors", financialErrors.isEmpty());
		assertEquals("financialErrors", 1, financialErrors.size());
		
		SbmErrWarningLogDTO financialError = financialErrors.get(0);
		
		assertEquals("ElementInError", "FinancialInformation", financialError.getElementInError());
		assertEquals("Error Code", "ER-046", financialError.getErrorWarningTypeCd());
	}
	
	/*
	 * validateFinancialInfo
	 */
	@Test
	public void test_validateProratedAmounts_R019_invalid_null_csr() {

		String qhpId = RandomStringUtils.randomAlphanumeric(16).toUpperCase();
		String stateCd = qhpId.substring(5, 7);

		List<String> bizRules = Arrays.asList("R019");
		SBMCache.getBusinessRules(stateCd).addAll(bizRules);
		
		expect(mockSbmDataService.getMetalLevelByQhpid(EasyMock.anyString(), EasyMock.anyString()))
		.andReturn(null).anyTimes();
		replay(mockSbmDataService);
		
		PolicyType policy = new PolicyType();
		policy.setPolicyStartDate(DateTimeUtil.getXMLGregorianCalendar(JAN_1));
		policy.setPolicyEndDate(DateTimeUtil.getXMLGregorianCalendar(DEC_31));
		policy.setQHPId(qhpId);
		
		FinancialInformation fit = new FinancialInformation();
		
		fit.setFinancialEffectiveStartDate(DateTimeUtil.getXMLGregorianCalendar(JAN_1));
		fit.setFinancialEffectiveEndDate(DateTimeUtil.getXMLGregorianCalendar(MAR_15));
		fit.setMonthlyTotalPremiumAmount(new BigDecimal("100"));
		fit.setMonthlyAPTCAmount(new BigDecimal("100"));
		fit.setMonthlyCSRAmount(new BigDecimal("20"));
		
		ProratedAmountType proratedAmt11 = new ProratedAmountType();
		proratedAmt11.setPartialMonthEffectiveStartDate(DateTimeUtil.getXMLGregorianCalendar(MAR_1));
		proratedAmt11.setPartialMonthEffectiveEndDate(DateTimeUtil.getXMLGregorianCalendar(MAR_15));
		proratedAmt11.setPartialMonthPremiumAmount(new BigDecimal("50"));
		proratedAmt11.setPartialMonthAPTCAmount(new BigDecimal("25"));
		proratedAmt11.setPartialMonthCSRAmount(new BigDecimal("10"));
		fit.getProratedAmount().add(proratedAmt11);
		
		policy.getFinancialInformation().add(fit);
		
		
		FinancialInformation fit2 = new FinancialInformation();
		
		fit2.setFinancialEffectiveStartDate(DateTimeUtil.getXMLGregorianCalendar(MAR_15.plusDays(1)));
		fit2.setFinancialEffectiveEndDate(DateTimeUtil.getXMLGregorianCalendar(DEC_31));
		fit2.setMonthlyTotalPremiumAmount(new BigDecimal("110"));
		fit2.setMonthlyAPTCAmount(new BigDecimal("60"));
		fit2.setMonthlyCSRAmount(new BigDecimal("30"));
		
		ProratedAmountType proratedAmt21 = new ProratedAmountType();
		proratedAmt21.setPartialMonthEffectiveStartDate(DateTimeUtil.getXMLGregorianCalendar(MAR_15.plusDays(1)));
		proratedAmt21.setPartialMonthEffectiveEndDate(DateTimeUtil.getXMLGregorianCalendar(MAR_31));
		proratedAmt21.setPartialMonthPremiumAmount(new BigDecimal("55"));
		proratedAmt21.setPartialMonthAPTCAmount(new BigDecimal("30"));
		//proratedAmt21.setPartialMonthCSRAmount(new BigDecimal("0"));
		fit2.getProratedAmount().add(proratedAmt21);
		
		policy.getFinancialInformation().add(fit2);
		
		List<SbmErrWarningLogDTO> financialErrors = sbmFinancialValidator.validateFinancialInfo(policy);
		
		assertFalse("financialErrors", financialErrors.isEmpty());
		assertEquals("financialErrors", 1, financialErrors.size());
		
		SbmErrWarningLogDTO financialError = financialErrors.get(0);
		
		assertEquals("ElementInError", "FinancialInformation", financialError.getElementInError());
		assertEquals("Error Code", "ER-046", financialError.getErrorWarningTypeCd());
	}
	
	/*
	 * validateFinancialInfo
	 */
	@Test
	public void test_validateProratedAmounts_R019_invalid_zero_csr() {
		
		String qhpId = RandomStringUtils.randomAlphanumeric(16).toUpperCase();
		String stateCd = qhpId.substring(5, 7);

		List<String> bizRules = Arrays.asList("R019");
		SBMCache.getBusinessRules(stateCd).addAll(bizRules);
		
		expect(mockSbmDataService.getMetalLevelByQhpid(EasyMock.anyString(), EasyMock.anyString()))
		.andReturn(null).anyTimes();
		replay(mockSbmDataService);
		
		PolicyType policy = new PolicyType();
		policy.setPolicyStartDate(DateTimeUtil.getXMLGregorianCalendar(JAN_1));
		policy.setPolicyEndDate(DateTimeUtil.getXMLGregorianCalendar(DEC_31));
		policy.setQHPId(qhpId);
		
		FinancialInformation fit = new FinancialInformation();
		
		fit.setFinancialEffectiveStartDate(DateTimeUtil.getXMLGregorianCalendar(JAN_1));
		fit.setFinancialEffectiveEndDate(DateTimeUtil.getXMLGregorianCalendar(MAR_15));
		fit.setMonthlyTotalPremiumAmount(new BigDecimal("100"));
		fit.setMonthlyAPTCAmount(new BigDecimal("100"));
		fit.setMonthlyCSRAmount(new BigDecimal("20"));
		
		ProratedAmountType proratedAmt11 = new ProratedAmountType();
		proratedAmt11.setPartialMonthEffectiveStartDate(DateTimeUtil.getXMLGregorianCalendar(MAR_1));
		proratedAmt11.setPartialMonthEffectiveEndDate(DateTimeUtil.getXMLGregorianCalendar(MAR_15));
		proratedAmt11.setPartialMonthPremiumAmount(new BigDecimal("50"));
		proratedAmt11.setPartialMonthAPTCAmount(new BigDecimal("25"));
		proratedAmt11.setPartialMonthCSRAmount(new BigDecimal("10"));
		fit.getProratedAmount().add(proratedAmt11);
		
		policy.getFinancialInformation().add(fit);
		
		
		FinancialInformation fit2 = new FinancialInformation();
		
		fit2.setFinancialEffectiveStartDate(DateTimeUtil.getXMLGregorianCalendar(MAR_15.plusDays(1)));
		fit2.setFinancialEffectiveEndDate(DateTimeUtil.getXMLGregorianCalendar(DEC_31));
		fit2.setMonthlyTotalPremiumAmount(new BigDecimal("110"));
		fit2.setMonthlyAPTCAmount(new BigDecimal("60"));
		fit2.setMonthlyCSRAmount(new BigDecimal("30"));
		
		ProratedAmountType proratedAmt21 = new ProratedAmountType();
		proratedAmt21.setPartialMonthEffectiveStartDate(DateTimeUtil.getXMLGregorianCalendar(MAR_15.plusDays(1)));
		proratedAmt21.setPartialMonthEffectiveEndDate(DateTimeUtil.getXMLGregorianCalendar(MAR_31));
		proratedAmt21.setPartialMonthPremiumAmount(new BigDecimal("55"));
		proratedAmt21.setPartialMonthAPTCAmount(new BigDecimal("30"));
		proratedAmt21.setPartialMonthCSRAmount(new BigDecimal("0"));
		fit2.getProratedAmount().add(proratedAmt21);
		
		policy.getFinancialInformation().add(fit2);
		
		List<SbmErrWarningLogDTO> financialErrors = sbmFinancialValidator.validateFinancialInfo(policy);
		
		assertFalse("financialErrors", financialErrors.isEmpty());
		assertEquals("financialErrors", 1, financialErrors.size());
		
		SbmErrWarningLogDTO financialError = financialErrors.get(0);
		
		assertEquals("ElementInError", "FinancialInformation", financialError.getElementInError());
		assertEquals("Error Code", "ER-046", financialError.getErrorWarningTypeCd());
	}
	
	/*
	 * validateFinancialInfo
	 */
	@Test
	public void test_validateProratedAmounts_R019_invalid_null_PartialMonthAmountsNode() {

		String qhpId = RandomStringUtils.randomAlphanumeric(16).toUpperCase();
		String stateCd = qhpId.substring(5, 7);

		List<String> bizRules = Arrays.asList("R019");
		SBMCache.getBusinessRules(stateCd).addAll(bizRules);
		
		expect(mockSbmDataService.getMetalLevelByQhpid(EasyMock.anyString(), EasyMock.anyString()))
		.andReturn(null).anyTimes();
		replay(mockSbmDataService);
		
		PolicyType policy = new PolicyType();
		policy.setPolicyStartDate(DateTimeUtil.getXMLGregorianCalendar(JAN_1));
		policy.setPolicyEndDate(DateTimeUtil.getXMLGregorianCalendar(DEC_31));
		policy.setQHPId(qhpId);
		
		FinancialInformation fit = new FinancialInformation();
		
		fit.setFinancialEffectiveStartDate(DateTimeUtil.getXMLGregorianCalendar(JAN_1));
		fit.setFinancialEffectiveEndDate(DateTimeUtil.getXMLGregorianCalendar(MAR_15));
		fit.setMonthlyTotalPremiumAmount(new BigDecimal("100"));
		fit.setMonthlyAPTCAmount(new BigDecimal("100"));
		fit.setMonthlyCSRAmount(new BigDecimal("20"));
		
		ProratedAmountType proratedAmt11 = new ProratedAmountType();
		proratedAmt11.setPartialMonthEffectiveStartDate(DateTimeUtil.getXMLGregorianCalendar(MAR_1));
		proratedAmt11.setPartialMonthEffectiveEndDate(DateTimeUtil.getXMLGregorianCalendar(MAR_15));
		proratedAmt11.setPartialMonthPremiumAmount(new BigDecimal("50"));
		proratedAmt11.setPartialMonthAPTCAmount(new BigDecimal("25"));
		proratedAmt11.setPartialMonthCSRAmount(new BigDecimal("10"));
		fit.getProratedAmount().add(proratedAmt11);
		
		policy.getFinancialInformation().add(fit);
		
		
		FinancialInformation fit2 = new FinancialInformation();
		
		fit2.setFinancialEffectiveStartDate(DateTimeUtil.getXMLGregorianCalendar(MAR_15.plusDays(1)));
		fit2.setFinancialEffectiveEndDate(DateTimeUtil.getXMLGregorianCalendar(DEC_31));
		fit2.setMonthlyTotalPremiumAmount(new BigDecimal("110"));
		fit2.setMonthlyAPTCAmount(new BigDecimal("60"));
		fit2.setMonthlyCSRAmount(new BigDecimal("30"));
		
		policy.getFinancialInformation().add(fit2);
		
		List<SbmErrWarningLogDTO> financialErrors = sbmFinancialValidator.validateFinancialInfo(policy);
		
		assertFalse("financialErrors", financialErrors.isEmpty());
		assertEquals("financialErrors", 1, financialErrors.size());
		
		SbmErrWarningLogDTO financialError = financialErrors.get(0);
		
		assertEquals("ElementInError", "FinancialInformation", financialError.getElementInError());
		assertEquals("Error Code", "ER-046", financialError.getErrorWarningTypeCd());
	}
	
	/*
	 * validateFinancialInfo
	 */
	@Test
	public void test_validateProratedAmounts_R019_valid_amounts_provided() {
		
		String qhpId = RandomStringUtils.randomAlphanumeric(16).toUpperCase();
		String stateCd = qhpId.substring(5, 7);

		List<String> bizRules = Arrays.asList("R019");
		SBMCache.getBusinessRules(stateCd).addAll(bizRules);
		
		expect(mockSbmDataService.getMetalLevelByQhpid(EasyMock.anyString(), EasyMock.anyString()))
		.andReturn(null).anyTimes();
		replay(mockSbmDataService);
		
		PolicyType policy = new PolicyType();
		policy.setPolicyStartDate(DateTimeUtil.getXMLGregorianCalendar(JAN_1));
		policy.setPolicyEndDate(DateTimeUtil.getXMLGregorianCalendar(DEC_31));
		policy.setQHPId(qhpId);
		
		FinancialInformation fit = new FinancialInformation();
		
		fit.setFinancialEffectiveStartDate(DateTimeUtil.getXMLGregorianCalendar(JAN_1));
		fit.setFinancialEffectiveEndDate(DateTimeUtil.getXMLGregorianCalendar(MAR_15));
		fit.setMonthlyTotalPremiumAmount(new BigDecimal("100"));
		fit.setMonthlyAPTCAmount(new BigDecimal("100"));
		fit.setMonthlyCSRAmount(new BigDecimal("20"));
		
		ProratedAmountType proratedAmt11 = new ProratedAmountType();
		proratedAmt11.setPartialMonthEffectiveStartDate(DateTimeUtil.getXMLGregorianCalendar(MAR_1));
		proratedAmt11.setPartialMonthEffectiveEndDate(DateTimeUtil.getXMLGregorianCalendar(MAR_15));
		proratedAmt11.setPartialMonthPremiumAmount(new BigDecimal("50"));
		proratedAmt11.setPartialMonthAPTCAmount(new BigDecimal("25"));
		proratedAmt11.setPartialMonthCSRAmount(new BigDecimal("10"));
		fit.getProratedAmount().add(proratedAmt11);
		
		policy.getFinancialInformation().add(fit);
		
		
		FinancialInformation fit2 = new FinancialInformation();
		
		fit2.setFinancialEffectiveStartDate(DateTimeUtil.getXMLGregorianCalendar(MAR_15.plusDays(1)));
		fit2.setFinancialEffectiveEndDate(DateTimeUtil.getXMLGregorianCalendar(DEC_31));
		fit2.setMonthlyTotalPremiumAmount(new BigDecimal("110"));
		fit2.setMonthlyAPTCAmount(new BigDecimal("60"));
		fit2.setMonthlyCSRAmount(new BigDecimal("30"));
		
		ProratedAmountType proratedAmt21 = new ProratedAmountType();
		proratedAmt21.setPartialMonthEffectiveStartDate(DateTimeUtil.getXMLGregorianCalendar(MAR_15.plusDays(1)));
		proratedAmt21.setPartialMonthEffectiveEndDate(DateTimeUtil.getXMLGregorianCalendar(MAR_31));
		proratedAmt21.setPartialMonthPremiumAmount(new BigDecimal("55"));
		proratedAmt21.setPartialMonthAPTCAmount(new BigDecimal("30"));
		proratedAmt21.setPartialMonthCSRAmount(new BigDecimal("15"));
		fit2.getProratedAmount().add(proratedAmt21);
		
		policy.getFinancialInformation().add(fit2);
		
		List<SbmErrWarningLogDTO> financialErrors = sbmFinancialValidator.validateFinancialInfo(policy);
		
		assertTrue("financialErrors", financialErrors.isEmpty());
	}
	
	/*
	 * validateFinancialInfo
	 */
	@Test
	public void test_validateProratedAmounts_R019_valid_prev_amounts_null() {
		
		String qhpId = RandomStringUtils.randomAlphanumeric(16).toUpperCase();
		String stateCd = qhpId.substring(5, 7);

		List<String> bizRules = Arrays.asList("R019");
		SBMCache.getBusinessRules(stateCd).addAll(bizRules);
		
		expect(mockSbmDataService.getMetalLevelByQhpid(EasyMock.anyString(), EasyMock.anyString()))
		.andReturn(null).anyTimes();
		replay(mockSbmDataService);
		
		PolicyType policy = new PolicyType();
		policy.setPolicyStartDate(DateTimeUtil.getXMLGregorianCalendar(JAN_1));
		policy.setPolicyEndDate(DateTimeUtil.getXMLGregorianCalendar(DEC_31));
		policy.setQHPId(qhpId);
		
		FinancialInformation fit = new FinancialInformation();
		
		fit.setFinancialEffectiveStartDate(DateTimeUtil.getXMLGregorianCalendar(JAN_1));
		fit.setFinancialEffectiveEndDate(DateTimeUtil.getXMLGregorianCalendar(MAR_15));
		fit.setMonthlyTotalPremiumAmount(new BigDecimal("100"));
		fit.setMonthlyAPTCAmount(new BigDecimal("100"));
		fit.setMonthlyCSRAmount(new BigDecimal("20"));
		
		ProratedAmountType proratedAmt11 = new ProratedAmountType();
		proratedAmt11.setPartialMonthEffectiveStartDate(DateTimeUtil.getXMLGregorianCalendar(MAR_1));
		proratedAmt11.setPartialMonthEffectiveEndDate(DateTimeUtil.getXMLGregorianCalendar(MAR_15));
		//proratedAmt11.setPartialMonthPremiumAmount(new BigDecimal("50"));
		//proratedAmt11.setPartialMonthAPTCAmount(new BigDecimal("25"));
		//proratedAmt11.setPartialMonthCSRAmount(new BigDecimal("10"));
		fit.getProratedAmount().add(proratedAmt11);
		
		policy.getFinancialInformation().add(fit);
		
		
		FinancialInformation fit2 = new FinancialInformation();
		
		fit2.setFinancialEffectiveStartDate(DateTimeUtil.getXMLGregorianCalendar(MAR_15.plusDays(1)));
		fit2.setFinancialEffectiveEndDate(DateTimeUtil.getXMLGregorianCalendar(DEC_31));
		fit2.setMonthlyTotalPremiumAmount(new BigDecimal("110"));
		fit2.setMonthlyAPTCAmount(new BigDecimal("60"));
		fit2.setMonthlyCSRAmount(new BigDecimal("30"));
		
		ProratedAmountType proratedAmt21 = new ProratedAmountType();
		proratedAmt21.setPartialMonthEffectiveStartDate(DateTimeUtil.getXMLGregorianCalendar(MAR_15.plusDays(1)));
		proratedAmt21.setPartialMonthEffectiveEndDate(DateTimeUtil.getXMLGregorianCalendar(MAR_31));
		proratedAmt21.setPartialMonthPremiumAmount(new BigDecimal("55"));
		proratedAmt21.setPartialMonthAPTCAmount(new BigDecimal("30"));
		proratedAmt21.setPartialMonthCSRAmount(new BigDecimal("15"));
		fit2.getProratedAmount().add(proratedAmt21);
		
		policy.getFinancialInformation().add(fit2);
		
		List<SbmErrWarningLogDTO> financialErrors = sbmFinancialValidator.validateFinancialInfo(policy);
		
		assertTrue("financialErrors", financialErrors.isEmpty());
	}
	
	/*
	 * validateFinancialInfo
	 */
	@Test
	public void test_validateProratedAmounts_R019_valid_monthly_amounts_null() {
		
		String qhpId = RandomStringUtils.randomAlphanumeric(16).toUpperCase();
		String stateCd = qhpId.substring(5, 7);

		List<String> bizRules = Arrays.asList("R019");
		SBMCache.getBusinessRules(stateCd).addAll(bizRules);
		
		expect(mockSbmDataService.getMetalLevelByQhpid(EasyMock.anyString(), EasyMock.anyString()))
		.andReturn(null).anyTimes();
		replay(mockSbmDataService);
		
		PolicyType policy = new PolicyType();
		policy.setPolicyStartDate(DateTimeUtil.getXMLGregorianCalendar(JAN_1));
		policy.setPolicyEndDate(DateTimeUtil.getXMLGregorianCalendar(DEC_31));
		policy.setQHPId(qhpId);
		
		FinancialInformation fit = new FinancialInformation();
		
		fit.setFinancialEffectiveStartDate(DateTimeUtil.getXMLGregorianCalendar(JAN_1));
		fit.setFinancialEffectiveEndDate(DateTimeUtil.getXMLGregorianCalendar(MAR_15));
		fit.setMonthlyTotalPremiumAmount(new BigDecimal("100"));
		fit.setMonthlyAPTCAmount(new BigDecimal("100"));
		fit.setMonthlyCSRAmount(new BigDecimal("20"));
		
		ProratedAmountType proratedAmt11 = new ProratedAmountType();
		proratedAmt11.setPartialMonthEffectiveStartDate(DateTimeUtil.getXMLGregorianCalendar(MAR_1));
		proratedAmt11.setPartialMonthEffectiveEndDate(DateTimeUtil.getXMLGregorianCalendar(MAR_15));
		proratedAmt11.setPartialMonthPremiumAmount(new BigDecimal("50"));
		proratedAmt11.setPartialMonthAPTCAmount(new BigDecimal("25"));
		proratedAmt11.setPartialMonthCSRAmount(new BigDecimal("10"));
		fit.getProratedAmount().add(proratedAmt11);
		
		policy.getFinancialInformation().add(fit);
		
		
		FinancialInformation fit2 = new FinancialInformation();
		
		fit2.setFinancialEffectiveStartDate(DateTimeUtil.getXMLGregorianCalendar(MAR_15.plusDays(1)));
		fit2.setFinancialEffectiveEndDate(DateTimeUtil.getXMLGregorianCalendar(DEC_31));
		//fit2.setMonthlyTotalPremiumAmount(new BigDecimal("110"));
		//fit2.setMonthlyAPTCAmount(new BigDecimal("60"));
		//fit2.setMonthlyCSRAmount(new BigDecimal("30"));
		
		ProratedAmountType proratedAmt21 = new ProratedAmountType();
		proratedAmt21.setPartialMonthEffectiveStartDate(DateTimeUtil.getXMLGregorianCalendar(MAR_15.plusDays(1)));
		proratedAmt21.setPartialMonthEffectiveEndDate(DateTimeUtil.getXMLGregorianCalendar(MAR_31));
		proratedAmt21.setPartialMonthPremiumAmount(new BigDecimal("55"));
		proratedAmt21.setPartialMonthAPTCAmount(new BigDecimal("30"));
		proratedAmt21.setPartialMonthCSRAmount(new BigDecimal("15"));
		fit2.getProratedAmount().add(proratedAmt21);
		
		policy.getFinancialInformation().add(fit2);
		
		List<SbmErrWarningLogDTO> financialErrors = sbmFinancialValidator.validateFinancialInfo(policy);
		
		assertTrue("financialErrors", financialErrors.isEmpty());
	}
	
	/*
	 * validateFinancialInfo
	 */
	@Test
	public void test_validateProratedAmounts_R019_valid_monthly_amounts_zero() {
		
		String qhpId = RandomStringUtils.randomAlphanumeric(16).toUpperCase();
		String stateCd = qhpId.substring(5, 7);

		List<String> bizRules = Arrays.asList("R019");
		SBMCache.getBusinessRules(stateCd).addAll(bizRules);
		
		expect(mockSbmDataService.getMetalLevelByQhpid(EasyMock.anyString(), EasyMock.anyString()))
		.andReturn(null).anyTimes();
		replay(mockSbmDataService);
		
		PolicyType policy = new PolicyType();
		policy.setPolicyStartDate(DateTimeUtil.getXMLGregorianCalendar(JAN_1));
		policy.setPolicyEndDate(DateTimeUtil.getXMLGregorianCalendar(DEC_31));
		policy.setQHPId(qhpId);
		
		FinancialInformation fit = new FinancialInformation();
		
		fit.setFinancialEffectiveStartDate(DateTimeUtil.getXMLGregorianCalendar(JAN_1));
		fit.setFinancialEffectiveEndDate(DateTimeUtil.getXMLGregorianCalendar(MAR_15));
		fit.setMonthlyTotalPremiumAmount(new BigDecimal("100"));
		fit.setMonthlyAPTCAmount(new BigDecimal("100"));
		fit.setMonthlyCSRAmount(new BigDecimal("20"));
		
		ProratedAmountType proratedAmt11 = new ProratedAmountType();
		proratedAmt11.setPartialMonthEffectiveStartDate(DateTimeUtil.getXMLGregorianCalendar(MAR_1));
		proratedAmt11.setPartialMonthEffectiveEndDate(DateTimeUtil.getXMLGregorianCalendar(MAR_15));
		proratedAmt11.setPartialMonthPremiumAmount(new BigDecimal("50"));
		proratedAmt11.setPartialMonthAPTCAmount(new BigDecimal("25"));
		proratedAmt11.setPartialMonthCSRAmount(new BigDecimal("10"));
		fit.getProratedAmount().add(proratedAmt11);
		
		policy.getFinancialInformation().add(fit);
		
		
		FinancialInformation fit2 = new FinancialInformation();
		
		fit2.setFinancialEffectiveStartDate(DateTimeUtil.getXMLGregorianCalendar(MAR_15.plusDays(1)));
		fit2.setFinancialEffectiveEndDate(DateTimeUtil.getXMLGregorianCalendar(DEC_31));
		fit2.setMonthlyTotalPremiumAmount(new BigDecimal("0"));
		fit2.setMonthlyAPTCAmount(new BigDecimal("0"));
		fit2.setMonthlyCSRAmount(new BigDecimal("0"));
		
		ProratedAmountType proratedAmt21 = new ProratedAmountType();
		proratedAmt21.setPartialMonthEffectiveStartDate(DateTimeUtil.getXMLGregorianCalendar(MAR_15.plusDays(1)));
		proratedAmt21.setPartialMonthEffectiveEndDate(DateTimeUtil.getXMLGregorianCalendar(MAR_31));
		proratedAmt21.setPartialMonthPremiumAmount(new BigDecimal("55"));
		proratedAmt21.setPartialMonthAPTCAmount(new BigDecimal("30"));
		proratedAmt21.setPartialMonthCSRAmount(new BigDecimal("15"));
		fit2.getProratedAmount().add(proratedAmt21);
		
		policy.getFinancialInformation().add(fit2);
		
		List<SbmErrWarningLogDTO> financialErrors = sbmFinancialValidator.validateFinancialInfo(policy);
		
		assertTrue("financialErrors", financialErrors.isEmpty());
	}
	
	/*
	 * validateFinancialInfo
	 */
	@Test
	public void test_validateProratedAmounts_R019_valid_Prev_prorated_Null() {
		
		String qhpId = RandomStringUtils.randomAlphanumeric(16).toUpperCase();
		String stateCd = qhpId.substring(5, 7);

		List<String> bizRules = Arrays.asList("R019");
		SBMCache.getBusinessRules(stateCd).addAll(bizRules);
		
		expect(mockSbmDataService.getMetalLevelByQhpid(EasyMock.anyString(), EasyMock.anyString()))
		.andReturn(null).anyTimes();
		replay(mockSbmDataService);
		
		PolicyType policy = new PolicyType();
		policy.setPolicyStartDate(DateTimeUtil.getXMLGregorianCalendar(JAN_1));
		policy.setPolicyEndDate(DateTimeUtil.getXMLGregorianCalendar(DEC_31));
		policy.setQHPId(qhpId);
		
		FinancialInformation fit = new FinancialInformation();
		
		fit.setFinancialEffectiveStartDate(DateTimeUtil.getXMLGregorianCalendar(JAN_1));
		fit.setFinancialEffectiveEndDate(DateTimeUtil.getXMLGregorianCalendar(MAR_15));
		fit.setMonthlyTotalPremiumAmount(new BigDecimal("100"));
		fit.setMonthlyAPTCAmount(new BigDecimal("100"));
		fit.setMonthlyCSRAmount(new BigDecimal("20"));
		
		ProratedAmountType proratedAmt11 = new ProratedAmountType();
		proratedAmt11.setPartialMonthEffectiveStartDate(DateTimeUtil.getXMLGregorianCalendar(MAR_1));
		proratedAmt11.setPartialMonthEffectiveEndDate(DateTimeUtil.getXMLGregorianCalendar(MAR_15));
		proratedAmt11.setPartialMonthPremiumAmount(new BigDecimal("50"));
		proratedAmt11.setPartialMonthAPTCAmount(new BigDecimal("25"));
		//proratedAmt11.setPartialMonthCSRAmount(new BigDecimal("10"));
		fit.getProratedAmount().add(proratedAmt11);
		
		policy.getFinancialInformation().add(fit);
		
		
		FinancialInformation fit2 = new FinancialInformation();
		
		fit2.setFinancialEffectiveStartDate(DateTimeUtil.getXMLGregorianCalendar(MAR_15));
		fit2.setFinancialEffectiveEndDate(DateTimeUtil.getXMLGregorianCalendar(DEC_31));
		fit2.setMonthlyTotalPremiumAmount(new BigDecimal("110"));
		fit2.setMonthlyAPTCAmount(new BigDecimal("60"));
		fit2.setMonthlyCSRAmount(new BigDecimal("30"));
		
		ProratedAmountType proratedAmt21 = new ProratedAmountType();
		proratedAmt21.setPartialMonthEffectiveStartDate(DateTimeUtil.getXMLGregorianCalendar(MAR_15.plusDays(1)));
		proratedAmt21.setPartialMonthEffectiveEndDate(DateTimeUtil.getXMLGregorianCalendar(MAR_31));
		proratedAmt21.setPartialMonthPremiumAmount(new BigDecimal("55"));
		proratedAmt21.setPartialMonthAPTCAmount(new BigDecimal("30"));
		proratedAmt21.setPartialMonthCSRAmount(new BigDecimal("15"));
		fit2.getProratedAmount().add(proratedAmt21);
		
		policy.getFinancialInformation().add(fit2);
		
		List<SbmErrWarningLogDTO> financialErrors = sbmFinancialValidator.validateFinancialInfo(policy);
		
		assertTrue("financialErrors", financialErrors.isEmpty());
	}
	
	/*
	 * validateFinancialInfo
	 */
	@Test
	public void test_validateProratedAmounts_R019_valid_Prev_prorated_And_later_Null() {
		
		String qhpId = RandomStringUtils.randomAlphanumeric(16).toUpperCase();
		String stateCd = qhpId.substring(5, 7);

		List<String> bizRules = Arrays.asList("R019");
		SBMCache.getBusinessRules(stateCd).addAll(bizRules);
		
		expect(mockSbmDataService.getMetalLevelByQhpid(EasyMock.anyString(), EasyMock.anyString()))
		.andReturn(null).anyTimes();
		replay(mockSbmDataService);
		
		PolicyType policy = new PolicyType();
		policy.setPolicyStartDate(DateTimeUtil.getXMLGregorianCalendar(JAN_1));
		policy.setPolicyEndDate(DateTimeUtil.getXMLGregorianCalendar(DEC_31));
		policy.setQHPId(qhpId);
		
		FinancialInformation fit = new FinancialInformation();
		
		fit.setFinancialEffectiveStartDate(DateTimeUtil.getXMLGregorianCalendar(JAN_1));
		fit.setFinancialEffectiveEndDate(DateTimeUtil.getXMLGregorianCalendar(MAR_15));
		fit.setMonthlyTotalPremiumAmount(new BigDecimal("100"));
		fit.setMonthlyAPTCAmount(new BigDecimal("100"));
		fit.setMonthlyCSRAmount(new BigDecimal("20"));
		
		ProratedAmountType proratedAmt11 = new ProratedAmountType();
		proratedAmt11.setPartialMonthEffectiveStartDate(DateTimeUtil.getXMLGregorianCalendar(MAR_1));
		proratedAmt11.setPartialMonthEffectiveEndDate(DateTimeUtil.getXMLGregorianCalendar(MAR_15));
		proratedAmt11.setPartialMonthPremiumAmount(new BigDecimal("50"));
		proratedAmt11.setPartialMonthAPTCAmount(new BigDecimal("25"));
		//proratedAmt11.setPartialMonthCSRAmount(new BigDecimal("10"));
		fit.getProratedAmount().add(proratedAmt11);
		
		policy.getFinancialInformation().add(fit);
		
		
		FinancialInformation fit2 = new FinancialInformation();
		
		fit2.setFinancialEffectiveStartDate(DateTimeUtil.getXMLGregorianCalendar(MAR_15));
		fit2.setFinancialEffectiveEndDate(DateTimeUtil.getXMLGregorianCalendar(DEC_31));
		fit2.setMonthlyTotalPremiumAmount(new BigDecimal("110"));
		fit2.setMonthlyAPTCAmount(new BigDecimal("60"));
		fit2.setMonthlyCSRAmount(new BigDecimal("30"));
		
		ProratedAmountType proratedAmt21 = new ProratedAmountType();
		proratedAmt21.setPartialMonthEffectiveStartDate(DateTimeUtil.getXMLGregorianCalendar(MAR_15.plusDays(1)));
		proratedAmt21.setPartialMonthEffectiveEndDate(DateTimeUtil.getXMLGregorianCalendar(MAR_31));
		proratedAmt21.setPartialMonthPremiumAmount(new BigDecimal("55"));
		proratedAmt21.setPartialMonthAPTCAmount(new BigDecimal("30"));
		//proratedAmt21.setPartialMonthCSRAmount(new BigDecimal("15"));
		fit2.getProratedAmount().add(proratedAmt21);
		
		policy.getFinancialInformation().add(fit2);
		
		List<SbmErrWarningLogDTO> financialErrors = sbmFinancialValidator.validateFinancialInfo(policy);
		
		assertTrue("financialErrors", financialErrors.isEmpty());
	}
	
	/*
	 * validateFinancialInfo
	 */
	@Test
	public void test_validateProratedAmounts_R019_valid_finStart_LT_Prev_proratedEnd() {
		
		String qhpId = RandomStringUtils.randomAlphanumeric(16).toUpperCase();
		String stateCd = qhpId.substring(5, 7);

		List<String> bizRules = Arrays.asList("R019");
		SBMCache.getBusinessRules(stateCd).addAll(bizRules);
		
		expect(mockSbmDataService.getMetalLevelByQhpid(EasyMock.anyString(), EasyMock.anyString()))
		.andReturn(null).anyTimes();
		replay(mockSbmDataService);
		
		PolicyType policy = new PolicyType();
		policy.setPolicyStartDate(DateTimeUtil.getXMLGregorianCalendar(JAN_1));
		policy.setPolicyEndDate(DateTimeUtil.getXMLGregorianCalendar(DEC_31));
		policy.setQHPId(qhpId);
		
		FinancialInformation fit = new FinancialInformation();
		
		fit.setFinancialEffectiveStartDate(DateTimeUtil.getXMLGregorianCalendar(JAN_1));
		fit.setFinancialEffectiveEndDate(DateTimeUtil.getXMLGregorianCalendar(MAR_15));
		fit.setMonthlyTotalPremiumAmount(new BigDecimal("100"));
		fit.setMonthlyAPTCAmount(new BigDecimal("100"));
		fit.setMonthlyCSRAmount(new BigDecimal("20"));
		
		ProratedAmountType proratedAmt11 = new ProratedAmountType();
		proratedAmt11.setPartialMonthEffectiveStartDate(DateTimeUtil.getXMLGregorianCalendar(MAR_1));
		proratedAmt11.setPartialMonthEffectiveEndDate(DateTimeUtil.getXMLGregorianCalendar(MAR_15));
		proratedAmt11.setPartialMonthPremiumAmount(new BigDecimal("50"));
		proratedAmt11.setPartialMonthAPTCAmount(new BigDecimal("25"));
		proratedAmt11.setPartialMonthCSRAmount(new BigDecimal("10"));
		fit.getProratedAmount().add(proratedAmt11);
		
		policy.getFinancialInformation().add(fit);
		
		
		FinancialInformation fit2 = new FinancialInformation();
		
		fit2.setFinancialEffectiveStartDate(DateTimeUtil.getXMLGregorianCalendar(MAR_15));
		fit2.setFinancialEffectiveEndDate(DateTimeUtil.getXMLGregorianCalendar(DEC_31));
		fit2.setMonthlyTotalPremiumAmount(new BigDecimal("110"));
		fit2.setMonthlyAPTCAmount(new BigDecimal("60"));
		fit2.setMonthlyCSRAmount(new BigDecimal("30"));
		
		ProratedAmountType proratedAmt21 = new ProratedAmountType();
		proratedAmt21.setPartialMonthEffectiveStartDate(DateTimeUtil.getXMLGregorianCalendar(MAR_15.plusDays(1)));
		proratedAmt21.setPartialMonthEffectiveEndDate(DateTimeUtil.getXMLGregorianCalendar(MAR_31));
		proratedAmt21.setPartialMonthPremiumAmount(new BigDecimal("55"));
		proratedAmt21.setPartialMonthAPTCAmount(new BigDecimal("30"));
		//proratedAmt21.setPartialMonthCSRAmount(new BigDecimal("15"));
		fit2.getProratedAmount().add(proratedAmt21);
		
		policy.getFinancialInformation().add(fit2);
		
		List<SbmErrWarningLogDTO> financialErrors = sbmFinancialValidator.validateFinancialInfo(policy);
		
		assertTrue("financialErrors", financialErrors.isEmpty());
	}
	
	/*
	 * validateFinancialInfo
	 */
	@Test
	public void test_validateProratedAmounts_R019_valid_finStart_proratedEnd_Diff_Months() {
		
		String qhpId = RandomStringUtils.randomAlphanumeric(16).toUpperCase();
		String stateCd = qhpId.substring(5, 7);

		List<String> bizRules = Arrays.asList("R019");
		SBMCache.getBusinessRules(stateCd).addAll(bizRules);
		
		expect(mockSbmDataService.getMetalLevelByQhpid(EasyMock.anyString(), EasyMock.anyString()))
		.andReturn(null).anyTimes();
		replay(mockSbmDataService);
		
		PolicyType policy = new PolicyType();
		policy.setPolicyStartDate(DateTimeUtil.getXMLGregorianCalendar(JAN_1));
		policy.setPolicyEndDate(DateTimeUtil.getXMLGregorianCalendar(DEC_31));
		policy.setQHPId(qhpId);
		
		FinancialInformation fit = new FinancialInformation();
		
		fit.setFinancialEffectiveStartDate(DateTimeUtil.getXMLGregorianCalendar(JAN_1));
		fit.setFinancialEffectiveEndDate(DateTimeUtil.getXMLGregorianCalendar(MAR_15));
		fit.setMonthlyTotalPremiumAmount(new BigDecimal("100"));
		fit.setMonthlyAPTCAmount(new BigDecimal("100"));
		fit.setMonthlyCSRAmount(new BigDecimal("20"));
		
		ProratedAmountType proratedAmt11 = new ProratedAmountType();
		proratedAmt11.setPartialMonthEffectiveStartDate(DateTimeUtil.getXMLGregorianCalendar(MAR_1));
		proratedAmt11.setPartialMonthEffectiveEndDate(DateTimeUtil.getXMLGregorianCalendar(MAR_15));
		proratedAmt11.setPartialMonthPremiumAmount(new BigDecimal("50"));
		proratedAmt11.setPartialMonthAPTCAmount(new BigDecimal("25"));
		proratedAmt11.setPartialMonthCSRAmount(new BigDecimal("10"));
		fit.getProratedAmount().add(proratedAmt11);
		
		policy.getFinancialInformation().add(fit);
		
		
		FinancialInformation fit2 = new FinancialInformation();
		
		fit2.setFinancialEffectiveStartDate(DateTimeUtil.getXMLGregorianCalendar(APR_1));
		fit2.setFinancialEffectiveEndDate(DateTimeUtil.getXMLGregorianCalendar(DEC_31));
		fit2.setMonthlyTotalPremiumAmount(new BigDecimal("110"));
		fit2.setMonthlyAPTCAmount(new BigDecimal("60"));
		fit2.setMonthlyCSRAmount(new BigDecimal("30"));
		
		ProratedAmountType proratedAmt21 = new ProratedAmountType();
		proratedAmt21.setPartialMonthEffectiveStartDate(DateTimeUtil.getXMLGregorianCalendar(MAR_15.plusDays(1)));
		proratedAmt21.setPartialMonthEffectiveEndDate(DateTimeUtil.getXMLGregorianCalendar(MAR_31));
		proratedAmt21.setPartialMonthPremiumAmount(new BigDecimal("55"));
		proratedAmt21.setPartialMonthAPTCAmount(new BigDecimal("30"));
		//proratedAmt21.setPartialMonthCSRAmount(new BigDecimal("15"));
		fit2.getProratedAmount().add(proratedAmt21);
		
		policy.getFinancialInformation().add(fit2);
		
		List<SbmErrWarningLogDTO> financialErrors = sbmFinancialValidator.validateFinancialInfo(policy);
		
		assertTrue("financialErrors", financialErrors.isEmpty());
	}

	/*
	 * validateFinancialInfo
	 */
	@Test
	public void test_validateProratedAmounts_R019_valid_finStart_proratedEnd_Diff_Years() {
		
		String qhpId = RandomStringUtils.randomAlphanumeric(16).toUpperCase();
		String stateCd = qhpId.substring(5, 7);

		List<String> bizRules = Arrays.asList("R019");
		SBMCache.getBusinessRules(stateCd).addAll(bizRules);
		
		expect(mockSbmDataService.getMetalLevelByQhpid(EasyMock.anyString(), EasyMock.anyString()))
		.andReturn(null).anyTimes();
		replay(mockSbmDataService);
		
		PolicyType policy = new PolicyType();
		policy.setPolicyStartDate(DateTimeUtil.getXMLGregorianCalendar(JAN_1));
		policy.setPolicyEndDate(DateTimeUtil.getXMLGregorianCalendar(DEC_31));
		policy.setQHPId(qhpId);
		
		FinancialInformation fit = new FinancialInformation();
		
		fit.setFinancialEffectiveStartDate(DateTimeUtil.getXMLGregorianCalendar(JAN_1));
		fit.setFinancialEffectiveEndDate(DateTimeUtil.getXMLGregorianCalendar(MAR_15));
		fit.setMonthlyTotalPremiumAmount(new BigDecimal("100"));
		fit.setMonthlyAPTCAmount(new BigDecimal("100"));
		fit.setMonthlyCSRAmount(new BigDecimal("20"));
		
		ProratedAmountType proratedAmt11 = new ProratedAmountType();
		proratedAmt11.setPartialMonthEffectiveStartDate(DateTimeUtil.getXMLGregorianCalendar(MAR_1));
		proratedAmt11.setPartialMonthEffectiveEndDate(DateTimeUtil.getXMLGregorianCalendar(MAR_15));
		proratedAmt11.setPartialMonthPremiumAmount(new BigDecimal("50"));
		proratedAmt11.setPartialMonthAPTCAmount(new BigDecimal("25"));
		proratedAmt11.setPartialMonthCSRAmount(new BigDecimal("10"));
		fit.getProratedAmount().add(proratedAmt11);
		
		policy.getFinancialInformation().add(fit);
		
		
		FinancialInformation fit2 = new FinancialInformation();
		
		fit2.setFinancialEffectiveStartDate(DateTimeUtil.getXMLGregorianCalendar(MAR_15.plusDays(1).plusYears(1)));
		fit2.setFinancialEffectiveEndDate(DateTimeUtil.getXMLGregorianCalendar(DEC_31));
		fit2.setMonthlyTotalPremiumAmount(new BigDecimal("110"));
		fit2.setMonthlyAPTCAmount(new BigDecimal("60"));
		fit2.setMonthlyCSRAmount(new BigDecimal("30"));
		
		ProratedAmountType proratedAmt21 = new ProratedAmountType();
		proratedAmt21.setPartialMonthEffectiveStartDate(DateTimeUtil.getXMLGregorianCalendar(MAR_15.plusDays(1)));
		proratedAmt21.setPartialMonthEffectiveEndDate(DateTimeUtil.getXMLGregorianCalendar(MAR_31));
		proratedAmt21.setPartialMonthPremiumAmount(new BigDecimal("55"));
		proratedAmt21.setPartialMonthAPTCAmount(new BigDecimal("30"));
		//proratedAmt21.setPartialMonthCSRAmount(new BigDecimal("15"));
		fit2.getProratedAmount().add(proratedAmt21);
		
		policy.getFinancialInformation().add(fit2);
		
		List<SbmErrWarningLogDTO> financialErrors = sbmFinancialValidator.validateFinancialInfo(policy);
		
		assertTrue("financialErrors", financialErrors.isEmpty());
	}

	/*
	 * validateFinancialInfo
	 */
	@Test
	public void test_validateProratedAmounts_R019_valid_No_ProratedAmounts() {
		
		String qhpId = RandomStringUtils.randomAlphanumeric(16).toUpperCase();
		String stateCd = qhpId.substring(5, 7);

		List<String> bizRules = Arrays.asList("R019");
		SBMCache.getBusinessRules(stateCd).addAll(bizRules);
		
		expect(mockSbmDataService.getMetalLevelByQhpid(EasyMock.anyString(), EasyMock.anyString()))
		.andReturn(null).anyTimes();
		replay(mockSbmDataService);
		
		PolicyType policy = new PolicyType();
		policy.setPolicyStartDate(DateTimeUtil.getXMLGregorianCalendar(JAN_1));
		policy.setPolicyEndDate(DateTimeUtil.getXMLGregorianCalendar(DEC_31));
		policy.setQHPId(qhpId);
		
		FinancialInformation fit = new FinancialInformation();
		
		fit.setFinancialEffectiveStartDate(DateTimeUtil.getXMLGregorianCalendar(JAN_1));
		fit.setFinancialEffectiveEndDate(DateTimeUtil.getXMLGregorianCalendar(MAR_15));
		fit.setMonthlyTotalPremiumAmount(new BigDecimal("100"));
		fit.setMonthlyAPTCAmount(new BigDecimal("100"));
		fit.setMonthlyCSRAmount(new BigDecimal("20"));
		
		policy.getFinancialInformation().add(fit);
		
		
		FinancialInformation fit2 = new FinancialInformation();
		
		fit2.setFinancialEffectiveStartDate(DateTimeUtil.getXMLGregorianCalendar(MAR_15.plusDays(1).plusYears(1)));
		fit2.setFinancialEffectiveEndDate(DateTimeUtil.getXMLGregorianCalendar(DEC_31));
		fit2.setMonthlyTotalPremiumAmount(new BigDecimal("110"));
		fit2.setMonthlyAPTCAmount(new BigDecimal("60"));
		fit2.setMonthlyCSRAmount(new BigDecimal("30"));
		
		ProratedAmountType proratedAmt21 = new ProratedAmountType();
		proratedAmt21.setPartialMonthEffectiveStartDate(DateTimeUtil.getXMLGregorianCalendar(MAR_15.plusDays(1)));
		proratedAmt21.setPartialMonthEffectiveEndDate(DateTimeUtil.getXMLGregorianCalendar(MAR_31));
		proratedAmt21.setPartialMonthPremiumAmount(new BigDecimal("55"));
		proratedAmt21.setPartialMonthAPTCAmount(new BigDecimal("30"));
		//proratedAmt21.setPartialMonthCSRAmount(new BigDecimal("15"));
		fit2.getProratedAmount().add(proratedAmt21);
		
		policy.getFinancialInformation().add(fit2);
		
		List<SbmErrWarningLogDTO> financialErrors = sbmFinancialValidator.validateFinancialInfo(policy);
		
		assertTrue("financialErrors", financialErrors.isEmpty());
	}
	
	/*
	 * validateFinancialInfo
	 */
	@Test
	public void test_validateProratedAmounts_R020_invalid_null_Premium() {
		
		String qhpId = RandomStringUtils.randomAlphanumeric(16).toUpperCase();
		String stateCd = qhpId.substring(5, 7);

		List<String> bizRules = Arrays.asList("R020");
		SBMCache.getBusinessRules(stateCd).addAll(bizRules);
		
		expect(mockSbmDataService.getMetalLevelByQhpid(EasyMock.anyString(), EasyMock.anyString()))
		.andReturn(null).anyTimes();
		replay(mockSbmDataService);
		
		PolicyType policy = new PolicyType();
		policy.setPolicyStartDate(DateTimeUtil.getXMLGregorianCalendar(JAN_1));
		policy.setPolicyEndDate(DateTimeUtil.getXMLGregorianCalendar(DEC_31));
		policy.setQHPId(qhpId);
		
		FinancialInformation fit = new FinancialInformation();
		
		fit.setFinancialEffectiveStartDate(DateTimeUtil.getXMLGregorianCalendar(JAN_1));
		fit.setFinancialEffectiveEndDate(DateTimeUtil.getXMLGregorianCalendar(JUL_15));
		fit.setMonthlyTotalPremiumAmount(new BigDecimal("200"));
		fit.setMonthlyAPTCAmount(new BigDecimal("100"));
		fit.setMonthlyCSRAmount(new BigDecimal("20"));
		
		ProratedAmountType proratedAmt11 = new ProratedAmountType();
		proratedAmt11.setPartialMonthEffectiveStartDate(DateTimeUtil.getXMLGregorianCalendar(JUL_1));
		proratedAmt11.setPartialMonthEffectiveEndDate(DateTimeUtil.getXMLGregorianCalendar(JUL_15));
		//proratedAmt11.setPartialMonthPremiumAmount(new BigDecimal("50"));
		proratedAmt11.setPartialMonthAPTCAmount(new BigDecimal("25"));
		proratedAmt11.setPartialMonthCSRAmount(new BigDecimal("10"));
		fit.getProratedAmount().add(proratedAmt11);
		
		policy.getFinancialInformation().add(fit);
		
		
		FinancialInformation fit2 = new FinancialInformation();
		
		fit2.setFinancialEffectiveStartDate(DateTimeUtil.getXMLGregorianCalendar(JUL_15.plusDays(1)));
		fit2.setFinancialEffectiveEndDate(DateTimeUtil.getXMLGregorianCalendar(DEC_31));
		fit2.setMonthlyTotalPremiumAmount(new BigDecimal("100"));
		fit2.setMonthlyAPTCAmount(new BigDecimal("50"));
		fit2.setMonthlyCSRAmount(new BigDecimal("30"));
		
		ProratedAmountType proratedAmt21 = new ProratedAmountType();
		proratedAmt21.setPartialMonthEffectiveStartDate(DateTimeUtil.getXMLGregorianCalendar(JUL_15.plusDays(1)));
		proratedAmt21.setPartialMonthEffectiveEndDate(DateTimeUtil.getXMLGregorianCalendar(JUL_31));
		proratedAmt21.setPartialMonthPremiumAmount(new BigDecimal("50"));
		proratedAmt21.setPartialMonthAPTCAmount(new BigDecimal("25"));
		proratedAmt21.setPartialMonthCSRAmount(new BigDecimal("15"));
		fit2.getProratedAmount().add(proratedAmt21);
		
		policy.getFinancialInformation().add(fit2);
		
		List<SbmErrWarningLogDTO> financialErrors = sbmFinancialValidator.validateFinancialInfo(policy);
		
		assertFalse("financialErrors", financialErrors.isEmpty());
		assertEquals("financialErrors", 1, financialErrors.size());
		
		SbmErrWarningLogDTO financialError = financialErrors.get(0);
		
		assertEquals("ElementInError", "FinancialInformation", financialError.getElementInError());
		assertEquals("Error Code", "ER-046", financialError.getErrorWarningTypeCd());
		
	}
	
	/*
	 * validateFinancialInfo
	 */
	@Test
	public void test_validateProratedAmounts_R020_invalid_zero_Premium() {
		
		String qhpId = RandomStringUtils.randomAlphanumeric(16).toUpperCase();
		String stateCd = qhpId.substring(5, 7);

		List<String> bizRules = Arrays.asList("R020");
		SBMCache.getBusinessRules(stateCd).addAll(bizRules);
		
		expect(mockSbmDataService.getMetalLevelByQhpid(EasyMock.anyString(), EasyMock.anyString()))
		.andReturn(null).anyTimes();
		replay(mockSbmDataService);
		
		PolicyType policy = new PolicyType();
		policy.setPolicyStartDate(DateTimeUtil.getXMLGregorianCalendar(JAN_1));
		policy.setPolicyEndDate(DateTimeUtil.getXMLGregorianCalendar(DEC_31));
		policy.setQHPId(qhpId);
		
		FinancialInformation fit = new FinancialInformation();
		
		fit.setFinancialEffectiveStartDate(DateTimeUtil.getXMLGregorianCalendar(JAN_1));
		fit.setFinancialEffectiveEndDate(DateTimeUtil.getXMLGregorianCalendar(JUL_15));
		fit.setMonthlyTotalPremiumAmount(new BigDecimal("200"));
		fit.setMonthlyAPTCAmount(new BigDecimal("100"));
		fit.setMonthlyCSRAmount(new BigDecimal("20"));
		
		ProratedAmountType proratedAmt11 = new ProratedAmountType();
		proratedAmt11.setPartialMonthEffectiveStartDate(DateTimeUtil.getXMLGregorianCalendar(JUL_1));
		proratedAmt11.setPartialMonthEffectiveEndDate(DateTimeUtil.getXMLGregorianCalendar(JUL_15));
		proratedAmt11.setPartialMonthPremiumAmount(new BigDecimal("0"));
		proratedAmt11.setPartialMonthAPTCAmount(new BigDecimal("50"));
		proratedAmt11.setPartialMonthCSRAmount(new BigDecimal("10"));
		fit.getProratedAmount().add(proratedAmt11);
		
		policy.getFinancialInformation().add(fit);
		
		
		FinancialInformation fit2 = new FinancialInformation();
		
		fit2.setFinancialEffectiveStartDate(DateTimeUtil.getXMLGregorianCalendar(JUL_15.plusDays(1)));
		fit2.setFinancialEffectiveEndDate(DateTimeUtil.getXMLGregorianCalendar(DEC_31));
		fit2.setMonthlyTotalPremiumAmount(new BigDecimal("100"));
		fit2.setMonthlyAPTCAmount(new BigDecimal("50"));
		fit2.setMonthlyCSRAmount(new BigDecimal("30"));
		
		ProratedAmountType proratedAmt21 = new ProratedAmountType();
		proratedAmt21.setPartialMonthEffectiveStartDate(DateTimeUtil.getXMLGregorianCalendar(JUL_15.plusDays(1)));
		proratedAmt21.setPartialMonthEffectiveEndDate(DateTimeUtil.getXMLGregorianCalendar(JUL_31));
		proratedAmt21.setPartialMonthPremiumAmount(new BigDecimal("50"));
		proratedAmt21.setPartialMonthAPTCAmount(new BigDecimal("25"));
		proratedAmt21.setPartialMonthCSRAmount(new BigDecimal("15"));
		fit2.getProratedAmount().add(proratedAmt21);
		
		policy.getFinancialInformation().add(fit2);
		
		List<SbmErrWarningLogDTO> financialErrors = sbmFinancialValidator.validateFinancialInfo(policy);
		
		assertFalse("financialErrors", financialErrors.isEmpty());
		assertEquals("financialErrors", 1, financialErrors.size());
		
		SbmErrWarningLogDTO financialError = financialErrors.get(0);
		
		assertEquals("ElementInError", "FinancialInformation", financialError.getElementInError());
		assertEquals("Error Code", "ER-046", financialError.getErrorWarningTypeCd());
	}
	
	/*
	 * validateFinancialInfo
	 */
	@Test
	public void test_validateProratedAmounts_R020_invalid_null_aptc() {
		
		String qhpId = RandomStringUtils.randomAlphanumeric(16).toUpperCase();
		String stateCd = qhpId.substring(5, 7);

		List<String> bizRules = Arrays.asList("R020");
		SBMCache.getBusinessRules(stateCd).addAll(bizRules);
		
		expect(mockSbmDataService.getMetalLevelByQhpid(EasyMock.anyString(), EasyMock.anyString()))
		.andReturn(null).anyTimes();
		replay(mockSbmDataService);
		
		PolicyType policy = new PolicyType();
		policy.setPolicyStartDate(DateTimeUtil.getXMLGregorianCalendar(JAN_1));
		policy.setPolicyEndDate(DateTimeUtil.getXMLGregorianCalendar(DEC_31));
		policy.setQHPId(qhpId);
		
		FinancialInformation fit = new FinancialInformation();
		
		fit.setFinancialEffectiveStartDate(DateTimeUtil.getXMLGregorianCalendar(JAN_1));
		fit.setFinancialEffectiveEndDate(DateTimeUtil.getXMLGregorianCalendar(JUL_15));
		fit.setMonthlyTotalPremiumAmount(new BigDecimal("200"));
		fit.setMonthlyAPTCAmount(new BigDecimal("100"));
		fit.setMonthlyCSRAmount(new BigDecimal("20"));
		
		ProratedAmountType proratedAmt11 = new ProratedAmountType();
		proratedAmt11.setPartialMonthEffectiveStartDate(DateTimeUtil.getXMLGregorianCalendar(JUL_1));
		proratedAmt11.setPartialMonthEffectiveEndDate(DateTimeUtil.getXMLGregorianCalendar(JUL_15));
		proratedAmt11.setPartialMonthPremiumAmount(new BigDecimal("100"));
		//proratedAmt11.setPartialMonthAPTCAmount(new BigDecimal("50"));
		proratedAmt11.setPartialMonthCSRAmount(new BigDecimal("10"));
		fit.getProratedAmount().add(proratedAmt11);
		
		policy.getFinancialInformation().add(fit);
		
		
		FinancialInformation fit2 = new FinancialInformation();
		
		fit2.setFinancialEffectiveStartDate(DateTimeUtil.getXMLGregorianCalendar(JUL_15.plusDays(1)));
		fit2.setFinancialEffectiveEndDate(DateTimeUtil.getXMLGregorianCalendar(DEC_31));
		fit2.setMonthlyTotalPremiumAmount(new BigDecimal("100"));
		fit2.setMonthlyAPTCAmount(new BigDecimal("50"));
		fit2.setMonthlyCSRAmount(new BigDecimal("30"));
		
		ProratedAmountType proratedAmt21 = new ProratedAmountType();
		proratedAmt21.setPartialMonthEffectiveStartDate(DateTimeUtil.getXMLGregorianCalendar(JUL_15.plusDays(1)));
		proratedAmt21.setPartialMonthEffectiveEndDate(DateTimeUtil.getXMLGregorianCalendar(JUL_31));
		proratedAmt21.setPartialMonthPremiumAmount(new BigDecimal("50"));
		proratedAmt21.setPartialMonthAPTCAmount(new BigDecimal("25"));
		proratedAmt21.setPartialMonthCSRAmount(new BigDecimal("15"));
		fit2.getProratedAmount().add(proratedAmt21);
		
		policy.getFinancialInformation().add(fit2);
		
		List<SbmErrWarningLogDTO> financialErrors = sbmFinancialValidator.validateFinancialInfo(policy);
		
		assertFalse("financialErrors", financialErrors.isEmpty());
		assertEquals("financialErrors", 1, financialErrors.size());
		
		SbmErrWarningLogDTO financialError = financialErrors.get(0);
		
		assertEquals("ElementInError", "FinancialInformation", financialError.getElementInError());
		assertEquals("Error Code", "ER-046", financialError.getErrorWarningTypeCd());
	}
	
	/*
	 * validateFinancialInfo
	 */
	@Test
	public void test_validateProratedAmounts_R020_invalid_zero_aptc() {
		
		String qhpId = RandomStringUtils.randomAlphanumeric(16).toUpperCase();
		String stateCd = qhpId.substring(5, 7);

		List<String> bizRules = Arrays.asList("R020");
		SBMCache.getBusinessRules(stateCd).addAll(bizRules);
		
		expect(mockSbmDataService.getMetalLevelByQhpid(EasyMock.anyString(), EasyMock.anyString()))
		.andReturn(null).anyTimes();
		replay(mockSbmDataService);
		
		PolicyType policy = new PolicyType();
		policy.setPolicyStartDate(DateTimeUtil.getXMLGregorianCalendar(JAN_1));
		policy.setPolicyEndDate(DateTimeUtil.getXMLGregorianCalendar(DEC_31));
		policy.setQHPId(qhpId);
		
		FinancialInformation fit = new FinancialInformation();
		
		fit.setFinancialEffectiveStartDate(DateTimeUtil.getXMLGregorianCalendar(JAN_1));
		fit.setFinancialEffectiveEndDate(DateTimeUtil.getXMLGregorianCalendar(JUL_15));
		fit.setMonthlyTotalPremiumAmount(new BigDecimal("200"));
		fit.setMonthlyAPTCAmount(new BigDecimal("100"));
		fit.setMonthlyCSRAmount(new BigDecimal("20"));
		
		ProratedAmountType proratedAmt11 = new ProratedAmountType();
		proratedAmt11.setPartialMonthEffectiveStartDate(DateTimeUtil.getXMLGregorianCalendar(JUL_1));
		proratedAmt11.setPartialMonthEffectiveEndDate(DateTimeUtil.getXMLGregorianCalendar(JUL_15));
		proratedAmt11.setPartialMonthPremiumAmount(new BigDecimal("100"));
		proratedAmt11.setPartialMonthAPTCAmount(new BigDecimal("00"));
		proratedAmt11.setPartialMonthCSRAmount(new BigDecimal("10"));
		fit.getProratedAmount().add(proratedAmt11);
		
		policy.getFinancialInformation().add(fit);
		
		
		FinancialInformation fit2 = new FinancialInformation();
		
		fit2.setFinancialEffectiveStartDate(DateTimeUtil.getXMLGregorianCalendar(JUL_15.plusDays(1)));
		fit2.setFinancialEffectiveEndDate(DateTimeUtil.getXMLGregorianCalendar(DEC_31));
		fit2.setMonthlyTotalPremiumAmount(new BigDecimal("100"));
		fit2.setMonthlyAPTCAmount(new BigDecimal("50"));
		fit2.setMonthlyCSRAmount(new BigDecimal("30"));
		
		ProratedAmountType proratedAmt21 = new ProratedAmountType();
		proratedAmt21.setPartialMonthEffectiveStartDate(DateTimeUtil.getXMLGregorianCalendar(JUL_15.plusDays(1)));
		proratedAmt21.setPartialMonthEffectiveEndDate(DateTimeUtil.getXMLGregorianCalendar(JUL_31));
		proratedAmt21.setPartialMonthPremiumAmount(new BigDecimal("50"));
		proratedAmt21.setPartialMonthAPTCAmount(new BigDecimal("25"));
		proratedAmt21.setPartialMonthCSRAmount(new BigDecimal("15"));
		fit2.getProratedAmount().add(proratedAmt21);
		
		policy.getFinancialInformation().add(fit2);
		
		List<SbmErrWarningLogDTO> financialErrors = sbmFinancialValidator.validateFinancialInfo(policy);
		
		assertFalse("financialErrors", financialErrors.isEmpty());
		assertEquals("financialErrors", 1, financialErrors.size());
		
		SbmErrWarningLogDTO financialError = financialErrors.get(0);
		
		assertEquals("ElementInError", "FinancialInformation", financialError.getElementInError());
		assertEquals("Error Code", "ER-046", financialError.getErrorWarningTypeCd());
	}
	
	/*
	 * validateFinancialInfo
	 */
	@Test
	public void test_validateProratedAmounts_R020_invalid_null_csr() {
		
		String qhpId = RandomStringUtils.randomAlphanumeric(16).toUpperCase();
		String stateCd = qhpId.substring(5, 7);

		List<String> bizRules = Arrays.asList("R020");
		SBMCache.getBusinessRules(stateCd).addAll(bizRules);
		
		expect(mockSbmDataService.getMetalLevelByQhpid(EasyMock.anyString(), EasyMock.anyString()))
		.andReturn(null).anyTimes();
		replay(mockSbmDataService);
		
		PolicyType policy = new PolicyType();
		policy.setPolicyStartDate(DateTimeUtil.getXMLGregorianCalendar(JAN_1));
		policy.setPolicyEndDate(DateTimeUtil.getXMLGregorianCalendar(DEC_31));
		policy.setQHPId(qhpId);
		
		FinancialInformation fit = new FinancialInformation();
		
		fit.setFinancialEffectiveStartDate(DateTimeUtil.getXMLGregorianCalendar(JAN_1));
		fit.setFinancialEffectiveEndDate(DateTimeUtil.getXMLGregorianCalendar(JUL_15));
		fit.setMonthlyTotalPremiumAmount(new BigDecimal("200"));
		fit.setMonthlyAPTCAmount(new BigDecimal("100"));
		fit.setMonthlyCSRAmount(new BigDecimal("20"));
		
		ProratedAmountType proratedAmt11 = new ProratedAmountType();
		proratedAmt11.setPartialMonthEffectiveStartDate(DateTimeUtil.getXMLGregorianCalendar(JUL_1));
		proratedAmt11.setPartialMonthEffectiveEndDate(DateTimeUtil.getXMLGregorianCalendar(JUL_15));
		proratedAmt11.setPartialMonthPremiumAmount(new BigDecimal("100"));
		proratedAmt11.setPartialMonthAPTCAmount(new BigDecimal("50"));
		//proratedAmt11.setPartialMonthCSRAmount(new BigDecimal("10"));
		fit.getProratedAmount().add(proratedAmt11);
		
		policy.getFinancialInformation().add(fit);
		
		
		FinancialInformation fit2 = new FinancialInformation();
		
		fit2.setFinancialEffectiveStartDate(DateTimeUtil.getXMLGregorianCalendar(JUL_15.plusDays(1)));
		fit2.setFinancialEffectiveEndDate(DateTimeUtil.getXMLGregorianCalendar(DEC_31));
		fit2.setMonthlyTotalPremiumAmount(new BigDecimal("100"));
		fit2.setMonthlyAPTCAmount(new BigDecimal("50"));
		fit2.setMonthlyCSRAmount(new BigDecimal("30"));
		
		ProratedAmountType proratedAmt21 = new ProratedAmountType();
		proratedAmt21.setPartialMonthEffectiveStartDate(DateTimeUtil.getXMLGregorianCalendar(JUL_15.plusDays(1)));
		proratedAmt21.setPartialMonthEffectiveEndDate(DateTimeUtil.getXMLGregorianCalendar(JUL_31));
		proratedAmt21.setPartialMonthPremiumAmount(new BigDecimal("50"));
		proratedAmt21.setPartialMonthAPTCAmount(new BigDecimal("25"));
		proratedAmt21.setPartialMonthCSRAmount(new BigDecimal("15"));
		fit2.getProratedAmount().add(proratedAmt21);
		
		policy.getFinancialInformation().add(fit2);
		
		List<SbmErrWarningLogDTO> financialErrors = sbmFinancialValidator.validateFinancialInfo(policy);
		
		assertFalse("financialErrors", financialErrors.isEmpty());
		assertEquals("financialErrors", 1, financialErrors.size());
		
		SbmErrWarningLogDTO financialError = financialErrors.get(0);
		
		assertEquals("ElementInError", "FinancialInformation", financialError.getElementInError());
		assertEquals("Error Code", "ER-046", financialError.getErrorWarningTypeCd());
	}
	
	/*
	 * validateFinancialInfo
	 */
	@Test
	public void test_validateProratedAmounts_R020_invalid_zero_csr() {
		
		String qhpId = RandomStringUtils.randomAlphanumeric(16).toUpperCase();
		String stateCd = qhpId.substring(5, 7);

		List<String> bizRules = Arrays.asList("R020");
		SBMCache.getBusinessRules(stateCd).addAll(bizRules);
		
		expect(mockSbmDataService.getMetalLevelByQhpid(EasyMock.anyString(), EasyMock.anyString()))
		.andReturn(null).anyTimes();
		replay(mockSbmDataService);
		
		PolicyType policy = new PolicyType();
		policy.setPolicyStartDate(DateTimeUtil.getXMLGregorianCalendar(JAN_1));
		policy.setPolicyEndDate(DateTimeUtil.getXMLGregorianCalendar(DEC_31));
		policy.setQHPId(qhpId);
		
		FinancialInformation fit = new FinancialInformation();
		
		fit.setFinancialEffectiveStartDate(DateTimeUtil.getXMLGregorianCalendar(JAN_1));
		fit.setFinancialEffectiveEndDate(DateTimeUtil.getXMLGregorianCalendar(JUL_15));
		fit.setMonthlyTotalPremiumAmount(new BigDecimal("200"));
		fit.setMonthlyAPTCAmount(new BigDecimal("100"));
		fit.setMonthlyCSRAmount(new BigDecimal("20"));
		
		ProratedAmountType proratedAmt11 = new ProratedAmountType();
		proratedAmt11.setPartialMonthEffectiveStartDate(DateTimeUtil.getXMLGregorianCalendar(JUL_1));
		proratedAmt11.setPartialMonthEffectiveEndDate(DateTimeUtil.getXMLGregorianCalendar(JUL_15));
		proratedAmt11.setPartialMonthPremiumAmount(new BigDecimal("100"));
		proratedAmt11.setPartialMonthAPTCAmount(new BigDecimal("50"));
		proratedAmt11.setPartialMonthCSRAmount(new BigDecimal("00"));
		fit.getProratedAmount().add(proratedAmt11);
		
		policy.getFinancialInformation().add(fit);
		
		
		FinancialInformation fit2 = new FinancialInformation();
		
		fit2.setFinancialEffectiveStartDate(DateTimeUtil.getXMLGregorianCalendar(JUL_15.plusDays(1)));
		fit2.setFinancialEffectiveEndDate(DateTimeUtil.getXMLGregorianCalendar(DEC_31));
		fit2.setMonthlyTotalPremiumAmount(new BigDecimal("100"));
		fit2.setMonthlyAPTCAmount(new BigDecimal("50"));
		fit2.setMonthlyCSRAmount(new BigDecimal("30"));
		
		ProratedAmountType proratedAmt21 = new ProratedAmountType();
		proratedAmt21.setPartialMonthEffectiveStartDate(DateTimeUtil.getXMLGregorianCalendar(JUL_15.plusDays(1)));
		proratedAmt21.setPartialMonthEffectiveEndDate(DateTimeUtil.getXMLGregorianCalendar(JUL_31));
		proratedAmt21.setPartialMonthPremiumAmount(new BigDecimal("50"));
		proratedAmt21.setPartialMonthAPTCAmount(new BigDecimal("25"));
		proratedAmt21.setPartialMonthCSRAmount(new BigDecimal("15"));
		fit2.getProratedAmount().add(proratedAmt21);
		
		policy.getFinancialInformation().add(fit2);
		
		List<SbmErrWarningLogDTO> financialErrors = sbmFinancialValidator.validateFinancialInfo(policy);
		
		assertFalse("financialErrors", financialErrors.isEmpty());
		assertEquals("financialErrors", 1, financialErrors.size());
		
		SbmErrWarningLogDTO financialError = financialErrors.get(0);
		
		assertEquals("ElementInError", "FinancialInformation", financialError.getElementInError());
		assertEquals("Error Code", "ER-046", financialError.getErrorWarningTypeCd());
	}
	
	/*
	 * validateFinancialInfo
	 */
	@Test
	public void test_validateProratedAmounts_R020_invalid_null_PartialMonthAmountsNode() {
		
		String qhpId = RandomStringUtils.randomAlphanumeric(16).toUpperCase();
		String stateCd = qhpId.substring(5, 7);

		List<String> bizRules = Arrays.asList("R020");
		SBMCache.getBusinessRules(stateCd).addAll(bizRules);
		
		expect(mockSbmDataService.getMetalLevelByQhpid(EasyMock.anyString(), EasyMock.anyString()))
		.andReturn(null).anyTimes();
		replay(mockSbmDataService);
		
		PolicyType policy = new PolicyType();
		policy.setPolicyStartDate(DateTimeUtil.getXMLGregorianCalendar(JAN_1));
		policy.setPolicyEndDate(DateTimeUtil.getXMLGregorianCalendar(DEC_31));
		policy.setQHPId(qhpId);
		
		FinancialInformation fit = new FinancialInformation();
		
		fit.setFinancialEffectiveStartDate(DateTimeUtil.getXMLGregorianCalendar(JAN_1));
		fit.setFinancialEffectiveEndDate(DateTimeUtil.getXMLGregorianCalendar(JUL_15));
		fit.setMonthlyTotalPremiumAmount(new BigDecimal("200"));
		fit.setMonthlyAPTCAmount(new BigDecimal("100"));
		fit.setMonthlyCSRAmount(new BigDecimal("20"));
		
/*		ProratedAmountType proratedAmt11 = new ProratedAmountType();
		proratedAmt11.setPartialMonthEffectiveStartDate(DateTimeUtil.getXMLGregorianCalendar(MAR_1));
		proratedAmt11.setPartialMonthEffectiveEndDate(DateTimeUtil.getXMLGregorianCalendar(MAR_15));
		proratedAmt11.setPartialMonthPremiumAmount(new BigDecimal("100"));
		proratedAmt11.setPartialMonthAPTCAmount(new BigDecimal("50"));
		proratedAmt11.setPartialMonthCSRAmount(new BigDecimal("10"));*/
		//fit.getProratedAmount().add(proratedAmt11);
		
		policy.getFinancialInformation().add(fit);
		
		
		FinancialInformation fit2 = new FinancialInformation();
		
		fit2.setFinancialEffectiveStartDate(DateTimeUtil.getXMLGregorianCalendar(JUL_15.plusDays(1)));
		fit2.setFinancialEffectiveEndDate(DateTimeUtil.getXMLGregorianCalendar(DEC_31));
		fit2.setMonthlyTotalPremiumAmount(new BigDecimal("100"));
		fit2.setMonthlyAPTCAmount(new BigDecimal("50"));
		fit2.setMonthlyCSRAmount(new BigDecimal("30"));
		
		ProratedAmountType proratedAmt21 = new ProratedAmountType();
		proratedAmt21.setPartialMonthEffectiveStartDate(DateTimeUtil.getXMLGregorianCalendar(JUL_15.plusDays(1)));
		proratedAmt21.setPartialMonthEffectiveEndDate(DateTimeUtil.getXMLGregorianCalendar(JUL_31));
		proratedAmt21.setPartialMonthPremiumAmount(new BigDecimal("50"));
		proratedAmt21.setPartialMonthAPTCAmount(new BigDecimal("25"));
		proratedAmt21.setPartialMonthCSRAmount(new BigDecimal("15"));
		fit2.getProratedAmount().add(proratedAmt21);
		
		policy.getFinancialInformation().add(fit2);
		
		List<SbmErrWarningLogDTO> financialErrors = sbmFinancialValidator.validateFinancialInfo(policy);
		
		assertFalse("financialErrors", financialErrors.isEmpty());
		assertEquals("financialErrors", 1, financialErrors.size());
		
		SbmErrWarningLogDTO financialError = financialErrors.get(0);
		
		assertEquals("ElementInError", "FinancialInformation", financialError.getElementInError());
		assertEquals("Error Code", "ER-046", financialError.getErrorWarningTypeCd());
	}
	
	/*
	 * validateFinancialInfo
	 */
	@Test
	public void test_validateProratedAmounts_R020_valid_amounts_provided() {
		
		String qhpId = RandomStringUtils.randomAlphanumeric(16).toUpperCase();
		String stateCd = qhpId.substring(5, 7);

		List<String> bizRules = Arrays.asList("R020");
		SBMCache.getBusinessRules(stateCd).addAll(bizRules);
		
		expect(mockSbmDataService.getMetalLevelByQhpid(EasyMock.anyString(), EasyMock.anyString()))
		.andReturn(null).anyTimes();
		replay(mockSbmDataService);
		
		PolicyType policy = new PolicyType();
		policy.setPolicyStartDate(DateTimeUtil.getXMLGregorianCalendar(JAN_1));
		policy.setPolicyEndDate(DateTimeUtil.getXMLGregorianCalendar(DEC_31));
		policy.setQHPId(qhpId);
		
		FinancialInformation fit = new FinancialInformation();
		
		fit.setFinancialEffectiveStartDate(DateTimeUtil.getXMLGregorianCalendar(JAN_1));
		fit.setFinancialEffectiveEndDate(DateTimeUtil.getXMLGregorianCalendar(JUL_15));
		fit.setMonthlyTotalPremiumAmount(new BigDecimal("200"));
		fit.setMonthlyAPTCAmount(new BigDecimal("100"));
		fit.setMonthlyCSRAmount(new BigDecimal("20"));
		
		ProratedAmountType proratedAmt11 = new ProratedAmountType();
		proratedAmt11.setPartialMonthEffectiveStartDate(DateTimeUtil.getXMLGregorianCalendar(JUL_1));
		proratedAmt11.setPartialMonthEffectiveEndDate(DateTimeUtil.getXMLGregorianCalendar(JUL_15));
		proratedAmt11.setPartialMonthPremiumAmount(new BigDecimal("100"));
		proratedAmt11.setPartialMonthAPTCAmount(new BigDecimal("50"));
		proratedAmt11.setPartialMonthCSRAmount(new BigDecimal("10"));
		fit.getProratedAmount().add(proratedAmt11);
		
		policy.getFinancialInformation().add(fit);
		
		
		FinancialInformation fit2 = new FinancialInformation();
		
		fit2.setFinancialEffectiveStartDate(DateTimeUtil.getXMLGregorianCalendar(JUL_15.plusDays(1)));
		fit2.setFinancialEffectiveEndDate(DateTimeUtil.getXMLGregorianCalendar(DEC_31));
		fit2.setMonthlyTotalPremiumAmount(new BigDecimal("100"));
		fit2.setMonthlyAPTCAmount(new BigDecimal("50"));
		fit2.setMonthlyCSRAmount(new BigDecimal("30"));
		
		ProratedAmountType proratedAmt21 = new ProratedAmountType();
		proratedAmt21.setPartialMonthEffectiveStartDate(DateTimeUtil.getXMLGregorianCalendar(JUL_15.plusDays(1)));
		proratedAmt21.setPartialMonthEffectiveEndDate(DateTimeUtil.getXMLGregorianCalendar(JUL_31));
		proratedAmt21.setPartialMonthPremiumAmount(new BigDecimal("50"));
		proratedAmt21.setPartialMonthAPTCAmount(new BigDecimal("25"));
		proratedAmt21.setPartialMonthCSRAmount(new BigDecimal("15"));
		fit2.getProratedAmount().add(proratedAmt21);
		
		policy.getFinancialInformation().add(fit2);
		
		List<SbmErrWarningLogDTO> financialErrors = sbmFinancialValidator.validateFinancialInfo(policy);
		
		assertTrue("financialErrors", financialErrors.isEmpty());
	}
	
	/*
	 * validateFinancialInfo
	 */
	@Test
	public void test_validateProratedAmounts_R020_valid_later_amounts_null() {
		
		String qhpId = RandomStringUtils.randomAlphanumeric(16).toUpperCase();
		String stateCd = qhpId.substring(5, 7);

		List<String> bizRules = Arrays.asList("R020");
		SBMCache.getBusinessRules(stateCd).addAll(bizRules);
		
		expect(mockSbmDataService.getMetalLevelByQhpid(EasyMock.anyString(), EasyMock.anyString()))
		.andReturn(null).anyTimes();
		replay(mockSbmDataService);
		
		PolicyType policy = new PolicyType();
		policy.setPolicyStartDate(DateTimeUtil.getXMLGregorianCalendar(JAN_1));
		policy.setPolicyEndDate(DateTimeUtil.getXMLGregorianCalendar(DEC_31));
		policy.setQHPId(qhpId);
		
		FinancialInformation fit = new FinancialInformation();
		
		fit.setFinancialEffectiveStartDate(DateTimeUtil.getXMLGregorianCalendar(JAN_1));
		fit.setFinancialEffectiveEndDate(DateTimeUtil.getXMLGregorianCalendar(JUL_15));
		fit.setMonthlyTotalPremiumAmount(new BigDecimal("200"));
		fit.setMonthlyAPTCAmount(new BigDecimal("100"));
		fit.setMonthlyCSRAmount(new BigDecimal("20"));
		
		ProratedAmountType proratedAmt11 = new ProratedAmountType();
		proratedAmt11.setPartialMonthEffectiveStartDate(DateTimeUtil.getXMLGregorianCalendar(JUL_1));
		proratedAmt11.setPartialMonthEffectiveEndDate(DateTimeUtil.getXMLGregorianCalendar(JUL_15));
		proratedAmt11.setPartialMonthPremiumAmount(new BigDecimal("100"));
		proratedAmt11.setPartialMonthAPTCAmount(new BigDecimal("50"));
		proratedAmt11.setPartialMonthCSRAmount(new BigDecimal("10"));
		fit.getProratedAmount().add(proratedAmt11);
		
		policy.getFinancialInformation().add(fit);
		
		
		FinancialInformation fit2 = new FinancialInformation();
		
		fit2.setFinancialEffectiveStartDate(DateTimeUtil.getXMLGregorianCalendar(JUL_15.plusDays(1)));
		fit2.setFinancialEffectiveEndDate(DateTimeUtil.getXMLGregorianCalendar(DEC_31));
		fit2.setMonthlyTotalPremiumAmount(new BigDecimal("100"));
		fit2.setMonthlyAPTCAmount(new BigDecimal("50"));
		fit2.setMonthlyCSRAmount(new BigDecimal("30"));
		
		ProratedAmountType proratedAmt21 = new ProratedAmountType();
		proratedAmt21.setPartialMonthEffectiveStartDate(DateTimeUtil.getXMLGregorianCalendar(JUL_15.plusDays(1)));
		proratedAmt21.setPartialMonthEffectiveEndDate(DateTimeUtil.getXMLGregorianCalendar(JUL_31));
/*		proratedAmt21.setPartialMonthPremiumAmount(new BigDecimal("50"));
		proratedAmt21.setPartialMonthAPTCAmount(new BigDecimal("25"));
		proratedAmt21.setPartialMonthCSRAmount(new BigDecimal("15"));*/
		fit2.getProratedAmount().add(proratedAmt21);
		
		policy.getFinancialInformation().add(fit2);
		
		List<SbmErrWarningLogDTO> financialErrors = sbmFinancialValidator.validateFinancialInfo(policy);
		
		assertTrue("financialErrors", financialErrors.isEmpty());
	}
	
	/*
	 * validateFinancialInfo
	 */
	@Test
	public void test_validateProratedAmounts_R020_valid_monthly_amounts_null() {
		
		String qhpId = RandomStringUtils.randomAlphanumeric(16).toUpperCase();
		String stateCd = qhpId.substring(5, 7);

		List<String> bizRules = Arrays.asList("R020");
		SBMCache.getBusinessRules(stateCd).addAll(bizRules);
		
		expect(mockSbmDataService.getMetalLevelByQhpid(EasyMock.anyString(), EasyMock.anyString()))
		.andReturn(null).anyTimes();
		replay(mockSbmDataService);
		
		PolicyType policy = new PolicyType();
		policy.setPolicyStartDate(DateTimeUtil.getXMLGregorianCalendar(JAN_1));
		policy.setPolicyEndDate(DateTimeUtil.getXMLGregorianCalendar(DEC_31));
		policy.setQHPId(qhpId);
		
		FinancialInformation fit = new FinancialInformation();
		
		fit.setFinancialEffectiveStartDate(DateTimeUtil.getXMLGregorianCalendar(JAN_1));
		fit.setFinancialEffectiveEndDate(DateTimeUtil.getXMLGregorianCalendar(JUL_15));
/*		fit.setMonthlyTotalPremiumAmount(new BigDecimal("200"));
		fit.setMonthlyAPTCAmount(new BigDecimal("100"));
		fit.setMonthlyCSRAmount(new BigDecimal("20"));*/
		
		ProratedAmountType proratedAmt11 = new ProratedAmountType();
		proratedAmt11.setPartialMonthEffectiveStartDate(DateTimeUtil.getXMLGregorianCalendar(JUL_1));
		proratedAmt11.setPartialMonthEffectiveEndDate(DateTimeUtil.getXMLGregorianCalendar(JUL_15));
		proratedAmt11.setPartialMonthPremiumAmount(new BigDecimal("100"));
		proratedAmt11.setPartialMonthAPTCAmount(new BigDecimal("50"));
		proratedAmt11.setPartialMonthCSRAmount(new BigDecimal("10"));
		fit.getProratedAmount().add(proratedAmt11);
		
		policy.getFinancialInformation().add(fit);
		
		
		FinancialInformation fit2 = new FinancialInformation();
		
		fit2.setFinancialEffectiveStartDate(DateTimeUtil.getXMLGregorianCalendar(JUL_15.plusDays(1)));
		fit2.setFinancialEffectiveEndDate(DateTimeUtil.getXMLGregorianCalendar(DEC_31));
		fit2.setMonthlyTotalPremiumAmount(new BigDecimal("100"));
		fit2.setMonthlyAPTCAmount(new BigDecimal("50"));
		fit2.setMonthlyCSRAmount(new BigDecimal("30"));
		
		ProratedAmountType proratedAmt21 = new ProratedAmountType();
		proratedAmt21.setPartialMonthEffectiveStartDate(DateTimeUtil.getXMLGregorianCalendar(JUL_15.plusDays(1)));
		proratedAmt21.setPartialMonthEffectiveEndDate(DateTimeUtil.getXMLGregorianCalendar(JUL_31));
		proratedAmt21.setPartialMonthPremiumAmount(new BigDecimal("50"));
		proratedAmt21.setPartialMonthAPTCAmount(new BigDecimal("25"));
		proratedAmt21.setPartialMonthCSRAmount(new BigDecimal("15"));
		fit2.getProratedAmount().add(proratedAmt21);
		
		policy.getFinancialInformation().add(fit2);
		
		List<SbmErrWarningLogDTO> financialErrors = sbmFinancialValidator.validateFinancialInfo(policy);
		
		assertTrue("financialErrors", financialErrors.isEmpty());
	}
	
	/*
	 * validateFinancialInfo
	 */
	@Test
	public void test_validateProratedAmounts_R020_valid_monthly_amounts_zero() {
		
		String qhpId = RandomStringUtils.randomAlphanumeric(16).toUpperCase();
		String stateCd = qhpId.substring(5, 7);

		List<String> bizRules = Arrays.asList("R020");
		SBMCache.getBusinessRules(stateCd).addAll(bizRules);
		
		expect(mockSbmDataService.getMetalLevelByQhpid(EasyMock.anyString(), EasyMock.anyString()))
		.andReturn(null).anyTimes();
		replay(mockSbmDataService);
		
		PolicyType policy = new PolicyType();
		policy.setPolicyStartDate(DateTimeUtil.getXMLGregorianCalendar(JAN_1));
		policy.setPolicyEndDate(DateTimeUtil.getXMLGregorianCalendar(DEC_31));
		policy.setQHPId(qhpId);
		
		FinancialInformation fit = new FinancialInformation();
		
		fit.setFinancialEffectiveStartDate(DateTimeUtil.getXMLGregorianCalendar(JAN_1));
		fit.setFinancialEffectiveEndDate(DateTimeUtil.getXMLGregorianCalendar(JUL_15));
		fit.setMonthlyTotalPremiumAmount(new BigDecimal("0"));
		fit.setMonthlyAPTCAmount(new BigDecimal("0"));
		fit.setMonthlyCSRAmount(new BigDecimal("0"));
		
		ProratedAmountType proratedAmt11 = new ProratedAmountType();
		proratedAmt11.setPartialMonthEffectiveStartDate(DateTimeUtil.getXMLGregorianCalendar(JUL_1));
		proratedAmt11.setPartialMonthEffectiveEndDate(DateTimeUtil.getXMLGregorianCalendar(JUL_15));
		proratedAmt11.setPartialMonthPremiumAmount(new BigDecimal("100"));
		proratedAmt11.setPartialMonthAPTCAmount(new BigDecimal("50"));
		proratedAmt11.setPartialMonthCSRAmount(new BigDecimal("10"));
		fit.getProratedAmount().add(proratedAmt11);
		
		policy.getFinancialInformation().add(fit);
		
		
		FinancialInformation fit2 = new FinancialInformation();
		
		fit2.setFinancialEffectiveStartDate(DateTimeUtil.getXMLGregorianCalendar(JUL_15.plusDays(1)));
		fit2.setFinancialEffectiveEndDate(DateTimeUtil.getXMLGregorianCalendar(DEC_31));
		fit2.setMonthlyTotalPremiumAmount(new BigDecimal("100"));
		fit2.setMonthlyAPTCAmount(new BigDecimal("50"));
		fit2.setMonthlyCSRAmount(new BigDecimal("30"));
		
		ProratedAmountType proratedAmt21 = new ProratedAmountType();
		proratedAmt21.setPartialMonthEffectiveStartDate(DateTimeUtil.getXMLGregorianCalendar(JUL_15.plusDays(1)));
		proratedAmt21.setPartialMonthEffectiveEndDate(DateTimeUtil.getXMLGregorianCalendar(JUL_31));
		proratedAmt21.setPartialMonthPremiumAmount(new BigDecimal("50"));
		proratedAmt21.setPartialMonthAPTCAmount(new BigDecimal("25"));
		proratedAmt21.setPartialMonthCSRAmount(new BigDecimal("15"));
		fit2.getProratedAmount().add(proratedAmt21);
		
		policy.getFinancialInformation().add(fit2);
		
		List<SbmErrWarningLogDTO> financialErrors = sbmFinancialValidator.validateFinancialInfo(policy);
		
		assertTrue("financialErrors", financialErrors.isEmpty());
	}
	
	/*
	 * validateFinancialInfo
	 */
	@Test
	public void test_validateProratedAmounts_R020_valid_Prev_prorated_Null() {
		
		String qhpId = RandomStringUtils.randomAlphanumeric(16).toUpperCase();
		String stateCd = qhpId.substring(5, 7);

		List<String> bizRules = Arrays.asList("R020");
		SBMCache.getBusinessRules(stateCd).addAll(bizRules);
		
		expect(mockSbmDataService.getMetalLevelByQhpid(EasyMock.anyString(), EasyMock.anyString()))
		.andReturn(null).anyTimes();
		replay(mockSbmDataService);
		
		PolicyType policy = new PolicyType();
		policy.setPolicyStartDate(DateTimeUtil.getXMLGregorianCalendar(JAN_1));
		policy.setPolicyEndDate(DateTimeUtil.getXMLGregorianCalendar(DEC_31));
		policy.setQHPId(qhpId);
		
		FinancialInformation fit = new FinancialInformation();
		
		fit.setFinancialEffectiveStartDate(DateTimeUtil.getXMLGregorianCalendar(JAN_1));
		fit.setFinancialEffectiveEndDate(DateTimeUtil.getXMLGregorianCalendar(JUL_15));
		fit.setMonthlyTotalPremiumAmount(new BigDecimal("200"));
		fit.setMonthlyAPTCAmount(new BigDecimal("100"));
		fit.setMonthlyCSRAmount(new BigDecimal("20"));
		
		ProratedAmountType proratedAmt11 = new ProratedAmountType();
		proratedAmt11.setPartialMonthEffectiveStartDate(DateTimeUtil.getXMLGregorianCalendar(JUL_1));
		proratedAmt11.setPartialMonthEffectiveEndDate(DateTimeUtil.getXMLGregorianCalendar(JUL_15));
		proratedAmt11.setPartialMonthPremiumAmount(new BigDecimal("100"));
		proratedAmt11.setPartialMonthAPTCAmount(new BigDecimal("50"));
		proratedAmt11.setPartialMonthCSRAmount(new BigDecimal("10"));
		fit.getProratedAmount().add(proratedAmt11);
		
		policy.getFinancialInformation().add(fit);
		
		
		FinancialInformation fit2 = new FinancialInformation();
		
		fit2.setFinancialEffectiveStartDate(DateTimeUtil.getXMLGregorianCalendar(JUL_15.plusDays(1)));
		fit2.setFinancialEffectiveEndDate(DateTimeUtil.getXMLGregorianCalendar(DEC_31));
		fit2.setMonthlyTotalPremiumAmount(new BigDecimal("100"));
		fit2.setMonthlyAPTCAmount(new BigDecimal("50"));
		fit2.setMonthlyCSRAmount(new BigDecimal("30"));
		
		ProratedAmountType proratedAmt21 = new ProratedAmountType();
		proratedAmt21.setPartialMonthEffectiveStartDate(DateTimeUtil.getXMLGregorianCalendar(JUL_15.plusDays(1)));
		proratedAmt21.setPartialMonthEffectiveEndDate(DateTimeUtil.getXMLGregorianCalendar(JUL_31));
		proratedAmt21.setPartialMonthPremiumAmount(new BigDecimal("50"));
		proratedAmt21.setPartialMonthAPTCAmount(new BigDecimal("25"));
		//proratedAmt21.setPartialMonthCSRAmount(new BigDecimal("15"));
		fit2.getProratedAmount().add(proratedAmt21);
		
		policy.getFinancialInformation().add(fit2);
		
		List<SbmErrWarningLogDTO> financialErrors = sbmFinancialValidator.validateFinancialInfo(policy);
		
		assertTrue("financialErrors", financialErrors.isEmpty());
	}
	
	/*
	 * validateFinancialInfo
	 */
	@Test
	public void test_validateProratedAmounts_R020_valid_Prev_prorated_And_later_Null() {
		
		String qhpId = RandomStringUtils.randomAlphanumeric(16).toUpperCase();
		String stateCd = qhpId.substring(5, 7);

		List<String> bizRules = Arrays.asList("R020");
		SBMCache.getBusinessRules(stateCd).addAll(bizRules);
		
		expect(mockSbmDataService.getMetalLevelByQhpid(EasyMock.anyString(), EasyMock.anyString()))
		.andReturn(null).anyTimes();
		replay(mockSbmDataService);
		
		PolicyType policy = new PolicyType();
		policy.setPolicyStartDate(DateTimeUtil.getXMLGregorianCalendar(JAN_1));
		policy.setPolicyEndDate(DateTimeUtil.getXMLGregorianCalendar(DEC_31));
		policy.setQHPId(qhpId);
		
		FinancialInformation fit = new FinancialInformation();
		
		fit.setFinancialEffectiveStartDate(DateTimeUtil.getXMLGregorianCalendar(JAN_1));
		fit.setFinancialEffectiveEndDate(DateTimeUtil.getXMLGregorianCalendar(JUL_15));
		fit.setMonthlyTotalPremiumAmount(new BigDecimal("200"));
		fit.setMonthlyAPTCAmount(new BigDecimal("100"));
		fit.setMonthlyCSRAmount(new BigDecimal("20"));
		
		ProratedAmountType proratedAmt11 = new ProratedAmountType();
		proratedAmt11.setPartialMonthEffectiveStartDate(DateTimeUtil.getXMLGregorianCalendar(JUL_1));
		proratedAmt11.setPartialMonthEffectiveEndDate(DateTimeUtil.getXMLGregorianCalendar(JUL_15));
		proratedAmt11.setPartialMonthPremiumAmount(new BigDecimal("100"));
		proratedAmt11.setPartialMonthAPTCAmount(new BigDecimal("50"));
		//proratedAmt11.setPartialMonthCSRAmount(new BigDecimal("10"));
		fit.getProratedAmount().add(proratedAmt11);
		
		policy.getFinancialInformation().add(fit);
		
		
		FinancialInformation fit2 = new FinancialInformation();
		
		fit2.setFinancialEffectiveStartDate(DateTimeUtil.getXMLGregorianCalendar(JUL_15.plusDays(1)));
		fit2.setFinancialEffectiveEndDate(DateTimeUtil.getXMLGregorianCalendar(DEC_31));
		fit2.setMonthlyTotalPremiumAmount(new BigDecimal("100"));
		fit2.setMonthlyAPTCAmount(new BigDecimal("50"));
		fit2.setMonthlyCSRAmount(new BigDecimal("30"));
		
		ProratedAmountType proratedAmt21 = new ProratedAmountType();
		proratedAmt21.setPartialMonthEffectiveStartDate(DateTimeUtil.getXMLGregorianCalendar(JUL_15.plusDays(1)));
		proratedAmt21.setPartialMonthEffectiveEndDate(DateTimeUtil.getXMLGregorianCalendar(JUL_31));
		proratedAmt21.setPartialMonthPremiumAmount(new BigDecimal("50"));
		proratedAmt21.setPartialMonthAPTCAmount(new BigDecimal("25"));
		//proratedAmt21.setPartialMonthCSRAmount(new BigDecimal("15"));
		fit2.getProratedAmount().add(proratedAmt21);
		
		policy.getFinancialInformation().add(fit2);
		
		List<SbmErrWarningLogDTO> financialErrors = sbmFinancialValidator.validateFinancialInfo(policy);
		
		assertTrue("financialErrors", financialErrors.isEmpty());
	}
	
	/*
	 * validateFinancialInfo
	 */
	@Test
	public void test_validateProratedAmounts_R020_valid_finEnd_GT_later_proratedStart() {
		
		String qhpId = RandomStringUtils.randomAlphanumeric(16).toUpperCase();
		String stateCd = qhpId.substring(5, 7);

		List<String> bizRules = Arrays.asList("R020");
		SBMCache.getBusinessRules(stateCd).addAll(bizRules);
		
		expect(mockSbmDataService.getMetalLevelByQhpid(EasyMock.anyString(), EasyMock.anyString()))
		.andReturn(null).anyTimes();
		replay(mockSbmDataService);
		
		PolicyType policy = new PolicyType();
		policy.setPolicyStartDate(DateTimeUtil.getXMLGregorianCalendar(JAN_1));
		policy.setPolicyEndDate(DateTimeUtil.getXMLGregorianCalendar(DEC_31));
		policy.setQHPId(qhpId);
		
		FinancialInformation fit = new FinancialInformation();
		
		fit.setFinancialEffectiveStartDate(DateTimeUtil.getXMLGregorianCalendar(JAN_1));
		fit.setFinancialEffectiveEndDate(DateTimeUtil.getXMLGregorianCalendar(JUL_31));
		fit.setMonthlyTotalPremiumAmount(new BigDecimal("200"));
		fit.setMonthlyAPTCAmount(new BigDecimal("100"));
		fit.setMonthlyCSRAmount(new BigDecimal("20"));
		
		ProratedAmountType proratedAmt11 = new ProratedAmountType();
		proratedAmt11.setPartialMonthEffectiveStartDate(DateTimeUtil.getXMLGregorianCalendar(JUL_1));
		proratedAmt11.setPartialMonthEffectiveEndDate(DateTimeUtil.getXMLGregorianCalendar(JUL_15));
		proratedAmt11.setPartialMonthPremiumAmount(new BigDecimal("100"));
		proratedAmt11.setPartialMonthAPTCAmount(new BigDecimal("50"));
		//proratedAmt11.setPartialMonthCSRAmount(new BigDecimal("10"));
		fit.getProratedAmount().add(proratedAmt11);
		
		policy.getFinancialInformation().add(fit);
		
		
		FinancialInformation fit2 = new FinancialInformation();
		
		fit2.setFinancialEffectiveStartDate(DateTimeUtil.getXMLGregorianCalendar(JUL_15.plusDays(1)));
		fit2.setFinancialEffectiveEndDate(DateTimeUtil.getXMLGregorianCalendar(DEC_31));
		fit2.setMonthlyTotalPremiumAmount(new BigDecimal("100"));
		fit2.setMonthlyAPTCAmount(new BigDecimal("50"));
		fit2.setMonthlyCSRAmount(new BigDecimal("30"));
		
		ProratedAmountType proratedAmt21 = new ProratedAmountType();
		proratedAmt21.setPartialMonthEffectiveStartDate(DateTimeUtil.getXMLGregorianCalendar(JUL_15.plusDays(1)));
		proratedAmt21.setPartialMonthEffectiveEndDate(DateTimeUtil.getXMLGregorianCalendar(JUL_31));
		proratedAmt21.setPartialMonthPremiumAmount(new BigDecimal("50"));
		proratedAmt21.setPartialMonthAPTCAmount(new BigDecimal("25"));
		proratedAmt21.setPartialMonthCSRAmount(new BigDecimal("15"));
		fit2.getProratedAmount().add(proratedAmt21);
		
		policy.getFinancialInformation().add(fit2);
		
		List<SbmErrWarningLogDTO> financialErrors = sbmFinancialValidator.validateFinancialInfo(policy);
		
		assertTrue("financialErrors", financialErrors.isEmpty());
	}
	
	/*
	 * validateFinancialInfo
	 */
	@Test
	public void test_validateProratedAmounts_R020_valid_finEnd_proratedStart_Diff_Months() {
		
		String qhpId = RandomStringUtils.randomAlphanumeric(16).toUpperCase();
		String stateCd = qhpId.substring(5, 7);

		List<String> bizRules = Arrays.asList("R020");
		SBMCache.getBusinessRules(stateCd).addAll(bizRules);
		
		expect(mockSbmDataService.getMetalLevelByQhpid(EasyMock.anyString(), EasyMock.anyString()))
		.andReturn(null).anyTimes();
		replay(mockSbmDataService);
		
		PolicyType policy = new PolicyType();
		policy.setPolicyStartDate(DateTimeUtil.getXMLGregorianCalendar(JAN_1));
		policy.setPolicyEndDate(DateTimeUtil.getXMLGregorianCalendar(DEC_31));
		policy.setQHPId(qhpId);
		
		FinancialInformation fit = new FinancialInformation();
		
		fit.setFinancialEffectiveStartDate(DateTimeUtil.getXMLGregorianCalendar(JAN_1));
		fit.setFinancialEffectiveEndDate(DateTimeUtil.getXMLGregorianCalendar(JUN_15));
		fit.setMonthlyTotalPremiumAmount(new BigDecimal("200"));
		fit.setMonthlyAPTCAmount(new BigDecimal("100"));
		fit.setMonthlyCSRAmount(new BigDecimal("20"));
		
		ProratedAmountType proratedAmt11 = new ProratedAmountType();
		proratedAmt11.setPartialMonthEffectiveStartDate(DateTimeUtil.getXMLGregorianCalendar(JUL_1));
		proratedAmt11.setPartialMonthEffectiveEndDate(DateTimeUtil.getXMLGregorianCalendar(JUL_15));
		proratedAmt11.setPartialMonthPremiumAmount(new BigDecimal("100"));
		proratedAmt11.setPartialMonthAPTCAmount(new BigDecimal("50"));
		//proratedAmt11.setPartialMonthCSRAmount(new BigDecimal("10"));
		fit.getProratedAmount().add(proratedAmt11);
		
		policy.getFinancialInformation().add(fit);
		
		
		FinancialInformation fit2 = new FinancialInformation();
		
		fit2.setFinancialEffectiveStartDate(DateTimeUtil.getXMLGregorianCalendar(JUL_15.plusDays(1)));
		fit2.setFinancialEffectiveEndDate(DateTimeUtil.getXMLGregorianCalendar(DEC_31));
		fit2.setMonthlyTotalPremiumAmount(new BigDecimal("100"));
		fit2.setMonthlyAPTCAmount(new BigDecimal("50"));
		fit2.setMonthlyCSRAmount(new BigDecimal("30"));
		
		ProratedAmountType proratedAmt21 = new ProratedAmountType();
		proratedAmt21.setPartialMonthEffectiveStartDate(DateTimeUtil.getXMLGregorianCalendar(JUL_15.plusDays(1)));
		proratedAmt21.setPartialMonthEffectiveEndDate(DateTimeUtil.getXMLGregorianCalendar(JUL_31));
		proratedAmt21.setPartialMonthPremiumAmount(new BigDecimal("50"));
		proratedAmt21.setPartialMonthAPTCAmount(new BigDecimal("25"));
		proratedAmt21.setPartialMonthCSRAmount(new BigDecimal("15"));
		fit2.getProratedAmount().add(proratedAmt21);
		
		policy.getFinancialInformation().add(fit2);
		
		List<SbmErrWarningLogDTO> financialErrors = sbmFinancialValidator.validateFinancialInfo(policy);
		
		assertTrue("financialErrors", financialErrors.isEmpty());
	}

	/*
	 * validateFinancialInfo
	 */
	@Test
	public void test_validateProratedAmounts_R020_valid_finStart_proratedEnd_Diff_Years() {

		String qhpId = RandomStringUtils.randomAlphanumeric(16).toUpperCase();
		String stateCd = qhpId.substring(5, 7);

		List<String> bizRules = Arrays.asList("R020");
		SBMCache.getBusinessRules(stateCd).addAll(bizRules);
		
		expect(mockSbmDataService.getMetalLevelByQhpid(EasyMock.anyString(), EasyMock.anyString()))
		.andReturn(null).anyTimes();
		replay(mockSbmDataService);
		
		PolicyType policy = new PolicyType();
		policy.setPolicyStartDate(DateTimeUtil.getXMLGregorianCalendar(JAN_1));
		policy.setPolicyEndDate(DateTimeUtil.getXMLGregorianCalendar(DEC_31));
		policy.setQHPId(qhpId);
		
		FinancialInformation fit = new FinancialInformation();
		
		fit.setFinancialEffectiveStartDate(DateTimeUtil.getXMLGregorianCalendar(JAN_1));
		fit.setFinancialEffectiveEndDate(DateTimeUtil.getXMLGregorianCalendar(JUL_15.plusYears(1)));
		fit.setMonthlyTotalPremiumAmount(new BigDecimal("200"));
		fit.setMonthlyAPTCAmount(new BigDecimal("100"));
		fit.setMonthlyCSRAmount(new BigDecimal("20"));
		
		ProratedAmountType proratedAmt11 = new ProratedAmountType();
		proratedAmt11.setPartialMonthEffectiveStartDate(DateTimeUtil.getXMLGregorianCalendar(JUL_1));
		proratedAmt11.setPartialMonthEffectiveEndDate(DateTimeUtil.getXMLGregorianCalendar(JUL_15));
		proratedAmt11.setPartialMonthPremiumAmount(new BigDecimal("100"));
		proratedAmt11.setPartialMonthAPTCAmount(new BigDecimal("50"));
		//proratedAmt11.setPartialMonthCSRAmount(new BigDecimal("10"));
		fit.getProratedAmount().add(proratedAmt11);
		
		policy.getFinancialInformation().add(fit);
		
		
		FinancialInformation fit2 = new FinancialInformation();
		
		fit2.setFinancialEffectiveStartDate(DateTimeUtil.getXMLGregorianCalendar(JUL_15.plusDays(1)));
		fit2.setFinancialEffectiveEndDate(DateTimeUtil.getXMLGregorianCalendar(DEC_31));
		fit2.setMonthlyTotalPremiumAmount(new BigDecimal("100"));
		fit2.setMonthlyAPTCAmount(new BigDecimal("50"));
		fit2.setMonthlyCSRAmount(new BigDecimal("30"));
		
		ProratedAmountType proratedAmt21 = new ProratedAmountType();
		proratedAmt21.setPartialMonthEffectiveStartDate(DateTimeUtil.getXMLGregorianCalendar(JUL_15.plusDays(1)));
		proratedAmt21.setPartialMonthEffectiveEndDate(DateTimeUtil.getXMLGregorianCalendar(JUL_31));
		proratedAmt21.setPartialMonthPremiumAmount(new BigDecimal("50"));
		proratedAmt21.setPartialMonthAPTCAmount(new BigDecimal("25"));
		proratedAmt21.setPartialMonthCSRAmount(new BigDecimal("15"));
		fit2.getProratedAmount().add(proratedAmt21);
		
		policy.getFinancialInformation().add(fit2);
		
		List<SbmErrWarningLogDTO> financialErrors = sbmFinancialValidator.validateFinancialInfo(policy);
		
		assertTrue("financialErrors", financialErrors.isEmpty());
	}

	/*
	 * validateFinancialInfo
	 */
	@Test
	public void test_validateProratedAmounts_R020_valid_No_ProratedAmounts() {
		
		String qhpId = RandomStringUtils.randomAlphanumeric(16).toUpperCase();
		String stateCd = qhpId.substring(5, 7);

		List<String> bizRules = Arrays.asList("R020");
		SBMCache.getBusinessRules(stateCd).addAll(bizRules);
		
		expect(mockSbmDataService.getMetalLevelByQhpid(EasyMock.anyString(), EasyMock.anyString()))
		.andReturn(null).anyTimes();
		replay(mockSbmDataService);
		
		PolicyType policy = new PolicyType();
		policy.setPolicyStartDate(DateTimeUtil.getXMLGregorianCalendar(JAN_1));
		policy.setPolicyEndDate(DateTimeUtil.getXMLGregorianCalendar(DEC_31));
		policy.setQHPId(qhpId);
		
		FinancialInformation fit = new FinancialInformation();
		
		fit.setFinancialEffectiveStartDate(DateTimeUtil.getXMLGregorianCalendar(JAN_1));
		fit.setFinancialEffectiveEndDate(DateTimeUtil.getXMLGregorianCalendar(JUL_15));
		fit.setMonthlyTotalPremiumAmount(new BigDecimal("200"));
		fit.setMonthlyAPTCAmount(new BigDecimal("100"));
		fit.setMonthlyCSRAmount(new BigDecimal("20"));
		
		ProratedAmountType proratedAmt11 = new ProratedAmountType();
		proratedAmt11.setPartialMonthEffectiveStartDate(DateTimeUtil.getXMLGregorianCalendar(JUL_1));
		proratedAmt11.setPartialMonthEffectiveEndDate(DateTimeUtil.getXMLGregorianCalendar(JUL_15));
		proratedAmt11.setPartialMonthPremiumAmount(new BigDecimal("100"));
		proratedAmt11.setPartialMonthAPTCAmount(new BigDecimal("50"));
		//proratedAmt11.setPartialMonthCSRAmount(new BigDecimal("10"));
		fit.getProratedAmount().add(proratedAmt11);
		
		policy.getFinancialInformation().add(fit);
		
		
		FinancialInformation fit2 = new FinancialInformation();
		
		fit2.setFinancialEffectiveStartDate(DateTimeUtil.getXMLGregorianCalendar(JUL_15.plusDays(1)));
		fit2.setFinancialEffectiveEndDate(DateTimeUtil.getXMLGregorianCalendar(DEC_31));
		fit2.setMonthlyTotalPremiumAmount(new BigDecimal("100"));
		fit2.setMonthlyAPTCAmount(new BigDecimal("50"));
		fit2.setMonthlyCSRAmount(new BigDecimal("30"));
		
		policy.getFinancialInformation().add(fit2);
		
		List<SbmErrWarningLogDTO> financialErrors = sbmFinancialValidator.validateFinancialInfo(policy);
		
		assertTrue("financialErrors", financialErrors.isEmpty());
	}

	/*
	 * validateFinancialInfo
	 */
	@Test
	public void test_validateProratedAmounts_R021_invalid() {
		
		String qhpId = RandomStringUtils.randomAlphanumeric(16).toUpperCase();
		String stateCd = qhpId.substring(5, 7);

		List<String> bizRules = Arrays.asList("R021");
		SBMCache.getBusinessRules(stateCd).addAll(bizRules);
		
		expect(mockSbmDataService.getMetalLevelByQhpid(EasyMock.anyString(), EasyMock.anyString()))
		.andReturn(null).anyTimes();
		replay(mockSbmDataService);
		
		PolicyType policy = new PolicyType();
		policy.setPolicyStartDate(DateTimeUtil.getXMLGregorianCalendar(JAN_1));
		policy.setPolicyEndDate(DateTimeUtil.getXMLGregorianCalendar(DEC_31));
		policy.setQHPId(qhpId);
		
		//Partial Date < Fin Start
		FinancialInformation fit = new FinancialInformation();
		
		fit.setFinancialEffectiveStartDate(DateTimeUtil.getXMLGregorianCalendar(JAN_15));
		fit.setFinancialEffectiveEndDate(DateTimeUtil.getXMLGregorianCalendar(JUL_15));
		fit.setMonthlyTotalPremiumAmount(new BigDecimal("200"));
		fit.setMonthlyAPTCAmount(new BigDecimal("100"));
		fit.setMonthlyCSRAmount(new BigDecimal("20"));
		
		ProratedAmountType proratedAmt11 = new ProratedAmountType();
		proratedAmt11.setPartialMonthEffectiveStartDate(DateTimeUtil.getXMLGregorianCalendar(JAN_1));
		proratedAmt11.setPartialMonthEffectiveEndDate(DateTimeUtil.getXMLGregorianCalendar(JAN_31));
		proratedAmt11.setPartialMonthPremiumAmount(new BigDecimal("100"));
		proratedAmt11.setPartialMonthAPTCAmount(new BigDecimal("50"));
		proratedAmt11.setPartialMonthCSRAmount(new BigDecimal("10"));
		fit.getProratedAmount().add(proratedAmt11);
		
		policy.getFinancialInformation().add(fit);
		
		//Partial Date < Fin Start But diff month
		FinancialInformation fit2 = new FinancialInformation();
		
		fit2.setFinancialEffectiveStartDate(DateTimeUtil.getXMLGregorianCalendar(AUG_1));
		fit2.setFinancialEffectiveEndDate(DateTimeUtil.getXMLGregorianCalendar(DEC_31));
		fit2.setMonthlyTotalPremiumAmount(new BigDecimal("100"));
		fit2.setMonthlyAPTCAmount(new BigDecimal("50"));
		fit2.setMonthlyCSRAmount(new BigDecimal("30"));
		
		ProratedAmountType proratedAmt21 = new ProratedAmountType();
		proratedAmt21.setPartialMonthEffectiveStartDate(DateTimeUtil.getXMLGregorianCalendar(JUL_15.plusDays(1)));
		proratedAmt21.setPartialMonthEffectiveEndDate(DateTimeUtil.getXMLGregorianCalendar(JUL_31));
		proratedAmt21.setPartialMonthPremiumAmount(new BigDecimal("50"));
		proratedAmt21.setPartialMonthAPTCAmount(new BigDecimal("25"));
		proratedAmt21.setPartialMonthCSRAmount(new BigDecimal("15"));
		fit2.getProratedAmount().add(proratedAmt21);
		
		policy.getFinancialInformation().add(fit2);
		
		List<SbmErrWarningLogDTO> financialErrors = sbmFinancialValidator.validateFinancialInfo(policy);
		
		assertFalse("financialErrors", financialErrors.isEmpty());
		assertEquals("financialErrors", 1, financialErrors.size());
		
		SbmErrWarningLogDTO financialError = financialErrors.get(0);
		
		assertEquals("ElementInError", "PartialMonthEffectiveStartDate", financialError.getElementInError());
		assertEquals("Error Code", "ER-047", financialError.getErrorWarningTypeCd());
	}
	
	/*
	 * validateFinancialInfo
	 */
	@Test
	public void test_validateProratedAmounts_R021_valid() {
		
		String qhpId = RandomStringUtils.randomAlphanumeric(16).toUpperCase();
		String stateCd = qhpId.substring(5, 7);

		List<String> bizRules = Arrays.asList("R021");
		SBMCache.getBusinessRules(stateCd).addAll(bizRules);
		
		expect(mockSbmDataService.getMetalLevelByQhpid(EasyMock.anyString(), EasyMock.anyString()))
		.andReturn(null).anyTimes();
		replay(mockSbmDataService);
		
		PolicyType policy = new PolicyType();
		policy.setPolicyStartDate(DateTimeUtil.getXMLGregorianCalendar(JAN_1));
		policy.setPolicyEndDate(DateTimeUtil.getXMLGregorianCalendar(DEC_31));
		policy.setQHPId(qhpId);
		
		//Partial Date = Fin Start
		FinancialInformation fit = new FinancialInformation();
		
		fit.setFinancialEffectiveStartDate(DateTimeUtil.getXMLGregorianCalendar(JAN_1));
		fit.setFinancialEffectiveEndDate(DateTimeUtil.getXMLGregorianCalendar(JUL_15));
		fit.setMonthlyTotalPremiumAmount(new BigDecimal("200"));
		fit.setMonthlyAPTCAmount(new BigDecimal("100"));
		fit.setMonthlyCSRAmount(new BigDecimal("20"));
		
		ProratedAmountType proratedAmt11 = new ProratedAmountType();
		proratedAmt11.setPartialMonthEffectiveStartDate(DateTimeUtil.getXMLGregorianCalendar(JAN_1));
		proratedAmt11.setPartialMonthEffectiveEndDate(DateTimeUtil.getXMLGregorianCalendar(JAN_15));
		proratedAmt11.setPartialMonthPremiumAmount(new BigDecimal("100"));
		proratedAmt11.setPartialMonthAPTCAmount(new BigDecimal("50"));
		proratedAmt11.setPartialMonthCSRAmount(new BigDecimal("10"));
		fit.getProratedAmount().add(proratedAmt11);
		
		//Partial Date > Fin Start but diff month
		ProratedAmountType proratedAmt12 = new ProratedAmountType();
		proratedAmt12.setPartialMonthEffectiveStartDate(DateTimeUtil.getXMLGregorianCalendar(JUL_1));
		proratedAmt12.setPartialMonthEffectiveEndDate(DateTimeUtil.getXMLGregorianCalendar(JUL_15));
		proratedAmt12.setPartialMonthPremiumAmount(new BigDecimal("100"));
		proratedAmt12.setPartialMonthAPTCAmount(new BigDecimal("50"));
		proratedAmt12.setPartialMonthCSRAmount(new BigDecimal("10"));
		fit.getProratedAmount().add(proratedAmt12);
		
		policy.getFinancialInformation().add(fit);
		
		//Partial Date > Fin Start
		FinancialInformation fit2 = new FinancialInformation();
		
		fit2.setFinancialEffectiveStartDate(DateTimeUtil.getXMLGregorianCalendar(JUL_1));
		fit2.setFinancialEffectiveEndDate(DateTimeUtil.getXMLGregorianCalendar(DEC_31));
		fit2.setMonthlyTotalPremiumAmount(new BigDecimal("100"));
		fit2.setMonthlyAPTCAmount(new BigDecimal("50"));
		fit2.setMonthlyCSRAmount(new BigDecimal("30"));
		
		ProratedAmountType proratedAmt21 = new ProratedAmountType();
		proratedAmt21.setPartialMonthEffectiveStartDate(DateTimeUtil.getXMLGregorianCalendar(JUL_15.plusDays(1)));
		proratedAmt21.setPartialMonthEffectiveEndDate(DateTimeUtil.getXMLGregorianCalendar(JUL_31));
		proratedAmt21.setPartialMonthPremiumAmount(new BigDecimal("50"));
		proratedAmt21.setPartialMonthAPTCAmount(new BigDecimal("25"));
		proratedAmt21.setPartialMonthCSRAmount(new BigDecimal("15"));
		fit2.getProratedAmount().add(proratedAmt21);
		
		policy.getFinancialInformation().add(fit2);
		
		List<SbmErrWarningLogDTO> financialErrors = sbmFinancialValidator.validateFinancialInfo(policy);
		
		assertTrue("financialErrors", financialErrors.isEmpty());
	}

	/*
	 * validateFinancialInfo
	 */
	@Test
	public void test_validateProratedAmounts_R022_invalid() {
		
		String qhpId = RandomStringUtils.randomAlphanumeric(16).toUpperCase();
		String stateCd = qhpId.substring(5, 7);

		List<String> bizRules = Arrays.asList("R022");
		SBMCache.getBusinessRules(stateCd).addAll(bizRules);
		
		expect(mockSbmDataService.getMetalLevelByQhpid(EasyMock.anyString(), EasyMock.anyString()))
		.andReturn(null).anyTimes();
		replay(mockSbmDataService);
		
		PolicyType policy = new PolicyType();
		policy.setPolicyStartDate(DateTimeUtil.getXMLGregorianCalendar(JAN_1));
		policy.setPolicyEndDate(DateTimeUtil.getXMLGregorianCalendar(DEC_15));
		policy.setQHPId(qhpId);
		
		//Partial End Date > Fin End
		FinancialInformation fit = new FinancialInformation();
		
		fit.setFinancialEffectiveStartDate(DateTimeUtil.getXMLGregorianCalendar(JAN_15));
		fit.setFinancialEffectiveEndDate(DateTimeUtil.getXMLGregorianCalendar(JUL_15));
		fit.setMonthlyTotalPremiumAmount(new BigDecimal("200"));
		fit.setMonthlyAPTCAmount(new BigDecimal("100"));
		fit.setMonthlyCSRAmount(new BigDecimal("20"));
		
		ProratedAmountType proratedAmt11 = new ProratedAmountType();
		proratedAmt11.setPartialMonthEffectiveStartDate(DateTimeUtil.getXMLGregorianCalendar(JUL_1));
		proratedAmt11.setPartialMonthEffectiveEndDate(DateTimeUtil.getXMLGregorianCalendar(JUL_31));
		proratedAmt11.setPartialMonthPremiumAmount(new BigDecimal("100"));
		proratedAmt11.setPartialMonthAPTCAmount(new BigDecimal("50"));
		proratedAmt11.setPartialMonthCSRAmount(new BigDecimal("10"));
		fit.getProratedAmount().add(proratedAmt11);
		
		policy.getFinancialInformation().add(fit);
		
		//Partial End Date > Fin End But diff month
		FinancialInformation fit2 = new FinancialInformation();
		
		fit2.setFinancialEffectiveStartDate(DateTimeUtil.getXMLGregorianCalendar(AUG_1));
		fit2.setFinancialEffectiveEndDate(DateTimeUtil.getXMLGregorianCalendar(NOV_30));
		fit2.setMonthlyTotalPremiumAmount(new BigDecimal("100"));
		fit2.setMonthlyAPTCAmount(new BigDecimal("50"));
		fit2.setMonthlyCSRAmount(new BigDecimal("30"));
		
		ProratedAmountType proratedAmt21 = new ProratedAmountType();
		proratedAmt21.setPartialMonthEffectiveStartDate(DateTimeUtil.getXMLGregorianCalendar(DEC_1));
		proratedAmt21.setPartialMonthEffectiveEndDate(DateTimeUtil.getXMLGregorianCalendar(DEC_15));
		proratedAmt21.setPartialMonthPremiumAmount(new BigDecimal("50"));
		proratedAmt21.setPartialMonthAPTCAmount(new BigDecimal("25"));
		proratedAmt21.setPartialMonthCSRAmount(new BigDecimal("15"));
		fit2.getProratedAmount().add(proratedAmt21);
		
		policy.getFinancialInformation().add(fit2);
		
		List<SbmErrWarningLogDTO> financialErrors = sbmFinancialValidator.validateFinancialInfo(policy);
		
		assertFalse("financialErrors", financialErrors.isEmpty());
		assertEquals("financialErrors", 1, financialErrors.size());
		
		SbmErrWarningLogDTO financialError = financialErrors.get(0);
		
		assertEquals("ElementInError", "PartialMonthEffectiveEndDate", financialError.getElementInError());
		assertEquals("Error Code", "ER-048", financialError.getErrorWarningTypeCd());
	}
	
	/*
	 * validateFinancialInfo
	 */
	@Test
	public void test_validateProratedAmounts_R022_valid() {
		
		String qhpId = RandomStringUtils.randomAlphanumeric(16).toUpperCase();
		String stateCd = qhpId.substring(5, 7);

		List<String> bizRules = Arrays.asList("R022");
		SBMCache.getBusinessRules(stateCd).addAll(bizRules);
		
		expect(mockSbmDataService.getMetalLevelByQhpid(EasyMock.anyString(), EasyMock.anyString()))
		.andReturn(null).anyTimes();
		replay(mockSbmDataService);
		
		PolicyType policy = new PolicyType();
		policy.setPolicyStartDate(DateTimeUtil.getXMLGregorianCalendar(JAN_1));
		policy.setPolicyEndDate(DateTimeUtil.getXMLGregorianCalendar(DEC_31));
		policy.setQHPId(qhpId);
		
		//Partial Date = Fin Start
		FinancialInformation fit = new FinancialInformation();
		
		fit.setFinancialEffectiveStartDate(DateTimeUtil.getXMLGregorianCalendar(JAN_1));
		fit.setFinancialEffectiveEndDate(DateTimeUtil.getXMLGregorianCalendar(JUL_15));
		fit.setMonthlyTotalPremiumAmount(new BigDecimal("200"));
		fit.setMonthlyAPTCAmount(new BigDecimal("100"));
		fit.setMonthlyCSRAmount(new BigDecimal("20"));
		
		ProratedAmountType proratedAmt11 = new ProratedAmountType();
		proratedAmt11.setPartialMonthEffectiveStartDate(DateTimeUtil.getXMLGregorianCalendar(JUL_1));
		proratedAmt11.setPartialMonthEffectiveEndDate(DateTimeUtil.getXMLGregorianCalendar(JUL_15));
		proratedAmt11.setPartialMonthPremiumAmount(new BigDecimal("100"));
		proratedAmt11.setPartialMonthAPTCAmount(new BigDecimal("50"));
		proratedAmt11.setPartialMonthCSRAmount(new BigDecimal("10"));
		fit.getProratedAmount().add(proratedAmt11);
		
		policy.getFinancialInformation().add(fit);
		
		//Partial Date > Fin Start
		FinancialInformation fit2 = new FinancialInformation();
		
		fit2.setFinancialEffectiveStartDate(DateTimeUtil.getXMLGregorianCalendar(JUL_1));
		fit2.setFinancialEffectiveEndDate(DateTimeUtil.getXMLGregorianCalendar(DEC_31));
		fit2.setMonthlyTotalPremiumAmount(new BigDecimal("100"));
		fit2.setMonthlyAPTCAmount(new BigDecimal("50"));
		fit2.setMonthlyCSRAmount(new BigDecimal("30"));
		
		ProratedAmountType proratedAmt21 = new ProratedAmountType();
		proratedAmt21.setPartialMonthEffectiveStartDate(DateTimeUtil.getXMLGregorianCalendar(DEC_1));
		proratedAmt21.setPartialMonthEffectiveEndDate(DateTimeUtil.getXMLGregorianCalendar(DEC_15));
		proratedAmt21.setPartialMonthPremiumAmount(new BigDecimal("50"));
		proratedAmt21.setPartialMonthAPTCAmount(new BigDecimal("25"));
		proratedAmt21.setPartialMonthCSRAmount(new BigDecimal("15"));
		fit2.getProratedAmount().add(proratedAmt21);
		
		policy.getFinancialInformation().add(fit2);
		
		List<SbmErrWarningLogDTO> financialErrors = sbmFinancialValidator.validateFinancialInfo(policy);
		
		assertTrue("financialErrors", financialErrors.isEmpty());
	}

	/*
	 * validateFinancialInfo
	 */
	@Test
	public void test_validateProratedAmounts_R023_invalid() {
		
		String qhpId = RandomStringUtils.randomAlphanumeric(16).toUpperCase();
		String stateCd = qhpId.substring(5, 7);

		List<String> bizRules = Arrays.asList("R023");
		SBMCache.getBusinessRules(stateCd).addAll(bizRules);
		
		expect(mockSbmDataService.getMetalLevelByQhpid(EasyMock.anyString(), EasyMock.anyString()))
		.andReturn(null).anyTimes();
		replay(mockSbmDataService);
		
		PolicyType policy = new PolicyType();
		policy.setPolicyStartDate(DateTimeUtil.getXMLGregorianCalendar(JAN_1));
		policy.setPolicyEndDate(DateTimeUtil.getXMLGregorianCalendar(DEC_15));
		policy.setQHPId(qhpId);
		
		FinancialInformation fit = new FinancialInformation();
		
		fit.setFinancialEffectiveStartDate(DateTimeUtil.getXMLGregorianCalendar(JAN_1));
		fit.setFinancialEffectiveEndDate(DateTimeUtil.getXMLGregorianCalendar(JUL_15));
		fit.setMonthlyTotalPremiumAmount(new BigDecimal("200"));
		fit.setMonthlyAPTCAmount(new BigDecimal("100"));
		fit.setMonthlyCSRAmount(new BigDecimal("20"));
		
		ProratedAmountType proratedAmt11 = new ProratedAmountType();
		proratedAmt11.setPartialMonthEffectiveStartDate(DateTimeUtil.getXMLGregorianCalendar(JAN_1));
		proratedAmt11.setPartialMonthEffectiveEndDate(DateTimeUtil.getXMLGregorianCalendar(JAN_15));
		proratedAmt11.setPartialMonthPremiumAmount(new BigDecimal("100"));
		proratedAmt11.setPartialMonthAPTCAmount(new BigDecimal("50"));
		proratedAmt11.setPartialMonthCSRAmount(new BigDecimal("10"));
		fit.getProratedAmount().add(proratedAmt11);
		
		ProratedAmountType proratedAmt12 = new ProratedAmountType();
		proratedAmt12.setPartialMonthEffectiveStartDate(DateTimeUtil.getXMLGregorianCalendar(JAN_15.plusDays(1)));
		proratedAmt12.setPartialMonthEffectiveEndDate(DateTimeUtil.getXMLGregorianCalendar(JAN_31));
		proratedAmt12.setPartialMonthPremiumAmount(new BigDecimal("100"));
		proratedAmt12.setPartialMonthAPTCAmount(new BigDecimal("50"));
		proratedAmt12.setPartialMonthCSRAmount(new BigDecimal("10"));
		fit.getProratedAmount().add(proratedAmt12);
		
		policy.getFinancialInformation().add(fit);
		
		List<SbmErrWarningLogDTO> financialErrors = sbmFinancialValidator.validateFinancialInfo(policy);
		
		assertFalse("financialErrors", financialErrors.isEmpty());
		assertEquals("financialErrors", 1, financialErrors.size());
		
		SbmErrWarningLogDTO financialError = financialErrors.get(0);
		
		assertEquals("ElementInError", "PartialMonthEffectiveStartDate", financialError.getElementInError());
		assertEquals("Error Code", "ER-029", financialError.getErrorWarningTypeCd());
	}
	
	/*
	 * validateFinancialInfo
	 */
	@Test
	public void test_validateProratedAmounts_R023_valid() {
		
		String qhpId = RandomStringUtils.randomAlphanumeric(16).toUpperCase();
		String stateCd = qhpId.substring(5, 7);

		List<String> bizRules = Arrays.asList("R023");
		SBMCache.getBusinessRules(stateCd).addAll(bizRules);
		
		expect(mockSbmDataService.getMetalLevelByQhpid(EasyMock.anyString(), EasyMock.anyString()))
		.andReturn(null).anyTimes();
		replay(mockSbmDataService);
		
		PolicyType policy = new PolicyType();
		policy.setPolicyStartDate(DateTimeUtil.getXMLGregorianCalendar(JAN_1));
		policy.setPolicyEndDate(DateTimeUtil.getXMLGregorianCalendar(DEC_31));
		policy.setQHPId(qhpId);
		
		//Partial Date = Fin Start
		FinancialInformation fit = new FinancialInformation();
		
		fit.setFinancialEffectiveStartDate(DateTimeUtil.getXMLGregorianCalendar(JAN_15));
		fit.setFinancialEffectiveEndDate(DateTimeUtil.getXMLGregorianCalendar(JUL_15));
		fit.setMonthlyTotalPremiumAmount(new BigDecimal("200"));
		fit.setMonthlyAPTCAmount(new BigDecimal("100"));
		fit.setMonthlyCSRAmount(new BigDecimal("20"));
		
		ProratedAmountType proratedAmt1 = new ProratedAmountType();
		proratedAmt1.setPartialMonthEffectiveStartDate(DateTimeUtil.getXMLGregorianCalendar(JAN_15));
		proratedAmt1.setPartialMonthEffectiveEndDate(DateTimeUtil.getXMLGregorianCalendar(JAN_31));
		proratedAmt1.setPartialMonthPremiumAmount(new BigDecimal("100"));
		proratedAmt1.setPartialMonthAPTCAmount(new BigDecimal("50"));
		proratedAmt1.setPartialMonthCSRAmount(new BigDecimal("10"));
		fit.getProratedAmount().add(proratedAmt1);
		
		ProratedAmountType proratedAmt11 = new ProratedAmountType();
		proratedAmt11.setPartialMonthEffectiveStartDate(DateTimeUtil.getXMLGregorianCalendar(JUL_1));
		proratedAmt11.setPartialMonthEffectiveEndDate(DateTimeUtil.getXMLGregorianCalendar(JUL_15));
		proratedAmt11.setPartialMonthPremiumAmount(new BigDecimal("100"));
		proratedAmt11.setPartialMonthAPTCAmount(new BigDecimal("50"));
		proratedAmt11.setPartialMonthCSRAmount(new BigDecimal("10"));
		fit.getProratedAmount().add(proratedAmt11);
		
		policy.getFinancialInformation().add(fit);
		
		
		FinancialInformation fit2 = new FinancialInformation();
		
		fit2.setFinancialEffectiveStartDate(DateTimeUtil.getXMLGregorianCalendar(JUL_1));
		fit2.setFinancialEffectiveEndDate(DateTimeUtil.getXMLGregorianCalendar(DEC_31));
		fit2.setMonthlyTotalPremiumAmount(new BigDecimal("100"));
		fit2.setMonthlyAPTCAmount(new BigDecimal("50"));
		fit2.setMonthlyCSRAmount(new BigDecimal("30"));
		
		ProratedAmountType proratedAmt21 = new ProratedAmountType();
		proratedAmt21.setPartialMonthEffectiveStartDate(DateTimeUtil.getXMLGregorianCalendar(DEC_1));
		proratedAmt21.setPartialMonthEffectiveEndDate(DateTimeUtil.getXMLGregorianCalendar(DEC_15));
		proratedAmt21.setPartialMonthPremiumAmount(new BigDecimal("50"));
		proratedAmt21.setPartialMonthAPTCAmount(new BigDecimal("25"));
		proratedAmt21.setPartialMonthCSRAmount(new BigDecimal("15"));
		fit2.getProratedAmount().add(proratedAmt21);
		
		policy.getFinancialInformation().add(fit2);
		
		List<SbmErrWarningLogDTO> financialErrors = sbmFinancialValidator.validateFinancialInfo(policy);
		
		assertTrue("financialErrors", financialErrors.isEmpty());
	}

	/*
	 * validateFinancialInfo
	 */
	@Test
	public void test_validateProratedAmounts_R024_invalid() {
		
		String qhpId = RandomStringUtils.randomAlphanumeric(16).toUpperCase();
		String stateCd = qhpId.substring(5, 7);

		List<String> bizRules = Arrays.asList("R024");
		SBMCache.getBusinessRules(stateCd).addAll(bizRules);
		
		expect(mockSbmDataService.getMetalLevelByQhpid(EasyMock.anyString(), EasyMock.anyString()))
		.andReturn(null).anyTimes();
		replay(mockSbmDataService);
		
		PolicyType policy = new PolicyType();
		policy.setPolicyStartDate(DateTimeUtil.getXMLGregorianCalendar(JAN_1));
		policy.setPolicyEndDate(DateTimeUtil.getXMLGregorianCalendar(DEC_15));
		policy.setQHPId(qhpId);
		
		FinancialInformation fit = new FinancialInformation();
		
		fit.setFinancialEffectiveStartDate(DateTimeUtil.getXMLGregorianCalendar(JAN_1));
		fit.setFinancialEffectiveEndDate(DateTimeUtil.getXMLGregorianCalendar(JUL_15));
		fit.setMonthlyTotalPremiumAmount(new BigDecimal("200"));
		fit.setMonthlyAPTCAmount(new BigDecimal("100"));
		fit.setMonthlyCSRAmount(new BigDecimal("20"));
		
		ProratedAmountType proratedAmt11 = new ProratedAmountType();
		proratedAmt11.setPartialMonthEffectiveStartDate(DateTimeUtil.getXMLGregorianCalendar(JAN_1));
		proratedAmt11.setPartialMonthEffectiveEndDate(DateTimeUtil.getXMLGregorianCalendar(FEB_1));
		proratedAmt11.setPartialMonthPremiumAmount(new BigDecimal("100"));
		proratedAmt11.setPartialMonthAPTCAmount(new BigDecimal("50"));
		proratedAmt11.setPartialMonthCSRAmount(new BigDecimal("10"));
		fit.getProratedAmount().add(proratedAmt11);
		
		policy.getFinancialInformation().add(fit);
		
		List<SbmErrWarningLogDTO> financialErrors = sbmFinancialValidator.validateFinancialInfo(policy);
		
		assertFalse("financialErrors", financialErrors.isEmpty());
		assertEquals("financialErrors", 1, financialErrors.size());
		
		SbmErrWarningLogDTO financialError = financialErrors.get(0);
		
		assertEquals("ElementInError", "PartialMonthEffectiveEndDate", financialError.getElementInError());
		assertEquals("Error Code", "ER-030", financialError.getErrorWarningTypeCd());
	}
	
	/*
	 * validateFinancialInfo
	 */
	@Test
	public void test_validateProratedAmounts_R024_valid() {
		
		String qhpId = RandomStringUtils.randomAlphanumeric(16).toUpperCase();
		String stateCd = qhpId.substring(5, 7);

		List<String> bizRules = Arrays.asList("R024");
		SBMCache.getBusinessRules(stateCd).addAll(bizRules);
		
		expect(mockSbmDataService.getMetalLevelByQhpid(EasyMock.anyString(), EasyMock.anyString()))
		.andReturn(null).anyTimes();
		replay(mockSbmDataService);
		
		PolicyType policy = new PolicyType();
		policy.setPolicyStartDate(DateTimeUtil.getXMLGregorianCalendar(JAN_1));
		policy.setPolicyEndDate(DateTimeUtil.getXMLGregorianCalendar(DEC_31));
		policy.setQHPId(qhpId);
		
		//Partial Date = Fin Start
		FinancialInformation fit = new FinancialInformation();
		
		fit.setFinancialEffectiveStartDate(DateTimeUtil.getXMLGregorianCalendar(JAN_15));
		fit.setFinancialEffectiveEndDate(DateTimeUtil.getXMLGregorianCalendar(JUL_15));
		fit.setMonthlyTotalPremiumAmount(new BigDecimal("200"));
		fit.setMonthlyAPTCAmount(new BigDecimal("100"));
		fit.setMonthlyCSRAmount(new BigDecimal("20"));
		
		ProratedAmountType proratedAmt1 = new ProratedAmountType();
		proratedAmt1.setPartialMonthEffectiveStartDate(DateTimeUtil.getXMLGregorianCalendar(JAN_15));
		proratedAmt1.setPartialMonthEffectiveEndDate(DateTimeUtil.getXMLGregorianCalendar(JAN_31));
		proratedAmt1.setPartialMonthPremiumAmount(new BigDecimal("100"));
		proratedAmt1.setPartialMonthAPTCAmount(new BigDecimal("50"));
		proratedAmt1.setPartialMonthCSRAmount(new BigDecimal("10"));
		fit.getProratedAmount().add(proratedAmt1);
		
		policy.getFinancialInformation().add(fit);
		
		
		FinancialInformation fit2 = new FinancialInformation();
		
		fit2.setFinancialEffectiveStartDate(DateTimeUtil.getXMLGregorianCalendar(JUL_1));
		fit2.setFinancialEffectiveEndDate(DateTimeUtil.getXMLGregorianCalendar(DEC_31));
		fit2.setMonthlyTotalPremiumAmount(new BigDecimal("100"));
		fit2.setMonthlyAPTCAmount(new BigDecimal("50"));
		fit2.setMonthlyCSRAmount(new BigDecimal("30"));
		
		ProratedAmountType proratedAmt21 = new ProratedAmountType();
		proratedAmt21.setPartialMonthEffectiveStartDate(DateTimeUtil.getXMLGregorianCalendar(DEC_1));
		proratedAmt21.setPartialMonthEffectiveEndDate(DateTimeUtil.getXMLGregorianCalendar(DEC_15));
		proratedAmt21.setPartialMonthPremiumAmount(new BigDecimal("50"));
		proratedAmt21.setPartialMonthAPTCAmount(new BigDecimal("25"));
		proratedAmt21.setPartialMonthCSRAmount(new BigDecimal("15"));
		fit2.getProratedAmount().add(proratedAmt21);
		
		policy.getFinancialInformation().add(fit2);
		
		List<SbmErrWarningLogDTO> financialErrors = sbmFinancialValidator.validateFinancialInfo(policy);
		
		assertTrue("financialErrors", financialErrors.isEmpty());
	}
	
	/*
	 * validateFinancialInfo
	 */
	@Test
	public void test_validateProratedAmounts_R025_invalid() {
		
		String qhpId = RandomStringUtils.randomAlphanumeric(16).toUpperCase();
		String stateCd = qhpId.substring(5, 7);

		List<String> bizRules = Arrays.asList("R025");
		SBMCache.getBusinessRules(stateCd).addAll(bizRules);
		
		expect(mockSbmDataService.getMetalLevelByQhpid(EasyMock.anyString(), EasyMock.anyString()))
		.andReturn(null).anyTimes();
		replay(mockSbmDataService);
		
		PolicyType policy = new PolicyType();
		policy.setPolicyStartDate(DateTimeUtil.getXMLGregorianCalendar(JAN_1));
		policy.setPolicyEndDate(DateTimeUtil.getXMLGregorianCalendar(DEC_15));
		policy.setQHPId(qhpId);
		
		FinancialInformation fit = new FinancialInformation();
		
		fit.setFinancialEffectiveStartDate(DateTimeUtil.getXMLGregorianCalendar(JAN_1));
		fit.setFinancialEffectiveEndDate(DateTimeUtil.getXMLGregorianCalendar(JUL_15));
		fit.setMonthlyTotalPremiumAmount(new BigDecimal("200"));
		fit.setMonthlyAPTCAmount(new BigDecimal("100"));
		fit.setMonthlyCSRAmount(new BigDecimal("20"));
		
		ProratedAmountType proratedAmt11 = new ProratedAmountType();
		proratedAmt11.setPartialMonthEffectiveStartDate(DateTimeUtil.getXMLGregorianCalendar(JUL_1));
		proratedAmt11.setPartialMonthEffectiveEndDate(DateTimeUtil.getXMLGregorianCalendar(JUL_15));
		proratedAmt11.setPartialMonthPremiumAmount(new BigDecimal("300"));
		proratedAmt11.setPartialMonthAPTCAmount(new BigDecimal("150"));
		proratedAmt11.setPartialMonthCSRAmount(new BigDecimal("110"));
		fit.getProratedAmount().add(proratedAmt11);
		
		policy.getFinancialInformation().add(fit);
		
		List<SbmErrWarningLogDTO> financialErrors = sbmFinancialValidator.validateFinancialInfo(policy);
		
		assertFalse("financialErrors", financialErrors.isEmpty());
		assertEquals("financialErrors", 1, financialErrors.size());
		
		SbmErrWarningLogDTO financialError = financialErrors.get(0);
		
		assertEquals("ElementInError", "PartialMonthPremiumAmount", financialError.getElementInError());
		assertEquals("Error Code", "ER-049", financialError.getErrorWarningTypeCd());
	}
	
	/*
	 * validateFinancialInfo
	 */
	@Test
	public void test_validateProratedAmounts_R025_valid() {
		
		String qhpId = RandomStringUtils.randomAlphanumeric(16).toUpperCase();
		String stateCd = qhpId.substring(5, 7);

		List<String> bizRules = Arrays.asList("R025");
		SBMCache.getBusinessRules(stateCd).addAll(bizRules);
		
		expect(mockSbmDataService.getMetalLevelByQhpid(EasyMock.anyString(), EasyMock.anyString()))
		.andReturn(null).anyTimes();
		replay(mockSbmDataService);
		
		PolicyType policy = new PolicyType();
		policy.setPolicyStartDate(DateTimeUtil.getXMLGregorianCalendar(JAN_1));
		policy.setPolicyEndDate(DateTimeUtil.getXMLGregorianCalendar(DEC_31));
		policy.setQHPId(qhpId);
		
		//Partial Date = Fin Start
		FinancialInformation fit = new FinancialInformation();
		
		fit.setFinancialEffectiveStartDate(DateTimeUtil.getXMLGregorianCalendar(JAN_15));
		fit.setFinancialEffectiveEndDate(DateTimeUtil.getXMLGregorianCalendar(JUL_15));
		fit.setMonthlyTotalPremiumAmount(new BigDecimal("200"));
		fit.setMonthlyAPTCAmount(new BigDecimal("100"));
		fit.setMonthlyCSRAmount(new BigDecimal("20"));
		
		ProratedAmountType proratedAmt1 = new ProratedAmountType();
		proratedAmt1.setPartialMonthEffectiveStartDate(DateTimeUtil.getXMLGregorianCalendar(JUL_1));
		proratedAmt1.setPartialMonthEffectiveEndDate(DateTimeUtil.getXMLGregorianCalendar(JUL_15));
		proratedAmt1.setPartialMonthPremiumAmount(new BigDecimal("100"));
		proratedAmt1.setPartialMonthAPTCAmount(new BigDecimal("50"));
		proratedAmt1.setPartialMonthCSRAmount(new BigDecimal("10"));
		fit.getProratedAmount().add(proratedAmt1);
		
		policy.getFinancialInformation().add(fit);
		
		
		FinancialInformation fit2 = new FinancialInformation();
		
		fit2.setFinancialEffectiveStartDate(DateTimeUtil.getXMLGregorianCalendar(JUL_15.plusDays(1)));
		fit2.setFinancialEffectiveEndDate(DateTimeUtil.getXMLGregorianCalendar(DEC_31));
		fit2.setMonthlyTotalPremiumAmount(new BigDecimal("100"));
		fit2.setMonthlyAPTCAmount(new BigDecimal("50"));
		fit2.setMonthlyCSRAmount(new BigDecimal("30"));
		
		ProratedAmountType proratedAmt21 = new ProratedAmountType();
		proratedAmt21.setPartialMonthEffectiveStartDate(DateTimeUtil.getXMLGregorianCalendar(JUL_15.plusDays(1)));
		proratedAmt21.setPartialMonthEffectiveEndDate(DateTimeUtil.getXMLGregorianCalendar(JUL_31));
		proratedAmt21.setPartialMonthPremiumAmount(new BigDecimal("100"));
		proratedAmt21.setPartialMonthAPTCAmount(new BigDecimal("50"));
		proratedAmt21.setPartialMonthCSRAmount(new BigDecimal("30"));
		fit2.getProratedAmount().add(proratedAmt21);
		
		policy.getFinancialInformation().add(fit2);
		
		List<SbmErrWarningLogDTO> financialErrors = sbmFinancialValidator.validateFinancialInfo(policy);
		
		assertTrue("financialErrors", financialErrors.isEmpty());
	}
	
	/*
	 * validateFinancialInfo
	 */
	@Test
	public void test_validateProratedAmounts_R028_invalid() {
		
		String qhpId = RandomStringUtils.randomAlphanumeric(16).toUpperCase();
		String stateCd = qhpId.substring(5, 7);

		List<String> bizRules = Arrays.asList("R028");
		SBMCache.getBusinessRules(stateCd).addAll(bizRules);
		
		expect(mockSbmDataService.getMetalLevelByQhpid(EasyMock.anyString(), EasyMock.anyString()))
		.andReturn(null).anyTimes();
		replay(mockSbmDataService);
		
		PolicyType policy = new PolicyType();
		policy.setPolicyStartDate(DateTimeUtil.getXMLGregorianCalendar(JAN_1));
		policy.setPolicyEndDate(DateTimeUtil.getXMLGregorianCalendar(DEC_15));
		policy.setQHPId(qhpId);
		
		FinancialInformation fit = new FinancialInformation();
		
		fit.setFinancialEffectiveStartDate(DateTimeUtil.getXMLGregorianCalendar(JAN_1));
		fit.setFinancialEffectiveEndDate(DateTimeUtil.getXMLGregorianCalendar(JUL_15));
		fit.setMonthlyTotalPremiumAmount(new BigDecimal("500"));
		fit.setMonthlyAPTCAmount(new BigDecimal("400"));
		fit.setMonthlyCSRAmount(new BigDecimal("20"));
		
		ProratedAmountType proratedAmt11 = new ProratedAmountType();
		proratedAmt11.setPartialMonthEffectiveStartDate(DateTimeUtil.getXMLGregorianCalendar(JUL_1));
		proratedAmt11.setPartialMonthEffectiveEndDate(DateTimeUtil.getXMLGregorianCalendar(JUL_15));
		proratedAmt11.setPartialMonthPremiumAmount(new BigDecimal("300"));
		//proratedAmt11.setPartialMonthAPTCAmount(new BigDecimal("150"));
		proratedAmt11.setPartialMonthCSRAmount(new BigDecimal("110"));
		fit.getProratedAmount().add(proratedAmt11);
		
		policy.getFinancialInformation().add(fit);
		
		List<SbmErrWarningLogDTO> financialErrors = sbmFinancialValidator.validateFinancialInfo(policy);
		
		assertFalse("financialErrors", financialErrors.isEmpty());
		assertEquals("financialErrors", 1, financialErrors.size());
		
		SbmErrWarningLogDTO financialError = financialErrors.get(0);
		
		assertEquals("ElementInError", "MonthlyAPTCAmount", financialError.getElementInError());
		assertEquals("Error Code", "ER-050", financialError.getErrorWarningTypeCd());
	}
	
	/*
	 * validateFinancialInfo
	 */
	@Test
	public void test_validateProratedAmounts_R028_invalid_zero_aptc() {
		
		String qhpId = RandomStringUtils.randomAlphanumeric(16).toUpperCase();
		String stateCd = qhpId.substring(5, 7);

		List<String> bizRules = Arrays.asList("R028");
		SBMCache.getBusinessRules(stateCd).addAll(bizRules);
		
		expect(mockSbmDataService.getMetalLevelByQhpid(EasyMock.anyString(), EasyMock.anyString()))
		.andReturn(null).anyTimes();
		replay(mockSbmDataService);
		
		PolicyType policy = new PolicyType();
		policy.setPolicyStartDate(DateTimeUtil.getXMLGregorianCalendar(JAN_1));
		policy.setPolicyEndDate(DateTimeUtil.getXMLGregorianCalendar(DEC_15));
		policy.setQHPId(qhpId);
		
		FinancialInformation fit = new FinancialInformation();
		
		fit.setFinancialEffectiveStartDate(DateTimeUtil.getXMLGregorianCalendar(JAN_1));
		fit.setFinancialEffectiveEndDate(DateTimeUtil.getXMLGregorianCalendar(JUL_15));
		fit.setMonthlyTotalPremiumAmount(new BigDecimal("500"));
		fit.setMonthlyAPTCAmount(new BigDecimal("400"));
		fit.setMonthlyCSRAmount(new BigDecimal("20"));
		
		ProratedAmountType proratedAmt11 = new ProratedAmountType();
		proratedAmt11.setPartialMonthEffectiveStartDate(DateTimeUtil.getXMLGregorianCalendar(JUL_1));
		proratedAmt11.setPartialMonthEffectiveEndDate(DateTimeUtil.getXMLGregorianCalendar(JUL_15));
		proratedAmt11.setPartialMonthPremiumAmount(new BigDecimal("300"));
		proratedAmt11.setPartialMonthAPTCAmount(new BigDecimal("0"));
		proratedAmt11.setPartialMonthCSRAmount(new BigDecimal("110"));
		fit.getProratedAmount().add(proratedAmt11);
		
		policy.getFinancialInformation().add(fit);
		
		List<SbmErrWarningLogDTO> financialErrors = sbmFinancialValidator.validateFinancialInfo(policy);
		
		assertFalse("financialErrors", financialErrors.isEmpty());
		assertEquals("financialErrors", 1, financialErrors.size());
		
		SbmErrWarningLogDTO financialError = financialErrors.get(0);
		
		assertEquals("ElementInError", "MonthlyAPTCAmount", financialError.getElementInError());
		assertEquals("Error Code", "ER-050", financialError.getErrorWarningTypeCd());
	}
	
	/*
	 * validateFinancialInfo
	 */
	@Test
	public void test_validateProratedAmounts_R028_valid() {
		
		String qhpId = RandomStringUtils.randomAlphanumeric(16).toUpperCase();
		String stateCd = qhpId.substring(5, 7);

		List<String> bizRules = Arrays.asList("R028");
		SBMCache.getBusinessRules(stateCd).addAll(bizRules);
		
		expect(mockSbmDataService.getMetalLevelByQhpid(EasyMock.anyString(), EasyMock.anyString()))
		.andReturn(null).anyTimes();
		replay(mockSbmDataService);
		
		PolicyType policy = new PolicyType();
		policy.setPolicyStartDate(DateTimeUtil.getXMLGregorianCalendar(JAN_1));
		policy.setPolicyEndDate(DateTimeUtil.getXMLGregorianCalendar(DEC_31));
		policy.setQHPId(qhpId);
		
		//Partial Date = Fin Start
		FinancialInformation fit = new FinancialInformation();
		
		fit.setFinancialEffectiveStartDate(DateTimeUtil.getXMLGregorianCalendar(JAN_1));
		fit.setFinancialEffectiveEndDate(DateTimeUtil.getXMLGregorianCalendar(JUL_15));
		fit.setMonthlyTotalPremiumAmount(new BigDecimal("200"));
		fit.setMonthlyAPTCAmount(new BigDecimal("100"));
		fit.setMonthlyCSRAmount(new BigDecimal("20"));
		
		ProratedAmountType proratedAmt1 = new ProratedAmountType();
		proratedAmt1.setPartialMonthEffectiveStartDate(DateTimeUtil.getXMLGregorianCalendar(JUL_1));
		proratedAmt1.setPartialMonthEffectiveEndDate(DateTimeUtil.getXMLGregorianCalendar(JUL_15));
		proratedAmt1.setPartialMonthPremiumAmount(new BigDecimal("100"));
		proratedAmt1.setPartialMonthAPTCAmount(new BigDecimal("50"));
		proratedAmt1.setPartialMonthCSRAmount(new BigDecimal("10"));
		fit.getProratedAmount().add(proratedAmt1);
		
		policy.getFinancialInformation().add(fit);
		
		
		FinancialInformation fit2 = new FinancialInformation();
		
		fit2.setFinancialEffectiveStartDate(DateTimeUtil.getXMLGregorianCalendar(JUL_15.plusDays(1)));
		fit2.setFinancialEffectiveEndDate(DateTimeUtil.getXMLGregorianCalendar(DEC_31));
		fit2.setMonthlyTotalPremiumAmount(new BigDecimal("100"));
		fit2.setMonthlyAPTCAmount(new BigDecimal("50"));
		fit2.setMonthlyCSRAmount(new BigDecimal("30"));
		
		ProratedAmountType proratedAmt21 = new ProratedAmountType();
		proratedAmt21.setPartialMonthEffectiveStartDate(DateTimeUtil.getXMLGregorianCalendar(JUL_15.plusDays(1)));
		proratedAmt21.setPartialMonthEffectiveEndDate(DateTimeUtil.getXMLGregorianCalendar(JUL_31));
		proratedAmt21.setPartialMonthPremiumAmount(new BigDecimal("100"));
		proratedAmt21.setPartialMonthAPTCAmount(new BigDecimal("50"));
		proratedAmt21.setPartialMonthCSRAmount(new BigDecimal("30"));
		fit2.getProratedAmount().add(proratedAmt21);
		
		policy.getFinancialInformation().add(fit2);
		
		List<SbmErrWarningLogDTO> financialErrors = sbmFinancialValidator.validateFinancialInfo(policy);
		
		assertTrue("financialErrors", financialErrors.isEmpty());
	}
	
	/*
	 * validateFinancialInfo
	 */
	@Test
	public void test_validateProratedAmounts_R026_invalid_zero_aptc() {
		
		String qhpId = RandomStringUtils.randomAlphanumeric(16).toUpperCase();
		String stateCd = qhpId.substring(5, 7);

		List<String> bizRules = Arrays.asList("R026");
		SBMCache.getBusinessRules(stateCd).addAll(bizRules);
		
		expect(mockSbmDataService.getMetalLevelByQhpid(EasyMock.anyString(), EasyMock.anyString()))
		.andReturn(null).anyTimes();
		replay(mockSbmDataService);
		
		PolicyType policy = new PolicyType();
		policy.setPolicyStartDate(DateTimeUtil.getXMLGregorianCalendar(JAN_1));
		policy.setPolicyEndDate(DateTimeUtil.getXMLGregorianCalendar(DEC_15));
		policy.setQHPId(qhpId);
		
		FinancialInformation fit = new FinancialInformation();
		
		fit.setFinancialEffectiveStartDate(DateTimeUtil.getXMLGregorianCalendar(JAN_1));
		fit.setFinancialEffectiveEndDate(DateTimeUtil.getXMLGregorianCalendar(JUL_15));
		fit.setMonthlyTotalPremiumAmount(new BigDecimal("500"));
		fit.setMonthlyAPTCAmount(new BigDecimal("400"));
		fit.setMonthlyCSRAmount(new BigDecimal("20"));
		
		ProratedAmountType proratedAmt11 = new ProratedAmountType();
		proratedAmt11.setPartialMonthEffectiveStartDate(DateTimeUtil.getXMLGregorianCalendar(JUL_1));
		proratedAmt11.setPartialMonthEffectiveEndDate(DateTimeUtil.getXMLGregorianCalendar(JUL_15));
		proratedAmt11.setPartialMonthPremiumAmount(new BigDecimal("500"));
		proratedAmt11.setPartialMonthAPTCAmount(new BigDecimal("500"));
		proratedAmt11.setPartialMonthCSRAmount(new BigDecimal("110"));
		fit.getProratedAmount().add(proratedAmt11);
		
		policy.getFinancialInformation().add(fit);
		
		List<SbmErrWarningLogDTO> financialErrors = sbmFinancialValidator.validateFinancialInfo(policy);
		
		assertFalse("financialErrors", financialErrors.isEmpty());
		assertEquals("financialErrors", 1, financialErrors.size());
		
		SbmErrWarningLogDTO financialError = financialErrors.get(0);
		
		assertEquals("ElementInError", "PartialMonthAPTCAmount", financialError.getElementInError());
		assertEquals("Error Code", "ER-051", financialError.getErrorWarningTypeCd());
	}
	
	/*
	 * validateFinancialInfo
	 */
	@Test
	public void test_validateProratedAmounts_R026_valid() {
		
		String qhpId = RandomStringUtils.randomAlphanumeric(16).toUpperCase();
		String stateCd = qhpId.substring(5, 7);

		List<String> bizRules = Arrays.asList("R026");
		SBMCache.getBusinessRules(stateCd).addAll(bizRules);
		
		expect(mockSbmDataService.getMetalLevelByQhpid(EasyMock.anyString(), EasyMock.anyString()))
		.andReturn(null).anyTimes();
		replay(mockSbmDataService);
		
		PolicyType policy = new PolicyType();
		policy.setPolicyStartDate(DateTimeUtil.getXMLGregorianCalendar(JAN_1));
		policy.setPolicyEndDate(DateTimeUtil.getXMLGregorianCalendar(DEC_31));
		policy.setQHPId(qhpId);
		
		//Partial Date = Fin Start
		FinancialInformation fit = new FinancialInformation();
		
		fit.setFinancialEffectiveStartDate(DateTimeUtil.getXMLGregorianCalendar(JAN_1));
		fit.setFinancialEffectiveEndDate(DateTimeUtil.getXMLGregorianCalendar(JUL_15));
		fit.setMonthlyTotalPremiumAmount(new BigDecimal("200"));
		fit.setMonthlyAPTCAmount(new BigDecimal("100"));
		fit.setMonthlyCSRAmount(new BigDecimal("20"));
		
		ProratedAmountType proratedAmt1 = new ProratedAmountType();
		proratedAmt1.setPartialMonthEffectiveStartDate(DateTimeUtil.getXMLGregorianCalendar(JUL_1));
		proratedAmt1.setPartialMonthEffectiveEndDate(DateTimeUtil.getXMLGregorianCalendar(JUL_15));
		proratedAmt1.setPartialMonthPremiumAmount(new BigDecimal("100"));
		proratedAmt1.setPartialMonthAPTCAmount(new BigDecimal("50"));
		proratedAmt1.setPartialMonthCSRAmount(new BigDecimal("10"));
		fit.getProratedAmount().add(proratedAmt1);
		
		policy.getFinancialInformation().add(fit);
		
		
		FinancialInformation fit2 = new FinancialInformation();
		
		fit2.setFinancialEffectiveStartDate(DateTimeUtil.getXMLGregorianCalendar(JUL_15.plusDays(1)));
		fit2.setFinancialEffectiveEndDate(DateTimeUtil.getXMLGregorianCalendar(DEC_31));
		fit2.setMonthlyTotalPremiumAmount(new BigDecimal("100"));
		fit2.setMonthlyAPTCAmount(new BigDecimal("50"));
		fit2.setMonthlyCSRAmount(new BigDecimal("30"));
		
		ProratedAmountType proratedAmt21 = new ProratedAmountType();
		proratedAmt21.setPartialMonthEffectiveStartDate(DateTimeUtil.getXMLGregorianCalendar(JUL_15.plusDays(1)));
		proratedAmt21.setPartialMonthEffectiveEndDate(DateTimeUtil.getXMLGregorianCalendar(JUL_31));
		proratedAmt21.setPartialMonthPremiumAmount(new BigDecimal("100"));
		proratedAmt21.setPartialMonthAPTCAmount(new BigDecimal("50"));
		proratedAmt21.setPartialMonthCSRAmount(new BigDecimal("30"));
		fit2.getProratedAmount().add(proratedAmt21);
		
		policy.getFinancialInformation().add(fit2);
		
		List<SbmErrWarningLogDTO> financialErrors = sbmFinancialValidator.validateFinancialInfo(policy);
		
		assertTrue("financialErrors", financialErrors.isEmpty());
	}
	
	/*
	 * validateFinancialInfo
	 */
	@Test
	public void test_validateProratedAmounts_R027_invalid() {
		
		String qhpId = RandomStringUtils.randomAlphanumeric(16).toUpperCase();
		String stateCd = qhpId.substring(5, 7);

		List<String> bizRules = Arrays.asList("R027");
		SBMCache.getBusinessRules(stateCd).addAll(bizRules);
		
		expect(mockSbmDataService.getMetalLevelByQhpid(EasyMock.anyString(), EasyMock.anyString()))
		.andReturn(null).anyTimes();
		replay(mockSbmDataService);
		
		PolicyType policy = new PolicyType();
		policy.setPolicyStartDate(DateTimeUtil.getXMLGregorianCalendar(JAN_1));
		policy.setPolicyEndDate(DateTimeUtil.getXMLGregorianCalendar(DEC_15));
		policy.setQHPId(qhpId);
		
		FinancialInformation fit = new FinancialInformation();
		
		fit.setFinancialEffectiveStartDate(DateTimeUtil.getXMLGregorianCalendar(JAN_1));
		fit.setFinancialEffectiveEndDate(DateTimeUtil.getXMLGregorianCalendar(JUL_15));
		fit.setMonthlyTotalPremiumAmount(new BigDecimal("500"));
		fit.setMonthlyAPTCAmount(new BigDecimal("400"));
		fit.setMonthlyCSRAmount(new BigDecimal("20"));
		
		ProratedAmountType proratedAmt11 = new ProratedAmountType();
		proratedAmt11.setPartialMonthEffectiveStartDate(DateTimeUtil.getXMLGregorianCalendar(JUL_1));
		proratedAmt11.setPartialMonthEffectiveEndDate(DateTimeUtil.getXMLGregorianCalendar(JUL_15));
		proratedAmt11.setPartialMonthPremiumAmount(new BigDecimal("300"));
		proratedAmt11.setPartialMonthAPTCAmount(new BigDecimal("350"));
		proratedAmt11.setPartialMonthCSRAmount(new BigDecimal("110"));
		fit.getProratedAmount().add(proratedAmt11);
		
		policy.getFinancialInformation().add(fit);
		
		List<SbmErrWarningLogDTO> financialErrors = sbmFinancialValidator.validateFinancialInfo(policy);
		
		assertFalse("financialErrors", financialErrors.isEmpty());
		assertEquals("financialErrors", 1, financialErrors.size());
		
		SbmErrWarningLogDTO financialError = financialErrors.get(0);
		
		assertEquals("ElementInError", "PartialMonthAPTCAmount", financialError.getElementInError());
		assertEquals("Error Code", "ER-052", financialError.getErrorWarningTypeCd());
	}
	
	/*
	 * validateFinancialInfo
	 */
	@Test
	public void test_validateProratedAmounts_R027_valid_null_aptc() {
		
		String qhpId = RandomStringUtils.randomAlphanumeric(16).toUpperCase();
		String stateCd = qhpId.substring(5, 7);

		List<String> bizRules = Arrays.asList("R027");
		SBMCache.getBusinessRules(stateCd).addAll(bizRules);
		
		expect(mockSbmDataService.getMetalLevelByQhpid(EasyMock.anyString(), EasyMock.anyString()))
		.andReturn(null).anyTimes();
		replay(mockSbmDataService);
		
		PolicyType policy = new PolicyType();
		policy.setPolicyStartDate(DateTimeUtil.getXMLGregorianCalendar(JAN_1));
		policy.setPolicyEndDate(DateTimeUtil.getXMLGregorianCalendar(DEC_15));
		policy.setQHPId(qhpId);
		
		FinancialInformation fit = new FinancialInformation();
		
		fit.setFinancialEffectiveStartDate(DateTimeUtil.getXMLGregorianCalendar(JAN_1));
		fit.setFinancialEffectiveEndDate(DateTimeUtil.getXMLGregorianCalendar(JUL_15));
		fit.setMonthlyTotalPremiumAmount(new BigDecimal("500"));
		fit.setMonthlyAPTCAmount(new BigDecimal("400"));
		fit.setMonthlyCSRAmount(new BigDecimal("20"));
		
		ProratedAmountType proratedAmt11 = new ProratedAmountType();
		proratedAmt11.setPartialMonthEffectiveStartDate(DateTimeUtil.getXMLGregorianCalendar(JUL_1));
		proratedAmt11.setPartialMonthEffectiveEndDate(DateTimeUtil.getXMLGregorianCalendar(JUL_15));
		proratedAmt11.setPartialMonthPremiumAmount(new BigDecimal("300"));
		proratedAmt11.setPartialMonthCSRAmount(new BigDecimal("110"));
		fit.getProratedAmount().add(proratedAmt11);
		
		policy.getFinancialInformation().add(fit);
		
		List<SbmErrWarningLogDTO> financialErrors = sbmFinancialValidator.validateFinancialInfo(policy);
		
		assertTrue("financialErrors", financialErrors.isEmpty());
	}
	
	/*
	 * validateFinancialInfo
	 */
	@Test
	public void test_validateProratedAmounts_R027_valid() {
		
		String qhpId = RandomStringUtils.randomAlphanumeric(16).toUpperCase();
		String stateCd = qhpId.substring(5, 7);

		List<String> bizRules = Arrays.asList("R027");
		SBMCache.getBusinessRules(stateCd).addAll(bizRules);
		
		expect(mockSbmDataService.getMetalLevelByQhpid(EasyMock.anyString(), EasyMock.anyString()))
		.andReturn(null).anyTimes();
		replay(mockSbmDataService);
		
		PolicyType policy = new PolicyType();
		policy.setPolicyStartDate(DateTimeUtil.getXMLGregorianCalendar(JAN_1));
		policy.setPolicyEndDate(DateTimeUtil.getXMLGregorianCalendar(DEC_31));
		policy.setQHPId(qhpId);
		
		FinancialInformation fit = new FinancialInformation();
		
		fit.setFinancialEffectiveStartDate(DateTimeUtil.getXMLGregorianCalendar(JAN_1));
		fit.setFinancialEffectiveEndDate(DateTimeUtil.getXMLGregorianCalendar(JUL_15));
		fit.setMonthlyTotalPremiumAmount(new BigDecimal("200"));
		fit.setMonthlyAPTCAmount(new BigDecimal("100"));
		fit.setMonthlyCSRAmount(new BigDecimal("20"));
		
		ProratedAmountType proratedAmt1 = new ProratedAmountType();
		proratedAmt1.setPartialMonthEffectiveStartDate(DateTimeUtil.getXMLGregorianCalendar(JUL_1));
		proratedAmt1.setPartialMonthEffectiveEndDate(DateTimeUtil.getXMLGregorianCalendar(JUL_15));
		proratedAmt1.setPartialMonthPremiumAmount(new BigDecimal("100"));
		proratedAmt1.setPartialMonthAPTCAmount(new BigDecimal("50"));
		proratedAmt1.setPartialMonthCSRAmount(new BigDecimal("10"));
		fit.getProratedAmount().add(proratedAmt1);
		
		policy.getFinancialInformation().add(fit);
		
		List<SbmErrWarningLogDTO> financialErrors = sbmFinancialValidator.validateFinancialInfo(policy);
		
		assertTrue("financialErrors", financialErrors.isEmpty());
	}
	
	/*
	 * validateProratedAmounts
	 */
	@Test
	public void test_validateProratedAmounts_R030_Invalid() {
		
		String qhpId = RandomStringUtils.randomAlphanumeric(16).toUpperCase();
		String stateCd = qhpId.substring(5, 7);

		List<String> bizRules = Arrays.asList("R030");
		SBMCache.getBusinessRules(stateCd).addAll(bizRules);
		
		expect(mockSbmDataService.getMetalLevelByQhpid(EasyMock.anyString(), EasyMock.anyString()))
		.andReturn(null).anyTimes();
		replay(mockSbmDataService);	
		
		ReflectionTestUtils.setField(sbmFinancialValidator, "sbmBusinessRules", bizRules);
		
		PolicyType policy = new PolicyType();
		policy.setPolicyStartDate(DateTimeUtil.getXMLGregorianCalendar(JAN_1));
		policy.setPolicyEndDate(DateTimeUtil.getXMLGregorianCalendar(JAN_31));
		policy.setQHPId(qhpId);
		policy.setEffectuationIndicator("Y");
		
		FinancialInformation fit = new FinancialInformation();
		fit.setFinancialEffectiveStartDate(DateTimeUtil.getXMLGregorianCalendar(JAN_1));
		fit.setFinancialEffectiveEndDate(DateTimeUtil.getXMLGregorianCalendar(JAN_31));
		fit.setMonthlyTotalPremiumAmount(new BigDecimal("450.00"));
		fit.setMonthlyTotalIndividualResponsibilityAmount(new BigDecimal("400.00"));
		fit.setMonthlyCSRAmount(new BigDecimal("400.00"));
		fit.setCSRVariantId("00");
		
		ProratedAmountType proratedAmt1 = new ProratedAmountType();
		proratedAmt1.setPartialMonthEffectiveStartDate(DateTimeUtil.getXMLGregorianCalendar(JAN_1));
		proratedAmt1.setPartialMonthEffectiveEndDate(DateTimeUtil.getXMLGregorianCalendar(JAN_15));
		proratedAmt1.setPartialMonthPremiumAmount(new BigDecimal("100"));
		proratedAmt1.setPartialMonthCSRAmount(new BigDecimal("10"));
		fit.getProratedAmount().add(proratedAmt1);
		
		policy.getFinancialInformation().add(fit);
		
		FinancialInformation fit2 = new FinancialInformation();
		fit2.setFinancialEffectiveStartDate(DateTimeUtil.getXMLGregorianCalendar(FEB_1));
		fit2.setFinancialEffectiveEndDate(DateTimeUtil.getXMLGregorianCalendar(APR_30));
		fit2.setMonthlyTotalPremiumAmount(new BigDecimal("450.00"));
		fit2.setMonthlyTotalIndividualResponsibilityAmount(new BigDecimal("400.00"));
		fit2.setMonthlyCSRAmount(new BigDecimal("500.00"));
		fit2.setCSRVariantId("01");
		
		ProratedAmountType proratedAmt2 = new ProratedAmountType();
		proratedAmt2.setPartialMonthEffectiveStartDate(DateTimeUtil.getXMLGregorianCalendar(FEB_1));
		proratedAmt2.setPartialMonthEffectiveEndDate(DateTimeUtil.getXMLGregorianCalendar(FEB_15));
		proratedAmt2.setPartialMonthPremiumAmount(new BigDecimal("100"));
		proratedAmt2.setPartialMonthAPTCAmount(new BigDecimal("50"));
		proratedAmt2.setPartialMonthCSRAmount(new BigDecimal("10"));
		fit2.getProratedAmount().add(proratedAmt2);
		
		policy.getFinancialInformation().add(fit2);
		
		List<SbmErrWarningLogDTO> financialErrors = sbmFinancialValidator.validateFinancialInfo(policy);
		
		assertFalse("financialErrors", financialErrors.isEmpty());
		assertEquals("financialErrors", 2, financialErrors.size());
		
		SbmErrWarningLogDTO financialError = financialErrors.get(0);
		
		assertEquals("ElementInError", "PartialMonthCSRAmount", financialError.getElementInError());
		assertEquals("Error Code", "ER-053", financialError.getErrorWarningTypeCd());
	}
	
	/*
	 * validateProratedAmounts
	 */
	@Test
	public void test_validateProratedAmounts_R030_Valid() {
		
		String qhpId = RandomStringUtils.randomAlphanumeric(16).toUpperCase();
		String stateCd = qhpId.substring(5, 7);

		List<String> bizRules = Arrays.asList("R030");
		SBMCache.getBusinessRules(stateCd).addAll(bizRules);
		
		expect(mockSbmDataService.getMetalLevelByQhpid(EasyMock.anyString(), EasyMock.anyString()))
		.andReturn(null).anyTimes();
		replay(mockSbmDataService);	
		
		PolicyType policy = new PolicyType();
		policy.setPolicyStartDate(DateTimeUtil.getXMLGregorianCalendar(JAN_1));
		policy.setPolicyEndDate(DateTimeUtil.getXMLGregorianCalendar(APR_30));
		policy.setQHPId(qhpId);
		policy.setEffectuationIndicator("Y");
		
		FinancialInformation fit = new FinancialInformation();
		fit.setFinancialEffectiveStartDate(DateTimeUtil.getXMLGregorianCalendar(JAN_1));
		fit.setFinancialEffectiveEndDate(DateTimeUtil.getXMLGregorianCalendar(JAN_31));
		fit.setMonthlyTotalPremiumAmount(new BigDecimal("450.00"));
		fit.setMonthlyTotalIndividualResponsibilityAmount(new BigDecimal("450.00"));
		fit.setCSRVariantId("02");
		
		ProratedAmountType proratedAmt1 = new ProratedAmountType();
		proratedAmt1.setPartialMonthEffectiveStartDate(DateTimeUtil.getXMLGregorianCalendar(JAN_1));
		proratedAmt1.setPartialMonthEffectiveEndDate(DateTimeUtil.getXMLGregorianCalendar(JAN_15));
		proratedAmt1.setPartialMonthPremiumAmount(new BigDecimal("100"));
		proratedAmt1.setPartialMonthCSRAmount(new BigDecimal("10"));
		fit.getProratedAmount().add(proratedAmt1);
		
		policy.getFinancialInformation().add(fit);
		
		FinancialInformation fit2 = new FinancialInformation();
		fit2.setFinancialEffectiveStartDate(DateTimeUtil.getXMLGregorianCalendar(FEB_1));
		fit2.setFinancialEffectiveEndDate(DateTimeUtil.getXMLGregorianCalendar(APR_30));
		fit2.setMonthlyTotalPremiumAmount(new BigDecimal("500.00"));
		fit2.setMonthlyTotalIndividualResponsibilityAmount(new BigDecimal("50.00"));
		fit2.setMonthlyCSRAmount(new BigDecimal("400.00"));
		fit2.setCSRVariantId("06");
		
		ProratedAmountType proratedAmt2 = new ProratedAmountType();
		proratedAmt2.setPartialMonthEffectiveStartDate(DateTimeUtil.getXMLGregorianCalendar(FEB_1));
		proratedAmt2.setPartialMonthEffectiveEndDate(DateTimeUtil.getXMLGregorianCalendar(FEB_15));
		proratedAmt2.setPartialMonthPremiumAmount(new BigDecimal("100"));
		proratedAmt2.setPartialMonthAPTCAmount(new BigDecimal("50"));
		proratedAmt2.setPartialMonthCSRAmount(new BigDecimal("10"));
		fit2.getProratedAmount().add(proratedAmt2);
		
		policy.getFinancialInformation().add(fit2);
		
		List<SbmErrWarningLogDTO> financialErrors = sbmFinancialValidator.validateFinancialInfo(policy);
		
		assertTrue("financialErrors", financialErrors.isEmpty());
	}
	
	/*
	 * validateProratedAmounts
	 */
	@Test
	public void test_validateProratedAmounts_R030_valid_ProratedCsrNull() {
		
		String qhpId = RandomStringUtils.randomAlphanumeric(16).toUpperCase();
		String stateCd = qhpId.substring(5, 7);

		List<String> bizRules = Arrays.asList("R030");
		SBMCache.getBusinessRules(stateCd).addAll(bizRules);
		
		expect(mockSbmDataService.getMetalLevelByQhpid(EasyMock.anyString(), EasyMock.anyString()))
		.andReturn(null).anyTimes();
		replay(mockSbmDataService);	
		
		ReflectionTestUtils.setField(sbmFinancialValidator, "sbmBusinessRules", bizRules);
		
		PolicyType policy = new PolicyType();
		policy.setPolicyStartDate(DateTimeUtil.getXMLGregorianCalendar(JAN_1));
		policy.setPolicyEndDate(DateTimeUtil.getXMLGregorianCalendar(JAN_31));
		policy.setQHPId(qhpId);
		policy.setEffectuationIndicator("Y");
		
		FinancialInformation fit = new FinancialInformation();
		fit.setFinancialEffectiveStartDate(DateTimeUtil.getXMLGregorianCalendar(JAN_1));
		fit.setFinancialEffectiveEndDate(DateTimeUtil.getXMLGregorianCalendar(JAN_31));
		fit.setMonthlyTotalPremiumAmount(new BigDecimal("450.00"));
		fit.setMonthlyTotalIndividualResponsibilityAmount(new BigDecimal("400.00"));
		fit.setMonthlyCSRAmount(new BigDecimal("00.00"));
		fit.setCSRVariantId("00");
		
		ProratedAmountType proratedAmt1 = new ProratedAmountType();
		proratedAmt1.setPartialMonthEffectiveStartDate(DateTimeUtil.getXMLGregorianCalendar(JAN_1));
		proratedAmt1.setPartialMonthEffectiveEndDate(DateTimeUtil.getXMLGregorianCalendar(JAN_15));
		proratedAmt1.setPartialMonthPremiumAmount(new BigDecimal("100"));
		proratedAmt1.setPartialMonthCSRAmount(new BigDecimal("0"));
		fit.getProratedAmount().add(proratedAmt1);
		
		policy.getFinancialInformation().add(fit);
		
		FinancialInformation fit2 = new FinancialInformation();
		fit2.setFinancialEffectiveStartDate(DateTimeUtil.getXMLGregorianCalendar(FEB_1));
		fit2.setFinancialEffectiveEndDate(DateTimeUtil.getXMLGregorianCalendar(APR_30));
		fit2.setMonthlyTotalPremiumAmount(new BigDecimal("450.00"));
		fit2.setMonthlyTotalIndividualResponsibilityAmount(new BigDecimal("400.00"));
		fit2.setMonthlyCSRAmount(new BigDecimal("500.00"));
		fit2.setCSRVariantId("01");
		
		ProratedAmountType proratedAmt2 = new ProratedAmountType();
		proratedAmt2.setPartialMonthEffectiveStartDate(DateTimeUtil.getXMLGregorianCalendar(FEB_1));
		proratedAmt2.setPartialMonthEffectiveEndDate(DateTimeUtil.getXMLGregorianCalendar(FEB_15));
		proratedAmt2.setPartialMonthPremiumAmount(new BigDecimal("100"));
		proratedAmt2.setPartialMonthAPTCAmount(new BigDecimal("50"));
		//proratedAmt2.setPartialMonthCSRAmount(new BigDecimal("10"));
		fit2.getProratedAmount().add(proratedAmt2);
		
		policy.getFinancialInformation().add(fit2);
		
		List<SbmErrWarningLogDTO> financialErrors = sbmFinancialValidator.validateFinancialInfo(policy);
		
		assertTrue("financialErrors", financialErrors.isEmpty());
	}
	
	/*
	 * validateProratedAmounts
	 */
	@Test
	public void test_validateProratedAmounts_R032_Warning_Variant_04_05_06() {

		BigDecimal csrMultiplier = new BigDecimal("1.11");
		
		String qhpId = RandomStringUtils.randomAlphanumeric(16).toUpperCase();
		String stateCd = qhpId.substring(5, 7);

		List<String> bizRules = Arrays.asList("R032");
		SBMCache.getBusinessRules(stateCd).addAll(bizRules);
		
		expect(mockSbmDataService.getMetalLevelByQhpid(EasyMock.anyString(), EasyMock.anyString()))
		.andReturn(SbmMetalLevelType.SILVER.getValue()).anyTimes();
		expect(mockSbmDataService.getCsrMultiplierByVariant(EasyMock.anyString(), EasyMock.anyString()))
		.andReturn(csrMultiplier).anyTimes();
		replay(mockSbmDataService);	
		
		PolicyType policy = new PolicyType();
		policy.setPolicyStartDate(DateTimeUtil.getXMLGregorianCalendar(JAN_1));
		policy.setPolicyEndDate(DateTimeUtil.getXMLGregorianCalendar(APR_30));
		policy.setQHPId(qhpId);
		policy.setEffectuationIndicator("Y");
		
		FinancialInformation fit = new FinancialInformation();
		fit.setFinancialEffectiveStartDate(DateTimeUtil.getXMLGregorianCalendar(JAN_15));
		fit.setFinancialEffectiveEndDate(DateTimeUtil.getXMLGregorianCalendar(JAN_31));
		fit.setMonthlyTotalPremiumAmount(new BigDecimal("450.00"));
		fit.setMonthlyTotalIndividualResponsibilityAmount(new BigDecimal("400.00"));
		fit.setMonthlyCSRAmount(new BigDecimal("400.00"));
		fit.setCSRVariantId("04");
		
		ProratedAmountType proratedAmt = new ProratedAmountType();
		proratedAmt.setPartialMonthEffectiveStartDate(DateTimeUtil.getXMLGregorianCalendar(JAN_15));
		proratedAmt.setPartialMonthEffectiveEndDate(DateTimeUtil.getXMLGregorianCalendar(JAN_31));
		proratedAmt.setPartialMonthPremiumAmount(new BigDecimal("250"));
		proratedAmt.setPartialMonthCSRAmount(new BigDecimal("200"));
		fit.getProratedAmount().add(proratedAmt);
		
		policy.getFinancialInformation().add(fit);
		
		FinancialInformation fit2 = new FinancialInformation();
		fit2.setFinancialEffectiveStartDate(DateTimeUtil.getXMLGregorianCalendar(FEB_1));
		fit2.setFinancialEffectiveEndDate(DateTimeUtil.getXMLGregorianCalendar(MAR_1.minusDays(1)));
		fit2.setMonthlyTotalPremiumAmount(new BigDecimal("450.50"));
		fit2.setMonthlyTotalIndividualResponsibilityAmount(new BigDecimal("400.00"));
		fit2.setMonthlyCSRAmount(new BigDecimal("400.50"));
		fit2.setCSRVariantId("05");
		
		ProratedAmountType proratedAmt2 = new ProratedAmountType();
		proratedAmt2.setPartialMonthEffectiveStartDate(DateTimeUtil.getXMLGregorianCalendar(FEB_1));
		proratedAmt2.setPartialMonthEffectiveEndDate(DateTimeUtil.getXMLGregorianCalendar(FEB_15));
		proratedAmt2.setPartialMonthPremiumAmount(new BigDecimal("250.50"));
		proratedAmt2.setPartialMonthCSRAmount(new BigDecimal("200.00"));
		fit2.getProratedAmount().add(proratedAmt2);
		
		policy.getFinancialInformation().add(fit2);
		
		FinancialInformation fit3 = new FinancialInformation();
		fit3.setFinancialEffectiveStartDate(DateTimeUtil.getXMLGregorianCalendar(MAR_1));
		fit3.setFinancialEffectiveEndDate(DateTimeUtil.getXMLGregorianCalendar(MAR_31));
		fit3.setMonthlyTotalPremiumAmount(new BigDecimal("450.89"));
		fit3.setMonthlyTotalIndividualResponsibilityAmount(new BigDecimal("400.00"));
		fit3.setMonthlyCSRAmount(new BigDecimal("400.89"));
		fit3.setCSRVariantId("06");
		
		ProratedAmountType proratedAmt3 = new ProratedAmountType();
		proratedAmt3.setPartialMonthEffectiveStartDate(DateTimeUtil.getXMLGregorianCalendar(MAR_1));
		proratedAmt3.setPartialMonthEffectiveEndDate(DateTimeUtil.getXMLGregorianCalendar(MAR_15));
		proratedAmt3.setPartialMonthPremiumAmount(new BigDecimal("250.89"));
		proratedAmt3.setPartialMonthCSRAmount(new BigDecimal("200.89"));
		fit3.getProratedAmount().add(proratedAmt3);
		
		policy.getFinancialInformation().add(fit3);
		
		List<SbmErrWarningLogDTO> financialErrors = sbmFinancialValidator.validateFinancialInfo(policy);
		
		assertFalse("financialErrors", financialErrors.isEmpty());
		assertEquals("financialErrors", 3, financialErrors.size());
		
		SbmErrWarningLogDTO financialFinWarning = financialErrors.get(0);
		
		assertEquals("ElementInError", "PartialMonthCSRAmount", financialFinWarning.getElementInError());
		assertEquals("Error Code", "WR-008", financialFinWarning.getErrorWarningTypeCd());
		
		assertEquals("fit1 CSR", 
				fit.getProratedAmount().get(0).getPartialMonthPremiumAmount().multiply(csrMultiplier).setScale(2, RoundingMode.HALF_UP), 
				fit.getProratedAmount().get(0).getPartialMonthCSRAmount());
		
		assertEquals("fit2 CSR", 
				fit2.getProratedAmount().get(0).getPartialMonthPremiumAmount().multiply(csrMultiplier).setScale(2, RoundingMode.HALF_UP), 
				fit2.getProratedAmount().get(0).getPartialMonthCSRAmount());
		
		assertEquals("fit3 CSR", 
				fit3.getProratedAmount().get(0).getPartialMonthPremiumAmount().multiply(csrMultiplier).setScale(2, RoundingMode.HALF_UP), 
				fit3.getProratedAmount().get(0).getPartialMonthCSRAmount());
	}
	
	/*
	 * validateProratedAmounts
	 */
	@Test
	public void test_validateProratedAmounts_R032_Warning_Variant_04_05_06_Non_SilverPlan() {

		BigDecimal csrMultiplier = new BigDecimal("1.11");
		
		String qhpId = RandomStringUtils.randomAlphanumeric(16).toUpperCase();
		String stateCd = qhpId.substring(5, 7);

		List<String> bizRules = Arrays.asList("R032");
		SBMCache.getBusinessRules(stateCd).addAll(bizRules);
		
		expect(mockSbmDataService.getMetalLevelByQhpid(EasyMock.anyString(), EasyMock.anyString()))
		.andReturn(SbmMetalLevelType.GOLD.getValue()).anyTimes();
		expect(mockSbmDataService.getCsrMultiplierByVariant(EasyMock.anyString(), EasyMock.anyString()))
		.andReturn(csrMultiplier).anyTimes();
		replay(mockSbmDataService);	
		
		PolicyType policy = new PolicyType();
		policy.setPolicyStartDate(DateTimeUtil.getXMLGregorianCalendar(JAN_1));
		policy.setPolicyEndDate(DateTimeUtil.getXMLGregorianCalendar(APR_30));
		policy.setQHPId(qhpId);
		policy.setEffectuationIndicator("Y");
		
		FinancialInformation fit = new FinancialInformation();
		fit.setFinancialEffectiveStartDate(DateTimeUtil.getXMLGregorianCalendar(JAN_15));
		fit.setFinancialEffectiveEndDate(DateTimeUtil.getXMLGregorianCalendar(JAN_31));
		fit.setMonthlyTotalPremiumAmount(new BigDecimal("450.00"));
		fit.setMonthlyTotalIndividualResponsibilityAmount(new BigDecimal("400.00"));
		fit.setMonthlyCSRAmount(new BigDecimal("400.00"));
		fit.setCSRVariantId("04");
		
		ProratedAmountType proratedAmt = new ProratedAmountType();
		proratedAmt.setPartialMonthEffectiveStartDate(DateTimeUtil.getXMLGregorianCalendar(JAN_15));
		proratedAmt.setPartialMonthEffectiveEndDate(DateTimeUtil.getXMLGregorianCalendar(JAN_31));
		proratedAmt.setPartialMonthPremiumAmount(new BigDecimal("250"));
		proratedAmt.setPartialMonthCSRAmount(new BigDecimal("200"));
		fit.getProratedAmount().add(proratedAmt);
		
		policy.getFinancialInformation().add(fit);
		
		FinancialInformation fit2 = new FinancialInformation();
		fit2.setFinancialEffectiveStartDate(DateTimeUtil.getXMLGregorianCalendar(FEB_1));
		fit2.setFinancialEffectiveEndDate(DateTimeUtil.getXMLGregorianCalendar(MAR_1.minusDays(1)));
		fit2.setMonthlyTotalPremiumAmount(new BigDecimal("450.50"));
		fit2.setMonthlyTotalIndividualResponsibilityAmount(new BigDecimal("400.00"));
		fit2.setMonthlyCSRAmount(new BigDecimal("400.50"));
		fit2.setCSRVariantId("05");
		
		ProratedAmountType proratedAmt2 = new ProratedAmountType();
		proratedAmt2.setPartialMonthEffectiveStartDate(DateTimeUtil.getXMLGregorianCalendar(FEB_1));
		proratedAmt2.setPartialMonthEffectiveEndDate(DateTimeUtil.getXMLGregorianCalendar(FEB_15));
		proratedAmt2.setPartialMonthPremiumAmount(new BigDecimal("250.50"));
		proratedAmt2.setPartialMonthCSRAmount(new BigDecimal("200.00"));
		fit2.getProratedAmount().add(proratedAmt2);
		
		policy.getFinancialInformation().add(fit2);
		
		FinancialInformation fit3 = new FinancialInformation();
		fit3.setFinancialEffectiveStartDate(DateTimeUtil.getXMLGregorianCalendar(MAR_1));
		fit3.setFinancialEffectiveEndDate(DateTimeUtil.getXMLGregorianCalendar(MAR_31));
		fit3.setMonthlyTotalPremiumAmount(new BigDecimal("450.89"));
		fit3.setMonthlyTotalIndividualResponsibilityAmount(new BigDecimal("400.00"));
		fit3.setMonthlyCSRAmount(new BigDecimal("400.89"));
		fit3.setCSRVariantId("06");
		
		ProratedAmountType proratedAmt3 = new ProratedAmountType();
		proratedAmt3.setPartialMonthEffectiveStartDate(DateTimeUtil.getXMLGregorianCalendar(MAR_1));
		proratedAmt3.setPartialMonthEffectiveEndDate(DateTimeUtil.getXMLGregorianCalendar(MAR_15));
		proratedAmt3.setPartialMonthPremiumAmount(new BigDecimal("250.89"));
		proratedAmt3.setPartialMonthCSRAmount(new BigDecimal("200.89"));
		fit3.getProratedAmount().add(proratedAmt3);
		
		policy.getFinancialInformation().add(fit3);
		
		List<SbmErrWarningLogDTO> financialErrors = sbmFinancialValidator.validateFinancialInfo(policy);
		
		assertFalse("financialErrors", financialErrors.isEmpty());
		assertEquals("financialErrors", 6, financialErrors.size());
		
		SbmErrWarningLogDTO financialError = financialErrors.get(0);
		assertEquals("ElementInError", "QHPId", financialError.getElementInError());
		assertEquals("Error Code", "ER-042", financialError.getErrorWarningTypeCd());
		
		SbmErrWarningLogDTO financialFinWarning = financialErrors.get(3);
		
		assertEquals("ElementInError", "PartialMonthCSRAmount", financialFinWarning.getElementInError());
		assertEquals("Error Code", "WR-008", financialFinWarning.getErrorWarningTypeCd());
		
		assertEquals("fit1 CSR", 
				fit.getProratedAmount().get(0).getPartialMonthPremiumAmount().multiply(csrMultiplier).setScale(2, RoundingMode.HALF_UP), 
				fit.getProratedAmount().get(0).getPartialMonthCSRAmount());
		
		assertEquals("fit2 CSR", 
				fit2.getProratedAmount().get(0).getPartialMonthPremiumAmount().multiply(csrMultiplier).setScale(2, RoundingMode.HALF_UP), 
				fit2.getProratedAmount().get(0).getPartialMonthCSRAmount());
		
		assertEquals("fit3 CSR", 
				fit3.getProratedAmount().get(0).getPartialMonthPremiumAmount().multiply(csrMultiplier).setScale(2, RoundingMode.HALF_UP), 
				fit3.getProratedAmount().get(0).getPartialMonthCSRAmount());
	}
	
	/*
	 * validateProratedAmounts
	 */
	@Test
	public void test_validateProratedAmounts_R032_Warning_Variant_02_03() {

		BigDecimal csrMultiplier = new BigDecimal("1.11");
		
		String qhpId = RandomStringUtils.randomAlphanumeric(16).toUpperCase();
		String stateCd = qhpId.substring(5, 7);

		List<String> bizRules = Arrays.asList("R032");
		SBMCache.getBusinessRules(stateCd).addAll(bizRules);
		
		expect(mockSbmDataService.getMetalLevelByQhpid(EasyMock.anyString(), EasyMock.anyString()))
		.andReturn(SbmMetalLevelType.GOLD.getValue()).anyTimes();
		expect(mockSbmDataService.getCsrMultiplierByVariantAndMetal(EasyMock.anyString(), EasyMock.anyString(), EasyMock.anyString()))
		.andReturn(csrMultiplier).anyTimes();
		replay(mockSbmDataService);	
		
		PolicyType policy = new PolicyType();
		policy.setPolicyStartDate(DateTimeUtil.getXMLGregorianCalendar(JAN_1));
		policy.setPolicyEndDate(DateTimeUtil.getXMLGregorianCalendar(APR_30));
		policy.setQHPId(qhpId);
		policy.setEffectuationIndicator("Y");
		
		FinancialInformation fit = new FinancialInformation();
		fit.setFinancialEffectiveStartDate(DateTimeUtil.getXMLGregorianCalendar(JAN_1));
		fit.setFinancialEffectiveEndDate(DateTimeUtil.getXMLGregorianCalendar(JAN_31));
		fit.setMonthlyTotalPremiumAmount(new BigDecimal("450.00"));
		fit.setMonthlyTotalIndividualResponsibilityAmount(new BigDecimal("400.00"));
		fit.setMonthlyCSRAmount(new BigDecimal("400.00"));
		fit.setCSRVariantId("02");
		
		ProratedAmountType proratedAmt = new ProratedAmountType();
		proratedAmt.setPartialMonthEffectiveStartDate(DateTimeUtil.getXMLGregorianCalendar(JAN_15));
		proratedAmt.setPartialMonthEffectiveEndDate(DateTimeUtil.getXMLGregorianCalendar(JAN_31));
		proratedAmt.setPartialMonthPremiumAmount(new BigDecimal("250"));
		proratedAmt.setPartialMonthCSRAmount(new BigDecimal("200"));
		fit.getProratedAmount().add(proratedAmt);
		
		policy.getFinancialInformation().add(fit);
		
		FinancialInformation fit2 = new FinancialInformation();
		fit2.setFinancialEffectiveStartDate(DateTimeUtil.getXMLGregorianCalendar(FEB_1));
		fit2.setFinancialEffectiveEndDate(DateTimeUtil.getXMLGregorianCalendar(MAR_1.minusDays(1)));
		fit2.setMonthlyTotalPremiumAmount(new BigDecimal("450.50"));
		fit2.setMonthlyTotalIndividualResponsibilityAmount(new BigDecimal("400.00"));
		fit2.setMonthlyCSRAmount(new BigDecimal("400.50"));
		fit2.setCSRVariantId("03");
		
		ProratedAmountType proratedAmt2 = new ProratedAmountType();
		proratedAmt2.setPartialMonthEffectiveStartDate(DateTimeUtil.getXMLGregorianCalendar(FEB_1));
		proratedAmt2.setPartialMonthEffectiveEndDate(DateTimeUtil.getXMLGregorianCalendar(FEB_15));
		proratedAmt2.setPartialMonthPremiumAmount(new BigDecimal("250.50"));
		proratedAmt2.setPartialMonthCSRAmount(new BigDecimal("200.00"));
		fit2.getProratedAmount().add(proratedAmt2);
		
		policy.getFinancialInformation().add(fit2);
		
		List<SbmErrWarningLogDTO> financialErrors = sbmFinancialValidator.validateFinancialInfo(policy);
		
		assertFalse("financialErrors", financialErrors.isEmpty());
		assertEquals("financialErrors", 2, financialErrors.size());
		
		SbmErrWarningLogDTO financialFinWarning = financialErrors.get(0);
		
		assertEquals("ElementInError", "PartialMonthCSRAmount", financialFinWarning.getElementInError());
		assertEquals("Error Code", "WR-008", financialFinWarning.getErrorWarningTypeCd());
		
		assertEquals("fit1 CSR", 
				fit.getProratedAmount().get(0).getPartialMonthPremiumAmount().multiply(csrMultiplier).setScale(2, RoundingMode.HALF_UP), 
				fit.getProratedAmount().get(0).getPartialMonthCSRAmount());
		
		assertEquals("fit2 CSR", 
				fit2.getProratedAmount().get(0).getPartialMonthPremiumAmount().multiply(csrMultiplier).setScale(2, RoundingMode.HALF_UP), 
				fit2.getProratedAmount().get(0).getPartialMonthCSRAmount());
	}
	
	/*
	 * validateProratedAmounts
	 */
	@Test
	public void test_validateProratedAmounts_R032_No_Warning() {

		BigDecimal csrMultiplier = new BigDecimal("1.11");
		
		String qhpId = RandomStringUtils.randomAlphanumeric(16).toUpperCase();
		String stateCd = qhpId.substring(5, 7);

		List<String> bizRules = Arrays.asList("R032");
		SBMCache.getBusinessRules(stateCd).addAll(bizRules);
		
		expect(mockSbmDataService.getMetalLevelByQhpid(EasyMock.anyString(), EasyMock.anyString()))
		.andReturn(SbmMetalLevelType.SILVER.getValue()).anyTimes();
		expect(mockSbmDataService.getCsrMultiplierByVariant(EasyMock.anyString(), EasyMock.anyString()))
		.andReturn(csrMultiplier).anyTimes();
		expect(mockSbmDataService.getCsrMultiplierByVariantAndMetal(EasyMock.anyString(), EasyMock.anyString(), EasyMock.anyString()))
		.andReturn(csrMultiplier).anyTimes();
		replay(mockSbmDataService);	
		
		PolicyType policy = new PolicyType();
		policy.setPolicyStartDate(DateTimeUtil.getXMLGregorianCalendar(JAN_1));
		policy.setPolicyEndDate(DateTimeUtil.getXMLGregorianCalendar(APR_30));
		policy.setQHPId(qhpId);
		policy.setEffectuationIndicator("Y");
		
		FinancialInformation fit = new FinancialInformation();
		fit.setFinancialEffectiveStartDate(DateTimeUtil.getXMLGregorianCalendar(JAN_1));
		fit.setFinancialEffectiveEndDate(DateTimeUtil.getXMLGregorianCalendar(JAN_31));
		fit.setMonthlyTotalPremiumAmount(new BigDecimal("450.00"));
		fit.setMonthlyTotalIndividualResponsibilityAmount(new BigDecimal("400.00"));
		fit.setMonthlyCSRAmount(new BigDecimal("499.50"));
		fit.setCSRVariantId("02");
		
		ProratedAmountType proratedAmt = new ProratedAmountType();
		proratedAmt.setPartialMonthEffectiveStartDate(DateTimeUtil.getXMLGregorianCalendar(JAN_15));
		proratedAmt.setPartialMonthEffectiveEndDate(DateTimeUtil.getXMLGregorianCalendar(JAN_31));
		proratedAmt.setPartialMonthPremiumAmount(new BigDecimal("250"));
		proratedAmt.setPartialMonthCSRAmount(new BigDecimal("277.50"));
		fit.getProratedAmount().add(proratedAmt);
		
		policy.getFinancialInformation().add(fit);
		
		FinancialInformation fit2 = new FinancialInformation();
		fit2.setFinancialEffectiveStartDate(DateTimeUtil.getXMLGregorianCalendar(FEB_1));
		fit2.setFinancialEffectiveEndDate(DateTimeUtil.getXMLGregorianCalendar(MAR_1.minusDays(1)));
		fit2.setMonthlyTotalPremiumAmount(new BigDecimal("450.50"));
		fit2.setMonthlyTotalIndividualResponsibilityAmount(new BigDecimal("400.00"));
		fit2.setMonthlyCSRAmount(new BigDecimal("500.06"));
		fit2.setCSRVariantId("04");
		
		ProratedAmountType proratedAmt2 = new ProratedAmountType();
		proratedAmt2.setPartialMonthEffectiveStartDate(DateTimeUtil.getXMLGregorianCalendar(FEB_1));
		proratedAmt2.setPartialMonthEffectiveEndDate(DateTimeUtil.getXMLGregorianCalendar(FEB_15));
		proratedAmt2.setPartialMonthPremiumAmount(new BigDecimal("250.50"));
		proratedAmt2.setPartialMonthCSRAmount(new BigDecimal("278.06"));
		fit2.getProratedAmount().add(proratedAmt2);
		
		policy.getFinancialInformation().add(fit2);
		
		List<SbmErrWarningLogDTO> financialErrors = sbmFinancialValidator.validateFinancialInfo(policy);
		
		assertTrue("stateFinErrors", financialErrors.isEmpty());
		
		assertEquals("fit1 CSR", 
				fit.getProratedAmount().get(0).getPartialMonthPremiumAmount().multiply(csrMultiplier).setScale(2, RoundingMode.HALF_UP), 
				fit.getProratedAmount().get(0).getPartialMonthCSRAmount());
		
		assertEquals("fit2 CSR", 
				fit2.getProratedAmount().get(0).getPartialMonthPremiumAmount().multiply(csrMultiplier).setScale(2, RoundingMode.HALF_UP), 
				fit2.getProratedAmount().get(0).getPartialMonthCSRAmount());
	}
	
	/*
	 * validateProratedAmounts
	 */
	@Test
	public void test_validateProratedAmounts_R032_No_Warning_NonCsrVariant() {
		
		String qhpId = RandomStringUtils.randomAlphanumeric(16).toUpperCase();
		String stateCd = qhpId.substring(5, 7);

		List<String> bizRules = Arrays.asList("R032");
		SBMCache.getBusinessRules(stateCd).addAll(bizRules);
		
		expect(mockSbmDataService.getMetalLevelByQhpid(EasyMock.anyString(), EasyMock.anyString()))
		.andReturn(SbmMetalLevelType.SILVER.getValue()).anyTimes();
		replay(mockSbmDataService);	
		
		PolicyType policy = new PolicyType();
		policy.setPolicyStartDate(DateTimeUtil.getXMLGregorianCalendar(JAN_1));
		policy.setPolicyEndDate(DateTimeUtil.getXMLGregorianCalendar(APR_30));
		policy.setQHPId(qhpId);
		policy.setEffectuationIndicator("Y");
		
		FinancialInformation fit = new FinancialInformation();
		fit.setFinancialEffectiveStartDate(DateTimeUtil.getXMLGregorianCalendar(JAN_1));
		fit.setFinancialEffectiveEndDate(DateTimeUtil.getXMLGregorianCalendar(JAN_31));
		fit.setMonthlyTotalPremiumAmount(new BigDecimal("450.00"));
		fit.setMonthlyTotalIndividualResponsibilityAmount(new BigDecimal("450.00"));
		fit.setCSRVariantId("01");
		
		ProratedAmountType proratedAmt = new ProratedAmountType();
		proratedAmt.setPartialMonthEffectiveStartDate(DateTimeUtil.getXMLGregorianCalendar(JAN_15));
		proratedAmt.setPartialMonthEffectiveEndDate(DateTimeUtil.getXMLGregorianCalendar(JAN_31));
		proratedAmt.setPartialMonthPremiumAmount(new BigDecimal("250"));
		fit.getProratedAmount().add(proratedAmt);
		
		policy.getFinancialInformation().add(fit);
		
		FinancialInformation fit2 = new FinancialInformation();
		fit2.setFinancialEffectiveStartDate(DateTimeUtil.getXMLGregorianCalendar(FEB_1));
		fit2.setFinancialEffectiveEndDate(DateTimeUtil.getXMLGregorianCalendar(MAR_31));
		fit2.setMonthlyTotalPremiumAmount(new BigDecimal("500.00"));
		fit2.setMonthlyTotalIndividualResponsibilityAmount(new BigDecimal("50.00"));
		fit2.setMonthlyCSRAmount(new BigDecimal("400.00"));
		fit2.setCSRVariantId("00");
		
		ProratedAmountType proratedAmt2 = new ProratedAmountType();
		proratedAmt2.setPartialMonthEffectiveStartDate(DateTimeUtil.getXMLGregorianCalendar(FEB_1));
		proratedAmt2.setPartialMonthEffectiveEndDate(DateTimeUtil.getXMLGregorianCalendar(FEB_15));
		proratedAmt2.setPartialMonthPremiumAmount(new BigDecimal("250.50"));
		proratedAmt2.setPartialMonthCSRAmount(new BigDecimal("200.00"));
		fit2.getProratedAmount().add(proratedAmt2);
		
		policy.getFinancialInformation().add(fit2);

		
		List<SbmErrWarningLogDTO> financialErrors = sbmFinancialValidator.validateFinancialInfo(policy);
		
		assertTrue("stateFinErrors", financialErrors.isEmpty());
	}
	
	/*
	 * validateProratedAmounts
	 */
	@Test
	public void test_validateProratedAmounts_R032_No_ProratedAmounts() {
		
		String qhpId = RandomStringUtils.randomAlphanumeric(16).toUpperCase();
		String stateCd = qhpId.substring(5, 7);

		List<String> bizRules = Arrays.asList("R032");
		SBMCache.getBusinessRules(stateCd).addAll(bizRules);
		
		expect(mockSbmDataService.getMetalLevelByQhpid(EasyMock.anyString(), EasyMock.anyString()))
		.andReturn(null).anyTimes();
		replay(mockSbmDataService);	
		
		PolicyType policy = new PolicyType();
		policy.setPolicyStartDate(DateTimeUtil.getXMLGregorianCalendar(JAN_1));
		policy.setPolicyEndDate(DateTimeUtil.getXMLGregorianCalendar(APR_30));
		policy.setQHPId(qhpId);
		policy.setEffectuationIndicator("Y");
		
		FinancialInformation fit = new FinancialInformation();
		fit.setFinancialEffectiveStartDate(DateTimeUtil.getXMLGregorianCalendar(JAN_1));
		fit.setFinancialEffectiveEndDate(DateTimeUtil.getXMLGregorianCalendar(JAN_31));
		fit.setMonthlyTotalPremiumAmount(new BigDecimal("450.00"));
		fit.setMonthlyTotalIndividualResponsibilityAmount(new BigDecimal("450.00"));
		fit.setCSRVariantId("02");
		policy.getFinancialInformation().add(fit);
		
		FinancialInformation fit2 = new FinancialInformation();
		fit2.setFinancialEffectiveStartDate(DateTimeUtil.getXMLGregorianCalendar(FEB_1));
		fit2.setFinancialEffectiveEndDate(DateTimeUtil.getXMLGregorianCalendar(MAR_31));
		fit2.setMonthlyTotalPremiumAmount(new BigDecimal("500.00"));
		fit2.setMonthlyTotalIndividualResponsibilityAmount(new BigDecimal("50.00"));
		fit2.setMonthlyCSRAmount(new BigDecimal("400.00"));
		fit2.setCSRVariantId("03");
		policy.getFinancialInformation().add(fit2);

		
		List<SbmErrWarningLogDTO> financialErrors = sbmFinancialValidator.validateFinancialInfo(policy);
		
		assertTrue("stateFinErrors", financialErrors.isEmpty());
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

	@After
	public void tearDown() {
		SBMCache.getBusinessRulesMap().clear();
	}
}
