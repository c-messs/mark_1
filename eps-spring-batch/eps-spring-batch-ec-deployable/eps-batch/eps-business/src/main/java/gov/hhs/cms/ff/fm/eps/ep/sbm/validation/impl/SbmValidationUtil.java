package gov.hhs.cms.ff.fm.eps.ep.sbm.validation.impl;
/**
 * 
 */


import java.util.Arrays;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gov.cms.dsh.sbmi.PolicyMemberType.MemberDates;
import gov.cms.dsh.sbmi.PolicyType.FinancialInformation;
import gov.cms.dsh.sbmi.ProratedAmountType;
import gov.hhs.cms.ff.fm.eps.ep.enums.SBMErrorWarningCode;
import gov.hhs.cms.ff.fm.eps.ep.sbm.SBMCache;
import gov.hhs.cms.ff.fm.eps.ep.sbm.SbmErrWarningLogDTO;

/**
 * This is a helper class for SBM Processing functionality
 * 
 * @author girish.padmanabhan
 */
public class SbmValidationUtil {
	private static final Logger LOG = LoggerFactory.getLogger(SbmValidationUtil.class);

	/**
	 * Method to get Issuer Id from QHP id. 
	 * @param qhpId
	 */
	public static String getIssuerIdFromQhpId(String qhpId) {
		String issuerId = StringUtils.EMPTY;
		int HIOS_ID_INDEX = 5;

		if (qhpId.length() >= HIOS_ID_INDEX) {
			issuerId = qhpId.substring(0, HIOS_ID_INDEX);
			
		} else {
			LOG.debug("Invalid QHP ID, unable to extract HIOS ID for QHPID = " + qhpId);
		}
		return issuerId;
	}
	
	/**
	 * Method to get State code from QHP id. 
	 * @param qhpId
	 */
	public static String getStateCdFromQhpId(String qhpId) {
		String stateCd = StringUtils.EMPTY;

		if (qhpId.length() >= 7) {
			stateCd = qhpId.substring(5, 7);
			
		} else {
			LOG.debug("Invalid QHP ID, unable to extract State Code for QHPID = " + qhpId);
		}
		return stateCd;
	}
	

	/**
	 *  Utility method to validate specific field length against max allowed in database and truncate if exceeds, logging warning
	 *  
	 * @param field
	 * @param fieldVal
	 * @param length
	 * @param fieldLengthWarnings
	 * @return
	 */
	public static  String truncateField(String field, String fieldVal, int length,
			List<SbmErrWarningLogDTO> fieldLengthWarnings) {
		
		if (StringUtils.isNotBlank(fieldVal)) {
			
			if(fieldVal.length() > length) {
				
				String truncatedVal = fieldVal.substring(0, length);
				
				//create Warning WR-005: incorrect value provided
				fieldLengthWarnings.add(SbmValidationUtil.createErrorWarningLogDTO(
						field, SBMErrorWarningCode.WR_004.getCode(), truncatedVal));
				
				LOG.info("InValid field truncated {} : {} ", fieldVal, truncatedVal);
				
				return truncatedVal;
			}
		}
		return null;
	}
	
	/**
	 * Sort the List<FinancialInformation>
	 * @param financialInfoList
	 */
	public static void sortFinancialInfo(List<FinancialInformation> financialInfoList) {

		if (financialInfoList.size() > 1) {
			// Sort the list by FinancialEffectiveStartDate
			financialInfoList.sort((financialInfo0, financialInfo1) -> financialInfo0.getFinancialEffectiveStartDate()
					.compare(financialInfo1.getFinancialEffectiveStartDate()));
		}
	}	
	
	/**
	 * Sort the List<ProratedAmountType>
	 * @param proratedAmounts
	 */
	public static void sortProratedAmounts(List<ProratedAmountType> proratedAmounts) {

		if (proratedAmounts.size() > 1) {
			// Sort the list by PartialMonthEffectiveStartDate
			proratedAmounts.sort((proratedAmount0, proratedAmount1) -> proratedAmount0.getPartialMonthEffectiveStartDate()
					.compare(proratedAmount1.getPartialMonthEffectiveStartDate()));
		}
	}
	
	/**
	 * Sort the List<MemberDates>
	 * @param memberDates
	 */
	public static void sortMemberDates(List<MemberDates> memberDates) {

		if (memberDates.size() > 1) {
			// Sort the list by MemberStartDate
			memberDates.sort((memberDate0, memberDate1) -> memberDate0.getMemberStartDate()
					.compare(memberDate1.getMemberStartDate()));
		}
	}	

	/*
	 * Method to create ErrorWarningLogDTO object for given error
	 */
	public static SbmErrWarningLogDTO createErrorWarningLogDTO(
			String elementInError, String errorCode, String... args) {
		
		SbmErrWarningLogDTO errorWarningDTO = new SbmErrWarningLogDTO();
		errorWarningDTO.setElementInError(elementInError);
		errorWarningDTO.setErrorWarningTypeCd(errorCode);
		errorWarningDTO.setErrorWarningDesc(Arrays.asList(args));

		return errorWarningDTO;
	}
	
	/*
	 * Method to create ErrorWarningLogDTO object for given Member level error
	 */
	public static SbmErrWarningLogDTO createMemberErrorWarningLogDTO(
			String elementInError, String errorCode, String exchangeMemberId, String... args) {
		
		SbmErrWarningLogDTO errorWarningDTO = createErrorWarningLogDTO(elementInError, errorCode, args);
		errorWarningDTO.setExchangeMemberId(exchangeMemberId);

		return errorWarningDTO;
	}

	/**
	 * Determines if errorList contains at least one Error.
	 * @param errorList
	 * @return 
	 */
	public static boolean hasValidationError(List<SbmErrWarningLogDTO> errorWarningList) {

		SbmErrWarningLogDTO errorTypeCd = 
				errorWarningList.stream()
				.filter(errWarn -> errWarn.getErrorWarningTypeCd().startsWith("ER"))
				.findAny()
				.orElse(null);
		
		if(errorTypeCd != null) {
			return true;
		}
		return false;
	}
	
	/**
	 * Determine whether a policy is duplicate
	 * 
	 * @param premiumRecs
	 * @return
	 */
	public static boolean isDuplicatePolicy(String inboundPolicyId) {

		List<String> policyIds = SBMCache.getPolicyIds();
		
		if(CollectionUtils.isNotEmpty(policyIds)) {
			long count = 
					policyIds.stream()
					.filter(policyId -> policyId.equals(inboundPolicyId))
					.count();
			
			if(count > 1) {
				return true;
			}
		}
		return false;
	}
	
}
