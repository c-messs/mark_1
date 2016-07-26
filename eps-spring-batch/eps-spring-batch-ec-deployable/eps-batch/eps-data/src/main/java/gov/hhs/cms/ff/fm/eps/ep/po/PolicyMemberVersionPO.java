package gov.hhs.cms.ff.fm.eps.ep.po;

import org.joda.time.DateTime;

/**
 * @author eps
 *
 */
public class PolicyMemberVersionPO extends GenericPolicyMemberPO<PolicyMemberVersionPO> {

	//Excluded from Hashode and Equals
	private Long transMsgID;
	private DateTime maintenanceStartDateTime;
	private DateTime maintenanceEndDateTime;
	
	// Excluded from hashCode and equals
	// Extracted from PolicyInfoType and not member loop
	private String exchangePolicyId;
	// Exracted from subscriber
	private String subscriberStateCd;

	//Attributes included in Hashcode and Equals.
	private String exchangeMemberID;
	private String issuerAssignedMemberID;
	private DateTime policyMemberEligStartDate;
	private DateTime policyMemberEligEndDate;
	private String subscriberInd;
	private DateTime policyMemberBirthDate;
	private String policyMemberLastNm;
	private String policyMemberFirstNm;
	private String policyMemberMiddleNm;
	private String policyMemberSalutationNm;
	private String policyMemberSuffixNm;
	private String policyMemberSSN;
	private String x12GenderTypeCd;


	/**
	 * @return the transMsgID
	 */
	public Long getTransMsgID() {
		return transMsgID;
	}

	/**
	 * @param transMsgID the transMsgID to set
	 */
	public void setTransMsgID(Long transMsgID) {
		this.transMsgID = transMsgID;
	}

	/**
	 * @return the maintenanceStartDateTime
	 */
	public DateTime getMaintenanceStartDateTime() {
		return maintenanceStartDateTime;
	}

	/**
	 * @param maintenanceStartDateTime the maintenanceStartDateTime to set
	 */
	public void setMaintenanceStartDateTime(DateTime maintenanceStartDateTime) {
		this.maintenanceStartDateTime = maintenanceStartDateTime;
	}

	/**
	 * @return the maintenanceEndDateTime
	 */
	public DateTime getMaintenanceEndDateTime() {
		return maintenanceEndDateTime;
	}

	/**
	 * @param maintenanceEndDateTime the maintenanceEndDateTime to set
	 */
	public void setMaintenanceEndDateTime(DateTime maintenanceEndDateTime) {
		this.maintenanceEndDateTime = maintenanceEndDateTime;
	}

	/**
	 * @return the exchangePolicyId
	 */
	public String getExchangePolicyId() {
		return exchangePolicyId;
	}

	/**
	 * @param exchangePolicyId the exchangePolicyId to set
	 */
	public void setExchangePolicyId(String exchangePolicyId) {
		this.exchangePolicyId = exchangePolicyId;
	}

	/**
	 * @return the subscriberStateCd
	 */
	public String getSubscriberStateCd() {
		return subscriberStateCd;
	}

	/**
	 * @param subscriberStateCd the subscriberStateCd to set
	 */
	public void setSubscriberStateCd(String subscriberStateCd) {
		this.subscriberStateCd = subscriberStateCd;
	}

	/**
	 * @return the exchangeMemberID
	 */
	public String getExchangeMemberID() {
		return exchangeMemberID;
	}

	/**
	 * @param exchangeMemberID the exchangeMemberID to set
	 */
	public void setExchangeMemberID(String exchangeMemberID) {
		this.exchangeMemberID = exchangeMemberID;
	}

	/**
	 * @return the issuerAssignedMemberID
	 */
	public String getIssuerAssignedMemberID() {
		return issuerAssignedMemberID;
	}

	/**
	 * @param issuerAssignedMemberID the issuerAssignedMemberID to set
	 */
	public void setIssuerAssignedMemberID(String issuerAssignedMemberID) {
		this.issuerAssignedMemberID = issuerAssignedMemberID;
	}

	/**
	 * @return the policyMemberEligStartDate
	 */
	public DateTime getPolicyMemberEligStartDate() {
		return policyMemberEligStartDate;
	}

	/**
	 * @param policyMemberEligStartDate the policyMemberEligStartDate to set
	 */
	public void setPolicyMemberEligStartDate(DateTime policyMemberEligStartDate) {
		this.policyMemberEligStartDate = policyMemberEligStartDate;
	}

	/**
	 * @return the policyMemberEligEndDate
	 */
	public DateTime getPolicyMemberEligEndDate() {
		return policyMemberEligEndDate;
	}

	/**
	 * @param policyMemberEligEndDate the policyMemberEligEndDate to set
	 */
	public void setPolicyMemberEligEndDate(DateTime policyMemberEligEndDate) {
		this.policyMemberEligEndDate = policyMemberEligEndDate;
	}

	/**
	 * @return the subscriberInd
	 */
	public String getSubscriberInd() {
		return subscriberInd;
	}

	/**
	 * @param subscriberInd the subscriberInd to set
	 */
	public void setSubscriberInd(String subscriberInd) {
		this.subscriberInd = subscriberInd;
	}

	/**
	 * @return the policyMemberBirthDate
	 */
	public DateTime getPolicyMemberBirthDate() {
		return policyMemberBirthDate;
	}

	/**
	 * @param policyMemberBirthDate the policyMemberBirthDate to set
	 */
	public void setPolicyMemberBirthDate(DateTime policyMemberBirthDate) {
		this.policyMemberBirthDate = policyMemberBirthDate;
	}

	/**
	 * @return the policyMemberLastNm
	 */
	public String getPolicyMemberLastNm() {
		return policyMemberLastNm;
	}

	/**
	 * @param policyMemberLastNm the policyMemberLastNm to set
	 */
	public void setPolicyMemberLastNm(String policyMemberLastNm) {
		this.policyMemberLastNm = policyMemberLastNm;
	}

	/**
	 * @return the policyMemberFirstNm
	 */
	public String getPolicyMemberFirstNm() {
		return policyMemberFirstNm;
	}

	/**
	 * @param policyMemberFirstNm the policyMemberFirstNm to set
	 */
	public void setPolicyMemberFirstNm(String policyMemberFirstNm) {
		this.policyMemberFirstNm = policyMemberFirstNm;
	}

	/**
	 * @return the policyMemberMiddleNm
	 */
	public String getPolicyMemberMiddleNm() {
		return policyMemberMiddleNm;
	}

	/**
	 * @param policyMemberMiddleNm the policyMemberMiddleNm to set
	 */
	public void setPolicyMemberMiddleNm(String policyMemberMiddleNm) {
		this.policyMemberMiddleNm = policyMemberMiddleNm;
	}

	/**
	 * @return the policyMemberSalutationNm
	 */
	public String getPolicyMemberSalutationNm() {
		return policyMemberSalutationNm;
	}

	/**
	 * @param policyMemberSalutationNm the policyMemberSalutationNm to set
	 */
	public void setPolicyMemberSalutationNm(String policyMemberSalutationNm) {
		this.policyMemberSalutationNm = policyMemberSalutationNm;
	}

	/**
	 * @return the policyMemberSuffixNm
	 */
	public String getPolicyMemberSuffixNm() {
		return policyMemberSuffixNm;
	}

	/**
	 * @param policyMemberSuffixNm the policyMemberSuffixNm to set
	 */
	public void setPolicyMemberSuffixNm(String policyMemberSuffixNm) {
		this.policyMemberSuffixNm = policyMemberSuffixNm;
	}

	/**
	 * @return the policyMemberSSN
	 */
	public String getPolicyMemberSSN() {
		return policyMemberSSN;
	}

	/**
	 * @param policyMemberSSN the policyMemberSSN to set
	 */
	public void setPolicyMemberSSN(String policyMemberSSN) {
		this.policyMemberSSN = policyMemberSSN;
	}

	/**
	 * @return the x12GenderTypeCd
	 */
	public String getX12GenderTypeCd() {
		return x12GenderTypeCd;
	}

	/**
	 * @param x12GenderTypeCd the x12GenderTypeCd to set
	 */
	public void setX12GenderTypeCd(String x12GenderTypeCd) {
		this.x12GenderTypeCd = x12GenderTypeCd;
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
				+ ((exchangeMemberID == null) ? 0 : exchangeMemberID.hashCode());
		result = prime
				* result
				+ ((issuerAssignedMemberID == null) ? 0
						: issuerAssignedMemberID.hashCode());
		result = prime
				* result
				+ ((policyMemberBirthDate == null) ? 0 : policyMemberBirthDate
						.hashCode());
		result = prime
				* result
				+ ((policyMemberEligEndDate == null) ? 0
						: policyMemberEligEndDate.hashCode());
		result = prime
				* result
				+ ((policyMemberEligStartDate == null) ? 0
						: policyMemberEligStartDate.hashCode());
		result = prime
				* result
				+ ((policyMemberFirstNm == null) ? 0 : policyMemberFirstNm
						.hashCode());
		result = prime
				* result
				+ ((policyMemberLastNm == null) ? 0 : policyMemberLastNm
						.hashCode());
		result = prime
				* result
				+ ((policyMemberMiddleNm == null) ? 0 : policyMemberMiddleNm
						.hashCode());
		result = prime * result
				+ ((policyMemberSSN == null) ? 0 : policyMemberSSN.hashCode());
		result = prime
				* result
				+ ((policyMemberSalutationNm == null) ? 0
						: policyMemberSalutationNm.hashCode());
		result = prime
				* result
				+ ((policyMemberSuffixNm == null) ? 0 : policyMemberSuffixNm
						.hashCode());
		result = prime * result
				+ ((subscriberInd == null) ? 0 : subscriberInd.hashCode());
		result = prime * result
				+ ((x12GenderTypeCd == null) ? 0 : x12GenderTypeCd.hashCode());
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
		PolicyMemberVersionPO other = (PolicyMemberVersionPO) obj;
		if (exchangeMemberID == null) {
			if (other.exchangeMemberID != null)
				return false;
		} else if (!exchangeMemberID.equals(other.exchangeMemberID))
			return false;
		if (issuerAssignedMemberID == null) {
			if (other.issuerAssignedMemberID != null)
				return false;
		} else if (!issuerAssignedMemberID.equals(other.issuerAssignedMemberID))
			return false;
		if (policyMemberBirthDate == null) {
			if (other.policyMemberBirthDate != null)
				return false;
		} else if (!policyMemberBirthDate.equals(other.policyMemberBirthDate))
			return false;
		if (policyMemberEligEndDate == null) {
			if (other.policyMemberEligEndDate != null)
				return false;
		} else if (!policyMemberEligEndDate
				.equals(other.policyMemberEligEndDate))
			return false;
		if (policyMemberEligStartDate == null) {
			if (other.policyMemberEligStartDate != null)
				return false;
		} else if (!policyMemberEligStartDate
				.equals(other.policyMemberEligStartDate))
			return false;
		if (policyMemberFirstNm == null) {
			if (other.policyMemberFirstNm != null)
				return false;
		} else if (!policyMemberFirstNm.equals(other.policyMemberFirstNm))
			return false;
		if (policyMemberLastNm == null) {
			if (other.policyMemberLastNm != null)
				return false;
		} else if (!policyMemberLastNm.equals(other.policyMemberLastNm))
			return false;
		if (policyMemberMiddleNm == null) {
			if (other.policyMemberMiddleNm != null)
				return false;
		} else if (!policyMemberMiddleNm.equals(other.policyMemberMiddleNm))
			return false;
		if (policyMemberSSN == null) {
			if (other.policyMemberSSN != null)
				return false;
		} else if (!policyMemberSSN.equals(other.policyMemberSSN))
			return false;
		if (policyMemberSalutationNm == null) {
			if (other.policyMemberSalutationNm != null)
				return false;
		} else if (!policyMemberSalutationNm
				.equals(other.policyMemberSalutationNm))
			return false;
		if (policyMemberSuffixNm == null) {
			if (other.policyMemberSuffixNm != null)
				return false;
		} else if (!policyMemberSuffixNm.equals(other.policyMemberSuffixNm))
			return false;
		if (subscriberInd == null) {
			if (other.subscriberInd != null)
				return false;
		} else if (!subscriberInd.equals(other.subscriberInd))
			return false;
		if (x12GenderTypeCd == null) {
			if (other.x12GenderTypeCd != null)
				return false;
		} else if (!x12GenderTypeCd.equals(other.x12GenderTypeCd))
			return false;
		return true;
	}
	


}