package gov.hhs.cms.ff.fm.eps.ep.po;

public class SbmPolicyVersionPO extends PolicyVersionPO {
	
	// Excluded from Hashcode and Equals (policy compare)
	private Long sbmTransMsgId;
	
	// Attributes included in Hashcode and Equals.
	private String sourceExchangeId;

	/**
	 * @return the sbmTransMsgId
	 */
	public Long getSbmTransMsgId() {
		return sbmTransMsgId;
	}
	/**
	 * @param sbmTransMsgId the sbmTransMsgId to set
	 */
	public void setSbmTransMsgId(Long sbmTransMsgId) {
		this.sbmTransMsgId = sbmTransMsgId;
	}
	/**
	 * @return the sourceExchangeId
	 */
	public String getSourceExchangeId() {
		return sourceExchangeId;
	}
	/**
	 * @param sourceExchangeId the sourceExchangeId to set
	 */
	public void setSourceExchangeId(String sourceExchangeId) {
		this.sourceExchangeId = sourceExchangeId;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((sourceExchangeId == null) ? 0 : sourceExchangeId.hashCode());
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
		SbmPolicyVersionPO other = (SbmPolicyVersionPO) obj;
		if (sourceExchangeId == null) {
			if (other.sourceExchangeId != null)
				return false;
		} else if (!sourceExchangeId.equals(other.sourceExchangeId))
			return false;
		return true;
	}
	
	
	
	
}
