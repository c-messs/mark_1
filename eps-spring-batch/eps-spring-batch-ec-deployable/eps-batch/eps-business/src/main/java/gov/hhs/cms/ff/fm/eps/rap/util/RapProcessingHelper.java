package gov.hhs.cms.ff.fm.eps.rap.util;
/**
 * 
 */


import gov.hhs.cms.ff.fm.eps.ep.StateProrationConfiguration;
import gov.hhs.cms.ff.fm.eps.ep.enums.PolicyStatus;
import gov.hhs.cms.ff.fm.eps.ep.enums.ProrationType;
import gov.hhs.cms.ff.fm.eps.rap.dao.RapDao;
import gov.hhs.cms.ff.fm.eps.rap.domain.IssuerUserFeeRate;
import gov.hhs.cms.ff.fm.eps.rap.domain.PolicyPremium;
import gov.hhs.cms.ff.fm.eps.rap.domain.RapConstants;
import gov.hhs.cms.ff.fm.eps.rap.dto.PolicyDataDTO;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.Days;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is a helper class for RAP Processing functionality
 * 
 * @author girish.padmanabhan
 */
public class RapProcessingHelper {
	private static final Logger LOG = LoggerFactory.getLogger(RapConstants.RAP_LOGGER);
	private static final String DATE_FORMAT_STR = "yyyy-MM-dd HH:mm:ss";
	public static final String DATE_FORMAT = "MM/dd/yyyy";
	public static final String YEAR_FORMAT = "yyyy";
	//InsrncAplctnTypeCd
	public static final String INS_APPLTYPE_1 = "1";
	public static final String INS_APPLTYPE_2 = "2";
	public static final String INS_APPLTYPE_3 = "3";
	public static final String INS_APPLTYPE_4 = "4";
	public static final String INS_APPLTYPE_5 = "5";
	public static final String INS_APPLTYPE_6 = "6";
	
	private static Map<String, Map<String, List<IssuerUserFeeRate>>> ufRateForRetroCoverageDateMap = new HashMap<String, Map<String, List<IssuerUserFeeRate>>>();

	//StateProrationConfigurationMap Structure Map<coverageYear, Map<stateCd, StateProrationConfiguration>> 
	private static Map<Integer, Map<String, StateProrationConfiguration>> stateProrationConfigMap = new HashMap<>();

	private static final DateTimeFormatter dateFormat = DateTimeFormat.forPattern(DATE_FORMAT);
	
	/**
	 * Method to get User Fee rate for the Coverage date and state
	 * @param coverageDate
	 * @param issuerStateCd
	 * @param insrncAplctnTypeCd
	 * @param rapDao
	 * @return
	 */
	public static IssuerUserFeeRate getUserFeeRateForRetroCoverageDate(DateTime coverageDate, String issuerStateCd, String insrncAplctnTypeCd, RapDao rapDao) {
		IssuerUserFeeRate rate = null;
		
		if(coverageDate == null || issuerStateCd == null || insrncAplctnTypeCd == null) {
			LOG.debug("These cannot be null coverageDate:{}, issuerStateCd:{}, insrncAplctnTypeCd:{}", coverageDate, issuerStateCd, insrncAplctnTypeCd);
			return null;
		}
		
		String coverageDateStr = dateFormat.print(coverageDate);
		
		Map<String, List<IssuerUserFeeRate>> innerMap = ufRateForRetroCoverageDateMap.get(coverageDateStr);
		
		if(ufRateForRetroCoverageDateMap.get(coverageDateStr) == null) {
			innerMap = getUserFeeRateMapFor(coverageDate, rapDao);
			ufRateForRetroCoverageDateMap.put(coverageDateStr, innerMap);
		}
		
		if(innerMap.get(issuerStateCd) != null) {
			String insAppType = mapInsrncAplctnTypeCd(insrncAplctnTypeCd);
			for(IssuerUserFeeRate item: innerMap.get(issuerStateCd)) {
				if(item.getInsrncAplctnTypeCd().equals(insAppType)) {
					return item;
				}
			}
		}
		
		LOG.debug("RetrocoverageDate:{}, UserFeeRate:", coverageDate, rate);
		return rate;
	}
	
	/*
	 * Private method to store user fee rate as a Map for the coverage date 
	 * @param coverageDate
	 * @param rapDao
	 * @return
	 */
	private static Map<String, List<IssuerUserFeeRate>> getUserFeeRateMapFor(DateTime coverageDate, RapDao rapDao) {
		
		Map<String, List<IssuerUserFeeRate>> rateMap = new HashMap<String, List<IssuerUserFeeRate>>();
		
		List<IssuerUserFeeRate> ufrateList = rapDao.getUserFeeRateForAllStates(coverageDate, String.valueOf(coverageDate.getYear()));
		for(IssuerUserFeeRate item: ufrateList) {
			if(rateMap.get(item.getIssuerUfStateCd()) == null) {
				rateMap.put(item.getIssuerUfStateCd(), new ArrayList<IssuerUserFeeRate>());
			}			
			
			rateMap.get(item.getIssuerUfStateCd()).add(item);			
		}
		
		return rateMap;
	}
	
	/**
	 * Map to store the Insurance Application Type code
	 * @param insrncAplctnTypeCd
	 * @return
	 */
	public static String mapInsrncAplctnTypeCd(String insrncAplctnTypeCd) {
		
		if(insrncAplctnTypeCd.equals(INS_APPLTYPE_5)  || insrncAplctnTypeCd.equals(INS_APPLTYPE_6)) {
			return INS_APPLTYPE_1;
		} else if (insrncAplctnTypeCd.equals(INS_APPLTYPE_4)) {
			return INS_APPLTYPE_2;
		}
		
		return insrncAplctnTypeCd;		
	}
	
	/**
	 * @return the ufRateForRetroCoverageDateMap
	 */
	public static Map<String, Map<String, List<IssuerUserFeeRate>>> getUfRateForRetroCoverageDateMap() {
		return ufRateForRetroCoverageDateMap;
	}
	
	/**
	 * Get Prorated Amount
	 * 
	 * @param paymentAmount
	 * @param prorationDays
	 * @param totalDaysInMonth
	 * @return
	 */
	public static BigDecimal getProratedAmount(BigDecimal paymentAmount, int prorationDays, int totalDaysInMonth) {
		
		MathContext mathContext = new MathContext(36, RoundingMode.HALF_UP);
		
		BigDecimal proratedAmount = paymentAmount.multiply(new BigDecimal(prorationDays), mathContext)
				.divide(new BigDecimal(totalDaysInMonth), mathContext)
				.setScale(2, RoundingMode.HALF_UP);
		
		return proratedAmount;
	}
	
	/**
	 * Determine the number of proration days
	 * 
	 * @param coverageDate
	 * @param effectiveStartDate
	 * @param premiumEndDate
	 * @return
	 */
	public static int getProrationDaysOfCoverage(DateTime coverageDate, DateTime effectiveStartDate, DateTime premiumEndDate) {
		
		Integer prorationDaysOfCoverage = coverageDate.dayOfMonth().getMaximumValue();
		
		if (!(effectiveStartDate.equals(coverageDate) && premiumEndDate.equals(coverageDate.dayOfMonth().withMaximumValue()))
				&& !premiumEndDate.isBefore(effectiveStartDate)) {
			
			prorationDaysOfCoverage = Days.daysBetween(effectiveStartDate, premiumEndDate.plusDays(1)).getDays();
		}
		
		return prorationDaysOfCoverage;
	}
	
	/**
	 * Determine whether latest policy status is 'INITIAL', 'CANCELLED' or 'SUPERSEDED'
	 * 
	 * @param policyVersion
	 * @return
	 */
	public static boolean isPolicyUneffectuated(PolicyDataDTO policyVersion) {
		
		String epsPolicyStatus = policyVersion.getPolicyStatus();
		
		if (StringUtils.isNotBlank(epsPolicyStatus)) {
			
			PolicyStatus policyStatus = PolicyStatus.getEnum(epsPolicyStatus);
			
			if(PolicyStatus.INITIAL_1.equals(policyStatus) || PolicyStatus.CANCELLED_3.equals(policyStatus) || PolicyStatus.SUPERSEDED_5.equals(policyStatus)) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Get Joda Date from String Date in CPP
	 * 
	 * @param dt
	 * @return
	 */
	public static DateTime getDateTimeFromString(String dt) {
		
		final DateTimeFormatter dateFormatter = DateTimeFormat.forPattern(DATE_FORMAT_STR);
		DateTime date = dateFormatter.parseDateTime(dt);
		return date;
	}
	
	/**
	 * Get State code from plan id
	 * 
	 * @param planId
	 * @return
	 */
	public static String getStateCode(String planId) {
		return planId.substring(5, 7);
	}
	
	/**
	 * @return the stateProrationConfigMap
	 */
	public static Map<Integer, Map<String, StateProrationConfiguration>> getStateProrationConfigMap() {
		return stateProrationConfigMap;
	}

	/**
	 * Returns StateProrationConfiguration for the given coverageYear and stateCd if exists, otherwise returns null
	 * @param coverageYear
	 * @param stateCd
	 * @return StateProrationConfiguration
	 */
	public static StateProrationConfiguration getStateProrationConfiguration(Integer coverageYear, String stateCd) {
		
		if(stateProrationConfigMap.get(coverageYear) != null) {
			return stateProrationConfigMap.get(coverageYear).get(stateCd);
		}
		
		return null;
	}
	
	/**
	 * Determine whether the state is prorating for the Payment Transaction year
	 * @param stateCode
	 * @param paymentTransYear
	 * @return
	 */
	public static ProrationType getProrationType(String stateCode, int paymentTransYear) {
		
		StateProrationConfiguration stateConfig = getStateProrationConfiguration(paymentTransYear, stateCode);
		
		if(stateConfig != null) {
			
			String prorationTypeCd = stateConfig.getProrationTypeCd();
			
			return ProrationType.getEnum(prorationTypeCd);
		}
		
		return ProrationType.NON_PRORATING;
	}

	/**
	 * Determine whether the SBM Premium records for the month has prorated amounts
	 * 
	 * @param proration
	 * @param premiumRecs
	 * @return boolean
	 */
	public static boolean isSbmWithoutProratedAmounts(ProrationType proration,
			List<PolicyPremium> premiumRecs) {

		if(proration.equals(ProrationType.SBM_PRORATING)) {
			return hasNoProratedAmounts(premiumRecs);
		}
		return false;
	}
	
	/**
	 * Determine whether the Premium records has prorated amounts
	 * 
	 * @param premiumRecs
	 * @return
	 */
	public static boolean hasNoProratedAmounts(List<PolicyPremium> premiumRecs) {

		long count = 
				premiumRecs.stream()
				.filter(premium -> ((premium.getProratedAptcAmount() != null && premium.getProratedAptcAmount().compareTo(BigDecimal.ZERO) != 0) 
						|| (premium.getProratedCsrAmount() != null && premium.getProratedCsrAmount().compareTo(BigDecimal.ZERO) != 0)))
				.count();
		
		if(count == 0) {
			return true;
		}
		
		return false;
	}
	
	/**
	 * Determine whether the Premium records has prorated csr amounts
	 * 
	 * @param premiumRecs
	 * @return
	 */
	public static boolean hasNoProratedCsr(List<PolicyPremium> premiumRecs) {

		long count = 
				premiumRecs.stream()
				.filter(premium -> (premium.getProratedCsrAmount() != null && premium.getProratedCsrAmount().compareTo(BigDecimal.ZERO) != 0))
				.count();
		
		if(count == 0) {
			return true;
		}
		
		return false;
	}
	
	/**
	 * Determine whether the Premium records has prorated aptc amounts
	 * 
	 * @param premiumRecs
	 * @return
	 */
	public static boolean hasNoProratedAptc(List<PolicyPremium> premiumRecs) {

		long count = 
				premiumRecs.stream()
				.filter(premium -> (premium.getProratedAptcAmount() != null && premium.getProratedAptcAmount().compareTo(BigDecimal.ZERO) != 0))
				.count();
		
		if(count == 0) {
			return true;
		}
		
		return false;
	}
}
