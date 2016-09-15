package gov.hhs.cms.ff.fm.eps.ep.enums;

/**
 * The Enum SchemaValidationRuleEnum.
 */
public enum SBMSchemaValidationRuleEnum {
	
	/** The CV c_ typ e_313. */
	CVC_TYPE_313 ("cvc-type.3.1.3"),
	
	/** The CV c_ comple x_ typ e_24 a. */
	CVC_COMPLEX_TYPE_24A ("cvc-complex-type.2.4.a"),
	
	/** The CV c_ comple x_ typ e_24 b. */
	CVC_COMPLEX_TYPE_24B ("cvc-complex-type.2.4.b"),
	
	/** The CV c_ comple x_ typ e_24 b. */
	CVC_COMPLEX_TYPE_24D ("cvc-complex-type.2.4.d"),
	
	/** The cvc maxlength valid. */
	CVC_MAXLENGTH_VALID ("cvc-maxLength-valid"),
	
	/** The cvc pattern valid. */
	CVC_PATTERN_VALID ("cvc-pattern-valid"),
	
	/** The cvc maxinclusive valid. */
	CVC_MAXINCLUSIVE_VALID ("cvc-maxInclusive-valid"),
			
	/** The cvc length valid. */
	CVC_LENGTH_VALID ("cvc-length-valid");
	

	/** The value. */
	String value;
		
	/**
	 * Instantiates a new schema validation rule enum.
	 *
	 * @param value the value
	 * @param isTwiceErrorRule the is twice error rule
	 * @param errorCode the error code
	 */
	private SBMSchemaValidationRuleEnum(String value) {

		this.value = value;
		
	}

	/**
	 * Find rule.
	 *
	 * @param value the value
	 * @return the schema validation rule enum
	 */
	public static SBMSchemaValidationRuleEnum findRule(String value) {

		SBMSchemaValidationRuleEnum rule = null;

		for(SBMSchemaValidationRuleEnum e : values()) {

			if(e.value.equals(value)) {

				rule = e;
				break;
			}
		}
		return rule;
	}

	
	/**
	 * Gets the value.
	 *
	 * @return the value
	 */
	public String getValue() {
		return value;
	}


}
