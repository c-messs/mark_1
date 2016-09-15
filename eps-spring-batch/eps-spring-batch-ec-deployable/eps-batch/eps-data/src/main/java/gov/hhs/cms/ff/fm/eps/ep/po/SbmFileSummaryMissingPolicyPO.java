package gov.hhs.cms.ff.fm.eps.ep.po;

/**
 * @author j.radziewski
 * 
 * Entity class for table SBMFILESUMMARYMISSINGPOLICY.
 *
 */
public class SbmFileSummaryMissingPolicyPO {
	
	private Long sbmFileProcSumId;
	private Long missingPolicyVersionId;
	private Long missingPolicyMemberVersionId;
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
	 * @return the missingPolicyVersionId
	 */
	public Long getMissingPolicyVersionId() {
		return missingPolicyVersionId;
	}
	/**
	 * @param missingPolicyVersionId the missingPolicyVersionId to set
	 */
	public void setMissingPolicyVersionId(Long missingPolicyVersionId) {
		this.missingPolicyVersionId = missingPolicyVersionId;
	}
	/**
	 * @return the missingPolicyMemberVersionId
	 */
	public Long getMissingPolicyMemberVersionId() {
		return missingPolicyMemberVersionId;
	}
	/**
	 * @param missingPolicyMemberVersionId the missingPolicyMemberVersionId to set
	 */
	public void setMissingPolicyMemberVersionId(Long missingPolicyMemberVersionId) {
		this.missingPolicyMemberVersionId = missingPolicyMemberVersionId;
	}

}
