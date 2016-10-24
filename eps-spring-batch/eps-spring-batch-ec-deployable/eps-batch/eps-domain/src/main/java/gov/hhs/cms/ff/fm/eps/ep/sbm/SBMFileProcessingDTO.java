package gov.hhs.cms.ff.fm.eps.ep.sbm;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import gov.cms.dsh.sbmi.FileInformationType;
import gov.cms.dsh.sbmi.FileInformationType.IssuerFileInformation.IssuerFileSet;

/**
 * @author j.radziewski
 *
 */
public class SBMFileProcessingDTO extends SBMFileProccessingSummary {
	
	private FileInformationType fileInfoType;
	private File sbmiFile;
	private SBMFileInfo sbmFileInfo;
	private List<SBMErrorDTO> errorList = new ArrayList<>();
	private Long batchId;
	private boolean isValidXML;
	private String sbmFileXML;
	private Integer xprProcGroupSize;
	private String fileInfoXML;
	private SBMSummaryAndFileInfoDTO fileProcSummaryFromDB;
	private InputStreamReader sbmFileXMLStream;
	
	/**
	 * @return the fileInfoType
	 */
	public FileInformationType getFileInfoType() {
		return fileInfoType;
	}

	/**
	 * @param fileInfoType the fileInfoType to set
	 */
	public void setFileInfoType(FileInformationType fileInfoType) {
		this.fileInfoType = fileInfoType;
	}

	/**
	 * @return the sbmiFile
	 */
	public File getSbmiFile() {
		return sbmiFile;
	}

	/**
	 * @param sbmiFile the sbmiFile to set
	 */
	public void setSbmiFile(File sbmiFile) {
		this.sbmiFile = sbmiFile;
	}

	/**
	 * @return the sbmFileInfo
	 */
	public SBMFileInfo getSbmFileInfo() {
		return sbmFileInfo;
	}

	/**
	 * @param sbmFileInfo the sbmFileInfo to set
	 */
	public void setSbmFileInfo(SBMFileInfo sbmFileInfo) {
		this.sbmFileInfo = sbmFileInfo;
	}
	
		
	/**
	 * @return the errorList
	 */
	public List<SBMErrorDTO> getErrorList() {
		return errorList;
	}

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
	 * @return the isValidXML
	 */
	public boolean isValidXML() {
		return isValidXML;
	}

	/**
	 * @param isValidXML the isValidXML to set
	 */
	public void setValidXML(boolean isValidXML) {
		this.isValidXML = isValidXML;
	}
	
	/**
	 * @return the sbmFileXML
	 */
	public String getSbmFileXML() {
		return sbmFileXML;
	}

	/**
	 * @param sbmFileXML the sbmFileXML to set
	 */
	public void setSbmFileXML(String sbmFileXML) {
		this.sbmFileXML = sbmFileXML;
	}
	
	/**
	 * @return the xprProcGroupSize
	 */
	public Integer getXprProcGroupSize() {
		return xprProcGroupSize;
	}

	/**
	 * @param xprProcGroupSize the xprProcGroupSize to set
	 */
	public void setXprProcGroupSize(Integer xprProcGroupSize) {
		this.xprProcGroupSize = xprProcGroupSize;
	}

	public String getLogMsg() {

		String msg = null;
		if (sbmiFile != null) {
			msg = "fileNm=" + sbmiFile.getName();
		}
		if (fileInfoType != null) {
			msg = ", tenantId=" + fileInfoType.getTenantId() + ", fileId=" + fileInfoType.getFileId();
			if (fileInfoType.getIssuerFileInformation() != null) {
				msg += ", issuerId=" + fileInfoType.getIssuerFileInformation().getIssuerId();
				msg += ", " + fileInfoType.getIssuerFileInformation().getIssuerFileSet().getFileNumber();
				IssuerFileSet issuerFileSet = fileInfoType.getIssuerFileInformation().getIssuerFileSet();
				if (issuerFileSet != null) {
					msg += ", issuerFileSetId=" + issuerFileSet.getIssuerFileSetId();
					msg += ", fileNumber " + issuerFileSet.getFileNumber() + " of " + issuerFileSet.getTotalIssuerFiles();
				} else {
					msg +=", issuerFileSet=null";
				}
			} else {
				msg += ", issuerFileInformation=null";
			}
		} else {
			msg = ", fileInfoType=null";
		}
		return msg;
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
	 * @return the fileProcSummaryFromDB
	 */
	public SBMSummaryAndFileInfoDTO getFileProcSummaryFromDB() {
		return fileProcSummaryFromDB;
	}

	/**
	 * @param fileProcSummaryFromDB the fileProcSummaryFromDB to set
	 */
	public void setFileProcSummaryFromDB(SBMSummaryAndFileInfoDTO fileProcSummaryFromDB) {
		this.fileProcSummaryFromDB = fileProcSummaryFromDB;
	}

	public InputStreamReader getSbmFileXMLStream() {
		return sbmFileXMLStream; 
	}
	
	public void setSbmFileXMLStream(InputStreamReader in) {
		this.sbmFileXMLStream = in; 
	}

	
}
