package gov.hhs.cms.ff.fm.eps.ep.po;

import java.time.LocalDateTime;



/**
 * @author eps
 *
 */
public class PolicyStatusPO extends GenericPolicyPO<PolicyStatusPO> {

	//Excluded from HashCode and Equals
	private LocalDateTime transDateTime;

	//Attributes included in HashCode and Equals.
	private String insuranacePolicyStatusTypeCd;

	/**
	 * @return the transDateTime
	 */
	public LocalDateTime getTransDateTime() {
		return transDateTime;
	}

	/**
	 * @param transDateTime the transDateTime to set
	 */
	public void setTransDateTime(LocalDateTime transDateTime) {
		this.transDateTime = transDateTime;
	}

	/**
	 * @return the insuranacePolicyStatusTypeCd
	 */
	public String getInsuranacePolicyStatusTypeCd() {
		return insuranacePolicyStatusTypeCd;
	}

	/**
	 * @param insuranacePolicyStatusTypeCd the insuranacePolicyStatusTypeCd to set
	 */
	public void setInsuranacePolicyStatusTypeCd(String insuranacePolicyStatusTypeCd) {
		this.insuranacePolicyStatusTypeCd = insuranacePolicyStatusTypeCd;
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
				+ ((insuranacePolicyStatusTypeCd == null) ? 0
						: insuranacePolicyStatusTypeCd.hashCode());
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
		PolicyStatusPO other = (PolicyStatusPO) obj;
		if (insuranacePolicyStatusTypeCd == null) {
			if (other.insuranacePolicyStatusTypeCd != null)
				return false;
		} else if (!insuranacePolicyStatusTypeCd
				.equals(other.insuranacePolicyStatusTypeCd))
			return false;
		return true;
	}	

}