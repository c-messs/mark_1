package gov.hhs.cms.ff.fm.eps.ep.po;

import org.joda.time.DateTime;

/**
 * @author eps
 *
 */
public class BatchTransMsgPO {
	private Long batchId;
	private Long transMsgId;
	private String processedToDbStatusTypeCd;
	private DateTime createDateTime;
	private DateTime lastModifiedDateTime;
	private String createBy;
	private String lastModifiedBy;
	private String exchangePolicyId;
	private String subscriberStateCd;
	private String issuerHiosId;
	private Long sourceVersionId;
	private DateTime sourceVersionDateTime;
	private String transMsgSkipReasonTypeCd;
	private String transMsgSkipReasonDesc;	

	
	public Long getBatchId() {
		return batchId;
	}
	public void setBatchId(Long batchId) {
		this.batchId = batchId;
	}
	public Long getTransMsgId() {
		return transMsgId;
	}
	public void setTransMsgId(Long transMsgId) {
		this.transMsgId = transMsgId;
	}
	public String getProcessedToDbStatusTypeCd() {
		return processedToDbStatusTypeCd;
	}
	public void setProcessedToDbStatusTypeCd(String processedToDbStatusTypeCd) {
		this.processedToDbStatusTypeCd = processedToDbStatusTypeCd;
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
	public String getExchangePolicyId() {
		return exchangePolicyId;
	}
	public void setExchangePolicyId(String exchangePolicyId) {
		this.exchangePolicyId = exchangePolicyId;
	}
	public String getSubscriberStateCd() {
		return subscriberStateCd;
	}
	public void setSubscriberStateCd(String subscriberStateCd) {
		this.subscriberStateCd = subscriberStateCd;
	}
	public String getIssuerHiosId() {
		return issuerHiosId;
	}
	public void setIssuerHiosId(String issuerHiosId) {
		this.issuerHiosId = issuerHiosId;
	}
	public Long getSourceVersionId() {
		return sourceVersionId;
	}
	public void setSourceVersionId(Long sourceVersionId) {
		this.sourceVersionId = sourceVersionId;
	}
	public DateTime getSourceVersionDateTime() {
		return sourceVersionDateTime;
	}
	public void setSourceVersionDateTime(DateTime sourceVersionDateTime) {
		this.sourceVersionDateTime = sourceVersionDateTime;
	}
	public String getTransMsgSkipReasonTypeCd() {
		return transMsgSkipReasonTypeCd;
	}
	public void setTransMsgSkipReasonTypeCd(String transMsgSkipReasonTypeCd) {
		this.transMsgSkipReasonTypeCd = transMsgSkipReasonTypeCd;
	}
	public String getTransMsgSkipReasonDesc() {
		return transMsgSkipReasonDesc;
	}
	public void setTransMsgSkipReasonDesc(String transMsgSkipReasonDesc) {
		this.transMsgSkipReasonDesc = transMsgSkipReasonDesc;
	}
	
}
