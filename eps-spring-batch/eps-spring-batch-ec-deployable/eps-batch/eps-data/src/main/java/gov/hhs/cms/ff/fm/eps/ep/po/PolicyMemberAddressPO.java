package gov.hhs.cms.ff.fm.eps.ep.po;



/**
 * @author eps
 *
 */
public class PolicyMemberAddressPO extends GenericPolicyMemberPO<PolicyMemberAddressPO> {
	
	//Attributes included in HashCode and Equals.
	private String stateCd;
	private String zipPlus4Cd;
	private String x12addressTypeCd;
	/**
	 * @return the stateCd
	 */
	public String getStateCd() {
		return stateCd;
	}
	/**
	 * @param stateCd the stateCd to set
	 */
	public void setStateCd(String stateCd) {
		this.stateCd = stateCd;
	}
	/**
	 * @return the zipPlus4Cd
	 */
	public String getZipPlus4Cd() {
		return zipPlus4Cd;
	}
	/**
	 * @param zipPlus4Cd the zipPlus4Cd to set
	 */
	public void setZipPlus4Cd(String zipPlus4Cd) {
		this.zipPlus4Cd = zipPlus4Cd;
	}
	/**
	 * @return the x12addressTypeCd
	 */
	public String getX12addressTypeCd() {
		return x12addressTypeCd;
	}
	/**
	 * @param x12addressTypeCd the x12addressTypeCd to set
	 */
	public void setX12addressTypeCd(String x12addressTypeCd) {
		this.x12addressTypeCd = x12addressTypeCd;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((stateCd == null) ? 0 : stateCd.hashCode());
		result = prime
				* result
				+ ((x12addressTypeCd == null) ? 0 : x12addressTypeCd.hashCode());
		result = prime * result
				+ ((zipPlus4Cd == null) ? 0 : zipPlus4Cd.hashCode());
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
		PolicyMemberAddressPO other = (PolicyMemberAddressPO) obj;
		if (stateCd == null) {
			if (other.stateCd != null)
				return false;
		} else if (!stateCd.equals(other.stateCd))
			return false;
		if (x12addressTypeCd == null) {
			if (other.x12addressTypeCd != null)
				return false;
		} else if (!x12addressTypeCd.equals(other.x12addressTypeCd))
			return false;
		if (zipPlus4Cd == null) {
			if (other.zipPlus4Cd != null)
				return false;
		} else if (!zipPlus4Cd.equals(other.zipPlus4Cd))
			return false;
		return true;
	}
	
	
	
}