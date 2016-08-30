package gov.hhs.cms.ff.fm.eps.rap.dto;

import org.joda.time.DateTime;

/**
 * This class is the DTO used for PolicyVersion data
 * 
 * @author girish.padmanabhan
 *
 */
public class PolicyDataDTO {
	
	private long policyVersionId;
	private String marketplaceGroupPolicyId;
	private String subscriberStateCd; 
	private String issuerHiosId;
	private String planId;
	private String policyStatus;
	private String insrncAplctnTypeCd; 
	private String exchangePolicyId; 
	private DateTime policyStartDate;
	private DateTime policyEndDate;
	private DateTime maintenanceStartDateTime;
	private DateTime issuerStartDate;
	private boolean policyCancelled;

	/**
	 * @return the policyVersionId
	 */
	public long getPolicyVersionId() {
		return policyVersionId;
	}

	/**
	 * @param policyVersionId the policyVersionId to set
	 */
	public void setPolicyVersionId(long policyVersionId) {
		this.policyVersionId = policyVersionId;
	}


	/**
	 * @return the marketplaceGroupPolicyId
	 */
	public String getMarketplaceGroupPolicyId() {
		return marketplaceGroupPolicyId;
	}

	/**
	 * @param marketplaceGroupPolicyId the marketplaceGroupPolicyId to set
	 */
	public void setMarketplaceGroupPolicyId(String marketplaceGroupPolicyId) {
		this.marketplaceGroupPolicyId = marketplaceGroupPolicyId;
	}

	/**
	 * @return the subscriberStateCd
	 */
	public String getSubscriberStateCd() {
		return subscriberStateCd;
	}

	/**
	 * @param subscriberStateCd the subscriberStateCd to set
	 */
	public void setSubscriberStateCd(String subscriberStateCd) {
		this.subscriberStateCd = subscriberStateCd;
	}

	/**
	 * @return the issuerHiosId
	 */
	public String getIssuerHiosId() {
		return issuerHiosId;
	}

	/**
	 * @param issuerHiosId the issuerHiosId to set
	 */
	public void setIssuerHiosId(String issuerHiosId) {
		this.issuerHiosId = issuerHiosId;
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
	public String getPolicyStatus() {
		return policyStatus;
	}

	/**
	 * @param policyStatus the policyStatus to set
	 */
	public void setPolicyStatus(String policyStatus) {
		this.policyStatus = policyStatus;
	}

	/**
	 * @return the insrncAplctnTypeCd
	 */
	public String getInsrncAplctnTypeCd() {
		return insrncAplctnTypeCd;
	}

	/**
	 * @param insrncAplctnTypeCd the insrncAplctnTypeCd to set
	 */
	public void setInsrncAplctnTypeCd(String insrncAplctnTypeCd) {
		this.insrncAplctnTypeCd = insrncAplctnTypeCd;
	}

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
	 * @return the policyStartDate
	 */
	public DateTime getPolicyStartDate() {
		return policyStartDate;
	}

	/**
	 * @param policyStartDate the policyStartDate to set
	 */
	public void setPolicyStartDate(DateTime policyStartDate) {
		this.policyStartDate = policyStartDate;
	}

	/**
	 * @return the policyEndDate
	 */
	public DateTime getPolicyEndDate() {
		return policyEndDate;
	}

	/**
	 * @param policyEndDate the policyEndDate to set
	 */
	public void setPolicyEndDate(DateTime policyEndDate) {
		this.policyEndDate = policyEndDate;
	}

	/**
	 * @return the maintenanceStartDateTime
	 */
	public DateTime getMaintenanceStartDateTime() {
		return maintenanceStartDateTime;
	}

	/**
	 * @param maintenanceStartDateTime the maintenanceStartDateTime to set
	 */
	public void setMaintenanceStartDateTime(DateTime maintenanceStartDateTime) {
		this.maintenanceStartDateTime = maintenanceStartDateTime;
	}
	
	/**
	 * @return the issuerStartDate
	 */
	public DateTime getIssuerStartDate() {
		return issuerStartDate;
	}

	/**
	 * @param issuerStartDate the issuerStartDate to set
	 */
	public void setIssuerStartDate(DateTime issuerStartDate) {
		this.issuerStartDate = issuerStartDate;
	}

	/**
	 * @return the policyCancelled
	 */
	public boolean isPolicyCancelled() {
		return policyCancelled;
	}

	/**
	 * @param policyCancelled the policyCancelled to set
	 */
	public void setPolicyCancelled(boolean policyCancelled) {
		this.policyCancelled = policyCancelled;
	}

	@Override
	public String toString() {
		return "PolicyDataDTO [policyVersionId=" + policyVersionId
				+ ", subscriberStateCd=" + subscriberStateCd
				+ ", issuerHiosId=" + issuerHiosId + ", planId=" + planId
				+ ", insrncAplctnTypeCd=" + insrncAplctnTypeCd
				+ ", exchangePolicyId=" + exchangePolicyId
				+ ", policyStartDate=" + policyStartDate + ", policyEndDate="
				+ policyEndDate + ", maintenanceStartDateTime="
				+ maintenanceStartDateTime + "]";
	}
	
}
