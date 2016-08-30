package gov.hhs.cms.ff.fm.eps.ep.po;

import java.time.LocalDate;


/**
 * Class for PolicyMemberPOs that contains common attributes.
 * @author j.radziewski
 * @param <T>
 */
public class GenericPolicyMemberPO<T> {
	
	private Long policyMemberVersionId;
	
	boolean isPolicyMemberChanged = true;
	
	private LocalDate createDateTime;
	private LocalDate lastModifiedDateTime;
	private String createBy;
	private String lastModifiedBy;
	/**
	 * @return the policyMemberVersionId
	 */
	public Long getPolicyMemberVersionId() {
		return policyMemberVersionId;
	}
	/**
	 * @param policyMemberVersionId the policyMemberVersionId to set
	 */
	public void setPolicyMemberVersionId(Long policyMemberVersionId) {
		this.policyMemberVersionId = policyMemberVersionId;
	}
	/**
	 * @return the isPolicyMemberChanged
	 */
	public boolean isPolicyMemberChanged() {
		return isPolicyMemberChanged;
	}
	/**
	 * @param isPolicyMemberChanged the isPolicyMemberChanged to set
	 */
	public void setPolicyMemberChanged(boolean isPolicyMemberChanged) {
		this.isPolicyMemberChanged = isPolicyMemberChanged;
	}
	/**
	 * @return the createDateTime
	 */
	public LocalDate getCreateDateTime() {
		return createDateTime;
	}
	/**
	 * @param createDateTime the createDateTime to set
	 */
	public void setCreateDateTime(LocalDate createDateTime) {
		this.createDateTime = createDateTime;
	}
	/**
	 * @return the lastModifiedDateTime
	 */
	public LocalDate getLastModifiedDateTime() {
		return lastModifiedDateTime;
	}
	/**
	 * @param lastModifiedDateTime the lastModifiedDateTime to set
	 */
	public void setLastModifiedDateTime(LocalDate lastModifiedDateTime) {
		this.lastModifiedDateTime = lastModifiedDateTime;
	}
	/**
	 * @return the createBy
	 */
	public String getCreateBy() {
		return createBy;
	}
	/**
	 * @param createBy the createBy to set
	 */
	public void setCreateBy(String createBy) {
		this.createBy = createBy;
	}
	/**
	 * @return the lastModifiedBy
	 */
	public String getLastModifiedBy() {
		return lastModifiedBy;
	}
	/**
	 * @param lastModifiedBy the lastModifiedBy to set
	 */
	public void setLastModifiedBy(String lastModifiedBy) {
		this.lastModifiedBy = lastModifiedBy;
	}
	
	
}
