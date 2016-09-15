package gov.hhs.cms.ff.fm.eps.ep.po;

/**
 * @author j.radziewski
 * 
 * Entity class for table SBMFILEERRORADDITIONALINFO.
 *
 */
public class SbmFileErrorAdditionalInfoPO extends GenericSbmFilePO<SbmFileErrorAdditionalInfoPO> {
	
	private String additionalErrorInfoText;

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
