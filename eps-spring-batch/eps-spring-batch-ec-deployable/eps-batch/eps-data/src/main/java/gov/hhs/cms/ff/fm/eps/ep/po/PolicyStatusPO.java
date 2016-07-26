package gov.hhs.cms.ff.fm.eps.ep.po;

import org.joda.time.DateTime;



/**
 * @author eps
 *
 */
public class PolicyStatusPO extends GenericPolicyPO<PolicyStatusPO> {

	//Excluded from HashCode and Equals
	private DateTime transDateTime;

	//Attributes included in HashCode and Equals.
	private String insuranacePolicyStatusTypeCd;


	/**
	 * @return the transDateTime
	 */
	public DateTime getTransDateTime() {
		return transDateTime;
	}
	/**
	 * @param transDateTime the transDateTime to set
	 */
	public void setTransDateTime(DateTime transDateTime) {
		this.transDateTime = transDateTime;
	}	
	public String getInsuranacePolicyStatusTypeCd() {
		return insuranacePolicyStatusTypeCd;
	}
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