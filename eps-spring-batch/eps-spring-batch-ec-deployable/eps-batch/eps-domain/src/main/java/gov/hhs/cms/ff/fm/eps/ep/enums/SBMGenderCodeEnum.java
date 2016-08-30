package gov.hhs.cms.ff.fm.eps.ep.enums;

public enum SBMGenderCodeEnum {
	
	F ("Female"),	
	M ("Male"),	
	U ("Unknown")
	;

	String code;

	private SBMGenderCodeEnum (String code) {
		this.code = code;		
	}
	
	/**
	 * @param value
	 * @return
	 */
	public static SBMGenderCodeEnum getEnum(String code) {
        for (SBMGenderCodeEnum type: SBMGenderCodeEnum.values()) {
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
