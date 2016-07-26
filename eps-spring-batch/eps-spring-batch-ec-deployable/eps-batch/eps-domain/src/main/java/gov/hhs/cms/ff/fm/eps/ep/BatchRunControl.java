/**
 * 
 */
package gov.hhs.cms.ff.fm.eps.ep;

import org.joda.time.DateTime;

/**
 * Class to hold BatchRunControl table attributes
 * 
 * @author girish.padmanabhan
 *
 */
public class BatchRunControl {
	
	private String batchRunControlId;
	private String extractFilePath;
	private DateTime highWaterMarkStartDateTime;
	private DateTime highWaterMarkEndDateTime;
	private Integer recordCountQuantity;
	private String preAuditExtractCompletionInd;
	private DateTime batchStartDateTime;
	private DateTime batchEndDateTime;
	private Long jobInstanceId;
	private String createdBy;
	private String lastModifiedBy;
	
	/**
	 * @return the batchRunControlIdentifier
	 */
	public String getBatchRunControlId() {
		return batchRunControlId;
	}
	/**
	 * @param batchRunControlIdentifier the batchRunControlIdentifier to set
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
	public DateTime getHighWaterMarkStartDateTime() {
		return highWaterMarkStartDateTime;
	}
	/**
	 * @param highWaterMarkStartDateTime the highWaterMarkStartDateTime to set
	 */
	public void setHighWaterMarkStartDateTime(DateTime highWaterMarkStartDateTime) {
		this.highWaterMarkStartDateTime = highWaterMarkStartDateTime;
	}
	/**
	 * @return the highWaterMarkEndDateTime
	 */
	public DateTime getHighWaterMarkEndDateTime() {
		return highWaterMarkEndDateTime;
	}
	/**
	 * @param highWaterMarkEndDateTime the highWaterMarkEndDateTime to set
	 */
	public void setHighWaterMarkEndDateTime(DateTime highWaterMarkEndDateTime) {
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
	public DateTime getBatchStartDateTime() {
		return batchStartDateTime;
	}
	/**
	 * @param batchStartDateTime the batchStartDateTime to set
	 */
	public void setBatchStartDateTime(DateTime batchStartDateTime) {
		this.batchStartDateTime = batchStartDateTime;
	}
	/**
	 * @return the batchEndDateTime
	 */
	public DateTime getBatchEndDateTime() {
		return batchEndDateTime;
	}
	/**
	 * @param batchEndDateTime the batchEndDateTime to set
	 */
	public void setBatchEndDateTime(DateTime batchEndDateTime) {
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
