/**
 * 
 */
package gov.hhs.cms.ff.fm.eps.rap.service;

import gov.hhs.cms.ff.fm.eps.rap.dto.PolicyDataDTO;

/**
 * @author girish.padmanabhan
 *
 */
public class RAPProcessingRequest {

	PolicyDataDTO policyDataDTO;

	/**
	 * @return the policyDataDTO
	 */
	public PolicyDataDTO getPolicyDataDTO() {
		return policyDataDTO;
	}

	/**
	 * @param policyDataDTO the policyDataDTO to set
	 */
	public void setPolicyDataDTO(PolicyDataDTO policyDataDTO) {
		this.policyDataDTO = policyDataDTO;
	}
}
