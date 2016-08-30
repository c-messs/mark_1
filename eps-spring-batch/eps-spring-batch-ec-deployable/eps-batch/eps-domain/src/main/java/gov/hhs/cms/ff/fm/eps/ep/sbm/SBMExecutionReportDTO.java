package gov.hhs.cms.ff.fm.eps.ep.sbm;

import java.time.LocalDateTime;

public class SBMExecutionReportDTO {

	private Integer coverageYear;
	private String stateCd;
	private String fileName;
	private String fileId;
	private LocalDateTime fileLoggedTimestamp;
	private String issuerId;
	private String issuerFileSetId;
	private Integer fileNum;
	private Integer totalFilesInFileset;
	private String fileStatus;
	private LocalDateTime fileStatusTimestamp;

	/**
	 * @return the coverageYear
	 */
	public Integer getCoverageYear() {
		return coverageYear;
	}

	/**
	 * @param coverageYear the coverageYear to set
	 */
	public void setCoverageYear(Integer coverageYear) {
		this.coverageYear = coverageYear;
	}

	/**
	 * @return the stateCd
	 */
	public String getStateCd() {
		return stateCd;
	}

	/**
	 * @param stateCd the stateCd to set
	 */
	public void setStateCd(String stateCd) {
		this.stateCd = stateCd;
	}

	/**
	 * @return the fileName
	 */
	public String getFileName() {
		return fileName;
	}

	/**
	 * @param fileName the fileName to set
	 */
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	/**
	 * @return the fileId
	 */
	public String getFileId() {
		return fileId;
	}

	/**
	 * @param fileId the fileId to set
	 */
	public void setFileId(String fileId) {
		this.fileId = fileId;
	}

	/**
	 * @return the fileLoggedTimestamp
	 */
	public LocalDateTime getFileLoggedTimestamp() {
		return fileLoggedTimestamp;
	}

	/**
	 * @param fileLoggedTimestamp the fileLoggedTimestamp to set
	 */
	public void setFileLoggedTimestamp(LocalDateTime fileLoggedTimestamp) {
		this.fileLoggedTimestamp = fileLoggedTimestamp;
	}

	/**
	 * @return the issuerId
	 */
	public String getIssuerId() {
		return issuerId;
	}

	/**
	 * @param issuerId the issuerId to set
	 */
	public void setIssuerId(String issuerId) {
		this.issuerId = issuerId;
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
	 * @return the fileNum
	 */
	public Integer getFileNum() {
		return fileNum;
	}

	/**
	 * @param fileNum the fileNum to set
	 */
	public void setSbmFileNum(Integer fileNum) {
		this.fileNum = fileNum;
	}

	/**
	 * @return the totalFilesInFileset
	 */
	public Integer getTotalFilesInFileset() {
		return totalFilesInFileset;
	}

	/**
	 * @param totalFilesInFileset the totalFilesInFileset to set
	 */
	public void setTotalFilesInFileset(Integer totalFilesInFileset) {
		this.totalFilesInFileset = totalFilesInFileset;
	}

	/**
	 * @return the fileStatus
	 */
	public String getFileStatus() {
		return fileStatus;
	}

	/**
	 * @param fileStatus the fileStatus to set
	 */
	public void setFileStatus(String fileStatus) {
		this.fileStatus = fileStatus;
	}

	/**
	 * @return the fileStatusTimestamp
	 */
	public LocalDateTime getFileStatusTimestamp() {
		return fileStatusTimestamp;
	}

	/**
	 * @param fileStatusTimestamp the fileStatusTimestamp to set
	 */
	public void setFileStatusTimestamp(LocalDateTime fileStatusTimestamp) {
		this.fileStatusTimestamp = fileStatusTimestamp;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("SBMUpdateStatusErrorDTO [coverageYear=").append(coverageYear)
				.append(", stateCd=").append(stateCd).append(", fileName=").append(fileName)
				.append(", fileId=").append(fileId).append(", fileLoggedTimestamp=").append(fileLoggedTimestamp)
				.append(", issuerId=").append(issuerId).append(", issuerFileSetId=").append(issuerFileSetId)
				.append(", fileNum=").append(fileNum).append(", totalFilesInFileset=").append(totalFilesInFileset)
				.append(", fileStatus=").append(fileStatus).append(", fileStatusTimestamp=").append(fileStatusTimestamp)
				.append("]");
		return builder.toString();
	}
	
	
}
