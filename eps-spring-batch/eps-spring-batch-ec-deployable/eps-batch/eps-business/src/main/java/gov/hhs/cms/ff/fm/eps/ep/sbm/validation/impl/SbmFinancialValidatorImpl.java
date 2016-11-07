package gov.hhs.cms.ff.fm.eps.ep.sbm.validation.impl;
import static gov.hhs.cms.ff.fm.eps.ep.sbm.SBMConstants.ERROR_DESC_INCORRECT_VALUE;
import static gov.hhs.cms.ff.fm.eps.ep.sbm.SBMConstants.ERROR_INFO_EXPECTED_VALUE_TXT;
import static gov.hhs.cms.ff.fm.eps.ep.sbm.SBMConstants.VARIANT_ID_01;
import static gov.hhs.cms.ff.fm.eps.ep.sbm.SBMConstants.VARIANT_ID_02;
import static gov.hhs.cms.ff.fm.eps.ep.sbm.SBMConstants.VARIANT_ID_03;
import static gov.hhs.cms.ff.fm.eps.ep.sbm.SBMConstants.VARIANT_ID_04;
import static gov.hhs.cms.ff.fm.eps.ep.sbm.SBMConstants.VARIANT_ID_05;
import static gov.hhs.cms.ff.fm.eps.ep.sbm.SBMConstants.VARIANT_ID_06;
import static gov.hhs.cms.ff.fm.eps.ep.sbm.SBMConstants.Y;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import gov.cms.dsh.sbmi.PolicyType;
import gov.cms.dsh.sbmi.PolicyType.FinancialInformation;
import gov.cms.dsh.sbmi.ProratedAmountType;
import gov.hhs.cms.ff.fm.eps.ep.enums.SBMErrorWarningCode;
import gov.hhs.cms.ff.fm.eps.ep.sbm.SBMCache;
import gov.hhs.cms.ff.fm.eps.ep.sbm.SBMPremium;
import gov.hhs.cms.ff.fm.eps.ep.sbm.SbmDataUtil;
import gov.hhs.cms.ff.fm.eps.ep.sbm.SbmErrWarningLogDTO;
import gov.hhs.cms.ff.fm.eps.ep.sbm.SbmMetalLevelType;
import gov.hhs.cms.ff.fm.eps.ep.sbm.services.SBMDataService;
import gov.hhs.cms.ff.fm.eps.ep.sbm.validation.SbmFinancialValidator;
import gov.hhs.cms.ff.fm.eps.ep.util.DateTimeUtil;
/**
 * @author girish.padmanabhan
 * 
 *
 */
public class SbmFinancialValidatorImpl implements SbmFinancialValidator {
	private static final Logger LOG = LoggerFactory.getLogger(SbmFinancialValidatorImpl.class);
	private static final String PART_MNTH_EFFECT_DATE = "PartialMonthEffectiveStartDate";
	private static final String FIN_EFFECT_DATE = "FinancialEffectiveStartDate";
	private static final String PART_EFFECT_END_DATE = "PartialMonthEffectiveEndDate";
	private static final String MNTH_CSR_AMT = "MonthlyCSRAmount";
	private SBMDataService sbmDataService;
	private List<String> sbmBusinessRules;
	@Override
	public Map<LocalDate, SBMPremium> processInboundPremiums(PolicyType policy) {
		Map<LocalDate, SBMPremium> epsBoundPremiums = new LinkedHashMap<LocalDate, SBMPremium>();
		List<FinancialInformation> financialInfo = policy.getFinancialInformation();
		financialInfo.forEach(financialInfoType -> {
			Map<LocalDate, SBMPremium> inboundPremiums = new LinkedHashMap<LocalDate, SBMPremium>();
			//Create Map for each parent FinancialInfo node
			Map<LocalDate, SBMPremium> tempPremiumMap = new LinkedHashMap<LocalDate, SBMPremium>();
			List<SBMPremium> tempPremiumList = new ArrayList<SBMPremium>();
			SBMPremium monthlyAmount = createEpsPremiumForFinancialInfo(financialInfoType);
			tempPremiumList.add(monthlyAmount);
			if (CollectionUtils.isNotEmpty(financialInfoType.getProratedAmount())) {
				financialInfoType.getProratedAmount().forEach(proratedAmount -> {
					SBMPremium proratedPremium = new SBMPremium();
					//Set Prorated Amounts
					setProratedPremium(proratedAmount, proratedPremium);
					//Set Monthly Amounts
					setMonthlyAmounts(financialInfoType, proratedPremium);
					tempPremiumList.add(proratedPremium);
				});			
			}
			tempPremiumList.forEach(premiumRec -> {
				LocalDate premiumEsd = premiumRec.getEffectiveStartDate();
				tempPremiumMap.put(premiumEsd, premiumRec);
				if(inboundPremiums.containsKey(premiumEsd)) {
					SBMPremium nonProratedPremium = inboundPremiums.get(premiumEsd);
					inboundPremiums.remove(premiumEsd);
					LocalDate newKey = tempPremiumMap.get(premiumEsd).getEffectiveEndDate().plusDays(1);
					inboundPremiums.put(newKey, nonProratedPremium);
				}
				inboundPremiums.putAll(tempPremiumMap);
			});
			if (!inboundPremiums.isEmpty()) {
				processProratedAmounts(inboundPremiums, policy);
			}
			epsBoundPremiums.putAll(inboundPremiums);
		});
		return epsBoundPremiums;
	}
	/*
	 * Get the prorated amounts. The inbound Policy will have upto to 2 prorated loops as child nodes of the monthly amounts loop. They will carry the prorated amounts.
	 */
	private void processProratedAmounts(Map<LocalDate, SBMPremium> inboundPremiums, PolicyType policy) {
		LocalDate policyStartDate = DateTimeUtil.getLocalDateFromXmlGC(policy.getPolicyStartDate());
		LocalDate policyEndDate = DateTimeUtil.getLocalDateFromXmlGC(policy.getPolicyEndDate());
		if(inboundPremiums.size() > 1) {
			LOG.info("Multiple Premium nodes present - process prorated amounts from 2nd and 3rd prorated nodes"); 
			//Prevent Overlapping EPS Policy Premium Records for Policies effective <1 Month
			//Identify if any loop with Prorated amounts has EffectiveStartDate and EffectiveEndDate identical to Policy Start AND End Date
			Iterator<Map.Entry<LocalDate, SBMPremium>> iterator = inboundPremiums.entrySet().iterator();
			Map.Entry<LocalDate, SBMPremium> monthlyAmounts = iterator.next();
			LOG.info("first key: " + monthlyAmounts.getKey());
			boolean hasIdenticalDates = false;
			while(iterator.hasNext()){
				Map.Entry<LocalDate, SBMPremium> sbmPremium = iterator.next();
				SBMPremium premium = sbmPremium.getValue();
				LocalDate dateKey = sbmPremium.getKey();
				//Remove the non-prorated premium which is after policy end date 
				//(This corresponds to removing non prorated monthly premium record for 1/15-2/15 type of scenario)
				// Add null check for PED.  Cannot be null for java.time.localDate else NPE.
				if(dateKey.isAfter(policyEndDate)) {
					iterator.remove();
					continue;
				}
				LOG.info("sbmPremium esd: " + premium.getEffectiveStartDate());
				LocalDate premiumStartDt = premium.getEffectiveStartDate();
				LocalDate premiumEndDt = premium.getEffectiveEndDate();
				if(premiumStartDt.isEqual(policyStartDate) && premiumEndDt.isEqual(policyEndDate)) {
					hasIdenticalDates = true;
					break;
				}
				//Identify if any loop with prorated amounts has EffectiveStartDate and EffectiveEndDate identical to those in other loop (without prorated amounts)
				LocalDate nonProratedEsd = monthlyAmounts.getValue().getEffectiveStartDate();
				LocalDate nonProratedEed = monthlyAmounts.getValue().getEffectiveEndDate();
				if(premiumStartDt.isEqual(nonProratedEsd) && premiumEndDt.isEqual(nonProratedEed)) {
					hasIdenticalDates = true;
					break;
				}
			}
			//Prorated Start & End Dates identical to policy/non-prorated loop
			if(hasIdenticalDates) {
				//Ignore the subscriber loop without prorated amounts and only process the subscriber loop with Prorated Amounts
				inboundPremiums.remove(monthlyAmounts.getKey());
			} else {
				//EPS Policy-Premium Record Date Alignment
				performPremiumDateAlignment(inboundPremiums);
			}
		} 
	}
	/*
	 * EPS Policy-Premium Records Date Alignment
	 */
	private void performPremiumDateAlignment(Map<LocalDate, SBMPremium> inboundPremiums) {
		Iterator<Map.Entry<LocalDate, SBMPremium>> iterator = inboundPremiums.entrySet().iterator();
		SBMPremium firstSubscriberPremium = iterator.next().getValue();
		LocalDate nonProratedEsd = firstSubscriberPremium.getEffectiveStartDate();
		while(iterator.hasNext()){
			SBMPremium additionalInfo = iterator.next().getValue();
			LocalDate premiumStartDt = additionalInfo.getEffectiveStartDate();
			LocalDate premiumEndDt = additionalInfo.getEffectiveEndDate();
			if(!premiumStartDt.isAfter(nonProratedEsd)) {
				//For any PolicyPremium record where the preceding record has prorated amounts, modify EffectiveStartDate of the selected record to be equal to the EffectiveEndDate of preceding record + 1 calendar day
				firstSubscriberPremium.setEffectiveStartDate(premiumEndDt.plusDays(1));
			} else {
				//For any PolicyPremium record where the trailing record has prorated amounts, modify EffectiveEndDate of the selected record to be equal to the EffectiveStartDate of the trailing record - 1 calendar day
				firstSubscriberPremium.setEffectiveEndDate(premiumStartDt.minusDays(1));
			}
		}
	}
	/*
	 * Creates a "flat" SBMPremium record, or "timeslice"
	 * @param financialInfo
	 * @return
	 */
	private SBMPremium createEpsPremiumForFinancialInfo(FinancialInformation financialInfo) {
		LocalDate effectiveStartDt = DateTimeUtil.getLocalDateFromXmlGC(
				financialInfo.getFinancialEffectiveStartDate());
		SBMPremium premium = new SBMPremium();
		premium.setEffectiveStartDate(effectiveStartDt);
		premium.setEffectiveEndDate(DateTimeUtil.getLocalDateFromXmlGC(financialInfo.getFinancialEffectiveEndDate()));
		setMonthlyAmounts(financialInfo, premium);
		return premium;
	}
	/*
	 * Updates a "flat" SBMPremium record, or "timeslice"
	 */
	private void setProratedPremium(ProratedAmountType proratedAmount, SBMPremium premium) {
		premium.setEffectiveStartDate(DateTimeUtil.getLocalDateFromXmlGC(
				proratedAmount.getPartialMonthEffectiveStartDate()));
		premium.setEffectiveEndDate(DateTimeUtil.getLocalDateFromXmlGC(
				proratedAmount.getPartialMonthEffectiveEndDate()));
		premium.setProratedAptc(proratedAmount.getPartialMonthAPTCAmount());
		premium.setProratedCsr(proratedAmount.getPartialMonthCSRAmount());
		premium.setProratedPremium(proratedAmount.getPartialMonthPremiumAmount());
	}
	/*
	 * Updates a "flat" SBMPremium record, or "timeslice"
	 */
	private void setMonthlyAmounts(FinancialInformation financialInfo, SBMPremium premium) {
		premium.setAptc(financialInfo.getMonthlyAPTCAmount());
		premium.setCsr(financialInfo.getMonthlyCSRAmount());
		premium.setCsrVariantId(financialInfo.getCSRVariantId());
		premium.setTotalPremium(financialInfo.getMonthlyTotalPremiumAmount());
		premium.setIndividualResponsibleAmt(financialInfo.getMonthlyTotalIndividualResponsibilityAmount());
		premium.setRatingArea(financialInfo.getRatingArea());
		premium.setOtherPayment1(financialInfo.getMonthlyOtherPaymentAmount1());
		premium.setOtherPayment2(financialInfo.getMonthlyOtherPaymentAmount2());	
	}
	@Override
	public List<SbmErrWarningLogDTO> validateFinancialInfo(PolicyType policy) {
		List<SbmErrWarningLogDTO> financialErrors = new ArrayList<SbmErrWarningLogDTO>();
		List<FinancialInformation> financialInfoList = policy.getFinancialInformation();
		SbmValidationUtil.sortFinancialInfo(financialInfoList);
		String stateCd = SbmValidationUtil.getStateCdFromQhpId(policy.getQHPId());
		if(StringUtils.isNotBlank(stateCd)) {
			this.sbmBusinessRules = SBMCache.getBusinessRules(stateCd);
		}
		//Validate Overlaps and Gaps
		if (financialInfoList.size() > 1) {
			LocalDate prevFinEndDt = DateTimeUtil.getLocalDateFromXmlGC(
					financialInfoList.get(0).getFinancialEffectiveStartDate()).minusDays(1);
			for(FinancialInformation financialInfo: financialInfoList) {
				LocalDate finStartDt = DateTimeUtil.getLocalDateFromXmlGC(financialInfo.getFinancialEffectiveStartDate());
				//FR-FM-PP-SBMI-454 Overlap check
				if(sbmBusinessRules.contains("R001")) {
					performOverlapCheck(prevFinEndDt, finStartDt, FIN_EFFECT_DATE,
							SBMErrorWarningCode.ER_033, financialErrors);
				}
				//FR-FM-PP-SBMI-456 Gap check
				if(sbmBusinessRules.contains("R002")) {
					performGapCheck(prevFinEndDt, finStartDt, FIN_EFFECT_DATE, SBMErrorWarningCode.ER_034,
							financialErrors);
				}
				prevFinEndDt = DateTimeUtil.getLocalDateFromXmlGC(financialInfo.getFinancialEffectiveEndDate());
			}
		}
		List<SbmErrWarningLogDTO> financialValidationErrors = performStateSpecificValidations(financialInfoList, policy);
		financialErrors.addAll(financialValidationErrors);
		List<SbmErrWarningLogDTO> prorationValidationErrors = performProrationValidations(financialInfoList, policy);
		financialErrors.addAll(prorationValidationErrors);
		return financialErrors;
	}
	/*
	 * Validate Proration Financial Nodes
	 */
	private List<SbmErrWarningLogDTO> performProrationValidations(List<FinancialInformation> financialInfoList,
			PolicyType policy) {
		List<SbmErrWarningLogDTO> prorationErrors = new ArrayList<SbmErrWarningLogDTO>();
		//Validate Overlaps and Gaps
		if (financialInfoList.size() > 1) {
			LocalDate prevEndDt = DateTimeUtil.getLocalDateFromXmlGC(
					financialInfoList.get(0).getFinancialEffectiveStartDate()).minusDays(1);
			if(CollectionUtils.isNotEmpty(financialInfoList.get(0).getProratedAmount())) {
				prevEndDt = DateTimeUtil.getLocalDateFromXmlGC(
						financialInfoList.get(0).getProratedAmount().get(0).getPartialMonthEffectiveStartDate()).minusDays(1);
			}
			for(FinancialInformation financialInfo: financialInfoList) {
				List<ProratedAmountType> proratedAmounts = financialInfo.getProratedAmount();
				if(CollectionUtils.isNotEmpty(proratedAmounts)) {
					SbmValidationUtil.sortProratedAmounts(proratedAmounts);
					LocalDate finStartDt = DateTimeUtil.getLocalDateFromXmlGC(
							proratedAmounts.get(0).getPartialMonthEffectiveStartDate());
					//FR-FM-PP-SBMI-458 Overlap check R016 Validate for partial month overlap dates on the prorated node from one parent node to another for the same month
					if(sbmBusinessRules.contains("R016")
							&& (prevEndDt.getMonth().getValue() == finStartDt.getMonth().getValue())) {
						performOverlapCheck(prevEndDt, finStartDt, PART_MNTH_EFFECT_DATE,
								SBMErrorWarningCode.ER_043, prorationErrors);
					}
					//FR-FM-PP-SBMI-460 Gap check - R017 Validate for  partial month gap dates on the prorated node from one parent node to another for the same month
					if(sbmBusinessRules.contains("R017") 
							&& (prevEndDt.getMonth().getValue() == finStartDt.getMonth().getValue())) {
						performGapCheck(prevEndDt, finStartDt,PART_MNTH_EFFECT_DATE,
								SBMErrorWarningCode.ER_044, prorationErrors);
					}
					prevEndDt = DateTimeUtil.getLocalDateFromXmlGC(
							proratedAmounts.get(proratedAmounts.size()-1).getPartialMonthEffectiveEndDate());
				}
			}
		}
		List<SbmErrWarningLogDTO> prorationValidationErrors = validateProrationNodes(financialInfoList, policy);
		prorationErrors.addAll(prorationValidationErrors);
		return prorationErrors;
	}
	/*
	 * Verify overlap between previous Effective End Date and Effective Start Date
	 */
	private void performOverlapCheck(LocalDate prevFinEndDt, LocalDate finStartDt, String fieldName,
			SBMErrorWarningCode errorCd, List<SbmErrWarningLogDTO> financialErrors) {
		if(!finStartDt.isAfter(prevFinEndDt)) {
			//create Error: incorrect value provided
			financialErrors.add(SbmValidationUtil.createErrorWarningLogDTO(fieldName, errorCd.getCode(),
					ERROR_DESC_INCORRECT_VALUE.concat(finStartDt.toString())));
			LOG.info("Financial node date overlap error {} ", finStartDt);
		}
	}
	/*
	 * Verify Effective Start Date is more than 1 day greater than previous Effective End Date.
	 */
	private void performGapCheck(LocalDate prevFinEndDt, LocalDate finStartDt, String fieldName,
			SBMErrorWarningCode errorCode, List<SbmErrWarningLogDTO> financialErrors) {
		if (finStartDt.isAfter(prevFinEndDt.plusDays(1))) {
			//create Error ER-034: incorrect value provided
			financialErrors.add(SbmValidationUtil.createErrorWarningLogDTO(fieldName,
					errorCode.getCode(), ERROR_DESC_INCORRECT_VALUE.concat(finStartDt.toString())));
			LOG.info("Financial node date gap error {} ", finStartDt);
		}
	}
	/*
	 * Validate Individual ProratedAmount Nodes
	 */
	private List<SbmErrWarningLogDTO> validateProrationNodes(List<FinancialInformation> financialInfoList, PolicyType policy) {
		List<SbmErrWarningLogDTO> prorationValidationErrors = new ArrayList<SbmErrWarningLogDTO>();
		LocalDate policyStartDate = DateTimeUtil.getLocalDateFromXmlGC(policy.getPolicyStartDate());
		String qhpId = policy.getQHPId();
		String planYear = String.valueOf(policyStartDate.getYear());
		LOG.info("Retrieving metal level for QHP Id {}, {} ",  qhpId, planYear);
		String metalLevel = sbmDataService.getMetalLevelByQhpid(qhpId, planYear);
		LOG.info("metalLevel: " + metalLevel);
		Map<String,Runnable>commands = new HashMap<>();
		financialInfoList.forEach(financialInfo -> {
			LocalDate finStartDt = DateTimeUtil.getLocalDateFromXmlGC(financialInfo.getFinancialEffectiveStartDate());
			LocalDate finEndDt = DateTimeUtil.getLocalDateFromXmlGC(financialInfo.getFinancialEffectiveEndDate());
			commands.put("R018", () -> validatePartialMonthDates(finStartDt, finEndDt, financialInfo, prorationValidationErrors));
			commands.put("R019", () -> validatePartialMonthStartForSameMonth(financialInfo, financialInfoList, prorationValidationErrors));
			commands.put("R020", () -> validatePartialMonthEndForSameMonth(financialInfo, financialInfoList, prorationValidationErrors));
			commands.put("R021", () -> validatePartialMonthStart(financialInfo, finStartDt, prorationValidationErrors));
			commands.put("R022", () -> validatePartialMonthEnd(financialInfo, finEndDt, prorationValidationErrors));
			commands.put("R023", () -> validateProratedNodesSameMonth(financialInfo, prorationValidationErrors));
			commands.put("R024", () -> validateProratedNodeAcrossMonths(financialInfo, prorationValidationErrors));
			commands.put("R025", () -> validateProratedPremium(financialInfo, prorationValidationErrors));
			commands.put("R026", () -> validateProratedAptc(financialInfo, prorationValidationErrors));
			commands.put("R027", () -> validateProratedAptcGreaterThanTPA(financialInfo, prorationValidationErrors));
			commands.put("R028", () -> validateAptcGreaterThanProratedTPA(financialInfo, prorationValidationErrors));
			commands.put("R030", () -> validateProratedCSRVariant(financialInfo, prorationValidationErrors));
			commands.put("R032", () -> calculateProratedCSR(financialInfo, metalLevel, planYear, prorationValidationErrors));
			sbmBusinessRules.forEach(businessRuleCd -> {
				if(commands.containsKey(businessRuleCd.toUpperCase())){
				   commands.get(businessRuleCd.toUpperCase()).run();
				}
		   	});
		});		
		return prorationValidationErrors;
	}
	/*
	 * Validate that each partial month dates on the prorated node are tied to either the financial start date or fiananical end date of the parent node for the same month
	 */
	private void validatePartialMonthDates(LocalDate finStartDt, LocalDate finEndDt, FinancialInformation financialInfo,
			List<SbmErrWarningLogDTO> prorationValidationErrors) {
		financialInfo.getProratedAmount().forEach(proratedAmount -> {
			LocalDate partialMonthStartDt = DateTimeUtil.getLocalDateFromXmlGC(proratedAmount.getPartialMonthEffectiveStartDate());
			LocalDate partialMonthEndDt = DateTimeUtil.getLocalDateFromXmlGC(proratedAmount.getPartialMonthEffectiveEndDate());
			if(!(partialMonthStartDt.compareTo(finStartDt) == 0 || partialMonthEndDt.compareTo(finEndDt) == 0)) {
				if(!(partialMonthStartDt.compareTo(finStartDt) == 0)) {
					//create Error ER-045: incorrect value provided
					prorationValidationErrors.add(SbmValidationUtil.createErrorWarningLogDTO(
							PART_MNTH_EFFECT_DATE,
							SBMErrorWarningCode.ER_045.getCode(),
							ERROR_DESC_INCORRECT_VALUE + partialMonthStartDt));
					LOG.info("ProratedAmount misaligned with parent FinancialInformation {} ", partialMonthStartDt);
				}
				if(!(partialMonthEndDt.compareTo(finEndDt) == 0)) {
					//create Error ER-045: incorrect value provided
					prorationValidationErrors.add(SbmValidationUtil.createErrorWarningLogDTO(
							PART_EFFECT_END_DATE,
							SBMErrorWarningCode.ER_045.getCode(),
							ERROR_DESC_INCORRECT_VALUE + partialMonthEndDt));
					LOG.info("ProratedAmount misaligned with parent FinancialInformation {} ", partialMonthEndDt);
				}
			}
		});
	}
	/*
	 * FR-FM-PP-SBMI-463, FR-FM-PP-SBMI-464: Validate for the second part of the prorated amount when the parent financial node has the monthly amount and the first part of the prorated node is provided on the previous parent financial node
	 */
	private void validatePartialMonthStartForSameMonth(FinancialInformation financialInfo, List<FinancialInformation> financialInfoList,
			List<SbmErrWarningLogDTO> prorationValidationErrors) {
		List<ProratedAmountType> proratedAmounts = financialInfo.getProratedAmount();
		if (CollectionUtils.isNotEmpty(proratedAmounts)) {
			ProratedAmountType endPartialMonthAmt = proratedAmounts.get(proratedAmounts.size()-1);
			LocalDate partialMonthEndDt = DateTimeUtil.getLocalDateFromXmlGC(endPartialMonthAmt.getPartialMonthEffectiveEndDate());
			financialInfoList.forEach(financialInformation -> {
				LocalDate financialStart = DateTimeUtil.getLocalDateFromXmlGC(financialInformation.getFinancialEffectiveStartDate());
				if(financialStart.isAfter(partialMonthEndDt) && financialStart.getYear() == partialMonthEndDt.getYear() && financialStart.getMonthValue() == partialMonthEndDt.getMonthValue()) {
					List<ProratedAmountType> subsequentProratedAmounts = financialInformation.getProratedAmount();
					ProratedAmountType subsequentPartialMonthAmt = null;
					if(!subsequentProratedAmounts.isEmpty()) {
						subsequentPartialMonthAmt = subsequentProratedAmounts.get(0);
					}
					if(conditionOne(endPartialMonthAmt,financialInformation,subsequentPartialMonthAmt)||
							conditionTwo(endPartialMonthAmt,financialInformation,subsequentPartialMonthAmt)||
							conditionThree(endPartialMonthAmt,financialInformation,subsequentPartialMonthAmt)){
					  //create Error ER-046: incorrect value provided
						prorationValidationErrors.add(SbmValidationUtil.createErrorWarningLogDTO(
								"FinancialInformation",
								SBMErrorWarningCode.ER_046.getCode(),
								"Financial information node starting " + financialStart + " is missing corresponding prorated amount node for month."));
						LOG.info("ProratedAmount node missing for month {} ", financialStart);
					}
				}
			});
		}
	}
	private boolean conditionThree(ProratedAmountType endPartialMonthAmt, FinancialInformation financialInformation,
		ProratedAmountType subsequentPartialMonthAmt) {
		if (endPartialMonthAmt.getPartialMonthPremiumAmount() != null 
				&& endPartialMonthAmt.getPartialMonthPremiumAmount().compareTo(BigDecimal.ZERO) > 0   
				&& financialInformation.getMonthlyTotalPremiumAmount() != null
				&& financialInformation.getMonthlyTotalPremiumAmount().compareTo(BigDecimal.ZERO) > 0
				&& (subsequentPartialMonthAmt == null
						|| subsequentPartialMonthAmt.getPartialMonthPremiumAmount() == null
						|| subsequentPartialMonthAmt.getPartialMonthPremiumAmount().compareTo(BigDecimal.ZERO) == 0)){
			return true;
		}
	return false;
}
private boolean conditionTwo(ProratedAmountType endPartialMonthAmt, FinancialInformation financialInformation,
		ProratedAmountType subsequentPartialMonthAmt) {
		if (endPartialMonthAmt.getPartialMonthCSRAmount() != null 
				&& endPartialMonthAmt.getPartialMonthCSRAmount().compareTo(BigDecimal.ZERO) > 0
				&& financialInformation.getMonthlyCSRAmount() != null
				&& financialInformation.getMonthlyCSRAmount().compareTo(BigDecimal.ZERO) > 0
				&& (subsequentPartialMonthAmt == null
						|| subsequentPartialMonthAmt.getPartialMonthCSRAmount() == null
						|| subsequentPartialMonthAmt.getPartialMonthCSRAmount().compareTo(BigDecimal.ZERO) == 0)){
				return true;	
				}
	return false;
}
private boolean conditionOne(ProratedAmountType endPartialMonthAmt, FinancialInformation financialInformation,
			ProratedAmountType subsequentPartialMonthAmt) {
		if(endPartialMonthAmt.getPartialMonthAPTCAmount() != null 
				&& endPartialMonthAmt.getPartialMonthAPTCAmount().compareTo(BigDecimal.ZERO) > 0
				&& financialInformation.getMonthlyAPTCAmount() != null
				&& financialInformation.getMonthlyAPTCAmount().compareTo(BigDecimal.ZERO) > 0
				&& (subsequentPartialMonthAmt == null
						|| subsequentPartialMonthAmt.getPartialMonthAPTCAmount() == null 
						|| subsequentPartialMonthAmt.getPartialMonthAPTCAmount().compareTo(BigDecimal.ZERO) == 0)){
				return true;
				}
		return false;
	}
	/*
	 * FR-FM-PP-SBMI-505, FR-FM-PP-SBMI-506: Validate for the first part of the prorated amount when the parent financial node has the monthly amount and the second part of the prorated node is provided on the subsequent parent financial node
	 */
	private void validatePartialMonthEndForSameMonth(
			FinancialInformation financialInfo, List<FinancialInformation> financialInfoList,
			List<SbmErrWarningLogDTO> prorationValidationErrors) {
		List<ProratedAmountType> proratedAmounts = financialInfo.getProratedAmount();
		if (CollectionUtils.isNotEmpty(proratedAmounts)) {
			ProratedAmountType beginPartialMonthAmt = proratedAmounts.get(0);
			LocalDate partialMonthStartDt = DateTimeUtil.getLocalDateFromXmlGC(beginPartialMonthAmt.getPartialMonthEffectiveStartDate());
			financialInfoList.forEach(financialInformation -> {
				LocalDate financialEnd = DateTimeUtil.getLocalDateFromXmlGC(financialInformation.getFinancialEffectiveEndDate());
				if(financialEnd.isBefore(partialMonthStartDt) && financialEnd.getYear() == partialMonthStartDt.getYear() && financialEnd.getMonthValue() == partialMonthStartDt.getMonthValue()) {
					List<ProratedAmountType> precedingProratedAmounts = financialInformation.getProratedAmount();
					ProratedAmountType precedingPartialMonthAmt = null;
					if(!precedingProratedAmounts.isEmpty()) {
						precedingPartialMonthAmt = precedingProratedAmounts.get(precedingProratedAmounts.size()-1);
					}
                    if(routOne(beginPartialMonthAmt,financialInformation,precedingPartialMonthAmt)||
                       routTwo(beginPartialMonthAmt,financialInformation,precedingPartialMonthAmt)||
                       routThree(beginPartialMonthAmt,financialInformation,precedingPartialMonthAmt)){
                    	//create Error ER-046: incorrect value provided
						prorationValidationErrors.add(SbmValidationUtil.createErrorWarningLogDTO(
								"FinancialInformation",
								SBMErrorWarningCode.ER_046.getCode(),
								"Financial information node ending " + financialEnd + " is missing corresponding prorated amount node for month."));
						LOG.info("ProratedAmount node missing for month {} ", financialEnd);
                    }
				}
			});
		}
	}
	private boolean routThree(ProratedAmountType beginPartialMonthAmt, FinancialInformation financialInformation,
			ProratedAmountType precedingPartialMonthAmt) {
		 if (beginPartialMonthAmt.getPartialMonthPremiumAmount() != null 
					&& beginPartialMonthAmt.getPartialMonthPremiumAmount().compareTo(BigDecimal.ZERO) > 0
					&& financialInformation.getMonthlyTotalPremiumAmount() != null
					&& financialInformation.getMonthlyTotalPremiumAmount().compareTo(BigDecimal.ZERO) > 0
					&& (precedingPartialMonthAmt == null
							|| precedingPartialMonthAmt.getPartialMonthPremiumAmount() == null
							|| precedingPartialMonthAmt.getPartialMonthPremiumAmount().compareTo(BigDecimal.ZERO) == 0)) {
		     return true;						
		 }
		return false;
	}
	private boolean routTwo(ProratedAmountType beginPartialMonthAmt, FinancialInformation financialInformation,
			ProratedAmountType precedingPartialMonthAmt) {
		if(beginPartialMonthAmt.getPartialMonthCSRAmount() != null 
				&& beginPartialMonthAmt.getPartialMonthCSRAmount().compareTo(BigDecimal.ZERO) > 0
				&& financialInformation.getMonthlyCSRAmount() != null
				&& financialInformation.getMonthlyCSRAmount().compareTo(BigDecimal.ZERO) > 0
				&& (precedingPartialMonthAmt == null
						|| precedingPartialMonthAmt.getPartialMonthCSRAmount() == null
						|| precedingPartialMonthAmt.getPartialMonthCSRAmount().compareTo(BigDecimal.ZERO) == 0)){
			return true;
		}
		return false;
	}
	private boolean routOne(ProratedAmountType beginPartialMonthAmt, FinancialInformation financialInformation,
			ProratedAmountType precedingPartialMonthAmt) {
		if(beginPartialMonthAmt.getPartialMonthAPTCAmount() != null 
				&& beginPartialMonthAmt.getPartialMonthAPTCAmount().compareTo(BigDecimal.ZERO) > 0
				&& financialInformation.getMonthlyAPTCAmount() != null
				&& financialInformation.getMonthlyAPTCAmount().compareTo(BigDecimal.ZERO) > 0
				&& (precedingPartialMonthAmt == null
						|| precedingPartialMonthAmt.getPartialMonthAPTCAmount() == null 
						|| precedingPartialMonthAmt.getPartialMonthAPTCAmount().compareTo(BigDecimal.ZERO) == 0)){
			return true;
		}
		return false;
	}
	/*
	 * Validate that the partial month start date is greater than or equal to the financial start date of the parent node
	 */
	private void validatePartialMonthStart(FinancialInformation financialInfo, LocalDate finStartDt,List<SbmErrWarningLogDTO> prorationValidationErrors) {
		financialInfo.getProratedAmount().forEach(proratedAmount -> {
			LocalDate partialMonthStartDt = DateTimeUtil.getLocalDateFromXmlGC(proratedAmount.getPartialMonthEffectiveStartDate());
			if(partialMonthStartDt.getMonthValue() == finStartDt.getMonthValue() 
					&& partialMonthStartDt.isBefore(finStartDt)) {
				//create Error ER-047: incorrect value provided
				prorationValidationErrors.add(SbmValidationUtil.createErrorWarningLogDTO(PART_MNTH_EFFECT_DATE,
								SBMErrorWarningCode.ER_047.getCode(), ERROR_DESC_INCORRECT_VALUE,
								ERROR_INFO_EXPECTED_VALUE_TXT
										.concat("Must be greater than or equal to the "+FIN_EFFECT_DATE
												+ finStartDt.toString())));
				LOG.info(PART_MNTH_EFFECT_DATE+" precedes "+FIN_EFFECT_DATE+" {}, {} ",
						partialMonthStartDt, finStartDt);
			}
		});
	}
	/*
	 * Validate that the partial month end date is less than or equal to the financial end date of the parent node
	 */
	private void validatePartialMonthEnd(FinancialInformation financialInfo, LocalDate finEndDt,List<SbmErrWarningLogDTO> prorationValidationErrors) {
		financialInfo.getProratedAmount().forEach(proratedAmount -> {
			LocalDate partialMonthEndDt = DateTimeUtil.getLocalDateFromXmlGC(proratedAmount.getPartialMonthEffectiveEndDate());
			if(partialMonthEndDt.getMonthValue() == finEndDt.getMonthValue() 
					&& partialMonthEndDt.isAfter(finEndDt)) {
				//create Error ER-048: incorrect value provided
				prorationValidationErrors.add(SbmValidationUtil.createErrorWarningLogDTO(PART_EFFECT_END_DATE,
								SBMErrorWarningCode.ER_048.getCode(), ERROR_DESC_INCORRECT_VALUE,
								ERROR_INFO_EXPECTED_VALUE_TXT
										.concat("Must be less than or equal to the FinancialEffectiveEndDate "
												+ finEndDt.toString())));
				LOG.info(PART_EFFECT_END_DATE+" exceeds FinancialEffectiveEndDate {}, {} ",
						partialMonthEndDt, finEndDt);
			}
		});
	}
	/*
	 * Validate for same month across multiple prorated nodes under one parent financial node 
	 */
	private void validateProratedNodesSameMonth(FinancialInformation financialInfo,List<SbmErrWarningLogDTO> prorationValidationErrors) {
		List<ProratedAmountType> proratedAmounts = financialInfo.getProratedAmount();
		if(proratedAmounts.size() > 1) {
			int month1 = proratedAmounts.get(0).getPartialMonthEffectiveStartDate().getMonth();
			int month2 = proratedAmounts.get(proratedAmounts.size()-1).getPartialMonthEffectiveStartDate().getMonth();
			if(month1 == month2) {
				//create Error ER-029: incorrect value provided
				prorationValidationErrors.add(SbmValidationUtil.createErrorWarningLogDTO("PartialMonthEffectiveStartDate",
								SBMErrorWarningCode.ER_029.getCode(), ERROR_DESC_INCORRECT_VALUE));
				LOG.info("Multiple prorated nodes under one financial node cannot share the same month {} ",
						proratedAmounts.get(proratedAmounts.size()-1).getPartialMonthEffectiveStartDate());
			}
		}
	}
	/*
	 * Validate for prorated nodes that span multiple months
	 */
	private void validateProratedNodeAcrossMonths(FinancialInformation financialInfo,List<SbmErrWarningLogDTO> prorationValidationErrors) {
		financialInfo.getProratedAmount().forEach(proratedAmount -> {
			if(proratedAmount.getPartialMonthEffectiveStartDate().getMonth() != proratedAmount.getPartialMonthEffectiveEndDate().getMonth()) {
				//create Error ER-030: incorrect value provided
				prorationValidationErrors.add(SbmValidationUtil.createErrorWarningLogDTO(PART_EFFECT_END_DATE,
								SBMErrorWarningCode.ER_030.getCode(), ERROR_DESC_INCORRECT_VALUE));
				LOG.info("Prorated nodes cannot span multiple months {}, {} ",
						proratedAmount.getPartialMonthEffectiveStartDate(), proratedAmount.getPartialMonthEffectiveEndDate());
			}
		});
	}
	/*
	 * Validate for PartialPremiumAmount less than or equal to MonthlyTotalPremiumAmount
	 */
	private void validateProratedPremium(FinancialInformation financialInfo,List<SbmErrWarningLogDTO> prorationValidationErrors) {
		financialInfo.getProratedAmount().forEach(proratedAmount -> {
			if(proratedAmount.getPartialMonthPremiumAmount().compareTo(financialInfo.getMonthlyTotalPremiumAmount()) > 0) {
				//create Error ER-049: incorrect value provided
				prorationValidationErrors.add(SbmValidationUtil.createErrorWarningLogDTO("PartialMonthPremiumAmount",
								SBMErrorWarningCode.ER_049.getCode(), ERROR_DESC_INCORRECT_VALUE,
								ERROR_INFO_EXPECTED_VALUE_TXT
										.concat("Must be less than or equal to the MonthlyTotalPremiumAmount "
												+ financialInfo.getMonthlyTotalPremiumAmount())));
				LOG.info("PartialMonthPremiumAmount exceeds MonthlyTotalPremiumAmount {}, {} ",
						proratedAmount.getPartialMonthPremiumAmount(), financialInfo.getMonthlyTotalPremiumAmount());
			}
		});
	}
	/*
	 * Validate for MonthlyAPTCAmount does not exceed the PartialMonthPRemiumAmount when PartialMonthAPTC is not provided
	 */
	private void validateAptcGreaterThanProratedTPA(FinancialInformation financialInfo,List<SbmErrWarningLogDTO> prorationValidationErrors) {
		financialInfo.getProratedAmount().forEach(proratedAmount -> {
			if(financialInfo.getMonthlyAPTCAmount().compareTo(proratedAmount.getPartialMonthPremiumAmount()) > 0
					&& (proratedAmount.getPartialMonthAPTCAmount() == null || proratedAmount.getPartialMonthAPTCAmount().compareTo(BigDecimal.ZERO) == 0)) {
				//create Error ER-050: incorrect value provided
				prorationValidationErrors.add(SbmValidationUtil.createErrorWarningLogDTO("MonthlyAPTCAmount",
								SBMErrorWarningCode.ER_050.getCode(), ERROR_DESC_INCORRECT_VALUE,
								ERROR_INFO_EXPECTED_VALUE_TXT
										.concat("Must be less than or equal to the PartialMonthPremiumAmount "
												+ proratedAmount.getPartialMonthPremiumAmount())));
				LOG.info("MonthlyAPTCAmount exceeds PartialMonthPremiumAmount {}, {} ",
						financialInfo.getMonthlyAPTCAmount(), proratedAmount.getPartialMonthPremiumAmount());
			}
		});
	}
	/*
	 * Validate for PartialMonthAPTCAmount less than or equal to MonthlyAPTC when PartialMonthAPTC exists.
	 */
	private void validateProratedAptc(FinancialInformation financialInfo,List<SbmErrWarningLogDTO> prorationValidationErrors) {
		financialInfo.getProratedAmount().forEach(proratedAmount -> {
			if(((proratedAmount.getPartialMonthAPTCAmount() != null && proratedAmount.getPartialMonthAPTCAmount().compareTo(BigDecimal.ZERO) > 0)) 
					&& proratedAmount.getPartialMonthAPTCAmount().compareTo(financialInfo.getMonthlyAPTCAmount()) > 0) {
				//create Error ER-051: incorrect value provided
				prorationValidationErrors
						.add(SbmValidationUtil.createErrorWarningLogDTO("PartialMonthAPTCAmount",
								SBMErrorWarningCode.ER_051.getCode(), ERROR_DESC_INCORRECT_VALUE,
								ERROR_INFO_EXPECTED_VALUE_TXT
										.concat("Must be less than or equal to the MonthlyAPTCAmount "
												+ financialInfo.getMonthlyAPTCAmount())));
				LOG.info("PartialMonthAPTCAmount exceeds MonthlyAPTCAmount {}, {} ",
						financialInfo.getMonthlyAPTCAmount(), financialInfo.getMonthlyAPTCAmount());
			}
		});
	}
	/*
	 * Validate for PartialMonthAPTCAmount less than or equal to the PartialMonthPremiumAmount when PartialMonthAPTC exists
	 */
	private void validateProratedAptcGreaterThanTPA(FinancialInformation financialInfo,List<SbmErrWarningLogDTO> prorationValidationErrors) {
		financialInfo.getProratedAmount().forEach(proratedAmount -> {
			if(proratedAmount.getPartialMonthAPTCAmount() != null
					&& proratedAmount.getPartialMonthAPTCAmount().compareTo(proratedAmount.getPartialMonthPremiumAmount()) > 0) {
				//create Error ER-052: incorrect value provided
				prorationValidationErrors
						.add(SbmValidationUtil.createErrorWarningLogDTO("PartialMonthAPTCAmount",
								SBMErrorWarningCode.ER_052.getCode(), ERROR_DESC_INCORRECT_VALUE,
								ERROR_INFO_EXPECTED_VALUE_TXT
										.concat("Must be less than or equal to the PartialMonthPremiumAmount "
												+ proratedAmount.getPartialMonthPremiumAmount())));
				LOG.info("PartialMonthAPTCAmount exceeds PartialMonthPremiumAmount {}, {} ",
						proratedAmount.getPartialMonthAPTCAmount(), proratedAmount.getPartialMonthPremiumAmount());
			}
		});
	}
	/*
	 * Validate CSRVariantID is 02-06 when PartialMonthCSRAmount is provided
	 */
	private void validateProratedCSRVariant(FinancialInformation financialInfo,List<SbmErrWarningLogDTO> prorationValidationErrors) {
		financialInfo.getProratedAmount().forEach(proratedAmount -> {
			if(proratedAmount.getPartialMonthCSRAmount() != null && proratedAmount.getPartialMonthCSRAmount().compareTo(BigDecimal.ZERO) > 0
					&& !SbmDataUtil.isCsrVariant(financialInfo.getCSRVariantId())) {
				//create Error ER-041: MonthlyCSRAmount Not Expected
				prorationValidationErrors.add(SbmValidationUtil.createErrorWarningLogDTO("PartialMonthCSRAmount",
						SBMErrorWarningCode.ER_053.getCode(), 
						ERROR_INFO_EXPECTED_VALUE_TXT.concat("PartialMonthCSRAmount should not be provided")));
				LOG.info("PartialMonthCSRAmount Not Expected");
			}
		});
	}
	/*
	 * Validate that the PartialMonthCSRAmount provided matches the system calculated PartialMonthCSRAmount
	 */
	private void calculateProratedCSR(FinancialInformation financialInfo, String metalLevel, String planYear,List<SbmErrWarningLogDTO> prorationValidationErrors) {
		List<ProratedAmountType> proratedAmounts = financialInfo.getProratedAmount();
		if(proratedAmounts.isEmpty()) {
			return;
		}
		String variantId = financialInfo.getCSRVariantId();
		LOG.info("CSR variantId: " + variantId);
		BigDecimal multiplier = null;
		validateSilverPlan(variantId, metalLevel, prorationValidationErrors);
		multiplier=retrieveMultiplier(variantId,multiplier,planYear,metalLevel);
		if (multiplier != null) {
			for(ProratedAmountType proratedAmount : proratedAmounts) {
				LOG.info("inbound Partial Month Csr Amount: " + proratedAmount.getPartialMonthCSRAmount());
				BigDecimal inboundPartialCsr = proratedAmount.getPartialMonthCSRAmount() == null ? BigDecimal.ZERO:proratedAmount.getPartialMonthCSRAmount();
				if(!(inboundPartialCsr.compareTo(BigDecimal.ZERO) == 0 && VARIANT_ID_01.equals(variantId))) {
					BigDecimal calculatedPartialCsr = proratedAmount.getPartialMonthPremiumAmount().multiply(multiplier).setScale(2, RoundingMode.HALF_UP);
					LOG.info("calculated Partial CSR Amount: " + calculatedPartialCsr);
					if(calculatedPartialCsr.compareTo(inboundPartialCsr) != 0) {
						proratedAmount.setPartialMonthCSRAmount(calculatedPartialCsr);
						//create Warning WR-008: PartialMonthCSRAmount corrected by CMS
						prorationValidationErrors.add(SbmValidationUtil.createErrorWarningLogDTO("PartialMonthCSRAmount",
								SBMErrorWarningCode.WR_008.getCode(), ERROR_INFO_EXPECTED_VALUE_TXT + calculatedPartialCsr));
						LOG.info("PartialMonthCSRAmount corrected by CMS");
					}
				}
			}
		}
	}
	private BigDecimal retrieveMultiplier(String variantId, BigDecimal multiplier, String planYear, String metalLevel) {
		if (VARIANT_ID_04.equals(variantId) || VARIANT_ID_05.equals(variantId) || VARIANT_ID_06.equals(variantId)) {
			multiplier = sbmDataService.getCsrMultiplierByVariant(variantId, planYear);
		}
		if (metalLevel!= null && (VARIANT_ID_02.equals(variantId) || VARIANT_ID_03.equals(variantId))) {
			multiplier = sbmDataService.getCsrMultiplierByVariantAndMetal(variantId, metalLevel, planYear);
		}
		LOG.info("CSR multiplier: " + multiplier);
		return multiplier;
	}
	/*
	 * Perform state specific validations configured in database
	 */
	private List<SbmErrWarningLogDTO> performStateSpecificValidations(List<FinancialInformation> financialInfoList, PolicyType policy) {
		List<SbmErrWarningLogDTO> financialValidationErrors = new ArrayList<SbmErrWarningLogDTO>();
		if(CollectionUtils.isNotEmpty(sbmBusinessRules)) {
			LocalDate finStartDt = DateTimeUtil.getLocalDateFromXmlGC(financialInfoList.get(0).getFinancialEffectiveStartDate());
			LocalDate finEndDt = DateTimeUtil.getLocalDateFromXmlGC(financialInfoList.get(financialInfoList.size()-1).getFinancialEffectiveEndDate());
			LocalDate policyStartDt = DateTimeUtil.getLocalDateFromXmlGC(policy.getPolicyStartDate());
			LocalDate policyEndDt = DateTimeUtil.getLocalDateFromXmlGC(policy.getPolicyEndDate());
			String effectuationInd = policy.getEffectuationIndicator();
			sbmBusinessRules.forEach(businessRuleCd -> {
				switch(businessRuleCd.toUpperCase()) {
				case "R003":
					//FR-FM-PP-SBMI-237: R003 - Validate for earliest financial start date equal to policy start
					validateFinStartEqPolicyStart(finStartDt, policyStartDt, financialValidationErrors);
					break;
				case "R006":
					//FR-FM-PP-SBMI-496: R006 - Validate for earliest financial start to be no more one month of the policy start (colorado)
					validateFinStartMonth(finStartDt, policyStartDt, financialValidationErrors);
					break;
				case "R007":
					//FR-FM-PP-SBMI-502: R007 - Validate for earliest financial start not to be less than the policy start (colorado)
					validateFinStartBeforePolicy(finStartDt, policyStartDt, financialValidationErrors);
					break;
				case "R004":
					//FR-FM-PP-SBMI-239: R004	Validate for latest financial end date equal to policy end
					if (effectuationInd != null && effectuationInd.equalsIgnoreCase(Y)) {
						validateFinEndEqPolicyEnd(finEndDt, policyEndDt, financialValidationErrors);
					}
					break;
				default:
					break;
				}
			});
			//validate each Financial Node
			validateFinancialNodes(financialInfoList, policy, sbmBusinessRules, financialValidationErrors);
		}
		return financialValidationErrors;
	}
	/*
	 * Validate Indidvidual FinancialInfrmation Nodes
	 */
	private void validateFinancialNodes(List<FinancialInformation> financialInfoList, PolicyType policy,List<String> sbmBusinessRules, List<SbmErrWarningLogDTO> financialValidationErrors) {
		LocalDate policyStartDate = DateTimeUtil.getLocalDateFromXmlGC(policy.getPolicyStartDate());
		LocalDate policyEndDt = DateTimeUtil.getLocalDateFromXmlGC(policy.getPolicyEndDate());
		String effectuationInd = policy.getEffectuationIndicator();
		String qhpId = policy.getQHPId();
		String planYear = String.valueOf(policyStartDate.getYear());
		String metalLevel = sbmDataService.getMetalLevelByQhpid(qhpId, planYear);
       Map<String, Runnable> commands = new HashMap<>();
		financialInfoList.forEach(financialInfo -> {
			LocalDate finStartDt = DateTimeUtil.getLocalDateFromXmlGC(financialInfo.getFinancialEffectiveStartDate());
			commands.put("R008", () -> validateFinStartAfterPolicyEnd(finStartDt, policyEndDt, financialValidationErrors));
			commands.put("R009", () -> validateMonthlyAPTC(financialInfo, financialValidationErrors));
			commands.put("R010", () -> validateMonthlyTPA(financialInfo, financialValidationErrors));
			commands.put("R012", () -> validateCSRMissing(financialInfo, financialValidationErrors));
			commands.put("R011", () -> validateCSRVariant(financialInfo, financialValidationErrors));
			commands.put("R013", () -> validateNonCSRVariant(financialInfo, financialValidationErrors));
			commands.put("R014", () -> validateSilverPlan(financialInfo.getCSRVariantId(), metalLevel, financialValidationErrors));
			commands.put("R068", () -> validateMetalLevelForCsr0203(financialInfo.getCSRVariantId(), metalLevel, financialValidationErrors));
			commands.put("R015", () -> calculateCsr(financialInfo, metalLevel, planYear, financialValidationErrors));
			commands.put("R067", () -> validateRatingArea(financialInfo, financialValidationErrors));
			sbmBusinessRules.forEach(businessRuleCd -> {
				LOG.info("Commands size "+commands.size());
				if(commands.containsKey(businessRuleCd.toUpperCase())){
					if(!businessRuleCd.toUpperCase().equals("R008"))
					   commands.get(businessRuleCd.toUpperCase()).run();
					else if(businessRuleCd.toUpperCase().equals("R008")&&effectuationInd != null && effectuationInd.equalsIgnoreCase(Y)){
						commands.get(businessRuleCd.toUpperCase()).run();
					}
				}
			});
		});		
	}
	/*
	 * Validate for earliest financial start date equal to policy start
	 */
	private void validateFinStartEqPolicyStart(LocalDate finStartDt, LocalDate policyStartDt,List<SbmErrWarningLogDTO> financialValidationErrors) {
		if(!finStartDt.isEqual(policyStartDt)) {
			//create Error ER-035: incorrect value provided
			financialValidationErrors.add(SbmValidationUtil.createErrorWarningLogDTO(FIN_EFFECT_DATE,
					SBMErrorWarningCode.ER_035.getCode(),
					ERROR_DESC_INCORRECT_VALUE.concat(finStartDt.toString()), 
					ERROR_INFO_EXPECTED_VALUE_TXT.concat("Must be equal to the PolicyStartDate " + policyStartDt.toString())));
			LOG.info("Financial Effective Start Date to Policy Start Date validation error {}, {} ", 
					finStartDt, policyStartDt);
		}
	}
	/*
	 * Validate for earliest financial start to be no more than one month of the policy start (colorado)
	 */
	private void validateFinStartMonth(LocalDate finStartDt, LocalDate policyStartDt,List<SbmErrWarningLogDTO> financialValidationErrors) {
		if(finStartDt.isAfter(policyStartDt.plusMonths(1).with(TemporalAdjusters.lastDayOfMonth()))) {
			//create Error ER-058: incorrect value provided
			financialValidationErrors.add(SbmValidationUtil.createErrorWarningLogDTO(FIN_EFFECT_DATE,
					SBMErrorWarningCode.ER_058.getCode(),
					ERROR_DESC_INCORRECT_VALUE.concat(finStartDt.toString()), 
					ERROR_INFO_EXPECTED_VALUE_TXT.concat("No more than one month of PolicyStartDate " + policyStartDt.toString())));
			LOG.info("Earliest Financial Effective Start Date more than one month of Policy Start Date month {}, {} ", 
					finStartDt, policyStartDt);
		}
	}
	/*
	 * Validate for earliest financial start not to be less than the policy start (colorado)
	 */
	private void validateFinStartBeforePolicy(LocalDate finStartDt, LocalDate policyStartDt,List<SbmErrWarningLogDTO> financialValidationErrors) {
		if(finStartDt.isBefore(policyStartDt)) {
			//create Error ER-059: incorrect value provided
			financialValidationErrors.add(SbmValidationUtil.createErrorWarningLogDTO(FIN_EFFECT_DATE,
					SBMErrorWarningCode.ER_059.getCode(),
					ERROR_DESC_INCORRECT_VALUE.concat(finStartDt.toString()), 
					ERROR_INFO_EXPECTED_VALUE_TXT.concat("Must be equal to or no more than one month of PolicyStartDate " + policyStartDt.toString())));
			LOG.info("Earliest "+FIN_EFFECT_DATE+" less than PolicyStartDate {}, {} ", 
					finStartDt, policyStartDt);
		}
	}
	/*
	 * Validate for latest financial end date eqial to policy end
	 */
	private void validateFinEndEqPolicyEnd(LocalDate finEndDt, LocalDate policyEndDt,List<SbmErrWarningLogDTO> financialValidationErrors) {
		if(!(finEndDt.isEqual(policyEndDt) || finEndDt.getMonthValue() == policyEndDt.getMonthValue())) {
			//create Error ER-036: incorrect value provided
			financialValidationErrors.add(SbmValidationUtil.createErrorWarningLogDTO("FinancialEffectiveEndDate",
					SBMErrorWarningCode.ER_036.getCode(),
					ERROR_DESC_INCORRECT_VALUE.concat(finEndDt.toString()), 
					ERROR_INFO_EXPECTED_VALUE_TXT.concat("Must be equal to or end in the same month of the PolicyEndDate " + policyEndDt.toString())));
			LOG.info("Financial End misaligned with Policy {}, {} ", 
					finEndDt, policyEndDt);
		}
	}
	/*
	 * Validate for financial start date to be less than or equal to the policy end date for effectuated policies
	 */
	private void validateFinStartAfterPolicyEnd(LocalDate finStartDt, LocalDate policyEndDt,List<SbmErrWarningLogDTO> financialValidationErrors) {
		if(finStartDt.isAfter(policyEndDt)) {
			//create Error ER-037: incorrect value provided
			financialValidationErrors.add(SbmValidationUtil.createErrorWarningLogDTO(FIN_EFFECT_DATE,
					SBMErrorWarningCode.ER_037.getCode(),
					ERROR_DESC_INCORRECT_VALUE, 
					ERROR_INFO_EXPECTED_VALUE_TXT.concat("Must be less than or equal to the PolicyEndDate " + policyEndDt.toString())));
			LOG.info("Financial Start after Policy End {}, {} ", 
					finStartDt, policyEndDt);
		}
	}
	/*
	 * Validate that the Monthly APTC amount does not exceed the Monthly Total Premium amount
	 */
	private void validateMonthlyAPTC(FinancialInformation financialInfo,List<SbmErrWarningLogDTO> financialValidationErrors) {
		BigDecimal aptc = financialInfo.getMonthlyAPTCAmount();
		BigDecimal tpa = financialInfo.getMonthlyTotalPremiumAmount();
		if(aptc != null && aptc.compareTo(tpa) > 0 ) {
			//create Error ER-038: incorrect value provided
			financialValidationErrors.add(SbmValidationUtil.createErrorWarningLogDTO("MonthlyAPTCAmount",
					SBMErrorWarningCode.ER_038.getCode(),
					ERROR_DESC_INCORRECT_VALUE, 
					ERROR_INFO_EXPECTED_VALUE_TXT.concat("Must be less than or equal to the MonthlyTotalPremiumAmount " + tpa)));
			LOG.info("MonthlyAPTC exceeds Premium {}, {} ", aptc, tpa);
		}
	}
	/*
	 * Validate that the Total Premium amount is equal to the sum of MTIRA + MAPTC + MOP1 + MOP2
	 */
	private void validateMonthlyTPA(FinancialInformation financialInfo,List<SbmErrWarningLogDTO> financialValidationErrors) {
		BigDecimal tpa = financialInfo.getMonthlyTotalPremiumAmount();
		BigDecimal tira = financialInfo.getMonthlyTotalIndividualResponsibilityAmount();
		BigDecimal aptc = financialInfo.getMonthlyAPTCAmount() == null ? BigDecimal.ZERO : financialInfo.getMonthlyAPTCAmount();
		BigDecimal op1 = financialInfo.getMonthlyOtherPaymentAmount1() == null ? BigDecimal.ZERO : financialInfo.getMonthlyOtherPaymentAmount1();
		BigDecimal op2 = financialInfo.getMonthlyOtherPaymentAmount2() == null ? BigDecimal.ZERO : financialInfo.getMonthlyOtherPaymentAmount2();
		BigDecimal totalNonPremiumAmounts = tira.add(aptc).add(op1).add(op2);
		if(totalNonPremiumAmounts.compareTo(tpa) !=0) {
			//create Error ER-039: incorrect value provided
			financialValidationErrors.add(SbmValidationUtil.createErrorWarningLogDTO("MonthlyTotalPremiumAmount",
					SBMErrorWarningCode.ER_039.getCode(),
					ERROR_DESC_INCORRECT_VALUE));
			LOG.info("MonthlyPremium misaligned with underlying amounts {}, {} ", tpa, totalNonPremiumAmounts);
		}
	}
	/*
	 * Validate that the Variant ID is 01 when Monthly CSR amount is not provided
	 */
	private void validateCSRMissing(FinancialInformation financialInfo,List<SbmErrWarningLogDTO> financialValidationErrors) {
		if(financialInfo.getMonthlyCSRAmount() == null && !SbmDataUtil.isNonCsrVariant(financialInfo.getCSRVariantId())) {
			//create Error ER-040: MonthlyCSRAmount must be provided
			financialValidationErrors.add(SbmValidationUtil.createErrorWarningLogDTO(MNTH_CSR_AMT,
					SBMErrorWarningCode.ER_040.getCode(), MNTH_CSR_AMT+" must be provided"));
			LOG.info(MNTH_CSR_AMT+" Missing");
		}
	}
	/*
	 * Validate that the Variant ID is 02-06 when Monthly CSR amount is provided
	 */
	private void validateCSRVariant(FinancialInformation financialInfo,List<SbmErrWarningLogDTO> financialValidationErrors) {
		BigDecimal csrAmount = financialInfo.getMonthlyCSRAmount();
		if(csrAmount != null && csrAmount.compareTo(BigDecimal.ZERO) > 0
				&& !SbmDataUtil.isCsrVariant(financialInfo.getCSRVariantId())) {
			//create Error ER-041: MonthlyCSRAmount Not Expected
			financialValidationErrors.add(SbmValidationUtil.createErrorWarningLogDTO(MNTH_CSR_AMT,
					SBMErrorWarningCode.ER_041.getCode(), MNTH_CSR_AMT+" should not be provided"));
			LOG.info(MNTH_CSR_AMT+" Not Expected");
		}
	}
	/*
	 * Validate that the amount is equal to 0 if the Variant ID is 01 and the Monthly CSR amount is present
	 */
	private void validateNonCSRVariant(FinancialInformation financialInfo,List<SbmErrWarningLogDTO> financialValidationErrors) {
		if(financialInfo.getMonthlyCSRAmount() != null && SbmDataUtil.isNonCsrVariant(financialInfo.getCSRVariantId())) {
			if(financialInfo.getMonthlyCSRAmount().compareTo(BigDecimal.ZERO) == 0) {
				//create Warning WR-006: MonthlyCSRAmount sent as 0 for 01 variant
				financialValidationErrors.add(SbmValidationUtil.createErrorWarningLogDTO(MNTH_CSR_AMT,
						SBMErrorWarningCode.WR_006.getCode()));
				LOG.info(MNTH_CSR_AMT+" sent as 0 for 01 variant");
			}
		}
	}
	/*
	 * Validate that the QHPID is valid for silver plan when CSR variant id is 04-06
	 */
	private void validateSilverPlan(String variantId, String metalLevel,List<SbmErrWarningLogDTO> financialValidationErrors) {
		if (metalLevel!= null 
				&& (VARIANT_ID_04.equals(variantId) || VARIANT_ID_05.equals(variantId) || VARIANT_ID_06.equals(variantId))) {
			if(!metalLevel.equalsIgnoreCase(SbmMetalLevelType.SILVER.getValue())) {
				//create Error ER_042: QHPID and CSRVariantID mismatch
				financialValidationErrors.add(SbmValidationUtil.createErrorWarningLogDTO("QHPId",
						SBMErrorWarningCode.ER_042.getCode(), ERROR_DESC_INCORRECT_VALUE));
				LOG.info("QHPID and CSRVariantID mismatch: QHPId validation of silver plan error for CSR Variant Id 04-06 on Monthly CSR Amount calculation");
			}
		}
	}
	/*
	 * Validate that the QHPID is valid for metal level when CSR variant id is 02-03
	 */
	private void validateMetalLevelForCsr0203(String variantId, String metalLevel,List<SbmErrWarningLogDTO> financialValidationErrors) {
		if (metalLevel!= null 
				&& (VARIANT_ID_02.equals(variantId) || VARIANT_ID_03.equals(variantId))) {
			if(!(metalLevel.equalsIgnoreCase(SbmMetalLevelType.PLATINUM.getValue())
					|| metalLevel.equalsIgnoreCase(SbmMetalLevelType.GOLD.getValue())
					|| metalLevel.equalsIgnoreCase(SbmMetalLevelType.SILVER.getValue())
					|| metalLevel.equalsIgnoreCase(SbmMetalLevelType.BRONZE.getValue())
					)) {
				//create Error ER_042: QHPID and CSRVariantID mismatch
				financialValidationErrors.add(SbmValidationUtil.createErrorWarningLogDTO("QHPId",
						SBMErrorWarningCode.ER_042.getCode(), ERROR_DESC_INCORRECT_VALUE));
				LOG.info("QHPID and CSRVariantID mismatch: QHPID validation of metal level plan error for CSR Variant Id 02-03");
			}
		}
	}
	/*
	 * Validate the provided Monthly CSR amount matches the system calculated Monthly CSR Amount
	 */
	private void calculateCsr(FinancialInformation financialInfo, String metalLevel, String planYear, 
			List<SbmErrWarningLogDTO> financialValidationErrors) {
		String variantId = financialInfo.getCSRVariantId();
		BigDecimal multiplier = null;
		BigDecimal inboundCsr = financialInfo.getMonthlyCSRAmount() == null ? BigDecimal.ZERO:financialInfo.getMonthlyCSRAmount();
		LOG.info("CSR variantId: " + variantId);
		LOG.info("inbound Csr Amount: " + financialInfo.getMonthlyCSRAmount());
		if (VARIANT_ID_04.equals(variantId) || VARIANT_ID_05.equals(variantId) || VARIANT_ID_06.equals(variantId)) {
			multiplier = sbmDataService.getCsrMultiplierByVariant(variantId, planYear);
		}
		if (metalLevel!= null 
				&& (VARIANT_ID_02.equals(variantId) || VARIANT_ID_03.equals(variantId))) {
			multiplier = sbmDataService.getCsrMultiplierByVariantAndMetal(variantId, metalLevel, planYear);
		}
		LOG.info("CSR multiplier: " + multiplier);
		if (multiplier != null) {
			BigDecimal calculatedCsr = financialInfo.getMonthlyTotalPremiumAmount().multiply(multiplier).setScale(2, RoundingMode.HALF_UP);
			LOG.info("calculated CSR Amount: " + calculatedCsr);
			if(calculatedCsr.compareTo(inboundCsr) != 0) {
				financialInfo.setMonthlyCSRAmount(calculatedCsr);
				//create Warning WR-007: MonthlyCSRAmount corrected by CMS
				financialValidationErrors.add(SbmValidationUtil.createErrorWarningLogDTO(MNTH_CSR_AMT,
						SBMErrorWarningCode.WR_007.getCode(), "System calculated Monthly CSRAmount: " + calculatedCsr));
				LOG.info(MNTH_CSR_AMT+" corrected by CMS");
			}
		}
	}
	/*
	 * Validate rating area length
	 */
	private void validateRatingArea(FinancialInformation financialInfo,
			List<SbmErrWarningLogDTO> financialValidationErrors) {
		String ratingArea = financialInfo.getRatingArea();
		if (StringUtils.isNotBlank(ratingArea) && !ratingArea.matches("R-[A-Z][A-Z][0-9][0-9][0-9]")) {
			//create Warning WR-005: incorrect value provided
			financialValidationErrors.add(SbmValidationUtil.createErrorWarningLogDTO(
					"RatingArea", SBMErrorWarningCode.WR_004.getCode(), ratingArea, "expected format: R-[A-Z][A-Z][0-9][0-9][0-9]"));
			financialInfo.setRatingArea(null);
			LOG.info("InValid format for RatingArea. Should be R-[A-Z][A-Z][0-9][0-9][0-9]");
		}
	}
	/**
	 * @param sbmDataService the sbmDataService to set
	 */
	public void setSbmDataService(SBMDataService sbmDataService) {
		this.sbmDataService = sbmDataService;
	}
}
