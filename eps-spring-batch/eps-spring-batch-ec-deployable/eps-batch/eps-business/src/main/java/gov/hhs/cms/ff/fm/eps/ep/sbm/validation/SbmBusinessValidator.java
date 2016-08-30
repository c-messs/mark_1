package gov.hhs.cms.ff.fm.eps.ep.sbm.validation;

import java.util.List;

import gov.cms.dsh.sbmi.PolicyType;
import gov.hhs.cms.ff.fm.eps.ep.sbm.SBMPolicyDTO;
import gov.hhs.cms.ff.fm.eps.ep.sbm.SbmErrWarningLogDTO;

/**
 * @author girish.padmanabhan
 * 
 * Business Validation interface for inbound policy.
 *
 */
public interface SbmBusinessValidator {
	

	/**
	 * Validate inbound policy
	 * 
	 * @param policyDTO
	 * @return
	 */
	public List<SbmErrWarningLogDTO> validatePolicy(SBMPolicyDTO policyDTO);
	
	/**
	 * Validate inbound policy members
	 * 
	 * @param policy
	 * @return
	 */
	public List<SbmErrWarningLogDTO> validatePolicyMembers(PolicyType policy);

	/**
	 * Validate inbound policy against the matching policy in EPS
	 * 
	 * @param dbSBMPolicyDTO
	 * @param policyDTO
	 */
	public List<SbmErrWarningLogDTO> validateEpsPolicy(SBMPolicyDTO dbSBMPolicyDTO, SBMPolicyDTO policyDTO);

}
