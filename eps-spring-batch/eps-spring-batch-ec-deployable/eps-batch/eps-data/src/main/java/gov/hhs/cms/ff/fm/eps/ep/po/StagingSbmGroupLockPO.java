package gov.hhs.cms.ff.fm.eps.ep.po;

/**
 * @author j.radziewski
 * 
 * Entity class for attributes of STAGINGSBMGROUPLOCK table.
 * 
 *
 */
public class StagingSbmGroupLockPO {

	private Long sbmFileProcSumId;
	private Long processingGroupId;
	private Long batchId;
	/**
	 * @return the sbmFileProcSumId
	 */
	public Long getSbmFileProcSumId() {
		return sbmFileProcSumId;
	}
	/**
	 * @param sbmFileProcSumId the sbmFileProcSumId to set
	 */
	public void setSbmFileProcSumId(Long sbmFileProcSumId) {
		this.sbmFileProcSumId = sbmFileProcSumId;
	}
	/**
	 * @return the processingGroupId
	 */
	public Long getProcessingGroupId() {
		return processingGroupId;
	}
	/**
	 * @param processingGroupId the processingGroupId to set
	 */
	public void setProcessingGroupId(Long processingGroupId) {
		this.processingGroupId = processingGroupId;
	}
	/**
	 * @return the batchId
	 */
	public Long getBatchId() {
		return batchId;
	}
	/**
	 * @param batchId the batchId to set
	 */
	public void setBatchId(Long batchId) {
		this.batchId = batchId;
	}
	
	

}
