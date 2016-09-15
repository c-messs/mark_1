package gov.hhs.cms.ff.fm.eps.ep;

import java.math.BigDecimal;

/**
 * @author rajesh.talanki
 *
 */
public class StateProrationConfiguration {

	private String stateCd;
	private int marketYear;
	private String prorationTypeCd;
	private BigDecimal errorThresholdPercent;
	private boolean cmsApprovalRequiredInd;
	private boolean sbmInd;
	
	/**
	 * @return the stateCd
	 */
	public String getStateCd() {
		return stateCd;
	}
	/**
	 * @param stateCd the stateCd to set
	 */
	public void setStateCd(String stateCd) {
		this.stateCd = stateCd;
	}
	/**
	 * @return the marketYear
	 */
	public int getMarketYear() {
		return marketYear;
	}
	/**
	 * @param marketYear the marketYear to set
	 */
	public void setMarketYear(int marketYear) {
		this.marketYear = marketYear;
	}
	/**
	 * @return the prorationTypeCd
	 */
	public String getProrationTypeCd() {
		return prorationTypeCd;
	}
	/**
	 * @param prorationTypeCd the prorationTypeCd to set
	 */
	public void setProrationTypeCd(String prorationTypeCd) {
		this.prorationTypeCd = prorationTypeCd;
	}
	/**
	 * @return the errorThresholdPercent
	 */
	public BigDecimal getErrorThresholdPercent() {
		return errorThresholdPercent;
	}
	/**
	 * @param errorThresholdPercent the errorThresholdPercent to set
	 */
	public void setErrorThresholdPercent(BigDecimal errorThresholdPercent) {
		this.errorThresholdPercent = errorThresholdPercent;
	}
	/**
	 * @return the cmsApprovalRequiredInd
	 */
	public boolean isCmsApprovalRequiredInd() {
		return cmsApprovalRequiredInd;
	}
	/**
	 * @param cmsApprovalRequiredInd the cmsApprovalRequiredInd to set
	 */
	public void setCmsApprovalRequiredInd(boolean cmsApprovalRequiredInd) {
		this.cmsApprovalRequiredInd = cmsApprovalRequiredInd;
	}
	/**
	 * @return the sbmInd
	 */
	public boolean isSbmInd() {
		return sbmInd;
	}
	/**
	 * @param sbmInd the sbmInd to set
	 */
	public void setSbmInd(boolean sbmInd) {
		this.sbmInd = sbmInd;
	}
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("StateProrationConfiguration [stateCd=").append(stateCd).append(", marketYear=")
				.append(marketYear).append(", prorationTypeCd=").append(prorationTypeCd)
				.append(", errorThresholdPercent=").append(errorThresholdPercent).append(", cmsApprovalRequiredInd=")
				.append(cmsApprovalRequiredInd).append(", sbmInd=").append(sbmInd).append("]");
		return builder.toString();
	}
	
	
}
