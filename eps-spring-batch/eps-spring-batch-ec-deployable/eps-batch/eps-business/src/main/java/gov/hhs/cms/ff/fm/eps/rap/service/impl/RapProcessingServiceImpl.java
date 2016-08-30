
/**
 * 
 */
package gov.hhs.cms.ff.fm.eps.rap.service.impl;

import static gov.hhs.cms.ff.fm.eps.rap.domain.RapConstants.APTC;
import static gov.hhs.cms.ff.fm.eps.rap.domain.RapConstants.COVERAGEPERIODPAID;
import static gov.hhs.cms.ff.fm.eps.rap.domain.RapConstants.CSR;
import static gov.hhs.cms.ff.fm.eps.rap.domain.RapConstants.ERC;
import static gov.hhs.cms.ff.fm.eps.rap.domain.RapConstants.HIGH_DATE;
import static gov.hhs.cms.ff.fm.eps.rap.domain.RapConstants.STATUS_APPROVED;
import static gov.hhs.cms.ff.fm.eps.rap.domain.RapConstants.STATUS_NOISE;
import static gov.hhs.cms.ff.fm.eps.rap.domain.RapConstants.STATUS_PENDING_APPROVAL;
import static gov.hhs.cms.ff.fm.eps.rap.domain.RapConstants.STATUS_PENDING_CYCLE;
import static gov.hhs.cms.ff.fm.eps.rap.domain.RapConstants.STATUS_REPLACED;
import static gov.hhs.cms.ff.fm.eps.rap.domain.RapConstants.TRANSPERIOD_RETROACTIVE;
import static gov.hhs.cms.ff.fm.eps.rap.domain.RapConstants.UF;

import gov.hhs.cms.ff.fm.eps.ep.enums.ProrationType;
import gov.hhs.cms.ff.fm.eps.rap.dao.RapDao;
import gov.hhs.cms.ff.fm.eps.rap.domain.IssuerUserFeeRate;
import gov.hhs.cms.ff.fm.eps.rap.domain.PolicyPremium;
import gov.hhs.cms.ff.fm.eps.rap.domain.RapConstants;
import gov.hhs.cms.ff.fm.eps.rap.dto.PolicyDataDTO;
import gov.hhs.cms.ff.fm.eps.rap.dto.PolicyDetailDTO;
import gov.hhs.cms.ff.fm.eps.rap.dto.PolicyPaymentTransDTO;
import gov.hhs.cms.ff.fm.eps.rap.service.RAPProcessingRequest;
import gov.hhs.cms.ff.fm.eps.rap.service.RAPProcessingResponse;
import gov.hhs.cms.ff.fm.eps.rap.service.RapProcessingService;
import gov.hhs.cms.ff.fm.eps.rap.util.CodeDecodesHelper;
import gov.hhs.cms.ff.fm.eps.rap.util.RapProcessingHelper;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.accenture.foundation.common.codetable.CodeRecord;
import com.accenture.foundation.common.exception.ApplicationException;
import com.accenture.foundation.common.exception.EnvironmentException;

/**
 * This is the main service class for the Retro Active Payment Processing.
 * It accepts the Policy version as input, compares the Payments that were made 
 * to the Payments that should have been made and creates Retroactive Payment transactions
 * 
 * @author girish.padmanabhan
 *
 */
public class RapProcessingServiceImpl implements RapProcessingService {
	private static final Logger LOGGER = LoggerFactory.getLogger(RapConstants.RAP_LOGGER);
	
	private CodeDecodesHelper codeDecodesHelper;
	private RapDao rapDao;
	List<DateTime> currentPmtMonth;
	List<DateTime> minMaxCoverageMonths;
	List<Long> reversalRefIds;
	List<Long> reversedTransIds;
	
	/**
	 * This method is the main method for processing the Retro Active Payments.
	 * It accepts the Policy version as input, compares the Payments that were made 
	 * to the Payments that should have been made and creates Retroactive Payment transactions
	 * 
	 * @param RAPProcessingRequest
	 * @return RAPProcessingResponse
	 */
	@Override
	public RAPProcessingResponse processRetroActivePayments(RAPProcessingRequest request) throws ApplicationException, EnvironmentException {
		
		LOGGER.info("ENTER RapProcessingServiceImpl.processRetroActivePayments() ");
		RAPProcessingResponse response = new RAPProcessingResponse();
		
		PolicyDataDTO policyVersion = request.getPolicyDataDTO();
		LOGGER.info("policyVersion: "+ policyVersion);
		
		currentPmtMonth = getPaymentMonthERC();
		
		minMaxCoverageMonths = determineMinMaxCoverageMonth(policyVersion);
		
		PolicyDetailDTO policyPaymentDetails = getPolicyPaymemntsData(policyVersion);
		
		//Evaluate the Payment Transactions and create Retro Active Payments for the policy version
		List<PolicyPaymentTransDTO> retroActivePayments = createRetroActivePaymentTxns(policyVersion, policyPaymentDetails);
		
		//create the RetroActive Payment Transactions
		response.getPolicyPaymentTransactions().addAll(retroActivePayments);
		
		LOGGER.debug("response: ", response);
		LOGGER.info("EXIT RapProcessingServiceImpl.processRetroActivePayments(): ", response);
		
		return response;
	}	
	
	/*
	 * Determine whether the state is FFM prorating for the Payment Transaction year
	 */
	private boolean isStateFfmProrating(String stateCode, int paymentTransYear) {
		
		//return codeDecodesHelper.isStateProrating(stateCode, paymentTransYear);
		
		return RapProcessingHelper.getProrationType(stateCode, paymentTransYear).equals(ProrationType.FFM_PRORATING);
	}

	/*
	 * returns available policy payments
	 */
	private PolicyDetailDTO getPolicyPaymemntsData(PolicyDataDTO policyVersion) {

		PolicyDetailDTO policyPaymentDetails= rapDao.retrievePolicyPaymentData(policyVersion);

		return policyPaymentDetails;
	}
	
	/*
	 * Creates RetroActive payments
	 */	
	private List<PolicyPaymentTransDTO> createRetroActivePaymentTxns(
			PolicyDataDTO policyVersion, PolicyDetailDTO policyDetails) {
		
		LOGGER.info("ENTER createRetroActivePaymentTxns()");
		LOGGER.debug("policyPaymentDetails: ", policyDetails);
		
		reversedTransIds = new ArrayList<Long>();
		
		List<PolicyPaymentTransDTO> pmtTransList = new ArrayList<PolicyPaymentTransDTO>();
		List<PolicyPaymentTransDTO> policyPayments = policyDetails.getPolicyPayments();
		List<PolicyPremium> policyPremiums = policyDetails.getPolicyPremiums();

		Map<DateTime, List<PolicyPaymentTransDTO>> paymentsForMonths = getPaymentsForCoverageMonths(policyPayments);
		Map<DateTime, List<PolicyPremium>> premiumsForMonths = getPremiumsForCoverageMonths(policyPremiums, policyVersion);
		
		policyVersion.setPolicyCancelled(RapProcessingHelper.isPolicyUneffectuated(policyVersion));
			
		//reverse payments for policy start date change to later date
		if(CollectionUtils.isNotEmpty(paymentsForMonths.keySet())){
			reversePmtsPriorToPolicyStartDate(pmtTransList, paymentsForMonths, policyVersion);
		}
		
		//Evaluate Policy version through the coverage Months 
		for(DateTime coverageDate: minMaxCoverageMonths) {
			
			ProrationType prorationType = RapProcessingHelper.getProrationType(
					policyVersion.getSubscriberStateCd(), coverageDate.getYear());
			
			LOGGER.info("prorationType: "+ prorationType.getValue());
			
			List<PolicyPremium> premiumRecs = premiumsForMonths.get(coverageDate);
			
			//If state is not prorating or is an SBM with no prorated amounts  
			// use only the earliest premium record for the month as basis for creating payments
			// ie, only one payment for the month based on the oldest effective premium for the month
			if ((!prorationType.equals(ProrationType.SBM_PRORATING) && !isStateFfmProrating(policyVersion.getSubscriberStateCd(), coverageDate.getYear())) 
					|| RapProcessingHelper.isSbmWithoutProratedAmounts(prorationType, premiumRecs)) { 
				
				if(CollectionUtils.isNotEmpty(premiumRecs)) {
					premiumRecs.subList(1, premiumRecs.size()).clear();
				}
			}
			
			List<PolicyPaymentTransDTO> payments = paymentsForMonths.get(coverageDate);
			
			//if no payments exists for coverage date then create retros for all three programTypes
			if(CollectionUtils.isEmpty(payments)){
				LOGGER.debug("No payments exists for coverage period - creating retros");
				
				if(CollectionUtils.isNotEmpty(premiumRecs) && !coverageDate.isAfter(policyVersion.getPolicyEndDate())) {
					
					if(prorationType.equals(ProrationType.SBM_PRORATING)) {
						
						pmtTransList.addAll(createRetrosForSbm(coverageDate, policyVersion, premiumRecs, prorationType));
						
					} else {
						
						pmtTransList.addAll(createRetros(coverageDate, policyVersion, premiumRecs, prorationType));
					}
				}
			} else {
				LOGGER.debug("Payments exist - creating reversals/adjustments");
				
				createAptcPayments(policyVersion, coverageDate, pmtTransList, payments, premiumRecs, prorationType);

				createCsrPayments(policyVersion, coverageDate, pmtTransList, payments, premiumRecs, prorationType);

				if(!prorationType.equals(ProrationType.SBM_PRORATING) && isUserFeeRateExists(coverageDate, policyVersion)) {
					createUfPayments(policyVersion, coverageDate, pmtTransList, payments, premiumRecs);
				} 
			}
		}
		LOGGER.info("EXIT createRetroActivePaymentTxns()");
		return pmtTransList;
	}

	/*
	 * Reverse the payments that exist prior to the new policy start date
	 */
	private void reversePmtsPriorToPolicyStartDate(
			List<PolicyPaymentTransDTO> pmtTransList,
			Map<DateTime, List<PolicyPaymentTransDTO>> paymentsForMonths,
			PolicyDataDTO policyVersion) {

		if (CollectionUtils.isNotEmpty(minMaxCoverageMonths)) { 
			
			List<DateTime> sortedDates = new ArrayList<DateTime>(paymentsForMonths.keySet());
			Collections.sort(sortedDates);
			
			DateTime minPaymentStart = minMaxCoverageMonths.get(0);
			
			for(DateTime coverageDt: sortedDates) {
				
				if(coverageDt.isBefore(minPaymentStart)) {
					List<PolicyPaymentTransDTO> payments = paymentsForMonths.get(coverageDt);
				
					for (PolicyPaymentTransDTO pmt : payments) {
						if(!reversalRefIds.contains(pmt.getPolicyPaymentTransId())) {
							createReversalForCancels(pmtTransList, coverageDt, pmt.getFinancialProgramTypeCd(), policyVersion, pmt);
						}
					}
					
				} else {
					break;
				}
			}
		} else {
			
			List<DateTime> sortedDates = new ArrayList<DateTime>(paymentsForMonths.keySet());
			Collections.sort(sortedDates);
			
			DateTime minPaymentStart = policyVersion.getPolicyStartDate();
			
			for(DateTime coverageDt: sortedDates) {
				
				if(coverageDt.isBefore(minPaymentStart)) {
					List<PolicyPaymentTransDTO> payments = paymentsForMonths.get(coverageDt);
				
					for (PolicyPaymentTransDTO pmt : payments) {
						if(!reversalRefIds.contains(pmt.getPolicyPaymentTransId())) {
							createReversalForCancels(pmtTransList, coverageDt, pmt.getFinancialProgramTypeCd(), policyVersion, pmt);
						}
					}
					
				} else {
					break;
				}
			}
			
		}
	}

	/*
	 * Evaluate APTC for Payments
	 */
	private void createAptcPayments(
			PolicyDataDTO policyVersion, DateTime coverageDate, 
			List<PolicyPaymentTransDTO> pmtTransList, List<PolicyPaymentTransDTO> payments, List<PolicyPremium> premiumRecs, ProrationType prorationType) {
		LOGGER.info("Evaluating Aptc Payment Records for.." + coverageDate);
		
		List<PolicyPaymentTransDTO> aptcPayments = getProgramSpecificPayments(payments, APTC);
		
		if(premiumRecs != null) {
			
			List<PolicyPremium> premiums = new ArrayList<PolicyPremium>();
			premiums.addAll(premiumRecs);
			
			if(prorationType.equals(ProrationType.SBM_PRORATING) && RapProcessingHelper.hasNoProratedAptc(premiums)) {
				premiums.subList(1, premiums.size()).clear();
			}
			
			for(PolicyPremium policyPremium : premiums) {
				
				BigDecimal epsAptc = policyPremium.getAptcAmount() == null ? BigDecimal.ZERO:policyPremium.getAptcAmount();
				
				DateTime coverageMonthEndDate = coverageDate.dayOfMonth().withMaximumValue();
				
				DateTime premiumStartDate = policyPremium.getEffectiveStartDate().isBefore(coverageDate) ?
						coverageDate : policyPremium.getEffectiveStartDate();
				
				DateTime premiumEndDate = (policyPremium.getEffectiveEndDate() == null 
						|| policyPremium.getEffectiveEndDate().isAfter(coverageMonthEndDate)) ? 
						coverageMonthEndDate : policyPremium.getEffectiveEndDate();
						
				PolicyPaymentTransDTO pmt = getPaymentForPremiumPeriod(
						aptcPayments, premiumStartDate, premiumEndDate);
				
				LOGGER.debug(premiumStartDate + " to " + premiumEndDate);
				
				//Premium Dates match existing Payment dates
				if (pmt != null) {
					
					BigDecimal proratedAmount = getProratedAmount(epsAptc, policyPremium.getProratedAptcAmount(),
							coverageDate, premiumStartDate, premiumEndDate, policyVersion.getSubscriberStateCd(), prorationType);
					
					if(!(pmt.getPaymentAmount().compareTo(proratedAmount) == 0)) {
						createAdjustmentForMatchingDates(
								pmtTransList, coverageDate, APTC, policyVersion, pmt, policyPremium, epsAptc, policyPremium.getProratedAptcAmount(), prorationType);
					} else {
						createReversalForCancels(pmtTransList, coverageDate, APTC, policyVersion, pmt);
					}
					
				} else {
				//Premium dates differ from existing payment dates	
					for(PolicyPaymentTransDTO payment: aptcPayments) {
	
						createReversalForNonMatchingDates(pmtTransList, coverageDate, premiumStartDate, APTC, policyVersion, payment);
					}
					
					if(!policyVersion.isPolicyCancelled() && !coverageDate.isAfter(premiumEndDate) && epsAptc.compareTo(BigDecimal.ZERO) != 0) {
						pmtTransList.add(createPolicyPaymentTrans(coverageDate, APTC, policyVersion, policyPremium, epsAptc, policyPremium.getProratedAptcAmount(), prorationType));
					}
				}
			}
		}
		
		//Future Cancel
		for(PolicyPaymentTransDTO payment: aptcPayments) {
			
			if (!reversedTransIds.contains(payment.getPolicyPaymentTransId())) {
				createReversalForCancels(pmtTransList, coverageDate, APTC, policyVersion, payment);
			}
		}
	}

	/*
	 * Create CSR Payment Transactions
	 */
	private void createCsrPayments(PolicyDataDTO policyVersion, DateTime coverageDate, 
			List<PolicyPaymentTransDTO> pmtTransList, List<PolicyPaymentTransDTO> payments, List<PolicyPremium> premiumRecs, ProrationType prorationType) {
		LOGGER.info("Evaluating CSR Payment Records for.." + coverageDate);
		
		List<PolicyPaymentTransDTO> csrPayments = getProgramSpecificPayments(payments, CSR);
		
		if(premiumRecs != null) {
			
			List<PolicyPremium> premiums = new ArrayList<PolicyPremium>();
			premiums.addAll(premiumRecs);
			
			if(prorationType.equals(ProrationType.SBM_PRORATING) && RapProcessingHelper.hasNoProratedCsr(premiums)) {
				premiums.subList(1, premiums.size()).clear();
			}
			
			for(PolicyPremium policyPremium : premiums) {
				
				BigDecimal epsCsr = policyPremium.getCsrAmount() == null ? BigDecimal.ZERO:policyPremium.getCsrAmount();
				
				DateTime coverageMonthEndDate = coverageDate.dayOfMonth().withMaximumValue();
				
				DateTime premiumStartDate = policyPremium.getEffectiveStartDate().isBefore(coverageDate) ?
						coverageDate : policyPremium.getEffectiveStartDate();
				
				DateTime premiumEndDate = (policyPremium.getEffectiveEndDate() == null 
						|| policyPremium.getEffectiveEndDate().isAfter(coverageMonthEndDate)) ? 
						coverageMonthEndDate : policyPremium.getEffectiveEndDate();
						
				PolicyPaymentTransDTO pmt = getPaymentForPremiumPeriod(
						csrPayments, premiumStartDate, premiumEndDate);
				
				LOGGER.debug(premiumStartDate + " to " + premiumEndDate);
				
				//Premium Dates match existing Payment dates
				if (pmt != null) {
					
					BigDecimal proratedAmount = getProratedAmount(epsCsr, policyPremium.getProratedCsrAmount(),
							coverageDate, premiumStartDate, premiumEndDate, policyVersion.getSubscriberStateCd(), prorationType);
					
					if(!(pmt.getPaymentAmount().compareTo(proratedAmount) == 0)) {
						
						createAdjustmentForMatchingDates(
								pmtTransList, coverageDate, CSR, policyVersion, pmt, policyPremium, epsCsr, policyPremium.getProratedCsrAmount(), prorationType);
					} else {
						createReversalForCancels(pmtTransList, coverageDate, CSR, policyVersion, pmt);
					}
					
				} else {
					//Premium dates differ from existing payment dates	
					for(PolicyPaymentTransDTO payment: csrPayments) {
						createReversalForNonMatchingDates(pmtTransList, coverageDate, premiumStartDate, CSR, policyVersion, payment);
					}
	
					//Create the new Adjustment Transaction
					if(!policyVersion.isPolicyCancelled() && !coverageDate.isAfter(premiumEndDate) && epsCsr.compareTo(BigDecimal.ZERO) != 0) {
						pmtTransList.add(createPolicyPaymentTrans(coverageDate, CSR, policyVersion, policyPremium, epsCsr, policyPremium.getProratedCsrAmount(), prorationType));
					}
				}
			}
		}
		
		//Future Cancel
		for(PolicyPaymentTransDTO payment: csrPayments) {
			
			if (!reversedTransIds.contains(payment.getPolicyPaymentTransId())) {
				createReversalForCancels(pmtTransList, coverageDate, CSR, policyVersion, payment);
			}
		}
	}
	
	/*
	 * Create User Fee Payment Transactions
	 */
	private void createUfPayments(PolicyDataDTO policyVersion, DateTime coverageDate,
			List<PolicyPaymentTransDTO> pmtTransList, List<PolicyPaymentTransDTO> payments, List<PolicyPremium> premiumRecs) {
		LOGGER.info("Evaluating UF Payment Records for.." + coverageDate);
		
		List<PolicyPaymentTransDTO> ufPayments = getProgramSpecificPayments(payments, UF);
		
		if(premiumRecs != null) {
			
			for(PolicyPremium policyPremium : premiumRecs) {
				
				BigDecimal epsUf = policyPremium.getTotalPremiumAmount() == null ? BigDecimal.ZERO:policyPremium.getTotalPremiumAmount();
				
				DateTime coverageMonthEndDate = coverageDate.dayOfMonth().withMaximumValue();
				
				DateTime premiumStartDate = policyPremium.getEffectiveStartDate().isBefore(coverageDate) ?
						coverageDate : policyPremium.getEffectiveStartDate();
				
				DateTime premiumEndDate = (policyPremium.getEffectiveEndDate() == null 
						|| policyPremium.getEffectiveEndDate().isAfter(coverageMonthEndDate)) ? 
						coverageMonthEndDate : policyPremium.getEffectiveEndDate();
						
				PolicyPaymentTransDTO pmt = getPaymentForPremiumPeriod(
						ufPayments, premiumStartDate, premiumEndDate);
				
				LOGGER.debug(premiumStartDate + " to " + premiumEndDate);
				
				//Premium Dates match existing Payment dates
				if (pmt != null) {
					
					BigDecimal proratedAmount = getProratedAmount(epsUf, policyPremium.getProratedPremiumAmount(),
							coverageDate, premiumStartDate, premiumEndDate, policyVersion.getSubscriberStateCd(),
							ProrationType.NON_PRORATING);
					
					if(!(pmt.getTotalPremiumAmount().compareTo(proratedAmount) == 0)) {
						
						Long reversalRefId = null;
						if(pmt.getLastPaymentProcStatusTypeCd().equalsIgnoreCase(STATUS_PENDING_CYCLE)) {
							
							createReplacementTrans(pmtTransList, pmt, policyVersion);
						} else {
							reversalRefId = pmt.getPolicyPaymentTransId(); 
							pmtTransList.add(createReversalPaymentTrans(coverageDate, UF, policyVersion, pmt, reversalRefId));
						}
						
						//Create the new Adjustment Transaction
						if(epsUf.compareTo(BigDecimal.ZERO) !=0 && !coverageDate.isAfter(policyVersion.getPolicyEndDate())
								&& !policyVersion.isPolicyCancelled()) {
							
							pmtTransList.add(createPolicyPaymentTrans(coverageDate, UF, policyVersion, policyPremium, epsUf, policyPremium.getProratedPremiumAmount(), ProrationType.NON_PRORATING));
						}
					} else {
						createReversalForCancels(pmtTransList, coverageDate, UF, policyVersion, pmt);
					}
					
				} else {
				//Premium dates differ from existing payment dates	
					for(PolicyPaymentTransDTO payment: ufPayments) {
	
						createReversalForNonMatchingDates(pmtTransList, coverageDate, premiumStartDate, UF, policyVersion, payment);
					}
					//Create the new Adjustment Transaction
					if(!policyVersion.isPolicyCancelled() && !coverageDate.isAfter(premiumEndDate) && epsUf.compareTo(BigDecimal.ZERO) != 0) {
						pmtTransList.add(createPolicyPaymentTrans(coverageDate, UF, policyVersion, policyPremium, epsUf, policyPremium.getProratedPremiumAmount(), ProrationType.NON_PRORATING));
					}
				}
			}
		}
		//Future Cancel
		for(PolicyPaymentTransDTO payment: ufPayments) {
			
			if (!reversedTransIds.contains(payment.getPolicyPaymentTransId())) {
				createReversalForCancels(pmtTransList, coverageDate, UF, policyVersion, payment);
			}
		}
	}
	
	/*
	 * create Reversals For Non Matching Dates
	 */
	private void createReversalForNonMatchingDates(
			List<PolicyPaymentTransDTO> pmtTransList, DateTime coverageDate,
			DateTime premiumStartDate, String programType,
			PolicyDataDTO policyVersion, PolicyPaymentTransDTO payment) {

		if(!premiumStartDate.isAfter(payment.getPaymentCoverageEndDate())) {
				
			LOGGER.debug(programType + " reversal For Non Matching Dates");
			
			Long reversalRefId = null;
			
			if(payment.getLastPaymentProcStatusTypeCd().equalsIgnoreCase(STATUS_PENDING_CYCLE)) {
				createReplacementTrans(pmtTransList, payment, policyVersion);
			} else {
				reversalRefId = payment.getPolicyPaymentTransId(); 
				
				if (!payment.getLastPaymentProcStatusTypeCd().equalsIgnoreCase(STATUS_REPLACED)  
						&& (payment.getParentPolicyPaymentTransId() == null 
						|| payment.getParentPolicyPaymentTransId().longValue() != reversalRefId.longValue())) {
					
					pmtTransList.add(createReversalPaymentTrans(coverageDate, programType, policyVersion, payment, reversalRefId));
					payment.setParentPolicyPaymentTransId(reversalRefId);
				}
			}
		} else {
			if (policyVersion.isPolicyCancelled() 
					|| coverageDate.isAfter(policyVersion.getPolicyEndDate())) {
				LOGGER.debug(programType + " Cancel/Term/End For Non MatchingDates");
				
				Long reversalRefId = null;
				if(payment.getLastPaymentProcStatusTypeCd().equalsIgnoreCase(STATUS_PENDING_CYCLE)) {
					createReplacementTrans(pmtTransList, payment, policyVersion);
				} else {
					reversalRefId = payment.getPolicyPaymentTransId(); 
					pmtTransList.add(createReversalPaymentTrans(coverageDate, programType, policyVersion, payment, reversalRefId));
				}
			}
		}
	}
	
	/*
	 * create Reversals For Cancels
	 */
	private void createReversalForCancels(
			List<PolicyPaymentTransDTO> pmtTransList, DateTime coverageDate,
			String programType, PolicyDataDTO policyVersion, PolicyPaymentTransDTO payment) {
		
		if (policyVersion.isPolicyCancelled()  
				|| payment.getPaymentCoverageStartDate().isAfter(policyVersion.getPolicyEndDate()) 
				|| payment.getPaymentCoverageStartDate().isBefore(policyVersion.getPolicyStartDate())) {
			LOGGER.debug(programType + " Cancel/Term/End Matching Dates");
			
			Long reversalRefId = null;
			if(payment.getLastPaymentProcStatusTypeCd().equalsIgnoreCase(STATUS_PENDING_CYCLE)) {
				createReplacementTrans(pmtTransList, payment, policyVersion);
			} else {
				reversalRefId = payment.getPolicyPaymentTransId(); 
				pmtTransList.add(createReversalPaymentTrans(coverageDate, programType, policyVersion, payment, reversalRefId));
			}
		}
	}

	/*
	 * Create Reversal and Adjustment For Matching Dates
	 */
	private void createAdjustmentForMatchingDates(List<PolicyPaymentTransDTO> pmtTransList, 
			DateTime coverageDate, String programType, PolicyDataDTO policyVersion,
			PolicyPaymentTransDTO payment, PolicyPremium policyPremium, BigDecimal epsAmount, BigDecimal proratedAmount, ProrationType prorationType) {
		LOGGER.debug(programType + " create Adjustment/Reversal For Matching Dates");
		
		Long reversalRefId = null;
		
		if(payment.getLastPaymentProcStatusTypeCd().equalsIgnoreCase(STATUS_PENDING_CYCLE)) {
			createReplacementTrans(pmtTransList, payment, policyVersion);
		} else {
			reversalRefId = payment.getPolicyPaymentTransId(); 
			pmtTransList.add(createReversalPaymentTrans(coverageDate, programType, policyVersion, payment, reversalRefId));
		}
		
		if(epsAmount.compareTo(BigDecimal.ZERO) !=0 
				&& !payment.getPaymentCoverageStartDate().isAfter(policyVersion.getPolicyEndDate()) 
				&& !policyVersion.isPolicyCancelled()) {
			pmtTransList.add(createPolicyPaymentTrans(coverageDate, programType, policyVersion, policyPremium, epsAmount, proratedAmount, prorationType));
		}
	}
	
	/*
	 * Get Prorated Amount
	 */
	private BigDecimal getProratedAmount(BigDecimal paymentAmount, BigDecimal proratedPmtAmount,
			DateTime coverageDate, DateTime effectiveStartDate, DateTime premiumEndDate, String stateCd, ProrationType prorationType) {
		
		if (!prorationType.equals(ProrationType.SBM_PRORATING) && !isStateFfmProrating(stateCd, coverageDate.getYear())) { 
			return paymentAmount;
		}
		
		if(proratedPmtAmount != null) {
			return proratedPmtAmount;
		}
		
		if(prorationType.equals(ProrationType.SBM_PRORATING)) {
			return paymentAmount;
		}
		
		int prorationDays = RapProcessingHelper.getProrationDaysOfCoverage(coverageDate, effectiveStartDate, premiumEndDate);
		int totalDaysInCoverageMonth = coverageDate.dayOfMonth().getMaximumValue();
		
		BigDecimal proratedAmount = RapProcessingHelper.getProratedAmount(paymentAmount, prorationDays, totalDaysInCoverageMonth);
		
		return proratedAmount;
	}

	/*
	 * Get Payment record corresponding to Premium Period
	 */
	private PolicyPaymentTransDTO getPaymentForPremiumPeriod(
			List<PolicyPaymentTransDTO> payments, DateTime effectiveStartDate, DateTime premiumEndDate) {
		
		for (PolicyPaymentTransDTO pmt : payments) {
			if(pmt.getPaymentCoverageStartDate().equals(effectiveStartDate) && pmt.getPaymentCoverageEndDate().equals(premiumEndDate)
					&& (!reversedTransIds.contains(pmt.getPolicyPaymentTransId()))
					&& (pmt.getLastPaymentProcStatusTypeCd().equals(STATUS_PENDING_CYCLE)
							|| pmt.getLastPaymentProcStatusTypeCd().equals(STATUS_NOISE)
							|| pmt.getLastPaymentProcStatusTypeCd().equals(STATUS_PENDING_APPROVAL)
							|| pmt.getLastPaymentProcStatusTypeCd().equals(STATUS_APPROVED))) {
				return pmt;
			}
		}
		return null;
	}
	
	/*
	 * Returns Payment transactions for the program type coverageDate
	 */
	private List<PolicyPaymentTransDTO> getProgramSpecificPayments(List<PolicyPaymentTransDTO> payments, String financialPgmType) {
		List<PolicyPaymentTransDTO> pmtList = new ArrayList<PolicyPaymentTransDTO>();
		
		for(PolicyPaymentTransDTO payment: payments) {
			
			if(!reversalRefIds.contains(payment.getPolicyPaymentTransId())) {
				
				String paymentPgmType = payment.getFinancialProgramTypeCd();
				if(financialPgmType.equals(paymentPgmType)) {
					pmtList.add(payment);
				}
			}
		}
		return pmtList;
	}
	
	/*
	 * Returns premium records grouped by coverageDate
	 */	
	private Map<DateTime, List<PolicyPremium>> getPremiumsForCoverageMonths(
			List<PolicyPremium> policyPremiums, PolicyDataDTO policyVersion) {
		
		Map<DateTime, List<PolicyPremium>> premiumsForMonths = new HashMap<DateTime, List<PolicyPremium>>();
		List<PolicyPremium> premiumsForCovrageMonths;
		
		if (policyPremiums != null) {
			for (PolicyPremium policyPremium: policyPremiums) {
				
				DateTime covrageDate = policyPremium.getEffectiveStartDate().withDayOfMonth(1);
				
				DateTime premiumEndDate = policyPremium.getEffectiveEndDate() == null ? 
						new DateTime(HIGH_DATE) : policyPremium.getEffectiveEndDate();
				
				if(premiumEndDate.isBefore(policyVersion.getPolicyStartDate())) {
					continue;
				}
						
				if (premiumsForMonths.containsKey(covrageDate)) {
					premiumsForMonths.get(covrageDate).add(policyPremium);

				} else {
					premiumsForCovrageMonths = new ArrayList<PolicyPremium>();
					premiumsForCovrageMonths.add(policyPremium);
					premiumsForMonths.put(covrageDate, premiumsForCovrageMonths);
				}
				
				if(policyPremium.getEffectiveEndDate() == null 
						|| policyPremium.getEffectiveEndDate().isAfter(covrageDate.dayOfMonth().withMaximumValue())) {
					for (DateTime covragePeriod: minMaxCoverageMonths) {
						
						if(covragePeriod.isAfter(covrageDate) && covragePeriod.isBefore(premiumEndDate)) {
							premiumsForCovrageMonths = new ArrayList<PolicyPremium>();
							premiumsForCovrageMonths.add(policyPremium);
							premiumsForMonths.put(covragePeriod, premiumsForCovrageMonths);
						}
					}
				}
				LOGGER.debug("policyPremium: ", policyPremium);
			}
		}
		
		if(!premiumsForMonths.isEmpty()) {
			PolicyPremium prevPolicyPremium = null;
			for (DateTime coverageMonth : minMaxCoverageMonths) {
				if (premiumsForMonths.containsKey(coverageMonth)) {
					prevPolicyPremium = premiumsForMonths.get(coverageMonth).get(premiumsForMonths.get(coverageMonth).size()-1);
				} else {
					if (prevPolicyPremium != null) {
						premiumsForMonths.put(coverageMonth, new ArrayList<PolicyPremium>());
						premiumsForMonths.get(coverageMonth).add(prevPolicyPremium);
					}
				}
			}
		}

		return premiumsForMonths;
	}

	/*
	 * Returns payment records grouped by coverageDate
	 */	
	private Map<DateTime, List<PolicyPaymentTransDTO>> getPaymentsForCoverageMonths(List<PolicyPaymentTransDTO> policyPayments) {
		Map<DateTime, List<PolicyPaymentTransDTO>> paymentsForMonths = new HashMap<DateTime, List<PolicyPaymentTransDTO>>();
		
		if (policyPayments != null) {
			
			reversalRefIds = new ArrayList<Long>();
			
			for (PolicyPaymentTransDTO paymentTrans: policyPayments) {
				
				if (paymentTrans.getParentPolicyPaymentTransId() == null) {
					
					DateTime covrageDate = paymentTrans.getCoverageDate();
					if (paymentsForMonths.containsKey(covrageDate)) {
						if (paymentsForMonths.get(covrageDate) == null) {
							paymentsForMonths.put(covrageDate, new ArrayList<PolicyPaymentTransDTO>());
						} 
						paymentsForMonths.get(covrageDate).add(paymentTrans);
					} else {
						paymentsForMonths.put(covrageDate, new ArrayList<PolicyPaymentTransDTO>());
						paymentsForMonths.get(covrageDate).add(paymentTrans);
					}
				} else {
					reversalRefIds.add(paymentTrans.getParentPolicyPaymentTransId());
				}
			}
		}
		return paymentsForMonths;
	}

	/*
	 * Determine the Min and Max Coverage Months
	 */
	private List<DateTime> determineMinMaxCoverageMonth(PolicyDataDTO policyVersion) {
		List<DateTime> minMaxPaymentMonths = new ArrayList<DateTime>();
		
		DateTime minPaymentMonth = policyVersion.getPolicyStartDate();
		LOGGER.info("min Payment Month: "+ minPaymentMonth);
		
		DateTime issuerTransitionDt = policyVersion.getIssuerStartDate();
		
		if(minPaymentMonth.getYear() < issuerTransitionDt.getYear()) {
			
			minPaymentMonth = new DateTime().withYear(issuerTransitionDt.getYear()).withDayOfYear(1).withTimeAtStartOfDay();
			
			LOGGER.info("Policy start year less than issuer Transition year. Setting Min Covg Month to Jan of Issuer transition Year "+ minPaymentMonth);
		}
		
		DateTime policyVersionDt = policyVersion.getMaintenanceStartDateTime();
		
		DateTime paymentMonth = currentPmtMonth.get(0);
		DateTime ercDate = currentPmtMonth.get(1);
		
		DateTime maxPaymentMonth = null;
		
		LOGGER.info("policyVersionDt: "+ policyVersionDt);
		
		if(!policyVersionDt.isBefore(ercDate)) {
			LOGGER.info("policyVersionDt is after erc Date ");
			
			maxPaymentMonth = paymentMonth;
			
		} else {
			LOGGER.info("policyVersionDt is on/before erc Date ");
			maxPaymentMonth = paymentMonth.minusMonths(1);
		}
		LOGGER.info("Max Payment Month: "+ maxPaymentMonth);
		
		DateTime coverageDate = minPaymentMonth.withDayOfMonth(1).withTimeAtStartOfDay();
	
		while(coverageDate.compareTo(maxPaymentMonth) <= 0) {
			minMaxPaymentMonths.add(coverageDate);
			coverageDate = coverageDate.plusMonths(1);
		}
		
		return minMaxPaymentMonths;
	}

	/*
	 * Determine the ERC of the latest Payment Month
	 */
	private List<DateTime> getPaymentMonthERC() {

		CodeRecord record = codeDecodesHelper.getDecode(COVERAGEPERIODPAID, ERC);
		
		if(record==null){
			String message = RapConstants.ERRORCODE_E9004+": Process Execution Error - COVERAGEPERIODPAID table is empty";
			LOGGER.error(message);
			throw new ApplicationException(message);
		}
		
		List<DateTime> currentPmtMonth = new ArrayList<DateTime>();
		DateTime pmtMonth = null;
		DateTime ercDate = null;
		
		String code = record.getCode();
		if(code != null) {
			LOGGER.info("cpm: "+ code);
		
			pmtMonth = RapProcessingHelper.getDateTimeFromString(code);
			LOGGER.info("paymentMonth: "+ pmtMonth);
			currentPmtMonth.add(pmtMonth);
		}
		
		String decode = record.getDecode();
		if(decode != null) {
			LOGGER.info("erc: "+ decode);
			ercDate = RapProcessingHelper.getDateTimeFromString(decode);
			LOGGER.info("ercDate: "+ ercDate);
			currentPmtMonth.add(ercDate);
		}
		return currentPmtMonth;
	}

	/*
	 * Check whether User Fee rate exists for the coverage date
	 */
	private boolean isUserFeeRateExists(DateTime coverageDate, PolicyDataDTO policy) {
		
		IssuerUserFeeRate rate = RapProcessingHelper.getUserFeeRateForRetroCoverageDate(
				coverageDate, RapProcessingHelper.getStateCode(policy.getPlanId()), policy.getInsrncAplctnTypeCd(), rapDao);
		if(rate != null) {
			LOGGER.info("UF Rate found for policy:{}", policy.getPolicyVersionId());
			return true;
		}
		LOGGER.info("No UF Rate found for policy:{}", policy.getPolicyVersionId());
		return false;
	}	
	
	/*
	 * Creates retro payments for all APTC,CSR & UF
	 */
	private List<PolicyPaymentTransDTO> createRetros(DateTime coverageDate, PolicyDataDTO policy, List<PolicyPremium> premiumRecs, ProrationType prorationType) {       
		List<PolicyPaymentTransDTO> transactionList = new ArrayList<PolicyPaymentTransDTO>();   

		for (PolicyPremium premium : premiumRecs) {
			
			DateTime premiumEndDate = premium.getEffectiveEndDate() == null ? 
					new DateTime(HIGH_DATE) : premium.getEffectiveEndDate();
			
			if (!premiumEndDate.isBefore(coverageDate) && !policy.isPolicyCancelled()) {
				
				BigDecimal aptcAmount = premium.getAptcAmount();
				if (aptcAmount != null && aptcAmount.compareTo(BigDecimal.ZERO) != 0) {
					transactionList.add(createPolicyPaymentTrans(coverageDate, APTC, policy, premium, premium.getAptcAmount(), premium.getProratedAptcAmount(), prorationType));
				}
				
				BigDecimal csrAmount = premium.getCsrAmount();
				if (csrAmount != null && csrAmount.compareTo(BigDecimal.ZERO) != 0) {
					transactionList.add(createPolicyPaymentTrans(coverageDate, CSR, policy, premium, premium.getCsrAmount(), premium.getProratedCsrAmount(), prorationType));
				}
				
				BigDecimal totalPremiumAmount = premium.getTotalPremiumAmount();
				if (totalPremiumAmount != null && totalPremiumAmount.compareTo(BigDecimal.ZERO) != 0) {
					if(!prorationType.equals(ProrationType.SBM_PRORATING) && isUserFeeRateExists(coverageDate, policy)) {
						transactionList.add(createPolicyPaymentTrans(coverageDate, UF, policy, premium, premium.getTotalPremiumAmount(), premium.getProratedPremiumAmount(), ProrationType.NON_PRORATING));
					}
				}
			}
		}
		return transactionList;
	}
	
	/*
	 * Creates retro payments for SBM ProrationType - APTC,CSR
	 */
	private List<PolicyPaymentTransDTO> createRetrosForSbm(DateTime coverageDate, PolicyDataDTO policy, List<PolicyPremium> premiumRecs, ProrationType prorationType) {       
		List<PolicyPaymentTransDTO> transactionList = new ArrayList<PolicyPaymentTransDTO>();   
		
		//APTC
		List<PolicyPremium> premiumsNoProratedAptc = new ArrayList<PolicyPremium>();
		premiumsNoProratedAptc.addAll(premiumRecs);
		
		if(RapProcessingHelper.hasNoProratedAptc(premiumsNoProratedAptc)) {
			premiumsNoProratedAptc.subList(1, premiumsNoProratedAptc.size()).clear();
		}
		
		for (PolicyPremium premium : premiumsNoProratedAptc) {
			
			DateTime premiumEndDate = premium.getEffectiveEndDate() == null ? 
					new DateTime(HIGH_DATE) : premium.getEffectiveEndDate();
			
			if (!premiumEndDate.isBefore(coverageDate) && !policy.isPolicyCancelled()) {
				BigDecimal aptcAmount = premium.getAptcAmount();
				if (aptcAmount != null && aptcAmount.compareTo(BigDecimal.ZERO) != 0) {
					transactionList.add(createPolicyPaymentTrans(coverageDate, APTC, policy, premium, premium.getAptcAmount(), premium.getProratedAptcAmount(), prorationType));
				}
			}
		}
		
		
		//CSR
		List<PolicyPremium> premiumsNoProratedCsr = new ArrayList<PolicyPremium>();
		premiumsNoProratedCsr.addAll(premiumRecs);
		
		if(RapProcessingHelper.hasNoProratedCsr(premiumsNoProratedCsr)) {
			premiumsNoProratedCsr.subList(1, premiumsNoProratedCsr.size()).clear();
		}
			
		for (PolicyPremium premium : premiumsNoProratedCsr) {
			
			DateTime premiumEndDate = premium.getEffectiveEndDate() == null ? 
					new DateTime(HIGH_DATE) : premium.getEffectiveEndDate();
			
			if (!premiumEndDate.isBefore(coverageDate) && !policy.isPolicyCancelled()) {
				BigDecimal csrAmount = premium.getCsrAmount();
				if (csrAmount != null && csrAmount.compareTo(BigDecimal.ZERO) != 0) {
					transactionList.add(createPolicyPaymentTrans(coverageDate, CSR, policy, premium, premium.getCsrAmount(), premium.getProratedCsrAmount(), prorationType));
				}
			}
		}
		
		return transactionList;
	}
	
	/*
	 * Creates PolicyPaymentTransDTO
	 */
	private PolicyPaymentTransDTO createPolicyPaymentTrans(
			DateTime coverageMonth, String financialProgramTypeCd, PolicyDataDTO policy,
			PolicyPremium premium, BigDecimal amount, BigDecimal proratedAmount, ProrationType prorationType) {

		PolicyPaymentTransDTO dto = new PolicyPaymentTransDTO();
		dto.setPolicyPaymentTransId(rapDao.getPolicyPaymentTransNextSeq());
		dto.setExchangePolicyId(policy.getExchangePolicyId());
		dto.setSubscriberStateCd(policy.getSubscriberStateCd());
		dto.setFinancialProgramTypeCd(financialProgramTypeCd);
		dto.setCoverageDate(coverageMonth);
		dto.setMaintenanceStartDateTime(policy.getMaintenanceStartDateTime());
		dto.setIssuerHiosId(policy.getIssuerHiosId());
		dto.setPolicyVersionId(policy.getPolicyVersionId());
		dto.setMarketplaceGroupPolicyId(policy.getMarketplaceGroupPolicyId());
		dto.setTransPeriodTypeCd(TRANSPERIOD_RETROACTIVE);        
		dto.setInsrncAplctnTypeCd(policy.getInsrncAplctnTypeCd());
		dto.setIssuerStateCd(RapProcessingHelper.getStateCode(policy.getPlanId()));     
		//set transaction status
		dto.setPaymentProcStatusTypeCd(STATUS_PENDING_CYCLE);
		dto.setLastPaymentProcStatusTypeCd(STATUS_PENDING_CYCLE);

		DateTime coverageStartDate = premium.getEffectiveStartDate();
		
		if(coverageStartDate.isAfter(coverageMonth)) {
			dto.setPaymentCoverageStartDate(coverageStartDate);
		} else {
			dto.setPaymentCoverageStartDate(coverageMonth);
		}
		
		DateTime coverageEndDate = premium.getEffectiveEndDate() == null ? 
				new DateTime(HIGH_DATE) : premium.getEffectiveEndDate();
		DateTime retroMonthEndDate = getRetroMonthEndDate(coverageMonth);
		
		if(coverageEndDate.isBefore(retroMonthEndDate)) {
			dto.setPaymentCoverageEndDate(coverageEndDate);
		} else {
			dto.setPaymentCoverageEndDate(retroMonthEndDate);
		}
		
		BigDecimal totalPremiumAmount = premium.getTotalPremiumAmount();

		Integer prorationDays = coverageMonth.dayOfMonth().getMaximumValue();
		
		if(isStateFfmProrating(policy.getSubscriberStateCd(), coverageMonth.getYear())) {
		
			prorationDays = RapProcessingHelper.getProrationDaysOfCoverage(
					coverageMonth, dto.getPaymentCoverageStartDate(), dto.getPaymentCoverageEndDate());
			
			Integer daysInCoverageMonth = coverageMonth.dayOfMonth().getMaximumValue();
			
			if(!UF.equals(financialProgramTypeCd)) {
				//Prorate Payment Amount
				if(proratedAmount == null) {
					amount = RapProcessingHelper.getProratedAmount(amount, prorationDays, daysInCoverageMonth);
				} else {
					amount = proratedAmount;
				}
			}
			
			if(premium.getProratedPremiumAmount() == null) {
				totalPremiumAmount = RapProcessingHelper.getProratedAmount(totalPremiumAmount, prorationDays, daysInCoverageMonth);
			} else {
				totalPremiumAmount = premium.getProratedPremiumAmount();	
				prorationDays = null;
			}

		} else if(prorationType.equals(ProrationType.SBM_PRORATING)) {
			
			if(proratedAmount != null) {
				amount = proratedAmount;
				prorationDays = null;
				
				if(premium.getProratedPremiumAmount() != null) {
					totalPremiumAmount = premium.getProratedPremiumAmount();
				}
			}
		}
		
		if(!UF.equals(financialProgramTypeCd)) {
			dto.setPaymentAmount(amount);
		}
		
		dto.setProrationDaysOfCoverageNum(prorationDays);
		dto.setTotalPremiumAmount(totalPremiumAmount);
		
		return dto;
	}
	
	/*
	 * Creates PolicyPaymentTransDTO for Reversal Transactions
	 */
	private PolicyPaymentTransDTO createReversalPaymentTrans(
			DateTime coverageDate, String financialProgramTypeCd, PolicyDataDTO policy,
			PolicyPaymentTransDTO paymentTrans, Long reversalRefId) {
		
		reversedTransIds.add(paymentTrans.getPolicyPaymentTransId());
		
		PolicyPaymentTransDTO dto = new PolicyPaymentTransDTO();
		dto.setPolicyPaymentTransId(rapDao.getPolicyPaymentTransNextSeq());
		dto.setParentPolicyPaymentTransId(reversalRefId);
		dto.setExchangePolicyId(policy.getExchangePolicyId());
		dto.setSubscriberStateCd(policy.getSubscriberStateCd());
		dto.setFinancialProgramTypeCd(financialProgramTypeCd);
		dto.setCoverageDate(coverageDate);
		dto.setMaintenanceStartDateTime(policy.getMaintenanceStartDateTime());
		dto.setIssuerHiosId(policy.getIssuerHiosId());
		dto.setPolicyVersionId(policy.getPolicyVersionId());
		dto.setMarketplaceGroupPolicyId(policy.getMarketplaceGroupPolicyId());
		dto.setTransPeriodTypeCd(TRANSPERIOD_RETROACTIVE);        
		dto.setInsrncAplctnTypeCd(policy.getInsrncAplctnTypeCd());
		dto.setIssuerStateCd(RapProcessingHelper.getStateCode(policy.getPlanId()));     
		//set transaction status
		dto.setPaymentProcStatusTypeCd(STATUS_PENDING_CYCLE);
		dto.setLastPaymentProcStatusTypeCd(STATUS_PENDING_CYCLE);

		dto.setPaymentCoverageStartDate(paymentTrans.getPaymentCoverageStartDate());
		dto.setPaymentCoverageEndDate(paymentTrans.getPaymentCoverageEndDate());

		dto.setTotalPremiumAmount(paymentTrans.getTotalPremiumAmount());
		dto.setProrationDaysOfCoverageNum(paymentTrans.getProrationDaysOfCoverageNum());
		
		if(paymentTrans.getPaymentAmount() != null) {
			dto.setPaymentAmount(paymentTrans.getPaymentAmount().negate());
		}
		
		return dto;
	}
	
	/*
	 * Set Replacement Status
	 */
	private void createReplacementTrans(
			List<PolicyPaymentTransDTO> pmtTransList, PolicyPaymentTransDTO pmt, PolicyDataDTO policy) {
		reversedTransIds.add(pmt.getPolicyPaymentTransId());
		
		pmt.setLastPaymentProcStatusTypeCd(STATUS_REPLACED);
		pmt.setPaymentProcStatusTypeCd(STATUS_REPLACED);
		pmt.setUpdateStatusRec(true);
		pmt.setMaintenanceStartDateTime(policy.getMaintenanceStartDateTime());
		pmtTransList.add(pmt);
	}
	
	/*
	 * get Retro Month EndD ate
	 */
	private DateTime getRetroMonthEndDate(DateTime coverageDate) {
		if (coverageDate != null) {
			return coverageDate.toLocalDate().dayOfMonth().withMaximumValue().toDateTimeAtStartOfDay();
		}
		return null;
	}
	
	/**
	 * @param rapDao the rapDao to set
	 */
	public void setRapDao(RapDao rapDao) {
		this.rapDao = rapDao;
	}

	/**
	 * @param codeDecodesHelper the codeDecodesHelper to set
	 */
	public void setCodeDecodesHelper(CodeDecodesHelper codeDecodesHelper) {
		this.codeDecodesHelper = codeDecodesHelper;
	}
}
