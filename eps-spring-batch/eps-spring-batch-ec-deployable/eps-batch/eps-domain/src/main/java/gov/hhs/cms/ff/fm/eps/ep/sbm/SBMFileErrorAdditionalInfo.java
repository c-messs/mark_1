package gov.hhs.cms.ff.fm.eps.ep.sbm;

/**
 * @author rajesh.talanki
 *
 */
public class SBMFileErrorAdditionalInfo {

	private Long sbmFileInfoId;
	private Long sbmFileErrorSequenceNum;
	private String additionalErrorInfoText;
	/**
	 * @return the sbmFileInfoId
	 */
	public Long getSbmFileInfoId() {
		return sbmFileInfoId;
	}
	/**
	 * @param sbmFileInfoId the sbmFileInfoId to set
	 */
	public void setSbmFileInfoId(Long sbmFileInfoId) {
		this.sbmFileInfoId = sbmFileInfoId;
	}
	/**
	 * @return the sbmFileErrorSequenceNum
	 */
	public Long getSbmFileErrorSequenceNum() {
		return sbmFileErrorSequenceNum;
	}
	/**
	 * @param sbmFileErrorSequenceNum the sbmFileErrorSequenceNum to set
	 */
	public void setSbmFileErrorSequenceNum(Long sbmFileErrorSequenceNum) {
		this.sbmFileErrorSequenceNum = sbmFileErrorSequenceNum;
	}
	/**
	 * @return the additionalErrorInfoText
	 */
	public String getAdditionalErrorInfoText() {
		return additionalErrorInfoText;
	}
	/**
	 * @param additionalErrorInfoText the additionalErrorInfoText to set
	 */
	public void setAdditionalErrorInfoText(String additionalErrorInfoText) {
		this.additionalErrorInfoText = additionalErrorInfoText;
	}

	
}
