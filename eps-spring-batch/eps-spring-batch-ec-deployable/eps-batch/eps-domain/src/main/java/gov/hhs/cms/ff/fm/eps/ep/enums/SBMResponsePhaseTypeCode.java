package gov.hhs.cms.ff.fm.eps.ep.enums;

import com.accenture.foundation.common.exception.ApplicationException;

public enum SBMResponsePhaseTypeCode {

	INITIAL	    ("I"),
	FINAL	    ("F"),
	STATUS		("S")
	;
	
	private String value;
	
	
	private SBMResponsePhaseTypeCode(String value) {
		this.value = value;
	}

	/**
	 * @param value
	 * @return
	 */
	public static SBMResponsePhaseTypeCode getEnum(String value) {
		
        for (SBMResponsePhaseTypeCode status: SBMResponsePhaseTypeCode.values()) {
            if (status.value.equals(value)) {
                return status;
            }
        }
        throw new ApplicationException("Invalid SBMFileStatusType: " + value);
    }
	
	/**
	 * @return the value
	 */
	public String getValue() {
		return value;
	}
	
	
}
