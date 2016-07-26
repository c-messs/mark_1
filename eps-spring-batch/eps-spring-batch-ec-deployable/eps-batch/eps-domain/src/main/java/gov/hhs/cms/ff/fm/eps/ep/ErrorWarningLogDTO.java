package gov.hhs.cms.ff.fm.eps.ep;


/**
 * @author eps
 *
 */
public class ErrorWarningLogDTO {
	
	private String errorWarningDetailedDesc;
	private String bizAppAckErrorCd;
	private String errorElement;
	private String processingErrorCd;
	private Long batchId;
	private Long transMsgId;
	private Long transMsgFileInfoId;
	
	/**
	 * Constructor
	 */
	public ErrorWarningLogDTO() {
		super();
	}
	
	
	/**
	 * Constructor with BEM attributes
	 * 
	 * @param bemDTO
	 */
	public ErrorWarningLogDTO(BenefitEnrollmentMaintenanceDTO bemDTO) {
		super();
		this.batchId = bemDTO.getBatchId();
		this.transMsgId = bemDTO.getTransMsgId();
		this.transMsgFileInfoId = bemDTO.getTxnMessageFileInfoId();
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
	public String getProcessingErrorCd() {
		return processingErrorCd;
	}
	public void setProcessingErrorCd(String processingErrorCd) {
		this.processingErrorCd = processingErrorCd;
	}
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
	public Long getTransMsgFileInfoId() {
		return transMsgFileInfoId;
	}
	public void setTransMsgFileInfoId(Long transMsgFileInfoId) {
		this.transMsgFileInfoId = transMsgFileInfoId;
	}


	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "ErrorWarningLogDTO [\n\terrorWarningDetailedDesc="
				+ errorWarningDetailedDesc + "\n\tbizAppAckErrorCd="
				+ bizAppAckErrorCd + "\n\terrorElement=" + errorElement
				+ "\n\tprocessingErrorCd=" + processingErrorCd + "\n\tbatchId="
				+ batchId + ", transMsgId=" + transMsgId
				+ "\n\ttransMsgFileInfoId=" + transMsgFileInfoId + "]";
	}
	
	

}
