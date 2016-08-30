/**
 * 
 */
package gov.hhs.cms.ff.fm.eps.ep.validation.impl;

import gov.cms.dsh.bem.AdditionalInfoType;
import gov.cms.dsh.bem.BenefitEnrollmentMaintenanceType;
import gov.cms.dsh.bem.MemberType;
import gov.cms.dsh.bem.TransactionInformationType;
import gov.hhs.cms.ff.fm.eps.ep.BEMDataUtil;
import gov.hhs.cms.ff.fm.eps.ep.BenefitEnrollmentMaintenanceDTO;
import gov.hhs.cms.ff.fm.eps.ep.EPSConstants;
import gov.hhs.cms.ff.fm.eps.ep.EPSValidationRequest;
import gov.hhs.cms.ff.fm.eps.ep.ErrorWarningLogDTO;
import gov.hhs.cms.ff.fm.eps.ep.enums.EProdEnum;
import gov.hhs.cms.ff.fm.eps.ep.services.PolicyDataService;
import gov.hhs.cms.ff.fm.eps.ep.services.TransMsgCompositeDAO;
import gov.hhs.cms.ff.fm.eps.ep.util.DateTimeUtil;
import gov.hhs.cms.ff.fm.eps.ep.validation.EPSValidationResponse;
import gov.hhs.cms.ff.fm.eps.ep.validation.EPSValidationService;
import gov.hhs.cms.ff.fm.eps.ep.validation.FinancialValidator;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.accenture.foundation.common.exception.ApplicationException;

/**
 * The Validator class implementation for ERL processing.
 * 
 * @author girish.padmanabhan
 *
 */
public class FFMValidationServiceImpl implements EPSValidationService {
	private static final Logger LOG = LoggerFactory.getLogger(FFMValidationServiceImpl.class);
	private static final int HIOS_ID_INDEX = 5;

	private FinancialValidator financialValidator;
	private PolicyDataService policyDataService;
	private TransMsgCompositeDAO txnMsgService;

	List <ErrorWarningLogDTO> blEditErrors;

	/**
	 * The FFM - specific implementation of the interface method.
	 * 1) Performs policy match 
	 * 2) Validates Grp Sender id against Issuer HIOS Id
	 * 
	 * @param epsValidationRequest
	 * @return epsValidationResponse
	 * @throws Exception
	 */
	@Override
	public EPSValidationResponse validateBEM(EPSValidationRequest epsValidationRequest)  {

		EPSValidationResponse epsValidationResponse = new EPSValidationResponse();
		blEditErrors = new ArrayList<ErrorWarningLogDTO>();
		BenefitEnrollmentMaintenanceDTO bemDTO = epsValidationRequest.getBenefitEnrollmentMaintenance();

		validateExtractionStatus(bemDTO);

		validateZeroPremium(bemDTO);

		// Verify there are no transactions for this inbound policy that are in "skipped" status.
		// If true, App Exception will be thrown and this Bem will also be skipped.
		checkPriorSkippedVersions(bemDTO);

		boolean versionSkippedEarlier = checkCurrentVersionWasSkipped(bemDTO);

		if (versionSkippedEarlier) {
			bemDTO.setVersionSkippedInPast(true);
			return epsValidationResponse;
		}

		//policy match
		BenefitEnrollmentMaintenanceDTO dbBemDto = performPolicyMatch(bemDTO);

		if (dbBemDto != null && dbBemDto.getBem() != null) {

			//Ignore the policy if the version with same timestamp already exists in eps OR if there is a later version in EPS
			if (checkIfDuplicateVersionExists(dbBemDto, bemDTO)
					|| checkIfLaterVersionExists(dbBemDto, bemDTO)) {

				bemDTO.setIgnore(true);
				return epsValidationResponse;
			}

			//Market Place group Policy Id match
			validateMarketPlaceGroupPolicyId(dbBemDto, bemDTO);

			//Grp Sender Id vs HIOS Id check
			validateGroupSenderId(dbBemDto, bemDTO);
		}

		//Add the premiums map
		Map<LocalDate, AdditionalInfoType> inboundPremiums = processInboundPremiums(bemDTO);
		bemDTO.setEpsPremiums(inboundPremiums);

		//Add error to errors to list in Bem DTO
		bemDTO.setErrorList(blEditErrors);

		return epsValidationResponse;
	}

	/*
	 * Method to check whether there were errors in extraction and skip the bem if so.
	 */
	private void validateExtractionStatus(BenefitEnrollmentMaintenanceDTO bemDTO) {

		BenefitEnrollmentMaintenanceType bem = bemDTO.getBem();

		if (bem != null && bem.getExtractionStatus() != null) {

			BigInteger extractionStatus = bem.getExtractionStatus().getExtractionStatusCode();

			if (extractionStatus.intValue() == 1) {
				//Extraction Error
				EProdEnum eProdError = EProdEnum.EPROD_31;

				String logMsg = "Extraction Error: " + bem.getExtractionStatus().getExtractionStatusText();
				LOG.warn(logMsg);
				throw new ApplicationException(logMsg, eProdError.getCode());
			}
		}
	}

	/*
	 * Method to check whether Total Premium Amount is 0, if so skip the transaction.
	 */
	private void validateZeroPremium(BenefitEnrollmentMaintenanceDTO bemDTO) {

		BigDecimal totalPremiumAmount = BEMDataUtil.getTotalPremiumAmount(BEMDataUtil.getSubscriberMember(bemDTO.getBem()));

		if (totalPremiumAmount != null && totalPremiumAmount.compareTo(BigDecimal.ZERO) == 0) {

			//EPROD-37: $0 Total Premium Amount
			EProdEnum eProdError = EProdEnum.EPROD_37;

			String logMsg = eProdError.getLogMsg();
			LOG.warn(logMsg);
			throw new ApplicationException(logMsg, eProdError.getCode());
		}
	}

	/*
	 * Method to check whether any earlier version of policy was skipped
	 *  
	 * @param bemDTO
	 */
	private void checkPriorSkippedVersions(BenefitEnrollmentMaintenanceDTO bemDTO) {

		Integer skipCount = txnMsgService.getSkippedTransMsgCount(bemDTO);

		if (skipCount != null && skipCount.intValue() > 0) {
			//EPROD-29: ERL - Previous Version of the Policy was skipped. Skip the current transaction
			EProdEnum eProdError = EProdEnum.EPROD_29;

			String logMsg = eProdError.getLogMsg();
			LOG.warn(logMsg);
			String exMsg = "Previous Version of the Policy was skipped.";
			throw new ApplicationException(exMsg, eProdError.getCode());
		}
	}

	/*
	 * Method to check whether same policy was skipped in a previous batch
	 *  
	 * @param bemDTO
	 */
	private boolean checkCurrentVersionWasSkipped(BenefitEnrollmentMaintenanceDTO bemDTO) {

		Integer numTimesVersionSkipped = txnMsgService.getSkippedVersionCount(bemDTO);

		if (numTimesVersionSkipped != null && numTimesVersionSkipped.intValue() > 0) {
			//EPROD-34 Policy version found in 'Skipped' status in an earlier processing.
			// Setting the inbound version to 'D' and all later versions to 'R'
			LOG.warn(EProdEnum.EPROD_34.getLogMsg() + 
					"Setting the earlier version to 'D' and current and later versions to 'R'");
			return true;
		}
		return false;
	}

	/*
	 * Method to check for same version of the policy in EPS bearing same timestamp
	 *  
	 * @param dbBemDto
	 * @param bemDTO
	 */
	private boolean checkIfDuplicateVersionExists(
			BenefitEnrollmentMaintenanceDTO dbBemDto,
			BenefitEnrollmentMaintenanceDTO bemDTO) {
		if (dbBemDto.getSourceVersionId() != null && dbBemDto.getSourceVersionDateTime() != null) {

			TransactionInformationType transactionInfo = bemDTO.getBem().getTransactionInformation(); 
			if (transactionInfo != null) {
				String sourceVersionId = transactionInfo.getPolicySnapshotVersionNumber();
				LocalDateTime sourceVersionTS = transactionInfo.getPolicySnapshotDateTime().toGregorianCalendar().toZonedDateTime().toLocalDateTime();

				if(sourceVersionId != null && sourceVersionTS != null 
						&& dbBemDto.getSourceVersionId().intValue() == Integer.parseInt(sourceVersionId)
						&& dbBemDto.getSourceVersionDateTime().isEqual(sourceVersionTS)) {
					// Will log EPROD-35 in writer.
					return true;
				}
			}
		}

		return false;
	}

	/*
	 * Method to check for later versions of the policy in EPS
	 *  
	 * @param dbBemDto
	 * @param bemDTO
	 */
	private boolean checkIfLaterVersionExists(
			BenefitEnrollmentMaintenanceDTO dbBemDto, BenefitEnrollmentMaintenanceDTO bemDTO) {

		if (dbBemDto.getSourceVersionId() != null) {

			TransactionInformationType transactionInfo = bemDTO.getBem().getTransactionInformation(); 
			
			if (transactionInfo != null) {
				
				String sourceVersionId = transactionInfo.getPolicySnapshotVersionNumber();
				
				if(sourceVersionId != null) {
					
					if(dbBemDto.getSourceVersionId().intValue() > Integer.parseInt(sourceVersionId)) {
						// Will log EPROD-35 in writer.
						return true;
						
					} else if(dbBemDto.getSourceVersionId().intValue() == Integer.parseInt(sourceVersionId)) { 
						
						LocalDateTime sourceVersionTS = transactionInfo.getPolicySnapshotDateTime().toGregorianCalendar().toZonedDateTime().toLocalDateTime();
						
						if(dbBemDto.getSourceVersionDateTime().isAfter(sourceVersionTS)) {
							// Will log EPROD-35 in writer.
							return true;
						}
					}
				}
			}
		}
		return false;
	}

	/*
	 * Method to validate Market Place  Group Policy Id among inbound version and eps version
	 */
	private void validateMarketPlaceGroupPolicyId(
			BenefitEnrollmentMaintenanceDTO dbBemDto, BenefitEnrollmentMaintenanceDTO bemDTO) {

		String marketPlaceGrpPolicyId = bemDTO.getBem().getPolicyInfo().getMarketplaceGroupPolicyIdentifier();
		String epsMarketPlaceGrpPolicyId = dbBemDto.getMarketplaceGroupPolicyId();

		if (StringUtils.isNotEmpty(epsMarketPlaceGrpPolicyId)) {

			if(StringUtils.isEmpty(marketPlaceGrpPolicyId) 
					|| !marketPlaceGrpPolicyId.equalsIgnoreCase(epsMarketPlaceGrpPolicyId)) {
				//EPROD-38: Issuer HIOS ID for transaction does not match existing Policy

				EProdEnum eProd = EProdEnum.EPROD_38;

				String logMsg = eProd.getLogMsg();
				LOG.warn(logMsg);
				throw new ApplicationException(logMsg, eProd.getCode());

			}
		}
	}

	/*
	 * Method to validate Group Sender id against Issuer HIOS Id
	 * 
	 * @param dbBemDto
	 * @param bemDTO
	 * @return boolean
	 */
	private boolean validateGroupSenderId(
			BenefitEnrollmentMaintenanceDTO dbBemDto, BenefitEnrollmentMaintenanceDTO bemDTO) {
		String grpSenderID = bemDTO.getFileInformation().getGroupSenderID();
		BenefitEnrollmentMaintenanceType bem = dbBemDto.getBem();

		String hiosIdFromGrpSenderId = getHiosIdFromGrpSenderId(grpSenderID);
		String issuerHiosId = bem.getIssuer() == null? 
				null : (bem.getIssuer().getHIOSID() == null? 
						null : bem.getIssuer().getHIOSID());

		if (StringUtils.isNotEmpty(hiosIdFromGrpSenderId) && StringUtils.isNotEmpty(issuerHiosId)) {
			if(!hiosIdFromGrpSenderId.equalsIgnoreCase(issuerHiosId)) {
				//EPROD-19: Issuer HIOS ID for transaction does not match existing Policy
				LOG.error(EProdEnum.EPROD_19.getLogMsg());

				//Add error to errors to list
				blEditErrors.add(createErrorLogDTO(bemDTO,
						EPSConstants.ERCODE_INCORRECT_DATA, "IssuerHIOSID", EPSConstants.E050_DESC));

				return false;
			}
		}
		return true;
	}

	/*
	 * Invoke PolicyDataService to retrieve the latest policy version from DB
	 *  
	 * @param bemDTO
	 * @return dbBemDto
	 */
	private BenefitEnrollmentMaintenanceDTO performPolicyMatch(
			BenefitEnrollmentMaintenanceDTO bemDTO) {

		BenefitEnrollmentMaintenanceDTO dbBemDto = policyDataService.getLatestBEMByPolicyId(bemDTO);

		if(dbBemDto != null) {

			LOG.debug("Matching Policy found: " + dbBemDto.getPolicyVersionId());
			bemDTO.setPolicyVersionId(dbBemDto.getPolicyVersionId());
			bemDTO.setSubscriberStateCd(dbBemDto.getSubscriberStateCd());
		}

		return dbBemDto;
	}


	/*
	 * Retrieve the prorated premium records from the subscriber loops
	 */
	private Map<LocalDate, AdditionalInfoType> processInboundPremiums(
			BenefitEnrollmentMaintenanceDTO bemDTO) {

		Map<LocalDate, AdditionalInfoType> inboundPremiums = new LinkedHashMap<LocalDate, AdditionalInfoType>();

		BenefitEnrollmentMaintenanceType bem = bemDTO.getBem();

		List<MemberType> inboundSubscribers = BEMDataUtil.getSubscriberOccurrances(bem);

		//Iterate through each subscriber loop in the bem - first subscriber loop will contain the subscriber attributes
		//along with monthly financial amounts. Subsequent subscriber loops if present (max of 3) will contain the prorated financial amounts
		for (MemberType inboundSubscriber : inboundSubscribers) {

			Map<LocalDate, AdditionalInfoType> tempMap = financialValidator.processInboundPremiums(inboundSubscriber);

			LocalDate premiumEsd = financialValidator.determineSystemSelectedEffectiveStartDate(inboundSubscriber);
			//This check handles the future situation when FFM E&E aligns CIPP(Monthly Premium) Start Date with the IPP Policy Start Date
			if(inboundPremiums.containsKey(premiumEsd)) {

				AdditionalInfoType nonProratedPremium = inboundPremiums.get(premiumEsd);
				inboundPremiums.remove(premiumEsd);

				LocalDate newKey = DateTimeUtil.getLocalDateFromXmlGC(tempMap.get(premiumEsd).getEffectiveEndDate()).plusDays(1);

				inboundPremiums.put(newKey, nonProratedPremium);
			}

			//Create the Map of Premium records to be processed
			inboundPremiums.putAll(tempMap);
		}

		if (!inboundPremiums.isEmpty()) {
			processProratedAmounts(inboundPremiums, bemDTO.getBem());
		}
		//Remove prorated amounts subscriber loops from Bem since it should not be persisted in db
		if(inboundSubscribers.size() > 1) {
			removeProratedSubscribers(bem);
		}

		return inboundPremiums;
	}

	/*
	 * Get the prorated amounts. The inbound Bem is assumed to have 1 to 3 subscriber loops. 2nd and 3rd if present will carry the prorated amounts
	 */
	private void processProratedAmounts(Map<LocalDate, AdditionalInfoType> inboundPremiums, BenefitEnrollmentMaintenanceType bem) {

		LocalDate policyStartDate = BEMDataUtil.getPolicyStartDate(bem);
		LocalDate policyEndDate = BEMDataUtil.getPolicyEndDate(bem);

		if(inboundPremiums.size() > 1) {
			LOG.info("Multiple subscribers present - process prorated amounts from 2nd and 3rd subscriber loops"); 

			//Prevent Overlapping EPS Policy Premium Records for Policies effective <1 Month

			//Identify if any subscriber loop with Prorated amounts has EffectiveStartDate and EffectiveEndDate identical to Policy Start AND End Date
			Iterator<Map.Entry<LocalDate, AdditionalInfoType>> iterator = inboundPremiums.entrySet().iterator();
			Map.Entry<LocalDate, AdditionalInfoType> firstSubscriberPremium = iterator.next();

			LOG.info("first subscriber key: " + firstSubscriberPremium.getKey());

			boolean hasIdenticalDates = false;

			while(iterator.hasNext()){

				Map.Entry<LocalDate, AdditionalInfoType> subscriberPremium = iterator.next();
				AdditionalInfoType additionalInfo = subscriberPremium.getValue();
				LocalDate dateKey = subscriberPremium.getKey();

				//Remove the non-prorated premium which is after policy end date 
				//(This corresponds to removing non prorated monthly premium record for 1/15-2/15 type of scenario)
				// Add null check for PED.  Cannot be null for java.time.localDate else NPE.
				if (policyEndDate != null) {
					if(dateKey.isAfter(policyEndDate)) {
						iterator.remove();
						continue;
					}
				}

				LOG.info("subscriber esd: " + additionalInfo.getEffectiveStartDate());

				LocalDate premiumStartDt = DateTimeUtil.getLocalDateFromXmlGC(additionalInfo.getEffectiveStartDate());
				LocalDate premiumEndDt = DateTimeUtil.getLocalDateFromXmlGC(additionalInfo.getEffectiveEndDate());

				if(premiumStartDt.isEqual(policyStartDate) && premiumEndDt.isEqual(policyEndDate)) {
					hasIdenticalDates = true;
					break;
				}

				//Identify if any subscriber loop with prorated amounts has EffectiveStartDate and EffectiveEndDate identical to those in other subscriber loop (without prorated amounts)
				LocalDate nonProratedEsd = DateTimeUtil.getLocalDateFromXmlGC(firstSubscriberPremium.getValue().getEffectiveStartDate());
				LocalDate nonProratedEed = DateTimeUtil.getLocalDateFromXmlGC(firstSubscriberPremium.getValue().getEffectiveEndDate());

				if(premiumStartDt.isEqual(nonProratedEsd) && premiumEndDt.isEqual(nonProratedEed)) {
					hasIdenticalDates = true;
					break;
				}
			}

			//Prorated Start & End Dates identical to policy/non-prorated subscriber loop
			if(hasIdenticalDates) {
				//Ignore the subscriber loop without prorated amounts and only process the subscriber loop with Prorated Amounts
				inboundPremiums.remove(firstSubscriberPremium.getKey());

			} else {
				//EPS Policy-Premium Record Date Alignment
				performPremiumDateAlignment(inboundPremiums);
			}

		} else { //Single subscriber
			LOG.debug("Single subscriber - Setting policy start and end dates to premium eff start and end dates");

			//Modify EffectiveStartDate and EffectiveEndDate of Policy Premium record to align with the PolicyStartDate and Policy EndDate
			Map.Entry<LocalDate,AdditionalInfoType> entry = inboundPremiums.entrySet().iterator().next();

			LocalDate premiumStartDt = entry.getKey();

			AdditionalInfoType premiumRec = entry.getValue();
			premiumRec.setEffectiveStartDate(DateTimeUtil.getXMLGregorianCalendar(policyStartDate));
			premiumRec.setEffectiveEndDate(DateTimeUtil.getXMLGregorianCalendar(policyEndDate));

			inboundPremiums.remove(premiumStartDt);
			inboundPremiums.put(policyStartDate, premiumRec);
		}

	}

	/*
	 * EPS Policy-Premium Records Date Alignment
	 */
	private void performPremiumDateAlignment(Map<LocalDate, AdditionalInfoType> inboundPremiums) {

		Iterator<Map.Entry<LocalDate, AdditionalInfoType>> iterator = inboundPremiums.entrySet().iterator();

		AdditionalInfoType firstSubscriberPremium = iterator.next().getValue();

		LocalDate nonProratedEsd = DateTimeUtil.getLocalDateFromXmlGC(firstSubscriberPremium.getEffectiveStartDate());

		while(iterator.hasNext()){

			AdditionalInfoType additionalInfo = iterator.next().getValue();

			LocalDate premiumStartDt = DateTimeUtil.getLocalDateFromXmlGC(additionalInfo.getEffectiveStartDate());
			LocalDate premiumEndDt = DateTimeUtil.getLocalDateFromXmlGC(additionalInfo.getEffectiveEndDate());

			if(!premiumStartDt.isAfter(nonProratedEsd)) {
				//For any PolicyPremium record where the preceding record has prorated amounts, modify EffectiveStartDate of the selected record to be equal to the EffectiveEndDate of preceding record + 1 calendar day
				firstSubscriberPremium.setEffectiveStartDate(
						DateTimeUtil.getXMLGregorianCalendar(premiumEndDt.plusDays(1)));
			} else {
				//For any PolicyPremium record where the trailing record has prorated amounts, modify EffectiveEndDate of the selected record to be equal to the EffectiveStartDate of the trailing record - 1 calendar day
				firstSubscriberPremium.setEffectiveEndDate(
						DateTimeUtil.getXMLGregorianCalendar(premiumStartDt.minusDays(1)));
			}
		}
	}


	/*
	 * Remove Prorated Subscriber loops from Bem
	 */
	private void removeProratedSubscribers(BenefitEnrollmentMaintenanceType bem) {

		if (bem != null) {
			Iterator<MemberType> iterator = bem.getMember().iterator();
			int subscriberIndex = 0;

			while(iterator.hasNext()) {
				MemberType member = iterator.next();

				//Keep only the first subscriber loop and remove subsequent ones, since amounts have been read and is not to be persisted in db
				if(BEMDataUtil.getIsSubscriber(member)) {
					subscriberIndex++;

					if(subscriberIndex > 1) {
						iterator.remove();
					}					
				}
			}
		}
	}

	/*
	 * Private method to get Issuer Hios Id from group receiver id. 
	 */
	private String getHiosIdFromGrpSenderId(String grpSenderId) {
		String hiosId = StringUtils.EMPTY;

		if (StringUtils.isNotEmpty(grpSenderId)) {
			if (grpSenderId.length() > HIOS_ID_INDEX) {
				hiosId = grpSenderId.substring(0, HIOS_ID_INDEX);
			} else {
				LOG.debug("Invalid Group Sender ID, unable to extract HIOS ID for groupSenderID = " + grpSenderId);
			}
		}
		return hiosId;
	}

	/*
	 * Method to create ErrorWarningLogDTO object for given error
	 */
	private ErrorWarningLogDTO createErrorLogDTO(
			BenefitEnrollmentMaintenanceDTO bemDTO, String errorCode,
			String elementInError, String errorDescription) {
		ErrorWarningLogDTO errorWarningDTO = new ErrorWarningLogDTO(bemDTO);
		errorWarningDTO.setBizAppAckErrorCd(errorCode);
		errorWarningDTO.setErrorElement(elementInError);
		errorWarningDTO.setErrorWarningDetailedDesc(errorDescription);

		return errorWarningDTO;
	}

	/**
	 * @param policyDataService the policyDataService to set
	 */
	public void setPolicyDataService(PolicyDataService policyDataService) {
		this.policyDataService = policyDataService;
	}

	public void setTxnMsgService(TransMsgCompositeDAO txnMsgService) {
		this.txnMsgService = txnMsgService;
	}

	/**
	 * @param financialValidator the financialValidator to set
	 */
	public void setFinancialValidator(FinancialValidator financialValidator) {
		this.financialValidator = financialValidator;
	}
}
