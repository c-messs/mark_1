package gov.hhs.cms.ff.fm.eps.ep.po;

/**
 * @author j.radziewski
 * 
 * Generic class for entities with same attributes to extend.
 * 
 * @param <T>
 */
public class GenericSbmFilePO<T> {
	
	private Long sbmFileInfoId;
	private Long sbmFileErrorSeqNum;

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
	 * @return the sbmFileErrorSeqNum
	 */
	public Long getSbmFileErrorSeqNum() {
		return sbmFileErrorSeqNum;
	}
	/**
	 * @param sbmFileErrorSeqNum the sbmFileErrorSeqNum to set
	 */
	public void setSbmFileErrorSeqNum(Long sbmFileErrorSeqNum) {
		this.sbmFileErrorSeqNum = sbmFileErrorSeqNum;
	}	

}
