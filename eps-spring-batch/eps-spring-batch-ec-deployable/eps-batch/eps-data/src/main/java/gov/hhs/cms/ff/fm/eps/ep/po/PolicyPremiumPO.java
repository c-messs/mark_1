package gov.hhs.cms.ff.fm.eps.ep.po;

import java.math.BigDecimal;

import org.joda.time.DateTime;

/**
 * @author eps
 *
 */
public class PolicyPremiumPO extends GenericPolicyPO<PolicyPremiumPO>  {
		
	private DateTime effectiveStartDate;
	private DateTime effectiveEndDate;
	private BigDecimal totalPremiumAmount;
	private BigDecimal individualResponsibleAmount;
	private String exchangeRateArea;
	private BigDecimal aptcAmount;
	private BigDecimal csrAmount;
	private BigDecimal proratedPremiumAmount;
	private BigDecimal proratedInddResponsibleAmount;
	private BigDecimal proratedAptcAmount;
	private BigDecimal proratedCsrAmount;
	private String insrncPlanVariantCmptTypeCd;
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
	 * @return the individualResponsibleAmount
	 */
	public BigDecimal getIndividualResponsibleAmount() {
		return individualResponsibleAmount;
	}
	/**
	 * @param individualResponsibleAmount the individualResponsibleAmount to set
	 */
	public void setIndividualResponsibleAmount(
			BigDecimal individualResponsibleAmount) {
		this.individualResponsibleAmount = individualResponsibleAmount;
	}
	/**
	 * @return the exchangeRateArea
	 */
	public String getExchangeRateArea() {
		return exchangeRateArea;
	}
	/**
	 * @param exchangeRateArea the exchangeRateArea to set
	 */
	public void setExchangeRateArea(String exchangeRateArea) {
		this.exchangeRateArea = exchangeRateArea;
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
	/**
	 * @return the proratedInddResponsibleAmount
	 */
	public BigDecimal getProratedInddResponsibleAmount() {
		return proratedInddResponsibleAmount;
	}
	/**
	 * @param proratedInddResponsibleAmount the proratedInddResponsibleAmount to set
	 */
	public void setProratedInddResponsibleAmount(
			BigDecimal proratedInddResponsibleAmount) {
		this.proratedInddResponsibleAmount = proratedInddResponsibleAmount;
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
	 * @return the insrncPlanVariantCmptTypeCd
	 */
	public String getInsrncPlanVariantCmptTypeCd() {
		return insrncPlanVariantCmptTypeCd;
	}
	/**
	 * @param insrncPlanVariantCmptTypeCd the insrncPlanVariantCmptTypeCd to set
	 */
	public void setInsrncPlanVariantCmptTypeCd(String insrncPlanVariantCmptTypeCd) {
		this.insrncPlanVariantCmptTypeCd = insrncPlanVariantCmptTypeCd;
	}
	
	
	
}