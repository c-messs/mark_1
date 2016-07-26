package gov.hhs.cms.ff.fm.eps.ep.po;

import org.joda.time.DateTime;

/**
 * Class for PolicyPOs that contains common attributes.
 * @author j.radziewski
 
 * @param <T>
 */
public class GenericPolicyPO<T> {
	
	private Long policyVersionId;
	
	private DateTime createDateTime;
	private DateTime lastModifiedDateTime;
	private String createBy;
	private String lastModifiedBy;
	
	public Long getPolicyVersionId() {
		return policyVersionId;
	}
	public void setPolicyVersionId(Long policyVersionId) {
		this.policyVersionId = policyVersionId;
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
