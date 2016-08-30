package gov.hhs.cms.ff.fm.eps.ep.sbm;

import java.util.ArrayList;
import java.util.List;

/**
 * @author eps
 *
 */
public class SbmErrWarningLogDTO {
	
	private String elementInError;
	private String errorWarningTypeCd;
	private List<String> errorWarningDesc;
	private String exchangeMemberId;
	
	/**
	 * Constructor
	 */
	public SbmErrWarningLogDTO() {
		super();
	}
	
	
	/**
	 * Constructor
	 * 
	 * @param policyDTO
	 */
	public SbmErrWarningLogDTO(SBMPolicyDTO policyDTO) {
		super();
	}

	/**
	 * @return the elementInError
	 */
	public String getElementInError() {
		return elementInError;
	}

	/**
	 * @param elementInError the elementInError to set
	 */
	public void setElementInError(String elementInError) {
		this.elementInError = elementInError;
	}

	/**
	 * @return the errorWarningTypeCd
	 */
	public String getErrorWarningTypeCd() {
		return errorWarningTypeCd;
	}

	/**
	 * @param errorWarningTypeCd the errorWarningTypeCd to set
	 */
	public void setErrorWarningTypeCd(String errorWarningTypeCd) {
		this.errorWarningTypeCd = errorWarningTypeCd;
	}

	/**
	 * @return the errorWarningDesc
	 */
	public List<String> getErrorWarningDesc() {
		if (errorWarningDesc == null) {
			errorWarningDesc = new ArrayList<String>();
		}
		return errorWarningDesc;
	}

	/**
	 * @param errorWarningDesc the errorWarningDesc to set
	 */
	public void setErrorWarningDesc(List<String> errorWarningDesc) {
		this.errorWarningDesc = errorWarningDesc;
	}


	/**
	 * @return the exchangeMemberid
	 */
	public String getExchangeMemberId() {
		return exchangeMemberId;
	}


	/**
	 * @param exchangeMemberid the exchangeMemberid to set
	 */
	public void setExchangeMemberId(String exchangeMemberId) {
		this.exchangeMemberId = exchangeMemberId;
	}


	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "SbmErrorWarningLogDTO [\n\terrorWarningDesc="
				+ errorWarningDesc + "\n\telementInError="
				+ elementInError + "\n\terrorWarningTypeCd=" + errorWarningTypeCd + "]";
	}

}
