package gov.hhs.cms.ff.fm.eps.ep.enums;

import com.accenture.foundation.common.exception.ApplicationException;

/**
 * @author j.radziewski
 * 
 * Type used for ErrorWarningLogDTO to distinguish between error or warning.
 */
public enum ErrorWarningType {
	
	ERROR ("E", "Error"),
	WARNING ("W", "Warning")
	;	
	private String value;
	private String description;
	
	private ErrorWarningType(String value, String description) {
		this.value = value;
		this.description = description;
	}

	/**
	 * @param value
	 * @return
	 */
	public static ErrorWarningType getEnum(String value) {
        for (ErrorWarningType type: ErrorWarningType.values()) {
            if (type.value.equals(value)) {
                return type;
            }
        }
        throw new ApplicationException("Invalid ErrorWarningType: " + value);
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
