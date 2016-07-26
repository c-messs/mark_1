/**
 * 
 */
package gov.hhs.cms.ff.fm.eps.ep.enums;

import com.accenture.foundation.common.exception.ApplicationException;

/**
 * @author girish.padmanabhan
 *
 */
public enum ExchangeType {
	FFM	("FFM", "FFM"),
	SBM ("SBM", "SBM"),
	SBM_EO ("SBM-EO", "SBM-EO"),
	FFM_ISSUER ("FFM-ISSUER", "FFM Issuer"),
	DUUQ ("DUUQ", "DUUQ")
	;	
	private String value;
	private String description;
	
	private ExchangeType(String value, String description) {
		this.value = value;
		this.description = description;
	}

    /**
     * @param value
     * @return
     */
    public static ExchangeType getEnum(String value) {
        for (ExchangeType type: ExchangeType.values()) {
            if (type.value.equals(value)) {
                return type;
            }
        }
        throw new ApplicationException("Invalid Exchange Type: "+value);
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
