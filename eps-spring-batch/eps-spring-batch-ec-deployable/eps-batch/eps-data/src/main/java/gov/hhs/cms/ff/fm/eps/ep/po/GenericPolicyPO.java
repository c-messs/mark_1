package gov.hhs.cms.ff.fm.eps.ep.po;

import java.time.LocalDate;

/**
 * Class for PolicyPOs that contains common attributes.
 * @author j.radziewski
 
 * @param <T>
 */
public class GenericPolicyPO<T> {
	
	private Long policyVersionId;
	boolean isPolicyChanged = true;
	
	private LocalDate createDateTime;
	private LocalDate lastModifiedDateTime;
	private String createBy;
	private String lastModifiedBy;
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
	 * @return the isPolicyChanged
	 */
	public boolean isPolicyChanged() {
		return isPolicyChanged;
	}
	/**
	 * @param isPolicyChanged the isPolicyChanged to set
	 */
	public void setPolicyChanged(boolean isPolicyChanged) {
		this.isPolicyChanged = isPolicyChanged;
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
