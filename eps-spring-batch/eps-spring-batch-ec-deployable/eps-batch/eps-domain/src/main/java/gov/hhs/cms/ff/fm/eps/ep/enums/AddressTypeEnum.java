/**
 * 
 */
package gov.hhs.cms.ff.fm.eps.ep.enums;

import com.accenture.foundation.common.exception.ApplicationException;

/**
 * @author girish.padmanabhan
 *
 */
public enum AddressTypeEnum {
	RESIDENTIAL	("1", "Residential");	
	private String value;
	private String description;
	
	private AddressTypeEnum(String value, String description) {
		this.value = value;
		this.description = description;
	}

    /**
     * @param value
     * @return
     */
    public static AddressTypeEnum getEnum(String value) {
        for (AddressTypeEnum type: AddressTypeEnum.values()) {
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
