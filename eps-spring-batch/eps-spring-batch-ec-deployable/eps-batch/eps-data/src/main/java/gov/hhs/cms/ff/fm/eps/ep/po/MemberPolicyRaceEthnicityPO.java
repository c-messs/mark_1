package gov.hhs.cms.ff.fm.eps.ep.po;


/**
 * @author eps
 *
 */
public class MemberPolicyRaceEthnicityPO extends GenericPolicyMemberPO<MemberPolicyRaceEthnicityPO> {
	
	//Attributes included in Hashcode and Equals.
	private String x12raceEthnicityTypeCd;
	
	public String getX12raceEthnicityTypeCd() {
		return x12raceEthnicityTypeCd;
	}
	
	public void setX12raceEthnicityTypeCd(String x12raceEthnicityTypeCd) {
		this.x12raceEthnicityTypeCd = x12raceEthnicityTypeCd;
	}	
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime
				* result
				+ ((x12raceEthnicityTypeCd == null) ? 0
						: x12raceEthnicityTypeCd.hashCode());
		return result;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		MemberPolicyRaceEthnicityPO other = (MemberPolicyRaceEthnicityPO) obj;
		if (x12raceEthnicityTypeCd == null) {
			if (other.x12raceEthnicityTypeCd != null)
				return false;
		} else if (!x12raceEthnicityTypeCd.equals(other.x12raceEthnicityTypeCd))
			return false;
		return true;
	}
	
	
}