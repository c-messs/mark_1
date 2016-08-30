/**
 * 
 */
package gov.hhs.cms.ff.fm.eps.ep.sbm;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * @author j.radziewski
 *
 */
public class SBMPremium {
	
	private LocalDate effectiveStartDate;
	private LocalDate effectiveEndDate;
	private BigDecimal totalPremium;
	private BigDecimal otherPayment1;
	private BigDecimal otherPayment2;
	private String ratingArea;
	private BigDecimal individualResponsibleAmt;
	private BigDecimal csr;
	private BigDecimal aptc;
	private BigDecimal proratedPremium;
	private BigDecimal proratedAptc;
	private BigDecimal proratedCsr;
	private String csrVariantId;
	
	/**
	 * @return the effectiveStartDate
	 */
	public LocalDate getEffectiveStartDate() {
		return effectiveStartDate;
	}
	/**
	 * @param effectiveStartDate the effectiveStartDate to set
	 */
	public void setEffectiveStartDate(LocalDate effectiveStartDate) {
		this.effectiveStartDate = effectiveStartDate;
	}
	/**
	 * @return the effectiveEndDate
	 */
	public LocalDate getEffectiveEndDate() {
		return effectiveEndDate;
	}
	/**
	 * @param effectiveEndDate the effectiveEndDate to set
	 */
	public void setEffectiveEndDate(LocalDate effectiveEndDate) {
		this.effectiveEndDate = effectiveEndDate;
	}
	/**
	 * @return the totalPremium
	 */
	public BigDecimal getTotalPremium() {
		return totalPremium;
	}
	/**
	 * @param totalPremium the totalPremium to set
	 */
	public void setTotalPremium(BigDecimal totalPremium) {
		this.totalPremium = totalPremium;
	}
	/**
	 * @return the otherPayment1
	 */
	public BigDecimal getOtherPayment1() {
		return otherPayment1;
	}
	/**
	 * @param otherPayment1 the otherPayment1 to set
	 */
	public void setOtherPayment1(BigDecimal otherPayment1) {
		this.otherPayment1 = otherPayment1;
	}
	/**
	 * @return the otherPayment2
	 */
	public BigDecimal getOtherPayment2() {
		return otherPayment2;
	}
	/**
	 * @param otherPayment2 the otherPayment2 to set
	 */
	public void setOtherPayment2(BigDecimal otherPayment2) {
		this.otherPayment2 = otherPayment2;
	}
	/**
	 * @return the ratingArea
	 */
	public String getRatingArea() {
		return ratingArea;
	}
	/**
	 * @param ratingArea the ratingArea to set
	 */
	public void setRatingArea(String ratingArea) {
		this.ratingArea = ratingArea;
	}
	/**
	 * @return the individualResponsibleAmt
	 */
	public BigDecimal getIndividualResponsibleAmt() {
		return individualResponsibleAmt;
	}
	/**
	 * @param individualResponsibleAmt the individualResponsibleAmt to set
	 */
	public void setIndividualResponsibleAmt(BigDecimal individualResponsibleAmt) {
		this.individualResponsibleAmt = individualResponsibleAmt;
	}
	/**
	 * @return the csr
	 */
	public BigDecimal getCsr() {
		return csr;
	}
	/**
	 * @param csr the csr to set
	 */
	public void setCsr(BigDecimal csr) {
		this.csr = csr;
	}
	/**
	 * @return the aptc
	 */
	public BigDecimal getAptc() {
		return aptc;
	}
	/**
	 * @param aptc the aptc to set
	 */
	public void setAptc(BigDecimal aptc) {
		this.aptc = aptc;
	}
	/**
	 * @return the proratedPremium
	 */
	public BigDecimal getProratedPremium() {
		return proratedPremium;
	}
	/**
	 * @param proratedPremium the proratedPremium to set
	 */
	public void setProratedPremium(BigDecimal proratedPremium) {
		this.proratedPremium = proratedPremium;
	}
	/**
	 * @return the proratedAptc
	 */
	public BigDecimal getProratedAptc() {
		return proratedAptc;
	}
	/**
	 * @param proratedAptc the proratedAptc to set
	 */
	public void setProratedAptc(BigDecimal proratedAptc) {
		this.proratedAptc = proratedAptc;
	}
	/**
	 * @return the proratedCsr
	 */
	public BigDecimal getProratedCsr() {
		return proratedCsr;
	}
	/**
	 * @param proratedCsr the proratedCsr to set
	 */
	public void setProratedCsr(BigDecimal proratedCsr) {
		this.proratedCsr = proratedCsr;
	}
	/**
	 * @return the csrVariantId
	 */
	public String getCsrVariantId() {
		return csrVariantId;
	}
	/**
	 * @param csrVariantId the csrVariantId to set
	 */
	public void setCsrVariantId(String csrVariantId) {
		this.csrVariantId = csrVariantId;
	}

}
