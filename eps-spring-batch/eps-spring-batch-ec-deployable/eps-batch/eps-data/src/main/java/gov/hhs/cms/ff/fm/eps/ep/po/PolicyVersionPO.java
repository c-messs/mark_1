package gov.hhs.cms.ff.fm.eps.ep.po;

import org.joda.time.DateTime;

/**
 * The persistent class for the EPSPOLICY database table.
 * 
 */

public class PolicyVersionPO extends GenericPolicyPO<PolicyVersionPO> {
	
	// needed to update old PolicyVersion maintenanceEndDateTime
	private Long previousPolicyVersionId;
	
	// Generic contains PolicyVersionId
	
	// data to be inserted into new PolicyVersion
	private String marketplaceGroupPolicyId;
	private String subscriberStateCd;
	private String exchangePolicyId;
	private DateTime maintenanceStartDateTime;
	private DateTime maintenanceEndDateTime;
	private String issuerPolicyId;
	private String issuerHiosId;
	private String issuerTaxPayerId;
	private String issuerNm;
	private String issuerSubscriberID;
	private String exchangeAssignedSubscriberID;
	private DateTime transDateTime;
	private String transControlNum;
	private DateTime eligibilityStartDate;
	private DateTime eligibilityEndDate;
	private DateTime premiumPaidToEndDate;
	private DateTime lastPremiumPaidDate;
	private String planID;
	private String employerGroupNum;
	private String x12InsrncLineTypeCd;
	private String insrncAplctnTypeCd;
	private String employerIdentificationNum;
	private Long transMsgID;
	private DateTime changeReportedDate;
	private String x12CoverageLevelTypeCd;
	private DateTime policyStartDate;
	private DateTime policyEndDate;
	private Long sourceVersionId;
	private DateTime sourceVersionDateTime;
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
	 * @return the maintenanceEndDateTime
	 */
	public DateTime getMaintenanceEndDateTime() {
		return maintenanceEndDateTime;
	}
	/**
	 * @param maintenanceEndDateTime the maintenanceEndDateTime to set
	 */
	public void setMaintenanceEndDateTime(DateTime maintenanceEndDateTime) {
		this.maintenanceEndDateTime = maintenanceEndDateTime;
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
	 * @return the transDateTime
	 */
	public DateTime getTransDateTime() {
		return transDateTime;
	}
	/**
	 * @param transDateTime the transDateTime to set
	 */
	public void setTransDateTime(DateTime transDateTime) {
		this.transDateTime = transDateTime;
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
	 * @return the eligibilityStartDate
	 */
	public DateTime getEligibilityStartDate() {
		return eligibilityStartDate;
	}
	/**
	 * @param eligibilityStartDate the eligibilityStartDate to set
	 */
	public void setEligibilityStartDate(DateTime eligibilityStartDate) {
		this.eligibilityStartDate = eligibilityStartDate;
	}
	/**
	 * @return the eligibilityEndDate
	 */
	public DateTime getEligibilityEndDate() {
		return eligibilityEndDate;
	}
	/**
	 * @param eligibilityEndDate the eligibilityEndDate to set
	 */
	public void setEligibilityEndDate(DateTime eligibilityEndDate) {
		this.eligibilityEndDate = eligibilityEndDate;
	}
	/**
	 * @return the premiumPaidToEndDate
	 */
	public DateTime getPremiumPaidToEndDate() {
		return premiumPaidToEndDate;
	}
	/**
	 * @param premiumPaidToEndDate the premiumPaidToEndDate to set
	 */
	public void setPremiumPaidToEndDate(DateTime premiumPaidToEndDate) {
		this.premiumPaidToEndDate = premiumPaidToEndDate;
	}
	/**
	 * @return the lastPremiumPaidDate
	 */
	public DateTime getLastPremiumPaidDate() {
		return lastPremiumPaidDate;
	}
	/**
	 * @param lastPremiumPaidDate the lastPremiumPaidDate to set
	 */
	public void setLastPremiumPaidDate(DateTime lastPremiumPaidDate) {
		this.lastPremiumPaidDate = lastPremiumPaidDate;
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
	 * @return the changeReportedDate
	 */
	public DateTime getChangeReportedDate() {
		return changeReportedDate;
	}
	/**
	 * @param changeReportedDate the changeReportedDate to set
	 */
	public void setChangeReportedDate(DateTime changeReportedDate) {
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
	public DateTime getSourceVersionDateTime() {
		return sourceVersionDateTime;
	}
	/**
	 * @param sourceVersionDateTime the sourceVersionDateTime to set
	 */
	public void setSourceVersionDateTime(DateTime sourceVersionDateTime) {
		this.sourceVersionDateTime = sourceVersionDateTime;
	}

}