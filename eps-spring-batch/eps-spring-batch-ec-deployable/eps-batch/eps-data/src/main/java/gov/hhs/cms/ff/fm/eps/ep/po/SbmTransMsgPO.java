package gov.hhs.cms.ff.fm.eps.ep.po;

import java.time.LocalDateTime;

public class SbmTransMsgPO {
	
	private Long sbmTransMsgId;
	private LocalDateTime transMsgDateTime;
	private String msg;         
	private String transMsgDirectionTypeCd;
	private String transMsgTypeCd;        
	private String subscriberStateCd;
	private Long sbmFileInfoId;
	private Integer recordControlNum;
	private String planId;
	private String sbmTransMsgProcStatusTypeCd;
	private String exchangeAssignedPolicyId;
	private String exchangeAssignedSubscriberId;
	
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
	 * @return the transMsgDateTime
	 */
	public LocalDateTime getTransMsgDateTime() {
		return transMsgDateTime;
	}
	/**
	 * @param transMsgDateTime the transMsgDateTime to set
	 */
	public void setTransMsgDateTime(LocalDateTime transMsgDateTime) {
		this.transMsgDateTime = transMsgDateTime;
	}
	/**
	 * @return the msg
	 */
	public String getMsg() {
		return msg;
	}
	/**
	 * @param msg the msg to set
	 */
	public void setMsg(String msg) {
		this.msg = msg;
	}
	/**
	 * @return the transMsgDirectionTypeCd
	 */
	public String getTransMsgDirectionTypeCd() {
		return transMsgDirectionTypeCd;
	}
	/**
	 * @param transMsgDirectionTypeCd the transMsgDirectionTypeCd to set
	 */
	public void setTransMsgDirectionTypeCd(String transMsgDirectionTypeCd) {
		this.transMsgDirectionTypeCd = transMsgDirectionTypeCd;
	}
	/**
	 * @return the transMsgTypeCd
	 */
	public String getTransMsgTypeCd() {
		return transMsgTypeCd;
	}
	/**
	 * @param transMsgTypeCd the transMsgTypeCd to set
	 */
	public void setTransMsgTypeCd(String transMsgTypeCd) {
		this.transMsgTypeCd = transMsgTypeCd;
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
	 * @return the recordControlNum
	 */
	public Integer getRecordControlNum() {
		return recordControlNum;
	}
	/**
	 * @param recordControlNum the recordControlNum to set
	 */
	public void setRecordControlNum(Integer recordControlNum) {
		this.recordControlNum = recordControlNum;
	}
	/**
	 * @return the planId
	 */
	public String getPlanId() {
		return planId;
	}
	/**
	 * @param planId the planId to set
	 */
	public void setPlanId(String planId) {
		this.planId = planId;
	}
	/**
	 * @return the sbmTransMsgProcStatusTypeCd
	 */
	public String getSbmTransMsgProcStatusTypeCd() {
		return sbmTransMsgProcStatusTypeCd;
	}
	/**
	 * @param sbmTransMsgProcStatusTypeCd the sbmTransMsgProcStatusTypeCd to set
	 */
	public void setSbmTransMsgProcStatusTypeCd(String sbmTransMsgProcStatusTypeCd) {
		this.sbmTransMsgProcStatusTypeCd = sbmTransMsgProcStatusTypeCd;
	}
	/**
	 * @return the exchangeAssignedPolicyId
	 */
	public String getExchangeAssignedPolicyId() {
		return exchangeAssignedPolicyId;
	}
	/**
	 * @param exchangeAssignedPolicyId the exchangeAssignedPolicyId to set
	 */
	public void setExchangeAssignedPolicyId(String exchangeAssignedPolicyId) {
		this.exchangeAssignedPolicyId = exchangeAssignedPolicyId;
	}
	/**
	 * @return the exchangeAssignedSubscriberId
	 */
	public String getExchangeAssignedSubscriberId() {
		return exchangeAssignedSubscriberId;
	}
	/**
	 * @param exchangeAssignedSubscriberId the exchangeAssignedSubscriberId to set
	 */
	public void setExchangeAssignedSubscriberId(String exchangeAssignedSubscriberId) {
		this.exchangeAssignedSubscriberId = exchangeAssignedSubscriberId;
	}

}
