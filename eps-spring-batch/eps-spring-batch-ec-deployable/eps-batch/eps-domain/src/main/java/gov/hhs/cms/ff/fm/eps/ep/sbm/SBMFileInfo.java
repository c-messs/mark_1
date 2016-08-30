/**
 * 
 */
package gov.hhs.cms.ff.fm.eps.ep.sbm;

import java.time.LocalDateTime;

/**
 * @author rajesh.talanki
 *
 */
public class SBMFileInfo {
	
	private Long sbmFileInfoId;
	private String sbmFileNm;
	private String sbmFileId;
	private LocalDateTime sbmFileCreateDateTime;
	private Integer sbmFileNum;
	private Long sbmFileProcessingSummaryId;
	private String tradingPartnerId;
	private String functionCd;
	private boolean rejectedInd;
	private LocalDateTime createDatetime;
	private LocalDateTime fileLastModifiedDateTime;
	
	/**
	 * @return the sbmFileNm
	 */
	public String getSbmFileNm() {
		return sbmFileNm;
	}
	/**
	 * @param sbmFileNm the sbmFileNm to set
	 */
	public void setSbmFileNm(String sbmFileNm) {
		this.sbmFileNm = sbmFileNm;
	}
	/**
	 * @return the tradingPartnerId
	 */
	public String getTradingPartnerId() {
		return tradingPartnerId;
	}
	/**
	 * @param tradingPartnerId the tradingPartnerId to set
	 */
	public void setTradingPartnerId(String tradingPartnerId) {
		this.tradingPartnerId = tradingPartnerId;
	}
	/**
	 * @return the functionCd
	 */
	public String getFunctionCd() {
		return functionCd;
	}
	/**
	 * @param functionCd the functionCd to set
	 */
	public void setFunctionCd(String functionCd) {
		this.functionCd = functionCd;
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
	 * @return the sbmFileId
	 */
	public String getSbmFileId() {
		return sbmFileId;
	}
	/**
	 * @param sbmFileId the sbmFileId to set
	 */
	public void setSbmFileId(String sbmFileId) {
		this.sbmFileId = sbmFileId;
	}
	/**
	 * @return the sbmFileCreateDateTime
	 */
	public LocalDateTime getSbmFileCreateDateTime() {
		return sbmFileCreateDateTime;
	}
	/**
	 * @param sbmFileCreateDateTime the sbmFileCreateDateTime to set
	 */
	public void setSbmFileCreateDateTime(LocalDateTime sbmFileCreateDateTime) {
		this.sbmFileCreateDateTime = sbmFileCreateDateTime;
	}
	
	/**
	 * @return the sbmFileNum
	 */
	public Integer getSbmFileNum() {
		return sbmFileNum;
	}
	/**
	 * @param sbmFileNum the sbmFileNum to set
	 */
	public void setSbmFileNum(Integer sbmFileNum) {
		this.sbmFileNum = sbmFileNum;
	}
	/**
	 * @return the sbmFileProcessingSummaryId
	 */
	public Long getSbmFileProcessingSummaryId() {
		return sbmFileProcessingSummaryId;
	}
	/**
	 * @param sbmFileProcessingSummaryId the sbmFileProcessingSummaryId to set
	 */
	public void setSbmFileProcessingSummaryId(Long sbmFileProcessingSummaryId) {
		this.sbmFileProcessingSummaryId = sbmFileProcessingSummaryId;
	}
	/**
	 * @return the rejectedInd
	 */
	public boolean isRejectedInd() {
		return rejectedInd;
	}
	/**
	 * @param rejectedInd the rejectedInd to set
	 */
	public void setRejectedInd(boolean rejectedInd) {
		this.rejectedInd = rejectedInd;
	}
	/**
	 * @return the createDatetime
	 */
	public LocalDateTime getCreateDatetime() {
		return createDatetime;
	}
	/**
	 * @param createDatetime the createDatetime to set
	 */
	public void setCreateDatetime(LocalDateTime createDatetime) {
		this.createDatetime = createDatetime;
	}
	
	/**
	 * @return the fileLastModifiedDateTime
	 */
	public LocalDateTime getFileLastModifiedDateTime() {
		return fileLastModifiedDateTime;
	}
	/**
	 * @param fileLastModifiedDateTime the fileLastModifiedDateTime to set
	 */
	public void setFileLastModifiedDateTime(LocalDateTime fileLastModifiedDateTime) {
		this.fileLastModifiedDateTime = fileLastModifiedDateTime;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("SBMFileInfo [sbmFileInfoId=").append(sbmFileInfoId).append(", sbmFileNm=").append(sbmFileNm)
				.append(", sbmFileId=").append(sbmFileId).append(", sbmFileCreateDateTime=")
				.append(sbmFileCreateDateTime).append(", sbmFileNum=").append(sbmFileNum)
				.append(", sbmFileProcessingSummaryId=").append(sbmFileProcessingSummaryId)
				.append(", tradingPartnerId=").append(tradingPartnerId).append(", functionCd=").append(functionCd)
				.append(", rejectedInd=").append(rejectedInd).append(", createDatetime=").append(createDatetime)
				.append(", fileLastModifiedDateTime=").append(fileLastModifiedDateTime).append("]");
		return builder.toString();
	}
			
}
