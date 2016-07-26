package gov.hhs.cms.ff.fm.eps.ep.enums;

import com.accenture.foundation.common.exception.ApplicationException;

/**
 * @author j.radziewski
 * 
 * One to one mappying for EE InsurancePlanPolicy status to EPS PolicyStatus
 *
 */
public enum PolicyStatus {

	INITIAL_1	("1", "Initial Enrollment"),
	EFFECTUATED_2 ("2", "Effectuated"),
	CANCELLED_3 ("3", "Cancelled"),
	TERMINATED_4 ("4", "Terminated"),
	SUPERSEDED_5 ("5", "Superseded")
	;	
	
	private String value;
	private String description;
	
	private PolicyStatus(String value, String description) {
		this.value = value;
		this.description = description;
	}

    /**
     * @param value
     * @return
     */
    public static PolicyStatus getEnum(String value) {
        for (PolicyStatus status: PolicyStatus.values()) {
            if (status.value.equals(value)) {
                return status;
            }
        }
        throw new ApplicationException(EProdEnum.EPROD_36.getLogMsg() + ": " + value, EProdEnum.EPROD_36.getCode());
    }
    
	/**
	 * @return the value
	 */
	public String getValue() {
		return value;
	}
    
	/**
	 * @return the description
	 */
	public String getDescription() {
		return description;
	}
}

