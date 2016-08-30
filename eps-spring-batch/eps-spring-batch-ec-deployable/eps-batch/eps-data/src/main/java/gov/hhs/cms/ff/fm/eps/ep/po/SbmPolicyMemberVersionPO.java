package gov.hhs.cms.ff.fm.eps.ep.po;

import java.time.LocalDate;

public class SbmPolicyMemberVersionPO extends PolicyMemberVersionPO {


	//Excluded from Hashcode and Equals
	private Long priorPolicyMemberVersionId;
	private Long sbmTransMsgID;

	//Attributes included in Hashcode and Equals.
	private LocalDate policyMemberDeathDate;
	private String x12TobaccoUseTypeCode;
	private String incorrectGenderTypeCode;
	private String nonCoveredSubscriberInd; // TODO Temporarily removed from hashCode/equals until functional clarification.
	private String x12LanguageCode;
	private String x12LanguageQualifierTypeCd;
	private String x12RaceEthnicityTypeCode;
	private String zipPlus4Cd;
	
	
	/**
	 * @return the priorPolicyMemberVersionId
	 */
	public Long getPriorPolicyMemberVersionId() {
		return priorPolicyMemberVersionId;
	}
	/**
	 * @param priorPolicyMemberVersionId the priorPolicyMemberVersionId to set
	 */
	public void setPriorPolicyMemberVersionId(Long priorPolicyMemberVersionId) {
		this.priorPolicyMemberVersionId = priorPolicyMemberVersionId;
	}
	/**
	 * @return the sbmTransMsgID
	 */
	public Long getSbmTransMsgID() {
		return sbmTransMsgID;
	}
	/**
	 * @param sbmTransMsgID the sbmTransMsgID to set
	 */
	public void setSbmTransMsgID(Long sbmTransMsgID) {
		this.sbmTransMsgID = sbmTransMsgID;
	}
	/**
	 * @return the policyMemberDeathDate
	 */
	public LocalDate getPolicyMemberDeathDate() {
		return policyMemberDeathDate;
	}
	/**
	 * @param policyMemberDeathDate the policyMemberDeathDate to set
	 */
	public void setPolicyMemberDeathDate(LocalDate policyMemberDeathDate) {
		this.policyMemberDeathDate = policyMemberDeathDate;
	}
	/**
	 * @return the x12TobaccoUseTypeCode
	 */
	public String getX12TobaccoUseTypeCode() {
		return x12TobaccoUseTypeCode;
	}
	/**
	 * @param x12TobaccoUseTypeCode the x12TobaccoUseTypeCode to set
	 */
	public void setX12TobaccoUseTypeCode(String x12TobaccoUseTypeCode) {
		this.x12TobaccoUseTypeCode = x12TobaccoUseTypeCode;
	}
	/**
	 * @return the incorrectGenderTypeCode
	 */
	public String getIncorrectGenderTypeCode() {
		return incorrectGenderTypeCode;
	}
	/**
	 * @param incorrectGenderTypeCode the incorrectGenderTypeCode to set
	 */
	public void setIncorrectGenderTypeCode(String incorrectGenderTypeCode) {
		this.incorrectGenderTypeCode = incorrectGenderTypeCode;
	}
	/**
	 * @return the nonCoveredSubscriberInd
	 */
	public String getNonCoveredSubscriberInd() {
		return nonCoveredSubscriberInd;
	}
	/**
	 * @param nonCoveredSubscriberInd the nonCoveredSubscriberInd to set
	 */
	public void setNonCoveredSubscriberInd(String nonCoveredSubscriberInd) {
		this.nonCoveredSubscriberInd = nonCoveredSubscriberInd;
	}
	/**
	 * @return the x12LanguageCode
	 */
	public String getX12LanguageCode() {
		return x12LanguageCode;
	}
	/**
	 * @param x12LanguageCode the x12LanguageCode to set
	 */
	public void setX12LanguageCode(String x12LanguageCode) {
		this.x12LanguageCode = x12LanguageCode;
	}
	/**
	 * @return the x12LanguageQualifierTypeCd
	 */
	public String getX12LanguageQualifierTypeCd() {
		return x12LanguageQualifierTypeCd;
	}
	/**
	 * @param x12LanguageQualifierTypeCd the x12LanguageQualifierTypeCd to set
	 */
	public void setX12LanguageQualifierTypeCd(String x12LanguageQualifierTypeCd) {
		this.x12LanguageQualifierTypeCd = x12LanguageQualifierTypeCd;
	}
	/**
	 * @return the x12RaceEthnicityTypeCode
	 */
	public String getX12RaceEthnicityTypeCode() {
		return x12RaceEthnicityTypeCode;
	}
	/**
	 * @param x12RaceEthnicityTypeCode the x12RaceEthnicityTypeCode to set
	 */
	public void setX12RaceEthnicityTypeCode(String x12RaceEthnicityTypeCode) {
		this.x12RaceEthnicityTypeCode = x12RaceEthnicityTypeCode;
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
	
	
	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((incorrectGenderTypeCode == null) ? 0 : incorrectGenderTypeCode.hashCode());
		// TODO result = prime * result + ((nonCoveredSubscriberInd == null) ? 0 : nonCoveredSubscriberInd.hashCode());
		result = prime * result + ((policyMemberDeathDate == null) ? 0 : policyMemberDeathDate.hashCode());
		result = prime * result + ((x12LanguageCode == null) ? 0 : x12LanguageCode.hashCode());
		result = prime * result + ((x12LanguageQualifierTypeCd == null) ? 0 : x12LanguageQualifierTypeCd.hashCode());
		result = prime * result + ((x12RaceEthnicityTypeCode == null) ? 0 : x12RaceEthnicityTypeCode.hashCode());
		result = prime * result + ((x12TobaccoUseTypeCode == null) ? 0 : x12TobaccoUseTypeCode.hashCode());
		result = prime * result + ((zipPlus4Cd == null) ? 0 : zipPlus4Cd.hashCode());
		return result;
	}
	
	
	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		SbmPolicyMemberVersionPO other = (SbmPolicyMemberVersionPO) obj;
		if (incorrectGenderTypeCode == null) {
			if (other.incorrectGenderTypeCode != null)
				return false;
		} else if (!incorrectGenderTypeCode.equals(other.incorrectGenderTypeCode))
			return false;
//	TODO	if (nonCoveredSubscriberInd == null) {
//			if (other.nonCoveredSubscriberInd != null)
//				return false;
//		} else if (!nonCoveredSubscriberInd.equals(other.nonCoveredSubscriberInd))
//			return false;
		if (policyMemberDeathDate == null) {
			if (other.policyMemberDeathDate != null)
				return false;
		} else if (!policyMemberDeathDate.equals(other.policyMemberDeathDate))
			return false;
		if (x12LanguageCode == null) {
			if (other.x12LanguageCode != null)
				return false;
		} else if (!x12LanguageCode.equals(other.x12LanguageCode))
			return false;
		if (x12LanguageQualifierTypeCd == null) {
			if (other.x12LanguageQualifierTypeCd != null)
				return false;
		} else if (!x12LanguageQualifierTypeCd.equals(other.x12LanguageQualifierTypeCd))
			return false;
		if (x12RaceEthnicityTypeCode == null) {
			if (other.x12RaceEthnicityTypeCode != null)
				return false;
		} else if (!x12RaceEthnicityTypeCode.equals(other.x12RaceEthnicityTypeCode))
			return false;
		if (x12TobaccoUseTypeCode == null) {
			if (other.x12TobaccoUseTypeCode != null)
				return false;
		} else if (!x12TobaccoUseTypeCode.equals(other.x12TobaccoUseTypeCode))
			return false;
		if (zipPlus4Cd == null) {
			if (other.zipPlus4Cd != null)
				return false;
		} else if (!zipPlus4Cd.equals(other.zipPlus4Cd))
			return false;
		return true;
	}
	
	
	
	
}
