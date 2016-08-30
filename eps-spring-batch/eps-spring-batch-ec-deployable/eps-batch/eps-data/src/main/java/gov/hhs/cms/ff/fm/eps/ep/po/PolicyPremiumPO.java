package gov.hhs.cms.ff.fm.eps.ep.po;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * @author eps
 *
 */
public class PolicyPremiumPO extends GenericPolicyPO<PolicyPremiumPO>  {

	//Attributes included in Hashcode and Equals (only SBM uses compare).
	// Note: equals methods uses "compareTo" for BigDecimals.
	private LocalDate effectiveStartDate;
	private LocalDate effectiveEndDate;
	private BigDecimal totalPremiumAmount;
	private BigDecimal individualResponsibleAmount;
	private String exchangeRateArea;
	private BigDecimal aptcAmount;
	private BigDecimal csrAmount;
	private BigDecimal proratedPremiumAmount;
	private BigDecimal proratedAptcAmount;
	private BigDecimal proratedCsrAmount;
	private String insrncPlanVariantCmptTypeCd;

	// Excluded from hashCode and equals (FFM only).
	private BigDecimal proratedInddResponsibleAmount;

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
	public void setIndividualResponsibleAmount(BigDecimal individualResponsibleAmount) {
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

	/**
	 * @return the proratedInddResponsibleAmount
	 */
	public BigDecimal getProratedInddResponsibleAmount() {
		return proratedInddResponsibleAmount;
	}

	/**
	 * @param proratedInddResponsibleAmount the proratedInddResponsibleAmount to set
	 */
	public void setProratedInddResponsibleAmount(BigDecimal proratedInddResponsibleAmount) {
		this.proratedInddResponsibleAmount = proratedInddResponsibleAmount;
	}
	

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((aptcAmount == null) ? 0 : aptcAmount.hashCode());
		result = prime * result + ((csrAmount == null) ? 0 : csrAmount.hashCode());
		result = prime * result + ((effectiveEndDate == null) ? 0 : effectiveEndDate.hashCode());
		result = prime * result + ((effectiveStartDate == null) ? 0 : effectiveStartDate.hashCode());
		result = prime * result + ((exchangeRateArea == null) ? 0 : exchangeRateArea.hashCode());
		result = prime * result + ((individualResponsibleAmount == null) ? 0 : individualResponsibleAmount.hashCode());
		result = prime * result + ((insrncPlanVariantCmptTypeCd == null) ? 0 : insrncPlanVariantCmptTypeCd.hashCode());
		result = prime * result + ((proratedAptcAmount == null) ? 0 : proratedAptcAmount.hashCode());
		result = prime * result + ((proratedCsrAmount == null) ? 0 : proratedCsrAmount.hashCode());
		result = prime * result + ((proratedPremiumAmount == null) ? 0 : proratedPremiumAmount.hashCode());
		result = prime * result + ((totalPremiumAmount == null) ? 0 : totalPremiumAmount.hashCode());
		return result;
	}

	
	/* This methods uses "compareTo" for BigDecimals.
	 * (non-Javadoc)
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
		PolicyPremiumPO other = (PolicyPremiumPO) obj;
		
		if (aptcAmount == null) {
			if (other.aptcAmount != null)
				return false;
		} else if (other.aptcAmount != null) {
			if (aptcAmount.compareTo(other.aptcAmount) != 0) {
				return false;
			} 
		} else {
			return false;
		}
	
		if (csrAmount == null) {
			if (other.csrAmount != null)
				return false;
		} else if (other.csrAmount != null) {
			if (csrAmount.compareTo(other.csrAmount) != 0) {
				return false;
			} 
		} else {
			return false;
		}
		
		if (effectiveEndDate == null) {
			if (other.effectiveEndDate != null)
				return false;
		} else if (!effectiveEndDate.equals(other.effectiveEndDate))
			return false;
		if (effectiveStartDate == null) {
			if (other.effectiveStartDate != null)
				return false;
		} else if (!effectiveStartDate.equals(other.effectiveStartDate))
			return false;
		if (exchangeRateArea == null) {
			if (other.exchangeRateArea != null)
				return false;
		} else if (!exchangeRateArea.equals(other.exchangeRateArea))
			return false;
		
		if (individualResponsibleAmount == null) {
			if (other.individualResponsibleAmount != null)
				return false;
		} else if (other.individualResponsibleAmount != null) {
			if (individualResponsibleAmount.compareTo(other.individualResponsibleAmount) != 0) {
				return false;
			} 
		} else {
			return false;
		}
		
		if (insrncPlanVariantCmptTypeCd == null) {
			if (other.insrncPlanVariantCmptTypeCd != null)
				return false;
		} else if (!insrncPlanVariantCmptTypeCd.equals(other.insrncPlanVariantCmptTypeCd))
			return false;
		
		if (proratedAptcAmount == null) {
			if (other.proratedAptcAmount != null)
				return false;
		} else if (other.proratedAptcAmount != null) {
			if (proratedAptcAmount.compareTo(other.proratedAptcAmount) != 0) {
				return false;
			} 
		} else {
			return false;
		}
		
		if (proratedCsrAmount == null) {
			if (other.proratedCsrAmount != null)
				return false;
		} else if (other.proratedCsrAmount != null) {
			if (proratedCsrAmount.compareTo(other.proratedCsrAmount) != 0) {
				return false;
			}
		} else {
			return false;
		}
		
		if (proratedPremiumAmount == null) {
			if (other.proratedPremiumAmount != null)
				return false;
		} else if (other.proratedPremiumAmount != null) {
			if (proratedPremiumAmount.compareTo(other.proratedPremiumAmount) != 0) {
				return false;
			}
		} else {
			return false;
		}
		
		if (totalPremiumAmount == null) {
			if (other.totalPremiumAmount != null)
				return false;
		} else if (other.totalPremiumAmount != null) {
			if (totalPremiumAmount.compareTo(other.totalPremiumAmount) != 0) {
				return false;
			}
		} else {
			return false;
		}
	
		return true;
	}

}