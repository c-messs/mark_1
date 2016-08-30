/**
 * 
 */
package gov.hhs.cms.ff.fm.eps.ep.sbm;

import com.accenture.foundation.common.exception.ApplicationException;

/**
 * @author girish.padmanabhan
 *
 */
public enum SbmMetalLevelType {
	
	PLATINUM ("1", "Platinum"),
	GOLD ("2", "Gold"),
	SILVER ("3", "Silver"),
	BRONZE ("4", "Bronze"),
	CATASTROPHIC ("5", "Catastrophic"),
	HIGH ("6", "High"),
	LOW ("7", "Low")
	;	
	private String value;
	private String description;
	
	private SbmMetalLevelType(String value, String description) {
		this.value = value;
		this.description = description;
	}

    /**
     * @param value
     * @return
     */
    public static SbmMetalLevelType getEnum(String value) {
        for (SbmMetalLevelType type: SbmMetalLevelType.values()) {
            if (type.value.equals(value)) {
                return type;
            }
        }
        throw new ApplicationException("Invalid Sbm Metal Level Type: "+value);
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
