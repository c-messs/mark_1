package gov.hhs.cms.ff.fm.eps.ep.po;

public class SbmFileArchivePO extends SbmFileInfoPO {

	private String tenantNum;
	private Integer coverageYear;
	private String issuerId;
	private String subscriberStateCd;
	
	/**
	 * @return the tenantNum
	 */
	public String getTenantNum() {
		return tenantNum;
	}
	/**
	 * @param tenantNum the tenantNum to set
	 */
	public void setTenantNum(String tenantNum) {
		this.tenantNum = tenantNum;
	}
	/**
	 * @return the coverageYear
	 */
	public Integer getCoverageYear() {
		return coverageYear;
	}
	/**
	 * @param coverageYear the coverageYear to set
	 */
	public void setCoverageYear(Integer coverageYear) {
		this.coverageYear = coverageYear;
	}
	/**
	 * @return the issuerId
	 */
	public String getIssuerId() {
		return issuerId;
	}
	/**
	 * @param issuerId the issuerId to set
	 */
	public void setIssuerId(String issuerId) {
		this.issuerId = issuerId;
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

}
