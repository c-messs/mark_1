/**
 * 
 */
package gov.hhs.cms.ff.fm.eps.rap.service;

import com.accenture.foundation.common.exception.ApplicationException;
import com.accenture.foundation.common.exception.EnvironmentException;

/**
 * @author girish.padmanabhan
 *
 */
public interface RapProcessingService {

	/**
	 * @param request
	 * @return
	 * @throws ApplicationException
	 * @throws EnvironmentException
	 */
	public RAPProcessingResponse processRetroActivePayments(RAPProcessingRequest request) 
			throws ApplicationException, EnvironmentException;
}
