package gov.hhs.cms.ff.fm.eps.ep.enums;

/**
 * The Enum SBMTobaccoUseCodeEnum.
 */
public enum SBMTobaccoUseCodeEnum {
	
	T ("Use"),	
	N ("No Use"),	
	U ("Unknown")
	;

	String code;

	private SBMTobaccoUseCodeEnum (String code) {
		this.code = code;		
	}
	
	/**
	 * @param code
	 * @return
	 */
	public static SBMTobaccoUseCodeEnum getEnum(String code) {
        for (SBMTobaccoUseCodeEnum type: SBMTobaccoUseCodeEnum.values()) {
            if (type.name().equals(code)) {
                return type;
            }
        }
        return null;
    }
	
	/**
	 * 
	 * @return
	 */
	public String getCode() {
		return code;
	}
		
}
