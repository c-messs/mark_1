/**
 * 
 */
package gov.hhs.cms.ff.fm.eps.ep.sbm.validation.impl;

import static gov.hhs.cms.ff.fm.eps.ep.sbm.SBMConstants.ERROR_DESC_INCORRECT_VALUE;
import static gov.hhs.cms.ff.fm.eps.ep.sbm.SBMConstants.Y;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gov.cms.dsh.sbmi.PolicyType;
import gov.hhs.cms.ff.fm.eps.ep.SBMValidationRequest;
import gov.hhs.cms.ff.fm.eps.ep.enums.SBMErrorWarningCode;
import gov.hhs.cms.ff.fm.eps.ep.sbm.SBMPolicyDTO;
import gov.hhs.cms.ff.fm.eps.ep.sbm.SBMPremium;
import gov.hhs.cms.ff.fm.eps.ep.sbm.SbmErrWarningLogDTO;
import gov.hhs.cms.ff.fm.eps.ep.sbm.services.SBMDataService;
import gov.hhs.cms.ff.fm.eps.ep.sbm.validation.SbmBusinessValidator;
import gov.hhs.cms.ff.fm.eps.ep.sbm.validation.SbmFinancialValidator;
import gov.hhs.cms.ff.fm.eps.ep.sbm.validation.SbmValidationService;

/**
 * The Validator class implementation for SBM processing.
 * 
 * @author girish.padmanabhan
 *
 */
public class SbmValidationServiceImpl implements SbmValidationService {
	private static final Logger LOG = LoggerFactory.getLogger(SbmValidationServiceImpl.class);

	private SbmBusinessValidator sbmBusinessValidator;
	private SbmFinancialValidator sbmFinancialValidator;
	private SBMDataService sbmDataService;

	List <SbmErrWarningLogDTO> errorWarningList;

	/**
	 * The SBM - specific implementation of the interface method.
	 * 1) Performs policy match 
	 * 2) Performs Business Validations
	 * 
	 * @param sbmValidationRequest
	 * @return 
	 * @throws Exception
	 */
	@Override
	public void validatePolicy(SBMValidationRequest sbmValidationRequest)  {

		errorWarningList = new ArrayList<SbmErrWarningLogDTO>();
		SBMPolicyDTO policyDTO = sbmValidationRequest.getPolicyDTO();

		performBusinessValidations(policyDTO);
		
		performFinancialValidations(policyDTO);
		
		//policy match
		SBMPolicyDTO dbSBMPolicyDTO = performPolicyMatch(policyDTO);
		
		performPolicyMatchValidations(dbSBMPolicyDTO, policyDTO);


		//Add the premiums map to DTO
		Map<LocalDate, SBMPremium> inboundPremiums = processInboundPremiums(policyDTO);
		
		policyDTO.setSbmPremiums(inboundPremiums);

		//Add error to errors to list in Bem DTO
		policyDTO.getErrorList().addAll(errorWarningList);
		
		// do this once and last
		if(SbmValidationUtil.hasValidationError(errorWarningList)) {
			policyDTO.setErrorFlag(true);
		}
		
	}

	private void performFinancialValidations(SBMPolicyDTO policyDTO) {
		
		//Policy Financial Validations
		List <SbmErrWarningLogDTO> financialValidationErrors = sbmFinancialValidator.validateFinancialInfo(policyDTO.getPolicy());
		
		if (CollectionUtils.isNotEmpty(financialValidationErrors)) {
			errorWarningList.addAll(financialValidationErrors);
			
			LOG.info("Policy Financial Validation Errors: " + financialValidationErrors.size());
		}
	}

	private void performBusinessValidations(SBMPolicyDTO policyDTO) {
		
		//Policy Business Validations
		List <SbmErrWarningLogDTO> policyBizErrors = sbmBusinessValidator.validatePolicy(policyDTO);
		
		if (CollectionUtils.isNotEmpty(policyBizErrors)) {
			errorWarningList.addAll(policyBizErrors);
			
			LOG.info("policy Business Validation Errors: " + policyBizErrors.size());
		}
		
		//Member Business Validations
		List <SbmErrWarningLogDTO> policyMemberErrors = sbmBusinessValidator.validatePolicyMembers(policyDTO.getPolicy());
		
		if (CollectionUtils.isNotEmpty(policyMemberErrors)) {
			errorWarningList.addAll(policyMemberErrors);
			
			LOG.info("Policy Member Business Validation Errors: " + policyMemberErrors.size());
		}
	}

	/*
	 * Invoke PolicyDataService to retrieve the latest policy version from EPS DB
	 *  
	 * @param bemDTO
	 * @return dbBemDto
	 */
	private SBMPolicyDTO performPolicyMatch(SBMPolicyDTO policyDTO) {

		SBMPolicyDTO epsPolicyDTO = sbmDataService.performPolicyMatch(policyDTO);

		if(epsPolicyDTO.getPolicy() != null) {

			LOG.debug("Matching Policy found: " + epsPolicyDTO);
		}
		return epsPolicyDTO;
	}
	
	/*
	 * Perform Validations on the matched eps policy
	 */
	private void performPolicyMatchValidations(SBMPolicyDTO dbSBMPolicyDTO, SBMPolicyDTO policyDTO) {
		
		if (dbSBMPolicyDTO != null && dbSBMPolicyDTO.getPolicy() != null) {
			
			policyDTO.setPolicyVersionId(dbSBMPolicyDTO.getPolicyVersionId());

			List <SbmErrWarningLogDTO> policyMatchValidationErrors = sbmBusinessValidator.validateEpsPolicy(dbSBMPolicyDTO, policyDTO);
			
			if (CollectionUtils.isNotEmpty(policyMatchValidationErrors)) {
				errorWarningList.addAll(policyMatchValidationErrors);
			}
			
		} else {
			
			PolicyType policy = policyDTO.getPolicy();
			
			String effectuationInd = policy.getEffectuationIndicator();
			
			if(!effectuationInd.equalsIgnoreCase(Y)) {
				
				//create Error ER-055: incorrect value provided
				errorWarningList.add(SbmValidationUtil.createErrorWarningLogDTO(
						"EffectuationIndicator", SBMErrorWarningCode.ER_055.getCode(), ERROR_DESC_INCORRECT_VALUE));
				
				LOG.info("Non-effectuated Policy ");
			}
		}
	}

	/*
	 * Retrieve the prorated premium records 
	 */
	private Map<LocalDate, SBMPremium> processInboundPremiums(SBMPolicyDTO policyDTO) {

		PolicyType policy = policyDTO.getPolicy();

		Map<LocalDate, SBMPremium> inboundPremiums = sbmFinancialValidator.processInboundPremiums(policy);
		
		return inboundPremiums;
	}

	/**
	 * @param sbmFinancialValidator the sbmFinancialValidator to set
	 */
	public void setSbmFinancialValidator(SbmFinancialValidator sbmFinancialValidator) {
		this.sbmFinancialValidator = sbmFinancialValidator;
	}

	/**
	 * @param sbmBusinessValidator the sbmBusinessValidator to set
	 */
	public void setSbmBusinessValidator(SbmBusinessValidator sbmBusinessValidator) {
		this.sbmBusinessValidator = sbmBusinessValidator;
	}

	/**
	 * @param sbmDataService the sbmDataService to set
	 */
	public void setSbmDataService(SBMDataService sbmDataService) {
		this.sbmDataService = sbmDataService;
	}
}
