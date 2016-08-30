package gov.hhs.cms.ff.fm.eps.ep.po;

public class SbmFileErrorPO extends GenericSbmFilePO<SbmFileErrorPO> {
	
	private String sbmErrorWarningTypeCd;
	private String elementInErrorNm;
	
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
	
}
