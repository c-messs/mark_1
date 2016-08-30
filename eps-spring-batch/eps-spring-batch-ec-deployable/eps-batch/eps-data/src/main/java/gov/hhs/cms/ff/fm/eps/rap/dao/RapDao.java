/**
 * 
 */
package gov.hhs.cms.ff.fm.eps.rap.dao;

import gov.hhs.cms.ff.fm.eps.ep.StateProrationConfiguration;
import gov.hhs.cms.ff.fm.eps.rap.domain.IssuerUserFeeRate;
import gov.hhs.cms.ff.fm.eps.rap.dto.PolicyDataDTO;
import gov.hhs.cms.ff.fm.eps.rap.dto.PolicyDetailDTO;

import java.util.List;

import org.joda.time.DateTime;

/**
 * Interface for RAP Data Access
 * 
 * @author girish.padmanabhan
 *
 */
public interface RapDao {

    /**
     * This method retrieves policy payment data for the given policy version
     * 
     * @param policyVersion
     * @return policyDetail
     */
	public PolicyDetailDTO retrievePolicyPaymentData(PolicyDataDTO policyVersion);
	
	/**
	 * Returns next sequence value of PolicyPaymentTransSeq
	 * @return Long PolicyPaymentTransSeq
	 */
	public Long getPolicyPaymentTransNextSeq();
	
	/**
	 * Gets user fee rate for all states for the given year and asOfDate
	 * 
	 * @param asOfDate
	 * @param year
	 * @return issuerUserFeeRates
	 */
	public List<IssuerUserFeeRate> getUserFeeRateForAllStates(DateTime asOfDate, String year);

	/**
	 * Gets Proration Configuration data for all states
	 * 
	 * @return
	 */
	public List<StateProrationConfiguration> getProrationConfiguration();
	
}
