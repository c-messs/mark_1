/**
 * 
 */
package gov.hhs.cms.ff.fm.eps.ep.validation.impl;

import gov.cms.dsh.bem.AdditionalInfoType;
import gov.cms.dsh.bem.BenefitEnrollmentMaintenanceType;
import gov.cms.dsh.bem.TransactionInformationType;
import gov.hhs.cms.ff.fm.eps.ep.BenefitEnrollmentMaintenanceDTO;
import gov.hhs.cms.ff.fm.eps.ep.EPSConstants;
import gov.hhs.cms.ff.fm.eps.ep.EPSValidationRequest;
import gov.hhs.cms.ff.fm.eps.ep.ErrorWarningLogDTO;
import gov.hhs.cms.ff.fm.eps.ep.enums.EProdEnum;
import gov.hhs.cms.ff.fm.eps.ep.services.PolicyDataService;
import gov.hhs.cms.ff.fm.eps.ep.services.TransMsgCompositeDAO;
import gov.hhs.cms.ff.fm.eps.ep.validation.EPSValidationResponse;
import gov.hhs.cms.ff.fm.eps.ep.validation.EPSValidationService;
import gov.hhs.cms.ff.fm.eps.ep.validation.FinancialValidator;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
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
	private TransMsgCompositeDAO txnMsgService;
	private EPSValidationHandler epsValidationHandler;
    List <ErrorWarningLogDTO> blEditErrors;
	private PolicyDataService policyDataService;
	private FinancialValidator financialValidator;

	/**
	 * The FFM - specific implementation of the interface method.
	 * 1) Performs policy match 
	 * 2) Validates Grp Sender id against Issuer HIOS Id
	 * @param epsValidationRequest
	 * @return epsValidationResponse
	 */
	@Override
	public EPSValidationResponse validateBEM(EPSValidationRequest epsValidationRequest)  {

		EPSValidationResponse epsValidationResponse = new EPSValidationResponse();
		blEditErrors = new ArrayList<ErrorWarningLogDTO>();
		BenefitEnrollmentMaintenanceDTO bemDTO = epsValidationRequest.getBenefitEnrollmentMaintenance();
		epsValidationHandler = new EPSValidationHandler();
		epsValidationHandler.validateExtractionStatus(bemDTO);

		epsValidationHandler.validateZeroPremium(bemDTO);

		// Verify there are no transactions for this inbound policy that are in "skipped" status.
		// If true, App Exception will be thrown and this Bem will also be skipped.
		checkPriorSkippedVersions(bemDTO);

		boolean versionSkippedEarlier = checkCurrentVersionWasSkipped(bemDTO);

		if (versionSkippedEarlier) {
			bemDTO.setVersionSkippedInPast(true);
			return epsValidationResponse;
		}

		//policy match
		BenefitEnrollmentMaintenanceDTO dbBemDto = epsValidationHandler.performPolicyMatch(bemDTO,policyDataService);

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
		Map<LocalDate, AdditionalInfoType> inboundPremiums = epsValidationHandler.processInboundPremiums(bemDTO,financialValidator);
		bemDTO.setEpsPremiums(inboundPremiums);

		//Add error to errors to list in Bem DTO
		bemDTO.setErrorList(blEditErrors);

		return epsValidationResponse;
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
