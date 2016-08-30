/**
 * 
 */
package gov.hhs.cms.ff.fm.eps.ep;

import java.time.LocalDateTime;

/**
 * Class to hold BatchRunControl table attributes
 * 
 * @author girish.padmanabhan
 *
 */
public class BatchRunControl {
	
	private String batchRunControlId;
	private String extractFilePath;
	private LocalDateTime highWaterMarkStartDateTime;
	private LocalDateTime highWaterMarkEndDateTime;
	private Integer recordCountQuantity;
	private String preAuditExtractCompletionInd;
	private LocalDateTime batchStartDateTime;
	private LocalDateTime batchEndDateTime;
	private Long jobInstanceId;
	private String createdBy;
	private String lastModifiedBy;
	/**
	 * @return the batchRunControlId
	 */
	public String getBatchRunControlId() {
		return batchRunControlId;
	}
	/**
	 * @param batchRunControlId the batchRunControlId to set
	 */
	public void setBatchRunControlId(String batchRunControlId) {
		this.batchRunControlId = batchRunControlId;
	}
	/**
	 * @return the extractFilePath
	 */
	public String getExtractFilePath() {
		return extractFilePath;
	}
	/**
	 * @param extractFilePath the extractFilePath to set
	 */
	public void setExtractFilePath(String extractFilePath) {
		this.extractFilePath = extractFilePath;
	}
	/**
	 * @return the highWaterMarkStartDateTime
	 */
	public LocalDateTime getHighWaterMarkStartDateTime() {
		return highWaterMarkStartDateTime;
	}
	/**
	 * @param highWaterMarkStartDateTime the highWaterMarkStartDateTime to set
	 */
	public void setHighWaterMarkStartDateTime(
			LocalDateTime highWaterMarkStartDateTime) {
		this.highWaterMarkStartDateTime = highWaterMarkStartDateTime;
	}
	/**
	 * @return the highWaterMarkEndDateTime
	 */
	public LocalDateTime getHighWaterMarkEndDateTime() {
		return highWaterMarkEndDateTime;
	}
	/**
	 * @param highWaterMarkEndDateTime the highWaterMarkEndDateTime to set
	 */
	public void setHighWaterMarkEndDateTime(LocalDateTime highWaterMarkEndDateTime) {
		this.highWaterMarkEndDateTime = highWaterMarkEndDateTime;
	}
	/**
	 * @return the recordCountQuantity
	 */
	public Integer getRecordCountQuantity() {
		return recordCountQuantity;
	}
	/**
	 * @param recordCountQuantity the recordCountQuantity to set
	 */
	public void setRecordCountQuantity(Integer recordCountQuantity) {
		this.recordCountQuantity = recordCountQuantity;
	}
	/**
	 * @return the preAuditExtractCompletionInd
	 */
	public String getPreAuditExtractCompletionInd() {
		return preAuditExtractCompletionInd;
	}
	/**
	 * @param preAuditExtractCompletionInd the preAuditExtractCompletionInd to set
	 */
	public void setPreAuditExtractCompletionInd(String preAuditExtractCompletionInd) {
		this.preAuditExtractCompletionInd = preAuditExtractCompletionInd;
	}
	/**
	 * @return the batchStartDateTime
	 */
	public LocalDateTime getBatchStartDateTime() {
		return batchStartDateTime;
	}
	/**
	 * @param batchStartDateTime the batchStartDateTime to set
	 */
	public void setBatchStartDateTime(LocalDateTime batchStartDateTime) {
		this.batchStartDateTime = batchStartDateTime;
	}
	/**
	 * @return the batchEndDateTime
	 */
	public LocalDateTime getBatchEndDateTime() {
		return batchEndDateTime;
	}
	/**
	 * @param batchEndDateTime the batchEndDateTime to set
	 */
	public void setBatchEndDateTime(LocalDateTime batchEndDateTime) {
		this.batchEndDateTime = batchEndDateTime;
	}
	/**
	 * @return the jobInstanceId
	 */
	public Long getJobInstanceId() {
		return jobInstanceId;
	}
	/**
	 * @param jobInstanceId the jobInstanceId to set
	 */
	public void setJobInstanceId(Long jobInstanceId) {
		this.jobInstanceId = jobInstanceId;
	}
	/**
	 * @return the createdBy
	 */
	public String getCreatedBy() {
		return createdBy;
	}
	/**
	 * @param createdBy the createdBy to set
	 */
	public void setCreatedBy(String createdBy) {
		this.createdBy = createdBy;
	}
	/**
	 * @return the lastModifiedBy
	 */
	public String getLastModifiedBy() {
		return lastModifiedBy;
	}
	/**
	 * @param lastModifiedBy the lastModifiedBy to set
	 */
	public void setLastModifiedBy(String lastModifiedBy) {
		this.lastModifiedBy = lastModifiedBy;
	}
	
	
}
