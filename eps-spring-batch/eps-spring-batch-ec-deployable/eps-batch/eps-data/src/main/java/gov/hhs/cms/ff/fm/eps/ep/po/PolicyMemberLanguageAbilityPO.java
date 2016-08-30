package gov.hhs.cms.ff.fm.eps.ep.po;

/**
 * @author eps
 *
 */
public class PolicyMemberLanguageAbilityPO extends GenericPolicyMemberPO<PolicyMemberLanguageAbilityPO> {

	//Attributes included in Hashcode and Equals.
	private String x12LanguageTypeCd;
	private String x12LanguageModeTypeCd;
	
	/**
	 * @return the x12LanguageTypeCd
	 */
	public String getX12LanguageTypeCd() {
		return x12LanguageTypeCd;
	}
	
	/**
	 * @param x12LanguageTypeCd the x12LanguageTypeCd to set
	 */
	public void setX12LanguageTypeCd(String x12LanguageTypeCd) {
		this.x12LanguageTypeCd = x12LanguageTypeCd;
	}
	
	/**
	 * @return the x12LanguageModeTypeCd
	 */
	public String getX12LanguageModeTypeCd() {
		return x12LanguageModeTypeCd;
	}
	
	/**
	 * @param x12LanguageModeTypeCd the x12LanguageModeTypeCd to set
	 */
	public void setX12LanguageModeTypeCd(String x12LanguageModeTypeCd) {
		this.x12LanguageModeTypeCd = x12LanguageModeTypeCd;
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
				+ ((x12LanguageModeTypeCd == null) ? 0 : x12LanguageModeTypeCd
						.hashCode());
		result = prime
				* result
				+ ((x12LanguageTypeCd == null) ? 0 : x12LanguageTypeCd
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
		PolicyMemberLanguageAbilityPO other = (PolicyMemberLanguageAbilityPO) obj;
		if (x12LanguageModeTypeCd == null) {
			if (other.x12LanguageModeTypeCd != null)
				return false;
		} else if (!x12LanguageModeTypeCd.equals(other.x12LanguageModeTypeCd))
			return false;
		if (x12LanguageTypeCd == null) {
			if (other.x12LanguageTypeCd != null)
				return false;
		} else if (!x12LanguageTypeCd.equals(other.x12LanguageTypeCd))
			return false;
		return true;
	}
	
	
	

	
	
}
