package gov.hhs.cms.ff.fm.eps.ep.enums;

/**
 * @author j.radziewski
 * 
 * Type used to distinguish between Proration Types.
 */
public enum ProrationType {
	
	NON_PRORATING ("0"),
	FFM_PRORATING ("1"),
	SBM_PRORATING ("2")
	;	
	private String value;
	
	private ProrationType(String value) {
		this.value = value;
	}

	/**
	 * @param value
	 * @return
	 */
	public static ProrationType getEnum(String value) {
        for (ProrationType type: ProrationType.values()) {
            if (type.value.equals(value)) {
                return type;
            }
        }
        return null;
    }
	
	/**
	 * @return the value
	 */
	public String getValue() {
		return value;
	}

}
