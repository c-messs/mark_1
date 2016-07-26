/**
 * 
 */
package gov.hhs.cms.ff.fm.eps.ep.validation;

import gov.hhs.cms.ff.fm.eps.ep.EPSValidationRequest;

/**
 * This is the Service interface for EPS 834 validations
 * 
 * @author girish.padmanabhan
 *
 */
public interface EPSValidationService {

	/**
	 * @param epsValidationRequest
	 * @return
	 * @throws Exception
	 */
	public EPSValidationResponse validateBEM(EPSValidationRequest epsValidationRequest);
}
