package gov.hhs.cms.ff.fm.eps.ep.sbm;

import org.apache.commons.csv.CSVRecord;

import gov.hhs.cms.ff.fm.eps.ep.enums.SBMFileStatus;

/**
 * Holds SBM Update Status Record data
 *
 */
public class SBMUpdateStatusRecordDTO {

	private String linenumber;
	private String tenantId;
	private String issuerId;
	private String fileId;
	private String issuerFileSetId;
	private String status;
	private CSVRecord csvRecord;
	private Long sbmFileProcSumId;
	private SBMFileStatus newFileSatus;
	private SBMFileStatus currentFileStatus;
	
	
	/**
	 * @return the linenumber
	 */
	public String getLinenumber() {
		return linenumber;
	}
	/**	
	 * @param linenumber the linenumber to set
	 */
	public void setLinenumber(String linenumber) {
		this.linenumber = linenumber;
	}
	/**
	 * @return the tenantId
	 */
	public String getTenantId() {
		return tenantId;
	}
	/**
	 * @param tenantId the tenantId to set
	 */
	public void setTenantId(String tenantId) {
		this.tenantId = tenantId;
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
	 * @return the status
	 */
	public String getStatus() {
		return status;
	}
	/**
	 * @param status the status to set
	 */
	public void setStatus(String status) {
		this.status = status;
	}
	/**
	 * @return the csvRecord
	 */
	public CSVRecord getCsvRecord() {
		return csvRecord;
	}
	/**
	 * @param csvRecord the csvRecord to set
	 */
	public void setCsvRecord(CSVRecord csvRecord) {
		this.csvRecord = csvRecord;
	}
	/**
	 * @return the sbmFileProcSumId
	 */
	public Long getSbmFileProcSumId() {
		return sbmFileProcSumId;
	}
	/**
	 * @param sbmFileProcSumId the sbmFileProcSumId to set
	 */
	public void setSbmFileProcSumId(Long sbmFileProcSumId) {
		this.sbmFileProcSumId = sbmFileProcSumId;
	}
	/**
	 * @return the newFileSatus
	 */
	public SBMFileStatus getNewFileSatus() {
		return newFileSatus;
	}
	/**
	 * @param newFileSatus the newFileSatus to set
	 */
	public void setNewFileSatus(SBMFileStatus newFileSatus) {
		this.newFileSatus = newFileSatus;
	}
	/**
	 * @return the currentFileStatus
	 */
	public SBMFileStatus getCurrentFileStatus() {
		return currentFileStatus;
	}
	/**
	 * @param currentFileStatus the currentFileStatus to set
	 */
	public void setCurrentFileStatus(SBMFileStatus currentFileStatus) {
		this.currentFileStatus = currentFileStatus;
	}
	
	
}
