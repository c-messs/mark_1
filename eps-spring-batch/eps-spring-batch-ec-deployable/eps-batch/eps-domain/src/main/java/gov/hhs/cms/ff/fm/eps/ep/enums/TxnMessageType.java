/**
 * 
 */
package gov.hhs.cms.ff.fm.eps.ep.enums;

import com.accenture.foundation.common.exception.ApplicationException;

/**
 * @author girish.padmanabhan
 *
 */
public enum TxnMessageType {
	MSG_834	("1", "834"),
	MSG_BAA ("2", "BAA"),
	MSG_999 ("3", "999"),
	MEDICAID_CHIP ("4", "MedicaidCHIP"),
	// TODO: update from 3 to correct value once schema ref data is added.
	// Temporarily set to 3 for unit testing.
	MSG_SBMI ("3", "SBMI")
	;	
	private String value;
	private String description;
	
	private TxnMessageType(String value, String description) {
		this.value = value;
		this.description = description;
	}

    /**
     * @param value
     * @return
     */
    public static TxnMessageType getEnum(String value) {
        for (TxnMessageType type: TxnMessageType.values()) {
            if (type.value.equals(value)) {
                return type;
            }
        }
        throw new ApplicationException("Invalid Txn Message Type: "+value);
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
