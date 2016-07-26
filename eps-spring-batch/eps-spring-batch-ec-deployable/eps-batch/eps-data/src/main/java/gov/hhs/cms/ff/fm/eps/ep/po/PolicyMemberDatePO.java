package gov.hhs.cms.ff.fm.eps.ep.po;

import org.joda.time.DateTime;

/**
 * @author eps
 *
 */
public class PolicyMemberDatePO extends GenericPolicyMemberPO<PolicyMemberDatePO> {
	
	//Attributes included in Hashcode and Equals.
	private DateTime policyMemberStartDate;
	private DateTime policyMemberEndDate;
	
	/**
	 * @return the policyMemberStartDate
	 */
	public DateTime getPolicyMemberStartDate() {
		return policyMemberStartDate;
	}
	/**
	 * @param policyMemberStartDate the policyMemberStartDate to set
	 */
	public void setPolicyMemberStartDate(DateTime policyMemberStartDate) {
		this.policyMemberStartDate = policyMemberStartDate;
	}
	/**
	 * @return the policyMemberEndDate
	 */
	public DateTime getPolicyMemberEndDate() {
		return policyMemberEndDate;
	}
	/**
	 * @param policyMemberEndDate the policyMemberEndDate to set
	 */
	public void setPolicyMemberEndDate(DateTime policyMemberEndDate) {
		this.policyMemberEndDate = policyMemberEndDate;
	}
	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime
				* result
				+ ((policyMemberEndDate == null) ? 0 : policyMemberEndDate
						.hashCode());
		result = prime
				* result
				+ ((policyMemberStartDate == null) ? 0 : policyMemberStartDate
						.hashCode());
		return result;
	}
	
	
	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		PolicyMemberDatePO other = (PolicyMemberDatePO) obj;
		if (policyMemberEndDate == null) {
			if (other.policyMemberEndDate != null)
				return false;
		} else if (!policyMemberEndDate.equals(other.policyMemberEndDate))
			return false;
		if (policyMemberStartDate == null) {
			if (other.policyMemberStartDate != null)
				return false;
		} else if (!policyMemberStartDate.equals(other.policyMemberStartDate))
			return false;
		return true;
	}
	
	

}
