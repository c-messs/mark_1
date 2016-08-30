/**
 * 
 */
package gov.hhs.cms.ff.fm.eps.ep.sbm;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import gov.cms.dsh.sbmi.FileInformationType;
import gov.cms.dsh.sbmi.PolicyType;
import gov.hhs.cms.ff.fm.eps.ep.enums.PolicyStatus;
import gov.hhs.cms.ff.fm.eps.ep.enums.SBMFileStatus;

/**
 * @author j.radziewski
 *
 */
public class SBMPolicyDTO {
	
	private Long batchId;
	private Long sbmTransMsgId;
	private Long sbmFileInfoId;
	private Long sbmFileProcSummaryId;
	private Long processingGroupid;
	private Long stagingSbmPolicyid;
	private Long policyVersionId;
	private PolicyType policy;
	private FileInformationType fileInfo;
	private String policyXml;
	private String fileInfoXml;
	private List<SbmErrWarningLogDTO> errorList = new ArrayList<SbmErrWarningLogDTO>();
	private List<SBMErrorDTO> schemaErrorList =  new ArrayList<SBMErrorDTO>();
	private Map<LocalDate, SBMPremium> sbmPremiums;
	private boolean errorFlag = false;
	private SBMFileStatus fileStatus;
	private PolicyStatus policyStatus;
	private LocalDateTime fileProcessDateTime;

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
	 * @return the sbmFileProcSummaryId
	 */
	public Long getSbmFileProcSummaryId() {
		return sbmFileProcSummaryId;
	}

	/**
	 * @param sbmFileProcSummaryId the sbmFileProcSummaryId to set
	 */
	public void setSbmFileProcSummaryId(Long sbmFileProcSummaryId) {
		this.sbmFileProcSummaryId = sbmFileProcSummaryId;
	}

	/**
	 * @return the processingGroupid
	 */
	public Long getProcessingGroupid() {
		return processingGroupid;
	}

	/**
	 * @param processingGroupid the processingGroupid to set
	 */
	public void setProcessingGroupid(Long processingGroupid) {
		this.processingGroupid = processingGroupid;
	}

	/**
	 * @return the stagingSbmPolicyid
	 */
	public Long getStagingSbmPolicyid() {
		return stagingSbmPolicyid;
	}

	/**
	 * @param stagingSbmPolicyid the stagingSbmPolicyid to set
	 */
	public void setStagingSbmPolicyid(Long stagingSbmPolicyid) {
		this.stagingSbmPolicyid = stagingSbmPolicyid;
	}
	
	/**
	 * @return the policyVersionId
	 */
	public Long getPolicyVersionId() {
		return policyVersionId;
	}

	/**
	 * @param policyVersionId the policyVersionId to set
	 */
	public void setPolicyVersionId(Long policyVersionId) {
		this.policyVersionId = policyVersionId;
	}

	/**
	 * @return the policy
	 */
	public PolicyType getPolicy() {
		return policy;
	}

	/**
	 * @param policy the policy to set
	 */
	public void setPolicy(PolicyType policy) {
		this.policy = policy;
	}
	
	/**
	 * @return the fileInfo
	 */
	public FileInformationType getFileInfo() {
		return fileInfo;
	}

	/**
	 * @param fileInfo the fileInfo to set
	 */
	public void setFileInfo(FileInformationType fileInfo) {
		this.fileInfo = fileInfo;
	}

	/**
	 * @return the policyXml
	 */
	public String getPolicyXml() {
		return policyXml;
	}

	/**
	 * @param policyXml the policyXml to set
	 */
	public void setPolicyXml(String policyXml) {
		this.policyXml = policyXml;
	}

	/**
	 * @return the fileInfoXml
	 */
	public String getFileInfoXml() {
		return fileInfoXml;
	}

	/**
	 * @param fileInfoXml the fileInfoXml to set
	 */
	public void setFileInfoXml(String fileInfoXml) {
		this.fileInfoXml = fileInfoXml;
	}

	/**
	 * @return the errorList
	 */
	public List<SbmErrWarningLogDTO> getErrorList() {
		return errorList;
	}
	
	/**
	 * @param errorList the errorList to set
	 */
	public void setErrorList(List<SbmErrWarningLogDTO> errorList) {
		this.errorList = errorList;
	}
	
	/**
	 * @return the schemaErrorList
	 */
	public List<SBMErrorDTO> getSchemaErrorList() {
		return schemaErrorList;
	}

	/**
	 * @param schemaErrorList the schemaErrorList to set
	 */
	public void setSchemaErrorList(List<SBMErrorDTO> schemaErrorList) {
		this.schemaErrorList = schemaErrorList;
	}

	/**
	 * @return the sbmPremiums
	 */
	public Map<LocalDate, SBMPremium> getSbmPremiums() {
		if (sbmPremiums == null) {
			sbmPremiums = new LinkedHashMap<LocalDate, SBMPremium>();
		}
		return sbmPremiums;
	}

	/**
	 * @param sbmPremiums the sbmPremiums to set
	 */
	public void setSbmPremiums(Map<LocalDate, SBMPremium> sbmPremiums) {
		this.sbmPremiums = sbmPremiums;
	}

	/**
	 * @return the errorFlag
	 */
	public boolean isErrorFlag() {
		return errorFlag;
	}

	/**
	 * @param errorFlag
	 */
	public void setErrorFlag(boolean errorFlag) {
		this.errorFlag = errorFlag;
	}


	/**
	 * @return the fileStatus
	 */
	public SBMFileStatus getFileStatus() {
		return fileStatus;
	}

	/**
	 * @param fileStatus the fileStatus to set
	 */
	public void setFileStatus(SBMFileStatus fileStatus) {
		this.fileStatus = fileStatus;
	}
	/**
	 * @return the policyStatus
	 */
	public PolicyStatus getPolicyStatus() {
		return policyStatus;
	}

	/**
	 * @param policyStatus the policyStatus to set
	 */
	public void setPolicyStatus(PolicyStatus policyStatus) {
		this.policyStatus = policyStatus;
	}
	
	/**
	 * Time EPS time an inbound SBM file is processed (SBMFileInfo record created).  
	 * Used downstream for TransDateTime in PolicyStatus.
	 * @return the fileProcessDateTime
	 */
	public LocalDateTime getFileProcessDateTime() {
		return fileProcessDateTime;
	}

	/**
	 * Time EPS time an inbound SBM file is processed (SBMFileInfo record created).  
	 * Used downstream for TransDateTime in PolicyStatus.
	 * @param fileProcessDateTime the fileProcessDateTime to set
	 */
	public void setFileProcessDateTime(LocalDateTime fileProcessDateTime) {
		this.fileProcessDateTime = fileProcessDateTime;
	}
	
	
	public String getLogMsg() {

		String msg = "batchId=" + batchId + 
				", sbmFileProcSummaryId=" + sbmFileProcSummaryId + 
				", sbmFileInfoId=" + sbmFileInfoId + 
				", stagingSbmPolicyid=" + stagingSbmPolicyid +
				", ExchangeAssignedPolicyId=";
		if (policy != null) {
			msg += policy.getExchangeAssignedPolicyId();
		} else {
			msg += null;
		}
		msg += ", policyVersionId=" + policyVersionId;
		
		return msg;
	}

}
