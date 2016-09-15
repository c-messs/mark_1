package gov.hhs.cms.ff.fm.eps.ep.po;

import java.time.LocalDate;

import gov.hhs.cms.ff.fm.eps.ep.enums.PolicyStatus;

/**
 * @author j.radziewski
 * 
 * Data side domain object for processing outbound SBMFILESUMMARYMISSINGPOLICY data joined
 * from POLICYVERSION and POLICYSTATUS.
 *
 */
public class SbmFileSummaryMissingPolicyData extends SbmFileSummaryMissingPolicyPO {
	
	private String exchangePolicyId;
    private String planId;
    private PolicyStatus policyStatus;
    
    // For determining missing policies terminated count. Not in outbound response.
    private LocalDate policyEndDate;
    
	/**
	 * @return the exchangePolicyId
	 */
	public String getExchangePolicyId() {
		return exchangePolicyId;
	}
	/**
	 * @param exchangePolicyId the exchangePolicyId to set
	 */
	public void setExchangePolicyId(String exchangePolicyId) {
		this.exchangePolicyId = exchangePolicyId;
	}
	/**
	 * @return the planId
	 */
	public String getPlanId() {
		return planId;
	}
	/**
	 * @param planId the planId to set
	 */
	public void setPlanId(String planId) {
		this.planId = planId;
	}
	/**
	 * @return the policyStatus
	 */
	public PolicyStatus getPolicyStatus() {
		return policyStatus;
	}
	/**
	 * @param policyStatus the policyStatus to set
	 */
	public void setPolicyStatus(PolicyStatus policyStatus) {
		this.policyStatus = policyStatus;
	}
	/**
	 * @return the policyEndDate
	 */
	public LocalDate getPolicyEndDate() {
		return policyEndDate;
	}
	/**
	 * @param policyEndDate the policyEndDate to set
	 */
	public void setPolicyEndDate(LocalDate policyEndDate) {
		this.policyEndDate = policyEndDate;
	}

}
