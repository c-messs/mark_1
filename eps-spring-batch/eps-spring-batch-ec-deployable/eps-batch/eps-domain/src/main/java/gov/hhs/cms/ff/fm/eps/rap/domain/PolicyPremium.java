/**
 * 
 */
package gov.hhs.cms.ff.fm.eps.rap.domain;

import java.io.Serializable;
import java.math.BigDecimal;

import org.joda.time.DateTime;

/**
 * @author rajesh.talanki
 *
 */
public class PolicyPremium implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private long policyVersionId;
	private DateTime effectiveStartDate;
	private DateTime effectiveEndDate;
	private BigDecimal totalPremiumAmount;	
	private BigDecimal csrAmount;	
	private BigDecimal aptcAmount;
	private BigDecimal proratedPremiumAmount;	
	private BigDecimal proratedCsrAmount;	
	private BigDecimal proratedAptcAmount;	

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
	 * @return the effectiveStartDate
	 */
	public DateTime getEffectiveStartDate() {
		return effectiveStartDate;
	}

	/**
	 * @param effectiveStartDate the effectiveStartDate to set
	 */
	public void setEffectiveStartDate(DateTime effectiveStartDate) {
		this.effectiveStartDate = effectiveStartDate;
	}

	/**
	 * @return the effectiveEndDate
	 */
	public DateTime getEffectiveEndDate() {
		return effectiveEndDate;
	}

	/**
	 * @param effectiveEndDate the effectiveEndDate to set
	 */
	public void setEffectiveEndDate(DateTime effectiveEndDate) {
		this.effectiveEndDate = effectiveEndDate;
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
	 * @return the csrAmount
	 */
	public BigDecimal getCsrAmount() {
		return csrAmount;
	}

	/**
	 * @param csrAmount the csrAmount to set
	 */
	public void setCsrAmount(BigDecimal csrAmount) {
		this.csrAmount = csrAmount;
	}

	/**
	 * @return the aptcAmount
	 */
	public BigDecimal getAptcAmount() {
		return aptcAmount;
	}

	/**
	 * @param aptcAmount the aptcAmount to set
	 */
	public void setAptcAmount(BigDecimal aptcAmount) {
		this.aptcAmount = aptcAmount;
	}
	
	/**
	 * @return the proratedCsrAmount
	 */
	public BigDecimal getProratedCsrAmount() {
		return proratedCsrAmount;
	}

	/**
	 * @param proratedCsrAmount the proratedCsrAmount to set
	 */
	public void setProratedCsrAmount(BigDecimal proratedCsrAmount) {
		this.proratedCsrAmount = proratedCsrAmount;
	}

	/**
	 * @return the proratedAptcAmount
	 */
	public BigDecimal getProratedAptcAmount() {
		return proratedAptcAmount;
	}

	/**
	 * @param proratedAptcAmount the proratedAptcAmount to set
	 */
	public void setProratedAptcAmount(BigDecimal proratedAptcAmount) {
		this.proratedAptcAmount = proratedAptcAmount;
	}

	/**
	 * @return the proratedPremiumAmount
	 */
	public BigDecimal getProratedPremiumAmount() {
		return proratedPremiumAmount;
	}

	/**
	 * @param proratedPremiumAmount the proratedPremiumAmount to set
	 */
	public void setProratedPremiumAmount(BigDecimal proratedPremiumAmount) {
		this.proratedPremiumAmount = proratedPremiumAmount;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "PolicyPremium [policyVersionId=" + policyVersionId
				+ ", effectiveStartDate=" + effectiveStartDate
				+ ", effectiveEndDate=" + effectiveEndDate
				+ ", totalPremiumAmount=" + totalPremiumAmount 
				+ ", csrAmount="+ csrAmount 
				+ ", aptcAmount=" + aptcAmount 
				+ ", proratedPremiumAmount=" + proratedPremiumAmount 
				+ ", proratedCsrAmount=" + proratedCsrAmount 
				+ ", proratedAptcAmount=" + proratedAptcAmount 
				+ "]";
	}
}
