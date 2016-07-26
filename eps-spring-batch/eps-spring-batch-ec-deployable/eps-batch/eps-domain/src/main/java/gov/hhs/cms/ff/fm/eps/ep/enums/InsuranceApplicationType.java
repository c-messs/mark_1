/**
 * 
 */
package gov.hhs.cms.ff.fm.eps.ep.enums;

import com.accenture.foundation.common.exception.ApplicationException;

/**
 * @author girish.padmanabhan
 *
 */
public enum InsuranceApplicationType {
	INDIVIDUAL	("1", "Individual Application"),
	SHOP_EMPLOYEE("2", "SHOP Employee Application"),
	EXEMPTION("3", "Exemption Application"),
	SHOP("4", "SHOP Employer Application")
	;	
	private String value;
	private String description;
	
	private InsuranceApplicationType(String value, String description) {
		this.value = value;
		this.description = description;
	}

    /**
     * @param value
     * @return
     */
    public static InsuranceApplicationType getEnum(String value) {
        for (InsuranceApplicationType type: InsuranceApplicationType.values()) {
            if (type.value.equals(value)) {
                return type;
            }
        }
        throw new ApplicationException("Invalid Address Type: "+value);
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
