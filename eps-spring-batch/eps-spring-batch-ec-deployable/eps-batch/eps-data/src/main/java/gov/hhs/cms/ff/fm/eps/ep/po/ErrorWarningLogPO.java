package gov.hhs.cms.ff.fm.eps.ep.po;


import java.time.LocalDate;

/**
 * @author eps
 *
 */
public class ErrorWarningLogPO {
	
	private Long errorWarningId ;
	private String errorWarningDetailedDesc;
	private String bizAppAckErrorCd;
	private String errorElement;
	private Long transMsgID;
	private String processingErrorCd;
	private LocalDate createDateTime;
	private LocalDate lastModifiedDateTime;
	private String createBy;
	private String lastModifiedBy;
	private Long transMsgFileInfoId;
	private Long batchId;
	/**
	 * @return the errorWarningId
	 */
	public Long getErrorWarningId() {
		return errorWarningId;
	}
	/**
	 * @param errorWarningId the errorWarningId to set
	 */
	public void setErrorWarningId(Long errorWarningId) {
		this.errorWarningId = errorWarningId;
	}
	/**
	 * @return the errorWarningDetailedDesc
	 */
	public String getErrorWarningDetailedDesc() {
		return errorWarningDetailedDesc;
	}
	/**
	 * @param errorWarningDetailedDesc the errorWarningDetailedDesc to set
	 */
	public void setErrorWarningDetailedDesc(String errorWarningDetailedDesc) {
		this.errorWarningDetailedDesc = errorWarningDetailedDesc;
	}
	/**
	 * @return the bizAppAckErrorCd
	 */
	public String getBizAppAckErrorCd() {
		return bizAppAckErrorCd;
	}
	/**
	 * @param bizAppAckErrorCd the bizAppAckErrorCd to set
	 */
	public void setBizAppAckErrorCd(String bizAppAckErrorCd) {
		this.bizAppAckErrorCd = bizAppAckErrorCd;
	}
	/**
	 * @return the errorElement
	 */
	public String getErrorElement() {
		return errorElement;
	}
	/**
	 * @param errorElement the errorElement to set
	 */
	public void setErrorElement(String errorElement) {
		this.errorElement = errorElement;
	}
	/**
	 * @return the transMsgID
	 */
	public Long getTransMsgID() {
		return transMsgID;
	}
	/**
	 * @param transMsgID the transMsgID to set
	 */
	public void setTransMsgID(Long transMsgID) {
		this.transMsgID = transMsgID;
	}
	/**
	 * @return the processingErrorCd
	 */
	public String getProcessingErrorCd() {
		return processingErrorCd;
	}
	/**
	 * @param processingErrorCd the processingErrorCd to set
	 */
	public void setProcessingErrorCd(String processingErrorCd) {
		this.processingErrorCd = processingErrorCd;
	}
	/**
	 * @return the createDateTime
	 */
	public LocalDate getCreateDateTime() {
		return createDateTime;
	}
	/**
	 * @param createDateTime the createDateTime to set
	 */
	public void setCreateDateTime(LocalDate createDateTime) {
		this.createDateTime = createDateTime;
	}
	/**
	 * @return the lastModifiedDateTime
	 */
	public LocalDate getLastModifiedDateTime() {
		return lastModifiedDateTime;
	}
	/**
	 * @param lastModifiedDateTime the lastModifiedDateTime to set
	 */
	public void setLastModifiedDateTime(LocalDate lastModifiedDateTime) {
		this.lastModifiedDateTime = lastModifiedDateTime;
	}
	/**
	 * @return the createBy
	 */
	public String getCreateBy() {
		return createBy;
	}
	/**
	 * @param createBy the createBy to set
	 */
	public void setCreateBy(String createBy) {
		this.createBy = createBy;
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
	/**
	 * @return the transMsgFileInfoId
	 */
	public Long getTransMsgFileInfoId() {
		return transMsgFileInfoId;
	}
	/**
	 * @param transMsgFileInfoId the transMsgFileInfoId to set
	 */
	public void setTransMsgFileInfoId(Long transMsgFileInfoId) {
		this.transMsgFileInfoId = transMsgFileInfoId;
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
