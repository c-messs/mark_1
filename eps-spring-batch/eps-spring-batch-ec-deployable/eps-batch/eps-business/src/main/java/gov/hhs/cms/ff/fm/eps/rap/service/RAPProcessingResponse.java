/**
 * 
 */
package gov.hhs.cms.ff.fm.eps.rap.service;

import gov.hhs.cms.ff.fm.eps.rap.dto.PolicyPaymentTransDTO;

import java.util.ArrayList;
import java.util.List;

/**
 * @author girish.padmanabhan
 *
 */
public class RAPProcessingResponse {

	List<PolicyPaymentTransDTO> policyPaymentTransactions;

	/**
	 * @return the policyPaymentTransactions
	 */
	public List<PolicyPaymentTransDTO> getPolicyPaymentTransactions() {
		if (policyPaymentTransactions == null) {
			policyPaymentTransactions = new ArrayList<PolicyPaymentTransDTO>();
		}
		return policyPaymentTransactions;
	}

	/**
	 * @param policyPaymentTransactions the policyPaymentTransactions to set
	 */
	public void setPolicyPaymentTransactions(
			List<PolicyPaymentTransDTO> policyPaymentTransactions) {
		this.policyPaymentTransactions = policyPaymentTransactions;
	}

}
