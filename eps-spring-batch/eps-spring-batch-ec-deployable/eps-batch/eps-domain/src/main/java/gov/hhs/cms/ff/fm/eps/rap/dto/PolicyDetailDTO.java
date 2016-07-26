/**
 * 
 */
package gov.hhs.cms.ff.fm.eps.rap.dto;

import gov.hhs.cms.ff.fm.eps.rap.domain.PolicyPremium;

import java.util.List;

/**
 * @author girish.padmanabhan
 *
 */
public class PolicyDetailDTO {

	private List<PolicyPremium> policyPremiums;
	private List<PolicyPaymentTransDTO> policyPayments;

	/**
	 * @return the policyPremiums
	 */
	public List<PolicyPremium> getPolicyPremiums() {
		return policyPremiums;
	}

	/**
	 * @param policyPremiums the policyPremiums to set
	 */
	public void setPolicyPremiums(List<PolicyPremium> policyPremiumChanges) {
		this.policyPremiums = policyPremiumChanges;
	}
	
	/**
	 * @return the policyPayments
	 */
	public List<PolicyPaymentTransDTO> getPolicyPayments() {
		return policyPayments;
	}

	/**
	 * @param policyPayments the policyPayments to set
	 */
	public void setPolicyPayments(List<PolicyPaymentTransDTO> policyPayments) {
		this.policyPayments = policyPayments;
	}
}
