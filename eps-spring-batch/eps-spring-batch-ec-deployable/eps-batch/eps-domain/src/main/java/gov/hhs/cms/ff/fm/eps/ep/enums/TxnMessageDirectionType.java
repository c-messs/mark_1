/**
 * 
 */
package gov.hhs.cms.ff.fm.eps.ep.enums;

import com.accenture.foundation.common.exception.ApplicationException;

/**
 * @author girish.padmanabhan
 *
 */
public enum TxnMessageDirectionType {
	INBOUND	("1", "Inbound"),
	OUTBOUND ("2", "Outbound")
	;	
	private String value;
	private String description;
	
	private TxnMessageDirectionType(String value, String description) {
		this.value = value;
		this.description = description;
	}

    /**
     * @param value
     * @return
     */
    public static TxnMessageDirectionType getEnum(String value) {
        for (TxnMessageDirectionType type: TxnMessageDirectionType.values()) {
            if (type.value.equals(value)) {
                return type;
            }
        }
        throw new ApplicationException("Invalid Txn Message Direction Type: "+value);
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
