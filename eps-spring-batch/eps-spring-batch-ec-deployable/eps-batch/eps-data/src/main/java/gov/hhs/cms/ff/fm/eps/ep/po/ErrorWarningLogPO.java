package gov.hhs.cms.ff.fm.eps.ep.po;


import org.joda.time.DateTime;

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
	private DateTime createDateTime;
	private DateTime lastModifiedDateTime;
	private String createBy;
	private String lastModifiedBy;
	private Long transMsgFileInfoId;
	private Long batchId;
	
	public Long getErrorWarningId() {
		return errorWarningId;
	}
	public void setErrorWarningId(Long errorWarningId) {
		this.errorWarningId = errorWarningId;
	}
	public String getErrorWarningDetailedDesc() {
		return errorWarningDetailedDesc;
	}
	public void setErrorWarningDetailedDesc(String errorWarningDetailedDesc) {
		this.errorWarningDetailedDesc = errorWarningDetailedDesc;
	}
	public String getBizAppAckErrorCd() {
		return bizAppAckErrorCd;
	}
	public void setBizAppAckErrorCd(String bizAppAckErrorCd) {
		this.bizAppAckErrorCd = bizAppAckErrorCd;
	}
	public String getErrorElement() {
		return errorElement;
	}
	public void setErrorElement(String errorElement) {
		this.errorElement = errorElement;
	}
	public Long getTransMsgID() {
		return transMsgID;
	}
	public void setTransMsgID(Long transMsgID) {
		this.transMsgID = transMsgID;
	}
	public String getProcessingErrorCd() {
		return processingErrorCd;
	}
	public void setProcessingErrorCd(String processingErrorCd) {
		this.processingErrorCd = processingErrorCd;
	}
	public DateTime getCreateDateTime() {
		return createDateTime;
	}
	public void setCreateDateTime(DateTime createDateTime) {
		this.createDateTime = createDateTime;
	}
	public DateTime getLastModifiedDateTime() {
		return lastModifiedDateTime;
	}
	public void setLastModifiedDateTime(DateTime lastModifiedDateTime) {
		this.lastModifiedDateTime = lastModifiedDateTime;
	}
	public String getCreateBy() {
		return createBy;
	}
	public void setCreateBy(String createBy) {
		this.createBy = createBy;
	}
	public String getLastModifiedBy() {
		return lastModifiedBy;
	}
	public void setLastModifiedBy(String lastModifiedBy) {
		this.lastModifiedBy = lastModifiedBy;
	}
	public Long getTransMsgFileInfoId() {
		return transMsgFileInfoId;
	}
	public void setTransMsgFileInfoId(Long transMsgFileInfoId) {
		this.transMsgFileInfoId = transMsgFileInfoId;
	}
	public Long getBatchId() {
		return batchId;
	}
	public void setBatchId(Long batchId) {
		this.batchId = batchId;
	}
}
