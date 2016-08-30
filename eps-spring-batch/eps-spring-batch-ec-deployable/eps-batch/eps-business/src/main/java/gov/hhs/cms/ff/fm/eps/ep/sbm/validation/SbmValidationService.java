/**
 * 
 */
package gov.hhs.cms.ff.fm.eps.ep.sbm.validation;

import gov.hhs.cms.ff.fm.eps.ep.SBMValidationRequest;

/**
 * This is the Service interface for SBM XPR validations
 * 
 * @author girish.padmanabhan
 *
 */
public interface SbmValidationService {

	/**
	 * @param sbmValidationRequest
	 * @return
	 * @throws Exception
	 */
	public void validatePolicy(SBMValidationRequest sbmValidationRequest);
}
