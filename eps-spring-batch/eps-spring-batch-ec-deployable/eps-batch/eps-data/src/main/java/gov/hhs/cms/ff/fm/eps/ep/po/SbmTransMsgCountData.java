package gov.hhs.cms.ff.fm.eps.ep.po;

import gov.hhs.cms.ff.fm.eps.ep.enums.SbmTransMsgStatus;

/**
 * @author j.radziewski
 * 
 * Data side domain object for processing outbound SBMFILESUMMARYMISSINGPOLICY data (new and match counts) joined
 * from SBMTRANSMSG and POLICYVERSION.
 *
 */
public class SbmTransMsgCountData {
	
	private SbmTransMsgStatus status;
	private boolean hasPolicyVersionId;
	private boolean hasPriorPolicyVersionId;
	private Integer countStatus;
	/**
	 * @return the status
	 */
	public SbmTransMsgStatus getStatus() {
		return status;
	}
	/**
	 * @param status the status to set
	 */
	public void setStatus(SbmTransMsgStatus status) {
		this.status = status;
	}
	/**
	 * @return the hasPolicyVersionId
	 */
	public boolean isHasPolicyVersionId() {
		return hasPolicyVersionId;
	}
	/**
	 * @param hasPolicyVersionId the hasPolicyVersionId to set
	 */
	public void setHasPolicyVersionId(boolean hasPolicyVersionId) {
		this.hasPolicyVersionId = hasPolicyVersionId;
	}
	/**
	 * @return the hasPriorPolicyVersionId
	 */
	public boolean isHasPriorPolicyVersionId() {
		return hasPriorPolicyVersionId;
	}
	/**
	 * @param hasPriorPolicyVersionId the hasPriorPolicyVersionId to set
	 */
	public void setHasPriorPolicyVersionId(boolean hasPriorPolicyVersionId) {
		this.hasPriorPolicyVersionId = hasPriorPolicyVersionId;
	}
	/**
	 * @return the countStatus
	 */
	public Integer getCountStatus() {
		return countStatus;
	}
	/**
	 * @param countStatus the countStatus to set
	 */
	public void setCountStatus(Integer countStatus) {
		this.countStatus = countStatus;
	}
	
	

}
