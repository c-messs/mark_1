package gov.hhs.cms.ff.fm.eps.ep.po;

public class SbmTransMsgValidationPO extends GenericSbmTransMsgPO<SbmTransMsgValidationPO> {
	
	private String sbmErrorWarningTypeCd;
	private String elementInErrorNm;
	private String exchangeAssignedMemberId;
	
	/**
	 * @return the sbmErrorWarningTypeCd
	 */
	public String getSbmErrorWarningTypeCd() {
		return sbmErrorWarningTypeCd;
	}
	/**
	 * @param sbmErrorWarningTypeCd the sbmErrorWarningTypeCd to set
	 */
	public void setSbmErrorWarningTypeCd(String sbmErrorWarningTypeCd) {
		this.sbmErrorWarningTypeCd = sbmErrorWarningTypeCd;
	}
	/**
	 * @return the elementInErrorNm
	 */
	public String getElementInErrorNm() {
		return elementInErrorNm;
	}
	/**
	 * @param elementInErrorNm the elementInErrorNm to set
	 */
	public void setElementInErrorNm(String elementInErrorNm) {
		this.elementInErrorNm = elementInErrorNm;
	}
	/**
	 * @return the exchangeAssignedMemberId
	 */
	public String getExchangeAssignedMemberId() {
		return exchangeAssignedMemberId;
	}
	/**
	 * @param exchangeAssignedMemberId the exchangeAssignedMemberId to set
	 */
	public void setExchangeAssignedMemberId(String exchangeAssignedMemberId) {
		this.exchangeAssignedMemberId = exchangeAssignedMemberId;
	}
	
	

}
