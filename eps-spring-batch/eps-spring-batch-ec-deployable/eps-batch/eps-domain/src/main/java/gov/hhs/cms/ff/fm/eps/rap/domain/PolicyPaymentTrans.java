/**
 * 
 */
package gov.hhs.cms.ff.fm.eps.rap.domain;

import java.io.Serializable;
import java.math.BigDecimal;

import org.joda.time.DateTime;

/**
 * @author prasad.ghanta; modified by rajesh.talanki, girish.padmanabhan
 *
 */
public class  PolicyPaymentTrans implements Serializable { 
     
   private static final long serialVersionUID = 1L;
   
	private Long policyPaymentTransId;
	private Long policyVersionId;
	private String marketplaceGroupPolicyId;
	private String financialProgramTypeCd;
	private String transPeriodTypeCd;
	private DateTime coverageDate;
	private Long parentPolicyPaymentTransId;
	private String issuerHiosId;
	private BigDecimal paymentAmount;
	private BigDecimal totalPremiumAmount;
	private String issuerStateCd;
	private String subscriberStateCd;
	private String insrncAplctnTypeCd; 
	private String exchangePolicyId;
	private Long issuerLevelTransactionId;
	private DateTime maintenanceStartDateTime;
	private DateTime createDateTime;
	private DateTime lastModifiedDateTime;
	private String createBy;	
	private String lastModifiedBy;
	private DateTime paymentCoverageStartDate;
	private DateTime paymentCoverageEndDate;
	private String lastPaymentProcStatusTypeCd;
	private Integer prorationDaysOfCoverageNum;
	
	/**
	 * @return the policyPaymentTransId
	 */
	public Long getPolicyPaymentTransId() {
		return policyPaymentTransId;
	}

	/**
	 * @param policyPaymentTransId the policyPaymentTransId to set
	 */
	public void setPolicyPaymentTransId(Long policyPaymentTransId) {
		this.policyPaymentTransId = policyPaymentTransId;
	}

	/**
	 * @return the policyVersionId
	 */
	public Long getPolicyVersionId() {
		return policyVersionId;
	}

	/**
	 * @param policyVersionId the policyVersionId to set
	 */
	public void setPolicyVersionId(Long policyVersionId) {
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
	 * @return the financialProgramTypeCd
	 */
	public String getFinancialProgramTypeCd() {
		return financialProgramTypeCd;
	}

	/**
	 * @param financialProgramTypeCd the financialProgramTypeCd to set
	 */
	public void setFinancialProgramTypeCd(String financialProgramTypeCd) {
		this.financialProgramTypeCd = financialProgramTypeCd;
	}

	/**
	 * @return the transPeriodTypeCd
	 */
	public String getTransPeriodTypeCd() {
		return transPeriodTypeCd;
	}

	/**
	 * @param transPeriodTypeCd the transPeriodTypeCd to set
	 */
	public void setTransPeriodTypeCd(String transPeriodTypeCd) {
		this.transPeriodTypeCd = transPeriodTypeCd;
	}

	/**
	 * @return the coverageDate
	 */
	public DateTime getCoverageDate() {
		return coverageDate;
	}

	/**
	 * @param coverageDate the coverageDate to set
	 */
	public void setCoverageDate(DateTime coverageDate) {
		this.coverageDate = coverageDate;
	}

	/**
	 * @return the parentPolicyPaymentTransId
	 */
	public Long getParentPolicyPaymentTransId() {
		return parentPolicyPaymentTransId;
	}

	/**
	 * @param parentPolicyPaymentTransId the parentPolicyPaymentTransId to set
	 */
	public void setParentPolicyPaymentTransId(Long parentPolicyPaymentTransId) {
		this.parentPolicyPaymentTransId = parentPolicyPaymentTransId;
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
	 * @return the paymentAmount
	 */
	public BigDecimal getPaymentAmount() {
		return paymentAmount;
	}

	/**
	 * @param paymentAmount the paymentAmount to set
	 */
	public void setPaymentAmount(BigDecimal paymentAmount) {
		this.paymentAmount = paymentAmount;
	}

	/**
	 * @return the totalPremiumAmount
	 */
	public BigDecimal getTotalPremiumAmount() {
		return totalPremiumAmount;
	}

	/**
	 * @param totalPremiumAmount the totalPremiumAmount to set
	 */
	public void setTotalPremiumAmount(BigDecimal totalPremiumAmount) {
		this.totalPremiumAmount = totalPremiumAmount;
	}

	/**
	 * @return the issuerStateCd
	 */
	public String getIssuerStateCd() {
		return issuerStateCd;
	}

	/**
	 * @param issuerStateCd the issuerStateCd to set
	 */
	public void setIssuerStateCd(String issuerStateCd) {
		this.issuerStateCd = issuerStateCd;
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

	public String getExchangePolicyId() {
		return exchangePolicyId;
	}

	public void setExchangePolicyId(String exchangePolicyId) {
		this.exchangePolicyId = exchangePolicyId;
	}

	/**
	 * @return the issuerLevelTransactionId
	 */
	public Long getIssuerLevelTransactionId() {
		return issuerLevelTransactionId;
	}

	/**
	 * @param issuerLevelTransactionId the issuerLevelTransactionId to set
	 */
	public void setIssuerLevelTransactionId(Long issuerLevelTransactionId) {
		this.issuerLevelTransactionId = issuerLevelTransactionId;
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
	 * @return the createDateTime
	 */
	public DateTime getCreateDateTime() {
		return createDateTime;
	}

	/**
	 * @param createDateTime the createDateTime to set
	 */
	public void setCreateDateTime(DateTime createDateTime) {
		this.createDateTime = createDateTime;
	}

	/**
	 * @return the lastModifiedDateTime
	 */
	public DateTime getLastModifiedDateTime() {
		return lastModifiedDateTime;
	}

	/**
	 * @param lastModifiedDateTime the lastModifiedDateTime to set
	 */
	public void setLastModifiedDateTime(DateTime lastModifiedDateTime) {
		this.lastModifiedDateTime = lastModifiedDateTime;
	}

	/**
	 * @return the createBy
	 */
	public String getCreateBy() {
		return createBy;
	}

	/**
	 * @param createBy the createBy to set
	 */
	public void setCreateBy(String createBy) {
		this.createBy = createBy;
	}

	/**
	 * @return the lastModifiedBy
	 */
	public String getLastModifiedBy() {
		return lastModifiedBy;
	}

	/**
	 * @param lastModifiedBy the lastModifiedBy to set
	 */
	public void setLastModifiedBy(String lastModifiedBy) {
		this.lastModifiedBy = lastModifiedBy;
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
	 * @return the paymentCoverageStartDate
	 */
	public DateTime getPaymentCoverageStartDate() {
		return paymentCoverageStartDate;
	}

	/**
	 * @param paymentCoverageStartDate the paymentCoverageStartDate to set
	 */
	public void setPaymentCoverageStartDate(DateTime paymentCoverageStartDate) {
		this.paymentCoverageStartDate = paymentCoverageStartDate;
	}

	/**
	 * @return the paymentCoverageEndDate
	 */
	public DateTime getPaymentCoverageEndDate() {
		return paymentCoverageEndDate;
	}

	/**
	 * @param paymentCoverageEndDate the paymentCoverageEndDate to set
	 */
	public void setPaymentCoverageEndDate(DateTime paymentCoverageEndDate) {
		this.paymentCoverageEndDate = paymentCoverageEndDate;
	}

	/**
	 * @return the lastPaymentProcStatusTypeCd
	 */
	public String getLastPaymentProcStatusTypeCd() {
		return lastPaymentProcStatusTypeCd;
	}

	/**
	 * @param lastPaymentProcStatusTypeCd the lastPaymentProcStatusTypeCd to set
	 */
	public void setLastPaymentProcStatusTypeCd(String lastPaymentProcStatusTypeCd) {
		this.lastPaymentProcStatusTypeCd = lastPaymentProcStatusTypeCd;
	}

	/**
	 * @return the prorationDaysOfCoverageNum
	 */
	public Integer getProrationDaysOfCoverageNum() {
		return prorationDaysOfCoverageNum;
	}

	/**
	 * @param prorationDaysOfCoverageNum the prorationDaysOfCoverageNum to set
	 */
	public void setProrationDaysOfCoverageNum(Integer prorationDaysOfCoverageNum) {
		this.prorationDaysOfCoverageNum = prorationDaysOfCoverageNum;
	}

	@Override
	public String toString() {
		return "PolicyPaymentTrans [policyPaymentTransId="
				+ policyPaymentTransId + ", policyVersionId=" + policyVersionId
				+ ", financialProgramTypeCd=" + financialProgramTypeCd
				+ ", transPeriodTypeCd=" + transPeriodTypeCd
				+ ", coverageDate=" + coverageDate
				+ ", PaymentCoverageStartDate=" + paymentCoverageStartDate
				+ ", PaymentCoverageEndDate=" + paymentCoverageEndDate
				+ ", lastPaymentProcStatusTypeCd=" + lastPaymentProcStatusTypeCd
				+ ", parentPolicyPaymentTransId=" + parentPolicyPaymentTransId
				+ ", issuerHiosId=" + issuerHiosId + ", paymentAmount="
				+ paymentAmount + ", totalPremiumAmount=" + totalPremiumAmount
				+ ", issuerStateCd=" + issuerStateCd + ", subscriberStateCd="
				+ subscriberStateCd + ", insrncAplctnTypeCd="
				+ insrncAplctnTypeCd + ", exchangePolicyId=" + exchangePolicyId
				+ ", issuerLevelTransactionId=" + issuerLevelTransactionId
				+ ", maintenanceStartDateTime=" + maintenanceStartDateTime
				+ ", createDateTime=" + createDateTime
				+ ", lastModifiedDateTime=" + lastModifiedDateTime
				+ ", prorationDaysOfCoverageNum=" + prorationDaysOfCoverageNum
				+ ", createBy=" + createBy + ", lastModifiedBy="
				+ lastModifiedBy + "]";
	}
}       