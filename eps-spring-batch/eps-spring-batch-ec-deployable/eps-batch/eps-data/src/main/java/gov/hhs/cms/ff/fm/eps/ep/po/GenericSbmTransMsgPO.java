package gov.hhs.cms.ff.fm.eps.ep.po;

/**
 * @author j.radziewski
 * 
 * Generic class for entities with same attributes to extend.
 *
 * @param <T>
 */
public class GenericSbmTransMsgPO<T> {
	
	private Long sbmTransMsgId;
	private Long validationSeqNum;
	
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
	 * @return the validationSeqNum
	 */
	public Long getValidationSeqNum() {
		return validationSeqNum;
	}
	/**
	 * @param validationSeqNum the validationSeqNum to set
	 */
	public void setValidationSeqNum(Long validationSeqNum) {
		this.validationSeqNum = validationSeqNum;
	}



}
