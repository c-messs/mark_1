package gov.hhs.cms.ff.fm.eps.ep.po;

import java.time.LocalDateTime;

/**
 * @author j.radziewski
 * 
 * Entity class for table SBMFILEINFO.
 *
 */
public class SbmFileInfoPO extends GenericSbmFilePO<SbmFileInfoPO> {

	private String sbmFileNm;
	private String sbmFileId;
	private LocalDateTime sbmFileCreateDateTime;
	private String issuerFileSetId;
	private Integer sbmFileNum;
	private Long sbmFileProcessingSummaryId;
	private String tradingPartnerId;
	private String functionCd;
	private String fileInfoXML;
	private boolean rejectedInd = false;
	private LocalDateTime createDateTime;
	private LocalDateTime sbmFileLastModifiedDateTime;
	
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
	 * @return the issuerFileSetId
	 */
	public String getIssuerFileSetId() {
		return issuerFileSetId;
	}
	/**
	 * @param issuerFileSetId the issuerFileSetId to set
	 */
	public void setIssuerFileSetId(String issuerFileSetId) {
		this.issuerFileSetId = issuerFileSetId;
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
	 * @return the fileInfoXML
	 */
	public String getFileInfoXML() {
		return fileInfoXML;
	}
	/**
	 * @param fileInfoXML the fileInfoXML to set
	 */
	public void setFileInfoXML(String fileInfoXML) {
		this.fileInfoXML = fileInfoXML;
	}
	/**
	 * @return the rejectedInd
	 */
	public boolean getRejectedInd() {
		return rejectedInd;
	}
	/**
	 * @param rejectedInd the rejectedInd to set
	 */
	public void setRejectedInd(boolean rejectedInd) {
		this.rejectedInd = rejectedInd;
	}
	/**
	 * @return the createDateTime
	 */
	public LocalDateTime getCreateDateTime() {
		return createDateTime;
	}
	/**
	 * @param createDateTime the createDateTime to set
	 */
	public void setCreateDateTime(LocalDateTime createDateTime) {
		this.createDateTime = createDateTime;
	}
	/**
	 * @return the sbmFileLastModifiedDateTime
	 */
	public LocalDateTime getSbmFileLastModifiedDateTime() {
		return sbmFileLastModifiedDateTime;
	}
	/**
	 * @param sbmFileLastModifiedDateTime the sbmFileLastModifiedDateTime to set
	 */
	public void setSbmFileLastModifiedDateTime(LocalDateTime sbmFileLastModifiedDateTime) {
		this.sbmFileLastModifiedDateTime = sbmFileLastModifiedDateTime;
	}	
	
}
