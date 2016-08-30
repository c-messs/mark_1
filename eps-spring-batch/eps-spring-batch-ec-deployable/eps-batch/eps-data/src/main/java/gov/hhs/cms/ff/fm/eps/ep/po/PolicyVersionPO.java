package gov.hhs.cms.ff.fm.eps.ep.po;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * The persistent class for the EPSPOLICY database table.
 * 
 */

public class PolicyVersionPO extends GenericPolicyPO<PolicyVersionPO> {
	
	// Excluded from Hashcode and Equals (FFM, policy compare)
	private Long previousPolicyVersionId;
	private LocalDateTime maintenanceStartDateTime;
	private LocalDateTime maintenanceEndDateTime;
	private Long transMsgID;
	private String marketplaceGroupPolicyId;
	private String issuerTaxPayerId;
	private String issuerNm;
	private LocalDateTime transDateTime;
	private String transControlNum;
	private LocalDate eligibilityStartDate;
	private LocalDate eligibilityEndDate;
	private LocalDate premiumPaidToEndDate;
	private LocalDate lastPremiumPaidDate;
	private String employerIdentificationNum;
	private LocalDate changeReportedDate;
	private String x12CoverageLevelTypeCd;
	private String employerGroupNum;
	private String insrncAplctnTypeCd;
	private Long sourceVersionId;
	private LocalDateTime sourceVersionDateTime;
	
	// Attributes included in Hashcode and Equals (SBM).
	private String subscriberStateCd;
	private String exchangePolicyId;
	private String issuerPolicyId;
	private String issuerHiosId;
	private String issuerSubscriberID;
	private String exchangeAssignedSubscriberID;
	private LocalDate policyStartDate;
	private LocalDate policyEndDate;
	private String planID;
	private String x12InsrncLineTypeCd;
	
	/**
	 * @return the previousPolicyVersionId
	 */
	public Long getPreviousPolicyVersionId() {
		return previousPolicyVersionId;
	}
	/**
	 * @param previousPolicyVersionId the previousPolicyVersionId to set
	 */
	public void setPreviousPolicyVersionId(Long previousPolicyVersionId) {
		this.previousPolicyVersionId = previousPolicyVersionId;
	}
	/**
	 * @return the maintenanceStartDateTime
	 */
	public LocalDateTime getMaintenanceStartDateTime() {
		return maintenanceStartDateTime;
	}
	/**
	 * @param maintenanceStartDateTime the maintenanceStartDateTime to set
	 */
	public void setMaintenanceStartDateTime(LocalDateTime maintenanceStartDateTime) {
		this.maintenanceStartDateTime = maintenanceStartDateTime;
	}
	/**
	 * @return the maintenanceEndDateTime
	 */
	public LocalDateTime getMaintenanceEndDateTime() {
		return maintenanceEndDateTime;
	}
	/**
	 * @param maintenanceEndDateTime the maintenanceEndDateTime to set
	 */
	public void setMaintenanceEndDateTime(LocalDateTime maintenanceEndDateTime) {
		this.maintenanceEndDateTime = maintenanceEndDateTime;
	}
	/**
	 * @return the transMsgID
	 */
	public Long getTransMsgID() {
		return transMsgID;
	}
	/**
	 * @param transMsgID the transMsgID to set
	 */
	public void setTransMsgID(Long transMsgID) {
		this.transMsgID = transMsgID;
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
	 * @return the issuerTaxPayerId
	 */
	public String getIssuerTaxPayerId() {
		return issuerTaxPayerId;
	}
	/**
	 * @param issuerTaxPayerId the issuerTaxPayerId to set
	 */
	public void setIssuerTaxPayerId(String issuerTaxPayerId) {
		this.issuerTaxPayerId = issuerTaxPayerId;
	}
	/**
	 * @return the issuerNm
	 */
	public String getIssuerNm() {
		return issuerNm;
	}
	/**
	 * @param issuerNm the issuerNm to set
	 */
	public void setIssuerNm(String issuerNm) {
		this.issuerNm = issuerNm;
	}
	/**
	 * @return the transDateTime
	 */
	public LocalDateTime getTransDateTime() {
		return transDateTime;
	}
	/**
	 * @param transDateTime the transDateTime to set
	 */
	public void setTransDateTime(LocalDateTime transDateTime) {
		this.transDateTime = transDateTime;
	}
	/**
	 * @return the eligibilityStartDate
	 */
	public LocalDate getEligibilityStartDate() {
		return eligibilityStartDate;
	}
	/**
	 * @param eligibilityStartDate the eligibilityStartDate to set
	 */
	public void setEligibilityStartDate(LocalDate eligibilityStartDate) {
		this.eligibilityStartDate = eligibilityStartDate;
	}
	/**
	 * @return the eligibilityEndDate
	 */
	public LocalDate getEligibilityEndDate() {
		return eligibilityEndDate;
	}
	/**
	 * @param eligibilityEndDate the eligibilityEndDate to set
	 */
	public void setEligibilityEndDate(LocalDate eligibilityEndDate) {
		this.eligibilityEndDate = eligibilityEndDate;
	}
	/**
	 * @return the premiumPaidToEndDate
	 */
	public LocalDate getPremiumPaidToEndDate() {
		return premiumPaidToEndDate;
	}
	/**
	 * @param premiumPaidToEndDate the premiumPaidToEndDate to set
	 */
	public void setPremiumPaidToEndDate(LocalDate premiumPaidToEndDate) {
		this.premiumPaidToEndDate = premiumPaidToEndDate;
	}
	/**
	 * @return the lastPremiumPaidDate
	 */
	public LocalDate getLastPremiumPaidDate() {
		return lastPremiumPaidDate;
	}
	/**
	 * @param lastPremiumPaidDate the lastPremiumPaidDate to set
	 */
	public void setLastPremiumPaidDate(LocalDate lastPremiumPaidDate) {
		this.lastPremiumPaidDate = lastPremiumPaidDate;
	}
	/**
	 * @return the employerIdentificationNum
	 */
	public String getEmployerIdentificationNum() {
		return employerIdentificationNum;
	}
	/**
	 * @param employerIdentificationNum the employerIdentificationNum to set
	 */
	public void setEmployerIdentificationNum(String employerIdentificationNum) {
		this.employerIdentificationNum = employerIdentificationNum;
	}
	/**
	 * @return the changeReportedDate
	 */
	public LocalDate getChangeReportedDate() {
		return changeReportedDate;
	}
	/**
	 * @param changeReportedDate the changeReportedDate to set
	 */
	public void setChangeReportedDate(LocalDate changeReportedDate) {
		this.changeReportedDate = changeReportedDate;
	}
	/**
	 * @return the x12CoverageLevelTypeCd
	 */
	public String getX12CoverageLevelTypeCd() {
		return x12CoverageLevelTypeCd;
	}
	/**
	 * @param x12CoverageLevelTypeCd the x12CoverageLevelTypeCd to set
	 */
	public void setX12CoverageLevelTypeCd(String x12CoverageLevelTypeCd) {
		this.x12CoverageLevelTypeCd = x12CoverageLevelTypeCd;
	}
	/**
	 * @return the employerGroupNum
	 */
	public String getEmployerGroupNum() {
		return employerGroupNum;
	}
	/**
	 * @param employerGroupNum the employerGroupNum to set
	 */
	public void setEmployerGroupNum(String employerGroupNum) {
		this.employerGroupNum = employerGroupNum;
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
	 * @return the sourceVersionId
	 */
	public Long getSourceVersionId() {
		return sourceVersionId;
	}
	/**
	 * @param sourceVersionId the sourceVersionId to set
	 */
	public void setSourceVersionId(Long sourceVersionId) {
		this.sourceVersionId = sourceVersionId;
	}
	/**
	 * @return the sourceVersionDateTime
	 */
	public LocalDateTime getSourceVersionDateTime() {
		return sourceVersionDateTime;
	}
	/**
	 * @param sourceVersionDateTime the sourceVersionDateTime to set
	 */
	public void setSourceVersionDateTime(LocalDateTime sourceVersionDateTime) {
		this.sourceVersionDateTime = sourceVersionDateTime;
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
	 * @return the issuerPolicyId
	 */
	public String getIssuerPolicyId() {
		return issuerPolicyId;
	}
	/**
	 * @param issuerPolicyId the issuerPolicyId to set
	 */
	public void setIssuerPolicyId(String issuerPolicyId) {
		this.issuerPolicyId = issuerPolicyId;
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
	 * @return the issuerSubscriberID
	 */
	public String getIssuerSubscriberID() {
		return issuerSubscriberID;
	}
	/**
	 * @param issuerSubscriberID the issuerSubscriberID to set
	 */
	public void setIssuerSubscriberID(String issuerSubscriberID) {
		this.issuerSubscriberID = issuerSubscriberID;
	}
	/**
	 * @return the exchangeAssignedSubscriberID
	 */
	public String getExchangeAssignedSubscriberID() {
		return exchangeAssignedSubscriberID;
	}
	/**
	 * @param exchangeAssignedSubscriberID the exchangeAssignedSubscriberID to set
	 */
	public void setExchangeAssignedSubscriberID(String exchangeAssignedSubscriberID) {
		this.exchangeAssignedSubscriberID = exchangeAssignedSubscriberID;
	}
	/**
	 * @return the transControlNum
	 */
	public String getTransControlNum() {
		return transControlNum;
	}
	/**
	 * @param transControlNum the transControlNum to set
	 */
	public void setTransControlNum(String transControlNum) {
		this.transControlNum = transControlNum;
	}
	/**
	 * @return the policyStartDate
	 */
	public LocalDate getPolicyStartDate() {
		return policyStartDate;
	}
	/**
	 * @param policyStartDate the policyStartDate to set
	 */
	public void setPolicyStartDate(LocalDate policyStartDate) {
		this.policyStartDate = policyStartDate;
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
	/**
	 * @return the planID
	 */
	public String getPlanID() {
		return planID;
	}
	/**
	 * @param planID the planID to set
	 */
	public void setPlanID(String planID) {
		this.planID = planID;
	}
	/**
	 * @return the x12InsrncLineTypeCd
	 */
	public String getX12InsrncLineTypeCd() {
		return x12InsrncLineTypeCd;
	}
	/**
	 * @param x12InsrncLineTypeCd the x12InsrncLineTypeCd to set
	 */
	public void setX12InsrncLineTypeCd(String x12InsrncLineTypeCd) {
		this.x12InsrncLineTypeCd = x12InsrncLineTypeCd;
	}
	
	
	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((exchangeAssignedSubscriberID == null) ? 0 : exchangeAssignedSubscriberID.hashCode());
		result = prime * result + ((exchangePolicyId == null) ? 0 : exchangePolicyId.hashCode());
		result = prime * result + ((issuerHiosId == null) ? 0 : issuerHiosId.hashCode());
		result = prime * result + ((issuerPolicyId == null) ? 0 : issuerPolicyId.hashCode());
		result = prime * result + ((issuerSubscriberID == null) ? 0 : issuerSubscriberID.hashCode());
		result = prime * result + ((planID == null) ? 0 : planID.hashCode());
		result = prime * result + ((policyEndDate == null) ? 0 : policyEndDate.hashCode());
		result = prime * result + ((policyStartDate == null) ? 0 : policyStartDate.hashCode());
		result = prime * result + ((subscriberStateCd == null) ? 0 : subscriberStateCd.hashCode());
		result = prime * result + ((x12InsrncLineTypeCd == null) ? 0 : x12InsrncLineTypeCd.hashCode());
		return result;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		PolicyVersionPO other = (PolicyVersionPO) obj;
		if (exchangeAssignedSubscriberID == null) {
			if (other.exchangeAssignedSubscriberID != null)
				return false;
		} else if (!exchangeAssignedSubscriberID.equals(other.exchangeAssignedSubscriberID))
			return false;
		if (exchangePolicyId == null) {
			if (other.exchangePolicyId != null)
				return false;
		} else if (!exchangePolicyId.equals(other.exchangePolicyId))
			return false;
		if (issuerHiosId == null) {
			if (other.issuerHiosId != null)
				return false;
		} else if (!issuerHiosId.equals(other.issuerHiosId))
			return false;
		if (issuerPolicyId == null) {
			if (other.issuerPolicyId != null)
				return false;
		} else if (!issuerPolicyId.equals(other.issuerPolicyId))
			return false;
		if (issuerSubscriberID == null) {
			if (other.issuerSubscriberID != null)
				return false;
		} else if (!issuerSubscriberID.equals(other.issuerSubscriberID))
			return false;
		if (planID == null) {
			if (other.planID != null)
				return false;
		} else if (!planID.equals(other.planID))
			return false;
		if (policyEndDate == null) {
			if (other.policyEndDate != null)
				return false;
		} else if (!policyEndDate.equals(other.policyEndDate))
			return false;
		if (policyStartDate == null) {
			if (other.policyStartDate != null)
				return false;
		} else if (!policyStartDate.equals(other.policyStartDate))
			return false;
		if (subscriberStateCd == null) {
			if (other.subscriberStateCd != null)
				return false;
		} else if (!subscriberStateCd.equals(other.subscriberStateCd))
			return false;
		if (x12InsrncLineTypeCd == null) {
			if (other.x12InsrncLineTypeCd != null)
				return false;
		} else if (!x12InsrncLineTypeCd.equals(other.x12InsrncLineTypeCd))
			return false;
		return true;
	}
		
}