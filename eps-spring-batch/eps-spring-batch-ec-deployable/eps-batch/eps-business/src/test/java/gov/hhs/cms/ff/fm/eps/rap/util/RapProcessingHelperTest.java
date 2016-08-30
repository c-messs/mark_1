package gov.hhs.cms.ff.fm.eps.rap.util;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.easymock.EasyMock;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gov.hhs.cms.ff.fm.eps.ep.StateProrationConfiguration;
import gov.hhs.cms.ff.fm.eps.ep.enums.PolicyStatus;
import gov.hhs.cms.ff.fm.eps.ep.enums.ProrationType;
import gov.hhs.cms.ff.fm.eps.rap.dao.RapDao;
import gov.hhs.cms.ff.fm.eps.rap.domain.IssuerUserFeeRate;
import gov.hhs.cms.ff.fm.eps.rap.domain.PolicyPremium;
import gov.hhs.cms.ff.fm.eps.rap.dto.PolicyDataDTO;
import gov.hhs.cms.ff.fm.eps.rap.service.RapServiceTestUtil;
import junit.framework.TestCase;

@RunWith(JUnit4.class)
public class RapProcessingHelperTest extends TestCase {
	RapProcessingHelper rapProcessingHelper = new RapProcessingHelper();
	
	private static final Logger LOG  = LoggerFactory.getLogger(RapProcessingHelperTest.class);
	public static final String INS_APPLTYPE_1 = "1";
	public static final String INS_APPLTYPE_2 = "2";
	public static final String INS_APPLTYPE_3 = "3";
	public static final String INS_APPLTYPE_4 = "4";
	public static final String INS_APPLTYPE_5 = "5";
	public static final String INS_APPLTYPE_6 = "6";

	private RapDao mockRapDao;
	
	@Before
	public void setup() throws Exception {

		mockRapDao = createMock(RapDao.class);
	}
	
	@Test
	public void test_mapInsrncAplctnTypeCd1() {
		String expectredInsrncAplctnTypeCd = "111";
		String insrncAplctnTypeCd = expectredInsrncAplctnTypeCd;
		String returnedInsrncAplctnTypeCd = RapProcessingHelper.mapInsrncAplctnTypeCd(insrncAplctnTypeCd);
		assertNotNull("mapInsrncAplctnTypeCd not null", returnedInsrncAplctnTypeCd);
		assertEquals("mapInsrncAplctnTypeCd equal", expectredInsrncAplctnTypeCd, returnedInsrncAplctnTypeCd);
	}
	
	@Test
	public void test_mapInsrncAplctnTypeCd2() {
		String expectredInsrncAplctnTypeCd = INS_APPLTYPE_1;
		String insrncAplctnTypeCd = INS_APPLTYPE_5;
		String returnedInsrncAplctnTypeCd = RapProcessingHelper.mapInsrncAplctnTypeCd(insrncAplctnTypeCd);
		assertNotNull("mapInsrncAplctnTypeCd not null", returnedInsrncAplctnTypeCd);
		assertEquals("mapInsrncAplctnTypeCd equal", expectredInsrncAplctnTypeCd, returnedInsrncAplctnTypeCd);
	
		insrncAplctnTypeCd = INS_APPLTYPE_6;
		returnedInsrncAplctnTypeCd = RapProcessingHelper.mapInsrncAplctnTypeCd(insrncAplctnTypeCd);
		assertNotNull("mapInsrncAplctnTypeCd not null", returnedInsrncAplctnTypeCd);
		assertEquals("mapInsrncAplctnTypeCd equal", expectredInsrncAplctnTypeCd, returnedInsrncAplctnTypeCd);

	}
	
	@Test
	public void test_mapInsrncAplctnTypeCd3() {
		String expectredInsrncAplctnTypeCd = INS_APPLTYPE_2;
		String insrncAplctnTypeCd = INS_APPLTYPE_4;
		String returnedInsrncAplctnTypeCd = RapProcessingHelper.mapInsrncAplctnTypeCd(insrncAplctnTypeCd);
		assertNotNull("mapInsrncAplctnTypeCd not null", returnedInsrncAplctnTypeCd);
		assertEquals("mapInsrncAplctnTypeCd equal", expectredInsrncAplctnTypeCd, returnedInsrncAplctnTypeCd);
	}
	
	@Test
	public void test_getUfRateForRetroCoverageDateMap() {
		
		Map<String, Map<String, List<IssuerUserFeeRate>>> returnedMap = RapProcessingHelper.getUfRateForRetroCoverageDateMap();
		assertNotNull("mapInsrncAplctnTypeCd not null", returnedMap);
	}
	
	@Test
	public void test_getUserFeeRateForRetroCoverageDateWithNulls() {
		
		IssuerUserFeeRate issuerUserFeeRate = RapProcessingHelper.getUserFeeRateForRetroCoverageDate(null, null, null, null); 
		assertNull("issuerUserFeeRate is null", issuerUserFeeRate);
		
		issuerUserFeeRate = RapProcessingHelper.getUserFeeRateForRetroCoverageDate(DateTime.now(), null, null, null);
		assertNull("issuerUserFeeRate is null", issuerUserFeeRate);
		
		issuerUserFeeRate = RapProcessingHelper.getUserFeeRateForRetroCoverageDate(DateTime.now(), "VA", null, null);
		assertNull("issuerUserFeeRate is null", issuerUserFeeRate);
	}
	
	@Test
	public void test_getUserFeeRateForRetroCoverageDate() {
		
		expect(mockRapDao.getUserFeeRateForAllStates(
				EasyMock.anyObject(DateTime.class), EasyMock.anyString()))
					.andReturn(RapServiceTestUtil.createIssuerUserFeeRateList()).anyTimes();
		replay(mockRapDao);
		
		IssuerUserFeeRate issuerUserFeeRate = 
				RapProcessingHelper.getUserFeeRateForRetroCoverageDate(new DateTime(2015, 5, 1, 0, 0), "NY", "1", mockRapDao);

		assertNotNull("issuerUserFeeRate is not null", issuerUserFeeRate);
		assertEquals("issuerUserFeeRate %", 3.5, issuerUserFeeRate.getIssuerUfPercent().doubleValue());
	}
	
	@Test
	public void test_getUserFeeRateForRetroCoverageDate_NullDate() {
		
		expect(mockRapDao.getUserFeeRateForAllStates(
				EasyMock.anyObject(DateTime.class), EasyMock.anyString()))
					.andReturn(RapServiceTestUtil.createIssuerUserFeeRateList()).anyTimes();
		replay(mockRapDao);
		
		IssuerUserFeeRate issuerUserFeeRate = 
				RapProcessingHelper.getUserFeeRateForRetroCoverageDate(new DateTime(2015, 5, 1, 0, 0), "VA", "1", mockRapDao);

		assertNull("issuerUserFeeRate is null", issuerUserFeeRate);
	}

	@Test
	public void testGetProrationDaysOfCoverage() {
		//January has 31 days
		int expected = 31;

		DateTime coverageDate = new DateTime("2015-01-01");
		DateTime effectiveStartDate = new DateTime("2015-02-01");
		DateTime premiumEndDate = new DateTime("2014-01-01");

		Integer actualProrationDaysOfCoverage = RapProcessingHelper.getProrationDaysOfCoverage(coverageDate, effectiveStartDate, premiumEndDate);

		assertEquals("Proration Days of Coverage", expected, actualProrationDaysOfCoverage.intValue());
	}
	
	@Test
	public void testGetProrationDaysOfCoverage_2() {

		int expected = 335;

		DateTime coverageDate = new DateTime("2015-01-01");
		DateTime effectiveStartDate = new DateTime("2015-02-01");
		DateTime premiumEndDate = new DateTime("2016-01-01");

		Integer actualProrationDaysOfCoverage = RapProcessingHelper.getProrationDaysOfCoverage(coverageDate, effectiveStartDate, premiumEndDate);

		assertEquals("Proration Days of Coverage", expected, actualProrationDaysOfCoverage.intValue());
	}	

	@Test
	public void testGetProrationDaysOfCoverage_LeapYear() {
		//Leap Year February has 29 days
		int expected = 29;

		DateTime coverageDate = new DateTime("2016-02-01");
		DateTime effectiveStartDate = new DateTime("2015-02-01");
		DateTime premiumEndDate = new DateTime("2014-01-01");

		Integer actualProrationDaysOfCoverage = RapProcessingHelper.getProrationDaysOfCoverage(coverageDate, effectiveStartDate, premiumEndDate);

		assertEquals("Proration Days of Coverage", expected, actualProrationDaysOfCoverage.intValue());
	}
	
	/**
	 * Test proration calculation.
	 */
	@Test
	public void testProrationCalculation() {
		
		BigDecimal monthlyPremium = BigDecimal.valueOf(15.15);
		int prorationDaysOfCoverage = 11;
		int numOfDaysInMonth = 30;
		LOG.info(" monthlyPremium:{} prorationDaysOfCoverage:{} numOfDaysInMonth:{}",  monthlyPremium, prorationDaysOfCoverage, numOfDaysInMonth);
		BigDecimal actualResult = RapProcessingHelper.getProratedAmount(monthlyPremium, prorationDaysOfCoverage, numOfDaysInMonth);
		LOG.info("ProratedAmount: {}", actualResult);
		BigDecimal expectedResult = BigDecimal.valueOf(5.56);
		assertTrue("Expected result:"+ expectedResult, (expectedResult.compareTo(actualResult)) == 0);
	}
	
	@Test
	public void test_isPolicyCancelled() {
		
		boolean expected = true;
		PolicyDataDTO policyVersion = new PolicyDataDTO();
		
		DateTime psd = new DateTime(2016, 1, 1, 0, 0);
		// Same date, different time component
		DateTime ped = new DateTime(2016, 1, 1, 5, 55);
		
		policyVersion.setPolicyStartDate(psd);
		policyVersion.setPolicyEndDate(ped);
		policyVersion.setPolicyStatus(PolicyStatus.CANCELLED_3.getValue());

		Boolean actual = RapProcessingHelper.isPolicyUneffectuated(policyVersion);

		assertEquals("isPolicyCancelledOnStartDate", expected, actual.booleanValue());	
	}
	
	@Test
	public void test_isSuperseded() {
		
		boolean expected = true;
		PolicyDataDTO policyVersion = new PolicyDataDTO();
		
		DateTime psd = new DateTime(2016, 1, 1, 0, 0);
		// Same date, different time component
		DateTime ped = new DateTime(2016, 1, 1, 5, 55);
		
		policyVersion.setPolicyStartDate(psd);
		policyVersion.setPolicyEndDate(ped);
		
		String policyStatus = PolicyStatus.SUPERSEDED_5.getValue();
		policyVersion.setPolicyStatus(policyStatus);

		Boolean actual = RapProcessingHelper.isPolicyUneffectuated(policyVersion);
		
		assertEquals("isPolicyCancelledOnStartDate", expected, actual.booleanValue());	
	}
	
	@Test
	public void test_isPolicyCancelled_nullStatusEps() {
		
		boolean expected = false;
		PolicyDataDTO policyVersion = new PolicyDataDTO();
		
		DateTime psd = new DateTime(2016, 1, 1, 0, 0);
		// Same date, different time component
		DateTime ped = new DateTime(2016, 1, 1, 5, 55);
		
		policyVersion.setPolicyStartDate(psd);
		policyVersion.setPolicyEndDate(ped);
		
		String policyStatus = null;
		policyVersion.setPolicyStatus(policyStatus);

		Boolean actual = RapProcessingHelper.isPolicyUneffectuated(policyVersion);

		assertEquals("isPolicyCancelledOnStartDate", expected, actual.booleanValue());	
	}
	
	@Test
	public void test_isPolicyCancelled_EffectuatedStatusEps() {
		
		boolean expected = false;
		PolicyDataDTO policyVersion = new PolicyDataDTO();
		
		DateTime psd = new DateTime(2016, 1, 1, 0, 0);
		// Same date, different time component
		DateTime ped = new DateTime(2016, 1, 1, 5, 55);
		
		policyVersion.setPolicyStartDate(psd);
		policyVersion.setPolicyEndDate(ped);
		
		String policyStatus = PolicyStatus.EFFECTUATED_2.getValue();
		policyVersion.setPolicyStatus(policyStatus);

		Boolean actual = RapProcessingHelper.isPolicyUneffectuated(policyVersion);

		assertEquals("isPolicyCancelledOnStartDate", expected, actual.booleanValue());	
	}
	
	@Test
	public void test_isPolicyCancelled_TerminatedStatusEps() {
		
		boolean expected = false;
		PolicyDataDTO policyVersion = new PolicyDataDTO();
		
		DateTime psd = new DateTime(2016, 1, 1, 0, 0);
		// Same date, different time component
		DateTime ped = new DateTime(2016, 1, 1, 5, 55);
		
		policyVersion.setPolicyStartDate(psd);
		policyVersion.setPolicyEndDate(ped);
		
		String policyStatus = PolicyStatus.TERMINATED_4.getValue();
		policyVersion.setPolicyStatus(policyStatus);

		Boolean actual = RapProcessingHelper.isPolicyUneffectuated(policyVersion);

		assertEquals("isPolicyCancelledOnStartDate", expected, actual.booleanValue());	
	}
	
	@Test
	public void test_GetStateCode() {

		String planId = "11111ZZ11";
		String expectedStateCd = "ZZ";
		
		String actualStateCd = RapProcessingHelper.getStateCode(planId);
		
		assertEquals("getStateCode", expectedStateCd, actualStateCd);	
	}
	
	@Test
	public void test_getStateProrationConfiguration() {
		
		StateProrationConfiguration stConfigExpected = new StateProrationConfiguration();
		stConfigExpected.setStateCd("VA");
		stConfigExpected.setMarketYear(new DateTime().getYear());
		stConfigExpected.setProrationTypeCd("2");
		
		Map<String, StateProrationConfiguration> stateCdMap = new HashMap<>();
		
		stateCdMap.put(stConfigExpected.getStateCd(), stConfigExpected);
		
		RapProcessingHelper.getStateProrationConfigMap().put(stConfigExpected.getMarketYear(), stateCdMap);
		
		StateProrationConfiguration stateConfigActual = 
				RapProcessingHelper.getStateProrationConfiguration(stConfigExpected.getMarketYear(), stConfigExpected.getStateCd());
		
		assertEquals("ProrationTypeCd", stConfigExpected.getProrationTypeCd(), stateConfigActual.getProrationTypeCd());	
	}
	
	@Test
	public void test_getStateProrationConfiguration_Null() {
		
		StateProrationConfiguration stConfigExpected = new StateProrationConfiguration();
		stConfigExpected.setStateCd("VA");
		stConfigExpected.setMarketYear(new DateTime().getYear());
		stConfigExpected.setProrationTypeCd("2");
		
		Map<String, StateProrationConfiguration> stateCdMap = new HashMap<>();
		
		stateCdMap.put(stConfigExpected.getStateCd(), stConfigExpected);
		
		RapProcessingHelper.getStateProrationConfigMap().put(stConfigExpected.getMarketYear(), stateCdMap);
		
		StateProrationConfiguration stateConfigActual = 
				RapProcessingHelper.getStateProrationConfiguration(stConfigExpected.getMarketYear()-10, "NY");
		
		assertNull("stateConfigActual", stateConfigActual);	
	}
	
	
	@Test
	public void test_getProrationType() {
		
		String stateCd = "VA";
		int marketYear = new DateTime().getYear();
		ProrationType prorationExpected = ProrationType.SBM_PRORATING;
		
		Map<String, StateProrationConfiguration> stateCdMap = getStateConfigMap(stateCd, marketYear, prorationExpected);
		
		RapProcessingHelper.getStateProrationConfigMap().put(marketYear, stateCdMap);
		
		ProrationType proration = RapProcessingHelper.getProrationType(stateCd, marketYear);
		
		assertNotNull("proration", proration);	
		
		assertEquals("ProrationTypeCd", prorationExpected, proration);

	}
	
	@Test
	public void test_getProrationType_null() {
		
		String stateCd = "VA";
		int marketYear = new DateTime().getYear();
		ProrationType prorationExpected = ProrationType.SBM_PRORATING;
		
		Map<String, StateProrationConfiguration> stateCdMap = getStateConfigMap(stateCd, marketYear, prorationExpected);
		
		RapProcessingHelper.getStateProrationConfigMap().put(marketYear, stateCdMap);
		
		ProrationType proration = RapProcessingHelper.getProrationType("NY", marketYear-10);
		
		assertEquals("proration", ProrationType.NON_PRORATING, proration);	
	}
	
	
	@Test
	public void test_isSbmWithoutProratedAmounts_true() {
		
		PolicyPremium premium = new PolicyPremium();
		PolicyPremium premium2 = new PolicyPremium();
		
		List<PolicyPremium> premiums = Arrays.asList(premium, premium2);
		
		boolean result = RapProcessingHelper.isSbmWithoutProratedAmounts(ProrationType.SBM_PRORATING, premiums);
		
		assertTrue("result", result);	
	}
	
	@Test
	public void test_isSbmWithoutProratedAmounts_true1() {
		
		PolicyPremium premium = new PolicyPremium();
		premium.setProratedAptcAmount(new BigDecimal("0.00"));
		premium.setProratedCsrAmount(new BigDecimal("0.00"));
		
		PolicyPremium premium2 = new PolicyPremium();
		premium2.setProratedAptcAmount(new BigDecimal("0.00"));
		premium2.setProratedCsrAmount(new BigDecimal("0.00"));
		
		List<PolicyPremium> premiums = Arrays.asList(premium, premium2);
		
		boolean result = RapProcessingHelper.isSbmWithoutProratedAmounts(ProrationType.SBM_PRORATING, premiums);
		
		assertTrue("result", result);	
	}
	
	@Test
	public void test_isSbmWithoutProratedAmounts_false() {
		
		PolicyPremium premium = new PolicyPremium();
		premium.setProratedAptcAmount(new BigDecimal("25.00"));
		premium.setProratedCsrAmount(new BigDecimal("125.00"));
		
		PolicyPremium premium2 = new PolicyPremium();
		premium2.setProratedAptcAmount(new BigDecimal("125.00"));
		premium2.setProratedCsrAmount(new BigDecimal("25.00"));
		
		List<PolicyPremium> premiums = Arrays.asList(premium, premium2);
		
		boolean result = RapProcessingHelper.isSbmWithoutProratedAmounts(ProrationType.SBM_PRORATING, premiums);
		
		assertFalse("result", result);	
	}
	
	@Test
	public void test_isSbmWithoutProratedAmounts_false1() {
		
		PolicyPremium premium = new PolicyPremium();
		premium.setProratedAptcAmount(new BigDecimal("25.00"));
		
		PolicyPremium premium2 = new PolicyPremium();
		premium2.setProratedCsrAmount(new BigDecimal("25.00"));
		
		List<PolicyPremium> premiums = Arrays.asList(premium, premium2);
		
		boolean result = RapProcessingHelper.isSbmWithoutProratedAmounts(ProrationType.SBM_PRORATING, premiums);
		
		assertFalse("result", result);	
	}
	
	@Test
	public void test_isSbmWithoutProratedAmounts_false2() {
		
		PolicyPremium premium = new PolicyPremium();
		
		PolicyPremium premium2 = new PolicyPremium();
		premium2.setProratedAptcAmount(new BigDecimal("125.00"));
		premium2.setProratedCsrAmount(new BigDecimal("25.00"));
		
		List<PolicyPremium> premiums = Arrays.asList(premium, premium2);
		
		boolean result = RapProcessingHelper.isSbmWithoutProratedAmounts(ProrationType.SBM_PRORATING, premiums);
		
		assertFalse("result", result);	
	}
	
	@Test
	public void test_isSbmWithoutProratedAmounts_false3() {
		
		PolicyPremium premium = new PolicyPremium();
		
		PolicyPremium premium2 = new PolicyPremium();
		premium2.setProratedAptcAmount(new BigDecimal("125.00"));
		premium2.setProratedCsrAmount(new BigDecimal("25.00"));
		
		List<PolicyPremium> premiums = Arrays.asList(premium, premium2);
		
		boolean result = RapProcessingHelper.isSbmWithoutProratedAmounts(ProrationType.FFM_PRORATING, premiums);
		
		assertFalse("result", result);	
	}
	
	@Test
	public void test_getDateTimeFromString() {

		String dateStr = "2015-2-1 00:00:00";
		DateTime dt = RapProcessingHelper.getDateTimeFromString(dateStr);
		System.out.println("dt: " + dt);
		assertEquals("DateTime", new DateTime(2015, 2, 1, 0, 0), dt);

		String dateStr1 = "2015-02-1 00:00:00";
		DateTime dt1 = RapProcessingHelper.getDateTimeFromString(dateStr1);
		System.out.println("dt1: " + dt1);
		assertEquals("DateTime1", new DateTime(2015, 2, 1, 0, 0), dt1);

		String dateStr2 = "2015-02-01 00:00:00";
		DateTime dt2 = RapProcessingHelper.getDateTimeFromString(dateStr2);
		System.out.println("dt2: " + dt2);
		assertEquals("DateTime2", new DateTime(2015, 2, 1, 0, 0), dt1);

	}
	
	private Map<String, StateProrationConfiguration> getStateConfigMap(String stateCd, int marketYear,
			ProrationType prorationType) {
		
		StateProrationConfiguration stConfig = new StateProrationConfiguration();
		stConfig.setStateCd(stateCd);
		stConfig.setMarketYear(marketYear);
		stConfig.setProrationTypeCd(prorationType.getValue());
		
		Map<String, StateProrationConfiguration> stateCdMap = new HashMap<>();
		stateCdMap.put(stConfig.getStateCd(), stConfig);
		
		return stateCdMap;
	}
	
}
