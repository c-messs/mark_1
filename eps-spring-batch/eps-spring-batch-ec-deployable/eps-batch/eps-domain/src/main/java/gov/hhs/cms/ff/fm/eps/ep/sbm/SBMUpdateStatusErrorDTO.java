package gov.hhs.cms.ff.fm.eps.ep.sbm;

public class SBMUpdateStatusErrorDTO {

	private String lineNumber;
	private String errorCode;
	private String errorDescription;
	private String fileId;
	private String fileSetId;
	
	/**
	 * @return the lineNumber
	 */
	public String getLineNumber() {
		return lineNumber;
	}
	/**
	 * @param lineNumber the lineNumber to set
	 */
	public void setLineNumber(String lineNumber) {
		this.lineNumber = lineNumber;
	}
	/**
	 * @return the errorCode
	 */
	public String getErrorCode() {
		return errorCode;
	}
	/**
	 * @param errorCode the errorCode to set
	 */
	public void setErrorCode(String errorCode) {
		this.errorCode = errorCode;
	}
	/**
	 * @return the errorDescription
	 */
	public String getErrorDescription() {
		return errorDescription;
	}
	/**
	 * @param errorDescription the errorDescription to set
	 */
	public void setErrorDescription(String errorDescription) {
		this.errorDescription = errorDescription;
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
	 * @return the fileSetId
	 */
	public String getFileSetId() {
		return fileSetId;
	}
	/**
	 * @param fileSetId the fileSetId to set
	 */
	public void setFileSetId(String fileSetId) {
		this.fileSetId = fileSetId;
	}
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("SBMUpdateStatusErrorDTO [lineNumber=").append(lineNumber).append(", errorCode=")
				.append(errorCode).append(", errorDescription=").append(errorDescription).append(", fileId=")
				.append(fileId).append(", fileSetId=").append(fileSetId).append("]");
		return builder.toString();
	}
	
	
}
