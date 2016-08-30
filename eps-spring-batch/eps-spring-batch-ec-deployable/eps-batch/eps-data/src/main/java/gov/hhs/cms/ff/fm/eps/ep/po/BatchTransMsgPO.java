package gov.hhs.cms.ff.fm.eps.ep.po;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * @author eps
 *
 */
public class BatchTransMsgPO {
	
	private Long batchId;
	private Long transMsgId;
	private String processedToDbStatusTypeCd;
	private LocalDate createDateTime;
	private LocalDate lastModifiedDateTime;
	private String createBy;
	private String lastModifiedBy;
	private String exchangePolicyId;
	private String subscriberStateCd;
	private String issuerHiosId;
	private Long sourceVersionId;
	private LocalDateTime sourceVersionDateTime;
	private String transMsgSkipReasonTypeCd;
	private String transMsgSkipReasonDesc;
	
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
	/**
	 * @return the transMsgId
	 */
	public Long getTransMsgId() {
		return transMsgId;
	}
	/**
	 * @param transMsgId the transMsgId to set
	 */
	public void setTransMsgId(Long transMsgId) {
		this.transMsgId = transMsgId;
	}
	/**
	 * @return the processedToDbStatusTypeCd
	 */
	public String getProcessedToDbStatusTypeCd() {
		return processedToDbStatusTypeCd;
	}
	/**
	 * @param processedToDbStatusTypeCd the processedToDbStatusTypeCd to set
	 */
	public void setProcessedToDbStatusTypeCd(String processedToDbStatusTypeCd) {
		this.processedToDbStatusTypeCd = processedToDbStatusTypeCd;
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
	 * @return the exchangePolicyId
	 */
	public String getExchangePolicyId() {
		return exchangePolicyId;
	}
	/**
	 * @param exchangePolicyId the exchangePolicyId to set
	 */
	public void setExchangePolicyId(String exchangePolicyId) {
		this.exchangePolicyId = exchangePolicyId;
	}
	/**
	 * @return the subscriberStateCd
	 */
	public String getSubscriberStateCd() {
		return subscriberStateCd;
	}
	/**
	 * @param subscriberStateCd the subscriberStateCd to set
	 */
	public void setSubscriberStateCd(String subscriberStateCd) {
		this.subscriberStateCd = subscriberStateCd;
	}
	/**
	 * @return the issuerHiosId
	 */
	public String getIssuerHiosId() {
		return issuerHiosId;
	}
	/**
	 * @param issuerHiosId the issuerHiosId to set
	 */
	public void setIssuerHiosId(String issuerHiosId) {
		this.issuerHiosId = issuerHiosId;
	}
	/**
	 * @return the sourceVersionId
	 */
	public Long getSourceVersionId() {
		return sourceVersionId;
	}
	/**
	 * @param sourceVersionId the sourceVersionId to set
	 */
	public void setSourceVersionId(Long sourceVersionId) {
		this.sourceVersionId = sourceVersionId;
	}
	/**
	 * @return the sourceVersionDateTime
	 */
	public LocalDateTime getSourceVersionDateTime() {
		return sourceVersionDateTime;
	}
	/**
	 * @param sourceVersionDateTime the sourceVersionDateTime to set
	 */
	public void setSourceVersionDateTime(LocalDateTime sourceVersionDateTime) {
		this.sourceVersionDateTime = sourceVersionDateTime;
	}
	/**
	 * @return the transMsgSkipReasonTypeCd
	 */
	public String getTransMsgSkipReasonTypeCd() {
		return transMsgSkipReasonTypeCd;
	}
	/**
	 * @param transMsgSkipReasonTypeCd the transMsgSkipReasonTypeCd to set
	 */
	public void setTransMsgSkipReasonTypeCd(String transMsgSkipReasonTypeCd) {
		this.transMsgSkipReasonTypeCd = transMsgSkipReasonTypeCd;
	}
	/**
	 * @return the transMsgSkipReasonDesc
	 */
	public String getTransMsgSkipReasonDesc() {
		return transMsgSkipReasonDesc;
	}
	/**
	 * @param transMsgSkipReasonDesc the transMsgSkipReasonDesc to set
	 */
	public void setTransMsgSkipReasonDesc(String transMsgSkipReasonDesc) {
		this.transMsgSkipReasonDesc = transMsgSkipReasonDesc;
	}	

	
}
