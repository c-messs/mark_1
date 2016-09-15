package gov.hhs.cms.ff.fm.eps.ep.sbm;

import java.util.ArrayList;
import java.util.List;

/**
 * @author rajesh.talanki
 *
 */
public class SBMErrorDTO {
	
	private Long sbmFileInfoId;
	private Long sbmTransMsgId;
	private String exchangeAssignedMemberId;
	private String sbmErrorWarningTypeCd;
	private String additionalErrorInfoText;
	private String elementInErrorNm;
	private List<String> additionalErrorInfoList = new ArrayList<>();
	
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
	 * @return the additionalErrorInfoList
	 */
	public List<String> getAdditionalErrorInfoList() {
		if (additionalErrorInfoList == null) {
			additionalErrorInfoList = new ArrayList<String>();
		}
		return additionalErrorInfoList;
	}
	
	/**
	 * @return the sbmTransMsgId
	 */
	public Long getSbmTransMsgId() {
		return sbmTransMsgId;
	}
	/**
	 * @param sbmTransMsgId the sbmTransMsgId to set
	 */
	public void setSbmTransMsgId(Long sbmTransMsgId) {
		this.sbmTransMsgId = sbmTransMsgId;
	}
	/**
	 * @return the exchangeAssignedMemerId
	 */
	public String getExchangeAssignedMemberId() {
		return exchangeAssignedMemberId;
	}
	/**
	 * @param exchangeAssignedMemerId the exchangeAssignedMemerId to set
	 */
	public void setExchangeAssignedMemberId(String exchangeAssignedMemerId) {
		this.exchangeAssignedMemberId = exchangeAssignedMemerId;
	}
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("SBMFileError [sbmFileInfoId=").append(sbmFileInfoId).append(", sbmTransMsgId=")
				.append(sbmTransMsgId).append(", exchangeAssignedMemerId=").append(exchangeAssignedMemberId)
				.append(", sbmErrorWarningTypeCd=").append(sbmErrorWarningTypeCd).append(", additionalErrorInfoText=")
				.append(additionalErrorInfoText).append(", elementInErrorNm=").append(elementInErrorNm)
				.append(", additionalErrorInfoList=").append(additionalErrorInfoList).append("]");
		return builder.toString();
	}
	
		
	
}
