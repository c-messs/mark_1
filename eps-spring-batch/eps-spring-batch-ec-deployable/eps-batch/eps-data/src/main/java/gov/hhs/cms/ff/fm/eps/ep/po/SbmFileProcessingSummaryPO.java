package gov.hhs.cms.ff.fm.eps.ep.po;

import java.math.BigDecimal;

/**
 * @author j.radziewski
 * 
 * Entity class for table SBMFILEPROCESSINGSUMMARY
 *
 */
public class SbmFileProcessingSummaryPO {
	
	private Long sbmFileProcSumId;
	private String tenantId;
	private String issuerFileSetId;
	private String issuerId;
	private Integer totalIssuerFileCount;
	private String cmsApprovalRequiredInd;
	private String cmsApprovedInd;
	private Integer totalPreviousPoliciesNotSubmit;
	private Integer notSubmittedEffectuatedCnt;
	private Integer notSubmittedTerminatedCnt;
	private Integer notSubmittedCancelledCnt;
	private Integer totalRecordProcessedCnt;
	private Integer totalRecordRejectedCnt;
	private Integer totalPolicyApprovedCnt;
	private Integer matchingPlcNoChangeCnt;
	private Integer matchingPlcChgApplCnt;
	private Integer matchingPlcCorrectedChgApplCnt;
	private Integer newPlcCreatedAsSentCnt;
	private Integer newPlcCreatedCorrectionApplCnt;
	private Integer effectuatedPolicyCount;
	private BigDecimal errorThresholdPercent;
	private Integer coverageYear;
	private String sbmFileStatusTypeCd;
	
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
	 * @return the totalIssuerFileCount
	 */
	public Integer getTotalIssuerFileCount() {
		return totalIssuerFileCount;
	}
	/**
	 * @param totalIssuerFileCount the totalIssuerFileCount to set
	 */
	public void setTotalIssuerFileCount(Integer totalIssuerFileCount) {
		this.totalIssuerFileCount = totalIssuerFileCount;
	}
	/**
	 * @return the cmsApprovalRequiredInd
	 */
	public String getCmsApprovalRequiredInd() {
		return cmsApprovalRequiredInd;
	}
	/**
	 * @param cmsApprovalRequiredInd the cmsApprovalRequiredInd to set
	 */
	public void setCmsApprovalRequiredInd(String cmsApprovalRequiredInd) {
		this.cmsApprovalRequiredInd = cmsApprovalRequiredInd;
	}
	/**
	 * @return the cmsApprovedInd
	 */
	public String getCmsApprovedInd() {
		return cmsApprovedInd;
	}
	/**
	 * @param cmsApprovedInd the cmsApprovedInd to set
	 */
	public void setCmsApprovedInd(String cmsApprovedInd) {
		this.cmsApprovedInd = cmsApprovedInd;
	}
	/**
	 * @return the totalPreviousPoliciesNotSubmit
	 */
	public Integer getTotalPreviousPoliciesNotSubmit() {
		return totalPreviousPoliciesNotSubmit;
	}
	/**
	 * @param totalPreviousPoliciesNotSubmit the totalPreviousPoliciesNotSubmit to set
	 */
	public void setTotalPreviousPoliciesNotSubmit(Integer totalPreviousPoliciesNotSubmit) {
		this.totalPreviousPoliciesNotSubmit = totalPreviousPoliciesNotSubmit;
	}
	/**
	 * @return the notSubmittedEffectuatedCnt
	 */
	public Integer getNotSubmittedEffectuatedCnt() {
		return notSubmittedEffectuatedCnt;
	}
	/**
	 * @param notSubmittedEffectuatedCnt the notSubmittedEffectuatedCnt to set
	 */
	public void setNotSubmittedEffectuatedCnt(Integer notSubmittedEffectuatedCnt) {
		this.notSubmittedEffectuatedCnt = notSubmittedEffectuatedCnt;
	}
	/**
	 * @return the notSubmittedTerminatedCnt
	 */
	public Integer getNotSubmittedTerminatedCnt() {
		return notSubmittedTerminatedCnt;
	}
	/**
	 * @param notSubmittedTerminatedCnt the notSubmittedTerminatedCnt to set
	 */
	public void setNotSubmittedTerminatedCnt(Integer notSubmittedTerminatedCnt) {
		this.notSubmittedTerminatedCnt = notSubmittedTerminatedCnt;
	}
	/**
	 * @return the notSubmittedCancelledCnt
	 */
	public Integer getNotSubmittedCancelledCnt() {
		return notSubmittedCancelledCnt;
	}
	/**
	 * @param notSubmittedCancelledCnt the notSubmittedCancelledCnt to set
	 */
	public void setNotSubmittedCancelledCnt(Integer notSubmittedCancelledCnt) {
		this.notSubmittedCancelledCnt = notSubmittedCancelledCnt;
	}
	/**
	 * @return the totalRecordProcessedCnt
	 */
	public Integer getTotalRecordProcessedCnt() {
		return totalRecordProcessedCnt;
	}
	/**
	 * @param totalRecordProcessedCnt the totalRecordProcessedCnt to set
	 */
	public void setTotalRecordProcessedCnt(Integer totalRecordProcessedCnt) {
		this.totalRecordProcessedCnt = totalRecordProcessedCnt;
	}
	/**
	 * @return the totalRecordRejectedCnt
	 */
	public Integer getTotalRecordRejectedCnt() {
		return totalRecordRejectedCnt;
	}
	/**
	 * @param totalRecordRejectedCnt the totalRecordRejectedCnt to set
	 */
	public void setTotalRecordRejectedCnt(Integer totalRecordRejectedCnt) {
		this.totalRecordRejectedCnt = totalRecordRejectedCnt;
	}
	/**
	 * @return the totalPolicyApprovedCnt
	 */
	public Integer getTotalPolicyApprovedCnt() {
		return totalPolicyApprovedCnt;
	}
	/**
	 * @param totalPolicyApprovedCnt the totalPolicyApprovedCnt to set
	 */
	public void setTotalPolicyApprovedCnt(Integer totalPolicyApprovedCnt) {
		this.totalPolicyApprovedCnt = totalPolicyApprovedCnt;
	}
	/**
	 * @return the matchingPlcNoChangeCnt
	 */
	public Integer getMatchingPlcNoChangeCnt() {
		return matchingPlcNoChangeCnt;
	}
	/**
	 * @param matchingPlcNoChangeCnt the matchingPlcNoChangeCnt to set
	 */
	public void setMatchingPlcNoChangeCnt(Integer matchingPlcNoChangeCnt) {
		this.matchingPlcNoChangeCnt = matchingPlcNoChangeCnt;
	}
	/**
	 * @return the matchingPlcChgApplCnt
	 */
	public Integer getMatchingPlcChgApplCnt() {
		return matchingPlcChgApplCnt;
	}
	/**
	 * @param matchingPlcChgApplCnt the matchingPlcChgApplCnt to set
	 */
	public void setMatchingPlcChgApplCnt(Integer matchingPlcChgApplCnt) {
		this.matchingPlcChgApplCnt = matchingPlcChgApplCnt;
	}
	/**
	 * @return the matchingPlcCorrectedChgApplCnt
	 */
	public Integer getMatchingPlcCorrectedChgApplCnt() {
		return matchingPlcCorrectedChgApplCnt;
	}
	/**
	 * @param matchingPlcCorrectedChgApplCnt the matchingPlcCorrectedChgApplCnt to set
	 */
	public void setMatchingPlcCorrectedChgApplCnt(Integer matchingPlcCorrectedChgApplCnt) {
		this.matchingPlcCorrectedChgApplCnt = matchingPlcCorrectedChgApplCnt;
	}
	/**
	 * @return the newPlcCreatedAsSentCnt
	 */
	public Integer getNewPlcCreatedAsSentCnt() {
		return newPlcCreatedAsSentCnt;
	}
	/**
	 * @param newPlcCreatedAsSentCnt the newPlcCreatedAsSentCnt to set
	 */
	public void setNewPlcCreatedAsSentCnt(Integer newPlcCreatedAsSentCnt) {
		this.newPlcCreatedAsSentCnt = newPlcCreatedAsSentCnt;
	}
	/**
	 * @return the newPlcCreatedCorrectionApplCnt
	 */
	public Integer getNewPlcCreatedCorrectionApplCnt() {
		return newPlcCreatedCorrectionApplCnt;
	}
	/**
	 * @param newPlcCreatedCorrectionApplCnt the newPlcCreatedCorrectionApplCnt to set
	 */
	public void setNewPlcCreatedCorrectionApplCnt(Integer newPlcCreatedCorrectionApplCnt) {
		this.newPlcCreatedCorrectionApplCnt = newPlcCreatedCorrectionApplCnt;
	}
	/**
	 * @return the effectuatedPolicyCount
	 */
	public Integer getEffectuatedPolicyCount() {
		return effectuatedPolicyCount;
	}
	/**
	 * @param effectuatedPolicyCount the effectuatedPolicyCount to set
	 */
	public void setEffectuatedPolicyCount(Integer effectuatedPolicyCount) {
		this.effectuatedPolicyCount = effectuatedPolicyCount;
	}
	/**
	 * @return the errorThresholdPercent
	 */
	public BigDecimal getErrorThresholdPercent() {
		return errorThresholdPercent;
	}
	/**
	 * @param errorThresholdPercent the errorThresholdPercent to set
	 */
	public void setErrorThresholdPercent(BigDecimal errorThresholdPercent) {
		this.errorThresholdPercent = errorThresholdPercent;
	}
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
	 * @return the sbmFileStatusTypeCd
	 */
	public String getSbmFileStatusTypeCd() {
		return sbmFileStatusTypeCd;
	}
	/**
	 * @param sbmFileStatusTypeCd the sbmFileStatusTypeCd to set
	 */
	public void setSbmFileStatusTypeCd(String sbmFileStatusTypeCd) {
		this.sbmFileStatusTypeCd = sbmFileStatusTypeCd;
	}
	
	
	

}
