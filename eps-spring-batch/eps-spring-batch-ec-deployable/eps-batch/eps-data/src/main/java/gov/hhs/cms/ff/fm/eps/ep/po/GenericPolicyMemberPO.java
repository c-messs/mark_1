package gov.hhs.cms.ff.fm.eps.ep.po;

import org.joda.time.DateTime;


/**
 * Class for PolicyMemberPOs that contains common attributes.
 * @author j.radziewski
 * @param <T>
 */
public class GenericPolicyMemberPO<T> {
	
	private Long policyMemberVersionId;
	
	boolean isPolicyMemberChanged = true;
	
	private DateTime createDateTime;
	private DateTime lastModifiedDateTime;
	private String createBy;
	private String lastModifiedBy;
	
	public Long getPolicyMemberVersionId() {
		return policyMemberVersionId;
	}

	public void setPolicyMemberVersionId(Long policyMemberVersionId) {
		this.policyMemberVersionId = policyMemberVersionId;
	}
	
	public boolean isPolicyMemberChanged() {
		return isPolicyMemberChanged;
	}

	public void setPolicyMemberChanged(boolean isPolicyMemberChanged) {
		this.isPolicyMemberChanged = isPolicyMemberChanged;
	}

	public DateTime getCreateDateTime() {
		return createDateTime;
	}
	
	public void setCreateDateTime(DateTime createDateTime) {
		this.createDateTime = createDateTime;
	}
	
	public DateTime getLastModifiedDateTime() {
		return lastModifiedDateTime;
	}
	
	public void setLastModifiedDateTime(DateTime lastModifiedDateTime) {
		this.lastModifiedDateTime = lastModifiedDateTime;
	}
	
	public String getCreateBy() {
		return createBy;
	}
	
	public void setCreateBy(String createBy) {
		this.createBy = createBy;
	}
	
	public String getLastModifiedBy() {
		return lastModifiedBy;
	}
	
	public void setLastModifiedBy(String lastModifiedBy) {
		this.lastModifiedBy = lastModifiedBy;
	}

}
